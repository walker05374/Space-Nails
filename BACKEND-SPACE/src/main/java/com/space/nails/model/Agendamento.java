package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profissional_id", nullable = false)
    private Usuario profissional;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status;

    private String observacoes;

    // Controles de notificação
    private boolean lembrete24hEnviado = false;
    private boolean lembrete2hEnviado = false;
    private boolean lembrete30minEnviado = false; // NOVO CAMPO

    private String localizacao;

    @Column(unique = true)
    private String codigo; // CÓDIGO ÚNICO DE AGENDAMENTO

    // CONSTRUTORES
    public Agendamento() {
    }

    public Agendamento(Long id, Usuario profissional, Cliente cliente, Servico servico, LocalDateTime dataHora,
            StatusAgendamento status, String observacoes, boolean lembrete24hEnviado, boolean lembrete2hEnviado,
            boolean lembrete30minEnviado, String localizacao, String codigo) {
        this.id = id;
        this.profissional = profissional;
        this.cliente = cliente;
        this.servico = servico;
        this.dataHora = dataHora;
        this.status = status;
        this.observacoes = observacoes;
        this.lembrete24hEnviado = lembrete24hEnviado;
        this.lembrete2hEnviado = lembrete2hEnviado;
        this.lembrete30minEnviado = lembrete30minEnviado;
        this.localizacao = localizacao;
        this.codigo = codigo;
    }

    @PrePersist
    public void gerarCodigo() {
        if (this.codigo == null) {
            // Gera um código de 6 caracteres (Ex: A1B2C3)
            this.codigo = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    // BUILDER MANUAL
    public static AgendamentoBuilder builder() {
        return new AgendamentoBuilder();
    }

    public static class AgendamentoBuilder {
        private Usuario profissional;
        private Cliente cliente;
        private Servico servico;
        private LocalDateTime dataHora;
        private StatusAgendamento status;
        private String observacoes;
        private String localizacao;
        private String codigo;

        public AgendamentoBuilder profissional(Usuario p) {
            this.profissional = p;
            return this;
        }

        public AgendamentoBuilder cliente(Cliente c) {
            this.cliente = c;
            return this;
        }

        public AgendamentoBuilder servico(Servico s) {
            this.servico = s;
            return this;
        }

        public AgendamentoBuilder dataHora(LocalDateTime d) {
            this.dataHora = d;
            return this;
        }

        public AgendamentoBuilder status(StatusAgendamento s) {
            this.status = s;
            return this;
        }

        public AgendamentoBuilder observacoes(String o) {
            this.observacoes = o;
            return this;
        }

        public AgendamentoBuilder localizacao(String l) {
            this.localizacao = l;
            return this;
        }

        public AgendamentoBuilder codigo(String c) {
            this.codigo = c;
            return this;
        }

        public Agendamento build() {
            // Inicializa os booleans como false por padrão
            return new Agendamento(null, profissional, cliente, servico, dataHora, status, observacoes, false, false,
                    false, localizacao, codigo);
        }
    }

    // GETTERS E SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public boolean isLembrete24hEnviado() {
        return lembrete24hEnviado;
    }

    public void setLembrete24hEnviado(boolean lembrete24hEnviado) {
        this.lembrete24hEnviado = lembrete24hEnviado;
    }

    public boolean isLembrete2hEnviado() {
        return lembrete2hEnviado;
    }

    public void setLembrete2hEnviado(boolean lembrete2hEnviado) {
        this.lembrete2hEnviado = lembrete2hEnviado;
    }

    public boolean isLembrete30minEnviado() {
        return lembrete30minEnviado;
    }

    public void setLembrete30minEnviado(boolean lembrete30minEnviado) {
        this.lembrete30minEnviado = lembrete30minEnviado;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}