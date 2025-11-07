# MapStruct - Guia de Mapeamento Entre Camadas

## Visão Geral

MapStruct é uma biblioteca que gera automaticamente código de mapeamento entre DTOs e Entidades, eliminando código boilerplate e reduzindo erros manuais.

## Configuração

### Dependência Maven
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

## Estrutura do Mapper

### Anotações Principais
- `@Mapper(componentModel = "spring")`: Registra como Bean do Spring
- `@Mapping`: Define mapeamentos customizados
- `@MappingTarget`: Indica objeto existente para atualização

### Exemplo: LivroMapper
```java
@Mapper(componentModel = "spring")
public interface LivroMapper {
    
    // Mapeamento de saída (Entidade -> DTO)
    @Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")
    @Mapping(target = "autoresNome", expression = "java(livro.getAutores().stream().map(a -> a.getNome()).collect(java.util.stream.Collectors.toList()))")
    LivroResponseDTO toResponseDTO(Livro livro);
    
    // Mapeamento de criação (DTO -> Entidade)
    @Mapping(target = "idLivro", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "autores", ignore = true)
    Livro toEntity(LivroRequestDTO dto);
    
    // Mapeamento de atualização (DTO -> Entidade existente)
    @Mapping(target = "idLivro", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    @Mapping(target = "autores", ignore = true)
    void updateEntityFromDto(LivroRequestDTO dto, @MappingTarget Livro livro);
}
```

## Tipos de Mapeamento

### 1. Mapeamento Simples
Campos com mesmo nome são mapeados automaticamente:
```java
// Automático: dto.titulo -> entity.titulo
```

### 2. Mapeamento com Source/Target
Para campos com nomes diferentes:
```java
@Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")
```

### 3. Mapeamento com Expression
Para lógica complexa:
```java
@Mapping(target = "autoresNome", 
         expression = "java(livro.getAutores().stream().map(a -> a.getNome()).collect(java.util.stream.Collectors.toList()))")
```

### 4. Campos Ignorados
Para segurança e controle:
```java
@Mapping(target = "idLivro", ignore = true)  // PK gerada pelo DB
@Mapping(target = "uuid", ignore = true)     // UUID gerado no @PrePersist
```

## Padrões de Segurança

### Proteção de Identificadores
```java
// CRIAÇÃO: Ignora IDs para que sejam gerados automaticamente
@Mapping(target = "idLivro", ignore = true)
@Mapping(target = "uuid", ignore = true)

// ATUALIZAÇÃO: Ignora IDs para evitar alteração acidental
@Mapping(target = "idLivro", ignore = true)
@Mapping(target = "uuid", ignore = true)
@Mapping(target = "isbn", ignore = true)  // ISBN imutável
```

### Relacionamentos Complexos
```java
// M:N gerenciado manualmente no Service
@Mapping(target = "autores", ignore = true)
```

## Integração com Spring

### Injeção no Service
```java
@Service
@RequiredArgsConstructor
public class LivroService {
    
    private final LivroMapper livroMapper;  // Injetado automaticamente
    
    public LivroResponseDTO criarLivro(LivroRequestDTO dto) {
        Livro livro = livroMapper.toEntity(dto);
        // ... lógica de negócio
        return livroMapper.toResponseDTO(livro);
    }
}
```

## Vantagens do MapStruct

### 1. Performance
- Código gerado em tempo de compilação
- Sem reflexão em runtime
- Performance similar ao código manual

### 2. Segurança de Tipos
- Erros detectados em tempo de compilação
- Refatoração segura
- IDE com autocomplete

### 3. Manutenibilidade
- Menos código boilerplate
- Mapeamentos centralizados
- Fácil de testar

## Debugging

### Código Gerado
O MapStruct gera implementações em `target/generated-sources/annotations/`:
```java
@Component
public class LivroMapperImpl implements LivroMapper {
    
    @Override
    public LivroResponseDTO toResponseDTO(Livro livro) {
        // Código gerado automaticamente
    }
}
```

### Logs de Compilação
Para ver o que está sendo gerado:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>1.5.5.Final</version>
            </path>
        </annotationProcessorPaths>
        <compilerArgs>
            <compilerArg>-Amapstruct.verbose=true</compilerArg>
        </compilerArgs>
    </configuration>
</plugin>
```

## Casos de Uso Comuns

### ENUM para String
```java
@Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")
```

### Coleções
```java
@Mapping(target = "autoresNome", 
         expression = "java(entity.getAutores().stream().map(Autor::getNome).toList())")
```

### Formatação de Datas
```java
@Mapping(target = "dataFormatada", 
         expression = "java(entity.getData().format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")))")
```

## Boas Práticas

1. **Sempre use componentModel = "spring"** para integração
2. **Ignore IDs em criação/atualização** para segurança
3. **Use expressions para lógica complexa** ao invés de métodos auxiliares
4. **Centralize mapeamentos** em interfaces dedicadas
5. **Teste os mapeamentos** especialmente os complexos
6. **Documente expressions** para facilitar manutenção