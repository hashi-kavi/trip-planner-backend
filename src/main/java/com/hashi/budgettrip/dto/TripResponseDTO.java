package com.hashi.budgettrip.dto;

import java.time.LocalDate;

public class TripResponseDTO {
    private Long id;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalBudget;
    private String imagePath;
    private Double spent;

    // Constructors
    public TripResponseDTO() {}

    public TripResponseDTO(Long id, String destination, LocalDate startDate,
                           LocalDate endDate, Double totalBudget, String imagePath, Double spent) {
        this.id = id;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalBudget = totalBudget;
        this.imagePath = imagePath;
        this.spent = spent;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getTotalBudget() { return totalBudget; }
    public void setTotalBudget(Double totalBudget) { this.totalBudget = totalBudget; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Double getSpent() { return spent; }
    public void setSpent(Double spent) { this.spent = spent; }
}