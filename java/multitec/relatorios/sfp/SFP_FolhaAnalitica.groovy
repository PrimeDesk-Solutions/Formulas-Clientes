package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abb11;
import sam.model.entities.ab.Abh21;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Folha Analítica
 * @author Lucas Eliel
 * @since 06/03/2019
 * @version 1.0
 */
//TODO Implementar o Citério de Seleção
public class SFP_FolhaAnalitica extends RelatorioBase{

    @Override
    public String getNomeTarefa() {
        return "SFP - Folha Analítica";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        filtrosDefault.put("isTrabalhador", true);
        filtrosDefault.put("isAutonomo", false);
        filtrosDefault.put("isProlabore", false);
        filtrosDefault.put("isTerceiros", false);
        filtrosDefault.put("calcFolha", true);
        filtrosDefault.put("calcFerias", true);
        filtrosDefault.put("calcRescisao", true);
        filtrosDefault.put("calcAdiantamento", false);
        filtrosDefault.put("calc13sal", false);
        filtrosDefault.put("calcPlr", false);
        filtrosDefault.put("calcOutros", false);
        filtrosDefault.put("trab", true);
        filtrosDefault.put("totSetor", true);
        filtrosDefault.put("totDepto", true);
        filtrosDefault.put("totDiv", true);
        filtrosDefault.put("totGeral", true);
        filtrosDefault.put("eveBCalc", true);
        filtrosDefault.put("eveNeutro", true);
        filtrosDefault.put("eveCalc", false);
        filtrosDefault.put("ordenamento", "0");
        LocalDate[] periodos = 	[MDate.date(), MDate.date()];
        filtrosDefault.put("periodos", periodos);
        filtrosDefault.put("tipoRel", "0");

        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsTrabalhadores = getListLong("trabalhadores");
        Set<Integer> tiposTrab = obterTipoTrabalhador();
        List<Long> idsDeptos = getListLong("departamentos");
        List<Long> idsCargos = getListLong("cargos");
        List<Long> idsSindicatos = getListLong("sindicatos");
        Set<Integer> tiposCalc = obterTiposCalculo();
        LocalDate[] periodos = getIntervaloDatas("periodos");
        Integer ordenacao = getInteger("ordenamento");
        Integer tipoRel = getInteger("tipoRel");
        String eventosNaoImprimir = getString("eventos") == null ? "": getString("eventos");

        String[] listaEventos = null;
        if (!eventosNaoImprimir.isEmpty()) {
            listaEventos = eventosNaoImprimir.split(",");
        }

        if (listaEventos != null) {
            for(int i = 0; i < listaEventos.length; i++) {
                String evento = listaEventos[i];
                evento = evento.trim();
                listaEventos[i] = evento;
            }
        }

        Set<Integer> tiposEveBC = new HashSet<Integer>();
        if((boolean) get("eveBCalc")) tiposEveBC.add(2);
        if((boolean) get("eveNeutro")) tiposEveBC.add(3);
        if((boolean) get("eveCalc")) tiposEveBC.add(4);

        Boolean chkImprimirTrabalhador = get("trab");
        Boolean chkImprimirDivisao = get("totDiv");
        Boolean chkImprimirDepartamento = get("totDepto");
        Boolean chkImprimirSetor = get("totSetor");
        Boolean chkImprimirTotalGeral = get("totGeral");

        String eveFeriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
        String eveFeriasPagas = getParametros(Parametros.FB_EVEPAGTOFERIASDESC);
        String eveAbonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
        String eveAbonoPago = getParametros(Parametros.FB_EVEPAGTOABONODESC);
        String eveAd13Liquido = getParametros(Parametros.FB_EVELIQADIANT13);
        String eveAd13Pago = getParametros(Parametros.FB_EVEPAGTO13SALDESC);
        String eveResLiquida = getParametros(Parametros.FB_EVERESLIQUIDA);
        String eveResPaga = getParametros(Parametros.FB_EVERESLIQUIDA);
        String eveFerNaoImpr = getParametros(Parametros.FB_EVELIQFERIAS);

        Aac10 aac10 = getVariaveis().getAac10();

        String endereco = null;
        if(aac10.getAac10endereco() != null) {
            if(aac10.getAac10numero() != null) {
                endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
            }else {
                endereco = aac10.getAac10endereco();
            }
            if(aac10.getAac10complem() != null) {
                endereco += " - " + aac10.getAac10complem();
            }
        }

        params.put("TITULO_RELATORIO", "Folha Analítica");
        params.put("EMP_RS", aac10.getAac10rs());
        params.put("EMP_COD", aac10.getAac10codigo());
        params.put("EMP_SFPTI1", aac10.getAac10ti());
        params.put("EMP_NI", aac10.getAac10ni());
        params.put("EMP_ENDERECO", endereco);
        params.put("EMP_CIDADE", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201nome() : null);
        params.put("EMP_UF", aac10.getAac10municipio() != null ? obterMunicipio(aac10.getAac10municipio().getIdValue()).getAag0201uf().getAag02uf() : null);
        params.put("PERIODO", "Período: " + DateUtils.formatDate(periodos[0]) + " a " + DateUtils.formatDate(periodos[1]));
        params.put("StreamSub1R1", carregarArquivoRelatorio("SFP_FolhaAnalitica_R1_S1"));
        params.put("StreamSub2R1", carregarArquivoRelatorio("SFP_FolhaAnalitica_R1_S2"));
        params.put("StreamSub3R1", carregarArquivoRelatorio("SFP_FolhaAnalitica_R1_S3"));
        params.put("StreamSub4R1", carregarArquivoRelatorio("SFP_FolhaAnalitica_R1_S2"));

        List<TableMap> listEventosRendDesc = new ArrayList<>();
        List<TableMap> listEventosBC = new ArrayList<>();
        List<TableMap> listEveTotalRendDesc = new ArrayList<>();
        List<TableMap> listEveTotalBC = new ArrayList<>();

        List<TableMap> folhaAnalitica = getDadosRelatorioFolhaAnalitica(listEventosRendDesc, listEventosBC, listEveTotalRendDesc, listEveTotalBC, idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, periodos, tiposTrab, tiposCalc, tiposEveBC,
                chkImprimirTrabalhador, chkImprimirDivisao, chkImprimirDepartamento, chkImprimirSetor, chkImprimirTotalGeral, ordenacao, eveFeriasLiquida, eveFeriasPagas, eveAbonoLiquido, eveAbonoPago, eveAd13Liquido, eveAd13Pago, eveResLiquida,
                eveResPaga, eveFerNaoImpr, listaEventos);

        TableMapDataSource dsPrincipal = new TableMapDataSource(folhaAnalitica);
        dsPrincipal.addSubDataSource("DsSub1R1", listEventosRendDesc, "keyDepto", "keyDepto");
        dsPrincipal.addSubDataSource("DsSub2R1", listEventosBC, "keyDepto", "keyDepto");
        dsPrincipal.addSubDataSource("DsSub3R1", listEveTotalRendDesc, "keyDepto", "keyDepto");
        dsPrincipal.addSubDataSource("DsSub4R1", listEveTotalBC, "keyDepto", "keyDepto");

        if(tipoRel == 1) {
            return gerarXLSX("SFP_FolhaAnalitica_R1", dsPrincipal);
        }else {
            return gerarPDF("SFP_FolhaAnalitica_R1", dsPrincipal);
        }
    }

