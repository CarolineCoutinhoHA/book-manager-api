# 🏗️ CRUD - CONTROLLER, SERVICE, MODEL - ARQUITETURA COMPLETA

## 🎯 ARQUITETURA EM CAMADAS

### Visão Geral
```
┌─────────────────┐    HTTP Request     ┌─────────────────┐
│   FRONTEND      │ ──────────────────> │   CONTROLLER    │
│  (React/Vue)    │                     │  (@RestController)│
└─────────────────┘                     └─────────────────┘
                                                 │
                                                 │ DTO
                                                 ▼
                                        ┌─────────────────┐
                                        │    SERVICE      │
                                        │   (@Service)    │
                                        └─────────────────┘
                                                 │
                                                 │ Entity
                                                 ▼
                                        ┌─────────────────┐
                                        │   REPOSITORY    │
                                        │ (@Repository)   │
                                        └─────────────────┘
                                                 │
                                                 │ SQL
                                                 ▼
                                        ┌─────────────────┐
                                        │    DATABASE     │
                                        │      (H2)       │
                                        └─────────────────┘
```

### Responsabilidades
- **Controller**: Recebe requisições HTTP, valida entrada, retorna JSON
- **Service**: Lógica de negócio, regras, transações
- **Repository**: Acesso aos dados, queries
- **Model/Entity**: Representação dos dados, mapeamento JPA

---

## 🎮 CONTROLLER - CAMADA DE APRESENTAÇÃO

### Anatomia de um Controller
```java
@RestController                    // 1. Marca como controller REST
@RequestMapping("/api/autores")    // 2. Base URL para todos os endpoints
@RequiredArgsConstructor          // 3. Lombok: construtor automático
@Tag(name = "Autores")            // 4. Swagger: agrupamento
public class AutorController {

    private final AutorService autorService;  // 5. Injeção do service

    // 6. Endpoint GET - Listar todos
    @GetMapping
    @Operation(summary = "Listar todos os autores")
    public ResponseEntity<List<AutorResponseDTO>> listarTodos() {
        List<AutorResponseDTO> autores = autorService.findAllAutores();
        return ResponseEntity.ok(autores);
    }

    // 7. Endpoint GET - Buscar por ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar autor por ID")
    public ResponseEntity<AutorResponseDTO> buscarPorId(@PathVariable Long id) {
        AutorResponseDTO autor = autorService.findAutorById(id);
        return ResponseEntity.ok(autor);
    }

    // 8. Endpoint POST - Criar novo
    @PostMapping
    @Operation(summary = "Criar novo autor")
    public ResponseEntity<AutorResponseDTO> criar(@Valid @RequestBody AutorRequestDTO dto) {
        AutorResponseDTO novoAutor = autorService.createAutor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAutor);
    }

    // 9. Endpoint PUT - Atualizar
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor")
    public ResponseEntity<AutorResponseDTO> atualizar(
            @PathVariable Long id, 
            @Valid @RequestBody AutorRequestDTO dto) {
        AutorResponseDTO autorAtualizado = autorService.updateAutor(id, dto);
        return ResponseEntity.ok(autorAtualizado);
    }

    // 10. Endpoint DELETE - Deletar (soft delete)
    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar autor")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        autorService.deleteAutor(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Anotações do Controller Explicadas
```java
// CLASSE
@RestController        // Combina @Controller + @ResponseBody (retorna JSON)
@RequestMapping("/api/autores")  // Prefixo para todos os endpoints
@RequiredArgsConstructor        // Lombok: gera construtor com campos final
@Tag(name = "Autores")         // Swagger: agrupa endpoints na documentação

// MÉTODOS
@GetMapping           // HTTP GET
@PostMapping          // HTTP POST  
@PutMapping("/{id}")  // HTTP PUT com path variable
@DeleteMapping("/{id}") // HTTP DELETE com path variable

// PARÂMETROS
@PathVariable Long id              // Captura {id} da URL
@RequestBody AutorRequestDTO dto   // Converte JSON do body para DTO
@Valid                            // Valida DTO automaticamente

// SWAGGER
@Operation(summary = "Descrição") // Documenta o endpoint
@ApiResponse(responseCode = "200") // Documenta possíveis respostas
```

---

## 🧠 SERVICE - CAMADA DE NEGÓCIO

### Anatomia de um Service
```java
@Service                          // 1. Marca como service do Spring
@RequiredArgsConstructor         // 2. Lombok: construtor automático
@Transactional(readOnly = true)  // 3. Transação read-only por padrão
@Slf4j                          // 4. Lombok: logger automático
public class AutorService {

    // 5. Dependências injetadas
    private final AutorRepository autorRepository;
    private final AutorMapper autorMapper;

    // 6. HELPER METHODS (métodos auxiliares privados)
    
