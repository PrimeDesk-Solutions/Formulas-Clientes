package Silcon.relatorios.scf

import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils


import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_LancamentosPorNatureza extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos Por Natureza";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("numeroInicial", "000000001");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("impressao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        Integer numDocIni = getInteger("numeroInicial");
        Integer numDocFin = getInteger("numeroFinal");
        List<Long> tipoDoc = getListLong("tipo");
        List<Long> plf = getListLong("plf");
        List<Long> contasCorrentes = getListLong("contaCorrente");
        String codNatIni = getString("naturezaIni");
        String codNatFin = getString("naturezaFin");
        List<Long> entidades = getListLong("entidade");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        Integer impressao = getInteger("impressao");
        Long idEmpresa = obterEmpresaAtiva().getAac10id();

        List<TableMap> dados = buscarLancamentos(numDocIni, numDocFin, tipoDoc, plf, codNatIni, codNatFin, entidades, dataEmissao, idEmpresa, contasCorrentes);

        if (impressao == 1) return gerarXLSX("SCF_LancamentosPorNatureza_Excel", dados);
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("titulo", "SCF - Lançamentos Por Natureza");

        return gerarPDF("SCF_LancamentosPorNatureza_PDF", dados);


    }

    private List<TableMap> buscarLancamentos(Integer numDocIni, Integer numDocFin, List<Long> tipoDoc, List<Long> plf, String codNatIni, String codNatFin, List<Long> entidades, LocalDate[] dataEmissao, Long idEmpresa, List<Long> contasCorrentes) {

        // Data Inicial - Final
        LocalDate dataIni = null;
        LocalDate dataFin = null;
        if (dataEmissao != null) {
            dataIni = dataEmissao[0];
            dataFin = dataEmissao[1];
        }

        String whereNumDoc = "WHERE abb01num BETWEEN :numDocIni AND :numDocFin ";
        String whereEmpresa = "AND dab10gc = :idEmpresa ";
        String whereContas = contasCorrentes != null && contasCorrentes.size() > 0 ? "AND dab01id IN (:contasCorrentes)" : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? "AND aah01id IN (:tipoDoc)" : "";
        String wherePlf = plf != null && plf.size() > 0 ? "AND dab10plf in (:plf) " : "";
        String whereNatureza = codNatIni != null && codNatFin != null ? "AND abf10codigo BETWEEN :codNatIni AND :codNatFin " : codNatIni != null && codNatFin == null ? "AND abf10codigo >= :codNatIni " : codNatIni == null && codNatFin != null ? "AND abf10codigo <= :codNatFin " : "";
        String whereEntidade = entidades != null && entidades.size() > 0 ? "AND abe01id IN (:entidades)" : "";
        String whereDataEmissao = dataIni != null && dataFin != null ? "AND abb01data BETWEEN :dataIni AND :dataFin " : "";

        Parametro parametroNumDocIni = Parametro.criar("numDocIni", numDocIni);
        Parametro parametroNumDocFin = Parametro.criar("numDocFin", numDocFin);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", idEmpresa);
        Parametro parametroContas = contasCorrentes != null && contasCorrentes.size() > 0 ? Parametro.criar("contasCorrentes", contasCorrentes) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;
        Parametro parametroPlf = plf != null && plf.size() > 0 ? Parametro.criar("plf", plf) : null;
        Parametro parametroNaturezaIni = codNatIni != null ? Parametro.criar("codNatIni", codNatIni) : null;
        Parametro parametroNaturezaFin = codNatFin != null ? Parametro.criar("codNatFin", codNatFin) : null;
        Parametro parametroEntidade = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroDataIni = dataIni != null ? Parametro.criar("dataIni", dataIni) : null;
        Parametro parametroDataFin = dataFin != null ? Parametro.criar("dataFin", dataFin) : null;

        String sql = "SELECT dab01codigo AS codCC, dab01nome AS nomeCC, abf10codigo AS codNatureza,abf10nome AS nomeNatureza, " +
                " dab10data AS dtLancamento,abe01codigo AS codEnt, abe01na AS naEnt, aah01codigo AS codTipoDoc, aah01nome AS descrTipoDoc, " +
                " abb01num AS numDoc, abb01parcela AS parcela, abb01quita AS quita, " +
                " CASE WHEN dab10mov = 0 THEN '0-Entrada' ELSE '1-Saída' END AS movimentacao, dab10011valor AS valorDoc, dab10valor AS valorPago " +
                " FROM dab10 " +
                " LEFT JOIN dab1002 ON dab1002lct = dab10id " +
                " LEFT JOIN dab01 ON dab01id = dab1002cc " +
                " LEFT JOIN dab1001 ON dab1001lct = dab10id " +
                " LEFT JOIN dab10011 ON dab10011depto = dab1001id " +
                " LEFT JOIN abf10 ON abf10id = dab10011nat " +
                " LEFT JOIN abb01 ON abb01id = dab10central " +
                " LEFT JOIN abe01 ON abe01id = abb01ent " +
                " LEFT JOIN aah01 ON aah01id = abb01tipo " +
                whereNumDoc +
                whereTipoDoc +
                wherePlf +
                whereNatureza +
                whereEntidade +
                whereDataEmissao +
                whereEmpresa +
                whereContas +
                "ORDER BY abb01num,aah01codigo, abf10codigo,abb01parcela, abb01data";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumDocIni, parametroNumDocFin, parametroEmpresa, parametroContas, parametroTipoDoc, parametroPlf, parametroNaturezaIni, parametroNaturezaFin, parametroEntidade, parametroDataIni, parametroDataFin);

    }
}