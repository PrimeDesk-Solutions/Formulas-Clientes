package Silcon.relatorios.scf

import br.com.multitec.utils.collections.TableMap
import com.lowagie.text.Table;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_LancamentosPorDeptoNat extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos por Centro de Custos e Nat";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsDeptos = getListLong("departamento");
        List<Long> idsNaturezas = getListLong("naturezas");
        LocalDate[] dataLcto = getIntervaloDatas("data");
        Integer agrupamento = getInteger("agrupamento");
        Integer detalhamento = getInteger("detalhamento");
        Integer impressao = getInteger("impressao");


        if(detalhamento == 0){
            params.put("titulo", "SCF - Lançamentos por Departamento e Natureza (Análitico)")
        }else{
            params.put("titulo", "SCF - Lançamentos por Departamento e Natureza (Sintético)")
        }

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())


        List<TableMap> dados = buscarDadosRelatorioAnalitico(idsDeptos, idsNaturezas, dataLcto, agrupamento, detalhamento);
        List<TableMap> dadosSintetico = new ArrayList<>()

        if(detalhamento == 1){
            Map<String, BigDecimal> totaisNatureza = new HashMap<>();
            String depto = ""

            for (dado in dados){
                String natureza = dado.getString("codNatureza");
                String departamento = dado.getString("codDepto");
                String key = agrupamento == 0 ? departamento + "|" + natureza : natureza + "|" + departamento  ;
                if (depto == dado.getString("codDepto")){
                    BigDecimal vlrNatureza = dado.getBigDecimal_Zero("valor");
                    BigDecimal totNatureza = totaisNatureza.containsKey(key) ? totaisNatureza.get(key) : new BigDecimal(0);
                    BigDecimal soma = totNatureza + vlrNatureza
                    totaisNatureza.put(key, soma);
                }else{
                    totaisNatureza.put(key, new BigDecimal(0));
                    BigDecimal vlrNatureza = dado.getBigDecimal_Zero("valor");
                    totaisNatureza.put(key, vlrNatureza)
                }

                depto = dado.getString("codDepto")
            }

            Map<String, BigDecimal> hashOrdenado = new TreeMap<>(totaisNatureza); // Ordena o hash pela chave

            for (totais in hashOrdenado.entrySet()){
                TableMap tmRegistro = new TableMap();

                def key = totais.key.split("\\|");
                BigDecimal valor = totais.value;
                String codDepartamento = agrupamento == 0 ? key[0] : key[1];
                String codNatureza = agrupamento == 0 ? key[1] : key[0];

                TableMap tmLcto = dados.stream().filter(f -> f.getString("codDepto").equalsIgnoreCase(codDepartamento)).filter(f -> f.getString("codNatureza").equalsIgnoreCase(codNatureza)).findFirst().orElse(null)

                tmRegistro.put("codDepto", tmLcto.getString("codDepto"));
                tmRegistro.put("nomeDepto", tmLcto.getString("nomeDepto"));
                tmRegistro.put("codNatureza", tmLcto.getString("codNatureza"));
                tmRegistro.put("descrNat", tmLcto.getString("descrNat"));
                tmRegistro.put("valor", valor);

                dadosSintetico.add(tmRegistro)

            }

        }



        if(agrupamento == 0 && detalhamento == 0 && impressao == 0){
            return gerarPDF("SCF_Lancamentos_Por_Depto_Nat_Analitico_PDF", dados);
        }else if(agrupamento == 0 && detalhamento == 0 && impressao == 1){
            return gerarXLSX("SCF_Lancamentos_Por_Depto_Nat_Analitico_Excel", dados);
        }else if(agrupamento == 0 && detalhamento == 1 && impressao == 0){
            return gerarPDF("SCF_Lancamentos_Por_Depto_Nat_Sintetico_PDF", dadosSintetico);
        }else if(agrupamento == 0 && detalhamento == 1 && impressao == 1){
            return gerarXLSX("SCF_Lancamentos_Por_Depto_Nat_Sintetico_Excel", dados);
        }else if(agrupamento == 1 && detalhamento == 0 && impressao == 0){
            return gerarPDF("SCF_Lancamentos_Por_Nat_Depto_Analitico_PDF", dados);
        }else if(agrupamento == 1 && detalhamento == 0 && impressao == 1){
            return gerarXLSX("SCF_Lancamentos_Por_Nat_Depto_Analitico_Excel", dados);
        }else if(agrupamento == 1 && detalhamento == 1 && impressao == 0){
            return gerarPDF("SCF_Lancamentos_Por_Nat_Depto_Sintetico_PDF", dados);
        }else{
            return gerarXLSX("SCF_Lancamentos_Por_Nat_Depto_Sintetico_Excel", dados);
        }


    }
    private List<TableMap> buscarDadosRelatorioAnalitico(List<Long> idsDeptos, List<Long> idsNaturezas, LocalDate[] dataLcto, Integer agrupamento, Integer detalhamento){
        String whereDeptos = idsDeptos != null && idsDeptos.size() > 0 ? "AND abb11id IN (:idsDeptos) " : "";
        String whereNat = idsNaturezas != null && idsNaturezas.size() > 0 ? "AND abf10id IN (:idsNaturezas) " : "";
        String whereDtLcto = dataLcto != null ? "AND dab10data between :dtInicial AND :dtFinal " : "";
        String whereEmpresa = "WHERE dab10gc = :idEmpresa ";

        Parametro parametroDeptos = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroNat = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroDtInicial = dataLcto != null ? Parametro.criar("dtInicial", dataLcto[0]) : null;
        Parametro parametroDtFinal = dataLcto != null ? Parametro.criar("dtFinal", dataLcto[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String orderBy = agrupamento == 0 ? "ORDER BY abb11codigo" : "ORDER BY abf10codigo"

        String sql = "SELECT DISTINCT abb11codigo AS codDepto, abb11nome AS nomeDepto, dab10data AS dtLcto, " +
                "dab01codigo AS codCC, dab01nome AS nomeCC, dab10historico AS historico,  " +
                "CASE WHEN dab10mov = 0 THEN 'C' ELSE 'D' END AS movimentacao, abf10codigo AS codNatureza, abf10nome AS descrNat, " +
                "CASE WHEN dab10mov = 1 THEN dab10011valor * (-1) ELSE dab10011valor END AS valor " +
                "FROM dab10 " +
                "INNER JOIN dab1001 ON dab1001lct = dab10id " +
                "INNER JOIN abb11 ON abb11id = dab1001depto " +
                "INNER JOIN dab1002 ON dab1002lct = dab10id " +
                "INNER JOIN dab01 ON dab01id = dab1002cc " +
                "INNER JOIN dab10011 ON dab10011depto = dab1001id "+
                "INNER JOIN abf10 ON abf10id = dab10011nat "+
                whereDeptos+
                whereNat+
                whereDtLcto+
                whereEmpresa +
                orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDeptos, parametroNat, parametroDtInicial, parametroDtFinal, parametroEmpresa)

    }
    private List<TableMap> buscarDadosRelatorioSintetico(List<Long> idsDeptos, List<Long> idsNaturezas, LocalDate[] dataLcto, Integer agrupamento, Integer detalhamento){
        String whereDeptos = idsDeptos != null && idsDeptos.size() > 0 ? "AND abb11id IN (:idsDeptos) " : "";
        String whereNat = idsNaturezas != null && idsNaturezas.size() > 0 ? "AND abf10id IN (:idsNaturezas) " : "";
        String whereDtLcto = dataLcto != null ? "AND dab10data between :dtInicial AND :dtFinal " : "";
        String whereEmpresa = "WHERE dab10gc = :idEmpresa ";

        Parametro parametroDeptos = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroNat = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroDtInicial = dataLcto != null ? Parametro.criar("dtInicial", dataLcto[0]) : null;
        Parametro parametroDtFinal = dataLcto != null ? Parametro.criar("dtFinal", dataLcto[2]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String orderBy = agrupamento == 0 ? "ORDER BY abb11codigo" : "ORDER BY abf10codigo"

        String sql = "SELECT DISTINCT abb11codigo AS codDepto, abb11nome AS nomeDepto, " +
                "CASE WHEN dab10mov = 0 THEN 'C' ELSE 'D' END AS movimentacao, abf10codigo AS codNatureza, abf10nome AS descrNat, " +
                "dab10011valor AS valor " +
                "FROM dab10 " +
                "INNER JOIN dab1001 ON dab1001lct = dab10id " +
                "INNER JOIN abb11 ON abb11id = dab1001depto " +
                "INNER JOIN dab1002 ON dab1002lct = dab10id " +
                "INNER JOIN dab01 ON dab01id = dab1002cc " +
                "INNER JOIN dab10011 ON dab10011depto = dab1001id "+
                "INNER JOIN abf10 ON abf10id = dab10011nat "+
                whereDeptos+
                whereNat+
                whereDtLcto+
                whereEmpresa +
                orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDeptos, parametroNat, parametroDtInicial, parametroDtFinal, parametroEmpresa)

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBwb3IgQ2VudHJvIGRlIEN1c3RvcyBlIE5hdCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==