package com.space.nails.controller;

import com.space.nails.dto.BackupData;
import com.space.nails.model.*;
import com.space.nails.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/backup")
@PreAuthorize("hasRole('ADMIN')")
public class BackupController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AgendaConfigRepository agendaConfigRepository;
    private final PortfolioRepository portfolioRepository;
    private final NotificacaoRepository notificacaoRepository;

    public BackupController(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
            ServicoRepository servicoRepository, AgendamentoRepository agendamentoRepository,
            AgendaConfigRepository agendaConfigRepository, PortfolioRepository portfolioRepository,
            NotificacaoRepository notificacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.agendaConfigRepository = agendaConfigRepository;
        this.portfolioRepository = portfolioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    @GetMapping
    public ResponseEntity<BackupData> exportData() {
        BackupData data = new BackupData(
                usuarioRepository.findAll(),
                clienteRepository.findAll(),
                servicoRepository.findAll(),
                agendamentoRepository.findAll(),
                agendaConfigRepository.findAll(),
                portfolioRepository.findAll(),
                notificacaoRepository.findAll(),
                LocalDateTime.now().toString());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/restore")
    @Transactional
    public ResponseEntity<String> restoreData(@RequestBody BackupData data) {
        try {
            // Limpa dados existentes (Ordem importa por causa das FKs)
            notificacaoRepository.deleteAll();
            agendamentoRepository.deleteAll();
            portfolioRepository.deleteAll();
            agendaConfigRepository.deleteAll();
            servicoRepository.deleteAll();
            clienteRepository.deleteAll();
            usuarioRepository.deleteAll();

            // Restaura dados (Ordem importa)
            // A ordem de restauração deve verificar dependências.
            // Usuarios -> Clientes -> Servicos -> Configs -> Portfolio ->
            // Agendamentos/Notificacoes

            if (data.getUsuarios() != null)
                usuarioRepository.saveAll(data.getUsuarios());
            if (data.getClientes() != null)
                clienteRepository.saveAll(data.getClientes());
            if (data.getServicos() != null)
                servicoRepository.saveAll(data.getServicos());
            if (data.getAgendaConfigs() != null)
                agendaConfigRepository.saveAll(data.getAgendaConfigs());
            if (data.getPortfolioItens() != null)
                portfolioRepository.saveAll(data.getPortfolioItens());
            if (data.getAgendamentos() != null)
                agendamentoRepository.saveAll(data.getAgendamentos());
            if (data.getNotificacoes() != null)
                notificacaoRepository.saveAll(data.getNotificacoes());

            return ResponseEntity.ok("Restauração concluída com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erro ao restaurar backup: " + e.getMessage());
        }
    }
}