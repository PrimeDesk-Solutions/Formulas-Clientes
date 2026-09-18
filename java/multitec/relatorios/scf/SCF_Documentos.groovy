package multitec.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.da.Daa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SCF_Documentos extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Documentos";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("ordem", "0");
		filtrosDefault.put("classe","0");
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");
		filtrosDefault.put("tipoData","0");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer ordem = getInteger("ordem");
		Integer classe = getInteger("classe");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> documento = getListLong("documento");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		LocalDate[] dataEmissao = getIntervaloDatas("dataEmissao");
		Integer tipoData = getInteger("tipoData");
		LocalDate[] data = getIntervaloDatas("data");

		switch (classe) {
			case 0: params.put("TITULO_RELATORIO", "Documentos à Receber");
				break;
			case 1: params.put("TITULO_RELATORIO", "Documentos Recebidos");
				break;
			case 2: params.put("TITULO_RELATORIO", "Documentos à Pagar");
				break;
			case 3: params.put("TITULO_RELATORIO", "Documentos Pagos");
				break;
		}

		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		if (dataEmissao != null) {
			params.put("PERIODO", "Período Emissão: " + dataEmissao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataEmissao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		if (dataVenc != null) {
			params.put("PERIODO", "Período Vencimento: " + dataVenc[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + dataVenc[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		if (data != null) {
			if (tipoData == 0) {
				params.put("PERIODO", "Período Pagamento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
			} else {
				params.put("PERIODO", "Período Recebimento: " + data[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + data[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
			}
		}

		List<TableMap> listaDocumentos = buscaDocumentos(ordem, classe, numeroInicial, numeroFinal, documento, entidade, dataVenc, dataEmissao,  tipoData, data);

		for (int i = 0; i < listaDocumentos.size(); i++) {
			String previsao = null;

			if (listaDocumentos.get(i).getInteger("daa01previsao").equals(1)) {
				previsao = "P";
			} else {
				previsao = "R";
			}
			listaDocumentos.get(i).put("previsao", previsao);
			
			Long dias = DateUtils.dateDiff(MDate.date(), listaDocumentos.get(i).getDate("daa01dtVctoN"), ChronoUnit.DAYS);
			listaDocumentos.get(i).put("dias", dias);
		}

		return gerarPDF("SCF_Documentos", listaDocumentos);
	}

	private List<TableMap> buscaDocumentos(Integer ordem, Integer classe, Integer numeroInicial, Integer numeroFinal, List<Long> documento, List<Long> entidade, LocalDate[] dataVenc, LocalDate[] dataEmissao, Integer tipoData, LocalDate[] data) {
		String whereRp = classe.equals(0) || classe.equals(1) ? " WHERE daa01.daa01rp = 0 " : " WHERE daa01.daa01rp = 1 ";
		String whereDtBaixa = classe.equals(0) || classe.equals(2) ? " AND daa01.daa01dtBaixa IS NULL " : classe.equals(1) || classe.equals(3) ? " AND daa01.daa01dtBaixa IS NOT NULL " : "";
		String whereTipoDoc = documento != null && documento.size() != 0 ? " AND aah01.aah01id IN (:idDocumentos) " : "";
		String whereEnt = entidade != null && entidade.size() != 0 ? " AND abe01.abe01id IN (:idEntidade) " : "";
		String whereVenc = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? getWhereDataInterval("AND", dataVenc, "daa01.daa01dtVctoR") : "";
		String whereEmissao = dataEmissao != null && dataEmissao[0] != null && dataEmissao[1] != null ? getWhereDataInterval("AND", dataEmissao, "abb01.abb01data") : "";
		String whereData = data != null && data[0] != null && data[1] != null ? 
																tipoData == 0 ? " AND daa01.daa01dtPgto >= '" + data[0] + "' and daa01.daa01dtPgto <= '" + data[1] + "' " 
																: " AND daa01.daa01dtBaixa >= '" + data[0] + "' and daa01.daa01dtBaixa <= '" + data[1] + "' " : "";
		String orderBy = ordem.equals(0) ? " ORDER BY abb01.abb01num, abb01parcela" : " ORDER BY daa01.daa01dtVctoN";
																
		String campoValor = classe.equals(0) || classe.equals(2) ? "daa01.daa01valor" : "daa01.daa01liquido";
		
		String sql = " SELECT abe01.abe01codigo, abe01.abe01na, aah01.aah01codigo, aah01.aah01nome, abb01.abb01num, abb01.abb01serie, " +
				" abb01.abb01parcela, abb01.abb01quita, abb01.abb01data, daa01.daa01dtVctoN, daa01.daa01dtVctoR, daa01.daa01dtPgto, " +
				" daa01.daa01dtBaixa, " + campoValor + " AS valor, daa01.daa01previsao, abf15.abf15codigo, abf15.abf15nome, abf16.abf16codigo, abf16.abf16nome " +
				" FROM daa01 AS daa01 " +
				" LEFT JOIN abb01 AS abb01 ON abb01id = daa01central " +
				" LEFT JOIN abe01 AS abe01 ON abe01id = abb01ent " +
				" LEFT JOIN abf15 AS abf15 ON abf15id = daa01port " +
				" LEFT JOIN abf16 AS abf16 ON abf16id = daa01oper " +
				" LEFT JOIN aah01 AS aah01 ON aah01id = abb01tipo " +
				whereRp + whereDtBaixa + " AND abb01.abb01num >= :numeroInicial AND abb01.abb01num <= :numeroFinal " +
				whereTipoDoc + whereEnt + whereVenc + whereEmissao + whereData + getSamWhere().getWherePadrao("AND", Daa01.class) + orderBy;

		Query query = getSession().createQuery(sql);
		query.setParameter("numeroInicial", numeroInicial);
		query.setParameter("numeroFinal", numeroFinal);

		if(documento != null && ! documento.isEmpty()) query.setParameter("idDocumentos", documento);
		if(entidade != null && ! entidade.isEmpty()) query.setParameter("idEntidade", entidade);

		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIERvY3VtZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=