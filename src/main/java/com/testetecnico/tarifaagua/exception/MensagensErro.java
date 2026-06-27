package com.testetecnico.tarifaagua.exception;

/**
 * Catálogo de chaves de mensagens de erro.
 *
 * <p>As chaves apontam para os textos parametrizáveis definidos em
 * {@code messages.properties}, evitando o uso de "strings mágicas" espalhadas
 * pelo código e centralizando a manutenção das mensagens.</p>
 */
public final class MensagensErro {

    private MensagensErro() {
    }

    // Regras de negócio - faixas
    public static final String FAIXA_COBERTURA_INICIAL = "negocio.faixa.coberturaInicial";
    public static final String FAIXA_ORDEM_INVALIDA = "negocio.faixa.ordemInvalida";
    public static final String FAIXA_SOBREPOSICAO = "negocio.faixa.sobreposicao";
    public static final String FAIXA_LACUNA = "negocio.faixa.lacuna";
    public static final String CATEGORIA_DUPLICADA = "negocio.categoria.duplicada";

    // Regras de negócio - cálculo
    public static final String CALCULO_SEM_TABELA_ATIVA = "negocio.calculo.semTabelaAtiva";
    public static final String CALCULO_CONSUMO_FORA_COBERTURA = "negocio.calculo.consumoForaCobertura";

    // Recursos
    public static final String TABELA_NAO_ENCONTRADA = "recurso.tabela.naoEncontrada";

    // Erros genéricos de API
    public static final String API_CORPO_INVALIDO = "api.corpoInvalido";
    public static final String API_PARAMETRO_INVALIDO = "api.parametroInvalido";
    public static final String API_VALORES_ACEITOS = "api.valoresAceitos";
    public static final String API_REQUISICAO_INVALIDA = "api.requisicaoInvalida";
    public static final String API_PARAMETRO_OBRIGATORIO = "api.parametroObrigatorio";
    public static final String API_METODO_NAO_SUPORTADO = "api.metodoNaoSuportado";
    public static final String API_ROTA_NAO_ENCONTRADA = "api.rotaNaoEncontrada";
    public static final String API_CONFLITO_INTEGRIDADE = "api.conflitoIntegridade";
    public static final String API_ERRO_INTERNO = "api.erroInterno";
}
