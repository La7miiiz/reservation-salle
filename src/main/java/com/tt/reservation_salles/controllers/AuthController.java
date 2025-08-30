package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.LogHistory;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.LogHistoryRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // ⚡ important pour React
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final LogHistoryRepository logHistoryRepository;

    public AuthController(UtilisateurRepository utilisateurRepository, LogHistoryRepository logHistoryRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.logHistoryRepository = logHistoryRepository;
    }

    // SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Utilisateur utilisateur) {
        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email déjà utilisé !"));
        }

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);

        logHistoryRepository.save(new LogHistory("SIGNUP", savedUser.getEmail(), savedUser.getRole()));

        return ResponseEntity.ok(Map.of(
                "message", "Inscription réussie ✅",
                "user", savedUser
        ));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utilisateur utilisateur, HttpSession session) {
        Utilisateur existing = utilisateurRepository.findByEmail(utilisateur.getEmail())
                .orElse(null);

        if (existing == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Utilisateur non trouvé !"));
        }

        if (!existing.getMotDePasse().equals(utilisateur.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Mot de passe incorrect !"));
        }

        // ⚡ enregistrer l'utilisateur en session
        session.setAttribute("user", existing);

        logHistoryRepository.save(new LogHistory("LOGIN", existing.getEmail(), existing.getRole()));

        return ResponseEntity.ok(Map.of(
                "message", "Connexion réussie ✅ Bienvenue " + existing.getNom(),
                "user", existing
        ));
    }

    // LOGOUT
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");
        if (user != null) {
            logHistoryRepository.save(new LogHistory("LOGOUT", user.getEmail(), user.getRole()));
        }
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie ✅"));
    }

    // ⚡ Vérifier si l'utilisateur est connecté
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        Utilisateur user = (Utilisateur) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non connecté"));
        }
        return ResponseEntity.ok(user);
    }
}
