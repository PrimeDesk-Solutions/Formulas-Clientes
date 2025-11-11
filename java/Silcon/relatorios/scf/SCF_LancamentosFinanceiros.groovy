package Silcon.relatorios.scf

import br.com.multitec.utils.collections.TableMap
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.Utils;
import sam.model.entities.aa.Aac10;


import java.time.LocalDate

public class SCF_LancamentosFinanceiros extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Lançamentos Financeiros";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<>();
        filtrosDefault.put("imprimir", "0");
        return filtrosDefault;
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsConta = getListLong("contaCorrente");

        Integer impressao = getInteger("imprimir");

        LocalDate[] dtLcto = getIntervaloDatas("dtLcto");

        List<TableMap> lancamentos = buscarRegistrosContaCorrentes (idsConta, dtLcto);
        List<TableMap> dados = new ArrayList<>();
        Long idEmpresa = obterEmpresaAtiva().getAac10id();
        List<Long> empresas = [idEmpresa];

        BigDecimal saldoInicial = buscarSaldoAnteriorConta (idsConta, dtLcto, empresas);

        BigDecimal saldoAtual = saldoInicial;

        for (lancamento in lancamentos) {

            Integer mov = lancamento.getInteger("movimentacao");

            lancamento.put("saldoInicial", saldoInicial);

            BigDecimal valor = lancamento.getBigDecimal_Zero("valorLancamento");

            // Verifica se é recebimento ou pagamento
           if (mov == 0) {
               saldoAtual = saldoAtual + valor;
               lancamento.put("mov", "C");

           } else {
               saldoAtual = saldoAtual - valor;
               lancamento.put("mov", "D");
           }

            lancamento.put("saldoAtual", saldoAtual);

            dados.add(lancamento);
        }

        if (impressao == 0) {
            return gerarXLSX("SCF_LancamentosFinanceiros(EXCEL)", dados);

        } else {
            return gerarPDF("SCF_LancamentosFinanceiros(PDF)", dados);
        }

    }

    private List<TableMap> buscarRegistrosContaCorrentes (List<Long> idsConta, LocalDate[] dtLcto) {

        // Data Corrente Inicial && Data Corrente Final
        LocalDate dataLancamentoFinancIni = null;
        LocalDate dataLancamentoFinancFin = null;

        if (dtLcto != null) {
            dataLancamentoFinancIni = dtLcto[0];
            dataLancamentoFinancFin = dtLcto[1];
        }

        String whereEmpresaAtiva = " WHERE dab10eg = :idEmpresa ";
        String whereDataLancamentoFinanc = dataLancamentoFinancIni != null && dataLancamentoFinancFin != null ? " AND dab10data BETWEEN :dataLancamentoFinancIni AND :dataLancamentoFinancFin " : "";

        String whereCodContaCorrente = idsConta != null && idsConta.size() > 0 ? "AND dab01id in (:idsConta)" : "";

        Parametro parametrosEmpresaAtiva = Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        Parametro parametrosDataLancamentoFinancIni = dataLancamentoFinancIni != null ? Parametro.criar("dataLancamentoFinancIni", dataLancamentoFinancIni) : null;
        Parametro parametrosDataLancamentoFinancFin = dataLancamentoFinancFin != null ? Parametro.criar("dataLancamentoFinancFin", dataLancamentoFinancFin) : null;

        Parametro parametrosContaCorrente =idsConta != null && idsConta.size() > 0 ? Parametro.criar("idsConta", idsConta) : null;

        String sql = " SELECT dab01id, dab01codigo as codigoConta, dab01nome as nomeConta, dab10data as dataLancamento, " +
                     " dab10historico as historicoLancamento, dab10mov as movimentacao, dab10valor as valorLancamento " +
                     " FROM dab10 " +
                     " INNER JOIN dab1002 on dab1002lct = dab10id " +
                     " INNER JOIN dab01 on dab01id = dab1002cc " +
                     whereEmpresaAtiva +
                     whereDataLancamentoFinanc +
                     whereCodContaCorrente +
                     " ORDER BY dab01codigo, dab10data";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, parametrosEmpresaAtiva, parametrosDataLancamentoFinancIni, parametrosDataLancamentoFinancFin, parametrosContaCorrente);
    }

    private BigDecimal buscarSaldoAnteriorConta (List<Long> idsContas, LocalDate[] dtLancamentos, List<Long> idsEmpresas) {
        BigDecimal saldoInicial = buscarSaldoInicial(idsContas, idsEmpresas );

        BigDecimal entradas = obterTotalLancamentos(dtLancamentos, idsContas, 0);
        BigDecimal saidas = obterTotalLancamentos(dtLancamentos, idsContas, 1);

        BigDecimal saldoAnterior = (saldoInicial + entradas) - saidas;

        return saldoAnterior;
    }

    private BigDecimal buscarSaldoInicial (List<Long> idsContas, List<Long> idsEmpresas) {
        String whereContas = idsContas != null && idsContas.size() > 0 ? "AND dab0101cc IN (:idsContas)  "  : "";
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and dab01gc in (:idsEmpresa) " : "and dab01gc = :idEmpresa " ;

        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro parametroEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = " SELECT SUM(dab0101saldo) "+
                     " FROM dab01 " +
                     " INNER JOIN dab0101 ON dab0101cc = dab01id "+
                     " WHERE dab0101mes = 0 AND dab0101ano = 0 "+
                     whereContas +
                     whereEmpresa;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroContas, parametroEmpresa);
    }

    private BigDecimal obterTotalLancamentos (LocalDate[] dtLancamentos, List<Long> idsContas, Integer mov) {
        String whereContas = idsContas != null && idsContas.size() > 0 ? "AND dab1002cc IN (:idsContas)  "  : "";
        String whereMov = "AND dab10mov = :mov ";
        String whereDtInicial = "AND dab10data < :dtInicial";

        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro parametroMov = Parametro.criar("mov", mov);
        Parametro parametroDtInicial = Parametro.criar("dtInicial", dtLancamentos[0]);

        String sql = " SELECT SUM(dab1002valor) " +
                     " FROM dab10 " +
                     " INNER JOIN dab1002 ON dab1002lct = dab10id " +
                     " WHERE TRUE " +
                     whereContas +
                     whereMov +
                     whereDtInicial;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroContas, parametroMov, parametroDtInicial);

    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIExhbsOnYW1lbnRvcyBGaW5hbmNlaXJvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==