# 🗺️ MAPSTRUCT & DTOs - GUIA COMPLETO

## 🎯 O QUE É MAPSTRUCT?

MapStruct é uma biblioteca que gera automaticamente código para mapear entre diferentes tipos de objetos (DTOs ↔ Entities).

### Por que usar?
- ✅ **Performance** - Código gerado em compile-time (não reflection)
- ✅ **Type-safe** - Erros detectados em tempo de compilação
- ✅ **Automático** - Mapeia campos com mesmo nome automaticamente
- ✅ **Customizável** - Permite mapeamentos complexos
- ✅ **Integração Spring** - Funciona como Bean do Spring

---

## 📦 PADRÃO DTO (DATA TRANSFER OBJECT)

### Por que usar DTOs?
```
┌─────────────────┐    DTO Request     ┌─────────────────┐
│   FRONTEND      │ ──────────────────> │   CONTROLLER    │
│                 │                     │                 │
│                 │ <────────────────── │                 │
└─────────────────┘    DTO Response     └─────────────────┘
                                                 │
                                                 │ Entity
                                                 ▼
                                        ┌─────────────────┐
                                        │    SERVICE      │
                                        │                 │
                                        └─────────────────┘
```

### Vantagens dos DTOs
- **Segurança** - Não expõe estrutura interna das entities
- **Flexibilidade** - Diferentes representações para diferentes endpoints
- **Versionamento** - Mudanças na API sem afetar entities
- **Performance** - Controla quais dados são transferidos
- **Validação** - Validações específicas por operação

---

## 🏗️ ESTRUTURA DE DTOs

### DTO de Requisição (Request)
```java
/**
 * DTO para RECEBER dados do frontend
 * Usado em POST e PUT
 */
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
    
) {
    // Records são imutáveis e geram automaticamente:
    // - Construtor com todos os parâmetros
    // - Getters (nome(), email(), etc.)
    // - equals(), hashCode(), toString()
}
```

### DTO de Resposta (Response)
```java
/**
 * DTO para ENVIAR dados para o frontend
 * Usado em respostas de GET, POST, PUT
 */
public record AutorResponseDTO(
    
    Long idAutor,           // ID para o frontend usar em operações
    String nome,            // Dados básicos
    String email,
    String cep,
    String telefone
    
    // NÃO inclui:
    // - UUID (interno)
    // - disponibilidade (regra de negócio interna)
    // - senha (se houvesse)
    // - timestamps de auditoria
    
) {}
```

### DTO Complexo com Relacionamentos
```java
/**
 * DTO de Livro com informações dos autores
 */
public record LivroResponseDTO(
    
    Long idLivro,
    String titulo,
    String isbn,
    LocalDate dataDePublicacao,
    String categoriaNome,           // Nome amigável da categoria (não o enum)
    List<String> autoresNome        // Lista com nomes dos autores (não IDs)
    
) {}

/**
 * DTO de Livro para criação/atualização
 */
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
    Categoria categoria,            // Enum direto (MapStruct converte automaticamente)
    
    @NotEmpty(message = "O livro deve ter pelo menos um autor associado")
    List<Long> autoresIds          // IDs dos autores para associar
    
) {}
```

---

## 🗺️ MAPSTRUCT - CONFIGURAÇÃO

### Dependências (já configuradas)
```gradle
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
```

### Mapper Básico
```java
/**
 * MAPPER SIMPLES - Mapeamento automático
 * 
 * MapStruct gera automaticamente a implementação
 * baseada nos nomes dos campos
 */
@Mapper(componentModel = "spring")  // Registra como Bean do Spring
public interface AutorMapper {

    // Entity → Response DTO (para GET)
    AutorResponseDTO toResponseDTO(Autor autor);
    
    // Request DTO → Entity (para POST)
    Autor toEntity(AutorRequestDTO autorRequestDTO);
    
    // Request DTO → Entity existente (para PUT)
    void updateEntityFromDto(AutorRequestDTO autorRequestDTO, @MappingTarget Autor autor);
    
    // Lista de Entities → Lista de DTOs
    List<AutorResponseDTO> toResponseDTOList(List<Autor> autores);
}
```

---

## 🔧 MAPSTRUCT - MAPEAMENTOS AVANÇADOS

