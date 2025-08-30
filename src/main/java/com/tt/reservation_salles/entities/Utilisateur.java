package com.tt.reservation_salles.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String email;
    private String motDePasse;

    @Enumerated(EnumType.STRING) // stocke "ADMIN" ou "CLIENT" dans la base
    private Role role;

    @OneToMany(mappedBy = "utilisateur")
    @JsonManagedReference
    private List<Reservation> reservations;
}
