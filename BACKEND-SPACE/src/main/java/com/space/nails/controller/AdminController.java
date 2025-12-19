package com.space.nails.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.space.nails.dto.DashboardStatsDTO;
import com.space.nails.model.*;
import com.space.nails.repository.*;
import com.space.nails.service.UsuarioService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UsuarioRepository usuarioRepository;
   
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UsuarioRepository usuarioRepository, 

                           UsuarioService usuarioService,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
    
    }



 
    
   

    public record UsuarioRequest(Long id, String nome, String email, String senha, Perfil perfil) {}

    @PostMapping("/usuarios")
    public ResponseEntity<?> salvarUsuario(@RequestBody UsuarioRequest req) {
        Usuario u;
        if (req.id != null) {
            u = usuarioRepository.findById(req.id).orElseThrow();
            u.setNome(req.nome);
            u.setEmail(req.email);
            u.setPerfil(req.perfil);
        } else {
            if (usuarioRepository.findByEmail(req.email).isPresent()) return ResponseEntity.badRequest().body("Email já existe");
            u = new Usuario();
            u.setNome(req.nome);
            u.setEmail(req.email);
            u.setPerfil(req.perfil);
            u.setDataCadastro(LocalDate.now());
            u.setAvatarUrl("https://ui-avatars.com/api/?name=" + req.nome.replace(" ", "+"));
        }
        if (req.senha != null && !req.senha.isBlank()) {
            u.setSenha(passwordEncoder.encode(req.senha));
        }
        usuarioRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Salvo com sucesso!"));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable Long id) {
        try {
            usuarioService.deletarUsuarioPeloAdmin(id);
            return ResponseEntity.ok(Map.of("message", "Excluído!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    