### Mapper Complexo com Customizações
```java
@Mapper(componentModel = "spring")
public interface LivroMapper {

    // ====================================================================
    // MAPEAMENTO DE SAÍDA: ENTIDADE → DTO DE RESPOSTA
    // ====================================================================
    
    // Mapeamento 1: ENUM → String amigável
    @Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")
    
    // Mapeamento 2: Relacionamento M:N → Lista de Strings
    @Mapping(target = "autoresNome", 
             expression = "java(livro.getAutores().stream().map(a -> a.getNome()).collect(java.util.stream.Collectors.toList()))")
    LivroResponseDTO toResponseDTO(Livro livro);

    // ====================================================================
    // MAPEAMENTO DE CRIAÇÃO: DTO → ENTIDADE
    // ====================================================================
    
    // Ignorar campos que não devem ser mapeados
    @Mapping(target = "idLivro", ignore = true)     // PK gerada pelo banco
    @Mapping(target = "uuid", ignore = true)        // UUID gerado pelo @PrePersist
    @Mapping(target = "autores", ignore = true)     // Relacionamento gerenciado pelo Service
    Livro toEntity(LivroRequestDTO livroRequestDTO);

    // ====================================================================
    // MAPEAMENTO DE ATUALIZAÇÃO: DTO → ENTIDADE EXISTENTE
    // ====================================================================
    
    @Mapping(target = "idLivro", ignore = true)     // PK imutável
    @Mapping(target = "uuid", ignore = true)        // UUID imutável
    @Mapping(target = "isbn", ignore = true)        // ISBN imutável (regra de negócio)
    @Mapping(target = "autores", ignore = true)     // Relacionamento não alterado aqui
    void updateEntityFromDto(LivroRequestDTO livroRequestDTO, @MappingTarget Livro livro);
}
```

### Mapeamentos com Métodos Customizados
```java
@Mapper(componentModel = "spring")
public interface AutorMapper {

    // Mapeamento padrão
    AutorResponseDTO toResponseDTO(Autor autor);
    
    // Mapeamento com formatação customizada
    @Mapping(target = "nomeCompleto", source = "autor", qualifiedByName = "formatarNomeCompleto")
    @Mapping(target = "telefoneFormatado", source = "telefone", qualifiedByName = "formatarTelefone")
    AutorDetalhadoDTO toDetalhadoDTO(Autor autor);
    
    // Método customizado para formatação
    @Named("formatarNomeCompleto")
    default String formatarNomeCompleto(Autor autor) {
        return autor.getNome().toUpperCase() + " (" + autor.getEmail() + ")";
    }
    
    @Named("formatarTelefone")
    default String formatarTelefone(String telefone) {
        if (telefone == null || telefone.isEmpty()) {
            return "Não informado";
        }
        return telefone;
    }
}
```

---

## 🔄 USANDO MAPPERS NO SERVICE

