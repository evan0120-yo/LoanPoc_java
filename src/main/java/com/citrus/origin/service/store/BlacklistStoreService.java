package com.citrus.origin.service.store;

import org.springframework.stereotype.Service;

import com.citrus.origin.dao.BlacklistDao;
import com.citrus.origin.enums.IdentifierEnum;
import com.citrus.origin.model.Blacklist;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlacklistStoreService {

    private final BlacklistDao blacklistDao;

    public Blacklist save(String userId, IdentifierEnum identifier, String identifierValue, String reason,
            String addedBy) {
        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setIdentifier(identifier);
        blacklist.setIdentifierValue(identifierValue);
        blacklist.setReason(reason);
        blacklist.setAddedBy(addedBy);
        return blacklistDao.save(blacklist);
    }

    public void delete(Blacklist blacklist, String deletedBy) {
        blacklistDao.delete(blacklist, deletedBy);
    }
}
