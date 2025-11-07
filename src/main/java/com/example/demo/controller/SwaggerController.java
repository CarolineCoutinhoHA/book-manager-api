package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SwaggerController {

    @GetMapping("/v3/api-docs-manual")
    public ResponseEntity<Map<String, Object>> getApiDocs() {
        Map<String, Object> apiDocs = Map.of(
            "openapi", "3.0.1",
            "info", Map.of(
                "title", "API de Livros e Autores",
                "version", "1.0.0"
            ),
            "paths", Map.of(
                "/api/auth/register", Map.of(
                    "post", Map.of(
                        "summary", "Registrar usuário",
                        "requestBody", Map.of(
                            "content", Map.of(
                                "application/json", Map.of(
                                    "schema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                            "username", Map.of("type", "string"),
                                            "password", Map.of("type", "string")
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                "/api/auth/login", Map.of(
                    "post", Map.of(
                        "summary", "Fazer login",
                        "requestBody", Map.of(
                            "content", Map.of(
                                "application/json", Map.of(
                                    "schema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                            "username", Map.of("type", "string"),
                                            "password", Map.of("type", "string")
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                "/api/autores", Map.of(
                    "get", Map.of("summary", "Listar autores"),
                    "post", Map.of("summary", "Criar autor")
                ),
                "/api/livros", Map.of(
                    "get", Map.of("summary", "Listar livros"),
                    "post", Map.of("summary", "Criar livro")
                )
            )
        );
        
        return ResponseEntity.ok(apiDocs);
    }
}