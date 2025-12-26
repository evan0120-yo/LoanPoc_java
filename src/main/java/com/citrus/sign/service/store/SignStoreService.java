package com.citrus.sign.service.store;

import com.citrus.sign.model.SignOrder;
import com.citrus.sign.model.SignOrder.OverallStatus;
import com.citrus.sign.model.SignOrder.StepStatus;
import com.citrus.sign.repository.SignOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sign Store Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SignStoreService {

    private final SignOrderRepository signOrderRepository;

    public SignOrder create(Long loanRecordId) {
        SignOrder order = SignOrder.builder()
                .loanRecordId(loanRecordId)
                .overallStatus(OverallStatus.PENDING)
                .build();
        return signOrderRepository.save(order);
    }

    public SignOrder updateKfsStatus(SignOrder order, StepStatus status, String url) {
        order.setKfsStatus(status);
        order.setKfsUrl(url);
        updateOverallStatus(order);
        return signOrderRepository.save(order);
    }

    public SignOrder updatePennyDropStatus(SignOrder order, StepStatus status, String utr) {
        order.setPennyDropStatus(status);
        order.setPennyDropUtr(utr);
        updateOverallStatus(order);
        return signOrderRepository.save(order);
    }

    public SignOrder updateEsignStatus(SignOrder order, StepStatus status, String ref) {
        order.setEsignStatus(status);
        order.setEsignRef(ref);
        updateOverallStatus(order);
        return signOrderRepository.save(order);
    }

    public SignOrder updateEnachStatus(SignOrder order, StepStatus status, String ref, String umrn) {
        order.setEnachStatus(status);
        order.setEnachRef(ref);
        order.setUmrn(umrn);
        updateOverallStatus(order);
        return signOrderRepository.save(order);
    }

    private void updateOverallStatus(SignOrder order) {
        if (order.isAllStepsCompleted()) {
            order.setOverallStatus(OverallStatus.COMPLETED);
        } else if (hasAnyFailed(order)) {
            order.setOverallStatus(OverallStatus.FAILED);
        } else if (hasAnyInProgress(order)) {
            order.setOverallStatus(OverallStatus.IN_PROGRESS);
        }
    }

    private boolean hasAnyFailed(SignOrder order) {
        return order.getKfsStatus() == StepStatus.FAILED ||
                order.getPennyDropStatus() == StepStatus.FAILED ||
                order.getEsignStatus() == StepStatus.FAILED ||
                order.getEnachStatus() == StepStatus.FAILED;
    }

    private boolean hasAnyInProgress(SignOrder order) {
        return order.getKfsStatus() == StepStatus.IN_PROGRESS ||
                order.getPennyDropStatus() == StepStatus.IN_PROGRESS ||
                order.getEsignStatus() == StepStatus.IN_PROGRESS ||
                order.getEnachStatus() == StepStatus.IN_PROGRESS;
    }
}
