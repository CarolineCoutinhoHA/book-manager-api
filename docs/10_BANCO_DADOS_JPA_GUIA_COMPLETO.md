# 🗄️ BANCO DE DADOS & JPA - GUIA COMPLETO

## 🎯 ARQUITETURA DE DADOS

### Stack de Persistência
```
┌─────────────────┐
│   APPLICATION   │  ← Service Layer
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   SPRING DATA   │  ← Repository Interface
│      JPA        │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   HIBERNATE     │  ← ORM Implementation
│     (JPA)       │
└─────────────────┘
         │
         ▼
┌─────────────────┐
│   H2 DATABASE   │  ← In-Memory Database
│   (Development) │
└─────────────────┘
```

### Configuração Atual
- **Database**: H2 (in-memory para desenvolvimento)
- **ORM**: Hibernate (implementação JPA)
- **Connection Pool**: HikariCP (padrão Spring Boot)
- **DDL**: create-drop (recria tabelas a cada execução)

---

## ⚙️ CONFIGURAÇÃO DO BANCO

### application.properties
```properties
# ===== CONFIGURAÇÃO H2 DATABASE =====
spring.datasource.url=jdbc:h2:mem:bookdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (para visualizar dados)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ===== CONFIGURAÇÃO JPA/HIBERNATE =====
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ===== CONFIGURAÇÃO DE LOGGING =====
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### Explicação das Configurações
```properties
# DATASOURCE
spring.datasource.url=jdbc:h2:mem:bookdb    # Banco em memória chamado "bookdb"
spring.datasource.username=sa               # Usuário padrão H2
spring.datasource.password=                 # Sem senha

# DDL (Data Definition Language)
spring.jpa.hibernate.ddl-auto=create-drop   # Recria tabelas a cada execução
# Opções:
# - create: Cria tabelas (apaga dados existentes)
# - create-drop: Cria tabelas e apaga ao finalizar
# - update: Atualiza schema (preserva dados)
# - validate: Apenas valida schema
# - none: Não faz nada

# LOGS
spring.jpa.show-sql=true                    # Mostra SQL no console
spring.jpa.properties.hibernate.format_sql=true  # Formata SQL legível
```

---

## 🏗️ ENTIDADES JPA

### Entidade Básica - Autor
```java
@Entity                                    // 1. Marca como entidade JPA
@Table(name = "autor")                     // 2. Nome da tabela no banco
@Getter                                    // 3. Lombok: getters automáticos
@NoArgsConstructor                         // 4. Construtor vazio (obrigatório JPA)
@ToString(onlyExplicitlyIncluded = true)   // 5. ToString customizado
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // 6. Equals/HashCode customizado
public class Autor {

    // ===== IDENTIFICADORES =====
    
    @Id                                           // Chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento pelo banco
    @Column(name = "id_autor")                    // Nome da coluna customizado
    @ToString.Include                             // Incluir no toString()
    private Long idAutor;

    @EqualsAndHashCode.Include                    // Usar no equals/hashCode
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    // ===== CAMPOS DE DADOS =====
    
    @Column(name = "nome", nullable = false)      // NOT NULL
    @ToString.Include
    private String nome;

    @Column(name = "email", nullable = false, unique = true)  // NOT NULL + UNIQUE
    @ToString.Include
    private String email;

    @Column(name = "cep", nullable = false)
    private String cep;

    @Column(name = "telefone")                    // Nullable (opcional)
    private String telefone;

    @Column(name = "disponibilidade", nullable = false)
    private boolean disponibilidade = true;       // Valor padrão

    // ===== RELACIONAMENTOS =====
    
    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    @ToString.Exclude                             // Evita LazyInitializationException
    private final Set<Livro> livrosSet = new HashSet<>();

    // ===== CICLO DE VIDA =====
    
    @PrePersist                                   // Antes de salvar pela primeira vez
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    // ===== CONSTRUTORES =====
    
    public Autor(String nome, String email, String cep, String telefone) {
        this.nome = nome;
        this.email = email;
        this.cep = cep;
        this.telefone = telefone;
    }

    // ===== MÉTODOS DE DOMÍNIO =====
    
    public void disponivel() {
        this.disponibilidade = true;
    }

