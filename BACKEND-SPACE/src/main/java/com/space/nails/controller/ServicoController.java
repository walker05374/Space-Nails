package com.space.nails.controller;

import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ServicoRepository;
import com.space.nails.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {

        private final ServicoRepository servicoRepository;
        private final UsuarioRepository usuarioRepository;

        public ServicoController(ServicoRepository servicoRepository, UsuarioRepository usuarioRepository) {
                this.servicoRepository = servicoRepository;
                this.usuarioRepository = usuarioRepository;
        }

        @GetMapping
        public ResponseEntity<List<Servico>> listarMeusServicos() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                // Busca TODOS (meus + globais)
                List<Servico> todos = servicoRepository.findByProfissionalOrProfissionalIsNull(profissional);

                java.util.Map<String, Servico> mapa = new java.util.HashMap<>();

                // 1. Adiciona Globais / Admin (Prioridade Baixa) - IGNORA SE ATIVO = FALSE (se
                // tiver essa prop no global) -> Mas globais normalmente sao ativos
                todos.stream()
                                .filter(s -> s.getProfissional() == null
                                                || !s.getProfissional().getId().equals(profissional.getId()))
                                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

                // 2. Adiciona Meus (Prioridade Alta - Sobrescreve)
                // AQUI ESTÁ O TRUQUE: Se o MEU serviço estiver INATIVO (Excluído), ele DEVE
                // SOBRESCREVER o Global e NÃO APARECER na lista final.
                // Então adicionamos ao mapa, e depois filtramos o mapa.
                todos.stream()
                                .filter(s -> s.getProfissional() != null
                                                && s.getProfissional().getId().equals(profissional.getId()))
                                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

                // 3. Remove inativos do mapa
                // Se eu "excluí" (soft delete) o 'Pedicure', ele está no mapa como inativo.
                // Removemos agora.
                mapa.values().removeIf(s -> s.getAtivo() != null && !s.getAtivo());

                // Retorna lista filtrada
                return ResponseEntity.ok(new java.util.ArrayList<>(mapa.values()));
        }

        @PostMapping
        public ResponseEntity<Servico> criarServico(@RequestBody Servico servico) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                servico.setProfissional(profissional);
                if (servico.getAtivo() == null)
                        servico.setAtivo(true);
                return ResponseEntity.ok(servicoRepository.save(servico));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                Servico servico = servicoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

                // CENÁRIO 1: É meu serviço -> Soft Delete
                if (servico.getProfissional() != null
                                && servico.getProfissional().getId().equals(profissional.getId())) {
                        servico.setAtivo(false);
                        servicoRepository.save(servico);
                        return ResponseEntity.noContent().build();
                }

                // CENÁRIO 2: É Global (ex: Pedicure padrão) -> Crio um Shadow Inativo para
                // "esconder"
                if (servico.getProfissional() == null
                                || !servico.getProfissional().getId().equals(profissional.getId())) {
                        Servico shadowInativo = new Servico();
                        shadowInativo.setNome(servico.getNome());
                        shadowInativo.setValor(servico.getValor());
                        shadowInativo.setTempoEstimado(servico.getTempoEstimado());
                        shadowInativo.setProfissional(profissional);
                        shadowInativo.setAtivo(false); // O pulo do gato
                        servicoRepository.save(shadowInativo);
                        return ResponseEntity.noContent().build();
                }

                return ResponseEntity.notFound().build();
        }

        @PutMapping("/{id}")
        public ResponseEntity<Servico> atualizarServico(@PathVariable Long id, @RequestBody Servico servicoAtualizado) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                Servico servico = servicoRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

                boolean isMeuServico = servico.getProfissional() != null
                                && servico.getProfissional().getId().equals(profissional.getId());

                if (!isMeuServico) {
                        // CLONAR como novo serviço para este profissional
                        Servico novoServico = new Servico();
                        novoServico.setNome(servicoAtualizado.getNome());
                        novoServico.setValor(servicoAtualizado.getValor());
                        novoServico.setTempoEstimado(servicoAtualizado.getTempoEstimado());
                        novoServico.setProfissional(profissional);
                        novoServico.setAtivo(true);

                        Servico salvo = servicoRepository.save(novoServico);

                        // LOGICA AVANÇADA: Se o usuário RENOMEOU um serviço Global/Padrão,
                        // ele espera que o ANTIGO suma.
                        String nomeOriginal = servico.getNome().trim();
                        String nomeNovo = servicoAtualizado.getNome().trim();

                        if (!nomeOriginal.equalsIgnoreCase(nomeNovo)) {
                                // Oculta o original criando um sombra inativo
                                Servico shadowInativo = new Servico();
                                shadowInativo.setNome(nomeOriginal);
                                shadowInativo.setValor(servico.getValor());
                                shadowInativo.setTempoEstimado(servico.getTempoEstimado());
                                shadowInativo.setProfissional(profissional);
                                shadowInativo.setAtivo(false); // Oculta
                                servicoRepository.save(shadowInativo);
                        }

                        return ResponseEntity.ok(salvo);
                }

                servico.setNome(servicoAtualizado.getNome());
                servico.setValor(servicoAtualizado.getValor());
                servico.setTempoEstimado(servicoAtualizado.getTempoEstimado());
                if (servicoAtualizado.getAtivo() != null)
                        servico.setAtivo(servicoAtualizado.getAtivo());

                return ResponseEntity.ok(servicoRepository.save(servico));
        }
}