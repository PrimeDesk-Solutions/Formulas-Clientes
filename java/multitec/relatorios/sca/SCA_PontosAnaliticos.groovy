package multitec.relatorios.sca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.ab.Aba01;
import sam.model.entities.fc.Fca10;
import sam.model.entities.fc.Fca1001;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Pontos Analíticos
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_PontosAnaliticos extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Pontos Analíticos";
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
		
		params.put("TITULO_RELATORIO", "Pontos Analíticos");
		params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("NOME_COM0", getParametros(Parametros.ABH1301_COMPLEMENTO0));
		params.put("NOME_COM1", getParametros(Parametros.ABH1301_COMPLEMENTO1));
		params.put("NOME_COM2", getParametros(Parametros.ABH1301_COMPLEMENTO2));
		params.put("NOME_COM3", getParametros(Parametros.ABH1301_COMPLEMENTO3));
		params.put("NOME_COM4", getParametros(Parametros.ABH1301_COMPLEMENTO4));

		List<TableMap> pontosAnaliticos = getPontosAnaliticos(idsTrabalhador, idsDepartamento, idsMapHorario, idsEvento, datas, situacoes, ordenacao);
		
		if(pontosAnaliticos != null && !pontosAnaliticos.isEmpty()) {
			for(TableMap map : pontosAnaliticos) {
	
				String codTrab = map.getString("abh80codigo");
				String nomeTrab = map.getString("abh80nome");
				LocalDate data = map.getDate("fca10data");
				String codEve = map.getString("abh21codigo");
				String key = ordenacao == 0 ? (codTrab + "/" + data + "/" + codEve) : (nomeTrab + "/" + data + "/" + codEve);
				String marcacoes = null;
				int clas = 0;
				
				List<Fca1001> fc1001 = findByUniqueKey(map.getLong("fca10id"),map.getLong("abh80id"), data);
				
				for(Fca1001 map1 : fc1001) {
					
					LocalTime horaBase = map1.getFca1001hrBase() == null ? null : map1.getFca1001hrBase();
					if(map1.getFca1001classificacao() == 0 || map1.getFca1001classificacao() == 1) {
						if(marcacoes == null) {
							clas = map1.getFca1001classificacao();
							marcacoes = "";
						}
						marcacoes += "    " + horaBase;																
					}
				}
				
				map.put("codTrab", codTrab);
				map.put("trabalhador", codTrab +" - "+ nomeTrab);
				map.put("data", data);
				map.put("semana", getDiaSemana(map.getDate("fca10data")));
				map.put("tipoDia", map.getString("abh08descr"));
				map.put("clas", clas);
				map.put("marcacoes", marcacoes);
				map.put("com0", map.getInteger("fca10complem0"));
				map.put("com1", map.getInteger("fca10complem1"));
				map.put("com2", map.getInteger("fca10complem2"));
				map.put("com3", map.getInteger("fca10complem3"));
				map.put("com4", map.getInteger("fca10complem4"));
				map.put("horasDiu", map.getInteger("fca10horDiu"));
				map.put("horasNot", map.getInteger("fca10horNot"));
				map.put("extrasDiu", map.getInteger("fca10heDiu"));
				map.put("extrasNot", map.getInteger("fca10heNot"));
				map.put("faltas", map.getInteger("fca10horFalt"));
				map.put("obs", map.getString("fca10obs"));
				map.put("codEve", codEve);
				map.put("nomeEve", map.getString("abh21nome"));
				map.put("horasEve", map.getInteger("horas"));
				map.put("key", key);
			}
		}
		
		Collections.sort(pontosAnaliticos, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });
		
		return gerarPDF("SCA_PontosAnaliticos",pontosAnaliticos);
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
	 * @return 	String (Parâmetros)
	 */
	private String getParametros(Parametro param){
		
		String aba01 = getSession().createCriteria(Aba01.class)
		.addFields("Aba01conteudo")
		.addWhere(Criterions.eq("aba01param", param.getParam().toUpperCase()))
		.addWhere(Criterions.eq("aba01aplic", "ABH1301"))
		.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
		.get(ColumnType.STRING);
		
		return aba01;
	}
	
	/**Método Diverso
	 * @return 	String (Dia da Semana)
	 */
	private String getDiaSemana(LocalDate data) {
		
		String diaDaSemana = null;

		if(data.getDayOfWeek() == DayOfWeek.SUNDAY) {
			diaDaSemana = "Dom";
		}else if(data.getDayOfWeek() == DayOfWeek.MONDAY){
			diaDaSemana = "Seg";
		}else if(data.getDayOfWeek() == DayOfWeek.TUESDAY){
			diaDaSemana = "Ter";
		}else if(data.getDayOfWeek() == DayOfWeek.WEDNESDAY){
			diaDaSemana = "Qua";
		}else if(data.getDayOfWeek() == DayOfWeek.THURSDAY){
			diaDaSemana = "Qui";
		}else if(data.getDayOfWeek() == DayOfWeek.FRIDAY){
			diaDaSemana = "Sex";
		}else {
			diaDaSemana = "Sab";
		}
		return diaDaSemana;
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Pontos Analíticos)
	 */
	private List<TableMap> getPontosAnaliticos(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, List<Long> idsEvento, LocalDate[] datas, Set<Integer> situacoes, int ordenacao){
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereAbh21Evento = idsEvento != null && !idsEvento.isEmpty() ? "AND abh21id IN (:idsEvento) " : "";
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
		
		List<TableMap> resultQuery = new ArrayList<>();
		
		for(int numEve = 0; numEve < 20; numEve++) {
			
			String eve = String.format("%02d", numEve);
		
		
			String sql = "SELECT abh80id, abh80codigo, abh80nome, fca10data, abb11codigo, abb11nome, abh21codigo, abh21nome, fca10obs, " +
						 "fca10complem0, fca10complem1, fca10complem2, fca10complem3, fca10complem4, abh08descr, " +     
						 "fca10jorBru, fca10jorLiq, fca10horDiu, fca10horNot, fca10heDiu, fca10heNot, fca10horFalt, fca10folha" + eve + " as horas " +
	                     "FROM Fca10 " +
	                     "INNER JOIN Abh80 ON abh80id = fca10trab " +
	                     "INNER JOIN Abh08 ON abh08id = fca10tpDia " +
	                     "INNER JOIN Abb11 ON abb11id = fca10depto " +
	                     "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
	                     "INNER JOIN Abh21 ON abh21id = fca10eve" + eve + " " +
	                     whereDt+ " AND fca10sit IN (:sit) " +
	                     "AND fca10folha" + eve + " > 0 " +
	                     whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + whereAbh21Evento + getSamWhere().getWherePadrao("AND", Fca10.class) + 
	                     "ORDER BY abh80codigo, fca10data, abh80codigo";
			
	    	Query query = getSession().createQuery(sql);
			
			if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
			if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
			if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
			if(idsEvento != null && !idsEvento.isEmpty()) query.setParameter("idsEvento", idsEvento);
			query.setParameter("sit", situacoes);
		
			if(query.getListTableMap().size() > 0) resultQuery.addAll(query.getListTableMap());
		}
		return resultQuery;
	}
	
	/**Método Diverso
	 * @return 	Fca1001 (Busca das Marcações)
	 */
	private List<Fca1001> findByUniqueKey(Long fca10id, Long abh80id, LocalDate fca10Data) {
		return getSession().createCriteria(Fca1001.class)
				.addJoin(Joins.fetch("fca1001ponto"))
				.addWhere(Criterions.eq("fca10Data", fca10Data))
				.addWhere(Criterions.eq("fca10Trab", abh80id))
				.setOrder("fca1001hrBase")
				.getList(ColumnType.ENTITY);
	}

}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFBvbnRvcyBBbmFsw610aWNvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==