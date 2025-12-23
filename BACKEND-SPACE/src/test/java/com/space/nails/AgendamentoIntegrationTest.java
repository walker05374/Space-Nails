package com.space.nails;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.space.nails.dto.PublicAgendamentoDTO;
import com.space.nails.dto.RegisterRequestDTO;
import com.space.nails.dto.UsuarioDTO;
import com.space.nails.model.Servico;
import com.space.nails.model.Usuario;
import com.space.nails.repository.ServicoRepository;
import com.space.nails.repository.UsuarioRepository;
import com.space.nails.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
public class AgendamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario profissional;
    private Servico servico;

    @BeforeEach
    void setup() {
        // Limpa dados de execuções anteriores (sujas)
        usuarioRepository.findByEmail("prof.teste@email.com").ifPresent(u -> {
            try {
                usuarioService.excluir(u.getId());
            } catch (Exception e) {
                // Ignore se falhar
            }
        });

        if (usuarioRepository.findByEmail("prof.teste@email.com").isPresent()) {
            // Caso a exclusão falhe ou algo estranho ocorra, tenta recuperar
            profissional = usuarioRepository.findByEmail("prof.teste@email.com").get();
        } else {
            RegisterRequestDTO dto = new RegisterRequestDTO();
            dto.setNome("Ana Teste");
            dto.setEmail("prof.teste@email.com");
            dto.setPassword("123456");
            dto.setTelefone("551100000000");
            UsuarioDTO criado = usuarioService.criarProfissional(dto);
            profissional = usuarioRepository.findById(criado.getId()).get();
        }

        // Garante serviço
        servico = servicoRepository.findByProfissional(profissional).stream().findFirst().orElseGet(() -> {
            Servico s = new Servico();
            s.setNome("Unha Teste");
            s.setValor(50.0);
            s.setTempoEstimado(30);
            s.setProfissional(profissional);
            return servicoRepository.save(s);
        });
    }

    @Test
    void deveBuscarProfissionalPeloCodigoConvite() throws Exception {
        String codigo = profissional.getCodigoConvite();

        mockMvc.perform(get("/api/public/profissional/slug/" + codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ana Teste"))
                .andExpect(jsonPath("$.id").value(profissional.getId()));
    }

    @Test
    void deveCriarAgendamentoComSucesso() throws Exception {
        PublicAgendamentoDTO agendamentoDTO = new PublicAgendamentoDTO();
        agendamentoDTO.setServicoId(servico.getId());
        // Agenda para amanhã
        agendamentoDTO.setDataHora(
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).truncatedTo(ChronoUnit.MINUTES));
        agendamentoDTO.setNomeCliente("Cliente Teste Auto");
        agendamentoDTO.setTelefoneCliente("5511999999999");

        mockMvc.perform(post("/api/public/agendar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(agendamentoDTO)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.codigo").exists());
    }
}
