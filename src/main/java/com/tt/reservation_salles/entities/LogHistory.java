package com.tt.reservation_salles.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class LogHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String email;
    private String role;
    private LocalDateTime timestamp = LocalDateTime.now();

    public LogHistory(String signup, String email, Role role) {}

    public LogHistory(String action, String email, String role) {
        this.action = action;
        this.email = email;
        this.role = role;
        this.timestamp = LocalDateTime.now();
    }
}
