package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.Reservation;
import com.tt.reservation_salles.entities.Salle;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.entities.StatutReservation;
import com.tt.reservation_salles.repositories.ReservationRepository;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.security.JwtUtil; // Import JwtUtil
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SalleRepository salleRepository;
    private final JwtUtil jwtUtil; // Inject JwtUtil

    public ReservationController(ReservationRepository reservationRepository,
                                 UtilisateurRepository utilisateurRepository,
                                 SalleRepository salleRepository,
                                 JwtUtil jwtUtil) { // Add to constructor
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

    // ✅ GET toutes les réservations
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Only ADMINs can get all reservations
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

        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new RuntimeException("❌ Salle invalide !"));

        if (!salle.isDisponible()) {
            throw new RuntimeException("❌ Salle déjà occupée !");
        }

        salle.setDisponible(false); // occuper la salle
        salleRepository.save(salle); // Save the updated salle status

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setSalle(salle);
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        reservation.setStatut(StatutReservation.ACTIVE);

        return ResponseEntity.ok(reservationRepository.save(reservation));
    }

    // ✅ PUT mise à jour d’une réservation
    @PutMapping("/{id}")
    public Reservation updateReservation(@PathVariable Long id,
                                         @RequestBody Map<String, Object> payload,
                                         @RequestHeader("Authorization") String authHeader) {
        Utilisateur utilisateur = extractUserFromToken(authHeader);
        Optional<Reservation> opt = reservationRepository.findById(id);

        if (opt.isEmpty()) {
            throw new RuntimeException("❌ Réservation non trouvée !");
        }

        Reservation reservation = opt.get();
        // Check if the user is the owner of the reservation or is an ADMIN
        if (!reservation.getUtilisateur().getId().equals(utilisateur.getId()) && !utilisateur.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("⚠️ Vous ne pouvez pas modifier une réservation qui ne vous appartient pas !");
        }

        // 🔹 Mise à jour de la salle
        if (payload.containsKey("salleId")) {
            Long nouvelleSalleId = Long.parseLong(payload.get("salleId").toString());
            Salle nouvelleSalle = salleRepository.findById(nouvelleSalleId)
                    .orElseThrow(() -> new RuntimeException("❌ Salle invalide !"));
            if (nouvelleSalle.isDisponible()) {
                reservation.getSalle().setDisponible(true); // libérer l’ancienne salle
                nouvelleSalle.setDisponible(false); // occuper la nouvelle salle
                reservation.setSalle(nouvelleSalle);
            } else {
                throw new RuntimeException("❌ Salle déjà occupée !");
            }
        }

        // 🔹 Mise à jour des dates
        if (payload.containsKey("dateDebut")) {
            reservation.setDateDebut(LocalDateTime.parse(payload.get("dateDebut").toString()));
        }
        if (payload.containsKey("dateFin")) {
            reservation.setDateFin(LocalDateTime.parse(payload.get("dateFin").toString()));
        }

        // 🔹 Mise à jour du statut
        if (payload.containsKey("statut")) {
            reservation.setStatut(StatutReservation.valueOf(payload.get("statut").toString().toUpperCase()));
        }

        return reservationRepository.save(reservation);
    }

    // ✅ PUT annuler ma réservation
    @PutMapping("/me/{id}/annuler")
    public Reservation annulerMaReservation(@PathVariable Long id,
                                            @RequestHeader("Authorization") String authHeader) {
        Utilisateur user = extractUserFromToken(authHeader);

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        if (!reservation.getUtilisateur().getId().equals(user.getId())) {
            throw new RuntimeException("⚠️ Vous ne pouvez pas annuler une réservation qui ne vous appartient pas !");
        }

        if (reservation.getStatut() == StatutReservation.EXPIREE ||
                reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RuntimeException("⚠️ La réservation est déjà terminée ou annulée.");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.getSalle().setDisponible(true); // libérer la salle
        salleRepository.save(reservation.getSalle()); // Save the updated salle status

        return reservationRepository.save(reservation);
    }

    // ✅ DELETE supprimer une réservation (réservé aux ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Use Spring Security for role check
    public void deleteReservation(@PathVariable Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Réservation non trouvée !"));

        reservation.getSalle().setDisponible(true); // libérer la salle
        salleRepository.save(reservation.getSalle()); // Save the updated salle status
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
