# API de Tabela Tarifária de Água

API REST para gerenciar e calcular tarifas de água com base em **categorias de
consumidores** e **faixas progressivas de consumo**. O sistema é totalmente
**parametrizável**: faixas e valores ficam no banco de dados, e qualquer ajuste
reflete imediatamente nos cálculos **sem alteração de código**.

---

## Sumário

- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Modelo de dados](#modelo-de-dados)
- [Regras de negócio](#regras-de-negócio)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do banco de dados](#configuração-do-banco-de-dados)
- [Como executar](#como-executar)
- [Endpoints](#endpoints)
- [Demonstração da parametrização](#demonstração-da-parametrização)
- [Como testar](#como-testar)
- [Scripts de banco de dados](#scripts-de-banco-de-dados)

---

## Stack tecnológica

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 4 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | PostgreSQL 18 |
| Migrations | Flyway |
| Boilerplate | Lombok (`@Getter`/`@Setter`, `@RequiredArgsConstructor`) |
| Validação | Bean Validation (Jakarta) |
| Documentação | springdoc-openapi (Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, H2 (memória), Cucumber (BDD) |
| Cobertura | JaCoCo |
| Build | Maven |

---

## Arquitetura

Organização em camadas, com separação clara de responsabilidades:

```
com.sinapsis.tarifaagua
├── controller   → endpoints REST (recebe/retorna DTOs)
├── service      → regras de negócio (validação de faixas, cálculo progressivo)
├── repository   → acesso a dados (Spring Data JPA)
├── domain       → entidades e enum (TabelaTarifaria, FaixaConsumo, Categoria)
├── dto          → objetos de entrada/saída (desacoplados das entidades)
├── exception    → exceções de negócio e handler global (@RestControllerAdvice)
└── config       → configuração de documentação (OpenAPI)
```

Decisões de projeto:

- **Valores monetários em `BigDecimal`** (escala 2), nunca `double`/`float`.
- **Exclusão lógica (soft delete)**: o `DELETE` apenas marca a tabela como
  inativa, preservando histórico e impedindo seu uso em cálculos futuros.
- **Parametrização real**: nenhum valor de tarifa no código; tudo vem do banco.

---

## Modelo de dados

```
tabela_tarifaria (1) ────< (N) faixa_consumo
```

**`tabela_tarifaria`** — uma tabela tarifária completa.

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGINT (identity) | Chave primária |
| nome | VARCHAR(150) | Nome da tabela |
| data_vigencia | DATE | Data em que passa a valer |
| ativo | BOOLEAN | Soft delete (default `true`) |
| criado_em | TIMESTAMP | Auditoria |

**`faixa_consumo`** — faixa progressiva vinculada a uma tabela e a uma categoria.

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| id | BIGINT (identity) | Chave primária |
| tabela_id | BIGINT (FK) | Referência à tabela tarifária |
| categoria | VARCHAR(20) | COMERCIAL, INDUSTRIAL, PARTICULAR, PUBLICO |
| inicio | INTEGER | m³ inicial da faixa |
| fim | INTEGER | m³ final da faixa |
| valor_unitario | NUMERIC(12,2) | R$/m³ |

Integridade garantida por constraints no banco: `inicio < fim`, `inicio >= 0`,
`valor_unitario > 0` e unicidade de `(tabela_id, categoria, inicio)`.

---

## Regras de negócio

### Validação das faixas (na criação)

Para cada categoria, as faixas são validadas quanto a:

| Regra | Descrição |
|-------|-----------|
| Cobertura completa | A primeira faixa deve iniciar em `0` m³ |
| Ordem válida | Em cada faixa, `inicio < fim` |
| Não sobreposição | Faixas não podem ter intervalos que se cruzam |
| Cobertura sem lacunas | Cada faixa começa logo após o fim da anterior |

### Cálculo progressivo

O valor é calculado **faixa a faixa**: o consumo dentro de cada faixa é
multiplicado pelo valor unitário daquela faixa, e os subtotais são somados.

**Exemplo (Industrial, 18 m³):**

| Faixa | m³ cobrados | Valor unitário | Subtotal |
|-------|-------------|----------------|----------|
| 0–10 | 10 | R$ 1,00 | R$ 10,00 |
| 11–20 | 8 | R$ 2,00 | R$ 16,00 |
| **Total** | | | **R$ 26,00** |

---

## Pré-requisitos

- **Java 21** (ou superior)
- **Maven 3.9+** — opcional; o projeto inclui o **Maven Wrapper**, então é
  possível usar `./mvnw` (Linux/macOS) ou `mvnw.cmd` (Windows) sem instalar o Maven
- **PostgreSQL 18** — local ou via Docker
- **Docker** (opcional, recomendado para subir o banco e rodar os testes de
  integração)

---

## Configuração do banco de dados

A aplicação lê a conexão de variáveis de ambiente (com valores padrão para
desenvolvimento):

| Variável | Padrão |
|----------|--------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/tarifa_agua` |
| `DB_USER` | `tarifa` |
| `DB_PASSWORD` | `tarifa` |

O **schema é criado automaticamente pelo Flyway** na inicialização (não é preciso
rodar SQL manualmente).

### Subindo o PostgreSQL 18 com Docker

```bash
docker compose -f docker-compose-postgresql.yml up -d
```

---

## Como executar

```bash
# 1. Subir o banco (via Docker) — ou use um PostgreSQL local já existente
docker compose -f docker-compose-postgresql.yml up -d

# 2. Executar a aplicação
mvn spring-boot:run
```

> **Banco local (sem Docker):** os valores padrão da aplicação são
> `localhost:5432`, banco `tarifa_agua`, usuário/senha `tarifa`/`tarifa`. Basta
> criar esse usuário e banco no seu PostgreSQL local (como superusuário):
> ```sql
> CREATE ROLE tarifa LOGIN PASSWORD 'tarifa';
> CREATE DATABASE tarifa_agua OWNER tarifa;
> ```

A API ficará disponível em `http://localhost:8080`.

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health check:** http://localhost:8080/actuator/health

> Na primeira inicialização, o Flyway cria o schema e insere uma tabela de
> exemplo (seed) que já reproduz o caso Industrial 18 m³ = R$ 26,00.

---

## Endpoints

### 1. Criar tabela tarifária

`POST /api/tabelas-tarifarias`

**Request:**

```json
{
  "nome": "Tabela Tarifária 2026",
  "dataVigencia": "2026-01-01",
  "categorias": [
    {
      "categoria": "INDUSTRIAL",
      "faixas": [
        { "inicio": 0,  "fim": 10,    "valorUnitario": 1.00 },
        { "inicio": 11, "fim": 20,    "valorUnitario": 2.00 },
        { "inicio": 21, "fim": 30,    "valorUnitario": 3.00 },
        { "inicio": 31, "fim": 99999, "valorUnitario": 4.00 }
      ]
    }
  ]
}
```

**Response `201 Created`:**

```json
{
  "id": 1,
  "nome": "Tabela Tarifária 2026",
  "dataVigencia": "2026-01-01",
  "ativo": true,
  "categorias": [
    {
      "categoria": "INDUSTRIAL",
      "faixas": [
        { "id": 1, "inicio": 0,  "fim": 10,    "valorUnitario": 1.00 },
        { "id": 2, "inicio": 11, "fim": 20,    "valorUnitario": 2.00 },
        { "id": 3, "inicio": 21, "fim": 30,    "valorUnitario": 3.00 },
        { "id": 4, "inicio": 31, "fim": 99999, "valorUnitario": 4.00 }
      ]
    }
  ]
}
```

### 2. Listar tabelas tarifárias

`GET /api/tabelas-tarifarias`
`GET /api/tabelas-tarifarias?categoria=INDUSTRIAL` (filtra pelas faixas da categoria)

**Response `200 OK`:** lista de tabelas com suas categorias e faixas (mesmo
formato do retorno da criação).

### 3. Excluir tabela tarifária

`DELETE /api/tabelas-tarifarias/{id}`

**Response `204 No Content`.** A exclusão é lógica: a tabela deixa de aparecer na
listagem e não é mais usada em cálculos. Tabela inexistente → `404 Not Found`.

### 4. Calcular valor a pagar

`POST /api/calculos`

**Request:**

```json
{
  "categoria": "INDUSTRIAL",
  "consumo": 18
}
```

**Response `200 OK`:**

```json
{
  "categoria": "INDUSTRIAL",
  "consumoTotal": 18,
  "valorTotal": 26.00,
  "detalhamento": [
    {
      "faixa": { "inicio": 0, "fim": 10 },
      "m3Cobrados": 10,
      "valorUnitario": 1.00,
      "subtotal": 10.00
    },
    {
      "faixa": { "inicio": 11, "fim": 20 },
      "m3Cobrados": 8,
      "valorUnitario": 2.00,
      "subtotal": 16.00
    }
  ]
}
```

### Respostas de erro

Formato padronizado para todos os erros:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 422,
  "erro": "Unprocessable Content",
  "mensagem": "As faixas da categoria INDUSTRIAL se sobrepõem entre os intervalos [0-10] e [8-20] m³.",
  "caminho": "/api/tabelas-tarifarias",
  "detalhes": []
}
```

As mensagens são **amigáveis e parametrizáveis** (externalizadas em
`messages.properties` / `ValidationMessages.properties`, com suporte a i18n).

| Situação | Status |
|----------|--------|
| Payload/JSON inválido, categoria inexistente, parâmetro inválido | `400 Bad Request` |
| Recurso/rota não encontrada | `404 Not Found` |
| Método HTTP não suportado | `405 Method Not Allowed` |
| Violação de integridade (constraint) | `409 Conflict` |
| Faixas inconsistentes / consumo fora da cobertura | `422 Unprocessable Content` |
| Erro inesperado | `500 Internal Server Error` |

---

## Demonstração da parametrização

O cálculo usa exclusivamente os valores do banco. Para comprovar:

1. Calcule `INDUSTRIAL` / `18` → **R$ 26,00**.
2. Altere no banco o valor unitário da faixa `0–10` de `1.00` para `2.00`:

   ```sql
   UPDATE faixa_consumo SET valor_unitario = 2.00
   WHERE categoria = 'INDUSTRIAL' AND inicio = 0;
   ```

3. Calcule novamente `INDUSTRIAL` / `18` → **R$ 36,00** (10×2,00 + 8×2,00).

Nenhuma linha de código foi alterada. Esse fluxo também é coberto
automaticamente pelo teste de aceite `CriteriosDeAceiteIT` e pelo cenário BDD
(`parametrizacao.feature`).

---

## Como testar

Todos os testes são **autocontidos** (não exigem Docker nem PostgreSQL):

```bash
# Testes unitários (JUnit + Mockito + MockMvc)
mvn test

# Suíte completa: unitários + aceite (H2 em memória) + BDD (Cucumber) + cobertura
mvn verify
```

Cobertura de testes (**JaCoCo**) é gerada em `target/site/jacoco/index.html`
após `mvn verify` (cobertura ~98% de instruções; código gerado pelo Lombok é
ignorado). Se preferir, use `./mvnw` no lugar de `mvn`.

**Camadas de teste:**

- **Unitários (Mockito):** `CalculoService` e `TabelaService` (cálculo, limites,
  validações de faixa, soft delete) com o repositório mockado; `ApiExceptionHandler`,
  `MensagemResolver` e exceções de negócio.
- **Web (`@WebMvcTest` + MockMvc):** contrato HTTP dos controllers (status/JSON).
- **Aceite (`@SpringBootTest` + H2):** `CriteriosDeAceiteIT` — um teste por
  critério do desafio, ponta a ponta, com rollback transacional.
- **BDD (Cucumber):** vários `.feature` em português (Gherkin) com Background,
  Esquema do Cenário (Exemplos) e tags:
  `calculo_progressivo`, `criacao_tabela`, `listagem`, `exclusao_logica`,
  `parametrizacao`. Passos divididos por contexto (`PassosTabela`,
  `PassosCalculo`, `PassosComuns`) compartilhando estado via `@ScenarioScope`.

**O que é coberto:**

- Cálculo progressivo: caso canônico (18 m³ = R$ 26,00), limites de faixa,
  consumo zero, última faixa, categoria sem faixas e consumo fora da cobertura.
- Validações de faixa: início em 0, ordem, sobreposição e lacunas.
- Soft delete (exclusão lógica, sem remoção física).
- Parametrização ponta a ponta (alterar valor no banco muda o cálculo).

---

## Scripts de banco de dados

O schema é versionado pelo **Flyway**, em
`src/main/resources/db/migration`:

- `V1__create_schema.sql` — criação das tabelas, constraints e índices.
- `V2__seed_dados_exemplo.sql` — dados de exemplo (seed): uma tabela **ativa**
  de 2026 (4 categorias × 5 faixas, reproduzindo o caso do desafio) e uma tabela
  **histórica inativa** de 2025 (demonstra a preservação do histórico).

As migrations são aplicadas automaticamente na inicialização da aplicação.
> Os testes usam **H2 em memória** (sem Flyway): o schema é gerado pelas
> entidades JPA. Em produção, o `ddl-auto: validate` garante que as entidades
> estão alinhadas ao schema versionado pelo Flyway.

---

## Coleção de exemplos (REST Client / Postman)

- `api.http` — requisições prontas para a extensão **REST Client** (VS Code) e
  o **HTTP Client** do IntelliJ.
- `postman_collection.json` — importável diretamente no **Postman**
  (`Import > File`). Alternativa: importar a especificação OpenAPI em
  `/v3/api-docs`.