    /**
     * HELPER 1: Buscar autor ou lançar exceção 404
     */
    private Autor findAutorOrThrow(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "O autor de ID: " + id + " não foi encontrado!"));
    }

    /**
     * HELPER 2: Validar email único
     */
    private void validateUniqueEmail(String email) {
        if (autorRepository.findAll().stream()
                .anyMatch(autor -> autor.getEmail().equals(email))) {
            throw new BusinessException("Email " + email + " já está em uso!");
        }
    }

    // 7. MÉTODOS PÚBLICOS (operações CRUD)

    /**
     * CREATE - Criar novo autor
     */
    @Transactional  // Sobrescreve readOnly = true
    public AutorResponseDTO createAutor(AutorRequestDTO dto) {
        log.info("Criando novo autor: {}", dto.nome());

        // 1. Validar regras de negócio
        validateUniqueEmail(dto.email());

        // 2. Converter DTO para Entity
        Autor novoAutor = autorMapper.toEntity(dto);

        // 3. Salvar no banco
        Autor autorSalvo = autorRepository.save(novoAutor);

        // 4. Converter Entity para DTO de resposta
        return autorMapper.toResponseDTO(autorSalvo);
    }

    /**
     * READ - Buscar por ID
     */
    public AutorResponseDTO findAutorById(Long id) {
        log.info("Buscando autor por ID: {}", id);

        Autor autor = findAutorOrThrow(id);
        return autorMapper.toResponseDTO(autor);
    }

    /**
     * READ - Listar todos (com filtro de soft delete)
     */
    public List<AutorResponseDTO> findAllAutores() {
        log.info("Listando todos os autores ativos");

        return autorRepository.findAll().stream()
                .filter(Autor::isDisponibilidade)  // Filtro soft delete
                .map(autorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * UPDATE - Atualizar autor
     */
    @Transactional
    public AutorResponseDTO updateAutor(Long id, AutorRequestDTO dto) {
        log.info("Atualizando autor ID: {} com dados: {}", id, dto.nome());

        // 1. Buscar autor existente
        Autor autorExistente = findAutorOrThrow(id);

        // 2. Validar email único (se mudou)
        if (!autorExistente.getEmail().equals(dto.email())) {
            validateUniqueEmail(dto.email());
        }

        // 3. Atualizar campos usando mapper
        autorMapper.updateEntityFromDto(dto, autorExistente);

        // 4. Salvar alterações
        Autor autorAtualizado = autorRepository.save(autorExistente);

        return autorMapper.toResponseDTO(autorAtualizado);
    }

    /**
     * DELETE - Soft delete
     */
    @Transactional
    public void deleteAutor(Long id) {
        log.info("Deletando autor ID: {}", id);

        Autor autor = findAutorOrThrow(id);
        
        // Soft delete: marca como indisponível
        autor.indisponivel();
        
        autorRepository.save(autor);
        log.info("Autor ID {} marcado como indisponível", id);
    }
}
```

### Anotações do Service Explicadas
```java
// CLASSE
@Service                         // Marca como service (especialização de @Component)
@RequiredArgsConstructor        // Lombok: construtor com campos final
@Transactional(readOnly = true) // Todas as operações são read-only por padrão
@Slf4j                         // Lombok: adiciona logger (log.info, log.error, etc.)

// MÉTODOS
@Transactional              // Sobrescreve readOnly para operações de escrita
@Transactional(readOnly = true) // Explicitamente read-only (otimização)

// PADRÕES
private Tipo findEntityOrThrow(Long id)  // Helper para buscar ou lançar 404
private void validateBusinessRule()      // Helper para validações
public DTOResponse createEntity()        // Método público para CREATE
public DTOResponse findEntityById()      // Método público para READ
public List<DTOResponse> findAll()       // Método público para READ ALL
public DTOResponse updateEntity()        // Método público para UPDATE
public void deleteEntity()               // Método público para DELETE
```

---

## 🗄️ MODEL/ENTITY - CAMADA DE DADOS

### Anatomia de uma Entity
```java
@Entity                           // 1. Marca como entidade JPA
@Table(name = "autor")           // 2. Nome da tabela no banco
@Getter                          // 3. Lombok: gera getters
@NoArgsConstructor              // 4. Lombok: construtor vazio (obrigatório JPA)
@ToString(onlyExplicitlyIncluded = true)  // 5. Lombok: toString customizado
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // 6. Lombok: equals/hashCode
public class Autor {

    // 7. IDENTIFICADORES
    
    @Id                                    // Chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento
    @Column(name = "id_autor")
    @ToString.Include                      // Incluir no toString
    private Long idAutor;

    @EqualsAndHashCode.Include            // Incluir no equals/hashCode
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    // 8. CAMPOS DE DADOS

    @Column(name = "nome", nullable = false)
    @ToString.Include
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    @ToString.Include
    private String email;

    @Column(name = "cep", nullable = false)
    private String cep;

    @Column(name = "telefone")
    private String telefone;

    @Column(name = "disponibilidade", nullable = false)
    private boolean disponibilidade = true;  // Soft delete

    // 9. RELACIONAMENTOS

    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    @ToString.Exclude  // Excluir do toString (evita LazyInitializationException)
    private final Set<Livro> livrosSet = new HashSet<>();

    // 10. CICLO DE VIDA

    @PrePersist  // Executa antes de salvar pela primeira vez
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    // 11. CONSTRUTORES

    // Construtor para criação (usado pelo Service)
    public Autor(String nome, String email, String cep, String telefone) {
        this.nome = nome;
        this.email = email;
        this.cep = cep;
        this.telefone = telefone;
    }

    // 12. MÉTODOS DE DOMÍNIO (setters controlados)

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // 13. MÉTODOS DE NEGÓCIO

    public void disponivel() {
        this.disponibilidade = true;
    }

    public void indisponivel() {
        this.disponibilidade = false;
    }

    public void atribuirLivroAoAutor(Livro livro) {
        this.livrosSet.add(livro);
    }
}
```

### Anotações JPA Explicadas
```java
// CLASSE
@Entity                    // Marca como entidade do banco
@Table(name = "autor")     // Nome da tabela (opcional se igual ao nome da classe)

// IDENTIFICADORES
@Id                        // Chave primária
@GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento pelo banco
@Column(name = "id_autor") // Nome da coluna (opcional se igual ao campo)

// COLUNAS
@Column(nullable = false)  // NOT NULL
@Column(unique = true)     // UNIQUE
@Column(length = 100)      // VARCHAR(100)
@Column(name = "nome")     // Nome da coluna customizado

// RELACIONAMENTOS
@OneToMany(mappedBy = "autor")           // 1:N (um autor, muitos livros)
@ManyToOne                               // N:1 (muitos livros, um autor)
@ManyToMany                              // N:N (muitos autores, muitos livros)
@JoinColumn(name = "autor_id")           // Coluna de foreign key
@JoinTable(name = "livro_autor")         // Tabela de junção para N:N

// FETCH
fetch = FetchType.LAZY     // Carregamento sob demanda (padrão para coleções)
fetch = FetchType.EAGER    // Carregamento imediato

// CASCADE
cascade = CascadeType.ALL        // Propaga todas as operações
cascade = CascadeType.PERSIST    // Propaga apenas INSERT
cascade = CascadeType.REMOVE     // Propaga apenas DELETE

// CICLO DE VIDA
@PrePersist    // Antes de salvar pela primeira vez
@PostPersist   // Depois de salvar pela primeira vez
@PreUpdate     // Antes de atualizar
@PostUpdate    // Depois de atualizar
@PreRemove     // Antes de deletar
@PostRemove    // Depois de deletar
```

---

## 🗂️ REPOSITORY - CAMADA DE ACESSO A DADOS

### Repository Básico
```java
@Repository  // Opcional (JpaRepository já é um componente)
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // Métodos herdados automaticamente:
    // save(entity)           - Salvar/atualizar
    // findById(id)           - Buscar por ID
    // findAll()              - Listar todos
    // deleteById(id)         - Deletar por ID
    // count()                - Contar registros
    // existsById(id)         - Verificar se existe

    // Métodos customizados (Spring Data gera automaticamente):
    
    Optional<Autor> findByEmail(String email);
    
    List<Autor> findByNomeContainingIgnoreCase(String nome);
    
    List<Autor> findByDisponibilidadeTrue();
    
    @Query("SELECT a FROM Autor a WHERE a.disponibilidade = true")
    List<Autor> findAutoresAtivos();
    
    @Query(value = "SELECT * FROM autor WHERE nome LIKE %?1%", nativeQuery = true)
    List<Autor> findByNomeLike(String nome);
}
```

### Métodos Query Automáticos
```java
// CONVENÇÕES DE NOMENCLATURA (Spring Data gera SQL automaticamente)

