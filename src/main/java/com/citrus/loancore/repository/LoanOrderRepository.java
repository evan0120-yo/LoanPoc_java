package com.citrus.loancore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citrus.loancore.model.LoanOrder;

public interface LoanOrderRepository extends JpaRepository<LoanOrder, String> {
    Optional<LoanOrder> findByUserId(String userId);
}
