package Atilatte.relatorios.scv

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;
import java.time.LocalDate


import java.util.Map;
import java.util.HashMap;

public class SCV_Pedidos_MarketPlace extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pedidos MarketPlace";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        Integer numInicial = getInteger("numeroInicial");
        Integer numFinal = getInteger("numeroFinal");
        List<Long> tiposDoc = getListLong("tipos");
        List<Long> entidades = getListLong("entidades");
        LocalDate[] dataEmissao = getIntervaloDatas("emissao");

        List<TableMap> dados = buscarDadosRelatorio(numInicial, numFinal, tiposDoc, entidades, dataEmissao);

        params.put("TITULO", "SCV - Pedidos MarketPlace");
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10rs())

        return gerarPDF("SCV_Pedidos_MarketPlace", dados);
    }

    private List<TableMap> buscarDadosRelatorio(Integer numInicial, Integer numFinal, List<Long> tiposDoc, List<Long> entidades, LocalDate[] dataEmissao) {
        String whereNumero = numInicial != null && numFinal != null ? " WHERE abb01pedido.abb01num BETWEEN " + numInicial + " AND " + numFinal + " " : "";
        String whereDatas = dataEmissao != null ? " AND abb01pedido.abb01data BETWEEN '" + dataEmissao[0] + "' AND '" + dataEmissao[1] + "' " : "";
        String whereTipos = tiposDoc != null && tiposDoc.size() > 0 ? "AND abb01pedido.abb01tipo IN (:tiposDoc) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abe01nota.abe01id IN (:entidades) " : "";
        String whereEmpresa = "AND eaa01pedido.eaa01eg = :empresa ";

        Parametro parametroTipos = tiposDoc != null && tiposDoc.size() > 0 ? Parametro.criar("tiposDoc", tiposDoc) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroEmpresa = Parametro.criar("empresa", obterEmpresaAtiva().aac10id);

        String sql = "SELECT DISTINCT eaa01pedido.eaa01esMov, abb01pedido.abb01num AS numPedido, abb01pedido.abb01data AS dataPedido, " +
                "eaa01pedido.eaa01totDoc AS totalPedido, abe01pedido.abe01codigo AS codEntidadePedido, abe01pedido.abe01nome AS nomeEntidadePedido, " +
                "abb01nota.abb01num AS numNota, abb01nota.abb01data AS dataNota, " +
                "eaa01nota.eaa01totDoc AS totalNota, abe01nota.abe01codigo AS codEntidadeNota, abe01nota.abe01nome AS nomeEntidadeNota " +
                "FROM eaa01 AS eaa01pedido " +
                "INNER JOIN abb01 AS abb01pedido ON abb01pedido.abb01id = eaa01pedido.eaa01central " +
                "INNER JOIN eaa0103 AS eaa0103pedido ON eaa0103pedido.eaa0103doc = eaa01pedido.eaa01id " +
                "INNER JOIN eaa01032 ON eaa01032itemScv = eaa0103pedido.eaa0103id " +
                "INNER JOIN eaa0103 AS eaa0103nota ON eaa0103nota.eaa0103id = eaa01032itemSRF " +
                "INNER JOIN eaa01 AS eaa01nota ON eaa01nota.eaa01id = eaa0103nota.eaa0103doc " +
                "INNER JOIN abb01 AS abb01nota ON abb01nota.abb01id = eaa01nota.eaa01central " +
                "INNER JOIN abe01 AS abe01pedido ON abe01pedido.abe01id = abb01pedido.abb01ent " +
                "INNER JOIN abe01 AS abe01nota ON abe01nota.abe01id = abb01nota.abb01ent " +
                whereNumero +
                whereDatas +
                whereTipos +
                whereEntidades +
                whereEmpresa +
                "ORDER BY abb01pedido.abb01num, abb01nota.abb01num "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipos, parametroEntidades, parametroEmpresa)
    }
}