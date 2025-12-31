package com.citrus.origin.usecase.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.origin.model.Blacklist;
import com.citrus.origin.object.req.BlacklistFindUserReq;
import com.citrus.origin.service.query.BlacklistQueryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OriginQueryUsecase {

    private final BlacklistQueryService blacklistQueryService;

    public List<Blacklist> findUser(BlacklistFindUserReq req) {
        return blacklistQueryService.findUser(req.getUserId());
    }
}
