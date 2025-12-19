package com.space.nails.repository;

import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    // Agora busca corretamente passando um Usuario
    List<Servico> findByProfissional(Usuario profissional);
}