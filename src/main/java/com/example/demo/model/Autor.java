package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Define a classe como uma Entidade JPA (mapeada para uma tabela no DB)
@Entity
// Gera getters para todos os campos (necessário para acesso aos dados)
@Getter
// Gera o construtor sem argumentos (OBRIGATÓRIO pelo JPA)
@NoArgsConstructor
// Mapeia esta entidade para a tabela "autor" no DB
@Table(name = "autor")
// Gera o método toString, incluindo explicitamente apenas os campos marcados com @ToString.Include
@ToString(onlyExplicitlyIncluded = true)
// Gera os métodos equals e hashCode, usando explicitamente apenas os campos marcados com @EqualsAndHashCode.Include
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Autor {


    // ----------------------------------------------------------------------
    // 1. CAMPOS DE IDENTIFICAÇÃO E AUDITORIA
    // ----------------------------------------------------------------------

    // Chave Primária (PK)
    @Id
    // Valor é gerado automaticamente pelo banco de dados (auto-incremento)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAutor")
    @ToString.Include // Inclui no método toString
    private Long idAutor;

    // Identificador Externo: UUID (Globalmente Único)
    @Column(name = "uuid", unique = true, nullable = false)
    @EqualsAndHashCode.Include // Inclui no equals/hashCode
    private UUID uuid;

    // ----------------------------------------------------------------------
    // 2. CAMPOS DE DADOS
    // ----------------------------------------------------------------------

    @Column(name = "nome", nullable = false)
    @ToString.Include
    private String nome;

    @Column(name = "email", nullable = false, unique = true)
    @ToString.Include
    private String email;

    @Column(name = "cep", nullable = false)
    @ToString.Include
    private String cep;

    @Column(name = "telefone", nullable = true)
    @ToString.Include
    private String telefone;


    // ----------------------------------------------------------------------
    // 3. RELACIONAMENTO MANY-TO-MANY (M:N) - O Autor é o DONO
    // ----------------------------------------------------------------------

    // Define o relacionamento M:N
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    // Garante que o JPA apenas propague as operações de salvar/atualizar.
    // É CRUCIAL NÃO USAR REMOVE aqui.

    // Define a Tabela de Junção (Regra do Dono)
    @JoinTable(
            name = "livro-autor", // Nome da tabela intermediária no DB
            // Coluna que aponta para ESTA entidade (Autor)
            joinColumns = @JoinColumn(name = "autor_id"),
            // Coluna que aponta para a OUTRA entidade (Livro)
            inverseJoinColumns = @JoinColumn(name = "livro_id")
    )
    // Coleção de Livros: Usamos Set para garantir que não haja duplicatas
    @ToString.Exclude
    private Set<Livro> livrosSet = new HashSet<>();

    // ----------------------------------------------------------------------
    // 4. CICLO DE VIDA E CONSTRUTORES/MÉTODOS DE DOMÍNIO
    // ----------------------------------------------------------------------

    // Método que é executado imediatamente antes de a entidade ser persistida pela primeira vez
    @PrePersist
    public void prePersist() {
        // Se o UUID não foi setado (primeira vez), ele é gerado automaticamente
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    // Setter de Domínio/Manutenção: Necessário para o MapStruct/JPA ao ATUALIZAR a entidade

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }


    // Construtor Manual: Usado pelo Service para criar um NOVO Autor (recebe dados do DTO)


    public Autor(String nome, String email, String cep, String telefone) {
        this.nome = nome;
        this.email = email;
        this.cep = cep;
        this.telefone = telefone;
    }

    // Método de Domínio: A ÚNICA forma de adicionar um livro à coleção de forma segura
    public void atribuirLivroAoAutor(Livro livro){
        // Usa o método .add() da coleção, que o Hibernate rastreia corretamente
        this.livrosSet.add(livro);
    }
}