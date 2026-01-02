package com.citrus.origin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Id;

import com.citrus.origin.enums.DecisionResultEnum;
import com.citrus.origin.enums.RejectReasonEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "decision_audit")
public class DecisionAudit {
    @Id
    private String decisionAuditId;
    private String loanOrderId;
    @Enumerated(EnumType.STRING)
    private DecisionResultEnum decisionResult;
    private BigDecimal approvedAmount;
    private BigDecimal approvedRate;
    private Integer approvedTenure;
    @Enumerated(EnumType.STRING)
    private RejectReasonEnum rejectReason;
    private String ruleName;
    private String inputSnapshot;
    private Instant createdAt;
}
