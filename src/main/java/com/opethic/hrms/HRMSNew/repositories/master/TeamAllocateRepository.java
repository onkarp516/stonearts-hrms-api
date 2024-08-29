package com.opethic.hrms.HRMSNew.repositories.master;

import com.opethic.hrms.HRMSNew.models.master.TeamAllocate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamAllocateRepository extends JpaRepository<TeamAllocate, Long> {
    @Query(value = "SELECT team_id FROM stonearts_new_db.team_allocation_tbl where team_leader_id=?1", nativeQuery = true)
    Long getTeamByTeamLeader(Long id);

    List<TeamAllocate> findByTeamLeaderIdAndStatus(Long id, boolean b);


    List<TeamAllocate> findByTeamIdAndStatus(Long id, boolean b);
}