package Silcon.relatorios.customizados

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class CST_Movimento_Caixa extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Movimento Caixa";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        LocalDate[] dtEmissao = getIntervaloDatas("dtEmissao");
        LocalDate[] dtLancamento = getIntervaloDatas("dtLcto");
        List<Long> departamentos = getListLong("departamentos");
        List<Long> naturezas = getListLong("naturezas");

        List<TableMap> dados = buscarDadosRelatorio(dtEmissao, dtLancamento, departamentos, naturezas);

        if(dados.size() == 0) interromper("Não foram encontrados registros com os filtros informados.");

        List<TableMap> registros = new ArrayList<>();

        String departamento = null;
        String natureza = null;

        TableMap dadosAux = new TableMap();
        TableMap valoresTotais = new TableMap();
        for(dado in dados){
            if(departamento == null && natureza == null){
                departamento = dado.getString("codDepto");
                natureza = dado.getString("codNat");
                dadosAux.putAll(dado);

                agruparValores(dado, valoresTotais);
            }else if(departamento == dado.getString("codDepto") && natureza == dado.getString("codNat")){
                agruparValores(dado, valoresTotais);
            }else{
                TableMap tmp = new TableMap();
                tmp.putAll(dadosAux);
                tmp.putAll(valoresTotais);
                registros.add(tmp);

                dadosAux = new TableMap();
                dadosAux.putAll(dado);
                valoresTotais = new TableMap();
                departamento = dado.getString("codDepto");
                natureza = dado.getString("codNat");

                agruparValores(dado, valoresTotais);
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosAux);
        tmp.putAll(valoresTotais);
        registros.add(tmp);

        if(dtEmissao != null){
            String periodo = "Período de: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
            params.put("periodo", periodo);
        }

        return gerarPDF("CST_Movimento_Caixa", registros);
    }
    private List<TableMap> buscarDadosRelatorio( LocalDate[] dtEmissao, LocalDate[] dtLancamento, List<Long> departamentos, List<Long> naturezas){

        String whereDtEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereDtLcto = dtLancamento != null ? "AND dab10data BETWEEN :dtLctoIni AND :dtLctoFin " : "";
        String whereDepartamento = departamentos != null && departamentos.size() > 0 ? "AND abb11id IN (:departamentos) " : "";
        String whereNaturezas = naturezas != null && naturezas.size() > 0 ? "AND abf10id IN (:naturezas) " : "";

        Parametro parametroDtEmissaoIni = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissao[1]) : null;
        Parametro parametroLctoIni = dtLancamento != null ? Parametro.criar("dtLctoIni", dtLancamento[0]) : null;
        Parametro parametroDtLctoFin = dtLancamento != null ? Parametro.criar("dtLctoFin", dtLancamento[1]) : null;
        Parametro parametroDepto = departamentos != null && departamentos.size() > 0 ? Parametro.criar("departamentos", departamentos) : null;
        Parametro parametroNaturezas = naturezas != null && naturezas.size() > 0 ? Parametro.criar("naturezas", naturezas) : null;

        String sql = "SELECT abb11codigo AS codDepto, abb11nome AS nomeDepto, abf10codigo AS codNat,  " +
                    "abf10nome AS nomeNat, CASE WHEN dab10mov = 0 THEN COALESCE(dab10011valor, 0.00) END AS debito, " +
                    "CASE WHEN dab10mov = 1 THEN COALESCE(dab10011valor * -1, 0.00) END AS credito " +
                    "FROM dab10 " +
                    "INNER JOIN dab1001 ON dab1001lct = dab10id " +
                    "INNER JOIN dab10011 ON dab10011depto = dab1001id " +
                    "INNER JOIN abb11 ON abb11id = dab1001depto " +
                    "INNER JOIN abf10 ON abf10id = dab10011nat " +
                    "INNER JOIN abb01 ON abb01id = dab10central "+
                    whereDtEmissao +
                    whereDtLcto +
                    whereDepartamento +
                    whereNaturezas +
                    "ORDER BY abb11codigo, abf10codigo, dab10data ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroLctoIni, parametroDtLctoFin, parametroDepto, parametroNaturezas);
    }

    private void agruparValores(TableMap dado, TableMap dadoAux){
        if(dadoAux.getBigDecimal("totalDebito") == null && dadoAux.getBigDecimal("totalCredito") == null ){
            dadoAux.put("totalDebito", dado.getBigDecimal_Zero("debito"));
            dadoAux.put("totalCredito", dado.getBigDecimal_Zero("credito"));
            dadoAux.put("saldo", dado.getBigDecimal_Zero("debito") + dado.getBigDecimal_Zero("credito"))
        }else{
            BigDecimal debitoTotal = dadoAux.getBigDecimal_Zero("totalDebito");
            BigDecimal creditoTotal = dadoAux.getBigDecimal_Zero("totalCredito");
            BigDecimal debitoLcto = dado.getBigDecimal_Zero("debito");
            BigDecimal creditoLcto = dado.getBigDecimal_Zero("credito");
            BigDecimal somaDebito = debitoTotal + debitoLcto;
            BigDecimal somaCredito = creditoTotal + creditoLcto;


            dadoAux.put("totalDebito", somaDebito);
            dadoAux.put("totalCredito", somaCredito);
            dadoAux.put("saldo", somaDebito + somaCredito);
        }
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIE1vdmltZW50byBDYWl4YSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==