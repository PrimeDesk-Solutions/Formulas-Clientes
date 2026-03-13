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
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        filtrosDefault.put("periodo", DateUtils.getStartAndEndMonth(MDate.date()));
        filtrosDefault.put("impressao", "0")
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idContaCorrente = getListLong("contaCorrente");
        LocalDate[] dataPeriodo = getIntervaloDatas("periodo");
        boolean isSaltarPagina = get("isSaltarPagina");
        Integer impressao = getInteger("impressao")
        List<Long> idsEmpresas

        List<TableMap> dados = new ArrayList<>();
        params.put("TITULO_RELATORIO", "Lançamentos Financeiros");
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("PERIODO", "Período: " + dataPeriodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataPeriodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

        List<TableMap> dab10s = obterDadosRelatorio(idContaCorrente, dataPeriodo);
        Integer i = 0;
        String codigoConta = "";
        def saldoInicial = buscarSaldoAnteriorConta(idContaCorrente, dataPeriodo, idsEmpresas);
        def saldoAtual = saldoInicial;

        for (dado in dab10s){

            Integer movi = dado.getInteger("dab10mov");

            dado.put("SALDOINICIAL", saldoInicial);


            // Verifica se é recebimento ou pagamento
            if(movi == 0 ){
                dado.put("receber", dado.getBigDecimal_Zero("dab1002valor"));
                dado.put("pagar", new BigDecimal(0));
                dado.put("movimentacao", "0-Recebimento");
            }else{
                dado.put("receber", new BigDecimal(0));
                dado.put("pagar", dado.getBigDecimal_Zero("dab1002valor"));
                dado.put("movimentacao", "1-Pagamento");
            }

            saldoAtual = (saldoAtual + dado.getBigDecimal_Zero("receber")) - dado.getBigDecimal_Zero("pagar");

            dado.put("dab10valor", dado.getBigDecimal_Zero("dab1002valor") )
            dado.put("SALDO", saldoAtual);
        }


        if(impressao == 1 ) return gerarXLSX("SCF_LancamentosFinanceiros", dab10s)
        return gerarPDF("SCF_LancamentosFinanceiros", dab10s, "codigoConta", isSaltarPagina)
    }

    private BigDecimal buscarSaldoAnteriorConta(List<Long> idsContas, LocalDate[] dtLancamentos, List<Long> idsEmpresas){
        BigDecimal saldoInicial = buscarSaldoInicial(idsContas, idsEmpresas );

        BigDecimal entradas = obterTotalLancamentos(dtLancamentos, idsContas, 0);
        BigDecimal saidas = obterTotalLancamentos(dtLancamentos, idsContas, 1);

        BigDecimal saldoAnterior = (saldoInicial + entradas) - saidas;

        return saldoAnterior
    }

    private BigDecimal buscarSaldoInicial(List<Long> idsContas, List<Long> idsEmpresas){
        String whereContas = idsContas != null && idsContas.size() > 0 ? "AND dab0101cc IN (:idsContas)  "  : "";
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and dab01gc in (:idsEmpresa) " : "and dab01gc = :idEmpresa " ;

        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro parametroEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsContas", idsContas) : Parametro.criar("idEmpresa", obterEmpresaAtiva().getAac10id());

        String sql = "SELECT SUM(dab0101saldo) "+
                "FROM dab01 " +
                "INNER JOIN dab0101 ON dab0101cc = dab01id "+
                "WHERE dab0101mes = 0 AND dab0101ano = 0 "+
                whereContas +
                whereEmpresa;

        return getAcessoAoBanco().obterBigDecimal(sql, parametroContas, parametroEmpresa);
    }

    private BigDecimal obterTotalLancamentos(LocalDate[] dtLancamentos, List<Long> idsContas, Integer mov){
        String whereContas = idsContas != null && idsContas.size() > 0 ? "AND dab1002cc IN (:idsContas)  "  : "";
        String whereMov = "AND dab10mov = :mov ";
        String whereDtInicial = "AND dab10data < :dtInicial";

        Parametro parametroContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
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

    public List<TableMap> obterDadosRelatorio (List<Long> idContaCorrente, LocalDate[] dataPeriodo)  {

        String wherePeriodoData = dataPeriodo != null && dataPeriodo.size() > 0 ? " where dab10.dab10data >= '" + dataPeriodo[0] + "' and dab10.dab10data <= '" + dataPeriodo[1] + "'": "";
        String whereIdsContaCorrente = idContaCorrente != null && idContaCorrente.size() > 0 ? " and dab01.dab01id IN (:idContaCorrente)": "";
        Parametro parametro = idContaCorrente != null && idContaCorrente.size() > 0 ? Parametro.criar("idContaCorrente", idContaCorrente) : null;
        String sql = " select abb01num,  Dab01.dab01id, Dab01.dab01codigo, Dab01.dab01nome, Dab10.dab10id, Dab10.dab10data, Dab10.dab10cc, Dab10.dab10mov, Dab10.dab10historico, dab1002valor " +
                " from Dab10 Dab10 " +
                " left join abb01 on abb01id = Dab10.dab10central "+
                " INNER JOIN dab1002 ON dab1002lct = Dab10.dab10id " +
                " LEFT JOIN dab01 ON dab01id = dab1002cc "+
                wherePeriodoData +
                whereIdsContaCorrente +
                " order by Dab10.dab10data, dab10id "


        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametro);
        return receberDadosRelatorio;
    }


}