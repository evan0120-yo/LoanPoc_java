package com.citrus.sign.repository;

import com.citrus.sign.model.SignOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignOrderRepository extends JpaRepository<SignOrder, Long> {

    Optional<SignOrder> findByLoanRecordId(Long loanRecordId);
}
