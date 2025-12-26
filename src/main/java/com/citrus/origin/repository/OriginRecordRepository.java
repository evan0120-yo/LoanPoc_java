package com.citrus.origin.repository;

import com.citrus.origin.model.OriginRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OriginRecordRepository extends JpaRepository<OriginRecord, Long> {

    Optional<OriginRecord> findByLoanRecordId(Long loanRecordId);

    Optional<OriginRecord> findByPanNumber(String panNumber);
}
