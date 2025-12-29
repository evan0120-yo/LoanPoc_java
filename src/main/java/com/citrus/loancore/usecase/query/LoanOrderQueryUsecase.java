package com.citrus.loancore.usecase.query;

import org.springframework.stereotype.Service;

import com.citrus.loancore.service.query.LoanOrderQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanOrderQueryUsecase {

    private final LoanOrderQueryService loanOrderQueryService;

}
