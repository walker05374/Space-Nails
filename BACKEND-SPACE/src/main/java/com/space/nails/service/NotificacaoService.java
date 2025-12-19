package com.space.nails.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.space.nails.model.Notificacao;
import com.space.nails.model.Cliente;
import com.space.nails.repository.NotificacaoRepository;
import com.space.nails.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Notificacao criarNotificacao(Cliente destinatario, String mensagem, String cor, String link) {
        // CORREÇÃO: Usando o construtor manual de Notificacao
        Notificacao notificacao = new Notificacao(
            null, // ID (será gerado automaticamente pelo DB)
            destinatario,
            mensagem,
            link,
            LocalDateTime.now(),
            false, // lido = false por padrão
            cor
        );
        return notificacaoRepository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesDoUsuario(Cliente cliente) {
        return notificacaoRepository.findByUsuarioOrderByDataEnvioDesc(cliente);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesNaoLidasDoUsuario(Cliente cliente) {
        return notificacaoRepository.findByUsuarioAndLidoFalseOrderByDataEnvioDesc(cliente);
    }

    @Transactional
    public Notificacao marcarNotificacaoComoLida(Long notificacaoId, String userEmail) {
        Cliente usuarioAutenticado = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userEmail));

        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada com ID: " + notificacaoId));
        
        if (!notificacao.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new SecurityException("Usuário não autorizado a marcar esta notificação como lida.");
        }
        
        notificacao.setLido(true);
        return notificacaoRepository.save(notificacao);
    }
}