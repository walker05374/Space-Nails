package com.space.nails.service;

import com.space.nails.dto.*;
import com.space.nails.model.Usuario;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GmailEmailService emailService; // Certifique-se de ter este serviço criado

    // --- LOGIN ---
    public LoginResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        var token = jwtService.generateToken(user);
        
        return LoginResponseDTO.builder()
                .token(token)
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .avatar(user.getFotoUrl())
                .build();
    }

    // --- ADMIN CRIA PROFISSIONAL ---
    public UsuarioDTO criarProfissional(RegisterRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado!");
        }

        Usuario novoProfissional = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .senha(passwordEncoder.encode(request.getPassword()))
                .role(Usuario.Role.PROFISSIONAL)
                .fotoUrl(request.getAvatarUrl())
                .build();

        usuarioRepository.save(novoProfissional);
        return mapToDTO(novoProfissional);
    }

    // --- ATUALIZAR DADOS (Genérico) ---
    public UsuarioDTO atualizarUsuario(Long id, UpdateUsuarioDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (request.getNome() != null) usuario.setNome(request.getNome());
        if (request.getTelefone() != null) usuario.setTelefone(request.getTelefone());
        
        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Este novo e-mail já está em uso.");
            }
            usuario.setEmail(request.getEmail());
        }

        if (request.getNovaSenha() != null && !request.getNovaSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        }

        usuarioRepository.save(usuario);
        return mapToDTO(usuario);
    }

    // --- RECUPERAÇÃO DE SENHA ---
    public void requestPasswordReset(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("E-mail não encontrado"));

        String token = UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuarioRepository.save(usuario);

        // Envio de e-mail (ajuste a mensagem conforme necessário)
        emailService.sendEmail(
            usuario.getEmail(), 
            "Recuperação de Senha", 
            "Seu token de recuperação é: " + token
        );
    }

    public void resetPassword(String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuario.setResetToken(null); // Limpa o token após uso
        usuarioRepository.save(usuario);
    }

    // --- LISTAGEM ---
    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO mapToDTO(Usuario user) {
        return UsuarioDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .telefone(user.getTelefone())
                .avatarUrl(user.getFotoUrl())
                .build();
    }
}