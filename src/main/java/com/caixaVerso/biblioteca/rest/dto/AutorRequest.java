package com.caixaVerso.biblioteca.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados enviados pelo cliente no POST, PUT e PATCH de autores.
 *
 * @param nome           nome completo, obrigatório no POST/PUT
 * @param nacionalidade  nacionalidade opcional
 * @param dataNascimento data de nascimento opcional, sempre no passado
 */
public record AutorRequest(
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
        String nome,

        @Size(max = 80, message = "nacionalidade deve ter no máximo 80 caracteres")
        String nacionalidade,

        @Past(message = "dataNascimento deve estar no passado")
        LocalDate dataNascimento
) {
}
