package com.hashi.budgettrip.controller;

import com.hashi.budgettrip.dto.TripResponseDTO;
import com.hashi.budgettrip.model.Trip;
import com.hashi.budgettrip.repository.ActivityRepository;
import com.hashi.budgettrip.repository.TripRepository;
import com.hashi.budgettrip.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:3000")
public class TripController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TripService tripService;

    // Use absolute path for uploads directory
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    // Create uploads directory if it doesn't exist
    private void ensureUploadDirExists() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    // Create trip with image
    @PostMapping
    public ResponseEntity<?> addTrip(
            @RequestParam("destination") String destination,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("totalBudget") Double totalBudget,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            // Check file size and validate image
            if (image != null && !image.isEmpty()) {
                if (image.getSize() > 10 * 1024 * 1024) { // 10MB limit
                    return ResponseEntity.badRequest().body("File size too large. Maximum allowed is 10MB");
                }
                if (!image.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image files are allowed");
                }
            }

            ensureUploadDirExists();
            Trip trip = new Trip();
            trip.setDestination(destination);
            trip.setStartDate(LocalDate.parse(startDate));
            trip.setEndDate(LocalDate.parse(endDate));
            trip.setTotalBudget(totalBudget);

            if (image != null && !image.isEmpty()) {
                String fileName = generateUniqueFileName(image.getOriginalFilename());
                String filePath = UPLOAD_DIR + File.separator + fileName;

                image.transferTo(new File(filePath));
                trip.setImagePath("uploads/" + fileName);
            }

            Trip savedTrip = tripService.saveTrip(trip);

            // Return as DTO with spent amount (0 for new trip)
            TripResponseDTO response = new TripResponseDTO(
                    savedTrip.getId(),
                    savedTrip.getDestination(),
                    savedTrip.getStartDate(),
                    savedTrip.getEndDate(),
                    savedTrip.getTotalBudget(),
                    savedTrip.getImagePath(),
                    0.0 // New trip, no activities yet
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error saving trip: " + e.getMessage());
        }
    }

    // Get all trips with spent amounts
    @GetMapping
    public List<TripResponseDTO> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();

        return trips.stream().map(trip -> {
            Double spent = activityRepository.sumCostByTripId(trip.getId());
            if (spent == null) spent = 0.0;

            return new TripResponseDTO(
                    trip.getId(),
                    trip.getDestination(),
                    trip.getStartDate(),
                    trip.getEndDate(),
                    trip.getTotalBudget(),
                    trip.getImagePath(),
                    spent
            );
        }).collect(Collectors.toList());
    }

    // Get trip by ID with spent amount
    @GetMapping("/{id}")
    public ResponseEntity<TripResponseDTO> getTrip(@PathVariable Long id) {
        Optional<Trip> tripOptional = tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip trip = tripOptional.get();
            Double spent = activityRepository.sumCostByTripId(id);
            if (spent == null) spent = 0.0;

            TripResponseDTO response = new TripResponseDTO(
                    trip.getId(),
                    trip.getDestination(),
                    trip.getStartDate(),
                    trip.getEndDate(),
                    trip.getTotalBudget(),
                    trip.getImagePath(),
                    spent
            );

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Update trip
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrip(
            @PathVariable Long id,
            @RequestParam("destination") String destination,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("totalBudget") Double totalBudget,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {

        try {
            ensureUploadDirExists();

            Trip existingTrip = tripService.getTripById(id);
            if (existingTrip == null) {
                return ResponseEntity.notFound().build();
            }

            existingTrip.setDestination(destination);
            existingTrip.setStartDate(LocalDate.parse(startDate));
            existingTrip.setEndDate(LocalDate.parse(endDate));
            existingTrip.setTotalBudget(totalBudget);

            if (image != null && !image.isEmpty()) {
                // Validate image
                if (!image.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image files are allowed");
                }

                // Delete old image if exists
                if (existingTrip.getImagePath() != null) {
                    deleteImageFile(existingTrip.getImagePath());
                }

                String fileName = generateUniqueFileName(image.getOriginalFilename());
                String filePath = UPLOAD_DIR + File.separator + fileName;
                image.transferTo(new File(filePath));
                existingTrip.setImagePath("uploads/" + fileName);
            }

            Trip updatedTrip = tripService.saveTrip(existingTrip);

            // Calculate spent amount for response
            Double spent = activityRepository.sumCostByTripId(id);
            if (spent == null) spent = 0.0;

            TripResponseDTO response = new TripResponseDTO(
                    updatedTrip.getId(),
                    updatedTrip.getDestination(),
                    updatedTrip.getStartDate(),
                    updatedTrip.getEndDate(),
                    updatedTrip.getTotalBudget(),
                    updatedTrip.getImagePath(),
                    spent
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating trip: " + e.getMessage());
        }
    }

    // Delete trip
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        if (trip != null && trip.getImagePath() != null) {
            deleteImageFile(trip.getImagePath());
        }
        tripService.deleteTrip(id);
        return ResponseEntity.noContent().build();
    }

    private String generateUniqueFileName(String originalFilename) {
        // Generate unique filename to prevent overwrites
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    // Delete image file when trip is deleted or image is updated
    private void deleteImageFile(String imagePath) {
        try {
            if (imagePath != null && imagePath.startsWith("uploads/")) {
                String fileName = imagePath.substring("uploads/".length());
                File file = new File(UPLOAD_DIR + File.separator + fileName);
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            System.err.println("Error deleting image file: " + e.getMessage());
        }
    }
}