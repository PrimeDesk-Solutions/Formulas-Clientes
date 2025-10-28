package multitec.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.da.Daa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_DocumentosPorMovimento extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos por Movimento Bancário";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("numeroInicial", "000000001");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("movInicial", "000000001");
		filtrosDefault.put("movFinal", "999999999");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override	
	public DadosParaDownload executar() {
		List<Long> codigoBanco = getListLong("banco");
		List<Long> documentos = getListLong("documentos");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		Integer movInicial = getInteger("movInicial");
		Integer movFinal = getInteger("movFinal");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		
		params.put("TITULO_RELATORIO", "Documentos por Movimento Bancário");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (dataVenc != null) {
			params.put("PERIODO", "Período Vencimento: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		
		List<TableMap> dados = obterDadosRelatorio(codigoBanco, documentos, entidade, dataVenc, dataEmissao, numeroInicial, numeroFinal, movInicial, movFinal);
		
		return gerarPDF("SCF_DocumentosPorMovimento", dados);

	}
	
	private List<TableMap> obterDadosRelatorio(List<Long> codigoBanco, List<Long> documentos, List<Long> entidade, LocalDate[] dataVenc, LocalDate[] dataEmissao, Integer numeroInicial, Integer numeroFinal, Integer movInicial, Integer movFinal) {
		
		String whereIdCodigoBanco = codigoBanco != null && codigoBanco.size() > 0 ? " and abf01.abf01id IN (:idCodigoBanco)": "";
		String whereMovimento = movInicial != null && movFinal != null ? " and daa0102.daa0102movim >= '" + movInicial + "' and daa0102.daa0102movim <= '" + movFinal + "'": "";
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? " and abb01.abb01data >= '" + dataEmissao[0] + "' and abb01.abb01data <= '" + dataEmissao[1] + "'": "";
		String whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " and daa01.daa01dtVctoN >= '" + dataVenc[0] + "' and daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";
		String whereIdDocumento = documentos != null && documentos.size() > 0 ? " and aah01.aah01id IN (:idDocumentos)": "";
		String whereIdEntidade = entidade != null && entidade.size() > 0 ? " and abe01.abe01id IN (:idEntidade)": "";
		
		Parametro paramDocumento = documentos != null && documentos.size() > 0 ? Parametro.criar("idDocumentos", documentos) : null;
		Parametro paramEntidade = entidade != null && entidade.size() > 0 ? Parametro.criar("idEntidade", entidade) : null;
		Parametro parametroBanco = codigoBanco != null && codigoBanco.size() > 0 ? Parametro.criar("idCodigoBanco", codigoBanco) : null;
	
		String sql = " SELECT aah01.aah01codigo, aah01.aah01na, abb01.abb01data, abb01.abb01num, abb01.abb01parcela, abb01.abb01valor, abe01.abe01codigo, abe01.abe01na, abe01.abe01ni, abf01.abf01nome, abf01.abf01codigo, " +
				" daa0102.daa0102movim, daa0102.daa0102remData, daa0102.daa0102remNum, daa0102.daa0102retData, CONCAT  (abf01Movim.abf01codigo, ' ', abf01Movim.abf01nome) AS bancoMovim, daa01.daa01dtVctoN, daa01.daa01nossoNum, daa01.daa01valor " +
				" FROM Daa01 daa01 " +
				" INNER JOIN Abb01 abb01 ON abb01id = daa01central " +
				" INNER JOIN Abe01 abe01 ON abe01id = abb01ent " +
				" INNER JOIN Abf01 abf01 ON abf01id = daa01banco " +
				" LEFT JOIN Aah01 aah01 ON aah01id = abb01tipo " +
				" LEFT JOIN Daa0102 daa0102 ON daa0102.daa0102doc = daa01.daa01id " +
				" LEFT JOIN Abf01 abf01Movim ON abf01Movim.abf01id = daa0102.daa0102banco " +
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				whereIdCodigoBanco +
				whereMovimento +
				whereNumero +
				whereEmissao +
				whereVencimento +
				whereIdDocumento +
				whereIdEntidade +
				" ORDER BY abb01.abb01num ASC, abb01.abb01parcela ASC ";

	List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, paramDocumento, paramEntidade, parametroBanco);
	return receberDadosRelatorio;
	} 
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MgcG9yIE1vdmltZW50byBCYW5jw6FyaW8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=