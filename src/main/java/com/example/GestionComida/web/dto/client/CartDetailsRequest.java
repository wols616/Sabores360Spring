package com.example.GestionComida.web.dto.client;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CartDetailsRequest {
    @NotNull @NotEmpty
    private List<Integer> ids;

    public CartDetailsRequest() {}

    public CartDetailsRequest(List<Integer> ids) { this.ids = ids; }

    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids; }
}
