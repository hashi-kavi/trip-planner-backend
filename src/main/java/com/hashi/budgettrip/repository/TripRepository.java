package com.hashi.budgettrip.repository;

import com.hashi.budgettrip.model.Trip;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @EntityGraph(attributePaths = "activities")
    @Override
    List<Trip> findAll();

    @EntityGraph(attributePaths = "activities")
    Optional<Trip> findById(Long id);

    // Fixed query - need to select both trip and the sum
    @Query("SELECT t, COALESCE(SUM(a.cost), 0) FROM Trip t LEFT JOIN t.activities a WHERE t.id = :tripId GROUP BY t")
    Object[] findTripWithSpentAmount(@Param("tripId") Long tripId);

    // Alternative approach that might work better:
    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.activities WHERE t.id = :tripId")
    Optional<Trip> findByIdWithActivities(@Param("tripId") Long tripId);
}