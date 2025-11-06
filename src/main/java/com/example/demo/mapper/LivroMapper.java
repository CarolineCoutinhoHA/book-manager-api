package com.example.demo.mapper;

import com.example.demo.dto.LivroRequestDTO;
import com.example.demo.dto.LivroResponseDTO;
import com.example.demo.model.Livro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.stream.Collectors;

// @Mapper: Indica ao MapStruct para gerar a implementação automaticamente
// componentModel = "spring": Registra o Mapper como um Bean do Spring para que possa ser injetado (via @Autowired/@RequiredArgsConstructor)
@Mapper(componentModel = "spring")
public interface LivroMapper {

    // ====================================================================
    // Mapeamento de SAÍDA: ENTIDADE -> DTO DE RESPOSTA (toResponseDTO)
    // Este método é o "Garçom" que prepara o JSON de saída.
    // ====================================================================

    // Mapeamento 1: ENUM para String amigável
    // target: categoriaNome (no DTO de Resposta)
    // source: category.nomeExibicao (o valor amigável dentro da Entidade)
    @Mapping(target = "categoriaNome", source = "categoria.nomeExibicao")

    // Mapeamento 2: M:N (A Lógica Complexa)
    // target: autoresNome (no DTO de Resposta)
    // expression: Executa código Java 8 para transformar o Set<Autor> em List<String> (apenas nomes)
    @Mapping(target = "autoresNome", expression = "java(livro.getAutores().stream().map(a -> a.getNome()).collect(java.util.stream.Collectors.toList()))")
    LivroResponseDTO toResponseDTO(Livro livro);

    // ====================================================================
    // Mapeamento de CRIAÇÃO: DTO DE REQUISIÇÃO -> ENTIDADE (toEntity)
    // Este método cria uma nova instância de Livro.
    // ====================================================================

    // Mapeamento 3: Segurança de Identificadores (PK)
    // target: idLivro
    // ignore = true: Garante que o campo fique NULL para que o JPA/DB gere a chave primária.
    @Mapping(target = "idLivro", ignore = true)

    // Mapeamento 4: Segurança de Identificadores (UUID)
    // ignore = true: Garante que o campo fique NULL para que o @PrePersist no Model gere o UUID.
    @Mapping(target = "uuid", ignore = true)

    // Mapeamento 5: Gerenciamento M:N
    // ignore = true: A coleção de Autores (M:N) é ignorada aqui. Ela será preenchida manualmente no Service.
    @Mapping(target = "autores", ignore = true)
    Livro toEntity(LivroRequestDTO livroRequestDTO);

    // ====================================================================
    // Mapeamento de ATUALIZAÇÃO: DTO -> ENTIDADE EXISTENTE
    // Este método é usado para o PUT/UPDATE.
    // ====================================================================

    // Mapeamento 6: Segurança (PK, UUID, ISBN)
    // target: idLivro, uuid, isbn
    // ignore = true: Estes campos são IMUTÁVEIS e não devem ser alterados durante um update (RN).
    @Mapping(target = "idLivro", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "isbn", ignore = true)

    // Mapeamento 7: Coleção M:N
    // ignore = true: A lista de Autores não é alterada por esta operação de update.
    @Mapping(target = "autores", ignore = true)

    // @MappingTarget Livro livro: Indica que este é o objeto existente que será modificado.
    // O MapStruct usará os setters de domínio (setTitulo, setCategoria) que criamos.
    void updateEntityFromDto(LivroRequestDTO livroRequestDTO, @MappingTarget Livro livro);
}