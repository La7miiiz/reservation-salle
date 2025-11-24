package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.Reservation;
import com.tt.reservation_salles.entities.Salle;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.entities.StatutReservation;
import com.tt.reservation_salles.repositories.ReservationRepository;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SalleRepository salleRepository;
    private final JwtUtil jwtUtil;

    public ReservationController(ReservationRepository reservationRepository,
                                 UtilisateurRepository utilisateurRepository,
                                 SalleRepository salleRepository,
                                 JwtUtil jwtUtil) {
        this.reservationRepository = reservationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.salleRepository = salleRepository;
        this.jwtUtil = jwtUtil;
    }

    private Utilisateur extractUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("⚠️ Token manquant ou invalide !");
        }
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.validateToken(token);
        String email = claims.getSubject();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Utilisateur non trouvé !"));
    }

    // ✅ GET toutes les réservations (ADMIN uniquement)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    // ✅ POST créer une réservation
    @PostMapping
    public ResponseEntity<Reservation> create(@RequestBody Map<String, Object> payload,
                                              @RequestHeader("Authorization") String authHeader) {
        Utilisateur utilisateur = extractUserFromToken(authHeader);

        Long salleId = Long.parseLong(payload.get("salleId").toString());
        LocalDateTime dateDebut = LocalDateTime.parse(payload.get("dateDebut").toString());
        LocalDateTime dateFin = LocalDateTime.parse(payload.get("dateFin").toString());

        if (dateDebut.isAfter(dateFin) || dateDebut.isEqual(dateFin)) {
            throw new RuntimeException("❌ La date de début doit être avant la date de fin !");
        }

        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("❌ Salle invalide !"));


        boolean conflict = reservationRepository.existsBySalleIdAndDateDebutBeforeAndDateFinAfter(
                salleId, dateFin, dateDebut
        );
        if (conflict) {
            throw new RuntimeException("❌ La salle est déjà réservée sur cet intervalle !");
        }

        // Mark as unavailable and save
        salleRepository.save(salle);

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setSalle(salle);
        reservation.setNom(salle.getNom());
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        reservation.setStatut(StatutReservation.ACTIVE);

        return ResponseEntity.ok(reservationRepository.save(reservation));
    }
    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        Utilisateur utilisateur = extractUserFromToken(authHeader);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        boolean isAdmin = utilisateur.getRole().name().equals("ADMIN");
        boolean isOwner = reservation.getUtilisateur().getId().equals(utilisateur.getId());
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("⚠️ Vous ne pouvez pas consulter une réservation qui ne vous appartient pas !");
        }
        return reservation;
    }

    // ✅ PUT mise à jour d’une réservation
    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id,
                                         @RequestBody Map<String, Object> payload,
                                         @RequestHeader("Authorization") String authHeader) {
        Utilisateur utilisateur = extractUserFromToken(authHeader);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        // Vérifier droits
        boolean isAdmin = utilisateur.getRole().name().equals("ADMIN");
        boolean isOwner = reservation.getUtilisateur().getId().equals(utilisateur.getId());
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("⚠️ Vous ne pouvez pas modifier une réservation qui ne vous appartient pas !");
        }

        // Mise à jour de la salle
        if (payload.containsKey("salleId")) {
            Long nouvelleSalleId = Long.parseLong(payload.get("salleId").toString());
            Salle nouvelleSalle = salleRepository.findById(nouvelleSalleId)
                    .orElseThrow(() -> new RuntimeException("❌ Salle invalide !"));
            reservation.setSalle(nouvelleSalle);
            reservation.setNom(nouvelleSalle.getNom());
        }

        // Mise à jour des dates
        if (payload.containsKey("dateDebut")) {
            reservation.setDateDebut(LocalDateTime.parse(payload.get("dateDebut").toString()));
        }
        if (payload.containsKey("dateFin")) {
            reservation.setDateFin(LocalDateTime.parse(payload.get("dateFin").toString()));
        }

        // Seul l'admin peut modifier le statut
        if (payload.containsKey("statut")) {
            if (!isAdmin) {
                throw new RuntimeException("⚠️ Seul un administrateur peut changer le statut d'une réservation !");
            }
            reservation.setStatut(StatutReservation.valueOf(payload.get("statut").toString().toUpperCase()));
        }

        return reservationRepository.save(reservation);
    }

    // ✅ DELETE supprimer une réservation (ADMIN uniquement)
    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Utilisateur user = extractUserFromToken(authHeader);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        boolean isOwner = reservation.getUtilisateur().getId().equals(user.getId());
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("⚠️ Vous ne pouvez supprimer que vos propres réservations !");
        }
        reservationRepository.delete(reservation);
    }


    // ✅ Historique du user connecté
    @GetMapping("/me")
    public List<Reservation> getMyReservations(@RequestHeader("Authorization") String authHeader) {
        Utilisateur user = extractUserFromToken(authHeader);
        return reservationRepository.findByUtilisateurId(user.getId());
    }

    // 🔹 Filtrer mes réservations par statut
    @GetMapping("/me/statut/{statut}")
    public List<Reservation> getMyReservationsByStatut(@PathVariable StatutReservation statut,
                                                       @RequestHeader("Authorization") String authHeader) {
        Utilisateur user = extractUserFromToken(authHeader);
        return reservationRepository.findByUtilisateurIdAndStatut(user.getId(), statut);
    }
}
