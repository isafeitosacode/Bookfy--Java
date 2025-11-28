package com.livraria.api.dto;

import java.util.List;

public class AdicionarLivroRequest {
    private String google_book_id;
    private String titulo;
    private Object autores; // Pode vir como Lista ou String do front, vamos tratar no controller
    private String capa_url;
    private String descricao;
    private String status; // 'Quero Ler', 'Lendo', etc.

    // Getters e Setters
    public String getGoogle_book_id() { return google_book_id; }
    public void setGoogle_book_id(String google_book_id) { this.google_book_id = google_book_id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    
    public Object getAutores() { return autores; }
    public void setAutores(Object autores) { this.autores = autores; }
    
    public String getCapa_url() { return capa_url; }
    public void setCapa_url(String capa_url) { this.capa_url = capa_url; }
    
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}