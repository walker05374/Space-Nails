package com.space.nails.dto;
import java.time.LocalDate;

public record DependenteDTO(
    Long id,
    String nome,
    LocalDate dataNascimento,
    String genero,
    String avatarUrl
) {}