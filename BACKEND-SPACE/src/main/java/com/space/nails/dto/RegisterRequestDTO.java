package com.space.nails.dto;

import java.time.LocalDate;

public class RegisterRequestDTO {
    private String nome;
    private String email;
    private String password;
    private String telefone;
    private String avatarUrl;
    private LocalDate dataValidade; // Novo campo para o Admin definir a validade

    public RegisterRequestDTO() {}

    public RegisterRequestDTO(String nome, String email, String password, String telefone, String avatarUrl, LocalDate dataValidade) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.telefone = telefone;
        this.avatarUrl = avatarUrl;
        this.dataValidade = dataValidade;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }
}