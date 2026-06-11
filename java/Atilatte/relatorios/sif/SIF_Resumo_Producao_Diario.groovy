package Atilatte.relatorios.sif;

import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.collections.TableMap
import java.time.Year
import java.time.YearMonth;
import java.util.Map;
import java.util.HashMap;
import br.com.multitec.utils.Utils
import java.time.LocalDate


public class SIF_Resumo_Producao_Diario extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SIF - Resumo Produção Diário";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        LocalDate[] datas = new LocalDate[2];
        List<Long> idsItens = getListLong("itens");
        String strDatas = getString("periodo");
        String[] parts = strDatas.split("/");
        int mes = Integer.parseInt(parts[0]);
        int ano = Integer.parseInt(parts[1]);
        YearMonth mesAno = YearMonth.of(ano, mes);
        LocalDate dtInicial = mesAno.atDay(1);
        LocalDate dtFinal = mesAno.atEndOfMonth();
        datas[0] = dtInicial;
        datas[1] = dtFinal;

        List<TableMap> dados = buscarDadosRelatorio(datas, idsItens, mes, ano);

        for(dado in dados){
            for(int i = 1; i <= 31; i++){
                if(i == 31 && datas[1].getDayOfMonth() == 30) continue;
                dado.put("nomeCampo" + i, (i.toString().length() < 2 ? "0" + i : i) + "/" + "0" + mes);
                dado.put("valorCampo" + i, dado.getBigDecimal("valor" + i));
            }
        }

        return gerarXLSX("SIF_Resumo_Producao_Diario_Excel", dados);
    }

    private List<TableMap> buscarDadosRelatorio(LocalDate[] datas, List<Long> idsItens, int mes, int ano) {
        StringBuilder campos = new StringBuilder()

        for(int i = 1; i <= 31; i++){

            String diaFmt = String.format("%02d", i);
            String mesFmt = String.format("%02d", mes);

            if(i == 31 && datas[1].getDayOfMonth() == 30) continue;

            String separador = i == 30 && datas[1].getDayOfMonth() == 30 ? " " : i == 31 && datas[1].getDayOfMonth() == 31 ? " " : ", ";
            String campo = "SUM(CASE WHEN bab0104data = '" + ano + "-" + mesFmt + "-" + diaFmt + "'" +" THEN bab01041qt * CAST(abm0101json ->> 'fator_litro' AS numeric(18,6)) ELSE 0 END) AS valor" + i + separador;

            campos.append(campo);
        }

        String whereDatas = "WHERE bab0104data BETWEEN :dtInicial AND :dtFinal ";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereTipoItens = "AND abm01tipo = 1 ";

        Parametro parametroDataInicial = Parametro.criar("dtInicial", datas[0]);
        Parametro parametroDataFinal = Parametro.criar("dtFinal", datas[1]);
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;

        String sql =
        "SELECT abm01codigo, abm01descr, " +
        campos +
        "FROM bab01 " +
        "INNER JOIN abp20 ON abp20id = bab01comp " +
        "INNER JOIN abm01 ON abm01id = abp20item " +
        "INNER JOIN bab0104 ON bab0104op = bab01id " +
        "INNER JOIN bab01041 ON bab01041pc = bab0104id " +
        "INNER JOIN abm0101 ON abm0101item = abm01id "+
        whereDatas +
        whereItens +
        whereTipoItens +
        "GROUP BY abm01codigo, abm01descr " +
        "ORDER BY abm01codigo"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDataInicial, parametroDataFinal, parametroItens)
    }
}