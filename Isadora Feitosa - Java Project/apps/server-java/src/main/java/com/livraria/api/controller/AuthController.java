package com.livraria.api.controller;

import com.livraria.api.dto.LoginRequest;
import com.livraria.api.model.Usuario;
import com.livraria.api.repository.UsuarioRepository;
import com.livraria.api.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api") // <--- ATENÇÃO: Agora ele responde direto em /api
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private TokenService tokenService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login") // Vai gerar: /api/login
    public ResponseEntity<?> login(@RequestBody LoginRequest loginData) {
        
        // Mesma lógica que estava no UsuarioController
        Optional<Usuario> usuarioOp = repository.findByEmailOrUsername(loginData.getEmail(), loginData.getEmail());

        if (usuarioOp.isEmpty()) {
            return ResponseEntity.status(401).body("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOp.get();

        if (!passwordEncoder.matches(loginData.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Senha incorreta.");
        }

        String token = tokenService.gerarToken(usuario);

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("usuario", usuario);
        resposta.put("token", token);

        return ResponseEntity.ok(resposta);
    }
}