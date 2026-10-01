package Inova.relatorios.spp

import br.com.multitec.utils.Utils
import sam.model.entities.ab.Abm0101;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate


public class SPP_Calculos_Necessidades_MRP extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Cálculos Necessidades (MRP)";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("optionDtNecessidades", "1");
        filtrosDefault.put("optionQtdNecessidades", "1");
        filtrosDefault.put("dataCalculo", LocalDate.now());
        filtrosDefault.put("chkImprimirProduto", true);
        filtrosDefault.put("chkEstoque", true);
        filtrosDefault.put("chkPreReserva", true);
        filtrosDefault.put("chkDetalhamentoOP", true);
        filtrosDefault.put("chkPedCompra", true);
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("estMinMax", "1");

        return Utils.map("filtros", filtrosDefault);

    }
    @Override
    public DadosParaDownload executar() {
        Integer numInicial = getInteger("numeroInicial");
        Integer numFinal = getInteger("numeroFinal");
        List<Long> itens = getListLong("itens");
        List<Integer> mps = getListInteger("mps")
        List<Long> processos = getListLong("processos");
        List<Long> tiposDoc = getListLong("tipos");
        LocalDate[] dtCriacao = getIntervaloDatas("dataCriacao")
        LocalDate dtCalculo = getLocalDate("dataCalculo");
        Integer optionDtNecessidades = getInteger("optionDtNecessidades");
        Integer optionQtdNecessidades = getInteger("optionQtdNecessidades");
        boolean chkEstoque = getBoolean("chkEstoque");
        boolean chkPreReserva = getBoolean("chkPreReserva");
        boolean chkPedCompra = getBoolean("chkPedCompra");
        boolean chkImprimirProduto = getBoolean("chkImprimirProduto");
        boolean chkDetalhamentoOP = getBoolean("chkDetalhamentoOP");
        Integer optionEstMinMax = getInteger("estMinMax");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = new ArrayList<>();
        List<TableMap> produtosOrdem = buscarProdutosOrdem(numInicial, numFinal, itens, mps, processos, tiposDoc, dtCriacao);
        List<Long> idsOrdens = new ArrayList<>();
        List<TableMap> listComponentesOP = new ArrayList<>();

        if(produtosOrdem == null && produtosOrdem.size() == 0) interromper("Não foram encontrados dados para exibição.");

        for(produto in produtosOrdem){
            LocalDate dtInicio = produto.getDate("dtInicio");
            Integer numOrdem = produto.getInteger("numOrdem");
            if(dtInicio == null) interromper("A ordem de produção " + numOrdem + " não possuí data de início de produção.");

            idsOrdens.add(produto.getLong("bab01id"));
        }

        List<TableMap> componentes = buscarComponentes(idsOrdens, optionDtNecessidades, optionQtdNecessidades,dtCalculo);
        List<TableMap> componentesOrdensProducao = buscarComponentesOP(idsOrdens, optionDtNecessidades, optionQtdNecessidades,dtCalculo);
        calcularMRP(componentes, dtCriacao, optionEstMinMax, chkPedCompra, chkEstoque);


        for(op in componentesOrdensProducao){
            String chaveComponenteOP = op.getString("chaveComponente");
            for(componente in componentes){
                String chaveComponente = componente.getString("chaveComponente");
                componente.put("chkDetalhamentoOP", chkDetalhamentoOP)
                if(chaveComponenteOP == chaveComponente) op.putAll(componente);
            }
        }

        dados.addAll(produtosOrdem);
        dados.addAll(listComponentesOP);

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsProdutos", produtosOrdem, "key", "key");
        dsPrincipal.addSubDataSource("dsComponentes", componentesOrdensProducao, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPP_Calculos_Necessidades_MRP_S1"));
        adicionarParametro("StreamSub2", carregarArquivoRelatorio("SPP_Calculos_Necessidades_MRP_S2"));


        LocalDate parametroData = optionDtNecessidades == 0 ? dtCalculo : dtCriacao != null ? dtCriacao[0] : null
        params.put("IMPRIMIR_PROD", chkImprimirProduto);
        params.put("TITULO", "SPP - Cálculos Necessidades (MRP)");
        params.put("DATA", parametroData);
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());

        if(impressao == 1) return gerarXLSX("SPP_Calculos_Necessidades_MRP_PDF", dsPrincipal);
        return gerarPDF("SPP_Calculos_Necessidades_MRP_PDF", dsPrincipal);
    }
    private List<TableMap> buscarProdutosOrdem(Integer numInicial, Integer numFinal, List<Long> itens, List<Integer> mps, List<Long> processos, List<Long> tiposDoc, LocalDate[] dtCriacao){

        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        String whereMPS = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";
        String whereProcessos = processos != null && processos.size() > 0 ? "AND abp10id IN (:processos) " : "";
        String whereTiposDoc = tiposDoc != null && tiposDoc.size() > 0 ? "AND abb01ordem.abb01tipo IN (:tiposDoc) " : "";
        String whereDtCriacao = dtCriacao != null ? "AND abb01ordem.abb01data BETWEEN :dtInicio AND :dtFinal " : "";
        String whereStatus = "AND bab01status = 0 ";
        String whereNumOrdem = numInicial != null && numFinal != null ? "WHERE abb01ordem.abb01num BETWEEN :numInicial AND :numFinal " :
                                 numInicial != null && numFinal == null ? "WHERE abb01ordem.abb01num >= :numInicial " :
                                 numInicial == null && numFinal != null ? "WHERE abb01ordem.abb01num <= :numInicial " : "";
        
        String sql = "SELECT bab01id, abb01ordem.abb01num AS numOrdem, abp10codigo AS codProcesso, abp10descr AS descrProcesso, abm01prod.abm01codigo AS codItem, abm01prod.abm01descr AS descrItem, " +
                        "aam06codigo AS umu, bab01qt, abb01plano.abb01num AS numPlano, baa01descr AS nomePlano, CAST(bab01json ->> 'data_inicio' AS DATE) AS dtInicio, 'ORDEM_PRODUCAO' AS tipo, '1' AS key " +
                        "FROM bab01 " +
                        "INNER JOIN abb01 AS abb01ordem ON abb01ordem.abb01id = bab01central " +
                        "INNER JOIN abp10 ON abp10id = bab01proc " +
                        "INNER JOIN abp20 ON abp20id = bab01comp " +
                        "INNER JOIN abm01 AS abm01prod ON abm01prod.abm01id = abp20item " +
                        "LEFT JOIN aam06 ON aam06id = abm01prod.abm01umu  " +
                        "LEFT JOIN bab0103 ON bab0103op = bab01id " +
                        "LEFT JOIN baa0101 ON baa0101id = bab0103itemPP " +
                        "LEFT JOIN baa01 ON baa01id = baa0101plano " +
                        "LEFT JOIN abb01 AS abb01plano ON abb01plano.abb01id = baa01central " +
                        whereNumOrdem +
                        whereItens +
                        whereMPS +
                        whereProcessos +
                        whereTiposDoc +
                        whereDtCriacao +
                        whereStatus +
                        "ORDER BY abm01codigo ";

        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroProcessos = processos != null && processos.size() > 0 ? Parametro.criar("processos", processos) : null;
        Parametro parametroTiposDoc = tiposDoc != null && tiposDoc.size() > 0 ? Parametro.criar("tiposDoc", tiposDoc) : null;
        Parametro parametroDtInicio = dtCriacao != null ? Parametro.criar("dtInicio", dtCriacao[0]) : null;
        Parametro parametroDtFinal = dtCriacao != null ? Parametro.criar("dtFinal", dtCriacao[1]) : null;
        Parametro parametroNumOrdemIni = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumOrdemFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItens, parametroMps, parametroProcessos, parametroTiposDoc, parametroDtInicio, parametroDtFinal, parametroNumOrdemIni, parametroNumOrdemFin )

    }
    private List<TableMap> buscarComponentes(List<Long> idsOrdens, Integer optionDtNecessidades, Integer optionQtdNecessidades, LocalDate dtCalculo){

        String field1 = optionDtNecessidades == 0 ? " :dtCalculo AS dtInicio, CONCAT(abm01id, :dtCalculo ) AS chaveComponente, " : "CAST(bab01json ->> 'data_inicio' AS DATE) AS dtInicio,CONCAT(abm01id, CAST(bab01json ->> 'data_inicio' AS DATE)) AS chaveComponente,  "
        String field2 = optionQtdNecessidades == 0 ? " SUM(bab0101qtP) AS qtd, " : "SUM(bab0101qtA) AS qtd, ";
        String groupBy = optionDtNecessidades == 0 ?  " GROUP BY abm01id, abm01codigo, abm01descr, mps, umu, dtInicio, key, abm13leadTime " : " GROUP BY abm01id, abm01codigo, abm01descr, mps, umu, dtInicio, key, chaveComponente, abm13leadTime ";

        String sql = "SELECT " + field1 + field2 + " abm01id AS idItem, abm01codigo AS codItem, abm01descr AS descrItem, " +
            " CASE WHEN abm01tipo = 0 THEN 'M' ELSE 'P' END AS mps, abm13leadTime,  " +
            " aam06codigo AS umu, '1' AS key " +
            " FROM bab01  " +
            " INNER JOIN abb01 AS abb01ordem ON abb01ordem.abb01id = bab01central  " +
            " INNER JOIN abp10 ON abp10id = bab01proc  " +
            " INNER JOIN bab0101 ON bab0101op = bab01id " +
            " INNER JOIN abm01 ON abm01id = bab0101item  " +
            " LEFT JOIN aam06 ON aam06id = abm01umu " +
            " INNER JOIN abm0101 ON abm0101item = abm01id "+
            " LEFT JOIN abm13 ON abm13id = abm0101comercial " +
            " WHERE bab01id IN (:idsOrdens) "+
            obterWherePadrao("Abm01", "AND") +
            groupBy +
            " ORDER BY abm01codigo, dtInicio"

        Parametro parametroOrdens = Parametro.criar("idsOrdens", idsOrdens);
        Parametro parametroDtCalculo = optionDtNecessidades == 0 ? Parametro.criar("dtCalculo", dtCalculo) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroOrdens, parametroDtCalculo)
    }
    private List<TableMap> buscarComponentesOP(List<Long> idsOrdens, Integer optionDtNecessidades, Integer optionQtdNecessidades, LocalDate dtCalculo){
        String field1 = optionDtNecessidades == 0 ? " :dtCalculo AS dtInicio, CONCAT(abm01id, :dtCalculo ) AS chaveComponente, " : "CAST(bab01json ->> 'data_inicio' AS DATE) AS dtInicio,CONCAT(abm01id, CAST(bab01json ->> 'data_inicio' AS DATE)) AS chaveComponente,  "
        String field2 = optionQtdNecessidades == 0 ? " bab0101qtP AS qtd, " : "bab0101qtA AS qtd, ";

        String sql = "SELECT " + field1 + field2 + "abm01id AS idItem, abb01ordem.abb01num AS numOrdem, bab0101qtP AS qtdOrdem " +
                    " FROM bab01  " +
                    " INNER JOIN abb01 AS abb01ordem ON abb01ordem.abb01id = bab01central  " +
                    " INNER JOIN abp10 ON abp10id = bab01proc  " +
                    " INNER JOIN bab0101 ON bab0101op = bab01id " +
                    " INNER JOIN abm01 ON abm01id = bab0101item  " +
                    " LEFT JOIN aam06 ON aam06id = abm01umu " +
                    " INNER JOIN abm0101 ON abm0101item = abm01id "+
                    " LEFT JOIN abm13 ON abm13id = abm0101comercial " +
                    obterWherePadrao("Abm01", "AND") +
                    "WHERE bab01id IN (:idsOrdens) "+
                    "ORDER BY abm01codigo, dtInicio, abb01ordem.abb01num"

        Parametro parametroOrdens = Parametro.criar("idsOrdens", idsOrdens);
        Parametro parametroDtCalculo = optionDtNecessidades == 0 ? Parametro.criar("dtCalculo", dtCalculo) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroOrdens, parametroDtCalculo)

    }
    private void calcularMRP(List<TableMap> componentes, LocalDate[] dtCriacao,Integer optionEstMinMax, boolean chkPedCompra, boolean chkEstoque) {

        Map<Long, BigDecimal> mapEstoque = new HashMap<>();
        Map<Long, BigDecimal> mapPedidosCompra = new HashMap<>();

        List<Long> idsItens = obterIdsItens(componentes);

        List<TableMap> pedidosCompra = chkPedCompra ? buscarPedidosCompraItens(idsItens, dtCriacao,) : new ArrayList<>();

        Long idItemAnterior = null;
        BigDecimal pedidoCompraAnterior = BigDecimal.ZERO;

        for (TableMap componente : componentes) {

            Long idItem = componente.getLong("idItem");
            BigDecimal qtd = componente.getBigDecimal_Zero("qtd");

            if (!idItem.equals(idItemAnterior)) {
                pedidoCompraAnterior = BigDecimal.ZERO;
            }

            BigDecimal estoqueMinMax = obterEstoqueMinMax(
                    idItem,
                    optionEstMinMax,
                    chkEstoque
            );

            BigDecimal saldoEstoqueAtual = obterSaldoEstoque(
                    idItem,
                    chkEstoque
            );

            atualizarEstoqueComPedidoAnterior(
                    idItem,
                    saldoEstoqueAtual,
                    pedidoCompraAnterior,
                    mapEstoque
            );

            BigDecimal saldo = calcularSaldo(
                    idItem,
                    qtd,
                    saldoEstoqueAtual,
                    mapEstoque,
                    chkEstoque
            );

            processarPedidoCompra(
                    componente,
                    pedidosCompra,
                    idItem,
                    mapPedidosCompra,
                    chkPedCompra
            );

            BigDecimal necessidadeLiquida = calcularNecessidadeLiquida(qtd, saldo, componente.getBigDecimal_Zero("pedidoCompra") );

            BigDecimal sugestaoCompra = calcularSugestaoCompra(
                    necessidadeLiquida,
                    estoqueMinMax,
                    optionEstMinMax,
                    chkEstoque,
                    saldo,
                    qtd
            );

            LocalDate dataSugerida = calcularDataSugerida(
                    componente.getDate("dtInicio"),
                    componente.getInteger("abm13leadTime")
            );

            preencherResultado(
                    componente,
                    saldo,
                    necessidadeLiquida,
                    estoqueMinMax,
                    sugestaoCompra,
                    dataSugerida
            );

            pedidoCompraAnterior = componente.getBigDecimal_Zero("pedidoCompra");
            idItemAnterior = idItem;
        }
    }
    private List<Long> obterIdsItens(List<TableMap> componentes) {

        List<Long> idsItens = new ArrayList<>();

        for (TableMap componente : componentes) {
            idsItens.add(componente.getLong("idItem"));
        }

        return idsItens;
    }
    private BigDecimal obterEstoqueMinMax(Long idItem, Integer optionEstMinMax, boolean chkEstoque) {
        if (!chkEstoque)
            return BigDecimal.ZERO;

        BigDecimal estoqueMaxMin = buscarPrecoMinMax(idItem, optionEstMinMax);

        if(estoqueMaxMin == null || estoqueMaxMin.compareTo(0) == 0) return BigDecimal.ZERO;

        return estoqueMaxMin;
    }
    private BigDecimal obterSaldoEstoque(Long idItem, boolean chkEstoque) {

        if (!chkEstoque)
            return BigDecimal.ZERO;

        BigDecimal saldo = buscarSaldoEstoqueItem(idItem);

        return saldo != null ? saldo : BigDecimal.ZERO;
    }
    private void atualizarEstoqueComPedidoAnterior(Long idItem, BigDecimal saldoEstoqueAtual, BigDecimal pedidoCompraAnterior, Map<Long, BigDecimal> mapEstoque) {

        if (pedidoCompraAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        BigDecimal saldoMap = mapEstoque.getOrDefault(
                idItem,
                saldoEstoqueAtual
        );

        mapEstoque.put(
                idItem,
                saldoMap.add(pedidoCompraAnterior)
        );
    }
    private BigDecimal calcularSaldo(Long idItem, BigDecimal qtd, BigDecimal saldoEstoqueAtual, Map<Long, BigDecimal> mapEstoque, boolean chkEstoque) {

        if (!chkEstoque) {
            return BigDecimal.ZERO;
        }

        return comporEstoque(
                idItem,
                saldoEstoqueAtual,
                mapEstoque,
                qtd
        );
    }
    private void processarPedidoCompra(TableMap componente, List<TableMap> pedidosCompra, Long idItem, Map<Long, BigDecimal> mapPedidosCompra, boolean chkPedCompra) {

        if (!chkPedCompra) {
            componente.put("pedidoCompra", BigDecimal.ZERO);
            componente.put("pedCompraAposInicioProd", BigDecimal.ZERO);
            return;
        }

        comporPedidosCompraItem(
                pedidosCompra,
                componente,
                idItem,
                componente.getDate("dtInicio"),
                mapPedidosCompra
        );
    }
    private BigDecimal calcularNecessidadeLiquida(BigDecimal qtd, BigDecimal saldo, BigDecimal pedCompra) {
        BigDecimal necessidade = qtd.subtract(saldo.add(pedCompra));

        return necessidade//necessidade.compareTo(BigDecimal.ZERO) > 0 ? necessidade : BigDecimal.ZERO;
    }
    private BigDecimal calcularSugestaoCompra(BigDecimal necessidadeLiquida, BigDecimal estoqueMinMax, Integer optionEstMinMax, boolean chkEstoque, BigDecimal saldo, BigDecimal qtd) {

        if (!chkEstoque) {
            return necessidadeLiquida;
        }

        if (optionEstMinMax == 0) {
            return BigDecimal.ZERO;
        }

        return calcularSujestaoCompra(
                optionEstMinMax,
                estoqueMinMax,
                necessidadeLiquida,
                qtd
        );
    }
    private LocalDate calcularDataSugerida(LocalDate dtInicioProd,Integer leadTime) {

        if (leadTime == null) {
            return null;
        }

        return dtInicioProd.minusDays(leadTime);
    }
    private void preencherResultado( TableMap componente, BigDecimal saldo, BigDecimal necessidadeLiquida, BigDecimal estoqueMinMax, BigDecimal sugestaoCompra, LocalDate dataSugerida) {

        componente.put("estoque", saldo);
        componente.put("necessidadeLiquida", necessidadeLiquida);
        componente.put("estoqueMinMax", estoqueMinMax);
        componente.put("sugestaoCompra", sugestaoCompra);
        componente.put("dataSugerida", dataSugerida);
        componente.put("preReserva", BigDecimal.ZERO);
    }
    private BigDecimal buscarSaldoEstoqueItem(Long idItem){
        String sql = "SELECT bcc02qt FROM bcc02 WHERE bcc02status = 4224 AND bcc02item = :idItem ";

        return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idItem", idItem));
    }
    private BigDecimal comporEstoque(Long idItem, BigDecimal saldoEstoqueAtual, Map<Long, BigDecimal> mapEstoque, BigDecimal qtd) {

        BigDecimal saldo = mapEstoque.getOrDefault(
                idItem,
                saldoEstoqueAtual
        );

        BigDecimal saldoDisponivel = saldo;

        saldo = saldo.subtract(qtd);

        mapEstoque.put(idItem, saldo);

        return saldoDisponivel;
    }
    private List<TableMap> buscarPedidosCompraItens(List<Long> idsItens, LocalDate[] dtCriacao ){
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND eaa0103item IN (:idsItens) " : "";
        String whereData = dtCriacao != null ? "AND eaa0103dtEntrega BETWEEN :dtInicial AND :dtFinal " : "";
        String whereAtendimento = "WHERE eaa0103pedAtend IN (0, 1) ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        String sql = "SELECT eaa0103dtEntrega, abm01id, abm01codigo, abm01na, " +
                    " SUM(eaa0103qtUso - COALESCE(eaa01032qtUso, 0.00)) AS qtd " +
                    " FROM eaa0103 " +
                    " INNER JOIN abm01 ON abm01id = eaa0103item " +
                    " LEFT JOIN eaa01032 ON eaa01032itemSCV = eaa0103id "+
                    " INNER JOIN eaa01 ON eaa01id = eaa0103doc "+
                    whereItens +
                    whereData +
                    whereAtendimento +
                    whereEmpresa +
                    "GROUP BY eaa0103dtEntrega, abm01id, abm01codigo, abm01na " +
                    " ORDER BY abm01id, eaa0103dtEntrega"

        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroDtInicio =  dtCriacao != null ? Parametro.criar("dtInicial", dtCriacao[0]) : null;
        Parametro parametroDtFinal =  dtCriacao != null ? Parametro.criar("dtFinal", dtCriacao[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens, parametroDtInicio, parametroDtFinal, parametroEmpresa);
    }
    private void comporPedidosCompraItem(List<TableMap> listPedidos, TableMap componente, Long idItem, LocalDate dtInicioProd, Map<Long, BigDecimal> mapPedidosCompra){
        BigDecimal totPedidosAntesData = BigDecimal.ZERO;
        BigDecimal totPedidosPosData = BigDecimal.ZERO;

        for(pedido in listPedidos){
            Long idItemPed = pedido.getLong("abm01id");
            LocalDate dtEntrega = pedido.getDate("eaa0103dtEntrega");
            BigDecimal qtd = pedido.getBigDecimal_Zero("qtd");

            if (idItem.equals(idItemPed)) {

                if (dtEntrega <= dtInicioProd) {
                    totPedidosAntesData = totPedidosAntesData.add(qtd);
                }

                if (dtEntrega > dtInicioProd) {
                    totPedidosPosData = totPedidosPosData.add(qtd);
                }
            }
        }

        // Quanto já foi considerado nas linhas anteriores
        BigDecimal pedidosJaConsiderados =
                mapPedidosCompra.getOrDefault(
                        idItem,
                        BigDecimal.ZERO
                );

        // Somente o que entrou desde a última linha
        BigDecimal pedidoCompraAtual =
                totPedidosAntesData.subtract(pedidosJaConsiderados);

        componente.put("pedidoCompra", pedidoCompraAtual);
        componente.put("pedCompraAposInicioProd", totPedidosPosData);

        // Guarda o acumulado para a próxima linha
        mapPedidosCompra.put(
                idItem,
                totPedidosAntesData
        );
    }

    private BigDecimal buscarPrecoMinMax(Long idItem, Integer optionEstMinMax){
        if(optionEstMinMax == 0) return BigDecimal.ZERO;

        String campo = optionEstMinMax == 1 ? "abm0101estMin AS estoqueMinMax " : optionEstMinMax == 2 ? "abm0101estMax AS estoqueMinMax " : "";
        String sql = "SELECT " + campo + " FROM abm0101 WHERE abm0101item = :idItem AND abm0101empresa = :idEmpresa ";
        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroEmpresa);
    }
    private BigDecimal calcularSujestaoCompra(Integer optionEstMinMax, BigDecimal estoqueMinMax, BigDecimal necessidadeLiq, BigDecimal qtd) {

        if (estoqueMinMax.compareTo(BigDecimal.ZERO) > 0) {

            // Não considerar estoque mínimo/máximo
            if (optionEstMinMax == 0) {
                return BigDecimal.ZERO;
            }

            // Necessidade já atende o estoque mínimo/máximo
            if (necessidadeLiq.compareTo(BigDecimal.ZERO) <= 0
                    && necessidadeLiq.abs().compareTo(estoqueMinMax) >= 0) {
                return BigDecimal.ZERO;
            }

            return necessidadeLiq.add(estoqueMinMax);
        }

        // Sem estoque mínimo/máximo configurado
        if (necessidadeLiq.compareTo(BigDecimal.ZERO) <= 0) {

            if (necessidadeLiq.abs().compareTo(qtd) > 0) {
                return BigDecimal.ZERO;
            }

            return necessidadeLiq.add(qtd);
        }

        if (necessidadeLiq.compareTo(qtd) > 0) {
            return necessidadeLiq.subtract(qtd);
        }

        return necessidadeLiq;
    }
}