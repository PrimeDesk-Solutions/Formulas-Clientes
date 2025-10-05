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
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter;


public class CST_PickingListLoteNF extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "CST - PickingList Notas Fiscais";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("pedEntSai", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> tipos = getListLong("tipos");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> despacho = getListLong("despacho");
        List<Long> redespacho = getListLong("redespacho");
        LocalDate[] emissao = getIntervaloDatas("emissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("entradaSaida");
        Integer pedEntSai = getInteger("pedEntSai");

        // Data Emissão
        LocalDate dtEmisIni = null;
        LocalDate dtEmisFin = null;
        if (emissao != null) {
            dtEmisIni = emissao[0];
            dtEmisFin = emissao[1];
        }

        // Data Entrada/Saída
        LocalDate dtEntradaSaidaIni = null;
        LocalDate dtEntradaSaidaFin = null;
        if (dtEntradaSaida != null) {
            dtEntradaSaidaIni = dtEntradaSaida[0];
            dtEntradaSaidaFin = dtEntradaSaida[1];
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

        List<TableMap> documentos = buscarPedidos(tipos, numeroInicial, numeroFinal, dtEmisIni, dtEmisFin, despacho, redespacho, pedEntSai, dtEntradaSaidaIni, dtEntradaSaidaFin);

        for (documento in documentos) {
            if (documento.getString("pcd") == "60009") {
                documento.put("volumes", documento.getBigDecimal("eaa01038qt"));
            }
        }

        return gerarPDF("CST_PickingList_NF", documentos);
    }

    private List<TableMap> buscarPedidos(List<Long> tipos, Integer numeroInicial, Integer numeroFinal,
                                         LocalDate dtEmisIni, LocalDate dtEmisFin, List<Long> despacho, List<Long> redespacho, Integer pedEntSai, LocalDate dtEntradaSaidaIni, LocalDate dtEntradaSaidaFin) {

        String whereEsMov = " WHERE eaa01nota.eaa01esMov = :esMov ";
        String whereClasDoc = " AND eaa01nota.eaa01clasDoc = 1 ";
        String whereDtEmissao = dtEmisIni != null && dtEmisFin != null ? " AND abb01nota.abb01data BETWEEN :dtEmisIni AND :dtEmisFin " : "";
        String whereEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? " AND eaa01nota.eaa01esData BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereNumDoc = numeroInicial != null && numeroFinal != null ? " AND abb01nota.abb01Num BETWEEN :numeroInicial AND :numeroFinal " : "";
        String whereTipos = tipos != null && tipos.size() > 0 ? " AND abb01nota.abb01tipo IN (:tipos) " : "";
        String whereDespacho = despacho != null && despacho.size() > 0 ? " and desp.abe01id in (:despacho)   " : "";
        String whereRedespacho = redespacho != null && redespacho.size() > 0 ? " and redesp.abe01id in (:redespacho)   " : "";

        Parametro parametroEsMov = Parametro.criar("esMov", pedEntSai);
        Parametro parametroDtEmissIni = dtEmisIni != null ? Parametro.criar("dtEmisIni", dtEmisIni) : null;
        Parametro parametroDtEmissFin = dtEmisFin != null ? Parametro.criar("dtEmisFin", dtEmisFin) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaidaIni != null ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaidaIni) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaidaFin != null ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaidaFin) : null;
        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroTipoDoc = tipos != null && tipos.size() > 0 ? Parametro.criar("tipos", tipos) : null;
        Parametro parametroDespacho = despacho != null && despacho.size() > 0 ? Parametro.criar("despacho", despacho) : null;
        Parametro parametroRedespacho = redespacho != null && redespacho.size() > 0 ? Parametro.criar("redespacho", redespacho) : null;


        String sql = "SELECT DISTINCT eaa01nota.eaa01esdata as dtSaida, eaa01038qt, abb01nota.abb01num AS numDoc,cliente.abe01codigo AS codCliente, cliente.abe01na AS nomeCliente, " +
                "abb01nota.abb01data AS dtemissao,abd01codigo as pcd, " +
                "desp.abe01codigo AS codDespacho, desp.abe01na AS naDespacho, redesp.abe01codigo AS codRedesp, redesp.abe01na AS naRedesp, " +
                "abb01pedido.abb01num as numPed, eaa01nota.eaa01id as idNota, eaa01038lote as Lote, abm01codigo as codItem, abm01na as naItem,aam06codigo as umu, SUM(eaa01038qt) as qtd, case when aam06codigo = 'KG' then round((eaa01038qt / cast(abm0101json ->> 'peso_caixa' as numeric(18,6)))) else round((eaa01038qt / cast(abm0101json ->> 'volume_caixa' as numeric(18,6)))) end as volumes, " +
                "cast(abm0101json ->> 'ordem_separacao' as numeric(18,6)) as ordemSep, aam04codigo as status, abm15nome as local  " +
                "FROM eaa01 as eaa01nota " +
                "INNER JOIN abb01 as abb01nota ON abb01nota.abb01id = eaa01nota.eaa01central " +
                "INNER JOIN abe01 AS cliente ON abe01id = abb01nota.abb01ent " +
                "INNER JOIN eaa0103 as eaa0103itemNota ON eaa0103itemNota.eaa0103doc = eaa01nota.eaa01id " +
                "INNER JOIN abd01 ON abd01id = eaa01nota.eaa01pcd " +
                "INNER JOIN abm01 ON abm01id = eaa0103itemNota.eaa0103item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "INNER JOIN eaa0102 ON eaa01nota.eaa01id = eaa0102doc " +
                "INNER JOIN abe01 AS desp ON desp.abe01id = eaa0102despacho " +
                "LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho " +
                "INNER JOIN eaa01032 on eaa01032itemsrf = eaa0103itemNota.eaa0103id " +
                "INNER JOIN eaa0103 as eaa0103itemPedido on eaa0103itemPedido.eaa0103id = eaa01032itemscv " +
                "INNER JOIN eaa01 as eaa01pedido on eaa01pedido.eaa01id = eaa0103itemPedido.eaa0103doc " +
                "INNER JOIN abb01 as abb01pedido on abb01pedido.abb01id = eaa01pedido.eaa01central " +
                "INNER JOIN eaa01038 on eaa01038item = eaa0103itemNota.eaa0103id " +
                "inner JOIN aam06 ON eaa0103itemNota.eaa0103umu = aam06id " +
                "LEFT JOIN aam04 ON aam04id = eaa01038status "+
                "LEFT JOIN abm15 ON abm15id = eaa01038ctrl0 "+
                whereEsMov +
                whereClasDoc +
                whereDtEmissao +
                whereEntradaSaida +
                whereNumDoc +
                whereTipos +
                whereDespacho +
                whereRedespacho +
                "GROUP BY abb01nota.abb01num,cliente.abe01codigo, cliente.abe01na, abb01nota.abb01data, desp.abe01codigo, desp.abe01na, redesp.abe01codigo, redesp.abe01na, abb01pedido.abb01num, eaa01nota.eaa01id,eaa01038lote, " +
                "abm01codigo, abm01na,aam06codigo,eaa01038qt,volumes,ordemSep,pcd,eaa01038qt, aam04codigo, abm15nome " +
                "order by abb01nota.abb01num,ordemSep  " ;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEsMov, parametroDtEmissIni, parametroDtEmissFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroNumDocIni, parametroNumDocFin, parametroTipoDoc, parametroDespacho, parametroRedespacho)
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmdMaXN0IE5vdGFzIEZpc2NhaXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IkNTVCAtIFBpY2tpbmdMaXN0IE5vdGFzIEZpc2NhaXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=