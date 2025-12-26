# 1. module定義
這個module主要用途是：外部資料源的防腐層 (Anti-Corruption Layer)，負責標準化所有第三方數據。包含打小報告用

# 2. 核心邏輯
核心邏輯：
    PAN 驗證：檢查稅號有效性與持有人姓名一致性。
    CIBIL 查詢：獲取信用分數、歷史違約紀錄 (Tradelines)。
    BSA 解析：解析銀行流水 (Bank Statement)，計算 FOIR (償債能力比)。
    快取策略：實作 Caching 機制 (e.g., 30 天內不重複查詢)，節省成本。