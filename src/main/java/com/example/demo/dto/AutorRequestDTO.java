package com.example.demo.dto;

import jakarta.validation.constraints.*;

public record AutorRequestDTO(

        @NotBlank(message = "O nome do autor é obrigatório!")
        @Size(min = 1, max = 100, message = "O nome deve ter no mínimo um caracter e no máximo 100 ")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório!")
        @Email(message = "O formato do e-mail é inválido.")
        String email,

        @NotBlank(message = "O CEP é obrigatório!")
        @Pattern(regexp = "^\\\\d{5}-?\\\\d{3}$", message = "O CEP deve estar no fromato XXXXX-XXX")
        String cep,

        @Pattern(regexp = "^\\\\(\\\\d{2}\\\\)\\\\s\\\\d{4,5}-\\\\d{4}$", message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
        String telefone

)
{ }
