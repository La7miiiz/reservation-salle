package com.tt.reservation_salles.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data // Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok generates a no-argument constructor
@AllArgsConstructor // Lombok generates a constructor with all fields
@Table(name = "log_history") // Good practice to explicitly name the table
public class LogHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String email;
    private String role;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Custom constructor for logging actions
    public LogHistory(String action, String email, String role) {
        this.action = action;
        this.email = email;
        this.role = role;
    }
}
