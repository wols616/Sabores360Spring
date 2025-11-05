package com.example.GestionComida.web.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangeStatusRequest {
    @NotBlank
    private String newStatus; // 'Confirmado','En preparación','En camino','Entregado','Cancelado'
    private String notes;

    public ChangeStatusRequest() {}

    public ChangeStatusRequest(String newStatus, String notes) {
        this.newStatus = newStatus;
        this.notes = notes;
    }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
