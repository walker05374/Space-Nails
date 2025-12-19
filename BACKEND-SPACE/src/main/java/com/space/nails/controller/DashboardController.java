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

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    // --- CONSTRUTOR MANUAL ---
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
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(LocalTime.MAX);

        long totalProfs = 0;
        long totalClientes = 0;
        long agendaHoje = 0;
        double faturamento = 0.0;

        if (usuario.getRole() == Usuario.Role.ADMIN) {
            totalProfs = usuarioRepository.count();
            totalClientes = clienteRepository.count();
            agendaHoje = agendamentoRepository.count(); 
        } else {
            totalProfs = 1; 
            totalClientes = clienteRepository.findByProfissional(usuario).size();
            agendaHoje = agendamentoRepository.findByProfissionalAndData(usuario, inicioDia, fimDia).size();
            
            Double fat = agendamentoRepository.calcularFaturamentoDoDia(usuario, inicioDia, fimDia);
            faturamento = (fat != null) ? fat : 0.0;
        }

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalProfissionais(totalProfs)
                .totalClientes(totalClientes)
                .agendamentosHoje(agendaHoje)
                .faturamentoHoje(faturamento)
                .build());
    }
}