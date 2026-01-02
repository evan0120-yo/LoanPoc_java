package com.citrus.origin.usecase.store;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.origin.event.OriginEvent;
import com.citrus.origin.model.Blacklist;
import com.citrus.origin.object.req.BlacklistDelReq;
import com.citrus.origin.object.req.BlacklistSaveReq;
import com.citrus.origin.object.req.LoanApplyReq;
import com.citrus.origin.service.query.BlacklistQueryService;
import com.citrus.origin.service.store.BlacklistStoreService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OriginStoreUsecase {

    private final BlacklistStoreService blacklistStoreService;
    private final BlacklistQueryService blacklistQueryService;
    private final OriginEvent originEvent;

    public void loanApply(LoanApplyReq req) {
        // 1. check blacklist
        List<Blacklist> blacklistList = blacklistQueryService.findUserInExist(req.getUserId());
        if (!blacklistList.isEmpty()) {
            throw new RuntimeException("User is in blacklist");
        }
        // 2. loan apply event → 寫入 Outbox
        originEvent.loanApplyEvent(
                req.getUserId(),
                req.getMobile(),
                req.getPanNumber(),
                req.getName(),
                req.getAppliedAmount(),
                req.getBankAccount(),
                req.getIfscCode(),
                req.getBankName());
    }

    public Blacklist saveBlacklist(BlacklistSaveReq req) {
        // 1. find exist
        List<Blacklist> blacklistList = blacklistQueryService.findUserInExist(req.getUserId());
        if (!blacklistList.isEmpty()) {
            throw new RuntimeException("Blacklist already exists");
        }
        // 2. save
        return blacklistStoreService.save(req.getUserId(), req.getIdentifier(), req.getIdentifierValue(),
                req.getReason(), req.getAddedBy());
    }

    public void deleteBlacklist(BlacklistDelReq req) {
        // 1. find
        List<Blacklist> blacklistList = blacklistQueryService.findUserInExist(req.getUserId());
        blacklistList.stream().forEach(blacklist -> {
            // 2. delete
            blacklistStoreService.delete(blacklist, req.getDeletedBy());
        });
    }
}
