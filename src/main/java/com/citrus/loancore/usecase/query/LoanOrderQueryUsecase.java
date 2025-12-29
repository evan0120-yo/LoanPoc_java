package com.citrus.loancore.usecase.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.object.req.LoanOrderGetAllReq;
import com.citrus.loancore.object.req.LoanOrderGetByIdReq;
import com.citrus.loancore.service.query.LoanOrderQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderQueryUsecase {

    private final LoanOrderQueryService loanOrderQueryService;

    public List<LoanOrder> findByUserId(LoanOrderGetAllReq req) {
        return loanOrderQueryService.findByUserId(req.getUserId());
    }

    public LoanOrder findById(LoanOrderGetByIdReq req) {
        return loanOrderQueryService.findById(req.getLoanOrderId());
    }
}
