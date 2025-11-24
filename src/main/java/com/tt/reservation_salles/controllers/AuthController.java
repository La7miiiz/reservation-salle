package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.LogHistory;
import com.tt.reservation_salles.entities.Role;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.LogHistoryRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import com.tt.reservation_salles.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins    = "http://localhost:4200")
public class AuthController {

    private final UtilisateurRepository utilisateurRepository;
    private final LogHistoryRepository logHistoryRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UtilisateurRepository utilisateurRepository,
                          LogHistoryRepository logHistoryRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.utilisateurRepository = utilisateurRepository;
        this.logHistoryRepository = logHistoryRepository;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Utilisateur utilisateur) {
        if (utilisateur.getMotDePasse() == null || utilisateur.getMotDePasse().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe est obligatoire"));
        }

        if (utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email déjà utilisé !"));
        }

        if (utilisateur.getEmail() == null || utilisateur.getEmail().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "entrez votre Email !"));
        }

        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Role.CLIENT);
        }

        if (utilisateur.getNom() == null || utilisateur.getNom().isEmpty()){
            return ResponseEntity.badRequest().body(Map.of("error", "entrez votre nom !"));
        }

        utilisateur.setMotDePasse(utilisateur.getMotDePasse());

        Utilisateur savedUser = utilisateurRepository.save(utilisateur);
        return ResponseEntity.ok(savedUser);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utilisateur utilisateur) {
        if (utilisateur.getEmail() == null || utilisateur.getMotDePasse() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email et mot de passe sont obligatoires"));
        }

        Optional<Utilisateur> optionalUser = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (optionalUser.isEmpty() || !optionalUser.get().getMotDePasse().equals(utilisateur.getMotDePasse())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou mot de passe incorrect"));
        }

        Utilisateur existing = optionalUser.get();
        logHistoryRepository.save(new LogHistory("LOGIN", existing.getEmail(), existing.getRole().name()));

        String token = jwtUtil.generateToken(existing.getEmail(), existing.getRole().name());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", existing.getId());
        userData.put("nom", existing.getNom());
        userData.put("email", existing.getEmail());
        userData.put("role", existing.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Connexion réussie ✅ Bienvenue " + existing.getNom(),
                "token", token,
                "user", userData
        ));
    }





    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                var claims = jwtUtil.validateToken(token);
                String email = claims.getSubject();
                utilisateurRepository.findByEmail(email)
                        .ifPresent(user -> logHistoryRepository.save(new LogHistory("LOGOUT", user.getEmail(), user.getRole().name())));
            } catch (Exception ignored) { }
        }
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie ✅"));
    }

    // ---------------- GET CURRENT USER ----------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token manquant"));
        }

        String token = authHeader.substring(7);
        try {
            var claims = jwtUtil.validateToken(token);
            String email = claims.getSubject();
            Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utilisateur non trouvé"));
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token invalide"));
        }
    }
}
