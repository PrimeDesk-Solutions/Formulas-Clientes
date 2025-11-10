package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh1301;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Relação de Trabalhadores em Faltas no Dia
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_RelacaoDeTrabalhadoresEmFaltasNoDia extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Relação de Trabalhadores em Faltas no Dia";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		filtrosDefault.put("data", MDate.date());
		filtrosDefault.put("ordenacao", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsMapHorario = getListLong("mapHorario");
		int ordenar = getInteger("ordenacao");
		LocalDate data = getLocalDate("data");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("DATA", DateUtils.formatDate(data));
		
		List<TableMap> dados = findDadosAbh80sByRelatorioFaltasNoDia(idsDeptos, idsMapHorario, data, ordenar == 0 ? true : false);
		List<TableMap> mapAbh80 = new ArrayList<>();
		
		if(dados != null && !dados.isEmpty()) {
			for(TableMap abh80 : dados) {
				
				TableMap tableMapAbh80 = new TableMap();
				
				boolean existePonto = existePontoNoDia(abh80.getLong("abh80id"), data);
				if(!existePonto) {
					
					Abh1301 abh1301 = findJornadaByUniqueKey(data, abh80.getLong("abh13id"));
					if(abh1301 != null) {
						String horario = StringUtils.concat(abh80.getString("abh13codigo"), " - ", abh80.getString("abh13nome"));
						
						Integer jorLiq = abh1301.getAbh1301horario() != null ? abh1301.getAbh1301horario().getAbh11jorLiq() : 0;
						String jornada = jorLiq == 0 ? "" : StringUtils.concat(String.format("%02d",  (jorLiq/60)), ":", String.format("%02d",  (jorLiq%60)));
						
						tableMapAbh80.put("trabalhador", abh80.getString("trabalhador"));
						tableMapAbh80.put("horario", horario);
						tableMapAbh80.put("depto", abh80.getString("depto"));
						tableMapAbh80.put("jornada", jornada);
						
						mapAbh80.add(tableMapAbh80);
					}
				}
			}
		}
		return gerarPDF("SCA_RelacaoDeTrabalhadoresEmFaltasNoDia", mapAbh80);
	}

	/**Método Diverso
	 * @return 	List TableMap (Faltas No Dia)
	 */
	private List<TableMap> findDadosAbh80sByRelatorioFaltasNoDia(List<Long> idsDeptos, List<Long> idsMapHorario, LocalDate data, boolean ordenarPorTrab){
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereMapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? " AND abh13id IN (:idsMapHorario) " : "";
		String ordena = ordenarPorTrab ? " ORDER BY abh80codigo" : " ORDER BY abb11codigo";
		
		String sql = "SELECT abh80id, abh13id, abh13codigo, abh13nome, " +
					 Fields.concat("abh80codigo", "'-'", "abh80nome") + " AS trabalhador, " +
					 Fields.concat("abb11codigo", "'-'", "abb11nome") + " AS depto " +
			     	 "FROM Abh80 " +
			     	 "INNER JOIN Abb11 ON abb11id = abh80depto " +
			     	 "LEFT JOIN Abh13 ON abh13id = abh80mapHor " +
			     	 "WHERE abh80sit <> 1 AND abh80id NOT IN (SELECT fbc01trab FROM Fbc01 INNER JOIN Fbc0101 ON fbc01id = fbc0101pa WHERE :data BETWEEN fbc0101pgi AND fbc0101pgf) " +
			     	 "AND abh80id NOT IN (SELECT fbb01trab FROM Fbb01 WHERE (:data >= fbb01dtSai AND fbb01dtRet IS NULL) OR (fbb01dtRet IS NOT NULL AND :data BETWEEN fbb01dtSai AND fbb01dtRet)) " +
			     	 "AND abh80id NOT IN (SELECT fca01trab FROM Fca01 WHERE :data BETWEEN fca01dtI AND fca01dtF) " +
			     	 whereDeptos + whereMapHorario + 
			     	 getSamWhere().getWherePadrao("AND", Abh80.class) +
			     	 ordena;
		
		Query query = getSession().createQuery(sql);
		
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("data", data);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	Boolean (Existe Ponto No Dia)
	 */
	private boolean existePontoNoDia(Long idAbh80, LocalDate data) {
		String sql = "SELECT * FROM Fca10 " +
			     	 "WHERE fca10trab = :idAbh80 AND fca10data = :data AND fca10sit = 0 " +
			     	 getSamWhere().getWherePadrao("AND", Fca10.class);
	
		Query query = getSession().createQuery(sql);
		query.setParameter("idAbh80", idAbh80);
		query.setParameter("data", data);
		query.setMaxResult(1);
	
		Fca10 fca10 = (Fca10) query.getUniqueResult(ColumnType.ENTITY);
		return fca10 != null;
	}
	
	/**Método Diverso
	 * @return 	Abh1301 (Busca Jornada - Marcações)
	 */
	private Abh1301 findJornadaByUniqueKey(LocalDate data, Long idAbh13) {
		return getSession().createCriteria(Abh1301.class)
				.addJoin(Joins.fetch("abh1301horario"))
				.addWhere(Criterions.eq("abh1301data", data))
				.addWhere(Criterions.eq("abh1301mapHor", idAbh13))
				.get();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFJlbGHDp8OjbyBkZSBUcmFiYWxoYWRvcmVzIGVtIEZhbHRhcyBubyBEaWEiLCJ0aXBvIjoicmVsYXRvcmlvIn0=