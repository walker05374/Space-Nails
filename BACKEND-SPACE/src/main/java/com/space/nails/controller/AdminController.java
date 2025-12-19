package com.space.nails.controller;

import com.space.nails.dto.RegisterRequestDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Só o Admin acessa aqui
public class AdminController {

    private final UsuarioService usuarioService;

    // CORREÇÃO: Construtor Manual (Substituindo o @RequiredArgsConstructor)
    public AdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Cria um novo Profissional (Usuário)
    @PostMapping("/profissionais")
    public ResponseEntity<UsuarioDTO> criarProfissional(@RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(usuarioService.criarProfissional(request));
    }

    // Lista todos os usuários do sistema
    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }
}