    public void indisponivel() {
        this.disponibilidade = false;
    }
}
```

### Entidade com Relacionamentos - Livro
```java
@Entity
@Table(name = "livro")
@Getter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livro")
    @ToString.Include
    private Long idLivro;

    @EqualsAndHashCode.Include
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "titulo", nullable = false)
    @ToString.Include
    private String titulo;

    @Column(name = "isbn", nullable = false, unique = true)
    @ToString.Include
    private String isbn;

    @Column(name = "data_de_publicacao", nullable = false)
    @ToString.Include
    private LocalDate dataDePublicacao;

    // ===== ENUM MAPEADO =====
    
    @Enumerated(EnumType.ORDINAL)              // Salva como número (0,1,2,3,4)
    @Column(name = "categoria", nullable = false)
    @ToString.Include
    private Categoria categoria;

    @Column(name = "disponibilidade", nullable = false)
    private boolean disponibilidade = true;

    // ===== RELACIONAMENTO MANY-TO-MANY =====
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "livro-autor",                     // Nome da tabela de junção
        joinColumns = @JoinColumn(name = "livro_id"),        // FK para Livro
        inverseJoinColumns = @JoinColumn(name = "autor_id")  // FK para Autor
    )
    @ToString.Exclude
    private final Set<Autor> autores = new HashSet<>();

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public Livro(String titulo, String isbn, LocalDate dataDePublicacao, Categoria categoria) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.dataDePublicacao = dataDePublicacao;
        this.categoria = categoria;
    }

    // ===== MÉTODOS DE RELACIONAMENTO =====
    
    public void adicionarAutor(Autor autor) {
        this.autores.add(autor);                  // Adiciona autor ao livro
        autor.atribuirLivroAoAutor(this);         // Sincroniza bidirecional
    }
}
```

---

## 🔗 TIPOS DE RELACIONAMENTOS

### One-to-Many (1:N)
```java
// Lado "One" (Categoria)
@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    
    // Um categoria tem muitos livros
    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Livro> livros = new ArrayList<>();
}

// Lado "Many" (Livro)
@Entity
public class Livro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Muitos livros pertencem a uma categoria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}
```

### Many-to-Many (N:N)
```java
// Lado "Owner" (Livro - gerencia a tabela de junção)
@Entity
public class Livro {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "livro_autor",                           // Tabela de junção
        joinColumns = @JoinColumn(name = "livro_id"),   // FK para esta entidade
        inverseJoinColumns = @JoinColumn(name = "autor_id")  // FK para outra entidade
    )
    private Set<Autor> autores = new HashSet<>();
}

// Lado "Inverse" (Autor - referencia o mapeamento)
@Entity
public class Autor {
    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    private Set<Livro> livros = new HashSet<>();
}
```

### One-to-One (1:1)
```java
// Lado "Owner"
@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;
}

