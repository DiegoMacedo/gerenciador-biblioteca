package com.caixaVerso.biblioteca.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Testes dos endpoints de gêneros e autores.
 */
@QuarkusTest
class CatalogoResourceTest {

    /**
     * Verifica se os dados iniciais ficam disponíveis.
     */
    @Test
    void deveCarregarDadosIniciais() {
        given()
                .when().get("/generos")
                .then()
                .statusCode(200)
                .body("nome", hasItem("Fantasia"));

        given()
                .when().get("/autores")
                .then()
                .statusCode(200)
                .body("nome", hasItem("Machado de Assis"));

        given()
                .when().get("/livros")
                .then()
                .statusCode(200)
                .body("titulo", hasItem("O Hobbit"));
    }

    /**
     * Executa o ciclo completo de CRUD de um gênero.
     */
    @Test
    void deveExecutarCrudDeGenero() {
        String nome = "Gênero " + UUID.randomUUID();

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome, "descricao", "Descrição inicial"))
                .when().post("/generos")
                .then()
                .statusCode(201)
                .header("Location", containsString("/generos/"))
                .body("id", notNullValue())
                .body("nome", equalTo(nome))
                .extract().path("id");

        given()
                .when().get("/generos/" + id)
                .then()
                .statusCode(200)
                .body("descricao", equalTo("Descrição inicial"));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome + " atualizado", "descricao", "Descrição do PUT"))
                .when().put("/generos/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo(nome + " atualizado"));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("descricao", "Somente o PATCH"))
                .when().patch("/generos/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo(nome + " atualizado"))
                .body("descricao", equalTo("Somente o PATCH"));

        given().when().delete("/generos/" + id).then().statusCode(204);
        given().when().get("/generos/" + id).then().statusCode(404);
    }

    /**
     * Executa o ciclo completo de CRUD de um autor.
     */
    @Test
    void deveExecutarCrudDeAutor() {
        String nome = "Autor " + UUID.randomUUID();

        Integer id = given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome, "nacionalidade", "Brasil", "dataNascimento", "1980-05-20"))
                .when().post("/autores")
                .then()
                .statusCode(201)
                .body("nome", equalTo(nome))
                .extract().path("id");

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nacionalidade", "Portugal"))
                .when().patch("/autores/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo(nome))
                .body("nacionalidade", equalTo("Portugal"));

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome + " II", "nacionalidade", "Brasil", "dataNascimento", "1981-06-21"))
                .when().put("/autores/" + id)
                .then()
                .statusCode(200)
                .body("nome", equalTo(nome + " II"));

        given().when().delete("/autores/" + id).then().statusCode(204);
        given().when().get("/autores/" + id).then().statusCode(404);
    }

    /**
     * Impede remover gênero e autor que possuem livros associados.
     */
    @Test
    void deveImpedirRemocaoDeGeneroEAutorEmUso() {
        long generoId = TestFixtures.criarGenero();
        long autorId = TestFixtures.criarAutor();
        TestFixtures.criarLivro(generoId, autorId, 1);

        given()
                .when().delete("/generos/" + generoId)
                .then()
                .statusCode(409);

        given()
                .when().delete("/autores/" + autorId)
                .then()
                .statusCode(409);
    }

    /**
     * Verifica conflito de nome repetido e erros de validação.
     */
    @Test
    void deveValidarConflitosEEntradasInvalidas() {
        String nome = "Gênero " + UUID.randomUUID();

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome))
                .when().post("/generos")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", nome.toUpperCase()))
                .when().post("/generos")
                .then()
                .statusCode(409);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("nome", ""))
                .when().post("/generos")
                .then()
                .statusCode(400);

        given()
                .contentType(ContentType.TEXT)
                .body("nome=Teste")
                .when().post("/generos")
                .then()
                .statusCode(415);

        given()
                .when().get("/generos/999999")
                .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("erro", containsString("999999"));
    }
}
