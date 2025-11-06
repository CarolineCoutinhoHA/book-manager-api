package com.example.demo.controller;

import com.example.demo.dto.LivroRequestDTO;
import com.example.demo.dto.LivroResponseDTO;
import com.example.demo.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController  //@RestController: Componente que lida com requisições HTTP e retorna JSON
@RequestMapping("/api/livros")  //@RequestMapping: Define o caminho base para todos os metodos (EX: /api/livro)
@RequiredArgsConstructor  //@RequiredArgsConstructor: Injeta o LivroService
public class LivroController {

    private final LivroService livroService;  //Inejção do Service

    //1. POST: CRIAR LIVRO E ASSOCIAR AUTORES (HTTP 201 Vreated)

    @PostMapping //@PostMapping: Mapeia para o metodo POST /api/livros
    //@RequestBody: Pega o JSON do corpo da requisição e mapeia para o DTO
    //@Valid: CRUCIAL! Aciona as validações do DTO(@NotBlank, @PastOrPresent, @NotEmpty)

    public ResponseEntity<LivroResponseDTO> createBook(@RequestBody @Valid LivroRequestDTO livroRequestDTO) {

        //1. Delegar para o service: O Service faz a busca dos Autores, a RN do ISBN, e salva a M:N
        LivroResponseDTO createdBook = livroService.createLivro(livroRequestDTO);

        //2. Resposta: Retorna 201 Created com o objeto criado no corpo
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }


    //2. GET: BUSCAR POR ID (HTTP 200 OK ou 404 via Handler)

  @GetMapping("/{id}") //Mapeia para GET /api/livros/{id}
    //@PathVariable: Captura o valor do {id} da URL
    public ResponseEntity<LivroResponseDTO> findBookById(@PathVariable Long id){

        //O Service lança ResourceNotFoundException se não achar
      LivroResponseDTO livro = livroService.findLivroById(id);

      //Retorna 200 OK
      return ResponseEntity.ok(livro);
  }

  //3. GET: LISTAR TODOS (HTTP 200 OK)

    @GetMapping //Mapeia para GET /api/livros
    public ResponseEntity<List<LivroResponseDTO>> findAllBook() {

        //O Service já filtra o Soft Delete e o Mapper resolve o M:N

        return ResponseEntity.ok(livroService.findAllLivros());
    }


    //4. PUT: ATUALIZAR (HTTP 200 OK)

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponseDTO> updateBook(
            @PathVariable Long id, //ID do livro a ser atualizado
            @RequestBody @Valid LivroRequestDTO livroRequestDTO){ //Novos dados a serem aplicados

        //O Service lida com regra de negociode correção do ISBN

        LivroResponseDTO updateBook = livroService.updateLivro(id, livroRequestDTO);

        //Retorna 200 OK

    }





}
