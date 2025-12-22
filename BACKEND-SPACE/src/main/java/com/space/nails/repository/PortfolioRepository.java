package com.space.nails.repository;

import com.space.nails.model.PortfolioItem;
import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<PortfolioItem, Long> {

    List<PortfolioItem> findByProfissionalOrderByDataCriacaoDesc(Usuario profissional);

    // Para filtrar no PublicBookingView
    List<PortfolioItem> findByServicoOrderByDataCriacaoDesc(Servico servico);

    // Para contar quantos itens o profissional tem (se necessário no dashboard)
    long countByProfissional(Usuario profissional);
}
