package com.space.nails.repository;

import com.space.nails.model.Agendamento;
import com.space.nails.model.StatusAgendamento;
import com.space.nails.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {
    
    // Buscar agendamentos do dia para o profissional específico
    @Query("SELECT a FROM Agendamento a WHERE a.profissional = :profissional AND a.dataHora BETWEEN :inicio AND :fim ORDER BY a.dataHora ASC")
    List<Agendamento> findByData(Cliente profissional, LocalDateTime inicio, LocalDateTime fim);

    // Buscar pendentes para notificação (Automação do WhatsApp)
    // Busca agendamentos PENDENTES que ocorrerão entre 'agora' e 'tempoLimite' e que ainda não foram notificados
    @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete24hEnviado = false AND a.dataHora BETWEEN :agora AND :amanha")
    List<Agendamento> findParaNotificar24h(LocalDateTime agora, LocalDateTime amanha);

    @Query("SELECT a FROM Agendamento a WHERE a.status = 'PENDENTE' AND a.lembrete2hEnviado = false AND a.dataHora BETWEEN :agora AND :daquiDuasHoras")
    List<Agendamento> findParaNotificar2h(LocalDateTime agora, LocalDateTime daquiDuasHoras);
    
    // Calcular Faturamento do dia (Soma valor dos serviços CONCLUIDOS hoje)
    @Query("SELECT SUM(s.valor) FROM Agendamento a JOIN a.servico s WHERE a.profissional = :profissional AND a.status = 'CONCLUIDO' AND a.dataHora BETWEEN :inicio AND :fim")
    Double calcularFaturamentoDoDia(@Param("profissional") Cliente profissional, @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}