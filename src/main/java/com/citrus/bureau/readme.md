# Bureau Module

這是貸款系統的**徵信模組**，負責對接第三方數據供應商（打小報告的模組 😂）。

> [!IMPORTANT]
> **Bureau 只收集外部資料，不做決策**
> - 呼叫外部 API 取得資料
> - 輸出標準化報告
> - 決策由 origin 負責

> [!NOTE]
> **黑名單已在 Origin apply 時檢查過了！**
> - 能進到 Bureau 的訂單都不在黑名單
> - PAN 驗證只確認「這個 ID 是真實存在的」

---

# 1. 模組定義

| 項目 | 說明 |
|------|------|
| 角色 | 偵探部門 / 防腐層 |
| 對外 API | ❌ 僅供 **loancore** 調用 |
| 核心職責 | PAN 驗證、CIBIL 查詢、BSA 分析 |
| 設計模式 | **工廠模式**（方便乙方開發商實作串接）|

---

# 2. 核心流程

```
loancore (BUREAU_CHECK 訂單)
    │
    │ MQ (via Outbox)
    ▼
┌─────────────────┐
│  BureauConsumer │ ← 接收 MQ 訊息
│                 │
│  ┌─────────────┐│
│  │ PAN 工廠    ││→ 確認 ID 是真的（不是查黑名單！）
│  ├─────────────┤│
│  │ CIBIL 工廠  ││→ 查信用分數
│  ├─────────────┤│
│  │ BSA 工廠    ││→ 查月收入
│  └─────────────┘│
│                 │
│  全部成功 → 回傳 BureauReport
│  全部失敗 → 回傳失敗，loancore 回滾狀態
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Origin      │ ← 拿報告做決策（此時黑名單已通過）
└─────────────────┘
```

---

# 3. 工廠模式設計

## 3.1 目錄結構

```
bureau/
├── consumer/
│   └── BureauConsumer.java         ← 接收 MQ
│
├── factory/
│   ├── PanVerifyFactory.java       ← PAN 驗證工廠
│   ├── CibilQueryFactory.java      ← CIBIL 查詢工廠
│   └── BsaAnalyzeFactory.java      ← BSA 分析工廠
│
├── provider/
│   ├── pan/
│   │   ├── PanProvider.java        ← interface
│   │   ├── MockPanProvider.java    ← POC 用（return 假資料）
│   │   ├── KarzaPanProvider.java   ← 乙方實作
│   │   └── SignzyPanProvider.java  ← 乙方實作
│   │
│   ├── cibil/
│   │   ├── CibilProvider.java      ← interface
│   │   ├── MockCibilProvider.java  ← POC 用
│   │   ├── TransUnionCibilProvider.java
│   │   └── ExperianCibilProvider.java
│   │
│   └── bsa/
│       ├── BsaProvider.java        ← interface
│       ├── MockBsaProvider.java    ← POC 用
│       ├── PerfiosBsaProvider.java
│       └── FinbitBsaProvider.java
│
├── model/
│   └── BureauReport.java           ← 標準化報告
│
└── service/
    └── BureauService.java          ← 組合三個工廠
```

## 3.2 介面設計

```java
// PAN 驗證
public interface PanProvider {
    PanResult verify(String panNumber, String name);
}

// CIBIL 查詢
public interface CibilProvider {
    CibilResult query(String panNumber);
}

// BSA 分析
public interface BsaProvider {
    BsaResult analyze(String bankAccountNumber);
}
```

## 3.3 Mock Provider 實作

```java
@Component
public class MockPanProvider implements PanProvider {
    @Override
    public PanResult verify(String panNumber, String name) {
        return PanResult.builder()
            .panNumber(panNumber)
            .isValid(true)           // 假資料：永遠有效
            .nameMatch(true)
            .status("ACTIVE")
            .build();
    }
}

@Component
public class MockCibilProvider implements CibilProvider {
    private final Random random = new Random();
    
    @Override
    public CibilResult query(String panNumber) {
        return CibilResult.builder()
            .cibilScore(500 + random.nextInt(300))  // 隨機 500-800
            .existingLoanCount(random.nextInt(5))
            .totalExposure(BigDecimal.valueOf(random.nextInt(1000000)))
            .build();
    }
}

@Component
public class MockBsaProvider implements BsaProvider {
    private final Random random = new Random();
    
    @Override
    public BsaResult analyze(String bankAccountNumber) {
        return BsaResult.builder()
            .monthlyIncome(BigDecimal.valueOf(30000 + random.nextInt(70000)))  // 30K-100K
            .avgMonthlyBalance(BigDecimal.valueOf(10000 + random.nextInt(40000)))
            .salaryRegularity("REGULAR")
            .build();
    }
}
```

