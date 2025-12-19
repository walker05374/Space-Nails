package com.space.nails.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.space.nails.model.Diario;

import java.util.List;

public interface DiarioRepository extends JpaRepository<Diario, Long> {
    // Busca o diário de uma criança específica, ordenado do mais recente
    List<Diario> findByDependenteIdOrderByDataRegistroDesc(Long dependenteId);
}