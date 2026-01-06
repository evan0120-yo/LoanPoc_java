package com.citrus.loancore.service.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.loancore.dao.LoanOrderDao;
import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderQueryService {

    private final LoanOrderDao loanOrderDao;

    public LoanOrder findById(String loanOrderId) {
        return loanOrderDao.findById(loanOrderId);
    }

    public List<LoanOrder> findByUserId(String userId) {
        return loanOrderDao.findByUserId(userId);
    }

    public List<LoanOrder> findByLoanState(LoanStateEnum loanState) {
        return loanOrderDao.findByLoanState(loanState);
    }

    /**
     * 認領 PENDING 訂單（使用 FOR UPDATE SKIP LOCKED）
     */
    public List<LoanOrder> claimPendingOrders(int limit) {
        return loanOrderDao.claimPendingOrders(limit);
    }

}
