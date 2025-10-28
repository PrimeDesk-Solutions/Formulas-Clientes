package multitec.relatorios.smr;

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.model.entities.ab.Abh80
import sam.model.entities.ab.Abm01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource
/**
 * Classe para gerar relatorio de Caixas de Ferramentas por Trabalhador
 * @author Samuel André
 * @version 1.0
 * @since 2019
 *
 */
public class SMR_CaixaDeFerramentas extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SMR - Caixa de Ferramentas";
	}

	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate[] dtEnvio = DateUtils.getStartAndEndMonth(MDate.date());
		filtrosDefault.put("dtEntrega", dtEnvio);
		return Utils.map("filtros", filtrosDefault);
	}
	
	@Override
	public DadosParaDownload executar() {
		List<Long> idsTrabalhadores = getListLong("trabalhadores");
		List<Long> idsItens = getListLong("itens");
		LocalDate[] datas = getIntervaloDatas("dtEntrega");
		List<Integer> tiposItem = getListInteger("mpms");
		
		params.put("TITULO_RELATORIO", "Caixa De Ferramentas");
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		TableMapDataSource caixaDeFerramentas = buscarDadosRelatorio(idsTrabalhadores, idsItens, datas, tiposItem);		
	     
	   String nomeRelatorio = "SMR_CaixaDeFerramentas_R1";
				
		return gerarPDF(nomeRelatorio, caixaDeFerramentas);
	}
	
	/**
	 * Método para buscar os dados do Relatório para composição
	 * @param idsTrabalhadores
	 * @param idsItens
	 * @param datas
	 * @param tiposItem
	 * @return TableMapDataSouce com os dados obtidos
	 */
	public TableMapDataSource buscarDadosRelatorio(List<Long> idsTrabalhadores, List<Long> idsItens, LocalDate[] datas, List<Integer> tiposItem) {
		// Busca as Caixas de Ferramentas por Trabalhadores		
		List<TableMap> listCaixas = buscarCaixasPorTrabalhadores(idsTrabalhadores);
		List<TableMap> mapCaixas =  new ArrayList<TableMap>();
		if(listCaixas != null && listCaixas.size() > 0) {
			for(int i = 0; i < listCaixas.size(); i++) {
				
				Long bed01trab = listCaixas.get(i).getLong("bed01trab");
				String key = i + "/" + bed01trab;
				
				// Cria o mapa de caixas
				comporLinhaMapa(mapCaixas, bed01trab, key, listCaixas.get(i).getString("abh80codigo"), listCaixas.get(i).getString("abh80nome"), listCaixas.get(i).getLong("bed01id"), listCaixas.get(i).getString("bed01obs"));
			}
		}
		
		// Busca os itens atuais na caixa de cada trabalhador
		List<TableMap> mapItensNaCaixa = new ArrayList<TableMap>();
		if(mapCaixas != null && mapCaixas.size() > 0) {
			for(int i = 0; i < mapCaixas.size(); i++) {
				
				Long caixa = mapCaixas.get(i).getLong("bed01id");
				Long bed01trab = listCaixas.get(i).getLong("bed01trab");
				String key = i + "/" + bed01trab;
				
				List<TableMap> listItensNaCaixa = buscarItensNaCaixa(caixa, idsItens, tiposItem)
	
				if(listItensNaCaixa != null && listItensNaCaixa.size() > 0) {
					for(int j = 0; j < listItensNaCaixa.size(); j++) {
						
						TableMap map = new TableMap();
						
						// Verifica a quantidade de itens que entraram e sairam
						TableMap itemEntrada = buscarTotalPorTipoMovimento(listItensNaCaixa.get(j).getLong("abm01id"), caixa, 0);
						TableMap itemSaida = buscarTotalPorTipoMovimento(listItensNaCaixa.get(j).getLong("abm01id"), caixa, 1);
						
						BigDecimal totEntr = itemEntrada != null ? itemEntrada.getBigDecimal("qtd") : 0;
						BigDecimal totSaid = itemSaida != null ? itemSaida.getBigDecimal("qtd") : 0; 
						
						// Calcula a diferença entre entrada e saida, caso a quantidade for maior que 0 mostra o item
						BigDecimal total = totEntr - totSaid;
						if(total > 0) {
							map.put("key", key);
							map.put("bed0101qt", total);
							map.put("abm01codigo", listItensNaCaixa.get(j).getString("abm01codigo"));
							map.put("abm01na", listItensNaCaixa.get(j).getString("abm01na"));
							mapItensNaCaixa.add(map);
						}
					}
				}
			}
		}
		
		// Busca os lançamentos na caixa de cada trabalhador
		List<TableMap> mapLancamentos = new ArrayList<TableMap>();
		int countItem = 0;
		if(mapCaixas != null && mapCaixas.size() > 0) {
			for(int i = 0; i < mapCaixas.size(); i++) {
				countItem = 0;
	
				Long bed01trab = mapCaixas.get(i).getLong("bed01trab");
				String key = i + "/" + bed01trab;
																							
				List<TableMap> listLancamentos = buscarLancamentosPorTrabalhador(bed01trab, idsItens, datas, tiposItem)
	
				if(listLancamentos != null && listLancamentos.size() > 0) {
					for(int j = 0; j < listLancamentos.size(); j++) {
						
						TableMap mapLancamento = new TableMap();
						mapLancamento.put("count", countItem);
						mapLancamento.put("key", key);
						mapLancamento.put("bed0101data", listLancamentos.get(j).getDate("bed0101data"));
						mapLancamento.put("abm01tipo", listLancamentos.get(j).getString("abm01tipo"));
						mapLancamento.put("abm01codigo", listLancamentos.get(j).getString("abm01codigo"));
						mapLancamento.put("abm01na", listLancamentos.get(j).getString("abm01na"));
						mapLancamento.put("abm01umu", listLancamentos.get(j).getString("abm01umu"));
						mapLancamento.put("bed0101qt", listLancamentos.get(j).getBigDecimal("bed0101qt"));
						mapLancamento.put("bed0101mov", listLancamentos.get(j).getInteger("bed0101mov") == 0 ? "0-Entrada" : "1-Saída");
						mapLancamento.put("bed0101hist", listLancamentos.get(j).getString("bed0101hist"));
						
						countItem++;
						mapLancamentos.add(mapLancamento);
					}
				}
			}
		}
		
		// Cria o DataSource com os respectivos Sub-DataSources
		TableMapDataSource dsPrincipal = new TableMapDataSource(mapCaixas);
		dsPrincipal.addSubDataSource("DsSub1R1", mapItensNaCaixa, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2R1", mapLancamentos, "key", "key");
		params.put("StreamSub1R1", carregarArquivoRelatorio("SMR_CaixaDeFerramentas_R1_S1"));
		params.put("StreamSub2R1", carregarArquivoRelatorio("SMR_CaixaDeFerramentas_R1_S2"));
		
		return dsPrincipal;
	}
	
	/**
	 * Método para buscar as caixas de ferramentas de cada trabalhador através do Id
	 * @param idsTrabalhadores
	 * @return List<TableMap> com as caixas de ferramentas obtidas
	 */
	public List<TableMap> buscarCaixasPorTrabalhadores(List<Long> idsTrabalhadores) {
		String whereAbh80s =  idsTrabalhadores != null && !idsTrabalhadores.isEmpty() ? " AND abh80id IN (:idsTrabalhadores)" : "";
		String sql = "SELECT abh80codigo, abh80nome, bed01id, bed01trab, bed01obs " +
					 "FROM Bed01 " +
					 "INNER JOIN Abh80 ON abh80id = bed01trab " +
					 getSamWhere().getWherePadrao("WHERE", Abh80.class) + whereAbh80s;
		
		Query query = getSession().createQuery(sql);
		query.setParameter("idsTrabalhadores", idsTrabalhadores)
		return  query.getListTableMap();
	}
	
	/**
	 * Método para buscar os lançamentos feitos nas caixas de ferramentas de cada trabalhador
	 * @param bed01trab
	 * @param idsItens
	 * @param datas
	 * @param tiposItem
	 * @return List<TableMap> com os lançamentos obtidos
	 */
	public List<TableMap> buscarLancamentosPorTrabalhador(Long bed01trab, List<Long> idsItens, LocalDate[] datas, List<Integer> tiposItem) {
		String whereData   = datas != null ? getWhereDataInterval("AND", datas, "bed0101data") : "";
		String whereAbm01s = idsItens != null && !idsItens.isEmpty() ? " AND (abm01id IN (:idsItens)) " : "";
		String whereTipos  = tiposItem != null && !tiposItem.isEmpty() ? " AND (abm01tipo IN (:tiposItem)) " : "";
		
		String sql = "SELECT bed0101data, abm01tipo, abm01codigo, abm01na, abm01umu, bed0101qt, bed0101mov, bed0101hist " +
					 "FROM Bed0101 " +
					 "INNER JOIN Bed01 ON bed01id = bed0101caixa " +
					 "INNER JOIN Abm01 ON abm01id = bed0101item " +
					 "INNER JOIN abh80 ON abh80id = bed01trab " +
					 "WHERE bed01trab = :bed01trab " +
					 whereData + whereAbm01s + whereTipos +
					 getSamWhere().getWherePadrao("AND", Abh80.class);
		
		Query query = getSession().createQuery(sql);
		
		if(bed01trab != null) query.setParameter("bed01trab", bed01trab);
		if(idsItens != null && !idsItens.isEmpty()) query.setParameter("idsItens", idsItens);
		if(tiposItem != null && !tiposItem.isEmpty()) query.setParameter("tiposItem", tiposItem);
		
		return  query.getListTableMap();
		
	}
	
	/**
	 * Método para buscar os itens encontrados em cada caixa de ferramenta, independente se tiver saldo ou não.
	 * Será retornado somente um de cada item
	 * @param caixa
	 * @param idsItens
	 * @param tiposItem
	 * @return List<TableMap> com os itens obtidos
	 */
	public List<TableMap> buscarItensNaCaixa(Long caixa, List<Long> idsItens, List<Integer> tiposItem) {
		String whereAbm01s = idsItens != null && !idsItens.isEmpty() ? " AND (abm01id IN (:idsItens)) " : "";
		String whereTipos  = tiposItem != null && !tiposItem.isEmpty() ? " AND (abm01tipo IN (:tiposItem)) " : "";
		
		String sql = "SELECT DISTINCT abm01id, abm01codigo, abm01na FROM Bed0101 " +
					 "INNER JOIN Abm01 ON abm01id = bed0101item " +
					 "INNER JOIN Bed01 ON bed01id = bed0101caixa " +
					 "INNER JOIN abh80 ON abh80id = bed01trab " +
					 "WHERE bed0101caixa = :caixa " +
					 whereAbm01s + whereTipos +
					getSamWhere().getWherePadrao("AND", Abm01.class);
		
		Query query = getSession().createQuery(sql);
		
		if(caixa != null) query.setParameter("caixa", caixa);
		if(idsItens != null && !idsItens.isEmpty()) query.setParameter("idsItens", idsItens);
		if(tiposItem != null && !tiposItem.isEmpty()) query.setParameter("tiposItem", tiposItem);
		
		return  query.getListTableMap();
	}
	
	/**
	 * Método para somar a quantidade de item recebendo como parametro o tipo de moventação
	 * 0-Entrada ou 1-Saída
	 * @param abm01id
	 * @param caixa
	 * @param tipoMov
	 * @return TableMap com a quantidade de itens obtidos
	 */
	public TableMap buscarTotalPorTipoMovimento(Long abm01id, Long caixa, Integer tipoMov) {
		String sql = "SELECT abm01codigo, abm01na, SUM(bed0101qt) AS qtd FROM Bed0101 " +
					 "INNER JOIN Abm01 ON abm01id = bed0101item " +
					 "WHERE bed0101caixa = :caixa " +
					 "AND bed0101item = :abm01id " +
					 "AND bed0101mov = :tipoMov " +
					 "GROUP BY abm01codigo, abm01na";
		
		Query query = getSession().createQuery(sql);
		
		if(abm01id != null) query.setParameter("abm01id", abm01id);
		if(caixa != null) query.setParameter("caixa", caixa);
		if(tipoMov != null) query.setParameter("tipoMov", tipoMov);
		
		return  query.getUniqueTableMap();
	}
	
	/**
	 * Métodos diversos
	 * @param mapCaixas
	 * @param bed01trab
	 * @param key
	 * @param abh80codigo
	 * @param abh80nome
	 * @param bed01id
	 * @param bed01obs
	 */
	public void comporLinhaMapa(List<TableMap> mapCaixas, Long bed01trab, String key, String abh80codigo, String abh80nome, Long bed01id, String bed01obs) {
		TableMap map = new TableMap();
		
		map.put("bed01trab", bed01trab);
		map.put("key", key);
		map.put("abh80codigo", abh80codigo);
		map.put("abh80nome", abh80nome);
		map.put("bed01id", bed01id);
		map.put("bed01obs", bed01obs);
		
		mapCaixas.add(map);
	}
}
//meta-sis-eyJkZXNjciI6IlNNUiAtIENhaXhhIGRlIEZlcnJhbWVudGFzIiwidGlwbyI6InJlbGF0b3JpbyJ9