---

# 4. 徵信項目詳解

## 4.1 PAN 驗證（Permanent Account Number）

**什麼是 PAN？**
- 印度所得稅部門發放的 **10 位稅號**
- 格式：`ABCDE1234F`（5 字母 + 4 數字 + 1 字母）

**驗證目的：**
- 確認 PAN 號碼真實有效（**不是黑名單檢查！**）
- 確認 PAN 與姓名匹配

**可用供應商：**
| 供應商 | 說明 |
|-------|------|
| Karza | 印度主流 KYC 供應商 |
| Signzy | 另一家 KYC 供應商 |
| Aadhaar Bridge | 也提供 PAN 驗證 |

> [!NOTE]
> **黑名單檢查在 Origin 模組**，不在這裡！
> Bureau 只問「這個 ID 是真的嗎？」

---

## 4.2 CIBIL 查詢（Credit Information Bureau India Limited）

**什麼是 CIBIL？**
- 印度最大的 **信用評分機構**（類似台灣的聯徵中心）

**CIBIL Score 範圍：**
| 分數 | 評級 | 說明 |
|-----|------|------|
| 300-549 | Poor | 信用風險高 |
| 550-649 | Fair | 可能獲得貸款 |
| 650-749 | Good | 良好 |
| 750-900 | Excellent | 優秀 |

**可用供應商：**
| 供應商 | 說明 |
|-------|------|
| TransUnion CIBIL | 主流 |
| Experian | 備選 |
| Equifax | 備選 |
| CRIF | 備選 |

---

## 4.3 BSA 分析（Bank Statement Analysis）

**什麼是 BSA？**
- 銀行流水分析，解析過去 3-12 個月的銀行交易記錄

**分析目的：**
- 計算 **月均收入**（用於 FOIR 計算）
- 計算 **月均餘額**

**可用供應商：**
| 供應商 | 說明 |
|-------|------|
| Perfios | 主流 |
| Finbit | 備選 |
| Account Aggregator (AA) | RBI 推動的新框架 |

---

# 5. API / 通訊

| 通訊方式 | 用途 | 發送者 → 接收者 |
|---------|------|----------------|
| **MQ (Outbox)** | 發送待審核訂單 | loancore → bureau |
| **MQ / HTTP** | 回傳徵信報告 | bureau → loancore |

---

# 6. 資料模型設計（三層架構）

類似支付模組，採用三層架構記錄和甲方的溝通：

```
┌─────────────────────────────────────────────────────────────┐
│ BureauRecord（狀態機）                                       │
│ - 一筆訂單一條記錄                                           │
│ - 追蹤整體狀態：PENDING → IN_PROGRESS → COMPLETED/FAILED    │
│ - 彙整最終結果（PAN、CIBIL、BSA）                           │
└─────────────────────────────────────────────────────────────┘
                          │ 1 : N
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ BureauAttempt（嘗試記錄）                                    │
│ - 每次呼叫甲方的「嘗試」                                      │
│ - 記錄用了哪個供應商、最終結果                               │
│ - 成功的 Attempt 會把結果寫回 Record                        │
└─────────────────────────────────────────────────────────────┘
                          │ 1 : N
                          ▼
┌─────────────────────────────────────────────────────────────┐
│ BureauApiLog（通訊日誌）                                     │
│ - 每次實際的 HTTP 請求/回應                                  │
│ - 完整的 req / resp JSON                                    │
│ - 回應時間、HTTP 狀態碼等                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 6.1 BureauRecord（彙整報告 + 狀態機？）

> [!WARNING]
> **討論：這裡需要狀態機嗎？**
> - LoanOrder 已經有 BUREAU_CHECK 狀態
> - BureauRecord 是否只需記錄結果，不需要自己的狀態機？

```java
@Entity
@Table(name = "bureau_record")
public class BureauRecord {
    @Id
    private String bureauRecordId;
    private String loanOrderId;
    
