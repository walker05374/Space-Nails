package com.space.nails.controller;

import com.space.nails.dto.DashboardStatsDTO;
import com.space.nails.model.Usuario;
import com.space.nails.repository.AgendamentoRepository;
import com.space.nails.repository.ClienteRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    // Construtor Manual (Injeção de Dependência)
    public DashboardController(UsuarioRepository usuarioRepository, 
                               ClienteRepository clienteRepository, 
                               AgendamentoRepository agendamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        long totalProfs = 0;
        long totalClientes = 0;
        long agendaHoje = 0;
        
        double faturamentoHoje = 0.0;
        double faturamentoTotal = 0.0;

        // Lógica de Estatísticas (Admin vs Profissional)
        if (usuario.getRole() == Usuario.Role.ADMIN) {
            totalProfs = usuarioRepository.count();
            totalClientes = clienteRepository.count();
            agendaHoje = agendamentoRepository.count(); 
        } else {
            // Profissional Comum
            totalProfs = 1; 
            totalClientes = clienteRepository.findByProfissional(usuario).size();
            
            // Agendamentos HOJE
            agendaHoje = agendamentoRepository.findByProfissionalAndData(usuario, inicioDia, fimDia).size();
            
            // 1. Faturamento Hoje (Só o que caiu hoje)
            Double fatHoje = agendamentoRepository.calcularFaturamentoDoDia(usuario, inicioDia, fimDia);
            faturamentoHoje = (fatHoje != null) ? fatHoje : 0.0;

            // 2. Faturamento Total (Acumulado de sempre)
            Double fatTotal = agendamentoRepository.calcularFaturamentoTotal(usuario);
            faturamentoTotal = (fatTotal != null) ? fatTotal : 0.0;
        }

        // --- LÓGICA DE VALIDADE DA ASSINATURA ---
        String avisoValidade = null;
        Integer diasRestantes = null;
        boolean assinaturaAtiva = true;

        if (usuario.getDataValidade() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), usuario.getDataValidade());
            diasRestantes = (int) dias;

            if (dias < 0) {
                avisoValidade = "Sua assinatura expirou. Renove agora.";
                assinaturaAtiva = false;
            } else if (dias <= 5) {
                avisoValidade = "Atenção: Sua assinatura vence em " + dias + " dias.";
            }
        }

        // --- RETORNO MANUAL (Preenchendo o DTO) ---
        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalProfissionais(totalProfs);
        dto.setTotalClientes(totalClientes);
        dto.setAgendamentosHoje(agendaHoje);
        
        // Setando os faturamentos separados
        dto.setFaturamentoHoje(faturamentoHoje);
        dto.setFaturamentoTotal(faturamentoTotal);
        
        dto.setAvisoValidade(avisoValidade);
        dto.setDiasRestantes(diasRestantes);
        dto.setAssinaturaAtiva(assinaturaAtiva);

        return ResponseEntity.ok(dto);
    }
}