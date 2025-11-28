package com.livraria.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Estantes")
public class Estante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estante") // Mapeando explicitamente para o nome do seu banco antigo
    @JsonProperty("id_estante")
    private Long id;

    private String nome;
    
    // No seu Node não tinha descrição, mas se quiser adicionar no futuro pode.
    // Se não tiver no banco, o Hibernate ignora ou cria se estiver configurado.
    
    @Column(name = "id_usuario")
    @JsonProperty("id_usuario")
    private Long usuarioId;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
}