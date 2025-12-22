package com.space.nails.dto;

import java.time.LocalDate;

public class UpdateUsuarioDTO {
    private String nome;
    private String email;
    private String telefone;
    private String novaSenha;
    private LocalDate dataValidade;
    private String endereco; // Novo

    public UpdateUsuarioDTO() {
    }

    public UpdateUsuarioDTO(String nome, String email, String telefone, String novaSenha, LocalDate dataValidade,
            String endereco) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.novaSenha = novaSenha;
        this.dataValidade = dataValidade;
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}