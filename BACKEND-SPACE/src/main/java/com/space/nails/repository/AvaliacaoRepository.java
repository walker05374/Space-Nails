package com.space.nails.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.space.nails.model.Avaliacao;
import com.space.nails.model.TipoAvaliacao;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByAlunoIdOrderByDataAvaliacaoDesc(Long alunoId);
    
    // CORREÇÃO: Retorna List ao invés de Optional para evitar erro de duplicidade
    List<Avaliacao> findByAlunoIdAndTipoAndUnidade(Long alunoId, TipoAvaliacao tipo, String unidade);
}