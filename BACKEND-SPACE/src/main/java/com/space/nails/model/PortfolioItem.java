package com.space.nails.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_items")
public class PortfolioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String imagemUrl;

    @Column(nullable = false)
    private Integer clicks = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Usuario profissional;

    // Opcional: Relacionar a um serviço específico (para mostrar "Fotos deste
    // serviço")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Servico servico;

    @Column(nullable = false)
    private LocalDateTime dataCriacao;

    // ID público do Cloudinary (para facilitar exclusão)
    @Column(length = 500)
    private String cloudinaryPublicId;

    // CONSTRUTORES
    public PortfolioItem() {
        this.dataCriacao = LocalDateTime.now();
    }

    public PortfolioItem(Long id, String titulo, String imagemUrl, Usuario profissional, Servico servico) {
        this.id = id;
        this.titulo = titulo;
        this.imagemUrl = imagemUrl;
        this.profissional = profissional;
        this.servico = servico;
        this.clicks = 0;
        this.dataCriacao = LocalDateTime.now();
    }

    // GETTERS E SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public Integer getClicks() {
        return clicks;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public Usuario getProfissional() {
        return profissional;
    }

    public void setProfissional(Usuario profissional) {
        this.profissional = profissional;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }
}
