package Silcon.relatorios.cgs

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abm70;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class CGS_Etiqueta_Modelo1 extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CGS - Etiqueta Modelo 1";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap();
        filtrosDefault.put("numeroEtiquetaInicial", "000000001");
        filtrosDefault.put("numeroEtiquetaFinal", "999999999");
        filtrosDefault.put("data", DateUtils.getStartAndEndMonth(MDate.date()));
        filtrosDefault.put("imprimir", "0");
        filtrosDefault.put("numeroDocumentoInicial", "000000001");
        filtrosDefault.put("numeroDocumentoFinal", "999999999");
        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        Integer numInicial = getInteger("numeroEtiquetaInicial");
        Integer numFinal = getInteger("numeroEtiquetaFinal");
        LocalDate[] data = getIntervaloDatas("data");
        Integer impressao = getInteger("imprimir");
        List<Integer> mps = getListInteger("mps");
        List<Long> itens = getListLong("itens");
        List<Long> ids = getListLong("abm70ids");

        List<TableMap> dados = new ArrayList<>();

        if(ids != null && ids.size() > 0){
            dados = buscarEtiquetasPorID(ids)
        } else {
            dados = buscarEtiquetas(numInicial, numFinal, data, impressao, mps, itens)
        }

        List<Long> abm70ids = new ArrayList<>();
        for (dado in dados){
            Long abm70id = dado.getLong("abm70id");
            abm70ids.add(abm70id);
        }

        gravarStatusImpressaoEtiquetas(abm70ids);

        params.put("caminhoLogo", "C:\\SAM-Servidor\\Formulas-Clientes\\src\\main\\resources\\Silcon\\relatorios\\cgs\\Logo Silcon.png")

        return gerarPDF("CGS_Etiqueta_Modelo1", dados);
    }
    private List<TableMap> buscarEtiquetasPorID(List<Long> ids){
        String sql = " SELECT abm70id, abm70num, abm70data, abm01descr, abm01codigo, abm01gtin, abm70qt, aam06codigo " +
                " FROM abm70 " +
                " INNER JOIN abm01 ON abm01id = abm70item " +
                " INNER JOIN aam05 ON aam05id = abm70modelo "+
                " LEFT JOIN aam06 ON aam06id = abm01umu " +
                " WHERE abm70id IN (:ids) " +
                " AND aam05codigo = '001' " +
                " ORDER BY abm70num ";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("ids", ids));
    }
    private List<TableMap> buscarEtiquetas( Integer numInicial,  Integer numFinal, LocalDate[] data, Integer impressao, List<Integer> mps, List<Long>itens){

        String whereNumInicial = numInicial != null && numFinal != null ? "AND abm70num BETWEEN :numInicial AND :numFinal " :
                                 numInicial != null && numFinal == null ? "AND abm70num >= :numInicial " :
                                 numInicial == null && numFinal != null  ? "AND abm70num <= :numFinal " : "";
        String whereData = data != null && data.size() > 0 ? "AND abm70data BETWEEN :dtInicial AND :dtFinal " : "";
        String whereImpressao = "AND abm70status = :impressao ";
        String whereMPS = mps != null && !mps.contains(-1) ? "AND abm01tipo IN (:mps) " : "";
        String whereItens = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";


        Parametro parametroNumInicial = numInicial != null ? Parametro.criar("numInicial", numInicial) : null;
        Parametro parametroNumFinal = numFinal != null ? Parametro.criar("numFinal", numFinal) : null;
        Parametro parametroDataIni = data != null && data.size() > 0 ? Parametro.criar("dtInicial", data[0]) : null;
        Parametro parametroDataFin = data != null && data.size() > 0 ? Parametro.criar("dtFinal", data[1]) : null;
        Parametro parametroImpressao = Parametro.criar("impressao", impressao);
        Parametro parametroMPS = mps != null && !mps.contains(-1) ? Parametro.criar("mps", mps) : null;
        Parametro parametroItens = itens != null && itens.size() > 0 ? Parametro.criar("itens", itens) : null;


        String sql = " SELECT abm70id, abm70num, abm70data, abm01descr, abm01codigo, abm01gtin, abm70qt, aam06codigo  " +
                " FROM abm70 " +
                " INNER JOIN abm01 ON abm01id = abm70item " +
                " INNER JOIN aam05 ON aam05id = abm70modelo "+
                " LEFT JOIN aam06 ON aam06id = abm01umu " +
                " WHERE TRUE " +
                " AND aam05codigo = '001' " +
                whereNumInicial +
                whereData +
                whereImpressao +
                whereMPS +
                whereItens +
                " ORDER BY abm70num "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametroNumInicial, parametroNumFinal, parametroDataIni, parametroDataFin, parametroImpressao, parametroMPS, parametroItens )
    }
    private void gravarStatusImpressaoEtiquetas(List<Long> abm70ids) {
        try {
            if(abm70ids == null || abm70ids.size() == 0) return;

            for(Long abm70id : abm70ids) {
                Abm70 abm70 = getSession().get(Abm70.class, "abm70id, abm70status", abm70id);
                abm70.setAbm70status(1);
                getSession().persist(abm70);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}