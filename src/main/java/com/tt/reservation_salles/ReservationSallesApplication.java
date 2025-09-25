package com.tt.reservation_salles;

import com.tt.reservation_salles.entities.Utilisateur;
import com.tt.reservation_salles.entities.Role;
import com.tt.reservation_salles.repositories.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReservationSallesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationSallesApplication.class, args);
	}

	// This runs automatically at startup
	@Bean
	CommandLineRunner initAdmin(UtilisateurRepository utilisateurRepository) {
		return args -> {
			// Check if admin already exists
			if (utilisateurRepository.findByEmail("admin@gmail.com").isEmpty()) {
				Utilisateur admin = new Utilisateur();
				admin.setNom("Admin");
				admin.setEmail("admin@gmail.com");
				admin.setMotDePasse("adminadmin"); // plain password
				admin.setRole(Role.ADMIN); // assuming Role enum has ADMIN
				utilisateurRepository.save(admin);

				System.out.println("✅ Admin account created: admin@gmail.com / adminadmin");
			} else {
				System.out.println("ℹ️ Admin account already exists");
			}
		};
	}
}
