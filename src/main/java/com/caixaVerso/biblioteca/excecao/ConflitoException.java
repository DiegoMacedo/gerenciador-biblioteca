package com.caixaVerso.biblioteca.excecao;

import jakarta.ws.rs.core.Response;

/**
 * Erro 409: a requisição é válida, mas conflita com o estado atual
 * (nome ou ISBN repetido, livro sem exemplares, operação bloqueada etc.).
 */
public class ConflitoException extends ApiException {

    /**
     * Cria um conflito com a mensagem informada.
     *
     * @param mensagem detalhes do conflito
     */
    public ConflitoException(String mensagem) {
        super(Response.Status.CONFLICT, mensagem);
    }
}
