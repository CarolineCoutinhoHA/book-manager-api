package com.example.demo.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * ENTIDADE AUTOR - MODELO DE DOMÍNIO
 * 
 * Esta classe representa um AUTOR no sistema de biblioteca.
 * 
 * RESPONSABILIDADES:
 * 1. MAPEAMENTO JPA - Define como os dados são armazenados no banco
 * 2. REGRAS DE DOMÍNIO - Métodos que implementam lógica de negócio
 * 3. RELACIONAMENTOS - Define como se relaciona com outras entidades
 * 4. INTEGRIDADE - Garante consistência dos dados
 * 5. ENCAPSULAMENTO - Controla acesso aos dados internos
 * 
 * PADRÕES IMPLEMENTADOS:
 * - Domain Driven Design (DDD) - Métodos de domínio
 * - Active Record Pattern - Entidade com comportamento
 * - Soft Delete Pattern - Remoção lógica em vez de física
 * - UUID Pattern - Identificador público seguro
 * 
 * RELACIONAMENTOS:
 * - Many-to-Many com Livro (um autor pode ter vários livros, um livro pode ter vários autores)
 */

// ===== ANOTAÇÕES JPA (MAPEAMENTO OBJETO-RELACIONAL) =====

@Entity                                    // Marca como entidade JPA (tabela no banco)
@Table(name = "autor")                     // Nome da tabela no banco de dados

// ===== ANOTAÇÕES LOMBOK (REDUÇÃO DE BOILERPLATE) =====

@Getter                                    // Gera getters automáticos para todos os campos
@NoArgsConstructor                         // Construtor vazio (OBRIGATÓRIO para JPA)
@ToString(onlyExplicitlyIncluded = true)   // ToString customizado (apenas campos marcados)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // Equals/HashCode customizado (apenas campos marcados)

public class Autor {

    // ===== IDENTIFICADORES =====
    // Todo objeto precisa de identificadores únicos para o banco e para o sistema
    
    /**
     * CHAVE PRIMÁRIA INTERNA (ID)
     * 
     * Este é o identificador INTERNO usado pelo banco de dados.
     * Características:
     * - Auto-incremento (1, 2, 3, 4...)
     * - Usado para foreign keys e joins
     * - Nunca exposto para o frontend (segurança)
     * - Imutável após criação
     */
    @Id                                           // Marca como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-incremento pelo banco
    @Column(name = "id_autor")                    // Nome da coluna no banco
    @ToString.Include                             // Incluir no toString() para debug
    private Long idAutor;

    /**
     * IDENTIFICADOR PÚBLICO EXTERNO (UUID)
     * 
     * Este é o identificador PÚBLICO usado pelo frontend e APIs externas.
     * Características:
     * - UUID aleatório (ex: 550e8400-e29b-41d4-a716-446655440000)
     * - Seguro para exposição (não revela informações internas)
     * - Único globalmente (pode ser usado entre sistemas)
     * - Gerado automaticamente no @PrePersist
     */
    @EqualsAndHashCode.Include                    // Usar no equals/hashCode (identificação única)
    @Column(name = "uuid", nullable = false, unique = true)  // NOT NULL + UNIQUE
    private UUID uuid;

    // ===== CAMPOS DE DADOS =====
    // Informações específicas do autor
    
    /**
     * NOME DO AUTOR
     * 
     * Campo obrigatório que identifica o autor.
     * Validações aplicadas no DTO:
     * - @NotBlank: Não pode ser vazio
     * - @Size(max = 100): Máximo 100 caracteres
     */
    @Column(name = "nome", nullable = false)      // NOT NULL no banco
    @ToString.Include                             // Incluir no toString() para debug
    private String nome;

    /**
     * EMAIL DO AUTOR
     * 
     * Campo obrigatório e ÚNICO no sistema.
     * Regra de negócio: cada autor deve ter email único.
     * Validações aplicadas no DTO:
     * - @NotBlank: Não pode ser vazio
     * - @Email: Deve ter formato válido
     * Constraint no banco: UNIQUE
     */
    @Column(name = "email", nullable = false, unique = true)  // NOT NULL + UNIQUE
    @ToString.Include                             // Incluir no toString() para debug
    private String email;

    /**
     * CEP DO AUTOR
     * 
     * Campo obrigatório para endereço.
     * Validações aplicadas no DTO:
     * - @NotBlank: Não pode ser vazio
     * - @Pattern: Deve seguir formato XXXXX-XXX
     */
    @Column(name = "cep", nullable = false)       // NOT NULL no banco
    private String cep;

