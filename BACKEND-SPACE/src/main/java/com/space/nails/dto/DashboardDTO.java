package com.space.nails.dto;

import java.util.List;

public record DashboardDTO(
    Double faturamentoDia,
    Integer agendamentosPendentes,
    Integer agendamentosRealizados,
    List<AgendamentoDTO> agendaDoDia
) {}