package com.example.DisasterRelief.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReliefRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must not exceed 200 characters")
    private String address;

    @Min(value = 0, message = "Towel quantity must be 0 or greater")
    private int towel;

    @Min(value = 0, message = "Instant noodles quantity must be 0 or greater")
    private int instantNoodles;

    @Min(value = 0, message = "Tissue paper quantity must be 0 or greater")
    private int tissuePaper;

    @Min(value = 0, message = "Water quantity must be 0 or greater")
    private int water;

    public ReliefRequestDto() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTowel() {
        return towel;
    }

    public void setTowel(int towel) {
        this.towel = towel;
    }

    public int getInstantNoodles() {
        return instantNoodles;
    }

    public void setInstantNoodles(int instantNoodles) {
        this.instantNoodles = instantNoodles;
    }

    public int getTissuePaper() {
        return tissuePaper;
    }

    public void setTissuePaper(int tissuePaper) {
        this.tissuePaper = tissuePaper;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }
}
