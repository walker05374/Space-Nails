package com.space.nails.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.space.nails.model.Semanario;

import java.util.Optional;

public interface SemanarioRepository extends JpaRepository<Semanario, Long> {
    // Buscar o último Semanário criado
    Optional<Semanario> findTopByOrderByDataCriacaoDesc();
}