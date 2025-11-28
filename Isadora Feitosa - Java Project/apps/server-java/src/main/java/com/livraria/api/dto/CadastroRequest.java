package com.livraria.api.dto;

import java.util.List;

public class CadastroRequest {
    private String username;
    private String email;
    private String password;
    private List<String> preferencias; // O Front manda uma lista, o Java recebe como lista

    // Getters e Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<String> getPreferencias() { return preferencias; }
    public void setPreferencias(List<String> preferencias) { this.preferencias = preferencias; }
}