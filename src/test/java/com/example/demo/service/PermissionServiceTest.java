package com.example.demo.service;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService Tests")
class PermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Permission testPermission;

    @BeforeEach
    void setUp() {
        testPermission = new Permission();
        testPermission.setId("1");
        testPermission.setName("READ_CONVENTION");
        testPermission.setDescription("Permission to read conventions");
        testPermission.setRoleIds(new ArrayList<>(Arrays.asList("role1", "role2")));
    }

    @Test
    @DisplayName("Should create permission successfully")
    void testCreatePermission() {
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        Permission result = permissionService.createPermission(testPermission);

        assertNotNull(result);
        assertEquals("READ_CONVENTION", result.getName());
        verify(permissionRepository, times(1)).save(testPermission);
    }

    @Test
    @DisplayName("Should get all permissions successfully")
    void testGetAllPermissions() {
        Permission perm2 = new Permission();
        perm2.setId("2");
        perm2.setName("WRITE_CONVENTION");
        
        List<Permission> permissions = Arrays.asList(testPermission, perm2);
        when(permissionRepository.findAll()).thenReturn(permissions);

        List<Permission> result = permissionService.getAllPermissions();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(permissionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get permission by ID successfully")
    void testGetPermissionById() {
        when(permissionRepository.findById("1")).thenReturn(Optional.of(testPermission));

        Permission result = permissionService.getPermissionById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("READ_CONVENTION", result.getName());
        verify(permissionRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("Should throw exception when permission not found")
    void testGetPermissionByIdNotFound() {
        when(permissionRepository.findById("999")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> permissionService.getPermissionById("999"));
        verify(permissionRepository, times(1)).findById("999");
    }

    @Test
    @DisplayName("Should update permission successfully")
    void testUpdatePermission() {
        Permission updatedPermission = new Permission();
        updatedPermission.setName("UPDATED_PERMISSION");
        
        when(permissionRepository.save(any(Permission.class))).thenReturn(updatedPermission);

        Permission result = permissionService.updatePermission("1", updatedPermission);

        assertNotNull(result);
        assertEquals("1", updatedPermission.getId());
        verify(permissionRepository, times(1)).save(updatedPermission);
    }

    @Test
    @DisplayName("Should delete permission successfully")
    void testDeletePermission() {
        doNothing().when(permissionRepository).deleteById("1");

        permissionService.deletePermission("1");

        verify(permissionRepository, times(1)).deleteById("1");
    }

    @Test
    @DisplayName("Should get permissions by role")
    void testGetPermissionsByRole() {
        List<Permission> permissions = Arrays.asList(testPermission);
        when(permissionRepository.findByRoleIdsContaining("role1")).thenReturn(permissions);

        List<Permission> result = permissionService.getPermissionsByRole("role1");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getRoleIds().contains("role1"));
        verify(permissionRepository, times(1)).findByRoleIdsContaining("role1");
    }

    @Test
    @DisplayName("Should assign permission to role successfully")
    void testAssignPermissionToRole() {
        when(permissionRepository.findById("1")).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        Permission result = permissionService.assignPermissionToRole("1", "role3");

        assertNotNull(result);
        verify(permissionRepository, times(1)).findById("1");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("Should not add duplicate role when assigning permission")
    void testAssignPermissionToRoleNoDuplicate() {
        when(permissionRepository.findById("1")).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        Permission result = permissionService.assignPermissionToRole("1", "role1");

        assertNotNull(result);
        verify(permissionRepository, times(1)).findById("1");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("Should initialize roleIds if null when assigning")
    void testAssignPermissionToRoleWithNullRoleIds() {
        Permission permWithoutRoles = new Permission();
        permWithoutRoles.setId("2");
        permWithoutRoles.setName("TEST_PERM");
        permWithoutRoles.setRoleIds(null);
        
        when(permissionRepository.findById("2")).thenReturn(Optional.of(permWithoutRoles));
        when(permissionRepository.save(any(Permission.class))).thenReturn(permWithoutRoles);

        Permission result = permissionService.assignPermissionToRole("2", "role1");

        assertNotNull(result);
        assertNotNull(result.getRoleIds());
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("Should remove permission from role successfully")
    void testRemovePermissionFromRole() {
        when(permissionRepository.findById("1")).thenReturn(Optional.of(testPermission));
        when(permissionRepository.save(any(Permission.class))).thenReturn(testPermission);

        Permission result = permissionService.removePermissionFromRole("1", "role1");

        assertNotNull(result);
        verify(permissionRepository, times(1)).findById("1");
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }

    @Test
    @DisplayName("Should handle removing from null roleIds")
    void testRemovePermissionFromRoleWithNullRoleIds() {
        Permission permWithoutRoles = new Permission();
        permWithoutRoles.setId("2");
        permWithoutRoles.setRoleIds(null);
        
        when(permissionRepository.findById("2")).thenReturn(Optional.of(permWithoutRoles));
        when(permissionRepository.save(any(Permission.class))).thenReturn(permWithoutRoles);

        Permission result = permissionService.removePermissionFromRole("2", "role1");

        assertNotNull(result);
        verify(permissionRepository, times(1)).save(any(Permission.class));
    }
}
