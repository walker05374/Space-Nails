package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String telefone;
    private String observacoes;

    // Relacionamento com o Profissional (Usuario)
    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Usuario profissional;

    private LocalDate dataCadastro;
    private String avatarUrl;

    // --- CONSTRUTORES ---
    public Cliente() {
        // Construtor vazio para o JPA
    }

    public Cliente(Long id, String nome, String telefone, String observacoes, Usuario profissional, LocalDate dataCadastro, String avatarUrl) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.observacoes = observacoes;
        this.profissional = profissional;
        this.dataCadastro = dataCadastro;
        this.avatarUrl = avatarUrl;
    }

    // --- GETTERS E SETTERS MANUAIS (Correção do Erro) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}