package com.livraria.api.repository;

import com.livraria.api.model.Estante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstanteRepository extends JpaRepository<Estante, Long> {
    
    // Busca todas as estantes de um usuário
    List<Estante> findByUsuarioIdOrderByNomeAsc(Long usuarioId);
    
    // Busca uma estante específica garantindo que pertence ao usuário (Segurança)
    Optional<Estante> findByIdAndUsuarioId(Long id, Long usuarioId);
    
    // Verifica se já existe nome duplicado para aquele usuário
    boolean existsByNomeAndUsuarioId(String nome, Long usuarioId);

Optional<Estante> findByNomeIgnoreCaseAndUsuarioId(String nome, Long usuarioId);
}