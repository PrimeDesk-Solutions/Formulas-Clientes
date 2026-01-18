package Silcon.relatorios.scf;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_Resumo_Saldos_Conta_Corrente extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SCF - Resumo de Conta Corrente";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {

        def data = LocalDate.now()

        Map<String, Object> filtros = new HashMap();
        filtros.put("dtIni", data.format(DateTimeFormatter.ofPattern("MM/yyyy")))
        filtros.put("dtFim", data.format(DateTimeFormatter.ofPattern("MM/yyyy")))
        filtros.put("isContaInativa", false);
        return Utils.map("filtros", filtros);
    }

    @Override
    public DadosParaDownload executar() {
        List<TableMap> dados = new ArrayList<>();

        List<Long> idContaCorrente = getListLong("contaCorrente");
        LocalDate dtIni = DateUtils.parseDate("01/" + getString("dtIni"))
        LocalDate dtFim = DateUtils.getStartAndEndMonth(DateUtils.parseDate("01/" + getString("dtFim")))[1]
        LocalDate[] dataPeriodo = [dtIni, dtFim]

        boolean isContaAtiva = get("isContaInativa");

        params.put("TITULO_RELATORIO", "Resumo de Conta Corrente");
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("MM/yyyy")).toString());

        List<TableMap> dab01s = obterDadosRelatorio(idContaCorrente, isContaAtiva);
        for (TableMap dab01 : dab01s) {
            TableMap tm = new TableMap();
            tm.put("dab10id", dab01.getLong("dab10id"));
            tm.put("dab01codigo", dab01.getString("dab01codigo"));
            tm.put("dab01nome", dab01.getString("dab01nome"));

            BigDecimal valorSaldo = buscarSaldoAnterior(dab01.getLong("dab01id"), dataPeriodo[0]);
            if (valorSaldo == null) {
                valorSaldo = buscarSaldoInicial(dab01.getLong("dab01id"), dataPeriodo[0]);
            }
            tm.put("anterior", valorSaldo);
            BigDecimal entrada = buscarTotalEntSai(dab01.getLong("dab01id"), dataPeriodo[0], dataPeriodo[1], Dab10.MOV_ENTRADA)
            tm.put("entrada", entrada);
            BigDecimal saida = buscarTotalEntSai(dab01.getLong("dab01id"), dataPeriodo[0], dataPeriodo[1], Dab10.MOV_SAIDA)
            tm.put("saida", saida);
            BigDecimal atual = valorSaldo.add(entrada).subtract(saida);
            tm.put("atual", atual);

            dados.add(tm)
        }

        return gerarPDF("SCF_ResumoSaldosContaCorrente", dados)
    }

    private BigDecimal buscarTotalEntSai(Long dab01id, LocalDate dataInicial, LocalDate dataFinal, Integer mov) {
        BigDecimal valorSaldo = session.createCriteria(Dab10.class)
                .addFields("SUM(dab10.dab10valor) as dab10valor")
                .addJoin(Joins.join("dab10cc").left(false).alias("dab01"))
                .addWhere(samWhere.getCritPadrao(Dab10.class))
                .addWhere(Criterions.eq("dab10mov", mov))
                .addWhere(Criterions.eq("dab10cc", dab01id))
                .addWhere(Criterions.ge("dab10data", dataInicial))
                .addWhere(Criterions.le("dab10data", dataFinal))
                .setGroupBy(" GROUP BY  Dab10.dab10mov, Dab01.dab01codigo, Dab01.dab01nome")
                .setOrder(" Dab01.dab01codigo").get(ColumnType.BIG_DECIMAL);

        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
    }

    private BigDecimal buscarSaldoInicial(Long dab01id, LocalDate[] data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
        BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.le(field, numMeses))
                .addWhere(Criterions.eq("dab0101mes", 0)).addWhere(Criterions.eq("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder("dab01.dab01codigo").get(ColumnType.BIG_DECIMAL);
        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
    }

    private BigDecimal buscarSaldoAnterior(Long dab01id, LocalDate[] data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
        BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.lt(field, numMeses))
                .addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);
        return valorSaldo
    }

    private BigDecimal buscarSaldoAtual(Long dab01id, LocalDate[] data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;
        BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.le(field, numMeses))
                .addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);
        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
    }

    public List<TableMap> obterDadosRelatorio(List<Long> idContaCorrente, boolean isContaAtiva)  {
        String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " AND dab01.dab01id IN (:idContaCorrente) ": "";
        String whereSomenteContaAtiva = !isContaAtiva ? " AND dab01.dab01di IS NULL ": "";
        Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;

        String sql = " SELECT Dab01.dab01codigo, Dab01.dab01nome, Dab01.dab01id " +
                " FROM Dab01 dab01 " +
                getSamWhere().getWherePadrao("WHERE", Dab01.class) +
                whereIdsContaCorrente +
                whereSomenteContaAtiva +
                " GROUP BY Dab01.dab01codigo, Dab01.dab01nome, Dab01.dab01id " +
                " ORDER BY Dab01.dab01codigo "

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
        return receberDadosRelatorio;
    }

}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFJlc3VtbyBkZSBDb250YSBDb3JyZW50ZSAtIExDUiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==