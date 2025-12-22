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

        // Lógica de Shadowing: Se eu tenho um serviço com mesmo nome do Global, mostre
        // o MEU.
        // Mapa: Nome -> Serviço.
        // Ordem: Processar Globais primeiro, depois Meus (sobrescrevendo).
        java.util.Map<String, Servico> mapa = new java.util.HashMap<>();

        // 1. Adiciona Globais / Admin (Prioridade Baixa)
        todos.stream()
                .filter(s -> s.getProfissional() == null || !s.getProfissional().getId().equals(profissional.getId()))
                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

        // 2. Adiciona Meus (Prioridade Alta - Sobrescreve)
        todos.stream()
                .filter(s -> s.getProfissional() != null && s.getProfissional().getId().equals(profissional.getId()))
                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

        // Retorna lista filtrada
        return ResponseEntity.ok(new java.util.ArrayList<>(mapa.values()));
    }

    @PostMapping
    public ResponseEntity<Servico> criarServico(@RequestBody Servico servico) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        servico.setProfissional(profissional);
        return ResponseEntity.ok(servicoRepository.save(servico));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarServico(@PathVariable Long id) {
        servicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizarServico(@PathVariable Long id, @RequestBody Servico servicoAtualizado) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario profissional = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        // Validação de Permissão e Lógica de "Edição" de Globais
        // Se for serviço GLOBAL (profissional == null), o usuário não pode editar,
        // mas pode criar uma CÓPIA personalizada para ele (Shadowing).

        // Lógica de "Edição" (Clonagem) de Serviços Globais ou de Outros (Templates)
        // Se o serviço NÃO pertence ao usuário logado, nós CLONAMOS.
        // Isso cobre: Globais (null), Admin (templates) ou qualquer outro.
        boolean isMeuServico = servico.getProfissional() != null
                && servico.getProfissional().getId().equals(profissional.getId());

        if (!isMeuServico) {
            // CLONAR como novo serviço para este profissional
            Servico novoServico = new Servico();
            novoServico.setNome(servicoAtualizado.getNome());
            novoServico.setValor(servicoAtualizado.getValor());
            novoServico.setProfissional(profissional);
            // Salva como novo registro, tornando-se "meu"
            return ResponseEntity.ok(servicoRepository.save(novoServico));
        }

        // Se cheguei aqui, É MEU SERVIÇO. Posso editar direto.
        // (A verificação de Admin removida pois Admin também cairia no !isMeuServico se
        // tentar editar de outro,
        // mas se o Admin quiser editar o PROPRIO GLOBAL?
        // Se Admin logado editar serviço do Admin, isMeuServico = true. Ele edita o
        // global. Correto.)

        servico.setNome(servicoAtualizado.getNome());
        servico.setValor(servicoAtualizado.getValor());

        return ResponseEntity.ok(servicoRepository.save(servico));
    }
}