package com.example.demo.dto;

public record AutorResponseDTO(

        Long idAutor,
        String nome,
        String email,
        String cep,
        String telefone
) {}
