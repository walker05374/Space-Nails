package com.space.nails.dto;

import java.time.LocalDateTime;

public record DiarioDTO(
    Long id,
    String emocao,
    int intensidade,
    String relato,

    String desenhoBase64,
    LocalDateTime dataRegistro
) {}