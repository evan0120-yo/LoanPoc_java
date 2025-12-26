# 1. module定義
這個module主要用途是：催收

# 2. 核心邏輯：
    分桶策略 (Bucketing)：依 DPD 自動分類 (B0, B1, B2, NPA)。
    案件指派：
        輕微逾期 -> 自動發送 SMS/WhatsApp。
        嚴重逾期 -> 指派給催收代理 (Agency)。
    催記管理：記錄每一次催收互動結果 (PTP - Promise to Pay)。