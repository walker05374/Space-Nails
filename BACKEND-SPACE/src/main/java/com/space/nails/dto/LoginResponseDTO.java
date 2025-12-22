package com.space.nails.dto;

import java.time.LocalDate;

public class LoginResponseDTO {
    private String token;
    private String nome;
    private String email;
    private String role;
    private Long userId;
    private String avatar;
    private LocalDate dataValidade;
    private String endereco; // Novo
    private String localizacaoUrl;
    private String codigoConvite;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String nome, String email, String role, Long userId, String avatar,
            LocalDate dataValidade, String endereco, String localizacaoUrl, String codigoConvite) {
        this.token = token;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.avatar = avatar;
        this.dataValidade = dataValidade;
        this.endereco = endereco;
        this.localizacaoUrl = localizacaoUrl;
        this.codigoConvite = codigoConvite;
    }

    public static LoginResponseDTOBuilder builder() {
        return new LoginResponseDTOBuilder();
    }

    public String getToken() {
        return token;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getLocalizacaoUrl() {
        return localizacaoUrl;
    }

    public String getCodigoConvite() {
        return codigoConvite;
    }

    public static class LoginResponseDTOBuilder {
        private String token;
        private String nome;
        private String email;
        private String role;
        private Long userId;
        private String avatar;
        private LocalDate dataValidade;

        private String endereco;
        private String localizacaoUrl;
        private String codigoConvite;

        public LoginResponseDTOBuilder token(String token) {
            this.token = token;
            return this;
        }

        public LoginResponseDTOBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public LoginResponseDTOBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginResponseDTOBuilder role(String role) {
            this.role = role;
            return this;
        }

        public LoginResponseDTOBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public LoginResponseDTOBuilder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public LoginResponseDTOBuilder dataValidade(LocalDate dataValidade) {
            this.dataValidade = dataValidade;
            return this;
        }

        public LoginResponseDTOBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public LoginResponseDTOBuilder localizacaoUrl(String localizacaoUrl) {
            this.localizacaoUrl = localizacaoUrl;
            return this;
        }

        public LoginResponseDTOBuilder codigoConvite(String codigoConvite) {
            this.codigoConvite = codigoConvite;
            return this;
        }

        public LoginResponseDTO build() {
            return new LoginResponseDTO(token, nome, email, role, userId, avatar, dataValidade, endereco,
                    localizacaoUrl, codigoConvite);
        }
    }
}