package Atilatte.relatorios.scv;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro



public class SCV_PedidoVendaEPI extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pedido de Venda EPI";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsTipo = getListLong("idsTipo");
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<TableMap> dadosRelatorio = buscarDadosRelatorio(idsTipo, numDocIni, numDocFin );

        params.put("empresa", obterEmpresaAtiva().getAac10na())
        return gerarPDF("SCV_PedidoVendaEPI", dadosRelatorio)
    }

    private List<TableMap> buscarDadosRelatorio(List<Long> idsTipo, Integer numDocIni, Integer numDocFin ){

        String sql = "select abb01num, abm01na, eaa0103qtComl as qtd, eaa0103dtentrega as dtEntrega, abe01na, cast(eaa0103json ->> 'ca' as character varying(200)) as certificado "+
                "from eaa01 "+
                "inner join eaa0103 on eaa0103doc = eaa01id "+
                "inner join abm01 on abm01id = eaa0103item "+
                "inner join abb01 on abb01id = eaa01central "+
                "inner join abe01 on abe01id = abb01ent "+
                "inner join aah01 on abb01tipo = aah01id "+
                "where abb01num between :numDocIni and :numDocFin  "+
                "and aah01id in (:idsTipo)";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idsTipo", idsTipo), Parametro.criar("numDocIni", numDocIni), Parametro.criar("numDocFin", numDocFin));
    }


}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkbyBkZSBWZW5kYSBFUEkiLCJ0aXBvIjoicmVsYXRvcmlvIn0=
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkbyBkZSBWZW5kYSBFUEkiLCJ0aXBvIjoicmVsYXRvcmlvIn0=