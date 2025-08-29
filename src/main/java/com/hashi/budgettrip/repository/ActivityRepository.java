package com.hashi.budgettrip.repository;

import com.hashi.budgettrip.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Get all activities for a specific trip
    List<Activity> findByTrip_Id(Long tripId);

    // Get total spent cost for a specific trip
    @Query("SELECT COALESCE(SUM(a.cost), 0) FROM Activity a WHERE a.trip.id = :tripId")
    Double sumCostByTripId(@Param("tripId") Long tripId);

}
