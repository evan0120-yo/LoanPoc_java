package com.citrus.lsp.repository;

import com.citrus.lsp.model.LeadRecord;
import com.citrus.lsp.model.LeadRecord.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRecordRepository extends JpaRepository<LeadRecord, Long> {

    Optional<LeadRecord> findByLoanRecordId(Long loanRecordId);

    List<LeadRecord> findByPartnerCode(String partnerCode);

    List<LeadRecord> findByStatus(LeadStatus status);

    Optional<LeadRecord> findByPartnerRef(String partnerRef);
}
