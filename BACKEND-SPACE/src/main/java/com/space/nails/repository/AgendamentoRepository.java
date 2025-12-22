package com.space.nails.repository;

import com.space.nails.model.Agendamento;
import com.space.nails.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        // Busca por código único (para remarcação)
        Optional<Agendamento> findByCodigo(String codigo);

        // Busca agendamentos de um profissional específico num intervalo de tempo
        @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora BETWEEN :inicio AND :fim ORDER BY a.dataHora ASC")
        List<Agendamento> findByProfissionalAndData(
                        @Param("profissional") Usuario profissional,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);

        // Busca apenas agendamentos do profissional (sem data, para listas gerais)
        List<Agendamento> findByProfissionalOrderByDataHoraDesc(Usuario profissional);

        List<Agendamento> findByCliente(com.space.nails.model.Cliente cliente);

        boolean existsByClienteAndDataHoraAfter(com.space.nails.model.Cliente cliente, LocalDateTime dataHora);

        // --- NOTIFICAÇÕES (Sistema Automático) ---

        // 1. Aviso de 24h antes
        @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete24hEnviado = false AND a.dataHora BETWEEN :agora AND :amanha")
        List<Agendamento> findParaNotificar24h(@Param("agora") LocalDateTime agora,
                        @Param("amanha") LocalDateTime amanha);

        // 2. Aviso de 2h antes
        @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete2hEnviado = false AND a.dataHora BETWEEN :agora AND :daquiDuasHoras")
        List<Agendamento> findParaNotificar2h(@Param("agora") LocalDateTime agora,
                        @Param("daquiDuasHoras") LocalDateTime daquiDuasHoras);

        // 3. Aviso de 30min antes
        @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete30minEnviado = false AND a.dataHora BETWEEN :agora AND :daquiMeiaHora")
        List<Agendamento> findParaNotificar30min(@Param("agora") LocalDateTime agora,
                        @Param("daquiMeiaHora") LocalDateTime daquiMeiaHora);

        // --- FATURAMENTO ---

        // 1. Faturamento do Dia (Status CONCLUIDO e Data Específica)
        @Query("SELECT SUM(s.valor) FROM Agendamento a JOIN a.servico s WHERE a.profissional = :profissional AND a.status = 'CONCLUIDO' AND a.dataHora BETWEEN :inicio AND :fim")
        Double calcularFaturamentoDoDia(@Param("profissional") Usuario profissional,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);

        // 2. Faturamento TOTAL (Status CONCLUIDO, sem filtro de data - ACUMULADO GERAL)
        @Query("SELECT SUM(s.valor) FROM Agendamento a JOIN a.servico s WHERE a.profissional = :profissional AND a.status = 'CONCLUIDO'")
        Double calcularFaturamentoTotal(@Param("profissional") Usuario profissional);

        // Validação de Conflito de Horário
        @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora = :dataHora AND a.status != 'CANCELADO'")
        boolean existeConflitoHorario(@Param("profissional") Usuario profissional,
                        @Param("dataHora") LocalDateTime dataHora);
}