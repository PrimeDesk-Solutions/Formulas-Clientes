package multitec.relatorios.sfp;

import java.time.LocalDate;

import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.dicdados.Parametro;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros;
import sam.model.entities.ab.Aba01;
import sam.model.entities.fb.Fba01;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

/**Classe para relatório SFP - Relação de Férias Pagas
 * @author Lucas Eliel
 * @since 06/05/2019
 * @version 1.0
 */

public class SFP_RelacaoDeFeriasPagas extends RelatorioBase {

	/**Método Principal
	 * @return String (Nome do Relatório)
	 */
	@Override
	public String getNomeTarefa() {
		return "SFP - Relação de Férias/Abonos Pagos";
	}
	
	/**Método Principal
	 * @return Map (Filtros do Front-end)
	 */
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		LocalDate[] dataPgto = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dataPgto", dataPgto);
		filtrosDefault.put("ordenacao", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	/**Método Principal
	 * @return dados do pdf
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsDeptos = getListLong("departamentos");
		LocalDate[] dataPgto = getIntervaloDatas("dataPgto");
		Integer ordenacao = getInteger("ordenacao");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("DT_INICIAL", DateUtils.formatDate(dataPgto[0]));
		params.put("DT_FINAL", DateUtils.formatDate(dataPgto[0]));
		
		String feriasLiquida = getParametros(Parametros.FB_EVELIQFERIAS);
		if(feriasLiquida == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQFERIAS.");
		
		String abonoLiquido = getParametros(Parametros.FB_EVELIQABONO);
		if(abonoLiquido == null) throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro FB_EVELIQABONO.");
		
		List<TableMap> dados = findDadosFba01011sByRelatorioFeriasPagas(idsTrabalhadores, idsDeptos, dataPgto, ordenacao, feriasLiquida, abonoLiquido);
		if(dados != null && dados.size() > 0) {
			for(TableMap map : dados) {
				
				String key = ordenacao == 0 ? map.getString("abh80codigo") + "/" + DateUtils.formatDate(map.getDate("fbc0101dtPagto")) : 
					 ordenacao == 1 ? map.getString("abh80nome") + "/" + DateUtils.formatDate(map.getDate("fbc0101dtPagto")) : 
					 map.getString("abb11codigo") + "/" + map.getString("abh80codigo") + "/" + DateUtils.formatDate(map.getDate("fbc0101dtPagto"));
		
				int tipo = map.getInteger("fba01011tipo");
				BigDecimal valor = map.getBigDecimal(tipo == 0 ? "valorFerias" : "valorAbono");
				if(valor == null) valor = new BigDecimal(0);
		
				map.put("key", key);
				
				if(tipo == 0) {
					map.put("valorFerias", valor.add(map.getBigDecimal("fba01011valor")));
					map.put("valorAbono", BigDecimal.ZERO);
				}else if(tipo == 1) {
					map.put("valorAbono", valor.add(map.getBigDecimal("fba01011valor")));
					map.put("valorFerias", BigDecimal.ZERO);
				}
				
				BigDecimal valFerias = map.getBigDecimal("valorFerias") != null  ? map.getBigDecimal("valorFerias") : BigDecimal.ZERO;
				BigDecimal valAbono = map.getBigDecimal("valorAbono") != null ? map.getBigDecimal("valorAbono") : BigDecimal.ZERO;
				
				map.put("valorPago", valFerias.add(valAbono));
			}
		}
		
		Collections.sort(dados, new Comparator<TableMap>() {
	        @Override
	        public int compare(TableMap tm1, TableMap tm2)
	        {
	            return  tm1.getString("key").compareTo(tm2.getString("key"));
	        }
	    });
		
		
		List<TableMap> dadosLst = new ArrayList<>();
		int i = 0;
		
		for(TableMap mapDados : dados) {
			
			TableMap mapDadosPrincipal = new TableMap();
			
			if(dadosLst.size() <= 0) {
				
				String key = ordenacao == 0 ? mapDados.getString("abh80codigo") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto")) : 
					 ordenacao == 1 ? mapDados.getString("abh80nome") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto")) : 
				     mapDados.getString("abb11codigo") + "/" + mapDados.getString("abh80codigo") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto"));
				mapDadosPrincipal.put("abb11codigo", mapDados.getString("abb11codigo"));
				mapDadosPrincipal.put("abb11nome", mapDados.getString("abb11nome"));
				mapDadosPrincipal.put("abh80codigo", mapDados.getString("abh80codigo"));
				mapDadosPrincipal.put("abh80nome", mapDados.getString("abh80nome"));
				mapDadosPrincipal.put("fbc0101pgi", mapDados.getDate("fbc0101pgi"));
				mapDadosPrincipal.put("fbc0101pgf", mapDados.getDate("fbc0101pgf"));
				mapDadosPrincipal.put("fbc0101pbi", mapDados.getDate("fbc0101pbi"));
				mapDadosPrincipal.put("fbc0101pbf", mapDados.getDate("fbc0101pbf"));
				mapDadosPrincipal.put("fbc0101dtPagto", mapDados.getDate("fbc0101dtPagto"));
				mapDadosPrincipal.put("valorFerias", mapDados.getBigDecimal("valorFerias"));
				mapDadosPrincipal.put("valorAbono", mapDados.getBigDecimal("valorAbono"));
				mapDadosPrincipal.put("valorPago", mapDados.getBigDecimal("valorFerias").add(mapDados.getBigDecimal("valorAbono")));
				mapDadosPrincipal.put("key", key);
				dadosLst.add(mapDadosPrincipal);
			}else {
				
				if(! mapDados.get("key").equals(dadosLst.get(i).get("key"))){
					
					String key = ordenacao == 0 ? mapDados.getString("abh80codigo") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto")) : 
						 ordenacao == 1 ? mapDados.getString("abh80nome") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto")) : 
					     mapDados.getString("abb11codigo") + "/" + mapDados.getString("abh80codigo") + "/" + DateUtils.formatDate(mapDados.getDate("fbc0101dtPagto"));
					mapDadosPrincipal.put("abb11codigo", mapDados.getString("abb11codigo"));
					mapDadosPrincipal.put("abb11nome", mapDados.getString("abb11nome"));
					mapDadosPrincipal.put("abh80codigo", mapDados.getString("abh80codigo"));
					mapDadosPrincipal.put("abh80nome", mapDados.getString("abh80nome"));
					mapDadosPrincipal.put("fbc0101pgi", mapDados.getDate("fbc0101pgi"));
					mapDadosPrincipal.put("fbc0101pgf", mapDados.getDate("fbc0101pgf"));
					mapDadosPrincipal.put("fbc0101pbi", mapDados.getDate("fbc0101pbi"));
					mapDadosPrincipal.put("fbc0101pbf", mapDados.getDate("fbc0101pbf"));
					mapDadosPrincipal.put("fbc0101dtPagto", mapDados.getDate("fbc0101dtPagto"));
					mapDadosPrincipal.put("valorFerias", mapDados.getBigDecimal("valorFerias"));
					mapDadosPrincipal.put("valorAbono", mapDados.getBigDecimal("valorAbono"));
					mapDadosPrincipal.put("valorPago", mapDados.getBigDecimal("valorFerias").add(mapDados.getBigDecimal("valorAbono")));
					mapDadosPrincipal.put("key", key);
					dadosLst.add(mapDadosPrincipal);
					i++;
					
				}else {
					dadosLst.get(i).put("valorFerias", dadosLst.get(i).getBigDecimal("valorFerias").add(mapDados.getBigDecimal("valorFerias")));
					dadosLst.get(i).put("valorAbono", dadosLst.get(i).getBigDecimal("valorAbono").add(mapDados.getBigDecimal("valorAbono")));
					dadosLst.get(i).put("valorPago", dadosLst.get(i).getBigDecimal("valorFerias").add(dadosLst.get(i).getBigDecimal("valorAbono")));
				}
			}
		}
		
		return gerarPDF(ordenacao == 2 ? "SFP_RelacaoDeFeriasPagas_R2" : "SFP_RelacaoDeFeriasPagas_R1", dadosLst);
	}

	/**Método Diverso
	 * @return 	Parametros (Aba01)
	 */
	private String getParametros(Parametro param) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", "FB"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.get();
		
		String conteudo = null;
		if(aba01 != null) {
			conteudo = aba01.getAba01conteudo();
		}
		return conteudo;
	}
	
