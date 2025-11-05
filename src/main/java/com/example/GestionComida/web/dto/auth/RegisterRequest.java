package com.example.GestionComida.web.dto.auth;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank private String name;
    @Email @NotBlank private String email;
    private String address;
    @Size(min = 8) private String password;

    public RegisterRequest() {}
    public RegisterRequest(String name, String email, String address, String password){
        this.name = name; this.email = email; this.address = address; this.password = password;
    }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
    public String getAddress(){ return address; }
    public void setAddress(String address){ this.address = address; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
}
