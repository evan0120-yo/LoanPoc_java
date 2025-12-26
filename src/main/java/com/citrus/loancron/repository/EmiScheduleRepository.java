package com.citrus.loancron.repository;

import com.citrus.loancron.model.EmiSchedule;
import com.citrus.loancron.model.EmiSchedule.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmiScheduleRepository extends JpaRepository<EmiSchedule, Long> {

    List<EmiSchedule> findByLoanRecordIdOrderByInstallmentNo(Long loanRecordId);

    List<EmiSchedule> findByDueDateAndStatusIn(LocalDate dueDate, List<EmiStatus> statuses);

    @Query("SELECT e FROM EmiSchedule e WHERE e.dueDate < :today AND e.status NOT IN ('PAID') ORDER BY e.dueDate")
    List<EmiSchedule> findOverdueEmis(@Param("today") LocalDate today);

    @Query("SELECT e FROM EmiSchedule e WHERE e.loanRecordId = :loanId AND e.status NOT IN ('PAID') ORDER BY e.installmentNo")
    List<EmiSchedule> findUnpaidByLoanId(@Param("loanId") Long loanRecordId);
}
