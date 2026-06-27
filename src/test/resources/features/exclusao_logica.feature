# language: pt
Funcionalidade: Exclusão lógica (soft delete)
  Tabelas excluídas são preservadas no histórico
  E deixam de ser usadas em listagens e cálculos

  Cenário: Tabela excluída deixa de aparecer na listagem
    Dado uma tabela tarifária ativa para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim   | valorUnitario |
      | 0      | 10    | 1.00          |
      | 11     | 99999 | 2.00          |
    Quando eu excluo a tabela criada
    Então a resposta deve ter status 204
    Quando eu listo as tabelas
    Então a tabela criada não deve aparecer na listagem

  Cenário: Excluir tabela inexistente retorna 404
    Quando eu excluo a tabela 999999
    Então a resposta deve ter status 404
    E a mensagem de erro deve conter "não encontrada"
