package com.example.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper=false)
@Document(collection = "applications")
public class Application extends Nomenclature {
    // Hérite de Nomenclature : id, code, libelle, description, actif, dateCreation, dateModification
}
