package com.caixaVerso.biblioteca.excecao;

import jakarta.ws.rs.core.Response;

/**
 * Erro 404: o identificador informado não corresponde a nenhum recurso.
 */
public class RecursoNaoEncontradoException extends ApiException {

    /**
     * Cria o erro 404 com o nome do recurso e o id procurado.
     *
     * @param recurso nome do recurso (Livro, Autor, Gênero etc.)
     * @param id      identificador inexistente
     */
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(Response.Status.NOT_FOUND, recurso + " com id " + id + " não encontrado(a).");
    }
}
