package com.citrus.origin.service.query;

import org.springframework.stereotype.Service;

import com.citrus.origin.dao.BlacklistDao;
import com.citrus.origin.model.Blacklist;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlacklistQueryService {

    private final BlacklistDao blacklistDao;

    public Blacklist findById(String blacklistId) {
        return blacklistDao.findById(blacklistId).orElseThrow(() -> new RuntimeException("Blacklist not found"));
    }

    public List<Blacklist> findByExistUser(String userId) {
        return blacklistDao.findByUserId(userId);
    }
}
