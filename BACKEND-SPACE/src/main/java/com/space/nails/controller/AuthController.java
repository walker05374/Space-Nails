package com.space.nails.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.space.nails.dto.*;
import com.space.nails.model.Perfil;
import com.space.nails.model.Usuario;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.security.JwtService;
import com.space.nails.service.UsuarioService;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    // --- REGISTRO GENÉRICO ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email já está em uso."));
        }
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(request.nome());
        novoUsuario.setEmail(request.email());
        novoUsuario.setSenha(passwordEncoder.encode(request.senha()));
        
        // Define perfil padrão como USUARIO ou similar. 
        // Verifique se o seu Enum Perfil tem 'USUARIO' ou 'CLIENTE', caso contrário mantenha RESPONSAVEL por enquanto e altere o Enum depois.
        novoUsuario.setPerfil(Perfil.RESPONSAVEL); // <-- Altere para Perfil.CLIENTE quando atualizar o Enum
        
        novoUsuario.setAvatarUrl(request.avatarUrl());
        novoUsuario.setDataCadastro(LocalDate.now());
        usuarioRepository.save(novoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Conta criada com sucesso!"));
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
            Usuario usuario = (Usuario) authentication.getPrincipal();
            String token = jwtService.generateToken(usuario);
            
            return ResponseEntity.ok(new LoginResponseDTO(
                token, 
                usuario.getNome(), 
                usuario.getEmail(), 
                usuario.getPerfil(),
                usuario.getAvatarUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Email ou senha inválidos."));
        }
    }

    // --- ATUALIZAÇÃO DE PERFIL ---
    public record PerfilCompletoDTO(String name, String nome, String avatarUrl) {}

    @PutMapping("/meu-perfil")
    public ResponseEntity<?> updatePerfil(@AuthenticationPrincipal UserDetails userDetails, @RequestBody PerfilCompletoDTO request) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        try {
            String email = userDetails.getUsername();
            String novoNome = request.nome() != null ? request.nome() : request.name();
            
            if (novoNome != null && !novoNome.isBlank()) {
                usuarioService.atualizarNome(email, novoNome);
            }
            if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
                usuarioService.atualizarAvatar(email, request.avatarUrl());
            }

            return ResponseEntity.ok(Map.of("message", "Perfil atualizado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Erro ao atualizar."));
        }
    }

    // --- RECUPERAÇÃO DE SENHA ---
    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            usuarioService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of("message", "Se o e-mail existir, o link foi enviado."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro ao enviar e-mail."));
        }
    }

    @PostMapping("/resetar-senha")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            usuarioService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}