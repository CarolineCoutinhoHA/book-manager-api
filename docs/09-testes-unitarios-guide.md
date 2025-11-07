# Testes Unitários - Guia Completo

## Visão Geral

Testes unitários verificam o comportamento de componentes individuais (Services, Controllers, Repositories) de forma isolada, usando mocks para dependências.

## Configuração

### Dependências Maven
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

Inclui automaticamente:
- JUnit 5
- Mockito
- AssertJ
- Hamcrest
- Spring Test

## Estrutura de Testes

### Localização
- **Diretório**: `src/test/java`
- **Pacotes**: Espelham a estrutura de `src/main/java`
- **Nomenclatura**: `ClasseTesteTest.java`

### Exemplo: AutorServiceTest
```java
@ExtendWith(MockitoExtension.class)
class AutorServiceTest {
    
    @Mock
    private AutorRepository autorRepository;
    
    @Mock
    private AutorMapper autorMapper;
    
    @InjectMocks
    private AutorService autorService;
    
    @Test
    void criarAutor_DeveRetornarAutorResponseDTO() {
        // Given (Arrange)
        AutorRequestDTO requestDTO = new AutorRequestDTO("João Silva", "joao@email.com");
        Autor autor = new Autor("João Silva", "joao@email.com");
        AutorResponseDTO expectedResponse = new AutorResponseDTO(UUID.randomUUID(), "João Silva", "joao@email.com");
        
        when(autorMapper.toEntity(requestDTO)).thenReturn(autor);
        when(autorRepository.save(autor)).thenReturn(autor);
        when(autorMapper.toResponseDTO(autor)).thenReturn(expectedResponse);
        
        // When (Act)
        AutorResponseDTO result = autorService.criarAutor(requestDTO);
        
        // Then (Assert)
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("João Silva");
        verify(autorRepository).save(autor);
    }
}
```

## Anotações Principais

### @ExtendWith(MockitoExtension.class)
- Integra Mockito com JUnit 5
- Inicializa mocks automaticamente

### @Mock
- Cria mock de dependências
- Comportamento definido com `when().thenReturn()`

### @InjectMocks
- Injeta mocks na classe testada
- Cria instância real da classe

### @Test
- Marca método como teste
- Executado pelo JUnit

## Padrões de Teste

### AAA Pattern (Arrange, Act, Assert)
```java
@Test
void exemploAAA() {
    // Arrange (Given) - Preparação
    String entrada = "dados de teste";
    String esperado = "resultado esperado";
    when(mock.metodo(entrada)).thenReturn(esperado);
    
    // Act (When) - Execução
    String resultado = service.executar(entrada);
    
    // Assert (Then) - Verificação
    assertThat(resultado).isEqualTo(esperado);
}
```

### Given-When-Then (BDD Style)
```java
@Test
void buscarAutor_QuandoExiste_DeveRetornarAutor() {
    // Given
    UUID uuid = UUID.randomUUID();
    Autor autor = new Autor("Nome", "email@test.com");
    when(autorRepository.findByUuidAndDisponibilidadeTrue(uuid))
        .thenReturn(Optional.of(autor));
    
    // When
    AutorResponseDTO resultado = autorService.buscarPorUuid(uuid);
    
    // Then
    assertThat(resultado).isNotNull();
    assertThat(resultado.nome()).isEqualTo("Nome");
}
```

## Tipos de Teste

### 1. Testes de Service (Lógica de Negócio)
```java
@ExtendWith(MockitoExtension.class)
class AutorServiceTest {
    
    @Mock private AutorRepository repository;
    @Mock private AutorMapper mapper;
    @InjectMocks private AutorService service;
    
    @Test
    void criarAutor_ComDadosValidos_DeveSalvarERetornar() {
        // Testa regras de negócio
        // Verifica interações com dependências
        // Valida transformações de dados
    }
    
    @Test
    void buscarAutor_QuandoNaoExiste_DeveLancarExcecao() {
        // Testa cenários de erro
        // Verifica exceções lançadas
    }
}
```

### 2. Testes de Controller (Camada Web)
```java
@WebMvcTest(AutorController.class)
class AutorControllerTest {
    
    @Autowired private MockMvc mockMvc;
    @MockBean private AutorService autorService;
    
    @Test
    void criarAutor_ComDadosValidos_DeveRetornar201() throws Exception {
        // Given
        AutorRequestDTO request = new AutorRequestDTO("Nome", "email@test.com");
        AutorResponseDTO response = new AutorResponseDTO(UUID.randomUUID(), "Nome", "email@test.com");
        
        when(autorService.criarAutor(any(AutorRequestDTO.class)))
            .thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/api/autores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Nome"));
    }
}
```

