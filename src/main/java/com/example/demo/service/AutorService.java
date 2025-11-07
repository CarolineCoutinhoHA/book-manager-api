package com.example.demo.service;

import com.example.demo.dto.AutorRequestDTO;
import com.example.demo.dto.AutorResponseDTO;
import com.example.demo.dto.NomeAutorDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.AutorMapper;
import com.example.demo.model.Autor;
import com.example.demo.repository.AutorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AutorService {

    //Dependencias
    private final AutorRepository autorRepository;
    private final AutorMapper autorMapper;


    //METODOS HELPERS

    //HELPER 1: Busca o Autor por ID ou Lança 404
    private Autor findAutorOrThrow(Long id) {

        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor com ID " + id + " não encontrado."));
    }

    //HELPER 2. Verfica se o email já existe (RN - Evita violacao da chave UNICA no DB)
    public void checkEmailUnique(String email, Long idAutor) {

        //Logica: Busca todos os autores e filtra para ver se o email ja estaem uso por outro autor.
        autorRepository.findAll().stream()
                .filter(autor -> idAutor == null || !autor.getIdAutor().equals(idAutor))
                .filter(autor -> autor.getEmail().equals(email))
                .findFirst() // Se encontra um diferente, para aqu
                .ifPresent(autor -> {
                    throw new BusinessException("E-mail '" + email + "' já registrado. Não é permitido duplicidades.");
                });
    }


    //METODOS PUBLICOS

    //1. Criar Autor (POST)
    @Transactional
    public AutorResponseDTO createAutor(AutorRequestDTO autorRequestDTO) {

        //1. RN: Validação de Unicidade do e-mail (chama helper 2, Id atual é null)
        checkEmailUnique(autorRequestDTO.email(), null);

        //2. Mapeamento: Usa o construtor custumizado no Model (criação segura)
        Autor autor = new Autor(autorRequestDTO.nome(), autorRequestDTO.email(), autorRequestDTO.cep(), autorRequestDTO.telefone());

        //3. Persistencia: O JPA salva o objeto (e o @PrePersitegera o UUID)
        return autorMapper.toResponseDTO(autorRepository.save(autor));
    }


    //2.Atualizar Autor (PUT)
    @Transactional
    public AutorResponseDTO updateAutor(Long id, AutorRequestDTO autorRequestDTO) {

        //1. Busca Autor Existente(HELPER 1)
        Autor autorExistente = findAutorOrThrow(id);

        //2. RN: Validação de E-mail único (Chama o Helper 2, passando o ID atual)
        //Isso garante o novo e-mail não seja de outra pessoa.
        checkEmailUnique(autorRequestDTO.email(), id);

        //3. Mapeamento: Transfere os dados do DTO para a entidade existente
        //O mapper chama os setter de dominio (setNome, setEmail, etc.)
        autorMapper.updateEntityFromDto(autorRequestDTO, autorExistente);

        //4. Salva a entidade modificada
        return autorMapper.toResponseDTO(autorRepository.save(autorExistente));
    }


    //Deletar Autor (SOFT DELETE)
    @Transactional
    public void deleteAutor(Long id) {
        Autor autor = findAutorOrThrow(id); //1. Busca o Autor

        //2. SOFT DELETE: Chama o metodo de domínio para marcar como indiponivel
        autor.indisponivel();

        //3. Salva a alteração de status (O registro permanece, mas inativo)
        autorRepository.save(autor);
        log.info("Autos ID {} marcado como indisponível (Soft Delete).", id);
    }

    //Metodos Publicos de busca (LEITURA)

    //4. Buscar Autor por ID (GET)
    public AutorResponseDTO findAutorById(Long id) {
        //1. Busca segura (Helper 1)
        //2. Convertepara o DTO de saída (Mapper)
        return autorMapper.toResponseDTO(findAutorOrThrow(id));
    }

    //Listar todos os Autores (GET)
    public List<AutorResponseDTO> findAllAutors() {
        //1. Busca todos no bando
        //2. Filtro de SOFT DELETE: usa o isDisponibilidade para retornar apenas os autores ativos.
        //3. Mapeia cada entidade para o DTO.
        return autorRepository.findAll().stream()
                .filter(Autor::isDisponibilidade)
                .map((autorMapper::toResponseDTO))
                .collect(Collectors.toList());
    }

    //Listar Autores: volta apenas nome e id (GET)
    public List<NomeAutorDTO> findAllAutorsName() {

        return autorRepository.findAll().stream()
                .filter(Autor::isDisponibilidade)
                .map((autorMapper::toNomeAutorDTO))
                .collect(Collectors.toList());
    }

    /**
     * 5. Listar Todos os Autores (GET) - COM PAGINAÇÃO
     * Retorna o DTO leve (NomeAutorDTO) para eficiência de listagem.
     */
    public Page<NomeAutorDTO> findAllAutosPaginacao(int page, int size) {
        // 1. Criar o objeto Pageable (instrução de paginação)
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));

        // 2. Busca e Filtra: Usa findAll(Pageable) para trazer os dados da página
        Page<Autor> autorPage = autorRepository.findAll(pageable);

        // 3. Aplica o filtro de disponibilidade e re-cria a Page para refletir o Soft Delete.
        List<Autor> autoresAtivos = autorPage.getContent().stream()
                .filter(Autor::isDisponibilidade)
                .collect(Collectors.toList());

        // Cria um PageImpl com os resultados ativos para retornar metadados corretos
        Page<Autor> filteredPage = new PageImpl<>(autoresAtivos, pageable, autorRepository.count());

        // 4. Mapeia a Page de Entidades para a Page de DTOs leves (NomeAutorDTO)
        return filteredPage.map(autorMapper::toNomeAutorDTO);


    }

    // Dentro da classe AuthorService (após o findAllAutors)

    /**
     * 6. Buscar Autor por E-mail (Simulando busca por CPF/Identificador Único)
     * Retorna um único Autor (o email é único no DB).
     */
    public AutorResponseDTO findAutorByEmail(String email) {
        // Lógica: Busca todos os autores ativos e filtra pelo e-mail
        return autorRepository.findAll().stream()
                .filter(Autor::isDisponibilidade) // Apenas ativos
                .filter(autor -> autor.getEmail().equals(email))
                .map(autorMapper::toResponseDTO)
                .findFirst() // Como o e-mail é único, buscamos o primeiro
                .orElseThrow(() -> new ResourceNotFoundException("Autor com e-mail '" + email + "' não encontrado."));
    }
}





