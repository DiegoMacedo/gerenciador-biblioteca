package com.caixaVerso.biblioteca.servico;

import com.caixaVerso.biblioteca.excecao.ConflitoException;
import com.caixaVerso.biblioteca.excecao.RecursoNaoEncontradoException;
import com.caixaVerso.biblioteca.excecao.RequisicaoInvalidaException;
import com.caixaVerso.biblioteca.modelo.Autor;
import com.caixaVerso.biblioteca.modelo.Genero;
import com.caixaVerso.biblioteca.modelo.Livro;
import com.caixaVerso.biblioteca.rest.dto.LivroRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio e armazenamento em memória do acervo de livros.
 */
@ApplicationScoped
public class LivroService {

    private final Map<Long, Livro> livros = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;
    private final GeneroService generoService;
    private final AutorService autorService;
    private final Instance<EmprestimoService> emprestimoService;

    /**
     * Cria o serviço e carrega os livros iniciais.
     *
     * <p>O {@link EmprestimoService} é obtido por {@link Instance} para evitar
     * dependência circular: o serviço de empréstimos também conhece o de livros.</p>
     *
     * @param validadorPatch    validador usado no PATCH
     * @param generoService     serviço de gêneros
     * @param autorService      serviço de autores
     * @param emprestimoService referência tardia ao serviço de empréstimos
     */
    public LivroService(ValidadorPatch validadorPatch, GeneroService generoService,
                        AutorService autorService, Instance<EmprestimoService> emprestimoService) {
        this.validadorPatch = validadorPatch;
        this.generoService = generoService;
        this.autorService = autorService;
        this.emprestimoService = emprestimoService;

        criar(new LivroRequest("O Hobbit", "9788595084742", 1937, 3, 1L, Set.of(1L)));
        criar(new LivroRequest("1984", "9788535914849", 1949, 4, 2L, Set.of(2L)));
        criar(new LivroRequest("Dom Casmurro", "9788594318602", 1899, 2, 3L, Set.of(3L)));
        criar(new LivroRequest("A Mão Esquerda da Escuridão", "9788599296493", 1969, 2, 2L, Set.of(4L)));
        criar(new LivroRequest("O Assassinato no Expresso do Oriente", "9788525057518", 1934, 3, 4L, Set.of(5L)));
    }

    /**
     * Lista livros com filtros opcionais.
     *
     * @param titulo   trecho do título, sem diferenciar maiúsculas de minúsculas
     * @param generoId identificador do gênero
     * @param autorId  identificador do autor
     * @return livros que atendem aos filtros, ordenados por id
     */
    public List<Livro> listar(String titulo, Long generoId, Long autorId) {
        String tituloNormalizado = titulo == null ? null : titulo.trim().toLowerCase(Locale.ROOT);

        return livros.values().stream()
                .filter(livro -> tituloNormalizado == null || tituloNormalizado.isBlank()
                        || livro.getTitulo().toLowerCase(Locale.ROOT).contains(tituloNormalizado))
                .filter(livro -> generoId == null || livro.getGenero().getId().equals(generoId))
                .filter(livro -> autorId == null || livro.getAutores().stream()
                        .anyMatch(autor -> autor.getId().equals(autorId)))
                .sorted(Comparator.comparing(Livro::getId))
                .toList();
    }

    /**
     * Busca um livro pelo identificador.
     *
     * @param id identificador do livro
     * @return livro encontrado
     * @throws RecursoNaoEncontradoException quando o livro não existe
     */
    public Livro buscar(Long id) {
        Livro livro = livros.get(id);
        if (livro == null) {
            throw new RecursoNaoEncontradoException("Livro", id);
        }
        return livro;
    }

    /**
     * Cria um livro, validando ISBN, gênero e autores.
     *
     * @param dados dados validados do livro
     * @return livro criado
     * @throws ConflitoException        quando o ISBN já está cadastrado
     * @throws RecursoNaoEncontradoException quando gênero ou autor não existe
     */
    public Livro criar(LivroRequest dados) {
        validarAnoPublicacao(dados.anoPublicacao());
        String isbn = normalizarIsbn(dados.isbn());
        verificarIsbnDisponivel(isbn, null);

        Genero genero = generoService.buscar(dados.generoId());
        Set<Autor> autores = carregarAutores(dados.autoresIds());

        Livro livro = new Livro(
                sequencia.incrementAndGet(),
                dados.titulo().trim(),
                isbn,
                dados.anoPublicacao(),
                dados.quantidadeTotal(),
                dados.quantidadeTotal(),
                genero,
                autores
        );
        livros.put(livro.getId(), livro);
        return livro;
    }

