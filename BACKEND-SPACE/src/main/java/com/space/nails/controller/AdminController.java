package com.space.nails.controller;

import com.space.nails.dto.RegisterRequestDTO;
import com.space.nails.dto.UpdateUsuarioDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;

    @PostMapping("/profissionais")
    public ResponseEntity<UsuarioDTO> criarProfissional(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(usuarioService.criarProfissional(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long id, @RequestBody UpdateUsuarioDTO request) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request));
    }
}