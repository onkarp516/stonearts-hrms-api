package com.opethic.hrms.HRMSNew.repositories.access_permissions_repositories;

import com.opethic.hrms.HRMSNew.models.access_permissions.RoleAccessPermissions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleAccessPermissionsRepository extends JpaRepository<RoleAccessPermissions,Long> {

    List<RoleAccessPermissions> findByRoleMasterIdAndStatus(Long roleId, boolean b);
    List<RoleAccessPermissions> findByStatus(boolean b);
    RoleAccessPermissions findByRoleMasterIdAndStatusAndSystemActionMappingId(Long id, boolean b, Long id1);
}
