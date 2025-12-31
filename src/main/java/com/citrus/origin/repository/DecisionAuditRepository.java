package com.citrus.origin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.citrus.origin.model.DecisionAudit;

public interface DecisionAuditRepository extends JpaRepository<DecisionAudit, String> {

}
