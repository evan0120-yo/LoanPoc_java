package com.citrus.callect.repository;

import com.citrus.callect.model.CollectionCase;
import com.citrus.callect.model.CollectionCase.Bucket;
import com.citrus.callect.model.CollectionCase.CaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionCaseRepository extends JpaRepository<CollectionCase, Long> {

    Optional<CollectionCase> findByLoanRecordId(Long loanRecordId);

    List<CollectionCase> findByBucket(Bucket bucket);

    List<CollectionCase> findByAgentId(String agentId);

    List<CollectionCase> findByStatusIn(List<CaseStatus> statuses);

    List<CollectionCase> findByBucketAndStatus(Bucket bucket, CaseStatus status);
}
