package org.collegemanagement.databases;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.repositories.RoleRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    @NullMarked
    public void run(String... args) {
        seedRoles();
    }

    public void seedRoles() {

        List<RoleType> roleTypes = Arrays.asList(RoleType.values());

        // Get existing role enums from DB
        Set<RoleType> existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        // Filter only new roles that are NOT in DB yet
        List<Role> rolesToInsert = roleTypes.stream()
                .filter(roleType -> !existingRoles.contains(roleType))
                .map(Role::new) // Role constructor without ID
                .collect(Collectors.toList());

        roleRepository.saveAll(rolesToInsert);
    }
}
