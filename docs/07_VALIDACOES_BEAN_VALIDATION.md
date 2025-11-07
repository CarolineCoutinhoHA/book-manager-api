# ✅ VALIDAÇÕES - BEAN VALIDATION COMPLETO

## 🎯 O QUE É BEAN VALIDATION?

Bean Validation é um padrão Java para validar dados automaticamente usando anotações.

### Vantagens
- ✅ **Declarativo** - Validações direto nas classes
- ✅ **Automático** - Spring valida automaticamente com @Valid
- ✅ **Reutilizável** - Mesmas validações em diferentes camadas
- ✅ **Padronizado** - Mensagens de erro consistentes

---

## 📝 ANOTAÇÕES DE VALIDAÇÃO BÁSICAS

### Validações de Nulidade
```java
@NotNull(message = "Campo não pode ser nulo")
private String nome;

@NotBlank(message = "Campo não pode estar vazio ou só espaços")
private String titulo;

@NotEmpty(message = "Lista não pode estar vazia")
private List<Long> autoresIds;
```

### Validações de Tamanho
```java
@Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
private String nome;

@Min(value = 1, message = "Valor mínimo é 1")
private Integer quantidade;

@Max(value = 100, message = "Valor máximo é 100")
private Integer idade;

@DecimalMin(value = "0.0", message = "Preço deve ser positivo")
private BigDecimal preco;

@DecimalMax(value = "999.99", message = "Preço máximo é 999.99")
private BigDecimal preco;
```

### Validações de Formato
```java
@Email(message = "Email deve ter formato válido")
private String email;

@Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
private String cep;

@Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
private String telefone;

@Pattern(regexp = "^[0-9-]+$", message = "ISBN deve conter apenas números e hífens")
private String isbn;
```

### Validações de Data
```java
@Past(message = "Data deve ser no passado")
private LocalDate nascimento;

@PastOrPresent(message = "Data não pode ser no futuro")
private LocalDate dataPublicacao;

@Future(message = "Data deve ser no futuro")
private LocalDate dataVencimento;

@FutureOrPresent(message = "Data não pode ser no passado")
private LocalDate dataInicio;
```

---

## 🏗️ IMPLEMENTAÇÃO PRÁTICA

### DTO com Validações Completas
```java
public record AutorRequestDTO(
    
    @NotBlank(message = "O nome do autor é obrigatório!")
    @Size(max = 100, message = "O máximo de caracteres para o nome é 100.")
    String nome,
    
    @NotBlank(message = "O email é obrigatório!")
    @Email(message = "O email deve ter um formato válido!")
    String email,
    
    @NotBlank(message = "O CEP é obrigatório!")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "O CEP deve estar no formato XXXXX-XXX")
    String cep,
    
    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "O telefone deve estar no formato (XX) XXXXX-XXXX")
    String telefone
    
) {}
```

### DTO de Livro com Validações Avançadas
```java
public record LivroRequestDTO(
    
    @NotBlank(message = "O titulo do livro é obrigatório!")
    @Size(max = 100, message = "O máximo de caracteres é 100.")
    String titulo,
    
    @NotBlank(message = "O ISBN é obrigatório")
    @Size(min = 10, max = 17, message = "O mínimo de caracteres é 10 e o máximo é 17")
    @Pattern(regexp = "^[0-9-]+$", message = "O ISBN deve conter apenas números e hífens")
    String isbn,
    
    @NotNull(message = "A data de publicação é obrigatória!")
    @PastOrPresent(message = "A data de publicação não pode ser no futuro")
    LocalDate dataDePublicacao,
    
    @NotNull(message = "A categoria é obrigatória")
    Categoria categoria,
    
    @NotEmpty(message = "O livro deve ter pelo menos um autor associado")
    @Size(min = 1, message = "Deve haver pelo menos 1 autor")
    List<Long> autoresIds
    
) {}
```

---

## 🎮 USANDO VALIDAÇÕES NO CONTROLLER

### Ativando Validação Automática
```java
@RestController
@RequestMapping("/api/autores")
public class AutorController {

    @PostMapping
    public ResponseEntity<AutorResponseDTO> criar(
            @Valid @RequestBody AutorRequestDTO dto) {  // @Valid ativa validação
        
        // Se chegou aqui, todas as validações passaram
        AutorResponseDTO novoAutor = autorService.createAutor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAutor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AutorResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AutorRequestDTO dto) {  // @Valid também no PUT
        
        AutorResponseDTO autorAtualizado = autorService.updateAutor(id, dto);
        return ResponseEntity.ok(autorAtualizado);
    }
}
```

