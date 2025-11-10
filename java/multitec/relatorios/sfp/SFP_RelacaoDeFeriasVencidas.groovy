package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fbc01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Férias Vencidas
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeFeriasVencidas extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Férias Vencidas";
	}

	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("dataBase", MDate.date());
		filtrosDefault.put("ordenacao", "0");
		filtrosDefault.put("considTrabAfastados", false);

		return Utils.map("filtros", filtrosDefault);
	}

	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		LocalDate dataBase = getLocalDate("dataBase");
		Integer ordenacao = getInteger("ordenacao");
		boolean considTrabAfastados = get("considTrabAfastados");

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("DATA_BASE", DateUtils.formatDate(dataBase));

		List<TableMap> dados = obterDadosFeriasVencidas(ordenacao, idsTrabalhadores, idsDeptos, considTrabAfastados, dataBase);
		if(dados != null && dados.size() > 0) {
			for(TableMap abh80 : dados) {

				Fbc01 fbc01 = obterUltimoCalculoFeriasPeloIdTrabalhadorETipoFerias(abh80.getLong("abh80id"));

				LocalDate perAqui;
				if(fbc01 != null) {
					perAqui = fbc01.getFbc01pai();
				} else {
					fbc01 = obterUltimoCalculoFeriasPeloIdTrabalhador(abh80.getLong("abh80id"));
					perAqui = fbc01.getFbc01pai();
				}

				LocalDate dtInicial = perAqui;
				perAqui = perAqui.plusYears(1);
				LocalDate dtFinal = perAqui.minusDays(1);

				perAqui = perAqui.plusYears(1);
				perAqui = perAqui.minusDays(30);

				LocalDate dtLimite = perAqui;

				perAqui = dtInicial;
				int mesesTrab = 0;
				while(DateUtils.dateDiff(perAqui, dataBase, ChronoUnit.DAYS) >= 15) {
					mesesTrab++;
					perAqui = perAqui.plusMonths(1);
				}

				if(ordenacao == 0) {
					LocalDate dt = dtLimite;

					String key = dt.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + "/" + abh80.getString("abh80codigo");

					abh80.put("key", key);
				}
				abh80.put("perAquiInicial", dtInicial);
				abh80.put("perAquiFinal", dtFinal);
				abh80.put("dataLimite", dtLimite);
				abh80.put("meses", mesesTrab);
			}
		}

		if(ordenacao == 0) {
			Collections.sort(dados, new Comparator<TableMap>() {
				@Override
				public int compare(TableMap tm1, TableMap tm2)
				{
					return  tm1.getString("key").compareTo(tm2.getString("key"));
				}
			});
		}

		return gerarPDF(ordenacao == 2 ? "SFP_RelacaoDeFeriasVencidas_R2" : "SFP_RelacaoDeFeriasVencidas_R1", dados);
	}

	/**Método Diverso
	 * @return 	Fbc01 (Query do Último Cálculo de Férias pelo id do Trabalhador e tipo de Férias)
	 */
	private Fbc01 obterUltimoCalculoFeriasPeloIdTrabalhadorETipoFerias(Long idAbh80){
		String sql = " SELECT * FROM Fbc01 AS fbc01 " +
				" WHERE fbc01trab = :idAbh80 AND fbc01status = 2 " + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				" ORDER BY fbc01pai ASC ";

		Query query = getSession().createQuery(sql);

		query.setParameter("idAbh80", idAbh80);
		query.setMaxResult(1);

		return query.getUniqueResult(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return 	Fbc01 (Query do Último Cálculo de Férias pelo id do Trabalhador e tipo de Férias)
	 */
	private Fbc01 obterUltimoCalculoFeriasPeloIdTrabalhador(Long idAbh80){
		String sql = " SELECT * FROM Fbc01 AS fbc01 " +
				" WHERE fbc01trab = :idAbh80 " + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				" ORDER BY fbc01pai DESC ";

		Query query = getSession().createQuery(sql);

		query.setParameter("idAbh80", idAbh80);
		query.setMaxResult(1);

		return query.getUniqueResult(ColumnType.ENTITY);
	}

	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Relação de Férias Vencidas)
	 */
	private List<TableMap> obterDadosFeriasVencidas(Integer ordenacao, List<Long> idsTrabalhadores, List<Long> idsDeptos, boolean considTrabAfastados, LocalDate dataBase){
		String ordem = ordenacao == 2 ? " ORDER BY abb11codigo, abh80codigo" : " ORDER BY abh80codigo, abb11codigo";
		String whereAfastados = considTrabAfastados ? "" : "AND abh80id NOT IN (SELECT fbb01trab FROM Fbb01 WHERE (:dataBase >= fbb01dtSai AND fbb01dtRet IS NULL)) ";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";

		String sql = "SELECT abh80id, abh80codigo, abh80nome, abh80dtAdmis, abb11codigo, abb11nome " +
				"FROM Abh80 " +
				"INNER JOIN Abb11 ON abb11id = abh80depto " +
				"WHERE abh80sit = 0 AND abh80tipo = 0 " +
				whereAfastados + whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				ordem;

		Query query = getSession().createQuery(sql);

		if(!considTrabAfastados) query.setParameter("dataBase", dataBase);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);

		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBGw6lyaWFzIFZlbmNpZGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9