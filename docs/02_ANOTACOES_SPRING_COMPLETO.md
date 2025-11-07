# 📝 GUIA COMPLETO DE ANOTAÇÕES SPRING - HACKATHON

## 🏗️ ANOTAÇÕES DE ESTRUTURA

### @SpringBootApplication
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```
**O QUE FAZ**: Combina 3 anotações em uma
- `@Configuration` - Classe de configuração
- `@EnableAutoConfiguration` - Configuração automática
- `@ComponentScan` - Escaneamento de componentes

---

## 🎯 ANOTAÇÕES DE COMPONENTES

### @Component
```java
@Component
public class EmailService {
    // Componente genérico do Spring
}
```
**O QUE FAZ**: Marca classe como componente gerenciado pelo Spring

### @Service
```java
@Service
public class AutorService {
    // Lógica de negócio
}
```
**O QUE FAZ**: Especialização de @Component para camada de serviço

### @Repository
```java
@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    // Acesso a dados
}
```
**O QUE FAZ**: Especialização de @Component para camada de dados

### @Controller vs @RestController
```java
@Controller
public class WebController {
    // Retorna views (HTML)
}

@RestController
public class ApiController {
    // Retorna JSON automaticamente
}
```
**DIFERENÇA**: 
- `@Controller` - Para páginas web
- `@RestController` - Para APIs REST (JSON)

---

## 🌐 ANOTAÇÕES WEB/REST

### @RequestMapping
```java
@RequestMapping("/api/autores")
public class AutorController {
    // Base URL para todos os métodos
}
```

### Métodos HTTP Específicos
```java
@GetMapping("/api/autores")           // GET
@PostMapping("/api/autores")          // POST  
@PutMapping("/api/autores/{id}")      // PUT
@DeleteMapping("/api/autores/{id}")   // DELETE
@PatchMapping("/api/autores/{id}")    // PATCH
```

### @PathVariable
```java
@GetMapping("/autores/{id}")
public AutorResponseDTO buscar(@PathVariable Long id) {
    // Captura {id} da URL
}
```

### @RequestBody
```java
@PostMapping("/autores")
public AutorResponseDTO criar(@RequestBody AutorRequestDTO dto) {
    // Converte JSON do body para objeto
}
```

### @RequestParam
```java
@GetMapping("/autores")
public List<AutorResponseDTO> listar(@RequestParam(defaultValue = "0") int page) {
    // Captura parâmetros da query string: ?page=1
}
```

### @RequestHeader
```java
@GetMapping("/me")
public String getUser(@RequestHeader("Authorization") String token) {
    // Captura header da requisição
}
```

---

## 🗄️ ANOTAÇÕES JPA/HIBERNATE

### @Entity
```java
@Entity
@Table(name = "autor")
public class Autor {
    // Marca como entidade do banco
}
```

### @Id e @GeneratedValue
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
**O QUE FAZ**: Define chave primária com auto-incremento

### @Column
```java
@Column(name = "nome", nullable = false, length = 100)
private String nome;

@Column(unique = true)
private String email;
```
**OPÇÕES**:
- `nullable = false` - NOT NULL
- `unique = true` - UNIQUE
- `length = 100` - VARCHAR(100)

### Relacionamentos
```java
// One-to-Many
@OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
private List<Livro> livros;

// Many-to-One
@ManyToOne
@JoinColumn(name = "autor_id")
private Autor autor;

// Many-to-Many
@ManyToMany
@JoinTable(name = "livro_autor",
    joinColumns = @JoinColumn(name = "livro_id"),
    inverseJoinColumns = @JoinColumn(name = "autor_id"))
private Set<Autor> autores;
```

### @Enumerated
```java
@Enumerated(EnumType.STRING)  // Salva como texto
private Categoria categoria;

@Enumerated(EnumType.ORDINAL) // Salva como número
private Status status;
```

---

## ✅ ANOTAÇÕES DE VALIDAÇÃO

### Validações Básicas
```java
@NotNull(message = "Campo obrigatório")
private String nome;

@NotBlank(message = "Não pode estar vazio")
private String titulo;

@NotEmpty(message = "Lista não pode estar vazia")
private List<Long> autoresIds;
```

### Validações de Tamanho
```java
@Size(min = 2, max = 100, message = "Entre 2 e 100 caracteres")
private String nome;

@Min(value = 1, message = "Mínimo 1")
private Integer quantidade;

@Max(value = 100, message = "Máximo 100")
private Integer idade;
```

### Validações de Formato
```java
@Email(message = "Email inválido")
private String email;

@Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido")
private String cep;

@Past(message = "Data deve ser no passado")
private LocalDate nascimento;

