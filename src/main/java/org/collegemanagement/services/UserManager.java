package org.collegemanagement.services;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.collegemanagement.entity.Role;
import org.collegemanagement.entity.User;
import org.collegemanagement.dto.UserDto;
import org.collegemanagement.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserManager implements UserDetailsManager {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final RoleService roleService;

    public UserManager(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    @Transactional
    @Override
    public void createUser(UserDetails userDetails) {
        User user = (User) userDetails;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUser(UserDetails userDetails) {
        User user = (User) userDetails;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(String username) {

    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream().peek(item -> {
            if (item.getCollege() != null) {
                item.getCollege().setUsers(null);
            }
        }).collect(Collectors.toList());
    }


    public boolean userExists(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.isPresent();
    }

    public List<User> findByCollegeIdAndRoles(Long id, Set<Role> roles) {
        return userRepository.findByCollegeIdAndRoles(id, roles).stream().peek(item -> {
            if (item.getCollege() != null) {
                item.getCollege().setUsers(null);
            }
        }).collect(Collectors.toList());
    }


    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format("User not found with id {0}", id)));
    }


    @Transactional
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // You can implement this method to change the user's password
        // For example, when a user wants to change their password
    }

    @Override
    public boolean userExists(String username) {
        return false;
    }

    public UserDto getUserById(Long id) {
        UserDto user = UserDto.fromEntity(userRepository.findById(id).orElseThrow());
        user.getCollege().setUsers(null);
        return user;
    }

    public boolean exitsByEmail(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);
        return userOptional.isPresent();
    }


    public List<Long> countByRoles(Set<Role> roles) {
        return userRepository.countByRoles(roles);
    }

    public long countByCollegeIdAndRoles(Long id, Set<Role> roles) {
        return userRepository.countByCollegeIdAndRoles(id, roles);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch the user from the repository by username
        Optional<User> emailOptional = userRepository.findUserByEmail(email);
        // Check if the user exists
        if (emailOptional.isEmpty()) {
            throw new UsernameNotFoundException(MessageFormat.format("User with username {0} not found", email));
        }

        // Return the UserDetails extracted from the User entity
        return emailOptional.get();
    }


}
