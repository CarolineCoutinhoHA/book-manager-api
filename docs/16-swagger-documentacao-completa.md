# Swagger/OpenAPI - Documentação Completa da API

## 🎯 Visão Geral

Swagger (OpenAPI 3.0) é uma ferramenta que gera automaticamente documentação interativa para APIs REST. Permite testar endpoints diretamente no navegador, facilitando desenvolvimento e integração.

## 🏗️ O que é Swagger

### Características:
- **Documentação Automática**: Gera docs a partir do código
- **Interface Interativa**: Testa endpoints no navegador
- **Padrão OpenAPI**: Especificação amplamente adotada
- **Geração de Código**: Pode gerar clientes em várias linguagens
- **Validação**: Valida requests/responses automaticamente

### Vantagens:
- ✅ **Zero Configuração**: Funciona out-of-the-box
- ✅ **Sempre Atualizado**: Sincronizado com o código
- ✅ **Testável**: Interface para testar API
- ✅ **Documentação Rica**: Exemplos, schemas, validações
- ✅ **Padrão da Indústria**: Amplamente reconhecido

## 🔧 Configuração Completa

### 1. Dependências (build.gradle)

```gradle
dependencies {
    // SpringDoc OpenAPI (Swagger 3.0)
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    
    // Outras dependências...
}
```

### 2. Configuração Básica (application.properties)

```properties
# ====================================================
# CONFIGURAÇÃO SWAGGER/OPENAPI
# ====================================================

# Habilitar Swagger UI
springdoc.swagger-ui.enabled=true

# Path customizado para Swagger UI
springdoc.swagger-ui.path=/swagger-ui.html

# Path para API docs JSON
springdoc.api-docs.path=/v3/api-docs

# Mostrar actuator endpoints (opcional)
springdoc.show-actuator=false

# ====================================================
# CONFIGURAÇÕES AVANÇADAS
# ====================================================

# Ordenar endpoints por método HTTP
springdoc.swagger-ui.operations-sorter=method

# Ordenar tags alfabeticamente
springdoc.swagger-ui.tags-sorter=alpha

# Tentar requisições automaticamente
springdoc.swagger-ui.try-it-out-enabled=true

# Mostrar extensões vendor
springdoc.swagger-ui.display-request-duration=true
```

### 3. Configuração Avançada (SwaggerConfig.java)

```java
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Biblioteca API",
        version = "1.0.0",
        description = "API REST para gerenciamento de biblioteca com autores e livros",
        contact = @Contact(
            name = "Equipe de Desenvolvimento",
            email = "dev@biblioteca.com",
            url = "https://biblioteca.com"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(
            description = "Desenvolvimento",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Produção",
            url = "https://api.biblioteca.com"
        )
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Token JWT obtido através do endpoint /api/auth/login"
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("biblioteca-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("autenticacao")
                .pathsToMatch("/api/auth/**")
                .build();
    }
}
```

## 🌐 Acessando o Swagger UI

### 1. Iniciar Aplicação
```bash
./gradlew bootRun
```

### 2. Abrir Swagger UI
**URL**: `http://localhost:8080/swagger-ui/index.html`

### 3. URLs Alternativas
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## 📝 Documentando Controllers

### 1. Controller com Anotações Básicas

```java
@RestController
@RequestMapping("/api/autores")
@RequiredArgsConstructor
@Tag(name = "Autores", description = "Operações relacionadas ao gerenciamento de autores")
public class AutorController {

    private final AutorService autorService;

    @Operation(
        summary = "Criar novo autor",
        description = "Cria um novo autor no sistema com validação de email único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Autor criado com sucesso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AutorResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos fornecidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email já está em uso",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<AutorResponseDTO> createAuthor(
            @Parameter(description = "Dados do autor a ser criado", required = true)
            @RequestBody @Valid AutorRequestDTO autorRequestDTO) {
        
        AutorResponseDTO createdAuthor = autorService.createAutor(autorRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    @Operation(
        summary = "Listar autores",
        description = "Retorna lista paginada de autores ativos no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de autores retornada com sucesso"
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<NomeAutorDTO>> FindAllAutors(
            @Parameter(description = "Número da página (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamanho da página", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        Page<NomeAutorDTO> autoresPage = autorService.findAllAutosPaginacao(page, size);
        return ResponseEntity.ok(autoresPage);
    }

    @Operation(
        summary = "Buscar autor por ID",
        description = "Retorna um autor específico pelo seu identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autor encontrado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Autor não encontrado"
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<AutorResponseDTO> findAutorById(
            @Parameter(description = "ID único do autor", example = "1")
            @PathVariable Long id) {
        
        AutorResponseDTO autor = autorService.findAutorById(id);
        return ResponseEntity.ok(autor);
    }
}
```

