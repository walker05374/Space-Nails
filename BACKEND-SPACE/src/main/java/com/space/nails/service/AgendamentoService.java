package com.space.nails.service;

import com.space.nails.dto.AgendamentoDTO;
import com.space.nails.model.*;
import com.space.nails.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final UsuarioRepository usuarioRepository;

    // CONSTRUTOR MANUAL
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

        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        if (!cliente.getProfissional().getId().equals(profissional.getId()) && profissional.getRole() != Usuario.Role.ADMIN) {
            throw new RuntimeException("Este cliente não pertence a você.");
        }

        if (agendamentoRepository.existeConflitoHorario(profissional, dto.getDataHora())) {
            throw new RuntimeException("Já existe um agendamento neste horário!");
        }

        // Usando o Builder manual do Agendamento (definido abaixo)
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
        
        // Campos extras para visualização
        dto.setNomeCliente(a.getCliente().getNome());
        dto.setNomeServico(a.getServico().getNome());
        dto.setValorServico(a.getServico().getValor());
        dto.setNomeProfissional(a.getProfissional().getNome());
        return dto;
    }
}