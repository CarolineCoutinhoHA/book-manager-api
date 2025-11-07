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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVIÇO DE AUTORES - CAMADA DE LÓGICA DE NEGÓCIO
 * 
 * Esta classe é o CORAÇÃO da lógica de negócio para autores.
 * Responsabilidades:
 * 1. REGRAS DE NEGÓCIO - Validações específicas (email único, etc.)
 * 2. TRANSAÇÕES - Gerenciamento de transações de banco de dados
 * 3. COORDENAÇÃO - Orquestra Repository, Mapper e outras dependências
 * 4. EXCEÇÕES - Lança exceções específicas para diferentes cenários
 * 5. LOGS - Registra operações importantes para auditoria
 * 
 * PADRÃO ARQUITETURAL:
 * Controller → Service → Repository → Database
 * 
 * TRANSAÇÕES:
 * - @Transactional(readOnly = true): Otimização para operações de leitura
 * - @Transactional: Transação completa para operações de escrita
 */
@Service                                    // Marca como componente de serviço do Spring
@RequiredArgsConstructor                    // Lombok: gera construtor com campos final
@Transactional(readOnly = true)            // PADRÃO: todas as operações são read-only (otimização)
@Slf4j                                     // Lombok: adiciona logger automático (log.info, log.error, etc.)
public class AutorService {

    // ===== DEPENDÊNCIAS INJETADAS =====
    // Todas as dependências são injetadas via construtor (RequiredArgsConstructor)
    
    private final AutorRepository autorRepository;  // Acesso aos dados (CRUD básico)
    private final AutorMapper autorMapper;          // Conversão DTO ↔ Entity (MapStruct)

    // ===== MÉTODOS HELPER PRIVADOS =====
    // Métodos auxiliares para reutilização de código e organização
    
    /**
     * HELPER 1: Buscar autor por ID ou lançar exceção 404
     * 
     * Este método encapsula a lógica de busca com tratamento de erro.
     * Se o autor não for encontrado, lança ResourceNotFoundException
     * que será capturada pelo GlobalExceptionHandler e retornará 404.
     * 
     * @param idAutor ID do autor a ser buscado
     * @return Autor encontrado
     * @throws ResourceNotFoundException se autor não existir
     */
    private Autor findAutorOrThrow(Long idAutor) {
        log.debug("Buscando autor por ID: {}", idAutor);
        
        return autorRepository.findById(idAutor)
                .orElseThrow(() -> {
                    log.warn("Autor não encontrado: ID {}", idAutor);
                    return new ResourceNotFoundException("O autor de ID: " + idAutor + " não foi encontrado!");
                });
    }

    /**
     * HELPER 2: Validar unicidade de email (REGRA DE NEGÓCIO)
     * 
     * Esta é uma REGRA DE NEGÓCIO importante: cada autor deve ter email único.
     * O método verifica se já existe outro autor com o mesmo email.
     * 
     * IMPLEMENTAÇÃO:
     * - Busca todos os autores (em produção, usar query específica)
     * - Filtra por email usando Stream API
     * - Lança BusinessException se encontrar duplicata
     * 
     * @param email Email a ser validado
     * @throws BusinessException se email já estiver em uso
     */
    private void checkEmailUnique(String email) {
        log.debug("Validando unicidade do email: {}", email);
        
        // Busca todos os autores e verifica se algum tem o mesmo email
        boolean emailExists = autorRepository.findAll().stream()
                .anyMatch(autor -> autor.getEmail().equals(email));
        
        if (emailExists) {
            log.warn("Tentativa de usar email duplicado: {}", email);
            throw new BusinessException("Email " + email + " já está em uso!");
        }
        
        log.debug("Email {} é único, validação passou", email);
    }

    // ===== MÉTODOS PÚBLICOS - OPERAÇÕES CRUD =====
    // Estes métodos implementam as operações principais do sistema
    
