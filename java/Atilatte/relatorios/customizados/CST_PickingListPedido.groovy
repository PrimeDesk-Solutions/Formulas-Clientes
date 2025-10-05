package Atilatte.relatorios.customizados;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ea.Eaa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Map;
import java.util.HashMap;

public class CST_PickingListPedido extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Picking List Pedido";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();

        String sql = "select distinct bfb01id from bfa01 " +
                "inner join bfa0101 on bfa0101rom = bfa01id  " +
                "inner join bfa01011 on bfa01011item = bfa0101id  " +
                "inner join abb01 on abb01id = bfa01docscv " +
                "inner join bfb0101 on bfb0101central = abb01id " +
                "inner join bfb01 on bfb01id = bfb0101lote "

        List<Long> listIds = getAcessoAoBanco().obterListaDeLong(sql);


        filtrosDefault.put("listIds", listIds);

        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> despacho = getListLong("despacho");
        List<Long> redespacho = getListLong("redespacho");
        LocalDate[] dtEmissao = getIntervaloDatas("emissao");
        List<Long> capaLote = getListLong("capaLote");

        LocalDate dtEmisIni = null;
        LocalDate dtEmisFin = null;

        if (dtEmissao != null) {
            dtEmisIni = dtEmissao[0];
            dtEmisFin = dtEmissao[1];
        }

        // Data Atual Sistema
        LocalDateTime dataAtual = LocalDateTime.now();

        // Formato de data e hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Formata a data e hora de acordo com o formato especificado
        String dtEmissaoRelatorio = dataAtual.format(formatter);

        // Periodo
        String periodo = "";
        if (dtEmissao != null) {
            periodo = "Período Emissão : " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        }

        params.put("periodo", periodo);
        params.put("emissaoRelatorio", "Emissão: " + dtEmissaoRelatorio)

        List<TableMap> documentos = buscarPedidos(numeroInicial, numeroFinal, dtEmisIni, dtEmisFin, despacho, redespacho, capaLote);

        for (documento in documentos) {

            // Define quantidade Max. e Min. de separação
            definirQuantidadeSeparacao(documento);

            BigDecimal caixa = definirQuantidadeDeCaixa(documento);

            documento.put("caixa", caixa);

        }

        return gerarPDF("CST_PickingList_Pedido", documentos);

    }

    private List<TableMap> buscarPedidos(Integer numeroInicial, Integer numeroFinal,
                                         LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> despacho, List<Long> redespacho, List<Long> capaLote) {

        String whereNumDoc = numeroInicial != null && numeroFinal != null ? "AND  abb01Num BETWEEN :numeroInicial AND :numeroFinal " : ""
        String whereDtEmissao = dtEmisIni != null && dtEmisFin != null ? "AND abb01data BETWEEN :dtEmisIni AND :dtEmisFin " : ""
        String whereDespacho = despacho != null && despacho.size() > 0 ? " AND desp.abe01id IN (:despacho)   " : "";
        String whereRedespacho = redespacho != null && redespacho.size() > 0 ? " AND desp.abe01id IN (:redespacho)   " : "";
        String whereCapaLote = capaLote != null && capaLote.size() > 0 ? "AND bfb01id IN (:capaLote)" : "";

        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroDtEmissaoIni = dtEmisIni != null ? Parametro.criar("dtEmisIni", dtEmisIni) : null;
        Parametro parametroDtEmissaoFin = dtEmisFin != null ? Parametro.criar("dtEmisFin", dtEmisFin) : null;
        Parametro parametroDespacho = despacho != null && despacho.size() > 0 ? Parametro.criar("despacho", despacho) : null;
        Parametro parametroRedespacho = redespacho != null && redespacho.size() > 0 ? Parametro.criar("redespacho", redespacho) : null;
        Parametro parametroCapaLote = capaLote != null && capaLote.size() > 0 ? Parametro.criar("capaLote", capaLote) : null

        String sql = "SELECT DISTINCT eaa01id,abb01num AS numPedido,desp.abe01codigo AS codDespacho,desp.abe01na AS naDespacho,redesp.abe01codigo AS codReDespacho, redesp.abe01na AS naRedespacho, " +
                "CAST(eaa0103json ->>'volumes' AS numeric(18,6)) AS volumes,cliente.abe01codigo AS codcliente, cliente.abe01na AS nomecliente, " +
                "eaa01esdata, abe02vidautil AS vida,CAST(abe02json ->> 'qtd_max_lote' as numeric(10,0)) AS qtdLote,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2))::int AS OrdemSeparacao, " +
                "abm01codigo AS coditem,abm01na AS naitem, bfa01011lote AS Lote,bfa01011qtUso AS qtd, aam06codigo AS unidadeMedida, " +
                "CAST(abm0101json ->>'cvdnf' AS numeric(18,6)) AS cvdnf, " +
                "CAST(abm0101json ->>'volume_caixa' AS numeric(18,6)) AS volumeCaixa,CAST(abm0101json ->>'peso_caixa' AS numeric(18,6)) AS pesoCaixa,CAST(abm0101json ->>'unidperc' AS numeric(18,6)) AS percentualSep " +
                "FROM bfa01 " +
                "INNER JOIN bfa0101 ON bfa0101rom = bfa01id " +
                "INNER JOIN bfa01011 ON bfa01011item = bfa0101id " +
                "INNER JOIN abb01 ON abb01id = bfa01docscv " +
                "INNER JOIN bfb0101 ON bfb0101central = abb01id " +
                "INNER JOIN bfb01 ON bfb01id = bfb0101lote " +
                "INNER JOIN eaa01 ON eaa01central = abb01id " +
                "INNER JOIN eaa0102 ON eaa0102doc = eaa01id " +
                "LEFT JOIN abe01 as desp ON desp.abe01id = eaa0102despacho " +
                "LEFT JOIN abe01 as redesp ON redesp.abe01id = eaa0102redespacho " +
                "INNER JOIN eaa0103 ON eaa0103id = bfa0101item " +
                "INNER JOIN abe01 as cliente ON cliente.abe01id = abb01ent " +
                "left JOIN abe02 ON abe02ent = cliente.abe01id " +
                "INNER JOIN abm01 ON abm01id = eaa0103item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "INNER JOIN aam06 ON aam06id = abm01umu " +
                "WHERE TRUE " +
                whereNumDoc +
                whereDtEmissao +
                whereDespacho +
                whereRedespacho +
                whereCapaLote +
                "ORDER BY abb01num,OrdemSeparacao";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDespacho, parametroRedespacho, parametroCapaLote)
    }

    private BigDecimal definirQuantidadeDeCaixa(TableMap documento) {
        String umu = documento.getString("unidadeMedida");
        BigDecimal quantidade = documento.getBigDecimal_Zero("qtd");
        BigDecimal caixa;

        if (umu.toUpperCase() == 'KG') {
            caixa = quantidade / documento.getBigDecimal_Zero("pesoCaixa")
            caixa = new BigDecimal(caixa).setScale(0, BigDecimal.ROUND_DOWN)
        } else {
            caixa = quantidade / documento.getBigDecimal_Zero("cvdnf").round(2)

        }

        return caixa
    }

    private void definirQuantidadeSeparacao(TableMap documento) {

        def percentualSep = documento.getBigDecimal_Zero("percentualSep");
        BigDecimal quantidade = documento.getBigDecimal_Zero("QTD");

        if (documento.getString("umu") == 'KG') {
            if (percentualSep == 0) {
                documento.put("qtdMin", 0.0001);
                documento.put("qtdMax", 999999.99999);
            } else if (percentualSep > 0 && percentualSep != 100) {
                if (percentualSep > 100) {
                    percentualSep = percentualSep - 100;
                }
                def qtdMax = quantidade + (quantidade * percentualSep / 100);
                def qtdMin = quantidade - (quantidade * percentualSep / 100);

                documento.put("qtdMin", round(qtdMin, 6));
                documento.put("qtdMax", round(qtdMax, 6))
            }
        } else {
            documento.put("qtdMin", quantidade);
            documento.put("qtdMax", quantidade)
        }
    }
}


//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmcgTGlzdCBQZWRpZG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmcgTGlzdCBQZWRpZG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=