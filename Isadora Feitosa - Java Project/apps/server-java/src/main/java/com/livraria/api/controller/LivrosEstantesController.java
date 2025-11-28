package com.livraria.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.api.dto.AdicionarLivroRequest;
import com.livraria.api.model.Estante;
import com.livraria.api.model.Livro;
import com.livraria.api.model.LivroEstante;
import com.livraria.api.model.Usuario;
import com.livraria.api.repository.EstanteRepository;
import com.livraria.api.repository.LivroEstanteRepository;
import com.livraria.api.repository.LivroRepository;
import com.livraria.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.livraria.api.dto.LivroEstanteResponse;
import java.util.stream.Collectors;

import java.util.Map;
import java.util.Optional;
import java.util.List; // <--- O erro é por causa dessa falta!
import java.util.stream.Collectors; // <--- Vai precisar dessa pro .collect funcionar

// Adicione esse import lá em cima:
    import java.net.URLDecoder;
    import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LivrosEstantesController {

    @Autowired private LivroRepository livroRepository;
    @Autowired private EstanteRepository estanteRepository;
    @Autowired private LivroEstanteRepository livroEstanteRepository;
    @Autowired private UsuarioRepository usuarioRepository; // Adicionado
    
    // --- Auxiliar para pegar Usuário ---
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        // Fallback: Tenta buscar pelo username se o Principal for string (acontece em alguns filtros)
        if (auth != null) {
             return usuarioRepository.findByUsername(auth.getName()).orElseThrow();
        }
        throw new RuntimeException("Usuário não autenticado");
    }

    // 1. Adicionar Livro na Estante (Agora aceita ID ou NOME)
    @PostMapping("/estantes/{identificador}/livros")
    public ResponseEntity<?> adicionaLivroEstante(@PathVariable String identificador, @RequestBody AdicionarLivroRequest dados) {
        Usuario user = getUsuarioLogado();

        Optional<Estante> estanteOp;

        // VERIFICAÇÃO: É um número (ID) ou Texto (Nome)?
        if (identificador.matches("\\d+")) {
            // É número: busca por ID
            Long idEstante = Long.parseLong(identificador);
            estanteOp = estanteRepository.findByIdAndUsuarioId(idEstante, user.getId());
        } else {
            // É texto: busca por Nome (ex: "Programação")
            // O Spring já decodifica o URL (Programa%C3%A7%C3%A3o -> Programação) automaticamente
            estanteOp = estanteRepository.findByNomeIgnoreCaseAndUsuarioId(identificador, user.getId());
        }

        if (estanteOp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Estante '" + identificador + "' não encontrada."));
        }
        Estante estante = estanteOp.get();

        // --- DAQUI PRA BAIXO É IGUAL AO ANTERIOR ---

        // B. Valida ou Cria o Livro na tabela 'Livros'
        Optional<Livro> livroOp = livroRepository.findByGoogleBookId(dados.getGoogle_book_id());
        Livro livro;
        
        if (livroOp.isPresent()) {
            livro = livroOp.get();
        } else {
            livro = new Livro();
            livro.setGoogleBookId(dados.getGoogle_book_id());
            livro.setTitulo(dados.getTitulo());
            livro.setCapaUrl(dados.getCapa_url());
            livro.setDescricao(dados.getDescricao());
            
            try {
                String autoresJson = new ObjectMapper().writeValueAsString(dados.getAutores());
                livro.setAutores(autoresJson);
            } catch (Exception e) {
                livro.setAutores("[]");
            }
            
            livro = livroRepository.save(livro);
        }

        // C. Verifica se já está na estante
        if (livroEstanteRepository.existsByEstanteIdAndLivroGoogleBookId(estante.getId(), livro.getGoogleBookId())) {
            return ResponseEntity.status(409).body(Map.of("message", "Este livro já está nesta estante."));
        }

        // D. Cria a relação
        LivroEstante relacao = new LivroEstante();
        relacao.setEstante(estante);
        relacao.setLivro(livro);
        relacao.setUsuarioId(user.getId());
        relacao.setStatus(dados.getStatus() != null ? dados.getStatus() : "Quero Ler");

        livroEstanteRepository.save(relacao);

        return ResponseEntity.status(201).body(Map.of("message", "Livro adicionado com sucesso!"));
    }

// 2. Listar Livros da Estante (GET /api/estantes/{identificador}/livros)
@GetMapping("/estantes/{identificador}/livros")
    public ResponseEntity<?> listaLivrosEstante(@PathVariable String identificador) {
        Usuario user = getUsuarioLogado();
        
        Optional<Estante> estanteOp;

        if (identificador.matches("\\d+")) {
            // É número
            Long idEstante = Long.parseLong(identificador);
            estanteOp = estanteRepository.findByIdAndUsuarioId(idEstante, user.getId());
        } else {
            // É texto: Vamos DECODIFICAR para garantir que acentos funcionem
            String nomeDecodificado = URLDecoder.decode(identificador, StandardCharsets.UTF_8);
            
            // Usamos o novo método IgnoreCase
            estanteOp = estanteRepository.findByNomeIgnoreCaseAndUsuarioId(nomeDecodificado, user.getId());
        }

        if (estanteOp.isEmpty()) {
             return ResponseEntity.status(404).body(Map.of("message", "Estante não encontrada."));
        }
        
        Estante estante = estanteOp.get();
       List<LivroEstante> listaBruta = livroEstanteRepository.findByEstanteIdAndUsuarioId(estante.getId(), user.getId());
        
        // Converte a lista aninhada para a lista achatada (igual ao Node)
        List<LivroEstanteResponse> listaFormatada = listaBruta.stream()
                .map(LivroEstanteResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(listaFormatada);
    }

    // 3. Remover Livro (DELETE /api/estantes/{shelfId}/livros/{bookId})
    @DeleteMapping("/estantes/{shelfId}/livros/{bookId}")
    public ResponseEntity<?> removeLivroEstante(@PathVariable Long shelfId, @PathVariable String bookId) {
        Usuario user = getUsuarioLogado();

        livroEstanteRepository.deleteByEstanteIdAndLivroGoogleBookIdAndUsuarioId(shelfId, bookId, user.getId());
        
        return ResponseEntity.noContent().build();
    }
}