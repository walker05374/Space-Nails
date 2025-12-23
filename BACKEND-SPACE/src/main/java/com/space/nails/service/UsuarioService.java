package com.space.nails.service;

import com.space.nails.dto.*;
import com.space.nails.model.Usuario;
import com.space.nails.repository.*;
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

    // Repositories for Cascade Delete
    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final NotificacaoRepository notificacaoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            GmailEmailService emailService,
            AgendamentoRepository agendamentoRepository,
            ServicoRepository servicoRepository,
            ClienteRepository clienteRepository,
            NotificacaoRepository notificacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        var user = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // 1. Verifica se a conta está desativada manualmente (toggle do admin)
        if (!user.isAtivo()) {
            throw new RuntimeException("CONTA_SUSPENSA");
        }

        // 2. Verifica se a validade expirou (REGRA RIGOROSA)
        if (user.getDataValidade() != null && user.getDataValidade().isBefore(LocalDate.now())) {
            // Lança exceção para o Frontend exibir o aviso
            throw new RuntimeException("ASSINATURA_EXPIRADA");
        }

        // 3. Garantir que o usuário tem um Código de Convite (Migração Lazy)
        if (user.getCodigoConvite() == null || user.getCodigoConvite().isEmpty()) {
            user.setCodigoConvite(gerarCodigoUnico());
            usuarioRepository.save(user);
        }

        // Se passou, tenta autenticar no Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var token = jwtService.generateToken(user);

        return LoginResponseDTO.builder()
                .token(token)
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .userId(user.getId())
                .avatar(user.getFotoUrl())
                .dataValidade(user.getDataValidade())
                .endereco(user.getEndereco())
                .localizacaoUrl(user.getLocalizacaoUrl())
                .codigoConvite(user.getCodigoConvite()) // Adicionar ao DTO depois
                .build();
    }

    private String gerarCodigoUnico() {
        // Gera um código de 6 caracteres alfanuméricos (Maiúsculas)
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public UsuarioDTO criarProfissional(RegisterRequestDTO request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("E-mail já cadastrado!");
        }

        LocalDate validadeFinal = request.getDataValidade() != null
                ? request.getDataValidade()
                : LocalDate.now().plusDays(30);

        Usuario novoProfissional = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .senha(passwordEncoder.encode(request.getPassword()))
                .role(Usuario.Role.PROFISSIONAL)
                .fotoUrl(request.getAvatarUrl() != null ? request.getAvatarUrl() : "https://i.pravatar.cc/150")
                .ativo(true)
                .codigoConvite(gerarCodigoUnico()) // Gera o código
                .dataValidade(validadeFinal)
                .build();

        usuarioRepository.save(novoProfissional);

        // --- INICIALIZAÇÃO DE SERVIÇOS PADRÃO ---
        // Copia os serviços "Template" (Globais/Admin) para o novo profissional
        // Assim ele já nasce com serviços, mas pode editar/excluir os SEUS sem afetar
        // os outros.
        List<com.space.nails.model.Servico> templates = servicoRepository.findTemplates();
        for (com.space.nails.model.Servico t : templates) {
            com.space.nails.model.Servico copia = new com.space.nails.model.Servico();
            copia.setNome(t.getNome());
            copia.setValor(t.getValor());
            copia.setTempoEstimado(t.getTempoEstimado());
            copia.setProfissional(novoProfissional); // Vincula ao novo usuário
            servicoRepository.save(copia);
        }

        return mapToDTO(novoProfissional);
    }

    public UsuarioDTO atualizarUsuario(Long id, UpdateUsuarioDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (request.getNome() != null)
            usuario.setNome(request.getNome());
        if (request.getTelefone() != null)
            usuario.setTelefone(request.getTelefone());
        if (request.getDataValidade() != null)
            usuario.setDataValidade(request.getDataValidade());
        if (request.getEndereco() != null)
            usuario.setEndereco(request.getEndereco());
        if (request.getLocalizacaoUrl() != null)
            usuario.setLocalizacaoUrl(request.getLocalizacaoUrl());

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

    @org.springframework.transaction.annotation.Transactional
    public void excluir(Long id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (user.getRole() == Usuario.Role.ADMIN) {
            throw new RuntimeException("Não é permitido excluir um administrador.");
        }

        // --- EXCLUSÃO EM CASCATA ---
        // 1. Notificações
        // Busca TODAS as notificações do profissional
        List<com.space.nails.model.Notificacao> notificacoes = notificacaoRepository
                .findByUsuarioOrderByDataEnvioDesc(user);
        notificacaoRepository.deleteAll(notificacoes);

        // 2. Agendamentos
        List<com.space.nails.model.Agendamento> agendamentos = agendamentoRepository
                .findByProfissionalOrderByDataHoraDesc(user);
        agendamentoRepository.deleteAll(agendamentos);

        // 3. Serviços
        List<com.space.nails.model.Servico> servicos = servicoRepository.findByProfissional(user);
        servicoRepository.deleteAll(servicos);

        // 4. Clientes
        List<com.space.nails.model.Cliente> clientes = clienteRepository.findByProfissional(user);
        clienteRepository.deleteAll(clientes);

        // 5. Remove o Usuário
        usuarioRepository.deleteById(id);
    }

    public void atualizarUltimoAcesso(Long id) {
        Usuario user = usuarioRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUltimoAcesso(java.time.LocalDateTime.now());
            usuarioRepository.save(user);
        }
    }

    public void requestPasswordReset(String email) {
        Optional<Usuario> userOpt = usuarioRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            usuarioRepository.save(user);

            // Ajuste a URL conforme seu ambiente
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
        user.setResetToken(null);
        usuarioRepository.save(user);
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UsuarioDTO mapToDTO(Usuario user) {
        boolean expirado = user.getDataValidade() != null && user.getDataValidade().isBefore(LocalDate.now());
        boolean statusVisual = user.isAtivo() && !expirado;

        return UsuarioDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole().name())
                .telefone(user.getTelefone())
                .endereco(user.getEndereco()) // Novo
                .localizacaoUrl(user.getLocalizacaoUrl())
                .avatarUrl(user.getFotoUrl())
                .ativo(statusVisual)
                .dataValidade(user.getDataValidade())
                .online(user.getUltimoAcesso() != null
                        && user.getUltimoAcesso().isAfter(java.time.LocalDateTime.now().minusMinutes(2)))
                // mapeia o código
                .codigoConvite(user.getCodigoConvite())
                .build();
    }
}