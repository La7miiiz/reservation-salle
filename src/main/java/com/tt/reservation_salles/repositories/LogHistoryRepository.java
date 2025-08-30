package com.tt.reservation_salles.repositories;

import com.tt.reservation_salles.entities.LogHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogHistoryRepository extends JpaRepository<LogHistory, Long> {
}
