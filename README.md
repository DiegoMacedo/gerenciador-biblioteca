# Gerenciador de Biblioteca (Quarkus + REST)

API REST didática para gerenciar uma biblioteca. O projeto segue a estrutura do projeto
`lms-quarkus` (aula 4), usando **serviços com dados em memória** em vez de banco de dados.

> **Importante:** nesta versão não há banco de dados. Gêneros, autores, livros e empréstimos
> ficam em `ConcurrentHashMap` dentro dos serviços. Ao reiniciar a aplicação, tudo volta aos
> dados iniciais.

## Funcionalidades

- CRUD completo de gêneros (`POST`, `GET`, `PUT`, `PATCH`, `DELETE`).
- CRUD completo de autores.
- CRUD completo de livros, com filtros por título, gênero e autor.
- Registro de empréstimos e devoluções.
- Controle automático da quantidade disponível no acervo.
- Status de empréstimo calculado: `ATIVO`, `ATRASADO` ou `DEVOLVIDO`.
- Validação dos dados com Bean Validation (`400`).
- Erros de negócio padronizados (`404` e `409`) no formato `{"status": ..., "erro": "..."}`.
- Documentação OpenAPI e Swagger UI.
- Testes de integração com `@QuarkusTest` e RestAssured.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| Java 26 | Linguagem e `maven.compiler.release` |
| Quarkus 3.40.1 | Framework da aplicação |
| Quarkus REST + Jackson | Endpoints REST e JSON |
| Hibernate Validator | Validação dos DTOs de entrada |
| SmallRye OpenAPI + Swagger UI | Documentação interativa |
| JUnit 5 + RestAssured | Testes de integração |
| ConcurrentHashMap + AtomicLong | Armazenamento e geração de ids em memória |

## Como executar

Pré-requisito: JDK 26. O Maven Wrapper já está no projeto; não é necessário instalar Maven.

### Modo de desenvolvimento

```bash
./mvnw quarkus:dev
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/q/swagger-ui>
- Documento OpenAPI: <http://localhost:8080/q/openapi>
- OpenAPI em JSON: <http://localhost:8080/q/openapi?format=json>
- Dev UI: <http://localhost:8080/q/dev/>

O modo dev recarrega a aplicação automaticamente ao alterar o código.

### Testes

```bash
./mvnw test
```

### Empacotar e executar o jar

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Limpar builds antigos

```bash
./mvnw clean test
```

## Estrutura do projeto

```
src/main/java/com/caixaVerso/biblioteca
├── config/
│   └── OpenApiConfig.java              Configuração global do Swagger/OpenAPI
├── modelo/
│   ├── Genero.java                     Gênero literário
│   ├── Autor.java                      Autor
│   ├── Livro.java                      Livro do acervo
│   ├── Emprestimo.java                 Empréstimo e cálculo do status
│   └── StatusEmprestimo.java           ATIVO, ATRASADO, DEVOLVIDO
├── excecao/
│   ├── ApiException.java               Base das exceções de negócio
│   ├── ConflitoException.java          409 Conflict
│   ├── RecursoNaoEncontradoException.java  404 Not Found
│   └── RequisicaoInvalidaException.java    400 Bad Request
├── servico/
│   ├── GeneroService.java              Regras e dados em memória dos gêneros
│   ├── AutorService.java               Regras e dados em memória dos autores
│   ├── LivroService.java               Regras e dados em memória dos livros
│   ├── EmprestimoService.java          Regras e dados em memória dos empréstimos
│   └── ValidadorPatch.java             Validação dos campos enviados no PATCH
└── rest/
    ├── ApiExceptionMapper.java         Converte ApiException em resposta JSON
    ├── GeneroResource.java             Endpoints de gêneros
    ├── AutorResource.java              Endpoints de autores
    ├── LivroResource.java              Endpoints de livros
    ├── EmprestimoResource.java         Endpoints de empréstimos
    └── dto/
        ├── GeneroRequest.java          Corpo do POST/PUT/PATCH de gênero
        ├── AutorRequest.java           Corpo do POST/PUT/PATCH de autor
        ├── LivroRequest.java           Corpo do POST/PUT/PATCH de livro
        ├── EmprestimoRequest.java      Corpo do POST de empréstimo
        └── ErroDTO.java                Formato padrão de erro
