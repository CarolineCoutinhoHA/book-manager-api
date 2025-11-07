package com.example.demo.controller;

import com.example.demo.DemoApplication;
import com.example.demo.model.Autor;
import com.example.demo.repository.AutorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

// Configura o ambiente de teste de integração
@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class LivroControllerTest {

    @Autowired private MockMvc mockMvc; // Simula requisições HTTP
    @Autowired private ObjectMapper objectMapper; // Converte JSON
    @Autowired private AutorRepository autorRepository;

    private Long authorId;
    private final String API_URL = "/api/livros";

    // Executado antes de cada teste para garantir que sempre tenhamos um autor válido no DB
    @BeforeEach
    void setUp() {
        Autor autor = new Autor("Base Author", "base@mail.com", "12345-678", "(11) 98765-4321");
        Autor savedAutor = autorRepository.save(autor);
        authorId = savedAutor.getIdAutor(); // Pega o ID (Long) do autor base
    }

    // Helper para criar JSON de Livro (usando o ID base)
    private String getValidJson(String isbn, Long... ids) {
        String authorsList = java.util.Arrays.stream(ids)
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));

        return String.format("""
            {
                "titulo": "Livro Teste",
                "isbn": "%s",
                "dataDePublicacao": "2020-01-01",
                "categoria": "CIENCIA",
                "autoresIds": [%s]
            }
        """, isbn, authorsList);
    }

    // ====================================================
    // TESTES DE SUCESSO (Criação e M:N)
    // ====================================================

    @Test
    @Transactional
    public void deveCriarUmLivroComSucessoEAssociarAutor() throws Exception {
        // ARRANGE: JSON com o ID do Autor base
        String json = getValidJson("978-0123456789", authorId);

        // ACT & ASSERT: Criação via POST
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) // Espera 201 Created
                .andExpect(jsonPath("$.titulo").value("Livro Teste"))
                .andExpect(jsonPath("$.autoresNome.length()").value(1)); // Verifica se a associação M:N funcionou
    }

    // ====================================================
    // TESTES DE FALHA (RNs)
    // ====================================================

    @Test
    @Transactional
    public void deveLancar400AoCriarComIsbnDuplicado() throws Exception {
        // ARRANGE: Cria um livro base para duplicar o ISBN
        String isbnDuplicado = "978-0123456788";

        // 1. Cria o primeiro livro
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getValidJson(isbnDuplicado, authorId)))
                .andExpect(status().isCreated());

        // 2. Tenta criar o segundo com o mesmo ISBN
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getValidJson(isbnDuplicado, authorId)))
                .andExpect(status().isBadRequest()) // Espera 400
                .andExpect(jsonPath("$.message").value(containsString("ISBN978-0123456788 já registrado")));
    }

    @Test
    @Transactional
    public void deveLancar404AoAssociarAutorInexistente() throws Exception {
        // ARRANGE: Tenta associar um ID que não existe (999L)
        String json = getValidJson("978-0123456787", 999L);

        // ACT & ASSERT: Espera 404 (ResourceNotFoundException - RN M:N)
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound()) // Espera 404
                .andExpect(jsonPath("$.message").value(containsString("IDs de autores não foram encontrados")));
    }
}