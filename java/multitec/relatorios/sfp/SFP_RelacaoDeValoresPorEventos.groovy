package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Relação de Valores por Eventos
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeValoresPorEventos extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Valores por Eventos";
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
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("periodoCalc", "0");
		filtrosDefault.put("calcFolha", true);
		filtrosDefault.put("calcFerias", true);
		filtrosDefault.put("calcRescisao", true);
		filtrosDefault.put("calcAdiantamento", true);
		filtrosDefault.put("calc13sal", true);
		filtrosDefault.put("calcPlr", true);
		filtrosDefault.put("calcOutros", true);
		filtrosDefault.put("detalhamento", "0");
		filtrosDefault.put("ordenacao", "0");
		filtrosDefault.put("isTrabalhadores", true);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		List<Long> idsCargos = getListLong("cargos");
		List<Long> idsEventos = getListLong("evento");
		List<Long> idsSindicatos = getListLong("sindicato");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		boolean isPeriodoCalc = getInteger("periodoCalc") == 0 ? true : false;
		Set<Integer> tiposCalc = obterTiposCalculo();
		if(tiposCalc == null || tiposCalc.size() == 0) {
			throw new ValidacaoException("Necessário informar um tipo de cálculo.");
		}
		
		Integer detalhamento = getInteger("detalhamento");
		Integer ordenacao = getInteger("ordenacao");
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		params.put("StreamSub1R3", carregarArquivoRelatorio("SFP_RelacaoDeValoresPorEventos_R3_S1"));
		params.put("StreamSub2R3", carregarArquivoRelatorio("SFP_RelacaoDeValoresPorEventos_R3_S2"));
		params.put("StreamSub1R4", carregarArquivoRelatorio("SFP_RelacaoDeValoresPorEventos_R4_S1"));
		params.put("StreamSub2R4", carregarArquivoRelatorio("SFP_RelacaoDeValoresPorEventos_R4_S2"));
		
		List<TableMap> dados = findDadosFba01011sByRelacaoPorEventos(idsTrabalhadores, idsDeptos, idsCargos, idsEventos, idsSindicatos, periodo, tiposTrab, tiposCalc, isPeriodoCalc, ordenacao);
		
		List<TableMap> relatorio = new ArrayList<>();
		List<TableMap> relatorioSubSintetico = new ArrayList<>();
		List<TableMap> relatorioSubAnalitico = new ArrayList<>();
		carregarDadosRelatorio(relatorio, relatorioSubSintetico, relatorioSubAnalitico, dados, ordenacao, detalhamento, idsDeptos, idsCargos, idsSindicatos, idsEventos, periodo, tiposTrab, tiposCalc, isPeriodoCalc);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(relatorio);
		dsPrincipal.addSubDataSource(ordenacao == 2 ? "DsSub1R4" : "DsSub1R3", relatorioSubSintetico, "key", "key");
		dsPrincipal.addSubDataSource(ordenacao == 2 ? "DsSub2R4" : "DsSub2R3", relatorioSubAnalitico, "key", "key");
		
		String nomeRelatorio = null;
		if(detalhamento == 0) {
			if(ordenacao == 2) {
				nomeRelatorio = "SFP_RelacaoDeValoresPorEventos_R2";
			}else {
				nomeRelatorio = "SFP_RelacaoDeValoresPorEventos_R1";
			}
		}else {
			if(ordenacao == 2) {
				nomeRelatorio = "SFP_RelacaoDeValoresPorEventos_R4";
			}else {
				nomeRelatorio = "SFP_RelacaoDeValoresPorEventos_R3";
			}
		}
		return gerarPDF(nomeRelatorio, dsPrincipal);
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
	 * @return Set Integer (Tipo de Cálculo)
	 */
	private Set<Integer> obterTiposCalculo(){
		Set<Integer> calc = new HashSet<>();
		
		if((boolean) get("calcFolha")) calc.add(0);
		if((boolean) get("calcAdiantamento")) calc.add(1);
		if((boolean) get("calc13sal")) calc.add(2);
		if((boolean) get("calcFerias")) calc.add(3);
		if((boolean) get("calcRescisao")) calc.add(4);
		if((boolean) get("calcPlr")) calc.add(6);
		if((boolean) get("calcOutros")) calc.add(9);
		
		if(calc.size() == 0) {
			calc.add(0);
			calc.add(1);
			calc.add(2);
			calc.add(3);
			calc.add(4);
			calc.add(6);
			calc.add(9);
		}
		return calc;
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca da Relação Valores por Eventos)
	 */
	private List<TableMap> findDadosFba01011sByRelacaoPorEventos(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsEventos, List<Long> idsSindicatos, LocalDate[] periodo, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, boolean isPeriodoCalc, Integer ordenacao){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereEventos = idsEventos != null && !idsEventos.isEmpty() ? " AND abh21id IN (:idsEventos)" : "";
		String whereSindicato = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String data = isPeriodoCalc ? "fba0101dtCalc" : "fba0101dtPgto";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, data) : "";
		//ordenacao 0-Código trabalhador 1-Nome trabalhador 2-Depto
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo " : (ordenacao == 1 ? " ORDER BY abh80nome " : " ORDER BY abb11codigo, abh80codigo ");
		
		String sql = "SELECT abh80codigo, abh80nome, abb11codigo, abb11nome, abh05codigo, abh05nome, abb11codigo, abb11nome, abh21tipo, abh21codigo, (CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, fba01011valor " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + " AND abh80tipo IN (:tiposAbh80) AND fba0101tpVlr IN (:tiposFba0101) AND (fba01011refHoras > 0 OR fba01011valor > 0  AND fba01011refUnid > 0 OR fba01011valor > 0 AND fba01011refDias > 0 OR fba01011valor > 0) " +
					 whereTrabalhadores + whereDeptos + whereCargos + whereEventos + whereSindicato + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsEventos != null && !idsEventos.isEmpty()) query.setParameter("idsEventos", idsEventos);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
		
		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca da Eventos pela Relação Valores por Eventos)
	 */
	public List<TableMap> findDadosFba01011sEventosByRelacaoPorEventos(List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsEventos, List<Long> idsSindicatos, String codAbh80, String codAbb11, LocalDate[] periodo, Set<Integer> tiposFba0101, boolean isPeriodoCalc, boolean totalizarEventos, Integer ordenacao) {
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereEventos = idsEventos != null && !idsEventos.isEmpty() ? " AND abh21id IN (:idsEventos)" : "";
		String whereSindicato = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereDeptoFinal = ordenacao == 2 ? " AND abb11codigo = :codAbb11 " : whereDeptos;
		String data = isPeriodoCalc ? "fba0101dtCalc" : "fba0101dtPgto";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, data) : "";
		String refAndValor = totalizarEventos ? ", SUM(CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, SUM(fba01011valor) as totalValor " : ", (CASE WHEN fba01011refHoras > 0 THEN fba01011refHoras WHEN fba01011refDias > 0 THEN fba01011refDias ELSE fba01011refUnid END) as totalRef, fba01011valor, " + data + " as periodo ";
		String grupo = totalizarEventos ? " GROUP BY abh21codigo, abh21nome, abh21tipo  " : " ";
		String ordem = totalizarEventos ? " ORDER BY abh21codigo" : " ORDER BY abh21codigo, " + data;
		
		String sql = "SELECT abh21codigo, abh21nome, abh21tipo" + refAndValor +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 whereData + " AND fba0101tpVlr IN (:tiposFba0101) AND abh80codigo = :codAbh80 AND (fba01011refHoras > 0 OR fba01011valor > 0  AND fba01011refUnid > 0 OR fba01011valor > 0 AND fba01011refDias > 0 OR fba01011valor > 0)" +
					 whereDeptoFinal + whereCargos + whereEventos + whereSindicato + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 grupo + ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("codAbh80", codAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
			
		if(ordenacao == 2) {
			query.setParameter("codAbb11", codAbb11);
		}else {
			if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		}
		
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsEventos != null && !idsEventos.isEmpty()) query.setParameter("idsEventos", idsEventos);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);

		return query.getListTableMap();
	}
	
	/**Método Diverso
	 * Compõe os dados do relatório
	 */
	private void carregarDadosRelatorio(List<TableMap> relatorio, List<TableMap> relatorioSubSintetico, List<TableMap> relatorioSubAnalitico, List<TableMap> dados, Integer ordenacao, Integer detalhamento, List<Long> idsDeptos, List<Long> idsCargos,
			List<Long> idsSindicatos, List<Long> idsEventos, LocalDate[] periodo, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, boolean isPeriodoCalc){
		
		//Insere os dados no mapa somando as referências e valores (se o evento for do tipo desconto subtrai o valor ou soma se for de qualquer outro tipo).
		if(dados != null && dados.size() > 0) {
			BigDecimal zero = new BigDecimal(0);
			BigDecimal totalRef = zero;
			BigDecimal totalValor = zero;

			for(int i = 0; i < dados.size(); i++) {
				totalRef = totalRef.add(dados.get(i).getBigDecimal("totalRef"));
				totalValor = totalValor.add(dados.get(i).getBigDecimal("fba01011valor"));

				String codAbh80 = dados.get(i).getString("abh80codigo");
				String codAbb11 = dados.get(i).getString("abb11codigo");
				if(i == (dados.size() - 1) || !codAbh80.equals(dados.get(i+1).getString("abh80codigo")) ||(ordenacao == 2 && !codAbb11.equals(dados.get(i+1).getString("abb11codigo")))) {
					String key = ordenacao == 2 ? codAbh80 + "/" + codAbb11 : codAbh80;
					
					dados.get(i).put("key", key);
					dados.get(i).put("totalRef", totalRef);
					dados.get(i).put("totalValor", totalValor);
					
					relatorio.add(dados.get(i));
					
					totalRef = zero;
					totalValor = zero;
				}
			}
		}
		
		//Verifica se é Sintética ou Analítica para agregar o SubDataSet
		if(detalhamento == 1 || detalhamento == 2) {
			for(int i = 0; i < relatorio.size(); i++) {
				
				String codAbh80 = relatorio.get(i).getString("abh80codigo");
				String codAbb11 = relatorio.get(i).getString("abb11codigo");
				boolean totalizarEventos = detalhamento == 1 ? true : false;
				
				List<TableMap> dadosSub = findDadosFba01011sEventosByRelacaoPorEventos(idsDeptos, idsCargos, idsEventos, idsSindicatos, codAbh80, codAbb11, periodo, tiposFba0101, isPeriodoCalc, totalizarEventos, ordenacao);
				if(dadosSub != null && dadosSub.size() > 0) {
					for(TableMap sub : dadosSub) {
						
						if(detalhamento == 1) {
							sub.put("key", relatorio.get(i).getString("key"));
							
							relatorioSubSintetico.add(sub);
						}else {
							sub.put("key", relatorio.get(i).getString("key"));
							
							relatorioSubAnalitico.add(sub);
						}
					}
				}
			}
		}
	
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBWYWxvcmVzIHBvciBFdmVudG9zIiwidGlwbyI6InJlbGF0b3JpbyJ9