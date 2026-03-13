package Silcon.relatorios.customizados

import br.com.multiorm.ColumnType
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

public class CST_SRF8101R1_Documentos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - SRF8101R1 Documentos";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("resumoOperacao", "0");
        filtrosDefault.put("nomeEntidade", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("total1", true)
        filtrosDefault.put("total2", true)
        filtrosDefault.put("total3", true)
        filtrosDefault.put("total4", true)
        filtrosDefault.put("devolucao", true)
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer operacao = getInteger("resumoOperacao");
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> idsEntidades = getListLong("entidades");
        List<Long> idsTiposDoc = getListLong("tiposDoc");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntSai");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        List<Long> idsPcds = getListLong("pcds");
        List<Long> idsEstados = getListLong("estados");
        List<Long> idsMunicipios = getListLong("municipios");
        Integer impressao = getInteger("impressao");
        String camposLivre1 = getString("campoLivre1");
        String camposFixo1 = getString("campoFixo1");
        String camposLivre2 = getString("campoLivre2");
        String camposFixo2 = getString("campoFixo2");
        String camposLivre3 = getString("campoLivre3");
        String camposFixo3 = getString("campoFixo3");
        Boolean totalizar1 = getBoolean("total1");
        Boolean totalizar2 = getBoolean("total2");
        Boolean totalizar3 = getBoolean("total3");

        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        if (operacao.equals(1)) {
            params.put("TITULO_RELATORIO", "SRF - Faturamento");
        } else {
            params.put("TITULO_RELATORIO", "Documentos - Recebimento");
        }

        if (dtEmissao != null) {
            params.put("PERIODO", "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        if (dtEntradaSaida != null) {
            params.put("PERIODO", "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        adicionarParametro("totalizar1", totalizar1)
        adicionarParametro("totalizar2", totalizar2)
        adicionarParametro("totalizar3", totalizar3)

        HashMap<String, String> campos = new HashMap();

        campos.put("1", camposLivre1 != null ? camposLivre1 : camposFixo1 != null ? camposFixo1 : null);
        campos.put("2", camposLivre2 != null ? camposLivre2 : camposFixo2 != null ? camposFixo2 : null);
        campos.put("3", camposLivre3 != null ? camposLivre3 : camposFixo3 != null ? camposFixo3 : null);

        List<TableMap> dados = buscarDadosRelatorio(operacao, numeroInicial, numeroFinal, idsEntidades, idsTiposDoc, dtEntradaSaida, dtEmissao, idsPcds, idsEstados, idsMunicipios);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        List<TableMap> dadosRelatorio = new ArrayList();
        Long idControle = null;
        TableMap dadosTmp = new TableMap();
        TableMap valoresTotais = new TableMap();

        for (dado in dados) {
            Long idCentral = dado.getLong("abb01id");
            String descrSituacao = dado.getString("descrSituacao");
            LocalDate dtCancelamento = dado.getDate("dtCancelamento");
            String docDerivado = buscarDesdobramentosDocumento(idCentral);

            if(descrSituacao == null || descrSituacao.isEmpty() && dtCancelamento == null){
                dado.put("descrSituacao", "CANCELADO");
            }

            dado.put("docDerivado", docDerivado);
            dado.put("nomeCampo1", "Total Doc.")
            dado.put("nomeCampo2", "Total Financ.")
            dado.put("valorCampo1", dado.getBigDecimal_Zero("eaa01totDoc"));
            dado.put("valorCampo2", dado.getBigDecimal_Zero("eaa01totFinanc"));
        }

        if(impressao == 1) return gerarXLSX("CST_SRF8101R1_Documentos_Excel", dados);

        return gerarPDF("CST_SRF8101R1_Documentos_PDF", dados)

    }

    private List<TableMap> buscarDadosRelatorio(Integer operacao, Integer numeroInicial, Integer numeroFinal, List<Long> idsEntidades, List<Long> idsTiposDoc, LocalDate[] dtEntradaSaida, LocalDate[] dtEmissao, List<Long> idsPcds, List<Long> idsEstados, List<Long> idsMunicipios){
        String whereNumDoc = " WHERE abb01num BETWEEN :numDocIni AND :numDocFin ";
        String whereOperacao = operacao == 0 ? " AND eaa01esMov = 0 " : " AND eaa01esMov = 1 ";
        String whereClassDoc = " AND eaa01clasDoc = " + Eaa01.CLASDOC_SRF + " ";
        //String whereCancelamento = " AND eaa01Nota.eaa01cancData IS NULL ";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? " AND abe01id IN (:idsEntidades) " : "";
        String whereTipoDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? " AND aah01id IN (:idsTiposDoc) " : "";
        String whereDtEmissao = dtEmissao != null ? " AND abb01data BETWEEN :dtEmissaoIni AND :dtEmissaoFin " : "";
        String whereEsData = dtEntradaSaida != null ? " AND eaa01esData BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String wherePcd = idsPcds != null && idsPcds.size() > 0 ? " AND abd01id IN (:idsPcds) " : "";
        String whereEstados = idsEstados != null && idsEstados.size() > 0 ? " AND aag02id IN (:idsEstados) " : "";
        String whereMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? "AND aag0201id IN (:idsMunicipios) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";

        Parametro parametroNumDocIni = numeroInicial != null ? Parametro.criar("numDocIni", numeroInicial) : null;
        Parametro parametroNumDocFin = numeroFinal != null ? Parametro.criar("numDocFin", numeroFinal) : null;
        Parametro parametroEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTiposDoc = idsTiposDoc != null && idsTiposDoc.size() > 0 ? Parametro.criar("idsTiposDoc", idsTiposDoc) : null;
        Parametro parametroDtEmissaoInicial = dtEmissao != null ? Parametro.criar("dtEmissaoIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFinal = dtEmissao != null ? Parametro.criar("dtEmissaoFin", dtEmissao[1]) : null;
        Parametro parametroDtEntSaidaInicial = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntSaidaFinal = dtEntradaSaida != null ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroPcds = idsPcds != null && idsPcds.size() > 0 ? Parametro.criar("idsPcds", idsPcds) : null;
        Parametro parametroEstados = idsEstados != null && idsEstados.size() > 0 ? Parametro.criar("idsEstados", idsEstados) : null;
        Parametro parametroMunicipios = idsMunicipios != null && idsMunicipios.size() > 0 ? Parametro.criar("idsMunicipios", idsMunicipios) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = " SELECT eaa01id, abb01id, aah01codigo AS codTipoDocNota, abb01num AS numDocNota, abb01data AS dtEmissaoNota, " +
                        " eaa01esData AS dtEntradaSaidaNota, aaj03codigo AS codSituacao, aaj03descr AS descrSituacao, abe01codigo AS codEntidade, " +
                        " abe01na AS nomeEntidade, aag02uf AS ufEntidade, abe30codigo AS codCondPgto, abe30nome AS descrCondPgto, aah01codigo AS codTipoDocRef, " +
                        " eaa01cancData AS dtCancelamento, eaa01totDoc, eaa01totFinanc"+
                        " FROM eaa01 " +
                        " INNER JOIN abb01 ON abb01id = eaa01central " +
                        " INNER JOIN abd01 ON abd01id = eaa01pcd "+
                        " INNER JOIN aah01 ON aah01id = abb01tipo " +
                        " LEFT JOIN aaj03 ON aaj03id = eaa01sitDoc " +
                        " INNER JOIN abe01 ON abe01id = abb01ent  " +
                        " LEFT JOIN abe0101 ON abe0101ent = abe01id AND abe0101principal = 1 " +
                        " LEFT JOIN aag0201 ON aag0201id = abe0101municipio " +
                        " LEFT JOIN aag02 ON aag02id = aag0201uf " +
                        " LEFT JOIN abe30 ON abe30id = eaa01cp " +
                        whereNumDoc +
                        whereOperacao +
                        whereClassDoc +
                        whereEntidades +
                        whereTipoDoc +
                        whereDtEmissao +
                        whereEsData +
                        wherePcd +
                        whereEstados +
                        whereMunicipios +
                        whereEmpresa +
                        "ORDER BY abb01num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroEntidades, parametroTiposDoc, parametroDtEmissaoInicial, parametroDtEmissaoFinal, parametroDtEntSaidaInicial, parametroDtEntSaidaFinal, parametroPcds, parametroEstados, parametroMunicipios, parametroEmpresa )
    }
    private String buscarDesdobramentosDocumento(Long idCentral){
        return getSession().createQuery("SELECT CONCAT(aah01codigo,' ', aah01nome, ' ', abb01num ) AS docDerivado " +
                                        "FROM abb0102 " +
                                        "INNER JOIN abb01 ON abb01id = abb0102doc " +
                                        "INNER JOIN aah01 ON aah01id = abb01tipo " +
                                        "WHERE abb0102central = :idCentral "+
                                        "AND abb01tipo IN (295886, 584028, 35854674, 35854595)").setMaxResult(1)
                                        .setParameter("idCentral", idCentral)
                                        .getUniqueResult(ColumnType.STRING)
    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIFNSRjgxMDFSMSBEb2N1bWVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9