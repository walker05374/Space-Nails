package com.space.nails.dto;

public record SemanarioRequestDTO(
    String segunda,
    String terca,
    String quarta,
    String quinta,
    String sexta,
    String objetivos // Novo campo JSON String
) {}