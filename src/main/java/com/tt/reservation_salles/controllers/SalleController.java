package com.tt.reservation_salles.controllers;

import com.tt.reservation_salles.entities.Role;
import com.tt.reservation_salles.entities.Salle;
import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.repositories.SalleRepository;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salles")
public class SalleController {

    private final SalleRepository salleRepository;
    private final UtilisateurRepository utilisateurRepository;

    public SalleController(SalleRepository salleRepository, UtilisateurRepository utilisateurRepository) {
        this.salleRepository = salleRepository;
        this.utilisateurRepository = utilisateurRepository;
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
    public Salle create(@RequestBody Salle salle, @RequestParam Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès refusé ❌ (seuls les ADMIN peuvent ajouter une salle)");
        }

        return salleRepository.save(salle);
    }

    // 🚫 Mettre à jour une salle (ADMIN seulement)
    @PutMapping("/{id}")
    public Salle update(@PathVariable Long id, @RequestBody Salle salleDetails, @RequestParam Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès refusé ❌ (seuls les ADMIN peuvent modifier une salle)");
        }

        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Salle non trouvée avec id : " + id));

        salle.setNom(salleDetails.getNom());
        salle.setCapacite(salleDetails.getCapacite());
        salle.setDisponible(salleDetails.isDisponible());

        return salleRepository.save(salle);
    }

    // 🚫 Supprimer une salle (ADMIN seulement)
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id, @RequestParam Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé !"));

        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Accès refusé ❌ (seuls les ADMIN peuvent supprimer une salle)");
        }

        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("❌ Salle non trouvée avec id : " + id));

        salleRepository.delete(salle);
        return "✅ Salle supprimée avec succès (id=" + id + ")";
    }
}
