package com.caixaVerso.biblioteca.rest;

import com.caixaVerso.biblioteca.modelo.Livro;
import com.caixaVerso.biblioteca.rest.dto.LivroRequest;
import com.caixaVerso.biblioteca.servico.LivroService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

/**
 * CRUD do acervo de livros.
 *
 * <pre>
 * GET    /livros       -> 200 lista (filtros opcionais por título, gênero e autor)
 * GET    /livros/{id}  -> 200 livro | 404
 * POST   /livros       -> 201 livro criado + Location | 400 | 404 | 409
 * PUT    /livros/{id}  -> 200 livro atualizado (todos os campos) | 400 | 404 | 409
 * PATCH  /livros/{id}  -> 200 livro atualizado (só campos enviados) | 400 | 404 | 409
 * DELETE /livros/{id}  -> 204 sem corpo | 404 | 409
 * </pre>
 */
@Path("/livros")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Livros", description = "Cadastro, consulta e manutenção do acervo")
public class LivroResource {

    private final LivroService service;

    /**
     * Recebe o serviço de livros por injeção de dependência.
     *
     * @param service serviço de livros
     */
    public LivroResource(LivroService service) {
        this.service = service;
    }

    /**
     * Lista livros com filtros opcionais.
     *
     * @param titulo   trecho do título
     * @param generoId identificador do gênero
     * @param autorId  identificador do autor
     * @return livros encontrados
     */
    @GET
    @Operation(
            summary = "Lista livros",
            description = "Retorna o acervo, permitindo filtrar por título, gênero e autor"
    )
    @APIResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<Livro> listar(
            @Parameter(description = "Trecho do título")
            @QueryParam("titulo") String titulo,
            @Parameter(description = "Identificador do gênero")
            @QueryParam("generoId") Long generoId,
            @Parameter(description = "Identificador do autor")
            @QueryParam("autorId") Long autorId) {
        return service.listar(titulo, generoId, autorId);
    }

    /**
     * Busca um livro pelo id.
     *
     * @param id identificador do livro
     * @return livro encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Busca um livro por id")
    @APIResponse(responseCode = "200", description = "Livro encontrado")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    public Livro buscar(
            @Parameter(description = "Identificador do livro", required = true)
            @PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Cria um livro.
     *
     * @param dados   dados validados
     * @param uriInfo informações da URI da requisição
     * @return resposta 201 com o livro criado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cria um livro")
    @APIResponse(responseCode = "201", description = "Livro criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Gênero ou autor não encontrado")
    @APIResponse(responseCode = "409", description = "ISBN já cadastrado")
    public Response criar(@NotNull @Valid LivroRequest dados, @Context UriInfo uriInfo) {
        Livro livro = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(livro.getId().toString()).build();
        return Response.created(location).entity(livro).build();
    }

    /**
     * PUT: substitui todos os campos do livro.
     *
     * @param id    identificador do livro
     * @param dados dados completos e validados
     * @return livro atualizado
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza um livro", description = "Substitui todos os campos do livro")
    @APIResponse(responseCode = "200", description = "Livro atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Livro, gênero ou autor não encontrado")
    @APIResponse(responseCode = "409", description = "ISBN já cadastrado ou quantidade incompatível")
    public Livro atualizar(
            @Parameter(description = "Identificador do livro", required = true)
            @PathParam("id") Long id,
            @NotNull @Valid LivroRequest dados) {
        return service.atualizar(id, dados);
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do livro
     * @param dados campos a alterar
     * @return livro atualizado
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza parcialmente um livro")
    @APIResponse(responseCode = "200", description = "Livro atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Livro, gênero ou autor não encontrado")
    @APIResponse(responseCode = "409", description = "ISBN já cadastrado ou quantidade incompatível")
    public Livro atualizarParcialmente(
            @Parameter(description = "Identificador do livro", required = true)
            @PathParam("id") Long id,
            LivroRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    /**
     * Remove um livro sem empréstimos registrados.
     *
     * @param id identificador do livro
     * @return resposta 204 sem corpo
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um livro")
    @APIResponse(responseCode = "204", description = "Livro removido")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    @APIResponse(responseCode = "409", description = "Livro possui empréstimos registrados")
    public Response remover(
            @Parameter(description = "Identificador do livro", required = true)
            @PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
