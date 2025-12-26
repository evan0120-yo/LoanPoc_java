package com.citrus.bureau.service.guard;

import com.citrus.share.exception.BusinessException;
import com.citrus.share.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Bureau Guard - 輸入驗證
 */
@Component
public class BureauGuard {

    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");

    /**
     * 驗證 PAN 格式
     */
    public void validatePanFormat(String panNumber) {
        if (panNumber == null || !PAN_PATTERN.matcher(panNumber).matches()) {
            throw new BusinessException(ErrorCode.BUREAU_PAN_INVALID, "Invalid PAN format: " + panNumber);
        }
    }

    /**
     * 驗證名稱匹配 (模糊比對)
     */
    public boolean isNameMatch(String input, String panHolder) {
        if (input == null || panHolder == null) {
            return false;
        }
        // 簡化比對: 忽略大小寫和空格
        String normalizedInput = input.toUpperCase().replaceAll("\\s+", "");
        String normalizedHolder = panHolder.toUpperCase().replaceAll("\\s+", "");

        // 包含比對 (處理名字順序不同的情況)
        return normalizedInput.contains(normalizedHolder) ||
                normalizedHolder.contains(normalizedInput);
    }
}
