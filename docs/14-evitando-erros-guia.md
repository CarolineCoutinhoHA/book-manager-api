# Guia Completo - Como Evitar Erros Comuns

## 🎯 Visão Geral

Este guia documenta TODOS os erros que corrigimos hoje e como evitá-los no futuro. É um manual de boas práticas baseado em problemas reais.

## 🚨 Erros de Compilação

### 1. Erro de Parâmetros em Records/DTOs

#### ❌ ERRO QUE TIVEMOS:
```java
// Record espera 2 parâmetros
public record NomeAutorDTO(Long idAutor, String nome) {}

// Mas estava sendo chamado com apenas 1
.map(autor -> new NomeAutorDTO(autor.getNome()))  // ❌ ERRO!
```

#### ✅ SOLUÇÃO:
```java
// Sempre passe TODOS os parâmetros do record
.map(autor -> new NomeAutorDTO(autor.getIdAutor(), autor.getNome()))  // ✅ CORRETO
```

#### 🛡️ COMO EVITAR:
1. **Sempre verifique a assinatura** do record/construtor
2. **Use IDE com autocomplete** (IntelliJ/VSCode)
3. **Compile frequentemente** durante desenvolvimento
4. **Leia mensagens de erro** com atenção

### 2. Erro de Tipos Genéricos

#### ❌ ERRO QUE TIVEMOS:
```java
// Erro: inference variable T has incompatible bounds
.collect(Collectors.toList());
```

#### ✅ SOLUÇÃO:
```java
// Especificar tipo explicitamente quando necessário
.collect(Collectors.toList());  // Funciona quando tipos estão corretos
```

#### 🛡️ COMO EVITAR:
1. **Mantenha consistência de tipos** na stream
2. **Use var com cuidado** - prefira tipos explícitos
3. **Teste streams simples** antes de complexificar

## 🗄️ Erros de JPA/Hibernate

### 3. Erro de Relacionamento Many-to-Many

#### ❌ ERRO QUE TIVEMOS:
```java
// Hibernate: Cannot invoke "org.hibernate.mapping.ToOne.getReferencedEntityName()" because "toOne" is null
```

#### 🔍 CAUSA:
```java
// mappedBy apontando para campo inexistente
@ManyToMany(mappedBy = "livrosSet")  // ❌ Campo não existe na outra entidade
```

#### ✅ SOLUÇÃO:
```java
// LADO DONO (quem tem @JoinTable)
@ManyToMany
@JoinTable(
    name = "autor_livro",
    joinColumns = @JoinColumn(name = "id_livro"),
    inverseJoinColumns = @JoinColumn(name = "id_autor")
)
private Set<Autor> autores;

// LADO INVERSO (quem tem mappedBy)
@ManyToMany(mappedBy = "autores")  // ✅ Aponta para campo correto
private Set<Livro> livrosSet;
```

#### 🛡️ COMO EVITAR:
1. **Defina claramente** quem é o lado dono
2. **mappedBy sempre aponta** para o nome do campo na outra entidade
3. **Apenas UM lado** tem @JoinTable
4. **Teste relacionamentos** com dados simples primeiro
5. **Use nomes consistentes** para campos de relacionamento

### 4. Regras para Relacionamentos Bidirecionais

#### ✅ PADRÃO CORRETO:
```java
// ENTIDADE A (lado dono)
@ManyToMany
@JoinTable(
    name = "tabela_juncao",
    joinColumns = @JoinColumn(name = "id_a"),
    inverseJoinColumns = @JoinColumn(name = "id_b")
)
private Set<EntidadeB> entidadesB;

// ENTIDADE B (lado inverso)
@ManyToMany(mappedBy = "entidadesB")  // Nome do campo em EntidadeA
private Set<EntidadeA> entidadesA;
```

#### 🛡️ REGRAS DE OURO:
1. **Apenas um lado** tem @JoinTable (lado dono)
2. **mappedBy** sempre no lado inverso
3. **mappedBy** aponta para nome do campo, não da tabela
4. **Sincronize ambos os lados** nos métodos de domínio
5. **Use @ToString.Exclude** para evitar loops infinitos

## 🌐 Erros de Configuração Web

### 5. Erro de CORS

#### ❌ ERRO COMUM:
```javascript
// Frontend: Access to fetch blocked by CORS policy
```

#### ✅ SOLUÇÃO:
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")                    // Endpoints da API
        .allowedOrigins("http://localhost:3000")  // Frontend específico
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("*");
}
```

#### 🛡️ COMO EVITAR:
1. **Configure CORS** desde o início
2. **Nunca use allowedOrigins("*")** em produção
3. **Teste com frontend real** cedo no desenvolvimento
4. **Use URLs específicas** para cada ambiente

### 6. Erro de Porta Ocupada

#### ❌ ERRO QUE TIVEMOS:
```
Port 8080 was already in use
```

#### ✅ SOLUÇÕES:
```bash
# Encontrar processo na porta
netstat -ano | findstr :8080

