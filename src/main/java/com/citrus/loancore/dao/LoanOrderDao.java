package com.citrus.loancore.dao;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Component;

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
        loanOrder.setLoanState(LoanStateEnum.PENDING);
        loanOrder.setCreatedAt(now);
        loanOrder.setUpdatedAt(now);
        loanOrder.setStateChangedAt(now);
        loanOrder.setApplicationDate(LocalDate.now(ZoneId.of("Asia/Kolkata")));
        return loanOrderRepository.save(loanOrder);
    }

    public LoanOrder update(LoanOrder loanOrder, LoanStateEnum loanState) {
        loanOrder.setLoanState(loanState);
        loanOrder.setStateChangedAt(Instant.now());
        loanOrder.setUpdatedAt(Instant.now());
        return loanOrderRepository.save(loanOrder);
    }

    public LoanOrder findById(String loanOrderId) {
        return loanOrderRepository.findById(loanOrderId)
                .orElseThrow(() -> new RuntimeException("Loan Order not found"));
    }

    public Optional<LoanOrder> findByUserId(String userId) {
        return loanOrderRepository.findByUserId(userId);
    }

}