    /**
     * CREATE - Criar novo autor
     * 
     * FLUXO COMPLETO:
     * 1. Recebe AutorRequestDTO do Controller
     * 2. Valida regra de negócio (email único)
     * 3. Converte DTO → Entity usando MapStruct
     * 4. Salva no banco via Repository
     * 5. Converte Entity → ResponseDTO
     * 6. Retorna DTO para o Controller
     * 
     * TRANSAÇÃO: @Transactional sobrescreve readOnly=true da classe
     * 
     * @param dto Dados do autor a ser criado
     * @return DTO com dados do autor criado (incluindo ID gerado)
     */
    @Transactional  // Sobrescreve readOnly = true para permitir escrita
    public AutorResponseDTO createAutor(AutorRequestDTO dto) {
        log.info("=== INICIANDO CRIAÇÃO DE AUTOR ===");
        log.info("Dados recebidos: nome='{}', email='{}'", dto.nome(), dto.email());

        try {
            // 1. VALIDAÇÃO DE REGRA DE NEGÓCIO
            // Verifica se email já está em uso (regra crítica)
            checkEmailUnique(dto.email());
            log.debug("Validação de email único: PASSOU");

            // 2. CONVERSÃO DTO → ENTITY
            // MapStruct faz a conversão automática dos campos
            Autor novoAutor = autorMapper.toEntity(dto);
            log.debug("Conversão DTO → Entity: CONCLUÍDA");

            // 3. PERSISTÊNCIA NO BANCO
            // Repository salva e retorna entity com ID gerado
            Autor autorSalvo = autorRepository.save(novoAutor);
            log.info("Autor salvo no banco com ID: {}", autorSalvo.getIdAutor());

            // 4. CONVERSÃO ENTITY → RESPONSE DTO
            // Prepara dados para retornar ao Controller/Frontend
            AutorResponseDTO response = autorMapper.toResponseDTO(autorSalvo);
            
            log.info("=== AUTOR CRIADO COM SUCESSO ===");
            log.info("ID: {}, Nome: '{}', Email: '{}'", 
                response.idAutor(), response.nome(), response.email());
            
            return response;
            
        } catch (BusinessException e) {
            // Re-lança exceções de negócio (email duplicado)
            log.error("Erro de regra de negócio ao criar autor: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Captura erros inesperados
            log.error("Erro inesperado ao criar autor: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao criar autor", e);
        }
    }

    /**
     * READ - Buscar autor por ID
     * 
     * FLUXO:
     * 1. Usa helper para buscar autor (com tratamento 404)
     * 2. Converte Entity → ResponseDTO
     * 3. Retorna dados para Controller
     * 
     * TRANSAÇÃO: Usa readOnly=true da classe (otimização)
     * 
     * @param id ID do autor a ser buscado
     * @return DTO com dados do autor
     * @throws ResourceNotFoundException se autor não existir
     */
    public AutorResponseDTO findAutorById(Long id) {
        log.info("=== BUSCANDO AUTOR POR ID ===");
        log.info("ID solicitado: {}", id);

        try {
            // 1. BUSCA COM TRATAMENTO DE ERRO
            // Helper lança ResourceNotFoundException se não encontrar
            Autor autor = findAutorOrThrow(id);
            log.debug("Autor encontrado: {}", autor.getNome());

            // 2. CONVERSÃO PARA DTO DE RESPOSTA
            AutorResponseDTO response = autorMapper.toResponseDTO(autor);
            
            log.info("=== AUTOR ENCONTRADO COM SUCESSO ===");
            log.info("Nome: '{}', Email: '{}'", response.nome(), response.email());
            
            return response;
            
        } catch (ResourceNotFoundException e) {
            // Re-lança exceção 404
            log.warn("Autor não encontrado: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar autor ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro interno ao buscar autor", e);
        }
    }

    /**
     * READ - Listar todos os autores com paginação
     * 
     * FUNCIONALIDADES:
     * 1. Implementa paginação usando Spring Data
     * 2. Aplica filtro de SOFT DELETE (apenas disponíveis)
     * 3. Retorna apenas nome (DTO otimizado)
     * 4. Ordena por nome automaticamente
     * 
     * PERFORMANCE:
     * - Paginação evita carregar muitos dados
     * - DTO específico (NomeAutorDTO) reduz tráfego
     * - Filtro aplicado no Java (em produção, usar query)
     * 
     * @param page Número da página (0-based)
     * @param size Itens por página
     * @return Page com metadados de paginação
     */
    public Page<NomeAutorDTO> findAllAutosPaginacao(int page, int size) {
        log.info("=== LISTANDO AUTORES COM PAGINAÇÃO ===");
        log.info("Página: {}, Tamanho: {}", page, size);

        try {
            // 1. CONFIGURAÇÃO DA PAGINAÇÃO
            // PageRequest.of(page, size, Sort) configura paginação + ordenação
            Pageable pageable = PageRequest.of(page, size, Sort.by("nome"));
            log.debug("Paginação configurada: página {}, {} itens, ordenado por nome", page, size);

            // 2. BUSCA PAGINADA NO BANCO
            // Repository retorna Page<Autor> com metadados
            Page<Autor> autorPage = autorRepository.findAll(pageable);
            log.debug("Busca no banco retornou {} autores de {} total", 
                autorPage.getNumberOfElements(), autorPage.getTotalElements());

            // 3. APLICAÇÃO DO FILTRO SOFT DELETE
            // Filtra apenas autores disponíveis (não deletados)
            List<Autor> autoresAtivos = autorPage.getContent().stream()
                    .filter(Autor::isDisponibilidade)  // Soft delete filter
                    .collect(Collectors.toList());
            
            log.debug("Após filtro soft delete: {} autores ativos", autoresAtivos.size());

            // 4. CONVERSÃO PARA DTO OTIMIZADO
            // NomeAutorDTO contém ID e nome
            List<NomeAutorDTO> autoresDTO = autoresAtivos.stream()
                    .map(autor -> new NomeAutorDTO(autor.getIdAutor(), autor.getNome()))
                    .collect(Collectors.toList());

            // 5. CRIAÇÃO DA PAGE DE RESPOSTA
            // PageImpl mantém metadados originais com conteúdo filtrado
            Page<NomeAutorDTO> result = new PageImpl<>(autoresDTO, pageable, autorPage.getTotalElements());
            
            log.info("=== LISTAGEM CONCLUÍDA ===");
            log.info("Retornando {} autores na página {} de {}", 
                result.getNumberOfElements(), result.getNumber() + 1, result.getTotalPages());
            
            return result;
            
        } catch (Exception e) {
            log.error("Erro ao listar autores paginados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro interno ao listar autores", e);
        }
    }

    /**
     * UPDATE - Atualizar autor existente
     * 
     * FLUXO COMPLEXO:
     * 1. Busca autor existente (404 se não encontrar)
     * 2. Valida email único (apenas se mudou)
     * 3. Atualiza campos usando MapStruct
     * 4. Salva alterações no banco
     * 5. Retorna dados atualizados
     * 
     * REGRAS DE NEGÓCIO:
     * - ID não pode ser alterado (imutável)
     * - Email deve continuar único
     * - UUID não pode ser alterado
     * 
     * @param id ID do autor a ser atualizado
     * @param dto Novos dados do autor
     * @return DTO com dados atualizados
     */
    @Transactional  // Transação de escrita
    public AutorResponseDTO updateAutor(Long id, AutorRequestDTO dto) {
        log.info("=== INICIANDO ATUALIZAÇÃO DE AUTOR ===");
        log.info("ID: {}, Novos dados: nome='{}', email='{}'", id, dto.nome(), dto.email());

        try {
            // 1. BUSCA AUTOR EXISTENTE
            // Lança ResourceNotFoundException se não encontrar
            Autor autorExistente = findAutorOrThrow(id);
            log.debug("Autor encontrado para atualização: {}", autorExistente.getNome());
            
            // Guarda dados originais para log
            String emailOriginal = autorExistente.getEmail();
            String nomeOriginal = autorExistente.getNome();

            // 2. VALIDAÇÃO DE EMAIL ÚNICO (APENAS SE MUDOU)
            // Só valida se o email foi alterado (otimização + UX)
            if (!autorExistente.getEmail().equals(dto.email())) {
                log.debug("Email mudou de '{}' para '{}', validando unicidade", 
                    emailOriginal, dto.email());
                checkEmailUnique(dto.email());
                log.debug("Validação de email único: PASSOU");
            } else {
                log.debug("Email não mudou, pulando validação de unicidade");
            }

            // 3. ATUALIZAÇÃO DOS CAMPOS
            // MapStruct atualiza apenas os campos mapeados, preservando ID, UUID, etc.
            autorMapper.updateEntityFromDto(dto, autorExistente);
            log.debug("Campos atualizados via MapStruct");

            // 4. PERSISTÊNCIA DAS ALTERAÇÕES
            // save() do JPA detecta mudanças e faz UPDATE
            Autor autorAtualizado = autorRepository.save(autorExistente);
            log.info("Alterações salvas no banco");

            // 5. CONVERSÃO PARA DTO DE RESPOSTA
            AutorResponseDTO response = autorMapper.toResponseDTO(autorAtualizado);
            
            log.info("=== AUTOR ATUALIZADO COM SUCESSO ===");
            log.info("ID: {}", response.idAutor());
            log.info("Nome: '{}' → '{}'", nomeOriginal, response.nome());
            log.info("Email: '{}' → '{}'", emailOriginal, response.email());
            
            return response;
            
        } catch (ResourceNotFoundException | BusinessException e) {
            // Re-lança exceções conhecidas
            log.error("Erro ao atualizar autor ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar autor ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro interno ao atualizar autor", e);
        }
    }

    /**
     * DELETE - Remover autor (SOFT DELETE)
     * 
     * IMPLEMENTAÇÃO DE SOFT DELETE:
     * - Não remove fisicamente do banco
     * - Marca campo 'disponibilidade' como false
     * - Preserva dados para auditoria/histórico
     * - Permite "undelete" se necessário
     * 
     * VANTAGENS DO SOFT DELETE:
     * - Preserva integridade referencial
     * - Mantém histórico para auditoria
     * - Permite recuperação de dados
     * - Evita problemas com foreign keys
     * 
     * @param id ID do autor a ser removido
     */
    @Transactional  // Transação de escrita
    public void deleteAutor(Long id) {
        log.info("=== INICIANDO SOFT DELETE DE AUTOR ===");
        log.info("ID a ser removido: {}", id);

        try {
            // 1. BUSCA AUTOR EXISTENTE
            // Lança ResourceNotFoundException se não encontrar
            Autor autor = findAutorOrThrow(id);
            log.debug("Autor encontrado para remoção: {}", autor.getNome());
            
            // Guarda dados para log
            String nomeAutor = autor.getNome();
            String emailAutor = autor.getEmail();

            // 2. SOFT DELETE - MARCA COMO INDISPONÍVEL
            // Usa método de domínio da entidade (encapsulamento)
            autor.indisponivel();
            log.debug("Autor marcado como indisponível");

            // 3. PERSISTÊNCIA DA ALTERAÇÃO
            // save() atualiza apenas o campo 'disponibilidade'
            autorRepository.save(autor);
            
            log.info("=== SOFT DELETE CONCLUÍDO ===");
            log.info("Autor removido: ID {}, Nome '{}', Email '{}'", id, nomeAutor, emailAutor);
            log.info("Dados preservados no banco para auditoria");
            
        } catch (ResourceNotFoundException e) {
            // Re-lança exceção 404
            log.warn("Tentativa de deletar autor inexistente: ID {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao deletar autor ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Erro interno ao deletar autor", e);
        }
    }

    /**
     * READ - Buscar autor por email
     * 
     * FUNCIONALIDADE ADICIONAL:
     * - Permite busca por email em vez de ID
     * - Útil para validações e integrações
     * - Implementa filtro manual (em produção, usar query)
     * 
     * @param email Email do autor a ser buscado
     * @return DTO com dados do autor
     * @throws ResourceNotFoundException se autor não existir
     */
    public AutorResponseDTO findAutorByEmail(String email) {
        log.info("=== BUSCANDO AUTOR POR EMAIL ===");
        log.info("Email solicitado: {}", email);

        try {
            // 1. BUSCA MANUAL POR EMAIL
            // Em produção, criar método no Repository: findByEmail(String email)
            Autor autor = autorRepository.findAll().stream()
                    .filter(a -> a.getEmail().equals(email))
                    .filter(Autor::isDisponibilidade)  // Apenas ativos
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("Autor não encontrado com email: {}", email);
                        return new ResourceNotFoundException("Autor com email " + email + " não foi encontrado!");
                    });
            
            log.debug("Autor encontrado: {}", autor.getNome());

            // 2. CONVERSÃO PARA DTO
            AutorResponseDTO response = autorMapper.toResponseDTO(autor);
            
            log.info("=== AUTOR ENCONTRADO POR EMAIL ===");
            log.info("Nome: '{}', ID: {}", response.nome(), response.idAutor());
            
            return response;
            
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar autor por email '{}': {}", email, e.getMessage(), e);
            throw new RuntimeException("Erro interno ao buscar autor por email", e);
        }
    }

    // ===== FUNCIONALIDADES AVANÇADAS =====
    
    /**
     * ROTINA AGENDADA - Auditoria noturna de dados
     * 
     * FUNCIONALIDADE AVANÇADA:
     * - Executa automaticamente todos os dias às 01:30h
     * - Verifica integridade dos dados
     * - Gera relatórios de auditoria
     * - Identifica possíveis problemas
     * 
     * CRON EXPRESSION: "0 30 1 * * *"
     * - 0: segundo 0
     * - 30: minuto 30
     * - 1: hora 1 (01:30h)
     * - *: qualquer dia do mês
     * - *: qualquer mês
     * - *: qualquer dia da semana
     * 
     * EM PRODUÇÃO:
     * - Adicionar verificações mais robustas
     * - Enviar relatórios por email
     * - Integrar com sistema de monitoramento
     */
    @Scheduled(cron = "0 30 1 * * *")  // Executa às 01:30h todos os dias
    @Transactional(readOnly = true)    // Apenas leitura
    public void rotinaVerificacaoDeDados() {
        log.warn("===============================================");
        log.warn("AUDITORIA NOTURNA (01:30h): INICIANDO VERIFICAÇÃO DE INTEGRIDADE");
        log.warn("===============================================");

        try {
            // 1. ESTATÍSTICAS BÁSICAS
            List<Autor> todosAutores = autorRepository.findAll();
            List<Autor> autoresAtivos = todosAutores.stream()
                    .filter(Autor::isDisponibilidade)
                    .collect(Collectors.toList());
            
            long totalAutores = todosAutores.size();
            long autoresAtivosCount = autoresAtivos.size();
            long autoresInativosCount = totalAutores - autoresAtivosCount;
            
            log.warn("ESTATÍSTICAS:");
            log.warn("- Total de autores: {}", totalAutores);
            log.warn("- Autores ativos: {}", autoresAtivosCount);
            log.warn("- Autores inativos (soft delete): {}", autoresInativosCount);

            // 2. VERIFICAÇÃO DE INTEGRIDADE DE EMAIL
            long emailsDuplicados = todosAutores.stream()
                    .collect(Collectors.groupingBy(Autor::getEmail, Collectors.counting()))
                    .values().stream()
                    .filter(count -> count > 1)
                    .count();
            
            if (emailsDuplicados > 0) {
                log.error("ALERTA: {} emails duplicados encontrados!", emailsDuplicados);
            } else {
                log.info("✓ Integridade de emails: OK");
            }

            // 3. VERIFICAÇÃO DE DADOS ANTIGOS
            // Simula verificação de autores muito antigos (exemplo de regra de negócio)
            long autoresAntigos = autoresAtivos.stream()
                    .filter(autor -> autor.getEmail().contains("@exemplo.com"))  // Exemplo de critério
                    .count();
            
            if (autoresAntigos > 0) {
                log.warn("ALERTA: {} autores com emails de exemplo encontrados", autoresAntigos);
            }

            // 4. RELATÓRIO FINAL
            log.warn("AUDITORIA CONCLUÍDA: Integridade dos dados verificada");
            log.warn("Próxima execução: amanhã às 01:30h");
            
        } catch (Exception e) {
            log.error("ERRO na auditoria noturna: {}", e.getMessage(), e);
        } finally {
            log.warn("===============================================");
        }
    }
}