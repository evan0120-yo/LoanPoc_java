package com.citrus.loancore.dao;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.citrus.loancore.model.LoanOrderHistory;
import com.citrus.loancore.repository.LoanOrderHistoryRepository;
import com.fasterxml.uuid.Generators;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanOrderHistoryDao {

    private final LoanOrderHistoryRepository loanOrderHistoryRepository;

    public void save(LoanOrderHistory loanOrderHistory) {
        Instant now = Instant.now();
        loanOrderHistory.setLoanOrderHistoryId(Generators.timeBasedGenerator().generate().toString());
        loanOrderHistory.setCreatedAt(now);
        loanOrderHistoryRepository.save(loanOrderHistory);
    }
}
