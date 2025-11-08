package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.Salle;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.security.JwtUtil; // Import JwtUtil
import io.jsonwebtoken.Claims;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")

@RestController
@RequestMapping("/api/salles")
public class SalleController {

    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtUtil jwtUtil; // Inject JwtUtil

    public SalleController(SalleRepository salleRepository, UtilisateurRepository utilisateurRepository, JwtUtil jwtUtil) {
        this.salleRepository = salleRepository;
        this.utilisateurRepository = utilisateurRepository;
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

    // ✅ Lister toutes les salles (accessible à tous)
    @GetMapping
    public List<Salle> getAll() {
        return salleRepository.findAll();
    }

    // ✅ Récupérer une salle par ID (accessible à tous)
    @GetMapping("/{id}")
    public Salle getById(@PathVariable Long id) {
        return salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Salle non trouvée avec id : " + id));
    }

    // 🚫 Ajouter une salle (ADMIN seulement)
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')") // Sécurisé avec Spring Security
    public Salle create(@RequestBody Salle salle) {
        return salleRepository.save(salle);
    }

    // 🚫 Mettre à jour une salle (ADMIN seulement)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Sécurisé avec Spring Security
    public Salle update(@PathVariable Long id, @RequestBody Salle salleDetails) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Salle non trouvée avec id : " + id));

        salle.setNom(salleDetails.getNom());
        salle.setCapacite(salleDetails.getCapacite());
        salle.setDisponible(salleDetails.isDisponible());

        return salleRepository.save(salle);
    }

    // 🚫 Supprimer une salle (ADMIN seulement)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')") // Sécurisé avec Spring Security
    public String delete(@PathVariable Long id) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Salle non trouvée avec id : " + id));

        salleRepository.delete(salle);
        return "✅ Salle supprimée avec succès (id=" + id + ")";
    }
}
