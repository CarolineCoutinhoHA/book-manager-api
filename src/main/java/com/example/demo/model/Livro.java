package com.example.demo.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * =====================================================
 * ENTIDADE LIVRO - MODELO DE DOMÍNIO
 * =====================================================
 * 
 * RESPONSABILIDADES:
 * - Representa um livro no sistema de biblioteca
 * - Gerencia relacionamento Many-to-Many com Autor
 * - Implementa Soft Delete (disponibilidade)
 * - Controla regras de negócio do domínio
 * 
 * PADRÕES IMPLEMENTADOS:
 * - Domain Model: Métodos de negócio na própria entidade
 * - Soft Delete: Campo boolean ao invés de DELETE físico
 * - UUID Pattern: Identificador público separado da PK
 * - Bidirectional Mapping: Sincronização automática M:N
 * 
 * RELACIONAMENTOS:
 * - Many-to-Many com Autor (lado inverso/mappedBy)
 * - Tabela de junção: autor_livro (gerenciada por Autor)
 * 
 * REGRAS DE NEGÓCIO:
 * - ISBN deve ser único no sistema
 * - UUID gerado automaticamente no @PrePersist
 * - Disponibilidade controla visibilidade (soft delete)
 * - Relacionamento bidirecional mantido sincronizado
 */

// ----------------------------------------------------
// ANOTAÇÕES BÁSICAS DE PERSISTÊNCIA E LOMBOK
// ----------------------------------------------------
@Entity // Marca esta classe como uma entidade JPA (tabela no DB)
@Table(name = "livro") // Define o nome da tabela no banco
@Getter // Gera todos os métodos getters
@NoArgsConstructor // Gera o construtor sem argumentos (OBRIGATÓRIO para JPA)
@ToString(onlyExplicitlyIncluded = true) // Gera toString apenas para campos @ToString.Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // Gera equals/hashCode apenas para campos @EqualsAndHashCode.Include
public class Livro {

    // ----------------------------------------------------
    // 1. IDENTIFICADORES
    // ----------------------------------------------------

    @Id // Chave Primária (PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento pelo DB
    @Column(name = "id_livro")
    @ToString.Include
    private Long idLivro; // PK interna

    @EqualsAndHashCode.Include // Incluído na comparação de objetos
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid; // ID público/externo

    // ----------------------------------------------------
    // 2. CAMPOS DE DADOS E REGRAS DE NEGÓCIO (RN)
    // ----------------------------------------------------

    @Column(name = "titulo", nullable = false)
    @ToString.Include
    private String titulo;

    @Column(name = "isbn", nullable = false, unique = true) // RN: ISBN deve ser único no DB
    @ToString.Include
    private String isbn;

    @Column(name = "data_de_publicacao", nullable = false)
    @ToString.Include
    private LocalDate dataDePublicacao;

    @Column(name = "categoria", nullable = false)
    @ToString.Include
    private Categoria categoria; // Uso do nosso ENUM customizado

    @Column(name = "disponibilidade", nullable = false)
    private boolean disponibilidade = true; // Campo para SOFT DELETE (true = ativo)

    // ----------------------------------------------------
    // 3. RELACIONAMENTO MANY-TO-MANY (M:N) - Lado INVERSO
    // ----------------------------------------------------

    // @JoinTable: Define a tabela de junção autor_livro (LADO DONO)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "autor_livro",
        joinColumns = @JoinColumn(name = "id_livro"),
        inverseJoinColumns = @JoinColumn(name = "id_autor")
    )
    @ToString.Exclude // Excluímos do toString para evitar LazyInitializationException
    private final Set<Autor> autores = new HashSet<>(); // Coleção de autores

    // ----------------------------------------------------
    // 4. CICLO DE VIDA E CONSTRUTORES
    // ----------------------------------------------------

    @PrePersist // Executa ANTES de salvar a primeira vez
    public void prePersist(){
        // RN: Garante que o UUID seja gerado
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    // Construtor Manual: Usado pelo Service para criar um NOVO Livro (recebe dados do DTO)
    public Livro(String titulo, String isbn, LocalDate dataDePublicacao, Categoria categoria) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.dataDePublicacao = dataDePublicacao;
        this.categoria = categoria;
    }

    // ----------------------------------------------------
    // 5. MÉTODOS DE DOMÍNIO (ESCRITA SEGURA E RN)
    // ----------------------------------------------------

    // Setter de Domínio: Permite a atualização controlada pelo Mapper
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Setter de Domínio: Permite a atualização controlada pelo Mapper
    public void setDataDePublicacao(LocalDate dataDePublicacao) {
        this.dataDePublicacao = dataDePublicacao;
    }

    // Setter de Domínio: Permite a atualização controlada pelo Mapper
    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    // Método de Domínio (RN Imutável Flexível): Permite corrigir o ISBN
    public void corrigirIsbn(String novoIsbn){
        this.isbn = novoIsbn;
    }

    // Setter para MapStruct: Permite o mapeamento automático do ISBN na criação
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    // Método de Domínio (Gerenciamento Bidirecional M:N)
    public void adicionarAutor(Autor autor){
        // 1. Adiciona o Autor à coleção deste Livro (Lado Inverso)
        this.autores.add(autor);

        // 2. Sincroniza: Adiciona ESTE Livro ao Autor (Lado Dono) - CRUCIAL para o JPA salvar a M:N
        autor.atribuirLivroAoAutor(this);
    }

    // Método de Domínio (Soft Delete Reversível): Marca como ATIVO (disponível)
    public void disponivel (){
        this.disponibilidade = true;
    }

    // Método de Domínio (Soft Delete): Marca como INATIVO (logicamente deletado)
    public void indisponivel(){
        this.disponibilidade = false;
    }
}