// Lado "Inverse"
@Entity
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "perfil", fetch = FetchType.LAZY)
    private Usuario usuario;
}
```

---

## 📊 REPOSITORY - SPRING DATA JPA

### Repository Básico
```java
@Repository  // Opcional (JpaRepository já é um componente)
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // ===== MÉTODOS HERDADOS AUTOMATICAMENTE =====
    
    // CREATE/UPDATE
    <S extends Autor> S save(S entity);
    <S extends Autor> List<S> saveAll(Iterable<S> entities);
    
    // READ
    Optional<Autor> findById(Long id);
    List<Autor> findAll();
    List<Autor> findAllById(Iterable<Long> ids);
    Page<Autor> findAll(Pageable pageable);
    
    // DELETE
    void deleteById(Long id);
    void delete(Autor entity);
    void deleteAll();
    
    // UTILITY
    boolean existsById(Long id);
    long count();
}
```

### Query Methods (Convenção de Nomenclatura)
```java
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // ===== BUSCA SIMPLES =====
    Optional<Autor> findByEmail(String email);
    List<Autor> findByNome(String nome);
    List<Autor> findByDisponibilidade(boolean disponibilidade);
    
    // ===== BUSCA COM LIKE =====
    List<Autor> findByNomeContaining(String nome);              // WHERE nome LIKE %nome%
    List<Autor> findByNomeContainingIgnoreCase(String nome);    // LIKE %nome% (case insensitive)
    List<Autor> findByNomeStartingWith(String prefixo);         // LIKE prefixo%
    List<Autor> findByNomeEndingWith(String sufixo);            // LIKE %sufixo
    
    // ===== BUSCA COM COMPARAÇÃO =====
    List<Autor> findByIdAutorGreaterThan(Long id);             // WHERE id > ?
    List<Autor> findByIdAutorLessThan(Long id);                // WHERE id < ?
    List<Autor> findByIdAutorBetween(Long inicio, Long fim);   // WHERE id BETWEEN ? AND ?
    
    // ===== BUSCA COM NULL =====
    List<Autor> findByTelefoneIsNull();                        // WHERE telefone IS NULL
    List<Autor> findByTelefoneIsNotNull();                     // WHERE telefone IS NOT NULL
    
    // ===== BUSCA COM BOOLEAN =====
    List<Autor> findByDisponibilidadeTrue();                   // WHERE disponibilidade = true
    List<Autor> findByDisponibilidadeFalse();                  // WHERE disponibilidade = false
    
    // ===== BUSCA COM MÚLTIPLOS CAMPOS =====
    Optional<Autor> findByNomeAndEmail(String nome, String email);
    List<Autor> findByNomeOrEmail(String nome, String email);
    
    // ===== ORDENAÇÃO =====
    List<Autor> findByDisponibilidadeTrueOrderByNomeAsc();
    List<Autor> findAllByOrderByNomeDesc();
    
    // ===== LIMITAÇÃO =====
    List<Autor> findTop5ByDisponibilidadeTrueOrderByNomeAsc();
    Autor findFirstByOrderByIdAutorDesc();
}
```

### Queries Customizadas com @Query
```java
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // ===== JPQL (Java Persistence Query Language) =====
    
    @Query("SELECT a FROM Autor a WHERE a.disponibilidade = true")
    List<Autor> findAutoresAtivos();
    
    @Query("SELECT a FROM Autor a WHERE a.nome LIKE %:nome% AND a.disponibilidade = true")
    List<Autor> findAutoresAtivosPorNome(@Param("nome") String nome);
    
    @Query("SELECT a FROM Autor a JOIN a.livrosSet l WHERE l.categoria = :categoria")
    List<Autor> findAutoresPorCategoriaLivro(@Param("categoria") Categoria categoria);
    
    // ===== SQL NATIVO =====
    
    @Query(value = "SELECT * FROM autor WHERE nome LIKE %?1% AND disponibilidade = true", 
           nativeQuery = true)
    List<Autor> findAutoresAtivosPorNomeNativo(String nome);
    
    @Query(value = """
        SELECT a.* FROM autor a 
        JOIN livro_autor la ON a.id_autor = la.autor_id 
        JOIN livro l ON la.livro_id = l.id_livro 
        WHERE l.categoria = ?1
        """, nativeQuery = true)
    List<Autor> findAutoresPorCategoriaLivroNativo(int categoria);
    
    // ===== QUERIES DE MODIFICAÇÃO =====
    
    @Modifying
    @Query("UPDATE Autor a SET a.disponibilidade = false WHERE a.idAutor = :id")
    int softDeleteAutor(@Param("id") Long id);
    
    @Modifying
    @Query("DELETE FROM Autor a WHERE a.disponibilidade = false")
    int deleteAutoresInativos();
}
```

---

## 🔄 TRANSAÇÕES

### @Transactional Explicado
```java
@Service
@Transactional(readOnly = true)  // Padrão: todas as operações são read-only
public class AutorService {

    // ===== TRANSAÇÃO READ-ONLY (padrão da classe) =====
    public List<AutorResponseDTO> findAllAutores() {
        // Operação de leitura - usa transação read-only
        // Otimização: Hibernate não precisa fazer dirty checking
    }

    // ===== TRANSAÇÃO DE ESCRITA =====
    @Transactional  // Sobrescreve readOnly = true
    public AutorResponseDTO createAutor(AutorRequestDTO dto) {
        // Operação de escrita - usa transação completa
        // Se houver exceção, faz rollback automático
    }

    // ===== TRANSAÇÃO COM PROPAGAÇÃO CUSTOMIZADA =====
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperacao(String operacao) {
        // Sempre cria nova transação (independente da transação pai)
        // Útil para logs que devem ser salvos mesmo se operação principal falhar
    }

    // ===== TRANSAÇÃO COM ROLLBACK CUSTOMIZADO =====
    @Transactional(rollbackFor = {BusinessException.class, ValidationException.class})
    public void operacaoComRollbackCustomizado() {
        // Por padrão, só faz rollback para RuntimeException
        // Com rollbackFor, também faz rollback para exceções específicas
    }
}
```

### Propagação de Transações
```java
public enum Propagation {
    REQUIRED,      // Usa transação existente ou cria nova (padrão)
    REQUIRES_NEW,  // Sempre cria nova transação
    SUPPORTS,      // Usa transação existente se houver
    NOT_SUPPORTED, // Executa fora de transação
    MANDATORY,     // Exige transação existente (erro se não houver)
    NEVER,         // Não pode haver transação (erro se houver)
    NESTED         // Cria subtransação (savepoint)
}
```

---

## 📈 PERFORMANCE E OTIMIZAÇÃO

### Fetch Types
```java
// ===== LAZY LOADING (padrão para coleções) =====
@OneToMany(fetch = FetchType.LAZY)
private List<Livro> livros;  // Carregado apenas quando acessado

// ===== EAGER LOADING =====
@ManyToOne(fetch = FetchType.EAGER)
private Categoria categoria;  // Carregado junto com a entidade principal

