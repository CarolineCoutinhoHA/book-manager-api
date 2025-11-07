package com.example.demo.mapper;

import com.example.demo.dto.AutorRequestDTO;
import com.example.demo.dto.AutorResponseDTO;
import com.example.demo.dto.NomeAutorDTO;
import com.example.demo.model.Autor;
import org.springframework.stereotype.Component;

@Component
public class AutorMapper {

    public AutorResponseDTO toResponseDTO(Autor autor) {
        return new AutorResponseDTO(
            autor.getIdAutor(),
            autor.getNome(),
            autor.getEmail(),
            autor.getCep(),
            autor.getTelefone()
        );
    }

    public NomeAutorDTO toNomeAutorDTO(Autor autor) {
        return new NomeAutorDTO(autor.getIdAutor(), autor.getNome());
    }

    public Autor toEntity(AutorRequestDTO dto) {
        Autor autor = new Autor();
        autor.setNome(dto.nome());
        autor.setEmail(dto.email());
        autor.setTelefone(dto.telefone());
        autor.setCep(dto.cep());
        return autor;
    }

    public void updateEntityFromDto(AutorRequestDTO dto, Autor autor) {
        autor.setNome(dto.nome());
        autor.setEmail(dto.email());
        autor.setTelefone(dto.telefone());
        autor.setCep(dto.cep());
    }
}