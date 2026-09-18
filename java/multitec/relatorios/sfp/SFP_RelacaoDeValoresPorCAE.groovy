package multitec.relatorios.sfp;

import java.time.LocalDate;

import org.jfree.data.time.Month;

import br.com.multiorm.Query;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

/**Classe para relatório SFP - Relação de Valores por CAE
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeValoresPorCAE extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Valores por CAE";
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
		List<Long> idsCaes = getListLong("cae");
		List<Long> idsSindicatos = getListLong("sindicato");
		LocalDate[] periodo = getIntervaloDatas("periodo");
		boolean isPeriodoCalc = getInteger("periodoCalc") == 0 ? true : false;
		
		Set<Integer> tiposCalc = obterTiposCalculo();
		if(tiposCalc == null || tiposCalc.size() == 0) {
			throw new ValidacaoException("Necessário informar um tipo de cálculo.");
		}
		
		Integer detalhamento = getInteger("detalhamento");
		Integer ordenacao = getInteger("ordenacao");
		
		boolean isTrabalhadores= get("isTrabalhadores");
		
		Set<Integer> tiposTrab = obterTipoTrabalhador();
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("PERIODO", DateUtils.formatDate(periodo[0]) + " à " + DateUtils.formatDate(periodo[1]));
		params.put("EXIBIR_TRAB", isTrabalhadores);
		params.put("StreamSub1R1", carregarArquivoRelatorio("SFP_RelacaoDeValoresPorCAE_R1_S1"));
		
		List<TableMap> dados = findDadosFba01011sByRelacaoPorCAE(idsTrabalhadores, idsDeptos, idsCargos, idsCaes, idsSindicatos, periodo, tiposTrab, tiposCalc, isPeriodoCalc, ordenacao);
		
		List<TableMap> relatorio = new ArrayList<>();
		List<TableMap> relatorioSub = new ArrayList<>();
		carregarDadosRelatorio(dados, relatorio, relatorioSub, detalhamento, isPeriodoCalc, ordenacao);
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(relatorio);
		dsPrincipal.addSubDataSource("DsSub1R1", relatorioSub, "key", "key");
		
		String nomeRelatorio = ordenacao == 3 ? "SFP_RelacaoDeValoresPorCAE_R4" : ordenacao == 2 ? "SFP_RelacaoDeValoresPorCAE_R2" : detalhamento == 2 ? "SFP_RelacaoDeValoresPorCAE_R3" : "SFP_RelacaoDeValoresPorCAE_R1";
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
	 * Compõe os dados do relatório
	 */
	private void carregarDadosRelatorio(List<TableMap> dados, List<TableMap> relatorio, List<TableMap> relatorioSub, Integer detalhamento, boolean isPeriodoCalc, Integer ordenacao){
		//detalhe 0-Geral 1-Evento 2-Mês
		
		if(dados != null && dados.size() > 0) {
			BigDecimal zero = new BigDecimal(0);
			
			if(detalhamento == 2) {

				BigDecimal vlrJan = zero;
				BigDecimal vlrFev = zero;
				BigDecimal vlrMar = zero;
				BigDecimal vlrAbr = zero;
				BigDecimal vlrMai = zero;
				BigDecimal vlrJun = zero;
				BigDecimal vlrJul = zero;
				BigDecimal vlrAgo = zero;
				BigDecimal vlrSet = zero;
				BigDecimal vlrOut = zero;
				BigDecimal vlrNov = zero;
				BigDecimal vlrDez = zero;
				BigDecimal totalValor = zero;
				String cod = ""
				
				for(int i = 0; i < dados.size(); i++) {
					LocalDate mesAno = isPeriodoCalc ? dados.get(i).getDate("fba0101dtCalc") : dados.get(i).getDate("fba0101dtPgto");
					String codAbh20 = dados.get(i).getString("abh20codigo");


					int cv = dados.get(i).getInteger("abh2101cvr");
					BigDecimal valor = dados.get(i).getBigDecimal("fba01011valor");

					if (cod != codAbh20){
						vlrJan = zero;
						vlrFev = zero;
						vlrMar = zero;
						vlrAbr = zero;
						vlrMai = zero;
						vlrJun = zero;
						vlrJul = zero;
						vlrAgo = zero;
						vlrSet = zero;
						vlrOut = zero;
						vlrNov = zero;
						vlrDez = zero;
						totalValor = zero;
					}
					
					if(mesAno.getMonthValue() == Month.JANUARY) {
						vlrJan = cv == 0 ? vlrJan.add(valor) : vlrJan.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.FEBRUARY) {
						vlrFev = cv == 0 ? vlrFev.add(valor) : vlrFev.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.MARCH) {
						vlrMar = cv == 0 ? vlrMar.add(valor) : vlrMar.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.APRIL) {
						vlrAbr = cv == 0 ? vlrAbr.add(valor) : vlrAbr.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.MAY) {
						vlrMai = cv == 0 ? vlrMai.add(valor) : vlrMai.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.JUNE) {
						vlrJun = cv == 0 ? vlrJun.add(valor) : vlrJun.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.JULY) {
						vlrJul = cv == 0 ? vlrJul.add(valor) : vlrJul.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.AUGUST) {
						vlrAgo = cv == 0 ? vlrAgo.add(valor) : vlrAgo.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.SEPTEMBER) {
						vlrSet = cv == 0 ? vlrSet.add(valor) : vlrSet.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.OCTOBER) {
						vlrOut = cv == 0 ? vlrOut.add(valor) : vlrOut.subtract(valor);
					} else if(mesAno.getMonthValue() == Month.NOVEMBER) {
						vlrNov = cv == 0 ? vlrNov.add(valor) : vlrNov.subtract(valor);
					} else  {
						vlrDez = cv == 0 ? vlrDez.add(valor) : vlrDez.subtract(valor);
					}
				 
					totalValor = cv == 0 ? totalValor.add(valor) : totalValor.subtract(valor);
					
					String codAbh80 = dados.get(i).getString("abh80codigo");

					cod = codAbh20
					String key = codAbh80 + "/" + codAbh20;
					
					
					if(i == (dados.size() - 1) || !codAbh80.equals(dados.get(i+1).getString("abh80codigo")) || !codAbh20.equals(dados.get(i+1).getString("abh20codigo"))) {
						dados.get(i).put("key", key);
						dados.get(i).put("totalRef", zero);
						dados.get(i).put("totalValor", totalValor);
						dados.get(i).put("vlrJan", vlrJan);
						dados.get(i).put("vlrFev", vlrFev);
						dados.get(i).put("vlrMar", vlrMar);
						dados.get(i).put("vlrAbr", vlrAbr);
						dados.get(i).put("vlrMai", vlrMai);
						dados.get(i).put("vlrJun", vlrJun);
						dados.get(i).put("vlrJul", vlrJul);
						dados.get(i).put("vlrAgo", vlrAgo);
						dados.get(i).put("vlrSet", vlrSet);
						dados.get(i).put("vlrOut", vlrOut);
						dados.get(i).put("vlrNov", vlrNov);
						dados.get(i).put("vlrDez", vlrDez);
						
						relatorio.add(dados.get(i));
					}
				}
			} else {
				BigDecimal totalRef = zero;
				BigDecimal totalValor = zero;
				
				for(int i = 0; i < dados.size(); i++ ) {
					int cv = dados.get(i).getInteger("abh2101cvr");
					BigDecimal ref = dados.get(i).getBigDecimal("fba01011refHoras");
					BigDecimal valor = dados.get(i).getBigDecimal("fba01011valor");
					
					totalRef = totalRef.add(ref);
					totalValor = cv == 0 ? totalValor.add(valor) : totalValor.subtract(valor);
					
					String codGrupo = ordenacao == 3 ? dados.get(i).getString("abb11codigo") : dados.get(i).getString("abh80codigo");
					String codGrupoProx = (i == dados.size() -1) ? null : ordenacao == 3 ? dados.get(i+1).getString("abb11codigo") : dados.get(i+1).getString("abh80codigo");
					String codAbh20 = dados.get(i).getString("abh20codigo");
					String codAbh20Prox = (i == dados.size() -1) ? null : dados.get(i+1).getString("abh20codigo");
					String key = ordenacao == 3 ? null : codGrupo + "/" + codAbh20;
					
					if(codGrupoProx == null || !codGrupo.equals(codGrupoProx) || !codAbh20.equals(codAbh20Prox)) {
						TableMap ds = new TableMap();
						ds.put("key", key);
						ds.put("abh80codigo", ordenacao == 3 ? null : codGrupo);
						ds.put("abh80nome", ordenacao == 3 ? null : dados.get(i).getString("abh80nome"));
						ds.put("abb11codigo", dados.get(i).getString("abb11codigo"));
						ds.put("abb11nome", dados.get(i).getString("abb11nome"));
						ds.put("abh20codigo", codAbh20);
						ds.put("abh20nome", dados.get(i).getString("abh20nome"));
						ds.put("totalRef", totalRef);
						ds.put("totalValor", totalValor);
						ds.put("vlrJan", zero);
						ds.put("vlrFev", zero);
						ds.put("vlrMar", zero);
						ds.put("vlrAbr", zero);
						ds.put("vlrMar", zero);
						ds.put("vlrAbr", zero);
						ds.put("vlrMai", zero);
						ds.put("vlrJun", zero);
						ds.put("vlrJul", zero);
						ds.put("vlrAgo", zero);
						ds.put("vlrSet", zero);
						ds.put("vlrOut", zero);
						ds.put("vlrNov", zero);
						ds.put("vlrDez", zero);
						
						relatorio.add(ds);
						
						totalRef = zero;
						totalValor = zero;
					}
					
					if(detalhamento == 1) {
						TableMap sub = new TableMap();
						sub.put("key", key);
						sub.put("abh21codigo", dados.get(i).getString("abh21codigo"));
						sub.put("abh21nome", dados.get(i).getString("abh21nome"));
						sub.put("fba01011refHoras", ref);
						sub.put("fba01011valor",  cv == 0 ? valor : DecimalUtils.create(valor).multiply(-1).get());
						
						relatorioSub.add(sub);
					}
				}
			}
		}
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca da Relação por CAE - Eventos)
	 */
	private List<TableMap> findDadosFba01011sByRelacaoPorCAE(List<Long> idsTrabalhadores, List<Long> idsDeptos, List<Long> idsCargos, List<Long> idsCaes, List<Long> idsSindicatos, LocalDate[] periodo, Set<Integer> tiposAbh80, Set<Integer> tiposFba0101, boolean isPeriodoCal, Integer ordenacao){
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String whereCargos = idsCargos != null && !idsCargos.isEmpty() ? " AND abh05id IN (:idsCargos) " : "";
		String whereCae = idsCaes != null && !idsCaes.isEmpty() ? " AND abh20id IN (:idsCaes)" : "";
		String whereSindicato = idsSindicatos != null && !idsSindicatos.isEmpty() ? " AND abh03id IN (:idsSindicatos) " : "";
		String data = isPeriodoCal ? "fba0101dtCalc" : "fba0101dtPgto";
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, data) : "";
		//ordenacao 0-Código trabalhador 1-Nome trabalhador 2-CAE 3-Depto
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo, abh20codigo, abh21codigo" : ordenacao == 1 ? " ORDER BY abh80nome, abh20codigo, abh21codigo" : ordenacao == 2 ? " ORDER BY abh20codigo, abh80nome" : " ORDER BY abb11codigo, abh20codigo";
		
		String sql = "SELECT abh80codigo, abh80nome, abb11codigo, abb11nome, abh21codigo, abh21nome, abh2101cvr, abh20codigo, abh20nome, fba01011refHoras, fba01011valor, fba0101dtCalc, fba0101dtPgto " +
					 "FROM Fba01011 " +
					 "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abh05 ON abh05id = abh80cargo " +
					 "INNER JOIN Abb11 ON abb11id = fba01011depto " +
					 "INNER JOIN Abh21 ON abh21id = fba01011eve " +
					 "INNER JOIN Abh2101 ON abh2101evento = abh21id " +
					 "LEFT JOIN Abh03 ON abh03id = abh80sindSindical " +
					 "INNER JOIN Abh20 ON abh20id = abh2101cae " +
					 whereData + " AND abh80tipo IN (:tiposAbh80) AND fba0101tpVlr IN (:tiposFba0101) AND (fba01011refHoras > 0 OR fba01011valor > 0) " +
					 whereTrabalhadores + whereDeptos + whereCargos + whereCae + whereSindicato + getSamWhere().getWherePadrao("AND", Fba01.class) +
					 ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("tiposAbh80", tiposAbh80);
		query.setParameter("tiposFba0101", tiposFba0101);
		
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		if(idsCargos != null && !idsCargos.isEmpty()) query.setParameter("idsCargos", idsCargos);
		if(idsCaes != null && !idsCaes.isEmpty()) query.setParameter("idsCaes", idsCaes);
		if(idsSindicatos != null && !idsSindicatos.isEmpty()) query.setParameter("idsSindicatos", idsSindicatos);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBWYWxvcmVzIHBvciBDQUUiLCJ0aXBvIjoicmVsYXRvcmlvIn0=