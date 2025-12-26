# 1. module定義
這個module主要用途是：自肥檢傷分類

# 2. 核心邏輯：
    流量清洗 (Filter)：執行 Dedupe (去重) 與黑名單過濾。
    策略引擎 (Strategy)：根據 Bureau 數據跑決策樹 (Decision Tree)。
    產品定價 (Pricing)：計算核准額度 (Limit)、利率 (ROI)、年期 (Tenure)。
    自肥判定：Pass -> 產出 Offer，進入 Sign。
    Reject -> 轉送至 LSP 模組。