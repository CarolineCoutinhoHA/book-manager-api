# 🧪 TESTES AUTOMATIZADOS - GUIA COMPLETO

## 🎯 PIRÂMIDE DE TESTES

### Estrutura de Testes
```
                    ┌─────────────────┐
                    │   E2E TESTS     │  ← Poucos, caros, lentos
                    │   (Selenium)    │
                    └─────────────────┘
                ┌─────────────────────────┐
                │  INTEGRATION TESTS      │  ← Alguns, médio custo
                │  (Controller + DB)      │
                └─────────────────────────┘
        ┌─────────────────────────────────────────┐
        │           UNIT TESTS                    │  ← Muitos, baratos, rápidos
        │     (Service, Repository, Mapper)       │
        └─────────────────────────────────────────┘
```

### Tipos de Teste no Projeto
- **Unit Tests** - Service, Repository, Mapper (isolados)
- **Integration Tests** - Controller + Service + DB (completo)
- **Slice Tests** - @WebMvcTest, @DataJpaTest (camadas específicas)

---

## 🏗️ CONFIGURAÇÃO DE TESTES

### Dependências (já configuradas)
```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'com.fasterxml.jackson.core:jackson-databind'
```

### Anotações Principais
```java
@SpringBootTest              // Teste de integração completo
@WebMvcTest                 // Teste apenas da camada web
@DataJpaTest                // Teste apenas da camada de dados
@MockBean                   // Mock de beans do Spring
@Autowired                  // Injeção em testes
@Test                       // Marca método como teste
@BeforeEach                 // Executa antes de cada teste
@AfterEach                  // Executa depois de cada teste
@Transactional              // Rollback automático após teste
```

---

## 🧪 TESTES UNITÁRIOS - SERVICE

### Teste de Service com Mocks
```java
@ExtendWith(MockitoExtension.class)  // Habilita Mockito
class AutorServiceTest {

    @Mock
    private AutorRepository autorRepository;  // Mock do repository

    @Mock
    private AutorMapper autorMapper;          // Mock do mapper

    @InjectMocks
    private AutorService autorService;        // Service real com mocks injetados

    private Autor autorMock;
    private AutorRequestDTO requestDTOMock;
    private AutorResponseDTO responseDTOMock;

    @BeforeEach
    void setUp() {
        // Preparar dados de teste
        autorMock = new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
        autorMock.setIdAutor(1L);

        requestDTOMock = new AutorRequestDTO(
            "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );

        responseDTOMock = new AutorResponseDTO(
            1L, "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );
    }

    @Test
    @DisplayName("Deve criar autor com sucesso")
    void deveCriarAutorComSucesso() {
        // Given (Arrange)
        when(autorRepository.findAll()).thenReturn(Collections.emptyList()); // Email único
        when(autorMapper.toEntity(requestDTOMock)).thenReturn(autorMock);
        when(autorRepository.save(autorMock)).thenReturn(autorMock);
        when(autorMapper.toResponseDTO(autorMock)).thenReturn(responseDTOMock);

        // When (Act)
        AutorResponseDTO resultado = autorService.createAutor(requestDTOMock);

        // Then (Assert)
        assertThat(resultado).isNotNull();
        assertThat(resultado.nome()).isEqualTo("João Silva");
        assertThat(resultado.email()).isEqualTo("joao@email.com");

        // Verificar interações
        verify(autorRepository).findAll();
        verify(autorMapper).toEntity(requestDTOMock);
        verify(autorRepository).save(autorMock);
        verify(autorMapper).toResponseDTO(autorMock);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar autor com email duplicado")
    void deveLancarExcecaoAoCriarAutorComEmailDuplicado() {
        // Given
        Autor autorExistente = new Autor("Outro", "joao@email.com", "00000-000", "(00) 00000-0000");
        when(autorRepository.findAll()).thenReturn(List.of(autorExistente));

        // When & Then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> autorService.createAutor(requestDTOMock)
        );

        assertThat(exception.getMessage()).contains("joao@email.com já está em uso");
        
        // Verificar que não tentou salvar
        verify(autorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar autor por ID com sucesso")
    void deveBuscarAutorPorIdComSucesso() {
        // Given
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autorMock));
        when(autorMapper.toResponseDTO(autorMock)).thenReturn(responseDTOMock);

        // When
        AutorResponseDTO resultado = autorService.findAutorById(1L);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.idAutor()).isEqualTo(1L);
        assertThat(resultado.nome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar autor inexistente")
    void deveLancarExcecaoAoBuscarAutorInexistente() {
        // Given
        when(autorRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> autorService.findAutorById(999L)
        );

        assertThat(exception.getMessage()).contains("999 não foi encontrado");
    }

    @Test
    @DisplayName("Deve listar apenas autores disponíveis")
    void deveListarApenasAutoresDisponiveis() {
        // Given
        Autor autorDisponivel = new Autor("Disponível", "disp@email.com", "11111-111", "(11) 11111-1111");
        autorDisponivel.disponivel();

        Autor autorIndisponivel = new Autor("Indisponível", "indisp@email.com", "22222-222", "(22) 22222-2222");
        autorIndisponivel.indisponivel();

        when(autorRepository.findAll()).thenReturn(List.of(autorDisponivel, autorIndisponivel));
        when(autorMapper.toResponseDTO(autorDisponivel)).thenReturn(
            new AutorResponseDTO(1L, "Disponível", "disp@email.com", "11111-111", "(11) 11111-1111")
        );

        // When
        List<AutorResponseDTO> resultado = autorService.findAllAutores();

        // Then
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nome()).isEqualTo("Disponível");
        
        // Verificar que só mapeou o disponível
        verify(autorMapper, times(1)).toResponseDTO(any());
    }
}
```

