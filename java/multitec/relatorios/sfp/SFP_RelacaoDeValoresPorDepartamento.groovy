package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Valores por Departamento
 * @author Lucas Eliel
 * @since 26/02/2019
 * @version 1.0
 */

public class SFP_RelacaoDeValoresPorDepartamento extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Valores por Departamento";
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
		filtrosDefault.put("calcFolha", true);
		filtrosDefault.put("calcFerias", true);
		filtrosDefault.put("calcRescisao", true);
		filtrosDefault.put("calcAdiantamento", false);
		filtrosDefault.put("calc13sal", false);
		filtrosDefault.put("calcPlr", false);
		filtrosDefault.put("calcOutros", false);
		filtrosDefault.put("detalhamento", "0");
		LocalDate[] periodos = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodos", periodos);

		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return String, byte[] (Dados para Download)
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsSindicatos = getListLong("sindicatos");
		Set<Integer> tiposCalc = obterTiposCalculo();
		Integer rdoDetail = getInteger("detalhamento");
		LocalDate[] periodo = getIntervaloDatas("periodos");

		Integer totalizacao = rdoDetail;
		
		Aac10 aac10 = getVariaveis().getAac10();
		params.put("TITULO_RELATORIO", "Relação de Valores por Departamento");
		params.put("PERIODO", "Período: " + DateUtils.formatDate(periodo[0]) + " a " + DateUtils.formatDate(periodo[1]));
		params.put("EMPRESA", aac10.getAac10na());
		params.put("TIPO_RELATORIO", 1);
		
		List<TableMap> relatorioRelacaoValoresPorDepartamento = getDadosRelatorioRelacaoValoresPorDepartamento(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, periodo, tiposTrab, tiposCalc, totalizacao);

		String relatorio = totalizacao == 0 ? "SFP_RelacaoDeValoresPorDepartamento_R1" : "SFP_RelacaoDeValoresPorDepartamento_R2";

		return gerarPDF(relatorio, relatorioRelacaoValoresPorDepartamento);
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
	 * @return Set Integer (Tipo de Cálculo)
	 */
	private Set<Integer> obterTiposCalculo(){
		Set<Integer> calc = new HashSet<>();
		
		if((boolean) get("calcFolha")) calc.add(0);
		if((boolean) get("calcAdiantamento")) calc.add(1);
		if((boolean) get("calc13sal")) calc.add(2);
		if((boolean) get("calcFerias")) calc.add(3);
		if((boolean) get("calcRescisao")) calc.add(4);
		if((boolean) get("calcPlr")) calc.add(6);
		if((boolean) get("calcOutros")) calc.add(9);
		
		if(calc.size() == 0) {
			calc.add(0);
			calc.add(1);
			calc.add(2);
			calc.add(3);
			calc.add(4);
			calc.add(6);
			calc.add(9);
		}
		return calc;
	}
	
	/**Método Diverso
	 * @return List TableMap (Eventos da Relação Valores por Departamento)
	 */
	public List<TableMap> getDadosRelatorioRelacaoValoresPorDepartamento(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, LocalDate[] periodo, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Integer totalizacao) {
		
		List<TableMap> eventos = findDadosFba01011sEventosByRelacaoValoresPorDepartamento(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, periodo, tiposTrab, tiposCalc, totalizacao);
		
		return eventos;
	}
	
	/**Método Diverso
	 * @return List TableMap (Query da busca de Eventos da Relação Valores por Departamento)
	 */
	public List<TableMap> findDadosFba01011sEventosByRelacaoValoresPorDepartamento(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, LocalDate[] periodo, Set<Integer> tiposTrab, Set<Integer> tiposCalc, Integer totalizacao) {
		String campos = totalizacao == 0 ? "" : " abh80codigo, abh80nome, ";
		String grupo = totalizacao == 0 ? "" : " abh80codigo, abh80nome, ";
		String ordem = totalizacao == 0 ? " ORDER BY abb11codigo, abh21codigo " : " ORDER BY abb11codigo, abh80codigo, abh21codigo ";
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String wherePeriodo = periodo != null ? getWhereDataInterval("WHERE", periodo, "fba0101dtCalc") : "";
		
		//TODO fb011ref
		String sql = "SELECT " + campos + " abb11codigo, abb11nome, abh21codigo, abh21nome, abh21tipo, SUM(fba01011refHoras) as totalRef, SUM(fba01011valor) as totalValor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 wherePeriodo + " AND abh80tipo IN (:tiposTrab) AND fba0101tpVlr IN (:tiposCalc) " +
					 whereTrabalhadores + whereDeptos + whereCargos + whereSindicatos + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 " GROUP BY " + grupo + " abb11codigo, abb11nome, abh21codigo, abh21nome, abh21tipo " +
					 "HAVING SUM(fba01011valor) > 0 " +
					 ordem;
		
		Query query = getSession().createQuery(sql);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		query.setParameter("tiposTrab", tiposTrab);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
		query.setParameter("tiposCalc", tiposCalc);
		
		return query.getListTableMap();
	}

}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBWYWxvcmVzIHBvciBEZXBhcnRhbWVudG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=