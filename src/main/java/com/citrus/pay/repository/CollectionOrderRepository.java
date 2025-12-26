package com.citrus.pay.repository;

import com.citrus.pay.model.CollectionOrder;
import com.citrus.pay.model.CollectionOrder.CollectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionOrderRepository extends JpaRepository<CollectionOrder, Long> {

    List<CollectionOrder> findByLoanRecordId(Long loanRecordId);

    Optional<CollectionOrder> findByPgRef(String pgRef);

    Optional<CollectionOrder> findByUtr(String utr);

    List<CollectionOrder> findByLoanRecordIdAndStatus(Long loanRecordId, CollectionStatus status);
}
