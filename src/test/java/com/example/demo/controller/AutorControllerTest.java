package com.example.demo.controller;

import com.example.demo.DemoApplication;
import com.example.demo.model.Autor;
import com.example.demo.repository.AutorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

// Configura o ambiente de teste para carregar o contexto Spring e o Controller
@SpringBootTest(classes = DemoApplication.class)
// AutoConfigureMockMvc: Configura o MockMvc para simular requisições HTTP
@AutoConfigureMockMvc
public class AutorControllerTest {

    @Autowired private MockMvc mockMvc; // Ferramenta para simular requisições
    @Autowired private ObjectMapper objectMapper; // Para converter JSON
    @Autowired private AutorRepository autorRepository; // Para preparar o DB

    private final String API_URL = "/api/autores";

    // Helper para criar JSON de Autor (simula o DTO Request)
    private String getValidJson(String email) {
        return String.format("""
            {
                "nome": "Autor Teste",
                "email": "%s",
                "cep": "12345-678",
                "telefone": "(11) 98765-4321"
            }
        """, email);
    }

    // ====================================================
    // TESTES DE SUCESSO (CRUD BÁSICO E SOFT DELETE)
    // ====================================================

    @Test
    @Transactional
    public void deveCriarUmAutorEBuscarPorId() throws Exception {
        // 1. ARRANGE & ACT: Criação via POST
        String json = getValidJson("autor@teste.com");

        var result = mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated()) // Espera 201 Created
                .andReturn();

        // Extrai o ID (Long) do Autor criado
        String responseBody = result.getResponse().getContentAsString();
        Long idAutor = objectMapper.readTree(responseBody).get("idAutor").asLong();

        // 2. ASSERT: Busca via GET
        mockMvc.perform(get(API_URL + "/" + idAutor))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Autor Teste"));
    }

    @Test
    @Transactional
    public void deveListarAutoresComPaginacao() throws Exception {
        // ARRANGE: Cria 5 autores
        for(int i = 0; i < 5; i++) {
            // Cria a entidade diretamente no DB para performance do teste
            Autor autor = new Autor("Autor " + i, "email" + i + "@test.com", "12345-678", "(11) 98765-4321");
            autorRepository.save(autor);
        }

        // ACT & ASSERT: Busca a lista completa (com Paginação, mas sem parâmetros)
        mockMvc.perform(get(API_URL).param("page", "0").param("size", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3)) // Verifica se a página tem 3 elementos
                .andExpect(jsonPath("$.totalPages").value(2)); // Total de 5 autores / size 3 = 2 páginas
    }

    @Test
    @Transactional
    public void deveFazerSoftDeleteEFiltrarNaBusca() throws Exception {
        // ARRANGE: Cria 1 autor ativo
        Autor autor = new Autor("Autor Ativo", "ativo@mail.com", "12345-678", "(11) 98765-4321");
        Autor savedAutor = autorRepository.save(autor);
        Long idAutor = savedAutor.getIdAutor();

        // ACT 1: Soft Delete (DELETE)
        mockMvc.perform(delete(API_URL + "/" + idAutor))
                .andExpect(status().isNoContent()); // Espera 204

        // ACT 2: Tenta listar todos os autores (deve vir 0)
        mockMvc.perform(get(API_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0)); // Verifica se o filtro Soft Delete funcionou
    }

    // ====================================================
    // TESTES DE FALHA (RNs e Exceções)
    // ====================================================

    @Test
    @Transactional
    public void deveLancar400AoCriarComEmailInvalido() throws Exception {
        // ARRANGE: E-mail inválido (RN do DTO - @Email)
        String json = """
            {
                "nome": "Nome Sobrenome",
                "email": "email-invalido", 
                "cep": "12345-678",
                "telefone": "(11) 98765-4321"
            }
        """;

        // ACT & ASSERT: Espera 400 Bad Request
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest()) // Verifica o 400 do GlobalExceptionHandler
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Transactional
    public void deveLancar400AoCriarComEmailDuplicado() throws Exception {
        // ARRANGE: Cria o primeiro autor
        autorRepository.save(new Autor("Autor Base", "duplicado@mail.com", "12345-678", "(11) 98765-4321"));

        // ACT & ASSERT: Tenta criar o segundo com o mesmo email
        String jsonDuplicado = getValidJson("duplicado@mail.com");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDuplicado))
                .andExpect(status().isBadRequest()) // Espera 400 (BusinessException)
                .andExpect(jsonPath("$.message").value(containsString("E-mail 'duplicado@mail.com' já registrado")));
    }
}