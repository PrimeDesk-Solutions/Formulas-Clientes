package Atilatte.relatorios.scf;

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

public class SCF_Saldo_Dos_Lancamentos_Aprovador extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SCF - Saldo dos Lançamentos(Aprovador)";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
        filtrosDefault.put("impressao", "0");
        filtrosDefault.put("conciliacao", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idContaCorrente = getListLong("contaCorrente");
        LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
        Integer conciliacao = getInteger("conciliacao")
        boolean isSaltarPagina = get("isSaltarPagina");
        Integer impressao = getInteger("impressao")

        List<TableMap> dados = new ArrayList<>();
        params.put("TITULO_RELATORIO", "Lançamentos Financeiros");
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        List<TableMap> dab10s = obterDadosRelatorio(idContaCorrente, dataPeriodo, conciliacao);
        Integer codigoConta = 0;
        BigDecimal saldo = BigDecimal.ZERO;

        for (TableMap dab10 : dab10s) {
            TableMap tm = new TableMap();

            if(dab10.getInteger("dab01codigo") != codigoConta){
                saldo = BigDecimal.ZERO;
                Long contaCorrente = dab10.getLong("dab01id");

                BigDecimal saldoCC = buscarSaldoConta(contaCorrente, dataPeriodo[0]) ;

                saldoCC = saldoCC == BigDecimal.ZERO ? buscarSaldoInicial(contaCorrente,dataPeriodo[0]) : saldoCC;

                BigDecimal saldoInicial = buscarSaldoInicial(contaCorrente,dataPeriodo[0]);

                BigDecimal saldoAnterior = buscarSaldoAnterior(contaCorrente, dataPeriodo, conciliacao,saldoInicial);

                tm.put("valorInicial", saldoAnterior);
            }

            tm.put("codConta", dab10.getString("dab01codigo"));
            tm.put("nomeConta", dab10.getString("dab01nome"));
            tm.put("data", dab10.getDate("dab10data"));
            tm.put("valorLancamento", dab10.getBigDecimal("dab10valor"));
            tm.put("codEntidade", dab10.getString("abe01codigo"));
            tm.put("naEntidade", dab10.getString("abe01na"));
            tm.put("parcela", dab10.getString("abb01parcela"));
            tm.put("quita", dab10.getString("abb01quita"));
            tm.put("numDoc", dab10.getString("abb01num"));
            tm.put("daa01dtPgto", dab10.getDate("daa01dtPgto"));
            tm.put("abb01data", dab10.getDate("abb01data"));
            tm.put("aab10user", dab10.getString("aab10user"));
            tm.put("abb0103data", dab10.getDate("abb0103data"));

            if(dab10.getDate("dab1002dtconc") == null){
                tm.put("conciliacao", 'N');
            }else{
                tm.put("conciliacao", 'S');

            }

            if(saldo == BigDecimal.ZERO){
                saldo = tm.getBigDecimal_Zero("valorInicial");
            }


            if(dab10.getInteger("dab10mov").equals(0)){
                tm.put("saldo", saldo.add(dab10.getBigDecimal("dab10valor")));
                tm.put("movimentacao", "E")
            }else{
                tm.put("saldo", saldo.subtract(dab10.getBigDecimal("dab10valor")));
                tm.put("movimentacao", "S")
            }

            saldo = tm.getBigDecimal_Zero("saldo");

            codigoConta = dab10.getInteger("dab01codigo");

            dados.add(tm);
        }

        params.put("empresa", obterEmpresaAtiva().getAac10codigo() +"-"+obterEmpresaAtiva().getAac10na());
        params.put("titulo", "SCF - Saldo dos Lançamentos(Aprovador)" );

        if(impressao == 1 ) return gerarXLSX("SCF_Saldo_Dos_Lancamentos_Aprovador_Excel", dados)
        return gerarPDF("SCF_Saldo_Dos_Lancamentos_Aprovador_PDF", dados, "codigoConta", isSaltarPagina)
    }


    public List<TableMap> obterDadosRelatorio (List<Long> idContaCorrente, LocalDate[] dataPeriodo, Integer conciliacao)  {

        String whereConciliacao = conciliacao == 0 ? "and dab1002dtconc is null " : conciliacao == 1 ? "and dab1002dtconc is not null " : "";
        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
        String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";
        Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;

        String sql = " SELECT abb01num, daa01dtPgto, abb01data, aab10user, abb0103data, Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, Dab10.dab10cc, " +
                    " Dab10.dab10mov, Dab10.dab10historico, Dab10.dab10valor, dab1002dtconc, " +
                    " abe01codigo, abe01na, abb01parcela, abb01quita "+
                    " FROM Dab10 Dab10" +
                    " LEFT JOIN abb01 on abb01id = Dab10.dab10central "+
                    " LEFT JOIN abb0103 ON abb0103central = abb01id "+
                    " LEFT JOIN aab10 ON aab10id = abb0103user " +
                    " LEFT JOIN daa01 ON daa01central = abb01id "+
                    " LEFT JOIN abe01 on abe01id = abb01ent " +
                    " LEFT JOIN dab1002 on dab1002lct = dab10id "+
                    " LEFT join Dab01 Dab01 on Dab01.dab01id = dab1002cc "+
                    wherePeriodoData +
                    whereIdsContaCorrente +
                    whereConciliacao +
                    getSamWhere().getWherePadrao(" AND ", Dab10.class) +
                    " ORDER BY Dab01.dab01codigo,Dab10.dab10data, dab10id "


        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
        return receberDadosRelatorio;
    }
    private  BigDecimal buscarSaldoAnterior (Long idContaCorrente, LocalDate[] dataPeriodo, Integer conciliacao,BigDecimal saldoInicial)  {

        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " WHERE dab10.dab10data < '" + dataPeriodo[0] + "'": "";
        String whereIdsContaCorrente = " AND dab01.dab01id = :idContaCorrente "

        Parametro parametroCC = Parametro.criar("idContaCorrente", idContaCorrente);

        String sql = " SELECT dab10mov, dab10valor "+
                    " FROM Dab10 Dab10" +
                    " LEFT JOIN dab1002 ON dab1002lct = dab10id "+
                    " LEFT JOIN Dab01 Dab01 ON Dab01.dab01id = dab1002cc " +
                    wherePeriodoData +
                    whereIdsContaCorrente +
                    getSamWhere().getWherePadrao(" AND ", Dab10.class) +
                    " ORDER BY Dab01.dab01codigo,Dab10.dab10data, dab10id "


        List<TableMap> lancamentos = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroCC);

        BigDecimal saldo = saldoInicial;

        for(lancamento in lancamentos){
            if(lancamento.getInteger("dab10mov").equals(0)){
                saldo += lancamento.getBigDecimal("dab10valor")
            }else{
                saldo -= lancamento.getBigDecimal("dab10valor")
            }

        }


        return saldo;
    }



    private BigDecimal buscarSaldoInicial(Long idConta, LocalDate[] data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;

        BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.le(field, numMeses))
                .addWhere(Criterions.eq("dab0101mes", 0)).addWhere(Criterions.eq("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", idConta)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder("dab01.dab01codigo").get(ColumnType.BIG_DECIMAL);
        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
    }

    private BigDecimal buscarSaldoConta(Long idConta, LocalDate[] data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data[0] != null ? (data[0].getYear() * 12) + data[0].getMonthValue() : 1;

        BigDecimal valorSaldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.lt(field, numMeses))
                .addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", idConta)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);
        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo;
    }

}