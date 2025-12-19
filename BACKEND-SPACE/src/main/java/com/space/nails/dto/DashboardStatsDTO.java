package com.space.nails.dto;

public class DashboardStatsDTO {
    private long totalProfissionais;
    private long totalClientes;
    private long agendamentosHoje;
    private double faturamentoHoje;

    // Construtor Vazio
    public DashboardStatsDTO() {}

    // Construtor Completo
    public DashboardStatsDTO(long totalProfissionais, long totalClientes, long agendamentosHoje, double faturamentoHoje) {
        this.totalProfissionais = totalProfissionais;
        this.totalClientes = totalClientes;
        this.agendamentosHoje = agendamentosHoje;
        this.faturamentoHoje = faturamentoHoje;
    }

    // --- BUILDER MANUAL (CORREÇÃO) ---
    public static DashboardStatsDTOBuilder builder() {
        return new DashboardStatsDTOBuilder();
    }

    public static class DashboardStatsDTOBuilder {
        private long totalProfissionais;
        private long totalClientes;
        private long agendamentosHoje;
        private double faturamentoHoje;

        public DashboardStatsDTOBuilder totalProfissionais(long totalProfissionais) {
            this.totalProfissionais = totalProfissionais;
            return this;
        }

        public DashboardStatsDTOBuilder totalClientes(long totalClientes) {
            this.totalClientes = totalClientes;
            return this;
        }

        public DashboardStatsDTOBuilder agendamentosHoje(long agendamentosHoje) {
            this.agendamentosHoje = agendamentosHoje;
            return this;
        }

        public DashboardStatsDTOBuilder faturamentoHoje(double faturamentoHoje) {
            this.faturamentoHoje = faturamentoHoje;
            return this;
        }

        public DashboardStatsDTO build() {
            return new DashboardStatsDTO(totalProfissionais, totalClientes, agendamentosHoje, faturamentoHoje);
        }
    }

    // Getters e Setters
    public long getTotalProfissionais() { return totalProfissionais; }
    public void setTotalProfissionais(long totalProfissionais) { this.totalProfissionais = totalProfissionais; }

    public long getTotalClientes() { return totalClientes; }
    public void setTotalClientes(long totalClientes) { this.totalClientes = totalClientes; }

    public long getAgendamentosHoje() { return agendamentosHoje; }
    public void setAgendamentosHoje(long agendamentosHoje) { this.agendamentosHoje = agendamentosHoje; }

    public double getFaturamentoHoje() { return faturamentoHoje; }
    public void setFaturamentoHoje(double faturamentoHoje) { this.faturamentoHoje = faturamentoHoje; }
}