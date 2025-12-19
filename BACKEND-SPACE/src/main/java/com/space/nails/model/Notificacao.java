package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CORREÇÃO: Relacionamento com Usuario (Profissional/Admin)
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String mensagem;
    private String link;
    private LocalDateTime dataEnvio;
    private boolean lido;
    private String cor; // 'success', 'warning', 'info', 'error'

    public Notificacao() {}

    public Notificacao(Long id, Usuario usuario, String mensagem, String link, LocalDateTime dataEnvio, boolean lido, String cor) {
        this.id = id;
        this.usuario = usuario;
        this.mensagem = mensagem;
        this.link = link;
        this.dataEnvio = dataEnvio;
        this.lido = lido;
        this.cor = cor;
    }

    // Getters e Setters Manuais
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
    public boolean isLido() { return lido; }
    public void setLido(boolean lido) { this.lido = lido; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
}