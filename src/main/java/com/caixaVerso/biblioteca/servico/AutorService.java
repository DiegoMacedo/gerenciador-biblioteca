package com.caixaVerso.biblioteca.servico;

import com.caixaVerso.biblioteca.excecao.ConflitoException;
import com.caixaVerso.biblioteca.excecao.RecursoNaoEncontradoException;
import com.caixaVerso.biblioteca.modelo.Autor;
import com.caixaVerso.biblioteca.rest.dto.AutorRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio e armazenamento em memória dos autores.
 */
@ApplicationScoped
public class AutorService {

    private final Map<Long, Autor> autores = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;
    private final Instance<LivroService> livroService;

    /**
     * Cria o serviço e carrega alguns autores iniciais.
     *
     * @param validadorPatch validador usado no PATCH
     * @param livroService   referência tardia ao serviço de livros
     */
    public AutorService(ValidadorPatch validadorPatch, Instance<LivroService> livroService) {
        this.validadorPatch = validadorPatch;
        this.livroService = livroService;
        criar(new AutorRequest("J. R. R. Tolkien", "Reino Unido", LocalDate.of(1892, 1, 3)));
        criar(new AutorRequest("George Orwell", "Reino Unido", LocalDate.of(1903, 6, 25)));
        criar(new AutorRequest("Machado de Assis", "Brasil", LocalDate.of(1839, 6, 21)));
        criar(new AutorRequest("Ursula K. Le Guin", "Estados Unidos", LocalDate.of(1929, 10, 21)));
        criar(new AutorRequest("Agatha Christie", "Reino Unido", LocalDate.of(1890, 9, 15)));
    }

    /**
     * Lista os autores em ordem alfabética.
     *
     * @return autores cadastrados
     */
    public List<Autor> listar() {
        return autores.values().stream()
                .sorted(Comparator.comparing(Autor::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Busca um autor pelo identificador.
     *
     * @param id identificador do autor
     * @return autor encontrado
     * @throws RecursoNaoEncontradoException quando o autor não existe
     */
    public Autor buscar(Long id) {
        Autor autor = autores.get(id);
        if (autor == null) {
            throw new RecursoNaoEncontradoException("Autor", id);
        }
        return autor;
    }

    /**
     * Cria um autor com os dados informados.
     *
     * @param dados dados validados do autor
     * @return autor criado
     */
    public Autor criar(AutorRequest dados) {
        Autor autor = new Autor(
                sequencia.incrementAndGet(),
                dados.nome().trim(),
                normalizarTexto(dados.nacionalidade()),
                dados.dataNascimento()
        );
        autores.put(autor.getId(), autor);
        return autor;
    }

    /**
     * PUT: substitui todos os campos do autor.
     *
     * @param id    identificador do autor
     * @param dados dados completos e validados
     * @return autor atualizado
     */
    public Autor atualizar(Long id, AutorRequest dados) {
        Autor autor = buscar(id);
        autor.setNome(dados.nome().trim());
        autor.setNacionalidade(normalizarTexto(dados.nacionalidade()));
        autor.setDataNascimento(dados.dataNascimento());
        return autor;
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do autor
     * @param dados campos que devem ser alterados
     * @return autor atualizado
     */
    public Autor atualizarParcialmente(Long id, AutorRequest dados) {
        Autor autor = buscar(id);
        validadorPatch.validarCamposInformados(dados);

        if (dados.nome() != null) {
            autor.setNome(dados.nome().trim());
        }
        if (dados.nacionalidade() != null) {
            autor.setNacionalidade(dados.nacionalidade().trim());
        }
        if (dados.dataNascimento() != null) {
            autor.setDataNascimento(dados.dataNascimento());
        }
        return autor;
    }

    /**
     * Remove um autor que não possua livros associados.
     *
     * @param id identificador do autor
     * @throws RecursoNaoEncontradoException quando o autor não existe
     * @throws ConflitoException quando existem livros usando esse autor
     */
    public void remover(Long id) {
        buscar(id);
        if (livroService.get().existeLivroDoAutor(id)) {
            throw new ConflitoException("O autor não pode ser removido porque possui livros associados.");
        }
        autores.remove(id);
    }

    /**
     * Converte texto opcional em {@code null} quando ele é vazio ou apenas espaços.
     *
     * @param texto texto informado
     * @return texto sem espaços nas extremidades ou {@code null}
     */
    private String normalizarTexto(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