    /**
     * PUT: substitui todos os dados do livro e recalcula a quantidade disponível.
     *
     * @param id    identificador do livro
     * @param dados dados completos e validados
     * @return livro atualizado
     * @throws ConflitoException quando o ISBN pertence a outro livro ou a nova
     *                           quantidade é menor que os empréstimos ativos
     */
    public Livro atualizar(Long id, LivroRequest dados) {
        Livro livro = buscar(id);
        validarAnoPublicacao(dados.anoPublicacao());
        String isbn = normalizarIsbn(dados.isbn());
        verificarIsbnDisponivel(isbn, id);

        long emprestimosAtivos = emprestimoService.get().contarEmprestimosAtivosDoLivro(id);
        if (dados.quantidadeTotal() < emprestimosAtivos) {
            throw new ConflitoException("A quantidade total não pode ser menor que a quantidade de "
                    + "empréstimos ativos (" + emprestimosAtivos + ").");
        }

        livro.setTitulo(dados.titulo().trim());
        livro.setIsbn(isbn);
        livro.setAnoPublicacao(dados.anoPublicacao());
        livro.setQuantidadeTotal(dados.quantidadeTotal());
        livro.setQuantidadeDisponivel((int) (dados.quantidadeTotal() - emprestimosAtivos));
        livro.setGenero(generoService.buscar(dados.generoId()));
        livro.setAutores(carregarAutores(dados.autoresIds()));
        return livro;
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do livro
     * @param dados campos que devem ser alterados
     * @return livro atualizado
     * @throws ConflitoException quando o ISBN pertence a outro livro ou a nova
     *                           quantidade é menor que os empréstimos ativos
     */
    public Livro atualizarParcialmente(Long id, LivroRequest dados) {
        Livro livro = buscar(id);
        validadorPatch.validarCamposInformados(dados);

        if (dados.titulo() != null) {
            livro.setTitulo(dados.titulo().trim());
        }
        if (dados.isbn() != null) {
            String isbn = normalizarIsbn(dados.isbn());
            verificarIsbnDisponivel(isbn, id);
            livro.setIsbn(isbn);
        }
        if (dados.anoPublicacao() != null) {
            validarAnoPublicacao(dados.anoPublicacao());
            livro.setAnoPublicacao(dados.anoPublicacao());
        }
        if (dados.generoId() != null) {
            livro.setGenero(generoService.buscar(dados.generoId()));
        }
        if (dados.autoresIds() != null) {
            livro.setAutores(carregarAutores(dados.autoresIds()));
        }
        if (dados.quantidadeTotal() != null) {
            long emprestimosAtivos = emprestimoService.get().contarEmprestimosAtivosDoLivro(id);
            if (dados.quantidadeTotal() < emprestimosAtivos) {
                throw new ConflitoException("A quantidade total não pode ser menor que a quantidade de "
                        + "empréstimos ativos (" + emprestimosAtivos + ").");
            }
            livro.setQuantidadeTotal(dados.quantidadeTotal());
            livro.setQuantidadeDisponivel((int) (dados.quantidadeTotal() - emprestimosAtivos));
        }
        return livro;
    }

    /**
     * Remove um livro que não tenha nenhum empréstimo registrado.
     *
     * @param id identificador do livro
     * @throws RecursoNaoEncontradoException quando o livro não existe
     * @throws ConflitoException quando existe histórico de empréstimos do livro
     */
    public void remover(Long id) {
        buscar(id);
        if (emprestimoService.get().existeEmprestimoDoLivro(id)) {
            throw new ConflitoException("O livro não pode ser removido porque possui empréstimos registrados.");
        }
        livros.remove(id);
    }

    /**
     * Informa se existe algum livro associado ao gênero.
     *
     * @param generoId identificador do gênero
     * @return {@code true} quando há pelo menos um livro do gênero
     */
    public boolean existeLivroDoGenero(Long generoId) {
        return livros.values().stream()
                .anyMatch(livro -> livro.getGenero().getId().equals(generoId));
    }

    /**
     * Informa se existe algum livro associado ao autor.
     *
     * @param autorId identificador do autor
     * @return {@code true} quando há pelo menos um livro do autor
     */
    public boolean existeLivroDoAutor(Long autorId) {
        return livros.values().stream()
                .anyMatch(livro -> livro.getAutores().stream()
                        .anyMatch(autor -> autor.getId().equals(autorId)));
    }

    /**
     * Garante que o ano de publicação não está no futuro.
     *
     * @param anoPublicacao ano informado
     * @throws RequisicaoInvalidaException quando o ano é posterior ao ano atual
     */
    private void validarAnoPublicacao(Integer anoPublicacao) {
        if (anoPublicacao > LocalDate.now().getYear()) {
            throw new RequisicaoInvalidaException("anoPublicacao não pode ser posterior ao ano atual.");
        }
    }

    /**
     * Remove hífens do ISBN e valida o tamanho de 10 ou 13 caracteres.
     *
     * @param isbnBruto ISBN informado na requisição
     * @return ISBN normalizado em maiúsculas
     * @throws RequisicaoInvalidaException quando o tamanho é inválido
     */
    private String normalizarIsbn(String isbnBruto) {
        String isbn = isbnBruto.trim()
                .replace("-", "")
                .toUpperCase(Locale.ROOT);
        if (isbn.length() != 10 && isbn.length() != 13) {
            throw new RequisicaoInvalidaException("isbn deve possuir 10 ou 13 caracteres após remover os hífens.");
        }
        return isbn;
    }

    /**
     * Verifica se o ISBN já pertence a outro livro.
     *
     * @param isbn     ISBN normalizado
     * @param idAtual  id do próprio livro na atualização; {@code null} na criação
     * @throws ConflitoException quando o ISBN já está em uso
     */
    private void verificarIsbnDisponivel(String isbn, Long idAtual) {
        boolean emUso = livros.values().stream()
                .anyMatch(livro -> livro.getIsbn().equals(isbn)
                        && !livro.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um livro com o ISBN " + isbn + ".");
        }
    }

    /**
     * Carrega todos os autores informados.
     *
     * @param autoresIds identificadores dos autores
     * @return autores encontrados
     * @throws RecursoNaoEncontradoException quando algum autor não existe
     */
    private Set<Autor> carregarAutores(Set<Long> autoresIds) {
        Set<Autor> autores = new LinkedHashSet<>();
        for (Long autorId : autoresIds) {
            autores.add(autorService.buscar(autorId));
        }
        return autores;
    }
}
