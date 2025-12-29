package com.citrus.loancore.service.store;

import org.springframework.stereotype.Service;

import com.citrus.loancore.dao.LoanOrderDao;
import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.model.LoanOrder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderStoreService {

    private final LoanOrderDao loanOrderDao;

    public LoanOrder save(LoanOrder loanOrder) {
        return loanOrderDao.save(loanOrder);
    }

    public LoanOrder updateState(LoanOrder loanOrder, LoanStateEnum loanState) {
        return loanOrderDao.update(loanOrder, loanState);
    }
}
