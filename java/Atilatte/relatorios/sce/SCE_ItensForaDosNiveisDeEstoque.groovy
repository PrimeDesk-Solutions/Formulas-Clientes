package Atilatte.relatorios.sce

import java.lang.reflect.Parameter
import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc02
import sam.model.entities.bc.Bcc0201
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro
import java.time.format.DateTimeFormatter;


public class SCE_ItensForaDosNiveisDeEstoque extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Itens Fora dos Niveis de Estoque";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        LocalDate data = MDate.date()
        filtrosDefault.put("dataSaldo", data)
        filtrosDefault.put("itemMovEst", true)
        filtrosDefault.put("itemNaoMovEst", true)
        filtrosDefault.put("naoAtend", true)
        filtrosDefault.put("parcialAtend", true)
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("loteIni", "")
        filtrosDefault.put("loteFin", "")
        filtrosDefault.put("serieIni", "")
        filtrosDefault.put("serieFin", "")
        filtrosDefault.put("optionsDetalhamento", "0")
        filtrosDefault.put("itemSaldoZero", true)
        filtrosDefault.put("pedidoVenda", true)
        filtrosDefault.put("pedidoCompra", true)

        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Integer> mps = getListInteger("mpm");
        List<Long> itens = getListLong("itens");
        Boolean itemMovEstoque = getBoolean("itemMovEst");
        Boolean itemNaoMovEst = getBoolean("itemNaoMovEst");
        String loteIni = getString("loteIni");
        String loteFin = getString("loteFin");
        String serieIni = getString("serieIni");
        String serieFin = getString("serieFin");
        LocalDate dataSaldo = getLocalDate("dataSaldo");
        Integer detalhamento = getInteger("optionsDetalhamento");
        Boolean naoImprimirSaldoZero = getBoolean("itemSaldoZero");
        LocalDate[] dtPedidos = getIntervaloDatas("dataPedido");
        LocalDate[] dtEntrega = getIntervaloDatas("dtEntrega");
        Boolean naoAtendido = getBoolean("naoAtend")
        Boolean parcialAtendido = getBoolean("parcialAtend")
        Integer impressao = getInteger("impressao");
        List<Long> idsStatus = getListLong("status");
        List<Long> idsLocal = getListLong("local");

        List<Integer> atendimentos = new ArrayList();

        if (naoAtendido) atendimentos.add(0);
        if (parcialAtendido) atendimentos.add(1);

        List<TableMap> dados = buscarDadosRelatorio(mps, itens, loteIni, loteFin, serieIni, serieFin, dataSaldo, detalhamento, naoImprimirSaldoZero, idsStatus, idsLocal, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega, atendimentos);

        String titulo = "";
        if (detalhamento == 0) {
            titulo = "SCE - Itens Acima do Estoque Máximo";
        } else if (detalhamento == 1) {
            titulo = "SCE - Itens Abaixo do Estoque Mínimo";
        } else if (detalhamento == 2) {
            titulo = "SCE - Itens Entre o Ponto de Pedido e Estoque Mínimo";
        } else {
            titulo = "SCE - Itens Fora dos Níveis de Estoque";
        }

        params.put("titulo", titulo);
        params.put("periodo", "Data Saldo: " + dataSaldo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());

        if (impressao == 1) return gerarXLSX("SCE_ItensForaDosNiveisDeEstoque_Excel", dados);

        return gerarPDF("SCE_ItensForaDosNiveisDeEstoque_PDF", dados);

    }


    private List<TableMap> buscarDadosRelatorio(List<Integer> mps, List<Long> itens, String loteIni, String loteFin, String serieIni, String serieFin, LocalDate dataSaldo,
                                                Integer detalhamento, Boolean naoImprimirSaldoZero, List<Long> idsStatus, List<Long> idsLocal, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos, LocalDate[] dtEntrega, List<Integer> atendimentos) {

        List<TableMap> listItensComMov = buscarItensComMovimentacao(mps, itens, itemMovEstoque, itemNaoMovEst, dtPedidos, dtEntrega, atendimentos);

        List<Long> idsItensMov = new ArrayList();
        List<TableMap> registros = new ArrayList();

        List<TableMap> mediaConsumoItens = buscarMediaVendaUltimos3Meses(itens);

        for (item in listItensComMov) {
            Long idItem = item.getLong("abm01id");
            BigDecimal saldo = buscarSaldoItem(idItem, dataSaldo, idsStatus, idsLocal, loteIni, loteFin, serieIni, serieFin);
            def esMov = item.getInteger("mov");
            BigDecimal totalCompra = esMov == 0 ? item.getBigDecimal_Zero("qtd") : new BigDecimal(0);
            BigDecimal totalVenda = esMov == 1 ? item.getBigDecimal_Zero("qtd") : new BigDecimal(0);

            idsItensMov.add(idItem);

            item.put("saldo", saldo);
            item.put("totCompra", totalCompra);
            item.put("totVenda", totalVenda);
            item.put("excesso", ((saldo + totalCompra - totalVenda) - item.getBigDecimal_Zero("abm0101estMax")));

            Integer itemValido = verificarItem(item, saldo, detalhamento);

            if (itemValido == 0) continue;
            if (naoImprimirSaldoZero && saldo == 0) continue

            TableMap tmMediaConsumo = mediaConsumoItens.stream().filter({tm -> tm.getLong("bcc01item") == idItem}).findFirst().orElse(null);
            if(tmMediaConsumo != null) item.put("mediaConsumo", tmMediaConsumo.getBigDecimal_Zero("media").round(2));

            registros.add(item)
        }

        List<TableMap> itensSemMov = buscarItensSemMovimentacao(idsItensMov, mps, itens, itemMovEstoque, itemNaoMovEst);

        for (item in itensSemMov) {
            Long idItem = item.getLong("abm01id");
            BigDecimal saldo = buscarSaldoItem(idItem, dataSaldo, idsStatus, idsLocal, loteIni, loteFin, serieIni, serieFin);

            item.put("saldo", saldo);
            item.put("totCompra", new BigDecimal(0));
            item.put("totVenda", new BigDecimal(0));
            item.put("excesso", saldo - item.getBigDecimal_Zero("abm0101estMax"));

            Integer itemValido = verificarItem(item, saldo, detalhamento);

            if (itemValido == 0) continue;
            if (naoImprimirSaldoZero && saldo == 0) continue;

            TableMap tmMediaConsumo = mediaConsumoItens.stream().filter({tm -> tm.getLong("bcc01item") == idItem}).findFirst().orElse(null);
            if(tmMediaConsumo != null) item.put("mediaConsumo", tmMediaConsumo.getBigDecimal_Zero("media").round(2));

            registros.add(item)

        }

        return registros;

    }

    private List<TableMap> buscarMediaVendaUltimos3Meses(List<Long> idsItens) {
        String whereItem = idsItens != null && idsItens.size() > 0 ? "AND bcc01item IN (:idsItens) " : "";
        Parametro parametroItem = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;

        String sql = "SELECT bcc01item, AVG(bcc01qt) AS media " +
                    "FROM bcc01 " +
                    "WHERE bcc01data >= CURRENT_DATE - INTERVAL '3 months' " +
                    //"AND bcc01ple = 128275 " +
                    "AND bcc01mov = 1 "+
                    whereItem +
                    "GROUP BY bcc01item";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItem);
    }

    private List<TableMap> buscarItensComMovimentacao(List<Integer> tipos, List<Long> itens, Boolean itemMovEstoque, Boolean itemNaoMovEst, LocalDate[] dtPedidos, LocalDate[] dtEntrega, List<Integer> atendimentos) {

        def movEst = !itemNaoMovEst && !itemMovEstoque ? [-1] : new ArrayList();

        if (itemNaoMovEst) movEst.add(0);
        if (itemMovEstoque) movEst.add(1);

        String whereGrupo = "WHERE abm01grupo = 0 ";
        String whereClasDoc = "AND eaa01clasdoc = 0 ";
        String whereTipo = tipos != null && !tipos.contains(-1) ? "AND abm01tipo IN (:tipos) " : null;
        String whereEmpresa = "AND abm01gc = :empresa ";
        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        String whereInativo = "AND abm01di IS NULL ";
        String whereMovEst = !movEst.contains(-1) ? "AND abm11movEst IN (:movEst) " : "";
        String whereDatas = dtPedidos != null && dtEntrega != null ? "AND (abb01data BETWEEN :dtPedidosIni AND :dtPedidosFin OR eaa0103dtEntrega BETWEEN :dtEntregaIni AND :dtEntregaFin) " :
                dtPedidos != null && dtEntrega == null ? "AND abb01data BETWEEN :dtPedidosIni AND :dtPedidosFin " :
                        dtPedidos == null && dtEntrega != null ? "eaa0103dtEntrega BETWEEN :dtEntregaIni AND :dtEntregaFin " : "";
        String whereAtendimento = atendimentos != null && atendimentos.size() > 0 ? "AND eaa01scvatend IN (:atendimentos) " : "AND eaa01scvatend IN (0,1) "


        Parametro parametroTipo = tipos != null && !tipos.contains(-1) ? Parametro.criar("tipos", tipos) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroMOvEst = !movEst.contains(-1) ? Parametro.criar("movEst", movEst) : null;
        Parametro parametroDataPedidoIni = dtPedidos != null ? Parametro.criar("dtPedidosIni", dtPedidos[0]) : null;
        Parametro parametroDataPedidoFin = dtPedidos != null ? Parametro.criar("dtPedidosFin", dtPedidos[1]) : null;
        Parametro parametroDataEntregaIni = dtEntrega != null ? Parametro.criar("dtEntregaIni", dtEntrega[0]) : null;
        Parametro parametroDataEntregaFin = dtEntrega != null ? Parametro.criar("dtEntregaFin", dtEntrega[1]) : null;
        Parametro parametroAtendimento = atendimentos != null && atendimentos.size() > 0 ? Parametro.criar("atendimentos", atendimentos) : Parametro.criar("atendimentos", [0, 1])

        String sql = "SELECT abm01tipo,abm01id, abm01codigo AS codItem, CASE WHEN abm01tipo = 0 THEN 'M' ELSE 'P' END AS mps, abm01na AS naItem, abm0101estMax, " +
                "abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo,eaa01esmov AS mov, COALESCE(SUM(eaa0103qtUso),0) - COALESCE(SUM(eaa01032qtUso),0) AS qtd " +
                "FROM abm01 " +
                "LEFT JOIN eaa0103 ON eaa0103item = abm01id " +
                "LEFT JOIN eaa01032 ON eaa01032itemscv = eaa0103id " +
                "LEFT JOIN eaa01 ON eaa01id = eaa0103doc AND eaa01cancdata IS NULL " +
                "LEFT JOIN abb01 ON abb01id = eaa01central " +
                "LEFT JOIN abm0101 ON abm0101item = abm01id " +
                "LEFT JOIN abm11 ON abm11id = abm0101estoque " +
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                whereGrupo +
                whereClasDoc +
                whereTipo +
                whereEmpresa +
                whereItens +
                whereInativo +
                whereMovEst +
                whereDatas +
                whereAtendimento +
                "GROUP BY abm01tipo, abm01id, abm01codigo,mps, abm01na, abm0101estMax," +
                "abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo, eaa01esmov " +
                "ORDER BY abm01tipo, abm01codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipo, parametroEmpresa, parametroItens, parametroMOvEst, parametroDataPedidoIni, parametroDataPedidoFin, parametroDataEntregaIni, parametroDataEntregaFin, parametroAtendimento)

    }

    private List<TableMap> buscarItensSemMovimentacao(List<Long> idItensAux, List<Integer> tipos, List<Long> itens, Boolean itemMovEstoque, Boolean itemNaoMovEst) {

        def movEst = !itemNaoMovEst && !itemMovEstoque ? [-1] : new ArrayList();

        if (itemNaoMovEst) movEst.add(0);
        if (itemMovEstoque) movEst.add(1);

        String whereGrupo = "WHERE abm01grupo = 0 ";
        String whereTipo = tipos != null && !tipos.contains(-1) ? "AND abm01tipo IN (:tipos) " : null;
        String whereEmpresa = "AND abm01gc = :empresa ";
        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        String whereItensAux = "AND abm01id NOT IN (:idItensAux) ";
        String whereInativo = "AND abm01di IS NULL ";
        String whereMovEst = !movEst.contains(-1) ? "AND abm11movEst IN (:movEst) " : "";


        Parametro parametroTipo = tipos != null && !tipos.contains(-1) ? Parametro.criar("tipos", tipos) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;
        Parametro parametroItensAux = Parametro.criar("idItensAux", idItensAux);
        Parametro parametroMovEst = !movEst.contains(-1) ? Parametro.criar("movEst", movEst) : null;

        String sql = "SELECT abm01tipo,abm01id, abm01codigo AS codItem, CASE WHEN abm01tipo = 0 THEN 'M' ELSE 'P' END AS mps, abm01na AS naItem, abm0101estMax, " +
                "abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo " +
                "FROM abm01 " +
                "LEFT JOIN abm0101 ON abm0101item = abm01id " +
                "LEFT JOIN abm11 ON abm11id = abm0101estoque " +
                "LEFT JOIN aam06 ON aam06id = abm01umu " +
                whereGrupo +
                whereTipo +
                whereEmpresa +
                whereItens +
                whereItensAux +
                whereInativo +
                whereMovEst +
                "GROUP BY abm01tipo, abm01id, abm01codigo,mps, abm01na, abm0101estMax," +
                "abm0101estMin, abm0101estSeg, abm0101ptoPed,aam06codigo " +
                "ORDER BY abm01tipo, abm01codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipo, parametroEmpresa, parametroItens, parametroItensAux, parametroMovEst)

    }

    private BigDecimal buscarSaldoItem(Long abm01id, LocalDate data, List<Long> idsStatus, List<Long> idsLocal, String loteIni, String loteFin, String serieIni, String serieFin) {
        String whereStatus = idsStatus != null && idsStatus.size() > 0 ? "AND bcc01status IN (:idsStatus) " : "";
        String whereLocal = idsLocal != null && idsLocal.size() > 0 ? "AND bcc01ctrl0 IN (:idsLocal) " : "";
        String whereLoteIni = loteIni != null && !loteIni.isEmpty() ? " AND bcc01lote >= :loteIni " : "";
        String whereLoteFin = loteFin != null && !loteFin.isEmpty() ? " AND bcc01lote <= :loteFin " : "";
        String whereSerieIni = serieIni != null && !serieIni.isEmpty() ? " AND bcc01serie >= :serieIni " : "";
        String whereSerieFin = serieFin != null && !serieFin.isEmpty() ? " AND bcc01serie <= :loteFin " : "";

        Parametro paramItem = Parametro.criar("abm01id", abm01id);
        Parametro paramData = Parametro.criar("data", data);
        Parametro paramLoteIni = Parametro.criar("loteIni", loteIni);
        Parametro paramLoteFin = Parametro.criar("loteFin", loteFin);
        Parametro paramSerieIni = Parametro.criar("serieIni", serieIni);
        Parametro paramSerieFin = Parametro.criar("serieFin", serieFin);
        Parametro parametroStatus = idsStatus != null && idsStatus.size() > 0 ? Parametro.criar("idsStatus", idsStatus) : null;
        Parametro parametroLocal = idsLocal != null && idsLocal.size() > 0 ? Parametro.criar("idsLocal", idsLocal) : null;


        String sql = "SELECT COALESCE(sum(bcc01qtps),0) AS qtd " +
                "FROM bcc01 " +
                "INNER JOIN abm01 ON abm01id = bcc01item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "INNER JOIN aam06 ON aam06id = abm01umu " +
                "WHERE abm01id = :abm01id " +
                "AND bcc01data <= :data " +
                whereStatus +
                whereLocal +
                whereLoteIni +
                whereLoteFin +
                whereSerieIni +
                whereSerieFin;

        return getAcessoAoBanco().obterBigDecimal(sql, paramItem, paramData, paramLoteIni, paramLoteFin, paramSerieIni, paramSerieFin, parametroStatus, parametroLocal);
    }

    private Integer verificarItem(TableMap item, BigDecimal saldo, Integer detalhamento) {
        /*
            Verifica se o item atende os requisitos do filtro de detalhamento, se sim retorna 1, caso contrário retorna 0

            Somente serão exibidos os itens caso o retorno dessa função for igual 1

         */

        def estMax = item.getBigDecimal_Zero("abm0101estMax");
        def estMin = item.getBigDecimal_Zero("abm0101estMin");
        def pontoPedido = item.getBigDecimal_Zero("abm0101ptoPed");

        if (saldo > estMax && detalhamento == 0) return 1;
        if (saldo < estMin && detalhamento == 1) return 1;
        if ((saldo >= pontoPedido && saldo <= pontoPedido) && detalhamento == 2) return 1;
        if (detalhamento == 3) return 1;

        return 0
    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIEl0ZW5zIEZvcmEgZG9zIE5pdmVpcyBkZSBFc3RvcXVlIiwidGlwbyI6InJlbGF0b3JpbyJ9