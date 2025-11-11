package Silcon.relatorios.customizados

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multitec.utils.collections.TableMap;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload
import br.com.multitec.utils.Utils;
import sam.model.entities.da.Daa01
import sam.server.samdev.utils.Parametro

import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
import java.time.format.DateTimeFormatter
import br.com.multiorm.criteria.criterion.Criterions;
import sam.model.entities.aa.Aac1001;
import br.com.multiorm.ColumnType


public class CST_Documentos_Ordenados_Por_Valor extends RelatorioBase {
    @Override
    public String getNomeTarefa() {
        return "CST - Documentos Ordenados por Valor";
    }

    @Override
    public Map<String, Object> criarValoresIniciais() {
        Map<String, Object> filtrosDefault = new HashMap<String, Object>();
        filtrosDefault.put("classe", "0");
        filtrosDefault.put("tp", "0");
        filtrosDefault.put("opcVcto", "0");
        filtrosDefault.put("numeroInicial", "000000000");
        filtrosDefault.put("numeroFinal", "999999999");
        filtrosDefault.put("exportar", "0");
        filtrosDefault.put("tipoData", "0");
        filtrosDefault.put("agrupamento", "D");
        filtrosDefault.put("ordem", "0");
        return Utils.map("filtros", filtrosDefault);
    }

