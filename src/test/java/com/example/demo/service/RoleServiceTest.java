package com.example.demo.service;

import com.example.demo.enums.ERole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId("1");
        testRole.setName(ERole.ROLE_USER);
    }

    @Test
    @DisplayName("Should get role by ID successfully")
    void testGetRoleById() {
        when(roleRepository.findById("1")).thenReturn(Optional.of(testRole));

        Role result = roleService.getRoleById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals(ERole.ROLE_USER, result.getName());
        verify(roleRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("Should throw exception when role not found by ID")
    void testGetRoleByIdNotFound() {
        when(roleRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleById("999"));
        verify(roleRepository, times(1)).findById("999");
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void testGetAllRoles() {
        Role role2 = new Role();
        role2.setId("2");
        role2.setName(ERole.ROLE_ADMIN);
        
        List<Role> roles = Arrays.asList(testRole, role2);
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should create role successfully")
    void testCreateRole() {
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.createRole(testRole);

        assertNotNull(result);
        assertEquals(ERole.ROLE_USER, result.getName());
        verify(roleRepository, times(1)).save(testRole);
    }

    @Test
    @DisplayName("Should update role successfully")
    void testUpdateRole() {
        Role updatedRole = new Role();
        updatedRole.setName(ERole.ROLE_ADMIN);
        
        when(roleRepository.findById("1")).thenReturn(Optional.of(testRole));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        Role result = roleService.updateRole("1", updatedRole);

        assertNotNull(result);
        verify(roleRepository, times(1)).findById("1");
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    @DisplayName("Should delete role successfully")
    void testDeleteRole() {
        doNothing().when(roleRepository).deleteById("1");

        roleService.deleteRole("1");

        verify(roleRepository, times(1)).deleteById("1");
    }

    @Test
    @DisplayName("Should get role by name successfully")
    void testGetRoleByName() {
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(testRole));

        Role result = roleService.getRoleByName(ERole.ROLE_USER);

        assertNotNull(result);
        assertEquals(ERole.ROLE_USER, result.getName());
        verify(roleRepository, times(1)).findByName(ERole.ROLE_USER);
    }

    @Test
    @DisplayName("Should throw exception when role not found by name")
    void testGetRoleByNameNotFound() {
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getRoleByName(ERole.ROLE_ADMIN));
        verify(roleRepository, times(1)).findByName(ERole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Should check if role exists by name")
    void testExistsByName() {
        when(roleRepository.existsByName(ERole.ROLE_USER)).thenReturn(true);
        when(roleRepository.existsByName(ERole.ROLE_ADMIN)).thenReturn(false);

        assertTrue(roleService.existsByName(ERole.ROLE_USER));
        assertFalse(roleService.existsByName(ERole.ROLE_ADMIN));
        
        verify(roleRepository, times(1)).existsByName(ERole.ROLE_USER);
        verify(roleRepository, times(1)).existsByName(ERole.ROLE_ADMIN);
    }
}
