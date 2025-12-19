package com.space.nails.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.space.nails.model.Usuario;
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
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        usuario.setNome(novoNome);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void atualizarAvatar(String email, String novaAvatarUrl) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
        usuario.setAvatarUrl(novaAvatarUrl);
        usuarioRepository.save(usuario);
    }

    // --- RECUPERAÇÃO DE SENHA ---
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiresAt(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            String resetLink = frontendBaseUrl + "/resetar-senha?token=" + token;
            emailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNome(), resetLink);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
            .orElseThrow(() -> new RuntimeException("Token inválido."));
            
        if (usuario.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
             throw new RuntimeException("Token expirado.");
        }

        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiresAt(null);
        usuarioRepository.save(usuario);
    }
}