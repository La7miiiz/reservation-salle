package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.LogHistory;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.repositories.LogHistoryRepository;
import com.tt.reservation_salles.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:3000"}, allowCredentials = "true")

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final LogHistoryRepository logHistoryRepository;
    private final JwtUtil jwtUtil;

    public UtilisateurController(UtilisateurRepository utilisateurRepository, LogHistoryRepository logHistoryRepository, JwtUtil jwtUtil) {
        this.utilisateurRepository = utilisateurRepository;
        this.logHistoryRepository = logHistoryRepository;
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

    // ✅ GET : récupérer tous les utilisateurs (réservé aux ADMIN)
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    // ✅ GET : récupérer un utilisateur par ID (réservé aux ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Utilisateur getById(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id " + id));
    }

    // ✅ PUT : modifier un utilisateur existant (réservé aux ADMIN)
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

    // ✅ DELETE : supprimer un utilisateur (réservé aux ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
    }

    // ✅ ADMIN : voir tous les logs (réservé aux ADMIN)
    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<LogHistory> getLogs() {
        return logHistoryRepository.findAll();
    }
}
