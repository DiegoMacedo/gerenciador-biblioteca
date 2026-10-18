package com.caixaVerso.biblioteca.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados enviados pelo cliente no POST, PUT e PATCH de gêneros.
 *
 * @param nome      nome do gênero, obrigatório no POST/PUT
 * @param descricao descrição opcional
 */
public record GeneroRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 80, message = "nome deve ter no máximo 80 caracteres")
        String nome,

        @Size(max = 255, message = "descricao deve ter no máximo 255 caracteres")
        String descricao
) {
}
