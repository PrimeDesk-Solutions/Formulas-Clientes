package Inova.relatorios.spp

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro;

import java.util.Map;
import java.util.HashMap;

public class SPP_Ordem_Producao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Ordem Produção";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsTiposDoc = getListLong("tipos");
        List<Long> planos = getListLong("planos");

        List<TableMap> dados = buscarOrdensProducao(idsTiposDoc, planos);
        List<TableMap> listComponentes = new ArrayList<>();

        for(dado in dados){
            Long idOrdem = dado.getLong("idOrdem");
            BigDecimal qtd = dado.getBigDecimal_Zero("baa0101ap");

            List<TableMap> componentes = buscarComponentesOrdem(idOrdem);

            for(componente in componentes){
                componente.put("key", idOrdem);
                listComponentes.add(componente);
            }

            dado.put("key", idOrdem);
        }


        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsComponentes", listComponentes, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPP_Ordem_Producao_S1"));

        return gerarPDF("SPP_Ordem_Producao", dsPrincipal);
    }
    private List<TableMap> buscarOrdensProducao(List<Long> idsTiposDoc,  List<Long> planos){
        String whereTiposDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? "AND abb01tipo IN (:idsTiposDoc) " : "";
        String wherePlanos = planos != null && planos.size() > 0 ? "AND baa01id IN (:planos) " : "";

        Parametro parametroTipoDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? Parametro.criar("idsTiposDoc", idsTiposDoc) : null;
        Parametro parametroPlanos = planos != null && planos.size() > 0 ? Parametro.criar("planos", planos) : null;

        String sql = "SELECT baa01descr,abb01plano.abb01num AS numPlano, abb01plano.abb01data AS dataPlano, abb01ordem.abb01num AS numOrdem, bab01id AS idOrdem, " +
                    "acabado.abm01codigo AS codAcab, acabado.abm01descr AS descrAcab, abb01ordem.abb01data, " +
                    "baa0101ap, baa0101opDte, baa01obs, baa0101ctDtI, abm01principal.abm01codigo AS codItemPrincipal, abm01principal.abm01descr AS descrItemPrincipal, " +
                    "abp10codigo, abp10descr, bab01opp "+
                    "FROM bab01 " +
                    "INNER JOIN abb01 AS abb01ordem ON abb01ordem.abb01id = bab01central " +
                    "INNER JOIN abp10 ON abp10id = bab01proc "+
                    "INNER JOIN abp20 AS abp20acabado ON abp20acabado.abp20id = bab01comp  " +
                    "INNER JOIN bab0103 on bab0103op = bab01id " +
                    "INNER JOIN baa0101 on baa0101id = bab0103itempp " +
                    "INNER JOIN baa01 on baa01id = baa0101plano " +
                    "INNER JOIN abm01 AS acabado ON acabado.abm01id = abp20acabado.abp20item " +
                    "INNER JOIN abp20 AS abp20principal ON abp20principal.abp20id = baa0101comp " +
                    "INNER JOIN abm01 AS abm01principal ON abm01principal.abm01id = abp20principal.abp20item " +
                    "INNER JOIN abb01 AS abb01Plano ON abb01Plano.abb01id = baa01central " +
                    whereTiposDoc +
                    wherePlanos +
                    "ORDER BY abb01plano.abb01num, descrItemPrincipal, acabado.abm01codigo, abp10codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipoDoc, parametroPlanos);
    }
    private List<TableMap> buscarComponentesOrdem(Long idOrdem){
        String sql = "SELECT abm01tipo AS tipoComponente, abm01codigo AS codComponente, " +
                    "abm01descr AS descrComponente, aam06codigo AS umuComponentes, " +
                    "bab0101qta AS qtdComponente, bab0101seq AS seqComponentes " +
                    "FROM bab01 "+
                    "INNER JOIN bab0101 ON bab0101op = bab01id " +
                    "INNER JOIN abm01 ON bab0101item = abm01id " +
                    "LEFT JOIN aam06 ON aam06id = abm01umu " +
                    "WHERE bab01id = :idOrdem "+
                    "ORDER BY bab0101seq "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idOrdem", idOrdem))
    }
}
//meta-sis-eyJkZXNjciI6IlNQUCAtIE9yZGVtIFByb2R1w6fDo28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=