package com.caixaVerso.biblioteca.servico;

import com.caixaVerso.biblioteca.excecao.ConflitoException;
import com.caixaVerso.biblioteca.excecao.RecursoNaoEncontradoException;
import com.caixaVerso.biblioteca.excecao.RequisicaoInvalidaException;
import com.caixaVerso.biblioteca.modelo.Emprestimo;
import com.caixaVerso.biblioteca.modelo.Livro;
import com.caixaVerso.biblioteca.modelo.StatusEmprestimo;
import com.caixaVerso.biblioteca.rest.dto.EmprestimoRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Regras de negócio e armazenamento em memória dos empréstimos.
 */
@ApplicationScoped
public class EmprestimoService {

    private final Map<Long, Emprestimo> emprestimos = new ConcurrentHashMap<>();
    private final AtomicLong sequencia = new AtomicLong();
    private final LivroService livroService;

    /**
     * Recebe o serviço de livros para consultar disponibilidade e atualizar o acervo.
     *
     * @param livroService serviço responsável pelos livros
     */
    public EmprestimoService(LivroService livroService) {
        this.livroService = livroService;
    }

    /**
     * Lista empréstimos com filtros opcionais de status e livro.
     *
     * @param status  status desejado; {@code null} lista todos
     * @param livroId identificador do livro
     * @return empréstimos que atendem aos filtros
     */
    public List<Emprestimo> listar(StatusEmprestimo status, Long livroId) {
        return emprestimos.values().stream()
                .filter(emprestimo -> status == null || emprestimo.getStatus() == status)
                .filter(emprestimo -> livroId == null || emprestimo.getLivro().getId().equals(livroId))
                .sorted(Comparator.comparing(Emprestimo::getDataDevolucaoPrevista)
                        .thenComparing(Emprestimo::getId))
                .toList();
    }

    /**
     * Busca um empréstimo pelo identificador.
     *
     * @param id identificador do empréstimo
     * @return empréstimo encontrado
     * @throws RecursoNaoEncontradoException quando o empréstimo não existe
     */
    public Emprestimo buscar(Long id) {
        Emprestimo emprestimo = emprestimos.get(id);
        if (emprestimo == null) {
            throw new RecursoNaoEncontradoException("Empréstimo", id);
        }
        return emprestimo;
    }

    /**
     * Registra um empréstimo e reduz a quantidade disponível do livro.
     *
     * <p>O método é sincronizado para evitar que duas requisições concorrentes
     * vejam o mesmo exemplar como disponível.</p>
     *
     * @param dados dados validados do empréstimo
     * @return empréstimo criado
     * @throws ConflitoException quando não há exemplar disponível
     * @throws RequisicaoInvalidaException quando a data prevista está no passado
     */
    public synchronized Emprestimo criar(EmprestimoRequest dados) {
        Livro livro = livroService.buscar(dados.livroId());
        if (livro.getQuantidadeDisponivel() <= 0) {
            throw new ConflitoException("Não há exemplares disponíveis para o livro informado.");
        }
        if (dados.dataDevolucaoPrevista().isBefore(LocalDate.now())) {
            throw new RequisicaoInvalidaException("dataDevolucaoPrevista não pode estar no passado.");
        }

        Emprestimo emprestimo = new Emprestimo(
                sequencia.incrementAndGet(),
                livro,
                dados.nomeLeitor().trim(),
                LocalDate.now(),
                dados.dataDevolucaoPrevista(),
                null
        );
        emprestimos.put(emprestimo.getId(), emprestimo);
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() - 1);
        return emprestimo;
    }

    /**
     * Registra a devolução e devolve o exemplar ao acervo.
     *
     * @param id identificador do empréstimo
     * @return empréstimo com a devolução registrada
     * @throws RecursoNaoEncontradoException quando o empréstimo não existe
     * @throws ConflitoException quando o empréstimo já foi devolvido
     */
    public synchronized Emprestimo devolver(Long id) {
        Emprestimo emprestimo = buscar(id);
        if (emprestimo.getDataDevolucao() != null) {
            throw new ConflitoException("Este empréstimo já foi devolvido.");
        }

        emprestimo.setDataDevolucao(LocalDate.now());
        Livro livro = emprestimo.getLivro();
        livro.setQuantidadeDisponivel(livro.getQuantidadeDisponivel() + 1);
        return emprestimo;
    }

    /**
     * Conta quantos empréstimos do livro ainda não foram devolvidos.
     *
     * @param livroId identificador do livro
     * @return quantidade de empréstimos ativos
     */
    public long contarEmprestimosAtivosDoLivro(Long livroId) {
        return emprestimos.values().stream()
                .filter(emprestimo -> emprestimo.getLivro().getId().equals(livroId))
                .filter(emprestimo -> emprestimo.getDataDevolucao() == null)
                .count();
    }

    /**
     * Informa se o livro possui algum empréstimo registrado, ativo ou devolvido.
     *
     * @param livroId identificador do livro
     * @return {@code true} quando existe histórico de empréstimos
     */
    public boolean existeEmprestimoDoLivro(Long livroId) {
        return emprestimos.values().stream()
                .anyMatch(emprestimo -> emprestimo.getLivro().getId().equals(livroId));
    }
}
