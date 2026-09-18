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

public class SCV_ImpressaoPedidosDeCompra extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Impressão Pedido De Compra";
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
            Long idCentral = dado.getLong("abb01id")
            dado.put("key", idDoc);

            List<TableMap> itens = buscarItensDoc(idDoc);

            for(item in itens){
                item.put("key", idDoc);
                listItens.add(item);
            }
            if(dado.getBigDecimal_Zero("eaa01totdoc") <= 50000){
                buscarUnicoAprovador(dado, idCentral);
            }else{
                buscarAprovadores(dado, idCentral);
            }


        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");

        adicionarParametro("SUBREPORT_DIR1", carregarArquivoRelatorio("IDM_PEDIDO_COMPRA_MAT-PROD_IMPORTAÇÃO_s1"))

        return gerarPDF("IDM_PEDIDO_COMPRA_MAT-PROD_IMPORTAÇÃO", dsPrincipal)
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

        String sql = "select DISTINCT eaa01id as idDoc,eaa01totdoc, abb01id,abb01num as numDoc, abe01codigo as codEntidade, abe01nome as nomeEntidade, abe0101ddd1 as ddd, abe0101fone1 as foneEntidade,\n" +
                    "aac10rs as nomeEmpresa, aac10endereco as enderecoEmpresa, aac10numero as numeroEmpresa, aac10bairro as bairroEmpresa,  \n" +
                    "aag0201empresa.aag0201nome as municipioEmpresa, aag02empresa.aag02uf as ufEmpresa, aag01empresa.aag01nome as paisEmpresa, aac10cep as cep, aac10ni as cnpjempresa, \n" +
                    "abb01operdata as dataOper, abb01operhora as horaOper, abe30codigo as codCondicaoPgto, abe30nome as nomeCondicaoPgto, aag10nome as moeda, \n" +
                    "case when eaa0102frete = 0 then 'REMETENTE (CIF)' \n" +
                    "when eaa0102frete = 1 then 'DESTINATÁRIO (FOB)'  \n" +
                    "when eaa0102frete = 2 then 'TERCEIROS' \n" +
                    "when eaa0102frete = 3 then 'PRÓPRIO POR CONTA DO REMETENTE' \n" +
                    "when eaa0102frete = 4 then 'PRÓPRIO POR CONTA DO DESTINATÁRIO' \n" +
                    "else null end as modoFrete, eaa01obscontrib as obsContrib, userNota.aab10user as usuario, " +
                    "eaa0102numero, eaa0102especie, "+
                    "(SELECT STRING_AGG(AAB10NOME,',') "+
				"FROM ABB01 AS ABB01APROV "+
				"LEFT JOIN ABB0103 ON ABB0103CENTRAL = ABB01APROV.ABB01ID "+
				"LEFT JOIN AAB10 ON AAB10ID = ABB0103USER "+
				"WHERE ABB01APROV.ABB01ID = eaa01.eaa01central) AS APROVADOR, "+
				"(SELECT STRING_AGG(to_char(abb0103data,'dd/mm/yyyy'),',') "+
				"FROM ABB01 AS ABB01APROV "+
				"LEFT JOIN ABB0103 ON ABB0103CENTRAL = ABB01APROV.ABB01ID "+
				"LEFT JOIN AAB10 ON AAB10ID = ABB0103USER "+
				"WHERE ABB01APROV.ABB01ID = eaa01.eaa01central) AS DTAPROV "+
                    "from eaa01\n" +
                    "inner join abb01 on abb01id = eaa01central\n" +
                    "left join abb0103 on abb0103central = abb01id "+
                	"left join aab10 as aprov on aprov.aab10id = abb0103user "+
                    "inner join abe01 on abb01ent = abe01id\n" +
                    "left join abe0101 on abe0101ent = abe01id and abe0101principal = 1\n" +
                    "left join eaa0102 on eaa0102doc = eaa01id\n" +
                    "inner join aac10 on aac10id = eaa01gc\n" +
                    "left join aag0201 as aag0201empresa on aag0201empresa.aag0201id = aac10municipio\n" +
                    "left join aag02 as aag02empresa on aag02empresa.aag02id = aag0201empresa.aag0201uf\n" +
                    "left join aag01 as aag01empresa on aag01empresa.aag01id = aac10pais\n" +
                    "inner join aab10 as userNota on userNota.aab10id = abb01operuser\n" +
                    "left join abe30 on abe30id = eaa01cp\n" +
                    "left join aag10 on aag10id = eaa01moeda "+
                    "where eaa01clasdoc = 0\n" +
                    "and eaa01esmov = 0 " +
                    whereNumDoc +
                    whereDtEmissao +
                    whereEntidade +
                    "order by abb01num"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni,parametroNumDocFin,parametroDtIni,parametroDtFin,parametroEntidade)
    }

    private List<TableMap> buscarItensDoc(Long idDoc){
        String sql = "SELECT  eaa0103seq, abm01codigo as codItem, abm01livre, abm01complem as naItem, cast(eaa0103json ->> 'ft' as text) as numFt, aam06codigo as unidadeMedida, eaa0103qtComl as qtdItem, "+
                "eaa0103unit as unitItem, eaa0103total as totItem, abg01codigo as codNcm, eaa0103dtentrega as dtEntrega, cast(eaa0103json ->> 'frete_dest' as numeric(18,6)) as frete, cast(eaa01json ->> 'embalagem' as numeric(18,6)) as embalagem, aag10nome as moeda,\n" +
                "eaa01totitens, eaa01totDoc " +
                "from eaa0103  " +
                "inner join eaa01 on eaa01id = eaa0103doc "+
                "inner join abm01 on abm01id = eaa0103item  " +
                "left join abg01 on abg01id = eaa0103ncm\n" +
                "left join aam06 on aam06id = abm01umu "+
                "left join aag10 on aag10id = eaa01moeda "+
                "where eaa0103doc = :idDoc "+
                "order by eaa0103seq"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", idDoc))
    }

    private void buscarUnicoAprovador(TableMap dado, Long idCentral){
        String sql = "select distinct aab10nome as aprovador1, abb0103data as dtAprov1 from abb0103\n" +
                "inner join aab10 on aab10id = abb0103user\n" +
                "where abb0103central = :idCentral ";

        TableMap aprovador = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idCentral", idCentral));

        dado.putAll(aprovador)

    }

    private void buscarAprovadores( TableMap dado, Long idCentral){

        String sql = "select distinct aab10nome as aprovador, abb0103data as dtAprov, cast(aab10camposcustom ->> 'grau_aprovador' as integer ) as grau from abb0103\n" +
                "inner join aab10 on aab10id = abb0103user\n" +
                "where abb0103central = :idCentral ";

        List<TableMap> aprovadores = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idCentral", idCentral));

        for(aprovador in aprovadores){
            if(aprovador.getInteger("grau") == 1){
                dado.put("aprovador1", aprovador.getString("aprovador"));
                dado.put("dtAprov1", aprovador.getDate("dtAprov"));
            }else{
                dado.put("aprovador2", aprovador.getString("aprovador"));
                dado.put("dtAprov2", aprovador.getDate("dtAprov"));
            }
        }
    }


}
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIERlIENvbXByYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIERlIENvbXByYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==