package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status; // PENDENTE, CONCLUIDO, CANCELADO

    // Controle de Notificações WhatsApp
    @Column(name = "lembrete_24h_enviado")
    private boolean lembrete24hEnviado = false;

    @Column(name = "lembrete_2h_enviado")
    private boolean lembrete2hEnviado = false;
    
    // Endereço do atendimento (pode vir do perfil da manicure, mas bom salvar aqui caso mude)
    @Column
    private String localizacao; 

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Usuario profissional;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }
    public boolean isLembrete24hEnviado() { return lembrete24hEnviado; }
    public void setLembrete24hEnviado(boolean lembrete24hEnviado) { this.lembrete24hEnviado = lembrete24hEnviado; }
    public boolean isLembrete2hEnviado() { return lembrete2hEnviado; }
    public void setLembrete2hEnviado(boolean lembrete2hEnviado) { this.lembrete2hEnviado = lembrete2hEnviado; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }
    public Cliente getProfissional() { return profissional; }
    public void setProfissional(Cliente profissional) { this.profissional = profissional; }
}