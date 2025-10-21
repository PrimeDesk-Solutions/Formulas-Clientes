package Atilatte.relatorios.scf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.da.Daa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro

import java.lang.reflect.Parameter
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_Tesouraria_Lancamentos_Retroativos extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Tesouraria Lançamentos Retroativos";
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
        LocalDate[] dtVcton = getIntervaloDatas("dtVcton");
        LocalDate[] dtVctor = getIntervaloDatas("dtVctor");
        List<Long> idsNaturezas = getListLong("naturezas");
        List<Long> idsEntidades = getListLong("entidades");
        List<Long> tipoDoc = getListLong("tipoDoc");
        Integer conciliacao = getInteger("conciliacao");
        Integer movimentacao = getInteger("movimentacao");
        Integer impressao = getInteger("impressao");
        Boolean imprimirNatureza = getBoolean("imprimirNatureza");
        Integer agrupamento = getInteger("agrupamento")
        Aac10 empresaAtiva = obterEmpresaAtiva();

        List<TableMap> dados = imprimirNatureza ? buscarLancamentosFinanceirosNatureza(idsEmpresas, idsContas, dtLancamentos, dtVcton, dtVctor, idsNaturezas, idsEntidades, empresaAtiva, conciliacao, movimentacao, agrupamento, tipoDoc) : buscarLancamentosFinanceiros(idsEmpresas, idsContas, dtLancamentos, dtVcton, dtVctor, idsNaturezas, idsEntidades, empresaAtiva, conciliacao, movimentacao, agrupamento, tipoDoc);

        def saldoInicial = buscarSaldoAnteriorConta(idsContas, dtLancamentos, idsEmpresas);
        def saldoAtual = saldoInicial;

        for (dado in dados){

            LocalDate dtConciliacao = dado.getDate("dtConc");
            Integer movi = dado.getInteger("dab10mov");

            dado.put("saldoInicial", saldoInicial);

            // Verifica se o documento está conciliado
            if(dtConciliacao != null){
                dado.put("conciliado", "S");
            }else{
                dado.put("conciliado", "N");
            }
            def valor = dado.getBigDecimal_Zero("valorNat");

            // Verifica se é recebimento ou pagamento
            if(movi == 0 ){
                dado.put("receber", dado.getBigDecimal_Zero("vlrLiquido"));
                dado.put("pagar", new BigDecimal(0));
                dado.put("movimentacao", "0-Recebimento");
            }else{
                dado.put("receber", new BigDecimal(0));
                dado.put("pagar", dado.getBigDecimal_Zero("vlrLiquido"));
                dado.put("movimentacao", "1-Pagamento");
            }

            dado.put("agrupamento", agrupamento);

            saldoAtual = (saldoAtual + dado.getBigDecimal_Zero("receber")) - dado.getBigDecimal_Zero("pagar")
            dado.put("saldoAtual", saldoAtual);
        }

        params.put("empresa",empresaAtiva.getAac10codigo() + " - " + empresaAtiva.getAac10na());

        if (impressao == 0 && !imprimirNatureza){
            params.put("titulo", "SCF - Tesouraria");
            return gerarPDF("SCF_Tesouraria_Lancamentos_Retroativos_PDF", dados);
        }else if(impressao == 0 && imprimirNatureza){
            params.put("titulo", "SCF - Tesouraria Naturezas");
            return gerarPDF("SCF_Tesouraria_Lancamentos_Retroativos_Natureza_PDF", dados);
        }else if(impressao == 1 && !imprimirNatureza){
            return gerarXLSX("SCF_Tesouraria_Lancamentos_Retroativos_Excel", dados);
        }else{
            return gerarXLSX("SCF_Tesouraria_Lancamentos_Retroativos_Natureza_Excel", dados);
        }

    }

    private List<TableMap> buscarLancamentosFinanceiros(List<Long> idsEmpresas, List<Long> idsContas, LocalDate[] dtLancamentos, LocalDate[] dtVcton, LocalDate[] dtVctor, List<Long> idsNaturezas, List<Long> idsEntidades, Aac10 empresaAtiva, Integer conciliacao, Integer movimentacao, Integer agrupamento, List<Long> tipoDoc) {

        Long idEmpresa = empresaAtiva.getAac10id();
        String orderBy = agrupamento == 0 ? "order by daa01dtvctor, daa01id" : "order by daa01dtPgto, daa01id ";
        String fieldAgroup = agrupamento == 0 ? "daa01dtvctor as grupoRelatorio " : "daa01dtpgto as grupoRelatorio ";
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and dab10gc in (:idsEmpresa) " : "and dab10gc = :idEmpresa " ;
        String whereContas = idsContas != null && idsContas.size() > 0 ? "and dab01id in (:idsContas) " : "";
        String whereDtLancamento = dtLancamentos != null ? "and dab10data between :dtLctoIni and :dtLctoFin " : "";
        String whereDtVcton = dtVcton != null ? "and daa01dtvcton between :dtVctonIni and :dtVctonFin " : ""
        String whereDtVctor = dtVctor != null ? "and daa01dtvctor between :dtVctorIni and :dtVctorFin " : ""
        String whereNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? "and abf10id in (:idsNaturezas) " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereConciliacao = conciliacao == 0 ? "and dab1002dtConc is not null " : conciliacao == 1 ? "and dab1002dtConc is null " : "";
        String whereMovimentacao = movimentacao == 0 ? "and dab10mov = 0 " : movimentacao == 1 ? "and dab10mov = 1 " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";


        Parametro pararamEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", idEmpresa);
        Parametro pararamContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro pararamDtLancamentoIni = dtLancamentos != null ? Parametro.criar("dtLctoIni", dtLancamentos[0]) : null;
        Parametro pararamDtLancamentoFin = dtLancamentos != null ? Parametro.criar("dtLctoFin", dtLancamentos[1]) : null;
        Parametro pararamDtVctonIni = dtVcton != null ? Parametro.criar("dtVctonIni", dtVcton[0]) : null;
        Parametro pararamDtVctonFin = dtVcton != null ? Parametro.criar("dtVctonFin", dtVcton[1]) : null;
        Parametro pararamDtVctorIni = dtVctor != null ? Parametro.criar("dtVctorIni", dtVctor[0]) : null;
        Parametro pararamDtVctorFin = dtVctor != null ? Parametro.criar("dtVctorFin", dtVctor[1]) : null;
        Parametro pararamNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? Parametro.criar("idsNaturezas", idsNaturezas) : null;
        Parametro pararamEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;


        String sql = "select distinct aab10user, daa01id,dab10id,abb01data as dtEmissao, daa01dtvcton as vencimentoNominal, \n" +
                "daa01dtvctor as vencimentoReal, dab10data as dtLancamento, daa01dtpgto as dataPagamento,\n" +
                "dab1002dtConc as dtConc, abb01num as numLcto, abb10codigo as codOper, abb10descr as descrOper,\n" +
                "abe01codigo as codEnt, abe01na as naEntidade, dab01codigo as codConta, dab01nome as nomeConta,\n" +
                "abf15codigo as codPortador, abf15nome as naPortador, abf16codigo as codOperFinanc, abf16nome as descrOperFinanc, aah01codigo as codTipoDoc, aah01na as descrTipoDoc,\n" +
                "abb01serie as serie, abb01parcela as parcela, abb01quita as quita, dab10mov,\n" +
                "dab1002valor as vlrLiquido, coalesce(daa01valor, dab10valor) as valorDoc,\n" +
                "cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto,\n" +
                "cast(daa01json ->> 'multa' as numeric(18,6)) as multa,\n" +
                "cast(daa01json ->> 'juros' as numeric(18,6)) as juros,\n" +
                "cast(daa01json ->> 'encargos' as numeric(18,6)) as encargos,\n" +
                "abe01ni as cnpj, "+
                fieldAgroup +
                "from dab10\n" +
                "left join abb01 on abb01id = dab10central\n" +
                "LEFT JOIN abb0103 ON abb0103central = abb01id "+
                "LEFT JOIN aab10 ON aab10id = abb0103user "+
                "left join dab1002 on dab1002lct = dab10id\n" +
                "left join abb10 on abb10id = abb01operCod\n" +
                "left join dab1001 on dab1001lct = dab10id\n" +
                "left join dab10011 on dab10011depto = dab1001id\n" +
                "left join abf10 on abf10id = dab10011nat\n" +
                "left join abe01 on abe01id = abb01ent\n" +
                "left join dab01 on dab01id = dab1002cc\n" +
                "left join daa01 on daa01central = abb01id\n" +
                "left join abf15 on abf15id = daa01port\n" +
                "left join abf16 on abf16id = daa01oper\n" +
                "left join aah01 on aah01id = abb01tipo\n" +
                "where true\n" +
                whereEmpresa +
                whereContas +
                whereDtLancamento +
                whereDtVcton +
                whereDtVctor +
                whereNaturezas +
                whereEntidades+
                whereConciliacao +
                whereMovimentacao +
                whereTipoDoc +
                "order by dab10data, dab10id"
        //"order by daa01dtvctor, daa01id"


        return getAcessoAoBanco().buscarListaDeTableMap(sql, pararamEmpresa, pararamContas, pararamDtLancamentoIni, pararamDtLancamentoFin, pararamDtVctonIni, pararamDtVctonFin, pararamDtVctorIni, pararamDtVctorFin, pararamNaturezas, pararamEntidades, parametroTipoDoc)
    }
    private List<TableMap> buscarLancamentosFinanceirosNatureza(List<Long> idsEmpresas, List<Long> idsContas, LocalDate[] dtLancamentos, LocalDate[] dtVcton, LocalDate[] dtVctor, List<Long> idsNaturezas, List<Long> idsEntidades, Aac10 empresaAtiva, Integer conciliacao, Integer movimentacao, Integer agrupamento, List<Long> tipoDoc) {

        Long idEmpresa = empresaAtiva.getAac10id();
        String orderBy = agrupamento == 0 ? "order by daa01dtvctor, daa01id" : "order by daa01dtPgto, daa01id ";
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and dab10gc in (:idsEmpresa) " : "and dab10gc = :idEmpresa " ;
        String fieldAgroup = agrupamento == 0 ? "daa01dtvctor as grupoRelatorio " : "daa01dtpgto as grupoRelatorio ";
        String whereContas = idsContas != null && idsContas.size() > 0 ? "and dab01id in (:idsContas) " : "";
        String whereDtLancamento = dtLancamentos != null ? "and dab10data between :dtLctoIni and :dtLctoFin " : "";
        String whereDtVcton = dtVcton != null ? "and daa01dtvcton between :dtVctonIni and :dtVctonFin " : ""
        String whereDtVctor = dtVctor != null ? "and daa01dtvctor between :dtVctorIni and :dtVctorFin " : ""
        String whereNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? "and abf10id in (:idsNaturezas) " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereConciliacao = conciliacao == 0 ? "and dab1002dtConc is not null " : conciliacao == 1 ? "and dab1002dtConc is null " : "";
        String whereMovimentacao = movimentacao == 0 ? "and dab10mov = 0 " : movimentacao == 1 ? "and dab10mov = 1 " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";

        Parametro pararamEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", idEmpresa);
        Parametro pararamContas = idsContas != null && idsContas.size() > 0 ? Parametro.criar("idsContas", idsContas) : null;
        Parametro pararamDtLancamentoIni = dtLancamentos != null ? Parametro.criar("dtLctoIni", dtLancamentos[0]) : null;
        Parametro pararamDtLancamentoFin = dtLancamentos != null ? Parametro.criar("dtLctoFin", dtLancamentos[1]) : null;
        Parametro pararamDtVctonIni = dtVcton != null ? Parametro.criar("dtVctonIni", dtVcton[0]) : null;
        Parametro pararamDtVctonFin = dtVcton != null ? Parametro.criar("dtVctonFin", dtVcton[1]) : null;
        Parametro pararamDtVctorIni = dtVctor != null ? Parametro.criar("dtVctorIni", dtVctor[0]) : null;
        Parametro pararamDtVctorFin = dtVctor != null ? Parametro.criar("dtVctorFin", dtVctor[1]) : null;
        Parametro pararamNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? Parametro.criar("idsNaturezas", idsNaturezas) : null;
        Parametro pararamEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;


        String sql = "select aab10user, dab10011valor as valorNat,abb01data as dtEmissao, daa01dtvcton as vencimentoNominal, \n" +
                "daa01dtvctor as vencimentoReal, dab10data as dtLancamento, daa01dtpgto as dataPagamento,\n" +
                "dab1002dtConc as dtConc, abb01num as numLcto, abb10codigo as codOper, abb10descr as descrOper,\n" +
                "abf10codigo as codNat, abf10nome as descrNat,abe01codigo as codEnt, abe01na as naEntidade, dab01codigo as codConta, dab01nome as nomeConta,\n" +
                "abf15codigo as codPortador, abf15nome as naPortador, abf16codigo as codOperFinanc, abf16nome as descrOperFinanc, aah01codigo as codTipoDoc, aah01na as descrTipoDoc,\n" +
                "abb01serie as serie, abb01parcela as parcela, abb01quita as quita, dab10mov,\n" +
                "dab1002valor as vlrLiquido, coalesce(daa01valor, dab10valor) as valorDoc,\n" +
                "cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto,\n" +
                "cast(daa01json ->> 'multa' as numeric(18,6)) as multa,\n" +
                "cast(daa01json ->> 'juros' as numeric(18,6)) as juros,\n" +
                "cast(daa01json ->> 'encargos' as numeric(18,6)) as encargos,\n" +
                "abe01ni as cnpj, "+
                fieldAgroup +
                "from dab10\n" +
                "left join abb01 on abb01id = dab10central\n" +
                "LEFT JOIN abb0103 ON abb0103central = abb01id "+
                "LEFT JOIN aab10 ON aab10id = abb0103user "+
                "left join dab1002 on dab1002lct = dab10id\n" +
                "left join abb10 on abb10id = abb01operCod\n" +
                "left join dab1001 on dab1001lct = dab10id\n" +
                "left join dab10011 on dab10011depto = dab1001id\n" +
                "left join abf10 on abf10id = dab10011nat\n" +
                "left join abe01 on abe01id = abb01ent\n" +
                "left join dab01 on dab01id = dab1002cc\n" +
                "left join daa01 on daa01central = abb01id\n" +
                "left join abf15 on abf15id = daa01port\n" +
                "left join abf16 on abf16id = daa01oper\n" +
                "left join aah01 on aah01id = abb01tipo\n" +
                "where true\n" +
                whereEmpresa +
                whereContas +
                whereDtLancamento +
                whereDtVcton +
                whereDtVctor +
                whereNaturezas +
                whereEntidades+
                whereConciliacao +
                whereMovimentacao +
                whereTipoDoc +
                "order by dab10data, dab10id"
        //"order by daa01dtvctor, daa01id"

        return getAcessoAoBanco().buscarListaDeTableMap(sql, pararamEmpresa, pararamContas, pararamDtLancamentoIni, pararamDtLancamentoFin, pararamDtVctonIni, pararamDtVctonFin, pararamDtVctorIni, pararamDtVctorFin, pararamNaturezas, pararamEntidades, parametroTipoDoc)
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


}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFRlc291cmFyaWEgTGFuw6dhbWVudG9zIFJldHJvYXRpdm9zIiwidGlwbyI6InJlbGF0b3JpbyJ9