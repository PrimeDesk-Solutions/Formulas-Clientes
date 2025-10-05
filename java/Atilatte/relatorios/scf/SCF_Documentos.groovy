package Atilatte.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.ValidacaoException;
import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion;
import br.com.multiorm.criteria.criterion.Criterions;
import sam.model.entities.da.Daa01
import org.apache.poi.hssf.util.HSSFColor;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.utils.Parametro
import java.util.Map;
import java.util.HashMap;
import sam.model.entities.aa.Aac1001;

public class SCF_Documentos extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos - LCR";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("classe","0");
		filtrosDefault.put("tp","0");
		filtrosDefault.put("opc","2");
		filtrosDefault.put("opcVcto","0");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("exportar","0");
		filtrosDefault.put("tipoData","0");
		filtrosDefault.put("agrupamento","D");
		filtrosDefault.put("ordem","0");
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> Emprs = getListLong("Emprs");
		Integer classe = getInteger("classe");
		Integer tp = getInteger("tp");
		Integer opc = getInteger("opc");
		Integer exportar = getInteger("exportar");
		List<Long> documento = getListLong("documento");
		Integer ordem = getInteger("ordem")
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidade = getListLong("entidade");
		List<Long> rep = getListLong("representante");
		List<Long> departamento = getListLong("departamento");
		List<Long> naturezas = getListLong("naturezas");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		Integer opcVcto = getInteger("opcVcto");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Integer tipoData = getInteger("tipoData");
		LocalDate[] data = getIntervaloDatas("dataPagBaixa");
		List<Long> port = getListLong("portador");
		List<Long> oper = getListLong("oper");
		boolean sintetico = get("sintetico");

		String isAgrupamento = get("agrupamento");

		params.put("TIPOCLASSE", buscarTipoClasse(classe));
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("OPCVCTO", opcVcto);
		params.put("SINTETICO", sintetico)

		if (dataVenc != null && dataVenc[0] != null && dataVenc[1] != null) {
			params.put("PERIODO", "Período: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		if (dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null) {
			params.put("PERIODO", "Período: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		if (data != null && data[0] != null && data[1] != null) {
			params.put("PERIODO", "Período: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = obterDadosRelatorio(Emprs, classe, tp, exportar, documento,  numeroInicial, numeroFinal, entidade, rep, departamento, naturezas, dataVenc, opcVcto, dataEmissao, tipoData, data, isAgrupamento, port, oper, ordem, opc);

		for(TableMap tm : dados) {
			TableMap daa01Json = tm.getTableMap("daa01json");
			if (daa01Json != null) {
				tm.putAll(daa01Json);
			}
		}

		if (isAgrupamento == "D" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Departamentos e Naturezas");
			return gerarPDF("SCF_Documentos_R1(PDF)", dados);
		}
		if (isAgrupamento == "D" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Departamentos e Naturezas");
			return gerarXLSX("SCF_Documentos_R1(Excel)", dados);
		}
		if (isAgrupamento == "N" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Naturezas e Departamentos");
			return gerarPDF("SCF_Documentos_R2(PDF)", dados);
		}
		if (isAgrupamento == "N" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Naturezas e Departamentos");
			return gerarXLSX("SCF_Documentos_R2(Excel)", dados);
		}
		if (isAgrupamento == "Nu" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Número");
			return gerarPDF("SCF_Documentos_R3(PDF)", dados);
		}
		if (isAgrupamento == "Nu" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Número");
			return gerarXLSX("SCF_Documentos_R3(Excel)", dados);
		}
		if (isAgrupamento == "E" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Entidades");
			return gerarPDF("SCF_Documentos_R4(PDF)", dados);
		}
		if (isAgrupamento == "E" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Entidades");
			return gerarXLSX("SCF_Documentos_R4(Excel)", dados);
		}
		if (isAgrupamento == "T" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Tipos de Documentos");
			return gerarPDF("SCF_Documentos_R5(PDF)", dados);
		}
		if (isAgrupamento == "T" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Tipos de Documentos");
			return gerarXLSX("SCF_Documentos_R5(Excel)", dados);
		}
		if (isAgrupamento == "V" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Vencimento Nominal");
			return gerarPDF("SCF_Documentos_R6(PDF)", dados);
		}
		if (isAgrupamento == "V" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Vencimento Nominal");
			return gerarXLSX("SCF_Documentos_R6(Excel)", dados);
		}
		if (isAgrupamento == "R" && exportar == 0) {
			params.put("TITULO_RELATORIO", "Documentos por Representantes");
			return gerarPDF("SCF_Documentos_R7(PDF)", dados);
		}
		if (isAgrupamento == "R" && exportar == 1) {
			params.put("TITULO_RELATORIO", "Documentos por Representantes");
			return gerarXLSX("SCF_Documentos_R7(Excel)", dados);
		}
	}

	private List<TableMap> obterDadosRelatorio(List<Long> Emprs, Integer classe, Integer exportar, Integer tp,  List<Long> documento, Integer numeroInicial, Integer numeroFinal, List<Long> entidade, List<Long> rep, List<Long> departamento, List<Long> naturezas, LocalDate[] dataVenc, Integer opcVcto, LocalDate[] dataEmissao, Integer tipoData, LocalDate[] data, String agrup, List<Long> port, List<Long> oper, Integer ordem, Integer opc) {

		String orderSeq = ordem == 0 ? "abb01.abb01num" : ordem == 1 ? opcVcto == 0 ? "daa01.daa01dtVctoN" : "daa01.daa01dtVctoR" : ordem == 2 ? "abe01.abe01codigo" : "";

		String orderBy = agrup == "D" ? " order by abb11.abb11codigo ASC, abf10.abf10codigo ASC, " + orderSeq :
				agrup == "N" ? " order by abf10.abf10codigo ASC, abb11.abb11codigo ASC, " + orderSeq :
						agrup == "Nu" ? "order by "  + orderSeq :
								agrup == "E" ? "order by abe01.abe01codigo ASC, abb01.abb01num ASC,  "  + orderSeq :
										agrup == "T" ? "order by aah01.aah01codigo ASC, abb01.abb01parcela ASC, "  + orderSeq :
												agrup == "V" ? "order by daa01.daa01dtVctoN ASC,daa01id, abb01.abb01parcela ASC, "  + orderSeq :
														agrup == "R" ? "order by abe01Rep.abe01codigo ASC, "  + orderSeq :"";

		String whereReceberRecebidos = null;
		if (classe.equals(0) || classe.equals(1) || classe.equals(4)) {
			whereReceberRecebidos = " AND daa01.daa01rp = " + Daa01.RP_RECEBER;
			
		} else {
			whereReceberRecebidos = " AND daa01.daa01rp = " + Daa01.RP_PAGAR;
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

		String whereOpc = opc != 2 ? " AND daa01previsao IN(:opc) " : "";
		String whereNum = numeroInicial != null && numeroFinal != null ? " AND abb01num BETWEEN  :numeroInicial and :numeroFinal " : "";
		String whereIdEmpresa = idsGc != null && idsGc.size() > 0 ? " WHERE aac01id in(:idEmprs)" : getSamWhere().getWherePadrao(" WHERE ", Daa01.class);
		String whereIdDepartamento = departamento != null && departamento.size() > 0 ? " AND abb11.abb11id IN (:idDepartamento)": "";
		String whereIdDocumento = documento != null && documento.size() > 0 ? " AND aah01.aah01id IN (:idDocumentos)": "";
		String whereIdNatureza = naturezas != null && naturezas.size() > 0 ? " AND abf10.abf10id IN (:idNaturezas)": "";
		String whereIdEntidade = entidade != null && entidade.size() > 0 ? " AND abe01.abe01id IN (:idEntidade)": "";
		String whereIdRep = rep != null && rep.size() > 0 ? " AND daa01rep0 IN (:idRep)": "";
		String whereVencimento = "";
		String whereDep = idsDep != null && idsDep.size() > 0 ? " AND daa0101id IN (:idsDep) " : "";
		String whereNat = idsNat != null && idsNat.size() > 0 ? " AND daa01011 IN (:idsNat) " : "";
		if(opcVcto == 0){
			whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01.daa01dtVctoN >= '" + dataVenc[0] + "' AND daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";
		}else{
			whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01.daa01dtVctoR >= '" + dataVenc[0] + "' AND daa01.daa01dtVctoR <= '" + dataVenc[1] + "'": "";
		}
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? " AND abb01.abb01data >= '" + dataEmissao[0] + "' AND abb01.abb01data <= '" + dataEmissao[1] + "'": "";
		String wherePort = port != null && port.size() != 0 ?  "AND abf15.abf15id IN (:idPort) " : "";
		String whereOper = oper != null && oper.size() != 0 ?  "AND abf16.abf16id IN (:idOper) " : "";
		String whereDataPagBaixa = "";
		
		if (data != null && data[0] != null) {
			if (tipoData.equals(0)) {
				whereDataPagBaixa = data[0] != null && data[1] != null ? " AND daa01.daa01dtPgto >= '" + data[0] + "' AND daa01.daa01dtPgto <= '" + data[1] + "'": "";
				
			} else {
				whereDataPagBaixa = data[0] != null && data[1] != null ? " AND daa01.daa01dtBaixa >= '" + data[0] + "' AND daa01.daa01dtBaixa <= '" + data[1] + "'": "";
			}
		}

		Parametro paramOpc = Parametro.criar("opc", opc);
		Parametro paramEmpresa = Emprs != null &&   Emprs.size() > 0 ? Parametro.criar("idEmprs",   Emprs) : Parametro.criar("idEmprs", idsGc);
		Parametro paramDepartamento = departamento != null && departamento.size() > 0 ? Parametro.criar("idDepartamento", departamento) : null;
		Parametro paramDocumento = documento != null && documento.size() > 0 ? Parametro.criar("idDocumentos", documento) : null;
		Parametro paramNaturezas = naturezas != null && naturezas.size() > 0 ? Parametro.criar("idNaturezas", naturezas) : null;
		Parametro paramEntidade = entidade != null && entidade.size() > 0 ? Parametro.criar("idEntidade", entidade) : null;
		Parametro paramRep = rep != null && rep.size() > 0 ? Parametro.criar("idRep", rep) : null;
		Parametro paramnumeroInicial =  Parametro.criar("numeroInicial", numeroInicial);
		Parametro paramnumeroFinal =  Parametro.criar("numeroFinal", numeroFinal);
		Parametro paramPort = port != null ? Parametro.criar("idPort", port) : null;
		Parametro paramOper = oper != null ? Parametro.criar("idOper", oper) : null;

		String sql = " SELECT DISTINCT daa01id,abe01.abe01codigo, abe01.abe01na, abb01.abb01num, abb01.abb01parcela, abb01.abb01data, " +
				(agrup == "D" || agrup == "N" ? "abb11.abb11codigo, abb11.abb11nome, abf10.abf10codigo, abf10.abf10nome, " : "")+
				"aah01.aah01codigo, aah01.aah01na, abe01.abe01ni as cnpj, "+
				" daa01.daa01dtVctoN, daa01.daa01dtVctoR, daa01.daa01dtPgto, daa01.daa01dtBaixa, "+
				(agrup == "D" || agrup == "N" ? "daa01011.daa01011valor AS valor, " : "daa01valor AS valor, ")+
				" aac10.aac10codigo as codemp, aac10.aac10na as nomeemp, abf15codigo, abf15nome, abf16codigo, abf16nome, " +
				" abe01Rep.abe01codigo as repcodigo, abe01Rep.abe01na as repna, daa01previsao, daa01json, "+
				" cast(daa01json ->> 'juros' as numeric(18,6)) + cast(daa01json ->> 'multa' as numeric(18,6)) + cast(daa01json ->> 'encargos' as numeric(18,6)) as jme, "+
				"case when cast(daa01json ->> 'desconto' as numeric(18,6)) is null then 0.000000 else cast(daa01json ->> 'desconto' as numeric(18,6)) end as desconto, "+
				(agrup == "D" || agrup == "N" ? "case when cast(daa01json ->> 'desconto' as numeric(18,6)) is null then daa01011.daa01011valor + 0.000000 else daa01011.daa01011valor + cast(daa01json ->> 'desconto' as numeric(18,6)) end AS liquido " : "case when cast(daa01json ->> 'desconto' as numeric(18,6)) is null then daa01valor + 0.000000 else daa01valor + cast(daa01json ->> 'desconto' as numeric(18,6)) end AS liquido ")+
				" FROM Daa01 daa01 " +
				" INNER JOIN aac01 as aac01 ON daa01gc = aac01id" +
				" INNER JOIN aac10 as aac10 ON daa01eg = aac10id "+
				" LEFT JOIN daa0101 as daa0101 on daa0101.daa0101doc = daa01.daa01id "+
				" LEFT JOIN daa01011 as daa01011 on daa01011.daa01011depto = daa0101.daa0101id "+
				" LEFT JOIN abb01 AS abb01 ON abb01id = daa01central" +
				" LEFT JOIN abe01 AS abe01 ON abe01id = abb01ent" +
				" LEFT JOIN abe01 AS abe01Rep ON abe01Rep.abe01id = daa01rep0" +
				" LEFT JOIN aah01 AS aah01 ON aah01id = abb01tipo" +
				" LEFT JOIN Abb11 abb11 on abb11.abb11id = daa0101.daa0101depto "+
				" LEFT JOIN Abf10 abf10 on abf10.Abf10id = daa01011.daa01011nat "+
				" LEFT JOIN Abf15 ON daa01port = abf15id"+
				" LEFT JOIN Abf16 ON daa01oper = abf16id"+
				whereIdEmpresa +
				whereReceberRecebidos +
				whereReceberPagar +
				whereRecebidosPagos +
				whereIdDepartamento +
				whereIdDocumento +
				whereNum +
				whereIdNatureza +
				whereIdEntidade +
				whereIdRep +
				whereVencimento +
				whereEmissao +
				whereDataPagBaixa +
				wherePort +
				whereOper +
				whereOpc +
				orderBy;



		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramEmpresa, paramDepartamento, paramDocumento, paramNaturezas, paramEntidade, paramRep, paramnumeroInicial, paramnumeroFinal, paramPort, paramOper, paramOpc);
		return receberDadosRelatorio;
	}

	private String buscarTipoClasse(Integer classe) {
		switch(classe) {
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
				"Docmuentos a Receber e Recebidos";
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
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgLSBMQ1IiLCJ0aXBvIjoicmVsYXRvcmlvIn0=