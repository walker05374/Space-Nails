package com.space.nails.service;

import com.space.nails.dto.AgendamentoDTO;
import com.space.nails.model.*;
import com.space.nails.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository,
            UsuarioRepository usuarioRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public AgendamentoDTO criarAgendamento(AgendamentoDTO dto, String emailProfissional) {
        Usuario profissional = usuarioRepository.findByEmail(emailProfissional)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // --- CORREÇÃO DE SERVIÇO (Evita o erro "Não encontrado") ---
        Servico servico = null;

        // 1. Se veio um ID, tenta buscar no banco
        if (dto.getServicoId() != null) {
            servico = servicoRepository.findById(dto.getServicoId()).orElse(null);
        }

        // 2. Se não achou pelo ID (ou ID era nulo), cria um novo serviço
        if (servico == null) {
            // Validação simples
            String nomeServico = dto.getNomeServico() != null ? dto.getNomeServico() : "Serviço Personalizado";
            Double valorServico = dto.getValorServico() != null ? dto.getValorServico() : 0.0;

            servico = new Servico();
            servico.setNome(nomeServico);
            servico.setValor(valorServico);
            servico.setTempoEstimado(60); // Padrão
            servico.setProfissional(profissional); // Vincula ao profissional logado

            servico = servicoRepository.save(servico);
        }

        // Verifica conflito de horário (Intervalo)
        LocalDateTime inicio = dto.getDataHora();
        int duracao = (servico.getTempoEstimado() != null && servico.getTempoEstimado() > 0)
                ? servico.getTempoEstimado()
                : 60;
        LocalDateTime fim = inicio.plusMinutes(duracao);

        LocalDateTime inicioDia = inicio.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicio.toLocalDate().atTime(23, 59, 59);
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findByProfissionalAndData(profissional, inicioDia,
                fimDia);

        for (Agendamento a : agendamentosDoDia) {
            if (a.getStatus() == StatusAgendamento.CANCELADO)
                continue;

            // Ignora se for o mesmo ID (caso fosse edição, mas aqui é criar cria novo)

            int duraA = (a.getServico().getTempoEstimado() != null && a.getServico().getTempoEstimado() > 0)
                    ? a.getServico().getTempoEstimado()
                    : 60;
            LocalDateTime aInicio = a.getDataHora();
            LocalDateTime aFim = aInicio.plusMinutes(duraA);

            if (aInicio.isBefore(fim) && aFim.isAfter(inicio)) {
                throw new RuntimeException("Já existe um agendamento neste intervalo de horário!");
            }
        }

        Agendamento agendamento = Agendamento.builder()
                .profissional(profissional)
                .cliente(cliente)
                .servico(servico)
                .dataHora(dto.getDataHora())
                .status(StatusAgendamento.PENDENTE)
                .observacoes(dto.getObservacoes())
                .build();

        agendamento = agendamentoRepository.save(agendamento);
        return mapToDTO(agendamento);
    }

    public List<AgendamentoDTO> listarMeusAgendamentos(String emailProfissional) {
        Usuario profissional = usuarioRepository.findByEmail(emailProfissional)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (profissional.getRole() == Usuario.Role.ADMIN) {
            return agendamentoRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        }

        return agendamentoRepository.findByProfissionalOrderByDataHoraDesc(profissional)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public AgendamentoDTO atualizarStatus(Long id, StatusAgendamento status, String emailUser) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        // VALIDAÇÃO: Não pode concluir futuro
        if (status == StatusAgendamento.CONCLUIDO && agendamento.getDataHora().isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Não é possível concluir um agendamento futuro.");
        }

        agendamento.setStatus(status);
        return mapToDTO(agendamentoRepository.save(agendamento));
    }

    private AgendamentoDTO mapToDTO(Agendamento a) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(a.getId());
        dto.setClienteId(a.getCliente().getId());
        dto.setServicoId(a.getServico().getId());
        dto.setDataHora(a.getDataHora());
        dto.setStatus(a.getStatus());
        dto.setObservacoes(a.getObservacoes());

        dto.setNomeCliente(a.getCliente().getNome());
        dto.setNomeServico(a.getServico().getNome());
        dto.setValorServico(a.getServico().getValor());
        dto.setNomeProfissional(a.getProfissional().getNome());
        return dto;
    }
}