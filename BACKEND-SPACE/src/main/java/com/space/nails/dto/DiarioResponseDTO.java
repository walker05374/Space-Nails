package com.space.nails.dto;
import java.time.LocalDateTime;

import com.space.nails.model.Emocao;

public record DiarioResponseDTO(
    Long id,
    Emocao emocao,
    int intensidade,
    String relato,
    LocalDateTime dataRegistro
) {}