```

O arquivo `requisicoes.http` contém exemplos prontos para o HTTP Client do IntelliJ ou a
extensão REST Client do VS Code.

## Modelo de domínio

### Gênero

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | Gerado pelo servidor |
| `nome` | `String` | Obrigatório e único (sem diferenciar maiúsculas) |
| `descricao` | `String` | Opcional |

### Autor

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | Gerado pelo servidor |
| `nome` | `String` | Obrigatório |
| `nacionalidade` | `String` | Opcional |
| `dataNascimento` | `LocalDate` | Opcional, deve estar no passado |

### Livro

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | Gerado pelo servidor |
| `titulo` | `String` | Obrigatório |
| `isbn` | `String` | Obrigatório e único; armazenado sem hífens |
| `anoPublicacao` | `Integer` | Obrigatório, entre 1450 e o ano atual |
| `quantidadeTotal` | `Integer` | Obrigatório e maior que zero |
| `quantidadeDisponivel` | `Integer` | Controlada pelos empréstimos |
| `genero` | `Genero` | Um gênero existente |
| `autores` | `Set<Autor>` | Pelo menos um autor existente |

### Empréstimo

| Campo | Tipo | Observação |
|---|---|---|
| `id` | `Long` | Gerado pelo servidor |
| `livro` | `Livro` | Livro emprestado |
| `nomeLeitor` | `String` | Obrigatório |
| `dataEmprestimo` | `LocalDate` | Definida pelo servidor no momento da criação |
| `dataDevolucaoPrevista` | `LocalDate` | Obrigatória, hoje ou futura |
| `dataDevolucao` | `LocalDate` | `null` enquanto o empréstimo estiver aberto |
| `status` | `StatusEmprestimo` | Calculado a partir das datas |

## Endpoints

### Gêneros

| Método | Caminho | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/generos` | Lista todos | `200` | - |
| GET | `/generos/{id}` | Busca por id | `200` | `404` |
| POST | `/generos` | Cria | `201` + `Location` | `400`, `409`, `415` |
| PUT | `/generos/{id}` | Substitui todos os campos | `200` | `400`, `404`, `409` |
| PATCH | `/generos/{id}` | Altera só os campos enviados | `200` | `400`, `404`, `409` |
| DELETE | `/generos/{id}` | Remove | `204` | `404`, `409` |

### Autores

| Método | Caminho | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/autores` | Lista todos | `200` | - |
| GET | `/autores/{id}` | Busca por id | `200` | `404` |
| POST | `/autores` | Cria | `201` + `Location` | `400`, `415` |
| PUT | `/autores/{id}` | Substitui todos os campos | `200` | `400`, `404` |
| PATCH | `/autores/{id}` | Altera só os campos enviados | `200` | `400`, `404` |
| DELETE | `/autores/{id}` | Remove | `204` | `404`, `409` |

### Livros

| Método | Caminho | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/livros?titulo=&generoId=&autorId=` | Lista com filtros opcionais | `200` | - |
| GET | `/livros/{id}` | Busca por id | `200` | `404` |
| POST | `/livros` | Cria | `201` + `Location` | `400`, `404`, `409`, `415` |
| PUT | `/livros/{id}` | Substitui todos os campos | `200` | `400`, `404`, `409` |
| PATCH | `/livros/{id}` | Altera só os campos enviados | `200` | `400`, `404`, `409` |
| DELETE | `/livros/{id}` | Remove | `204` | `404`, `409` |

### Empréstimos

| Método | Caminho | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/emprestimos?status=&livroId=` | Lista com filtros opcionais | `200` | - |
| GET | `/emprestimos/{id}` | Busca por id | `200` | `404` |
| POST | `/emprestimos` | Registra empréstimo | `201` + `Location` | `400`, `404`, `409`, `415` |
| POST | `/emprestimos/{id}/devolucao` | Registra devolução | `200` | `404`, `409` |

Não existem `PUT`, `PATCH` ou `DELETE` para empréstimos: eles são registros históricos.
A única alteração permitida é a devolução.

## Corpos das requisições

```jsonc
// gênero
{"nome": "Aventura", "descricao": "Histórias de exploração"}

// autor
{"nome": "Júlio Verne", "nacionalidade": "França", "dataNascimento": "1828-02-08"}

// livro
{
  "titulo": "Viagem ao Centro da Terra",
  "isbn": "9788535914863",
  "anoPublicacao": 1864,
  "quantidadeTotal": 2,
  "generoId": 1,
  "autoresIds": [1]
}

