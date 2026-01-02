package com.citrus.origin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.citrus.origin.object.req.BlacklistDelReq;
import com.citrus.origin.object.req.BlacklistFindUserReq;
import com.citrus.origin.object.req.BlacklistSaveReq;
import com.citrus.origin.object.req.LoanApplyReq;
import com.citrus.origin.usecase.query.OriginQueryUsecase;
import com.citrus.origin.usecase.store.OriginStoreUsecase;

import lombok.RequiredArgsConstructor;

@CrossOrigin(value = "*")
@RestController
@RequestMapping(value = "/origin")
@RequiredArgsConstructor
public class OriginController {

    private final OriginStoreUsecase originStoreUsecase;
    private final OriginQueryUsecase originQueryUsecase;

    @PostMapping(value = "/blacklist/save")
    public ResponseEntity<?> saveBlacklist(@RequestBody BlacklistSaveReq req) {
        return ResponseEntity.ok(originStoreUsecase.saveBlacklist(req));
    }

    @PostMapping(value = "/blacklist/delete")
    public ResponseEntity<?> deleteBlacklist(@RequestBody BlacklistDelReq req) {
        originStoreUsecase.deleteBlacklist(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/blacklist/findUser")
    public ResponseEntity<?> findUserBlacklist(@RequestBody BlacklistFindUserReq req) {
        return ResponseEntity.ok(originQueryUsecase.findUser(req));
    }

    @PostMapping(value = "/loanApply")
    public ResponseEntity<?> loanApply(@RequestBody LoanApplyReq req) {
        originStoreUsecase.loanApply(req);
        return ResponseEntity.ok().build();
    }

}
