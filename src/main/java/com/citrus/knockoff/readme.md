# 1. module定義
這個module主要用途是：決定「還進來的錢」要怎麼分配。沖銷用

# 2. 核心邏輯：
    還款順序 (Waterfall)：
        Tax (GST)：稅金最優先。
        Penalty：滯納金/罰息。
        Interest：利息。
        Principal：本金。
    部分還款 (Partial)：支援不完全還款，依上述順序填坑，剩餘部分掛帳。
    溢繳處理 (Excess)：多還的錢暫存入 Excess Bucket 或退款。
    強耦合：計算結果直接 Call Ledger 寫入。