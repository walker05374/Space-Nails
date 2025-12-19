package com.space.nails.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicos")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double valor;

    @Column(name = "tempo_estimado_minutos")
    private Integer tempoEstimado;

    // --- CORREÇÃO IMPORTANTE ---
    // Estava "private Cliente profissional;" -> ISSO CAUSA O ERRO
    // Deve ser "private Usuario profissional;" -> POIS QUEM PRESTA O SERVIÇO É O USUÁRIO
    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Usuario profissional; 

    // CONSTRUTORES
    public Servico() {}

    public Servico(Long id, String nome, Double valor, Integer tempoEstimado, Usuario profissional) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.tempoEstimado = tempoEstimado;
        this.profissional = profissional;
    }

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    
    public Integer getTempoEstimado() { return tempoEstimado; }
    public void setTempoEstimado(Integer tempoEstimado) { this.tempoEstimado = tempoEstimado; }
    
    // ATUALIZE O GETTER/SETTER TAMBÉM
    public Usuario getProfissional() { return profissional; }
    public void setProfissional(Usuario profissional) { this.profissional = profissional; }
}