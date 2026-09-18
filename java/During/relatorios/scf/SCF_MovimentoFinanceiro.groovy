package During.relatorios.scf

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_Movimentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Movimentos Financeiros";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsContas = getListLong("contas");
        List<Long> idsMovimento = getListLong("movimentos");
        LocalDate[] dtMovimento = getIntervaloDatas("periodo");

        List<TableMap> dados = buscarDadosRelatorio(idsContas, idsMovimento, dtMovimento);

        for (dado in dados){
            String historico = alterarHistorico(dado);
            dado.put("daa1001historico", historico)
        }

        return gerarPDF("SCF_Movimentos", dados);
    }

    private List<TableMap> buscarDadosRelatorio(List<Long> idsContas, List<Long> idsMovimento, LocalDate[] dtMovimento){
        String whereMovimento = idsMovimento != null && idsMovimento.size() > 0 ? "and daa10id in (:idsMovimento) " : "";
        String whereData = dtMovimento != null ? "and daa10data between :dtInicial and :dtFinal " : "";
        String whereContas = idsContas != null && idsContas.size() > 0 ? "and dab01id in (:idsContas) " : "";

        Parametro parametroMovimento = idsMovimento != null && idsMovimento.size() > 0 ? Parametro.criar("idsMovimento", idsMovimento) : null;
        Parametro parametroDtInicial = dtMovimento != null ? Parametro.criar("dtInicial", dtMovimento[0]) : null;
        Parametro parametroDtFinal = dtMovimento != null ? Parametro.criar("dtFinal", dtMovimento[1]) : null;
        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsMovimento", idsMovimento) : null;

        String sql = "select daa10nome, abe01nome, daa1001valor, daa10data, dab01codigo, dab01nome, aah01codigo, abb01num, case when daa1001mov = 0 then 'C' else 'D' end as mov, " +
                "daa1001valor, (COALESCE(CAST(DAA1001JSON ->> 'juros' AS NUMERIC(18,6)),0.00) + COALESCE(CAST(DAA1001JSON ->> 'multa' AS NUMERIC(18,6)),0.00) + COALESCE(CAST(DAA1001JSON ->> 'encargos' AS NUMERIC(18,6)),0.00)) as jme, " +
                "COALESCE(cast(daa1001json ->> 'desconto' as numeric(18,6)),0.00) as desconto, daa1001liquido,daa01dtvcton, daa1001dtpgto, abf20codigo, abf20descr, daa1001historico " +
                "from daa10 " +
                "inner join daa1001 on daa1001movim = daa10id " +
                "inner join dab01 on dab01id = daa1001cc " +
                "inner join abb01 on abb01id = daa1001central " +
                "left join abe01 on abe01id = abb01ent "+
                "inner join daa01 on daa01central = abb01id " +
                "inner join aah01 on aah01id = abb01tipo " +
                "inner join abf20 on abf20id = daa1001plf "+
                whereMovimento +
                whereData +
                whereContas +
                "order by daa10nome, daa1001num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroMovimento, parametroContas, parametroDtInicial, parametroDtFinal);
    }

    private alterarHistorico(TableMap documento){
        String historico = documento.getString("daa1001historico");

        historico = historico.replace("\$2", documento.getInteger("abb01num").toString());
        historico = historico.replace("\$5", documento.getString("abe01nome") == null ? "" : documento.getString("abe01nome") );

        return historico;
    }
}
//meta-sis-eyJkZXNjciI6IlNDRl9Nb3ZpbWVudG9GaW5hbmNlaXJvIiwidGlwbyI6InJlbGF0b3JpbyJ9