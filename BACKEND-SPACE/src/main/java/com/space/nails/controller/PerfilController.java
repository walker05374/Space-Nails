package com.space.nails.controller;

import com.space.nails.dto.UpdateUsuarioDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.model.Usuario;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @PutMapping("/me")
    public ResponseEntity<UsuarioDTO> atualizarMeuPerfil(@RequestBody UpdateUsuarioDTO request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));

        return ResponseEntity.ok(usuarioService.atualizarUsuario(usuario.getId(), request));
    }
}