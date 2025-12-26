# 1. module定義
這個module主要用途是：所有outbox以外的排程, 批次排成支付, 包含冷靜期邏輯

# 2. 核心邏輯：
    日切 (EOD)：每日 00:00 觸發。
    計息觸發：發送 ACCRUAL_Run 指令給 Ledger。
    逾期檢測：掃描未還款訂單，更新 DPD (Days Past Due)。
    狀態遷移：觸發 Active -> Overdue -> NPA 的狀態改變。