// ===== PROBLEMA N+1 =====
// Problema: Para cada autor, faz query separada para buscar livros
List<Autor> autores = autorRepository.findAll();
for (Autor autor : autores) {
    autor.getLivros().size();  // N queries adicionais!
}

// Solução: Join Fetch
@Query("SELECT DISTINCT a FROM Autor a LEFT JOIN FETCH a.livros")
List<Autor> findAllWithLivros();
```

### Paginação
```java
@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // Paginação automática
    Page<Autor> findByDisponibilidadeTrue(Pageable pageable);
    
    // Paginação com query customizada
    @Query("SELECT a FROM Autor a WHERE a.nome LIKE %:nome%")
    Page<Autor> findByNomeContaining(@Param("nome") String nome, Pageable pageable);
}

// Uso no Service
@Service
public class AutorService {
    
    public Page<AutorResponseDTO> findAutoresPaginados(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Autor> autoresPage = autorRepository.findByDisponibilidadeTrue(pageable);
        return autoresPage.map(autorMapper::toResponseDTO);
    }
}
```

### Projeções (DTOs diretos do banco)
```java
// Interface Projection
public interface AutorProjection {
    Long getIdAutor();
    String getNome();
    String getEmail();
}

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    // Retorna apenas os campos necessários (mais eficiente)
    @Query("SELECT a.idAutor as idAutor, a.nome as nome, a.email as email FROM Autor a")
    List<AutorProjection> findAutoresBasicos();
    
    // Class-based Projection
    @Query("SELECT new com.example.demo.dto.AutorBasicoDTO(a.idAutor, a.nome, a.email) FROM Autor a")
    List<AutorBasicoDTO> findAutoresBasicosDTO();
}
```

---

## 🔍 DEBUGGING E MONITORAMENTO

### Logs SQL Detalhados
```properties
# Mostrar SQL formatado
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Mostrar parâmetros das queries
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Mostrar estatísticas de performance
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG

# Mostrar cache de segundo nível
logging.level.org.hibernate.cache=DEBUG
```

### Exemplo de Log SQL
```sql
-- Query gerada pelo Hibernate
Hibernate: 
    select
        autor0_.id_autor as id_autor1_0_,
        autor0_.cep as cep2_0_,
        autor0_.disponibilidade as disponib3_0_,
        autor0_.email as email4_0_,
        autor0_.nome as nome5_0_,
        autor0_.telefone as telefone6_0_,
        autor0_.uuid as uuid7_0_ 
    from
        autor autor0_ 
    where
        autor0_.disponibilidade=?

-- Parâmetros
2024-01-15 10:30:00.123 TRACE --- [main] o.h.type.descriptor.sql.BasicBinder : binding parameter [1] as [BOOLEAN] - [true]
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Configuração Simples para Desenvolvimento
```properties
# H2 para desenvolvimento (rápido, sem configuração)
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### 2. Entidades Bem Estruturadas
```java
@Entity
@Table(name = "nome_tabela")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MinhaEntidade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Campos com constraints apropriadas
    @Column(nullable = false, unique = true)
    private String campoUnico;
    
    // Relacionamentos com fetch apropriado
    @ManyToOne(fetch = FetchType.LAZY)
    private OutraEntidade relacao;
}
```

### 3. Repository Eficiente
```java
public interface MinhaRepository extends JpaRepository<MinhaEntidade, Long> {
    
    // Query methods para casos simples
    List<MinhaEntidade> findByStatus(Status status);
    
    // @Query para casos complexos
    @Query("SELECT e FROM MinhaEntidade e WHERE e.campo = :valor")
    List<MinhaEntidade> findCustom(@Param("valor") String valor);
}
```

---

## 🎯 CHECKLIST BANCO DE DADOS

### Configuração
- [ ] H2 configurado corretamente
- [ ] Console H2 habilitado (/h2-console)
- [ ] Logs SQL habilitados para debug
- [ ] DDL create-drop para desenvolvimento

### Entidades
- [ ] @Entity e @Table configurados
- [ ] @Id e @GeneratedValue para PKs
- [ ] @Column com constraints apropriadas
- [ ] Relacionamentos mapeados corretamente
- [ ] @PrePersist para inicializações
- [ ] Lombok para reduzir boilerplate

### Repository
- [ ] Extends JpaRepository<Entity, Long>
- [ ] Query methods seguindo convenções
- [ ] @Query para casos complexos
- [ ] Paginação implementada onde necessário

### Performance
- [ ] FetchType.LAZY para relacionamentos
- [ ] Projeções para queries específicas
- [ ] @Transactional apropriado
- [ ] Evitar N+1 queries