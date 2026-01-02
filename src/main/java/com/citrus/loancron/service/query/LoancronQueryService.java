package com.citrus.loancron.service.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.citrus.loancore.enums.LoanStateEnum;
import com.citrus.loancore.repository.LoanOrderRepository;

import lombok.RequiredArgsConstructor;

/**
 * Loancron 查詢服務
 * 查詢 loancore 的訂單狀態
 */
@Service
@RequiredArgsConstructor
public class LoancronQueryService {

    private final LoanOrderRepository loanOrderRepository;

    /**
     * 查詢 PENDING 狀態的訂單 ID 列表
     */
    public List<String> findPendingOrderIds() {
        return loanOrderRepository.findOrderIdsByStatus(LoanStateEnum.PENDING);
    }
}