findBy + Campo                    // WHERE campo = ?
findBy + Campo + Containing       // WHERE campo LIKE %?%
findBy + Campo + IgnoreCase       // WHERE UPPER(campo) = UPPER(?)
findBy + Campo + StartingWith     // WHERE campo LIKE ?%
findBy + Campo + EndingWith       // WHERE campo LIKE %?
findBy + Campo + Between          // WHERE campo BETWEEN ? AND ?
findBy + Campo + LessThan         // WHERE campo < ?
findBy + Campo + GreaterThan      // WHERE campo > ?
findBy + Campo + IsNull           // WHERE campo IS NULL
findBy + Campo + IsNotNull        // WHERE campo IS NOT NULL
findBy + Campo + True             // WHERE campo = true
findBy + Campo + False            // WHERE campo = false

// EXEMPLOS PRÁTICOS
Optional<Autor> findByEmail(String email);
List<Autor> findByNomeContainingIgnoreCase(String nome);
List<Autor> findByDisponibilidadeTrue();
List<Autor> findByNomeStartingWith(String prefixo);
List<Autor> findByIdAutorBetween(Long inicio, Long fim);
```

---

## 🔄 FLUXO COMPLETO CRUD

### CREATE (POST)
```
1. Frontend envia JSON → Controller
2. Controller valida @Valid → DTO
3. Controller chama Service
4. Service valida regras de negócio
5. Service usa Mapper: DTO → Entity
6. Service chama Repository.save()
7. Repository executa INSERT no banco
8. Service usa Mapper: Entity → ResponseDTO
9. Controller retorna 201 Created + JSON
```

### READ (GET)
```
1. Frontend faz GET /api/autores/1
2. Controller captura @PathVariable id
3. Controller chama Service.findById(id)
4. Service chama Repository.findById(id)
5. Repository executa SELECT no banco
6. Service usa Mapper: Entity → ResponseDTO
7. Controller retorna 200 OK + JSON
```

### UPDATE (PUT)
```
1. Frontend envia JSON + ID → Controller
2. Controller valida @Valid → DTO
3. Controller chama Service.update(id, dto)
4. Service busca Entity existente
5. Service valida regras de negócio
6. Service usa Mapper para atualizar Entity
7. Service chama Repository.save()
8. Repository executa UPDATE no banco
9. Service retorna ResponseDTO
10. Controller retorna 200 OK + JSON
```

### DELETE (DELETE)
```
1. Frontend faz DELETE /api/autores/1
2. Controller captura @PathVariable id
3. Controller chama Service.delete(id)
4. Service busca Entity existente
5. Service marca como indisponível (soft delete)
6. Service chama Repository.save()
7. Repository executa UPDATE (não DELETE)
8. Controller retorna 204 No Content
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Estrutura Consistente
```java
// SEMPRE seguir o mesmo padrão em todos os controllers
@RestController
@RequestMapping("/api/{recurso}")
@RequiredArgsConstructor
@Tag(name = "{Recurso}")
public class {Recurso}Controller {
    
    private final {Recurso}Service service;
    
    @GetMapping
    public ResponseEntity<List<{Recurso}ResponseDTO>> listarTodos() {}
    
    @GetMapping("/{id}")
    public ResponseEntity<{Recurso}ResponseDTO> buscarPorId(@PathVariable Long id) {}
    
    @PostMapping
    public ResponseEntity<{Recurso}ResponseDTO> criar(@Valid @RequestBody {Recurso}RequestDTO dto) {}
    
    @PutMapping("/{id}")
    public ResponseEntity<{Recurso}ResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody {Recurso}RequestDTO dto) {}
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {}
}
```

