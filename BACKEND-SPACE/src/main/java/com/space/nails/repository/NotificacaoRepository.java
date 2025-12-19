package com.space.nails.repository;

import com.space.nails.model.Notificacao;
import com.space.nails.model.Usuario; // Mudança de Cliente para Usuario
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    
    // Busca notificações de um USUARIO específico
    List<Notificacao> findByUsuarioOrderByDataEnvioDesc(Usuario usuario);

    // Busca não lidas de um USUARIO específico
    List<Notificacao> findByUsuarioAndLidoFalseOrderByDataEnvioDesc(Usuario usuario);
}