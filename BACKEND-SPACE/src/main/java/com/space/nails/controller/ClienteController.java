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

    // --- CONSTRUTOR MANUAL (CORREÇÃO DO ERRO) ---
    public ClienteController(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Pega o Profissional que está logado no momento
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado"));
    }

    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Usuario profissional = getUsuarioLogado();
        
        // VINCULA O CLIENTE AO PROFISSIONAL LOGADO
        cliente.setProfissional(profissional); 
        
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarMeusClientes() {
        Usuario profissional = getUsuarioLogado();
        
        // Se for ADMIN, vê todos. Se for PROFISSIONAL, vê só os seus.
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

        // Segurança: Só deixa editar se o cliente for desse profissional (ou se for Admin)
        if (!cliente.getProfissional().getId().equals(profissional.getId()) 
            && profissional.getRole() != Usuario.Role.ADMIN) {
            throw new RuntimeException("Acesso negado: Este cliente pertence a outro profissional.");
        }

        cliente.setNome(dados.getNome());
        cliente.setTelefone(dados.getTelefone());
        cliente.setObservacoes(dados.getObservacoes());
        
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }
}