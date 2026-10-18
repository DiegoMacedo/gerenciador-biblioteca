package com.caixaVerso.biblioteca.rest;

import com.caixaVerso.biblioteca.modelo.Emprestimo;
import com.caixaVerso.biblioteca.modelo.StatusEmprestimo;
import com.caixaVerso.biblioteca.rest.dto.EmprestimoRequest;
import com.caixaVerso.biblioteca.servico.EmprestimoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
 * Registro de empréstimos e devoluções.
 *
 * <pre>
 * GET  /emprestimos                  -> 200 lista (filtros por status e livro)
 * GET  /emprestimos/{id}             -> 200 empréstimo | 404
 * POST /emprestimos                  -> 201 empréstimo criado + Location | 400 | 404 | 409
 * POST /emprestimos/{id}/devolucao   -> 200 empréstimo devolvido | 404 | 409
 * </pre>
 */
@Path("/emprestimos")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Empréstimos", description = "Retirada e devolução de exemplares")
public class EmprestimoResource {

    private final EmprestimoService service;

    /**
     * Recebe o serviço de empréstimos por injeção de dependência.
     *
     * @param service serviço de empréstimos
     */
    public EmprestimoResource(EmprestimoService service) {
        this.service = service;
    }

    /**
     * Lista empréstimos com filtros opcionais.
     *
     * @param status  status desejado (ATIVO, ATRASADO ou DEVOLVIDO)
     * @param livroId identificador do livro
     * @return empréstimos encontrados
     */
    @GET
    @Operation(
            summary = "Lista empréstimos",
            description = "Retorna os empréstimos, permitindo filtrar por status e livro"
    )
    @APIResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public List<Emprestimo> listar(
            @Parameter(description = "Status do empréstimo")
            @QueryParam("status") StatusEmprestimo status,
            @Parameter(description = "Identificador do livro")
            @QueryParam("livroId") Long livroId) {
        return service.listar(status, livroId);
    }

    /**
     * Busca um empréstimo pelo id.
     *
     * @param id identificador do empréstimo
     * @return empréstimo encontrado
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Busca um empréstimo por id")
    @APIResponse(responseCode = "200", description = "Empréstimo encontrado")
    @APIResponse(responseCode = "404", description = "Empréstimo não encontrado")
    public Emprestimo buscar(
            @Parameter(description = "Identificador do empréstimo", required = true)
            @PathParam("id") Long id) {
        return service.buscar(id);
    }

    /**
     * Registra um empréstimo.
     *
     * @param dados   dados validados
     * @param uriInfo informações da URI da requisição
     * @return resposta 201 com o empréstimo criado
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Registra um empréstimo")
    @APIResponse(responseCode = "201", description = "Empréstimo registrado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados inválidos")
    @APIResponse(responseCode = "404", description = "Livro não encontrado")
    @APIResponse(responseCode = "409", description = "Não há exemplares disponíveis")
    public Response criar(@NotNull @Valid EmprestimoRequest dados, @Context UriInfo uriInfo) {
        Emprestimo emprestimo = service.criar(dados);
        URI location = uriInfo.getAbsolutePathBuilder().path(emprestimo.getId().toString()).build();
        return Response.created(location).entity(emprestimo).build();
    }

    /**
     * Registra a devolução de um empréstimo.
     *
     * @param id identificador do empréstimo
     * @return empréstimo com a devolução registrada
     */
    @POST
    @Path("/{id}/devolucao")
    @Operation(summary = "Registra a devolução de um empréstimo")
    @APIResponse(responseCode = "200", description = "Devolução registrada")
    @APIResponse(responseCode = "404", description = "Empréstimo não encontrado")
    @APIResponse(responseCode = "409", description = "Empréstimo já devolvido")
    public Emprestimo devolver(
            @Parameter(description = "Identificador do empréstimo", required = true)
            @PathParam("id") Long id) {
        return service.devolver(id);
    }
}
