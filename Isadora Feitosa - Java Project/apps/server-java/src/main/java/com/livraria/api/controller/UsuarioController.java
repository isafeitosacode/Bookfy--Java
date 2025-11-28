package com.livraria.api.controller;

import com.livraria.api.model.Usuario;
import com.livraria.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.livraria.api.dto.CadastroRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import com.livraria.api.dto.LoginRequest; 
import com.livraria.api.service.TokenService; 
import java.util.HashMap; 
import java.util.Map; 

@RestController
@RequestMapping("/api/usuarios") // Mesma rota do Node
@CrossOrigin(origins = "*") // Permite o front acessar
public class UsuarioController {

    @Autowired
    private UsuarioRepository repository;
    
    @Autowired
    private TokenService tokenService; // <--- Injetar o serviço de token

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Helper para pegar usuário logado (igual usamos nos outros controllers)
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        throw new RuntimeException("Usuário não autenticado");
    }

    // RF01.1 – Consultar usuário (GET /api/usuarios/{username})
    @GetMapping("/{username}")
    public ResponseEntity<Usuario> consultarUsuario(@PathVariable String username) {
        Optional<Usuario> usuario = repository.findByUsername(username);

        if (usuario.isEmpty()) {
            return ResponseEntity.status(404).build();
        }
        // O campo senha já é removido automaticamente pelo @JsonIgnore na Entidade
        return ResponseEntity.ok(usuario.get());
    }

@PostMapping
    public ResponseEntity<?> cadastrarUsuario(@RequestBody CadastroRequest dados) {
        
        // 1. Validação
        if (dados.getUsername() == null || dados.getEmail() == null || dados.getPassword() == null) {
            return ResponseEntity.badRequest().body("Todos os campos são obrigatórios.");
        }

        try {
            Usuario novoUsuario = new Usuario();
            novoUsuario.setUsername(dados.getUsername());
            novoUsuario.setEmail(dados.getEmail());

            // 2. Criptografia
            String hash = passwordEncoder.encode(dados.getPassword());
            novoUsuario.setPassword(hash);

            // 3. Conversão das Preferências (JSON)
            // Se tiver preferências, converte. Se não, salva como lista vazia "[]"
            if (dados.getPreferencias() != null) {
                String jsonPreferencias = new ObjectMapper().writeValueAsString(dados.getPreferencias());
                novoUsuario.setPreferenciasLiterarias(jsonPreferencias);
            } else {
                novoUsuario.setPreferenciasLiterarias("[]");
            }

            // 4. Salvar no Banco
            Usuario salvo = repository.save(novoUsuario);
            return ResponseEntity.status(201).body(salvo);

        } catch (Exception e) {
            // 5. Tratamento de Erros (Duplicidade ou erro geral)
            if (e.getMessage() != null && e.getMessage().contains("constraint")) {
                return ResponseEntity.status(409).body("Email ou username já cadastrado.");
            }
            // Se der erro no JSON ou no Banco, cai aqui
            return ResponseEntity.status(500).body("Erro ao cadastrar: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        
        // 1. Busca usuário por Email OU Username (igual ao Node)
        Optional<Usuario> usuarioOp = repository.findByEmailOrUsername(loginData.getEmail(), loginData.getEmail());

        if (usuarioOp.isEmpty()) {
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }

        Usuario usuario = usuarioOp.get();

        // 2. Verifica a senha (Bcrypt)
        if (!passwordEncoder.matches(loginData.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }

        // 3. Gera o Token
        String token = tokenService.gerarToken(usuario);

        // 4. Monta a resposta exatamente como o Front espera: { usuario: {...}, token: "..." }
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("usuario", usuario);
        resposta.put("token", token);

        return ResponseEntity.ok(resposta);
    }

@PatchMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Long id, @RequestBody Usuario dadosNovos) {
        Usuario usuarioLogado = getUsuarioLogado();

        // SEGURANÇA: Impede que o usuário mude o perfil de outra pessoa pela URL
        if (!usuarioLogado.getId().equals(id)) {
            return ResponseEntity.status(403).body("Você não tem permissão para editar este usuário.");
        }

        // Carrega o usuário do banco para garantir que está atualizado
        Usuario usuarioBanco = repository.findById(id).get();

        // Atualiza só o que veio preenchido
        if (dadosNovos.getNome() != null) usuarioBanco.setNome(dadosNovos.getNome());
        if (dadosNovos.getFotoPerfilUrl() != null) usuarioBanco.setFotoPerfilUrl(dadosNovos.getFotoPerfilUrl());
        if (dadosNovos.getBiografia() != null) usuarioBanco.setBiografia(dadosNovos.getBiografia());
        
        // Preferencias já tratamos no cadastro, mas se quiser editar aqui, precisaria da lógica do JSON
        if (dadosNovos.getPreferenciasLiterarias() != null) usuarioBanco.setPreferenciasLiterarias(dadosNovos.getPreferenciasLiterarias());

        repository.save(usuarioBanco);
        
        return ResponseEntity.ok(usuarioBanco);
    }

    // RF01.5 – Deletar (DELETE /api/usuarios/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarUsuario(@PathVariable Long id) {
        // TODO: Validar se o ID bate com o token
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}