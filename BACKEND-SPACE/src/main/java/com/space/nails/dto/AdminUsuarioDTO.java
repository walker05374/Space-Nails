package com.space.nails.dto;

import java.time.LocalDate;
import java.util.List;

import com.space.nails.model.Perfil;

public record AdminUsuarioDTO(
    Long id,
    String nome,
    String email,
    Perfil perfil,
    LocalDate dataCadastro,
    List<AdminUsuarioDTO> dependentes // Novo campo para aninhamento
) {}