// empréstimo
{
  "livroId": 2,
  "nomeLeitor": "Ana Souza",
  "dataDevolucaoPrevista": "2030-12-31"
}
```

No `PATCH`, envie apenas os campos que deseja alterar:

```jsonc
// alterar somente a quantidade total do livro 1
{"quantidadeTotal": 5}

// alterar somente a nacionalidade do autor 1
{"nacionalidade": "Reino Unido"}
```

## Exemplos com curl

### Listar o acervo

```bash
curl http://localhost:8080/livros
```

### Filtrar livros

```bash
curl "http://localhost:8080/livros?titulo=hobbit"
curl "http://localhost:8080/livros?generoId=2"
curl "http://localhost:8080/livros?autorId=1"
```

### Criar um gênero

```bash
curl -i -X POST http://localhost:8080/generos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Aventura","descricao":"Histórias de exploração"}'
```

Resposta esperada:

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/generos/5
Content-Type: application/json

{"id":5,"nome":"Aventura","descricao":"Histórias de exploração"}
```

### Criar um autor

```bash
curl -X POST http://localhost:8080/autores \
  -H "Content-Type: application/json" \
  -d '{"nome":"Júlio Verne","nacionalidade":"França","dataNascimento":"1828-02-08"}'
```

### Criar um livro

```bash
curl -X POST http://localhost:8080/livros \
  -H "Content-Type: application/json" \
  -d '{
    "titulo":"Viagem ao Centro da Terra",
    "isbn":"9788535914863",
    "anoPublicacao":1864,
    "quantidadeTotal":2,
    "generoId":5,
    "autoresIds":[6]
  }'
```

### Registrar um empréstimo

```bash
curl -X POST http://localhost:8080/emprestimos \
  -H "Content-Type: application/json" \
  -d '{"livroId":2,"nomeLeitor":"Ana Souza","dataDevolucaoPrevista":"2030-12-31"}'
```

### Devolver um empréstimo

```bash
curl -X POST http://localhost:8080/emprestimos/1/devolucao
```

### Atualizar parcialmente um livro

```bash
curl -X PATCH http://localhost:8080/livros/2 \
  -H "Content-Type: application/json" \
  -d '{"quantidadeTotal":5}'
```

### Remover um gênero

```bash
curl -i -X DELETE http://localhost:8080/generos/5
```

## Regras de negócio

1. O nome do gênero deve ser único, sem diferenciar maiúsculas de minúsculas (`409`).
2. O ISBN deve ter 10 ou 13 caracteres após remover os hífens e ser único (`400`/`409`).
3. O ano de publicação não pode ser posterior ao ano atual.
4. Todo livro precisa de um gênero existente e de pelo menos um autor existente (`404`).
5. Gêneros e autores que possuem livros não podem ser removidos (`409`).
6. Livros com qualquer empréstimo registrado não podem ser removidos (`409`).
7. Só é possível emprestar um livro com `quantidadeDisponivel > 0` (`409`).
8. Cada empréstimo reduz a quantidade disponível em 1; cada devolução aumenta em 1.
9. A data prevista de devolução deve ser hoje ou futura (`400`).
10. Um empréstimo já devolvido não pode ser devolvido novamente (`409`).
11. A quantidade total de um livro não pode ser menor que a quantidade de empréstimos ativos (`409`).
12. O status é sempre calculado:
    - `DEVOLVIDO` quando `dataDevolucao` está preenchida;
    - `ATRASADO` quando não há devolução e a data prevista já passou;
    - `ATIVO` nos demais casos.

## Formato dos erros

Erros de negócio (lançados pelos serviços) usam `ErroDTO`:

```json
{
  "status": 404,
  "erro": "Livro com id 999 não encontrado(a)."
}
```

```json
{
  "status": 409,
  "erro": "Não há exemplares disponíveis para o livro informado."
}
```

Erros de validação de `@Valid` usam o formato padrão do Quarkus, com a lista `violations`,
e status `400`.

---

# Explicação detalhada do funcionamento (linha a linha)

Esta seção explica o caminho completo de uma requisição e o papel de cada classe e método.
Ela foi escrita para quem está aprendendo Quarkus e REST.

## 1. Fluxo geral de uma requisição

```
Cliente HTTP
   |
   v
Resource (@Path, @GET, @POST...)
   |  converte JSON em *Request via Jackson
   |  valida com @Valid/Bean Validation (POST/PUT)
   v
Service (regras de negócio + Map em memória)
   |  lança ApiException (400/404/409) quando necessário
   |  devolve o modelo
   v
Jackson converte o modelo em JSON
   |
   v
Resposta HTTP (200/201/204 ou erro)
```

