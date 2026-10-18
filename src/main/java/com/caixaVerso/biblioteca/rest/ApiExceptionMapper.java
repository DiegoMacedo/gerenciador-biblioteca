package com.caixaVerso.biblioteca.rest;

import com.caixaVerso.biblioteca.excecao.ApiException;
import com.caixaVerso.biblioteca.rest.dto.ErroDTO;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Converte qualquer {@link ApiException} em resposta HTTP com o status
 * correspondente e um JSON no formato {@link ErroDTO}.
 */
@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    /**
     * Monta a resposta de erro a partir da exceção lançada pelo serviço.
     *
     * @param exception exceção de negócio
     * @return resposta HTTP com status e corpo padronizados
     */
    @Override
    public Response toResponse(ApiException exception) {
        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErroDTO(exception.getStatus().getStatusCode(), exception.getMessage()))
                .build();
    }
}
