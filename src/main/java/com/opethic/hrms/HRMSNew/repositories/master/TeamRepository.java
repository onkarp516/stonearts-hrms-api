package com.opethic.hrms.HRMSNew.repositories.master;

import com.opethic.hrms.HRMSNew.models.master.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team,Long> {

    List<Team> findAllByCompanyIdAndBranchIdAndStatus(Long companyId, Long branchId, boolean b);

    Team findByIdAndStatus(Long teamId, boolean b);

    List<Team> findByBranchIdAndStatus(Long branchId, boolean b);

    List<Team> findAllByStatus(boolean b);
}
