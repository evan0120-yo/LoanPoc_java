package com.citrus.loancore.service.store;

import org.springframework.stereotype.Service;

import com.citrus.loancore.dao.LoanOrderDao;
import com.citrus.loancore.dao.LoanOrderHistoryDao;
import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.model.LoanOrderHistory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderStoreService {

    private final LoanOrderDao loanOrderDao;
    private final LoanOrderHistoryDao loanOrderHistoryDao;

    public LoanOrder saveLoanOrder(LoanOrder loanOrder) {
        return loanOrderDao.save(loanOrder);
    }

    public void saveLoanOrderHistory(LoanOrderHistory loanOrderHistory) {
        loanOrderHistoryDao.save(loanOrderHistory);
    }

    public LoanOrder updateState(LoanOrder loanOrder, LoanStateEnum loanState) {
        return loanOrderDao.update(loanOrder, loanState);
    }
}