# Matar processo
taskkill /PID NUMERO_PID /F

# Ou usar porta diferente
server.port=8081
```

#### 🛡️ COMO EVITAR:
1. **Sempre pare** aplicações anteriores
2. **Use Ctrl+C** para parar graciosamente
3. **Configure portas diferentes** para projetos diferentes
4. **Use scripts** para gerenciar processos

## 🔐 Erros de Segurança/JWT

### 7. Erro 403 Forbidden

#### ❌ ERRO COMUM:
```
403 Forbidden - Access Denied
```

#### 🔍 CAUSAS POSSÍVEIS:
1. **Token ausente** no header
2. **Token malformado** (sem "Bearer ")
3. **Token expirado**
4. **Endpoint não liberado** no SecurityConfig

#### ✅ SOLUÇÕES:
```java
// 1. Verificar SecurityConfig
.requestMatchers("/api/auth/**").permitAll()

// 2. Formato correto do header
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

// 3. Verificar expiração
public Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

#### 🛡️ COMO EVITAR:
1. **Teste endpoints públicos** primeiro
2. **Implemente logs** no filtro JWT
3. **Use Postman** para testar headers
4. **Configure tempo de expiração** adequado
5. **Trate erros** de token graciosamente

## 📝 Erros de Validação

### 8. Erro de Bean Validation

#### ❌ ERRO COMUM:
```java
// Validation failed: email deve ter formato válido
```

#### ✅ PADRÕES CORRETOS:
```java
@NotBlank(message = "Nome é obrigatório")
@Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
private String nome;

@Email(message = "Email deve ter formato válido")
@NotBlank(message = "Email é obrigatório")
private String email;

@Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP deve ter formato XXXXX-XXX")
private String cep;

@Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Telefone deve ter formato (XX) XXXXX-XXXX")
private String telefone;
```

#### 🛡️ COMO EVITAR:
1. **Use regex simples** e testadas
2. **Teste validações** com dados reais
3. **Mensagens claras** para o usuário
4. **Valide no frontend** também
5. **Use @Valid** nos controllers

## 🗃️ Erros de Banco de Dados

### 9. Erro de Constraint Violation

#### ❌ ERRO COMUM:
```
Unique index or primary key violation
```

#### ✅ TRATAMENTO:
```java
try {
    autorRepository.save(autor);
} catch (DataIntegrityViolationException e) {
    if (e.getMessage().contains("email")) {
        throw new BusinessException("Email já está em uso");
    }
    throw new BusinessException("Erro de integridade dos dados");
}
```

#### 🛡️ COMO EVITAR:
1. **Valide unicidade** antes de salvar
2. **Trate exceções** específicas
3. **Use constraints** adequadas no banco
4. **Teste com dados duplicados**

### 10. Erro de LazyInitializationException

#### ❌ ERRO COMUM:
```
LazyInitializationException: could not initialize proxy
```

#### ✅ SOLUÇÕES:
```java
// 1. Use @ToString.Exclude em relacionamentos
@ManyToMany(mappedBy = "autores")
@ToString.Exclude
private Set<Livro> livros;

// 2. Ou configure fetch EAGER (cuidado com performance)
@ManyToMany(fetch = FetchType.EAGER)

// 3. Ou use @Transactional no método que acessa
@Transactional(readOnly = true)
public void metodoQueAcessaRelacionamento() {
    // código que acessa relacionamentos lazy
}
```

#### 🛡️ COMO EVITAR:
1. **Use @ToString.Exclude** em relacionamentos
2. **Configure fetch** adequadamente
3. **Use @Transactional** quando necessário
4. **Evite acessar** relacionamentos fora de transação

## 🔧 Erros de Configuração

### 11. Erro de Bean Not Found

#### ❌ ERRO COMUM:
```
No qualifying bean of type 'com.example.Service' available
```

#### ✅ SOLUÇÕES:
```java
// 1. Verificar anotações
@Service  // ou @Component, @Repository
public class MinhaService {
}

// 2. Verificar scan de pacotes
@SpringBootApplication
@ComponentScan(basePackages = "com.example")  // Se necessário

// 3. Verificar injeção
@RequiredArgsConstructor  // Lombok
private final MinhaService service;

// Ou
@Autowired
private MinhaService service;
```

#### 🛡️ COMO EVITAR:
1. **Use anotações corretas** (@Service, @Repository, etc.)
2. **Mantenha estrutura** de pacotes organizada
3. **Use @RequiredArgsConstructor** do Lombok
4. **Teste injeção** com classes simples primeiro

## 📊 Erros de Mapeamento (MapStruct)

### 12. Erro de Mapeamento de Campos

