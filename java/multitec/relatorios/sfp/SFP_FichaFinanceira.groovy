package multitec.relatorios.sfp;

import br.com.multiorm.Query;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Ficha Financeira
 * @author Lucas Eliel
 * @since 30/04/2019
 * @version 1.0
 */
	
public class SFP_FichaFinanceira extends RelatorioBase{

	/**Método Principal	
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Ficha Financeira";
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
		Integer periodo = MDate.date().getYear();
		filtrosDefault.put("periodo", periodo);
		
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
		List<Long> idsEventos = getListLong("eventos");
		Integer ano = getInteger("periodo");
		Integer mesInicial = getInteger("mesInicial");
		Integer mesFinal = getInteger("mesFinal");

		Aac10 aac10 = getVariaveis().getAac10();
		String razaoSocial  = aac10.getAac10rs();		
		String CNPJ = aac10.getAac10ni();
		
		params.put("TITULO_RELATORIO", "Ficha Financeira");
		params.put("EMP_RS", razaoSocial);
		params.put("EMP_NI", CNPJ);
		params.put("ANO_BASE", ano);
		
		List<TableMap> fichaFinanceira = getDadosRelatorioFichaFinanceira(idsTrabalhadores, idsDeptos, idsCargos, idsEventos, ano, mesInicial, mesFinal, tiposTrab);
		
		return gerarPDF("SFP_FichaFinanceira_R1", fichaFinanceira);
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
	 * @return 	List TableMap (Query da busca - Eventos e Valores por Ficha Financeira)
	 */
	public List<TableMap> findDadosFba01011sEventosAndValoresByFichaFinanceira(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsEventos, int ano, int mes, Set<Integer> tiposTrab) {		
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abh80depto IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereEventos = idsEventos != null && !idsEventos.isEmpty() ? " AND abh21id IN (:idsEventos) " : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abh80dtadmis, abh21codigo, abh21nome, abh21tipo, abh05codigo, abh05nome, aap03codigo, abb11codigo, abb11nome, SUM(fba01011valor) as totalValor " +
				     "FROM Fba01011 " +
				     "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				     "INNER JOIN Abh80 ON abh80id = fba0101trab " +
				     "INNER JOIN Abb11 ON abb11id = abh80depto " +
				     "INNER JOIN Abh05 ON abh05id = abh80cargo " +
				     "LEFT JOIN Aap03 ON aap03id = abh05cbo " +
				     "INNER JOIN Abh21 ON abh21id = fba01011eve " +
				     "INNER JOIN Fba01 ON fba01id = fba0101calculo "+
				     "WHERE DATE_PART('MONTH', fba0101dtCalc) = "+mes+" AND DATE_PART('YEAR', fba0101dtCalc) = "+ano+" AND abh80tipo IN (:tiposAbh80) " +
				     whereTrabalhadores + whereDeptos + whereCargos + whereEventos + getSamWhere().getWherePadrao("AND", Fba01.class) +
				     " GROUP BY abh80codigo, abh80nome, abh80dtadmis, abh21codigo, abh21nome, abh21tipo, abh05codigo, abh05nome, aap03codigo, abb11codigo, abb11nome " +
				     "ORDER BY abh80codigo, abh21codigo";
			
			Query query = getSession().createQuery(sql);
			if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
			if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
			if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
			if(idsEventos != null && !idsEventos.isEmpty()) query.setParameter("idsEventos", idsEventos);
			query.setParameter("tiposAbh80", tiposTrab);
			
			return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Ficha Financeira)
	 */
	public List<TableMap> getDadosRelatorioFichaFinanceira(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsEventos, int ano, int mesInicial, int mesFinal, Set<Integer> tiposTrab) {

		List<TableMap> mapDadosList = new ArrayList<>();
		
		for(int i = mesInicial; i <= mesFinal; i++) {
			List<TableMap> eventos = findDadosFba01011sEventosAndValoresByFichaFinanceira(idsTrabalhadores, idsDeptos, idsCargos, idsEventos, ano, i, tiposTrab);
		
			if(eventos != null && eventos.size() > 0) {
				for(TableMap map : eventos) {
				
					TableMap mapDados = new TableMap();

					String key = map.getString("abh80codigo") + "/" + map.getString("abh21codigo");
					mapDados.put("codTrab", map.getString("abh80codigo"));
					mapDados.put("nomeTrab", map.getString("abh80nome"));
					mapDados.put("admisTrab", map.getDate("abh80dtadmis"));
					mapDados.put("codEve", map.getString("abh21codigo"));
					mapDados.put("nomeEve", map.getString("abh21nome"));

					String tipoEve = null;
					switch(map.getInteger("abh21tipo")){
						case 0: tipoEve = "Rendimento";
							break;
						case 1: tipoEve = "Desconto";
							break;
						case 2: tipoEve = "BCálculo";
							break;
						case 3: tipoEve = "Neutro";
							break;
						case 4: tipoEve = "Cálculo";
							break;
						case 5: tipoEve = "Totalização";
							break;
					}
					mapDados.put("tipoEve", tipoEve);
					mapDados.put("codCargo", map.getString("abh05codigo"));
					mapDados.put("nomeCargo", map.getString("abh05nome"));
					mapDados.put("cbo", map.getString("aap03codigo"));
					mapDados.put("codDepto", map.getString("abb11codigo"));
					mapDados.put("nomeDepto", map.getString("abb11nome"));

					String mes = null;
					switch(i) {
						case 1: mes = "jan";
							break;
						case 2: mes = "fev";
							break;
						case 3: mes = "mar";
							break;
						case 4: mes = "abr";
							break;
						case 5: mes = "mai";
							break;
						case 6: mes = "jun";
							break;
						case 7: mes = "jul";
							break;
						case 8: mes = "ago";
							break;
						case 9: mes = "set";
							break;
						case 10: mes = "out";
							break;
						case 11: mes = "nov";
							break;
						case 12: mes = "dez";
							break;
					}					
					mapDados.put(mes, map.getBigDecimal("totalValor"));
					mapDados.put("key", key);
					mapDadosList.add(mapDados);
				}
			}
		}
		
		List<TableMap> mapRelatorioList = new ArrayList<>();
		
		Collections.sort(mapDadosList, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });

