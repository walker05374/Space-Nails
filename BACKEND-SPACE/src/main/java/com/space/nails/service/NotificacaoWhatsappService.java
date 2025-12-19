package com.space.nails.service;

import com.space.nails.model.Agendamento;
import com.space.nails.repository.AgendamentoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoWhatsappService {

    private final AgendamentoRepository agendamentoRepository;
    private final RestTemplate restTemplate;

    public NotificacaoWhatsappService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.restTemplate = new RestTemplate(); // Usado para chamar APIs externas
    }

    // Roda a cada 1 minuto (60000ms) para garantir precisão nos 30min
    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void verificarLembretes() {
        LocalDateTime agora = LocalDateTime.now();

        // -----------------------------------------------------------
        // 1. Verificar Lembretes de 24 horas (dia anterior)
        // -----------------------------------------------------------
        // Janela de busca: entre 23h50 e 24h10 a partir de agora
        List<Agendamento> lista24h = agendamentoRepository.findParaNotificar24h(
            agora.plusMinutes(1430), agora.plusMinutes(1450)
        );
        
        for (Agendamento a : lista24h) {
            enviarMensagem(a, "Lembrete: Seu horário é amanhã! 💅");
            a.setLembrete24hEnviado(true);
            agendamentoRepository.save(a);
        }

        // -----------------------------------------------------------
        // 2. Verificar Lembretes de 2 horas (mesmo dia)
        // -----------------------------------------------------------
        // Janela de busca: entre 1h55 e 2h05 a partir de agora
        List<Agendamento> lista2h = agendamentoRepository.findParaNotificar2h(
            agora.plusMinutes(115), agora.plusMinutes(125)
        );
        
        for (Agendamento a : lista2h) {
            enviarMensagem(a, "Seu horário é daqui a 2 horas! Não se atrase. ⏰");
            a.setLembrete2hEnviado(true);
            agendamentoRepository.save(a);
        }

        // -----------------------------------------------------------
        // 3. Verificar Lembretes de 30 minutos (urgente)
        // -----------------------------------------------------------
        // Janela de busca: entre 25min e 35min a partir de agora
        List<Agendamento> lista30min = agendamentoRepository.findParaNotificar30min(
            agora.plusMinutes(25), agora.plusMinutes(35)
        );
        
        for (Agendamento a : lista30min) {
            enviarMensagem(a, "Seu horário começa em 30 minutos! Estamos te esperando. 🏃‍♀️");
            a.setLembrete30minEnviado(true);
            agendamentoRepository.save(a);
        }
    }

    private void enviarMensagem(Agendamento a, String mensagemAviso) {
        if (a.getCliente().getTelefone() == null || a.getCliente().getTelefone().isEmpty()) {
            System.out.println(">>> ERRO: Cliente " + a.getCliente().getNome() + " não tem telefone.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String horario = a.getDataHora().format(formatter);
        String local = a.getLocalizacao() != null ? a.getLocalizacao() : "No nosso espaço.";
        
        String mensagemCompleta = String.format(
            "Olá %s! ✨\n%s\n\nServiço: %s\nHorário: %s\nLocal: %s",
            a.getCliente().getNome(),
            mensagemAviso,
            a.getServico().getNome(),
            horario,
            local
        );

        // --- SIMULAÇÃO NO CONSOLE (Para você ver funcionando) ---
        System.out.println("==================================================");
        System.out.println(">>> ENVIANDO WHATSAPP PARA: " + a.getCliente().getTelefone());
        System.out.println(">>> CONTEÚDO:");
        System.out.println(mensagemCompleta);
        System.out.println("==================================================");

        // --- CÓDIGO REAL (Descomente quando tiver uma API de WhatsApp) ---
        /*
        try {
            // Exemplo usando Evolution API ou Z-API
            String urlApi = "http://localhost:8080/message/sendText/instancia1"; // Sua URL aqui
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("number", "55" + a.getCliente().getTelefone()); // Adiciona 55 do Brasil
            payload.put("options", Map.of("delay", 1200, "presence", "composing"));
            payload.put("textMessage", Map.of("text", mensagemCompleta));
            
            // Header com API Key se necessário
            // HttpHeaders headers = new HttpHeaders();
            // headers.set("apikey", "SUA_API_KEY");
            // HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // restTemplate.postForEntity(urlApi, request, String.class);
            
        } catch (Exception e) {
            System.err.println("Falha ao enviar msg real: " + e.getMessage());
        }
        */
    }
}