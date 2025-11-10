package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Trabalhadores com Horas Extras
 * @author Lucas Eliel
 * @since 13/05/2019
 * @version 1.0
 */

public class SCA_TrabalhadoresComHorasExtras extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Trabalhadores com Horas Extras";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("sitTrabalhando", true);
		filtrosDefault.put("sitAfastado", true);
		filtrosDefault.put("sitFerias", true);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhador = getListLong("trabalhador");
		List<Long> idsDepartamento = getListLong("departamento");
		List<Long> idsMapHorario = getListLong("mapHorario");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Set<Integer> situacoes = getSituacoes();

		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> trabComHorasExtras =  getTrabalhadoresComExtras(idsTrabalhador, idsDepartamento, idsMapHorario, datas, situacoes);
		
		return gerarPDF("SCA_TrabalhadoresComHorasExtras", trabComHorasExtras);
	}

	/**Método Principal
	 * @return Set Integer (Situações)
	 */
	private Set<Integer> getSituacoes(){
		Set<Integer> situacoes = new HashSet<>();
		
		if((boolean) get("sitTrabalhando")) situacoes.add(0);
		if((boolean) get("sitFerias")) situacoes.add(1);
		if((boolean) get("sitAfastado")) situacoes.add(2);
		
		if(situacoes.size() == 0) {
			situacoes.add(0);
			situacoes.add(1);
			situacoes.add(2);
		}
		return situacoes;
	}
	
	/**Método Principal
	 * @return List TableMap (Trabalhadores com Extras)
	 */
	private List<TableMap> getTrabalhadoresComExtras(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] datas, Set<Integer> situacoes){
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
	
		String sql = "SELECT abh80codigo, abh80nome, abh08descr, fca10data, fca10heDiu, fca10heNot, fca10heDiu + fca10heNot as total, abb11codigo, abb11nome " +
	                 "FROM Fca10 " +
	                 "INNER JOIN Abh80 ON abh80id = fca10trab " +
	                 "INNER JOIN Abb11 ON abb11id = fca10depto " +
	                 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
	                 "INNER JOIN Abh08 ON abh08id = fca10tpDia " +
	                 whereDt + "AND fca10consistente = :cons AND fca10sit IN (:sit) AND (fca10heDiu <> 0 OR fca10heNot <> 0) " + 
	                 whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) +
	                 " ORDER BY abh80codigo, abb11codigo, fca10data";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("sit", situacoes);
		query.setParameter("cons", 1);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFRyYWJhbGhhZG9yZXMgY29tIEhvcmFzIEV4dHJhcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==