package com.livraria.api.repository;

import com.livraria.api.model.LivroEstante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface LivroEstanteRepository extends JpaRepository<LivroEstante, Long> {
    // Busca livros de uma estante específica
    List<LivroEstante> findByEstanteIdAndUsuarioId(Long estanteId, Long usuarioId);

    // Busca livros por status (Quero Ler, etc)
    List<LivroEstante> findByStatusAndUsuarioId(String status, Long usuarioId);

    // Remove livro da estante
    @Transactional
    @Modifying
    void deleteByEstanteIdAndLivroGoogleBookIdAndUsuarioId(Long estanteId, String googleBookId, Long usuarioId);

    // Deleta tudo de uma estante (para quando deletar a estante)
    @Transactional
    @Modifying
    void deleteByEstanteIdAndUsuarioId(Long estanteId, Long usuarioId);
    
    // Verifica se já existe
    boolean existsByEstanteIdAndLivroGoogleBookId(Long estanteId, String googleBookId);
}