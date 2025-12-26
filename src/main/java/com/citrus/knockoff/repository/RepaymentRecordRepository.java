package com.citrus.knockoff.repository;

import com.citrus.knockoff.model.RepaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface RepaymentRecordRepository extends JpaRepository<RepaymentRecord, Long> {

    List<RepaymentRecord> findByLoanRecordId(Long loanRecordId);

    @Query("SELECT COALESCE(SUM(r.principalAmount), 0) FROM RepaymentRecord r WHERE r.loanRecordId = :loanId AND r.status = 'POSTED'")
    BigDecimal sumPrincipalByLoanId(@Param("loanId") Long loanRecordId);

    @Query("SELECT COALESCE(SUM(r.interestAmount), 0) FROM RepaymentRecord r WHERE r.loanRecordId = :loanId AND r.status = 'POSTED'")
    BigDecimal sumInterestByLoanId(@Param("loanId") Long loanRecordId);
}
