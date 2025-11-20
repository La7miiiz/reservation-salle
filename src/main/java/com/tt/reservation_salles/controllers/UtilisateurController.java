package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.LogHistory;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.entities.StatutReservation;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.repositories.LogHistoryRepository;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.ReservationRepository;
import com.tt.reservation_salles.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final LogHistoryRepository logHistoryRepository;
    private final SalleRepository salleRepository;
    private final ReservationRepository reservationRepository;
    private final JwtUtil jwtUtil;

    public UtilisateurController(
            UtilisateurRepository utilisateurRepository,
            LogHistoryRepository logHistoryRepository,
            SalleRepository salleRepository,
            ReservationRepository reservationRepository,
            JwtUtil jwtUtil
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.logHistoryRepository = logHistoryRepository;
        this.salleRepository = salleRepository;
        this.reservationRepository = reservationRepository;
        this.jwtUtil = jwtUtil;
    }

    private Utilisateur extractUserFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("⚠️ Token manquant ou invalide !");
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.validateToken(token);
            String email = claims.getSubject();
            return utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("❌ Utilisateur non trouvé !"));
        } catch (Exception e) {
            throw new RuntimeException("❌ Token invalide", e);
        }
    }

    // ADMIN: Get all users
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    // ADMIN: Get user by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Utilisateur getById(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id " + id));
    }

    // ADMIN: Update user by ID
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Utilisateur update(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id " + id));

        existing.setNom(utilisateur.getNom());
        existing.setEmail(utilisateur.getEmail());
        existing.setRole(utilisateur.getRole());

        return utilisateurRepository.save(existing);
    }

    // ADMIN: Delete user by ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
    }

    // ADMIN: Get all logs
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<LogHistory> getLogs() {
        return logHistoryRepository.findAll();
    }

    // --- PROFILE ENDPOINTS ---

    // GET: Profile of current user
    @GetMapping("/me")
    public Utilisateur getMe(@RequestHeader("Authorization") String authHeader) {
        return extractUserFromToken(authHeader);
    }

    // PUT: Update current user's profile
    @PutMapping("/me")
    public Utilisateur updateMe(@RequestBody Utilisateur changes,
                                @RequestHeader("Authorization") String authHeader) {
        Utilisateur user = extractUserFromToken(authHeader);
        if (changes.getNom() != null) user.setNom(changes.getNom());
        if (changes.getEmail() != null) user.setEmail(changes.getEmail());
        if (changes.getAvatarSeed() != null) user.setAvatarSeed(changes.getAvatarSeed());
        // TODO: Secure password hash if you add password changes
        return utilisateurRepository.save(user);
    }

    // --- ADMIN DASHBOARD STATS ---

    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", utilisateurRepository.count());
        stats.put("rooms", salleRepository.count());
        stats.put("reservations", reservationRepository.count());
        stats.put("active", reservationRepository.countByStatut(StatutReservation.ACTIVE));
        stats.put("expired", reservationRepository.countByStatut(StatutReservation.EXPIREE));
        return stats;
    }
}
