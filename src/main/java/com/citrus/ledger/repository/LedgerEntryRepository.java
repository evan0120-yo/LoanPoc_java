package com.citrus.ledger.repository;

import com.citrus.ledger.model.LedgerEntry;
import com.citrus.ledger.model.LedgerEntry.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByLoanRecordIdOrderByPostingDateDesc(Long loanRecordId);

    List<LedgerEntry> findByLoanRecordIdAndEntryType(Long loanRecordId, EntryType entryType);

    @Query("SELECT COALESCE(SUM(e.debitAmount), 0) FROM LedgerEntry e " +
            "WHERE e.loanRecordId = :loanId AND e.debitAccount = :account AND e.isReversal = false")
    BigDecimal sumDebitByAccount(@Param("loanId") Long loanRecordId, @Param("account") String account);

    @Query("SELECT COALESCE(SUM(e.creditAmount), 0) FROM LedgerEntry e " +
            "WHERE e.loanRecordId = :loanId AND e.creditAccount = :account AND e.isReversal = false")
    BigDecimal sumCreditByAccount(@Param("loanId") Long loanRecordId, @Param("account") String account);

    List<LedgerEntry> findByPostingDate(LocalDate date);
}
