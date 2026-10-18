package com.caixaVerso.biblioteca.rest.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados enviados pelo cliente para registrar um empréstimo.
 *
 * @param livroId               livro que será emprestado
 * @param nomeLeitor            leitor que está retirando o livro
 * @param dataDevolucaoPrevista data combinada para devolução
 */
public record EmprestimoRequest(
        @NotNull(message = "livroId é obrigatório")
        @Positive(message = "livroId deve ser positivo")
        Long livroId,

        @NotBlank(message = "nomeLeitor é obrigatório")
        @Size(max = 120, message = "nomeLeitor deve ter no máximo 120 caracteres")
        String nomeLeitor,

        @NotNull(message = "dataDevolucaoPrevista é obrigatória")
        @FutureOrPresent(message = "dataDevolucaoPrevista não pode estar no passado")
        LocalDate dataDevolucaoPrevista
) {
}
