package com.citrus.loancore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;

public interface LoanOrderRepository extends JpaRepository<LoanOrder, String> {
    List<LoanOrder> findByUserId(String userId);

    /**
     * 查詢指定狀態的訂單 ID 列表
     * 用於 loancron 排程查詢 PENDING 訂單
     */
    @Query("SELECT o.orderId FROM LoanOrder o WHERE o.state = :status")
    List<String> findOrderIdsByStatus(@Param("status") LoanStateEnum status);
}
