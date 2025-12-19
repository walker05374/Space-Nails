package com.space.nails.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    
    @Column(unique = true)
    private String email;
    
    private String senha;
    private String telefone;
    private String fotoUrl;
    private String resetToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role { ADMIN, PROFISSIONAL }

    public Usuario() {}

    public Usuario(Long id, String nome, String email, String senha, String telefone, String fotoUrl, String resetToken, Role role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.fotoUrl = fotoUrl;
        this.resetToken = resetToken;
        this.role = role;
    }

    // --- BUILDER MANUAL ---
    public static UsuarioBuilder builder() { return new UsuarioBuilder(); }

    public static class UsuarioBuilder {
        private Long id;
        private String nome;
        private String email;
        private String senha;
        private String telefone;
        private String fotoUrl;
        private String resetToken;
        private Role role;

        public UsuarioBuilder id(Long id) { this.id = id; return this; }
        public UsuarioBuilder nome(String nome) { this.nome = nome; return this; }
        public UsuarioBuilder email(String email) { this.email = email; return this; }
        public UsuarioBuilder senha(String senha) { this.senha = senha; return this; }
        public UsuarioBuilder telefone(String telefone) { this.telefone = telefone; return this; }
        public UsuarioBuilder fotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; return this; }
        public UsuarioBuilder resetToken(String resetToken) { this.resetToken = resetToken; return this; }
        public UsuarioBuilder role(Role role) { this.role = role; return this; }

        public Usuario build() {
            return new Usuario(id, nome, email, senha, telefone, fotoUrl, resetToken, role);
        }
    }

    // --- GETTERS E SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}