package Silcon.relatorios.customizados

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

public class CST_Integracao_Bancaria_Contabil extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Integração Bancária na Contabilidade";
    }
    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("conciliacao", "2")
        filtrosDefault.put("movimentacao", "2")
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("agrupamento", "0")


        return Utils.map("filtros", filtrosDefault);
    }
    @Override
    public DadosParaDownload executar() {
        List<Long> idsEmpresas = getListLong("empresas");
        List<Long> idsContas = getListLong("contasCorrentes");
        LocalDate[] dtLancamentos = getIntervaloDatas("dtLancamento");
        List<Long> tipoDoc = getListLong("tipoDoc");
        Integer conciliacao = getInteger("conciliacao");
        Integer movimentacao = getInteger("movimentacao");
        Integer impressao = getInteger("impressao");
        Aac10 empresaAtiva = obterEmpresaAtiva();

        params.put("EMPRESA", obterEmpresaAtiva().getAac10codigo() + " - " + obterEmpresaAtiva().getAac10na());
        params.put("TITULO", "Lançamentos Bancários na Contabilidade");


        if (dtLancamentos != null) {
            params.put("PERIODO","Período: " + dtLancamentos[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtLancamentos[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() );
        }

        List<TableMap> dados = buscarLancamentos(idsEmpresas, idsContas, dtLancamentos, empresaAtiva, conciliacao, movimentacao, tipoDoc)

        Long idControle = null;
        BigDecimal saldoInicial = new BigDecimal(0);
        BigDecimal saldoAtual = new BigDecimal(0);
        for (dado in dados){
            LocalDate dtConciliacao = dado.getDate("dtConc");
            Integer movi = dado.getInteger("dab10mov");
            Long idConta = dado.getLong("dab01id");

            if(idControle != idConta){
                saldoAtual = new BigDecimal(0);
                saldoInicial = buscarSaldoAnteriorConta(List.of(idConta), dtLancamentos, idsEmpresas);
                saldoAtual = saldoInicial;
            }

            // Verifica se o documento está conciliado
            if(dtConciliacao != null){
                dado.put("conciliado", "S");
            }else{
                dado.put("conciliado", "N");
            }

            // Verifica se é recebimento ou pagamento
            if(movi == 0 ){
                dado.put("receber", dado.getBigDecimal_Zero("valor"));
                dado.put("pagar", new BigDecimal(0));
                dado.put("movimentacao", "0-Recebimento");
            }else{
                dado.put("receber", new BigDecimal(0));
                dado.put("pagar", dado.getBigDecimal_Zero("valor"));
                dado.put("movimentacao", "1-Pagamento");
            }

            dado.put("saldoInicial", saldoInicial);
            saldoAtual = (saldoAtual + dado.getBigDecimal_Zero("receber")) - dado.getBigDecimal_Zero("pagar")
            dado.put("saldoAtual", saldoAtual);

            idControle = dado.getLong("dab01id");
        }

        if(impressao == 1) return gerarXLSX("CST_Integracao_Bancaria_Contabil_Excel", dados);

        return gerarPDF("CST_Integracao_Bancaria_Contabil_PDF", dados);
    }
    private List<TableMap> buscarLancamentos(List<Long> idsEmpresas, List<Long> idsContas, LocalDate[] dtLancamentos, Aac10 empresaAtiva, Integer conciliacao, Integer movimentacao, List<Long> tipoDoc){
        Long idEmpresa = empresaAtiva.getAac10id();
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? " AND dab10gc in (:idsEmpresa) " : " AND dab10gc = :idEmpresa " ;
        String whereContas = idsContas != null && idsContas.size() > 0 ? " AND dab01id IN (:idsContas) " : "";
        String whereDtLancamento = dtLancamentos != null ? " AND dab10data between :dtLctoIni  AND :dtLctoFin " : "";
        String whereConciliacao = conciliacao == 0 ? " AND dab1002dtConc is not null " : conciliacao == 1 ? " AND dab1002dtConc IS NULL " : "";
        String whereMovimentacao = movimentacao == 0 ? " AND dab10mov = 0 " : movimentacao == 1 ? " AND dab10mov = 1 " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";


        Parametro pararamEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", idEmpresa);
        Parametro pararamContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro pararamDtLancamentoIni = dtLancamentos != null ? Parametro.criar("dtLctoIni", dtLancamentos[0]) : null;
        Parametro pararamDtLancamentoFin = dtLancamentos != null ? Parametro.criar("dtLctoFin", dtLancamentos[1]) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;
        
        String sql = "SELECT dab10data AS dtLcto, dab01codigo AS codCC, dab01nome AS nomeCC,   " +
                    "abe01ni AS cnpj, dab10mov, dab10historico AS historico, abb01num AS numDoc, aah01codigo AS codTipoDoc,  " +
                    "abf40descr AS formaPgto, dab1002dtConc as dtConc, dab1002valor AS valor, dab01id  " +
                    "FROM dab10  " +
                    "INNER JOIN dab1002 ON dab1002lct = dab10id  " +
                    "INNER JOIN dab01 ON dab01id = dab1002cc  " +
                    "INNER JOIN abb01 ON abb01id = dab10central  " +
                    "INNER JOIN abe01 ON abe01id = abb01ent  " +
                    "INNER JOIN abf40 ON abf40id = dab1002fp  " +
                    "LEFT JOIN daa01 ON daa01central = abb01id "+
                    "LEFT JOIN aah01 ON aah01id = abb01tipo "+
                    "WHERE TRUE  " +
                    whereEmpresa +
                    whereContas +
                    whereDtLancamento +
                    whereConciliacao +
                    whereMovimentacao +
                    whereTipoDoc +
                    "ORDER BY dab01codigo, dab10data, aah01codigo, daa01dtVctoR "

        return getAcessoAoBanco().buscarListaDeTableMap(sql, pararamEmpresa, pararamContas, pararamDtLancamentoIni, pararamDtLancamentoFin, parametroTipoDoc)
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
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? " AND dab01gc in (:idsEmpresa) " : " AND dab01gc = :idEmpresa " ;

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
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIEludGVncmHDp8OjbyBCYW5jw6FyaWEgbmEgQ29udGFiaWxpZGFkZSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==