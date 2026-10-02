package Atilatte.relatorios.srf

import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import java.time.LocalDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.ea.Eaa01
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import sam.server.samdev.relatorio.TableMapDataSource


public class SRF_Itens_Naturezas extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SRF - Itens Naturezas";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("total1", true);
        filtrosDefault.put("total2", true);
        filtrosDefault.put("total3", true);
        filtrosDefault.put("total4", true);
        filtrosDefault.put("total5", true);
        filtrosDefault.put("total6", true);
        filtrosDefault.put("devolucao", true);
        filtrosDefault.put("chkDevolucao", true);
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("operacao", "0");
        filtrosDefault.put("tipoOperacao", "0");
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("resumo", "0");
        filtrosDefault.put("resumoOperacao", "0")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<Long> idsTipoDoc = getListLong("tipo");
        List<Long> idsPcd = getListLong("pcd");
        Integer resumoOperacao = getInteger("resumoOperacao");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        LocalDate[] dtEntradaSaida = getIntervaloDatas("dataEntradaSaida");
        List<Long> idsEntidades = getListLong("entidade");
        List<Long> idsTransps = getListLong("transportadoras");
        List<Long> idsNcm = getListLong("ncm");
        List<Long> idsDepartamentos = getListLong("departamentos");
        List<Long> idsItens = getListLong("itens");
        List<Integer> mps = getListInteger("mps");
        Integer impressao = getInteger("impressao");
        Aac10 empresa = obterEmpresaAtiva();
        Long idEmpresa = empresa.aac10id;
        boolean devolucoes = getBoolean("chkDevolucao");

        String periodo = ""
        if (dtEmissao != null) {
            periodo = "Período Emissão: " + dtEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        } else if (dtEntradaSaida) {
            periodo = "Período Entrada/Saída: " + dtEntradaSaida[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtEntradaSaida[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString()
        }

        params.put("title", "SRF - Itens Naturezas");
        params.put("empresa", empresa.aac10codigo + "-" + empresa.aac10na);
        params.put("periodo", periodo);

        Map<String, String> campos = new HashMap();

        List<TableMap> dados = buscarDocumentos(numDocIni, numDocFin, idsTipoDoc, idsPcd, resumoOperacao, dtEmissao, dtEntradaSaida, idsEntidades, idsItens, idEmpresa, idsTransps, idsNcm, idsDepartamentos, mps);

        if (dados.size() == 0) interromper("Não foram encontrado dados com os filtros selecionados.");

        if (impressao == 0) return gerarPDF("SRF_Itens_Naturezas_PDF", dados);
        return gerarXLSX("SRF_Itens_Naturezas_Excel", dados);
    }

    private List<TableMap> buscarDocumentos(Integer numDocIni, Integer numDocFin, List<Long> idsTipoDoc, List<Long> idsPcd, Integer resumoOperacao, LocalDate[] dtEmissao, LocalDate[] dtEntradaSaida, List<Long> idsEntidades, List<Long> idsItens, Long idEmpresa, List<Long> idTransp, List<Long> idsNcm, List<Long> idsDepartamentos, List<Integer> mps) {
        //Data Emissao
        LocalDate dtEmissIni = null;
        LocalDate dtEmissFin = null;
        if (dtEmissao != null) {
            dtEmissIni = dtEmissao[0];
            dtEmissFin = dtEmissao[1];
        }

        //Data Entrada/Saida
        LocalDate dtEntradaSaidaIni = null;
        LocalDate dtEntradaSaidaFin = null;
        if (dtEntradaSaida != null) {
            dtEntradaSaidaIni = dtEntradaSaida[0];
            dtEntradaSaidaFin = dtEntradaSaida[1];
        }

        String whereNumIni = numDocIni != null ? "AND abb01num >= :numDocIni " : "";
        String whereNumFin = numDocFin != null ? "AND abb01num <= :numDocFin " : "";
        String whereTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? "AND aah01id in (:idsTipoDoc) " : "";
        String wherePcd = idsPcd != null && idsPcd.size() > 0 ? "AND abd01id in (:idsPcd) " : "";
        String whereDtEmissao = dtEmissIni != null && dtEmissFin != null ? "AND abb01data BETWEEN :dtEmissIni AND :dtEmissFin " : "";
        String whereDtEntradaSaida = dtEntradaSaidaIni != null && dtEntradaSaidaFin != null ? "AND eaa01esdata BETWEEN :dtEntradaSaidaIni AND :dtEntradaSaidaFin " : "";
        String whereEntidade = idsEntidades != null && idsEntidades.size() > 0 ? "AND ent.abe01id IN (:idsEntidades) " : "";
        String whereTransp = idTransp != null && idTransp.size() > 0 ? "AND desp.abe01id IN (:idTransp) " : "";
        String whereES = resumoOperacao == 1 ? " and eaa01esMov = 1 " : " AND eaa01esMov = 0 ";
        String whereItens = idsItens != null && idsItens.size() > 0 ? "AND abm01id IN (:idsItens) " : "";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereNcm = idsNcm != null && idsNcm.size() > 0 ? "AND abg01id IN (:idsNcm) " : "";
        String whereDepartamento = idsDepartamentos != null && idsDepartamentos.size() > 0 ? "AND abb11id IN (:departamentos) " : "";
        String whereMPS = !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";

        Parametro parametroNumIni = numDocIni != null ? Parametro.criar("numDocIni", numDocIni) : null;
        Parametro parametroNumFin = numDocFin != null ? Parametro.criar("numDocFin", numDocFin) : null;
        Parametro parametroTipoDoc = idsTipoDoc != null && idsTipoDoc.size() > 0 ? Parametro.criar("idsTipoDoc", idsTipoDoc) : null;
        Parametro parametroPcd = idsPcd != null && idsPcd.size() > 0 ? Parametro.criar("idsPcd", idsPcd) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissIni", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null && dtEmissao.size() > 0 ? Parametro.criar("dtEmissFin", dtEmissao[1]) : null;
        Parametro parametroDtEntradaSaidaIni = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaIni", dtEntradaSaida[0]) : null;
        Parametro parametroDtEntradaSaidaFin = dtEntradaSaida != null && dtEntradaSaida.size() > 0 ? Parametro.criar("dtEntradaSaidaFin", dtEntradaSaida[1]) : null;
        Parametro parametroEntidade = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTransp = idTransp != null && idTransp.size() > 0 ? Parametro.criar("idTransp", idTransp) : null;
        Parametro parametroItens = idsItens != null && idsItens.size() > 0 ? Parametro.criar("idsItens", idsItens) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroNcm = idsNcm != null && idsNcm.size() > 0 ? Parametro.criar("idsNcm", idsNcm) : null;
        Parametro parametroDepto = idsDepartamentos != null && idsDepartamentos.size() > 0 ? Parametro.criar("departamentos", idsDepartamentos) : null;
        Parametro parametroMPS = mps.size() > 0 && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;

        String sql = "SELECT abm01codigo, abm01descr, abg01codigo, abg01descr, aah01codigo, aah01nome, " +
                "abb01num, abd01codigo, abd01descr, abb01data, eaa01esData, abe01codigo, abe01na, aam06codigo, " +
                "eaa0103qtUso, eaa0103qtComl, eaa0103totDoc, abf10codigo, abf10nome "+
                "FROM eaa01  " +
                "INNER JOIN abd01 on abd01id = eaa01pcd  " +
                "INNER JOIN abb01 on abb01id = eaa01central  " +
                "INNER JOIN aah01 on aah01id = abb01tipo  " +
                "INNER JOIN abe01 as ent on ent.abe01id = abb01ent  " +
                "INNER JOIN eaa0103 on eaa0103doc = eaa01id  " +
                "LEFT JOIN eaa01039 ON eaa01039item = eaa0103id "+
                "LEFT JOIN eaa010391 ON eaa010391depto = eaa01039id " +
                "LEFT JOIN abf10 ON abf10id = eaa010391nat "+
                "INNER JOIN abm01 on abm01id = eaa0103item  " +
                "LEFT JOIN aam06 on aam06id = abm01umu  " +
                "LEFT JOIN abg01 on abg01id = eaa0103ncm  " +
                "WHERE eaa01clasDoc = " + Eaa01.CLASDOC_SRF + " " +
                "AND eaa01cancData IS NULL " +
                "AND eaa01nfestat <> 5 " +
                whereEmpresa +
                whereNumIni +
                whereNumFin +
                whereTipoDoc +
                wherePcd +
                whereDtEmissao +
                whereDtEntradaSaida +
                whereEntidade +
                whereES +
                whereItens +
                whereTransp +
                whereNcm +
                whereDepartamento +
                whereMPS +
                "ORDER BY abb01num, abm01codigo "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroEmpresa, parametroNumIni, parametroNumFin, parametroTipoDoc, parametroPcd, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroDtEntradaSaidaIni, parametroDtEntradaSaidaFin, parametroEntidade,
                parametroItens, parametroTransp, parametroNcm, parametroDepto, parametroMPS);

    }
}