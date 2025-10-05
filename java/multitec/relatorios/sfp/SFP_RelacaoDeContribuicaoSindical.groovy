package multitec.relatorios.sfp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.aa.Aac10;
import sam.model.entities.ab.Aba01;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Contribuição Sindical
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeContribuicaoSindical extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Contribuição Sindical";
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
		filtrosDefault.put("posicao", MDate.date());
		filtrosDefault.put("ordenacao", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {		
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsSindicatos = getListLong("sindicatos");
		LocalDate dtPosicao = getLocalDate("posicao");
		int ordenacao = getInteger("ordenacao"); 
		
		LocalDate calInicio = dtPosicao;
		calInicio.withDayOfMonth(1);
		
		LocalDate calFim = dtPosicao;
		calFim.withDayOfMonth(calFim.getDayOfMonth());
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		String eveSindical = getParametros(Parametros.FB_EVESINDICAL);
		if(eveSindical == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVESINDICAL.");
		
		Aac10 aac10 = getVariaveis().getAac10();
		params.put("TITULO_RELATORIO", "Relação de Contribuição Sindical");
		params.put("EMPRESA", aac10.getAac10na());
		params.put("PERIODO", calInicio.format(formatter) + " a " + calFim.format(formatter));
		
		List<TableMap> relacaoDeContribuicaoSindical = getDadosFba01011sByRelacaoContribuicaoSindical(idsTrabalhadores, idsDeptos, idsCargos, idsSindicatos, tiposTrab, dtPosicao.getMonthValue(), dtPosicao.getYear(), eveSindical, ordenacao);
		
		return gerarPDF("SFP_RelacaoDeContribuicaoSindical_R1", relacaoDeContribuicaoSindical);
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
	 * @return 	Parametros (Aba01)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FB"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.get();
		
		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca da Relação de Contribuição Sindical)
	 */
	public List<TableMap> getDadosFba01011sByRelacaoContribuicaoSindical(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsSindicatos, Set<Integer> tiposTrab, int mes, int ano, String eveSindical, int ordenacao){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereSindicatos = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo" : "ORDER BY abh80nome";
		
		String sql = "SELECT abh80codigo, abh80nome, abh80pis, abh80dtadmis, abh03codigo, abh80salario, abh05nome, abh21codigo, SUM(fba01011valor) as totalValor " +
				     "FROM Fba01011 " +
				     "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				     "INNER JOIN Abh80 ON abh80id = fba0101trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "INNER JOIN Abh03 ON abh03id = abh80sindsindical " +
				     "INNER JOIN Abh21 ON abh21id = fba01011eve " +
				     "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
				     "WHERE DATE_PART('MONTH', fba0101dtCalc) = "+mes+" AND DATE_PART('YEAR', fba0101dtCalc) = "+ano+" AND abh80tipo IN (:tiposAbh80) AND abh21codigo = '"+eveSindical+"' "+				
				     whereTrabalhadores + whereDeptos + whereCargos + whereSindicatos + getSamWhere().getWherePadrao("AND", Fba01.class) +
				     "GROUP BY abh80codigo, abh80nome, abh80pis, abh80dtadmis, abh03codigo, abh05nome, fba0101dtCalc, abh80salario, abh21codigo " +
				     "HAVING SUM(fba01011valor) > 0" +
				     ordem;
			
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
		query.setParameter("tiposAbh80", tiposTrab);

		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBDb250cmlidWnDp8OjbyBTaW5kaWNhbCIsInRpcG8iOiJyZWxhdG9yaW8ifQ==