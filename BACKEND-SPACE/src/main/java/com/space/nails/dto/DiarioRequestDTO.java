package com.space.nails.dto;
import com.space.nails.model.Emocao;

public record DiarioRequestDTO(
    Emocao emocao,
    int intensidade,
    String relato,
    Long criancaId // Opcional: Se o pai estiver registrando pelo filho
) {}