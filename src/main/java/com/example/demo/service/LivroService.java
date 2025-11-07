package com.example.demo.service;

import com.example.demo.dto.LivroRequestDTO;
import com.example.demo.dto.LivroResponseDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.LivroMapper;
import com.example.demo.model.Autor;
import com.example.demo.model.Livro;
import com.example.demo.repository.AutorRepository;
import com.example.demo.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LivroService {

    private final AutorRepository autorRepository;
    private final LivroRepository livroRepository;
    private final LivroMapper livroMapper;

    private Livro findLivroOrThrow(Long idLivro){
        return livroRepository.findById(idLivro)
                .orElseThrow(() -> new ResourceNotFoundException("O livro de ID: " + idLivro + " não foi encontrado!"));
    }

    private void checkIsbnUnique(String isbn){
        if (livroRepository.findAll().stream()
                .anyMatch(livro -> livro.getIsbn().equals(isbn))){
            throw new BusinessException("ISBN" + isbn + " já registrado. Não é permitido duplicidade.");
        }
    }

    private List<Autor> findAuthorsAndValidate(List<Long> autoresIds) {
        List<Autor> todosAutores = autorRepository.findAll();
        List<Autor> autoresEncontrados = todosAutores.stream()
                .filter(autor -> autoresIds.contains(autor.getIdAutor()))
                .toList();

        if (autoresEncontrados.size() != autoresIds.size()){
            throw new ResourceNotFoundException("Um ou mais IDs de autores não foram encontrados.");
        }
        return autoresEncontrados;
    }

    @Transactional
    public LivroResponseDTO createLivro(LivroRequestDTO livroRequestDTO){
        checkIsbnUnique(livroRequestDTO.isbn());
        List<Autor> autores = findAuthorsAndValidate(livroRequestDTO.autoresIds());
        Livro novoLivro = livroMapper.toEntity(livroRequestDTO);
        autores.forEach(novoLivro::adicionarAutor);
        
        Livro livroSalvo = livroRepository.save(novoLivro);
        return createResponseDTO(livroSalvo);
    }

    @Transactional
    public LivroResponseDTO updateLivro(Long id, LivroRequestDTO livroRequestDTO){
        Livro livroExistente = findLivroOrThrow(id);
        
        if (!livroExistente.getIsbn().equals(livroRequestDTO.isbn())){
            checkIsbnUnique(livroRequestDTO.isbn());
            livroExistente.corrigirIsbn(livroRequestDTO.isbn());
        }

        livroMapper.updateEntityFromDto(livroRequestDTO, livroExistente);
        Livro livroAtualizado = livroRepository.save(livroExistente);
        return createResponseDTO(livroAtualizado);
    }

    @Transactional
    public void deletarLivro(Long id){
        Livro livro = findLivroOrThrow(id);
        livro.indisponivel();
        livroRepository.save(livro);
        log.info("Livro ID {} marcado como indisponível (Soft Delete).", id);
    }

    public LivroResponseDTO findLivroById(Long id){
        Livro livro = findLivroOrThrow(id);
        return createResponseDTO(livro);
    }

    public List<LivroResponseDTO> findAllLivros(){
        return livroRepository.findAll().stream()
                .filter(Livro::isDisponibilidade)
                .map(this::createResponseDTO)
                .collect(Collectors.toList());
    }

    private LivroResponseDTO createResponseDTO(Livro livro) {
        List<String> autoresNomes = livro.getAutores().stream()
                .map(Autor::getNome)
                .collect(Collectors.toList());
        
        return new LivroResponseDTO(
            livro.getIdLivro(),
            livro.getTitulo(), 
            livro.getIsbn(),
            livro.getDataDePublicacao(),
            livro.getCategoria().getNomeExibicao(),
            autoresNomes
        );
    }

    @Scheduled(cron = "0 30 1 * * *")
    @Transactional(readOnly = true)
    public void rotinaVerificacaoDeDados(){
        List<Livro> livrosAtivos = livroRepository.findAll().stream()
                .filter(Livro::isDisponibilidade)
                .collect(Collectors.toList());

        log.warn("===============================");
        log.warn("AUDITORIA NOTURNA (01:30h): INICIANDO VERIFICAÇÃO DE INTEGRIDADE.");
        log.warn("STATUS:Verificando {} livros ativos.", livrosAtivos.size());

        livrosAtivos.stream()
                .filter(livro -> livro.getDataDePublicacao().isAfter(LocalDate.now().minusYears(50)))
                .forEach(livro -> log.warn("ALERTA: livro '{}' com mais de 5  anos encontrado . Sugerir arquivamento.",livro.getTitulo()));

        log.info("AUDITORIA CONCLUÍDA: integridade dos dados checados.");
        log.warn("===============================");
    }

    public Page<LivroResponseDTO> findAllLivrosPaginates(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("titulo"));
        Page<Livro> livroPage = livroRepository.findAll(pageable);
        
        List<Livro> livrosAtivos = livroPage.getContent().stream()
                .filter(Livro::isDisponibilidade)
                .collect(Collectors.toList());

        Page<Livro> filteredPage = new PageImpl<>(livrosAtivos, pageable, livroRepository.count());
        return filteredPage.map(this::createResponseDTO);
    }
}