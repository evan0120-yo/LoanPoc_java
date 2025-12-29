package com.citrus.loancore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citrus.loancore.model.LoanOrder;
import com.citrus.loancore.object.req.LoanOrderGetAllReq;
import com.citrus.loancore.object.req.LoanOrderGetByIdReq;
import com.citrus.loancore.object.req.LoanOrderInitReq;
import com.citrus.loancore.object.req.LoanOrderUpdateReq;
import com.citrus.loancore.usecase.query.LoanOrderQueryUsecase;
import com.citrus.loancore.usecase.store.LoanOrderStoreUsecase;

import lombok.RequiredArgsConstructor;

@CrossOrigin(value = "*")
@RestController
@RequestMapping(value = "/loancore/order")
@RequiredArgsConstructor
public class LoanOrderController {

    private final LoanOrderStoreUsecase loanOrderStoreUsecase;
    private final LoanOrderQueryUsecase loanOrderQueryUsecase;

    @PostMapping(value = "/init")
    public LoanOrder initLoanOrder(@RequestBody LoanOrderInitReq req) {
        return loanOrderStoreUsecase.save(req);
    }

    @PostMapping(value = "/updateState")
    public LoanOrder updateLoanOrderState(@RequestBody LoanOrderUpdateReq req) {
        return loanOrderStoreUsecase.updateState(req);
    }

    @PostMapping(value = "/all")
    public List<LoanOrder> findAll(@RequestBody LoanOrderGetAllReq req) {
        return loanOrderQueryUsecase.findByUserId(req);
    }

    @PostMapping(value = "/findById")
    public LoanOrder findById(@RequestBody LoanOrderGetByIdReq req) {
        return loanOrderQueryUsecase.findById(req);
    }

}