---

## 🌐 TESTES DE INTEGRAÇÃO - CONTROLLER

### Teste de Controller Completo
```java
@SpringBootTest(classes = DemoApplication.class)  // Contexto completo
@AutoConfigureMockMvc                             // MockMvc configurado
@Transactional                                    // Rollback após cada teste
class AutorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;                      // Simula requisições HTTP

    @Autowired
    private ObjectMapper objectMapper;            // Converte JSON

    @Autowired
    private AutorRepository autorRepository;      // Repository real

    private final String API_URL = "/api/autores";

    @BeforeEach
    void setUp() {
        // Limpar dados antes de cada teste
        autorRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar autor com sucesso via POST")
    void deveCriarAutorComSucessoViaPost() throws Exception {
        // Given
        AutorRequestDTO requestDTO = new AutorRequestDTO(
            "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // When & Then
        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())                    // 201 Created
                .andExpect(jsonPath("$.nome").value("João Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.cep").value("12345-678"))
                .andExpect(jsonPath("$.telefone").value("(11) 99999-9999"))
                .andExpect(jsonPath("$.idAutor").exists())          // ID foi gerado
                .andDo(print());                                    // Debug: imprime request/response

        // Verificar se foi salvo no banco
        List<Autor> autores = autorRepository.findAll();
        assertThat(autores).hasSize(1);
        assertThat(autores.get(0).getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve retornar 400 ao criar autor com dados inválidos")
    void deveRetornar400AoCriarAutorComDadosInvalidos() throws Exception {
        // Given - DTO com dados inválidos
        AutorRequestDTO requestDTO = new AutorRequestDTO(
            "",                    // Nome vazio (inválido)
            "email-inválido",      // Email inválido
            "12345678",           // CEP sem hífen (inválido)
            "telefone-inválido"   // Telefone inválido
        );

        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // When & Then
        mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())                 // 400 Bad Request
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").exists())      // Erros de validação
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.cep").exists())
                .andDo(print());

        // Verificar que não foi salvo no banco
        List<Autor> autores = autorRepository.findAll();
        assertThat(autores).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar autor por ID via GET")
    void deveBuscarAutorPorIdViaGet() throws Exception {
        // Given - Criar autor no banco primeiro
        Autor autor = new Autor("Maria Santos", "maria@email.com", "54321-876", "(21) 88888-8888");
        Autor autorSalvo = autorRepository.save(autor);

        // When & Then
        mockMvc.perform(get(API_URL + "/" + autorSalvo.getIdAutor()))
                .andExpect(status().isOk())                         // 200 OK
                .andExpect(jsonPath("$.idAutor").value(autorSalvo.getIdAutor()))
                .andExpect(jsonPath("$.nome").value("Maria Santos"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andDo(print());
    }

    @Test
    @DisplayName("Deve retornar 404 ao buscar autor inexistente")
    void deveRetornar404AoBuscarAutorInexistente() throws Exception {
        // When & Then
        mockMvc.perform(get(API_URL + "/999"))
                .andExpect(status().isNotFound())                   // 404 Not Found
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("999 não foi encontrado")))
                .andDo(print());
    }

    @Test
    @DisplayName("Deve listar todos os autores via GET")
    void deveListarTodosAutoresViaGet() throws Exception {
        // Given - Criar múltiplos autores
        Autor autor1 = new Autor("Autor 1", "autor1@email.com", "11111-111", "(11) 11111-1111");
        Autor autor2 = new Autor("Autor 2", "autor2@email.com", "22222-222", "(22) 22222-2222");
        
        autorRepository.saveAll(List.of(autor1, autor2));

        // When & Then
        mockMvc.perform(get(API_URL))
                .andExpect(status().isOk())                         // 200 OK
                .andExpect(jsonPath("$").isArray())                 // Retorna array
                .andExpect(jsonPath("$.length()").value(2))         // 2 autores
                .andExpect(jsonPath("$[0].nome").value("Autor 1"))
                .andExpect(jsonPath("$[1].nome").value("Autor 2"))
                .andDo(print());
    }

    @Test
    @DisplayName("Deve atualizar autor via PUT")
    void deveAtualizarAutorViaPut() throws Exception {
        // Given - Criar autor primeiro
        Autor autor = new Autor("Nome Original", "original@email.com", "00000-000", "(00) 00000-0000");
        Autor autorSalvo = autorRepository.save(autor);

        AutorRequestDTO updateDTO = new AutorRequestDTO(
            "Nome Atualizado", "atualizado@email.com", "99999-999", "(99) 99999-9999"
        );

        String jsonRequest = objectMapper.writeValueAsString(updateDTO);

        // When & Then
        mockMvc.perform(put(API_URL + "/" + autorSalvo.getIdAutor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())                         // 200 OK
                .andExpect(jsonPath("$.idAutor").value(autorSalvo.getIdAutor()))
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpected(jsonPath("$.email").value("atualizado@email.com"))
                .andDo(print());

        // Verificar se foi atualizado no banco
        Autor autorAtualizado = autorRepository.findById(autorSalvo.getIdAutor()).orElseThrow();
        assertThat(autorAtualizado.getNome()).isEqualTo("Nome Atualizado");
        assertThat(autorAtualizado.getEmail()).isEqualTo("atualizado@email.com");
    }

    @Test
    @DisplayName("Deve deletar autor via DELETE (soft delete)")
    void deveDeletarAutorViaDelete() throws Exception {
        // Given
        Autor autor = new Autor("Para Deletar", "deletar@email.com", "12345-678", "(11) 99999-9999");
        Autor autorSalvo = autorRepository.save(autor);

        // When & Then
        mockMvc.perform(delete(API_URL + "/" + autorSalvo.getIdAutor()))
                .andExpect(status().isNoContent())                  // 204 No Content
                .andDo(print());

        // Verificar soft delete (ainda existe no banco, mas indisponível)
        Autor autorDeletado = autorRepository.findById(autorSalvo.getIdAutor()).orElseThrow();
        assertThat(autorDeletado.isDisponibilidade()).isFalse();
    }
}
```

