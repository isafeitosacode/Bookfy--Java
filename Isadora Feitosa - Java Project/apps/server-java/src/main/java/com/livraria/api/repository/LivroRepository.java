package com.livraria.api.repository;

import com.livraria.api.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LivroRepository extends JpaRepository<Livro, Long> {
    Optional<Livro> findByGoogleBookId(String googleBookId);
}