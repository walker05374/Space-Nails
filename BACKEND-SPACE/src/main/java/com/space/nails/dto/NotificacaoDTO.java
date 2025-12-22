package com.space.nails.dto;

import java.time.LocalDateTime;

public class NotificacaoDTO {
    private Long id;
    private String mensagem;
    private String link;
    private LocalDateTime dataEnvio;
    private boolean lido;
    private String cor;

    public NotificacaoDTO(Long id, String mensagem, String link, LocalDateTime dataEnvio, boolean lido, String cor) {
        this.id = id;
        this.mensagem = mensagem;
        this.link = link;
        this.dataEnvio = dataEnvio;
        this.lido = lido;
        this.cor = cor;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public boolean isLido() {
        return lido;
    }

    public String getCor() {
        return cor;
    }
}
