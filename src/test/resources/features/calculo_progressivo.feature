# language: pt
Funcionalidade: Cálculo progressivo do valor a pagar
  Como área de cobrança
  Quero calcular o valor de forma progressiva por faixas
  Para que a conta reflita os valores parametrizados no banco

  Contexto:
    Dado uma tabela tarifária ativa para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim   | valorUnitario |
      | 0      | 10    | 1.00          |
      | 11     | 20    | 2.00          |
      | 21     | 30    | 3.00          |
      | 31     | 99999 | 4.00          |

  Cenário: Caso do desafio (18 m³ = R$ 26,00)
    Quando eu calculo o valor para a categoria "INDUSTRIAL" e consumo 18
    Então o valor total deve ser "26.00"
    E o detalhamento deve conter 2 faixas

  Esquema do Cenário: Cálculo para diferentes consumos
    Quando eu calculo o valor para a categoria "INDUSTRIAL" e consumo <consumo>
    Então o valor total deve ser "<total>"
    E o detalhamento deve conter <faixas> faixas

    Exemplos:
      | consumo | total  | faixas |
      | 0       | 0.00   | 0      |
      | 5       | 5.00   | 1      |
      | 10      | 10.00  | 1      |
      | 15      | 20.00  | 2      |
      | 25      | 45.00  | 3      |
      | 100     | 340.00 | 4      |

  Cenário: Consumo fora da cobertura é rejeitado
    Quando eu calculo o valor para a categoria "INDUSTRIAL" e consumo 999999
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "excede"

  Cenário: Categoria sem tabela ativa é rejeitada
    Quando eu calculo o valor para a categoria "COMERCIAL" e consumo 10
    Então a resposta deve ter status 422
    E a mensagem de erro deve conter "COMERCIAL"
