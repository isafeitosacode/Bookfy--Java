package com.livraria.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Livros") // Garanta que o nome está igual ao do banco
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_book_id", unique = true)
    @JsonProperty("google_book_id")
    private String googleBookId;

    private String titulo;
    
    // --- MUDANÇA AQUI: TEXT permite texto infinito ---
    @Column(columnDefinition = "TEXT") 
    private String autores; 

    @Column(name = "capa_url", columnDefinition = "TEXT") // URL as vezes é gigante
    @JsonProperty("capa_url")
    private String capaUrl;

    @Column(columnDefinition = "TEXT") // Sinopse é gigante
    private String descricao;

    // ... (Mantenha seus Getters e Setters abaixo) ...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGoogleBookId() { return googleBookId; }
    public void setGoogleBookId(String googleBookId) { this.googleBookId = googleBookId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutores() { return autores; }
    public void setAutores(String autores) { this.autores = autores; }
    public String getCapaUrl() { return capaUrl; }
    public void setCapaUrl(String capaUrl) { this.capaUrl = capaUrl; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}