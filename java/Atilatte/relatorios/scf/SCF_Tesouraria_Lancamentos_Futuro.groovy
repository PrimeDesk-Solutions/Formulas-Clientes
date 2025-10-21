package Atilatte.relatorios.scf

import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.da.Daa01;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.utils.Parametro
import br.com.multitec.utils.StringUtils

import java.lang.reflect.Parameter
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class SCF_Tesouraria_Lancamentos_Futuro extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "SCF - Tesouraria Lançamentos Futuros";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap()
        filtrosDefault.put("conciliacao", "2")
        filtrosDefault.put("movimentacao", "2")
        filtrosDefault.put("impressao", "0")
        filtrosDefault.put("agrupamento", "0");
        filtrosDefault.put("pagamento", "2")


        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> idsEmpresas = getListLong("empresas");
        LocalDate[] dtDocumento = getIntervaloDatas("dtDocumento");
        LocalDate[] dtVcton = getIntervaloDatas("dtVcton");
        LocalDate[] dtVctor = getIntervaloDatas("dtVctor");
        List<Long> idsNaturezas = getListLong("naturezas");
        List<Long> idsEntidades = getListLong("entidades");
        Integer movimentacao = getInteger("movimentacao");
        Integer pagamento = getInteger("pagamento");
        List<Long> tipoDoc = getListLong("tipoDoc");
        Integer impressao = getInteger("impressao");
        Boolean imprimirNatureza = getBoolean("imprimirNatureza");
        def saldoIni = get("saldoIni");
        saldoIni = !StringUtils.isNullOrEmpty(saldoIni) ? new BigDecimal(saldoIni.replace(",", ".")) : new BigDecimal(0);
        Aac10 empresaAtiva = obterEmpresaAtiva();

        List<TableMap> dados = imprimirNatureza ? buscarDocumentosFinanceirosNatureza(idsEmpresas, dtDocumento, dtVcton, dtVctor, idsNaturezas, idsEntidades, empresaAtiva, movimentacao, pagamento, tipoDoc) : buscarDocumentosFinanceiros(idsEmpresas, dtDocumento, dtVcton, dtVctor, idsNaturezas, idsEntidades, empresaAtiva, movimentacao, pagamento, tipoDoc);

        def saldoAtual = saldoIni;

        for (dado in dados){
            Integer movi = dado.getInteger("daa01rp");

            dado.put("saldoInicial", saldoIni);


            // Verifica se é recebimento ou pagamento
            if(movi == 0 ){
                dado.put("receber", dado.getBigDecimal_Zero("valorDoc"));
                dado.put("pagar", new BigDecimal(0));
                dado.put("movimentacao", "0-Recebimento");
            }else{
                dado.put("receber", new BigDecimal(0));
                dado.put("pagar", dado.getBigDecimal_Zero("valorDoc"));
                dado.put("movimentacao", "1-Pagamento");
            }

            saldoAtual = (saldoAtual + dado.getBigDecimal_Zero("receber")) - dado.getBigDecimal_Zero("pagar");

            dado.put("saldoAtual", saldoAtual);
        }

        params.put("empresa",empresaAtiva.getAac10codigo() + " - " + empresaAtiva.getAac10na());

        if (impressao == 0 && !imprimirNatureza){
            params.put("titulo", "SCF - Tesouraria Lançamentos Futuros");
            return gerarPDF("SCF_Tesouraria_Lancamentos_Futuros_PDF", dados);
        }else if(impressao == 0 && imprimirNatureza){
            params.put("titulo", "SCF - Tesouraria Lançamentos Futuros Naturezas");
            return gerarPDF("SCF_Tesouraria_Lancamentos_Futuros_Natureza_PDF", dados);
        }else if(impressao == 1 && !imprimirNatureza){
            return gerarXLSX("SCF_Tesouraria_Lancamentos_Futuros_Excel", dados);
        }else{
            return gerarXLSX("SCF_Tesouraria_Lancamentos_Futuros_Natureza_Excel", dados);
        }

    }

    private List<TableMap> buscarDocumentosFinanceiros(List<Long> idsEmpresas,LocalDate[] dtDocumento, LocalDate[] dtVcton, LocalDate[] dtVctor, List<Long> idsNaturezas, List<Long> idsEntidades, Aac10 empresaAtiva, Integer movimentacao, Integer pagamento, List<Long> tipoDoc) {

        Long idEmpresa = empresaAtiva.getAac10id();
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and daa01gc in (:idsEmpresa) " : "and daa01gc = :idEmpresa " ;
        String whereDtDocumento = dtDocumento != null ? "and daa01dtLcto between :dtLctoIni and :dtLctoFin " : "";
        String whereDtVcton = dtVcton != null ? "and daa01dtvcton between :dtVctonIni and :dtVctonFin " : ""
        String whereDtVctor = dtVctor != null ? "and daa01dtvctor between :dtVctorIni and :dtVctorFin " : ""
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereMovimentacao = movimentacao == 0 ? "and daa01rp = 0 " : movimentacao == 1 ? "and daa01rp = 1 " : "";
        String wherePagamento = pagamento == 0 ? "AND daa01dtPgto IS NOT NULL " : pagamento == 1 ? "AND daa01dtPgto IS NULL " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";



        Parametro pararamEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", idEmpresa);
        Parametro pararamDtLancamentoIni = dtDocumento != null ? Parametro.criar("dtLctoIni", dtDocumento[0]) : null;
        Parametro pararamDtLancamentoFin = dtDocumento != null ? Parametro.criar("dtLctoFin", dtDocumento[1]) : null;
        Parametro pararamDtVctonIni = dtVcton != null ? Parametro.criar("dtVctonIni", dtVcton[0]) : null;
        Parametro pararamDtVctonFin = dtVcton != null ? Parametro.criar("dtVctonFin", dtVcton[1]) : null;
        Parametro pararamDtVctorIni = dtVctor != null ? Parametro.criar("dtVctorIni", dtVctor[0]) : null;
        Parametro pararamDtVctorFin = dtVctor != null ? Parametro.criar("dtVctorFin", dtVctor[1]) : null;
        Parametro pararamNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? Parametro.criar("idsNaturezas", idsNaturezas) : null;
        Parametro pararamEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;


        String sql = "select aab10user, daa01id,daa01dtlcto as dtLancamento,abb01data as dtEmissao, daa01dtvcton as vencimentoNominal,  " +
                "daa01dtvctor as vencimentoReal, daa01dtpgto as dataPagamento, " +
                "abb01num as numDoc, abb10codigo as codOper, abb10descr as descrOper, " +
                "abe01codigo as codEnt, abe01na as naEntidade, " +
                "abf15codigo as codPortador, abf15nome as naPortador, abf16codigo as codOperFinanc, abf16nome as descrOperFinanc, aah01codigo as codTipoDoc, aah01na as descrTipoDoc, " +
                "abb01serie as serie, abb01parcela as parcela, abb01quita as quita, daa01rp, " +
                "daa01valor as valorDoc, daa01liquido as vlrLiquido, " +
                "cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto, " +
                "cast(daa01json ->> 'multa' as numeric(18,6)) as multa, " +
                "cast(daa01json ->> 'juros' as numeric(18,6)) as juros, " +
                "cast(daa01json ->> 'encargos' as numeric(18,6)) as encargos, " +
                "abe01ni as cnpj " +
                "from daa01  " +
                "left join abb01 on abb01id = daa01central  " +
                "left join abb10 on abb10id = abb01operCod " +
                "LEFT JOIN abb0103 ON abb0103central = abb01id "+
                "LEFT JOIN aab10 ON aab10id = abb0103user "+
                "left join abe01 on abe01id = abb01ent " +
                "left join abf15 on abf15id = daa01port " +
                "left join abf16 on abf16id = daa01oper " +
                "left join aah01 on aah01id = abb01tipo " +
                "where true " +
                wherePagamento +
                whereEmpresa +
                whereDtDocumento +
                whereDtVcton +
                whereDtVctor +
                whereEntidades+
                whereMovimentacao +
                whereTipoDoc +
                "order by daa01dtvctor, daa01id";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, pararamEmpresa, pararamDtLancamentoIni, pararamDtLancamentoFin, pararamDtVctonIni, pararamDtVctonFin, pararamDtVctorIni, pararamDtVctorFin, pararamNaturezas, pararamEntidades, parametroTipoDoc)
    }
    private List<TableMap> buscarDocumentosFinanceirosNatureza(List<Long> idsEmpresas, LocalDate[] dtDocumento, LocalDate[] dtVcton, LocalDate[] dtVctor, List<Long> idsNaturezas, List<Long> idsEntidades, Aac10 empresaAtiva, Integer movimentacao, Integer pagamento, List<Long> tipoDoc) {

        Long idEmpresa = empresaAtiva.getAac10id();
        String whereEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? "and daa01gc in (:idsEmpresa) " : "and daa01gc = :idEmpresa " ;
        String whereDtDocumento = dtDocumento != null ? "and daa01dtLcto between :dtLctoIni and :dtLctoFin " : "";
        String whereDtVcton = dtVcton != null ? "and daa01dtvcton between :dtVctonIni and :dtVctonFin " : ""
        String whereDtVctor = dtVctor != null ? "and daa01dtvctor between :dtVctorIni and :dtVctorFin " : ""
        String whereNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? "and abf10id in (:idsNaturezas) " : "";
        String whereEntidades = idsEntidades != null && idsEntidades.size() > 0 ? "and abe01id in (:idsEntidades) " : "";
        String whereMovimentacao = movimentacao == 0 ? "and daa01rp = 0 " : movimentacao == 1 ? "and daa01rp = 1 " : "";
        String wherePagamento = pagamento == 0 ? "AND daa01dtPgto IS NOT NULL " : pagamento == 1 ? "AND daa01dtPgto IS NULL " : "";
        String whereTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? " AND abb01tipo IN (:tipoDoc) " : "";


        Parametro pararamEmpresa = idsEmpresas != null && idsEmpresas.size() > 0 ? Parametro.criar("idsEmpresa", idsEmpresas) : Parametro.criar("idEmpresa", idEmpresa);
        Parametro pararamDtLancamentoIni = dtDocumento != null ? Parametro.criar("dtLctoIni", dtDocumento[0]) : null;
        Parametro pararamDtLancamentoFin = dtDocumento != null ? Parametro.criar("dtLctoFin", dtDocumento[1]) : null;
        Parametro pararamDtVctonIni = dtVcton != null ? Parametro.criar("dtVctonIni", dtVcton[0]) : null;
        Parametro pararamDtVctonFin = dtVcton != null ? Parametro.criar("dtVctonFin", dtVcton[1]) : null;
        Parametro pararamDtVctorIni = dtVctor != null ? Parametro.criar("dtVctorIni", dtVctor[0]) : null;
        Parametro pararamDtVctorFin = dtVctor != null ? Parametro.criar("dtVctorFin", dtVctor[1]) : null;
        Parametro pararamNaturezas = idsNaturezas != null && idsNaturezas.size() > 0 ? Parametro.criar("idsNaturezas", idsNaturezas) : null;
        Parametro pararamEntidades = idsEntidades != null && idsEntidades.size() > 0 ? Parametro.criar("idsEntidades", idsEntidades) : null;
        Parametro parametroTipoDoc = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;


        String sql = "select aab10user, daa01011valor as valorDoc,abb01data as dtEmissao, daa01dtvcton as vencimentoNominal,  " +
                "daa01dtvctor as vencimentoReal, daa01dtpgto as dataPagamento,abb01num as numDoc, abb10codigo as codOper, abb10descr as descrOper, " +
                "abf10codigo as codNat, abf10nome as descrNat,abe01codigo as codEnt, abe01na as naEntidade, " +
                "abf15codigo as codPortador, abf15nome as naPortador, abf16codigo as codOperFinanc, abf16nome as descrOperFinanc, aah01codigo as codTipoDoc, aah01na as descrTipoDoc, " +
                "abb01serie as serie, abb01parcela as parcela, abb01quita as quita, daa01rp, " +
                "cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto, " +
                "cast(daa01json ->> 'multa' as numeric(18,6)) as multa, " +
                "cast(daa01json ->> 'juros' as numeric(18,6)) as juros, " +
                "cast(daa01json ->> 'encargos' as numeric(18,6)) as encargos, " +
                "abe01ni as cnpj " +
                "from daa01 " +
                "left join abb01 on abb01id = daa01central " +
                "left join abb10 on abb10id = abb01operCod " +
                "LEFT JOIN abb0103 ON abb0103central = abb01id "+
                "LEFT JOIN aab10 ON aab10id = abb0103user "+
                "left join daa0101 on daa0101doc = daa01id " +
                "left join daa01011 on daa01011depto = daa0101id " +
                "left join abf10 on abf10id = daa01011nat " +
                "left join abe01 on abe01id = abb01ent " +
                "left join abf15 on abf15id = daa01port " +
                "left join abf16 on abf16id = daa01oper " +
                "left join aah01 on aah01id = abb01tipo " +
                "where true " +
                wherePagamento +
                whereEmpresa +
                whereDtDocumento +
                whereDtVcton +
                whereDtVctor +
                whereNaturezas +
                whereEntidades+
                whereMovimentacao +
                whereTipoDoc +
                "order by daa01dtvctor, daa01id";

        return getAcessoAoBanco().buscarListaDeTableMap(sql, pararamEmpresa, pararamDtLancamentoIni, pararamDtLancamentoFin, pararamDtVctonIni, pararamDtVctonFin, pararamDtVctorIni, pararamDtVctorFin, pararamNaturezas, pararamEntidades, parametroTipoDoc)
    }
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIFRlc291cmFyaWEgTGFuw6dhbWVudG9zIEZ1dHVyb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=