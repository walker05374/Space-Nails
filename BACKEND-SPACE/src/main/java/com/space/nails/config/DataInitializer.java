package com.space.nails.config;

import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ServicoRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, 
                                   ServicoRepository servicoRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Cria Admin se não existir
            if (usuarioRepository.findByEmail("admin@space.com").isEmpty()) {
                Usuario admin = Usuario.builder()
                        .nome("Administrador")
                        .email("admin@space.com")
                        .senha(passwordEncoder.encode("admin123"))
                        .role(Usuario.Role.ADMIN)
                        .ativo(true)
                        .build();
                usuarioRepository.save(admin);
                System.out.println("✅ Admin criado: admin@space.com / admin123");
            }

            // Exemplo: Cria um profissional padrão
            if (usuarioRepository.findByEmail("prof@space.com").isEmpty()) {
                Usuario prof = Usuario.builder()
                        .nome("Ana Manicure")
                        .email("prof@space.com")
                        .senha(passwordEncoder.encode("123456"))
                        .role(Usuario.Role.PROFISSIONAL)
                        .ativo(true)
                        .dataValidade(LocalDate.now().plusDays(30))
                        .build();
                prof = usuarioRepository.save(prof);
                
                // Cria serviços iniciais para este profissional
                servicoRepository.save(new Servico(null, "Manicure", 35.00, 40, prof));
                servicoRepository.save(new Servico(null, "Pedicure", 40.00, 40, prof));
                System.out.println("✅ Profissional e Serviços criados!");
            }
        };
    }
}