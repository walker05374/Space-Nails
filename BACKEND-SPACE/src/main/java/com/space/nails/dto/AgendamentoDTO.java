package com.space.nails.dto;
import java.time.LocalDateTime;

public record AgendamentoDTO(
    Long id,
    String nomeCliente,
    String servico,
    Double valor,
    LocalDateTime dataHora,
    String status,
    String telefoneCliente
) {}