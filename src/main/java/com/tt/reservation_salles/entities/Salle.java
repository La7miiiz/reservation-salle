package com.tt.reservation_salles.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private int capacite;
    private boolean disponible = true;


}