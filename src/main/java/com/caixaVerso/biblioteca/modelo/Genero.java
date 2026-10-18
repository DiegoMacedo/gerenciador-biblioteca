package com.caixaVerso.biblioteca.modelo;

/**
 * Gênero literário usado para classificar os livros do acervo.
 */
public class Genero {

    private Long id;
    private String nome;
    private String descricao;

    /**
     * Cria um gênero com todos os seus dados.
     *
     * @param id        identificador gerado pelo servidor
     * @param nome      nome do gênero
     * @param descricao descrição opcional
     */
    public Genero(Long id, String nome, String descricao) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
    }

    /**
     * Retorna o identificador do gênero.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o nome do gênero.
     *
     * @return nome do gênero
     */
    public String getNome() {
        return nome;
    }

    /**
     * Altera o nome do gênero.
     *
     * @param nome novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna a descrição do gênero.
     *
     * @return descrição ou {@code null}
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Altera a descrição do gênero.
     *
     * @param descricao nova descrição
     */
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
