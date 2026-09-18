package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Demitidos
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeDemitidos extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Demitidos";
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
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		LocalDate[] periodo = getIntervaloDatas("periodo");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		
		List<TableMap> dados = getDadosAbh80sByRelacaoDemitidos(idsTrabalhadores, idsDeptos, idsCargos, periodo, ordenacao, tiposTrab);	
		
		return gerarPDF("SFP_RelacaoDeDemitidos_R1", dados);
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
	 * @return 	List TableMap (Query da busca - Trabalhadores Demitidos)
	 */
	private List<TableMap> getDadosAbh80sByRelacaoDemitidos(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, LocalDate[] periodo, int ordenacao, Set<Integer> tiposAbh80){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String ordem = ordenacao == 0? " ORDER BY abh80codigo " : ordenacao == 1 ? " ORDER BY abh80nome " : " ORDER BY abb11codigo ";
		String whereData = periodo != null ? getWhereDataInterval("AND", periodo, "abh80dtResTrans") : "";
		
		String sql = "SELECT DISTINCT abh80codigo, abh80nome, abh80hs, abh80dtAdmis, abh80dtResTrans, abb11codigo, abh05codigo, aap03codigo, abh06codigo, abh06causa, fbd10descr "+ 
					 "FROM Fbd10 " +
					 "INNER JOIN Abh80 ON abh80id = fbd10trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
			     	 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
			     	 "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
			     	 "INNER JOIN Abh06 ON abh06id = fbd10causa " +
			     	 "WHERE abh80tipo IN (:tiposAbh80) AND abh80dtResTrans IS NOT NULL " + whereData + 
			     	 whereTrabalhadores + whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fbd10.class) +
			     	 ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBEZW1pdGlkb3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=