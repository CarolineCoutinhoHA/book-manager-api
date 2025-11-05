package com.example.demo.dto;

import com.example.demo.model.Categoria;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record LivroRequestDTO (

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
        Categoria categoria,

        @NotEmpty(message = "O livro deve ter pelo menos um autor associado")
        List<Long> autoresIds
) {}
