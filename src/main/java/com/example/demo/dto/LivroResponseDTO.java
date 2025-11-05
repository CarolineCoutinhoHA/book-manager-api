package com.example.demo.dto;

import com.example.demo.model.Categoria;

import java.time.LocalDate;
import java.util.List;

public record LivroResponseDTO (

        Long idLivro,
        String titulo,
        String isbn,
        LocalDate dataDePublicacao,
        String categoriaNome,
        List<String> autoresNome

) {}
