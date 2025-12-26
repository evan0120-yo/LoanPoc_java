package com.citrus.pay.repository;

import com.citrus.pay.model.DisbursalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisbursalOrderRepository extends JpaRepository<DisbursalOrder, Long> {

    Optional<DisbursalOrder> findByLoanRecordId(Long loanRecordId);

    Optional<DisbursalOrder> findByUtr(String utr);

    Optional<DisbursalOrder> findByPgRef(String pgRef);
}