---

## 🎯 TESTES DE SLICE - @WebMvcTest

### Teste Apenas da Camada Web
```java
@WebMvcTest(AutorController.class)  // Testa apenas o controller
class AutorControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // Mock do service (não carrega implementação real)
    private AutorService autorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve chamar service corretamente ao criar autor")
    void deveChamarServiceCorretamenteAoCriarAutor() throws Exception {
        // Given
        AutorRequestDTO requestDTO = new AutorRequestDTO(
            "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );

        AutorResponseDTO responseDTO = new AutorResponseDTO(
            1L, "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );

        when(autorService.createAutor(any(AutorRequestDTO.class))).thenReturn(responseDTO);

        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        // When & Then
        mockMvc.perform(post("/api/autores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João Silva"));

        // Verificar que o service foi chamado
        verify(autorService).createAutor(any(AutorRequestDTO.class));
    }
}
```

---

## 🗄️ TESTES DE REPOSITORY - @DataJpaTest

### Teste da Camada de Dados
```java
@DataJpaTest  // Configura apenas JPA (H2 in-memory)
class AutorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;  // Para preparar dados de teste

    @Autowired
    private AutorRepository autorRepository;

    @Test
    @DisplayName("Deve salvar e buscar autor corretamente")
    void deveSalvarEBuscarAutorCorretamente() {
        // Given
        Autor autor = new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");

        // When
        Autor autorSalvo = autorRepository.save(autor);

        // Then
        assertThat(autorSalvo.getIdAutor()).isNotNull();
        assertThat(autorSalvo.getUuid()).isNotNull();  // UUID gerado pelo @PrePersist
        assertThat(autorSalvo.isDisponibilidade()).isTrue();  // Padrão

        // Buscar do banco
        Optional<Autor> autorEncontrado = autorRepository.findById(autorSalvo.getIdAutor());
        assertThat(autorEncontrado).isPresent();
        assertThat(autorEncontrado.get().getNome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve respeitar constraint de email único")
    void deveRespeitarConstraintDeEmailUnico() {
        // Given
        Autor autor1 = new Autor("Autor 1", "mesmo@email.com", "11111-111", "(11) 11111-1111");
        Autor autor2 = new Autor("Autor 2", "mesmo@email.com", "22222-222", "(22) 22222-2222");

        // When
        autorRepository.save(autor1);

        // Then
        assertThrows(DataIntegrityViolationException.class, () -> {
            autorRepository.save(autor2);
            entityManager.flush();  // Força execução do SQL
        });
    }

    @Test
    @DisplayName("Deve buscar autor por email")
    void deveBuscarAutorPorEmail() {
        // Given
        Autor autor = new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
        entityManager.persistAndFlush(autor);

        // When
        Optional<Autor> resultado = autorRepository.findByEmail("joao@email.com");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("João Silva");
    }
}
```

