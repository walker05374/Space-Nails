package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String sobrenome;

    @Column(nullable = false)
    private String telefone; // Essencial para o WhatsApp (ex: 5511999999999)

    @Column(name = "total_gasto")
    private Double totalGasto = 0.0;

    @Column(name = "qtd_visitas")
    private Integer qtdVisitas = 0;

    @Column(name = "ultima_visita")
    private LocalDateTime ultimaVisita;

    // Vincula o cliente à Manicure (Usuario) que o cadastrou
    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Cliente profissional;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSobrenome() { return sobrenome; }
    public void setSobrenome(String sobrenome) { this.sobrenome = sobrenome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public Double getTotalGasto() { return totalGasto; }
    public void setTotalGasto(Double totalGasto) { this.totalGasto = totalGasto; }
    public Integer getQtdVisitas() { return qtdVisitas; }
    public void setQtdVisitas(Integer qtdVisitas) { this.qtdVisitas = qtdVisitas; }
    public LocalDateTime getUltimaVisita() { return ultimaVisita; }
    public void setUltimaVisita(LocalDateTime ultimaVisita) { this.ultimaVisita = ultimaVisita; }
    public Cliente getProfissional() { return profissional; }
    public void setProfissional(Cliente profissional) { this.profissional = profissional; }
}