package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "agenda_configs")
public class AgendaConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario profissional;

    // 1 (Segunda) a 7 (Domingo) ou Java DayOfWeek
    @Column(nullable = false)
    private Integer diaSemana;

    private boolean ativo = true;

    private LocalTime inicioTrabalho;
    private LocalTime fimTrabalho;

    private LocalTime inicioIntervalo;
    private LocalTime fimIntervalo;

    public AgendaConfig() {
    }

    public AgendaConfig(Usuario profissional, Integer diaSemana, boolean ativo, LocalTime inicioTrabalho,
            LocalTime fimTrabalho, LocalTime inicioIntervalo, LocalTime fimIntervalo) {
        this.profissional = profissional;
        this.diaSemana = diaSemana;
        this.ativo = ativo;
        this.inicioTrabalho = inicioTrabalho;
        this.fimTrabalho = fimTrabalho;
        this.inicioIntervalo = inicioIntervalo;
        this.fimIntervalo = fimIntervalo;
    }

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

    public Integer getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(Integer diaSemana) {
        this.diaSemana = diaSemana;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalTime getInicioTrabalho() {
        return inicioTrabalho;
    }

    public void setInicioTrabalho(LocalTime inicioTrabalho) {
        this.inicioTrabalho = inicioTrabalho;
    }

    public LocalTime getFimTrabalho() {
        return fimTrabalho;
    }

    public void setFimTrabalho(LocalTime fimTrabalho) {
        this.fimTrabalho = fimTrabalho;
    }

    public LocalTime getInicioIntervalo() {
        return inicioIntervalo;
    }

    public void setInicioIntervalo(LocalTime inicioIntervalo) {
        this.inicioIntervalo = inicioIntervalo;
    }

    public LocalTime getFimIntervalo() {
        return fimIntervalo;
    }

    public void setFimIntervalo(LocalTime fimIntervalo) {
        this.fimIntervalo = fimIntervalo;
    }
}
