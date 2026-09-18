package multitec.relatorios.sca;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Pontos por Acesso
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_PontosPorAcesso extends RelatorioBase{
	
	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Pontos por Acesso";
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
		filtrosDefault.put("horaInicial", "00:00");
		filtrosDefault.put("horaFinal", "23:59");
		
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
		LocalTime horaInicial = getLocalTime("horaInicial");
		LocalTime horaFinal = getLocalTime("horaFinal");
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		params.put("TITULO_RELATORIO", "Pontos por Acesso");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO_DT", "Data: " + datas[0].format(formatter) + " à " + datas[1].format(formatter));
		params.put("PERIODO_HR", "Hora: " + horaInicial + " à " + horaFinal);
		
		List<TableMap> pontosPorAcesso =  getPontosPorAcesso(idsTrabalhador, idsDepartamento, idsMapHorario, datas, situacoes, horaInicial, horaFinal);
				
		if(pontosPorAcesso != null && !pontosPorAcesso.isEmpty()) {
			for(TableMap map : pontosPorAcesso) {
				
				String clas = null;
				
				switch (map.getInteger("fca1001classificacao")) {
					case 0: clas = "Entrada";        
						break;
					case 1: clas = "Saída";         
						break;
					case 2: clas = "Indefinida";     
						break;
					case 3: clas = "Desconsiderado"; 
						break;
				}
				
				map.put("clas", clas);
			}
		}
		return gerarPDF("SCA_PontosPorAcesso", pontosPorAcesso);
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
	 * @return 	List TableMap (Pontos Por Acesso)
	 */
	private List<TableMap> getPontosPorAcesso(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] datas, Set<Integer> situacoes, LocalTime horaInicial, LocalTime horaFinal){	
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";//TODO
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, fca10data, fca1001hrBase, fca1001hrRep, fca1001classificacao, fca1001justificativa " +
                	 "FROM Fca1001 " +
                	 "INNER JOIN Fca10 ON fca10id = fca1001ponto " +
                	 "INNER JOIN Abh80 ON abh80id = fca10trab " +
                	 "INNER JOIN Abb11 ON abb11id = fca10depto " +
                	 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
                	 whereDt +" AND fca10consistente = :cons AND fca10sit IN (:sit) AND fca1001hrBase BETWEEN :horaInicial AND :horaFinal " +
                	 whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) + 
                	 " ORDER BY abh80codigo, fca10data, fca1001HrBase";
                	 
    	Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("sit", situacoes);
		query.setParameter("cons", 1);
		query.setParameter("horaInicial", horaInicial);
		query.setParameter("horaFinal", horaFinal);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFBvbnRvcyBwb3IgQWNlc3NvIiwidGlwbyI6InJlbGF0b3JpbyJ9