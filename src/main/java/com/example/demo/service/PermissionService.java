package com.example.demo.service;

import com.example.demo.model.Permission;
import com.example.demo.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission createPermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission getPermissionById(String id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
    }

    public Permission updatePermission(String id, Permission permission) {
        permission.setId(id);
        return permissionRepository.save(permission);
    }

    public void deletePermission(String id) {
        permissionRepository.deleteById(id);
    }

    public List<Permission> getPermissionsByRole(String roleId) {
        return permissionRepository.findByRoleIdsContaining(roleId);
    }

    @Transactional
    public Permission assignPermissionToRole(String permissionId, String roleId) {
        Permission permission = getPermissionById(permissionId);
        if (permission.getRoleIds() == null) {
            permission.setRoleIds(new ArrayList<>());
        }
        if (!permission.getRoleIds().contains(roleId)) {
            permission.getRoleIds().add(roleId);
        }
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission removePermissionFromRole(String permissionId, String roleId) {
        Permission permission = getPermissionById(permissionId);
        if (permission.getRoleIds() != null) {
            permission.getRoleIds().remove(roleId);
        }
        return permissionRepository.save(permission);
    }
}