    /**
     * TELEFONE DO AUTOR
     * 
     * Campo OPCIONAL (pode ser null).
     * Validações aplicadas no DTO:
     * - @Pattern: Se preenchido, deve seguir formato (XX) XXXXX-XXXX
     */
    @Column(name = "telefone")                    // Nullable (campo opcional)
    private String telefone;

    /**
     * DISPONIBILIDADE (SOFT DELETE)
     * 
     * Campo para implementar SOFT DELETE.
     * - true: Autor ativo no sistema
     * - false: Autor "deletado" (mas preservado no banco)
     * 
     * VANTAGENS DO SOFT DELETE:
     * - Preserva dados para auditoria
     * - Mantém integridade referencial
     * - Permite "undelete" se necessário
     * - Evita problemas com foreign keys
     */
    @Column(name = "disponibilidade", nullable = false)
    private boolean disponibilidade = true;       // Padrão: ativo

    // ===== RELACIONAMENTOS =====
    // Define como esta entidade se relaciona com outras
    
    /**
     * RELACIONAMENTO MANY-TO-MANY COM LIVROS (LADO INVERSO)
     * 
     * Um autor pode escrever vários livros.
     * Um livro pode ter vários autores.
     * 
     * CONFIGURAÇÃO:
     * - mappedBy = "autores": Indica que o lado "dono" está na entidade Livro
     * - fetch = FetchType.LAZY: Carrega livros apenas quando acessados (performance)
     * - final Set: Coleção imutável (não pode ser substituída)
     * - HashSet: Implementação que evita duplicatas
     * 
     * LADO INVERSO:
     * Esta entidade é o lado "inverso" do relacionamento.
     * O lado "dono" (Livro) gerencia a tabela de junção.
     */
    @ManyToMany(mappedBy = "autores", fetch = FetchType.LAZY)
    @ToString.Exclude                             // Excluir do toString (evita LazyInitializationException)
    private final Set<Livro> livrosSet = new HashSet<>();

    // ===== CICLO DE VIDA JPA =====
    // Métodos executados automaticamente pelo JPA em momentos específicos
    
    /**
     * PRÉ-PERSISTÊNCIA - Executado ANTES de salvar no banco
     * 
     * Este método é chamado automaticamente pelo JPA antes de fazer INSERT.
     * Usado para:
     * - Gerar UUID se não existir
     * - Definir valores padrão
     * - Validações de última hora
     * - Logs de auditoria
     */
    @PrePersist
    public void prePersist() {
        // Gera UUID apenas se não foi definido manualmente
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        
        // Log para auditoria (em produção, usar sistema de auditoria adequado)
        System.out.println("Criando novo autor: " + nome + " (" + email + ")");
    }

    // ===== CONSTRUTORES =====
    // Diferentes formas de criar instâncias da entidade
    
    /**
     * CONSTRUTOR PARA CRIAÇÃO MANUAL
     * 
     * Usado pelo Service quando cria um novo autor.
     * Recebe apenas os dados essenciais fornecidos pelo usuário.
     * 
     * CAMPOS AUTOMÁTICOS:
     * - idAutor: Será gerado pelo banco (auto-increment)
     * - uuid: Será gerado no @PrePersist
     * - disponibilidade: Padrão true
     * - livrosSet: Inicializado como HashSet vazio
     * 
     * @param nome Nome do autor
     * @param email Email único do autor
     * @param cep CEP do autor
     * @param telefone Telefone do autor (pode ser null)
     */
    public Autor(String nome, String email, String cep, String telefone) {
        this.nome = nome;
        this.email = email;
        this.cep = cep;
        this.telefone = telefone;
        // disponibilidade = true (padrão)
        // uuid será gerado no @PrePersist
        // idAutor será gerado pelo banco
    }

    // ===== MÉTODOS DE DOMÍNIO =====
    // Métodos que implementam regras de negócio e comportamentos da entidade
    
    /**
     * SETTER CONTROLADO - NOME
     * 
     * Permite atualização do nome de forma controlada.
     * Usado pelo MapStruct durante updates.
     * 
     * REGRAS:
     * - Aceita apenas valores não nulos
     * - Pode adicionar validações extras se necessário
     * 
     * @param nome Novo nome do autor
     */
    public void setNome(String nome) {
        if (nome != null && !nome.trim().isEmpty()) {
            this.nome = nome.trim();  // Remove espaços extras
        }
    }

    /**
     * SETTER CONTROLADO - EMAIL
     * 
     * Permite atualização do email de forma controlada.
     * Usado pelo MapStruct durante updates.
     * 
     * IMPORTANTE: A validação de unicidade é feita no Service,
     * não aqui (separação de responsabilidades).
     * 
     * @param email Novo email do autor
     */
    public void setEmail(String email) {
        if (email != null && !email.trim().isEmpty()) {
            this.email = email.trim().toLowerCase();  // Normaliza email
        }
    }

