package com.caixaVerso.biblioteca.rest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Dados enviados pelo cliente no POST, PUT e PATCH de livros.
 *
 * @param titulo          título do livro, obrigatório no POST/PUT
 * @param isbn            ISBN com 10 ou 13 caracteres, podendo conter hífens
 * @param anoPublicacao   ano de publicação
 * @param quantidadeTotal quantidade total de exemplares
 * @param generoId        identificador do gênero
 * @param autoresIds      identificadores dos autores
 */
public record LivroRequest(
        @NotBlank(message = "titulo é obrigatório")
        @Size(max = 150, message = "titulo deve ter no máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "isbn é obrigatório")
        @Pattern(
                regexp = "^[0-9Xx-]{10,17}$",
                message = "isbn deve conter apenas dígitos, X e hífens (10 a 17 caracteres)"
        )
        String isbn,

        @NotNull(message = "anoPublicacao é obrigatório")
        @Min(value = 1450, message = "anoPublicacao deve ser maior ou igual a 1450")
        @Max(value = 2100, message = "anoPublicacao deve ser menor ou igual a 2100")
        Integer anoPublicacao,

        @NotNull(message = "quantidadeTotal é obrigatória")
        @Positive(message = "quantidadeTotal deve ser maior que zero")
        Integer quantidadeTotal,

        @NotNull(message = "generoId é obrigatório")
        @Positive(message = "generoId deve ser positivo")
        Long generoId,

        @NotEmpty(message = "autoresIds deve conter pelo menos um autor")
        Set<@NotNull(message = "autor id não pode ser nulo")
                @Positive(message = "autor id deve ser positivo") Long> autoresIds
) {
}
