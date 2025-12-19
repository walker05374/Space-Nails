package com.space.nails.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.space.nails.model.Cliente;
import com.space.nails.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final GmailEmailService emailService;
    
    @Value("${app.frontend.url}")
    private String frontendBaseUrl;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, GmailEmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void atualizarNome(String email, String novoNome) {
        Cliente cliente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        cliente.setNome(novoNome);
        usuarioRepository.save(cliente);
    }

    @Transactional
    public void atualizarAvatar(String email, String novaAvatarUrl) {
        Cliente cliente = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        cliente.setAvatarUrl(novaAvatarUrl);
        usuarioRepository.save(cliente);
    }

    // --- RECUPERAÇÃO DE SENHA ---
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<Cliente> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isPresent()) {
            Cliente cliente = usuarioOptional.get();
            String token = UUID.randomUUID().toString();
            cliente.setResetToken(token);
            cliente.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(cliente);

            String resetLink = frontendBaseUrl + "/resetar-senha?token=" + token;
            emailService.sendPasswordResetEmail(cliente.getEmail(), cliente.getNome(), resetLink);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Cliente cliente = usuarioRepository.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Token inválido."));
            
        if (cliente.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
             throw new RuntimeException("Token expirado.");
        }

        cliente.setSenha(passwordEncoder.encode(newPassword));
        cliente.setResetToken(null);
        cliente.setResetTokenExpiresAt(null);
        usuarioRepository.save(cliente);
    }
}