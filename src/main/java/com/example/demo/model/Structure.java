package com.example.demo.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper=false)
@Document(collection = "structures")
public class Structure extends Nomenclature {
    private String typeStructure;
    private String adresse;
    private String zoneGeographiqueId;
    private String gouvernement; // Nouveau champ pour le gouvernement
}
