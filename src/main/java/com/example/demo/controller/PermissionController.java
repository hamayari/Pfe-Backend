package com.example.demo.controller;

import com.example.demo.model.Permission;
import com.example.demo.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public List<Permission> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @PostMapping
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionService.createPermission(permission);
    }

    @PutMapping("/{id}")
    public Permission updatePermission(@PathVariable String id, @RequestBody Permission permission) {
        return permissionService.updatePermission(id, permission);
    }

    @DeleteMapping("/{id}")
    public void deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
    }

    @PostMapping("/{permissionId}/roles/{roleId}")
    public Permission assignToRole(@PathVariable String permissionId, @PathVariable String roleId) {
        return permissionService.assignPermissionToRole(permissionId, roleId);
    }

    @DeleteMapping("/{permissionId}/roles/{roleId}")
    public Permission removeFromRole(@PathVariable String permissionId, @PathVariable String roleId) {
        return permissionService.removePermissionFromRole(permissionId, roleId);
    }
}
