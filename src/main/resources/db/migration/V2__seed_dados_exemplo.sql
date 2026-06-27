-- =====================================================================
-- Dados de exemplo (seed).
--
-- Tabela 1 (ATIVA, vigente em 2026): 4 categorias x 5 faixas progressivas.
--   Reproduz o caso do desafio: INDUSTRIAL, 18 m³ -> R$ 26,00
--   (faixa 0-10: 10 x 1,00 = 10,00; faixa 11-20: 8 x 2,00 = 16,00).
--
-- Tabela 2 (INATIVA, histórica de 2025): demonstra a preservação do histórico
--   (exclusão lógica) — não é utilizada em cálculos.
-- =====================================================================

INSERT INTO tabela_tarifaria (id, nome, data_vigencia, ativo) VALUES
  (1, 'Tabela Tarifária 2026',            DATE '2026-01-01', TRUE),
  (2, 'Tabela Tarifária 2025 (histórica)', DATE '2025-01-01', FALSE);

-- ---------- Tabela 1 (ATIVA / vigente) ----------
INSERT INTO faixa_consumo (tabela_id, categoria, inicio, fim, valor_unitario) VALUES
-- INDUSTRIAL (caso do desafio)
(1, 'INDUSTRIAL', 0,  10,    1.00),
(1, 'INDUSTRIAL', 11, 20,    2.00),
(1, 'INDUSTRIAL', 21, 30,    3.00),
(1, 'INDUSTRIAL', 31, 50,    4.00),
(1, 'INDUSTRIAL', 51, 99999, 5.00),
-- COMERCIAL
(1, 'COMERCIAL',  0,  10,    1.50),
(1, 'COMERCIAL',  11, 20,    2.50),
(1, 'COMERCIAL',  21, 30,    3.50),
(1, 'COMERCIAL',  31, 50,    4.50),
(1, 'COMERCIAL',  51, 99999, 5.50),
-- PARTICULAR
(1, 'PARTICULAR', 0,  10,    0.80),
(1, 'PARTICULAR', 11, 20,    1.60),
(1, 'PARTICULAR', 21, 30,    2.40),
(1, 'PARTICULAR', 31, 50,    3.20),
(1, 'PARTICULAR', 51, 99999, 4.00),
-- PUBLICO
(1, 'PUBLICO',    0,  10,    1.20),
(1, 'PUBLICO',    11, 20,    2.20),
(1, 'PUBLICO',    21, 30,    3.20),
(1, 'PUBLICO',    31, 50,    4.20),
(1, 'PUBLICO',    51, 99999, 5.20);

-- ---------- Tabela 2 (INATIVA / histórica - valores menores de 2025) ----------
INSERT INTO faixa_consumo (tabela_id, categoria, inicio, fim, valor_unitario) VALUES
(2, 'INDUSTRIAL', 0,  10,    0.90),
(2, 'INDUSTRIAL', 11, 20,    1.80),
(2, 'INDUSTRIAL', 21, 99999, 2.70),
(2, 'COMERCIAL',  0,  10,    1.30),
(2, 'COMERCIAL',  11, 20,    2.30),
(2, 'COMERCIAL',  21, 99999, 3.30),
(2, 'PARTICULAR', 0,  10,    0.70),
(2, 'PARTICULAR', 11, 20,    1.40),
(2, 'PARTICULAR', 21, 99999, 2.10),
(2, 'PUBLICO',    0,  10,    1.10),
(2, 'PUBLICO',    11, 20,    2.10),
(2, 'PUBLICO',    21, 99999, 3.10);

-- Garante que a sequence/identity continue após os ids inseridos manualmente.
SELECT setval(pg_get_serial_sequence('tabela_tarifaria', 'id'), (SELECT MAX(id) FROM tabela_tarifaria));
