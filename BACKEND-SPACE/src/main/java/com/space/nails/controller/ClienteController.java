package com.space.nails.controller;

import com.space.nails.model.Cliente;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ClienteRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.space.nails.repository.AgendamentoRepository agendamentoRepository;

    public ClienteController(ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            com.space.nails.repository.AgendamentoRepository agendamentoRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));
    }

    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Usuario profissional = getUsuarioLogado();
        cliente.setProfissional(profissional);
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarMeusClientes() {
        Usuario profissional = getUsuarioLogado();
        if (profissional.getRole() == Usuario.Role.ADMIN) {
            return ResponseEntity.ok(clienteRepository.findAll());
        }
        return ResponseEntity.ok(clienteRepository.findByProfissional(profissional));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> editarCliente(@PathVariable Long id, @RequestBody Cliente dados) {
        Usuario profissional = getUsuarioLogado();
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getProfissional().getId().equals(profissional.getId())
                && profissional.getRole() != Usuario.Role.ADMIN) {
            throw new RuntimeException("Acesso negado.");
        }

        cliente.setNome(dados.getNome());
        cliente.setTelefone(dados.getTelefone());
        cliente.setObservacoes(dados.getObservacoes());

        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    // --- NOVO: MÉTODO DE EXCLUIR ---
    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Void> excluirCliente(@PathVariable Long id) {
        try {
            Usuario profissional = getUsuarioLogado();
            Cliente cliente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            // Segurança: Só apaga se for o dono ou Admin
            if (!cliente.getProfissional().getId().equals(profissional.getId())
                    && profissional.getRole() != Usuario.Role.ADMIN) {
                throw new RuntimeException("Acesso negado: Você não pode excluir este cliente.");
            }

            // 1. Verificaa se tem agendamentos futuros
            if (agendamentoRepository.existsByClienteAndDataHoraAfter(cliente, java.time.LocalDateTime.now())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT,
                        "Este cliente possui agendamentos futuros. Cancele-os antes de excluir.");
            }

            List<com.space.nails.model.Agendamento> agendamentos = agendamentoRepository.findByCliente(cliente);
            agendamentoRepository.deleteAll(agendamentos);

            // 2. Remove o cliente
            clienteRepository.delete(cliente);
            return ResponseEntity.noContent().build();
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // Console do Backend
            throw new RuntimeException("Erro ao excluir cliente: " + e.getMessage());
        }
    }
}