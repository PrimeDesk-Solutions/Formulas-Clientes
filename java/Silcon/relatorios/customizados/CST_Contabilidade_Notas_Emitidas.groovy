package Silcon.relatorios.customizados

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ea.Eaa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class CST_Contabilidade_Notas_Emitidas extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Contabilidade Notas Emitidas";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        filtrosDefault.put("nomeEntidade", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("devolucao", true)
        filtrosDefault.put("status", 0)
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> idsEntidades = getListLong("entidades");
        List<Long> idsTiposDoc = getListLong("tiposDoc");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntSai");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        Integer impressao = getInteger("impressao");
        String camposLivre1 = getString("campoLivre1");
        String camposFixo1 = getString("campoFixo1");
        String camposLivre2 = getString("campoLivre2");
        String camposFixo2 = getString("campoFixo2");
        String camposLivre3 = getString("campoLivre3");
        String camposFixo3 = getString("campoFixo3");
        String camposLivre4 = getString("campoLivre4");
        String camposFixo4 = getString("campoFixo4");
        String camposLivre5 = getString("campoLivre5");
        String camposFixo5 = getString("campoFixo5");
        Integer status = getInteger("status");

        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        if (status.equals(0)) {
            params.put("TITULO_RELATORIO", "Todos os Documentos Emitidos Entrada/Saída");
        } else if (status.equals(1)) {
            params.put("TITULO_RELATORIO", "Documentos Cancelados Entrada/Saída");
        } else if (status.equals(2)) {
            params.put("TITULO_RELATORIO", "Documentos Denegados");
        } else {
            params.put("TITULO_RELATORIO", "Documentos Inutilizados");
        }

        if (dtEmissao != null) {
            params.put("PERIODO_INICIAL",dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
            params.put("PERIODO_FINAL",dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        if (dtEntradaSaida != null) {
            params.put("PERIODO_INICIAL",dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
            params.put("PERIODO_FINAL",dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        HashMap<String, String> campos = new HashMap();

        campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null);
        campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null);
        campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null);
        campos.put("4", camposLivre4 != null ? camposLivre4 : camposFixo4 != null ? camposFixo4 : null);
        campos.put("5", camposLivre5 != null ? camposLivre5 : camposFixo5 != null ? camposFixo5 : null);


        List<TableMap> dados = buscarDadosRelatorio(numeroInicial, numeroFinal, idsEntidades, idsTiposDoc, dtEntradaSaida, dtEmissao, status);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<TableMap> dadosRelatorio = new ArrayList();
        Long idControle = null;
        TableMap dadosTmp = new TableMap();
        TableMap valoresTotais = new TableMap();

        for (dado in dados) {

            if (idControle == null) {
                dadosTmp.putAll(dado);
                idControle = dado.getLong("eaa01id");
                somarValores(dado, valoresTotais, campos);
            } else if (idControle == dado.getLong("eaa01id")) {
                somarValores(dado, valoresTotais, campos);
            } else {
                TableMap tmp = new TableMap();
                tmp.putAll(dadosTmp);
                tmp.putAll(valoresTotais);
                comporValores(tmp, campos);
                dadosRelatorio.add(tmp);

                dadosTmp = new TableMap();
                dadosTmp.putAll(dado);
                valoresTotais = new TableMap()
                idControle = dado.getLong("eaa01id");
                somarValores(dado, valoresTotais, campos);
            }
        }

        TableMap tmp = new TableMap();
        tmp.putAll(dadosTmp);
        tmp.putAll(valoresTotais);
        comporValores(tmp, campos);

        dadosRelatorio.add(tmp);

        if(impressao == 1) return gerarXLSX("CST_Contabilidade_Notas_Emitidas_Excel", dadosRelatorio);


        return gerarPDF("CST_Contabilidade_Notas_Emitidas_PDF", dadosRelatorio);
    }

    private List<TableMap> buscarDadosRelatorio(Integer numeroInicial, Integer numeroFinal, List<Long> idsEntidades, List<Long> idsTiposDoc, LocalDate[] dtEntradaSaida, LocalDate[] dtEmissao, Integer status) {
        String whereStatus = "";
        if(status == 1){
            whereStatus = "AND eaa01nfeStat = 7 AND eaa01cancData IS NOT NULL ";
        }else if(status == 2){
            whereStatus = "AND eaa01nfeStat = 5 ";
        }else if(status == 3){
            whereStatus = "AND eaa01nfeStat = 6"
        }
        String whereNumDoc = " AND abb01num BETWEEN :numDocIni AND :numDocFin ";
        String whereClassDoc = " AND eaa01clasDoc = " + Eaa01.CLASDOC_SRF;
        String whereCancelamento = " AND eaa01cancData IS NULL ";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? " AND abe01id IN (:idsEntidades) " : "";
        String whereTipoDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? " AND aah01id IN (:idsTiposDoc) " : "";
        String whereDtEmissao = dtEmissao != null ? " AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereEsData = dtEntradaSaida != null ? " AND eaa01esData BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numDocIni", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numDocFin", numeroFinal) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTiposDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? Parametro.criar("idsTiposDoc", idsTiposDoc) : null;
        Parametro parametroDtEmissaoInicial = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFinal = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissao[1]) : null;
        Parametro parametroDtEntSaidaInicial = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntSaidaFinal = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = "SELECT eaa01id, abb01num, abe01nome, aaj03descr, abe01ni, abe01ie, abb01data, eaa01nfestat, " +
                    "TRIM(UPPER(REPLACE(aaj03descr,'Documento',''))) AS situacao, eaa0103json, eaa01cancdata, " +
                    " eaa0103qtUso AS eaa0103qtuso, eaa0103qtComl AS eaa0103qtcoml, eaa0103unit AS eaa0103unit, eaa0103total AS eaa0103total, eaa0103totDoc AS eaa0103totdoc, eaa0103totFinanc AS eaa0103totfinanc, eaa0103json  "+
                    "FROM eaa01 " +
                    "INNER JOIN abb01 ON abb01id = eaa01central " +
                    "INNER JOIN abe01 ON abe01id = abb01ent " +
                    "INNER JOIN aaj03 ON aaj03id = eaa01sitDoc " +
                    "INNER JOIN eaa0103 ON eaa0103doc = eaa01id " +
                    "INNER JOIN aah01 ON aah01id = abb01tipo "+
                    "WHERE eaa01clasDoc = 1 " +
                    whereStatus +
                    whereNumDoc +
                    whereClassDoc +
                    whereCancelamento +
                    whereEntidades +
                    whereTipoDoc +
                    whereDtEmissao +
                    whereEsData +
                    whereEmpresa +
                    "ORDER BY abb01num, eaa01esData "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroEntidades, parametroTiposDoc, parametroDtEmissaoInicial, parametroDtEmissaoFinal, parametroDtEntSaidaInicial, parametroDtEntSaidaFinal, parametroEmpresa )
    }
    private void somarValores(TableMap valoresDocumento, TableMap valoresTotais, Map<String, String> campos) {
        for (campo in campos) {
            if (campo != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value.toString()) != null ? buscarNomeCampoFixo(campo.value.toString()) : campo.value.toString();
                if (valoresDocumento.getBigDecimal(nomeCampo) != null) {
                    if (valoresTotais.getBigDecimal(nomeCampo) == null) {
                        valoresTotais.put(nomeCampo, valoresDocumento.getBigDecimal_Zero(nomeCampo));
                    } else {
                        BigDecimal valorTotal = valoresTotais.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresTotais.getBigDecimal(nomeCampo);
                        BigDecimal valorItem = valoresDocumento.getBigDecimal(nomeCampo) == null ? new BigDecimal(0) : valoresDocumento.getBigDecimal(nomeCampo);
                        BigDecimal soma = valorTotal + valorItem;
                        valoresTotais.put(nomeCampo, soma)
                    }
                }
            }
        }
    }
    private void comporValores(TableMap dado, HashMap<String, String> campos) {
        for (campo in campos) {
            if (campo.value != null) {
                String nomeCampo = buscarNomeCampoFixo(campo.value);
                if (nomeCampo != null) {
                    dado.put("nomeCampo" + campo.key, campo.value);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(nomeCampo))
                } else {
                    nomeCampo = buscarNomeCampoLivre(campo.value);
                    dado.put("nomeCampo" + campo.key, nomeCampo);
                    dado.put("valorCampo" + campo.key, dado.getBigDecimal_Zero(campo.value));
                }
            }
        }
    }
    private String buscarNomeCampoFixo(String campo) {
        switch (campo) {
            case "Qtde. de Uso":
                return "eaa0103qtuso"
                break
            case "Qtde. Comercial":
                return "eaa0103qtcoml"
                break
            case "Preço Unitário":
                return "eaa0103unit"
                break
            case "Total do Item":
                return "eaa0103total"
                break
            case "Total Documento":
                return "eaa0103totdoc"
                break
            case "Total Financeiro":
                return "eaa0103totfinanc"
                break
        }
    }
    private String buscarNomeCampoLivre(String campo) {
        def sql = " SELECT aah02descr FROM aah02 WHERE aah02nome = :nome "
        return getAcessoAoBanco().obterString(sql, criarParametroSql("nome", campo))

    }

}
//meta-sis-eyJkZXNjciI6IkNTVCAtIENvbnRhYmlsaWRhZGUgTm90YXMgRW1pdGlkYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=