# 1. module定義
這個module主要用途是：原先貸超的access and credit, for合作商的

# 2. 核心邏輯
核心邏輯：
    瀑布流路由 (Waterfall)：依利潤高低排序，
    依序詢問 Partner A -> Partner B。
    狀態追蹤：記錄導流成功/失敗狀態 (Lead Status)。
    回調處理：接收 Partner 的核准/放款回調 (Postback)，計算導流佣金。