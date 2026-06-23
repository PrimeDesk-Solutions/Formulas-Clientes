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

public class SCV_ImpressaoPedidoDeVendaNacional extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Impressão Pedido Venda Nacional";
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

        List<TableMap> dados = buscarDadosRealatorio(numInicial,numFinal, dtEmissao,entidades);
        List<TableMap> listItens = new ArrayList();

        for(dado in dados){
            Long idDoc = dado.getLong("idDoc");
            dado.put("key", idDoc);

            List<TableMap> itens = buscarItensDoc(idDoc);

            for(item in itens){
                item.put("key", idDoc);
                listItens.add(item);
            }

        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");

        adicionarParametro("SUBREPORT_DIR1", carregarArquivoRelatorio("IDM_PEDIDO_VENDA_NACIONAL_s1"))

        return gerarPDF("IDM_PEDIDO_VENDA_NACIONAL", dsPrincipal)
    }

    private List<TableMap> buscarDadosRealatorio(Integer numInicial,Integer numFinal, LocalDate[] dtEmissao,List<Long> entidades){
        LocalDate dtEmissaoIni = null;
        LocalDate dtEmissaoFin = null;
        if(dtEmissao != null){
            dtEmissaoIni = dtEmissao[0];
            dtEmissaoFin = dtEmissao[1];
        }

        String whereNumDoc = numInicial != null && numFinal != null ? "and abb01num between :numInicial and :numFinal " : numInicial != null && numFinal == null ? "and abb01num >= :numInicial " : numInicial == null && numFinal != null ? "and abb01num <= :numFinal " : "";
        String whereDtEmissao = dtEmissaoIni != null && dtEmissaoFin != null ? "and abb01data between :dtEmissaoIni and :dtEmissaoFin " : "";
        String whereEntidade = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : "";

        Parametro parametroNumDocIni = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumDocFin = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroDtIni = dtEmissaoIni != null ? Parametro.criar("dtEmissaoIni", dtEmissaoIni) : null
        Parametro parametroDtFin = dtEmissaoFin != null ? Parametro.criar("dtEmissaoFin", dtEmissaoFin) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;

        String sql = "select eaa01id as idDoc,abb01num as numDoc, abe01codigo as codEntidade, abe01nome as nomeEntidade, abe0101ddd1 as ddd, abe0101fone1 as foneEntidade, abe01ni as cnpj,\n" +
                "aac10rs as nomeEmpresa, aac10endereco as enderecoEmpresa, aac10numero as numeroEmpresa, aac10bairro as bairroEmpresa,  \n" +
                "aag0201empresa.aag0201nome as municipioEmpresa, aag02empresa.aag02uf as ufEmpresa, aag01empresa.aag01nome as paisEmpresa, aac10cep as cep, aac10ni as cnpjempresa, \n" +
                "abb01operdata as dataOper, abb01operhora as horaOper,eaa01totitens, eaa01totDoc, abe30codigo as codCondicaoPgto, abe30nome as nomeCondicaoPgto, \n" +
                "case when eaa0102frete is null then '0' \n" +
                "else eaa0102frete end as modoFrete, eaa01obscontrib as obsContrib,cast(eaa01json ->> 'frete_dest' as numeric(18,6)) as frete, userNota.aab10user as usuario,eaa0102numero, eaa0102especie, " +
                "aprov.aab10nome as aprovador, abb0103data as dtAprov, aag0201entidade.aag0201nome as cidadeEnt, aag02entidade.aag02uf as ufEntidade, abd01codigo as codPcd, abd01descr as descrPcd, "+
                "case when eaa0102frete = 0 then 'Por conta da During do Brasil' \n" +
                "when eaa0102frete = 1 then 'Por conta da During do Brasil até o entreposto indicado pelo cliente'  \n" +
                "when eaa0102frete = 2 then 'TERCEIROS' \n" +
                "when eaa0102frete = 3 then 'PRÓPRIO POR CONTA DO REMETENTE' \n" +
                "when eaa0102frete = 4 then 'PRÓPRIO POR CONTA DO DESTINATÁRIO' \n" +
                "else 'Por Conta do Cliente' end as modoFrete "+
                "from eaa01\n" +
                "inner join abb01 on abb01id = eaa01central\n" +
                "inner join abd01 on abd01id = eaa01pcd "+
                "left join abb0103 on abb0103central = abb01id "+
                "left join aab10 as aprov on aprov.aab10id = abb0103user "+
                "inner join abe01 on abb01ent = abe01id\n" +
                "left join abe0101 on abe0101ent = abe01id and abe0101principal = 1\n" +
                 "inner join aag0201 as aag0201entidade on aag0201entidade.aag0201id = abe0101municipio " +
                "inner join aag02 as aag02entidade on aag02entidade.aag02id = aag0201entidade.aag0201uf " +
                "left join eaa0102 on eaa0102doc = eaa01id\n" +
                "inner join aac10 on aac10id = eaa01gc\n" +
                "left join aag0201 as aag0201empresa on aag0201empresa.aag0201id = aac10municipio\n" +
                "left join aag02 as aag02empresa on aag02empresa.aag02id = aag0201empresa.aag0201uf\n" +
                "left join aag01 as aag01empresa on aag01empresa.aag01id = aac10pais\n" +
                "inner join aab10 as userNota on userNota.aab10id = abb01operuser\n" +
                "left join abe30 on abe30id = eaa01cp\n" +
                "where eaa01clasdoc = 0\n" +
                "and eaa01esmov = 1 " +
                whereNumDoc +
                whereDtEmissao +
                whereEntidade +
                "order by abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni,parametroNumDocFin,parametroDtIni,parametroDtFin,parametroEntidade)
    }

    private List<TableMap> buscarItensDoc(Long idDoc){
        String sql = "SELECT  eaa0103seq, abm01codigo as codItem, abm01livre, abm01na as naItem, cast(eaa0103json ->> 'ft' as text) as numFt, aam06codigo as unidadeMedida, eaa0103qtComl as qtdItem, "+
                "eaa0103unit as unitItem, eaa0103total as totItem, abg01codigo as codNcm, eaa0103dtentrega as dtEntrega, cast(eaa0103json ->> 'frete_dest' as numeric(18,6)) as frete, cast(eaa0103json ->> 'embalagem' as numeric(18,6)) as embalagem,\n" +
                "cast(eaa0103json ->> 'aliq_ipi' as numeric(18,6)) as aliq_ipi,eaa0103pcNUm as pcNum, "+
                "eaa01totitens, eaa01totDoc " +
                "from eaa0103  " +
                "inner join eaa01 on eaa01id = eaa0103doc "+
                "inner join abm01 on abm01id = eaa0103item  " +
                "left join abg01 on abg01id = eaa0103ncm\n" +
                "left join aam06 on aam06id = abm01umu "+
                "where eaa0103doc = :idDoc "+
                "order by eaa0103seq"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc))
    }


}
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIFZlbmRhIE5hY2lvbmFsIiwidGlwbyI6InJlbGF0b3JpbyJ9