package com.citrus.loancore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citrus.loancore.model.LoanOrder;

public interface LoanOrderRepository extends JpaRepository<LoanOrder, String> {
    List<LoanOrder> findByUserId(String userId);
}
