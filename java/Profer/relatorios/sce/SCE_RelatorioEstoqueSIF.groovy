// Relatório SIF Finalizado
package Profer.relatorios.sce

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
        List<Integer> mps = getListInteger("mps");
        List<Long> itens = getListLong("itens");
        LocalDate[] periodo = getIntervaloDatas("periodo");
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

        List<TableMap> dados = buscarDadosRelatorioItem(itens, periodo, status, local, tipoDoc, pleInvent,statusInvent,localInvent, pleTransf,statusTransf,localTransf,impressaoQuilo  )


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

    private List<TableMap> buscarDadosRelatorioItem(List<Long> itens, LocalDate[] periodo, List<Long> status, List<Long> local, List<Long> tipoDoc, List<Long> pleInvent, List<Long> statusInvent, List<Long> localInvent, List<Long> pleTransf, List<Long> statusTransf, List<Long> localTransf, Boolean impressaoQuilo ){

        List<TableMap> saidas = buscarSaidasItens(itens,periodo, tipoDoc);

        for(saida in saidas){
            def fatorQuilo = saida.getBigDecimal("fatorQuilo");

            if(fatorQuilo == null) interromper("Não foi informado fator de conversão de quilo no item " + saida.getString("abm01codigo"));

            Long idItem = saida.getLong("abm01id")
            BigDecimal saldoInicial = buscarSaldoInicial(idItem, periodo, status, local );
            BigDecimal qtdProdFrasco = buscarProducaoItem(idItem, periodo);
            BigDecimal vendasFrascos= saida.getBigDecimal_Zero("qtdVendida");
            BigDecimal devolucoes = buscarDevolucoesItens(idItem,periodo,tipoDoc);
            BigDecimal estoqueFinalFrasco = buscarEstoqueFinal(idItem, periodo);
            BigDecimal inventarioFrasco = buscarInventarioItem(idItem,periodo, pleInvent, statusInvent, localInvent)
            BigDecimal transfFrasco = buscarTransferenciaItem(idItem,periodo, pleTransf, statusTransf, localTransf)

            saida.put("fator", fatorQuilo);
            saida.put("saldoInicial", saldoInicial);
            saida.put("qtdProdFrasco", qtdProdFrasco);
            saida.put("vendasFrascos", vendasFrascos);
            saida.put("devFrasco", devolucoes);
            saida.put("estoqueFinalFrasco", estoqueFinalFrasco);
            saida.put("inventarioFrasco", inventarioFrasco );
            saida.put("transfFrasco", transfFrasco );

            if(impressaoQuilo){
                saida.put("qtdProdQuilo", qtdProdFrasco * fatorQuilo);
                saida.put("vendasQuilo", vendasFrascos * fatorQuilo);
                saida.put("devQuilo", devolucoes * fatorQuilo);
                saida.put("estoqueFinalQuilo", estoqueFinalFrasco * fatorQuilo);
                saida.put("inventarioQuilo", inventarioFrasco * fatorQuilo);
                saida.put("transfQuilo", transfFrasco * fatorQuilo );
            }

        }

        return  saidas;
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

    private BigDecimal buscarEstoqueFinal(Long idItem, LocalDate[] periodo){
        LocalDate periodoSaldo = periodo[1]

        String whereItem = "WHERE abm01id = :idItem ";
        String wherePeriodo = "AND bcc01data <= :periodo ";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroPeriodo = Parametro.criar("periodo",periodoSaldo);

        String sql = "SELECT SUM(bcc01qtPS) AS quantidade FROM bcc01 " +
                "INNER JOIN Abm01 ON abm01id = bcc01item " +
                "LEFT JOIN Aam06 ON aam06id = abm01umu " +
                whereItem +
                wherePeriodo;


        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroPeriodo, parametroStatus, parametroLocal )
    }

    private BigDecimal buscarProducaoItem(Long idItem, LocalDate[] periodo){
        String whereItem = "where abm01id = :idItem ";
        String whereData = "and bab0104data between :dtInicial and :dtFinal ";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial",periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal",periodo[1]);

        String sql = "SELECT SUM(bab01qtp) AS qtdProduzida " +
                "FROM bab01 " +
                "INNER JOIN abp20 ON abp20id = bab01comp " +
                "INNER JOIN abm01 ON abm01id = abp20item " +
                "INNER JOIN bab0104 ON bab0104op = bab01id " +
                whereItem +
                whereData +
                "GROUP BY abm01codigo"

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroDtIni, parametroDtFin);
    }

    private List<TableMap> buscarSaidasItens(List<Long> itens, LocalDate[] periodo, List<Long> tipoDoc){
        String whereItem = itens != null && itens.size() > 0 ? "and eaa0103item IN (:itens) " : "";
        String whereData = "and abb01data between :dtInicial and :dtFinal ";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "and abb01tipo in (:tipoDoc) "  : "";
        String whereCancData = "and eaa01cancdata is null ";

        Parametro parametroItem = itens != null && itens.size() > 0 ? Parametro.criar("itens",itens) : null;
        Parametro parametroDtIni = Parametro.criar("dtInicial",periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal",periodo[1]);
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;

        String sql = "SELECT abm01id, abm01codigo, abm01na,aba3001descr AS classificacao, abm01pesoLiq AS fatorQuilo, " +
                "SUM(eaa0103qtComl) AS qtdVendida " +
                "FROM eaa0103 AS eaa0103 " +
                "INNER JOIN eaa01 ON eaa01id = eaa0103doc " +
                "INNER JOIN abd01 on eaa01pcd = abd01id " +
                "INNER JOIN abb01 ON abb01id = eaa01central " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "INNER JOIN abm0102 ON abm0102item = abm01id " +
                "INNER JOIN aba3001 ON aba3001id = abm0102criterio " +
                "INNER JOIN aba30 ON aba3001criterio = aba30id " +
                "WHERE aba30nome = 'SIF' " +
                whereItem +
                whereData +
                whereTipoDoc +
                whereCancData +
                "AND eaa01esmov = 1 " +
                "and abd01isce = 1 " +
                "and eaa01isce = 1 " +
                "AND eaa01clasdoc = 1 " +
                "GROUP BY abm01id, " +
                "abm01codigo, " +
                "abm01na, " +
                "aba3001descr, " +
                "fatorQuilo " +
                "ORDER BY abm01codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItem, parametroDtIni, parametroDtFin, parametroTipoDoc);
    }

    private BigDecimal buscarDevolucoesItens(Long idItem, LocalDate[] periodo, List<Long> tipoDoc){

        String whereItem = "and eaa0103item IN (:idItem) ";
        String whereData = "and abb01data between :dtInicial and :dtFinal ";
        String whereTipoDoc =  "and abb01tipo in (:tipoDoc) "
        String whereCancData = "and eaa01cancdata is null ";

        Parametro parametroItem = Parametro.criar("idItem",idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial",periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal",periodo[1]);
        Parametro parametroTipoDoc = Parametro.criar("tipoDoc", tipoDoc)

        String sql = "SELECT SUM(eaa0103qtComl) AS qtdDevolvida " +
                "FROM eaa0103 " +
                "INNER JOIN eaa01 ON eaa01id = eaa0103doc "+
                "INNER JOIN abd01 on eaa01pcd = abd01id "+
                "INNER JOIN abb01 ON abb01id = eaa01central "+
                whereItem +
                whereData +
                whereTipoDoc +
                whereCancData +
                "AND eaa01esmov = 0 " +
                "and abd01isce = 1 " +
                "and eaa01isce = 1 " +
                "AND eaa01clasdoc = 1 ";

        return getAcessoAoBanco().obterBigDecimal(sql, parametroItem, parametroDtIni, parametroDtFin, parametroTipoDoc);
    }

    private BigDecimal buscarInventarioItem(Long idItem, LocalDate[] periodo, List<Long> pleInvent, List<Long> statusInvent, List<Long> localInvent){
        String wherePLE = pleInvent != null && pleInvent.size() > 0 ? "AND bcc01ple in (:pleInvent) " : "";
        String whereItem =  "and bcc01item = :idItem ";
        String wherePeriodo = "and bcc01data between :dtInicial and :dtFinal ";
        String whereStatus = "and bcc01status in (:statusInvent) ";
        String whereLocal = "and bcc01ctrl0 in (:localInvent) ";

        Parametro parametroPLE = pleInvent != null && pleInvent.size() > 0 ? Parametro.criar("pleInvent", pleInvent) : null;
        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial", periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal", periodo[1]);
        Parametro parametroStatus = Parametro.criar("statusInvent", statusInvent);
        Parametro parametroLocal = Parametro.criar("localInvent", localInvent)


        String sql = "SELECT SUM(bcc01qtps) as inventario " +
                "FROM bcc01 " +
                "WHERE TRUE "+
                wherePLE+
                whereItem+
                wherePeriodo+
                whereStatus +
                whereLocal;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroPLE, parametroItem, parametroDtIni, parametroDtFin, parametroStatus, parametroLocal );
    }

    private BigDecimal buscarTransferenciaItem(Long idItem, LocalDate[] periodo, List<Long> pleInvent, List<Long> statusTransf, List<Long> localTransf){
        String wherePLE = pleInvent != null && pleInvent.size() > 0 ? "AND bcc01ple in (:pleInvent) " : "";
        String whereItem =  "and bcc01item = :idItem ";
        String wherePeriodo = "and bcc01data between :dtInicial and :dtFinal ";
        String whereStatus = "and bcc01status in (:statusTransf) ";
        String whereLocal = "and bcc01ctrl0 in (:localTransf) ";

        Parametro parametroPLE = pleInvent != null && pleInvent.size() > 0 ? Parametro.criar("pleInvent", pleInvent) : null;
        Parametro parametroItem = Parametro.criar("idItem", idItem);
        Parametro parametroDtIni = Parametro.criar("dtInicial", periodo[0]);
        Parametro parametroDtFin = Parametro.criar("dtFinal", periodo[1]);
        Parametro parametroStatus = Parametro.criar("statusTransf", statusTransf);
        Parametro parametroLocal = Parametro.criar("localTransf", localTransf)

        String sql = "SELECT SUM(bcc01qtps) as transferencia " +
                "FROM bcc01 " +
                "WHERE TRUE "+
                wherePLE+
                whereItem+
                wherePeriodo +
                whereStatus +
                whereLocal;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroPLE, parametroItem, parametroDtIni, parametroDtFin, parametroStatus, parametroLocal );


    }
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0b3JpbyBFc3RvcXVlIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlbGF0b3JpbyBFc3RvcXVlIChTSUYpIiwidGlwbyI6InJlbGF0b3JpbyJ9