Quando um serviço lança `ApiException`, o `ApiExceptionMapper` captura a exceção e devolve
`ErroDTO` com o status correto. Os resources não precisam de `try/catch`.

## 2. `pom.xml`: dependências e por que cada uma existe

- `quarkus-rest-jackson`: cria os endpoints REST e serializa/desserializa JSON com Jackson.
- `quarkus-arc`: container de injeção de dependência (CDI). Permite `@ApplicationScoped` e
  injeção por construtor.
- `quarkus-hibernate-validator`: implementa Bean Validation (`@NotBlank`, `@NotNull`,
  `@Past`, `@Positive` etc.). Quando `@Valid` falha, o Quarkus responde `400`.
- `quarkus-smallrye-openapi`: gera o documento OpenAPI e publica o Swagger UI.
- `quarkus-junit` e `rest-assured` (escopo `test`): sobem a aplicação e fazem requisições
  HTTP reais nos testes.
- Não há dependências de banco de dados nesta versão.

## 3. `application.properties` linha a linha

```properties
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.info-title=Gerenciador de Biblioteca API
quarkus.smallrye-openapi.info-version=1.0.0
quarkus.jackson.fail-on-unknown-properties=false
```

- `quarkus.swagger-ui.always-include=true`: publica o Swagger UI também no jar final, não
  apenas no modo dev.
- `quarkus.smallrye-openapi.info-title=...`: título exibido no topo da documentação.
- `quarkus.smallrye-openapi.info-version=1.0.0`: versão exibida na documentação.
- `quarkus.jackson.fail-on-unknown-properties=false`: ignora propriedades desconhecidas no
  JSON. Isso permite evoluir o contrato sem quebrar clientes antigos.

## 4. Modelos: o que é devolvido em JSON

Os modelos são classes simples, sem anotações de banco. O Jackson usa os getters públicos
para montar o JSON.

### 4.1 `Genero`

```java
private Long id;
private String nome;
private String descricao;
```

- `id`: gerado pelo `AtomicLong` do serviço, nunca pelo cliente.
- `nome`: usado na regra de unicidade.
- `descricao`: opcional.

O construtor recebe todos os dados. Os setters permitem que `PUT` e `PATCH` alterem o objeto
que já está no `Map`.

### 4.2 `Autor`

```java
private Long id;
private String nome;
private String nacionalidade;
private LocalDate dataNascimento;
```

`LocalDate` é serializado pelo Jackson como `"aaaa-mm-dd"`. A data é validada no DTO com
`@Past`.

### 4.3 `Livro`

```java
private Long id;
private String titulo;
private String isbn;
private Integer anoPublicacao;
private Integer quantidadeTotal;
private Integer quantidadeDisponivel;
private Genero genero;
private Set<Autor> autores;
```

- `genero` guarda a referência para o objeto `Genero` do `GeneroService`.
- `autores` é um `LinkedHashSet`, que preserva a ordem de inserção e evita autores repetidos.
- `quantidadeDisponivel` é alterada apenas pelos empréstimos/devoluções e pela atualização da
  quantidade total.
- O construtor copia o `Set` recebido (`new LinkedHashSet<>(autores)`) para que o objeto não
  fique dependente de uma coleção externa imutável.
- `setAutores` também copia a coleção recebida.

### 4.4 `Emprestimo`

```java
private Long id;
private Livro livro;
private String nomeLeitor;
private LocalDate dataEmprestimo;
private LocalDate dataDevolucaoPrevista;
private LocalDate dataDevolucao;
```

O método `getStatus()` é a regra central:

```java
public StatusEmprestimo getStatus() {
    if (dataDevolucao != null) {
        return StatusEmprestimo.DEVOLVIDO;
    }
    return dataDevolucaoPrevista.isBefore(LocalDate.now())
            ? StatusEmprestimo.ATRASADO
            : StatusEmprestimo.ATIVO;
}
```

Linha a linha:

1. Se `dataDevolucao` não é nula, o livro já voltou: `DEVOLVIDO`.
2. Caso contrário, compara a data prevista com hoje.
3. Se a data prevista for anterior a hoje: `ATRASADO`.
4. Se for hoje ou futura: `ATIVO`.

Como o status é calculado, ele nunca fica inconsistente com as datas.