---

## 🔧 TESTES DE MAPPER

### Teste de MapStruct
```java
@SpringBootTest  // Necessário para carregar o mapper como Bean
class AutorMapperTest {

    @Autowired
    private AutorMapper autorMapper;

    @Test
    @DisplayName("Deve mapear Entity para ResponseDTO corretamente")
    void deveMapeareEntityParaResponseDTOCorretamente() {
        // Given
        Autor autor = new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
        autor.setIdAutor(1L);

        // When
        AutorResponseDTO dto = autorMapper.toResponseDTO(autor);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.idAutor()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("João Silva");
        assertThat(dto.email()).isEqualTo("joao@email.com");
        assertThat(dto.cep()).isEqualTo("12345-678");
        assertThat(dto.telefone()).isEqualTo("(11) 99999-9999");
    }

    @Test
    @DisplayName("Deve mapear RequestDTO para Entity corretamente")
    void deveMapeareRequestDTOParaEntityCorretamente() {
        // Given
        AutorRequestDTO dto = new AutorRequestDTO(
            "Maria Santos", "maria@email.com", "54321-876", "(21) 88888-8888"
        );

        // When
        Autor autor = autorMapper.toEntity(dto);

        // Then
        assertThat(autor).isNotNull();
        assertThat(autor.getNome()).isEqualTo("Maria Santos");
        assertThat(autor.getEmail()).isEqualTo("maria@email.com");
        assertThat(autor.getCep()).isEqualTo("54321-876");
        assertThat(autor.getTelefone()).isEqualTo("(21) 88888-8888");
        
        // Campos que não devem ser mapeados
        assertThat(autor.getIdAutor()).isNull();
        assertThat(autor.getUuid()).isNull();
        assertThat(autor.isDisponibilidade()).isTrue();  // Valor padrão
    }

    @Test
    @DisplayName("Deve atualizar Entity existente com dados do DTO")
    void deveAtualizarEntityExistenteComDadosDoDTO() {
        // Given
        Autor autorExistente = new Autor("Nome Antigo", "antigo@email.com", "00000-000", "(00) 00000-0000");
        autorExistente.setIdAutor(1L);

        AutorRequestDTO dto = new AutorRequestDTO(
            "Nome Novo", "novo@email.com", "11111-111", "(11) 11111-1111"
        );

        // When
        autorMapper.updateEntityFromDto(dto, autorExistente);

        // Then
        assertThat(autorExistente.getIdAutor()).isEqualTo(1L);  // ID preservado
        assertThat(autorExistente.getNome()).isEqualTo("Nome Novo");
        assertThat(autorExistente.getEmail()).isEqualTo("novo@email.com");
        assertThat(autorExistente.getCep()).isEqualTo("11111-111");
        assertThat(autorExistente.getTelefone()).isEqualTo("(11) 11111-1111");
    }
}
```

