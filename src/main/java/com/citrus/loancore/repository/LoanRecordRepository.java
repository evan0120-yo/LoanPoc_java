package com.citrus.loancore.repository;

import com.citrus.loancore.model.LoanRecord;
import com.citrus.loancore.model.LoanRecord.LoanState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {

    Optional<LoanRecord> findByUserId(String userId);

    Optional<LoanRecord> findByMobile(String mobile);

    Optional<LoanRecord> findByPanNumber(String panNumber);

    List<LoanRecord> findByState(LoanState state);

    @Query("SELECT l FROM LoanRecord l WHERE l.state IN :states")
    List<LoanRecord> findByStateIn(@Param("states") List<LoanState> states);

    @Query("SELECT l FROM LoanRecord l WHERE l.userId = :userId AND l.state NOT IN ('CLOSED', 'WRITTEN_OFF', 'CANCELLED')")
    List<LoanRecord> findActiveByUserId(@Param("userId") String userId);

    boolean existsByPanNumberAndStateNotIn(String panNumber, List<LoanState> excludedStates);
}
