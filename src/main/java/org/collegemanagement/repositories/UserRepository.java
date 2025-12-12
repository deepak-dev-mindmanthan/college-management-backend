package org.collegemanagement.repositories;

import org.collegemanagement.entity.Role;
import org.collegemanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmail(String email);

    List<User> findUsersByNameAndRoles(String name, Set<Role> roles);

    List<User> findByCollegeIdAndRoles(Long id, Set<Role> roles);

    Long countByRolesContaining(Role role);

    long countByCollegeIdAndRoles(Long id, Set<Role> roles);

    boolean getUserById(Long id);
    boolean existsByEmail(String email);
}