package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "permissions")
@Data
public class Permission {
    
    @Id
    private String id;
    
    private String name; // Ex: USER_CREATE, USER_READ, USER_UPDATE, USER_DELETE
    private String description;
    
    // Relation avec les rôles
    private List<String> roleIds;
}
