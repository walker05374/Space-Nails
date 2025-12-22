package com.space.nails.controller;

import com.space.nails.model.AgendaConfig;
import com.space.nails.model.Usuario;
import com.space.nails.repository.AgendaConfigRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {

    private final AgendaConfigRepository agendaConfigRepository;
    private final UsuarioRepository usuarioRepository;

    public AgendaController(AgendaConfigRepository agendaConfigRepository, UsuarioRepository usuarioRepository) {
        this.agendaConfigRepository = agendaConfigRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<AgendaConfig>> getMinhaAgenda() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        return ResponseEntity.ok(agendaConfigRepository.findByProfissional(profissional));
    }

    @PostMapping
    public ResponseEntity<List<AgendaConfig>> salvarAgenda(@RequestBody List<AgendaConfig> configs) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        for (AgendaConfig config : configs) {
            config.setProfissional(profissional);

            // Busca se já existe config para o dia, para atualizar ID
            agendaConfigRepository.findByProfissionalAndDiaSemana(profissional, config.getDiaSemana())
                    .ifPresent(existing -> config.setId(existing.getId()));

            agendaConfigRepository.save(config);
        }

        return ResponseEntity.ok(agendaConfigRepository.findByProfissional(profissional));
    }
}
