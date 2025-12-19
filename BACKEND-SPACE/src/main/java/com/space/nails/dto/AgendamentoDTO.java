package com.space.nails.dto;

import com.space.nails.model.StatusAgendamento;
import java.time.LocalDateTime;

public class AgendamentoDTO {
    private Long id;
    private Long clienteId;
    private Long servicoId;
    private LocalDateTime dataHora;
    private String observacoes;
    private StatusAgendamento status;
    
    // Dados para leitura
    private String nomeCliente;
    private String nomeServico;
    private Double valorServico;
    private String nomeProfissional;

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getNomeServico() { return nomeServico; }
    public void setNomeServico(String nomeServico) { this.nomeServico = nomeServico; }
    public Double getValorServico() { return valorServico; }
    public void setValorServico(Double valorServico) { this.valorServico = valorServico; }
    public String getNomeProfissional() { return nomeProfissional; }
    public void setNomeProfissional(String nomeProfissional) { this.nomeProfissional = nomeProfissional; }
}