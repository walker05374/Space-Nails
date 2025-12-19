package com.space.nails.controller;

import com.space.nails.model.Agendamento;
import com.space.nails.model.Usuario;
import com.space.nails.repository.AgendamentoRepository;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.service.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recibos")
public class ReciboController {

    private final PdfService pdfService;
    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;

    // --- CONSTRUTOR MANUAL ---
    public ReciboController(PdfService pdfService, 
                            AgendamentoRepository agendamentoRepository, 
                            UsuarioRepository usuarioRepository) {
        this.pdfService = pdfService;
        this.agendamentoRepository = agendamentoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/{agendamentoId}")
    public ResponseEntity<byte[]> baixarRecibo(@PathVariable Long agendamentoId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = usuarioRepository.findByEmail(auth.getName()).orElseThrow();

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!agendamento.getProfissional().getId().equals(usuarioLogado.getId()) 
            && usuarioLogado.getRole() != Usuario.Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        byte[] pdfBytes = pdfService.gerarReciboPDF(agendamento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "recibo_" + agendamentoId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}