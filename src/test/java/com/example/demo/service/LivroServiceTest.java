package com.example.demo.service;

import com.example.demo.dto.LivroRequestDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.LivroMapper;
import com.example.demo.model.Autor;
import com.example.demo.model.Livro;
import com.example.demo.model.Categoria;
import com.example.demo.repository.AutorRepository;
import com.example.demo.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Habilita o ambiente Mockito
@ExtendWith(MockitoExtension.class)
public class LivroServiceTest {

    // A CLASSE REAL que estamos testando
    @InjectMocks
    private LivroService livroService;

    // Dependências FALSAS (Mocks)
    @Mock private LivroRepository livroRepository;
    @Mock private AutorRepository autorRepository;
    @Mock private LivroMapper mapper;

    // Constantes de Teste
    private final Long BOOK_ID = 1L;
    private final Long VALID_AUTHOR_ID = 10L;
    private final Long INVALID_AUTHOR_ID = 999L;

    // ====================================================
    // TESTES DE LÓGICA DE NEGÓCIO (RNs)
    // ====================================================

    // CENÁRIO 1: Criar Livro com ISBN Duplicado (RN - Unicidade)
    @Test
    void createLivro_ShouldThrowBusinessException_WhenIsbnIsDuplicate() {
        // ARRANGE
        LivroRequestDTO dto = new LivroRequestDTO(
                "Título Teste", "123-456", LocalDate.now(), Categoria.FICCAO, List.of(VALID_AUTHOR_ID));

        // Simula a lista que o service vai buscar: um livro que já existe
        Livro livroDuplicado = new Livro();
        livroDuplicado.corrigirIsbn("123-456"); // Setter de domínio

        // Mockito: Simula que o findAll retornou um livro com o mesmo ISBN (RN)
        when(livroRepository.findAll()).thenReturn(List.of(livroDuplicado));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            livroService.createLivro(dto);
        });

        // Verificação: Garante a mensagem e que o save NUNCA foi chamado
        assertTrue(exception.getMessage().contains("já registrado"));
        verify(livroRepository, never()).save(any(Livro.class));
    }

    // CENÁRIO 2: Teste de Soft Delete (Ação de Domínio)
    @Test
    void deleteLivro_ShouldCallIndisponivelAndSave() {
        // ARRANGE
        Livro livroMock = mock(Livro.class);

        // Configura o Helper para retornar nosso mock
        when(livroRepository.findById(BOOK_ID)).thenReturn(Optional.of(livroMock));

        // ACT
        livroService.deletarLivro(BOOK_ID);

        // ASSERT (Verificação)
        // 1. Garante que o método indisponivel() (Soft Delete) FOI chamado
        verify(livroMock, times(1)).indisponivel();

        // 2. Garante que o save FOI chamado (para persistir o status 'disponibilidade=false')
        verify(livroRepository, times(1)).save(livroMock);
    }

    // CENÁRIO 3: Criar Livro com ID de Autor Inválido (RN - M:N)
    @Test
    void createLivro_ShouldThrowResourceNotFound_WhenAuthorIsMissing() {
        // ARRANGE
        List<Long> idsSolicitados = List.of(VALID_AUTHOR_ID, INVALID_AUTHOR_ID);
        LivroRequestDTO dto = new LivroRequestDTO(
                "Título Teste", "789-012", LocalDate.now(), Categoria.FICCAO, idsSolicitados);

        // Mockito: Simula que o findAll (usado pelo helper) só encontra 1 autor (o válido)
        // O helper findAuthorsAndValidate usa findAll() e filtra.
        Autor autorValido = mock(Autor.class);
        when(autorValido.getIdAutor()).thenReturn(VALID_AUTHOR_ID);

        when(autorRepository.findAll()).thenReturn(List.of(autorValido));

        // ACT & ASSERT
        // Esperamos que o Service falhe com ResourceNotFoundException
        assertThrows(ResourceNotFoundException.class, () -> {
            livroService.createLivro(dto);
        });

        // Verificação final: O save nunca foi chamado
        verify(livroRepository, never()).save(any(Livro.class));
    }
}