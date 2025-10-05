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

/**Classe para relatório SCA - Marcações
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_Marcacoes extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Marcações";
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
		
		params.put("TITULO_RELATORIO", "Marcações");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> marcacoes = getMarcacoes(idsTrabalhador, idsDepartamento, idsMapHorario, datas, situacoes);
		
		if(marcacoes != null && !marcacoes.isEmpty()) {
			for(TableMap map : marcacoes) {
				
				String clas = null;
				String trabalhador = map.getString("abh80codigo") + " - " + map.getString("abh80nome");
				Integer extras = map.getInteger("fca10heDiu") + map.getInteger("fca10heNot");
				
				if(map.getInteger("fca1001classificacao") == null) {
					clas = "";
				}else if(map.getInteger("fca1001classificacao") == 0) {
					clas = "E";
				}else if(map.getInteger("fca1001classificacao") == 1) {
					clas = "S";
				}else if(map.getInteger("fca1001classificacao") == 2) {
					clas = "I";
				}else {
					clas = "D";
				}
				
				map.put("clas", clas);
				map.put("trabalhador", trabalhador);
				map.put("extras", extras);
			}	
		}
		return gerarPDF("SCA_Marcacoes", marcacoes);
	}
	
	/**Método Diverso
	 * @return 	Set Integer (Situações)
	 */
	private Set<Integer> getSituacoes(){
		Set<Integer> situacoes = new HashSet<>();
		
		if((boolean) get("sitTrabalhando")) situacoes.add(0);
		if((boolean) get("sitAfastado")) situacoes.add(1);
		if((boolean) get("sitFerias")) situacoes.add(2);
		
		if(situacoes.size() == 0) {
			situacoes.add(0);
			situacoes.add(1);
			situacoes.add(2);
		}
		return situacoes;
	}
	
	/**Método Diverso
	 * @return 	List de TableMap (Marcações)
	 */
	private List<TableMap> getMarcacoes(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] datas, Set<Integer> situacoes){	
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";//TODO
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, fca10data, abb11codigo, fca10jorBru, fca10jorLiq, fca10horDiu, fca10horNot, fca10heDiu, fca10heNot, fca10horFalt, " +
				     "fca10obs, fca1001hrBase, fca1001hrRep, fca1001classificacao, fca1001justificativa " +
				     "FROM Fca10 " +
				     "LEFT JOIN Fca1001 ON fca1001ponto = fca10id " +
				     "INNER JOIN Abh80 ON abh80id = fca10trab " +
				     "INNER JOIN Abb11 ON abb11id = fca10depto " +
				     "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
				     whereDt + "AND fca10consistente = :cons AND fca10sit IN (:sit) " +
				     whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) + 
				     " ORDER BY abh80codigo, fca10data, fca1001hrBase";
                
    	Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("sit", situacoes);
		query.setParameter("cons", 1);
				
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIE1hcmNhw6fDtWVzIiwidGlwbyI6InJlbGF0b3JpbyJ9