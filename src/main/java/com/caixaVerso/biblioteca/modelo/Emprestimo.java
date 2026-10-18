package com.caixaVerso.biblioteca.modelo;

import java.time.LocalDate;

/**
 * Empréstimo de um exemplar de livro para um leitor.
 */
public class Emprestimo {

    private Long id;
    private Livro livro;
    private String nomeLeitor;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucao;

    /**
     * Cria um empréstimo com todos os seus dados.
     *
     * @param id                     identificador gerado pelo servidor
     * @param livro                  livro emprestado
     * @param nomeLeitor             nome do leitor
     * @param dataEmprestimo         data da retirada
     * @param dataDevolucaoPrevista  data combinada para devolução
     * @param dataDevolucao          data efetiva da devolução ou {@code null}
     */
    public Emprestimo(Long id, Livro livro, String nomeLeitor,
                      LocalDate dataEmprestimo, LocalDate dataDevolucaoPrevista,
                      LocalDate dataDevolucao) {
        this.id = id;
        this.livro = livro;
        this.nomeLeitor = nomeLeitor;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoPrevista = dataDevolucaoPrevista;
        this.dataDevolucao = dataDevolucao;
    }

    /**
     * Retorna o identificador do empréstimo.
     *
     * @return identificador único
     */
    public Long getId() {
        return id;
    }

    /**
     * Retorna o livro emprestado.
     *
     * @return livro associado
     */
    public Livro getLivro() {
        return livro;
    }

    /**
     * Retorna o nome do leitor.
     *
     * @return nome do leitor
     */
    public String getNomeLeitor() {
        return nomeLeitor;
    }

    /**
     * Retorna a data em que o empréstimo foi realizado.
     *
     * @return data da retirada
     */
    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    /**
     * Retorna a data prevista para devolução.
     *
     * @return data combinada para devolução
     */
    public LocalDate getDataDevolucaoPrevista() {
        return dataDevolucaoPrevista;
    }

    /**
     * Retorna a data efetiva da devolução.
     *
     * @return data da devolução ou {@code null} quando o livro ainda está emprestado
     */
    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    /**
     * Registra a devolução do livro.
     *
     * @param dataDevolucao data em que o livro foi devolvido
     */
    public void setDataDevolucao(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
    }

    /**
     * Calcula e retorna o status atual do empréstimo.
     *
     * @return DEVOLVIDO quando há data de devolução, ATRASADO quando o prazo
     *         venceu ou ATIVO quando ainda está no prazo
     */
    public StatusEmprestimo getStatus() {
        if (dataDevolucao != null) {
            return StatusEmprestimo.DEVOLVIDO;
        }
        return dataDevolucaoPrevista.isBefore(LocalDate.now())
                ? StatusEmprestimo.ATRASADO
                : StatusEmprestimo.ATIVO;
    }
}
