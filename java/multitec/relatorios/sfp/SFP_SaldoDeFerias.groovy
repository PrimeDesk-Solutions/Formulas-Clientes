package multitec.relatorios.sfp;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.fb.Fbc01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Saldo de Férias
 * @author Lucas Eliel
 * @since 27/01/2019
 * @version 1.0
 */

public class SFP_SaldoDeFerias extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Saldo de Férias";
	}

	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("ordenacao", "0");

		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		Integer ordenacao = getInteger("ordenacao");

		List<Fbc01> listFbc01 = findIdsFbc01sByTrabalhadoresAndDeptos(idsTrabalhadores, idsDeptos);
		List<Long> idsFbc01s = null;
		if(listFbc01 != null && !listFbc01.isEmpty()) {
			idsFbc01s = new ArrayList<>();

			for(int i = 0; i < listFbc01.size(); i++) {
				idsFbc01s.add(listFbc01.get(i).getFbc01id());
			}
		}

		List<TableMap> dados = getDadosFbc01sBySaldoDeFerias(idsTrabalhadores, idsDeptos, ordenacao == 0 ? false : true, idsFbc01s);
		if(dados != null && !dados.isEmpty()) {
			for(TableMap map : dados) {
				Integer saldo = map.getInteger("fbc0101diasDireito") - (map.getInteger("fbc0101diasFerias") + map.getInteger("fbc0101diasAbono"));
				map.put("saldo", saldo);
			}
		}

		return gerarPDF(ordenacao == 1 ? "SFP_SaldoDeFerias_R2" : "SFP_SaldoDeFerias_R1", dados);
	}

	/**Método Diverso
	 * @return List TableMap (Query da busca do Período Aquisitivo pelo Trabalhador e Por Departamento)
	 */
	private List<Fbc01> findIdsFbc01sByTrabalhadoresAndDeptos(List<Long> idsTrabalhadores, List<Long> idsDeptos) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";

		String sql = "SELECT MAX(fbc01id) as fbc01id, fbc01trab " +
				"FROM Fbc01 " +
				"LEFT JOIN FBC0101 ON fbc0101pa = FBC01ID " +
				"INNER JOIN Abh80 ON abh80id = fbc01trab " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"WHERE abh80sit <> 1 " +
				"and fbc0101diasDireito <> (fbc0101diasFerias + fbc0101diasAbono)" +
				whereTrabalhadores + whereDeptos +
				getSamWhere().getWherePadrao("AND", Fbc01.class) +
				"GROUP BY fbc01trab ";

		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);

		return query.getList(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return List TableMap (Query da busca do Período Aquisitivo pelo Saldo de Férias)
	 */
	private List<TableMap> getDadosFbc01sBySaldoDeFerias(List<Long> idsTrabalhadores, List<Long> idsDeptos, boolean ordenarPorDepto, List<Long> idsFbc01s) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";
		String sqlIds = idsFbc01s != null && idsFbc01s.size() > 0 ? "AND fbc01id IN (:idsFbc01s) " : "";
		String sqlOrdem = ordenarPorDepto ? " ORDER BY abb11codigo, abh80codigo, fbc0101dtPagto " : " ORDER BY abh80codigo, fbc0101dtPagto ";

		String sql = "SELECT fbc01trab, fbc01pai, fbc01paf, fbc0101pgi, fbc0101pgf, fbc0101pbi, fbc0101pbf, fbc0101obs, fbc0101dtPagto, fbc0101diasDireito, fbc0101diasFerias, fbc0101diasAbono, " +
				"abh80codigo, abh80nome, abb11codigo, abb11nome " +
				"FROM Fbc0101 " +
				"INNER JOIN Fbc01 ON fbc01id = fbc0101pa "+
				"INNER JOIN Abh80 ON abh80id = fbc01trab "+
				"INNER JOIN Abb11 ON abb11id = abh80depto "+
				"WHERE fbc0101diasDireito <> (fbc0101diasFerias + fbc0101diasAbono) AND abh80sit <> 1 " + sqlIds +
				"AND fbc01status = 2 " +
				whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				sqlOrdem;

		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsFbc01s != null && idsFbc01s.size() > 0) query.setParameter("idsFbc01s", idsFbc01s);

		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFNhbGRvIGRlIEbDqXJpYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=