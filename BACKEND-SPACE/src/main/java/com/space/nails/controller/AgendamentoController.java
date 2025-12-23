package com.space.nails.controller;

import com.space.nails.dto.AgendamentoDTO;
import com.space.nails.model.StatusAgendamento;
import com.space.nails.service.AgendamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    // CONSTRUTOR MANUAL (Sem Lombok)
    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody AgendamentoDTO dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return ResponseEntity.ok(agendamentoService.criarAgendamento(dto, auth.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<AgendamentoDTO>> listar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(agendamentoService.listarMeusAgendamentos(auth.getName()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AgendamentoDTO> atualizarStatus(@PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String novoStatus = payload.get("status");
        return ResponseEntity
                .ok(agendamentoService.atualizarStatus(id, StatusAgendamento.valueOf(novoStatus), auth.getName()));
    }
}