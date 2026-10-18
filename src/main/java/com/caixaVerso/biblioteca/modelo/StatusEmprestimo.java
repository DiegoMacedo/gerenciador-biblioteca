package com.caixaVerso.biblioteca.modelo;

/**
 * Situação de um empréstimo.
 *
 * <p>O status é calculado a partir das datas, não é armazenado.</p>
 */
public enum StatusEmprestimo {

    /** Empréstimo em aberto e dentro do prazo. */
    ATIVO,

    /** Empréstimo em aberto com a data prevista já ultrapassada. */
    ATRASADO,

    /** Livro já devolvido. */
    DEVOLVIDO
}
