package com.space.nails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.space.nails.model.Notificacao;
import com.space.nails.model.Usuario;

import java.util.List;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioOrderByDataEnvioDesc(Usuario usuario);
    List<Notificacao> findByUsuarioAndLidoFalseOrderByDataEnvioDesc(Usuario usuario);
}