### Service com MapStruct
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AutorService {

    private final AutorRepository autorRepository;
    private final AutorMapper autorMapper;  // Injeção do mapper

    /**
     * CREATE - Usando mapper para conversão
     */
    @Transactional
    public AutorResponseDTO createAutor(AutorRequestDTO dto) {
        log.info("Criando autor: {}", dto.nome());

        // 1. Validações de negócio
        validateUniqueEmail(dto.email());

        // 2. DTO → Entity (MapStruct)
        Autor novoAutor = autorMapper.toEntity(dto);

        // 3. Salvar no banco
        Autor autorSalvo = autorRepository.save(novoAutor);

        // 4. Entity → DTO Response (MapStruct)
        return autorMapper.toResponseDTO(autorSalvo);
    }

    /**
     * READ - Lista com conversão automática
     */
    public List<AutorResponseDTO> findAllAutores() {
        log.info("Listando todos os autores");

        List<Autor> autores = autorRepository.findAll().stream()
                .filter(Autor::isDisponibilidade)
                .collect(Collectors.toList());

        // Lista de Entities → Lista de DTOs (MapStruct)
        return autorMapper.toResponseDTOList(autores);
    }

    /**
     * UPDATE - Usando @MappingTarget
     */
    @Transactional
    public AutorResponseDTO updateAutor(Long id, AutorRequestDTO dto) {
        log.info("Atualizando autor ID: {}", id);

        // 1. Buscar entidade existente
        Autor autorExistente = findAutorOrThrow(id);

        // 2. Validar email único (se mudou)
        if (!autorExistente.getEmail().equals(dto.email())) {
            validateUniqueEmail(dto.email());
        }

        // 3. DTO → Entity existente (MapStruct atualiza campos)
        autorMapper.updateEntityFromDto(dto, autorExistente);

        // 4. Salvar alterações
        Autor autorAtualizado = autorRepository.save(autorExistente);

        // 5. Entity → DTO Response
        return autorMapper.toResponseDTO(autorAtualizado);
    }
}
```

---

## 🧩 MAPSTRUCT - CASOS ESPECIAIS

### Mapeamento com Diferentes Nomes de Campos
```java
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(source = "nomeCompleto", target = "nome")
    @Mapping(source = "enderecoEmail", target = "email")
    @Mapping(source = "dataNascimento", target = "nascimento")
    UsuarioDTO toDTO(Usuario usuario);
}
```

### Mapeamento com Conversões de Tipo
```java
@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    @Mapping(source = "preco", target = "precoFormatado", qualifiedByName = "formatarPreco")
    @Mapping(source = "categoria", target = "categoriaNome", qualifiedByName = "categoriaToString")
    ProdutoDTO toDTO(Produto produto);

    @Named("formatarPreco")
    default String formatarPreco(BigDecimal preco) {
        return "R$ " + preco.toString().replace(".", ",");
    }

    @Named("categoriaToString")
    default String categoriaToString(Categoria categoria) {
        return categoria.getNomeExibicao();
    }
}
```

### Mapeamento Condicional
```java
@Mapper(componentModel = "spring")
public interface ContaMapper {

    @Mapping(source = "saldo", target = "saldoExibicao", qualifiedByName = "formatarSaldo")
    ContaDTO toDTO(Conta conta);

    @Named("formatarSaldo")
    default String formatarSaldo(BigDecimal saldo) {
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            return "Saldo negativo";
        }
        return "R$ " + saldo.toString();
    }
}
```

---

## 🔍 MAPSTRUCT GERADO - ENTENDENDO O CÓDIGO

### Implementação Gerada Automaticamente
```java
// Arquivo gerado em: build/generated/sources/annotationProcessor/java/main/
@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-15T10:30:00-0300"
)
@Component  // Registrado como Bean do Spring
public class AutorMapperImpl implements AutorMapper {

    @Override
    public AutorResponseDTO toResponseDTO(Autor autor) {
        if (autor == null) {
            return null;
        }

        // MapStruct gera código otimizado (sem reflection)
        Long idAutor = autor.getIdAutor();
        String nome = autor.getNome();
        String email = autor.getEmail();
        String cep = autor.getCep();
        String telefone = autor.getTelefone();

        AutorResponseDTO autorResponseDTO = new AutorResponseDTO(
            idAutor, nome, email, cep, telefone
        );

        return autorResponseDTO;
    }

    @Override
    public Autor toEntity(AutorRequestDTO autorRequestDTO) {
        if (autorRequestDTO == null) {
            return null;
        }

        // Usa construtor da Entity
        Autor autor = new Autor(
            autorRequestDTO.nome(),
            autorRequestDTO.email(),
            autorRequestDTO.cep(),
            autorRequestDTO.telefone()
        );

        return autor;
    }

    @Override
    public void updateEntityFromDto(AutorRequestDTO autorRequestDTO, Autor autor) {
        if (autorRequestDTO == null) {
            return;
        }

        // Usa setters da Entity
        autor.setNome(autorRequestDTO.nome());
        autor.setEmail(autorRequestDTO.email());
        autor.setCep(autorRequestDTO.cep());
        autor.setTelefone(autorRequestDTO.telefone());
    }
}
```

---

## 🧪 TESTANDO MAPPERS

### Testes Unitários de Mapper
```java
@SpringBootTest
class AutorMapperTest {

    @Autowired
    private AutorMapper autorMapper;

    @Test
    void deveConverterEntityParaResponseDTO() {
        // Given
        Autor autor = new Autor("João Silva", "joao@email.com", "12345-678", "(11) 99999-9999");
        autor.setIdAutor(1L);

        // When
        AutorResponseDTO dto = autorMapper.toResponseDTO(autor);

        // Then
        assertThat(dto.idAutor()).isEqualTo(1L);
        assertThat(dto.nome()).isEqualTo("João Silva");
        assertThat(dto.email()).isEqualTo("joao@email.com");
        assertThat(dto.cep()).isEqualTo("12345-678");
        assertThat(dto.telefone()).isEqualTo("(11) 99999-9999");
    }

