package com.livraria.api.controller;

import com.livraria.api.dto.LivroEstanteResponse;
import com.livraria.api.dto.StatusRequest;
import com.livraria.api.model.LivroEstante;
import com.livraria.api.model.Usuario;
import com.livraria.api.repository.LivroEstanteRepository;
import com.livraria.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StatusLivrosController {

    @Autowired private LivroEstanteRepository livroEstanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // --- Auxiliar para pegar Usuário ---
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        throw new RuntimeException("Usuário não autenticado");
    }

    // 1. Atualizar Status (PATCH /api/livros_estante/{id}/status)
    @PatchMapping("/livros_estante/{idAssociacao}/status")
    public ResponseEntity<?> atualizaStatusLivro(@PathVariable Long idAssociacao, @RequestBody StatusRequest dados) {
        Usuario user = getUsuarioLogado();

        if (dados.getStatus() == null || dados.getStatus().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "O novo status é obrigatório."));
        }

        // Busca a associação pelo ID
        Optional<LivroEstante> associacaoOp = livroEstanteRepository.findById(idAssociacao);

        if (associacaoOp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Livro na estante não encontrado."));
        }

        LivroEstante associacao = associacaoOp.get();

        // SEGURANÇA: Verifica se essa associação pertence ao usuário logado
        if (!associacao.getUsuarioId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Você não tem permissão para alterar este livro."));
        }

        // Atualiza e Salva
        associacao.setStatus(dados.getStatus());
        livroEstanteRepository.save(associacao);

        return ResponseEntity.ok(Map.of("message", "Status atualizado com sucesso!"));
    }

    // 2. Filtrar por Status (GET /api/livros/status/{status})
    // Ex: /api/livros/status/Lendo
    @GetMapping("/livros/status/{status}")
    public ResponseEntity<?> buscaStatusLivro(@PathVariable String status) {
        Usuario user = getUsuarioLogado();

        // Decodifica URL (Lendo%20Agora -> Lendo Agora)
        String statusDecodificado = URLDecoder.decode(status, StandardCharsets.UTF_8);

        List<LivroEstante> listaBruta = livroEstanteRepository.findByStatusAndUsuarioId(statusDecodificado, user.getId());

        // Converte para o DTO achatado para o Front ler os dados do livro
        List<LivroEstanteResponse> listaFormatada = listaBruta.stream()
                .map(LivroEstanteResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(listaFormatada);
    }
}