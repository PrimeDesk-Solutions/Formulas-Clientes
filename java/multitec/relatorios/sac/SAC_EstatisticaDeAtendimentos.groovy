package multitec.relatorios.sac;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.apache.commons.collections4.map.HashedMap

import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abm01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource
/**
 * Classe para Relatorio SAC - Estatistica de Atendimentos
 * @author Samuel André
 * @version 1.0
 * @copyright Multitec Sistemas
 *
 */
public class SAC_EstatisticaDeAtendimentos extends RelatorioBase{
	@Override
	public String getNomeTarefa() {
		return "SAC - Estatística de Atendimentos";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashedMap<String, Object>();
		LocalDate[] periodo = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("periodo", periodo);
		filtrosDefault.put("listar", '0');		
		return Utils.map("filtros", filtrosDefault);
	}
	
	@Override
	public DadosParaDownload executar() {
		LocalDate[] periodo = getIntervaloDatas("periodo");
		Integer listarPor = getInteger("listar");
		List<Integer> tipoItem = getListInteger("mpms");
		List<Long> itens = getListLong("itens");
		List<Long> reclam = getListLong("reclamacoes");
		String nomeRelatorio = null;
		Aac10 aac10 = getVariaveis().getAac10();
		
		
		if(listarPor == 0) {
			nomeRelatorio = "SAC_EstatisticaDeAtendimentos_R1"; // Listar por Itens
		}else if(listarPor == 1){
			nomeRelatorio = "SAC_EstatisticaDeAtendimentos_R2"; // Listar por Reclamações
		}else {
			nomeRelatorio = "SAC_EstatisticaDeAtendimentos_R3"; // Listar por Itens e Reclamações
		}
		
		// Seta os parametros principais
		params.put("TITULO_RELATORIO", "Estatística de Atendimentos");
		params.put("EMPRESA", aac10.getAac10na());
		params.put("PERIODO", "Período: " + periodo[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() + " à " + periodo[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
		
		TableMapDataSource dadosRelatorio = buscarDadosRelatorio(periodo, listarPor, tipoItem, itens, reclam);
		
		return gerarPDF(nomeRelatorio, dadosRelatorio);
	}
	
	/**
	 * Método para buscar os dados que compoe o relatorio
	 * @param periodo
	 * @param listarPor
	 * @param tipoItem
	 * @param itens
	 * @param reclam
	 * @return TableMapDataSource com o dados do relatorio
	 */
	public TableMapDataSource buscarDadosRelatorio(LocalDate[] periodo, Integer listarPor, List<Integer> tipoItem, List<Long> itens, List<Long> reclam) {
		return listarPor == 2 ? buscarDadosPorItemEReclamacao(periodo, listarPor, itens, tipoItem, reclam) : buscarDadosPorItemOuReclamacao(periodo, listarPor, itens, tipoItem, reclam);
	}
	
	/**
	 * Método para buscar os dados por item e reclamação
	 * @param periodo
	 * @param listarPor
	 * @param itens
	 * @param tipoItem
	 * @param reclam
	 * @return TableMapDataSource com os dados
	 */
	public TableMapDataSource buscarDadosPorItemEReclamacao(LocalDate[] periodo, Integer listarPor, List<Long> itens, List<Integer> tipoItem, List<Long> reclam) {
		List<TableMap> listAtendimentos = buscarAtendimentosPorItemEReclamacao(periodo, listarPor, itens, tipoItem, reclam);
		 		
		//Reclamações
		List<TableMap>  mapRec = new ArrayList();
		for(int i = 0; i < listAtendimentos.size(); i++) {
			List<TableMap> listRec = buscarTotalReclamacoesPorItem(periodo, listAtendimentos.get(i).getLong("abm01id"), reclam); 
			if(listRec != null && listRec.size() > 0) {
				for(int j = 0; j < listRec.size(); j++) {
					TableMap map = new TableMap();
					map.put("abm01id", listAtendimentos.get(i).getLong("abm01id"));
					map.put("caa01codigo", listRec.get(j).getString("caa01codigo"));
					map.put("caa01descr", listRec.get(j).getString("caa01descr"));
					map.put("qtd", listRec.get(j).getBigDecimal("qtd"));
					
					mapRec.add(map);
				}
			}
		}
		
		// Cria os sub-relatórios
		TableMapDataSource dsPrincipal = new TableMapDataSource(listAtendimentos);
		dsPrincipal.addSubDataSource("DsSub1", mapRec, "abm01id", "abm01id");
		params.put("StreamSub1", carregarArquivoRelatorio("SAC_EstatisticaDeAtendimentos_R3_S1"));
		
		return dsPrincipal;
	}
	
	/**
	 * Método para buscar os dados por item ou reclamação
	 * @param periodo
	 * @param listarPor
	 * @param itens
	 * @param tipoItem
	 * @param reclam
	 * @return TableMapDataSource com os dados
	 */
	public TableMapDataSource buscarDadosPorItemOuReclamacao(LocalDate[] periodo, Integer listarPor, List<Long> itens, List<Integer> tipoItem, List<Long> reclam) {
		List<TableMap> listAtendimentos = null;
		if(listarPor == 0) {	//0-Item
			listAtendimentos = buscarAtendimentosPorItem(periodo, itens, tipoItem, reclam);
		}else {					//1-Reclamação
			listAtendimentos = buscarAtendimentosPorReclamacao(periodo, itens, tipoItem, reclam);
		}
		
		// Cria os sub-relatórios
		TableMapDataSource dsPrincipal = new TableMapDataSource(listAtendimentos);
		
		return dsPrincipal;		
	}
	
	/**
	 * Método para buscar os atendimentos por item e reclamação
	 * @param periodo
	 * @param listarPor
	 * @param itens
	 * @param tipoItem
	 * @param reclam
	 * @return List<TableMap> com os dados obtidos no banco
	 */
	public List<TableMap> buscarAtendimentosPorItemEReclamacao(LocalDate[] periodo, Integer listarPor, List<Long> itens, List<Integer> tipoItem, List<Long> reclam) {
		String whereData   = periodo != null ? getWhereDataInterval("WHERE", periodo, "caa10data") : "";
		String whereItens  = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
		String whereReclam = reclam != null && reclam.size() > 0 ? "AND caa01id IN (:reclam) " : "";
		String whereTpItem = tipoItem != null && tipoItem.size() > 0 ? "AND abm01tipo IN (:tipoItem) " : "";
		
		String sql = " SELECT COUNT(abm01id) as qtd, abm01id, abm01tipo, abm01codigo, abm01na " +
					 " FROM Caa10 " +
					 " LEFT JOIN Caa1001 ON caa10id = caa1001atend " +
					 " LEFT JOIN Caa01 ON caa01id = caa1001rec " +
					 " INNER JOIN Abm01 ON abm01id = caa10item " +
					   whereData + whereItens + whereReclam + whereTpItem + 
					   getSamWhere().getWherePadrao("AND", Abm01.class) +
					 " GROUP BY abm01id, abm01tipo, abm01codigo, abm01na " +
					 " ORDER BY COUNT(*) DESC, abm01tipo, abm01codigo";
					 
		Query query = getSession().createQuery(sql);
		if(itens != null && itens.size() > 0)query.setParameter("itens", itens);
		if(reclam != null && reclam.size() > 0)query.setParameter("reclam", reclam);
		if(tipoItem != null && tipoItem.size() > 0)query.setParameter("tipoItem", tipoItem);
		
		return query.getListTableMap();
	}
	
	/**
	 * Método para buscar o total de reclamações por item
	 * @param periodo
	 * @param abm01id
	 * @param reclam
	 * @return List<TableMap> com os dados obtidos no banco
	 */
	public List<TableMap> buscarTotalReclamacoesPorItem(LocalDate[] periodo, Long abm01id, List<Long> reclam) {
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "caa10data") : "";
		String whereReclam = reclam != null && reclam.size() > 0 ? "AND caa01id IN (:reclam) " : "";
		
		String sql = " SELECT COUNT(caa01id) as qtd, caa01codigo, caa01descr " +
					 " FROM Caa10 " +
					 " INNER JOIN Caa1001 ON caa10id = caa1001atend " +
					 " INNER JOIN Caa01 ON caa01id = caa1001rec " +
					   whereData + whereReclam + "AND caa10item = :abm01id " +
					 " GROUP BY caa01codigo, caa01descr " +
					 " ORDER BY COUNT(*) DESC, caa01codigo";
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("abm01id", abm01id);
		if(reclam != null && reclam.size() > 0)query.setParameter("reclam", reclam);
		return query.getListTableMap();
	}
	
	/**
	 * Método para buscar o total de atendimento por item
	 * @param periodo
	 * @param itens
	 * @param tipoItem
	 * @param reclam
	 * @return List<TableMap> com os dados obtidos no banco
	 */
	public List<TableMap> buscarAtendimentosPorItem(LocalDate[] periodo, List<Long> itens, List<Integer> tipoItem, List<Long> reclam) {
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "caa10data") : "";
		String whereItens  = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
		String whereReclam = reclam != null && reclam.size() > 0 ? "AND caa01id IN (:reclam) " : "";
		String whereTpItem = tipoItem != null && tipoItem.size() > 0 ? "AND abm01tipo IN (:tipoItem) " : "";
		
		String sql = " SELECT COUNT(abm01id) as qtd, abm01tipo, abm01codigo, abm01na " +
					 " FROM Caa10 " +
					 " LEFT JOIN Caa1001 ON caa1001atend = caa10id " +
					 " LEFT JOIN Caa01 ON caa01id = caa1001rec " +
					 " INNER JOIN Abm01 ON abm01id = caa10item " +
					   whereData + whereItens + whereReclam + whereTpItem +
					   getSamWhere().getWherePadrao("AND", Abm01.class) +
					 " GROUP BY abm01tipo, abm01codigo, abm01na " +
					 " ORDER BY COUNT(*) DESC, abm01tipo, abm01codigo";
					 
		Query query = getSession().createQuery(sql);
		if(itens != null && itens.size() > 0)query.setParameter("itens", itens);
		if(reclam != null && reclam.size() > 0)query.setParameter("reclam", reclam);
		if(tipoItem != null && tipoItem.size() > 0)query.setParameter("tipoItem", tipoItem);
		
		return query.getListTableMap();
	}
	
	/**
	 * Método para buscar o total de atendimentos por Reclamação
	 * @param periodo
	 * @param itens
	 * @param tipoItem
	 * @param reclam
	 * @return List<TableMap> com os dados obtidos no banco
	 */
	public List<TableMap> buscarAtendimentosPorReclamacao(LocalDate[] periodo, List<Long> itens, List<Integer> tipoItem, List<Long> reclam) {
		String whereData = periodo != null ? getWhereDataInterval("WHERE", periodo, "caa10data") : "";
		String whereItens  = itens != null && itens.size() > 0 ? "AND abm01id IN (:itens) " : "";
		String whereReclam = reclam != null && reclam.size() > 0 ? "AND caa01id IN (:reclam) " : "";
		String whereTpItem = tipoItem != null && tipoItem.size() > 0 ? "AND abm01tipo IN (:tipoItem) " : "";
		
		String sql = " SELECT COUNT(caa01id) as qtd, caa01codigo, caa01descr " +
					 " FROM Caa10 " +
					 " INNER JOIN Caa1001 ON caa10id = caa1001atend " +
					 " INNER JOIN Caa01 ON caa01id = caa1001rec " +
					 " INNER JOIN Abm01 ON abm01id = caa10item " +
					   whereData + whereItens + whereReclam + whereTpItem +
					   getSamWhere().getWherePadrao("AND", Abm01.class) +
					 " GROUP BY caa01codigo, caa01descr " +
					 " ORDER BY COUNT(*) DESC, caa01codigo";
					 
		Query query = getSession().createQuery(sql);
		if(itens != null && itens.size() > 0)query.setParameter("itens", itens);
		if(reclam != null && reclam.size() > 0)query.setParameter("reclam", reclam);
		if(tipoItem != null && tipoItem.size() > 0)query.setParameter("tipoItem", tipoItem);
		
		return query.getListTableMap();
	}
}
//meta-sis-eyJkZXNjciI6IlNBQyAtIEVzdGF0w61zdGljYSBkZSBBdGVuZGltZW50b3MiLCJ0aXBvIjoicmVsYXRvcmlvIn0=