package com.citrus.loancron.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ShedLock 分散式鎖資料表
 * 
 * 注意：這個 Entity 只是讓 Hibernate 自動建表用
 * ShedLock 本身用 JdbcTemplate 直接操作，不走 JPA
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shedlock")
public class Shedlock {

    @Id
    private String name;

    private Instant lockUntil;

    private Instant lockedAt;

    private String lockedBy;
}
