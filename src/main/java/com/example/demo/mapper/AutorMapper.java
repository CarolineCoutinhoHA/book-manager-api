package com.example.demo.mapper;

import com.example.demo.dto.AutorResponseDTO;
import com.example.demo.dto.AutorRequestDTO; // Usando o nome correto
import com.example.demo.model.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.stream.Collectors; // Necessário para a documentação complexa no comentário

// @Mapper: Indica ao MapStruct para gerar a implementação automaticamente
// componentModel = "spring": Registra o Mapper como um Bean do Spring
@Mapper(componentModel = "spring")
public interface AutorMapper {

    /**
     * Responsável por garantir que a saída da aplicação seja segura.
     * Pega a entidade autor e filtra apenas o que é seguro e público (nome, email, uuid),
     * esconde o idAutor (chave interna) e a lista gigante de livroSet.
     * Realiza a tradução do que está na entidade para o DTO de Resposta.
     * Da ENTIDADE AUTOR -> método toResponse -> AutorResponseDTO.
     *
     * Regra para Mapeamento (@Mapping):
     * target = o destino -- campo no DTO de Resposta (AutorResponseDTO)
     * source = campo de origem -- campo na ENTIDADE (Autor)
     */
    AutorResponseDTO toResponseDTO(Autor autor);


    // ====================================================================
    // 2. Mapeamento de CRIAÇÃO: DTO DE REQUISIÇÃO -> ENTIDADE (toEntity)
    // ====================================================================

    // Ignora campos gerados (ID, UUID) e a coleção (M:N).
    @Mapping(target = "idAutor", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "livrosSet", ignore = true)
    Autor toEntity(AutorRequestDTO dto);

    // ====================================================================
    // 3. Mapeamento de ATUALIZAÇÃO: DTO -> ENTIDADE EXISTENTE
    // ====================================================================

    // Ignora campos imutáveis (ID, UUID) e a coleção M:N (Segurança).
    @Mapping(target = "idAutor", ignore = true)
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "livrosSet", ignore = true)

    // @MappingTarget Autor autor: O objeto Autor existente que será modificado.
    void updateEntityFromDto(AutorRequestDTO dto, @MappingTarget Autor autor);
}