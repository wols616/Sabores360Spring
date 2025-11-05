// src/main/java/com/example/GestionComida/web/dto/seller/AvailabilityRequest.java
package com.example.GestionComida.web.dto.seller;

import jakarta.validation.constraints.NotNull;

public class AvailabilityRequest {
    @NotNull
    private Boolean available;

    public AvailabilityRequest() {}

    public AvailabilityRequest(Boolean available) {
        this.available = available;
    }

    public Boolean getAvailable() {
        return available;
    }
    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