@PastOrPresent(message = "Data não pode ser futura")
private LocalDate publicacao;
```

### @Valid
```java
@PostMapping("/autores")
public ResponseEntity<AutorResponseDTO> criar(@Valid @RequestBody AutorRequestDTO dto) {
    // Valida automaticamente o DTO
}
```

---

## 🔧 ANOTAÇÕES DE CONFIGURAÇÃO

### @Configuration
```java
@Configuration
public class AppConfig {
    // Classe de configuração
}
```

### @Bean
```java
@Configuration
public class AppConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### @Value
```java
@Value("${app.jwt.secret}")
private String jwtSecret;

@Value("${app.jwt.expiration:86400000}")
private Long jwtExpiration;
```

---

## 🔒 ANOTAÇÕES DE SEGURANÇA

### @EnableWebSecurity
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configuração de segurança
}
```

### @PreAuthorize
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/autores/{id}")
public void deletar(@PathVariable Long id) {
    // Só admins podem deletar
}
```

---

## 🧪 ANOTAÇÕES DE TESTE

### @SpringBootTest
```java
@SpringBootTest
class AutorServiceTest {
    // Teste de integração completo
}
```

### @WebMvcTest
```java
@WebMvcTest(AutorController.class)
class AutorControllerTest {
    // Teste apenas do controller
}
```

### @MockBean
```java
@MockBean
private AutorService autorService;
```

### @Autowired (em testes)
```java
@Autowired
private MockMvc mockMvc;

@Autowired
private TestRestTemplate restTemplate;
```

---

## 📦 ANOTAÇÕES LOMBOK

### @Data
```java
@Data
public class AutorDTO {
    // Gera: getters, setters, equals, hashCode, toString
}
```

### @Getter / @Setter
```java
@Getter
@Setter
public class Autor {
    // Gera apenas getters e setters
}
```

### @NoArgsConstructor / @AllArgsConstructor
```java
@NoArgsConstructor  // Construtor sem parâmetros
@AllArgsConstructor // Construtor com todos os parâmetros
public class Autor {
}
```

### @RequiredArgsConstructor
```java
@RequiredArgsConstructor
public class AutorService {
    private final AutorRepository repository; // Construtor automático
}
```

### @Builder
```java
@Builder
public class Autor {
    // Permite: Autor.builder().nome("João").email("joao@email.com").build()
}
```

---

## 🗺️ ANOTAÇÕES MAPSTRUCT

### @Mapper
```java
@Mapper(componentModel = "spring")
public interface AutorMapper {
    // Mapeamento automático DTO ↔ Entity
}
```

### @Mapping
```java
@Mapping(target = "id", ignore = true)
@Mapping(source = "nomeCompleto", target = "nome")
AutorEntity toEntity(AutorDTO dto);
```

---

## 📚 ANOTAÇÕES SWAGGER/OPENAPI

### @Tag
```java
@Tag(name = "Autores", description = "Gerenciamento de autores")
@RestController
public class AutorController {
}
```

### @Operation
```java
@Operation(summary = "Buscar autor por ID", description = "Retorna um autor específico")
@GetMapping("/{id}")
public AutorResponseDTO buscar(@PathVariable Long id) {
}
```

### @ApiResponse
```java
@ApiResponse(responseCode = "200", description = "Autor encontrado")
@ApiResponse(responseCode = "404", description = "Autor não encontrado")
@GetMapping("/{id}")
public AutorResponseDTO buscar(@PathVariable Long id) {
}
```

---

## 🎯 TOP 20 ANOTAÇÕES MAIS USADAS

1. **@SpringBootApplication** - Classe principal
2. **@RestController** - Controller de API
3. **@Service** - Camada de serviço
4. **@Repository** - Camada de dados
5. **@Entity** - Entidade JPA
6. **@Id** - Chave primária
7. **@GetMapping** - Endpoint GET
8. **@PostMapping** - Endpoint POST
9. **@RequestBody** - Body da requisição
10. **@PathVariable** - Variável da URL
11. **@Autowired** - Injeção de dependência
12. **@Valid** - Validação automática
13. **@NotNull** - Campo obrigatório
14. **@Column** - Coluna do banco
15. **@Configuration** - Classe de configuração
16. **@Bean** - Bean do Spring
17. **@RequiredArgsConstructor** - Construtor Lombok
18. **@Transactional** - Transação de banco
19. **@Value** - Propriedade do application.properties
20. **@Component** - Componente genérico

---

## 💡 DICAS PRO HACKATHON

### Combinações Poderosas
```java
// Controller completo
@RestController
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@Tag(name = "Autores")
public class AutorController {

    // Service com transação
    @Service
    @RequiredArgsConstructor
    @Transactional(readOnly = true)
    public class AutorService {

        // Entity completa
        @Entity
        @Table(name = "autor")
        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        public class Autor {
```

### Validação Completa
```java
public record AutorRequestDTO(
    @NotBlank(message = "Nome obrigatório")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String nome,
    
    @Email(message = "Email inválido")
    @NotBlank(message = "Email obrigatório")
    String email
) {}
```