### 2. Controller de Autenticação

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Registrar novo usuário",
        description = "Cria uma nova conta de usuário no sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuário registrado com sucesso"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Username já existe"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Parameter(description = "Dados para registro do usuário")
            @RequestBody @Valid UsuarioRequestDTO dto) {
        
        authService.register(dto);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuário registrado com sucesso");
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Fazer login",
        description = "Autentica usuário e retorna token JWT para acesso aos endpoints protegidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Parameter(description = "Credenciais de login")
            @RequestBody @Valid LoginRequestDTO dto) {
        
        String token = authService.login(dto);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
}
```

## 📊 Documentando DTOs

### 1. DTO de Request com Validações

```java
@Schema(description = "Dados para criação de um novo autor")
public record AutorRequestDTO(
    
    @Schema(
        description = "Nome completo do autor",
        example = "João Silva",
        minLength = 1,
        maxLength = 100
    )
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,
    
    @Schema(
        description = "Email único do autor",
        example = "joao.silva@email.com",
        format = "email"
    )
    @Email(message = "Email deve ter formato válido")
    @NotBlank(message = "Email é obrigatório")
    String email,
    
    @Schema(
        description = "CEP do endereço do autor",
        example = "12345-678",
        pattern = "\\d{5}-\\d{3}"
    )
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve ter formato XXXXX-XXX")
    @NotBlank(message = "CEP é obrigatório")
    String cep,
    
    @Schema(
        description = "Telefone do autor (opcional)",
        example = "(11) 99999-9999",
        pattern = "\\(\\d{2}\\) \\d{5}-\\d{4}"
    )
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve ter formato (XX) XXXXX-XXXX")
    String telefone
) {}
```

### 2. DTO de Response

```java
@Schema(description = "Dados de resposta de um autor")
public record AutorResponseDTO(
    
    @Schema(description = "Identificador único do autor", example = "1")
    Long idAutor,
    
    @Schema(description = "Nome completo do autor", example = "João Silva")
    String nome,
    
    @Schema(description = "Email do autor", example = "joao.silva@email.com")
    String email
) {}
```

### 3. DTO de Livro com Enum

```java
@Schema(description = "Dados para criação de um novo livro")
public record LivroRequestDTO(
    
    @Schema(description = "Título do livro", example = "Clean Code")
    @NotBlank(message = "Título é obrigatório")
    String titulo,
    
    @Schema(description = "ISBN único do livro", example = "978-0132350884")
    @NotBlank(message = "ISBN é obrigatório")
    String isbn,
    
    @Schema(description = "Data de publicação", example = "2008-08-01")
    @NotNull(message = "Data de publicação é obrigatória")
    LocalDate dataDePublicacao,
    
    @Schema(
        description = "Categoria do livro",
        example = "TECNOLOGIA",
        allowableValues = {"FICCAO", "NAO_FICCAO", "TECNOLOGIA", "CIENCIA", "HISTORIA"}
    )
    @NotNull(message = "Categoria é obrigatória")
    Categoria categoria,
    
    @Schema(
        description = "Lista de IDs dos autores do livro",
        example = "[1, 2]"
    )
    @NotEmpty(message = "Livro deve ter pelo menos um autor")
    List<Long> autoresIds
) {}
```

## 🔒 Configurando Autenticação JWT

### 1. Esquema de Segurança Global

```java
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Token JWT obtido através do endpoint /api/auth/login. " +
                 "Formato: Bearer {token}"
)
```

### 2. Aplicando Segurança nos Endpoints

```java
@SecurityRequirement(name = "bearerAuth")
@PostMapping
public ResponseEntity<AutorResponseDTO> createAuthor(...) {
    // implementação
}
```

### 3. Configuração no Swagger UI

No Swagger UI:
1. Clique no botão **"Authorize"** (cadeado)
2. Digite: `Bearer SEU_TOKEN_JWT`
3. Clique **"Authorize"**
4. Agora todos os endpoints protegidos funcionarão

## 🎨 Personalizando a Interface

### 1. Configuração Visual

```properties
# Tema escuro
springdoc.swagger-ui.theme=dark

# Configurar layout
springdoc.swagger-ui.layout=BaseLayout

# Mostrar/ocultar seções
springdoc.swagger-ui.default-models-expand-depth=1
springdoc.swagger-ui.default-model-expand-depth=1

# Configurar filtros
springdoc.swagger-ui.filter=true
```

### 2. Customização Avançada

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Biblioteca API")
            .version("1.0.0")
            .description("API REST completa para gerenciamento de biblioteca")
            .termsOfService("https://biblioteca.com/terms")
            .contact(new Contact()
                .name("Suporte Técnico")
                .url("https://biblioteca.com/support")
                .email("suporte@biblioteca.com"))
            .license(new License()
                .name("MIT")
                .url("https://opensource.org/licenses/MIT")))
        .externalDocs(new ExternalDocumentation()
            .description("Documentação Completa")
            .url("https://docs.biblioteca.com"))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(new Components()
            .addSecuritySchemes("bearerAuth", new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
}
```

