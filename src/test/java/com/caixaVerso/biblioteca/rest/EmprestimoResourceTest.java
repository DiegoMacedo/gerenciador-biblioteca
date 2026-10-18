package com.caixaVerso.biblioteca.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

/**
 * Testes dos endpoints de empréstimos e devoluções.
 */
@QuarkusTest
class EmprestimoResourceTest {

    /**
     * Executa o fluxo de empréstimo, consulta, devolução e bloqueio de remoção.
     */
    @Test
    void deveExecutarFluxoCompletoDeEmprestimo() {
        long generoId = TestFixtures.criarGenero();
        long autorId = TestFixtures.criarAutor();
        long livroId = TestFixtures.criarLivro(generoId, autorId, 1);

        Integer emprestimoId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "livroId", livroId,
                        "nomeLeitor", "Leitor de Teste",
                        "dataDevolucaoPrevista", LocalDate.now().plusDays(7).toString()
                ))
                .when().post("/emprestimos")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .body("status", equalTo("ATIVO"))
                .body("dataDevolucao", nullValue())
                .extract().path("id");

        given()
                .when().get("/livros/" + livroId)
                .then()
                .statusCode(200)
                .body("quantidadeDisponivel", equalTo(0));

        given()
                .queryParam("status", "ATIVO")
                .queryParam("livroId", livroId)
                .when().get("/emprestimos")
                .then()
                .statusCode(200)
                .body("id", hasItem(emprestimoId));

        given()
                .when().post("/emprestimos/" + emprestimoId + "/devolucao")
                .then()
                .statusCode(200)
                .body("status", equalTo("DEVOLVIDO"))
                .body("dataDevolucao", notNullValue());

        given()
                .when().get("/livros/" + livroId)
                .then()
                .statusCode(200)
                .body("quantidadeDisponivel", equalTo(1));

        given()
                .queryParam("status", "DEVOLVIDO")
                .queryParam("livroId", livroId)
                .when().get("/emprestimos")
                .then()
                .statusCode(200)
                .body("id", hasItem(emprestimoId));

        given()
                .when().post("/emprestimos/" + emprestimoId + "/devolucao")
                .then()
                .statusCode(409);

        given()
                .when().delete("/livros/" + livroId)
                .then()
                .statusCode(409);
    }

    /**
     * Impede dois empréstimos simultâneos do único exemplar.
     */
    @Test
    void deveImpedirEmprestimoSemExemplarDisponivel() {
        long generoId = TestFixtures.criarGenero();
        long autorId = TestFixtures.criarAutor();
        long livroId = TestFixtures.criarLivro(generoId, autorId, 1);

        Map<String, Object> emprestimo = Map.of(
                "livroId", livroId,
                "nomeLeitor", "Primeiro Leitor",
                "dataDevolucaoPrevista", LocalDate.now().plusDays(3).toString()
        );

        given()
                .contentType(ContentType.JSON)
                .body(emprestimo)
                .when().post("/emprestimos")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(emprestimo)
                .when().post("/emprestimos")
                .then()
                .statusCode(409);
    }

    /**
     * Verifica validações, livro inexistente e empréstimo inexistente.
     */
    @Test
    void deveValidarRequisicoesDeEmprestimo() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "livroId", 1,
                        "nomeLeitor", "",
                        "dataDevolucaoPrevista", LocalDate.now().minusDays(1).toString()
                ))
                .when().post("/emprestimos")
                .then()
                .statusCode(400);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "livroId", 999999,
                        "nomeLeitor", "Leitor",
                        "dataDevolucaoPrevista", LocalDate.now().plusDays(1).toString()
                ))
                .when().post("/emprestimos")
                .then()
                .statusCode(404);

        given()
                .when().get("/emprestimos/999999")
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }
}
