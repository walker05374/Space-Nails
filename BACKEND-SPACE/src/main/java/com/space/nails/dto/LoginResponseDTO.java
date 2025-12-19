package com.space.nails.dto;

import com.space.nails.model.Perfil;

public record LoginResponseDTO(
    String token,
    String nome,
    String email,
    Perfil role, // Mudamos de 'perfil' para 'role' para padronizar com o front
    String avatarUrl
) {}