### Validando Path Variables
```java
@GetMapping("/{id}")
public ResponseEntity<AutorResponseDTO> buscarPorId(
        @PathVariable 
        @Min(value = 1, message = "ID deve ser maior que 0") 
        Long id) {
    
    AutorResponseDTO autor = autorService.findAutorById(id);
    return ResponseEntity.ok(autor);
}
```

### Validando Query Parameters
```java
@GetMapping
public ResponseEntity<Page<AutorResponseDTO>> listarPaginado(
        @RequestParam(defaultValue = "0") 
        @Min(value = 0, message = "Página deve ser >= 0") 
        int page,
        
        @RequestParam(defaultValue = "10") 
        @Min(value = 1, message = "Tamanho deve ser >= 1")
        @Max(value = 100, message = "Tamanho deve ser <= 100")
        int size) {
    
    Page<AutorResponseDTO> autores = autorService.findAllAutoresPaginados(page, size);
    return ResponseEntity.ok(autores);
}
```

---

## 🚨 TRATAMENTO DE ERROS DE VALIDAÇÃO

### GlobalExceptionHandler para Validações
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ERRO 400 - Validação de campos (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        log.warn("ERRO 400 - Validação: {}", ex.getMessage());

        // Extrair todos os erros de validação
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : fieldErrors) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            LocalDateTime.now(),
            400,
            "Dados inválidos. Verifique os campos.",
            errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * ERRO 400 - Validação de parâmetros (@PathVariable, @RequestParam)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex) {
        
        log.warn("ERRO 400 - Constraint: {}", ex.getMessage());

        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            400,
            message
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
```

### DTOs de Resposta de Erro
```java
public record ValidationErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    Map<String, String> fieldErrors  // Campo -> Mensagem de erro
) {}

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message
) {}
```

---

## 🔧 VALIDAÇÕES CUSTOMIZADAS

### Criando Anotação Customizada
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ISBNValidator.class)
@Documented
public @interface ValidISBN {
    String message() default "ISBN inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### Implementando Validador Customizado
```java
public class ISBNValidator implements ConstraintValidator<ValidISBN, String> {

    @Override
    public void initialize(ValidISBN constraintAnnotation) {
        // Inicialização se necessário
    }

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }

        // Remove hífens e espaços
        String cleanISBN = isbn.replaceAll("[\\s-]", "");

        // Verifica se tem 10 ou 13 dígitos
        if (cleanISBN.length() != 10 && cleanISBN.length() != 13) {
            return false;
        }

        // Verifica se são todos números (ISBN-10 pode ter X no final)
        if (cleanISBN.length() == 10) {
            return cleanISBN.matches("\\d{9}[\\dX]");
        } else {
            return cleanISBN.matches("\\d{13}");
        }
    }
}
```

### Usando Validação Customizada
```java
public record LivroRequestDTO(
    @NotBlank(message = "O título é obrigatório")
    String titulo,
    
    @ValidISBN(message = "ISBN deve ter formato válido (10 ou 13 dígitos)")
    String isbn,
    
    // outros campos...
) {}
```

---

## 🎯 VALIDAÇÕES POR GRUPOS

### Definindo Grupos de Validação
```java
public interface CreateGroup {}
public interface UpdateGroup {}

public record AutorRequestDTO(
    
    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    String nome,
    
    @Email(groups = {CreateGroup.class, UpdateGroup.class})
    String email,
    
    @NotNull(groups = CreateGroup.class)  // Só obrigatório na criação
    String senha
    
) {}
```

### Usando Grupos no Controller
```java
@PostMapping
public ResponseEntity<AutorResponseDTO> criar(
        @Validated(CreateGroup.class) @RequestBody AutorRequestDTO dto) {
    // Valida apenas regras do grupo CreateGroup
}

@PutMapping("/{id}")
public ResponseEntity<AutorResponseDTO> atualizar(
        @PathVariable Long id,
        @Validated(UpdateGroup.class) @RequestBody AutorRequestDTO dto) {
    // Valida apenas regras do grupo UpdateGroup
}
```

---

## 🔍 VALIDAÇÕES CONDICIONAIS

### Validação Baseada em Outro Campo
```java
@ValidConditional
public record LivroRequestDTO(
    
    @NotBlank
    String titulo,
    
    String isbn,
    
    @NotNull
    TipoLivro tipo,  // FISICO ou DIGITAL
    
    // Só obrigatório se tipo = FISICO
    String editora
    
) {}

// Validador customizado
public class ConditionalValidator implements ConstraintValidator<ValidConditional, LivroRequestDTO> {
    
