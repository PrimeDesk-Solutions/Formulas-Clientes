package Profer.relatorios.scv

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

        List<Long> tipos = getListLong("tipos");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidades = getListLong("entidades");
        LocalDate[] emissao = getIntervaloDatas("emissao");

        List<TableMap> dados = buscarDadosRelatorio(tipos, numeroInicial, numeroFinal, entidades, emissao);
        List<TableMap> listItens = new ArrayList();

        for(dado in dados){
            Long idDoc = dado.getInteger("eaa01id");
            dado.put("key", idDoc);

            listItens = buscarDadosItens(idDoc);

            for(item in listItens){
                Long idItem = item.getLong("eaa0103item");
                BigDecimal estoqueAtual = buscarSaldoAtualItem(idItem);

                item.put("key", idDoc );
                item.put("estoqueAtual", estoqueAtual)
            }

        }

        TableMapDataSource dsPrincipal = new TableMapDataSource(dados);
        dsPrincipal.addSubDataSource("DsSub1", listItens, "key", "key");


        adicionarParametro("StreamSub1", carregarArquivoRelatorio("SCV_ImpressaoPedidoCompra_subreport1"))

        return gerarPDF("SCV_ImpressaoPedidoCompra", dsPrincipal)
    }

    private List<TableMap> buscarDadosRelatorio(List<Long> tipos, Integer numeroInicial, Integer numeroFinal, List<Long> entidades,LocalDate[] emissao){

        String whereEmpresa = "where eaa01gc = :idEmpresa "
        String whereClasDoc = "and eaa01clasdoc = 0 "
        String whereTipos = tipos != null && tipos.size() > 0 ? "and abb01tipo in (:tipos) " : "";
        String whereNumDoc = numeroInicial != null && numeroFinal != null ? "and abb01num between :numeroInicial and :numeroFinal " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "and abe01id in (:entidades) " : "";
        String whereDtEmissao = emissao != null ? "and abb01data between :dtInicial and :dtFinal " : "";

        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id())
        Parametro parametroTipos = tipos != null && tipos.size() > 0 ? Parametro.criar("tipos", tipos) : null;
        Parametro parametroNumIni = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumFin = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroDtEmissaoIni = emissao != null ? Parametro.criar("dtInicial", emissao[0]) : null;
        Parametro parametroDtEmissaoFin = emissao != null ? Parametro.criar("dtFinal", emissao[1]) : null;

        String sql =    "select distinct abb01num as numDoc, abe01nome as nomeEntidade, abe01ni as CNPJ, abe0101endereco as enderecoEntidade,  " +
                        "abe0101numero as numeroEntidade, abe0101bairro as bairroEntidade, eaa0102pcNome as contato,  " +
                        "abe0101ddd1 as DDDEntidade, abe0101fone1 as foneEntidade, aag0201nome as municipioEntidade, abb01data as dataDoc, eaa0103dtEntrega as dtEntrega, " +
                        "eaa01totDoc as totDoc, abe30nome as condPgto, eaa01id " +
                        "from eaa01 " +
                        "inner join abb01 on eaa01central = abb01id " +
                        "inner join abe01 on abb01ent = abe01id " +
                        "left join abe0101 on abe0101ent = abe01id and abe0101principal = 1 " +
                        "inner join eaa0102 on eaa0102doc = eaa01id " +
                        "inner join aag0201 on abe0101municipio = aag0201id " +
                        "inner join eaa0103 on eaa0103doc = eaa01id " +
                        "left join abe30 on eaa01cp = abe30id " +
                        whereEmpresa +
                        whereTipos+
                        whereNumDoc+
                        whereEntidades+
                        whereDtEmissao+
                        whereClasDoc;
        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroTipos, parametroNumIni, parametroNumFin, parametroEntidades, parametroDtEmissaoIni, parametroDtEmissaoFin )

    }

    private List<TableMap> buscarDadosItens(Long eaa01id){

        String sql = "select eaa0103seq, abm01descr as descrItem, eaa0103qtUso as qtd, abm01pesoBruto as pesoBrutoItem,  " +
                        "cast(eaa0103json ->> 'peso_bruto' as numeric(18,6)) as pesoBrutoDoc, eaa0103pcNum, abm01umu as umu, eaa0103unit as unit, eaa0103total as totalItem, " +
                        "cast(eaa0103json ->> 'icms' as numeric(18,6)) as icms, " +
                        "cast(eaa0103json ->> 'ipi' as numeric(18,6)) as ipi, eaa0103item " +
                        "from eaa0103 " +
                        "inner join abm01 on eaa0103item = abm01id " +
                        "inner join aam06 on abm01umu = aam06id " +
                        "where eaa0103doc = :idDoc "+
                        "order by eaa0103seq"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idDoc", eaa01id ));
    }

    private BigDecimal buscarSaldoAtualItem(Long idItem){
        String sql = "select bcc02qt from bcc02 " +
                        "where bcc02item =  :idItem " +
                        "and bcc02status = 4224";

        BigDecimal saldo =  getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idItem", idItem))

        return saldo == null ? new BigDecimal(0) : saldo;
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIEltcHJlc3PDo28gUGVkaWRvIENvbXByYSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==