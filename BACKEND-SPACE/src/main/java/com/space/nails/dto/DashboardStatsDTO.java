package com.space.nails.dto;

public class DashboardStatsDTO {

    private long totalProfissionais;
    private long totalClientes;
    private long agendamentosHoje;
    
    // Separando os faturamentos
    private Double faturamentoHoje;  // Só do dia atual
    private Double faturamentoTotal; // Acumulado de sempre

    private String avisoValidade;
    private Integer diasRestantes;
    private boolean assinaturaAtiva;

    // --- CONSTRUTOR VAZIO ---
    public DashboardStatsDTO() {
    }

    // --- GETTERS E SETTERS MANUAIS ---

    public long getTotalProfissionais() {
        return totalProfissionais;
    }

    public void setTotalProfissionais(long totalProfissionais) {
        this.totalProfissionais = totalProfissionais;
    }

    public long getTotalClientes() {
        return totalClientes;
    }

    public void setTotalClientes(long totalClientes) {
        this.totalClientes = totalClientes;
    }

    public long getAgendamentosHoje() {
        return agendamentosHoje;
    }

    public void setAgendamentosHoje(long agendamentosHoje) {
        this.agendamentosHoje = agendamentosHoje;
    }

    public Double getFaturamentoHoje() {
        return faturamentoHoje;
    }

    public void setFaturamentoHoje(Double faturamentoHoje) {
        this.faturamentoHoje = faturamentoHoje;
    }

    public Double getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(Double faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
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