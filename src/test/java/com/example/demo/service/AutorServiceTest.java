package com.example.demo.service;

import com.example.demo.dto.AutorRequestDTO;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.AutorMapper;
import com.example.demo.model.Autor;
import com.example.demo.repository.AutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Habilita o ambiente Mockito para que as anotações funcionem
@ExtendWith(MockitoExtension.class)
public class AutorServiceTest {

    // A CLASSE REAL que estamos testando
    @InjectMocks
    private AutorService autorService;

    // Dependências FALSAS (Mocks)
    @Mock private AutorRepository autorRepository;
    @Mock private AutorMapper autorMapper;

    // Constantes de Teste
    private final Long AUTOR_ID = 1L;
    private final Long ANOTHER_AUTOR_ID = 2L;
    private final String VALID_EMAIL = "teste@email.com";
    private final String DUPLICATE_EMAIL = "duplicado@mail.com";

    // ====================================================
    // TESTES DE LÓGICA DE NEGÓCIO (RNs)
    // ====================================================

    // CENÁRIO 1: Teste de Soft Delete (Ação de Domínio)
    @Test
    void deleteAutor_ShouldCallIndisponivelAndSave() {
        // ARRANGE
        Autor autorMock = mock(Autor.class);
        when(autorRepository.findById(AUTOR_ID)).thenReturn(Optional.of(autorMock));

        // ACT
        autorService.deleteAutor(AUTOR_ID);

        // ASSERT (Verificação)
        verify(autorMock, times(1)).indisponivel();
        verify(autorRepository, times(1)).save(autorMock);
    }

    // CENÁRIO 2: Criar Autor com E-mail Duplicado (RN - Unicidade)
    @Test
    void createAutor_ShouldThrowBusinessException_WhenEmailIsDuplicate() {
        // ARRANGE
        AutorRequestDTO dto = new AutorRequestDTO("Nome", DUPLICATE_EMAIL, "123", "123");
        
        Autor autorExistente = mock(Autor.class);
        when(autorExistente.getEmail()).thenReturn(DUPLICATE_EMAIL);
        when(autorRepository.findAll()).thenReturn(List.of(autorExistente));

        // ACT & ASSERT
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            autorService.createAutor(dto);
        });

        assertTrue(exception.getMessage().contains("já registrado"));
        verify(autorRepository, never()).save(any(Autor.class));
    }

    // CENÁRIO 3: Atualizar E-mail para um E-mail que JÁ PERTENCE a Outro Autor
    @Test
    void updateAutor_ShouldThrowBusinessException_WhenEmailBelongsToAnother() {
        // ARRANGE
        AutorRequestDTO dto = new AutorRequestDTO("Nome", DUPLICATE_EMAIL, "123", "123");

        Autor autorAlvo = mock(Autor.class);
        when(autorAlvo.getIdAutor()).thenReturn(AUTOR_ID);
        
        Autor autorComEmailExistente = mock(Autor.class);
        when(autorComEmailExistente.getIdAutor()).thenReturn(ANOTHER_AUTOR_ID);
        when(autorComEmailExistente.getEmail()).thenReturn(DUPLICATE_EMAIL);

        when(autorRepository.findById(AUTOR_ID)).thenReturn(Optional.of(autorAlvo));
        when(autorRepository.findAll()).thenReturn(List.of(autorAlvo, autorComEmailExistente));

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            autorService.updateAutor(AUTOR_ID, dto);
        });

        verify(autorRepository, never()).save(any(Autor.class));
    }
}