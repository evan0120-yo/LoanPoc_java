# 1. module定義
這個module主要用途是：連動支付系統, 一次性支付

# 2. 核心邏輯：
    放款執行 (Payout)：呼叫 PG 放款 API (Nodal Account)。
    還款連結 (Collection)：生成還款連結 (Payment Link / UPI Intent)。
    Webhook 處理：
        接收 PG 回調，去重 (Idempotency)。
        將雜亂的銀行狀態轉譯為標準事件 PAYMENT_RECEIVED。
        發送 MQ 給 Ledger。