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

public class SCF_LancamentosFinanceiros extends RelatorioBase {

    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        return criarFiltros("periodo", DateUtils.getStartAndEndMonth(MDate.date()), "imprimir", "1");

    }

    @Override
    public DadosParaDownload executar() {

        List<Long> idContaCorrente = getListLong("contaCorrente");
        LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
        Integer imprimir = getInteger("imprimir")
        boolean isSaltarPagina = get("isSaltarPagina");

        List<TableMap> dados = new ArrayList<>();
        params.put("TITULO_RELATORIO", "Lançamentos");
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        List<TableMap> dab10s = obterDadosRelatorio(idContaCorrente, dataPeriodo);
        String codigoConta = "0";
        BigDecimal saldo = BigDecimal.ZERO;
        for (TableMap dab10 : dab10s) {
            TableMap tm = new TableMap();
            if (tm.getBigDecimal("SALDOINICIAL") == null) tm.put("SALDOINICIAL", BigDecimal.ZERO);

            tm.put("dab10id", dab10.getLong("dab10id"));
            tm.put("codigoConta", dab10.getString("dab01codigo"));
            if (tm.getString("codigoConta") != codigoConta) {
                saldo = BigDecimal.ZERO;
                BigDecimal valorSaldo = buscarSaldoInicial(dab10.getLong("dab01id"), dataPeriodo[0]);
                tm.put("SALDOINICIAL", valorSaldo);
            }


            tm.put("aac10na", dab10.getString("aac10na"));

            tm.put("dab01codigo", dab10.getString("dab01codigo"));
            tm.put("dab01nome", dab10.getString("dab01nome"));

            tm.put("dab10data", dab10.getDate("dab10data"));
            tm.put("dab10historico", dab10.getString("dab10historico"));
            tm.put("dab10mov", dab10.getInteger("dab10mov"));
            tm.put("dab10valor", dab10.getBigDecimal("dab10valor"));

            if (saldo == BigDecimal.ZERO) {
                tm.put("SALDO", tm.getBigDecimal("SALDOINICIAL"));
                saldo = tm.getBigDecimal("SALDO");
            }

            if (tm.getInteger("dab10mov").equals(0)) {
                tm.put("SALDO", saldo.add(tm.getBigDecimal("dab10valor")));
            } else {
                tm.put("SALDO", saldo.subtract(tm.getBigDecimal("dab10valor")));
            }
            saldo = tm.getBigDecimal("SALDO");


            codigoConta = dab10.getString("dab01codigo")

            dados.add(tm)
        }

        if(imprimir == 0) {
            return gerarXLSX("SCF_LancamentosXLS", dados)
        }else {
            return gerarPDF("SCF_Lancamentos", dados, "codigoConta", isSaltarPagina)
        }

    }

    private BigDecimal buscarSaldoInicial(Long dab01id, LocalDate data) {


        BigDecimal valorSaldo = null


        valorSaldo = buscarValorInicialDoMes(dab01id, data) + (somarValorEntrada(dab01id, data) - somarValorSaida(dab01id, data))


        Parametro paramId = dab01id != null ? Parametro.criar("id", dab01id) : null

        return valorSaldo == null ? BigDecimal.ZERO : valorSaldo


    }

    private BigDecimal somarValorEntrada(Long dab01id, LocalDate data) {

        String whereId = dab01id != null ? " and dab01id in (:id) " : ""
        LocalDate diaAnterior = data.getDayOfMonth() != 1 ? (data.minusDays(1)) : data

        String sql = " SELECT distinct sum(dab10valor) " +
                " FROM dab10 " +
                " INNER JOIN dab1002 ON dab1002lct = dab10id "+
                " INNER JOIN dab01 ON dab1002cc = dab01id " +
                obterWherePadrao("dab10011", "where") +
                " AND dab10mov = '0' " +
                " AND dab10data between '" +
                data.getYear() + "-" + data.getMonthValue() + "-01' and '" +
                diaAnterior + "' " +
                whereId;

        Parametro paramId = dab01id != null ? Parametro.criar("id", dab01id) : null

        return getAcessoAoBanco().obterBigDecimal(sql, paramId)

    }

    private BigDecimal somarValorSaida(Long dab01id, LocalDate data) {

        String whereId = dab01id != null ? " AND dab01id IN (:id) " : ""
        LocalDate diaAnterior = data.getDayOfMonth() != 1 ? (data.minusDays(1)) : data

        String sql = " SELECT DISTINCT SUM(dab10valor) " +
                " FROM dab10 " +
                " INNER JOIN dab1002 ON dab1002lct = dab10id "+
                " INNER JOIN dab01 on dab1002cc = dab01id " +
                obterWherePadrao("dab10011", "WHERE") +
                " AND dab10mov = '1' " +
                " AND dab10data BETWEEN '" + data.getYear() + "-" + data.getMonthValue() + "-01' AND '" +
                diaAnterior + "' " +
                whereId

        Parametro paramId = dab01id != null ? Parametro.criar("id", dab01id) : null

        return getAcessoAoBanco().obterBigDecimal(sql, paramId)

    }

    private BigDecimal buscarValorInicialDoMes(Long dab01id, LocalDate data) {

        Integer mesAnterior = data.getMonthValue() - 1
        BigDecimal valor = 0

        while(valor == 0 || mesAnterior < 0) { //Verificar o valor inicial antes do mes selecionado

            String whereId = dab01id != null ? " and dab01id in (:id) " : ""
            String whereMes = mesAnterior != 1 ? " and dab0101mes = '" + mesAnterior + "' " : " and dab0101mes = 0"
            String whereAno = mesAnterior > 1 ? " and dab0101ano = '" + data.getYear() + "' " : " and dab0101ano = 0 "

            String sql = " SELECT dab0101saldo " +
                    " FROM dab01 " +
                    " INNER JOIN dab0101 on dab0101cc = dab01id " +
                    obterWherePadrao("dab01", "WHERE") +
                    whereAno +
                    whereMes +
                    whereId

            Parametro paramId = dab01id != null ? Parametro.criar("id", dab01id) : null

            valor = getAcessoAoBanco().obterBigDecimal(sql, paramId)

            if (valor != 0 || mesAnterior < 0) {
                return valor
            }
            else {
                mesAnterior = mesAnterior - 1
            }
        }
    }

    public List<TableMap> obterDadosRelatorio ( List<Long> idContaCorrente, LocalDate[] dataPeriodo)  {

        String wherePeriodoData = " WHERE dab10.dab10data >= '" + dataPeriodo[0] + "' AND dab10.dab10data <= '" + dataPeriodo[1] + "'"
        String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " AND dab01.dab01id IN (:idContaCorrente)": "";
        Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;


        String sql = " SELECT aac10.aac10na, Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, " +
                " Dab10.dab10cc, Dab10.dab10mov, Dab10.dab10historico, Dab10.dab10valor" +
                " FROM Dab10 Dab10" +
                " INNER JOIN dab1002 ON dab1002lct = dab10id "+
                " INNER JOIN dab01 on dab1002cc = dab01id " +
                " INNER JOIN Abb01 ON abb01id = dab10central " +
                " INNER JOIN aac01 AS aac01 ON dab10gc = aac01id "+
                " INNER JOIN aac10 AS aac10 ON dab10eg = aac10id "+
                wherePeriodoData +
                whereIdsContaCorrente +
                " ORDER BY Dab01.dab01codigo, Dab10.dab10data";

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
        return receberDadosRelatorio;
    }


}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBGaW5hbmNlaXJvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==