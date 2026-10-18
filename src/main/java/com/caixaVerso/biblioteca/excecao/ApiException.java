package com.caixaVerso.biblioteca.excecao;

import jakarta.ws.rs.core.Response;

/**
 * Exceção base da API: cada subclasse conhece o status HTTP que representa.
 *
 * <p>O {@link com.caixaVerso.biblioteca.rest.ApiExceptionMapper} transforma
 * qualquer subclasse em uma resposta JSON padronizada.</p>
 */
public abstract class ApiException extends RuntimeException {

    private final Response.Status status;

    /**
     * Cria a exceção com o status HTTP e a mensagem informados.
     *
     * @param status   status HTTP devolvido ao cliente
     * @param mensagem detalhes do erro
     */
    protected ApiException(Response.Status status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    /**
     * Retorna o status HTTP associado à exceção.
     *
     * @return status HTTP da resposta
     */
    public Response.Status getStatus() {
        return status;
    }
}
