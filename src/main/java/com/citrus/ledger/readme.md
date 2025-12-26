# 1. module定義
這個module主要用途是：會計module，目前先收到MQ即可。

# 2. 核心邏輯：
    複式簿記 (Double Entry)：借貸必相等 (Debit = Credit)。
    不可變性 (Immutable)：禁止 UPDATE 操作，餘額修正必須透過「紅沖藍補」。
    交易紀錄 (Journal)：記錄所有資金變動 (Disbursal, Repayment, Accrual, Write-off)。
    日結餘額：維護每日 Snapshot 以供快速查詢。