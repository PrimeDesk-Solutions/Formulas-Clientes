package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fb.Fbc01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Trabalhadores em Férias
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeTrabalhadoresEmFerias extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Trabalhadores em Férias";
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
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
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
		List<Long> idsCargos = getListLong("cargos");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Integer ordenacao = getInteger("ordenacao");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		
		List<TableMap> dados = getDadosFbc0101sTrabalhadoresByRelacaoFerias(idsTrabalhadores, idsDeptos, idsCargos, periodo, tiposTrab, ordenacao == 0 ? true: false);
		
		return gerarPDF("SFP_RelacaoDeTrabalhadoresEmFerias_R1", dados);
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
	 * @return 	List TableMap (Query da busca da Relação de Férias - Trabalhadores)
	 */
	private List<TableMap> getDadosFbc0101sTrabalhadoresByRelacaoFerias(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, LocalDate[] dtPeriodo, Set<Integer> tiposAbh80, boolean ordenarPorTrab) {
		String ordem = ordenarPorTrab ? " ORDER BY abh80codigo " : " ORDER BY abb11codigo ";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereDataPgi = dtPeriodo != null ? getWhereDataInterval("", dtPeriodo, "fbc0101pgi") : "";
		String whereDataPgf = dtPeriodo != null ? getWhereDataInterval("", dtPeriodo, "fbc0101pgf") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abh80dtAdmis, abb11codigo, fbc0101pgi, fbc0101pgf, fbc0101diasFerias " +
				     "FROM Fbc0101 " +
				     "INNER JOIN Fbc01 ON fbc01id = fbc0101pa " +
					 "INNER JOIN Abh80 ON abh80id = fbc01trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "WHERE ( " + whereDataPgi + " OR " + whereDataPgf + ") AND abh80tipo IN (:tiposAbh80) " +
				     whereTrabalhadores +whereDeptos + whereCargos + getSamWhere().getWherePadrao("AND", Fbc01.class) +
				     ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBUcmFiYWxoYWRvcmVzIGVtIEbDqXJpYXMiLCJ0aXBvIjoicmVsYXRvcmlvIn0=