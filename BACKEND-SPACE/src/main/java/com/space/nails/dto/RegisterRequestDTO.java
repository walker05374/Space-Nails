package com.space.nails.dto;

public record RegisterRequestDTO(
    String nome,
    String email,
    String senha,
    String pin, // O novo campo obrigatório
    String avatarUrl
) {}