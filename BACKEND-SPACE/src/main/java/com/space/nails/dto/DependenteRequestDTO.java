package com.space.nails.dto;

import java.time.LocalDate;

public record DependenteRequestDTO(
    String nome,
    LocalDate dataNascimento,
    String avatarUrl
) {}