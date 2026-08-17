package PrimeDesk.relatorios.scf

import br.com.multitec.utils.Utils
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
        filtrosDefault.put("agrupamento", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("detalhamento", "0");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsDeptos = getListLong("departamento");
        List<Long> idsNaturezas = getListLong("naturezas");
        LocalDate[] dataLcto = getIntervaloDatas("data");
        Integer agrupamento = getInteger("agrupamento");
        Integer detalhamento = getInteger("detalhamento");
        List<Long> entidades = getListLong("entidade");
        List<Long> documentos = getListLong("documento");
        List<Long> idsContas = getListLong("contas");
        Integer impressao = getInteger("impressao");


        if(detalhamento == 0){
            params.put("titulo", "SCF - Lançamentos por Departamento e Natureza (Analítico)")
        }else{
            params.put("titulo", "SCF - Lançamentos por Departamento e Natureza (Sintético)")
        }

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na())


        List<TableMap> dados = buscarDadosRelatorio(idsDeptos, idsNaturezas, dataLcto, agrupamento, entidades, documentos, idsContas);
        List<TableMap> dadosSintetico = new ArrayList<>()

        if(detalhamento == 1){
            Map<String, BigDecimal> totaisNatureza = new HashMap<>();
            String depto = ""

            for (dado in dados){
                Long idLcto = dado.getLong("dab10id");
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

        List<Long> idsLcto = new ArrayList<>();
        for(dado in dados){
            idsLcto.add(dado.getLong("dab10id"));
        }

        List<TableMap> listContasCorrentes = buscarContasCorrentesLctos(idsLcto);
        for(dado in dados){
            Long idLcto = dado.getLong("dab10id");
            TableMap tmContaCorrente = listContasCorrentes.stream().filter(t -> t.getLong("dab1002lct") == (idLcto)).findFirst().orElse(null);

            if(tmContaCorrente != null && tmContaCorrente.size() > 0) dado.putAll(tmContaCorrente);
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
    private List<TableMap> buscarDadosRelatorio(List<Long> idsDeptos, List<Long> idsNaturezas,
                                                         LocalDate[] dataLcto, Integer agrupamento, List<Long> entidades, List<Long> documentos, List<Long> idsContas){
        String whereDeptos = idsDeptos != null && idsDeptos.size() > 0 ? "AND abb11id IN (:idsDeptos) " : "";
        String whereNat = idsNaturezas != null && idsNaturezas.size() > 0 ? "AND abf10id IN (:idsNaturezas) " : "";
        String whereDtLcto = dataLcto != null ? "AND dab10data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereEmpresa = "WHERE dab10gc = :idEmpresa ";
        String whereTipoDoc = documentos != null && documentos.size() > 0 ? "AND abb01tipo IN (:documentos) " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND abb01ent IN (:entidades) " : "";
        String whereContasCorrentes = idsContas != null && idsContas.size() > 0 ? "AND dab1002cc IN (:idsContas) " : "";

        Parametro parametroDeptos = idsDeptos != null && idsDeptos.size() > 0 ? Parametro.criar("idsDeptos", idsDeptos) : null;
        Parametro parametroNat = idsNaturezas != null && idsNaturezas.size() > 0 ? Parametro.criar("idsNaturezas", idsNaturezas) : null;
        Parametro parametroDtInicial = dataLcto != null ? Parametro.criar("dtInicial", dataLcto[0]) : null;
        Parametro parametroDtFinal = dataLcto != null ? Parametro.criar("dtFinal", dataLcto[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());
        Parametro parametroTipoDoc = documentos != null && documentos.size() > 0 ? Parametro.criar("documentos", documentos) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;


        String orderBy = agrupamento == 0 ? "ORDER BY abb11codigo" : "ORDER BY abf10codigo"

        String sql = "SELECT DISTINCT dab10id, abb11codigo AS codDepto, abb11nome AS nomeDepto, dab10data AS dtLcto, " +
                "dab10historico AS historico,  " +
                "CASE WHEN dab10mov = 0 THEN 'C' ELSE 'D' END AS movimentacao, abf10codigo AS codNatureza, abf10nome AS descrNat, " +
                "dab10011valor AS valor " +
                "FROM dab10 " +
                "LEFT JOIN abb01 ON abb01id = dab10central "+
                "INNER JOIN dab1001 ON dab1001lct = dab10id " +
                "INNER JOIN abb11 ON abb11id = dab1001depto " +
                "INNER JOIN dab10011 ON dab10011depto = dab1001id "+
                "INNER JOIN dab1002 ON dab1002lct = dab10id "+
                "INNER JOIN abf10 ON abf10id = dab10011nat "+
                whereDeptos+
                whereNat+
                whereDtLcto+
                whereEmpresa +
                whereTipoDoc +
                whereEntidades +
                whereContasCorrentes +
                orderBy

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroDeptos, parametroNat,
                parametroDtInicial, parametroDtFinal, parametroEmpresa, parametroTipoDoc, parametroEntidades, parametroContas)

    }
    private List<TableMap> buscarContasCorrentesLctos(List<Long> idLcto){
        String sql = "SELECT dab1002lct, STRING_AGG(dab01codigo, ',') AS codCC, STRING_AGG(dab01nome, ',') AS nomeCC " +
                "FROM dab1002 "+
                "INNER JOIN dab01 ON dab01id = dab1002cc "+
                "WHERE dab1002lct IN (:idLcto) "+
                "GROUP BY dab1002lct"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idLcto", idLcto));
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBwb3IgQ2VudHJvIGRlIEN1c3RvcyBlIE5hdCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==