package Silcon.relatorios.customizados

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class CST_Controle_Cheques extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Controle de Cheques";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("ordenacao", "0");
        filtrosDefault.put("depositar", true);
        filtrosDefault.put("depositado", true);
        filtrosDefault.put("devolvido", true);
        filtrosDefault.put("pagamento", true);
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> idsContas = getListLong("contas");
        List<Long> idsEntidades = getListLong("entidades");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtVencimento = getIntervaloDatas("dataVcto");
        Boolean depositar = getBoolean("depositar");
        Boolean depositado = getBoolean("depositado");
        Boolean devolvido = getBoolean("devolvido");
        Boolean pagamento = getBoolean("pagamento");
        Integer impressao = getInteger("impressao");
        Integer ordem = getInteger("ordenacao");

        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());
        params.put("TITULO", "Controle de Cheques");


        if (dtEmissao != null) {
            params.put("PERIODO",dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() );
        }

        if (dtVencimento != null) {
            params.put("PERIODO",dtVencimento[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtVencimento[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        List<TableMap> dados = buscarDadosRelatorio(numeroInicial, numeroFinal, idsContas, idsEntidades, dtEmissao, dtVencimento, depositar, depositado, devolvido, pagamento, ordem);

        for(dado in dados){
            Integer status = dado.getInteger("status");
            if(status == 0){
                dado.put("situacao", "1-Depositar");
            }else if(status == 1){
                dado.put("situacao", "2-Depositado");
            }else if(status == 2){
                dado.put("situacao", "3-Devolvido");
            }else{
                dado.put("situacao", "4-Utilizado")
            }
        }

        if(impressao == 1 ) return gerarXLSX("CST_Controle_Cheques_Excel", dados);

        return gerarPDF("CST_Controle_Cheques_PDF", dados);
    }

    private List<TableMap> buscarDadosRelatorio(Integer numeroInicial, Integer numeroFinal, List<Long> idsContas, List<Long> idsEntidades, LocalDate[] dtEmissao, LocalDate[] dtVencimento, Boolean depositar, Boolean depositado, Boolean devolvido, Boolean pagamento, Integer ordem){
        List<Integer> status = new ArrayList<>();
        if (depositar) status.add(0);
        if (depositado) status.add(1);
        if (devolvido) status.add(2);
        if (pagamento) status.add(3);

        String whereNumCheque = " AND dab20num BETWEEN :numeroInicial AND :numeroFinal ";
        String whereContas = idsContas != null && idsContas.size() > 0 ? " AND dab1002cc IN (:idsContas) " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? " AND correntista.abe01id IN (:idsEntidades) " : "";
        String whereDtEmissao = dtEmissao != null ? " AND dab20dtEmis BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDtVencimento = dtVencimento != null ? " AND dab20dtVcto BETWEEN :dtVctoIni AND :dtVctoFin " : "";
        String whereStatus = status != null && status.size() > 0 ? " AND dab20status IN (:status) " : "";
        String whereOrdem = ordem == 0 ? " ORDER BY dab20dtVcto " : ordem == 1 ? " ORDER BY dab20valor " : " ORDER BY correntista.abe01codigo";

        Parametro parametroNumeroInicial = numeroInicial != null ? Parametro.criar("numeroInicial", numeroInicial) : null;
        Parametro parametroNumeroFinal = numeroFinal != null ? Parametro.criar("numeroFinal", numeroFinal) : null;
        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametrodtEmissaoInicial = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissao[0]) : null;
        Parametro parametrodtEmissaoFinal = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissao[1]) : null;
        Parametro parametrodtVctoInicial = dtVencimento != null ? Parametro.criar("dtVctoIni", dtVencimento[0]) : null;
        Parametro parametroVctoFinal = dtVencimento != null ? Parametro.criar("dtVctoFin", dtVencimento[1]) : null;
        Parametro parametroStatus = status != null && status.size() > 0 ? Parametro.criar("status", status) : null;

        String sql = "SELECT correntista.abe01codigo AS codCorrentista, correntista.abe01na AS nomeCorrentista, " +
                        "entidade.abe01codigo AS codEntidade, entidade.abe01na AS nomeEntidade, entidade.abe01ni AS cnpj, " +
                        "dab20status AS status, dab20dtVcto AS vencimento, dab20banco AS banco, dab20agencia AS agencia, dab20conta AS conta, " +
                        "dab20num AS numero, dab20valor AS valor,  dab01codigo AS codCC, dab01nome AS nomeCC " +
                        "FROM dab20 " +
                        "INNER JOIN dab1002 ON dab1002cheque = dab20id " +
                        "INNER JOIN dab10 ON dab10id = dab1002lct " +
                        "LEFT JOIN abb01 ON abb01id = dab10central " +
                        "INNER JOIN abe01 AS correntista ON correntista.abe01id = dab20cEnt " +
                        "INNER JOIN abe01 AS entidade ON entidade.abe01id = abb01ent " +
                        "INNER JOIN dab01 ON dab01id = dab1002cc " +
                        "WHERE TRUE " +
                        whereNumCheque +
                        whereContas +
                        whereEntidades +
                        whereDtEmissao +
                        whereDtVencimento +
                        whereStatus +
                        whereOrdem;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumeroInicial, parametroNumeroFinal, parametroContas, parametroEntidades, parametrodtEmissaoInicial, parametrodtEmissaoFinal, parametrodtVctoInicial, parametroVctoFinal, parametroStatus)

    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIENvbnRyb2xlIGRlIENoZXF1ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=