package com.space.nails.service;

import com.space.nails.model.Agendamento;
import com.space.nails.repository.AgendamentoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacaoWhatsappService {

    private final AgendamentoRepository agendamentoRepository;

    public NotificacaoWhatsappService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    // Roda a cada 5 minutos para verificar notificações
    @Scheduled(fixedRate = 300000) 
    @Transactional
    public void verificarLembretes() {
        LocalDateTime agora = LocalDateTime.now();

        // 1. Verificar Lembretes de 24 horas (dia anterior)
        LocalDateTime amanha = agora.plusHours(24);
        // Pega agendamentos entre 23h e 25h a partir de agora (janela de tolerância)
        List<Agendamento> lista24h = agendamentoRepository.findParaNotificar24h(agora.plusHours(23), agora.plusHours(25));
        
        for (Agendamento a : lista24h) {
            enviarMensagem(a, "Lembrete: Seu horário é amanhã!");
            a.setLembrete24hEnviado(true);
            agendamentoRepository.save(a);
        }

        // 2. Verificar Lembretes de 2 horas (mesmo dia)
        // Pega agendamentos entre 1h50min e 2h10min a partir de agora
        List<Agendamento> lista2h = agendamentoRepository.findParaNotificar2h(agora.plusMinutes(110), agora.plusMinutes(130));
        
        for (Agendamento a : lista2h) {
            enviarMensagem(a, "Lembrete: Seu horário é daqui a pouco!");
            a.setLembrete2hEnviado(true);
            agendamentoRepository.save(a);
        }
    }

    private void enviarMensagem(Agendamento a, String tipoAviso) {
        // AQUI ENTRARIA A API REAL DO WHATSAPP (Twilio, Z-API, etc)
        // Por enquanto, vamos simular no console para você ver funcionando.
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String horario = a.getDataHora().format(formatter);
        String local = a.getLocalizacao() != null ? a.getLocalizacao() : "No endereço cadastrado.";
        
        String mensagem = String.format(
            "Olá %s! %s\nServiço: %s\nHorário: %s\nLocal: %s\nGoogle Maps: https://maps.google.com/?q=%s",
            a.getCliente().getNome(),
            tipoAviso,
            a.getServico().getNome(),
            horario,
            local,
            local.replace(" ", "+")
        );

        System.out.println(">>> ENVIANDO ZAP PARA: " + a.getCliente().getTelefone());
        System.out.println(">>> MENSAGEM: " + mensagem);
        System.out.println("--------------------------------------------------");
    }
}