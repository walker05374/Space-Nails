package com.space.nails.dto;

public class DashboardStatsDTO {

    // Campos originais
    private long totalProfissionais;
    private long totalClientes;
    private long agendamentosHoje;
    private double faturamentoHoje;

    // Novos campos de Validade
    private String avisoValidade;
    private Integer diasRestantes;
    private boolean assinaturaAtiva;

    // --- CONSTRUTOR VAZIO ---
    public DashboardStatsDTO() {
    }

    // --- GETTERS E SETTERS ---

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

    public double getFaturamentoHoje() {
        return faturamentoHoje;
    }

    public void setFaturamentoHoje(double faturamentoHoje) {
        this.faturamentoHoje = faturamentoHoje;
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