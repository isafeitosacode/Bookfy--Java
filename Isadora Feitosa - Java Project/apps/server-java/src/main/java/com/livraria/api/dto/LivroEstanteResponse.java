package com.livraria.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.livraria.api.model.Livro;
import com.livraria.api.model.LivroEstante;

public class LivroEstanteResponse {
    
    // Dados da Associação
    @JsonProperty("id_associacao")
    private Long idAssociacao;
    private String status;

    // Dados do Livro (Achatados)
    @JsonProperty("google_book_id")
    private String googleBookId;
    private String titulo;
    private String autores;
    @JsonProperty("capa_url")
    private String capaUrl;
    private String descricao;

    // Construtor que faz a mágica de "tirar da gaveta"
    public LivroEstanteResponse(LivroEstante associacao) {
        this.idAssociacao = associacao.getId();
        this.status = associacao.getStatus();
        
        Livro livro = associacao.getLivro();
        if (livro != null) {
            this.googleBookId = livro.getGoogleBookId();
            this.titulo = livro.getTitulo();
            this.autores = livro.getAutores();
            this.capaUrl = livro.getCapaUrl();
            this.descricao = livro.getDescricao();
        }
    }

    // Getters (necessários para gerar o JSON)
    public Long getIdAssociacao() { return idAssociacao; }
    public String getStatus() { return status; }
    public String getGoogleBookId() { return googleBookId; }
    public String getTitulo() { return titulo; }
    public String getAutores() { return autores; }
    public String getCapaUrl() { return capaUrl; }
    public String getDescricao() { return descricao; }
}