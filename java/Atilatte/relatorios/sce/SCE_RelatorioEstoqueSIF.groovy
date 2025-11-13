// Relatório SIF Finalizado
package Atilatte.relatorios.sce

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table
import sam.core.variaveis.MDate;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class SCE_RelatorioEstoqueSIF extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Relatorio Estoque (SIF)";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("detalhamento", "0");
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {

        //if(obterUsuarioLogado().getAab10id() != 1331927) interromper("Relatorio em manutenção")
        List<Integer> mps = getListInteger("mps");
        List<Long> itens = getListLong("itens");
        LocalDate[] periodo = getIntervaloDatas("periodo");
        LocalDate[] dtDevolucao = getIntervaloDatas("dtDevolucao");
        Integer impressao = getInteger("impressao");
        List<Long> status = getListLong("status");
        List<Long> local = getListLong("local");
        List<Long> tipoDoc = getListLong("tipo");
        List<Long> pleInvent = getListLong("pleInventario");
        List<Long> statusInvent = getListLong("statusInvent");
        List<Long> localInvent = getListLong("localInvent");
        List<Long> pleTransf = getListLong("pleTransferencia");
        List<Long> statusTransf = getListLong("statusTransf");
        List<Long> localTransf = getListLong("localTransf");
        Boolean impressaoQuilo = getBoolean("impressaoQuilo");

        List<TableMap> dados = buscarDadosRelatorioItem(itens, periodo, status, local, tipoDoc, pleInvent,statusInvent,localInvent, pleTransf,statusTransf,localTransf,impressaoQuilo, dtDevolucao  )

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() +"-"+ obterEmpresaAtiva().getAac10na());
        params.put("periodo", "Periodo " + periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString())

        if(impressaoQuilo){
            if(impressao == 0 ){
                params.put("titulo", "Relatorio Estoque SIF Itens (Quilo) ");
                return gerarPDF("SCE_RelatorioEstoqueSifItensQuiloPDF", dados);
            }else{
                return gerarXLSX("SCE_RelatorioEstoqueSifItensQuiloExcel", dados);
            }
        }else{
            if(impressao == 0 ){
                params.put("titulo", "Relatorio Estoque SIF Itens (Litro) ");
                return gerarPDF("SCE_RelatorioEstoqueSifItensPDF", dados);
            }else{
                return gerarXLSX("SCE_RelatorioEstoqueSifItensExcel", dados);
            }
        }

    }

    private List<TableMap> buscarDadosRelatorioItem(List<Long> itens, LocalDate[] periodo, List<Long> status, List<Long> local, List<Long> tipoDoc, List<Long> pleInvent, List<Long> statusInvent, List<Long> localInvent, List<Long> pleTransf, List<Long> statusTransf, List<Long> localTransf, Boolean impressaoQuilo, LocalDate[] dtDevolucao ){

        List<TableMap> listItens = buscarItensRelatorio(itens);
        List<Long> listIdsItens = buscarIDsItens(itens);
        List<TableMap> devolucoesSemEstoque = buscarDevolucoesSemEstoque(listIdsItens, periodo, tipoDoc);
        List<TableMap> devolucoesComEstoque = buscarDevolucoesComEstoque(listIdsItens, periodo, tipoDoc);


        for(item in listItens){
            def fatorQuilo = item.getBigDecimal("fatorQuilo");
            if(fatorQuilo == null) interromper("Não foi informado fator de conversão de quilo no item " + item.getString("abm01codigo"));
            Long idItem = item.getLong("abm01id");
            BigDecimal devSemEstoque = BigDecimal.ZERO;
            BigDecimal devComEstoque = BigDecimal.ZERO;


            // Compoe devoluções sem estoque do item
            devolucoesSemEstoque.forEach(
                    devolucao ->{
                        if(devolucao.getLong("id") == idItem){
                            devSemEstoque = devolucao.getBigDecimal_Zero("qtd");
                        }
                    }
            );

            // Compoe devoluções sem estoque do item
            devolucoesComEstoque.forEach(
                    devolucao ->{
                        if(devolucao.getLong("id") == idItem){
                            devComEstoque = devolucao.getBigDecimal_Zero("qtd");
                        }
                    }
            )


            BigDecimal saldoInicial = buscarSaldoInicial(idItem, periodo, status, local );
            BigDecimal qtdProdFrasco = buscarProducaoItem(idItem, periodo);
            TableMap saidaItem = buscarSaidasItens(idItem,periodo, tipoDoc) == null ? new TableMap() : buscarSaidasItens(idItem,periodo, tipoDoc);
            BigDecimal vendasFrascos= saidaItem.getBigDecimal_Zero("qtdVendida");
            BigDecimal estoqueFinalFrasco = buscarEstoqueFinal(idItem, periodo, status, local);
            BigDecimal inventarioFrasco = buscarInventarioItem(idItem,periodo, pleInvent, statusInvent, localInvent)
            BigDecimal transfFrasco = buscarTransferenciaItem(idItem,periodo, pleTransf, statusTransf, localTransf)

            item.put("fator", fatorQuilo);
            item.put("saldoInicial", saldoInicial);
            item.put("qtdProdFrasco", qtdProdFrasco);
            item.put("vendasFrascos", vendasFrascos);
            item.put("estoqueFinalFrasco", estoqueFinalFrasco);
            item.put("inventarioFrasco", inventarioFrasco );
            item.put("transfFrasco", transfFrasco );
            item.put("devSemEstoqueFrasco", devSemEstoque);
            item.put("devComEstoqueFrasco", devComEstoque);

            if(impressaoQuilo){
                item.put("qtdProdQuilo", qtdProdFrasco * fatorQuilo);
                item.put("vendasQuilo", vendasFrascos * fatorQuilo);
                item.put("estoqueFinalQuilo", estoqueFinalFrasco * fatorQuilo);
                item.put("inventarioQuilo", inventarioFrasco * fatorQuilo);
                item.put("transfQuilo", transfFrasco * fatorQuilo );
                item.put("devSemEstoqueQuilo", devSemEstoque * fatorQuilo );
                item.put("devComEstoqueQuilo", devComEstoque * fatorQuilo );
            }

        }

        return  listItens;
    }

    private List<TableMap> buscarItensRelatorio(List<Long> itens){

        String whereItem = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        Parametro parametroItem = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;

        String sql = "SELECT abm01id, abm01codigo, abm01na, CAST(abm0101json ->> 'fator_litro' AS numeric(18,6)) as fatorQuilo, aba3001descr AS classificacao " +
                "FROM abm01 " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "INNER JOIN abm0102 ON abm0102item = abm01id " +
                "INNER JOIN aba3001 ON aba3001id = abm0102criterio " +
                "INNER JOIN aba30 ON aba3001criterio = aba30id " +
                "WHERE aba30nome = 'SIF' " +
                whereItem +
                "ORDER BY abm01codigo"


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItem);
    }
    private List<Long> buscarIDsItens(List<Long> itens){

        String whereItem = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
        Parametro parametroItem = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;

        String sql = "SELECT abm01id "+
                        "FROM abm01 " +
                        "INNER JOIN abm0101 ON abm0101item = abm01id " +
                        "INNER JOIN abm0102 ON abm0102item = abm01id " +
                        "INNER JOIN aba3001 ON aba3001id = abm0102criterio " +
                        "INNER JOIN aba30 ON aba3001criterio = aba30id " +
                        "WHERE aba30nome = 'SIF' " +
                        whereItem +
                        "ORDER BY abm01codigo"

        return getAcessoAoBanco().obterListaDeLong(sql, parametroItem);
    }

    private BigDecimal buscarSaldoInicial(Long idItem, LocalDate[] periodo, List<Long> status, List<Long> local){
        LocalDate periodoAnterior = periodo[0].minusDays(1)

        String whereItem = "WHERE abm01id = :idItem ";
        String wherePeriodo = "AND bcc01data <= :periodoAnterior ";
        String whereStatus = status != null && status.size() > 0 ? "AND bcc01status IN (:status) " : "";
        String whereLocal = local != null && local.size() > 0 ? "AND bcc01ctrl0 IN (:local) " : "";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroPeriodo = Parametro.criar("periodoAnterior",periodoAnterior);
        Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status",status) : null;
        Parametro parametroLocal = local != null && local.size() > 0 ? Parametro.criar("local",local) : null;


        String sql = "SELECT SUM(bcc01qtPS) AS quantidade FROM bcc01 " +
                "INNER JOIN Abm01 ON abm01id = bcc01item " +
                "LEFT JOIN Aam06 ON aam06id = abm01umu " +
                whereItem +
                wherePeriodo +
                whereStatus +
                whereLocal +
                "GROUP BY abm01tipo, abm01codigo, abm01na, abm01grupo, aam06codigo "

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroPeriodo, parametroStatus, parametroLocal )
    }

    private BigDecimal buscarEstoqueFinal(Long idItem, LocalDate[] periodo, List<Long> status, List<Long> local){
        LocalDate periodoSaldo = periodo[1]

        String whereItem = "WHERE abm01id = :idItem ";
        String wherePeriodo = "AND bcc01data <= :periodo ";
        String whereStatus = status != null && status.size() > 0 ? "AND bcc01status IN (:status) " : "";
        String whereLocal = local != null && local.size() > 0 ? "AND bcc01ctrl0 IN (:local) " : "";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroPeriodo = Parametro.criar("periodo",periodoSaldo);
        Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status",status) : null;
        Parametro parametroLocal = local != null && local.size() > 0 ? Parametro.criar("local",local) : null;

        String sql = "SELECT SUM(bcc01qtPS) AS quantidade FROM bcc01 " +
                "INNER JOIN Abm01 ON abm01id = bcc01item " +
                "LEFT JOIN Aam06 ON aam06id = abm01umu " +
                whereItem +
                wherePeriodo+
                whereStatus +
                whereLocal;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroPeriodo, parametroStatus, parametroLocal )
    }

    private BigDecimal buscarProducaoItem(Long idItem, LocalDate[] periodo){
        String whereItem = " WHERE abm01id = :idItem ";
        String whereData = " AND bab0104data BETWEEN :dtInicial AND :dtFinal ";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial",periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal",periodo[1]);

        String sql = "SELECT SUM(bab01041qt) AS qtdProduzida " +
                "FROM bab01 " +
                "INNER JOIN abp20 ON abp20id = bab01comp " +
                "INNER JOIN abm01 ON abm01id = abp20item " +
                "INNER JOIN bab0104 ON bab0104op = bab01id " +
                "INNER JOIN bab01041 ON bab01041pc = bab0104id " +
                whereItem +
                whereData +
                "GROUP BY abm01codigo"

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroDtIni, parametroDtFin);
    }

    private TableMap buscarSaidasItens(Long idItem, LocalDate[] periodo, List<Long> tipoDoc){
        String whereItem = " WHERE eaa0103item = :idItem "
        String whereData = " AND abb01data between :dtInicial AND :dtFinal ";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "AND abb01tipo IN (:tipoDoc) "  : "";
        String whereCancData = " AND eaa01cancdata IS NULL ";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial",periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal",periodo[1]);
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;

        String sql = "SELECT SUM(eaa0103qtComl) AS qtdVendida " +
                " FROM eaa0103 AS eaa0103 " +
                " INNER JOIN eaa01 ON eaa01id = eaa0103doc " +
                " INNER JOIN abd01 on eaa01pcd = abd01id " +
                " INNER JOIN abb01 ON abb01id = eaa01central " +
                whereItem +
                whereData +
                whereTipoDoc +
                whereCancData +
                " AND eaa01esmov = 1 " +
                " AND abd01isce = 1 " +
                " AND eaa01isce = 1 " +
                " AND eaa01clasdoc = 1 ";


        return getAcessoAoBanco().buscarUnicoTableMap(sql, parametroItem, parametroDtIni, parametroDtFin, parametroTipoDoc);
    }
    private List<TableMap> buscarDevolucoesSemEstoque(List<Long> idsItem, LocalDate[] periodo, List<Long> tiposDoc){
        String whereItem = " WHERE eaa0103item IN (:idsItem) ";
        String wherePeriodo = periodo != null ? "AND abb01data BETWEEN :dataInicial AND :dataFinal " : "";
        String whereMovEstoque = "AND abd01isce = 0 AND eaa01isce = 0 ";
        String whereQtComl =  " AND (eaa01033qtComl > 0 OR eaa01033qtUso > 0) "
        String whereTipoDoc = tiposDoc != null && tiposDoc.size() > 0 ? "AND abb01tipo IN (:tiposDoc) " : "";

        String sql = " SELECT eaa0103item AS id, SUM(eaa01033qtComl) AS qtd  "+
                " FROM eaa01033 "+
                " INNER JOIN eaa0103 on eaa0103id = eaa01033item  "+
                " INNER JOIN eaa01 ON eaa01id = eaa0103doc "+
                " INNER JOIN abb01 ON abb01id = eaa01central "+
                " INNER JOIN abd01 ON abd01id = eaa01pcd "+
                whereItem +
                wherePeriodo +
                whereMovEstoque +
                whereQtComl +
                whereTipoDoc +
                "GROUP BY eaa0103item ";

        Parametro parametroItens = Parametro.criar("idsItem", idsItem);
        Parametro parametroPeriodoInicial = periodo != null ? Parametro.criar("dataInicial", periodo[0]) : null;
        Parametro parametroPeriodoFinal = periodo != null ? Parametro.criar("dataFinal", periodo[1]) : null;
        Parametro parametroTipoDoc = tiposDoc != null && tiposDoc.size() > 0 ? Parametro.criar("tiposDoc", tiposDoc) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens, parametroPeriodoInicial, parametroPeriodoFinal, parametroTipoDoc);
    }
    private List<TableMap> buscarDevolucoesComEstoque(List<Long> idsItem, LocalDate[] periodo, List<Long> tiposDoc){
        String whereItem = " WHERE eaa0103item IN (:idsItem) ";
        String wherePeriodo = periodo != null ? "AND abb01data BETWEEN :dataInicial AND :dataFinal " : "";
        String whereMovEstoque = "AND abd01isce = 1 AND eaa01isce = 1 ";
        String whereQtComl =  " AND (eaa01033qtComl > 0 OR eaa01033qtUso > 0) "
        String whereTipoDoc = tiposDoc != null && tiposDoc.size() > 0 ? "AND abb01tipo IN (:tiposDoc) " : "";

        String sql = " SELECT eaa0103item AS id, SUM(eaa01033qtComl) AS qtd  "+
                " FROM eaa01033 "+
                " INNER JOIN eaa0103 on eaa0103id = eaa01033item  "+
                " INNER JOIN eaa01 ON eaa01id = eaa0103doc "+
                " INNER JOIN abb01 ON abb01id = eaa01central "+
                " INNER JOIN abd01 ON abd01id = eaa01pcd "+
                whereItem +
                wherePeriodo +
                whereMovEstoque +
                whereQtComl +
                whereTipoDoc +
                "GROUP BY eaa0103item ";

        Parametro parametroItens = Parametro.criar("idsItem", idsItem);
        Parametro parametroPeriodoInicial = periodo != null ? Parametro.criar("dataInicial", periodo[0]) : null;
        Parametro parametroPeriodoFinal = periodo != null ? Parametro.criar("dataFinal", periodo[1]) : null;
        Parametro parametroTipoDoc = tiposDoc != null && tiposDoc.size() > 0 ? Parametro.criar("tiposDoc", tiposDoc) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroItens, parametroPeriodoInicial, parametroPeriodoFinal, parametroTipoDoc);
    }
    private BigDecimal buscarInventarioItem(Long idItem, LocalDate[] periodo, List<Long> pleInvent, List<Long> statusInvent, List<Long> localInvent){
        String wherePLE = pleInvent != null && pleInvent.size() > 0 ? " AND bcc01ple IN (:pleInvent) " : "";
        String whereItem =  " AND bcc01item = :idItem ";
        String wherePeriodo = " AND bcc01data BETWEEN :dtInicial  AND :dtFinal ";
        String whereStatus = " AND bcc01status IN (:statusInvent) ";
        String whereLocal = " AND bcc01ctrl0 IN (:localInvent) ";

        Parametro parametroPLE = pleInvent != null && pleInvent.size() > 0 ? Parametro.criar("pleInvent", pleInvent) : null;
        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial", periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal", periodo[1]);
        Parametro parametroStatus = Parametro.criar("statusInvent", statusInvent);
        Parametro parametroLocal = Parametro.criar("localInvent", localInvent)


        String sql = "SELECT SUM(bcc01qtps) as inventario " +
                " FROM bcc01 " +
                " WHERE TRUE "+
                wherePLE+
                whereItem+
                wherePeriodo+
                whereStatus +
                whereLocal;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroPLE, parametroItem, parametroDtIni, parametroDtFin, parametroStatus, parametroLocal );
    }
    private BigDecimal buscarTransferenciaItem(Long idItem, LocalDate[] periodo, List<Long> pleInvent, List<Long> statusTransf, List<Long> localTransf){
        String wherePLE = pleInvent != null && pleInvent.size() > 0 ? " AND bcc01ple in (:pleInvent) " : "";
        String whereItem =  " AND bcc01item = :idItem ";
        String wherePeriodo = " AND bcc01data between :dtInicial AND :dtFinal ";
        String whereStatus = statusTransf != null && statusTransf.size() > 0 ? " AND bcc01status IN (:statusTransf) " : "";
        String whereLocal = localTransf != null && localTransf.size() > 0 ? " AND bcc01ctrl0 IN (:localTransf) " : "";

        Parametro parametroPLE = pleInvent != null && pleInvent.size() > 0 ? Parametro.criar("pleInvent", pleInvent) : null;
        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial", periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal", periodo[1]);
        Parametro parametroStatus = statusTransf != null && statusTransf.size() > 0 ? Parametro.criar("statusTransf", statusTransf) : null;
        Parametro parametroLocal = localTransf != null && localTransf.size() > 0 ? Parametro.criar("localTransf", localTransf) : null;

        String sql = "SELECT SUM(bcc01qtps) as transferencia " +
                " FROM bcc01 " +
                " WHERE TRUE "+
                wherePLE+
                whereItem+
                whereStatus +
                whereLocal +
                wherePeriodo

        return getAcessoAoBanco().obterBigDecimal(sql, parametroPLE, parametroItem, parametroDtIni, parametroDtFin, parametroStatus, parametroLocal );
    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0b3JpbyBFc3RvcXVlIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0b3JpbyBFc3RvcXVlIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0b3JpbyBFc3RvcXVlIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9