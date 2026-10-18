package com.caixaVerso.biblioteca.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Configuração global da documentação OpenAPI da aplicação.
 *
 * <p>A interface Swagger UI é publicada em {@code /q/swagger-ui} e o
 * documento OpenAPI em {@code /q/openapi}.</p>
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Gerenciador de Biblioteca API",
                version = "1.0.0",
                description = """
                        API REST para gerenciamento de uma biblioteca.

                        Permite cadastrar gêneros, autores e livros, além de
                        registrar empréstimos e devoluções com controle
                        automático da quantidade disponível no acervo.

                        Nesta versão os dados ficam em memória e são perdidos
                        quando a aplicação é reiniciada.
                        """,
                contact = @Contact(
                        name = "Equipe Gerenciador de Biblioteca",
                        email = "contato@biblioteca.exemplo"
                ),
                license = @License(name = "Apache 2.0")
        ),
        tags = {
                @Tag(name = "Gêneros", description = "Cadastro e consulta de gêneros literários"),
                @Tag(name = "Autores", description = "Cadastro e consulta de autores"),
                @Tag(name = "Livros", description = "Cadastro, consulta e manutenção do acervo"),
                @Tag(name = "Empréstimos", description = "Retirada e devolução de exemplares")
        }
)
public class OpenApiConfig {
}