		int i = 0;
		
		for(TableMap mapDados : mapDadosList) {
			
			TableMap mapRelatorio = new TableMap();
			
			if(mapRelatorioList.size() <= 0) {
				
				String key = mapDados.getString("codTrab") + "/" + mapDados.getString("codEve");
				mapRelatorio.put("codTrab", mapDados.getString("codTrab"));
				mapRelatorio.put("nomeTrab", mapDados.getString("nomeTrab"));
				mapRelatorio.put("admisTrab", mapDados.getDate("admisTrab"));
				mapRelatorio.put("codEve", mapDados.getString("codEve"));
				mapRelatorio.put("nomeEve", mapDados.getString("nomeEve"));
				mapRelatorio.put("tipoEve", mapDados.getString("tipoEve"));
				mapRelatorio.put("codCargo", mapDados.getString("codCargo"));
				mapRelatorio.put("nomeCargo", mapDados.getString("nomeCargo"));
				mapRelatorio.put("cbo", mapDados.getString("cbo"));
				mapRelatorio.put("codDepto", mapDados.getString("codDepto"));
				mapRelatorio.put("nomeDepto", mapDados.getString("nomeDepto"));
				mapRelatorio.put("jan", mapDados.getBigDecimal("jan") == null ? 0 : mapDados.getBigDecimal("jan"));
				mapRelatorio.put("fev", mapDados.getBigDecimal("fev") == null ? 0 : mapDados.getBigDecimal("fev"));
				mapRelatorio.put("mar", mapDados.getBigDecimal("mar") == null ? 0 : mapDados.getBigDecimal("mar"));
				mapRelatorio.put("abr", mapDados.getBigDecimal("abr") == null ? 0 : mapDados.getBigDecimal("abr"));
				mapRelatorio.put("mai", mapDados.getBigDecimal("mai") == null ? 0 : mapDados.getBigDecimal("mai"));
				mapRelatorio.put("jun", mapDados.getBigDecimal("jun") == null ? 0 : mapDados.getBigDecimal("jun"));
				mapRelatorio.put("jul", mapDados.getBigDecimal("jul") == null ? 0 : mapDados.getBigDecimal("jul"));
				mapRelatorio.put("ago", mapDados.getBigDecimal("ago") == null ? 0 : mapDados.getBigDecimal("ago"));
				mapRelatorio.put("set", mapDados.getBigDecimal("set") == null ? 0 : mapDados.getBigDecimal("set"));
				mapRelatorio.put("out", mapDados.getBigDecimal("out") == null ? 0 : mapDados.getBigDecimal("out"));
				mapRelatorio.put("nov", mapDados.getBigDecimal("nov") == null ? 0 : mapDados.getBigDecimal("nov"));
				mapRelatorio.put("dez", mapDados.getBigDecimal("dez") == null ? 0 : mapDados.getBigDecimal("dez"));
				mapRelatorio.put("key", key);
				mapRelatorioList.add(mapRelatorio);
				
			}else {
				
				if(! mapDados.get("key").equals(mapRelatorioList.get(i).get("key"))) {
					String key = mapDados.getString("codTrab") + "/" + mapDados.getString("codEve");
					mapRelatorio.put("codTrab", mapDados.getString("codTrab"));
					mapRelatorio.put("nomeTrab", mapDados.getString("nomeTrab"));
					mapRelatorio.put("admisTrab", mapDados.getDate("admisTrab"));
					mapRelatorio.put("codEve", mapDados.getString("codEve"));
					mapRelatorio.put("nomeEve", mapDados.getString("nomeEve"));
					mapRelatorio.put("tipoEve", mapDados.getString("tipoEve"));
					mapRelatorio.put("codCargo", mapDados.getString("codCargo"));
					mapRelatorio.put("nomeCargo", mapDados.getString("nomeCargo"));
					mapRelatorio.put("cbo", mapDados.getString("cbo"));
					mapRelatorio.put("codDepto", mapDados.getString("codDepto"));
					mapRelatorio.put("nomeDepto", mapDados.getString("nomeDepto"));
					mapRelatorio.put("jan", mapDados.getBigDecimal("jan") == null ? 0 : mapDados.getBigDecimal("jan"));
					mapRelatorio.put("fev", mapDados.getBigDecimal("fev") == null ? 0 : mapDados.getBigDecimal("fev"));
					mapRelatorio.put("mar", mapDados.getBigDecimal("mar") == null ? 0 : mapDados.getBigDecimal("mar"));
					mapRelatorio.put("abr", mapDados.getBigDecimal("abr") == null ? 0 : mapDados.getBigDecimal("abr"));
					mapRelatorio.put("mai", mapDados.getBigDecimal("mai") == null ? 0 : mapDados.getBigDecimal("mai"));
					mapRelatorio.put("jun", mapDados.getBigDecimal("jun") == null ? 0 : mapDados.getBigDecimal("jun"));
					mapRelatorio.put("jul", mapDados.getBigDecimal("jul") == null ? 0 : mapDados.getBigDecimal("jul"));
					mapRelatorio.put("ago", mapDados.getBigDecimal("ago") == null ? 0 : mapDados.getBigDecimal("ago"));
					mapRelatorio.put("set", mapDados.getBigDecimal("set") == null ? 0 : mapDados.getBigDecimal("set"));
					mapRelatorio.put("out", mapDados.getBigDecimal("out") == null ? 0 : mapDados.getBigDecimal("out"));
					mapRelatorio.put("nov", mapDados.getBigDecimal("nov") == null ? 0 : mapDados.getBigDecimal("nov"));
					mapRelatorio.put("dez", mapDados.getBigDecimal("dez") == null ? 0 : mapDados.getBigDecimal("dez"));
					mapRelatorio.put("key", key);
					mapRelatorioList.add(mapRelatorio);
					i++;
				}else {
					mapRelatorioList.get(i).put("jan", mapRelatorioList.get(i).getBigDecimal("jan").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("jan") : mapDados.getBigDecimal("jan") == null ? 0 : mapDados.getBigDecimal("jan"));
					mapRelatorioList.get(i).put("fev", mapRelatorioList.get(i).getBigDecimal("fev").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("fev") : mapDados.getBigDecimal("fev") == null ? 0 : mapDados.getBigDecimal("fev"));
					mapRelatorioList.get(i).put("mar", mapRelatorioList.get(i).getBigDecimal("mar").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("mar") : mapDados.getBigDecimal("mar") == null ? 0 : mapDados.getBigDecimal("mar"));
					mapRelatorioList.get(i).put("abr", mapRelatorioList.get(i).getBigDecimal("abr").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("abr") : mapDados.getBigDecimal("abr") == null ? 0 : mapDados.getBigDecimal("abr"));
					mapRelatorioList.get(i).put("mai", mapRelatorioList.get(i).getBigDecimal("mai").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("mai") : mapDados.getBigDecimal("mai") == null ? 0 : mapDados.getBigDecimal("mai"));
					mapRelatorioList.get(i).put("jun", mapRelatorioList.get(i).getBigDecimal("jun").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("jun") : mapDados.getBigDecimal("jun") == null ? 0 : mapDados.getBigDecimal("jun"));
					mapRelatorioList.get(i).put("jul", mapRelatorioList.get(i).getBigDecimal("jul").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("jul") : mapDados.getBigDecimal("jul") == null ? 0 : mapDados.getBigDecimal("jul"));
					mapRelatorioList.get(i).put("ago", mapRelatorioList.get(i).getBigDecimal("ago").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("ago") : mapDados.getBigDecimal("ago") == null ? 0 : mapDados.getBigDecimal("ago"));
					mapRelatorioList.get(i).put("set", mapRelatorioList.get(i).getBigDecimal("set").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("set") : mapDados.getBigDecimal("set") == null ? 0 : mapDados.getBigDecimal("set"));
					mapRelatorioList.get(i).put("out", mapRelatorioList.get(i).getBigDecimal("out").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("out") : mapDados.getBigDecimal("out") == null ? 0 : mapDados.getBigDecimal("out"));
					mapRelatorioList.get(i).put("nov", mapRelatorioList.get(i).getBigDecimal("nov").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("nov") : mapDados.getBigDecimal("nov") == null ? 0 : mapDados.getBigDecimal("nov"));
					mapRelatorioList.get(i).put("dez", mapRelatorioList.get(i).getBigDecimal("dez").compareTo(BigDecimal.ZERO) > 0 ? mapRelatorioList.get(i).getBigDecimal("dez") : mapDados.getBigDecimal("dez") == null ? 0 : mapDados.getBigDecimal("dez"));
				}
			} 					
		}
		
		//Calcula o total dos meses
		for(TableMap mapRelatorio : mapRelatorioList) {

			BigDecimal total = new BigDecimal(0);
			total = total.add(mapRelatorio.getBigDecimal("jan"));
			total = total.add(mapRelatorio.getBigDecimal("fev"));
			total = total.add(mapRelatorio.getBigDecimal("mar"));
			total = total.add(mapRelatorio.getBigDecimal("abr"));
			total = total.add(mapRelatorio.getBigDecimal("mai"));
			total = total.add(mapRelatorio.getBigDecimal("jun"));
			total = total.add(mapRelatorio.getBigDecimal("jul"));
			total = total.add(mapRelatorio.getBigDecimal("ago"));
			total = total.add(mapRelatorio.getBigDecimal("set"));
			total = total.add(mapRelatorio.getBigDecimal("out"));
			total = total.add(mapRelatorio.getBigDecimal("nov"));
			total = total.add(mapRelatorio.getBigDecimal("dez"));
			mapRelatorio.put("total", total);
		}
		return mapRelatorioList;
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIEZpY2hhIEZpbmFuY2VpcmEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=