    @Override
    public DadosParaDownload executar() {
        List<Long> Emprs = getListLong("Emprs");
        Integer classe = getInteger("classe");
        Integer tp = getInteger("tp");
        Integer exportar = getInteger("exportar");
        List<Long> tipoDoc = getListLong("tipoDoc");
        Integer ordem = getInteger("ordem")
        Integer numeroInicial = getInteger("numeroInicial");
        Integer numeroFinal = getInteger("numeroFinal");
        List<Long> entidade = getListLong("entidade");
        List<Long> departamento = getListLong("departamento");
        List<Long> naturezas = getListLong("naturezas");
        LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
        Integer opcVcto = getInteger("opcVcto");
        LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
        Integer tipoData = getInteger("tipoData");
        LocalDate[] dtPgtoBaixa = getIntervaloDatas("dataPagBaixa");
        List<Long> port = getListLong("portador");
        List<Long> oper = getListLong("oper");


        params.put("TIPOCLASSE", buscarTipoClasse(classe));
        params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
        params.put("OPCVCTO", opcVcto);

        if (dataVenc != null && dataVenc[0] != null && dataVenc[1] != null) {
            params.put("PERIODO", "Período: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        if (dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null) {
            params.put("PERIODO", "Período: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        if (dtPgtoBaixa != null && dtPgtoBaixa[0] != null && dtPgtoBaixa[1] != null) {
            params.put("PERIODO", "Período: " + dtPgtoBaixa[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dtPgtoBaixa[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
        }

        List<TableMap> dados = obterDadosRelatorio(Emprs, classe, tp, tipoDoc, numeroInicial, numeroFinal, entidade, departamento, naturezas, dataVenc, opcVcto, dataEmissao, tipoData, dtPgtoBaixa, port, oper, ordem);


        if ( exportar == 0) {
            params.put("TITULO_RELATORIO", "CST - Documentos Ordenados por Valor");
            return gerarPDF("CST_Documentos_Ordenados_Por_Valor_PDF", dados);
        }


        return gerarXLSX("CST_Documentos_Ordenados_Por_Valor_Excel", dados);
    }

    private obterDadosRelatorio(List<Long> Emprs, Integer classe, Integer tp, List<Long> tipoDoc, Integer numeroInicial, Integer numeroFinal, List<Long> entidade, List<Long> departamento, List<Long> naturezas, LocalDate[] dataVenc, Integer opcVcto, LocalDate[] dataEmissao, Integer tipoData, LocalDate[] dtPgtoBaixa, List<Long> port, List<Long> oper, Integer ordem) {
        String campoData = opcVcto == 0 ? "daa01dtVctoN, " : "daa01dtVctoR, ";
        String orderBy = "ORDER BY " + campoData + "daa01valor, abb01num, abb01parcela, abb01serie, abb01quita"

        String whereReceberRecebidos = null;
        if (classe.equals(0) || classe.equals(1) || classe.equals(4)) {
            whereReceberRecebidos = " AND daa01rp = " + Daa01.RP_RECEBER;
        } else {
            whereReceberRecebidos = " AND daa01rp = " + Daa01.RP_PAGAR;
        }

        String whereReceberPagar = "";
        if (classe.equals(0) || classe.equals(2)) {
            whereReceberPagar = " AND daa01.daa01dtBaixa IS NULL ";
        }

        String whereRecebidosPagos = "";
        if (classe.equals(1) || classe.equals(3)) {
            whereRecebidosPagos = " AND daa01.daa01dtBaixa IS NOT NULL";
        }

        List<Long> idsGc = obterGCbyEmpresa(Emprs, "Da");

        String whereNum = numeroInicial != null && numeroFinal != null ? " AND abb01num BETWEEN  :numeroInicial and :numeroFinal " : "";
        String whereIdEmpresa = idsGc != null && idsGc.size() > 0 ? " WHERE aac01id IN (:idEmprs)" : getSamWhere().getWherePadrao(" WHERE ", Daa01.class);
        String whereIdDepartamento = departamento != null && departamento.size() > 0 ? " AND abb11id IN (:idDepartamento)" : "";
        String whereIdDocumento = tipoDoc != null && tipoDoc.size() > 0 ? " AND aah01id IN (:tipoDoc)" : "";
        String whereIdNatureza = naturezas != null && naturezas.size() > 0 ? " AND abf10id IN (:idNaturezas)" : "";
        String whereIdEntidade = entidade != null && entidade.size() > 0 ? " AND abe01id IN (:idEntidade)" : "";
        String whereVencimento = "";
        if (opcVcto == 0) {
            whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01dtVctoN >= '" + dataVenc[0] + "' AND daa01dtVctoN <= '" + dataVenc[1] + "'" : "";
        } else {
            whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01dtVctoR >= '" + dataVenc[0] + "' AND daa01dtVctoR <= '" + dataVenc[1] + "'" : "";
        }
        String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? " AND abb01data >= '" + dataEmissao[0] + "' AND abb01data <= '" + dataEmissao[1] + "'" : "";
        String wherePort = port != null && port.size() != 0 ? "AND abf15id IN (:idPort) " : "";
        String whereOper = oper != null && oper.size() != 0 ? "AND abf16id IN (:idOper) " : "";
        String whereDataPagBaixa = "";
        if (dtPgtoBaixa != null && dtPgtoBaixa[0] != null) {
            if (tipoData.equals(0)) {
                whereDataPagBaixa = dtPgtoBaixa[0] != null && dtPgtoBaixa[1] != null ? " AND daa01dtPgto >= '" + dtPgtoBaixa[0] + "' AND daa01dtPgto <= '" + dtPgtoBaixa[1] + "'" : "";
            } else {
                whereDataPagBaixa = dtPgtoBaixa[0] != null && dtPgtoBaixa[1] != null ? " AND daa01dtBaixa >= '" + dtPgtoBaixa[0] + "' AND daa01dtBaixa <= '" + dtPgtoBaixa[1] + "'" : "";
            }
        }

        Parametro paramEmpresa = Emprs != null && Emprs.size() > 0 ? Parametro.criar("idEmprs", Emprs) : Parametro.criar("idEmprs", idsGc);
        Parametro paramDepartamento = departamento != null && departamento.size() > 0 ? Parametro.criar("idDepartamento", departamento) : null;
        Parametro paramDocumento = tipoDoc != null && tipoDoc.size() > 0 ? Parametro.criar("tipoDoc", tipoDoc) : null;
        Parametro paramNaturezas = naturezas != null && naturezas.size() > 0 ? Parametro.criar("idNaturezas", naturezas) : null;
        Parametro paramEntidade = entidade != null && entidade.size() > 0 ? Parametro.criar("idEntidade", entidade) : null;
        Parametro paramnumeroInicial = Parametro.criar("numeroInicial", numeroInicial);
        Parametro paramnumeroFinal = Parametro.criar("numeroFinal", numeroFinal);
        Parametro paramPort = port != null ? Parametro.criar("idPort", port) : null;
        Parametro paramOper = oper != null ? Parametro.criar("idOper", oper) : null;

        String fieldDias = opcVcto == 0 ? "daa01dtVctoN - CURRENT_DATE AS dias " : "daa01dtVctoR - CURRENT_DATE AS dias ";

        String sql = " SELECT DISTINCT daa01id, abe01codigo, abe01na, abb01num,abb01parcela, abb01data, abb01serie, abb01quita,  " +
                "aah01codigo, aah01nome, abe01ni AS cnpj, " +
                " daa01dtVctoN, daa01dtVctoR, daa01dtPgto, daa01dtBaixa, daa01valor AS valor, " +
                " aac10codigo AS codemp, aac10na AS nomeemp, abf15codigo, abf15nome, abf16codigo, abf16nome, " +
                " daa01previsao, daa01json, " +
                " CAST(daa01json ->> 'juros' AS numeric(18,6)) + CAST(daa01json ->> 'multa' AS numeric(18,6)) + CAST(daa01json ->> 'encargos' AS numeric(18,6)) AS jme, " +
                " CASE WHEN CAST(daa01json ->> 'desconto' AS numeric(18,6)) IS NULL THEN 0.000000 ELSE CAST(daa01json ->> 'desconto' AS numeric(18,6)) END AS desconto, " +
                " CASE WHEN CAST(daa01json ->> 'desconto' AS numeric(18,6)) IS NULL THEN daa01valor + 0.000000 ELSE daa01valor + CAST(daa01json ->> 'desconto' AS numeric(18,6)) END AS liquido, " +
                fieldDias +
                " FROM Daa01 " +
                " INNER JOIN aac01 ON daa01gc = aac01id" +
                " INNER JOIN aac10 ON daa01eg = aac10id " +
                " LEFT JOIN daa0101 on daa0101doc = daa01id " +
                " LEFT JOIN daa01011 on daa01011.daa01011depto = daa0101id " +
                " LEFT JOIN abb01 ON abb01id = daa01central" +
                " LEFT JOIN abe01 ON abe01id = abb01ent" +
                " LEFT JOIN aah01 ON aah01id = abb01tipo" +
                " LEFT JOIN Abb11 ON abb11id = daa0101depto " +
                " LEFT JOIN Abf10 ON abf10.Abf10id = daa01011.daa01011nat " +
                " LEFT JOIN Abf15 ON daa01port = abf15id " +
                " LEFT JOIN Abf16 ON daa01oper = abf16id " +
                whereIdEmpresa +
                whereReceberRecebidos +
                whereReceberPagar +
                whereRecebidosPagos +
                whereIdDepartamento +
                whereIdDocumento +
                whereNum +
                whereIdNatureza +
                whereIdEntidade +
                whereVencimento +
                whereEmissao +
                whereDataPagBaixa +
                wherePort +
                whereOper +
                orderBy;

        List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramEmpresa, paramDepartamento, paramDocumento, paramNaturezas, paramEntidade, paramnumeroInicial, paramnumeroFinal, paramPort, paramOper);
        return receberDadosRelatorio;

    }

    private String buscarTipoClasse(Integer classe) {
        switch (classe) {
            case 0:
                "Documentos A Receber";
                break;
            case 1:
                "Documentos Recebidos";
                break;
            case 2:
                "Documentos A Pagar";
                break;
            case 3:
                "Documentos Pagos";
                break;
            case 4:
                "Documentos a Receber e Recebidos";
                break;
            case 5:
                "Documentos a Pagar e Pagos";
                break
        }
    }

    public List<Long> obterGCbyEmpresa(List<Long> empresa, String tabela) {
        Criterion whereEmpresa = empresa != null && empresa.size() > 0 ? Criterions.in("aac1001empresa", empresa) : Criterions.in("aac1001empresa", obterEmpresaAtiva().aac10id);
        Criterion whereTabela = tabela != null ? Criterions.eq("aac1001tabela", tabela) : null;

        return getSession().createCriteria(Aac1001.class)
                .addFields("aac1001gc")
                .addWhere(whereEmpresa)
                .addWhere(whereTabela)
                .getList(ColumnType.LONG);

    }
}
//meta-sis-eyJkZXNjciI6IkNTVCAtIERvY3VtZW50b3MgT3JkZW5hZG9zIHBvciBWYWxvciIsInRpcG8iOiJyZWxhdG9yaW8ifQ==