---

## 🚀 EXECUTANDO TESTES

### Comandos Gradle
```bash
# Executar todos os testes
./gradlew test

# Executar testes específicos
./gradlew test --tests "AutorServiceTest"
./gradlew test --tests "*.controller.*"
./gradlew test --tests "*IntegrationTest"

# Executar com relatório detalhado
./gradlew test --info

# Executar testes em paralelo (mais rápido)
./gradlew test --parallel
```

### Relatórios de Teste
```
# Relatório HTML
build/reports/tests/test/index.html

# Relatório XML (para CI/CD)
build/test-results/test/

# Logs detalhados
build/reports/tests/test/classes/
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Estrutura de Teste (AAA Pattern)
```java
@Test
void nomeDescritivo() {
    // ARRANGE (Given) - Preparar dados
    Autor autor = new Autor("João", "joao@email.com", "12345-678", "(11) 99999-9999");
    
    // ACT (When) - Executar ação
    AutorResponseDTO resultado = autorService.createAutor(requestDTO);
    
    // ASSERT (Then) - Verificar resultado
    assertThat(resultado.nome()).isEqualTo("João");
}
```

### 2. Nomes de Teste Descritivos
```java
// ❌ Ruim
@Test
void test1() {}

// ✅ Bom
@Test
@DisplayName("Deve criar autor com sucesso quando dados são válidos")
void deveCriarAutorComSucessoQuandoDadosSaoValidos() {}
```

### 3. Dados de Teste Consistentes
```java
class AutorTestData {
    public static AutorRequestDTO createValidAutorRequest() {
        return new AutorRequestDTO(
            "João Silva", "joao@email.com", "12345-678", "(11) 99999-9999"
        );
    }
    
    public static Autor createValidAutor() {
        return new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
    }
}
```

---

## 🎯 CHECKLIST TESTES

### Cobertura Básica
- [ ] Testes unitários de Service (regras de negócio)
- [ ] Testes de integração de Controller (endpoints)
- [ ] Testes de Repository (queries customizadas)
- [ ] Testes de Mapper (conversões)

### Cenários Testados
- [ ] Casos de sucesso (happy path)
- [ ] Casos de erro (validações, exceções)
- [ ] Casos limite (dados nulos, vazios)
- [ ] Regras de negócio específicas

### Qualidade
- [ ] Nomes descritivos (@DisplayName)
- [ ] Estrutura AAA (Arrange, Act, Assert)
- [ ] Dados de teste isolados
- [ ] Mocks apropriados (não over-mocking)
- [ ] Verificações completas (verify, assertThat)

### Performance
- [ ] Testes rápidos (< 1s cada)
- [ ] @Transactional para rollback
- [ ] Dados mínimos necessários
- [ ] Slice tests quando possível