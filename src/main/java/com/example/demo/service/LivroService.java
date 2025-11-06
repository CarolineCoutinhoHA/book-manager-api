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


    //Metodos HELPERS

    //HELPER 1: Buscar Livro por ID se nao achar lança o erro404
    private Livro findLivroOrThrow(Long idLivro){
        //Logica: Se o findById retornar Optional.empty, lança a exceção 404
        return livroRepository.findById(idLivro)
                .orElseThrow(() -> new ResourceNotFoundException("O livro de ID: " + idLivro + " não foi encontrado!"));
    }

    //HELPER 2: Checa o registro unico na regra de negocio do ISBN(Registro unico de livro)
    private void checkIsbnUnique(String isbn){
        //Logica: Verifica se já existe uma entidade com esse ISBN registrado no banco de dados
        if (livroRepository.findAll().stream()
                .anyMatch(livro -> livro.getIsbn().equals(isbn))){
            throw new BusinessException("ISBN" + isbn + " já registrado. Não é permitido duplicidade.");
        }
    }

    //HELPER 3: Validação de Autores no Many-to-Many (M:N)
    private List<Autor> findAuthorsAndValidate(List<Long> autoresIds) {
        //1. Busca todos os autores no banco de dados
        List<Autor> todosAutores = autorRepository.findAll();

        //2.Filtra a lista completa para obter apenas os Autores solicitados
        List<Autor> autoresEncontrados = todosAutores.stream()
                .filter(autor -> autoresIds.contains(autor.getIdAutor()))
                .toList();

        //3. Regra de Negocido M:N: Compara o numero de autores encontrados com o numero de IDs pedidos
        if (autoresEncontrados.size() != autoresIds.size()){

            throw new ResourceNotFoundException("Um ou mais IDs de autores não foram encontrados.");
        }
        return autoresEncontrados;
    }


    //METODOS PUBLICOS


    //Criar LIVRO e Associar AUTORES(POST)

    @Transactional //Sobrescreve o readOnly = true, permitindo escrita no DB
    public LivroResponseDTO createLivro(LivroRequestDTO livroRequestDTO){
        //1. Regra de Negócio: Validação de Unicidade do ISBN(chama o HELPER 2)
        checkIsbnUnique(livroRequestDTO.isbn());

        //2. Busca e Valida Autores (Chama o HELPER 3)
        List<Autor> autores = findAuthorsAndValidate(livroRequestDTO.autoresIds());

        //3. Mapeamento: DTO -> Entidade (MapStruct mapeia os campos)
        Livro novoLivro = livroMapper.toEntity(livroRequestDTO);

        //4. Regra de Negocio: Gerenciamento Bidirecional M:N (O CORAÇAO DO RELACIONAMENTO)
        //Para cada autor, usa o metodo de dominio seguro do Livro para sincronizar as coleções
        autores.forEach(novoLivro::adicionarAutor);

        //5. Salva o Livro (O JPA salva o Livro e as associações na tabela de junção)
        return livroMapper.toResponseDTO(livroRepository.save(novoLivro));
    }

    //ATUALIZAR LIVRO (PUT)

    @Transactional
    public LivroResponseDTO updateLivro(Long id, LivroRequestDTO livroRequestDTO){

        //1. Busca Livro (HELPER 1)
        Livro livroExistente = findLivroOrThrow(id);

        //2. Regra de Negocio: Lógica de Correção do ISBN (RN imutável flexivel)
        if (!livroExistente.getIsbn().equals(livroRequestDTO.isbn())){
            checkIsbnUnique(livroRequestDTO.isbn()); //Se o ISBN mudou, verifica se o novo ISBN está livre (HELPER 2)
            livroExistente.corrigirIsbn(livroRequestDTO.isbn());
        }

        //3.Mapeia as alterações (titulo, data, categoria)
        //O Mapper usa os setters de domínio do Livro.java (ex: setTitulo)
        livroMapper.updateEntityFromDto(livroRequestDTO, livroExistente);

        //4.Salva a entidade modificada
        return livroMapper.toResponseDTO(livroRepository.save(livroExistente));
    }


    //Deletar Livro - Soft Delete

    @Transactional
    public void deletarLivro(Long id){
        //1. Busca Livro
        Livro livro = findLivroOrThrow(id);

        //2. SOFT DELETE: chama o metodo de dominio para marcar como indisponivel
        livro.indisponivel();

        //3. Salva a alteração de status
        livroRepository.save(livro);
        log.info("Livro ID {} marcado como indisponível (Soft Delete).", id);
    }


    //Metodos Publicos de Busca (LEITURA)

    //4. Buscar Livro por ID (GET)
    public LivroResponseDTO findLivroById(Long id){

        return livroMapper.toResponseDTO(findLivroOrThrow(id));
    }

    //5. Listar todos os livros (GET)
    public List<LivroResponseDTO> findAllLivros(){

        return livroRepository.findAll().stream()
                .filter(Livro::isDisponibilidade)
                .map(livroMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    //Rotina Agendada - @Schedule (Funcionalidade Avançada)

    /**
     * Rotina Agendada - @Scheduled(cron = "0 30 1 * * *"): Executa toda madrugada à 01:30h.
     * Simula a auditoria diária de ISBNs e dados essenciais, como é feito em sistemas financeiros.
     */

    @Scheduled(cron = "0 30 1 * * *") // Executa Às 01:30:00da madrugada
    @Transactional(readOnly = true)
    public void rotinaVerificacaoDeDados(){

        //1. Busca todos os livros ativos (RN do filtro)
        List<Livro> livrosAtivos = livroRepository.findAll().stream()
                .filter(Livro::isDisponibilidade)
                .collect(Collectors.toList());

        //2. Lógica: Simula a checagem de integridade do ISBN (busca por duplicidade no banco)
        //Mesmo que a RN esteja no Service, a autidoria noturna checa se o DB está integro

        log.warn("===============================");
        log.warn("AUDITORIA NOTURNA (01:30h): INICIANDO VERIFICAÇÃO DE INTEGRIDADE.");
        log.warn("STATUS:Verificando {} livros ativos.", livrosAtivos.size());

        //Simulação de RN: Checa se há algum livro muito antiqo para arquivamento

        livrosAtivos.stream()
                .filter(livro -> livro.getDataDePublicacao().isAfter(LocalDate.now().minusYears(50)))
                .forEach(livro -> log.warn("ALERTA: livro '{}' com mais de 5  anos encontrado . Sugerir arquivamento.",livro.getTitulo()));


        log.info("AUDITORIA CONCLUÍDA: integridade dos dados checados.");
        log.warn("===================================");
    }





}



