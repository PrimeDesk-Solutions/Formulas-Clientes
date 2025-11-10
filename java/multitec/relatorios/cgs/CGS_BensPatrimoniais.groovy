package multitec.relatorios.cgs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import br.com.multiorm.Query;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abb20;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class CGS_BensPatrimoniais extends RelatorioBase{

	@Override
	public DadosParaDownload executar() {
		List<Long> idsBens = getListLong("bens");
		List<Long> idsDepartamentos = getListLong("departamentos");
		LocalDate[] datasAquisicao = getIntervaloDatas("datasAquisicao");
		LocalDate[] datasBaixa = getIntervaloDatas("datasBaixa");
		boolean baixa = get("baixa");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Bens Patrimoniais");
		if (datasAquisicao != null) {
			params.put("PERIODO", "Período: " + datasAquisicao[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + datasAquisicao[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		}

		String whereAquisicao = datasAquisicao != null ? getWhereDataInterval("AND", datasAquisicao, "abb20.abb20aquis") : "";
		String whereBaixa = baixa && datasBaixa != null ? " AND abb20.abb20baixa IS NOT NULL " + getWhereDataInterval("AND", datasBaixa, "abb20.abb20baixa") : "";
		String whereIdsBens = idsBens != null ? " AND abb20.abb20id IN (:idsBens) ": "";
		String whereIdsDep = idsDepartamentos != null ? " AND (abb11.abb11id IN (:idsDepartamentos) OR abb11.abb11id IS NULL) ": "";
		
		Query query = getSession().createQuery(" SELECT abb20.abb20codigo, abb20.abb20nome, abb20.abb20chapa, " +
				 				 " abb20.abb20aquis, abb20.abb20baixa, abb11.abb11codigo FROM Abb20 AS abb20 " +
								 " LEFT JOIN Abb11 AS abb11 ON abb11id = abb20depto" +
								 getSamWhere().getWherePadrao(" WHERE ", Abb20.class) +
								 whereAquisicao + whereIdsBens + whereIdsDep + whereBaixa + 
				 				 " ORDER BY abb20.abb20codigo");
		
		if(datasAquisicao != null && datasAquisicao[0] != null)query.setParameter("dtAquiIni", datasAquisicao[0]);
		if(datasAquisicao != null && datasAquisicao[1] != null)query.setParameter("dtAquiFim", datasAquisicao[1]);
		if(baixa && datasBaixa != null && datasBaixa[0] != null)query.setParameter("dtBaixaIni", datasBaixa[0]);
		if(baixa && datasBaixa != null && datasBaixa[1] != null)query.setParameter("dtBaixaFim", datasBaixa[1]);
		if(idsBens != null)query.setParameter("idsBens", idsBens);
		if(idsDepartamentos != null)query.setParameter("idsDepartamentos", idsDepartamentos);
		
		List<TableMap> dados = query.getListTableMap();
		for (int i = 0; i < dados.size(); i++) {
			int next = i + 1;
			if(dados.get(i).getString("abb20codigo").length() == 2) {
				String codigo = dados.get(i).getString("abb20codigo");
				TableMap proximo = new TableMap();
				try {
					proximo = dados.get(next);
					if(proximo.getString("abb20codigo").length() == 2 && !proximo.getString("abb20codigo").equalsIgnoreCase(codigo)) {
						dados.remove(i);
						i--;
					}
				}catch (IndexOutOfBoundsException err) {
					dados.remove(i);
				}
			}
		}
		return gerarPDF("CGS_BensPatrimoniais", dados);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("baixa", false);
		return Utils.map("filtros", filtrosDefault);
	}
	@Override
	public String getNomeTarefa() {
		return "CGS - Bens Patrimoniais";
	}

}
//meta-sis-eyJkZXNjciI6IkNHUyAtIEJlbnMgUGF0cmltb25pYWlzIiwidGlwbyI6InJlbGF0b3JpbyJ9