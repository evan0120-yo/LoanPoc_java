package com.citrus.loancore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citrus.loancore.model.LoanOrderHistory;

public interface LoanOrderHistoryRepository extends JpaRepository<LoanOrderHistory, String> {

}