    private Set<Integer> obterTipoTrabalhador(){
        Set<Integer> tiposTrab = new HashSet<>();

        if((boolean) get("isTrabalhador")) tiposTrab.add(0);
        if((boolean) get("isAutonomo")) tiposTrab.add(1);
        if((boolean) get("isProlabore")) tiposTrab.add(2);
        if((boolean) get("isTerceiros")) tiposTrab.add(3);

        if(tiposTrab.size() == 0) {
            tiposTrab.add(0);
            tiposTrab.add(1);
            tiposTrab.add(2);
            tiposTrab.add(3);
        }
        return tiposTrab;
    }

    private Set<Integer> obterTiposCalculo(){
        Set<Integer> calc = new HashSet<>();

        if((boolean) get("calcFolha")) calc.add(0);
        if((boolean) get("calcAdiantamento")) calc.add(1);
        if((boolean) get("calc13sal")) calc.add(2);
        if((boolean) get("calcFerias")) calc.add(3);
        if((boolean) get("calcRescisao")) calc.add(4);
        if((boolean) get("calcPlr")) calc.add(6);
        if((boolean) get("calcOutros")) calc.add(9);

        if(calc.size() == 0) {
            calc.add(0);
            calc.add(1);
            calc.add(2);
            calc.add(3);
            calc.add(4);
            calc.add(6);
            calc.add(9);
        }
        return calc;
    }

    private Aag0201 obterMunicipio(Long aac10municipio) {
        return getSession().createCriteria(Aag0201.class)
                .addJoin(Joins.fetch("aag0201uf"))
                .addWhere(Criterions.eq("aag0201id", aac10municipio))
                .get();
    }

    private String getParametros(Parametro param) {
        Aba01 aba01 = getSession().createCriteria(Aba01.class)
                .addWhere(Criterions.eq("aba01param", param.getParam()))
                .addWhere(Criterions.eq("aba01aplic", "FB"))
                .addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
                .get();

        String conteudo = null;
        if(aba01 != null) {
            conteudo = aba01.getAba01conteudo();
        }
        return conteudo;
    }