## 5. DTOs de entrada e validações

Os DTOs são `record`, ou seja, objetos imutáveis usados apenas para receber o JSON.

### `GeneroRequest`

| Campo | Anotações | Efeito |
|---|---|---|
| `nome` | `@NotBlank`, `@Size(max=80)` | Obrigatório e com até 80 caracteres |
| `descricao` | `@Size(max=255)` | Opcional, até 255 caracteres |

### `AutorRequest`

| Campo | Anotações | Efeito |
|---|---|---|
| `nome` | `@NotBlank`, `@Size(max=120)` | Obrigatório |
| `nacionalidade` | `@Size(max=80)` | Opcional |
| `dataNascimento` | `@Past` | Opcional, mas se enviada deve estar no passado |

### `LivroRequest`

| Campo | Anotações | Efeito |
|---|---|---|
| `titulo` | `@NotBlank`, `@Size(max=150)` | Obrigatório |
| `isbn` | `@NotBlank`, `@Pattern` | Obrigatório, só dígitos/X/hífens, 10 a 17 caracteres |
| `anoPublicacao` | `@NotNull`, `@Min(1450)`, `@Max(2100)` | Obrigatório; o serviço também impede ano futuro |
| `quantidadeTotal` | `@NotNull`, `@Positive` | Obrigatório e maior que zero |
| `generoId` | `@NotNull`, `@Positive` | Obrigatório |
| `autoresIds` | `@NotEmpty`, elementos `@NotNull` e `@Positive` | Pelo menos um autor |

### `EmprestimoRequest`

| Campo | Anotações | Efeito |
|---|---|---|
| `livroId` | `@NotNull`, `@Positive` | Obrigatório |
| `nomeLeitor` | `@NotBlank`, `@Size(max=120)` | Obrigatório |
| `dataDevolucaoPrevista` | `@NotNull`, `@FutureOrPresent` | Hoje ou futura |

Nos endpoints `POST` e `PUT` usamos `@Valid`. O Quarkus valida o DTO antes de chamar o
método e responde `400` se houver erro. No `PATCH` não usamos `@Valid`; o
`ValidadorPatch` faz a validação apenas dos campos enviados.

## 6. Serviços: armazenamento em memória

Cada serviço segue o mesmo padrão:

```java
private final Map<Long, X> itens = new ConcurrentHashMap<>();
private final AtomicLong sequencia = new AtomicLong();
```

- `ConcurrentHashMap`: seguro para várias requisições simultâneas.
- `AtomicLong`: gera ids únicos sem risco de dois clientes receberem o mesmo id.
- Os construtores chamam `criar(...)` para carregar dados iniciais.
- `listar()` ordena os valores antes de devolver.
- `buscar(id)` lança `RecursoNaoEncontradoException` quando o id não existe.

### 6.1 `GeneroService`

#### Construtor

```java
public GeneroService(ValidadorPatch validadorPatch, Instance<LivroService> livroService) {
    this.validadorPatch = validadorPatch;
    this.livroService = livroService;
    criar(new GeneroRequest("Fantasia", "Histórias com elementos mágicos e mundos imaginários"));
    // ...
}
```

Linha a linha:

1. Recebe o validador de PATCH.
2. Recebe um `Instance<LivroService>`, que é uma referência tardia usada apenas na remoção.
3. Cria os quatro gêneros iniciais chamando `criar`.

O `Instance` evita dependência circular: `LivroService` conhece `GeneroService` para validar
o gênero do livro, e `GeneroService` só precisa de `LivroService` quando alguém tenta remover
um gênero.

#### `criar`

```java
public Genero criar(GeneroRequest dados) {
    String nome = dados.nome().trim();
    verificarNomeDisponivel(nome, null);
    Genero genero = new Genero(sequencia.incrementAndGet(), nome, normalizarTexto(dados.descricao()));
    generos.put(genero.getId(), genero);
    return genero;
}
```

Linha a linha:

1. Remove espaços das extremidades do nome.
2. Verifica se já existe outro gênero com o mesmo nome (ignorando maiúsculas/minúsculas).
3. Cria o objeto com id novo, nome e descrição normalizada.
4. Guarda o objeto no `Map`.
5. Devolve o gênero criado para o resource montar o `201 Created`.

#### `atualizar` (PUT)

1. Busca o gênero (`404` se não existir).
2. Normaliza o novo nome.
3. Verifica a unicidade ignorando o id atual.
4. Substitui nome e descrição.
5. Devolve a entidade atualizada.

