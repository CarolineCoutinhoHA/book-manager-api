package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * Record (DTO) usado para formatar a resposta JSON em caso de erro.
 * Garante que o cliente receba uma mensagem padronizada com detalhes técnicos.
 */
public record ErrorResponse(
        // Momento exato em que o erro ocorreu
        LocalDateTime timestamp,

        // Código de status HTTP (Ex: 400, 404)
        int status,

        // Mensagem detalhada do erro (Ex: "ISBN já registrado.")
        String message
) {}