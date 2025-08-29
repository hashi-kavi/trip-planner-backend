package com.hashi.budgettrip.controller;

import com.hashi.budgettrip.model.Activity;
import com.hashi.budgettrip.model.Trip;
import com.hashi.budgettrip.repository.ActivityRepository;
import com.hashi.budgettrip.repository.TripRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "http://localhost:3000")
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final TripRepository tripRepository;

    public ActivityController(ActivityRepository activityRepository, TripRepository tripRepository) {
        this.activityRepository = activityRepository;
        this.tripRepository = tripRepository;
    }

    // Create new activity - WITHOUT date field
    @PostMapping
    public ResponseEntity<?> createActivity(@RequestBody ActivityRequest request) {
        try {
            Activity activity = new Activity();
            activity.setName(request.getName());
            activity.setLocation(request.getLocation());
            activity.setTime(request.getTime());
            activity.setCost(request.getCost());
            activity.setNotes(request.getNotes());

            Optional<Trip> tripOptional = tripRepository.findById(request.getTripId());
            if (tripOptional.isPresent()) {
                activity.setTrip(tripOptional.get());
                Activity savedActivity = activityRepository.save(activity);
                return ResponseEntity.ok(savedActivity);
            } else {
                return ResponseEntity.badRequest().body("Trip not found with ID: " + request.getTripId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to save activity: " + e.getMessage());
        }
    }

    // Get all activities
    @GetMapping
    public ResponseEntity<?> getAllActivities() {
        try {
            return ResponseEntity.ok(activityRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching activities");
        }
    }

    // Get activities by trip ID
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<?> getActivitiesByTripId(@PathVariable Long tripId) {
        try {
            List<Activity> activities = activityRepository.findByTrip_Id(tripId);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching activities for trip");
        }
    }

    // Delete activity
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            activityRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting activity");
        }
    }

    // Update activity - WITHOUT date field
    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @RequestBody ActivityRequest request) {
        try {
            Optional<Activity> activityOptional = activityRepository.findById(id);
            if (activityOptional.isPresent()) {
                Activity activity = activityOptional.get();
                activity.setName(request.getName());
                activity.setLocation(request.getLocation());
                activity.setTime(request.getTime());
                activity.setCost(request.getCost());
                activity.setNotes(request.getNotes());

                // Only update trip if tripId is provided and different
                if (request.getTripId() != null &&
                        (activity.getTrip() == null || !activity.getTrip().getId().equals(request.getTripId()))) {
                    Optional<Trip> tripOptional = tripRepository.findById(request.getTripId());
                    if (tripOptional.isPresent()) {
                        activity.setTrip(tripOptional.get());
                    } else {
                        return ResponseEntity.badRequest().body("Trip not found with ID: " + request.getTripId());
                    }
                }

                Activity updatedActivity = activityRepository.save(activity);
                return ResponseEntity.ok(updatedActivity);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to update activity: " + e.getMessage());
        }
    }

    // Request DTO for activity creation - WITHOUT date field
    public static class ActivityRequest {
        private String name;
        private String location;
        private java.time.LocalTime time;
        private Double cost;
        private String notes;
        private Long tripId;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public java.time.LocalTime getTime() { return time; }
        public void setTime(java.time.LocalTime time) { this.time = time; }

        public Double getCost() { return cost; }
        public void setCost(Double cost) { this.cost = cost; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Long getTripId() { return tripId; }
        public void setTripId(Long tripId) { this.tripId = tripId; }
    }
}