    /**
     * SETTER CONTROLADO - CEP
     * 
     * Permite atualização do CEP de forma controlada.
     * 
     * @param cep Novo CEP do autor
     */
    public void setCep(String cep) {
        if (cep != null && !cep.trim().isEmpty()) {
            this.cep = cep.trim();
        }
    }

    /**
     * SETTER CONTROLADO - TELEFONE
     * 
     * Permite atualização do telefone de forma controlada.
     * Telefone pode ser null (campo opcional).
     * 
     * @param telefone Novo telefone do autor (pode ser null)
     */
    public void setTelefone(String telefone) {
        // Telefone pode ser null ou vazio (campo opcional)
        this.telefone = (telefone != null && !telefone.trim().isEmpty()) 
            ? telefone.trim() 
            : null;
    }

    // ===== MÉTODOS DE NEGÓCIO =====
    // Implementam regras de negócio específicas do domínio
    
    /**
     * ATIVAR AUTOR (REVERTER SOFT DELETE)
     * 
     * Marca o autor como disponível no sistema.
     * Usado para reverter um soft delete ou reativar autor.
     * 
     * CASOS DE USO:
     * - Reverter exclusão acidental
     * - Reativar autor após período de inatividade
     * - Operações de administração
     */
    public void disponivel() {
        this.disponibilidade = true;
        System.out.println("Autor reativado: " + nome);
    }

    /**
     * DESATIVAR AUTOR (SOFT DELETE)
     * 
     * Marca o autor como indisponível (soft delete).
     * O autor não é removido fisicamente do banco.
     * 
     * VANTAGENS:
     * - Preserva dados para auditoria
     * - Mantém integridade referencial com livros
     * - Permite recuperação posterior
     * - Histórico completo mantido
     */
    public void indisponivel() {
        this.disponibilidade = false;
        System.out.println("Autor desativado (soft delete): " + nome);
    }

    /**
     * GERENCIAMENTO BIDIRECIONAL - ADICIONAR LIVRO
     * 
     * Adiciona um livro à coleção deste autor.
     * Usado para manter sincronização bidirecional do relacionamento Many-to-Many.
     * 
     * IMPORTANTE: Este método é chamado pelo lado "dono" (Livro)
     * para manter ambos os lados do relacionamento sincronizados.
     * 
     * FLUXO TÍPICO:
     * 1. Livro.adicionarAutor(autor) é chamado
     * 2. Livro adiciona autor à sua coleção
     * 3. Livro chama autor.atribuirLivroAoAutor(livro)
     * 4. Autor adiciona livro à sua coleção
     * 5. Relacionamento fica bidirecional
     * 
     * @param livro Livro a ser associado a este autor
     */
    public void atribuirLivroAoAutor(Livro livro) {
        if (livro != null) {
            this.livrosSet.add(livro);
            System.out.println("Livro '" + livro.getTitulo() + "' associado ao autor '" + nome + "'");
        }
    }

    /**
     * MÉTODO DE CONSULTA - VERIFICAR SE TEM LIVROS
     * 
     * Verifica se o autor tem livros associados.
     * Útil para validações de negócio.
     * 
     * @return true se o autor tem livros, false caso contrário
     */
    public boolean temLivros() {
        return !livrosSet.isEmpty();
    }

    /**
     * MÉTODO DE CONSULTA - CONTAR LIVROS
     * 
     * Retorna a quantidade de livros do autor.
     * Útil para estatísticas e relatórios.
     * 
     * @return Número de livros do autor
     */
    public int quantidadeLivros() {
        return livrosSet.size();
    }

    /**
     * MÉTODO DE VALIDAÇÃO - PODE SER DELETADO
     * 
     * Verifica se o autor pode ser removido do sistema.
     * Implementa regra de negócio: autor com livros não pode ser deletado.
     * 
     * @return true se pode ser deletado, false caso contrário
     */
    public boolean podeSerDeletado() {
        return !temLivros();
    }

    // ===== MÉTODOS DE UTILIDADE =====
    
    /**
     * REPRESENTAÇÃO TEXTUAL PERSONALIZADA
     * 
     * Sobrescreve toString() para debug e logs mais informativos.
     * Inclui apenas informações essenciais para evitar vazamento de dados.
     */
    @Override
    public String toString() {
        return String.format("Autor{id=%d, nome='%s', email='%s', disponivel=%s, livros=%d}", 
            idAutor, nome, email, disponibilidade, quantidadeLivros());
    }
}