#### `atualizarParcialmente` (PATCH)

1. Busca o gênero.
2. Chama `ValidadorPatch` para validar só os campos enviados.
3. Se `nome` veio no JSON, valida unicidade e altera.
4. Se `descricao` veio no JSON, altera.
5. Campos não enviados permanecem como estavam.

#### `remover`

1. Busca o gênero.
2. Pede ao `LivroService` para verificar se existe livro usando o gênero.
3. Se existir, lança `409` (`ConflitoException`).
4. Caso contrário, remove do `Map`.

### 6.2 `AutorService`

Segue o mesmo padrão do gênero. Os pontos específicos são:

- Não há unicidade de nome, pois pessoas diferentes podem ter o mesmo nome.
- `dataNascimento` é opcional, mas validada como passada no DTO.
- Na remoção, pergunta ao `LivroService` se existe algum livro associado ao autor.

### 6.3 `LivroService`

#### Construtor

Recebe `ValidadorPatch`, `GeneroService`, `AutorService` e `Instance<EmprestimoService>`.
Depois cria os cinco livros iniciais. Cada livro referencia um gênero e um autor já criados
pelos outros serviços.

```java
criar(new LivroRequest("O Hobbit", "9788595084742", 1937, 3, 1L, Set.of(1L)));
```

- Título: `O Hobbit`
- ISBN: `9788595084742`
- Ano: `1937`
- Quantidade total: `3`
- Gênero: id `1` (`Fantasia`)
- Autor: id `1` (`J. R. R. Tolkien`)

#### `listar(titulo, generoId, autorId)`

```java
String tituloNormalizado = titulo == null ? null : titulo.trim().toLowerCase(Locale.ROOT);

return livros.values().stream()
        .filter(livro -> tituloNormalizado == null || tituloNormalizado.isBlank()
                || livro.getTitulo().toLowerCase(Locale.ROOT).contains(tituloNormalizado))
        .filter(livro -> generoId == null || livro.getGenero().getId().equals(generoId))
        .filter(livro -> autorId == null || livro.getAutores().stream()
                .anyMatch(autor -> autor.getId().equals(autorId)))
        .sorted(Comparator.comparing(Livro::getId))
        .toList();
```

Linha a linha:

1. Normaliza o título para `null` ou minúsculas.
2. Percorre todos os livros do `Map`.
3. Filtra por trecho do título, se informado.
4. Filtra pelo id do gênero, se informado.
5. Filtra pelo id do autor, se informado.
6. Ordena por id.
7. Gera uma lista imutável com `toList()`.

#### `criar` (POST)

```java
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
```

Linha a linha:

1. Valida que o ano não é futuro.
2. Remove hífens e valida o tamanho do ISBN; converte para maiúsculas.
3. Verifica se o ISBN já pertence a outro livro (`409`).
4. Busca o gênero; se não existir, `404`.
5. Carrega todos os autores; se algum não existir, `404`.
6. Cria o livro com id novo.
7. `quantidadeDisponivel` começa igual a `quantidadeTotal`.
8. Guarda no `Map`.
9. Devolve o livro para o `201 Created`.

#### `atualizar` (PUT)

1. Busca o livro.
2. Valida ano e ISBN, ignorando o próprio id na verificação de unicidade.
3. Conta empréstimos ativos do livro (`dataDevolucao == null`).
4. Se a nova quantidade total for menor que os empréstimos ativos, lança `409`.
5. Substitui título, ISBN, ano, quantidades, gênero e autores.
6. A nova quantidade disponível é `quantidadeTotal - empréstimosAtivos`.

#### `atualizarParcialmente` (PATCH)

Para cada campo enviado, executa a regra correspondente:

- `titulo`: altera.
- `isbn`: normaliza, valida unicidade, altera.
- `anoPublicacao`: valida ano e altera.
- `generoId`: busca gênero e altera.
- `autoresIds`: carrega autores e substitui a coleção.
- `quantidadeTotal`: conta empréstimos ativos, valida a quantidade e recalcula a disponível.

Campos não enviados são preservados.

#### `remover`

1. Busca o livro.
2. Verifica se existe qualquer empréstimo (ativo ou devolvido) do livro.
3. Se existir, `409`, pois o histórico seria perdido.
4. Caso contrário, remove do `Map`.

#### `existeLivroDoGenero` e `existeLivroDoAutor`

