package com.hashi.budgettrip.service;

import com.hashi.budgettrip.model.Trip;
import com.hashi.budgettrip.repository.TripRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TripService {
    private final TripRepository repo;

    public TripService(TripRepository repo) {
        this.repo = repo;
    }

    public Trip saveTrip(Trip trip) {
        return repo.save(trip);
    }

    public List<Trip> getAllTrips() {
        return repo.findAll();
    }

    public Trip getTripById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void deleteTrip(Long id) {
        repo.deleteById(id);
    }
}
