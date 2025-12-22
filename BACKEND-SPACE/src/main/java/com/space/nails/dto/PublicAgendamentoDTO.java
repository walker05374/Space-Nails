package com.space.nails.dto;

import java.time.LocalDateTime;

public class PublicAgendamentoDTO {

    private Long servicoId;
    private LocalDateTime dataHora;
    private String nomeCliente;
    private String telefoneCliente;

    public PublicAgendamentoDTO() {
    }

    public PublicAgendamentoDTO(Long servicoId, LocalDateTime dataHora, String nomeCliente, String telefoneCliente) {
        this.servicoId = servicoId;
        this.dataHora = dataHora;
        this.nomeCliente = nomeCliente;
        this.telefoneCliente = telefoneCliente;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTelefoneCliente() {
        return telefoneCliente;
    }

    public void setTelefoneCliente(String telefoneCliente) {
        this.telefoneCliente = telefoneCliente;
    }
}
