package com.citrus.loancore.controller;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.object.req.LoanApplyReq;
import com.citrus.loancore.object.resp.LoanRecordResp;
import com.citrus.loancore.usecase.query.LoanQueryUseCase;
import com.citrus.loancore.usecase.store.LoanApplyUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanApplyUseCase loanApplyUseCase;
    private final LoanQueryUseCase loanQueryUseCase;

    /**
     * 申請貸款
     */
    @PostMapping("/apply")
    public ResponseEntity<LoanRecordResp> apply(@Valid @RequestBody LoanApplyReq req) {
        LoanRecord loan = loanApplyUseCase.execute(req);
        return ResponseEntity.ok(loanQueryUseCase.findById(loan.getId()));
    }

    /**
     * 查詢貸款 by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanRecordResp> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loanQueryUseCase.findById(id));
    }

    /**
     * 查詢貸款 by userId
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LoanRecordResp> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(loanQueryUseCase.findByUserId(userId));
    }
}
