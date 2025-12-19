package com.space.nails.dto;

public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String telefone;
    private String avatarUrl;

    public UsuarioDTO() {}

    public UsuarioDTO(Long id, String nome, String email, String role, String telefone, String avatarUrl) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.role = role;
        this.telefone = telefone;
        this.avatarUrl = avatarUrl;
    }

    public static UsuarioDTOBuilder builder() { return new UsuarioDTOBuilder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public static class UsuarioDTOBuilder {
        private Long id;
        private String nome;
        private String email;
        private String role;
        private String telefone;
        private String avatarUrl;

        public UsuarioDTOBuilder id(Long id) { this.id = id; return this; }
        public UsuarioDTOBuilder nome(String nome) { this.nome = nome; return this; }
        public UsuarioDTOBuilder email(String email) { this.email = email; return this; }
        public UsuarioDTOBuilder role(String role) { this.role = role; return this; }
        public UsuarioDTOBuilder telefone(String telefone) { this.telefone = telefone; return this; }
        public UsuarioDTOBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }

        public UsuarioDTO build() {
            return new UsuarioDTO(id, nome, email, role, telefone, avatarUrl);
        }
    }
}