package com.space.nails.dto;

import java.time.LocalDate;

public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String telefone;
    private String avatarUrl;
    private boolean ativo;
    private LocalDate dataValidade;
    private String endereco;
    private String localizacaoUrl;
    private String codigoConvite;
    private boolean online; // Novo

    public UsuarioDTO() {
    }

    public UsuarioDTO(Long id, String nome, String email, String role, String telefone, String avatarUrl, boolean ativo,
            LocalDate dataValidade, String endereco, String localizacaoUrl, String codigoConvite, boolean online) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.telefone = telefone;
        this.avatarUrl = avatarUrl;
        this.ativo = ativo;
        this.dataValidade = dataValidade;
        this.endereco = endereco;
        this.localizacaoUrl = localizacaoUrl;
        this.codigoConvite = codigoConvite;
        this.online = online;
    }

    public static UsuarioDTOBuilder builder() {
        return new UsuarioDTOBuilder();
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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

    public String getLocalizacaoUrl() {
        return localizacaoUrl;
    }

    public void setLocalizacaoUrl(String localizacaoUrl) {
        this.localizacaoUrl = localizacaoUrl;
    }

    public String getCodigoConvite() {
        return codigoConvite;
    }

    public void setCodigoConvite(String codigoConvite) {
        this.codigoConvite = codigoConvite;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public static class UsuarioDTOBuilder {
        private Long id;
        private String nome;
        private String email;
        private String role;
        private String telefone;
        private String avatarUrl;
        private boolean ativo;
        private LocalDate dataValidade;
        private String endereco;
        private String localizacaoUrl;
        private String codigoConvite;
        private boolean online;

        public UsuarioDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioDTOBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public UsuarioDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UsuarioDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UsuarioDTOBuilder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public UsuarioDTOBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public UsuarioDTOBuilder ativo(boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public UsuarioDTOBuilder dataValidade(LocalDate dataValidade) {
            this.dataValidade = dataValidade;
            return this;
        }

        public UsuarioDTOBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public UsuarioDTOBuilder localizacaoUrl(String localizacaoUrl) {
            this.localizacaoUrl = localizacaoUrl;
            return this;
        }

        public UsuarioDTOBuilder codigoConvite(String codigoConvite) {
            this.codigoConvite = codigoConvite;
            return this;
        }

        public UsuarioDTOBuilder online(boolean online) {
            this.online = online;
            return this;
        }

        public UsuarioDTO build() {
            return new UsuarioDTO(id, nome, email, role, telefone, avatarUrl, ativo, dataValidade, endereco,
                    localizacaoUrl, codigoConvite, online);
        }
    }
}