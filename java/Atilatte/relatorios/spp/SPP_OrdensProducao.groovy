package Atilatte.relatorios.spp

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SPP_OrdensProducao extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPP - Ordens Produção";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String,Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("criada", true);
        filtrosDefault.put("emProcesso", true);
        filtrosDefault.put("concluida", true);

        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numIni = getInteger("numeroInicial");
        Integer numFin = getInteger("numeroFinal");
        LocalDate[] dtEncerramento = getIntervaloDatas("dataEncerramento");
        List<Long> itens = getListLong("itens");
        Integer impressao = getInteger("impressao");
        Boolean statusCriada = getBoolean("criada");
        Boolean statusEmProcesso = getBoolean("emProcesso");
        Boolean statusConcluida = getBoolean("concluida");

        List<Integer> status = new ArrayList<>()

        if(statusCriada) status.add(0);
        if(statusEmProcesso) status.add(1);
        if(statusConcluida) status.add(2);

        List<TableMap> dados = buscarDadosRelatorio(numIni, numFin, dtEncerramento, itens, status);

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());
        params.put("titulo", "SPP - Ordens de Produção");

        if(impressao == 1 ) return gerarXLSX("SPP_OrdensProducao(Excel)", dados);

        return gerarPDF("SPP_OrdensProducao(PDF)", dados)
    }

    private List<TableMap> buscarDadosRelatorio(Integer numIni, Integer numFin, LocalDate[] dtEncerramento, List<Long> itens, List<Integer> status){

        LocalDate dtEncIni = null;
        LocalDate dtEncFin = null;
        if(dtEncerramento != null){
            dtEncIni = dtEncerramento[0];
            dtEncFin = dtEncerramento[1];
        }

        String whereNumDocIni = "WHERE abb01num >= :numIni ";
        String whereNumDocFin = "AND abb01num <= :numFin ";
        String whereDtEncerramento = dtEncerramento != null ? "AND bab01ctdtf BETWEEN :dtEncIni AND :dtEncFin " : "";
        String whereItens = itens != null ? "AND abm01id IN (:itens) " : "";
        String whereStatus = status != [] ? "AND bab01status IN (:status) " : "";


        Parametro parametroNumDocIni = Parametro.criar("numIni", numIni);
        Parametro parametroNumDocFin = Parametro.criar("numFin", numFin);
        Parametro parametroDtEncerramentoIni = dtEncerramento != null ? Parametro.criar("dtEncIni", dtEncIni) : null;
        Parametro parametroDtEncerramentoFin = dtEncerramento != null ? Parametro.criar("dtEncFin", dtEncFin) : null;
        Parametro parametroItens = itens != null ? Parametro.criar("itens", itens) : null;
        Parametro parametroStatus = status != [] ? Parametro.criar("status", status) : null;

        String sql = "SELECT abb01num as numOp, abm01codigo as codItem, abm01na as naItem, " +
                "CASE WHEN bab01status = 0 then '0-Criada' WHEN bab01status = 1 THEN '1-Em Processo' else '2-Concluída' end as status, " +
                "baa01descr as plano " +
                "FROM bab01 " +
                "INNER JOIN abb01 ON abb01id = bab01central " +
                "INNER JOIN abp20 ON abp20id = bab01comp " +
                "INNER JOIN abm01 ON abm01id = abp20item " +
                "INNER JOIN bab0103 ON bab0103op = bab01id " +
                "LEFT JOIN baa0101 ON baa0101id = bab0103itempp " +
                "LEFT JOIN baa01 ON baa01id = baa0101plano " +
                whereNumDocIni  +
                whereNumDocFin +
                whereDtEncerramento +
                whereItens+
                whereStatus +
                "order by abb01num"
            return getAcessoAoBanco().buscarListaDeTableMap(sql,parametroNumDocIni,parametroNumDocFin,parametroDtEncerramentoIni,parametroDtEncerramentoFin,parametroItens,parametroStatus)
    }

}
//meta-sis-eyJkZXNjciI6IlNQUCAtIE9yZGVucyBQcm9kdcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9