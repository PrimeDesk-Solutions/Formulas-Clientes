package During.relatorios.scv
import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.lang.reflect.Parameter
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_ImpressaoPedidosDeVenda extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Impressão Pedido De Venda";
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
        LocalDate dtEmissao = getIntervaloDatas("dtEmissao");
        List<Long> entidades = getListLong("entidades");
        List<Long> idsTipo = getListLong("idsTipo");

        List<TableMap> dados = buscarDadosRealatorio(numInicial,numFinal, dtEmissao,entidades, idsTipo);
        List<TableMap> listItens = new ArrayList();

        for(dado in dados){
            Long idDoc = dado.getLong("idDoc");
            dado.put("key", idDoc);

            List<TableMap> itens = buscarItensDoc(idDoc);

            for(item in itens){
                item.put("key", idDoc);
                listItens.add(item);
                dado.put("entrega", item.getString("entrega"))
            }

        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");

        adicionarParametro("SUBREPORT_DIR1", carregarArquivoRelatorio("SCV_ImpressaoPedidosDeVenda_s1"))

        return gerarPDF("SCV_ImpressaoPedidosDeVenda", dsPrincipal)
    }

    private List<TableMap> buscarDadosRealatorio(Integer numInicial,Integer numFinal, LocalDate[] dtEmissao,List<Long> entidades, List<Long> idsTipo){
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;
        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        String whereNumDoc = numInicial != null && numFinal != null ? "and abb01num between :numInicial and :numFinal " : numInicial != null && numFinal == null ? "and abb01num >= :numInicial " : numInicial == null && numFinal != null ? "and abb01num <= :numFinal " : "";
        String whereDtEmissao = dtEmissaoIni != null && dtEmissaoFin != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereEntidade = entidades != null && entidades.size() > 0 ? "and ent.abe01id in (:entidades) " : "";
        String whereTipoDoc = idsTipo != null && idsTipo.size() > 0 ? "and aah01id in (:idsTipo) " : "";

        Parametro parametroNumDocIni = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroDtIni = dtEmissaoIni != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null
        Parametro parametroDtFin = dtEmissaoFin != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroTipoDoc = idsTipo != null && idsTipo.size() > 0 ? Parametro.criar("idsTipo", idsTipo) : null;

        String sql = "select eaa01id as idDoc, aac10endereco as enderecoEmpresa, aac10numero as numeroEmpresa, aac10cep as cepEmpresa, aag0201empresa.aag0201nome as cidadeEmpresa, aag02empresa.aag02uf as ufEmpresa, " +
                "aac10dddfone as ddEmpresa, aac10fone as foneEmpresa, aac10email as emailEmpresa, cast(aac10json ->> 'site' as text) as siteEmpresa, eaa01obsgerais, abe01nome as nomeEnt, " +
                "abe0101endereco as enderecoEnt,aag0201entidade.aag0201nome as cidadeEnt, aag02entidade.aag02uf as ufEntidade, abe0101numero, abe0101ddd1 as ddd1Ent, eaa0102numero as numero, abe0101fone1 as fone1Ent, eaa01obsusoint, " +
                "abb01num as numDoc, userNota.aab10nome as user, abb01data as dtEmissao, aac10rs as nomeEmpresa, aac10bairro as bairroEmpresa, " +
                "cast(eaa01json ->> 'mod_frete' as text ) modoFrete, abe30nome as condicaoPgto, cast(eaa01json ->> 'cotacao_moeda' as text) as cotacaoMoeda, " +
                "coalesce(cast(eaa01json ->> 'tot_item_s_imp' as numeric(18,2)),0.00) as totSemImpostoDoc2, "+
                "coalesce(cast(eaa01json ->> 'tot_item_c_imp' as numeric(18,2)),0.00) as totComImpostoDoc2, "+
                "coalesce(cast(eaa01json ->> 'frete' as numeric(18,2)),0.00) as frete, aprov.aab10nome as aprovador,abb0103data as dtAprov, eaa0102pcnome as contato, eaa0102pcddd as ddContato, eaa0102pcfone as foneContato,  "+
                "eaa0102pcemail as emailContato, "+
                "eaa0102marca as marca "+
                "from eaa01 " +
                "inner join aac10 on aac10id = eaa01gc " +
                "inner join aag0201 as aag0201empresa on aag0201empresa.aag0201id = aac10municipio " +
                "inner join aag02 as aag02empresa on aag02empresa.aag02id = aag0201empresa.aag0201uf " +
                "inner join abb01 on abb01id = eaa01central " +
                "inner join aah01 on aah01id = abb01tipo " +
                "inner join abe01 as ent on ent.abe01id = abb01ent " +
                "inner join abe0101 on abe0101ent = ent.abe01id and abe0101principal = 1 " +
                "left join aag0201 as aag0201entidade on aag0201entidade.aag0201id = abe0101municipio " +
                "left join aag02 as aag02entidade on aag02entidade.aag02id = aag0201entidade.aag0201uf " +
                "inner join eaa0102 on eaa0102doc = eaa01id " +
                "left join abb0103 on abb0103central = abb01id "+
                "left join aab10 as aprov on aprov.aab10id = abb0103user "+
                "left join aab10 as userNota on userNota.aab10id = abb01operuser " +
                "left join abe30 on abe30id = eaa01cp " +
                "where eaa01clasdoc = 0 " +
                whereNumDoc +
                whereDtEmissao +
                whereEntidade +
                whereTipoDoc +
                "order by abb01num "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni,parametroNumDocFin,parametroDtIni,parametroDtFin,parametroEntidade, parametroTipoDoc)
    }

    private List<TableMap> buscarItensDoc(Long idDoc){
        String sql = "SELECT abm01id, eaa0103seq as seq, aam06codigo as unid,abm01arqFig,abm01codigo as codItem, eaa0103qtComl as qtd, abm01na as naItem, eaa0103descr as descrItem, abm01obs as obsItem,  " +
                "abg01codigo as ncm, coalesce(cast(eaa0103json ->> 'aliq_pis' as numeric(18,6)),0.00) as aliqPis, coalesce(cast(eaa0103json ->> 'aliq_cofins' as numeric(18,6)),0.00) as aliqCofins,   " +
                "coalesce(cast(eaa0103json ->> 'aliq_icms' as numeric(18,6)),0.00) as aliqIcms, coalesce(cast(eaa0103json ->> 'aliq_iss' as numeric(18,6)),0.00) as aliqIss, case when coalesce(cast(eaa0103json ->> '_red_bc_icms' as numeric(18,6)),0.00) = 0.00 then 'ISENTO' else cast(eaa0103json ->> '_red_bc_icms' as numeric(18,2))::text end as aliqRedBcIcms,  " +
                "case when coalesce(cast(eaa0103json ->> 'aliq_ipi' as numeric(18,6)),0.00) = 0.00 then 'ISENTO' else cast(eaa0103json ->> 'aliq_ipi' as numeric(18,2))::text end as aliqIpi, case when coalesce(cast(eaa0103json ->> 'aliq_icms_st' as numeric(18,6)),0.00) = 0.00 then 'ISENTO' else cast(eaa0103json ->> 'aliq_icms_st' as numeric(18,2))::text end as aliqIcmsSt, "+
                "coalesce(cast(eaa0103json ->> 'aliq_ir' as numeric(18,6)),0.00) as aliqIr, coalesce(cast(eaa0103json ->> 'aliq_csll' as numeric(18,6)),0.00) as aliqCsll, " +
                "coalesce(cast(eaa0103json ->> 'unit_s_imp' as numeric(18,6)),0.00) as unitSemImpostoItem, coalesce(cast(eaa0103json ->> 'tot_item_s_imp' as numeric(18,6)),0.00) as totSemImpostoItem, "+
                "eaa0103unit as unitario, eaa0103total as totalItem, (eaa0103dtentrega - abb01data )::text as entrega "+
                "from eaa0103  " +
                "inner join abm01 on abm01id = eaa0103item  " +
                "inner join abm0101 on abm0101item = abm01id  " +
                "left join abg01 on abg01id = abm0101ncm  " +
                "left join aam06 on aam06id = abm01umu "+
                "inner join eaa01 on eaa01id = eaa0103doc "+
                "inner join abb01 on abb01id = eaa01central "+
                "where eaa0103doc = :idDoc "+
                "order by eaa0103seq"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc))
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIERlIFZlbmRhIiwidGlwbyI6InJlbGF0b3JpbyJ9