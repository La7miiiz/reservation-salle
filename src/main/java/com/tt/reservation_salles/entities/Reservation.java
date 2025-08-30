package com.tt.reservation_salles.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @JsonBackReference
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    private StatutReservation statut = StatutReservation.ACTIVE; // par défaut

}
