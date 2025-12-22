package com.space.nails.controller;

import com.space.nails.dto.PublicAgendamentoDTO;
import com.space.nails.model.*;
import com.space.nails.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap; // Novo
import java.util.List;
import java.util.Map; // Novo

@RestController
@RequestMapping("/api/public")
public class PublicAgendamentoController {

    private final ServicoRepository servicoRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final AgendaConfigRepository agendaConfigRepository;

    public PublicAgendamentoController(ServicoRepository servicoRepository,
            AgendamentoRepository agendamentoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoRepository notificacaoRepository,
            AgendaConfigRepository agendaConfigRepository) {
        this.servicoRepository = servicoRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.agendaConfigRepository = agendaConfigRepository;
    }

    @GetMapping("/profissional/slug/{slug}")
    public ResponseEntity<Usuario> getProfissionalBySlug(@PathVariable String slug) {
        // 1. Tenta buscar pelo CÓDIGO DE CONVITE (Exato)
        java.util.Optional<Usuario> pPorCodigo = usuarioRepository.findByCodigoConvite(slug.toUpperCase());
        if (pPorCodigo.isPresent()) {
            return ResponseEntity.ok(pPorCodigo.get());
        }

        // REMOVIDO: Busca por nome (Legacy)
        // Para garantir sigilo, não permitimos mais buscar por nome/slug previsível.

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/profissional/{id}")
    public ResponseEntity<String> getNomeProfissional(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(Usuario::getNome)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profissional/{id}/info")
    public ResponseEntity<Map<String, String>> getDetalhesProfissional(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    Map<String, String> dados = new HashMap<>();
                    dados.put("nome", u.getNome());
                    dados.put("telefone", u.getTelefone());
                    dados.put("endereco", u.getEndereco()); // Retorna o endereço agora
                    dados.put("localizacaoUrl", u.getLocalizacaoUrl()); // Link do Maps
                    return ResponseEntity.ok(dados);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/servicos")
    public ResponseEntity<List<Servico>> listarServicos(@RequestParam Long profissionalId) {
        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // Busca TODOS (meus + globais)
        List<Servico> todos = servicoRepository.findByProfissionalOrProfissionalIsNull(profissional);

        // Lógica de Shadowing (Igual ao Admin): Preferir Serviços do Profissional se
        // houver conflito de nome
        java.util.Map<String, Servico> mapa = new java.util.HashMap<>();

        // 1. Prioridade Baixa: Globais/Admin
        todos.stream()
                .filter(s -> s.getProfissional() == null || !s.getProfissional().getId().equals(profissional.getId()))
                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

        // 2. Prioridade Alta: Meus (Sobrescreve)
        todos.stream()
                .filter(s -> s.getProfissional() != null && s.getProfissional().getId().equals(profissional.getId()))
                .forEach(s -> mapa.put(s.getNome().trim().toLowerCase(), s));

        return ResponseEntity.ok(new ArrayList<>(mapa.values()));
    }

    @GetMapping("/slots")
    public ResponseEntity<List<LocalTime>> listarSlots(
            @RequestParam Long servicoId,
            @RequestParam String data,
            @RequestParam Long profissionalId) {

        LocalDate dataConsulta = LocalDate.parse(data);

        if (dataConsulta.isBefore(LocalDate.now())) {
            throw new RuntimeException("Não é permitido agendar em datas passadas.");
        }

        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // BUSCA CONFIGURAÇÃO DE AGENDA PARA O DIA DA SEMANA
        // 1 = Segunda, 7 = Domingo (ISO-8601 define Monday=1)
        // AgendaConfig usa 1..7?
        // LocalDate.getDayOfWeek().getValue() retorna 1 (Mon) a 7 (Sun).
        int diaSemana = dataConsulta.getDayOfWeek().getValue();

        // Padrão (Fallback): 08:00 - 18:00 sem intervalo
        LocalTime horaInicio = LocalTime.of(8, 0);
        LocalTime horaFim = LocalTime.of(18, 0);
        LocalTime pausaInicio = null;
        LocalTime pausaFim = null;
        boolean diaAtivo = true;

        java.util.Optional<com.space.nails.model.AgendaConfig> agendaOpt = agendaConfigRepository
                .findByProfissionalAndDiaSemana(profissional, diaSemana);
        if (agendaOpt.isPresent()) {
            com.space.nails.model.AgendaConfig config = agendaOpt.get();
            if (!config.isAtivo()) {
                diaAtivo = false; // Loja fechada neste dia
            } else {
                horaInicio = config.getInicioTrabalho() != null ? config.getInicioTrabalho() : horaInicio;
                horaFim = config.getFimTrabalho() != null ? config.getFimTrabalho() : horaFim;
                pausaInicio = config.getInicioIntervalo();
                pausaFim = config.getFimIntervalo();
            }
        }

        List<LocalTime> slotsDisponiveis = new ArrayList<>();

        if (!diaAtivo) {
            return ResponseEntity.ok(slotsDisponiveis); // Retorna vazio (Fechado)
        }

        LocalTime inicio = horaInicio;

        // Loop usando os horários dinâmicos
        while (inicio.isBefore(horaFim)) {

            // Verifica se cai no intervalo de almoço
            boolean noAlmoco = false;
            if (pausaInicio != null && pausaFim != null) {
                // Se o slot começa DENTRO do intervalo OU termina DENTRO (simplificado: Se
                // começar >= pausaInicio e < pausaFim)
                if ((inicio.equals(pausaInicio) || inicio.isAfter(pausaInicio)) && inicio.isBefore(pausaFim)) {
                    noAlmoco = true;
                }
            }

            if (!noAlmoco) {
                LocalDateTime dataHoraSlot = LocalDateTime.of(dataConsulta, inicio);
                boolean isSlotPassado = dataConsulta.isEqual(LocalDate.now()) && inicio.isBefore(LocalTime.now());

                if (!isSlotPassado && !agendamentoRepository.existeConflitoHorario(profissional, dataHoraSlot)) {
                    slotsDisponiveis.add(inicio);
                }
            }

            int tempo = (servico.getTempoEstimado() != null && servico.getTempoEstimado() > 0)
                    ? servico.getTempoEstimado()
                    : 60;
            inicio = inicio.plusMinutes(tempo);
        }

        return ResponseEntity.ok(slotsDisponiveis);
    }

    @PostMapping("/agendar")
    public ResponseEntity<Agendamento> agendar(@RequestBody PublicAgendamentoDTO dto) {
        // Validação de data no backend (Data e Hora)
        if (dto.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é permitido agendar em horários passados.");
        }

        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        Usuario profissional = servico.getProfissional();

        // 1. Busca cliente por telefone
        // LOGICA CORRIGIDA: Se existe, mantém o nome original. Se não, cria novo.
        Cliente cliente = clienteRepository.findByTelefone(dto.getTelefoneCliente())
                .orElseGet(() -> {
                    Cliente novo = new Cliente();
                    novo.setNome(dto.getNomeCliente()); // Só define nome se for NOVO
                    novo.setTelefone(dto.getTelefoneCliente());
                    novo.setDataCadastro(LocalDate.now());
                    novo.setProfissional(profissional);
                    novo.setObservacoes("Cliente cadastrado via Auto-Agendamento");
                    return clienteRepository.save(novo);
                });

        // Se o cliente já existia, NÃO atualizamos o nome dele.
        // Preserva o histórico.

        // 2. Verifica conflito novamente (segurança)
        if (agendamentoRepository.existeConflitoHorario(profissional, dto.getDataHora())) {
            throw new RuntimeException("Horário não está mais disponível.");
        }

        // 3. Cria agendamento
        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setObservacoes("Agendamento Online");

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // CRIA NOTIFICAÇÃO
        try {
            // Remove notificações anteriores deste mesmo agendamento (Deduping)
            notificacaoRepository.deleteByAgendamento(salvo);

            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");
            String dataFormatada = dto.getDataHora().format(dtf);

            Notificacao notif = new Notificacao(
                    null,
                    profissional,
                    "Novo agendamento: " + cliente.getNome() + " - " + servico.getNome() + " - " + dataFormatada,
                    null,
                    LocalDateTime.now(),
                    false,
                    "success",
                    salvo); // Link com Agendamento
            notificacaoRepository.save(notif);
        } catch (Exception e) {
            System.err.println("Erro ao salvar notificação: " + e.getMessage());
        }

        return ResponseEntity.ok(salvo);
    }

    @GetMapping("/agendamento/{codigo}")
    public ResponseEntity<Agendamento> buscarPorCodigo(@PathVariable String codigo) {
        return agendamentoRepository.findByCodigo(codigo.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/agendamento/{codigo}/remarcar")
    public ResponseEntity<Agendamento> remarcar(@PathVariable String codigo, @RequestBody Map<String, String> payload) {
        Agendamento agendamento = agendamentoRepository.findByCodigo(codigo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        if (!payload.containsKey("novaDataHora")) {
            throw new RuntimeException("Nova data e hora é obrigatória.");
        }

        LocalDateTime novaDataHora = LocalDateTime.parse(payload.get("novaDataHora"));

        if (novaDataHora.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é permitido agendar em horários passados.");
        }

        // Valida conflito
        if (agendamentoRepository.existeConflitoHorario(agendamento.getProfissional(), novaDataHora)) {
            throw new RuntimeException("Novo horário indisponível.");
        }

        agendamento.setDataHora(novaDataHora);
        agendamento.setStatus(StatusAgendamento.PENDENTE); // Volta para pendente se estava cancelado? Ou mantém?
                                                           // Geralmente volta pra pendente.

        // Resetar notificações?? Talvez sim
        agendamento.setLembrete24hEnviado(false);
        agendamento.setLembrete2hEnviado(false);
        agendamento.setLembrete30minEnviado(false);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // Formata data bonita
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");
        String dataFormatada = novaDataHora.format(dtf);

        // CRIA NOTIFICAÇÃO DE REMARCAÇÃO
        try {
            // Remove notificações anteriores deste mesmo agendamento (Deduping)
            notificacaoRepository.deleteByAgendamento(salvo);

            Notificacao notif = new Notificacao(
                    null,
                    agendamento.getProfissional(),
                    "Agendamento Remarcado: " + agendamento.getCliente().getNome() + " para " + dataFormatada,
                    null,
                    LocalDateTime.now(),
                    false,
                    "warning",
                    salvo); // Link com Agendamento
            notificacaoRepository.save(notif);
        } catch (Exception e) {
            System.err.println("Erro ao salvar notificação: " + e.getMessage());
        }

        return ResponseEntity.ok(salvo);
    }
}
