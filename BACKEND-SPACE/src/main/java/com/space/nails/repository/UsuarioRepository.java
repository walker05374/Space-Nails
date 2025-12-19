package com.space.nails.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.space.nails.model.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    
    // --- NOVO MÉTODO NECESSÁRIO PARA RECUPERAÇÃO DE SENHA ---
    Optional<Usuario> findByResetToken(String resetToken);
}