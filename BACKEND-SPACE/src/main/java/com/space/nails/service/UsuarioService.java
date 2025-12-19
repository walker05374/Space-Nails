package com.space.nails.service;

import com.space.nails.dto.*;
import com.space.nails.model.Usuario;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GmailEmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          GmailEmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        var user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        if (!user.isAtivo()) {
            throw new RuntimeException("CONTA_SUSPENSA");
        }
        
        if (user.getDataValidade() != null && user.getDataValidade().isBefore(LocalDate.now())) {
            throw new RuntimeException("ASSINATURA_EXPIRADA");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
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

    // Criar profissionais via Admin ou Registro
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
                .fotoUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "https://i.pravatar.cc/150")
                .ativo(true)
                .dataValidade(LocalDate.now().plusDays(30)) // Validade inicial 30 dias
                .build();

        usuarioRepository.save(novoProfissional);
        return mapToDTO(novoProfissional);
    }

    public UsuarioDTO atualizarUsuario(Long id, UpdateUsuarioDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (request.getNome() != null) usuario.setNome(request.getNome());
        if (request.getTelefone() != null) usuario.setTelefone(request.getTelefone());
        if (request.getDataValidade() != null) usuario.setDataValidade(request.getDataValidade());
        
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

    public void alternarStatus(Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        if (user.getRole() == Usuario.Role.ADMIN) {
            throw new RuntimeException("Não é permitido desativar um administrador.");
        }

        user.setAtivo(!user.isAtivo());
        usuarioRepository.save(user);
    }

    public void excluir(Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getRole() == Usuario.Role.ADMIN) {
            throw new RuntimeException("Não é permitido excluir um administrador.");
        }

        usuarioRepository.deleteById(id);
    }

    // --- MÉTODOS DE RECUPERAÇÃO DE SENHA (CORREÇÃO DO ERRO) ---

    public void requestPasswordReset(String email) {
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);
        
        // Se o usuário não existir, não fazemos nada por segurança (ou lançamos erro genérico)
        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            usuarioRepository.save(user);

            // Link do Frontend para redefinir
            String link = "http://localhost:5173/redefinir-senha?token=" + token;
            
            String assunto = "Recuperação de Senha - Space Nails";
            String corpo = "Olá, " + user.getNome() + "!\n\n" +
                           "Você solicitou a redefinição de senha.\n" +
                           "Clique no link abaixo para criar uma nova senha:\n\n" +
                           link + "\n\n" +
                           "Se não foi você, ignore este e-mail.";

            emailService.sendEmail(user.getEmail(), assunto, corpo);
        }
    }

    public void resetPassword(String token, String newPassword) {
        Usuario user = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado."));

        user.setSenha(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Limpa o token para não ser usado novamente
        usuarioRepository.save(user);
    }

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
                .ativo(user.isAtivo())
                .dataValidade(user.getDataValidade())
                .build();
    }
}