    // === PAN 驗證結果 ===
    private Boolean panValid;
    private Boolean panNameMatch;
    
    // === CIBIL 結果 ===
    private Integer cibilScore;
    private Integer existingLoanCount;
    private BigDecimal totalExposure;
    
    // === BSA 結果 ===
    private BigDecimal monthlyIncome;
    private BigDecimal avgMonthlyBalance;
    
    // === 狀態（是否需要？）===
    @Enumerated(EnumType.STRING)
    private BureauStatusEnum status;  // PENDING / IN_PROGRESS / COMPLETED / FAILED
    
    private Instant createdAt;
    private Instant completedAt;
}
```

---

## 6.2 BureauAttempt（嘗試記錄）

每次呼叫甲方的嘗試，可能 call 多個供應商。

```java
@Entity
@Table(name = "bureau_attempt")
public class BureauAttempt {
    @Id
    private String attemptId;
    private String bureauRecordId;      // FK → BureauRecord
    
    @Enumerated(EnumType.STRING)
    private BureauApiTypeEnum apiType;  // PAN_VERIFY / CIBIL_QUERY / BSA_ANALYZE
    private String providerName;        // KARZA / TRANSUNION / PERFIOS...
    
    @Enumerated(EnumType.STRING)
    private BureauAttemptStatusEnum status;  // PENDING / SUCCESS / FAILED
    
    @Column(columnDefinition = "TEXT")
    private String result;              // 成功時的結果 JSON
    private String errorMessage;        // 失敗原因
    
    private Instant createdAt;
    private Instant completedAt;
}
```

---

## 6.3 BureauApiLog（通訊日誌）

每次 HTTP 請求/回應的完整記錄，用於審計、除錯、對帳。

```java
@Entity
@Table(name = "bureau_api_log")
public class BureauApiLog {
    @Id
    private String apiLogId;
    private String attemptId;           // FK → BureauAttempt
    
    // === 請求 ===
    @Column(columnDefinition = "TEXT")
    private String requestPayload;
    private Instant requestAt;
    
    // === 回應 ===
    @Column(columnDefinition = "TEXT")
    private String responsePayload;
    private Integer httpStatus;
    private Instant responseAt;
    
    // === 效能 ===
    private Long responseTimeMs;
}
```

---

## 6.4 舉例：查 CIBIL 失敗後換供應商成功

```
BureauRecord (order_A, status=COMPLETED, cibilScore=720)
│
├── BureauAttempt #1 (CIBIL, TransUnion, FAILED)
│   └── BureauApiLog #1 (504 timeout)
│
└── BureauAttempt #2 (CIBIL, Experian, SUCCESS)
    └── BureauApiLog #2 (200, score=720)
```

---

# 7. 回滾機制（失敗處理）

當所有供應商都失敗時，Bureau 需要通知 loancore 回滾狀態：

```
Bureau 執行
    │
    ├── 成功 → 回傳 BureauReport 給 Origin
    │
    └── 全部失敗
            │
            ▼
        通知 Loancore
            │
            ▼
        狀態改回 PENDING
        等下次 loancron 觸發重試
```

**實作方式：**
```java
// BureauConsumer
try {
    BureauReport report = bureauService.runFullReport(...);
    // 成功 → 發送給 origin 決策
    sendToOrigin(report);
} catch (AllProvidersFailedException e) {
    // 全部失敗 → 回滾狀態
    loancoreClient.rollbackStatus(orderId, LoanStateEnum.PENDING);
}
```

---

# 8. 實作進度

| 項目 | 狀態 |
|-----|------|
| BureauConsumer（接收 MQ）| ✅ 已完成 |
| Factory 結構 | ⏳ 待實作 |
| MockPanProvider | ⏳ 待實作 |
| MockCibilProvider | ⏳ 待實作 |
| MockBsaProvider | ⏳ 待實作 |
| BureauReport Model | ⏳ 待實作 |
| 回傳報告給 Origin | ⏳ 待實作 |
| 失敗回滾機制 | ⏳ 待實作 |