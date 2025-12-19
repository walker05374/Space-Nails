package com.space.nails.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome; // Ex: "Pé e Mão"

    @Column(nullable = false)
    private Double valor; // Ex: 60.00

    @Column(name = "tempo_estimado_minutos")
    private Integer tempoEstimado; // Ex: 60 minutos

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Cliente profissional;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public Integer getTempoEstimado() { return tempoEstimado; }
    public void setTempoEstimado(Integer tempoEstimado) { this.tempoEstimado = tempoEstimado; }
    public Cliente getProfissional() { return profissional; }
    public void setProfissional(Cliente profissional) { this.profissional = profissional; }
}