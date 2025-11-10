package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Pontos para a Folha de Pagamento
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_PontosParaAFolhaDePagamento extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Pontos para a Folha de Pagamento";
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
		LocalDate[] datas = getIntervaloDatas("periodo");
		Set<Integer> situacoes = getSituacoes();
		int ordenacao = getInteger("ord");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		
		List<TableMap> ponto =  getDadosPonto(idsTrabalhador, idsDepartamento, idsMapHorario, datas, situacoes, ordenacao);
		
		List<TableMap> mapDadosList = new ArrayList<>();
		
		if(ponto != null && !ponto.isEmpty()) {
			for(TableMap pontoMap : ponto) {
				
				TableMap tableMapDados = new TableMap();

				String key = ordenacao == 0 ? pontoMap.getString("abh80codigo") : pontoMap.getString("abh80nome");
				key += "/" + pontoMap.getString("abh21codigo");

				tableMapDados.put("abh80codigo", pontoMap.getString("abh80codigo"));
				tableMapDados.put("abh80nome", pontoMap.getString("abh80nome"));
				tableMapDados.put("abh21codigo", pontoMap.getString("abh21codigo"));
				tableMapDados.put("abh21nome", pontoMap.getString("abh21nome"));
				
				String tipo = null;
				switch (pontoMap.getInteger("abh21tipo")) {
				case 0: tipo = "Rendimento";	  
					break;
				case 1: tipo = "Desconto";	 	  
					break;
				case 2: tipo = "Base de Cálculo"; 
					break;
				case 3: tipo = "Neutro"; 		  
					break;
				case 4: tipo = "Cálculo";		  
					break;
				}
				
				tableMapDados.put("tipo", tipo);
				tableMapDados.put("unidade", pontoMap.getString("Aap18descr") != null ? pontoMap.getString("Aap18descr") : "");
				Integer horas = pontoMap.getInteger("horas");
				if(horas == null) horas = 0; 
				BigDecimal numHoras = DecimalUtils.create(pontoMap.getInteger("horas")).divide(60).round(2).get();
				tableMapDados.put("horas", horas);
				tableMapDados.put("numHoras", numHoras);
				tableMapDados.put("key", key);
				
				mapDadosList.add(tableMapDados);
			}
		}
		
		Collections.sort(mapDadosList, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });
		
		List<TableMap> mapPrincipal = new ArrayList<>();
		
		for(TableMap mapDados : mapDadosList) {
			
			TableMap mapRelatorio = new TableMap();
			
			mapRelatorio.put("abh80codigo", mapDados.getString("abh80codigo"));
			mapRelatorio.put("abh80nome", mapDados.getString("abh80nome"));
			mapRelatorio.put("abh21codigo", mapDados.getString("abh21codigo"));
			mapRelatorio.put("abh21nome", mapDados.getString("abh21nome"));
			mapRelatorio.put("tipo", mapDados.getString("tipo"));
			mapRelatorio.put("unidade", mapDados.getString("unidade"));
			mapRelatorio.put("horas", mapDados.getInteger("horas"));
			mapRelatorio.put("numHoras", mapDados.getBigDecimal("numHoras"));
			
			mapPrincipal.add(mapRelatorio);		
		}
		return gerarPDF("SCA_PontosParaAFolhaDePagamento", mapPrincipal);
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
	 * @return 	List TableMap (Dados do Ponto)
	 */
	private List<TableMap> getDadosPonto(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] datas, Set<Integer> situacoes, int ordenacao){
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
		String ordem = ordenacao == 0 ? "abh80codigo" : "abh80nome";
		
		List<TableMap> resultQuery = new ArrayList<>();

		for(int numEve = 0; numEve < 20; numEve++) {
			
			String eve = String.format("%02d", numEve);
			
			String sql = "SELECT abh80codigo, abh80nome, abh21codigo, abh21nome, abh21tipo, Aap18descr, SUM(fca10folha" + eve + ") as horas " +
					 	 "FROM Fca10 "+
						 "INNER JOIN Abh80 ON abh80id = fca10trab "+
						 "INNER JOIN Abb11 ON abb11id = fca10depto "+
						 "INNER JOIN Abh21 ON abh21id = fca10eve" + eve + " "+
						 "LEFT JOIN Aap18 ON Aap18id = Abh21unidPagto "+
						 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +		
						 " "+whereDt+" AND fca10sit IN (:sit) AND abh21sca IN (1, 3) " +
						 whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) +
						 "GROUP BY abh80codigo, abh80nome, abh21codigo, abh21nome, abh21tipo, Aap18descr " +
						 "HAVING SUM(fca10folha" + eve + ") > 0 "+
						 "ORDER BY " + ordem + " ";
						 
			Query query = getSession().createQuery(sql);
			
			if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
			if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
			if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
			query.setParameter("sit", situacoes);
			
			if(query.getListTableMap().size() > 0) resultQuery.addAll(query.getListTableMap());
		}
		return resultQuery; 
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFBvbnRvcyBwYXJhIGEgRm9saGEgZGUgUGFnYW1lbnRvIiwidGlwbyI6InJlbGF0b3JpbyJ9