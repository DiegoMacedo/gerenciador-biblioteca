package com.caixaVerso.biblioteca.rest;

import com.caixaVerso.biblioteca.modelo.Autor;
import com.caixaVerso.biblioteca.rest.dto.AutorRequest;
import com.caixaVerso.biblioteca.servico.AutorService;
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
 * CRUD de autores.
 *
 * <pre>
 * GET    /autores       -> 200 lista
 * GET    /autores/{id}  -> 200 autor | 404
 * POST   /autores       -> 201 autor criado + Location | 400
 * PUT    /autores/{id}  -> 200 autor atualizado (todos os campos) | 400 | 404
 * PATCH  /autores/{id}  -> 200 autor atualizado (só campos enviados) | 400 | 404
 * DELETE /autores/{id}  -> 204 sem corpo | 404 | 409
 * </pre>
 */
@Path("/autores")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Autores", description = "Cadastro e consulta de autores")
public class AutorResource {

    private final AutorService service;

    /**
     * Recebe o serviço de autores por injeção de dependência.
     *
     * @param service serviço de autores
     */
    public AutorResource(AutorService service) {
        this.service = service;
    }

    /**
     * Lista todos os autores.
     *
     * @return autores cadastrados
     */
    @GET
    @Operation(summary = "Lista autores", description = "Retorna todos os autores em ordem alfabética")
    @APIResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<Autor> listar() {
        return service.listar();
    }

    /**
     * Busca um autor pelo id.
     *
     * @param id identificador do autor
     * @return autor encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Busca um autor por id")
    @APIResponse(responseCode = "200", description = "Autor encontrado")
    @APIResponse(responseCode = "404", description = "Autor não encontrado")
    public Autor buscar(
            @Parameter(description = "Identificador do autor", required = true)
            @PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Cria um autor.
     *
     * @param dados   dados validados
     * @param uriInfo informações da URI da requisição
     * @return resposta 201 com o autor criado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cria um autor")
    @APIResponse(responseCode = "201", description = "Autor criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    public Response criar(@NotNull @Valid AutorRequest dados, @Context UriInfo uriInfo) {
        Autor autor = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(autor.getId().toString()).build();
        return Response.created(location).entity(autor).build();
    }

    /**
     * PUT: substitui todos os campos do autor.
     *
     * @param id    identificador do autor
     * @param dados dados completos e validados
     * @return autor atualizado
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza um autor", description = "Substitui todos os campos do autor")
    @APIResponse(responseCode = "200", description = "Autor atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Autor não encontrado")
    public Autor atualizar(
            @Parameter(description = "Identificador do autor", required = true)
            @PathParam("id") Long id,
            @NotNull @Valid AutorRequest dados) {
        return service.atualizar(id, dados);
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do autor
     * @param dados campos a alterar
     * @return autor atualizado
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza parcialmente um autor")
    @APIResponse(responseCode = "200", description = "Autor atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Autor não encontrado")
    public Autor atualizarParcialmente(
            @Parameter(description = "Identificador do autor", required = true)
            @PathParam("id") Long id,
            AutorRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    /**
     * Remove um autor sem livros associados.
     *
     * @param id identificador do autor
     * @return resposta 204 sem corpo
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um autor")
    @APIResponse(responseCode = "204", description = "Autor removido")
    @APIResponse(responseCode = "404", description = "Autor não encontrado")
    @APIResponse(responseCode = "409", description = "Autor possui livros associados")
    public Response remover(
            @Parameter(description = "Identificador do autor", required = true)
            @PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
