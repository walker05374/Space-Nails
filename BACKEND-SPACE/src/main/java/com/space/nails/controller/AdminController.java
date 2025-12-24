package com.space.nails.controller;

import com.space.nails.dto.RegisterRequestDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.dto.UpdateUsuarioDTO;
import com.space.nails.service.UsuarioService;
import com.space.nails.service.BackupService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioService usuarioService;
    private final BackupService backupService;

    public AdminController(UsuarioService usuarioService, BackupService backupService) {
        this.usuarioService = usuarioService;
        this.backupService = backupService;
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

    @PostMapping("/backup")
    public ResponseEntity<Resource> fazerBackup() {
        try {
            File file = backupService.gerarBackup();
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file.toPath()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restaurarBackup(@RequestParam("file") MultipartFile file) {
        try {
            String report = backupService.restaurarBackup(file);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erro ao restaurar: " + e.getMessage());
        }
    }
}