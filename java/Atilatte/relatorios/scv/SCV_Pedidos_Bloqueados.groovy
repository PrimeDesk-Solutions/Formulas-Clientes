package Atilatte.relatorios.scv

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCV_Pedidos_Bloqueados extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCV - Pedidos Bloqueados";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("numeroInicial", '1');
        filtrosDefault.put("numeroFinal", '99999999');
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numInicial = getInteger("numeroInicial");
        Integer numFinal = getInteger("numeroFinal");
        List<Long> entidades = getListLong("entidade");
        List<Long> redespacho = getListLong("redespacho");
        List<Long> representantes = getListLong("representante");
        LocalDate[] dtEmissao = getIntervaloDatas("dataEmissao");
        Integer impressao = getInteger("impressao");

        List<TableMap> dados = buscarDadosRelatorio(numInicial, numFinal, entidades, redespacho, representantes, dtEmissao);

        params.put("titulo", "SCV - Pedidos Bloqueados");
        params.put("empresa", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());


        if (impressao == 1) return gerarXLSX("SCV_Pedidos_Bloqueados_Excel", dados);

        return gerarPDF("SCV_Pedidos_Bloqueados_PDF", dados)

    }

    private buscarDadosRelatorio(Integer numInicial, Integer numFinal, List<Long> entidades, List<Long> redespacho, List<Long> representantes, LocalDate[] dtEmissao){

        String whereStatus = "WHERE eaa01bloqueado = 1 ";
        String whereCancData = "AND eaa01cancData IS NULL ";
        String whereEmpresa = "AND eaa01gc = :idEmpresa ";
        String whereNumero = numInicial != null && numFinal != null ? "AND abb01num BETWEEN :numInicial AND :numFinal " : "";
        String whereEntidades = entidades != null && entidades.size() > 0 ? "AND ent.abe01id IN (:entidades) " : "";
        String whereRedespacho = redespacho != null && redespacho.size() > 0 ? "AND redesp.abe01id IN (:redespacho) " : "";
        String whereRepresentantes = representantes != null && representantes.size() > 0 ? "AND (eaa01rep0 IN (:representantes) OR eaa01rep1 IN (:representantes) OR eaa01rep2 IN (:representantes)) " : "";
        String whereDtEmissao = dtEmissao != null ? "AND abb01data BETWEEN :dtInicial AND :dtFinal " : "";
        String orderBy = "ORDER BY ent.abe01codigo, abb01num"

        Parametro parametroNumInicial = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumFinal = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroEntidades = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null;
        Parametro parametroRedespacho = redespacho != null && redespacho.size() > 0 ? Parametro.criar("redespacho", redespacho) : null;
        Parametro parametroRepresentante = representantes != null && representantes.size() > 0 ? Parametro.criar("representantes", representantes) : null;
        Parametro parametroDtEmissaoIni = dtEmissao != null ? Parametro.criar("dtInicial", dtEmissao[0]) : null;
        Parametro parametroDtEmissaoFin = dtEmissao != null ? Parametro.criar("dtFinal", dtEmissao[1]) : null;
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());


        String sql = "SELECT abb01num AS numDoc,abb01data AS dataEmissao, ent.abe01codigo AS codEntidade, ent.abe01na AS naEntidade, " +
                        "rep0.abe01codigo AS codRep0, rep0.abe01na AS naRep0, " +
                        "rep1.abe01codigo AS codRep1, rep1.abe01na AS naRep1, " +
                        "rep2.abe01codigo AS codRep2, rep2.abe01na AS naRep2, " +
                        "redesp.abe01codigo AS codRedesp, redesp.abe01na AS naRedesp, " +
                        "'Bloqueado' AS status, eaa01totDoc AS totDoc, CAST(eaa01json ->> 'peso_bruto' AS numeric(18,6)) AS pesoBruto " +
                        "FROM eaa01 " +
                        "INNER JOIN abb01 ON abb01id = eaa01central " +
                        "INNER JOIN abe01 AS ent ON ent.abe01id = abb01ent " +
                        "LEFT JOIN abe01 AS rep0 ON rep0.abe01id = eaa01rep0 " +
                        "LEFT JOIN abe01 AS rep1 ON rep1.abe01id = eaa01rep1 " +
                        "LEFT JOIN abe01 AS rep2 ON rep2.abe01id = eaa01rep2 " +
                        "LEFT JOIN eaa0102 ON eaa0102doc = eaa01id " +
                        "LEFT JOIN abe01 AS redesp ON redesp.abe01id = eaa0102redespacho " +
                        whereStatus +
                        whereCancData +
                        whereEmpresa +
                        whereNumero +
                        whereEntidades +
                        whereRedespacho +
                        whereRepresentantes +
                        whereDtEmissao+
                        orderBy;

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumInicial, parametroNumFinal, parametroEntidades, parametroRedespacho, parametroRepresentante, parametroDtEmissaoIni, parametroDtEmissaoFin, parametroEmpresa );


    }
}
//meta-sis-eyJkZXNjciI6IlNDViAtIFBlZGlkb3MgQmxvcXVlYWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==