package Fast.relatorios.scv

import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_ImpressaoPedidoCompra extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Impressão Pedido Compra";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> tipos = getListLong("tipoDoc");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidades = getListLong("entidades");
        LocalDate[] emissao = getIntervaloDatas("emissao");

        List<TableMap> dados = buscarDadosRelatorio(tipos, numeroInicial, numeroFinal, entidades, emissao);

        List<TableMap>  listItens = new ArrayList();
        List<TableMap>  listParcelas = new ArrayList();

        for(dado in dados){
            Long idDocumento = dado.getLong("eaa01id");
            dado.put("key", idDocumento);

            List<TableMap> itens = buscarItensDocumento(idDocumento);
            List<TableMap> parcelas = buscarParcelasDocumento(idDocumento);

            for(item in itens){
                item.put("key", idDocumento);
                item.put("subtotal", dado.getBigDecimal_Zero("subtotal"));
                item.put("descontoTotal", dado.getBigDecimal_Zero("descontoTotal"));
                item.put("ipiTotal", dado.getBigDecimal_Zero("ipiTotal"));
                item.put("icmsST", dado.getBigDecimal_Zero("icmsST"));
                item.put("totalDocumento", dado.getBigDecimal_Zero("totalDocumento"));

                listItens.add(item)
            }

            Integer numParcela = 0;
            for(parcela in parcelas){
                numParcela++;

                parcela.put("numParcela", numParcela);
                parcela.put("key", idDocumento);

                listParcelas.add(parcela);
            }
        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");
        dsPrincipal.addSubDataSource("DsSub2", listParcelas, "key", "key");


        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCV_ImpressaoPedidoCompra_s1"))
        adicionarParametro("StreamSub2", carregarArquivoRelatorio("SCV_ImpressaoPedidoCompra_s2"))

        return gerarPDF("SCV_ImpressaoPedidoCompra", dsPrincipal);

    }

    private List<TableMap> buscarDadosRelatorio(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades,LocalDate[] emissao){

        String whereTipos = tipos != null && tipos.size() > 0 ? "and abb01tipo in (:tipos) " : "";
        String whereNumDoc = numeroInicial != null && numeroFinal != null ? "and abb01num between :numeroInicial and :numeroFinal " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : "";
        String whereDtEmissao = emissao != null ? "and abb01data between :dtInicial and :dtFinal " : "";
        String whereEmpresa = "and eaa01gc = :idEmpresa "


        Parametro parametroTipos = tipos != null && tipos.size() > 0 ? Parametro.criar("tipos", tipos) : null;
        Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroDtEmissaoIni = emissao != null ? Parametro.criar("dtInicial", emissao[0]) : null;
        Parametro parametroDtEmissaoFin = emissao != null ? Parametro.criar("dtFinal", emissao[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = "select distinct abb01num as numDoc, aac10na as nomeEmpresa, aac10ni as cnpj, aac1002ie as inscricaoEstadual, " +
                        "aac10endereco as enderecoEmpresa, aac10numero as numEmpresa, aac10bairro as bairroEmpresa, aag0201empresa.aag0201nome as municipioEmpresa, " +
                        "aag02empresa.aag02uf as ufEmpresa, aac10cep as cepEmpresa, aac10dddFone as dddEmpresa, aac10fone as foneEmpresa, " +
                        "abe01na as nomeEntidade, abe01ni as cnpjEntidade, abe0101endereco as enderecoEntidade, abe0101numero as numEntidade, " +
                        "abe0101bairro as bairroEntidade, aag0201entidade.aag0201nome as municipioEntidade, aag02entidade.aag02uf as ufEntidade, abe0101cep as cepEntidade, " +
                        "eaa0102pcEmail as nomeContado, abe0101ddd1 as dddEntidade, abe0101fone1 as foneEntidade,eaa01totItens as subtotal, cast(eaa01json ->> 'desconto' as numeric(18,6)) as descontoTotal, cast(eaa01json ->> 'ipi' as numeric(18,6)) as ipiTotal, " +
                        "cast(eaa01json ->> 'icms_st' as numeric(18,6)) as icmsST, eaa01totDoc as totalDocumento,eaa01id, abe30nome as cndPgto, " +
                        "case when eaa0102frete = 0 then 'Remetente (CIF)'  " +
                        "when eaa0102frete = 1 then 'Destinatário (FOB)'  " +
                        "when eaa0102frete = 2 then 'Terceiros'  " +
                        "when eaa0102frete = 3 then 'Próprio por conta do remetente' " +
                        "else 'Próprio por conta do destinatário' end as frete, " +
                        "cast(eaa01json ->> 'frete_dest' as numeric(18,6)) as valorFrete, " +
                        "cast(eaa01json ->> 'seguro' as numeric(18,6)) as valorSeguro, " +
                        "eaa01operDescr as categoria, " +
                        "abb01operdata as dataInclusao, abb01operhora as horaInclusao, " +
                        "eaa0103dtentrega as previsaoEntrega, aab10nome as user, aab10email as contatoUser, to_char(eaa0103dtentrega, 'dd/mm/yyyy') as dtEntregaAux, " +
                        "eaa01obsgerais as obsGerais " +
                        "from eaa01 " +
                        "inner join abb01 on abb01id = eaa01central " +
                        "inner join aac10 on aac10id = eaa01gc " +
                        "inner join aac1002 on aac1002empresa = aac10id " +
                        "inner join aag0201 as aag0201empresa on aag0201empresa.aag0201id = aac10municipio " +
                        "inner join aag02 as aag02empresa on aag02empresa.aag02id = aag0201uf " +
                        "inner join abe01 on abe01id = abb01ent " +
                        "left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 " +
                        "left join aag0201 as aag0201entidade on aag0201entidade.aag0201id = abe0101municipio " +
                        "left join aag02 as aag02entidade on aag02entidade.aag02id = aag0201entidade.aag0201uf " +
                        "left join eaa0102 on eaa0102doc = eaa01id " +
                        "inner join eaa0103 on eaa0103doc = eaa01id " +
                        "inner join aab10 on aab10id = abb01operuser " +
                        "left join abe30 on abe30id = eaa01cp " +
                        "where eaa01esmov = 0 " +
                        "and eaa01clasdoc = 0 "+
                        whereNumDoc+
                        whereTipos+
                        whereEntidades+
                        whereDtEmissao+
                        whereEmpresa

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroTipos, parametroNumIni, parametroNumFin, parametroEntidades, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroEmpresa )

    }

    private List<TableMap> buscarItensDocumento(Long idDocumento){

        String sql = "select eaa0103seq as seqItem, abm01descr as descrItem, abg01codigo as codNCM, eaa0103qtComl as qtdItem, " +
                        "eaa0103unit as unitItem, cast(eaa0103json ->> 'desconto' as numeric(18,6)) as desconto, eaa0103total as totItem  " +
                        "from eaa0103 " +
                        "inner join abm01 on abm01id = eaa0103item " +
                        "left join abm0101 on abm0101item = abm01id " +
                        "left join abg01 on abg01id = abm0101ncm " +
                        "where eaa0103doc = :idDocumento "+
                        "order by eaa0103seq "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocumento", idDocumento));
    }

    private List<TableMap> buscarParcelasDocumento(Long idDocumento){
        String sql = "select eaa0113dtvcton as dtVencimento, eaa0113valor as valor " +
                "from eaa0113  " +
                "where  eaa0113doc = :idDocumento " +
                "order by eaa0113dtvcton"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDocumento", idDocumento));
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIENvbXByYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==