    @Override
    public boolean isValid(LivroRequestDTO dto, ConstraintValidatorContext context) {
        if (dto.tipo() == TipoLivro.FISICO && 
            (dto.editora() == null || dto.editora().trim().isEmpty())) {
            
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Editora é obrigatória para livros físicos")
                   .addPropertyNode("editora")
                   .addConstraintViolation();
            return false;
        }
        return true;
    }
}
```

---

## 📱 VALIDAÇÕES NO FRONTEND

### JavaScript - Validação Client-Side
```javascript
function validateAutor(autor) {
    const errors = {};

    // Nome
    if (!autor.nome || autor.nome.trim().length < 2) {
        errors.nome = 'Nome deve ter pelo menos 2 caracteres';
    }
    if (autor.nome && autor.nome.length > 100) {
        errors.nome = 'Nome deve ter no máximo 100 caracteres';
    }

    // Email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!autor.email || !emailRegex.test(autor.email)) {
        errors.email = 'Email deve ter formato válido';
    }

    // CEP
    const cepRegex = /^\d{5}-\d{3}$/;
    if (!autor.cep || !cepRegex.test(autor.cep)) {
        errors.cep = 'CEP deve estar no formato XXXXX-XXX';
    }

    // Telefone
    const telefoneRegex = /^\(\d{2}\) \d{5}-\d{4}$/;
    if (autor.telefone && !telefoneRegex.test(autor.telefone)) {
        errors.telefone = 'Telefone deve estar no formato (XX) XXXXX-XXXX';
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors: errors
    };
}

// Uso
const validation = validateAutor(autorData);
if (!validation.isValid) {
    displayErrors(validation.errors);
    return;
}
```

### Máscaras de Input
```javascript
// Máscara para CEP
function applyCepMask(input) {
    input.addEventListener('input', (e) => {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 5) {
            value = value.substring(0, 5) + '-' + value.substring(5, 8);
        }
        e.target.value = value;
    });
}

// Máscara para telefone
function applyTelefoneMask(input) {
    input.addEventListener('input', (e) => {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 0) {
            if (value.length <= 2) {
                value = `(${value}`;
            } else if (value.length <= 7) {
                value = `(${value.substring(0, 2)}) ${value.substring(2)}`;
            } else {
                value = `(${value.substring(0, 2)}) ${value.substring(2, 7)}-${value.substring(7, 11)}`;
            }
        }
        e.target.value = value;
    });
}
```

---

## 🧪 TESTANDO VALIDAÇÕES

### Testes de Validação
```java
@SpringBootTest
class AutorValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveRejeitarNomeVazio() {
        AutorRequestDTO dto = new AutorRequestDTO("", "email@test.com", "12345-678", "(11) 99999-9999");
        
        Set<ConstraintViolation<AutorRequestDTO>> violations = validator.validate(dto);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
            .isEqualTo("O nome do autor é obrigatório!");
    }

    @Test
    void deveRejeitarEmailInvalido() {
        AutorRequestDTO dto = new AutorRequestDTO("João", "email-inválido", "12345-678", "(11) 99999-9999");
        
        Set<ConstraintViolation<AutorRequestDTO>> violations = validator.validate(dto);
        
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
            .isEqualTo("email");
    }

    @Test
    void deveAceitarDadosValidos() {
        AutorRequestDTO dto = new AutorRequestDTO("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
        
        Set<ConstraintViolation<AutorRequestDTO>> violations = validator.validate(dto);
        
        assertThat(violations).isEmpty();
    }
}
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Mensagens Claras e Específicas
```java
// ❌ Mensagem genérica
@NotBlank(message = "Campo obrigatório")

// ✅ Mensagem específica
@NotBlank(message = "O nome do autor é obrigatório!")
```

### 2. Validações Consistentes
```java
// ✅ Sempre usar os mesmos padrões
@Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve estar no formato XXXXX-XXX")
@Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
```

### 3. Validação em Múltiplas Camadas
```
Frontend (JavaScript) → Validação UX (imediata)
↓
Controller (@Valid) → Validação de entrada
↓
Service (Business Rules) → Validação de negócio
↓
Database (Constraints) → Validação de integridade
```

---

## 🎯 CHECKLIST VALIDAÇÕES

### Implementação
- [ ] DTOs com anotações de validação
- [ ] @Valid nos controllers
- [ ] GlobalExceptionHandler para erros de validação
- [ ] Mensagens de erro específicas e claras
- [ ] Validações customizadas se necessário

### Frontend
- [ ] Validação client-side implementada
- [ ] Máscaras de input aplicadas
- [ ] Feedback visual de erros
- [ ] Validação em tempo real (opcional)

### Testes
- [ ] Testes unitários de validação
- [ ] Testes de integração com dados inválidos
- [ ] Verificação de mensagens de erro
- [ ] Cobertura de todos os cenários de validação