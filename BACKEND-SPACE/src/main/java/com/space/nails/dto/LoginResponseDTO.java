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

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, String nome, String email, String role, Long userId, String avatar, LocalDate dataValidade) {
        this.token = token;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.avatar = avatar;
        this.dataValidade = dataValidade;
    }

    public static LoginResponseDTOBuilder builder() { return new LoginResponseDTOBuilder(); }

    public String getToken() { return token; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getUserId() { return userId; }
    public String getAvatar() { return avatar; }
    public LocalDate getDataValidade() { return dataValidade; }

    public static class LoginResponseDTOBuilder {
        private String token;
        private String nome;
        private String email;
        private String role;
        private Long userId;
        private String avatar;
        private LocalDate dataValidade;

        public LoginResponseDTOBuilder token(String token) { this.token = token; return this; }
        public LoginResponseDTOBuilder nome(String nome) { this.nome = nome; return this; }
        public LoginResponseDTOBuilder email(String email) { this.email = email; return this; }
        public LoginResponseDTOBuilder role(String role) { this.role = role; return this; }
        public LoginResponseDTOBuilder userId(Long userId) { this.userId = userId; return this; }
        public LoginResponseDTOBuilder avatar(String avatar) { this.avatar = avatar; return this; }
        public LoginResponseDTOBuilder dataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; return this; }

        public LoginResponseDTO build() {
            return new LoginResponseDTO(token, nome, email, role, userId, avatar, dataValidade);
        }
    }
}