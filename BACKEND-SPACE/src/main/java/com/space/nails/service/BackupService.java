package com.space.nails.service;

import com.space.nails.model.*;
import com.space.nails.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificacaoRepository notificacaoRepository;

    private final JdbcTemplate jdbcTemplate;

    public BackupService(UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository,
            AgendamentoRepository agendamentoRepository,
            AgendaConfigRepository agendaConfigRepository,
            PortfolioRepository portfolioRepository,
            NotificacaoRepository notificacaoRepository,
            JdbcTemplate jdbcTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.agendaConfigRepository = agendaConfigRepository;
        this.portfolioRepository = portfolioRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public File gerarBackup() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = File.createTempFile("backup_spacenails_" + timestamp + "_", ".sql");

        try (FileWriter writer = new FileWriter(backupFile, StandardCharsets.UTF_8)) {
            writer.write("-- Backup Space Nails - " + timestamp + "\n");
            writer.write("-- Generator: Portable SQL (Null Safe)\n\n");

            // 1. Usuarios
            log.info("Processando backup Usuarios...");
            for (Usuario u : usuarioRepository.findAll()) {
                // FIXED: Include ALL fields: foto_url, reset_token
                String sql = String.format(
                        "INSERT INTO usuarios (id, nome, email, senha, role, telefone, data_validade, ativo, codigo_convite, localizacao_url, endereco, ultimo_acesso, foto_url, reset_token) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                        val(u.getId()), val(u.getNome()), val(u.getEmail()), val(u.getSenha()),
                        val(u.getRole()), val(u.getTelefone()), val(u.getDataValidade()),
                        val(u.isAtivo()), val(u.getCodigoConvite()), val(u.getLocalizacaoUrl()),
                        val(u.getEndereco()), val(u.getUltimoAcesso()), val(u.getFotoUrl()), val(u.getResetToken()));
                writer.write(sql);
            }
            writer.write("\n");

            // 2. Clientes
            log.info("Processando backup Clientes...");
            for (Cliente c : clienteRepository.findAll()) {
                String sql = String.format(
                        "INSERT INTO clientes (id, nome, telefone, observacoes, data_cadastro, avatar_url, profissional_id) VALUES (%s, %s, %s, %s, %s, %s, %s);\n",
                        val(c.getId()), val(c.getNome()), val(c.getTelefone()),
                        val(c.getObservacoes()), val(c.getDataCadastro()), val(c.getAvatarUrl()),
                        val(c.getProfissional()));
                writer.write(sql);
            }
            writer.write("\n");

            // 3. Servicos
            log.info("Processando backup Servicos...");
            for (Servico s : servicoRepository.findAll()) {
                String sql = String.format(
                        "INSERT INTO servicos (id, nome, valor, tempo_estimado_minutos, ativo, profissional_id) VALUES (%s, %s, %s, %s, %s, %s);\n",
                        val(s.getId()), val(s.getNome()), val(s.getValor()),
                        val(s.getTempoEstimado()), val(s.getAtivo()), val(s.getProfissional()));
                writer.write(sql);
            }
            writer.write("\n");

            // 4. Agenda Configs
            log.info("Processando backup AgendaConfig...");
            for (AgendaConfig ac : agendaConfigRepository.findAll()) {
                // Table: agenda_configs | Col: usuario_id
                String sql = String.format(
                        "INSERT INTO agenda_configs (id, dia_semana, ativo, inicio_trabalho, fim_trabalho, inicio_intervalo, fim_intervalo, usuario_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);\n",
                        val(ac.getId()), val(ac.getDiaSemana()), val(ac.isAtivo()),
                        val(ac.getInicioTrabalho()), val(ac.getFimTrabalho()), val(ac.getInicioIntervalo()),
                        val(ac.getFimIntervalo()),
                        val(ac.getProfissional()));
                writer.write(sql);
            }
            writer.write("\n");

            // 5. Agendamentos
            log.info("Processando backup Agendamentos...");
            for (Agendamento a : agendamentoRepository.findAll()) {
                String sql = String.format(
                        "INSERT INTO agendamentos (id, data_hora, status, observacoes, cliente_id, profissional_id, servico_id, codigo, localizacao, lembrete24h_enviado, lembrete2h_enviado, lembrete30min_enviado) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);\n",
                        val(a.getId()), val(a.getDataHora()), val(a.getStatus()), val(a.getObservacoes()),
                        val(a.getCliente()), val(a.getProfissional()), val(a.getServico()),
                        val(a.getCodigo()), val(a.getLocalizacao()),
                        val(a.isLembrete24hEnviado()), val(a.isLembrete2hEnviado()), val(a.isLembrete30minEnviado()));
                writer.write(sql);
            }
            writer.write("\n");

            // 6. Portfolio
            log.info("Processando backup Portfolio...");
            for (PortfolioItem p : portfolioRepository.findAll()) {
                String sql = String.format(
                        "INSERT INTO portfolio_items (id, titulo, imagem_url, clicks, servico_id, profissional_id, data_criacao, cloudinary_public_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);\n",
                        val(p.getId()), val(p.getTitulo()), val(p.getImagemUrl()), val(p.getClicks()),
                        val(p.getServico()), val(p.getProfissional()),
                        val(p.getDataCriacao()), val(p.getCloudinaryPublicId()));
                writer.write(sql);
            }
            writer.write("\n");

            // 7. Notificacoes
            log.info("Processando backup Notificacoes...");
            for (Notificacao n : notificacaoRepository.findAll()) {
                String sql = String.format(
                        "INSERT INTO notificacoes (id, mensagem, lido, data_envio, usuario_id, link, cor, agendamento_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s);\n",
                        val(n.getId()), val(n.getMensagem()), val(n.isLido()), val(n.getDataEnvio()),
                        val(n.getUsuario()),
                        val(n.getLink()), val(n.getCor()),
                        val(n.getAgendamento()));
                writer.write(sql);
            }
        }

        log.info("Backup SQL gerado com sucesso: {}", backupFile.getAbsolutePath());
        return backupFile;
    }

    @Transactional
    public String restaurarBackup(MultipartFile file) throws IOException {
        log.info("Iniciando restauração SQL...");
        StringBuilder logReport = new StringBuilder();
        logReport.append("Relatorio de Restauracao:\n");

        // 1. Limpeza
        logReport.append("- Limpando dados antigos...\n");
        jdbcTemplate.execute("DELETE FROM notificacoes");
        jdbcTemplate.execute("DELETE FROM portfolio_items");
        jdbcTemplate.execute("DELETE FROM agendamentos");
        jdbcTemplate.execute("DELETE FROM agenda_configs");
        jdbcTemplate.execute("DELETE FROM servicos");
        jdbcTemplate.execute("DELETE FROM clientes");
        jdbcTemplate.execute("DELETE FROM usuarios");

        // 2. Leitura
        int successCount = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--"))
                    continue;
                try {
                    jdbcTemplate.execute(line);
                    successCount++;
                } catch (Exception e) {
                    log.error("Erro SQL na linha: " + line, e);
                    // CRITICAL: Throw exception to rollback transaction
                    throw new RuntimeException(
                            "Falha critica ao restaurar linha: " + line + ". Erro: " + e.getMessage());
                }
            }
        }

        resetSequences();
        logReport.append("- Comandos executados com sucesso: " + successCount + "\n");
        logReport.append("- Sequences resetadas.\n");
        logReport.append("Restauração concluída com sucesso!");

        log.info("Restauração concluída!");
        return logReport.toString();
    }

    // --- Null-Safe Helpers ---

    private String val(Object obj) {
        if (obj == null)
            return "NULL";

        // Entities (Always return ID)
        if (obj instanceof Usuario)
            return val(((Usuario) obj).getId());
        if (obj instanceof Cliente)
            return val(((Cliente) obj).getId());
        if (obj instanceof Servico)
            return val(((Servico) obj).getId());
        if (obj instanceof Agendamento)
            return val(((Agendamento) obj).getId());

        // Numbers & Booleans
        if (obj instanceof Number)
            return obj.toString();
        if (obj instanceof Boolean)
            return ((Boolean) obj) ? "TRUE" : "FALSE";

        // Enums
        if (obj instanceof Enum)
            return "'" + ((Enum<?>) obj).name() + "'";

        // Temporals (Dates, Times) - Quotes
        if (obj instanceof Temporal)
            return "'" + obj.toString() + "'";

        // Fallback String
        return "'" + ((String) obj).toString().replace("'", "''") + "'";
    }

    private String val(String s) {
        if (s == null)
            return "NULL";
        return "'" + s.replace("'", "''") + "'";
    }

    private void resetSequences() {
        try {
            List<String> tables = List.of("usuarios", "clientes", "servicos", "agendamentos", "portfolio_items",
                    "notificacoes", "agenda_configs");
            for (String table : tables) {
                String sql = "SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), COALESCE(MAX(id), 1)) FROM "
                        + table;
                try {
                    jdbcTemplate.execute(sql);
                } catch (Exception e) {
                    /* ignore H2 */ }
            }
        } catch (Exception e) {
            /* ignore */ }
    }
}