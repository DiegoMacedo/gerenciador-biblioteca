package com.caixaVerso.biblioteca.servico;

import com.caixaVerso.biblioteca.excecao.RequisicaoInvalidaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.stream.Collectors;

/**
 * Valida os dados de um PATCH.
 *
 * <p>No PATCH o cliente envia apenas os campos que deseja alterar, por isso
 * não usamos {@code @Valid}: as anotações reclamariam de campos ausentes.
 * Aqui apenas as violações de campos realmente enviados são consideradas.</p>
 */
@ApplicationScoped
public class ValidadorPatch {

    private final Validator validator;

    /**
     * Recebe o validador do Bean Validation via injeção de dependência.
     *
     * @param validator validador fornecido pelo Quarkus
     */
    public ValidadorPatch(Validator validator) {
        this.validator = validator;
    }

    /**
     * Valida somente os campos presentes no corpo da requisição.
     *
     * @param request objeto com os campos enviados
     * @throws RequisicaoInvalidaException quando o corpo é nulo ou algum campo enviado é inválido
     */
    public void validarCamposInformados(Object request) {
        if (request == null) {
            throw new RequisicaoInvalidaException("corpo da requisição é obrigatório");
        }
        String erros = validator.validate(request).stream()
                .filter(violacao -> violacao.getInvalidValue() != null)
                .map(ConstraintViolation::getMessage)
                .sorted()
                .collect(Collectors.joining("; "));

        if (!erros.isEmpty()) {
            throw new RequisicaoInvalidaException(erros);
        }
    }
}
