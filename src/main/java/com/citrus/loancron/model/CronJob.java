package com.citrus.loancron.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

import jakarta.persistence.Id;

import com.citrus.loancron.enums.JobStatusEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cron_job")
public class CronJob {
    @Id
    private String jobId;
    private String jobName;
    private JobStatusEnum jobStatus;
    private Instant startAt;
    private Instant endAt;
    private Integer processedCount;
    private Integer failedCount;
    private String errorMessage;
}
