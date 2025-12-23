package com.space.nails.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
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

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String senha;
    private String telefone;
    private String fotoUrl;
    private String resetToken;
    private String endereco; // Novo campo de endereço
    private String localizacaoUrl; // Link do Google Maps

    @Column(unique = true)
    private String codigoConvite; // Código curto para URL (Ex: "AF3K19")

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean ativo = true;

    // Campo de Validade (Assinatura)
    private LocalDate dataValidade;

    private java.time.LocalDateTime ultimoAcesso;

    public enum Role {
        ADMIN, PROFISSIONAL
    }

    // --- CONSTRUTORES ---
    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senha, String telefone, String fotoUrl, String resetToken,
            String endereco, String localizacaoUrl, String codigoConvite,
            Role role, boolean ativo, LocalDate dataValidade) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.fotoUrl = fotoUrl;
        this.resetToken = resetToken;
        this.endereco = endereco;
        this.localizacaoUrl = localizacaoUrl;
        this.codigoConvite = codigoConvite;
        this.role = role;
        this.ativo = ativo;
        this.dataValidade = dataValidade;
    }

    // --- BUILDER MANUAL ---
    public static UsuarioBuilder builder() {
        return new UsuarioBuilder();
    }

    public static class UsuarioBuilder {
        private Long id;
        private String nome;
        private String email;
        private String senha;
        private String telefone;
        private String fotoUrl;

        private String resetToken;
        private String endereco;
        private String localizacaoUrl;
        private String codigoConvite;
        private Role role;
        private boolean ativo = true;
        private LocalDate dataValidade;

        public UsuarioBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public UsuarioBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UsuarioBuilder senha(String senha) {
            this.senha = senha;
            return this;
        }

        public UsuarioBuilder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public UsuarioBuilder fotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
            return this;
        }

        public UsuarioBuilder resetToken(String resetToken) {
            this.resetToken = resetToken;
            return this;
        }

        public UsuarioBuilder endereco(String endereco) {
            this.endereco = endereco;
            return this;
        }

        public UsuarioBuilder localizacaoUrl(String localizacaoUrl) {
            this.localizacaoUrl = localizacaoUrl;
            return this;
        }

        public UsuarioBuilder codigoConvite(String codigoConvite) {
            this.codigoConvite = codigoConvite;
            return this;
        }

        public UsuarioBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UsuarioBuilder ativo(boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        public UsuarioBuilder dataValidade(LocalDate dataValidade) {
            this.dataValidade = dataValidade;
            return this;
        }

        public Usuario build() {
            return new Usuario(id, nome, email, senha, telefone, fotoUrl, resetToken, endereco, localizacaoUrl,
                    codigoConvite, role,
                    ativo,
                    dataValidade);
        }
    }

    // --- GETTERS E SETTERS ---
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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public java.time.LocalDateTime getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(java.time.LocalDateTime ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }

    // --- USER DETAILS METHODS ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null)
            return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Se dataValidade for nula, assume-se vitalício (retorna true)
        if (dataValidade == null)
            return true;

        // Se a data de validade for ANTERIOR a hoje, está expirado (retorna false)
        return !dataValidade.isBefore(LocalDate.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}