package com.citrus.loancore.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.object.req.LoanOrderInitReq;
import com.citrus.loancore.usecase.store.LoanOrderStoreUsecase;

import lombok.RequiredArgsConstructor;

@CrossOrigin(value = "*")
@RestController
@RequestMapping(value = "/loancore")
@RequiredArgsConstructor
public class LoanOrderController {

    private final LoanOrderStoreUsecase loanOrderStoreUsecase;

    @PostMapping(value = "/order/init")
    public LoanOrder initLoanOrder(@RequestBody LoanOrderInitReq req) {
        return loanOrderStoreUsecase.save(req);
    }
}
