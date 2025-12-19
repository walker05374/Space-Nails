package com.space.nails.repository;

import com.space.nails.model.Agendamento;
import com.space.nails.model.Usuario;
import com.space.nails.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    // Busca agendamentos de um profissional específico num intervalo de tempo
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora BETWEEN :inicio AND :fim ORDER BY a.dataHora ASC")
    List<Agendamento> findByProfissionalAndData(
        @Param("profissional") Usuario profissional, 
        @Param("inicio") LocalDateTime inicio, 
        @Param("fim") LocalDateTime fim
    );

    // Busca apenas agendamentos do profissional (sem data, para listas gerais)
    List<Agendamento> findByProfissionalOrderByDataHoraDesc(Usuario profissional);

    // Notificações (Lógica do sistema)
    @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete24hEnviado = false AND a.dataHora BETWEEN :agora AND :amanha")
    List<Agendamento> findParaNotificar24h(LocalDateTime agora, LocalDateTime amanha);

    @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete2hEnviado = false AND a.dataHora BETWEEN :agora AND :daquiDuasHoras")
    List<Agendamento> findParaNotificar2h(LocalDateTime agora, LocalDateTime daquiDuasHoras);
    
    // Dashboard: Faturamento do Dia (Status CONCLUIDO)
    @Query("SELECT SUM(s.valor) FROM Agendamento a JOIN a.servico s WHERE a.profissional = :profissional AND a.status = 'CONCLUIDO' AND a.dataHora BETWEEN :inicio AND :fim")
    Double calcularFaturamentoDoDia(@Param("profissional") Usuario profissional, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    // Validação de Conflito de Horário
    @Query("SELECT COUNT(a) > 0 FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora = :dataHora AND a.status != 'CANCELADO'")
    boolean existeConflitoHorario(Usuario profissional, LocalDateTime dataHora);
}