    @Test
    void deveConverterRequestDTOParaEntity() {
        // Given
        AutorRequestDTO dto = new AutorRequestDTO(
            "Maria Santos", "maria@email.com", "54321-876", "(21) 88888-8888"
        );

        // When
        Autor autor = autorMapper.toEntity(dto);

        // Then
        assertThat(autor.getNome()).isEqualTo("Maria Santos");
        assertThat(autor.getEmail()).isEqualTo("maria@email.com");
        assertThat(autor.getCep()).isEqualTo("54321-876");
        assertThat(autor.getTelefone()).isEqualTo("(21) 88888-8888");
        assertThat(autor.getIdAutor()).isNull(); // Não deve ser mapeado
    }

    @Test
    void deveAtualizarEntityExistente() {
        // Given
        Autor autorExistente = new Autor("Nome Antigo", "antigo@email.com", "00000-000", "(00) 00000-0000");
        autorExistente.setIdAutor(1L);

        AutorRequestDTO dto = new AutorRequestDTO(
            "Nome Novo", "novo@email.com", "11111-111", "(11) 11111-1111"
        );

        // When
        autorMapper.updateEntityFromDto(dto, autorExistente);

        // Then
        assertThat(autorExistente.getIdAutor()).isEqualTo(1L); // ID não deve mudar
        assertThat(autorExistente.getNome()).isEqualTo("Nome Novo");
        assertThat(autorExistente.getEmail()).isEqualTo("novo@email.com");
        assertThat(autorExistente.getCep()).isEqualTo("11111-111");
        assertThat(autorExistente.getTelefone()).isEqualTo("(11) 11111-1111");
    }
}
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Estrutura Consistente de DTOs
```java
// Padrão para todos os recursos
{Recurso}RequestDTO   // Para POST/PUT
{Recurso}ResponseDTO  // Para GET/POST/PUT responses
{Recurso}ListDTO      // Para listagens (se diferente do Response)
{Recurso}DetailDTO    // Para detalhes completos (se necessário)
```

### 2. Validações Específicas por Operação
```java
// DTO para criação - todos os campos obrigatórios
public record AutorCreateDTO(
    @NotBlank String nome,
    @Email String email,
    @NotBlank String cep,
    String telefone
) {}

// DTO para atualização - campos opcionais
public record AutorUpdateDTO(
    String nome,        // Opcional
    String email,       // Opcional
    String cep,         // Opcional
    String telefone     // Opcional
) {}
```

### 3. DTOs Específicos para Diferentes Contextos
```java
// DTO básico para listagens
public record AutorListDTO(
    Long id,
    String nome,
    String email
) {}

// DTO completo para detalhes
public record AutorDetailDTO(
    Long id,
    String nome,
    String email,
    String cep,
    String telefone,
    List<LivroBasicDTO> livros  // Livros do autor
) {}
```

---

## 🎯 CHECKLIST MAPSTRUCT & DTOs

### DTOs
- [ ] RequestDTO com validações para cada operação
- [ ] ResponseDTO com dados necessários para frontend
- [ ] Records em vez de classes (mais conciso)
- [ ] Validações específicas e mensagens claras
- [ ] Campos opcionais bem definidos

### MapStruct
- [ ] @Mapper(componentModel = "spring") configurado
- [ ] Mapeamentos básicos funcionando
- [ ] @Mapping para campos com nomes diferentes
- [ ] @MappingTarget para updates
- [ ] Campos ignorados corretamente (@Mapping(ignore = true))
- [ ] Métodos customizados para conversões complexas

### Integração
- [ ] Mappers injetados nos Services
- [ ] Conversões DTO ↔ Entity em todos os CRUDs
- [ ] Relacionamentos mapeados corretamente
- [ ] Performance adequada (sem N+1 queries)

### Testes
- [ ] Testes unitários dos mappers
- [ ] Verificação de todos os campos mapeados
- [ ] Testes de campos ignorados
- [ ] Testes de mapeamentos customizados