package com.space.nails.controller;

import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ServicoRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    public ServicoController(ServicoRepository servicoRepository, UsuarioRepository usuarioRepository) {
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Servico>> listarMeusServicos() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Retorna apenas os serviços deste profissional
        return ResponseEntity.ok(servicoRepository.findByProfissional(profissional));
    }

    @PostMapping
    public ResponseEntity<Servico> criarServico(@RequestBody Servico servico) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        servico.setProfissional(profissional);
        return ResponseEntity.ok(servicoRepository.save(servico));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        servicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}