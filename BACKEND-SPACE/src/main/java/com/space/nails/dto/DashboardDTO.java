package com.space.nails.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {

    // Estatísticas Gerais
    private long agendamentosHoje;
    private long agendamentosSemana;
    private BigDecimal faturamentoMensal;
    private long totalClientes;

    // Listas para Tabelas/Gráficos
    private List<AgendamentoDTO> proximosAgendamentos;
    private List<BigDecimal> faturamentoUltimos6Meses; 
    
    // --- NOVOS CAMPOS PARA AVISO DE VALIDADE ---
    private String avisoValidade;
    private Integer diasRestantes;
    private boolean assinaturaAtiva;

    // --- CONSTRUTOR VAZIO (Necessário) ---
    public DashboardDTO() {
    }

    // --- GETTERS E SETTERS MANUAIS ---

    public long getAgendamentosHoje() {
        return agendamentosHoje;
    }

    public void setAgendamentosHoje(long agendamentosHoje) {
        this.agendamentosHoje = agendamentosHoje;
    }

    public long getAgendamentosSemana() {
        return agendamentosSemana;
    }

    public void setAgendamentosSemana(long agendamentosSemana) {
        this.agendamentosSemana = agendamentosSemana;
    }

    public BigDecimal getFaturamentoMensal() {
        return faturamentoMensal;
    }

    public void setFaturamentoMensal(BigDecimal faturamentoMensal) {
        this.faturamentoMensal = faturamentoMensal;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public List<AgendamentoDTO> getProximosAgendamentos() {
        return proximosAgendamentos;
    }

    public void setProximosAgendamentos(List<AgendamentoDTO> proximosAgendamentos) {
        this.proximosAgendamentos = proximosAgendamentos;
    }

    public List<BigDecimal> getFaturamentoUltimos6Meses() {
        return faturamentoUltimos6Meses;
    }

    public void setFaturamentoUltimos6Meses(List<BigDecimal> faturamentoUltimos6Meses) {
        this.faturamentoUltimos6Meses = faturamentoUltimos6Meses;
    }

    public String getAvisoValidade() {
        return avisoValidade;
    }

    public void setAvisoValidade(String avisoValidade) {
        this.avisoValidade = avisoValidade;
    }

    public Integer getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(Integer diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public boolean isAssinaturaAtiva() {
        return assinaturaAtiva;
    }

    public void setAssinaturaAtiva(boolean assinaturaAtiva) {
        this.assinaturaAtiva = assinaturaAtiva;
    }
}