### 2. Tratamento de Erros
```java
// Service sempre lança exceções específicas
private Autor findAutorOrThrow(Long id) {
    return autorRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Autor não encontrado: " + id));
}

// GlobalExceptionHandler captura e padroniza respostas
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(404).body(new ErrorResponse(404, ex.getMessage()));
}
```

### 3. Logs Informativos
```java
@Service
@Slf4j
public class AutorService {
    
    public AutorResponseDTO createAutor(AutorRequestDTO dto) {
        log.info("Criando autor: {}", dto.nome());
        
        try {
            // lógica...
            log.info("Autor criado com sucesso: ID {}", autor.getId());
            return response;
        } catch (Exception e) {
            log.error("Erro ao criar autor: {}", e.getMessage());
            throw e;
        }
    }
}
```

---

## 🎯 CHECKLIST CRUD COMPLETO

### Controller
- [ ] @RestController e @RequestMapping configurados
- [ ] Todos os endpoints CRUD implementados (GET, POST, PUT, DELETE)
- [ ] @Valid em RequestBody para validação automática
- [ ] ResponseEntity com status codes corretos
- [ ] @PathVariable para capturar IDs
- [ ] Swagger documentado com @Operation

### Service
- [ ] @Service e @Transactional configurados
- [ ] Métodos helper privados para busca e validação
- [ ] Regras de negócio implementadas
- [ ] Logs informativos com @Slf4j
- [ ] Exceções específicas para cada erro
- [ ] Soft delete implementado

### Model/Entity
- [ ] @Entity e @Table configurados
- [ ] @Id e @GeneratedValue para chave primária
- [ ] @Column com constraints (nullable, unique)
- [ ] Relacionamentos mapeados corretamente
- [ ] @PrePersist para inicializações
- [ ] Métodos de domínio para operações seguras
- [ ] Lombok para reduzir boilerplate

### Repository
- [ ] Extends JpaRepository<Entity, Long>
- [ ] Métodos de busca customizados se necessário
- [ ] Queries nativas apenas quando necessário