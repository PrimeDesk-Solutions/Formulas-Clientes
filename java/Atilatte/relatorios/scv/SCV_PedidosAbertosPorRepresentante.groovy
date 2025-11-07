package Atilatte.relatorios.scv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_PedidosAbertosPorRepresentante extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pedidos Em Aberto Representantes";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("impressao", "0")
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsReps = getListLong("representantes");
        LocalDate[] dtEmissao = getIntervaloDatas("dataPedido");
        Integer impressao = getInteger("impressao");

        if (idsReps == null || idsReps.size() == 0) idsReps = buscarRepresentantes();

        List<TableMap> dados = buscarDadosRelatorio(idsReps, dtEmissao);

        params.put("titulo", "SCV - Pedidos em Aberto");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())

        if(impressao == 1) return gerarXLSX("SCV_PedidosAbertosPorRepresentante(Excel)", dados)

        return gerarPDF("SCV_PedidosAbertosPorRepresentante(PDF)", dados);

    }

    private List<TableMap> buscarDadosRelatorio(List<Long> idsReps, LocalDate[] dtEmissao ){
        List<TableMap> dados = new ArrayList()
        for (idRep in idsReps){
            TableMap representante = buscarInformacoesRepresentante(idRep)
            Long id = representante.getLong("abe01id");
            List<TableMap> pedidos = buscarPedidosEmAberto(idRep, dtEmissao);
            for(pedido in pedidos){
                pedido.putAll(representante);
            }
            dados.addAll(pedidos);
        }

        return dados;
    }

    private  List<TableMap> buscarPedidosEmAberto(Long idRep, LocalDate[] dtEmissao){
        String whereData = dtEmissao != null ? "AND abb01data BETWEEN :dtInicial and :dtFinal " : ""
        String whereRepresentantes = "AND (eaa01rep0 = :idRep OR eaa01rep1 = :idRep OR eaa01rep2 = :idRep OR eaa01rep3 = :idRep OR eaa01rep4 = :idRep) ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroDtInicial = dtEmissao != null ? Parametro.criar("dtInicial", dtEmissao[0]) : null;
        Parametro parametroDtFinal = dtEmissao != null ? Parametro.criar("dtFinal", dtEmissao[1]) : null;
        Parametro parametroRepresentantes = Parametro.criar("idRep", idRep);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = "SELECT abb01num AS numDoc, abb01data AS dtPedido, aag03nome AS regiao, abe01codigo AS codEntidade, abe01na AS naEntidade, " +
            "eaa01totDoc AS totDoc " +
            "FROM eaa01 " +
            "INNER JOIN abb01 ON abb01id = eaa01central " +
            "INNER JOIN abe01 ON abe01id = abb01ent "+
            "INNER JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 "+
            "LEFT JOIN aag03 ON aag03id = abe0101regiao "+
            "WHERE TRUE "+
            whereData +
            whereRepresentantes +
            whereEmpresa +
            "AND eaa01clasDoc = 0 " +
            "AND eaa01scvAtend = 0 " +
            "AND eaa01cancData IS NULL "+
            "ORDER BY abb01num, abb01data "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDtInicial, parametroDtFinal, parametroRepresentantes, parametroEmpresa);

    }

    private List<Long> buscarRepresentantes(){
        String sql = "SELECT abe01id AS idRep " +
                    "FROM abe01 " +
                    "WHERE abe01codigo LIKE '04%' " +
                    "ORDER BY abe01codigo"

        return getAcessoAoBanco().obterListaDeLong(sql)
    }

    private TableMap buscarInformacoesRepresentante(Long idRep){
        String sql = "SELECT abe01id, abe01codigo AS coRep, abe01na AS naRep FROM abe01 WHERE abe01id = :idRep"

        return getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("idRep", idRep))
    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MgRW0gQWJlcnRvIFJlcHJlc2VudGFudGVzIiwidGlwbyI6InJlbGF0b3JpbyJ9