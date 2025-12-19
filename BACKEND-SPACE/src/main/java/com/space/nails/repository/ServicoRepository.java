package com.space.nails.repository;

import com.space.nails.model.Servico;
import com.space.nails.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByProfissional(Cliente profissional);
}