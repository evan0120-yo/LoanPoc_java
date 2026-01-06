package com.citrus.loancore.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Component;
import com.fasterxml.uuid.Generators;
import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.repository.LoanOrderRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanOrderDao {

    private final LoanOrderRepository loanOrderRepository;

    public LoanOrder save(LoanOrder loanOrder) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        loanOrder.setLoanOrderId(Generators.timeBasedGenerator().generate().toString());
        loanOrder.setLoanState(LoanStateEnum.PENDING);
        loanOrder.setCreatedAt(now);
        loanOrder.setUpdatedAt(now);
        loanOrder.setStateChangedAt(now);
        loanOrder.setApplicationDate(today);
        return loanOrderRepository.save(loanOrder);
    }

    public LoanOrder update(LoanOrder loanOrder, LoanStateEnum loanState) {
        Instant now = Instant.now();
        loanOrder.setLoanState(loanState);
        loanOrder.setStateChangedAt(now);
        loanOrder.setUpdatedAt(now);
        return loanOrderRepository.save(loanOrder);
    }

    public LoanOrder findById(String loanOrderId) {
        return loanOrderRepository.findById(loanOrderId)
                .orElseThrow(() -> new RuntimeException("Loan Order not found"));
    }

    public List<LoanOrder> findByUserId(String userId) {
        return loanOrderRepository.findByUserId(userId);
    }

    public List<LoanOrder> findByLoanState(LoanStateEnum loanState) {
        return loanOrderRepository.findByLoanState(loanState);
    }

    /**
     * 認領 PENDING 訂單（使用 FOR UPDATE SKIP LOCKED）
     */
    public List<LoanOrder> claimPendingOrders(int limit) {
        return loanOrderRepository.claimPendingOrders(limit);
    }
}
