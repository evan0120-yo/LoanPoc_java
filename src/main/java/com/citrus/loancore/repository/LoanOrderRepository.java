package com.citrus.loancore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;

public interface LoanOrderRepository extends JpaRepository<LoanOrder, String> {
    List<LoanOrder> findByUserId(String userId);

    /**
     * 查詢指定狀態的訂單列表
     * 用於 loancore 自己查詢 PENDING 訂單
     */
    List<LoanOrder> findByLoanState(LoanStateEnum loanState);
}
