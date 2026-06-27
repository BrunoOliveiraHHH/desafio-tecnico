# language: pt
@criacao
Funcionalidade: Criação e validação de tabelas tarifárias
  Para garantir a integridade da tabela tarifária
  As faixas informadas devem ser consistentes

  Cenário: Cria uma tabela válida
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim   | valorUnitario |
      | 0      | 10    | 1.00          |
      | 11     | 99999 | 2.00          |
    Então a resposta deve ter status 201

  @validacao
  Cenário: Rejeita quando a primeira faixa não inicia em 0
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim | valorUnitario |
      | 1      | 10  | 1.00          |
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "iniciar em 0"

  @validacao
  Cenário: Rejeita faixas que se sobrepõem
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim | valorUnitario |
      | 0      | 10  | 1.00          |
      | 8      | 20  | 2.00          |
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "sobrep"

  @validacao
  Cenário: Rejeita lacuna entre faixas
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim | valorUnitario |
      | 0      | 10  | 1.00          |
      | 15     | 20  | 2.00          |
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "lacuna"

  @validacao
  Cenário: Rejeita início maior ou igual ao fim
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim | valorUnitario |
      | 0      | 10  | 1.00          |
      | 15     | 12  | 2.00          |
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "menor que o fim"

  @validacao
  Cenário: Rejeita valor unitário não positivo (validação de entrada, 400)
    Quando eu tento criar uma tabela para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim | valorUnitario |
      | 0      | 10  | 0.00          |
    Então a resposta deve ter status 400
    E a mensagem de erro deve conter "inválidos"
