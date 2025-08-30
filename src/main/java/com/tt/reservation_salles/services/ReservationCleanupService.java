package com.tt.reservation_salles.services;

import com.tt.reservation_salles.entities.Reservation;
import com.tt.reservation_salles.entities.StatutReservation;
import com.tt.reservation_salles.repositories.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationCleanupService {

    private final ReservationRepository reservationRepository;

    public ReservationCleanupService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // Vérifie toutes les minutes si des réservations doivent expirer
    @Scheduled(fixedRate = 60000) // 60 000 ms = 1 minute
    public void mettreAJourReservationsExpirees() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expirees = reservationRepository.findByDateFinBeforeAndStatut(now, StatutReservation.ACTIVE);

        for (Reservation r : expirees) {
            r.setStatut(StatutReservation.EXPIREE);
        }

        if (!expirees.isEmpty()) {
            reservationRepository.saveAll(expirees);
            System.out.println("⏳ " + expirees.size() + " réservations expirées mises à jour.");
        }
    }
}