    public List<TableMap> findDadosFb0101sByFolhaAnalitica(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, LocalDate[] periodos, Set<Integer> tiposTrab,
                                                           Set<Integer> tiposCalc, Integer ordenacao, String[] eventosNaoImprimir) {

        String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
        String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
        String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
        String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
        String wherePeriodos = periodos != null ? getWhereDataInterval("WHERE", periodos, "fba0101dtCalc") : "";
        String whereEventosNaoImprimir = eventosNaoImprimir != null ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

        String ordem = null;
        if(ordenacao == 0) {
            ordem = " ORDER BY abb11codigo, abh80codigo ";
        }else if(ordenacao == 1) {
            ordem = " ORDER BY abb11codigo, abh80nome ";
        }else if(ordenacao == 2) {
            ordem = " ORDER BY abh80nome ";
        }else {
            ordem = " ORDER BY abh80codigo ";
        }

        String sql = "SELECT abb11codigo, abb11nome, abh80id, abh80codigo, abh80nome, abh80dtadmis, abh80cpf, abh80salario " +
                "FROM Fba01011 " +
                "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
                "INNER JOIN Abh21 ON abh21id = fba01011eve " +
                "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
                "INNER JOIN Abh80 ON abh80id = fba0101trab " +
                "INNER JOIN Abb11 ON abb11id = abh80depto " +
                "INNER JOIN Abh05 ON abh05id = abh80cargo " +
                "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
                wherePeriodos +" AND fba0101tpVlr IN (:tiposCalc) AND abh80tipo IN (:tiposTrab) " +
                whereTrabalhadores +
                whereDeptos +
                whereCargos +
                whereSindicatos +
                whereEventosNaoImprimir +
                getSamWhere().getWherePadrao("AND", Fba01.class) +
                " GROUP BY abb11codigo, abb11nome, abh80id, abh80codigo, abh80nome, abh80dtadmis, abh80cpf " +
                ordem;

        Query query = getSession().createQuery(sql);

        if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
        query.setParameter("tiposTrab", tiposTrab);
        if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
        if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
        if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
        if(eventosNaoImprimir != null) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);
        query.setParameter("tiposCalc", tiposCalc);

