package com.citrus.origin.service.guard;

import com.citrus.share.exception.BusinessException;
import com.citrus.share.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Origin Guard - 審核驗證
 */
@Component
public class OriginGuard {

    private static final int MIN_CIBIL_SCORE = 600;
    private static final double MAX_FOIR = 0.60;

    /**
     * 驗證 CIBIL 分數門檻
     */
    public void validateCibilScore(Integer cibilScore) {
        if (cibilScore == null || cibilScore < MIN_CIBIL_SCORE) {
            throw new BusinessException(
                    ErrorCode.ORIGIN_REJECTED,
                    "CIBIL score below minimum threshold: " + cibilScore);
        }
    }

    /**
     * 檢查是否通過 FOIR 門檻
     */
    public boolean checkFoirPass(java.math.BigDecimal foir) {
        return foir != null && foir.doubleValue() <= MAX_FOIR;
    }
}
