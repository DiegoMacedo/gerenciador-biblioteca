package com.caixaVerso.biblioteca.rest.dto;

/**
 * Corpo padrão das respostas de erro da API.
 *
 * @param status código HTTP
 * @param erro   mensagem explicando o erro
 */
public record ErroDTO(int status, String erro) {
}
