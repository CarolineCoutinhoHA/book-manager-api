package com.example.demo.mapper;

import com.example.demo.dto.LivroRequestDTO;
import com.example.demo.dto.LivroResponseDTO;
import com.example.demo.model.Livro;
import org.springframework.stereotype.Component;

@Component
public class LivroMapper {

    public LivroResponseDTO toResponseDTO(Livro livro) {
        return new LivroResponseDTO(
            livro.getIdLivro(),
            livro.getTitulo(),
            livro.getIsbn(),
            livro.getDataDePublicacao(),
            null, // será preenchido no service
            null  // será preenchido no service
        );
    }

    public Livro toEntity(LivroRequestDTO dto) {
        Livro livro = new Livro();
        livro.setTitulo(dto.titulo());
        livro.setIsbn(dto.isbn());
        livro.setDataDePublicacao(dto.dataDePublicacao());
        livro.setCategoria(dto.categoria());
        return livro;
    }

    public void updateEntityFromDto(LivroRequestDTO dto, Livro livro) {
        livro.setTitulo(dto.titulo());
        livro.setDataDePublicacao(dto.dataDePublicacao());
        livro.setCategoria(dto.categoria());
    }
}