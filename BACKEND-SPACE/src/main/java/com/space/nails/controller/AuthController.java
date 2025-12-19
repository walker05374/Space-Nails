package com.space.nails.controller;

import com.space.nails.dto.ForgotPasswordRequest;
import com.space.nails.dto.LoginRequestDTO;
import com.space.nails.dto.LoginResponseDTO;
import com.space.nails.dto.ResetPasswordRequest;
import com.space.nails.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    // --- CONSTRUTOR MANUAL (A SOLUÇÃO) ---
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        usuarioService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("Se o e-mail existir, um token foi enviado.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        usuarioService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Senha redefinida com sucesso.");
    }
}