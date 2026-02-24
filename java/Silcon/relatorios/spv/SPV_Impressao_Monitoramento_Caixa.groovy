package Silcon.relatorios.spv

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import sam.model.entities.da.Dab01
import sam.model.entities.da.Dab0101
import sam.model.entities.da.Dab10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro;
import br.com.multitec.utils.collections.TableMap

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SPV_Impressao_Monitoramento_Caixa extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SPV - Impressao Monitoramento Caixa";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        return filtrosDefault;
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsLctos = getListLong("lancamentos");
        String codConta = getString("codConta");
        String nomeConta = getString("nomeConta");
        String usuario = getString("usuario");
        BigDecimal saldoAtual = getBigDecimal("saldoAtual");
        Long idConta = getLong("idConta");
        LocalDate data = LocalDate.parse(getString("data"))

        List<TableMap> dados = buscarLancamentos(idsLctos);

        BigDecimal saldoAnterior = buscarSaldoAnterior(idConta, data);

        BigDecimal saldo = saldoAnterior;
        for (dado in dados) {
            BigDecimal valor = dado.getBigDecimal_Zero("valor");

            if (dado.getInteger("mov") == 0) {
                saldo = saldo.add(valor);
                dado.put("saldo", saldo);
            } else {
                saldo = saldo.subtract(valor);
                dado.put("saldo", saldo);
            }

            dado.put("codConta", codConta);
            dado.put("nomeConta", nomeConta);
            dado.put("usuario", usuario);
            dado.put("saldoAtual", saldoAtual);
        }

        TableMap tmSaldoAnterior = new TableMap();
        tmSaldoAnterior.put("mov", 0);
        tmSaldoAnterior.put("historico", "Saldo Anterior");
        tmSaldoAnterior.put("valor", new BigDecimal(0));
        tmSaldoAnterior.put("saldo", saldoAnterior);
        tmSaldoAnterior.put("numLcto", 0);
        tmSaldoAnterior.put("codConta", codConta);
        tmSaldoAnterior.put("nomeConta", nomeConta);
        tmSaldoAnterior.put("usuario", usuario);
        tmSaldoAnterior.put("saldoAtual", saldoAtual);
        dados.add(0, tmSaldoAnterior)

        params.put("TITULO", "SPV - Monitoramento de Caixa");
        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + "-" + obterEmpresaAtiva().getAac10na());
        params.put("LOGO_EMPRESA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\spv\\Logo Silcon.png");
        params.put("LOGO_REVENDA", "C:\\SAM-Servidor\\samdev\\resources\\Silcon\\relatorios\\spv\\logoPrimeDesk.png");

        return gerarPDF("SPV_Impressao_Monitoramento_Caixa", dados);
    }

    private BigDecimal buscarSaldoAnterior(Long dab01id, LocalDate data) {
        String field = Fields.numMeses("dab0101mes", "dab0101ano").toString();
        int numMeses = data != null ? (data.getYear() * 12) + data.getMonthValue() : 1;

        BigDecimal saldo = session.createCriteria(Dab0101.class)
                .addFields("dab0101saldo")
                .addJoin(Joins.join("dab0101cc").left(false).alias("dab01"))
                .addWhere(Criterions.lt(field, numMeses))
                .addWhere(Criterions.ne("dab0101mes", 0)).addWhere(Criterions.ne("dab0101ano", 0))
                .addWhere(Criterions.in("dab01id", dab01id)).setMaxResults(1)
                .addWhere(samWhere.getCritPadrao(Dab01.class))
                .setOrder(field + "desc").get(ColumnType.BIG_DECIMAL);

        BigDecimal saldoAnterior = saldo != null ? saldo : BigDecimal.ZERO;

        if (data.getDayOfMonth() != 1) {
            LocalDate dataDia1 = LocalDate.of(data.getYear(), data.getMonthValue(), 1);
            LocalDate dataFim = data.minusDays(1);
            BigDecimal entradas = somarLancamentosPeloIdCC(dataDia1, dataFim, dab01id, 0);
            BigDecimal saidas = somarLancamentosPeloIdCC(dataDia1, dataFim, dab01id, 1);
            saldoAnterior = saldoAnterior.add(entradas).subtract(saidas);
        }

        return saldoAnterior;

    }

    private BigDecimal somarLancamentosPeloIdCC(LocalDate dataInicio, LocalDate dataFim, Long dab01id, Integer mov) {
        String sql = "SELECT SUM(dab1002valor) " +
                " FROM Dab1002 " +
                " INNER JOIN dab10 ON dab10id = dab1002lct " +
                " WHERE dab1002cc = :dab01id " +
                " AND dab10mov = :dab10mov " +
                " AND dab10data BETWEEN :dataInicio AND :dataFim" +
                getSamWhere().getWherePadrao("AND", Dab10.class);

        BigDecimal valor = getSession().createQuery(sql).setParameters(new Object[]{"dab01id", dab01id, "dab10mov", mov, "dataInicio", dataInicio, "dataFim", dataFim}).setMaxResult(1).getUniqueResult(ColumnType.BIG_DECIMAL);

        return valor == null ? BigDecimal.ZERO : valor;
    }

    private List<TableMap> buscarLancamentos(List<Long> idsLctos) {
        String sql = "SELECT dab10data AS dtLcto, dab10historico AS historico, aah01codigo AS codTipoDoc, " +
                "aah01nome AS nomeTipoDoc, abb01num AS numLcto, dab10mov AS mov, dab1002valor AS valor " +
                "FROM dab10 " +
                "INNER JOIN abb01 ON abb01id = dab10central " +
                "INNER JOIN aah01 ON aah01id = abb01tipo " +
                "INNER JOIN dab1002 ON dab1002lct = dab10id "+
                "WHERE dab10id IN (:idsLctos) " +
                "ORDER BY dab10data";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("idsLctos", idsLctos));
    }
}