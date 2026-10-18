package com.caixaVerso.biblioteca.modelo;

import java.time.LocalDate;

/**
 * Autor de um ou mais livros do acervo.
 */
public class Autor {

    private Long id;
    private String nome;
    private String nacionalidade;
    private LocalDate dataNascimento;

    /**
     * Cria um autor com todos os seus dados.
     *
     * @param id             identificador gerado pelo servidor
     * @param nome           nome completo
     * @param nacionalidade  nacionalidade opcional
     * @param dataNascimento data de nascimento opcional
     */
    public Autor(Long id, String nome, String nacionalidade, LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.nacionalidade = nacionalidade;
        this.dataNascimento = dataNascimento;
    }

    /**
     * Retorna o identificador do autor.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o nome do autor.
     *
     * @return nome completo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Altera o nome do autor.
     *
     * @param nome novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Retorna a nacionalidade do autor.
     *
     * @return nacionalidade ou {@code null}
     */
    public String getNacionalidade() {
        return nacionalidade;
    }

    /**
     * Altera a nacionalidade do autor.
     *
     * @param nacionalidade nova nacionalidade
     */
    public void setNacionalidade(String nacionalidade) {
        this.nacionalidade = nacionalidade;
    }

    /**
     * Retorna a data de nascimento do autor.
     *
     * @return data de nascimento ou {@code null}
     */
    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    /**
     * Altera a data de nascimento do autor.
     *
     * @param dataNascimento nova data de nascimento
     */
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}
