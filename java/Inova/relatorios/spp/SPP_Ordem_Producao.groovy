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
        List<Long> ordens = getListLong("ordens");

        List<TableMap> dados = buscarOrdensProducao(idsTiposDoc, ordens);
        List<TableMap> listComponentes = new ArrayList<>();
        List<TableMap> listRemove = new ArrayList<>();

        for(dado in dados){
            Long idSemiAcabado = dado.getLong("idSemiAcabado");
            BigDecimal qtd = dado.getBigDecimal_Zero("bab01qt");

            List<TableMap> componentes = buscarComponentesProdutos(idSemiAcabado);

            if(componentes == null || componentes.size() == 0){
                listRemove.add(dado);
              continue;
            }

            for(componente in componentes){
                componente.put("key", idSemiAcabado);
                componente.put("qtdComponente", componente.getBigDecimal_Zero("qtdComponente") * qtd);
                listComponentes.add(componente);
            }

            dado.put("key", idSemiAcabado);
        }

        dados.removeAll(listRemove)

        // Cria os sub-relatórios
        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("dsComponentes", listComponentes, "key", "key");
        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SPP_Ordem_Producao_S1"));

        return gerarPDF("SPP_Ordem_Producao", dsPrincipal);
    }
    private List<TableMap> buscarOrdensProducao(List<Long> idsTiposDoc,  List<Long> ordens){
        String whereTiposDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? "AND abb01tipo IN (:idsTiposDoc) " : "";
        String whereOrdens = ordens != null && ordens.size() > 0 ? "AND bab01id IN (:ordens) " : "";

        Parametro parametroTipoDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? Parametro.criar("idsTiposDoc", idsTiposDoc) : null;
        Parametro parametroOrdens = ordens != null && ordens.size() > 0 ? Parametro.criar("ordens", ordens) : null;



        String sql = "SELECT abm01compProduto.abm01id AS idItem, abb01num, acabado.abm01tipo AS tipoAcab, acabado.abm01codigo AS codAcab, acabado.abm01descr AS descrAcab, " +
                    "abm01compProduto.abm01codigo AS codCompProduto, abm01compProduto.abm01descr AS descrCompProduto, abm01compProduto.abm01id AS idSemiAcabado, " +
                    "abb01num, abb01data, bab01qt, bab01dtE, bab01obs, bab01ctDtI  " +
                    "FROM bab01 " +
                    "INNER JOIN abb01 ON abb01id = bab01central " +
                    "INNER JOIN abp20 ON abp20id = bab01comp " +
                    "INNER JOIN abp2001 ON abp2001comp = abp20id " +
                    "INNER JOIN abp20011 ON abp20011proc = abp2001id " +
                    "INNER JOIN abm01 AS acabado ON acabado.abm01id = abp20item " +
                    "INNER JOIN abm01 AS abm01compProduto ON abm01compProduto.abm01id = abp20011item " +
                    whereTiposDoc +
                    whereOrdens +
                    "AND abm01compProduto.abm01tipo = 1 " +
                    "ORDER BY abb01num, abm01compProduto.abm01codigo"


        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipoDoc, parametroOrdens);
    }
    private List<TableMap> buscarComponentesProdutos(Long idItem){
        String sql = "SELECT abm01tipo AS tipoComponente, abm01codigo AS codComponente, " +
                    "abm01descr AS descrComponente, aam06codigo AS umuComponentes, " +
                    "abp20011qt AS qtdComponente, abp20011seq AS seqComponentes " +
                    "FROM abp20 "+
                    "INNER JOIN abp2001 ON abp2001comp = abp20id " +
                    "INNER JOIN abp20011 ON abp20011proc = abp2001id " +
                    "INNER JOIN abm01 ON abp20011item = abm01id " +
                    "LEFT JOIN aam06 ON aam06id = abm01umu " +
                    "WHERE abp20item = :idItem "+
                    "AND abp20di IS NULL "+
                    "ORDER BY  abp20011seq"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idItem", idItem))
    }
}