## 📋 Exemplos de Uso no Swagger UI

### 1. Testando Autenticação

#### Registrar Usuário:
1. Abra **POST /api/auth/register**
2. Clique **"Try it out"**
3. Cole o JSON:
```json
{
    "username": "admin",
    "password": "123456"
}
```
4. Clique **"Execute"**

#### Fazer Login:
1. Abra **POST /api/auth/login**
2. Use as mesmas credenciais
3. Copie o token da resposta
4. Clique **"Authorize"** no topo
5. Digite: `Bearer SEU_TOKEN`

### 2. Testando CRUD de Autores

#### Criar Autor:
```json
{
    "nome": "João Silva",
    "email": "joao@email.com",
    "cep": "12345-678",
    "telefone": "(11) 99999-9999"
}
```

#### Listar Autores:
- Parâmetros: `page=0`, `size=10`

#### Buscar por ID:
- Path parameter: `id=1`

### 3. Testando CRUD de Livros

#### Criar Livro:
```json
{
    "titulo": "Clean Code",
    "isbn": "978-0132350884",
    "dataDePublicacao": "2008-08-01",
    "categoria": "TECNOLOGIA",
    "autoresIds": [1]
}
```

## 🚨 Troubleshooting

### 1. Swagger UI não carrega
```
404 Not Found - /swagger-ui/index.html
```

**Soluções**:
```properties
# Verificar dependência
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'

# Verificar configuração
springdoc.swagger-ui.enabled=true
```

### 2. Endpoints não aparecem
**Causas**:
- Controladores não anotados com `@RestController`
- Paths não incluídos no `GroupedOpenApi`
- Aplicação não iniciada corretamente

### 3. Autenticação não funciona
**Soluções**:
1. Verificar `@SecurityRequirement(name = "bearerAuth")`
2. Configurar `@SecurityScheme` corretamente
3. Usar formato correto: `Bearer TOKEN`

### 4. Schemas não aparecem
**Soluções**:
1. Adicionar `@Schema` nos DTOs
2. Verificar imports do OpenAPI
3. Usar `@Parameter` nos métodos

## 🔒 Segurança do Swagger

### Configuração por Ambiente:

```yaml
# application-dev.yml
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true

# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false  # DESABILITAR EM PRODUÇÃO!
  api-docs:
    enabled: false
```

### Proteção com Autenticação:

```java
@Configuration
public class SwaggerSecurityConfig {
    
    @Bean
    @Profile("prod")
    public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                .hasRole("ADMIN")  // Apenas admins
            )
            .build();
    }
}
```

## 📊 Métricas e Monitoramento

### Integração com Actuator:

```properties
# Mostrar endpoints do Actuator no Swagger
springdoc.show-actuator=true

# Configurar Actuator
management.endpoints.web.exposure.include=health,info,metrics
```

### Documentar Endpoints de Saúde:

```java
@Tag(name = "Monitoramento", description = "Endpoints de saúde e métricas")
@RestController
public class HealthController {
    
    @Operation(summary = "Verificar saúde da aplicação")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
```

## 📋 Checklist Swagger

### Configuração:
- [ ] Dependência SpringDoc adicionada
- [ ] Swagger UI habilitado
- [ ] Configuração básica funcionando
- [ ] URLs acessíveis

### Documentação:
- [ ] Controllers anotados com `@Tag`
- [ ] Métodos com `@Operation`
- [ ] DTOs com `@Schema`
- [ ] Parâmetros com `@Parameter`
- [ ] Respostas com `@ApiResponse`

### Segurança:
- [ ] JWT configurado
- [ ] `@SecurityRequirement` nos endpoints
- [ ] Botão Authorize funcionando
- [ ] Testes com token funcionando

### Testes:
- [ ] Todos os endpoints testáveis
- [ ] Exemplos funcionando
- [ ] Validações aparecendo
- [ ] Erros documentados

## 🎯 Resumo Final

Swagger/OpenAPI oferece:

1. **Documentação Automática** - Sempre sincronizada
2. **Interface Interativa** - Testa API no navegador
3. **Padrão da Indústria** - Amplamente reconhecido
4. **Facilita Integração** - Desenvolvedores frontend
5. **Validação Automática** - Schemas e exemplos

**URLs importantes:**
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

**Fluxo de uso:**
1. Registrar usuário
2. Fazer login e copiar token
3. Autorizar no Swagger (Bearer TOKEN)
4. Testar todos os endpoints

O Swagger torna sua API profissional e fácil de usar! 🚀