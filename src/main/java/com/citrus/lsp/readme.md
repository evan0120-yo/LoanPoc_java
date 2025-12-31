# LSP Module

---

# 貸款導流 (Waterfall Mode)

## 1. 業務背景

當我們自己的審批拒絕用戶（例如 CIBIL 分數不夠），可以把這個案子**轉介給合作商**。
合作商如果成功放款，我們可以收一筆**導流佣金**。

**白話說：** 把自己不做的案子賣給別人。

### 為什麼需要？

1. **流量變現** — 拒絕的案子也有價值
2. **用戶體驗** — 與其直接拒絕，不如幫他找到其他貸款機會
3. **多元收入** — 除了利息收入，還有導流佣金收入

---

## 2. 流程圖

```
用戶申請貸款
    ↓
origin → loancore 建單 (status=PENDING)
    ↓
loancore 協調徵信 + 決策
    ↓
origin 審核：CIBIL 650，我們的政策要求 680+
    ↓
loancore 更新狀態為 LSP_ROUTING，發送 MQ 給 lsp
    ↓
lsp 收到 Lead，開始瀑布流路由：
    1. Partner A：要求 CIBIL > 600 ✓ → 佣金 3%
    2. Partner B：要求 CIBIL > 550 ✓ → 佣金 2%
    3. Partner C：只要有 PAN 就收 ✓ → 佣金 1%
    ↓
選擇佣金最高的 Partner A
    ↓
推送 Lead 給 Partner A
    ↓
等待 Partner A 回調...
    ↓
Partner A 核准 → 用戶跳轉完成申請
Partner A 拒絕 → 嘗試 Partner B...
全部拒絕 → lsp 通知 loancore 更新狀態
```

---

## 3. Lead 狀態定義

| 狀態 | 說明 |
|-----|------|
| PENDING | 待推送 |
| SENT | 已推送給合作商 |
| APPROVED | 合作商核准 |
| REJECTED | 合作商拒絕 |
| DISBURSED | 合作商已放款 |
| EXHAUSTED | 所有合作商都拒絕 |
| EXPIRED | 超時未回調 |

---

## 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /lsp/lead/submit` | 提交 Lead | loancore |
| `POST /lsp/lead/{leadId}` | 查詢 Lead 狀態 | 後台 |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /lsp/postback/{partnerId}` | 接收合作商回調 | Partner (外部) |
| `POST /lsp/partner/list` | 查詢合作商列表 | 後台 |
| `POST /lsp/partner/create` | 新增合作商 | 後台 |
| `POST /lsp/partner/{id}/update` | 更新合作商配置 | 後台 |
| `POST /lsp/commission/pending` | 待結算佣金 | 財務 |
| `POST /lsp/commission/settle` | 標記佣金已結算 | 財務 |

---

## 5. 合作商管理

| 欄位 | 說明 |
|-----|------|
| partnerId | 合作商 ID |
| partnerName | 名稱 |
| apiUrl | 推送 API |
| apiKey | 認證金鑰 |
| commissionRate | 佣金率 (%) |
| minCibilScore | 最低 CIBIL 要求 |
| isActive | 是否啟用 |

---

## 6. 佣金計算

```
佣金 = 放款金額 × 佣金率

例：Partner A 放款 ₹30,000，佣金率 3%
佣金 = ₹30,000 × 3% = ₹900
```

---

## 7. 模組關係

```
            ┌─────────────┐
            │  loancore   │ ← 統一資料來源
            │ (LoanOrder) │
            └──────┬──────┘
                   │ 狀態=LSP_ROUTING 時發 MQ
                   ▼
            ┌─────────────┐           ┌─────────────────┐
            │     lsp     │◀─────────▶│    Partners     │
            └──────┬──────┘  API/回調  │ (外部合作商)    │
                   │                  └─────────────────┘
                   │ 結果通知 (MQ)
                   ▼
            ┌─────────────┐
            │  loancore   │ ← 更新 LoanOrder 狀態
            └─────────────┘
```

---

## 8. 設計考量

| 項目 | 建議 |
|-----|------|
| **超時處理** | 合作商不回調時，設定超時自動嘗試下一家 |
| **簽名驗證** | Postback 要驗證簽名，避免偽造 |
| **去重** | 同一 Postback 可能重複發送，要冪等處理 |
| **佣金對帳** | 定期和合作商對帳佣金 |
| **API 版本** | 不同合作商 API 格式不同，要有 Adapter 層 |

---
---
---

# 貸超 (Marketplace Mode)

> [!NOTE]
> 待正式需求確認後再開發。

## 1. 業務背景

用戶進入「貸款超市」頁面，系統同時查詢多家合作商的資格，
顯示符合條件的 Offer 列表，讓用戶自行選擇。

**白話說：** 貸款版的比價網站。

---

## 2. 流程圖

```
用戶進入貸超頁面
    ↓
輸入基本資料（金額、PAN 等）
    ↓
lsp 同時查詢所有合作商資格：
    - Partner A：✓ 可借 ₹50,000，利率 18%
    - Partner B：✓ 可借 ₹30,000，利率 15%
    - Partner C：✗ 不符合條件
    ↓
前端顯示 Offer 列表
    ↓
用戶選擇 Partner B
    ↓
跳轉到 Partner B 完成申請
    ↓
Partner B 放款後回調 → 計算佣金
```

---

## 3. 與貸款導流的差異

| 項目 | 貸款導流 (Waterfall) | 貸超 (Marketplace) |
|------|-------------------|-------------------|
| 觸發方式 | 系統自動 | 用戶主動 |
| 選擇權 | 系統選 | 用戶選 |
| 查詢方式 | 依序 (串行) | 同時 (並行) |
| 用戶感知 | 無感知 | 看到多個選項 |

---

## 4. API 清單

### Phase 1

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /lsp/marketplace/offers` | 查詢可用 Offer 列表 | 前端 |
| `POST /lsp/marketplace/select` | 用戶選擇 Offer | 前端 |

### Phase 2

| API | 用途 | 呼叫者 |
|-----|------|-------|
| `POST /lsp/marketplace/postback/{partnerId}` | 接收合作商回調 | Partner (外部) |
| `POST /lsp/marketplace/commission/pending` | 待結算佣金 | 財務 |
| `POST /lsp/marketplace/commission/settle` | 標記佣金已結算 | 財務 |