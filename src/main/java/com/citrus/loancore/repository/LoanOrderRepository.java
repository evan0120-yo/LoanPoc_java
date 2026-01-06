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
     * 查詢指定狀態的訂單列表
     * 用於 loancore 自己查詢 PENDING 訂單
     */
    List<LoanOrder> findByLoanState(LoanStateEnum loanState);

    /**
     * 認領 PENDING 訂單（使用 FOR UPDATE SKIP LOCKED 避免多台 Server 重複處理）
     * - FOR UPDATE: 鎖定查到的 row
     * - SKIP LOCKED: 跳過已被其他 Transaction 鎖定的 row
     */
    @Query(value = """
            SELECT * FROM loan_order
            WHERE loan_state = 'PENDING'
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """, nativeQuery = true)
    List<LoanOrder> claimPendingOrders(@Param("limit") int limit);

}
