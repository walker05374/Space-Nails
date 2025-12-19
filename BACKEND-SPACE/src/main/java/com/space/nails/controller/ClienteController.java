package com.space.nails.controller;

import com.space.nails.model.Cliente;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ClienteRepository;
import com.space.nails.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    // Helper para pegar o usuário logado
    private Usuario getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco"));
    }

    // 1. Cadastrar Cliente (Vincula automaticamente ao profissional logado)
    @PostMapping
    public ResponseEntity<Cliente> criarCliente(@RequestBody Cliente cliente) {
        Usuario profissional = getUsuarioLogado();
        cliente.setProfissional(profissional); // Vincula o cliente ao dono do login
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }

    // 2. Listar Meus Clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarMeusClientes() {
        Usuario profissional = getUsuarioLogado();
        
        // Se for ADMIN, talvez queira ver todos? Se sim, descomente abaixo:
        // if (profissional.getRole() == Usuario.Role.ADMIN) return ResponseEntity.ok(clienteRepository.findAll());

        // Retorna apenas os clientes deste profissional
        return ResponseEntity.ok(clienteRepository.findByProfissional(profissional));
    }

    // 3. Editar Cliente
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> editarCliente(@PathVariable Long id, @RequestBody Cliente dadosAtualizados) {
        Usuario profissional = getUsuarioLogado();
        
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Segurança: Verifica se o cliente pertence ao profissional logado (ou se é Admin)
        if (!cliente.getProfissional().getId().equals(profissional.getId()) 
            && profissional.getRole() != Usuario.Role.ADMIN) {
            throw new RuntimeException("Acesso negado: Este cliente não é seu.");
        }

        cliente.setNome(dadosAtualizados.getNome());
        cliente.setTelefone(dadosAtualizados.getTelefone());
        cliente.setObservacoes(dadosAtualizados.getObservacoes());
        
        return ResponseEntity.ok(clienteRepository.save(cliente));
    }
}