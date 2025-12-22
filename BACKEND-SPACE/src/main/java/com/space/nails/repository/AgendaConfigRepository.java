package com.space.nails.repository;

import com.space.nails.model.AgendaConfig;
import com.space.nails.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AgendaConfigRepository extends JpaRepository<AgendaConfig, Long> {
    List<AgendaConfig> findByProfissional(Usuario profissional);

    Optional<AgendaConfig> findByProfissionalAndDiaSemana(Usuario profissional, Integer diaSemana);
}
