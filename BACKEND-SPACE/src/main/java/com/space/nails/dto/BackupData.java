package com.space.nails.dto;

import com.space.nails.model.*;
import java.util.List;

public class BackupData {
    private List<Usuario> usuarios;
    private List<Cliente> clientes;
    private List<Servico> servicos;
    private List<Agendamento> agendamentos;
    private List<AgendaConfig> agendaConfigs;
    private List<PortfolioItem> portfolioItens;
    private List<Notificacao> notificacoes;
    private String timestamp;

    public BackupData() {
    }

    public BackupData(List<Usuario> usuarios, List<Cliente> clientes, List<Servico> servicos,
            List<Agendamento> agendamentos, List<AgendaConfig> agendaConfigs,
            List<PortfolioItem> portfolioItens, List<Notificacao> notificacoes, String timestamp) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.servicos = servicos;
        this.agendamentos = agendamentos;
        this.agendaConfigs = agendaConfigs;
        this.portfolioItens = portfolioItens;
        this.notificacoes = notificacoes;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(List<Servico> servicos) {
        this.servicos = servicos;
    }

    public List<Agendamento> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<Agendamento> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<AgendaConfig> getAgendaConfigs() {
        return agendaConfigs;
    }

    public void setAgendaConfigs(List<AgendaConfig> agendaConfigs) {
        this.agendaConfigs = agendaConfigs;
    }

    public List<PortfolioItem> getPortfolioItens() {
        return portfolioItens;
    }

    public void setPortfolioItens(List<PortfolioItem> portfolioItens) {
        this.portfolioItens = portfolioItens;
    }

    public List<Notificacao> getNotificacoes() {
        return notificacoes;
    }

    public void setNotificacoes(List<Notificacao> notificacoes) {
        this.notificacoes = notificacoes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
