# 1. module定義
這個module主要用途是：法律文件生成與銀行帳戶綁定。簽約用

# 2. 核心邏輯：
    KFS 生成：產出合規的關鍵事實聲明 (PDF)，揭露 APR 與手續費。
    Penny Drop：打入 1 Rupee 驗證用戶銀行帳戶有效性。
    e-Sign：觸發 Aadhaar 電子簽章流程。
    e-NACH：設定自動扣款授權 (Auto-debit Mandate)。
    狀態鎖定：簽約完成 -> 觸發放款指令 (Disbursal Ready)。