package Silcon.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abf10
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro;

public class SCF_ResumoLancamentosNaturezas extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Resumo dos Lançamentos por Natureza";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
        filtrosDefault.put("periodo", datas);
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        boolean exibeTotalGeral = get("totalGeral");
        List<Long> idContaCorrente = getListLong("contaCorrente");
        List<Long> idNaturezas = getListLong("naturezas");
        LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
        List<TableMap> dados = new ArrayList<>();
        params.put("TITULO_RELATORIO", "Resumo dos Lançamentos por Naturezas");
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        Integer[] qtDigGrau = verificarGraus("ABF10", "ESTRNATUREZA");

        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal saldoAnterior = BigDecimal.ZERO;
        String codigoConta = null;

        List<TableMap> dadosRel =   obterDadosRelatorio(idNaturezas, idContaCorrente, dataPeriodo, exibeTotalGeral)
        List<String> graus = new ArrayList<>();

        Integer controle = 0
        for (TableMap mapDados : dadosRel) {
            for(int k = 0; k < qtDigGrau.size(); k++) {
                Integer indexOfTm = null;

                String grauNatureza = mapDados.getString("abf10codigo").substring(0, qtDigGrau[k]);
                TableMap tm = new TableMap();
                if (!Utils.isEmpty(dados)) {
                    TableMap find = null;
                    if (exibeTotalGeral) {
                        find = dados.stream().filter({f -> f.getString("abf10codigo").equalsIgnoreCase(mapDados.getString("abf10codigo"))}).findFirst().orElse(null);
                    } else {
                        find = dados.stream().filter({f -> f.getString("abf10codigo").equalsIgnoreCase(mapDados.getString("abf10codigo"))}).filter({f -> f.getString("dab01codigo").equalsIgnoreCase(mapDados.getString("dab01codigo"))}).findFirst().orElse(null);
                    }
                    if (find != null) {
                        tm = find;
                        indexOfTm = dados.indexOf(tm);
                    }
                }

                if (tm.getBigDecimal("valorNatureza") == null) tm.put("valorNatureza", BigDecimal.ZERO);


                if (!exibeTotalGeral) {
                    if(mapDados.getString("dab01codigo") == null) interromper("Existem lançamentos financeiros sem conta corrente.");
                    tm.put("codigoConta", mapDados.getString("dab01codigo"));
                    if (!tm.getString("codigoConta").equalsIgnoreCase(codigoConta)) {
                        saldo = BigDecimal.ZERO;
                        saldoAnterior = BigDecimal.ZERO;
                        graus.clear();
                        saldoAnterior = buscarSaldoAnteriorConta(mapDados.getLong("dab01id"), dataPeriodo, null);
                        if (saldoAnterior == BigDecimal.ZERO) {
                            saldoAnterior = buscarSaldoInicial(mapDados.getLong("dab01id"), null);
                        }
                    }
                } else {
                    //Sem filtro de conta corrente, busca o saldo todo
                    String whereIdsContaCorrente = !Utils.isEmpty(idContaCorrente) ? " and dab01.dab01id IN (:idContaCorrente)": "";
                    String sqlSaldoContas = " SELECT dab01id FROM Dab01 " + getSamWhere().getWherePadrao("WHERE", Dab01.class) + whereIdsContaCorrente;
                    List<Long> dab01ids = getAcessoAoBanco().obterListaDeLong(sqlSaldoContas, !Utils.isEmpty(idContaCorrente) ? Parametro.criar("idContaCorrente", idContaCorrente) : null);
                    saldoAnterior = BigDecimal.ZERO;
                    if(controle == 0){ // Garante que busca o saldo todo apenas uma vez
                        for (dab01id in dab01ids) {
                            BigDecimal saldoTotal = buscarSaldoAnteriorConta(dab01id, dataPeriodo, null);
                            saldoAnterior = saldoAnterior.add(saldoTotal);
                        }
                    }

                    controle++
                }

                if (mapDados.getString("abf10codigo") != grauNatureza) {
                    if (graus.contains(grauNatureza)) continue;
                    Abf10 natureza = buscarNatPeloCodigo(grauNatureza);

                    List<TableMap> totalNatureza = null;
                    if (!exibeTotalGeral) {
                        totalNatureza = buscarTotalNatureza(mapDados.getLong("dab01id"), idNaturezas, dataPeriodo, qtDigGrau[k], grauNatureza)
                    } else {
                        totalNatureza = buscarTotalNaturezaGeral(idContaCorrente,idNaturezas, dataPeriodo, qtDigGrau[k], grauNatureza)
                    }

                    for (TableMap map : totalNatureza) {
                        tm.put("dab01codigo", mapDados.getString("dab01codigo"));
                        tm.put("dab01nome", mapDados.getString("dab01nome"));
                        tm.put("abf10codigo", natureza == null ? mapDados.getString("abf10codigo"): natureza.abf10codigo);
                        tm.put("abf10nome", natureza == null ? mapDados.getString("abf10nome"): natureza.abf10nome);
                        if (map.getInteger("dab10mov").equals(0)){
                            tm.put("valorNatureza", tm.getBigDecimal("valorNatureza").add(map.getBigDecimal("valorNatureza")));
                        } else {
                            tm.put("valorNatureza", tm.getBigDecimal("valorNatureza").subtract(map.getBigDecimal("valorNatureza")));
                        }
                    }
                } else {
                    tm.put("dab01codigo", mapDados.getString("dab01codigo"));
                    tm.put("dab01nome", mapDados.getString("dab01nome"));
                    tm.put("abf10codigo", mapDados.getString("abf10codigo"));
                    tm.put("abf10nome", mapDados.getString("abf10nome"));
                    if (mapDados.getInteger("dab10mov").equals(0)){
                        BigDecimal valorNatureza = tm.getBigDecimal("valorNatureza") != null ? tm.getBigDecimal("valorNatureza") : BigDecimal.ZERO;
                        tm.put("valorNatureza", valorNatureza.add(mapDados.getBigDecimal("valorNatureza")));
                        saldo = saldo.add(mapDados.getBigDecimal("valorNatureza"));
                    } else {
                        BigDecimal valorNatureza = tm.getBigDecimal("valorNatureza") != null ? tm.getBigDecimal("valorNatureza") : BigDecimal.ZERO;
                        tm.put("valorNatureza", valorNatureza.add(mapDados.getBigDecimal("valorNatureza") * -1));
                        saldo = saldo.subtract(mapDados.getBigDecimal("valorNatureza"));
                    }
                }

                tm.put("valorTotalPeri", saldo);
                tm.put("valorTotalNat", saldoAnterior.add(saldo));
                tm.put("saldoAnterior", saldoAnterior);
                codigoConta = mapDados.getString("dab01codigo");
                if (indexOfTm == null) dados.add(tm);
                graus.add(grauNatureza);
            }
        }

        if (exibeTotalGeral) {
            return gerarPDF("SCF_ResumoLancamentosNaturezas_R2", dados)
        } else {
            return gerarPDF("SCF_ResumoLancamentosNaturezas", dados)
        }
    }

    private List<TableMap> obterDadosRelatorio(List<Long> idNatureza, List<Long> idContaCorrente, LocalDate[] dataPeriodo, boolean exibeTotalGeral) {

        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
        String whereIdNatureza = idNatureza != null && idNatureza.size() > 0 ? " and abf10.abf10id IN (:idNatureza)": "";
        String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";

        Parametro paramCC = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
        Parametro paramNatureza = idNatureza != null && idNatureza.size() > 0 ? Parametro.criar("idNatureza", idNatureza) : null;

        String select = null;
        String orderBy = null;
        String groupBy = null;
        if (exibeTotalGeral) {
            select = " SELECT abf10.abf10codigo, abf10.abf10nome, dab10.dab10mov, SUM(Dab10011.Dab10011valor) AS valorNatureza ";
            orderBy = " ORDER BY abf10.abf10codigo, abf10.abf10nome ";
            groupBy = " GROUP BY abf10.abf10codigo, abf10.abf10nome, dab10.dab10mov ";
        } else {
            select = " SELECT dab01.dab01id, dab01.dab01codigo, dab01.dab01nome, abf10.abf10codigo, abf10.abf10nome, dab10.dab10mov, SUM(Dab10011.Dab10011valor) AS valorNatureza ";
            orderBy = " ORDER BY dab01.dab01codigo, abf10.abf10codigo ";
            groupBy = " GROUP BY dab01.dab01id, dab01.dab01codigo, dab01.dab01nome, abf10.abf10codigo, abf10.abf10nome, dab10.dab10mov ";
        }

        String sql = select +
                " FROM Dab10011 Dab10011 " +
                " INNER JOIN Dab1001 dab1001 ON dab1001.dab1001id = dab10011.dab10011depto " +
                " INNER JOIN Dab10 dab10 ON dab10.dab10id = dab1001.dab1001lct" +
                " LEFT JOIN Abf20 abf20 ON Abf20.abf20id = dab10.dab10plf" +
                " LEFT JOIN dab1002 on dab1002lct = dab10id "+
                " LEFT JOIN dab01 dab01 ON dab01.dab01id = dab1002cc" +
                " LEFT JOIN Abf10 abf10 ON abf10.abf10id = dab10011.dab10011nat" +
                wherePeriodoData +
                whereIdNatureza +
                whereIdsContaCorrente +
                getSamWhere().getWherePadrao("AND", Dab10.class) +
                groupBy +
                orderBy;

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramNatureza);
        return receberDadosRelatorio;
    }

    private List<TableMap> buscarTotalNatureza(Long idContaCorrente, List<Long> idNatureza, LocalDate[] dataPeriodo, Integer grau, String codNat) {

        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " WHERE dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
        String whereIdsContaCorrente = idContaCorrente != null  ? " AND dab01.dab01id IN (:idContaCorrente)": "";
        String whereCodNatureza = grau != null ? " AND SUBSTRING(abf10codigo, 1, " + grau + ") = :codigo " : "";
        String whereIdNatureza = idNatureza != null && idNatureza.size() > 0 ? " AND abf10.abf10id IN (:idNatureza)": "";

        Parametro paramCC = idContaCorrente != null  ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
        Parametro paramCodNat = grau != null ? Parametro.criar("codigo", codNat) : null;
        Parametro paramNatureza = idNatureza != null && idNatureza.size() > 0 ? Parametro.criar("idNatureza", idNatureza) : null;

        String sql = " SELECT SUM(Dab10011.Dab10011valor) AS valorNatureza, dab10.dab10mov" +
                " FROM Dab10011 Dab10011 " +
                " INNER JOIN Dab1001 dab1001 ON dab1001.dab1001id = dab10011.dab10011depto " +
                " INNER JOIN Dab10 dab10 ON dab10.dab10id = dab1001.dab1001lct " +
                " LEFT JOIN Abf20 abf20 ON Abf20.abf20id = dab10.dab10plf " +
                " LEFT JOIN dab1002 on dab1002lct = dab10id "+
                " LEFT JOIN dab01 dab01 ON dab01.dab01id = dab1002cc " +
                " INNER JOIN Abf10 abf10 ON abf10.abf10id = dab10011.dab10011nat " +
                wherePeriodoData +
                whereIdsContaCorrente +
                whereCodNatureza +
                whereIdNatureza +
                getSamWhere().getWherePadrao("AND", Dab10.class) +
                " GROUP BY dab10.dab10mov";

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramCodNat, paramNatureza);
        return receberDadosRelatorio;
    }

    private List<TableMap> buscarTotalNaturezaGeral(List<Long> idContaCorrente, List<Long> idNatureza, LocalDate[] dataPeriodo, Integer grau, String codNat) {

        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " WHERE dab10.dab10data >= '" + dataPeriodo[0] + "' AND dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
        String whereIdsContaCorrente = idContaCorrente != null  ? " AND dab01.dab01id IN (:idContaCorrente)": "";
        String whereCodNatureza = grau != null ? " AND SUBSTRING(abf10codigo, 1, " + grau + ") = :codigo " : "";
        String whereIdNatureza = idNatureza != null && idNatureza.size() > 0 ? " AND abf10.abf10id IN (:idNatureza)": "";

        Parametro paramCC = idContaCorrente != null  ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
        Parametro paramCodNat = grau != null ? Parametro.criar("codigo", codNat) : null;
        Parametro paramNatureza = idNatureza != null && idNatureza.size() > 0 ? Parametro.criar("idNatureza", idNatureza) : null;

        String sql = " select SUM(Dab10011.Dab10011valor) AS valorNatureza, dab10.dab10mov" +
                " FROM Dab10011 Dab10011 " +
                " INNER JOIN Dab1001 dab1001 ON dab1001.dab1001id = dab10011.dab10011depto " +
                " INNER JOIN Dab10 dab10 ON dab10.dab10id = dab1001.dab1001lct " +
                " LEFT JOIN Abf20 abf20 ON Abf20.abf20id = dab10.dab10plf " +
                " LEFT JOIN dab1002 ON dab1002lct = dab10id "+
                " LEFT JOIN dab01 dab01 ON dab01.dab01id = dab1002cc " +
                " INNER JOIN Abf10 abf10 ON abf10.abf10id = dab10011.dab10011nat " +
                wherePeriodoData +
                whereIdsContaCorrente +
                whereCodNatureza +
                whereIdNatureza +
                getSamWhere().getWherePadrao("AND", Dab10.class) +
                " GROUP BY dab10.dab10mov";

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramCC, paramCodNat, paramNatureza);
        return receberDadosRelatorio;
    }
    private BigDecimal buscarSaldoAnteriorConta(Long idContas, LocalDate[] dtLancamentos, List<Long> idsEmpresas){
        BigDecimal saldoInicial = buscarSaldoInicial(idContas, idsEmpresas);

        BigDecimal entradas = obterTotalLancamentos(dtLancamentos, idContas, 0);
        BigDecimal saidas = obterTotalLancamentos(dtLancamentos, idContas, 1);

        BigDecimal saldoAnterior = (saldoInicial + entradas) - saidas;

        return saldoAnterior
    }

    private BigDecimal buscarSaldoInicial(Long idContas, List<Long> idsEmpresas){
        String whereContas = "AND dab0101cc = :idContas ";
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "AND dab01gc IN (:idsEmpresa) " : "AND dab01gc = :idEmpresa ";

        Parametro parametroContas = Parametro.criar("idContas", idContas);
        Parametro parametroEmpresa = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = "SELECT SUM(dab0101saldo) "+
                "FROM dab01 " +
                "INNER JOIN dab0101 ON dab0101cc = dab01id "+
                "WHERE dab0101mes = 0 AND dab0101ano = 0 "+
                whereContas +
                whereEmpresa;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroContas, parametroEmpresa);
    }

    private BigDecimal obterTotalLancamentos(LocalDate[] dtLancamentos, Long idContas, Integer mov){
        String whereContas = "AND dab1002cc = :idContas ";
        String whereMov = "AND dab10mov = :mov ";
        String whereDtInicial = "AND dab10data < :dtInicial";

        Parametro parametroContas = Parametro.criar("idContas", idContas);
        Parametro parametroMov = Parametro.criar("mov", mov);
        Parametro parametroDtInicial = Parametro.criar("dtInicial", dtLancamentos[0]);

        String sql = "SELECT SUM(dab1002valor) " +
                    "FROM dab10 " +
                    "INNER JOIN dab1002 ON dab1002lct = dab10id " +
                    "WHERE TRUE " +
                    whereContas +
                    whereMov +
                    whereDtInicial;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroContas, parametroMov, parametroDtInicial);

    }

    private List<TableMap> buscarTotalNatureza(List<Long> idNaturezas, LocalDate[] dataPeriodo, Integer grau, String codNat) {
        return  buscarTotalNatureza(null, idNaturezas, dataPeriodo, grau, codNat);
    }


    private Abf10 buscarNatPeloCodigo(String abf10codigo){
        return session.createQuery(" SELECT abf10id, abf10codigo, abf10nome",
                "  FROM Abf10",
                "  WHERE LOWER(abf10codigo) = :abf10codigo",
                samWhere.getWherePadrao("AND", Abf10.class))
                .setParameter("abf10codigo", abf10codigo.toLowerCase())
                .getUniqueResult(ColumnType.ENTITY);
    }

    private Integer[] verificarGraus(String aplic, String param) {
        String parametros = getAcessoAoBanco().buscarParametro(param, aplic)

        int qtGrau;
        int[] qtDig;
        int tamanhoMaxGrau = 0;

        if (parametros != null) {
            StringTokenizer strToken = new StringTokenizer(parametros, "|");
            qtGrau = strToken.countTokens();

            if(qtGrau < 2 || qtGrau > 12)throw new ValidacaoException("A estrutura de código de contas deve ser no mínimo 2 e no máximo 12 graus.");

            qtDig = new int[qtGrau];

            int i = 0;
            while(strToken.hasMoreTokens()){
                int digitos = strToken.nextToken().length();
                if(i > 0){
                    qtDig[i] = qtDig[i-1] + digitos;
                }else{
                    qtDig[i] = digitos;
                }
                i++;
                tamanhoMaxGrau += digitos;
            }
        }

        return qtDig;
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFJlc3VtbyBkb3MgTGFuw6dhbWVudG9zIFBvciBOYXR1cmV6YSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==