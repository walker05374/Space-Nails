package com.space.nails.controller;

import com.space.nails.dto.RegisterRequestDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.dto.UpdateUsuarioDTO;
import com.space.nails.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") 
public class AdminController {

    private final UsuarioService usuarioService;

    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/profissionais")
    public ResponseEntity<UsuarioDTO> criarProfissional(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(usuarioService.criarProfissional(request));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @PatchMapping("/usuarios/{id}/status")
    public ResponseEntity<Void> alternarStatus(@PathVariable Long id) {
        usuarioService.alternarStatus(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @RequestBody UpdateUsuarioDTO request) {
        return ResponseEntity.ok(usuarioService.atualizarUsuario(id, request));
    }
}