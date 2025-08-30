package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.LogHistory;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.repositories.LogHistoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final LogHistoryRepository logHistoryRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository , LogHistoryRepository logHistoryRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.logHistoryRepository = logHistoryRepository;
    }

    // GET : récupérer tous les utilisateurs
    @GetMapping
    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    // GET : récupérer un utilisateur par ID
    @GetMapping("/{id}")
    public Utilisateur getById(@PathVariable Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id " + id));
    }

    // PUT : modifier un utilisateur existant
    @PutMapping("/{id}")
    public Utilisateur update(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        Utilisateur existing = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec id " + id));

        existing.setNom(utilisateur.getNom());
        existing.setEmail(utilisateur.getEmail());
        existing.setRole(utilisateur.getRole());

        return utilisateurRepository.save(existing);
    }

    // DELETE : supprimer un utilisateur
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        utilisateurRepository.deleteById(id);
    }
    // ADMIN : voir tous les logs
    @GetMapping("/logs")
    public List<LogHistory> getLogs(HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");
        if (user == null || !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Accès refusé ❌ (réservé aux admins)");
        }

        return logHistoryRepository.findAll();
    }
}
