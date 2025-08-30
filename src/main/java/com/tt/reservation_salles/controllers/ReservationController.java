package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.Reservation;
import com.tt.reservation_salles.entities.Salle;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.entities.StatutReservation;
import com.tt.reservation_salles.repositories.ReservationRepository;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SalleRepository salleRepository;

    public ReservationController(ReservationRepository reservationRepository,
                                 UtilisateurRepository utilisateurRepository,
                                 SalleRepository salleRepository) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.salleRepository = salleRepository;
    }

    // ✅ GET toutes les réservations
    @GetMapping
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    // ✅ POST créer une réservation
    @PostMapping
    public Reservation create(@RequestParam Long utilisateurId,
                              @RequestParam Long salleId,
                              @RequestParam String dateDebut,
                              @RequestParam String dateFin) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId).orElse(null);
        Salle salle = salleRepository.findById(salleId).orElse(null);

        if (utilisateur != null && salle != null && salle.isDisponible()) {
            salle.setDisponible(false); // occuper la salle

            Reservation reservation = new Reservation();
            reservation.setUtilisateur(utilisateur);
            reservation.setSalle(salle);
            reservation.setDateDebut(LocalDateTime.parse(dateDebut));
            reservation.setDateFin(LocalDateTime.parse(dateFin));
            reservation.setStatut(StatutReservation.ACTIVE);

            return reservationRepository.save(reservation);
        }
        throw new RuntimeException("❌ Impossible de créer la réservation (utilisateur/salle invalide ou salle déjà occupée)");
    }

    // ✅ PUT mise à jour d’une réservation
    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id,
                                         @RequestParam(required = false) Long salleId,
                                         @RequestParam(required = false) String dateDebut,
                                         @RequestParam(required = false) String dateFin,
                                         @RequestParam(required = false) String statut) {
        Optional<Reservation> opt = reservationRepository.findById(id);
        if (opt.isEmpty()) {
            throw new RuntimeException("❌ Réservation non trouvée !");
        }

        Reservation reservation = opt.get();

        // 🔹 Mise à jour de la salle
        if (salleId != null) {
            Salle nouvelleSalle = salleRepository.findById(salleId).orElse(null);
            if (nouvelleSalle != null && nouvelleSalle.isDisponible()) {
                reservation.getSalle().setDisponible(true); // libérer l’ancienne salle
                nouvelleSalle.setDisponible(false); // occuper la nouvelle salle
                reservation.setSalle(nouvelleSalle);
            } else {
                throw new RuntimeException("❌ Salle invalide ou déjà occupée !");
            }
        }

        // 🔹 Mise à jour des dates
        if (dateDebut != null) {
            reservation.setDateDebut(LocalDateTime.parse(dateDebut));
        }
        if (dateFin != null) {
            reservation.setDateFin(LocalDateTime.parse(dateFin));
        }

        // 🔹 Mise à jour du statut
        if (statut != null) {
            reservation.setStatut(StatutReservation.valueOf(statut.toUpperCase()));
        }

        return reservationRepository.save(reservation);
    }

    // ✅ PUT annuler ma réservation
    @PutMapping("/me/{id}/annuler")
    public Reservation annulerMaReservation(@PathVariable Long id, HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        if (user == null) {
            throw new RuntimeException("⚠️ Aucun utilisateur connecté !");
        }

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        // 🔹 Vérifier que la réservation appartient bien à l’utilisateur connecté
        if (!reservation.getUtilisateur().getId().equals(user.getId())) {
            throw new RuntimeException("⚠️ Vous ne pouvez pas annuler une réservation qui ne vous appartient pas !");
        }

        if (reservation.getStatut() == StatutReservation.EXPIREE ||
                reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RuntimeException("⚠️ La réservation est déjà terminée ou annulée.");
        }

        // 🔹 Annuler
        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.getSalle().setDisponible(true); // libérer la salle

        return reservationRepository.save(reservation);
    }

    // ✅ DELETE supprimer une réservation (réservé aux ADMIN)
    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id, HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        if (user == null) {
            throw new RuntimeException("⚠️ Aucun utilisateur connecté !");
        }

        // 🔹 Vérifier si l'utilisateur est ADMIN
        if (!"ADMIN".equalsIgnoreCase(String.valueOf(user.getRole()))) {
            throw new RuntimeException("❌ Vous n’avez pas l’autorisation de supprimer une réservation !");
        }

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        reservation.getSalle().setDisponible(true); // libérer la salle
        reservationRepository.delete(reservation);
    }
    // ✅ Historique du user connecté
    @GetMapping("/me")
    public List<Reservation> getMyReservations(HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        if (user == null) {
            throw new RuntimeException("⚠️ Aucun utilisateur connecté !");
        }

        // Retourner uniquement les réservations de l’utilisateur connecté
        return reservationRepository.findByUtilisateurId(user.getId());
    }
    // 🔹 Filtrer mes réservations par statut
    @GetMapping("/me/statut/{statut}")
    public List<Reservation> getMyReservationsByStatut(@PathVariable StatutReservation statut, HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        if (user == null) {
            throw new RuntimeException("⚠️ Aucun utilisateur connecté !");
        }

        return reservationRepository.findByUtilisateurIdAndStatut(user.getId(), statut);
    }
}
