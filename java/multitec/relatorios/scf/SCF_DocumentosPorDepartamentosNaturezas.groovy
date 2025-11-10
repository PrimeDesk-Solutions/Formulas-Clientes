package multitec.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.da.Daa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_DocumentosPorDepartamentosNaturezas extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos Por Departamentos e Naturezas";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("classe","0");
		filtrosDefault.put("tipoData","0");
		filtrosDefault.put("agrupamento","D");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {

		Integer classe = getInteger("classe");
		List<Long> departamento = getListLong("departamento");
		List<Long> naturezas = getListLong("naturezas");
		List<Long> documento = getListLong("documento");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Integer tipoData = getInteger("tipoData");
		LocalDate[] data = getIntervaloDatas("dataPagBaixa");
		String isAgrupamento = get("agrupamento");

		params.put("TITULO_RELATORIO", "Documentos por Departamentos e Naturezas");
		params.put("TIPOCLASSE", buscarTipoClasse(classe));
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (dataVenc != null && dataVenc[0] != null && dataVenc[1] != null) {
			params.put("PERIODO", "Período: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		if (dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null) {
			params.put("PERIODO", "Período: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		if (data != null && data[0] != null && data[1] != null) {
			params.put("PERIODO", "Período: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		List<TableMap> dados = obterDadosRelatorio(classe, departamento, naturezas, documento, entidade, dataVenc, dataEmissao,  tipoData, data, isAgrupamento == "D");

		if (isAgrupamento == "D") {
			return gerarPDF("SCF_DocumentosPorDepartamentosNaturezas_R1", dados);
		} else {
			return gerarPDF("SCF_DocumentosPorDepartamentosNaturezas_R2", dados);
		}
	}

	private List<TableMap> obterDadosRelatorio(Integer classe, List<Long> departamento, List<Long> naturezas, List<Long> documento, List<Long> entidade, LocalDate[] dataVenc,
			LocalDate[] dataEmissao, Integer tipoData, LocalDate[] data, boolean isByDepto) {

			
		String orderBy = isByDepto ? " abb11.abb11codigo ASC, abf10.abf10codigo ASC, aah01.aah01codigo ASC, abb01.abb01num ASC, abb01.abb01parcela ASC, daa01.daa01dtVctoN ASC" : 
									 " abf10.abf10codigo ASC, abb11.abb11codigo ASC, aah01.aah01codigo ASC, abb01.abb01num ASC, abb01.abb01parcela ASC, daa01.daa01dtVctoN ASC";
		
		String whereReceberRecebidos = null;
		if (classe.equals(0) || classe.equals(1)) {
			whereReceberRecebidos = " WHERE daa01.daa01rp = " + Daa01.RP_RECEBER;
		} else {
			whereReceberRecebidos = " WHERE daa01.daa01rp = " + Daa01.RP_PAGAR;
		}

		String whereReceberPagar = "";
		if (classe.equals(0) || classe.equals(2)) {
			whereReceberPagar = " AND daa01.daa01dtBaixa IS NULL ";
		}

		String whereRecebidosPagos = "";
		if (classe.equals(1) || classe.equals(3)) {
			whereRecebidosPagos = " AND daa01.daa01dtBaixa IS NOT NULL";
		}

		String whereIdDepartamento = departamento != null && departamento.size() > 0 ? " AND abb11.abb11id IN (:idDepartamento)": "";
		String whereIdDocumento = documento != null && documento.size() > 0 ? " AND aah01.aah01id IN (:idDocumentos)": "";
		String whereIdNatureza = naturezas != null && naturezas.size() > 0 ? " AND abf10.abf10id IN (:idNaturezas)": "";
		String whereIdEntidade = entidade != null && entidade.size() > 0 ? " AND abe01.abe01id IN (:idEntidade)": "";
		String whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " AND daa01.daa01dtVctoN >= '" + dataVenc[0] + "' AND daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? " AND abb01.abb01data >= '" + dataEmissao[0] + "' AND abb01.abb01data <= '" + dataEmissao[1] + "'": "";

		String whereDataPagBaixa = "";
		if (data != null && data[0] != null) {
			if (tipoData.equals(0)) {
				whereDataPagBaixa = data[0] != null && data[1] != null ? " AND daa01.daa01dtPgto >= '" + data[0] + "' AND daa01.daa01dtPgto <= '" + data[1] + "'": "";
			} else {
				whereDataPagBaixa = data[0] != null && data[1] != null ? " AND daa01.daa01dtBaixa >= '" + data[0] + "' AND daa01.daa01dtBaixa <= '" + data[1] + "'": "";
			}
		}


		Parametro paramDepartamento = departamento != null && departamento.size() > 0 ? Parametro.criar("idDepartamento", departamento) : null;
		Parametro paramDocumento = documento != null && documento.size() > 0 ? Parametro.criar("idDocumentos", documento) : null;
		Parametro paramNaturezas = naturezas != null && naturezas.size() > 0 ? Parametro.criar("idNaturezas", naturezas) : null;
		Parametro paramEntidade = entidade != null && entidade.size() > 0 ? Parametro.criar("idEntidade", entidade) : null;

		String sql = " SELECT abe01.abe01codigo, abe01.abe01na, abb01.abb01num, abb01.abb01parcela, " +
					 " abb11.abb11codigo, abb11.abb11nome, aah01.aah01codigo, aah01.aah01na, " + 
					 " abf10.abf10codigo, abf10.abf10nome, daa01.daa01dtVctoN, daa01011.daa01011valor AS valor " +
					" FROM Daa01 daa01 " +
					" INNER JOIN daa0101 as daa0101 on daa0101.daa0101doc = daa01.daa01id " +
					" INNER JOIN daa01011 as daa01011 on daa01011.daa01011depto = daa0101.daa0101id " +
					" LEFT JOIN abb01 AS abb01 ON abb01id = daa01central" +
					" LEFT JOIN abe01 AS abe01 ON abe01id = abb01ent" +
					" LEFT JOIN aah01 AS aah01 ON aah01id = abb01tipo" +
					" LEFT JOIN Abb11 abb11 on abb11.abb11id = daa0101.daa0101depto" +
					" LEFT JOIN Abf10 abf10 on abf10.Abf10id = daa01011.daa01011nat" +
					whereReceberRecebidos +
					whereReceberPagar +
					whereRecebidosPagos +
					whereIdDepartamento +
					whereIdDocumento +
					whereIdNatureza +
					whereIdEntidade +
					whereVencimento +
					whereEmissao +
					whereDataPagBaixa +
					getSamWhere().getWherePadrao("AND", Daa01.class) +
					" ORDER BY " + orderBy;

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramDepartamento, paramDocumento, paramNaturezas, paramEntidade);
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
		}
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgUG9yIERlcGFydGFtZW50b3MgZSBOYXR1cmV6YXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=