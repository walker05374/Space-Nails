package com.space.nails.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.space.nails.model.Notificacao;
import com.space.nails.model.Usuario; // CORREÇÃO: Importar Usuario, não Cliente
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
    public Notificacao criarNotificacao(Usuario destinatario, String mensagem, String cor, String link) {
        Notificacao notificacao = new Notificacao(
            null, 
            destinatario, // Agora recebe um Usuario
            mensagem,
            link,
            LocalDateTime.now(),
            false, 
            cor
        );
        return notificacaoRepository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesDoUsuario(Usuario usuario) {
        return notificacaoRepository.findByUsuarioOrderByDataEnvioDesc(usuario);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesNaoLidasDoUsuario(Usuario usuario) {
        return notificacaoRepository.findByUsuarioAndLidoFalseOrderByDataEnvioDesc(usuario);
    }

    @Transactional
    public Notificacao marcarNotificacaoComoLida(Long notificacaoId, String userEmail) {
        // CORREÇÃO DO ERRO: Agora a variável é do tipo Usuario
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + userEmail));

        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada com ID: " + notificacaoId));
        
        // Verifica se a notificação pertence ao usuário logado
        if (!notificacao.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new SecurityException("Usuário não autorizado a marcar esta notificação como lida.");
        }
        
        notificacao.setLido(true);
        return notificacaoRepository.save(notificacao);
    }
}