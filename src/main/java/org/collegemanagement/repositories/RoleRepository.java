package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.Role;
import org.collegemanagement.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findRolesByName(RoleType name);
    Optional<Role> findByName(RoleType name);
    boolean existsByName(RoleType name);
}
