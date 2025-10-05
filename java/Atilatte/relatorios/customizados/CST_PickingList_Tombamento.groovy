package Atilatte.relatorios.customizados

import br.com.multiorm.validacao.ValidacaoDeRegistros;

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
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter;

public class CST_PickingList_Tombamento extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Picking List Pedido (Tombamento)";
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
        filtrosDefault.put("impressao", "0");

        return Utils.map("filtros", filtrosDefault);
        //return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> despacho = getListLong("despacho");
        List<Long> redespacho = getListLong("redespacho");
        LocalDate[] emissao = getIntervaloDatas("emissao");
        List<Long> capaLote = getListLong("capaLote");
        Integer impressao = getInteger("impressao");

        capaLote = capaLote == null || capaLote.size() == 0 ? preencherLotesProcessados() : capaLote;

        validarLotes(capaLote);

        LocalDate dtEmisIni = null;
        LocalDate dtEmisFin = null;
        if (emissao != null) {
            dtEmisIni = emissao[0];
            dtEmisFin = emissao[1];
        }

        // Data Atual Sistema
        LocalDateTime now = LocalDateTime.now();

        // Formato de data e hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Formata a data e hora de acordo com o formato especificado
        String emissaoRelatorio = now.format(formatter);

        // Periodo
        String periodo = "";
        if (emissao != null) {
            periodo = "Período Emissão : " + emissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + emissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        }

        params.put("periodo", periodo);
        params.put("emissaoRelatorio", "Emissão: " + emissaoRelatorio)


        List<TableMap> documentos = buscarPedidos(numeroInicial, numeroFinal, dtEmisIni, dtEmisFin, despacho, redespacho, capaLote);


        for (documento in documentos) {
            BigDecimal caixa = definirQuantidadeDeCaixa(documento);

            documento.put("caixa", caixa);
        }


        if (impressao == 1) return gerarXLSX("CST_PickingList_Pedido_Tombamento(Excel)", documentos);
        return gerarPDF("CST_PickingList_Pedido_Tombamento(PDF)", documentos);
    }


    private List<TableMap> buscarPedidos(Integer numeroInicial, Integer numeroFinal,
                                         LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> despacho, List<Long> redespacho, List<Long> capaLote) {


        String whereNumDoc = numeroInicial != null && numeroFinal != null ? "and  abb01Num BETWEEN :numeroInicial AND :numeroFinal " : "";
        String whereDtEmissao = dtEmisIni != null && dtEmisFin != null ? "AND abb01data BETWEEN :dtEmisIni AND :dtEmisFin " : "";
        String whereDespacho = despacho != null && despacho.size() > 0 ? " and desp.abe01id in (:despacho)   " : "";
        String whereRedespacho = redespacho != null && redespacho.size() > 0 ? " and desp.abe01id in (:redespacho)   " : "";
        String whereCapaLote = capaLote != null && capaLote.size() > 0 ? "and bfb01id in (:capaLote)" : "";

        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroDtEmissaoIni = dtEmisIni != null ? Parametro.criar("dtEmisIni", dtEmisIni) : null;
        Parametro parametroDtEmissaoFin = dtEmisFin != null ? Parametro.criar("dtEmisFin", dtEmisFin) : null;
        Parametro parametroDespacho = despacho != null && despacho.size() > 0 ? Parametro.criar("despacho", despacho) : null;
        Parametro parametroRedespacho = redespacho != null && redespacho.size() > 0 ? Parametro.criar("redespacho", redespacho) : null;
        Parametro parametroCapaLote = capaLote != null && capaLote.size() > 0 ? Parametro.criar("capaLote", capaLote) : null

        String sql = "select distinct eaa01id,abb01num as numPedido,desp.abe01codigo as codDespacho,desp.abe01na AS naDespacho,redesp.abe01codigo AS codRedespacho, redesp.abe01na AS naRedespacho, " +
                "CAST(eaa0103json ->>'volumes' AS numeric(18,6)) AS volumes,cliente.abe01codigo AS codCliente, cliente.abe01na AS nomeCliente, " +
                "eaa01esdata, abe02vidautil AS vida,cast(abe02json ->> 'qtd_max_lote' as numeric(10,0)) as qtdLote,CAST(abm0101json ->>'ordem_separacao' AS numeric(18,2))::int AS ordemSeparacao, " +
                "abm01codigo AS codItem,abm01na AS naItem, bfa01011lote as Lote,bfa01011qtUso as qtd, aam06codigo as umu, aam06codigo as unidadeMedida, " +
                " CAST(abm0101json ->>'cvdnf' AS numeric(18,6)) as cvdnf, " +
                "CAST(abm0101json ->>'volume_caixa' AS numeric(18,6)) as volumeCaixa,CAST(abm0101json ->>'peso_caixa' AS numeric(18,6)) as pesoCaixa,CAST(abm0101json ->>'unidperc' AS numeric(18,6)) as percentualSep, bfb01lote as capaLote, " +
                "CAST(eaa0103json ->>'peso_bruto' AS numeric(18,6)) as pesoBruto " +
                "from bfa01 " +
                "inner join bfa0101 on bfa0101rom = bfa01id " +
                "inner join bfa01011 on bfa01011item = bfa0101id " +
                "inner join abb01 on abb01id = bfa01docscv " +
                "inner join bfb0101 on bfb0101central = abb01id " +
                "inner join bfb01 on bfb01id = bfb0101lote " +
                "inner join eaa01 on eaa01central = abb01id " +
                "inner join eaa0102 on eaa0102doc = eaa01id " +
                "inner join abe01 as desp on desp.abe01id = eaa0102despacho " +
                "left join abe01 as redesp on redesp.abe01id = eaa0102redespacho " +
                "inner join eaa0103 on eaa0103id = bfa0101item " +
                "inner join abe01 as cliente on cliente.abe01id = abb01ent " +
                "inner join abe02 on abe02ent = cliente.abe01id " +
                "inner join abm01 on abm01id = eaa0103item " +
                "inner join abm0101 on abm0101item = abm01id " +
                "inner join aam06 on aam06id = abm01umu " +
                "where true " +
                whereNumDoc +
                whereDtEmissao +
                whereDespacho +
                whereRedespacho +
                whereCapaLote +
                "ORDER BY OrdemSeparacao, abm01codigo";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDespacho, parametroRedespacho, parametroCapaLote )


    }


    private void validarLotes(List<Long> capaLote) {

        String sql = "select distinct bfb01lote, bfb0101romProc, bfb0101id " +
                "from bfb01 " +
                "left join bfb0101 on bfb0101lote = bfb01id " +
                "where bfb01id in (:capaLote)";

        List<TableMap> tmLotes = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("capaLote", capaLote));

        for (lote in tmLotes) {
            String nomeLote = lote.getString("bfb01lote");

            if (lote.getInteger("bfb0101romProc") == 0) interromper("O lote " + nomeLote + " ainda não foi Processado");
        }
    }

    private BigDecimal definirQuantidadeDeCaixa(TableMap documento) {
        String umu = documento.getString("umu");
        BigDecimal quantidade = documento.getBigDecimal_Zero("qtd")
        BigDecimal caixa;

        if (umu.toUpperCase() == 'KG') {
            caixa = quantidade / documento.getBigDecimal_Zero("pesoCaixa")
            caixa = new BigDecimal(caixa).setScale(0, BigDecimal.ROUND_DOWN)
        } else {
            caixa = quantidade / documento.getBigDecimal_Zero("cvdnf").round(2)

        }

        return caixa
    }


    private List<Long> preencherLotesProcessados() {
        String sql = "select distinct bfb01id from bfb01 " +
                "left join bfb0101 on bfb0101lote = bfb01id " +
                "where bfb0101romproc = 1";

        return getAcessoAoBanco().obterListaDeLong(sql);
    }


}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmcgTGlzdCBQZWRpZG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmcgTGlzdCBQZWRpZG8gKFRvbWJhbWVudG8pIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmcgTGlzdCBQZWRpZG8gKFRvbWJhbWVudG8pIiwidGlwbyI6InJlbGF0b3JpbyJ9