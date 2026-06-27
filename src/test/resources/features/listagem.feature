# language: pt
Funcionalidade: Listagem de tabelas tarifárias
  Para consultar as tarifas vigentes
  Quero listar as tabelas ativas, com filtro opcional por categoria

  Contexto:
    Dado uma tabela tarifária ativa para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim   | valorUnitario |
      | 0      | 10    | 1.00          |
      | 11     | 99999 | 2.00          |

  Cenário: Lista todas as tabelas ativas
    Quando eu listo as tabelas
    Então a resposta deve ter status 200
    E a lista deve conter 1 tabela

  Cenário: Lista filtrando pela categoria informada
    Quando eu listo as tabelas da categoria "INDUSTRIAL"
    Então a resposta deve ter status 200
    E a lista deve conter 1 tabela
    E a primeira tabela deve ter a categoria "INDUSTRIAL"

  Cenário: Filtro por categoria sem faixas retorna lista vazia
    Quando eu listo as tabelas da categoria "PUBLICO"
    Então a resposta deve ter status 200
    E a lista deve conter 0 tabelas
