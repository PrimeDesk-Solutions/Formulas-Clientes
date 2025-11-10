package multitec.relatorios.sca;

import java.time.LocalDate;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.ab.Aba01;
import sam.model.entities.fc.Fca10;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SCA - Pontos Totalizados
 * @author Lucas Eliel
 * @since 10/05/2019
 * @version 1.0
 */

public class SCA_PontosTotalizados extends RelatorioBase{

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SCA - Pontos Totalizados";
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
		params.put("COM0", getParametros(Parametros.ABH1301_COMPLEMENTO0));
		params.put("COM1", getParametros(Parametros.ABH1301_COMPLEMENTO1));
		params.put("COM2", getParametros(Parametros.ABH1301_COMPLEMENTO2));
		params.put("COM3", getParametros(Parametros.ABH1301_COMPLEMENTO3));
		params.put("COM4", getParametros(Parametros.ABH1301_COMPLEMENTO4));
		
		List<TableMap> pontosTotalizados =  getPontosTotalizados(idsTrabalhador, idsDepartamento, idsMapHorario, datas, situacoes);
		
		if(pontosTotalizados != null && !pontosTotalizados.isEmpty()) {
			for(TableMap map : pontosTotalizados) {
				
				Integer het = map.getInteger("fca10heDiu") + map.getInteger("fca10heNot");
		
				map.put("het", het);
				
			}
		}
		return gerarPDF("SCA_PontosTotalizados", pontosTotalizados);
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
	public String getParametros(Parametro param){
		
		String aba01 = getSession().createCriteria(Aba01.class)
		.addFields("Aba01conteudo")
		.addWhere(Criterions.eq("aba01param", param.getParam().toUpperCase()))
		.addWhere(Criterions.eq("aba01aplic", "ABH1301"))
		.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
		.get(ColumnType.STRING);
		
		return aba01;
	}

	/**Método Diverso
	 * @return 	List TableMap (Pontos Totalizados)
	 */
	private List<TableMap> getPontosTotalizados(List<Long> idsTrabalhador, List<Long> idsDepartamento, List<Long> idsMapHorario, LocalDate[] datas, Set<Integer> situacoes){
		String whereAbh80Trabalhador = idsTrabalhador != null && !idsTrabalhador.isEmpty() ? "AND abh80id IN (:idsTrabalhador) " : "";
		String whereAbb11Depto = idsDepartamento != null && !idsDepartamento.isEmpty() ? "AND abb11id IN (:idsDepartamento) " : "";
		String whereAbh13MapHorario = idsMapHorario != null && !idsMapHorario.isEmpty() ? "AND abh13id IN (:idsMapHorario) " : "";
		String whereDt = datas != null ? getWhereDataInterval("WHERE", datas, "fca10data") : "";
	
		String sql = "SELECT abh80codigo, abh80nome, fca10data, abh08descr, abh13codigo, abb11codigo, fca10sit, abh07codigo, fca10jorBru, fca10jorLiq, " +
                	 "fca10complem0, fca10complem1, fca10complem2, fca10complem3, fca10complem4, " +
                	 "fca10horDiu, fca10horNot, fca10heDiu, fca10heNot, abh12codigo, fca10horFalt, fca10hpComp, fca10hnComp " +
                	 "FROM Fca10 " +
                	 "INNER JOIN Abh80 ON abh80id = fca10trab " +
                	 "INNER JOIN Abb11 ON abb11id = fca10depto " +
                	 "INNER JOIN Abh08 ON abh08id = fca10tpDia " +
                	 "INNER JOIN Abh13 ON abh13id = fca10mapHor " +
                	 "LEFT JOIN Abh07 ON abh07id = fca10motAfast " +
                	 "LEFT JOIN Abh12 ON abh12id = fca10divHE " +
                	 whereDt + "AND fca10consistente = :cons AND fca10sit IN (:sit) " +
                	 whereAbh80Trabalhador + whereAbb11Depto + whereAbh13MapHorario + getSamWhere().getWherePadrao("AND", Fca10.class) +
                	 " ORDER BY abh80codigo, fca10data";
		
		Query query = getSession().createQuery(sql);
		
		if(idsTrabalhador != null && !idsTrabalhador.isEmpty()) query.setParameter("idsTrabalhador", idsTrabalhador);
		if(idsDepartamento != null && !idsDepartamento.isEmpty()) query.setParameter("idsDepartamento", idsDepartamento);
		if(idsMapHorario != null && !idsMapHorario.isEmpty()) query.setParameter("idsMapHorario", idsMapHorario);
		query.setParameter("sit", situacoes);
		query.setParameter("cons", 1);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNDQSAtIFBvbnRvcyBUb3RhbGl6YWRvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==