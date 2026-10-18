package com.caixaVerso.biblioteca.rest;

import com.caixaVerso.biblioteca.modelo.Genero;
import com.caixaVerso.biblioteca.rest.dto.GeneroRequest;
import com.caixaVerso.biblioteca.servico.GeneroService;
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
 * CRUD de gêneros literários.
 *
 * <pre>
 * GET    /generos       -> 200 lista
 * GET    /generos/{id}  -> 200 gênero | 404
 * POST   /generos       -> 201 gênero criado + Location | 400 | 409
 * PUT    /generos/{id}  -> 200 gênero atualizado (todos os campos) | 400 | 404 | 409
 * PATCH  /generos/{id}  -> 200 gênero atualizado (só campos enviados) | 400 | 404 | 409
 * DELETE /generos/{id}  -> 204 sem corpo | 404 | 409
 * </pre>
 */
@Path("/generos")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Gêneros", description = "Cadastro e consulta de gêneros literários")
public class GeneroResource {

    private final GeneroService service;

    /**
     * Recebe o serviço de gêneros por injeção de dependência.
     *
     * @param service serviço de gêneros
     */
    public GeneroResource(GeneroService service) {
        this.service = service;
    }

    /**
     * Lista todos os gêneros.
     *
     * @return gêneros cadastrados
     */
    @GET
    @Operation(summary = "Lista gêneros", description = "Retorna todos os gêneros em ordem alfabética")
    @APIResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<Genero> listar() {
        return service.listar();
    }

    /**
     * Busca um gênero pelo id.
     *
     * @param id identificador do gênero
     * @return gênero encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Busca um gênero por id")
    @APIResponse(responseCode = "200", description = "Gênero encontrado")
    @APIResponse(responseCode = "404", description = "Gênero não encontrado")
    public Genero buscar(
            @Parameter(description = "Identificador do gênero", required = true)
            @PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Cria um gênero.
     *
     * <p>Retorna {@link Response} para controlar o status 201 e o header Location.</p>
     *
     * @param dados   dados validados
     * @param uriInfo informações da URI da requisição
     * @return resposta 201 com o gênero criado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cria um gênero")
    @APIResponse(responseCode = "201", description = "Gênero criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "409", description = "Nome já cadastrado")
    public Response criar(@NotNull @Valid GeneroRequest dados, @Context UriInfo uriInfo) {
        Genero genero = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(genero.getId().toString()).build();
        return Response.created(location).entity(genero).build();
    }

    /**
     * PUT: substitui todos os campos do gênero.
     *
     * @param id    identificador do gênero
     * @param dados dados completos e validados
     * @return gênero atualizado
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza um gênero", description = "Substitui todos os campos do gênero")
    @APIResponse(responseCode = "200", description = "Gênero atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Gênero não encontrado")
    @APIResponse(responseCode = "409", description = "Nome já cadastrado")
    public Genero atualizar(
            @Parameter(description = "Identificador do gênero", required = true)
            @PathParam("id") Long id,
            @NotNull @Valid GeneroRequest dados) {
        return service.atualizar(id, dados);
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do gênero
     * @param dados campos a alterar
     * @return gênero atualizado
     */
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Atualiza parcialmente um gênero")
    @APIResponse(responseCode = "200", description = "Gênero atualizado")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Gênero não encontrado")
    @APIResponse(responseCode = "409", description = "Nome já cadastrado")
    public Genero atualizarParcialmente(
            @Parameter(description = "Identificador do gênero", required = true)
            @PathParam("id") Long id,
            GeneroRequest dados) {
        return service.atualizarParcialmente(id, dados);
    }

    /**
     * Remove um gênero sem livros associados.
     *
     * @param id identificador do gênero
     * @return resposta 204 sem corpo
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um gênero")
    @APIResponse(responseCode = "204", description = "Gênero removido")
    @APIResponse(responseCode = "404", description = "Gênero não encontrado")
    @APIResponse(responseCode = "409", description = "Gênero possui livros associados")
    public Response remover(
            @Parameter(description = "Identificador do gênero", required = true)
            @PathParam("id") Long id) {
        service.remover(id);
        return Response.noContent().build();
    }
}
