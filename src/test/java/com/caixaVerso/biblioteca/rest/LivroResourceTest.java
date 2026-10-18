package com.caixaVerso.biblioteca.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testes dos endpoints de livros.
 */
@QuarkusTest
class LivroResourceTest {

    /**
     * Executa criação, filtros, atualizações e remoção de um livro.
     */
    @Test
    void deveExecutarFluxoCompletoDeLivro() {
        long generoId = TestFixtures.criarGenero();
        long autorId = TestFixtures.criarAutor();
        String titulo = "Livro " + UUID.randomUUID();
        String isbn = TestFixtures.isbnUnico();

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "titulo", titulo,
                        "isbn", isbn,
                        "anoPublicacao", 2020,
                        "quantidadeTotal", 3,
                        "generoId", generoId,
                        "autoresIds", new long[]{autorId}
                ))
                .when().post("/livros")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("quantidadeDisponivel", equalTo(3))
                .body("genero.id", equalTo((int) generoId))
                .body("autores[0].id", equalTo((int) autorId))
                .extract().path("id");

        given()
                .queryParam("titulo", titulo.substring(0, 10))
                .queryParam("generoId", generoId)
                .queryParam("autorId", autorId)
                .when().get("/livros")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(id));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("titulo", titulo + " - PATCH", "quantidadeTotal", 5))
                .when().patch("/livros/" + id)
                .then()
                .statusCode(200)
                .body("titulo", equalTo(titulo + " - PATCH"))
                .body("quantidadeTotal", equalTo(5))
                .body("quantidadeDisponivel", equalTo(5));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "titulo", titulo + " - PUT",
                        "isbn", isbn,
                        "anoPublicacao", 2021,
                        "quantidadeTotal", 4,
                        "generoId", generoId,
                        "autoresIds", new long[]{autorId}
                ))
                .when().put("/livros/" + id)
                .then()
                .statusCode(200)
                .body("titulo", equalTo(titulo + " - PUT"))
                .body("anoPublicacao", equalTo(2021))
                .body("quantidadeDisponivel", equalTo(4));

        given().when().delete("/livros/" + id).then().statusCode(204);
        given().when().get("/livros/" + id).then().statusCode(404);
    }

    /**
     * Verifica conflito de ISBN, referências inexistentes e PUT incompleto.
     */
    @Test
    void deveValidarDadosDoLivro() {
        long generoId = TestFixtures.criarGenero();
        long autorId = TestFixtures.criarAutor();
        String isbn = TestFixtures.isbnUnico();

        Map<String, Object> livro = Map.of(
                "titulo", "Livro " + UUID.randomUUID(),
                "isbn", isbn,
                "anoPublicacao", 2020,
                "quantidadeTotal", 1,
                "generoId", generoId,
                "autoresIds", new long[]{autorId}
        );

        given()
                .contentType(ContentType.JSON)
                .body(livro)
                .when().post("/livros")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(livro)
                .when().post("/livros")
                .then()
                .statusCode(409);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "titulo", "Gênero inexistente",
                        "isbn", TestFixtures.isbnUnico(),
                        "anoPublicacao", 2020,
                        "quantidadeTotal", 1,
                        "generoId", 999999,
                        "autoresIds", new long[]{autorId}
                ))
                .when().post("/livros")
                .then()
                .statusCode(404);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("titulo", "Somente o título"))
                .when().put("/livros/1")
                .then()
                .statusCode(400);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("anoPublicacao", 3000))
                .when().patch("/livros/1")
                .then()
                .statusCode(400);
    }
}
