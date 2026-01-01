package Silcon.relatorios.scf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_LancamentosPorCentroCusto extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos por Centro de Custo";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("exportar", "0");
        filtrosDefault.put("classe", "0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsDeptos = getListLong("departamento");
        LocalDate[] datas = getIntervaloDatas("data");
        List<Long> contasCorrentes = getListLong("contaCorrente");
        Integer movimentacao = getInteger("classe");
        Integer impressao = getInteger("exportar");
        Aac10 empresa = obterEmpresaAtiva()

        List<TableMap> dados = buscarDadosRelatorio(idsDeptos, datas, contasCorrentes, movimentacao, empresa);

        params.put("TITULO", "SCF - Lançamentos por Centro de Custos");
        params.put("EMPRESA", empresa.getAac10codigo() + " - " + empresa.getAac10na())

        if(impressao == 1 ) return gerarXLSX("SCF_Lancamentos_Por_Centro_Custo_Excel", dados);

        return gerarPDF("SCF_Lancamentos_Por_Centro_Custo_PDF", dados);
    }
    private List<TableMap> buscarDadosRelatorio( List<Long>idsDeptos, LocalDate[] datas,  List<Long> contasCorrentes, Integer movimentacao, Aac10 empresa){

        LocalDate dtInicial = null
        LocalDate dtFinal = null
        if( datas != null){
            dtInicial = datas[0]
            dtFinal = datas[1]
        }

        String whereDepartamento = idsDeptos != null && idsDeptos.size() > 0 ? "AND abb11id IN (:idsDeptos) " : "";
        String whereDatas = dtInicial != null && dtFinal != null ? "AND dab10data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereContasCorrentes = contasCorrentes != null && contasCorrentes.size() > 0 ? "AND dab01id IN (:contasCorrentes) " : "";
        String whereMovimentacao = movimentacao == 0 ? "AND dab10mov IN (0,1) " : movimentacao == 1 ? "AND dab10mov = 1 " : "AND dab10mov = 0 ";
        String whereEmpresa = "WHERE dab10gc IN (:idEmpresa) ";

        Parametro parametroDepartamento = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroDtInicial = dtInicial != null ? Parametro.criar("dtInicial", dtInicial) : null;
        Parametro parametroDtFinal = dtFinal != null ? Parametro.criar("dtFinal", dtFinal) : null;
        Parametro parametroContasCorrentes = contasCorrentes != null && contasCorrentes.size() > 0 ? Parametro.criar("contasCorrentes", contasCorrentes) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", empresa.getAac10id());

        String sql = "SELECT abb11codigo AS codDepto, abb11nome AS nomeDepto, dab10data AS dtLcto, " +
                    "dab01codigo AS codCC, dab01nome AS nomeCC, dab10historico AS historico,  " +
                    "CASE WHEN dab10mov = 0 THEN 'C' ELSE 'D' END AS movimentacao, dab10valor AS valor " +
                    "FROM dab10 " +
                    "INNER JOIN dab1001 ON dab1001lct = dab10id " +
                    "INNER JOIN abb11 ON abb11id = dab1001depto " +
                    "INNER JOIN dab1002 ON dab1002lct = dab10id " +
                    "INNER JOIN dab01 ON dab01id = dab1002cc "+
                    whereEmpresa +
                    whereDepartamento +
                    whereDatas +
                    whereContasCorrentes +
                    whereMovimentacao +
                    "ORDER BY abb11codigo, dab10data ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroEmpresa, parametroDepartamento, parametroDtInicial, parametroDtFinal, parametroContasCorrentes );

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBwb3IgQ2VudHJvIGRlIEN1c3RvIiwidGlwbyI6InJlbGF0b3JpbyJ9
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBwb3IgQ2VudHJvIGRlIEN1c3RvIiwidGlwbyI6InJlbGF0b3JpbyJ9