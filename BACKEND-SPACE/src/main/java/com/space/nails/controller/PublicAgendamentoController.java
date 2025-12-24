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
        try {
            // 1. Tenta buscar pelo CÓDIGO DE CONVITE (Exato)
            java.util.Optional<Usuario> pPorCodigo = usuarioRepository.findByCodigoConvite(slug.toUpperCase());
            if (pPorCodigo.isPresent()) {
                return ResponseEntity.ok(pPorCodigo.get());
            }

            // REMOVIDO: Busca por nome (Legacy)
            // Para garantir sigilo, não permitimos mais buscar por nome/slug previsível.

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
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

        // 3. REMOVE INATIVOS (Soft Delete)
        mapa.values().removeIf(s -> s.getAtivo() != null && !s.getAtivo());

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

        Servico servicoSelecioando = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        int duracaoMinutos = (servicoSelecioando.getTempoEstimado() != null
                && servicoSelecioando.getTempoEstimado() > 0)
                        ? servicoSelecioando.getTempoEstimado()
                        : 60;

        Usuario profissional = usuarioRepository.findById(profissionalId)
                .orElseThrow(() -> new RuntimeException("Profissional não encontrado"));

        // BUSCA AGENDAMENTOS DO DIA PARA VERIFICAR INTERVALOS
        LocalDateTime inicioDia = dataConsulta.atStartOfDay();
        LocalDateTime fimDia = dataConsulta.atTime(23, 59, 59);
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findByProfissionalAndData(profissional, inicioDia,
                fimDia);

        // BUSCA CONFIGURAÇÃO DE AGENDA
        int diaSemana = dataConsulta.getDayOfWeek().getValue(); // 1=Mon, 7=Sun

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
                diaAtivo = false;
            } else {
                horaInicio = config.getInicioTrabalho() != null ? config.getInicioTrabalho() : horaInicio;
                horaFim = config.getFimTrabalho() != null ? config.getFimTrabalho() : horaFim;
                pausaInicio = config.getInicioIntervalo();
                pausaFim = config.getFimIntervalo();
            }
        }

        List<LocalTime> slotsDisponiveis = new ArrayList<>();
        if (!diaAtivo) {
            return ResponseEntity.ok(slotsDisponiveis);
        }

        LocalTime cursor = horaInicio;

        // Loop: Avança de 30 em 30 min (granularidade padrão de visualização)
        // Mas verifica se o BLOCO Inteiro (cursor -> cursor + duracao) cabe
        while (cursor.plusMinutes(duracaoMinutos).isBefore(horaFim)
                || cursor.plusMinutes(duracaoMinutos).equals(horaFim)) {

            LocalTime terminoSlot = cursor.plusMinutes(duracaoMinutos);
            boolean conflito = false;

            // 1. Verifica Pausa (Almoço)
            // Se o intervalo do serviço [cursor, terminoSlot] encosta ou entra no almoço
            // [pausaInicio, pausaFim]
            if (pausaInicio != null && pausaFim != null) {
                // Sobreposição de intervalos: Max(StartA, StartB) < Min(EndA, EndB)
                // Almoço: A, Serviço: B
                // if (cursor < pausaFim && terminoSlot > pausaInicio)
                if (cursor.isBefore(pausaFim) && terminoSlot.isAfter(pausaInicio)) {
                    conflito = true;
                }
            }

            // 2. Verifica Conflito com Agendamentos Existentes
            if (!conflito) {
                LocalDateTime slotInicio = LocalDateTime.of(dataConsulta, cursor);
                LocalDateTime slotFim = LocalDateTime.of(dataConsulta, terminoSlot);

                // Slot passado?
                if (dataConsulta.isEqual(LocalDate.now()) && cursor.isBefore(LocalTime.now())) {
                    conflito = true;
                } else {
                    for (Agendamento a : agendamentosDoDia) {
                        if (a.getStatus() == StatusAgendamento.CANCELADO)
                            continue; // Ignora cancelados

                        // Calcula Fim do Agendamento Existente
                        int duracaoA = (a.getServico().getTempoEstimado() != null
                                && a.getServico().getTempoEstimado() > 0)
                                        ? a.getServico().getTempoEstimado()
                                        : 60;
                        LocalDateTime aInicio = a.getDataHora();
                        LocalDateTime aFim = aInicio.plusMinutes(duracaoA);

                        // Lógica de Interseção: (StartA < EndB) and (EndA > StartB)
                        if (aInicio.isBefore(slotFim) && aFim.isAfter(slotInicio)) {
                            conflito = true;
                            break;
                        }
                    }
                }
            }

            if (!conflito) {
                slotsDisponiveis.add(cursor);
            }

            // Próximo slot: avança 30 min (padrão de grade) ou flexível?
            // Geralmente sistemas usam grade fixa (ex: 30min).
            cursor = cursor.plusMinutes(30);
        }

        return ResponseEntity.ok(slotsDisponiveis);
    }

    @PostMapping("/agendar")
    public ResponseEntity<Agendamento> agendar(@RequestBody PublicAgendamentoDTO dto) {
        if (dto.getDataHora().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Não é permitido agendar em horários passados.");
        }

        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        Usuario profissional = servico.getProfissional();

        // VALIDAÇÃO DE CONFLITO POR INTERVALO
        int duracao = (servico.getTempoEstimado() != null && servico.getTempoEstimado() > 0)
                ? servico.getTempoEstimado()
                : 60;
        LocalDateTime novoInicio = dto.getDataHora();
        LocalDateTime novoFim = novoInicio.plusMinutes(duracao);

        LocalDateTime inicioDia = novoInicio.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = novoInicio.toLocalDate().atTime(23, 59, 59);
        List<Agendamento> agendamentosDoDia = agendamentoRepository.findByProfissionalAndData(profissional, inicioDia,
                fimDia);

        for (Agendamento a : agendamentosDoDia) {
            if (a.getStatus() == StatusAgendamento.CANCELADO)
                continue;

            int duraA = (a.getServico().getTempoEstimado() != null) ? a.getServico().getTempoEstimado() : 60;
            LocalDateTime aInicio = a.getDataHora();
            LocalDateTime aFim = aInicio.plusMinutes(duraA);

            if (aInicio.isBefore(novoFim) && aFim.isAfter(novoInicio)) {
                throw new RuntimeException("Horário indisponível (conflito de horário).");
            }
        }

        // 1. Busca/Cria Cliente
        Cliente cliente = clienteRepository.findByTelefone(dto.getTelefoneCliente())
                .orElseGet(() -> {
                    Cliente novo = new Cliente();
                    novo.setNome(dto.getNomeCliente());
                    novo.setTelefone(dto.getTelefoneCliente());
                    novo.setDataCadastro(LocalDate.now());
                    novo.setProfissional(profissional);
                    novo.setObservacoes("Cadastrado via Auto-Agendamento");
                    return clienteRepository.save(novo);
                });

        Agendamento agendamento = new Agendamento();
        agendamento.setCliente(cliente);
        agendamento.setProfissional(profissional);
        agendamento.setServico(servico);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setObservacoes("Agendamento Online");

        Agendamento salvo = agendamentoRepository.save(agendamento);
        this.criarNotificacao(salvo, "Novo agendamento", "success");

        return ResponseEntity.ok(salvo);
    }

    // Método auxiliar para notificação (reduz duplicidade)
    private void criarNotificacao(Agendamento agendamento, String prefixo, String tipo) {
        try {
            notificacaoRepository.deleteByAgendamento(agendamento);
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");
            String dataF = agendamento.getDataHora().format(dtf);
            String msg = prefixo + ": " + agendamento.getCliente().getNome() + " - "
                    + agendamento.getServico().getNome() + " - " + dataF;

            Notificacao notif = new Notificacao(null, agendamento.getProfissional(), msg, null, LocalDateTime.now(),
                    false, tipo, agendamento);
            notificacaoRepository.save(notif);
        } catch (Exception e) {
            System.err.println("Erro notif: " + e.getMessage());
        }
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

        // Valida conflito (ignorando o próprio agendamento atual se for a mesma data,
        // mas aqui mudou data/hora)
        // Precisamos checar conflito com OUTROS agendamentos
        int duracao = (agendamento.getServico().getTempoEstimado() != null)
                ? agendamento.getServico().getTempoEstimado()
                : 60;
        LocalDateTime novoFim = novaDataHora.plusMinutes(duracao);

        LocalDateTime inicioDia = novaDataHora.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = novaDataHora.toLocalDate().atTime(23, 59, 59);
        List<Agendamento> agendamentosDoDia = agendamentoRepository
                .findByProfissionalAndData(agendamento.getProfissional(), inicioDia, fimDia);

        for (Agendamento a : agendamentosDoDia) {
            if (a.getId().equals(agendamento.getId()))
                continue; // Ignora a versão antiga dele mesmo no banco
            if (a.getStatus() == StatusAgendamento.CANCELADO)
                continue;

            int duraA = (a.getServico().getTempoEstimado() != null) ? a.getServico().getTempoEstimado() : 60;
            LocalDateTime aInicio = a.getDataHora();
            LocalDateTime aFim = aInicio.plusMinutes(duraA);

            if (aInicio.isBefore(novoFim) && aFim.isAfter(novaDataHora)) {
                throw new RuntimeException("Novo horário indisponível.");
            }
        }

        agendamento.setDataHora(novaDataHora);
        agendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamento.setLembrete24hEnviado(false);
        agendamento.setLembrete2hEnviado(false);
        agendamento.setLembrete30minEnviado(false);

        Agendamento salvo = agendamentoRepository.save(agendamento);
        this.criarNotificacao(salvo, "Agendamento Remarcado", "warning");

        return ResponseEntity.ok(salvo);
    }
}
