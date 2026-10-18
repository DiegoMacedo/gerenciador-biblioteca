package com.caixaVerso.biblioteca.servico;

import com.caixaVerso.biblioteca.excecao.ConflitoException;
import com.caixaVerso.biblioteca.excecao.RecursoNaoEncontradoException;
import com.caixaVerso.biblioteca.modelo.Genero;
import com.caixaVerso.biblioteca.rest.dto.GeneroRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio e armazenamento em memória dos gêneros.
 *
 * <p>Os dados somem quando a aplicação reinicia. O {@link ConcurrentHashMap}
 * permite leituras e escritas concorrentes e o {@link AtomicLong} gera ids
 * únicos mesmo com várias requisições simultâneas.</p>
 */
@ApplicationScoped
public class GeneroService {

    private final Map<Long, Genero> generos = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final ValidadorPatch validadorPatch;
    private final Instance<LivroService> livroService;

    /**
     * Cria o serviço e carrega alguns gêneros iniciais.
     *
     * <p>{@link Instance} adia a obtenção do {@link LivroService} para evitar
     * dependência circular entre os serviços.</p>
     *
     * @param validadorPatch validador usado no PATCH
     * @param livroService   referência tardia ao serviço de livros
     */
    public GeneroService(ValidadorPatch validadorPatch, Instance<LivroService> livroService) {
        this.validadorPatch = validadorPatch;
        this.livroService = livroService;
        criar(new GeneroRequest("Fantasia", "Histórias com elementos mágicos e mundos imaginários"));
        criar(new GeneroRequest("Ficção Científica", "Narrativas sobre ciência, tecnologia e futuros possíveis"));
        criar(new GeneroRequest("Romance", "Histórias centradas em relacionamentos e sentimentos"));
        criar(new GeneroRequest("Suspense", "Tramas de mistério, tensão e investigação"));
    }

    /**
     * Lista os gêneros em ordem alfabética.
     *
     * @return gêneros cadastrados
     */
    public List<Genero> listar() {
        return generos.values().stream()
                .sorted(Comparator.comparing(Genero::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Busca um gênero pelo identificador.
     *
     * @param id identificador do gênero
     * @return gênero encontrado
     * @throws RecursoNaoEncontradoException quando o gênero não existe
     */
    public Genero buscar(Long id) {
        Genero genero = generos.get(id);
        if (genero == null) {
            throw new RecursoNaoEncontradoException("Gênero", id);
        }
        return genero;
    }

    /**
     * Cria um gênero após verificar se o nome já está em uso.
     *
     * @param dados dados validados do gênero
     * @return gênero criado
     * @throws ConflitoException quando já existe um gênero com o mesmo nome
     */
    public Genero criar(GeneroRequest dados) {
        String nome = dados.nome().trim();
        verificarNomeDisponivel(nome, null);
        Genero genero = new Genero(sequencia.incrementAndGet(), nome, normalizarTexto(dados.descricao()));
        generos.put(genero.getId(), genero);
        return genero;
    }

    /**
     * PUT: substitui todos os campos do gênero.
     *
     * @param id    identificador do gênero
     * @param dados dados completos e validados
     * @return gênero atualizado
     * @throws ConflitoException quando o novo nome pertence a outro gênero
     */
    public Genero atualizar(Long id, GeneroRequest dados) {
        Genero genero = buscar(id);
        String nome = dados.nome().trim();
        verificarNomeDisponivel(nome, id);
        genero.setNome(nome);
        genero.setDescricao(normalizarTexto(dados.descricao()));
        return genero;
    }

    /**
     * PATCH: altera somente os campos enviados.
     *
     * @param id    identificador do gênero
     * @param dados campos que devem ser alterados
     * @return gênero atualizado
     * @throws ConflitoException quando o novo nome pertence a outro gênero
     */
    public Genero atualizarParcialmente(Long id, GeneroRequest dados) {
        Genero genero = buscar(id);
        validadorPatch.validarCamposInformados(dados);

        if (dados.nome() != null) {
            String nome = dados.nome().trim();
            verificarNomeDisponivel(nome, id);
            genero.setNome(nome);
        }
        if (dados.descricao() != null) {
            genero.setDescricao(dados.descricao().trim());
        }
        return genero;
    }

    /**
     * Remove um gênero que não possua livros associados.
     *
     * @param id identificador do gênero
     * @throws RecursoNaoEncontradoException quando o gênero não existe
     * @throws ConflitoException quando existem livros usando esse gênero
     */
    public void remover(Long id) {
        buscar(id);
        if (livroService.get().existeLivroDoGenero(id)) {
            throw new ConflitoException("O gênero não pode ser removido porque possui livros associados.");
        }
        generos.remove(id);
    }

    /**
     * Verifica se o nome já pertence a outro gênero.
     *
     * @param nome      nome normalizado
     * @param idAtual   id do próprio gênero durante a atualização; {@code null} na criação
     * @throws ConflitoException quando o nome já está em uso
     */
    private void verificarNomeDisponivel(String nome, Long idAtual) {
        boolean emUso = generos.values().stream()
                .anyMatch(genero -> genero.getNome().equalsIgnoreCase(nome)
                        && !genero.getId().equals(idAtual));
        if (emUso) {
            throw new ConflitoException("Já existe um gênero com o nome " + nome + ".");
        }
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