### 3. Testes de Repository (Camada de Dados)
```java
@DataJpaTest
class AutorRepositoryTest {
    
    @Autowired private TestEntityManager entityManager;
    @Autowired private AutorRepository autorRepository;
    
    @Test
    void findByUuidAndDisponibilidadeTrue_QuandoExiste_DeveRetornar() {
        // Given
        Autor autor = new Autor("Nome", "email@test.com");
        entityManager.persistAndFlush(autor);
        
        // When
        Optional<Autor> resultado = autorRepository
            .findByUuidAndDisponibilidadeTrue(autor.getUuid());
        
        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Nome");
    }
}
```

## Mockito - Principais Métodos

### Configuração de Comportamento
```java
// Retorno simples
when(mock.metodo()).thenReturn(valor);

// Retorno baseado em parâmetro
when(mock.metodo(anyString())).thenReturn(valor);
when(mock.metodo("específico")).thenReturn(valor);

// Lançar exceção
when(mock.metodo()).thenThrow(new RuntimeException("Erro"));

// Múltiplas chamadas
when(mock.metodo()).thenReturn(valor1).thenReturn(valor2);
```

### Verificações
```java
// Verificar se foi chamado
verify(mock).metodo();

// Verificar número de chamadas
verify(mock, times(2)).metodo();
verify(mock, never()).metodo();

// Verificar parâmetros
verify(mock).metodo(eq("valor"));
verify(mock).metodo(any(String.class));

// Verificar ordem
InOrder inOrder = inOrder(mock1, mock2);
inOrder.verify(mock1).metodo1();
inOrder.verify(mock2).metodo2();
```

## AssertJ - Asserções Fluentes

### Asserções Básicas
```java
// Valores simples
assertThat(resultado).isNotNull();
assertThat(resultado).isEqualTo(esperado);
assertThat(numero).isGreaterThan(0);

// Strings
assertThat(texto).isNotEmpty();
assertThat(texto).contains("substring");
assertThat(texto).startsWith("prefixo");

// Coleções
assertThat(lista).hasSize(3);
assertThat(lista).contains(elemento);
assertThat(lista).extracting("propriedade").contains("valor");

// Exceções
assertThatThrownBy(() -> service.metodo())
    .isInstanceOf(RuntimeException.class)
    .hasMessage("Mensagem esperada");
```

## Cenários de Teste Comuns

### 1. Teste de Criação
```java
@Test
void criarEntidade_ComDadosValidos_DeveSalvarERetornar() {
    // Testa fluxo completo de criação
    // Verifica mapeamentos
    // Confirma persistência
}
```

### 2. Teste de Busca
```java
@Test
void buscarPorId_QuandoExiste_DeveRetornar() {
    // Testa busca bem-sucedida
}

@Test
void buscarPorId_QuandoNaoExiste_DeveLancarExcecao() {
    // Testa cenário de não encontrado
}
```

### 3. Teste de Atualização
```java
@Test
void atualizarEntidade_ComDadosValidos_DeveAtualizarERetornar() {
    // Testa atualização de campos
    // Verifica preservação de IDs
}
```

### 4. Teste de Exclusão (Soft Delete)
```java
@Test
void excluirEntidade_DeveMarcarComoIndisponivel() {
    // Testa soft delete
    // Verifica que não remove fisicamente
}
```

## Boas Práticas

### 1. Nomenclatura Clara
```java
// ❌ Ruim
@Test
void test1() {}

// ✅ Bom
@Test
void criarAutor_ComEmailDuplicado_DeveLancarExcecao() {}
```

### 2. Testes Independentes
```java
// Cada teste deve ser independente
// Não depender de ordem de execução
// Limpar estado entre testes
```

### 3. Dados de Teste
```java
// Use builders ou factory methods
private AutorRequestDTO criarAutorRequestValido() {
    return new AutorRequestDTO("Nome Teste", "teste@email.com");
}
```

### 4. Verificações Específicas
```java
// ❌ Genérico demais
verify(repository).save(any());

// ✅ Específico
verify(repository).save(argThat(autor -> 
    autor.getNome().equals("Nome Esperado")));
```

## Execução de Testes

### Maven
```bash
# Todos os testes
mvn test

# Classe específica
mvn test -Dtest=AutorServiceTest

# Método específico
mvn test -Dtest=AutorServiceTest#criarAutor_DeveRetornar
```

### IDE
- Botão direito na classe/método → "Run Test"
- Atalhos: Ctrl+Shift+F10 (IntelliJ)

## Cobertura de Código

### JaCoCo Plugin
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Relatório
```bash
mvn clean test jacoco:report
# Relatório em: target/site/jacoco/index.html
```