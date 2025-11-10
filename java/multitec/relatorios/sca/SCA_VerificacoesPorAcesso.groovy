package multitec.relatorios.sca;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh11;
import sam.model.entities.ab.Abh1301;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Verificações por Acesso
 * @author Lucas Eliel
 * @since 13/05/2019
 * @version 1.0
 */

public class SCA_VerificacoesPorAcesso extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Verificações por Acesso";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("exibirMarcacoes", false);
		filtrosDefault.put("sitTrabalhando", true);
		filtrosDefault.put("sitAfastado", true);
		filtrosDefault.put("sitFerias", true);
		filtrosDefault.put("foraDaTolerancia", false);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsMapHorario = getListLong("mapHorario");
		boolean isForaDaTolerancia = get("foraDaTolerancia");
		
		LocalDate[] periodo = getIntervaloDatas("periodo");
		
		Set<Integer> situacoes = getSituacoes();
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", "Data: " + DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		
		List<TableMap> dados = findMarcacoesByTrabalhadorAndDeptoAndHorarioAndSitAndPeriodo(idsTrabalhadores, idsDeptos, idsMapHorario, situacoes, periodo);
		if(dados != null && !dados.isEmpty()) {
			for(TableMap fca10 : dados) {
				Abh1301 abh1301 = findMarcacoesByUniqueKey(fca10.getLong("abh13id"), fca10.getDate("fca10data"));
				
				if(abh1301 == null || !isForaDaTolerancia || (isForaDaTolerancia && !isMarcacaoTolerante(abh1301.getAbh1301horario(), fca10.getTime("fca1001hrBase")))) {
					if(abh1301 != null) {
						Abh11 abh11 = abh1301.getAbh1301horario();
						
						fca10.put("abh11horaE", abh11.getAbh11horaE());
						fca10.put("abh11intervE", abh11.getAbh11intervE()); 
						fca10.put("abh11horaS", abh11.getAbh11horaS()); 
						fca10.put("abh11intervS", abh11.getAbh11intervS()); 
					}
				}
			}
		}
		return gerarPDF("SCA_VerificacoesPorAcesso", dados);
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
	 * @return List TableMap (Trabalhador, Departamento, Horario, Situação e Periodo)
	 */
	private List<TableMap> findMarcacoesByTrabalhadorAndDeptoAndHorarioAndSitAndPeriodo(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsMapHorario, Set<Integer> situacoes, LocalDate[] periodo){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13id IN (:idsMapHorario) " : "";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "fca10data") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, fca10data, fca1001hrBase, fca1001hrRep, abh13id " +
                	 "FROM Fca1001 " +
                	 "INNER JOIN Fca10 ON fca10id = fca1001ponto " +
                	 "INNER JOIN Abh80 ON abh80id = fca10trab " +
                	 "INNER JOIN Abb11 ON abb11id = fca10depto " +
                	 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
                	 whereData + 
                	 " AND fca10sit IN (:situacoes) " + 
                	 whereTrabalhadores + whereDeptos + whereMapHorario + 
                	 getSamWhere().getWherePadrao("AND", Fca10.class) + 
                	 " ORDER BY abh80codigo, fca10data, fca1001hrBase";

		Query query = getSession().createQuery(sql);
		query.setParameter("situacoes", situacoes);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		
		return query.getListTableMap();
	}
	
	/**Método Principal
	 * @return Abh1301 (Marcações)
	 */
	private Abh1301 findMarcacoesByUniqueKey(Long idAbh13, LocalDate data) {
		return getSession().createCriteria(Abh1301.class)
				.addJoin(Joins.fetch("abh1301horario"))
				.addWhere(Criterions.eq("abh1301data", data))
				.addWhere(Criterions.eq("abh1301mapHor", idAbh13))
				.get();
	}
	
	/**Método Principal
	 * @return Abh1301 (Marcações Tolerantes)
	 */
	private boolean isMarcacaoTolerante(Abh11 abh11, LocalTime fca1001hrBase) {
		if(abh11 == null) return false;
		
		if(abh11.getAbh11horaE() != null && DateUtils.dateDiff(abh11.getAbh11horaE(), fca1001hrBase, ChronoUnit.MINUTES) == 0) return true;
		else if(abh11.getAbh11intervE() != null && DateUtils.dateDiff(abh11.getAbh11intervE(), fca1001hrBase, ChronoUnit.MINUTES) == 0) return true;
		else if(abh11.getAbh11horaS() != null && DateUtils.dateDiff(abh11.getAbh11horaS(), fca1001hrBase, ChronoUnit.MINUTES) == 0) return true;
		else if(abh11.getAbh11intervS() != null && DateUtils.dateDiff(abh11.getAbh11intervS(), fca1001hrBase, ChronoUnit.MINUTES) == 0) return true;
		return false;
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFZlcmlmaWNhw6fDtWVzIHBvciBBY2Vzc28iLCJ0aXBvIjoicmVsYXRvcmlvIn0=