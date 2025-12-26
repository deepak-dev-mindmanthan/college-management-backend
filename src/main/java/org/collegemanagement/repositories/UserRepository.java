package org.collegemanagement.repositories;

import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findUserByEmail(String email);

    List<User> findUsersByNameAndRoles(String name, Set<Role> roles);

    List<User> findByCollegeIdAndRolesContaining(Long id, Role roles);

    Long countByRolesContaining(Role role);

    long countByCollegeIdAndRolesContaining(Long id, Role roles);

    boolean getUserById(Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Find user by UUID
     */
    @Query("""
            SELECT u FROM User u
            WHERE u.uuid = :uuid
            """)
    Optional<User> findByUuid(@Param("uuid") String uuid);
}