#### ❌ ERRO COMUM:
```java
// Campo não mapeado ou mapeamento incorreto
```

#### ✅ PADRÕES CORRETOS:
```java
@Mapper(componentModel = "spring")
public interface AutorMapper {
    
    // Mapeamento automático (nomes iguais)
    AutorResponseDTO toResponseDTO(Autor autor);
    
    // Mapeamento com @Mapping para nomes diferentes
    @Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")
    LivroResponseDTO toResponseDTO(Livro livro);
    
    // Ignorar campos perigosos
    @Mapping(target = "idAutor", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    Autor toEntity(AutorRequestDTO dto);
}
```

#### 🛡️ COMO EVITAR:
1. **Ignore IDs** em criação/atualização
2. **Use source/target** para campos diferentes
3. **Teste mapeamentos** com dados reais
4. **Verifique código gerado** em target/generated-sources

## 🧪 Erros de Teste

### 13. Erro de Contexto de Teste

#### ❌ ERRO COMUM:
```java
// Testes em src/main/java não funcionam
```

#### ✅ ESTRUTURA CORRETA:
```
src/
├── main/java/          # Código de produção
└── test/java/          # Código de teste
    └── com/example/demo/
        ├── AutorServiceTest.java
        └── AutorControllerTest.java
```

#### 🛡️ COMO EVITAR:
1. **Sempre coloque testes** em src/test/java
2. **Use anotações corretas** (@Test, @ExtendWith)
3. **Mantenha estrutura** espelhada
4. **Teste uma coisa** por vez

## 📋 Checklist de Prevenção de Erros

### Antes de Compilar:
- [ ] Verificar assinaturas de métodos/construtores
- [ ] Conferir imports necessários
- [ ] Validar anotações JPA
- [ ] Verificar relacionamentos bidirecionais

### Antes de Testar:
- [ ] Configurar CORS adequadamente
- [ ] Verificar SecurityConfig
- [ ] Testar endpoints públicos primeiro
- [ ] Validar formato de tokens JWT

### Antes de Fazer Deploy:
- [ ] Testar com dados reais
- [ ] Verificar constraints de banco
- [ ] Validar tratamento de erros
- [ ] Testar cenários de falha

## 🎯 Metodologia de Debug

### 1. Leia a Mensagem de Erro Completa
```
❌ "Deu erro"
✅ "Cannot invoke getReferencedEntityName() because toOne is null"
```

### 2. Identifique a Categoria do Erro
- **Compilação**: Erro de sintaxe/tipos
- **Runtime**: Erro de lógica/configuração
- **HTTP**: Erro de rede/autenticação

### 3. Use Logs Estratégicos
```java
log.info("Iniciando operação X com parâmetro: {}", parametro);
log.debug("Estado intermediário: {}", objeto);
log.error("Erro na operação X: {}", e.getMessage(), e);
```

### 4. Teste Isoladamente
- **Teste uma coisa** por vez
- **Use dados simples** primeiro
- **Remova complexidade** até funcionar

### 5. Consulte Documentação
- **Spring Boot Reference**
- **JPA/Hibernate docs**
- **Stack Overflow** para erros específicos

## 🚀 Boas Práticas Gerais

### 1. Desenvolvimento Incremental
```java
// ✅ Faça assim:
// 1. Crie entidade simples
// 2. Teste CRUD básico
// 3. Adicione relacionamentos
// 4. Adicione validações
// 5. Adicione segurança

// ❌ Não faça tudo de uma vez
```

### 2. Testes Frequentes
```bash
# Compile frequentemente
./gradlew compileJava

# Teste frequentemente
./gradlew test

# Execute frequentemente
./gradlew bootRun
```

### 3. Versionamento Inteligente
```bash
# Commit pequenos e frequentes
git add .
git commit -m "feat: adiciona entidade Autor"

# Não espere tudo funcionar para commitar
```

### 4. Documentação Contínua
```java
// ✅ Documente enquanto desenvolve
/**
 * Cria novo autor no sistema.
 * Valida email único antes de salvar.
 */
public AutorResponseDTO criarAutor(AutorRequestDTO dto) {
    // implementação
}
```

## 🎯 Resumo Final

**Os erros mais comuns são:**
1. **Relacionamentos JPA** mal configurados
2. **Parâmetros incorretos** em construtores/métodos
3. **Configuração de segurança** inadequada
4. **Validações** mal implementadas
5. **Estrutura de projeto** incorreta

**Para evitá-los:**
1. **Compile frequentemente**
2. **Teste incrementalmente**
3. **Leia mensagens de erro**
4. **Use ferramentas adequadas** (IDE, Postman)
5. **Siga padrões estabelecidos**

**Lembre-se:** Erros são normais no desenvolvimento. O importante é aprender com eles e criar sistemas para evitá-los no futuro! 🚀