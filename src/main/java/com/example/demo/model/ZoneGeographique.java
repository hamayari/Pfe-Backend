package com.example.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper=false)
@Document(collection = "zones_geographiques")
public class ZoneGeographique extends Nomenclature {
    private String gouvernement; // Gouvernement (ex: "Tunis", "Sfax", "Sousse")
    
    // Hérite de Nomenclature : id, code, libelle, description, actif, dateCreation, dateModification
}
