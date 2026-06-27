# language: pt
Funcionalidade: Parametrização total (sem alteração de código)
  Ajustar faixas/valores no banco deve refletir imediatamente nos cálculos

  Contexto:
    Dado uma tabela tarifária ativa para a categoria "INDUSTRIAL" com as faixas:
      | inicio | fim   | valorUnitario |
      | 0      | 10    | 1.00          |
      | 11     | 20    | 2.00          |
      | 21     | 99999 | 3.00          |

  Cenário: Alterar o valor de uma faixa no banco muda o cálculo
    Quando eu calculo o valor para a categoria "INDUSTRIAL" e consumo 18
    Então o valor total deve ser "26.00"
    Quando eu altero no banco o valor unitário da faixa que inicia em 0 da categoria "INDUSTRIAL" para "2.00"
    E eu calculo o valor para a categoria "INDUSTRIAL" e consumo 18
    Então o valor total deve ser "36.00"
