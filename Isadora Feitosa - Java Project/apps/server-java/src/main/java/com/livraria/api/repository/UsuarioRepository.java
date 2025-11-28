package com.livraria.api.repository;

import com.livraria.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    
    // Para o login (email ou username)
    Optional<Usuario> findByEmailOrUsername(String email, String username);
}