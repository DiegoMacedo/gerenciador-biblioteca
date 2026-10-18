package com.caixaVerso.biblioteca.rest;

import io.restassured.http.ContentType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static io.restassured.RestAssured.given;

/**
 * Métodos auxiliares para criar recursos únicos nos testes de integração.
 *
 * <p>Como os dados ficam em memória e são compartilhados entre os testes,
 * todos os nomes e ISBNs gerados aqui são únicos.</p>
 */
final class TestFixtures {

    private static final AtomicLong ISBN = new AtomicLong(1_000_000_000_000L);

    /**
     * Impede a instanciação da classe utilitária.
     */
    private TestFixtures() {
    }

    /**
     * Cria um gênero com nome único.
     *
     * @return identificador do gênero criado
     */
    static long criarGenero() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", "Gênero " + UUID.randomUUID()))
                .when().post("/generos")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /**
     * Cria um autor com nome único.
     *
     * @return identificador do autor criado
     */
    static long criarAutor() {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", "Autor " + UUID.randomUUID()))
                .when().post("/autores")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /**
     * Cria um livro com os relacionamentos informados.
     *
     * @param generoId        identificador do gênero
     * @param autorId         identificador do autor
     * @param quantidadeTotal quantidade de exemplares
     * @return identificador do livro criado
     */
    static long criarLivro(long generoId, long autorId, int quantidadeTotal) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "titulo", "Livro " + UUID.randomUUID(),
                        "isbn", isbnUnico(),
                        "anoPublicacao", 2020,
                        "quantidadeTotal", quantidadeTotal,
                        "generoId", generoId,
                        "autoresIds", new long[]{autorId}
                ))
                .when().post("/livros")
                .then()
                .statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    /**
     * Gera um ISBN numérico de 13 caracteres.
     *
     * @return ISBN único
     */
    static String isbnUnico() {
        return String.valueOf(ISBN.incrementAndGet());
    }
}
