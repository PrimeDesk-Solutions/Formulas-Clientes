package Atilatte.relatorios.spp;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SPP_Programacao_Fabrica extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Programação Fábrica";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsOrdens = getListLong("ordens");
        List<Long> idsItens = getListLong("itens");
        LocalDate[] datas = getIntervaloDatas("data");

        List<TableMap> dadosProdutosFinais = buscarDadosProdutosFinais(idsOrdens, idsItens, datas);
        List<TableMap> dadosComponentes = buscarDadosComponentes(idsOrdens, idsItens, datas);
        List<TableMap> dadosRelatorio = new ArrayList<>();

       for(produtoFinal in dadosProdutosFinais){
           produtoFinal.put("key", "1");

           for(componente in dadosComponentes){
               componente.put("key", "1");
           }
           dadosRelatorio.add(produtoFinal);
       }

        adicionarParametro("DATA_PROGRAMACAO", datas[0].format("dd/MM/yyyy"));

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dadosRelatorio);
        dsPrincipal.addSubDataSource("dsComponentes", dadosComponentes, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPP_Programacao_Fabrica_S1"));

        return gerarPDF("SPP_Programacao_Fabrica", dsPrincipal);
    }

    private List<TableMap> buscarDadosProdutosFinais(List<Long> idsOrdens, List<Long> idsItens, LocalDate[] datas) {
        String whereOrdens = idsOrdens != null && idsOrdens.size() > 0 ? "AND baa01id IN (:idsOrdens) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereDatas = datas != null ? "AND abb01data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereDescrPlano = "AND baa01descr ILIKE '%env%' ";
        String whereFatorItem = "AND CAST(abm0101json ->> 'cvdnf' AS INT) > 0 ";
        String whereStatusPlano = "AND baa01status IN (0,1) ";

        String sql = "SELECT abm01reduzido, abm01codigo, abm01descr, bab01lote, " +
                "SUM(ROUND(baa0101ap / CAST(abm0101json ->> 'cvdnf' AS INT),2)) AS qtdCaixa, " +
                "'PRODUTO_FINAL' AS categoria " +
                "FROM bab01 " +
                "INNER JOIN bab0103 ON bab0103op = bab01id " +
                "INNER JOIN baa0101 ON baa0101id = bab0103itempp " +
                "INNER JOIN baa01 ON baa01id = baa0101plano " +
                "INNER JOIN abp20 ON abp20id = baa0101comp " +
                "INNER JOIN abm01 ON abm01id = abp20item " +
                "INNER JOIN abm0101 ON abm0101item = abm01id " +
                "INNER JOIN abb01 ON abb01id = baa01central " +
                "WHERE TRUE " +
                whereOrdens +
                whereItens +
                whereDatas +
                whereDescrPlano +
                whereFatorItem +
                whereStatusPlano +
                "GROUP BY abm01reduzido, abm01codigo, abm01descr, bab01lote " +
                "ORDER BY abm01codigo"

        Parametro parametroOrdens = idsOrdens != null && idsOrdens.size() > 0 ? Parametro.criar("idsOrdens", idsOrdens) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroDataInicial = datas != null ? Parametro.criar("dtInicial", datas[0]) : null;
        Parametro parametroDataFinal = datas != null ? Parametro.criar("dtFinal", datas[1]) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroOrdens, parametroItens, parametroDataInicial, parametroDataFinal);
    }

    private List<TableMap> buscarDadosComponentes(List<Long> idsOrdens, List<Long> idsItens, LocalDate[] datas) {
        String whereOrdens = idsOrdens != null && idsOrdens.size() > 0 ? "AND baa01id IN (:idsOrdens) " : "";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abp20item IN (:idsItens) " : "";
        String whereDatas = datas != null ? "AND abb01data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereDescrPlano = "AND baa01descr ILIKE '%env%' ";
        String whereStatusOrdem = "AND bab01status <> 2 ";
        String whereTipoItem = "AND abm01tipo = 1 ";

        String sql = "SELECT abm01codigo, abm01descr, 'COMPONENTES' AS categoria,  SUM(bab0101qtA) AS qtd " +
                "FROM bab01 " +
                "INNER JOIN bab0101 ON bab01id = bab0101op " +
                "INNER JOIN abm01 ON abm01id = bab0101item " +
                "INNER JOIN abb01 ON abb01id = bab01central " +
                "INNER JOIN bab0103 ON bab0103op = bab01id " +
                "INNER JOIN baa0101 ON baa0101id = bab0103itempp " +
                "INNER JOIN abp20 ON abp20id = baa0101comp " +
                "INNER JOIN baa01 ON baa01id = baa0101plano " +
                "WHERE TRUE " +
                whereOrdens +
                whereItens +
                whereDatas +
                whereDescrPlano +
                whereStatusOrdem +
                whereTipoItem +
                "GROUP BY abm01codigo, abm01descr, categoria " +
                "ORDER BY abm01codigo"

        Parametro parametroOrdens = idsOrdens != null && idsOrdens.size() > 0 ? Parametro.criar("idsOrdens", idsOrdens) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroDataInicial = datas != null ? Parametro.criar("dtInicial", datas[0]) : null;
        Parametro parametroDataFinal = datas != null ? Parametro.criar("dtFinal", datas[1]) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroOrdens, parametroItens, parametroDataInicial, parametroDataFinal)
    }
}
