package com.space.nails.controller;

import com.space.nails.model.Notificacao;
import com.space.nails.model.Usuario;
import com.space.nails.repository.NotificacaoRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/notificacoes")
public class NotificacaoController {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoController(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/nao-lidas/{usuarioId}")
    public ResponseEntity<List<com.space.nails.dto.NotificacaoDTO>> listarNaoLidas(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Notificacao> notificacoes = notificacaoRepository.findByUsuarioAndLidoFalseOrderByDataEnvioDesc(usuario);

        List<com.space.nails.dto.NotificacaoDTO> dtos = notificacoes.stream()
                .map(n -> new com.space.nails.dto.NotificacaoDTO(
                        n.getId(),
                        n.getMensagem(),
                        n.getLink(),
                        n.getDataEnvio(),
                        n.isLido(),
                        n.getCor()))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long id) {
        Notificacao notificacao = notificacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));
        notificacao.setLido(true);
        notificacaoRepository.save(notificacao);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/limpar/{usuarioId}")
    public ResponseEntity<Void> limparTodas(@PathVariable Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        notificacaoRepository.deleteByUsuario(usuario);
        return ResponseEntity.ok().build();
    }
}
