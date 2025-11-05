package com.example.GestionComida.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {
    @NotBlank private String token;
    @Size(min = 8) private String password;

    public ResetPasswordRequest() {}
    public ResetPasswordRequest(String token, String password){ this.token = token; this.password = password; }

    public String getToken(){ return token; }
    public void setToken(String token){ this.token = token; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
}
