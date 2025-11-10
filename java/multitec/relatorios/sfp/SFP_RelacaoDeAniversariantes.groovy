package multitec.relatorios.sfp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Aniversariantes
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeAniversariantes extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Aniversariantes";
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
		filtrosDefault.put("mes", 0);
		
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
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		int mesNumber = getInteger("mes");
		String mes = null;
		switch(getInteger("mes")) {
			case 0: mes = "Janeiro";
				break;
			case 1: mes = "Fevereiro";
				break;
			case 2: mes = "Março";
				break;
			case 3: mes = "Abril";
				break;
			case 4: mes = "Maio";
				break;
			case 5: mes = "Junho";
				break;
			case 6: mes = "Julho";
				break;
			case 7: mes = "Agosto";
				break;
			case 8: mes = "Setembro";
				break;
			case 9: mes = "Outubro";
				break;
			case 10: mes = "Novembro";
				break;
			case 11: mes = "Dezembro";
				break;
		}
		params.put("MES", mes);
		
		List<TableMap> dados = getDadosAbh80sByRelacaoAniversariantes(idsTrabalhadores, idsDeptos, idsCargos, tiposTrab, ordenacao == 0 ? true : false, mesNumber+1);
		
		return gerarPDF("SFP_RelacaoDeAniversariantes_R1", dados);
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
	 * @return List TableMap (Query da busca da Relação de Aniversariantes)
	 */
	private List<TableMap> getDadosAbh80sByRelacaoAniversariantes(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, Set<Integer> tiposAbh80, boolean ordenarPorData, int mes) {
		String ordem = ordenarPorData ? " ORDER BY " + Fields.day("abh80nascData") : " ORDER BY abh80nome ";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abh80nascData, abb11codigo, abb11nome " +
				     "FROM Abh80 " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "WHERE " + Fields.month("abh80nascData") + " = :mes AND abh80tipo IN (:tiposAbh80) AND abh80dtResTrans IS NULL " +
				     whereTrabalhadores + whereDeptos +whereCargos + getSamWhere().getWherePadrao("AND", Abh80.class) +
				     ordem;
	
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("mes", mes);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
	
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBBbml2ZXJzYXJpYW50ZXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=