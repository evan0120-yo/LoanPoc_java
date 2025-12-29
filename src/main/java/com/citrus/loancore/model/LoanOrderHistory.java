package com.citrus.loancore.model;

import java.time.Instant;

import com.citrus.loancore.enums.LoanStateEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loan_order_history")
public class LoanOrderHistory {
    @Id
    private String loanOrderHistoryId;
    private String loanOrderId;
    @Enumerated(EnumType.STRING)
    private LoanStateEnum fromStatus;
    @Enumerated(EnumType.STRING)
    private LoanStateEnum toStatus;
    private String triggeredBy; // module name
    private String remark;
    private Instant createdAt;
}
