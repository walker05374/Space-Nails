package com.space.nails.repository;

import com.space.nails.model.Cliente;
import com.space.nails.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByProfissional(Usuario profissional);
}