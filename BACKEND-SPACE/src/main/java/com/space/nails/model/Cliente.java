package com.space.nails.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "clientes")
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String telefone;
    private String email; // Opcional, apenas para contato
    private String observacoes;

    // VINCULO: Este cliente pertence a qual profissional?
    @ManyToOne
    @JoinColumn(name = "profissional_id")
    @JsonIgnore // Para não dar erro de loop infinito ao buscar
    private Usuario profissional;
}