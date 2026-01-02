package com.citrus.origin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import jakarta.persistence.Id;

import com.citrus.origin.enums.IdentifierEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "blacklist")
public class Blacklist {
    @Id
    private String blacklistId;
    private String userId;
    @Enumerated(EnumType.STRING)
    private IdentifierEnum identifier;
    private String identifierValue;
    private String reason;
    private String addedBy; // ex: system, Aaron等等
    private Instant createdAt;
    private Instant deletedAt; // null 表示未刪除
    private String deletedBy;
}
