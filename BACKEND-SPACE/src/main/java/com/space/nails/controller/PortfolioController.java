package com.space.nails.controller;

import com.space.nails.model.PortfolioItem;
import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import com.space.nails.repository.PortfolioRepository;
import com.space.nails.repository.ServicoRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final PortfolioRepository portfolioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ServicoRepository servicoRepository;
    private final com.space.nails.service.FileStorageService fileStorageService;

    public PortfolioController(PortfolioRepository portfolioRepository, UsuarioRepository usuarioRepository,
            ServicoRepository servicoRepository, com.space.nails.service.FileStorageService fileStorageService) {
        this.portfolioRepository = portfolioRepository;
        this.usuarioRepository = usuarioRepository;
        this.servicoRepository = servicoRepository;
        this.fileStorageService = fileStorageService;
    }

    // --- ENDPOINTS PROTEGIDOS (PROFISSIONAL) ---

    // Upload/Criar Item
    @PostMapping(value = "/portfolio", consumes = { "multipart/form-data" })
    public ResponseEntity<PortfolioItem> criar(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "servicoId", required = false) Long servicoId,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "imagemUrl", required = false) String imagemUrlExterno) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String finalUrl = "";

        if (file != null && !file.isEmpty()) {
            String filename = fileStorageService.store(file);
            finalUrl = "/uploads/" + filename;
        } else if (imagemUrlExterno != null && !imagemUrlExterno.isEmpty()) {
            finalUrl = imagemUrlExterno;
        } else {
            throw new RuntimeException("É necessário enviar uma Imagem (Arquivo) ou URL.");
        }

        Servico servico = null;
        if (servicoId != null) {
            servico = servicoRepository.findById(servicoId).orElse(null);
        }

        PortfolioItem item = new PortfolioItem();
        item.setTitulo(titulo);
        item.setImagemUrl(finalUrl);
        item.setProfissional(profissional);
        item.setServico(servico);
        item.setClicks(0);
        item.setDataCriacao(LocalDateTime.now());

        return ResponseEntity.ok(portfolioRepository.save(item));
    }

    // Listar Meus Itens
    @GetMapping("/portfolio/meus")
    public ResponseEntity<List<PortfolioItem>> listarMeus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(portfolioRepository.findByProfissionalOrderByDataCriacaoDesc(profissional));
    }

    @DeleteMapping("/portfolio/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PortfolioItem item = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // Segurança: Só dono ou Admin deleta
        if (!item.getProfissional().getId().equals(profissional.getId())
                && profissional.getRole() != Usuario.Role.ADMIN) {
            throw new RuntimeException("Acesso negado");
        }

        portfolioRepository.delete(item);
        return ResponseEntity.noContent().build();
    }

    // --- ENDPOINTS PÚBLICOS ---

    // Listar Itens do Profissional (Público)
    @GetMapping("/public/portfolio/{profissionalId}")
    public ResponseEntity<List<PortfolioItem>> listarPublico(@PathVariable Long profissionalId) {
        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));
        return ResponseEntity.ok(portfolioRepository.findByProfissionalOrderByDataCriacaoDesc(profissional));
    }

    // Listar Itens do Serviço (Público -> para Modal no Booking)
    @GetMapping("/public/portfolio/servico/{servicoId}")
    public ResponseEntity<List<PortfolioItem>> listarPorServico(@PathVariable Long servicoId) {
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        return ResponseEntity.ok(portfolioRepository.findByServicoOrderByDataCriacaoDesc(servico));
    }

    // Incrementar Clicks
    @PostMapping("/public/portfolio/{id}/click")
    public ResponseEntity<Void> click(@PathVariable Long id) {
        PortfolioItem item = portfolioRepository.findById(id).orElse(null);
        if (item != null) {
            item.setClicks(item.getClicks() + 1);
            portfolioRepository.save(item);
        }
        return ResponseEntity.ok().build();
    }
}
