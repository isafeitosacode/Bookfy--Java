package com.livraria.api.controller;

import com.livraria.api.model.Estante;
import com.livraria.api.model.Usuario;
import com.livraria.api.repository.EstanteRepository;
import com.livraria.api.repository.LivroEstanteRepository;
import com.livraria.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api") // O front chama /api/estantes e /api/livros/aleatorios
@CrossOrigin(origins = "*")
public class EstanteController {

    @Autowired private EstanteRepository estanteRepository;
    @Autowired private LivroEstanteRepository livroEstanteRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RestTemplate restTemplate; // Para chamar o Google Books

    @Value("${google.books.api-key}") // Adicione isso no application.properties depois!
    private String googleApiKey;

// --- MÉTODO CORRIGIDO ---
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Verifica se tem alguém logado e se é o nosso Usuário
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal(); // Pega o usuário direto da memória! Sem ir no banco.
        }
        
        throw new RuntimeException("Usuário não encontrado ou token inválido.");
    }

    // 1. Busca Estantes (GET /api/estantes)
    @GetMapping("/estantes")
    public List<Estante> buscaEstante() {
        Usuario user = getUsuarioLogado();
        return estanteRepository.findByUsuarioIdOrderByNomeAsc(user.getId());
    }

    // 2. Cria Estante (POST /api/estantes)
    @PostMapping("/estantes")
    public ResponseEntity<?> criaEstante(@RequestBody Estante novaEstante) {
        Usuario user = getUsuarioLogado();

        if (novaEstante.getNome() == null || novaEstante.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "O nome da estante é obrigatório."));
        }

        if (estanteRepository.existsByNomeAndUsuarioId(novaEstante.getNome().trim(), user.getId())) {
            return ResponseEntity.status(409).body(Map.of("message", "Uma estante com este nome já existe."));
        }

        novaEstante.setNome(novaEstante.getNome().trim());
        novaEstante.setUsuarioId(user.getId());
        
        Estante salva = estanteRepository.save(novaEstante);
        return ResponseEntity.status(201).body(salva);
    }

    // 3. Atualiza Estante (PUT /api/estantes/{id})
    @PutMapping("/estantes/{id}")
    public ResponseEntity<?> atualizaNomeEstante(@PathVariable Long id, @RequestBody Estante dados) {
        Usuario user = getUsuarioLogado();
        
        Optional<Estante> estanteOp = estanteRepository.findByIdAndUsuarioId(id, user.getId());
        if (estanteOp.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Estante não encontrada."));
        }

        Estante estante = estanteOp.get();
        estante.setNome(dados.getNome().trim());
        
        try {
            estanteRepository.save(estante);
            return ResponseEntity.ok(Map.of("message", "Estante atualizada com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(409).body(Map.of("message", "Erro ao atualizar (nome duplicado?)."));
        }
    }

    // 4. Deleta Estante (DELETE /api/estantes/{id})
    @DeleteMapping("/estantes/{id}")
    public ResponseEntity<?> deletaEstante(@PathVariable Long id) {
        Usuario user = getUsuarioLogado();

        if (!estanteRepository.existsById(id)) { // Simplificado
             return ResponseEntity.status(404).body(Map.of("message", "Estante não encontrada."));
        }
        
        // Primeiro deleta os livros da estante (Cascade manual)
        livroEstanteRepository.deleteByEstanteIdAndUsuarioId(id, user.getId());
        // Depois deleta a estante
        estanteRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // 5. Busca Aleatória - Google Books (GET /api/livros/aleatorios)
    @GetMapping("/livros/aleatorios")
    public ResponseEntity<?> buscaAleatoria() {
        Usuario user = getUsuarioLogado();
        
        // Pega as preferências (Assume que já é uma String limpa ou JSON)
        // Simplificação: Vamos pegar uma categoria fixa se falhar
        String[] interesses = {"software development", "science fiction", "history"};
        
        // Sorteia
        String categoria = interesses[(int) (Math.random() * interesses.length)];
        
        String url = "https://www.googleapis.com/books/v1/volumes?q=" + categoria + "&maxResults=18&key=" + googleApiKey;
        
        try {
            // Chama o Google
            Map resposta = restTemplate.getForObject(url, Map.class);
            return ResponseEntity.ok(resposta.get("items"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao comunicar com Google Books"));
        }
    }
}