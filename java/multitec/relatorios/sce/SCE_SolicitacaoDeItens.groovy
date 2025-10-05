package multitec.relatorios.sce;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10
import sam.model.entities.bc.Bcd01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SCE_SolicitacaoDeItens extends RelatorioBase {

	@Override
	public String getNomeTarefa() {
		return "SCE - Solicitação de Itens";
	}

	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("isMaterial", true);
		filtrosDefault.put("isProduto", true);
		filtrosDefault.put("isServico", true);
		filtrosDefault.put("numeroIni", "0000000000");
		filtrosDefault.put("numeroFin", "9999999999");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		List<Long> idsItens = getListLong("itens");
		Set<Integer> tiposItem = obterTiposDeItem();
		Boolean naoDestinado = get("naoDestinado");
		Boolean naoAutorizada = get("naoAutorizada");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Long numeroIni = getLong("numeroIni");
		Long numeroFin = getLong("numeroFin");
		
		params.put("TITULO_RELATORIO", "Solicitação de Itens");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		if (periodo != null) {
			params.put("PERIODO", "Período: " + periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}
		
		List<TableMap> solicitacaoDeItens = buscarDadosRelatorio(idsItens, tiposItem, naoDestinado, naoAutorizada, periodo, numeroIni, numeroFin);
		
		return gerarPDF("SCE_SolicitacaoDeItens", solicitacaoDeItens);
	}

	public List<TableMap> buscarDadosRelatorio(List<Long> idsItens, Set<Integer> tiposItem, Boolean naoDestinado, Boolean naoAutorizada, LocalDate[] periodo,  Long numeroIni, Long numeroFin) {
		
		List<TableMap> listSolicitacao = buscarSolicitacoesItens(idsItens, tiposItem, naoDestinado, naoAutorizada, periodo, numeroIni, numeroFin);

		return listSolicitacao;
	}

	public List<TableMap> buscarSolicitacoesItens(List<Long> idsItens, Set<Integer> tiposItem, Boolean naoDestinado, Boolean naoAutorizada, LocalDate[] periodo, Long numeroIni, Long numeroFin) {
		String wherePeriodo = periodo != null ? getWhereDataInterval("AND", periodo, "abb01data") : "";
		String whereUtiliza = periodo != null ? getWhereDataInterval(" AND", periodo, "bcd01dataUso") : "";
		String whereNaoDest = naoDestinado ? " AND bcd0101qtN = 0 AND bcd0101qtE = 0 " : "";
		String whereNaoAuto = naoAutorizada ? " AND bcd0101qtN > 0 " : "";

		String sql =  " SELECT abb01num, abb01data, abb11codigo, abb11nome, aab10nome, bcd01obs, bcd01status, abm01tipo, abm01codigo, abm01na, aam06codigo, bcd0101qtS, bcd0101qtN, bcd0101qtE, bcd01dataUso " +
		              "   FROM Bcd01 " +
					  "  INNER JOIN Abb01 ON Abb01id = bcd01central " +
					  "   LEFT JOIN Abb11 ON Abb11id = bcd01depto " +
					  "  INNER JOIN Aab10 ON Aab10id = bcd01user " + 
					  "   LEFT JOIN Bcd0101 ON bcd0101si = bcd01id " +
					  "   LEFT JOIN Abm01 ON abm01id = bcd0101item " +
					  "  INNER JOIN Aam06 ON aam06id = abm01umu " +
					  getSamWhere().getWherePadrao("WHERE", Bcd01.class) +
					  wherePeriodo + 
					  whereUtiliza +
					  "   AND abb01num BETWEEN :numeroIni AND :numeroFin " +
					  "   AND abm01tipo IN (:tiposItem) " +
					  whereNaoDest + 
					  whereNaoAuto +  
					  " ORDER BY abb01num, abm01tipo, abm01codigo, bcd01dataUso, abm01umu";
					  
		Query query = getSession().createQuery(sql); 

		query.setParameter("numeroIni", numeroIni);
		query.setParameter("numeroFin", numeroFin);
		query.setParameter("tiposItem", tiposItem);
		
		return query.getListTableMap();
	}

	private Set<Integer> obterTiposDeItem() {
		Set<Integer> tiposItem = new HashSet<Integer>();

		if ((Boolean) get("isMaterial"))
			tiposItem.add(0);
		if ((Boolean) get("isProduto"))
			tiposItem.add(1);
		if ((Boolean) get("isServico"))
			tiposItem.add(2);

		if (tiposItem.size() == 0) {
			tiposItem.add(0);
			tiposItem.add(1);
			tiposItem.add(2);
		}
		return tiposItem;
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFNvbGljaXRhw6fDo28gZGUgSXRlbnMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=