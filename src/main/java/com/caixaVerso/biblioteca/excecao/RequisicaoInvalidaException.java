package com.caixaVerso.biblioteca.excecao;

import jakarta.ws.rs.core.Response;

/**
 * Erro 400: os dados enviados pelo cliente violam uma regra de negócio.
 */
public class RequisicaoInvalidaException extends ApiException {

    /**
     * Cria o erro 400 com a mensagem informada.
     *
     * @param mensagem detalhes da validação
     */
    public RequisicaoInvalidaException(String mensagem) {
        super(Response.Status.BAD_REQUEST, mensagem);
    }
}
