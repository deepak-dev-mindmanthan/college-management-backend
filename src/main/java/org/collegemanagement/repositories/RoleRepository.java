package org.collegemanagement.repositories;

import org.collegemanagement.entity.Role;
import org.collegemanagement.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findRolesByName(RoleType name);
    boolean existsByName(RoleType name);
    Role findRoleByName(RoleType roleType);
}
