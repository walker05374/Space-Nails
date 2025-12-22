package com.space.nails.controller;

import com.space.nails.model.*;
import com.space.nails.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class BackupController {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificacaoRepository notificacaoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public BackupController(AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository,
            AgendaConfigRepository agendaConfigRepository,
            PortfolioRepository portfolioRepository,
            NotificacaoRepository notificacaoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.agendaConfigRepository = agendaConfigRepository;
        this.portfolioRepository = portfolioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> gerarBackup() {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("-- Backup Space Nails (").append(LocalDateTime.now()).append(")\n\n");

            // Ordem Importa para FKs: Usuarios -> Clientes -> Servicos -> AgendaConfig ->
            // Agendamentos -> Portfolio -> Notificacoes

            // USUARIOS
            List<Usuario> usuarios = usuarioRepository.findAll();
            for (Usuario u : usuarios) {
                // H2 SQL Insert - Incluindo ativo, foto_url, reset_token
                sql.append(String.format(
                        "INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, data_validade, ativo, foto_url, reset_token) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %s, %b, '%s', '%s');\n",
                        u.getId(), escape(u.getNome()), escape(u.getEmail()), escape(u.getSenha()), u.getRole(),
                        escape(u.getTelefone()), escape(u.getEndereco()),
                        u.getDataValidade() != null ? "'" + u.getDataValidade() + "'" : "NULL",
                        u.isAtivo(), escape(u.getFotoUrl()), escape(u.getResetToken())));
            }
            sql.append("\n");

            // CLIENTES
            List<Cliente> clientes = clienteRepository.findAll();
            for (Cliente c : clientes) {
                sql.append(String.format(
                        "INSERT INTO clientes (id, nome, telefone, observacoes) VALUES (%d, '%s', '%s', '%s');\n",
                        c.getId(), escape(c.getNome()), escape(c.getTelefone()), escape(c.getObservacoes())));
            }
            sql.append("\n");

            // SERVICOS
            List<Servico> servicos = servicoRepository.findAll();
            for (Servico s : servicos) {
                sql.append(String.format(
                        "INSERT INTO servicos (id, nome, valor, profissional_id) VALUES (%d, '%s', %s, %s);\n",
                        s.getId(), escape(s.getNome()), s.getValor().toString().replace(",", "."),
                        s.getProfissional() != null ? s.getProfissional().getId() : "NULL"));
            }
            sql.append("\n");

            // AGENDA CONFIG
            List<AgendaConfig> confs = agendaConfigRepository.findAll();
            for (AgendaConfig a : confs) {
                sql.append(String.format(
                        "INSERT INTO agenda_configs (id, dia_semana, ativo, inicio_trabalho, fim_trabalho, inicio_intervalo, fim_intervalo, usuario_id) VALUES (%d, '%s', %b, %s, %s, %s, %s, %d);\n",
                        a.getId(), a.getDiaSemana(), a.isAtivo(),
                        a.getInicioTrabalho() != null ? "'" + a.getInicioTrabalho() + "'" : "NULL",
                        a.getFimTrabalho() != null ? "'" + a.getFimTrabalho() + "'" : "NULL",
                        a.getInicioIntervalo() != null ? "'" + a.getInicioIntervalo() + "'" : "NULL",
                        a.getFimIntervalo() != null ? "'" + a.getFimIntervalo() + "'" : "NULL",
                        a.getProfissional().getId()));
            }
            sql.append("\n");

            // AGENDAMENTOS
            List<Agendamento> agendamentos = agendamentoRepository.findAll();
            for (Agendamento a : agendamentos) {
                // Tabela agendamentos tem: id, codigo, data_hora, lembrete24h_enviado,
                // lembrete2h_enviado, lembrete30min_enviado, localizacao, observacoes, status,
                // cliente_id, profissional_id, servico_id
                sql.append(String.format(
                        "INSERT INTO agendamentos (id, codigo, data_hora, status, observacoes, lembrete24h_enviado, lembrete2h_enviado, lembrete30min_enviado, localizacao, cliente_id, profissional_id, servico_id) VALUES (%d, '%s', '%s', '%s', '%s', %b, %b, %b, '%s', %d, %d, %s);\n",
                        a.getId(), escape(a.getCodigo()), a.getDataHora(), a.getStatus(), escape(a.getObservacoes()),
                        a.isLembrete24hEnviado(), a.isLembrete2hEnviado(), a.isLembrete30minEnviado(),
                        escape(a.getLocalizacao()),
                        a.getCliente().getId(), a.getProfissional().getId(),
                        a.getServico() != null ? a.getServico().getId() : "NULL"));
            }
            sql.append("\n");

            // PORTFOLIO
            List<PortfolioItem> items = portfolioRepository.findAll();
            for (PortfolioItem p : items) {
                sql.append(String.format(
                        "INSERT INTO portfolio_items (id, titulo, imagem_url, clicks, servico_id, profissional_id) VALUES (%d, '%s', '%s', %d, %s, %d);\n",
                        p.getId(), escape(p.getTitulo()), escape(p.getImagemUrl()), p.getClicks(),
                        p.getServico() != null ? p.getServico().getId() : "NULL",
                        p.getProfissional().getId()));
            }
            sql.append("\n");

            // NOTIFICACOES
            List<Notificacao> nots = notificacaoRepository.findAll();
            for (Notificacao n : nots) {
                sql.append(String.format(
                        "INSERT INTO notificacoes (id, mensagem, link, lido, data_envio, cor, usuario_id, agendamento_id) VALUES (%d, '%s', '%s', %b, '%s', '%s', %d, %s);\n",
                        n.getId(), escape(n.getMensagem()), escape(n.getLink()), n.isLido(), n.getDataEnvio(),
                        escape(n.getCor()),
                        n.getUsuario().getId(),
                        n.getAgendamento() != null ? n.getAgendamento().getId() : "NULL"));
            }

            String filename = "backup_spacenails_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".sql";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.TEXT_PLAIN) // SQL é texto
                    .body(sql.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("'", "''"); // Escape simples para SQL (' -> '')
    }

    @PostMapping(value = "/restore", consumes = { "multipart/form-data" })
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> restaurarBackup(@RequestParam("file") MultipartFile file) {
        try {
            String content = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // 1. Limpar Banco (Ordem Inversa da Inserção)
            // PostgreSQL não suporta SET REFERENTIAL_INTEGRITY (H2). Usamos DELETE normal
            // na ordem correta.

            entityManager.createNativeQuery("DELETE FROM notificacoes").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM portfolio_items").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM agendamentos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM agenda_configs").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM servicos").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM clientes").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM usuarios").executeUpdate();

            // 2. Executar inserts
            String[] statements = content.split(";");
            for (String stmt : statements) {
                if (!stmt.trim().isEmpty()) {
                    entityManager.createNativeQuery(stmt).executeUpdate();
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao restaurar backup SQL: " + e.getMessage());
        }
    }
}