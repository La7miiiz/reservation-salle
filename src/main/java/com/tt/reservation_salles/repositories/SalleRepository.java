package com.tt.reservation_salles.repositories;

import com.tt.reservation_salles.entities.Salle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalleRepository extends JpaRepository<Salle, Long> {}