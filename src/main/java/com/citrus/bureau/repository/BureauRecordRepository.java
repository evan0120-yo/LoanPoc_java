package com.citrus.bureau.repository;

import com.citrus.bureau.model.BureauRecord;
import com.citrus.bureau.model.BureauRecord.BureauStatus;
import com.citrus.bureau.model.BureauRecord.QueryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface BureauRecordRepository extends JpaRepository<BureauRecord, Long> {

    /**
     * 查找有效快取
     */
    @Query("SELECT b FROM BureauRecord b WHERE b.panNumber = :pan AND b.queryType = :type " +
            "AND b.status = 'SUCCESS' AND b.expiresAt > :now ORDER BY b.createdAt DESC LIMIT 1")
    Optional<BureauRecord> findValidCache(
            @Param("pan") String panNumber,
            @Param("type") QueryType queryType,
            @Param("now") Instant now);

    Optional<BureauRecord> findTopByPanNumberAndQueryTypeOrderByCreatedAtDesc(
            String panNumber, QueryType queryType);
}