Percorrem o `Map` de livros e respondem se algum livro aponta para o gênero/autor. São usados
na remoção de gêneros e autores.

#### `normalizarIsbn`

```java
String isbn = isbnBruto.trim().replace("-", "").toUpperCase(Locale.ROOT);
if (isbn.length() != 10 && isbn.length() != 13) {
    throw new RequisicaoInvalidaException("isbn deve possuir 10 ou 13 caracteres após remover os hífens.");
}
return isbn;
```

1. Remove espaços.
2. Remove hífens.
3. Converte `x` para `X`, quando existir.
4. Garante tamanho 10 ou 13.
5. Devolve o ISBN normalizado.

### 6.4 `EmprestimoService`

#### `listar(status, livroId)`

1. Percorre todos os empréstimos.
2. Se `status` foi informado, mantém apenas os empréstimos com aquele status calculado.
3. Se `livroId` foi informado, mantém apenas os do livro.
4. Ordena por data prevista de devolução e, em caso de empate, por id.
5. Devolve a lista.

#### `criar` (POST)

```java
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
```

Linha a linha:

1. `synchronized` evita que duas requisições concorrentes vejam o mesmo exemplar disponível.
2. Busca o livro (`404` se não existir).
3. Se não há exemplar disponível, `409`.
4. Se a data prevista está no passado, `400`.
5. Cria o empréstimo com id novo, livro, leitor, data atual, data prevista e devolução nula.
6. Guarda no `Map`.
7. Reduz a quantidade disponível do livro em 1.
8. Devolve o empréstimo para o `201 Created`.

#### `devolver` (POST `/emprestimos/{id}/devolucao`)

1. Busca o empréstimo.
2. Se `dataDevolucao` já está preenchida, `409`.
3. Grava a data atual em `dataDevolucao`.
4. Aumenta a quantidade disponível do livro em 1.
5. Devolve o empréstimo atualizado com status `DEVOLVIDO`.

#### `contarEmprestimosAtivosDoLivro`

Conta empréstimos do livro cuja `dataDevolucao` é `null`. Usado pelo `LivroService` para
recalcular a quantidade disponível e impedir redução inválida da quantidade total.

#### `existeEmprestimoDoLivro`

Verifica se existe qualquer empréstimo do livro, ativo ou devolvido. Usado para impedir a
remoção de livros com histórico.

### 6.5 `ValidadorPatch`

```java
String erros = validator.validate(request).stream()
        .filter(violacao -> violacao.getInvalidValue() != null)
        .map(ConstraintViolation::getMessage)
        .sorted()
        .collect(Collectors.joining("; "));
```

Linha a linha:

1. `validator.validate(request)` aplica todas as anotações do DTO.
2. O filtro remove violações de campos que não foram enviados (valor `null`).
3. As mensagens restantes são extraídas.
4. As mensagens são ordenadas para a resposta ficar previsível.
5. São unidas com `; `.
6. Se houver alguma mensagem, o serviço lança `400`.

Assim, um PATCH com `{"nome": ""}` é inválido, mas um PATCH com `{"descricao": "nova"}`
não reclama da ausência do nome.

## 7. Resources: a camada HTTP

Os resources são finos: recebem a requisição, delegam ao serviço e devolvem o objeto. A
maioria dos métodos tem poucas linhas.

### Anotações usadas

- `@Path("/livros")`: prefixo da URL.
- `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`: método HTTP.
- `@Produces(MediaType.APPLICATION_JSON)`: a resposta é JSON.
- `@Consumes(MediaType.APPLICATION_JSON)`: o corpo precisa ser JSON nos métodos que recebem
  corpo. Se o cliente enviar `text/plain`, o Quarkus responde `415`.
- `@PathParam`: captura o `{id}` da URL.
- `@QueryParam`: captura filtros da query string.
- `@Context UriInfo`: permite montar a URL do header `Location`.
- `@Valid`: dispara Bean Validation.
- `@NotNull`: garante que o corpo existe.

### Métodos de CRUD

Para gêneros, autores e livros, o padrão é:

| Método | Linha 1 | Linha 2 | Resultado |
|---|---|---|---|
| `listar` | chama `service.listar(...)` | devolve a lista | `200` |
| `buscar` | recebe `@PathParam Long id` | chama `service.buscar(id)` | `200` |
| `criar` | valida o corpo com `@Valid` | chama `service.criar`, monta `Location` | `201` |
| `atualizar` | valida o corpo com `@Valid` | chama `service.atualizar` | `200` |
| `atualizarParcialmente` | não usa `@Valid` | o serviço valida os campos enviados | `200` |
| `remover` | chama `service.remover` | devolve `Response.noContent()` | `204` |

