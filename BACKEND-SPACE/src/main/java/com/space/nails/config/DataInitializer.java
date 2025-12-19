package com.space.nails.config;

import com.space.nails.model.Usuario;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String emailAdmin = "admin@admin.com";
            Optional<Usuario> adminExistente = usuarioRepository.findByEmail(emailAdmin);

            if (adminExistente.isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNome("Administrador Principal");
                admin.setEmail(emailAdmin);
                admin.setSenha(passwordEncoder.encode("admin123"));
                admin.setRole(Usuario.Role.ADMIN); // Define como ADMIN
                admin.setFotoUrl("https://ui-avatars.com/api/?name=Admin&background=0D8ABC&color=fff");
                
                usuarioRepository.save(admin);
                System.out.println("✅ ADMIN (Usuario) CRIADO COM SUCESSO.");
            } else {
                System.out.println("ℹ️ ADMIN JÁ EXISTE.");
            }
        };
    }
}