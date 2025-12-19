package com.space.nails.repository;

import com.space.nails.model.Cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByProfissionalOrderByNomeAsc(Cliente profissional);
    
    // Para ranking de clientes (quem gastou mais)
    List<Cliente> findByProfissionalOrderByTotalGastoDesc(Cliente profissional);
}