O `POST` devolve `Response` porque precisamos controlar o status `201` e o header `Location`:

```java
Genero genero = service.criar(dados);
URI location = uriInfo.getAbsolutePathBuilder().path(genero.getId().toString()).build();
return Response.created(location).entity(genero).build();
```

1. Cria o recurso.
2. Monta a URI com o id gerado.
3. `Response.created(location)` define `201`.
4. `.entity(genero)` define o JSON do corpo.

### Filtros de livros

```java
public List<Livro> listar(@QueryParam("titulo") String titulo,
                          @QueryParam("generoId") Long generoId,
                          @QueryParam("autorId") Long autorId) {
    return service.listar(titulo, generoId, autorId);
}
```

Os três parâmetros são opcionais. Se nenhum for enviado, todos os livros são retornados.

### Empréstimos

O `POST /emprestimos` registra a retirada. O endpoint de devolução é uma ação:

```
POST /emprestimos/{id}/devolucao
```

Não é `PUT` nem `PATCH` porque não estamos substituindo o recurso inteiro, e sim executando
uma operação de negócio sobre ele. O corpo é vazio, por isso o método não declara
`@Consumes`.

## 8. Exceções e respostas de erro

### `ApiException`

Classe abstrata que guarda um `Response.Status`. As subclasses definem o status:

| Exceção | Status | Quando |
|---|---|---|
| `RequisicaoInvalidaException` | `400` | regra de negócio violada nos dados |
| `RecursoNaoEncontradoException` | `404` | id inexistente |
| `ConflitoException` | `409` | nome/ISBN repetido, remoção bloqueada, sem exemplar etc. |

### `ApiExceptionMapper`

```java
return Response.status(exception.getStatus())
        .type(MediaType.APPLICATION_JSON)
        .entity(new ErroDTO(exception.getStatus().getStatusCode(), exception.getMessage()))
        .build();
```

1. Lê o status da exceção.
2. Define o tipo da resposta como JSON.
3. Monta `ErroDTO` com status e mensagem.
4. Devolve a resposta.

### Erros de `@Valid`

Quando um DTO de `POST`/`PUT` viola `@NotBlank`, `@Positive` etc., o Quarkus responde `400`
antes de chamar o resource. O corpo é o formato padrão do Quarkus, com a lista `violations`.

## 9. Concorrência e limitações desta versão

- `ConcurrentHashMap` protege a estrutura dos mapas, mas não torna automaticamente atômicas
  operações compostas.
- Por isso, `EmprestimoService.criar` e `devolver` são `synchronized`: duas retiradas
  simultâneas do último exemplar não podem acontecer.
- Os dados são perdidos ao reiniciar a aplicação.
- Não há autenticação/autorização.
- A quantidade disponível é mantida em memória; em uma versão com banco de dados, o ideal
  seria uma transação com lock para garantir a consistência.
- O ISBN não é validado por dígito verificador; apenas formato e tamanho são conferidos.

## 10. Testes de integração

Os testes sobem a aplicação Quarkus e fazem requisições HTTP reais com RestAssured.

Arquivos:

- `CatalogoResourceTest`: dados iniciais, CRUD de gêneros e autores, conflitos e validações.
- `LivroResourceTest`: CRUD de livro, filtros, PATCH, PUT e ISBN duplicado.
- `EmprestimoResourceTest`: empréstimo, controle de disponibilidade, devolução e erros.
- `TestFixtures`: helpers que criam gêneros, autores e livros com dados únicos.

Como os dados ficam em memória e são compartilhados entre os testes, os helpers geram nomes
com `UUID` e ISBNs sequenciais para evitar colisões.

Para rodar:

```bash
./mvnw test
```

## 11. Próximos passos sugeridos

- Substituir os `Map` por um banco H2 em memória com Hibernate/Panache, mantendo os mesmos
  endpoints e regras.
- Adicionar paginação nas listagens.
- Adicionar validação de dígito verificador do ISBN.
- Modelar leitores como uma entidade separada (`Leitor`), em vez de guardar apenas o nome.
- Criar limites de prazo e renovação de empréstimos.
- Adicionar autenticação e controle de acesso.
