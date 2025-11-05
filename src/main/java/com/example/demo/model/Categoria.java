package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

// O ENUM deve ser visível para o JPA e o Jackson
@Getter
public enum Categoria {

    FICCAO("Ficção"),
    NAO_FICCAO("Não Ficção"), // Corrigido o espaço para consistência
    CIENCIA("Ciência"),
    TECNOLOGIA("Tecnologia"),
    HISTORIA("História"); // Corrigida a acentuação

    private final String nomeExibicao;

    // Construtor: Chamado automaticamente para cada constante
    Categoria(String nomeExibicao){
        this.nomeExibicao = nomeExibicao;
    }

    // ----------------------------------------------------
    // TRATAMENTO JSON: ENTRADA (Desserialização)
    // ----------------------------------------------------

    /**
     * @JsonCreator: Usado pelo Jackson para converter uma String (do JSON) no objeto ENUM.
     * Trata case-insensitivity e garante que o valor não seja nulo.
     */
    @JsonCreator
    public static Categoria fromString(String valor) {
        if (valor == null || valor.trim().isEmpty()){
            throw new IllegalArgumentException("Categoria não pode ser nula ou vazia.");
        }
        try {
            // Converte para MAIÚSCULAS antes de usar valueOf, aceitando "ficcao", "FiCcao", etc.
            return Categoria.valueOf(valor.trim().toUpperCase());
        }
        catch (IllegalArgumentException e) {
            // Se falhar, lança uma exceção clara para o GlobalExceptionHandler capturar.
            throw new IllegalArgumentException("Valor de categoria inválido: '" + valor + "'.");
        }
    }

    // ----------------------------------------------------
    // TRATAMENTO JSON: SAÍDA (Serialização)
    // ----------------------------------------------------

    /**
     * @JsonValue: Define qual valor deve ser retornado no JSON.
     * Retorna o nome técnico da constante (Ex: "FICCAO").
     */
    @JsonValue
    public String toValue(){
        return name();
    }

    // O Getter para 'nomeExibicao' (getNomeExibicao()) já é gerado pelo @Getter

}