package org.collegemanagement.services.impl;

import org.collegemanagement.entity.Role;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.repositories.RoleRepository;
import org.collegemanagement.services.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Service
public class RoleServiceImpl implements RoleService {


    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Role getRoleById(Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.orElseThrow(() -> new RuntimeException("Role not found with id:"+id));
    }

    @Override
    public Set<Role> getRoles(RoleType roleType) {
        return new HashSet<>(roleRepository.findRolesByName(roleType));
    }
}
