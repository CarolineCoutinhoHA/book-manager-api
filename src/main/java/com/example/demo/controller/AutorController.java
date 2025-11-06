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

//@RestController: Marca a classe como um componente de controle REST (retorna JSON)
@RestController
//@RequestMapping: Define o caminho base da API (Ex: /api/autores)
@RequestMapping("/api/autores")
//@RequiredArgsConstructo: Injeta o AuthorService
@RequiredArgsConstructor
public class AutorController {

    private final AutorService autorService; //Injeção do Service (Lógica de Negócio)

    //1. POST: CRIAR AUTOR (HTTP 201 Created)

    @PostMapping //Mapeia para o metodo POST /api/autores
    //@RequestBody: Pega o Json de entrada. @Valid: Aciona as RNs de formato (@Email, etc)
    public ResponseEntity<AutorResponseDTO> createAuthor(@RequestBody @Valid AutorRequestDTO autorRequestDTO) {

        //1. Delega para o Service: O Service faz a Regra de Negocio de e-mail único e salva a Entidade.
        AutorResponseDTO createdAuthor = autorService.createAutor(autorRequestDTO);

        //2. Resposta (ResponseEntity): Retorna 201 Created e o objeto criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    //2. GET: LISTAR TODOS COM PAGINAÇÃO

    @GetMapping//Mapeia para GET /api/autores
    //@RequestParam: Captura os parametos opcionais 'page' e 'size' de URL.
    //defaultValue: Se o cliente não enciar os parâmetros, usa o valor padrão.
    public ResponseEntity<org.springframework.data.domain.Page<NomeAutorDTO>> FindAllAutors(
            @RequestParam(defaultValue = "0") int page, //Pagina inicial (0)
            @RequestParam(defaultValue = "10") int size) {//Tamano da página (10 itens)

        //1. Delegar para o Service, passandoasinstruções depaginação (page e size);
        Page<NomeAutorDTO> autoresPage = autorService.findAllAutosPaginacao(page, size);

        //2. Resposta: Retorna 200 OK com o objeto Page (que inclui a lista filtrada e metadados)
        return ResponseEntity.ok(autoresPage);
    }

    //GET: BUSCAR POR ID (HTTP 200 OK ou 404 via Handler)

    @GetMapping("/{id}") //Mapeia para GET /api/autores/{id}
    //@PathVariable: Cptura o ID do autor na URL
    public ResponseEntity<AutorResponseDTO> findAutorById(@PathVariable Long id) {

        //O Service lança RsourceNotFoundException se não achar
        AutorResponseDTO autor = autorService.findAutorById(id);

        //Retorna 200 OK com o DTO completo (AutorResponseDTO)
        return ResponseEntity.ok(autor);
    }

    //4. PUT: ATUALIZAR (HTTP 200 OK)

    @PutMapping("/{id}") //Mapeia para PUT /api/autores/{id}
    public ResponseEntity<AutorResponseDTO> updateAutor(
            @PathVariable Long id, //Id do autor a ser atualizado
            @RequestBody @Valid AutorRequestDTO autorRequestDTO) {// Novos dados a serem aplicados

        //O Service lida com a RN de uncidade de e-mail e atualização dos campos
        AutorResponseDTO updatedAutor = autorService.updateAutor(id, autorRequestDTO);

        //Retorna 200 OK
        return ResponseEntity.ok(updatedAutor);
    }

    //5. DELETE: REMOVER (HHTP 204 No Content - Soft Delete)
    @DeleteMapping("/{id}") //Mapeia para DELETE /api/autores/{i}
    public ResponseEntity<Void> deleteAutor(@PathVariable Long id) {

        //O Service faz o Soft Delete (chamando autor.indisponivel() e salvando)
        autorService.deleteAutor(id);

        //Padrão REST: Retorna 204 No Content (sucesso sem corpo)
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------
    // 4. GET: BUSCAR POR IDENTIFICADOR (E-MAIL/CPF)
    // ----------------------------------------------------
    @GetMapping("/busca") // Mapeia para GET /api/autores/busca?email=exemplo@mail.com
    public ResponseEntity<AutorResponseDTO> findAuthorByEmail(
            @RequestParam(name = "email") String email) { // Captura o valor após o '?'

        // O Service faz o filtro manual e lança 404 se não encontrar
        AutorResponseDTO autor = autorService.findAutorByEmail(email);

        return ResponseEntity.ok(autor);
    }
}
