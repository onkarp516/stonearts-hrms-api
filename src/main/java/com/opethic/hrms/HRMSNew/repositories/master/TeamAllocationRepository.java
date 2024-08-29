package com.opethic.hrms.HRMSNew.repositories.master;

import com.opethic.hrms.HRMSNew.models.master.Team;
import com.opethic.hrms.HRMSNew.models.master.TeamAllocate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamAllocationRepository extends JpaRepository<TeamAllocate, Long> {
    List<TeamAllocate> findByTeamIdAndTeamLeaderIdAndStatus(Long teamId, Long teamLeaderId, boolean b);

    List<TeamAllocate> findByTeamLeaderIdAndStatus(Long teamLeaderId, boolean b);

    @Query(value = "SELECT ta.* FROM stonearts_new_db.team_allocation_tbl ta LEFT JOIN stonearts_new_db.team_tbl t ON ta.team_id = t.id WHERE t.company_id=?1 AND t.branch_id=?2 AND ta.status = 1", nativeQuery = true)
    List<TeamAllocate> getTeamAllocationData(Long cid, Long bid);

    // List<TeamAllocate> findByIdAndStatus(Long teamId, boolean b);

    TeamAllocate findByIdAndStatus(Long teamId, boolean b);

}
