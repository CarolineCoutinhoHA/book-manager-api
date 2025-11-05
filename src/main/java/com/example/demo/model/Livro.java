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

@Entity
@Table(name = "livro")
@Getter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_livro")
    @ToString.Include
    private Long idLivro;

    @EqualsAndHashCode.Include
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "titulo", nullable = false)
    @ToString.Include
    private String titulo;

    @Column(name = "isbn", nullable = false, unique = true)
    @ToString.Include
    private String isbn;

    @Column(name = "data_de_publicacao", nullable = false)
    @ToString.Include
    private LocalDate dataDePublicacao;

    @Column(name = "categoria", nullable = false)
    @ToString.Include
    private Categoria categoria;

    @ManyToMany(mappedBy = "livrosSet", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Autor> autores = new HashSet<>();

    @PrePersist
    public void prePersist(){
        if (uuid == null){
            uuid = UUID.randomUUID();
        }
    }

    public Livro(String titulo, String isbn, LocalDate dataDePublicacao, com.example.demo.model.Categoria categoria) {
        this.titulo = titulo;
        this.isbn = isbn;
        this.dataDePublicacao = dataDePublicacao;
        this.categoria = categoria;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDataDePublicacao(LocalDate dataDePublicacao) {
        this.dataDePublicacao = dataDePublicacao;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void corrigirIsbn(String novoIsbn){
        this.isbn = novoIsbn;
    }

    public void adicionarAutor(Autor autor){

        this.autores.add(autor);

        autor.atribuirLivroAoAutor(this);


    }



}
