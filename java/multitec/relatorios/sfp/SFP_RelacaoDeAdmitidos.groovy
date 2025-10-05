package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Admitidos
 * @author Lucas Eliel
 * @since 30/04/2019
 * @version 1.0
 */

public class SFP_RelacaoDeAdmitidos extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Admitidos";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("isTrabalhador", true);
		filtrosDefault.put("isAutonomo", false);
		filtrosDefault.put("isProlabore", false);
		filtrosDefault.put("isTerceiros", false);
		filtrosDefault.put("ordenacao", "0");
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("considExperiencia", false);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		
		Integer ordenacao = getInteger("ordenacao");
		
		boolean considerarExp = get("considExperiencia");
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		LocalDate[] periodo = getIntervaloDatas("periodo");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		
		List<TableMap> dados = findAbh80sByRelacaoAdmitidos(idsTrabalhadores, idsDeptos, idsCargos, periodo, tiposTrab, ordenacao);	
		if(dados != null && !dados.isEmpty()) {
			for(TableMap mapPrinc : dados) {
				
				LocalDate dtAdmis = mapPrinc.getDate("abh80dtAdmis");
				
				dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(29)}));
				mapPrinc.put("dias30", dtAdmis);
				
				dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(15)}));
				mapPrinc.put("dias45", dtAdmis);
				
				dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(15)}));
				mapPrinc.put("dias60", dtAdmis);
				
				dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(15)}));
				mapPrinc.put("dias75", dtAdmis);
				
				dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(15)}));
				mapPrinc.put("dias90", dtAdmis);
				
				//Dias experiência e renovação de experiência do sindicato
				dtAdmis = mapPrinc.getDate("abh80dtAdmis");
				
				if(mapPrinc.getInteger("abh03exp") != null) {
					dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(mapPrinc.getInteger("abh03exp")-1)}));
					mapPrinc.put("terminoExp", dtAdmis);
				}
				
				if(mapPrinc.getInteger("abh03renovExp") != null) {
					dtAdmis = dtAdmis.with(TemporalAdjusters.ofDateAdjuster({date -> date.plusDays(mapPrinc.getInteger("abh03renovExp")-1)}));
					mapPrinc.put("terminoExpRen", dtAdmis);
				}
			}
		}
		return gerarPDF(considerarExp ? "SFP_RelacaoDeAdmitidos_R1" : "SFP_RelacaoDeAdmitidos_R2", dados);
	}
	
	/**Método Diverso
	 * @return Set Integer (Tipo de Trabalhador)
	 */
	private Set<Integer> obterTipoTrabalhador(){
		Set<Integer> tiposTrab = new HashSet<>();
		
		if((boolean) get("isTrabalhador")) tiposTrab.add(0);
		if((boolean) get("isAutonomo")) tiposTrab.add(1);
		if((boolean) get("isProlabore")) tiposTrab.add(2);
		if((boolean) get("isTerceiros")) tiposTrab.add(3);
		
		if(tiposTrab.size() == 0) {
			tiposTrab.add(0);
			tiposTrab.add(1);
			tiposTrab.add(2);
			tiposTrab.add(3);
		}
		
		return tiposTrab;
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca da Relação de Trabalhadores Admitidos)
	 */
	private List<TableMap> findAbh80sByRelacaoAdmitidos(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, LocalDate[] periodo, Set<Integer> tiposAbh80, int ordenacao){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo" : ordenacao == 1 ? " ORDER BY abh80nome" : " ORDER BY abb11codigo";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "abh80dtAdmis") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abh80hs, abh80nascData, abh80dtAdmis, abh80dtResTrans, abb11codigo, abh05codigo, abh05nome, abh03codigo, abh03exp, abh03renovExp " +
				     "FROM Abh80 " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
				     "WHERE abh80tipo IN (:tiposAbh80) " + whereData + 
				     whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);

		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBBZG1pdGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=