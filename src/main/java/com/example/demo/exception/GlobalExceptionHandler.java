package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.demo.dto.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice // Habilita a captura de exceções em todos os @Controllers
@Slf4j// Para logar os erros no console
public class GlobalExceptionHandler {

    // ====================================================================
    // 1. ERROS DE VALIDAÇÃO DE DTO (@Valid, @NotBlank, @Email) - HTTP 400
    // ====================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        // Coleta todos os erros de campo e os junta em uma única string clara
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.error("ERRO 400 - Validação de DTO: {}", errorMessage);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // 400
                "Dados de entrada inválidos: " + errorMessage
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ====================================================================
    // 2. ERROS DE REGRA DE NEGÓCIO (ISBN Duplicado, Estoque Insuficiente) - HTTP 400
    // ====================================================================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("ERRO 400 - Regra de Negócio: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // 400
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // ====================================================================
    // 3. ERROS DE PERSISTÊNCIA/INTEGRIDADE (UNIQUE constraint violation) - HTTP 409
    // ====================================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // Ex: Tentativa de inserir um email ou ISBN que já existe no DB (UNIQUE=true)

        String logMessage = "Conflito de Integridade: " + (ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage());
        log.warn("ERRO 409 - Conflito DB: {}", logMessage);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(), // 409 CONFLICT
                "Conflito de dados. Verifique campos únicos ou relações obrigatórias."
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // ====================================================================
    // 4. RECURSO NÃO ENCONTRADO (ID Inexistente) - HTTP 404
    // ====================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("ERRO 404 - Recurso não encontrado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // ====================================================================
    // 5. ERRO INTERNO GENÉRICO (Catch-All) - HTTP 500
    // ====================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        // Logamos o stack trace completo com 'ex' para a equipe de DevOps,
        // mas devolvemos uma mensagem genérica e segura para o cliente.
        log.error("ERRO 500 - Não mapeado. StackTrace: ", ex);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                "Ocorreu um erro interno inesperado no servidor. Tente novamente mais tarde."
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}