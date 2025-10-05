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

/**Classe para relatório SCA - Pontos por Eventos
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_PontosPorEventos extends RelatorioBase{
	
	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Pontos por Eventos";
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
		filtrosDefault.put("ord", "0");
		
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
		List<Long> idsEvento = getListLong("evento");
		LocalDate[] datas = getIntervaloDatas("periodo");
		Set<Integer> situacoes = getSituacoes();
		int ordenacao = getInteger("ord");
		String nomeRelcustom = ordenacao == 0 ? "SCA_PontosPorEventos_R1" : "SCA_PontosPorEventos_R2";

		params.put("TITULO_RELATORIO", "Pontos por Eventos");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> pontosPorEventos = getPontosPorEventos(idsTrabalhador, idsDepartamento, idsMapHorario, idsEvento, datas, situacoes, ordenacao);
		
		if(pontosPorEventos != null && !pontosPorEventos.isEmpty()) {
			for(TableMap map : pontosPorEventos) {
				
				Integer extras = map.getInteger("fca10heDiu") + map.getInteger("fca10heNot");
				Integer horEve = map.getInteger("horas");
				String trabalhador = map.getString("abh80codigo")+ " - " + map.getString("abh80nome");
				String depto = map.getString("abb11codigo") + " - " + map.getString("abb11nome");
				
				String key = "";
				if(ordenacao == 0) {
					key = map.getString("abb11codigo") + "/" + map.getString("abh80codigo");
				}else if(ordenacao == 1) {
					key = map.getString("abh80codigo");
				}else {
					key = map.getString("abh80nome");
				}
				map.getDate("fca10data");
				key += "/" + map.getDate("fca10data") + "/" + map.getString("abh21codigo");
				
				map.put("trabalhador", trabalhador);
				map.put("depto", depto);
				map.put("extras", extras);
				map.put("horEve", horEve);
				map.put("key", key);
			}	
		}
		
		Collections.sort(pontosPorEventos, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });
		
		return gerarPDF(nomeRelcustom, pontosPorEventos);
	}

	/**Método Diverso
	 * @return 	Set Integer (Situações)
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
	
	/**Método Diverso
	 * @return 	List TableMap (Pontos Por Eventos)
	 */
	private List<TableMap> getPontosPorEventos(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, List<Long> idsEvento, LocalDate[] datas, Set<Integer> situacoes, int ordenacao){	
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereAbh21Evento = idsEvento != null && !idsEvento.isEmpty() ? "AND abh21id IN (:idsEvento) " : "";
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
		String ordem = ordenacao == 0 ? "ORDER BY abb11codigo, abh80codigo, fca10data, abh21codigo" : ordenacao == 1 ? "ORDER BY abh80codigo, fca10data, abh21codigo" : "ORDER BY abh80nome, fca10data, abh21codigo";

		List<TableMap> resultQuery = new ArrayList<>();

		for(int numEve = 0; numEve < 20; numEve++) {
			
			String eve = String.format("%02d", numEve);
		
			String sql = "SELECT abh80id, abh80codigo, abh80nome, fca10data, abb11codigo, abb11nome, abh21codigo, abh21nome, fca10obs, " +
	                	 "fca10jorBru, fca10jorLiq, fca10horDiu, fca10horNot, fca10heDiu, fca10heNot, fca10horFalt, fca10folha" + eve + " as horas " +
	                	 "FROM Fca10 " +
	                	 "INNER JOIN Abh80 ON abh80id = fca10trab " +
	                	 "INNER JOIN Abb11 ON abb11id = fca10depto " +
	                	 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
	                	 "INNER JOIN Abh21 ON abh21id = fca10eve" + eve + " " +
	                	 whereDt + " AND fca10sit IN (:sit) " +
	                	 "AND fca10folha" + eve + " > 0 " +
	                	 whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + whereAbh21Evento + getSamWhere().getWherePadrao("AND", Fca10.class) + 
	                	 ordem;
                
		
	    	Query query = getSession().createQuery(sql);
			
			if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
			if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
			if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
			if(idsEvento != null && !idsEvento.isEmpty()) query.setParameter("idsEvento", idsEvento);
			query.setParameter("sit", situacoes);
			query.setParameter("cons", 1);
			
			if(query.getListTableMap().size() > 0) resultQuery.addAll(query.getListTableMap());
		}
		return resultQuery;
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFBvbnRvcyBwb3IgRXZlbnRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==