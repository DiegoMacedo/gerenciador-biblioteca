package com.caixaVerso.biblioteca.modelo;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Livro do acervo da biblioteca.
 *
 * <p>O livro pertence a um gênero e pode ter vários autores. A quantidade
 * disponível é alterada pelos empréstimos e devoluções.</p>
 */
public class Livro {

    private Long id;
    private String titulo;
    private String isbn;
    private Integer anoPublicacao;
    private Integer quantidadeTotal;
    private Integer quantidadeDisponivel;
    private Genero genero;
    private Set<Autor> autores;

    /**
     * Cria um livro com todos os seus dados.
     *
     * @param id                   identificador gerado pelo servidor
     * @param titulo               título do livro
     * @param isbn                 ISBN normalizado
     * @param anoPublicacao        ano de publicação
     * @param quantidadeTotal      total de exemplares
     * @param quantidadeDisponivel exemplares disponíveis
     * @param genero               gênero do livro
     * @param autores              autores do livro
     */
    public Livro(Long id, String titulo, String isbn, Integer anoPublicacao,
                 Integer quantidadeTotal, Integer quantidadeDisponivel,
                 Genero genero, Set<Autor> autores) {
        this.id = id;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.quantidadeTotal = quantidadeTotal;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.genero = genero;
        this.autores = new LinkedHashSet<>(autores);
    }

    /**
     * Retorna o identificador do livro.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o título do livro.
     *
     * @return título
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Altera o título do livro.
     *
     * @param titulo novo título
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Retorna o ISBN do livro.
     *
     * @return ISBN sem hífens
     */
    public String getIsbn() {
        return isbn;
    }

    /**
     * Altera o ISBN do livro.
     *
     * @param isbn novo ISBN normalizado
     */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    /**
     * Retorna o ano de publicação.
     *
     * @return ano de publicação
     */
    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    /**
     * Altera o ano de publicação.
     *
     * @param anoPublicacao novo ano
     */
    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    /**
     * Retorna a quantidade total de exemplares.
     *
     * @return quantidade total
     */
    public Integer getQuantidadeTotal() {
        return quantidadeTotal;
    }

    /**
     * Altera a quantidade total de exemplares.
     *
     * @param quantidadeTotal nova quantidade total
     */
    public void setQuantidadeTotal(Integer quantidadeTotal) {
        this.quantidadeTotal = quantidadeTotal;
    }

    /**
     * Retorna a quantidade disponível para empréstimo.
     *
     * @return quantidade disponível
     */
    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    /**
     * Altera a quantidade disponível para empréstimo.
     *
     * @param quantidadeDisponivel nova quantidade disponível
     */
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    /**
     * Retorna o gênero do livro.
     *
     * @return gênero associado
     */
    public Genero getGenero() {
        return genero;
    }

    /**
     * Altera o gênero do livro.
     *
     * @param genero novo gênero
     */
    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    /**
     * Retorna os autores do livro.
     *
     * @return autores associados
     */
    public Set<Autor> getAutores() {
        return autores;
    }

    /**
     * Substitui os autores do livro.
     *
     * @param autores novos autores
     */
    public void setAutores(Set<Autor> autores) {
        this.autores = new LinkedHashSet<>(autores);
    }
}
