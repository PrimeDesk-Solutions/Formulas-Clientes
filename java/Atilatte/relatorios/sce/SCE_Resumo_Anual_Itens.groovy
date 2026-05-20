package Atilatte.relatorios.sce

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCE_Resumo_Anual_Itens extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCE - Resumo Anual Itens";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("movimentacao", "1")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsItens = getListLong("itens");
        List<Integer> mps = getListInteger("mps");
        List<Long> idsPLF = getListLong("plf");
        Integer movimentacao = getInteger("movimentacao");
        Integer impressao = getInteger("impressao");
        LocalDate[] data = getIntervaloDatas("data");

        List<TableMap> dados = buscarDadosRelatorio(idsItens, mps, idsPLF, movimentacao, data);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<TableMap> dadosRelatorio = new ArrayList<>();
        TableMap dadosTmp = new TableMap();
        TableMap valoresTotais = new TableMap();

        Long idItemAux = null;
        Integer mesAux = null;
        for (dado in dados) {

            if(idItemAux == null){
                dadosTmp.putAll(dado);
                idItemAux = dado.getLong("abm01id");
                mesAux = dado.getDate("bcc01data").getMonthValue();
                valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero("qtd"));
            }else if(idItemAux == dado.getLong("abm01id")){
                if(mesAux == dado.getDate("bcc01data").getMonthValue()){
                    valoresTotais.put("valor" + mesAux, valoresTotais.getBigDecimal_Zero("valor" + mesAux) + dado.getBigDecimal_Zero("qtd"));
                }else{
                    mesAux = dado.getDate("bcc01data").getMonthValue();
                    valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero("qtd"));
                }
            }else{
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                dadosRelatorio.add(tmp);

                dadosTmp = new TableMap();
                dadosTmp.putAll(dado);
                valoresTotais = new TableMap();
                idItemAux = dado.getLong("abm01id");
                mesAux = dado.getDate("bcc01data").getMonthValue();
                valoresTotais.put("valor" + mesAux, dado.getBigDecimal_Zero("qtd"));
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);

        dadosRelatorio.add(tmp);

        params.put("TOTALIZAR", true);
        params.put("TITULO","SCE - Resumo Itens Anuais");
        params.put("EMPRESA", obterEmpresaAtiva().aac10codigo + "-" + obterEmpresaAtiva().aac10na);

        if (impressao == 1) return gerarXLSX("SCE_Resumo_Anual_Itens_Excel",dadosRelatorio);
        return gerarPDF("SCE_Resumo_Anual_Itens_PDF",dadosRelatorio)

    }

    private List<TableMap> buscarDadosRelatorio(List<Long> idsItens, List<Integer> mps, List<Long> idsPLF, Integer movimentacao, LocalDate[] data) {
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereMps = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";
        String whereGC = "AND bcc01gc = :idGC ";
        String wherePLF = idsPLF != null && idsPLF.size() > 0 ? "AND abf20id IN (:idsPlf) " : "";
        String whereMovimentacao = movimentacao == 0 ? "AND bcc01mov = 0 " : "AND bcc01mov = 1 ";
        String whereDatas = data != null ? "AND bcc01data BETWEEN :dtInicial AND :dtFinal " : "";

        String sql = "SELECT abm01id, abm01tipo, abm01codigo, abm01descr, bcc01data,  SUM(bcc01qt) AS qtd " +
                "FROM bcc01 " +
                "INNER JOIN abm01 ON abm01id = bcc01item " +
                "WHERE TRUE " +
                whereItens +
                whereMps +
                whereGC +
                wherePLF +
                whereMovimentacao +
                whereDatas +
                "GROUP BY abm01id, abm01tipo, abm01codigo, abm01descr, bcc01data " +
                "ORDER BY abm01tipo, abm01codigo, bcc01data";

        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroMps = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroGC = Parametro.criar("idGC", obterEmpresaAtiva().getAac10id());
        Parametro parametroPLF = idsPLF != null && idsPLF.size() > 0 ? Parametro.criar("idsPlf", idsPLF) : null;
        Parametro dtInicial = data != null ? Parametro.criar("dtInicial", data[0]) : null;
        Parametro dtFinal = data != null ? Parametro.criar("dtFinal", data[1]) : null;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroItens, parametroMps, parametroGC, parametroPLF, dtInicial, dtFinal)
    }
}