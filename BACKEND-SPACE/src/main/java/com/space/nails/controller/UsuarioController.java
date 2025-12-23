package com.space.nails.controller;

import com.space.nails.model.Usuario;
import com.space.nails.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/ping")
    public ResponseEntity<Void> ping(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            usuarioService.atualizarUltimoAcesso(usuario.getId());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/offline")
    public ResponseEntity<Void> offline(@AuthenticationPrincipal Usuario usuario) {
        if (usuario != null) {
            usuarioService.registrarOffline(usuario.getId());
        }
        return ResponseEntity.ok().build();
    }
}