	/**Método Diverso
	 * @return 	List TableMap (Query da busca - Relação de Férias Pagas)
	 */
	private List<TableMap> findDadosFba01011sByRelatorioFeriasPagas(List<Long> idsTrabalhadores, List<Long> idsDeptos, LocalDate[] dataPgto, int ordenacao, String codEveFerias, String codEveAbono) {
		String whereTrabalhadores = idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores) " : "";
		String whereDeptos = idsDeptos != null && !idsDeptos.isEmpty() ? " AND abb11id IN (:idsDeptos) " : "";
		String ordem = ordenacao == 0 ? " ORDER BY abh80codigo, fbc0101dtPagto " : ordenacao == 1 ? " ORDER BY abh80nome, fbc0101dtPagto " : " ORDER BY abb11codigo, abh80codigo, fbc0101dtPagto ";
		String whereData = dataPgto != null ? getWhereDataInterval("AND", dataPgto, "fbc0101dtPagto") : "";
		
		String sql = "SELECT abh80codigo, abh80nome, abb11codigo, abb11nome, fbc0101dtPagto, fbc0101pgi, fbc0101pgf, fbc0101pbi, fbc0101pbf, fba01011tipo, fba01011valor " +
				     "FROM Fba01011 " +
				     "INNER JOIN Fba0101 ON fba0101id = fba01011vlr " +
				     "INNER JOIN Abh21 ON abh21id = fba01011eve " +
				     "LEFT JOIN Fbc0101 ON fbc0101id = fba0101fer " +
					 "INNER JOIN Abh80 ON abh80id = fba0101trab " +
					 "INNER JOIN Abb11 ON abb11id = abh80depto " +
					 "INNER JOIN Fba01 ON fba01id = fba0101calculo " +
				     "WHERE (abh21codigo = :codEveFerias OR abh21codigo = :codEveAbono) " +
				     whereData + whereTrabalhadores + whereDeptos + getSamWhere().getWherePadrao("AND", Fba01.class) +
				     ordem;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("codEveFerias", codEveFerias);
		query.setParameter("codEveAbono", codEveAbono);
		if(idsTrabalhadores != null && !idsTrabalhadores.isEmpty()) query.setParameter("idsTrabalhadores", idsTrabalhadores);
		if(idsDeptos != null && !idsDeptos.isEmpty()) query.setParameter("idsDeptos", idsDeptos);
		
		return query.getListTableMap();
	}

}
//meta-sis-eyJkZXNjciI6IlNGUCAtIFJlbGHDp8OjbyBkZSBGw6lyaWFzL0Fib25vcyBQYWdvcyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==