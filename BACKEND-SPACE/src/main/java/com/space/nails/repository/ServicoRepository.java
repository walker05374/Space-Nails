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

    // Busca serviços do profissional E os globais (onde profissional é null OU é
    // ADMIN)
    // Busca serviços do profissional E os globais (onde profissional é null OU é
    // ADMIN)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Servico s LEFT JOIN s.profissional p WHERE p = :profissional OR s.profissional IS NULL OR p.role = 'ADMIN'")
    List<Servico> findByProfissionalOrProfissionalIsNull(
            @org.springframework.data.repository.query.Param("profissional") Usuario profissional);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Servico s LEFT JOIN s.profissional p WHERE s.profissional IS NULL OR p.role = 'ADMIN'")
    List<Servico> findTemplates();

    List<Servico> findByProfissionalId(Long profissionalId);
}