        return query.getListTableMap();
    }

    public TableMap findFba0101ByIdAndPeriodo(Long idAbh80, LocalDate[] periodos) {
        String wherePeriodos = periodos != null ? getWhereDataInterval("WHERE", periodos, "fba0101dtCalc") : "";

        String sql = "SELECT DISTINCT abh05codigo, abh05nome, aap03codigo  FROM Fba0101 " +
                "INNER JOIN Abh80 ON abh80id = fba0101trab " +
                "INNER JOIN Abh05 ON abh05id = abh80cargo " +
                "INNER JOIN Aap03 ON aap03id = abh05cbo "+
                wherePeriodos +" AND fba0101trab = "+idAbh80+" " +
                getSamWhere().getWherePadrao("AND", Abh80.class);

        Query query = getSession().createQuery(sql);

        return query.getUniqueTableMap();
    }

    public String findNomeByCodigo(String codigo) {
        String sql = "SELECT abb11nome FROM Abb11 WHERE UPPER(abb11codigo) = :codigo " + getSamWhere().getWherePadrao("AND", Abb11.class);
        Query query = getSession().createQuery(sql);
        query.setParameter("codigo", codigo.toUpperCase());
        return (String)query.getUniqueResult(ColumnType.STRING);
    }

    public List<TableMap> findDadosFba01011sEventosByFolhaAnalitica(List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, Long idAbh80, LocalDate[] periodos, Set<Integer> tiposCalc, Set<Integer> tiposEve,
                                                                    String eveFerNaoImpr, String[] eventosNaoImprimir) {

        String whereEve = eveFerNaoImpr != null ? "AND abh21codigo <> :eveFerNaoImpr " : "";
        String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
        String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
        String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
        String wherePeriodos = periodos != null ? getWhereDataInterval("WHERE", periodos, "fba0101dtCalc") : "";
        String whereEventosNaoImprimir = eventosNaoImprimir != null? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

        String sql = "SELECT abh21codigo, abh21nome, abh21tipo, abh80codigo, abb11codigo, abb11nome, SUM(CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, SUM(fba01011valor) as totalValor " +
                "FROM Fba01011 " +
                "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
                "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
                "INNER JOIN Abh21 ON abh21id = fba01011eve " +
                "INNER JOIN Abh80 ON abh80id = fba0101trab " +
                "INNER JOIN Abb11 ON abb11id = abh80depto " +
                "INNER JOIN Abh05 ON abh05id = abh80cargo " +
                "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
                wherePeriodos + " AND fba0101trab = :idAbh80 AND fba0101tpVlr IN (:tiposCalc) AND abh21tipo IN (:tiposEve) " + whereEve +
                whereDeptos +
                whereCargos +
                whereSindicatos +
                whereEventosNaoImprimir +
                getSamWhere().getWherePadrao("AND", Fba01.class) +
                " GROUP BY abh21codigo, abh21nome, abh21tipo, abh21codigo, abb11codigo, abb11nome, abh80codigo " +
                "HAVING SUM(fba01011valor) > 0 " +
                "ORDER BY abh21tipo, abh21codigo";

        Query query = getSession().createQuery(sql);

        query.setParameter("idAbh80", idAbh80);
        query.setParameter("tiposCalc", tiposCalc);
        query.setParameter("tiposEve", tiposEve);

        if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos)
        if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos)
        if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos)
        if(eveFerNaoImpr != null) query.setParameter("eveFerNaoImpr", eveFerNaoImpr);
        if(eventosNaoImprimir != null) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);

        return query.getListTableMap();
    }

    public List<TableMap> findDadosFba01011sEventosTotalizadosByFolhaAnalitica(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, String codAbb11, LocalDate[] periodos, Set<Integer> tiposTrab,
                                                                               Set<Integer> tiposCalc, Set<Integer> tiposEve, String eveFerNaoImpr, String[] eventosNaoImprimir) {

        String whereEve = eveFerNaoImpr != null ? "AND abh21codigo <> '"+eveFerNaoImpr+"' " : "";
        String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
        String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
        String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
        String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
        String wherePeriodos = periodos != null ? getWhereDataInterval("WHERE", periodos, "fba0101dtCalc") : "";
        String whereDepto = null;
        String whereEventosNaoImprimir = eventosNaoImprimir != null ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

        if(codAbb11.equals("*")) {
            whereDepto = "";
        }else {
            whereDepto = "AND abb11codigo LIKE '"+codAbb11.toUpperCase()+"%"+"' ";
        }

        String sql = "SELECT abh21codigo, abh21nome, abh21tipo, SUM(CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, SUM(fba01011valor) as totalValor " +
                "FROM Fba01011 " +
                "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
                "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
                "INNER JOIN Abh21 ON abh21id = fba01011eve " +
                "INNER JOIN Abh80 ON abh80id = fba0101trab " +
                "INNER JOIN Abb11 ON abb11id = abh80depto " +
                "INNER JOIN Abh05 ON abh05id = abh80cargo " +
                "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
                wherePeriodos +" AND fba0101tpVlr IN (:tiposCalc) AND abh80tipo IN (:tiposTrab) AND abh21tipo IN (:tiposEve) " + whereEve + whereDepto +
                whereTrabalhadores +
                whereDeptos +
                whereCargos +
                whereSindicatos +
                whereEventosNaoImprimir +
                getSamWhere().getWherePadrao("AND", Fba01.class) +
                " GROUP BY abh21codigo, abh21nome, abh21tipo " +
                " HAVING SUM(fba01011valor) > 0 " +
                " ORDER BY abh21tipo, abh21codigo";

        Query query = getSession().createQuery(sql);

        query.setParameter("tiposTrab", tiposTrab);
        query.setParameter("tiposCalc", tiposCalc);
        query.setParameter("tiposEve", tiposEve);
        if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
        if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
        if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
        if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
        if(eventosNaoImprimir != null) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);

        return query.getListTableMap();
    }

    public BigDecimal findFba01011ValorEventoParaZerarFeriasAndRescisao(List<Long> idsTrabalhadores, List<Long>  idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, Long idAbh80, Set<Integer> tiposTrab, String codAbb11, LocalDate[] periodos,
                                                                        int tipoCalc, String codAbh21, String[] eventosNaoImprimir) {

        String where = idAbh80 != null ? "AND fba0101trab = :idAbh80 " : (codAbb11.equals("*") ? "" : "AND abb11codigo LIKE :codAbb11 ");
        String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
        String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
        String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
        String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
        String wherePeriodos = periodos != null ? getWhereDataInterval("WHERE", periodos, "fba0101dtCalc") : "";
        String whereEventosNaoImprimir = eventosNaoImprimir != null ? " AND abh21codigo NOT IN (:eventosNaoImprimir) " : "";

        String sql = "SELECT SUM(fba01011valor) FROM Fba01011 " +
                "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
                "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
                "INNER JOIN Abh21 ON abh21id = fba01011eve " +
                "INNER JOIN Abh80 ON abh80id = fba0101trab " +
                "INNER JOIN Abb11 ON abb11id = abh80depto " +
                "INNER JOIN Abh05 ON abh05id = abh80cargo " +
                "INNER JOIN Abh03 ON abh03id = abh80sindSindical " +
                wherePeriodos + " AND abh80tipo IN (:tiposTrab) AND fba0101tpVlr = :tipoCalc AND abh21codigo = :codAbh21 AND fba01011valor > 0 " + where +
                whereTrabalhadores +
                whereDeptos +
                whereCargos +
                whereSindicatos +
                whereEventosNaoImprimir +
                getSamWhere().getWherePadrao("AND", Fba01.class);

        Query query = getSession().createQuery(sql);

        if(idAbh80 != null) {
            query.setParameter("idAbh80", idAbh80);
        }else if(!codAbb11.equals("*")) {
            query.setParameter("codAbb11", codAbb11.toUpperCase()+"%");
        }

        query.setParameter("tiposTrab", tiposTrab);
        query.setParameter("tipoCalc", tipoCalc);
        query.setParameter("codAbh21", codAbh21);
        if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
        if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
        if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
        if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
        if(eventosNaoImprimir != null) query.setParameter("eventosNaoImprimir", eventosNaoImprimir);

        return query.getUniqueResult(ColumnType.BIG_DECIMAL);
    }

    public Abh21 findByUniqueKey(String abh21codigo){
        return getSession().createCriteria(Abh21.class)
                .addWhere(Criterions.eq("abh21codigo", abh21codigo))
                .setMaxResults(1).get();
    }

    public List<TableMap> getDadosRelatorioFolhaAnalitica(List<TableMap> listEventosRendDesc, List<TableMap> listEventosBC, List<TableMap> listEveTotalRendDesc, List<TableMap> listEveTotalBC, List<Long> idsTrabalhadores, List<Long> idsDeptos,
                                                          List<Long> idsCargos, List<Long> idsSindicatos, LocalDate[] periodos, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Set<Integer> tiposEveBC, Boolean chkImprimirTrabalhador, Boolean chkImprimirDivisao, Boolean chkImprimirDepartamento,
                                                          Boolean chkImprimirSetor, Boolean chkImprimirTotalGeral, Integer ordenacao, String eveFeriasLiquida, String eveFeriasPagas, String eveAbonoLiquido, String eveAbonoPago, String eveAd13Liquido, String eveAd13Pago, String eveResLiquida,
                                                          String eveResPaga, String eveFerNaoImpr, String[] eventosNaoImprimir) {

        List<TableMap> listValores = findDadosFb0101sByFolhaAnalitica(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, periodos, tiposTrab, tiposCalc, ordenacao, eventosNaoImprimir);
        List<TableMap> listMapValores = new ArrayList<>();
        List<TableMap> listMapPrincipalValores = new ArrayList<>();

        boolean imprimeTotais = (ordenacao == 0 || ordenacao == 1);

        int numTrabSetor = 0;
        int numTrabDep = 0;
        int numTrabDiv = 0;
        int numTrabTotal = 0;

        Set<String> codAbh80s = new HashSet<String>();
        if(listValores != null && listValores.size() > 0) {
            int i = 0;
            for(TableMap rsValores : listValores) {

                String codigoSetor = chkImprimirSetor ? rsValores.getString("abb11codigo") : null;
                String codigoDepartamento = chkImprimirDepartamento ?  rsValores.getString("abb11codigo").substring(0, 4) : null;
                String codigoDivisao = chkImprimirDivisao ? rsValores.getString("abb11codigo").substring(0, 2) : null;

                String codAbh80 = rsValores.getString("abh80codigo");
                if(!codAbh80s.contains(codAbh80)) {
                    codAbh80s.add(codAbh80);
                }
                numTrabSetor++;

                //Se a opção "imprimir trabalhador" estiver flegada insere no mapa de valores os cálculos por trabalhador.
                if(chkImprimirTrabalhador) {
                    TableMap mapValores = new TableMap();
                    String keyDepto = rsValores.getString("abh80codigo") + "/" + rsValores.getString("abb11codigo");

                    mapValores.put("keyDepto", keyDepto);
                    mapValores.put("abb11codigo", rsValores.getString("abb11codigo"));
                    mapValores.put("abb11nome", rsValores.getString("abb11nome"));
                    mapValores.put("abh80id", rsValores.getLong("abh80id"));
                    mapValores.put("abh80codigo", rsValores.getString("abh80codigo"));
                    mapValores.put("abh80nome", rsValores.getString("abh80nome"));
                    mapValores.put("abh80dtadmis", rsValores.getDate("abh80dtadmis"));
                    mapValores.put("abh80cpf", rsValores.getString("abh80cpf"));
                    mapValores.put("abh80salario", rsValores.getBigDecimal("abh80salario"));

                    String codAbh05 = null;
                    String nomeAbh05 = null;
                    String cbo = null;

                    TableMap fba0101 = findFba0101ByIdAndPeriodo(rsValores.getLong("abh80id"), periodos);
                    if(fba0101 != null && fba0101.getString("abh05codigo") != null) {
                        codAbh05 = fba0101.getString("abh05codigo");
                        nomeAbh05 = fba0101.getString("abh05nome");
                        cbo = fba0101.getString("aap03codigo") != null ? cbo = fba0101.getString("aap03codigo") : null;
                    }

                    mapValores.put("abh05codigo", codAbh05);
                    mapValores.put("abh05nome", nomeAbh05);
                    mapValores.put("abh05cbo", cbo);

                    String setor = codigoSetor != null ?  codigoSetor + " - " + rsValores.getString("abb11nome") : "";
                    String departamento = codigoDepartamento != null ? codigoDepartamento + " - " + findNomeByCodigo(codigoDepartamento) : "";
                    String divisao = codigoDivisao != null ? codigoDivisao + " - " + findNomeByCodigo(codigoDivisao) : "";

                    if(!imprimeTotais) {
                        setor = "";
                    }
                    if(!imprimeTotais) {
                        departamento = "";
                    }
                    if(!imprimeTotais) {
                        divisao = "";
                    }

                    mapValores.put("setor", setor);
                    mapValores.put("departamento", departamento);
                    mapValores.put("divisao", divisao);

                    listMapValores.add(mapValores);
                }

                //Compõe as linhas de totalização e o numero de trabalhadores por setor, departamento, divisão e total geral.
                boolean ultimoRegistro = (i == listValores.size() - 1);
                if(ultimoRegistro || !codigoSetor.equals(listValores.get(i + 1).getString("abb11codigo"))) {
                    if(chkImprimirSetor) {
                        TableMap mapValores = new TableMap();
                        comporLinhaTotalizacao(mapValores, codigoSetor, codigoDepartamento, codigoDivisao, numTrabSetor);
                        listMapValores.add(mapValores);
                    }
                    numTrabDep += numTrabSetor;
                    numTrabSetor = 0;
                    codAbh80s.clear();

                    if(ultimoRegistro || !codigoDepartamento.equals(listValores.get(i + 1).getString( "abb11codigo").substring(0, 4))) {
                        if(chkImprimirDepartamento) {
                            TableMap mapValores = new TableMap();
                            comporLinhaTotalizacao(mapValores, codigoSetor, codigoDepartamento, codigoDivisao, numTrabDep);
                            listMapValores.add(mapValores);
                        }
                        numTrabDiv += numTrabDep;
                        numTrabDep = 0;

                        if(ultimoRegistro || !codigoDivisao.equals(listValores.get(i + 1).getString( "abb11codigo").substring(0, 2))) {
                            if(chkImprimirDivisao) {
                                TableMap mapValores = new TableMap();
                                comporLinhaTotalizacao(mapValores, codigoSetor, codigoDepartamento, codigoDivisao, numTrabDiv);
                                listMapValores.add(mapValores);
                            }
                            numTrabTotal += numTrabDiv;
                            numTrabDiv = 0;

                            if(ultimoRegistro && chkImprimirTotalGeral) {
                                TableMap mapValores = new TableMap();
                                comporLinhaTotalizacao(mapValores, null, null, null, numTrabTotal);
                                listMapValores.add(mapValores);
                            }
                        }
                    }
                }
                i++;
            }
        }


        //Percorre o mapa de valores preenchendo os mapas de eventos(SubDataSet). Soma os rendimentos e descontos de cada cálculo e seta no mapa principal.
        Set<Integer> tiposEveRD = new HashSet<Integer>();
        tiposEveRD.add(0);
        tiposEveRD.add(1);
        for(TableMap mapValores : listMapValores) {

            Long idAbh80 = mapValores.getLong("abh80id");
            String codAbb11 = mapValores.getString("abb11codigo");
            BigDecimal totalRend = new BigDecimal(0);
            BigDecimal totalDesc = new BigDecimal(0);

            String keyDepto = mapValores.getString("abh80codigo") + "/" + mapValores.getString("abb11codigo");
            boolean isTotal = !mapValores.getString("keyDepto").equals(keyDepto);
            if(!isTotal) {
                //Eventos de rendimento e desconto.
                List<TableMap> lstEventosRendDesc = findDadosFba01011sEventosByFolhaAnalitica(idsDeptos, idsCargos, idsSindicatos, idAbh80, periodos, tiposCalc, tiposEveRD, eveFerNaoImpr, eventosNaoImprimir);
                if(lstEventosRendDesc != null && lstEventosRendDesc.size() > 0) {
                    for(TableMap rsEventosRendDesc : lstEventosRendDesc) {

                        TableMap mapEventosRendDesc = new TableMap(); //Mapa para os eventos de desconto e rendimento.

                        mapEventosRendDesc.put("keyDepto", keyDepto);
                        mapEventosRendDesc.put("evento", rsEventosRendDesc.getString("abh21codigo") + " - " + rsEventosRendDesc.getString("abh21nome"));
                        mapEventosRendDesc.put("depto", rsEventosRendDesc.getString("abb11codigo"));
                        mapEventosRendDesc.put("ref", rsEventosRendDesc.getBigDecimal("totalRef"));

                        if(rsEventosRendDesc.getInteger("abh21tipo") == 0) {
                            mapEventosRendDesc.put("rendimento", rsEventosRendDesc.getBigDecimal("totalValor"));
                            totalRend = totalRend.add(rsEventosRendDesc.getBigDecimal("totalValor"));
                        }else {
                            mapEventosRendDesc.put("desconto", rsEventosRendDesc.getBigDecimal("totalValor"));
                            totalDesc = totalDesc.add(rsEventosRendDesc.getBigDecimal("totalValor"));
                        }

                        listEventosRendDesc.add(mapEventosRendDesc); //Retorno da lista de Eventos Rend Desc
                    }
                }

                //Eventos de Base de cálculo.
                if(tiposEveBC != null && tiposEveBC.size() > 0) {
                    List<TableMap> lstEventosBC = findDadosFba01011sEventosByFolhaAnalitica(idsDeptos, idsCargos, idsSindicatos, idAbh80, periodos, tiposCalc, tiposEveBC, eveFerNaoImpr, eventosNaoImprimir);
                    if(lstEventosBC != null && lstEventosBC.size() > 0) {
                        for(TableMap rsEventosBC : lstEventosBC) {

                            TableMap mapEventosBC = new TableMap(); //Mapa para os eventos de base de cálculo.

                            mapEventosBC.put("keyDepto", keyDepto);
                            mapEventosBC.put("evento", rsEventosBC.getString("abh21codigo") + " - " + rsEventosBC.getString("abh21nome"));
                            mapEventosBC.put("valor", rsEventosBC.getBigDecimal("totalValor"));
                            mapEventosBC.put("tipo", rsEventosBC.getInteger("abh21tipo"));

                            listEventosBC.add(mapEventosBC); //Retorno da lista de Eventos BC
                        }
                    }
                }
            }else {
                //Eventos totalizados de rendimento e desconto.
                List<TableMap> lstEveTotalRendDesc = findDadosFba01011sEventosTotalizadosByFolhaAnalitica(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, codAbb11, periodos, tiposTrab, tiposCalc, tiposEveRD, eveFerNaoImpr, eventosNaoImprimir);

                if(lstEveTotalRendDesc != null && lstEveTotalRendDesc.size() > 0) {
                    for(TableMap rsEveTotalRendDesc : lstEveTotalRendDesc) {

                        TableMap mapEveTotalRendDesc = new TableMap(); // Mapa para os eventos totalizados de rendimento e desconto.

                        mapEveTotalRendDesc.put("keyDepto", mapValores.getString("keyDepto"));
                        mapEveTotalRendDesc.put("evento", rsEveTotalRendDesc.getString("abh21codigo") + " - " + rsEveTotalRendDesc.getString("abh21nome"));
                        mapEveTotalRendDesc.put("ref", rsEveTotalRendDesc.getBigDecimal("totalRef"));

                        if(rsEveTotalRendDesc.getInteger("abh21tipo") == 0) {
                            mapEveTotalRendDesc.put("rendimento", rsEveTotalRendDesc.getBigDecimal("totalValor"));
                            totalRend = totalRend.add(rsEveTotalRendDesc.getBigDecimal("totalValor"));
                        }else {
                            mapEveTotalRendDesc.put("desconto", rsEveTotalRendDesc.getBigDecimal("totalValor"));
                            totalDesc = totalDesc.add(rsEveTotalRendDesc.getBigDecimal("totalValor"));
                        }

                        listEveTotalRendDesc.add(mapEveTotalRendDesc); //Retorno da lista de Eventos Total Rend Desc
                    }
                }

                //Eventos totalizados de base de cálculo.
                if(tiposEveBC != null && tiposEveBC.size() > 0) {
                    List<TableMap> lstEveTotalBC = findDadosFba01011sEventosTotalizadosByFolhaAnalitica(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, codAbb11, periodos, tiposTrab, tiposCalc, tiposEveBC, eveFerNaoImpr, eventosNaoImprimir);
                    if(lstEveTotalBC != null && lstEveTotalBC.size() > 0) {
                        for(TableMap rsEveTotalBC : lstEveTotalBC) {

                            TableMap mapEveTotalBC = new TableMap(); //Mapa para os eventos totalizados de base de cálculo.

                            mapEveTotalBC.put("keyDepto", mapValores.getString("keyDepto"));
                            mapEveTotalBC.put("evento", rsEveTotalBC.getString("abh21codigo") + " - " + rsEveTotalBC.getString("abh21nome"));
                            mapEveTotalBC.put("valor", rsEveTotalBC.getBigDecimal("totalValor"));
                            mapEveTotalBC.put("tipo", rsEveTotalBC.getInteger("abh21tipo"));

                            listEveTotalBC.add(mapEveTotalBC); //Retorno da lista de Eventos Total BC
                        }
                    }
                }
            }

            if(tiposCalc.contains(3)) {
                //Verifica se existe evento de férias líquida para zerar com férias pagas.
                Abh21 abh21FeriasPagas = findByUniqueKey(eveFeriasPagas);
                BigDecimal valorFerias = findFba01011ValorEventoParaZerarFeriasAndRescisao(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idAbh80, tiposTrab, codAbb11, periodos, 3, eveFeriasLiquida, eventosNaoImprimir);
                if(abh21FeriasPagas != null && valorFerias != null && valorFerias.compareTo(new BigDecimal(0)) > 0) {
                    if(!isTotal) {
                        TableMap mapEventosRendDesc = new TableMap();
                        comporEventoPago(mapEventosRendDesc, keyDepto, abh21FeriasPagas.getAbh21codigo(), abh21FeriasPagas.getAbh21nome(), codAbb11, valorFerias);
                        listEventosRendDesc.add(mapEventosRendDesc);
                    }else {
                        TableMap mapEveTotalRendDesc = new TableMap();
                        comporEventoPago(mapEveTotalRendDesc, mapValores.getString("keyDepto"), abh21FeriasPagas.getAbh21codigo(), abh21FeriasPagas.getAbh21nome(), codAbb11, valorFerias);
                        listEveTotalRendDesc.add(mapEveTotalRendDesc);
                    }
                    totalDesc = totalDesc.add(valorFerias);
                }

                //Verifica se existe evento de abono líquido para zerar com abono pago.
                Abh21 abh21AbonoPago = findByUniqueKey(eveAbonoPago);
                BigDecimal valorAbono = findFba01011ValorEventoParaZerarFeriasAndRescisao(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idAbh80, tiposTrab, codAbb11, periodos, 3, eveAbonoLiquido, eventosNaoImprimir);
                if(abh21AbonoPago != null && valorAbono != null && valorAbono.compareTo(new BigDecimal(0)) > 0) {
                    if(!isTotal) {
                        TableMap mapEventosRendDesc = new TableMap();
                        comporEventoPago(mapEventosRendDesc, keyDepto, abh21AbonoPago.getAbh21codigo(), abh21AbonoPago.getAbh21nome(), codAbb11, valorAbono);
                        listEventosRendDesc.add(mapEventosRendDesc);
                    }else {
                        TableMap mapEveTotalRendDesc = new TableMap();
                        comporEventoPago(mapEveTotalRendDesc, mapValores.getString("keyDepto"), abh21AbonoPago.getAbh21codigo(), abh21AbonoPago.getAbh21nome(), codAbb11, valorAbono);
                        listEveTotalRendDesc.add(mapEveTotalRendDesc);
                    }
                    totalDesc = totalDesc.add(valorAbono);
                }

                //Verifica se existe evento de adiantamento de 13º salário líquido para zerar com adiant. 13º pago.
                Abh21 abh21Ad13Pago = findByUniqueKey(eveAd13Pago);
                BigDecimal valorAd13 = findFba01011ValorEventoParaZerarFeriasAndRescisao(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idAbh80, tiposTrab, codAbb11, periodos, 3, eveAd13Liquido, eventosNaoImprimir);
                if(abh21Ad13Pago != null && valorAd13 != null && valorAd13.compareTo(new BigDecimal(0)) > 0) {
                    if(!isTotal) {
                        TableMap mapEventosRendDesc = new TableMap();
                        comporEventoPago(mapEventosRendDesc, keyDepto, abh21Ad13Pago.getAbh21codigo(), abh21Ad13Pago.getAbh21nome(), codAbb11, valorAd13);
                        listEventosRendDesc.add(mapEventosRendDesc);
                    }else {
                        TableMap mapEveTotalRendDesc = new TableMap();
                        comporEventoPago(mapEveTotalRendDesc, mapValores.getString("keyDepto"), abh21Ad13Pago.getAbh21codigo(), abh21Ad13Pago.getAbh21nome(), codAbb11, valorAd13);
                        listEveTotalRendDesc.add(mapEveTotalRendDesc);
                    }
                    totalDesc = totalDesc.add(valorAd13);
                }
            }

            if(tiposCalc.contains(4)) {
                //Verifica se existe evento de rescisão líquido para zerar com rescisão paga.
                Abh21 abh21ResPaga = findByUniqueKey(eveResPaga);
                BigDecimal valorRes = findFba01011ValorEventoParaZerarFeriasAndRescisao(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, idAbh80, tiposTrab, codAbb11, periodos, 4, eveResLiquida, eventosNaoImprimir);
                if(abh21ResPaga != null && valorRes != null && valorRes.compareTo(new BigDecimal(0)) > 0) {
                    if(!isTotal) {
                        TableMap mapEventosRendDesc = new TableMap();
                        comporEventoPago(mapEventosRendDesc, keyDepto, abh21ResPaga.getAbh21codigo(), abh21ResPaga.getAbh21nome(), codAbb11, valorRes);
                        listEventosRendDesc.add(mapEventosRendDesc);
                    }else {
                        TableMap mapEveTotalRendDesc = new TableMap();
                        comporEventoPago(mapEveTotalRendDesc, mapValores.getString("keyDepto"), abh21ResPaga.getAbh21codigo(), abh21ResPaga.getAbh21nome(), codAbb11, valorRes);
                        listEveTotalRendDesc.add(mapEveTotalRendDesc);
                    }
                    totalDesc = totalDesc.add(valorRes);
                }
            }

            mapValores.put("totalRend", totalRend);
            mapValores.put("totalDesc", totalDesc);
            mapValores.put("totalLiquido", totalRend.subtract(totalDesc));

            listMapPrincipalValores.add(mapValores);
        }

        return listMapPrincipalValores;
    }

    private void comporLinhaTotalizacao(TableMap mapa, String codigoSetor, String codigoDepartamento, String codigoDivisao, int numTrabs) {
        String setor = (codigoSetor == null ? "" : codigoSetor + " -  " + findNomeByCodigo(codigoSetor) + " ");
        String departamento = (codigoDepartamento == null ? "" : codigoDepartamento + " - " + findNomeByCodigo(codigoDepartamento));
        String divisao = (codigoDivisao == null ? "" : codigoDivisao + " - " + findNomeByCodigo(codigoDivisao));

        mapa.put("setor", setor);
        mapa.put("departamento", departamento);
        mapa.put("divisao", divisao);
        mapa.put("numTrabs", numTrabs);

        //Cria a chave para linkar com os mapas dos eventos de totalização. (*) --> todos os centros de custo.
        String depto = codigoSetor != null ? codigoSetor : (codigoDepartamento != null ? codigoDepartamento : (codigoDivisao != null ? codigoDivisao : "*"));
        mapa.put("abb11codigo", depto);
        mapa.put("keyDepto", depto);
        if(depto.equals("*")) mapa.put("divisao", "Total Geral");
    }

    private void comporEventoPago(TableMap mapa, String key, String codAbh21, String nomeAbh21, String codAbb11, BigDecimal valor) {
        mapa.put("keyDepto", key);
        mapa.put("evento", codAbh21 + " - " + nomeAbh21);
        mapa.put("depto", codAbb11);
        mapa.put("ref", new BigDecimal(0));
        mapa.put("desconto", valor);
    }

}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEZvbGhhIEFuYWzDrXRpY2EiLCJ0aXBvIjoicmVsYXRvcmlvIn0=