package com.livraria.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Livros_Estante")
public class LivroEstante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_associacao")
    @JsonProperty("id_associacao")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_estante_fk")
    private Estante estante;

    @ManyToOne
    @JoinColumn(name = "id_livro_fk", referencedColumnName = "google_book_id")
    private Livro livro;

    @Column(name = "id_usuario") // Redundante, mas facilita a performance igual no seu Node
    private Long usuarioId;

    private String status; // 'Quero Ler', 'Lendo', etc.

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Estante getEstante() { return estante; }
    public void setEstante(Estante estante) { this.estante = estante; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}