package com.space.nails.controller;

import com.space.nails.dto.LoginRequestDTO;
import com.space.nails.dto.LoginResponseDTO;
import com.space.nails.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }
    
    // Endpoints de forgot-password e reset-password ficariam aqui
}