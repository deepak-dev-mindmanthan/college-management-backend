package org.collegemanagement.services;


import org.collegemanagement.entity.Role;
import org.collegemanagement.enums.RoleType;

import java.util.Set;


public interface RoleService {
    Role getRoleById(Long id);
    Set<Role> getRoles(RoleType roleType);
    Role getRoleByName(RoleType roleType);
}
