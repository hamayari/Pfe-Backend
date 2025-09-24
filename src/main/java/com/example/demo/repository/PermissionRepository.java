package com.example.demo.repository;

import com.example.demo.model.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PermissionRepository extends MongoRepository<Permission, String> {
    
    List<Permission> findByRoleIdsContaining(String roleId);
    
    Permission findByName(String name);
}
