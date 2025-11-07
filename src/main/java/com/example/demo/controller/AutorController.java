package com.example.demo.controller;

import com.example.demo.dto.AutorRequestDTO;
import com.example.demo.dto.AutorResponseDTO;
import com.example.demo.dto.NomeAutorDTO;
import com.example.demo.service.AutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER DE AUTORES - CAMADA DE APRESENTAÇÃO
 * 
 * Este controller é responsável por:
 * 1. Receber requisições HTTP do frontend
 * 2. Validar dados de entrada (@Valid)
 * 3. Chamar a camada de serviço (business logic)
 * 4. Retornar respostas HTTP padronizadas
 * 
 * ENDPOINTS DISPONÍVEIS:
 * - POST   /api/autores          → Criar novo autor
 * - GET    /api/autores          → Listar todos os autores (paginado)
 * - GET    /api/autores/{id}     → Buscar autor por ID
 * - PUT    /api/autores/{id}     → Atualizar autor existente
 * - DELETE /api/autores/{id}     → Deletar autor (soft delete)
 * - GET    /api/autores/busca    → Buscar autor por email
 */
@RestController                           // Combina @Controller + @ResponseBody (retorna JSON)
@RequestMapping("/api/autores")           // Base URL para todos os endpoints deste controller
@RequiredArgsConstructor                  // Lombok: gera construtor com campos final (injeção de dependência)
public class AutorController {

    // INJEÇÃO DE DEPENDÊNCIA
    // Service é injetado automaticamente pelo Spring via construtor (RequiredArgsConstructor)
    private final AutorService autorService;

    /**
     * ENDPOINT: POST /api/autores
     * 
     * FUNÇÃO: Criar um novo autor no sistema
     * BODY: AutorRequestDTO (JSON) - dados do autor
     * RETORNO: AutorResponseDTO (JSON) - autor criado com ID
     * STATUS: 201 Created ou 400 Bad Request
     */
    @PostMapping                              // Mapeia requisições HTTP POST
    public ResponseEntity<AutorResponseDTO> createAuthor(@RequestBody @Valid AutorRequestDTO autorRequestDTO) {
        // @Valid: Ativa validações do Bean Validation (ex: @NotBlank, @Email)
        // @RequestBody: Converte JSON do body da requisição para AutorRequestDTO
        
        // 1. Se chegou aqui, validações passaram
        // 2. Chama service para criar autor (inclui validação de email único)
        AutorResponseDTO createdAuthor = autorService.createAutor(autorRequestDTO);

        // 3. Retorna 201 Created com o autor criado
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    /**
     * ENDPOINT: GET /api/autores?page=0&size=10
     * 
     * FUNÇÃO: Listar autores com paginação (para grandes volumes de dados)
     * PARÂMETROS:
     * - page: número da página (padrão: 0)
     * - size: itens por página (padrão: 10)
     * RETORNO: Page<NomeAutorDTO> - página com metadados
     * STATUS: 200 OK
     */
    @GetMapping                               // Mapeia requisições HTTP GET
    public ResponseEntity<Page<NomeAutorDTO>> FindAllAutors(
            @RequestParam(defaultValue = "0") int page,    // Query parameter: ?page=1
            @RequestParam(defaultValue = "10") int size) { // Query parameter: ?size=20
        
        // 1. Service implementa paginação usando Spring Data
        Page<NomeAutorDTO> autoresPage = autorService.findAllAutosPaginacao(page, size);

        // 2. Page contém: dados + metadados (total, páginas, etc.)
        return ResponseEntity.ok(autoresPage);
    }

    /**
     * ENDPOINT: GET /api/autores/{id}
     * 
     * FUNÇÃO: Buscar um autor específico pelo ID
     * PARÂMETRO: {id} - ID do autor na URL
     * RETORNO: AutorResponseDTO (JSON)
     * STATUS: 200 OK ou 404 Not Found
     */
    @GetMapping("/{id}")                      // {id} é uma variável na URL
    public ResponseEntity<AutorResponseDTO> findAutorById(@PathVariable Long id) {
        // @PathVariable captura o {id} da URL e converte para Long
        
        // 1. Chama service (se não encontrar, lança ResourceNotFoundException)
        AutorResponseDTO autor = autorService.findAutorById(id);
        
        // 2. Se chegou aqui, autor foi encontrado
        return ResponseEntity.ok(autor);
    }

    /**
     * ENDPOINT: PUT /api/autores/{id}
     * 
     * FUNÇÃO: Atualizar dados de um autor existente
     * PARÂMETRO: {id} - ID do autor a ser atualizado
     * BODY: AutorRequestDTO (JSON) - novos dados
     * RETORNO: AutorResponseDTO (JSON) - autor atualizado
     * STATUS: 200 OK, 404 Not Found ou 400 Bad Request
     */
    @PutMapping("/{id}")                      // Mapeia requisições HTTP PUT
    public ResponseEntity<AutorResponseDTO> updateAutor(
            @PathVariable Long id,                    // ID da URL
            @RequestBody @Valid AutorRequestDTO autorRequestDTO) {  // Dados do body
        
        // 1. Validações automáticas (@Valid)
        // 2. Service busca autor existente e atualiza (inclui validação de email único)
        AutorResponseDTO updatedAutor = autorService.updateAutor(id, autorRequestDTO);
        
        // 3. Retorna autor atualizado
        return ResponseEntity.ok(updatedAutor);
    }

    /**
     * ENDPOINT: DELETE /api/autores/{id}
     * 
     * FUNÇÃO: Remover autor do sistema (SOFT DELETE)
     * PARÂMETRO: {id} - ID do autor a ser removido
     * RETORNO: Sem conteúdo
     * STATUS: 204 No Content ou 404 Not Found
     * 
     * NOTA: Implementa SOFT DELETE (marca como indisponível, não remove do banco)
     */
    @DeleteMapping("/{id}")                   // Mapeia requisições HTTP DELETE
    public ResponseEntity<Void> deleteAutor(@PathVariable Long id) {
        // 1. Service marca autor como indisponível (soft delete)
        autorService.deleteAutor(id);
        
        // 2. Retorna 204 No Content (sucesso sem conteúdo)
        return ResponseEntity.noContent().build();
    }

    /**
     * ENDPOINT: GET /api/autores/busca?email=exemplo@mail.com
     * 
     * FUNÇÃO: Buscar autor por email específico
     * PARÂMETRO: email - Email do autor (query parameter)
     * RETORNO: AutorResponseDTO (JSON)
     * STATUS: 200 OK ou 404 Not Found
     */
    @GetMapping("/busca")                     // Endpoint específico para busca por email
    public ResponseEntity<AutorResponseDTO> findAuthorByEmail(
            @RequestParam(name = "email") String email) { // Query parameter: ?email=teste@email.com
        
        // 1. Service faz busca por email e lança 404 se não encontrar
        AutorResponseDTO autor = autorService.findAutorByEmail(email);

        // 2. Retorna autor encontrado
        return ResponseEntity.ok(autor);
    }
}