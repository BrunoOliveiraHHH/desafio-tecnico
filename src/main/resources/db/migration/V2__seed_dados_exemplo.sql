-- Dados de exemplo (seed). Reproduzem o caso do desafio:
-- INDUSTRIAL, consumo 18 m3 -> R$ 26,00.

INSERT INTO tabela_tarifaria (id, nome, data_vigencia, ativo)
VALUES (1, 'Tabela Tarifaria 2026', DATE '2026-01-01', TRUE);

INSERT INTO faixa_consumo (tabela_id, categoria, inicio, fim, valor_unitario) VALUES
-- INDUSTRIAL (caso do desafio)
(1, 'INDUSTRIAL', 0,  10,    1.00),
(1, 'INDUSTRIAL', 11, 20,    2.00),
(1, 'INDUSTRIAL', 21, 30,    3.00),
(1, 'INDUSTRIAL', 31, 99999, 4.00),
-- COMERCIAL
(1, 'COMERCIAL',  0,  10,    1.50),
(1, 'COMERCIAL',  11, 20,    2.50),
(1, 'COMERCIAL',  21, 30,    3.50),
(1, 'COMERCIAL',  31, 99999, 4.50),
-- PARTICULAR
(1, 'PARTICULAR', 0,  10,    0.80),
(1, 'PARTICULAR', 11, 20,    1.60),
(1, 'PARTICULAR', 21, 30,    2.40),
(1, 'PARTICULAR', 31, 99999, 3.20),
-- PUBLICO
(1, 'PUBLICO',    0,  10,    1.20),
(1, 'PUBLICO',    11, 20,    2.20),
(1, 'PUBLICO',    21, 30,    3.20),
(1, 'PUBLICO',    31, 99999, 4.20);

-- Garante que a sequence/identity continue apos o id inserido manualmente.
SELECT setval(pg_get_serial_sequence('tabela_tarifaria', 'id'), (SELECT MAX(id) FROM tabela_tarifaria));
