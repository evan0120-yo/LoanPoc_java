package com.citrus.origin.dao;

import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.citrus.origin.model.DecisionAudit;
import com.citrus.origin.repository.DecisionAuditRepository;

@Component
@RequiredArgsConstructor
public class DecisionAuditDao {

    private final DecisionAuditRepository decisionAuditRepository;

    public DecisionAudit save(DecisionAudit decisionAudit) {
        return decisionAuditRepository.save(decisionAudit);
    }

    public void delete(DecisionAudit decisionAudit) {
        decisionAuditRepository.delete(decisionAudit);
    }

    public Optional<DecisionAudit> findById(String id) {
        return decisionAuditRepository.findById(id);
    }

}
