package com.citrus.origin.usecase.store;

import com.citrus.bureau.object.bo.BureauResultBo;
import com.citrus.origin.model.OriginRecord;
import com.citrus.origin.model.OriginRecord.Decision;
import com.citrus.origin.model.OriginRecord.RiskGrade;
import com.citrus.origin.object.bo.UnderwritingBo;
import com.citrus.origin.service.guard.OriginGuard;
import com.citrus.origin.service.store.OriginStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 審核 UseCase
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnderwritingUseCase {

    private final OriginStoreService originStoreService;
    private final OriginGuard originGuard;

    /**
     * 執行審核流程
     */
    @Transactional
    public UnderwritingBo execute(Long loanRecordId, String panNumber,
            BureauResultBo bureauResult, BigDecimal appliedAmount) {
        log.info("Starting underwriting: loanId={}, pan={}", loanRecordId, panNumber);

        // 1. 建立審核記錄
        OriginRecord record = originStoreService.create(loanRecordId, panNumber);

        UnderwritingBo.UnderwritingBoBuilder resultBuilder = UnderwritingBo.builder()
                .loanRecordId(loanRecordId)
                .panNumber(panNumber)
                .cibilScore(bureauResult.getCibilScore())
                .foir(bureauResult.getFoir())
                .appliedAmount(appliedAmount);

        // 2. Dedupe 檢查
        boolean dedupePass = performDedupe(record, panNumber);
        resultBuilder.dedupePass(dedupePass);
        if (!dedupePass) {
            return buildRejection(record, resultBuilder, "Dedupe failed");
        }

        // 3. 黑名單檢查
        boolean blacklistHit = performBlacklistCheck(record, panNumber);
        resultBuilder.blacklistHit(blacklistHit);
        if (blacklistHit) {
            return buildRejection(record, resultBuilder, "Blacklisted user");
        }

        // 4. 風險評估
        int riskScore = calculateRiskScore(bureauResult);
        RiskGrade grade = determineGrade(riskScore);
        originStoreService.updateRiskScore(record, riskScore, grade);
        resultBuilder.riskScore(riskScore).riskGrade(grade);

        // 5. 決策
        if (!originGuard.checkFoirPass(bureauResult.getFoir())) {
            return buildLspRouting(record, resultBuilder, "FOIR too high");
        }

        if (bureauResult.getCibilScore() < 650) {
            return buildLspRouting(record, resultBuilder, "Low CIBIL score");
        }

        // 6. 定價 (自肥)
        BigDecimal approvedLimit = calculateLimit(appliedAmount, grade);
        BigDecimal roi = calculateRoi(grade);
        Integer tenure = 12; // 固定 12 個月

        originStoreService.updatePricing(record, approvedLimit, roi, tenure);
        originStoreService.finalize(record, Decision.APPROVED, null);

        log.info("Underwriting approved: loanId={}, limit={}, roi={}",
                loanRecordId, approvedLimit, roi);

        return resultBuilder
                .decision(Decision.APPROVED)
                .approvedLimit(approvedLimit)
                .roi(roi)
                .tenure(tenure)
                .build();
    }

    private boolean performDedupe(OriginRecord record, String panNumber) {
        // TODO: 實際 Dedupe 邏輯
        boolean pass = true;
        originStoreService.updateDedupeResult(record, pass, null);
        return pass;
    }

    private boolean performBlacklistCheck(OriginRecord record, String panNumber) {
        // TODO: 實際黑名單檢查
        boolean hit = false;
        originStoreService.updateBlacklistResult(record, hit, null);
        return hit;
    }

    private int calculateRiskScore(BureauResultBo bureau) {
        // 簡化的風險評分
        int score = 0;

        if (bureau.getCibilScore() != null) {
            score += Math.min(bureau.getCibilScore() / 10, 80);
        }

        if (bureau.getFoir() != null && bureau.getFoir().doubleValue() < 0.4) {
            score += 20;
        }

        return score;
    }

    private RiskGrade determineGrade(int score) {
        if (score >= 90)
            return RiskGrade.A;
        if (score >= 75)
            return RiskGrade.B;
        if (score >= 60)
            return RiskGrade.C;
        if (score >= 45)
            return RiskGrade.D;
        return RiskGrade.E;
    }

    private BigDecimal calculateLimit(BigDecimal applied, RiskGrade grade) {
        double multiplier = switch (grade) {
            case A -> 1.0;
            case B -> 0.9;
            case C -> 0.7;
            case D -> 0.5;
            case E -> 0.3;
        };
        return applied.multiply(BigDecimal.valueOf(multiplier));
    }

    private BigDecimal calculateRoi(RiskGrade grade) {
        return switch (grade) {
            case A -> new BigDecimal("18.00");
            case B -> new BigDecimal("21.00");
            case C -> new BigDecimal("24.00");
            case D -> new BigDecimal("27.00");
            case E -> new BigDecimal("30.00");
        };
    }

    private UnderwritingBo buildRejection(OriginRecord record,
            UnderwritingBo.UnderwritingBoBuilder builder,
            String reason) {
        originStoreService.finalize(record, Decision.REJECTED, reason);
        return builder.decision(Decision.REJECTED).rejectReason(reason).build();
    }

    private UnderwritingBo buildLspRouting(OriginRecord record,
            UnderwritingBo.UnderwritingBoBuilder builder,
            String reason) {
        originStoreService.finalize(record, Decision.LSP, reason);
        return builder.decision(Decision.LSP).rejectReason(reason).build();
    }
}
