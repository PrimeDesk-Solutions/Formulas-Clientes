package multitec.relatorios.sap;

import java.time.LocalDate

import br.com.multiorm.Query
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.aa.Aac10
import sam.model.entities.ec.Ecc01
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

/**
 * Classe para Relatório - Ficha do CIAP
 * @author Samuel
 * @version 1.0
 *
 */
public class SAP_FichaDoCIAP extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SAP - Ficha do CIAP";
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		filtrosDefault.put("numIni", "00000");
		filtrosDefault.put("numFin", "99999");
		filtrosDefault.put("modelo", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Long numIni = getLong("numIni");
		Long numFin = getLong("numFin");
		Integer modelo = getInteger("modelo");
		Aac10 aac10 = getVariaveis().getAac10();
		
		params.put("TITULO_RELATORIO", "Ficha do CIAP");
		params.put("EMPRESA", aac10.getAac10na());
		params.put("EMPRESA_IE", aac10.getAac10ni());
		String nomeRelatorio = modelo == 0 ? "SAP_FichaDoCIAP_R1" : "SAP_FichaDoCIAP_R2";
		
		TableMapDataSource fichaDoCiap = buscarDadosRelatorio(numIni, numFin, modelo);
		
		return gerarPDF(nomeRelatorio, fichaDoCiap);
	}
	
	/**
	 * Método para buscar os dados do relatório
	 * @param numIni
	 * @param numFin
	 * @param modelo
	 * @return TableMapDataSource - Dados do CIAP
	 */
	public TableMapDataSource buscarDadosRelatorio(Long numIni, Long numFin, Integer modelo) {
		
		// Busca os dados Principais da Ficha do CIAP e compoe as linhas dos MAPAS
		List<TableMap> listFichas = buscarFichasCiapPorNumEModelo(numIni, numFin, modelo);
		List<TableMap> mapFichas = new ArrayList<TableMap>();
		
		int linha = 0;
		if(listFichas != null && listFichas.size() > 0) {
			for(int i = 0; i < listFichas.size(); i++) {
				
				Long ecc01id = listFichas.get(i).getLong("ecc01id");
				String key = linha + "/" + ecc01id;
				
				comporLinhaMapa(mapFichas, key, ecc01id, listFichas.get(i).getInteger("ecc01modelo"), listFichas.get(i).getInteger("ecc01num"), listFichas.get(i).getInteger("ecc01livro"), 
								listFichas.get(i).getInteger("ecc01pag"), listFichas.get(i).getDate("ecc01data"), listFichas.get(i).getBigDecimal_Zero("ecc01icms"), listFichas.get(i).getString("ecc01evento"), 
								listFichas.get(i).getDate("ecc01dtEvento"), listFichas.get(i).getBigDecimal_Zero("ecc01fator1"), listFichas.get(i).getBigDecimal_Zero("ecc01fator2"), listFichas.get(i).getBigDecimal_Zero("ecc01fator3"), 
								listFichas.get(i).getBigDecimal_Zero("ecc01fator4"), listFichas.get(i).getBigDecimal_Zero("ecc01fator5"), listFichas.get(i).getBigDecimal_Zero("ecc01valor1"), listFichas.get(i).getBigDecimal_Zero("ecc01valor2"), 
								listFichas.get(i).getBigDecimal_Zero("ecc01valor3"), listFichas.get(i).getBigDecimal_Zero("ecc01valor4"), listFichas.get(i).getBigDecimal_Zero("ecc01valor5"), listFichas.get(i).getString("ecc01obs"), 
								listFichas.get(i).getString("abb20nome"), listFichas.get(i).getString("abe01na"), listFichas.get(i).getLong("abb20central"), listFichas.get(i).getLong("abb20centralBx"), 
								listFichas.get(i).getDate("abb20baixa"), listFichas.get(i).getString("aah01nome"));
				linha++;
		
			}
		}
		
		// Busca os dados do Sub-relatório e compoe o mapa
		List<TableMap> mapValores = new ArrayList<TableMap>();
		int countItem = 0;
		if(mapFichas != null && mapFichas.size() > 0) {
			for(int i = 0; i < mapFichas.size(); i++) {
				countItem = 0;

				Long ecc01id = mapFichas.get(i).getLong("ecc01id");
				String key = i + "/" + ecc01id;
				
				List<TableMap> listValores = buscarValoresPorIdCiap(ecc01id);
				
				if(listValores != null && listValores.size() > 0) {
					TableMap mapValor = new TableMap();
					for(int j = 0; j < listValores.size(); j++) {
						mapValor.put("count", countItem);
						mapValor.put("key", key);
						
						mapValor.put("valor"+ (j+1), listValores.get(j).getBigDecimal_Zero("ecc0101icms"));
						
						mapValor.put("fator"+ (j+1), listValores.get(j).getBigDecimal_Zero("ecc0101fator"));
						
						mapValor.put("mesAno"+ (j+1), String.format("%02d", listValores.get(j).getInteger("ecc0101mes")) + "/" + listValores.get(j).getInteger("ecc0101ano"));
						
						countItem++;
					}
					mapValores.add(mapValor)
				}
		
			}
		}
		
		if(mapFichas == null || mapFichas.size() == 0) throw new ValidacaoException("Não foram encontrados registros para exibição.");
		
		// Cria o relatório principal e adiciona os sub-relatórios
		TableMapDataSource dsPrincipal = new TableMapDataSource(mapFichas);
		if(modelo == 0) {
			dsPrincipal.addSubDataSource("DsSub1", mapValores, "key", "key");
			params.put("StreamSub1", carregarArquivoRelatorio("SAP_FichaDoCIAP_R1_S1"));
		}else if(modelo == 1) {
			dsPrincipal.addSubDataSource("DsSub1", mapValores, "key", "key");
			params.put("StreamSub1", carregarArquivoRelatorio("SAP_FichaDoCIAP_R2_S1"));
		}else {
			dsPrincipal.addSubDataSource("DsSub1", mapValores, "key", "key");
			params.put("StreamSub1", carregarArquivoRelatorio("SAP_FichaDoCIAP_R2_S2"));
		}
		
		return dsPrincipal;
	}
	
	/**
	 * Métodos diversos
	 */
	private void comporLinhaMapa(List<TableMap> mapa, String key, Long ecc01id, Integer ecc01modelo, Integer ecc01num, Integer ecc01livro, Integer ecc01pag, LocalDate ecc01data, BigDecimal ecc01icms, String ecc01evento, LocalDate ecc01dtEvento, BigDecimal ecc01fator1, BigDecimal ecc01fator2, BigDecimal ecc01fator3, BigDecimal ecc01fator4, BigDecimal ecc01fator5, BigDecimal ecc01valor1, BigDecimal ecc01valor2, BigDecimal ecc01valor3, BigDecimal ecc01valor4, BigDecimal ecc01valor5, String ecc01obs, String abb20nome, String abe01na, Long abb20central, Long abb20centralBx, LocalDate abb20baixa, String aah01nome) {
		TableMap map = new TableMap();
		
		map.put("key", key);
		map.put("ecc01id", ecc01id);
		map.put("ecc01modelo", ecc01modelo);
		map.put("ecc01num", ecc01num);
		map.put("ecc01livro", ecc01livro);
		map.put("ecc01pag", ecc01pag);
		map.put("ecc01data", ecc01data);
		map.put("ecc01icms", ecc01icms);
		map.put("ecc01evento", ecc01evento);
		map.put("ecc01dtEvento", ecc01dtEvento);
		map.put("ecc01fator1", ecc01fator1);
		map.put("ecc01fator2", ecc01fator2);
		map.put("ecc01fator3", ecc01fator3);
		map.put("ecc01fator4", ecc01fator4);
		map.put("ecc01fator5", ecc01fator5);
		map.put("ecc01valor1", ecc01valor1);
		map.put("ecc01valor2", ecc01valor2);
		map.put("ecc01valor3", ecc01valor3);
		map.put("ecc01valor4", ecc01valor4);
		map.put("ecc01valor5", ecc01valor5);
		map.put("ecc01obs", ecc01obs);
		map.put("abb20nome", abb20nome);
		map.put("abe01na", abe01na);
		map.put("abb20central", abb20central);
		map.put("abb20centralBx", abb20centralBx);
		map.put("abb20baixa", abb20baixa);
		map.put("aah01nome", aah01nome);
		
		mapa.add(map);
	}

	/**
	 * Métodos Diversos - Busca as fichas do CIAP pelo numero e modelo
	 * @param numIni
	 * @param numFin
	 * @param modelo
	 * @return List<TableMap> - Dados do Banco
	 */
	public List<TableMap> buscarFichasCiapPorNumEModelo(Long numIni, Long numFin, Integer modelo){
		String sql = "SELECT ecc01id, ecc01modelo, ecc01num, ecc01livro, ecc01pag, ecc01data, ecc01icms, ecc01evento, ecc01dtEvento, ecc01fator1, ecc01valor1, ecc01fator2, ecc01valor2, ecc01fator3, " +
					 "ecc01valor3, ecc01fator4, ecc01valor4, ecc01fator5, ecc01valor5, ecc01obs, abb20nome, abe01na, abb20central, abb20centralBx, abb20baixa, aah01nome " + 
					 "FROM Ecc01 " +
					 "INNER JOIN Abb20 ON abb20id = ecc01bem " + 
					 "LEFT JOIN Abb01 ON abb01id = abb20centralBx " +
					 "LEFT JOIN Abe01 ON abe01id = abb01ent " + 
					 "LEFT JOIN Aah01 ON aah01id = abb01tipo " +
					 "WHERE ecc01modelo = :modelo " + 
					 "AND ecc01num BETWEEN :numIni AND :numFin " + 
					 getSamWhere().getWherePadrao("AND", Ecc01.class) +
					 " ORDER BY ecc01num ";
											 
		Query query = getSession().createQuery(sql);
		query.setParameter("modelo", modelo);
		query.setParameter("numIni", numIni);
		query.setParameter("numFin", numFin);
		
		return query.getListTableMap();
		
	}
	
	/**
	 * Métodos Diversos - Buscas os valores das Fichas por ID
	 * @param ecc01id
	 * @return List<TableMap> - Dados do Banco
	 */
	public List<TableMap> buscarValoresPorIdCiap(Long ecc01id) {
		String sql = "SELECT * FROM Ecc0101 INNER JOIN Ecc01 ON ecc01id = ecc0101ficha " +
					 "WHERE ecc01id = :ecc01id " +
					 getSamWhere().getWherePadrao("AND", Ecc01.class) + 
					 " ORDER BY ecc01num, ecc0101ano, ecc0101mes";
						 
		Query query = getSession().createQuery(sql);
		query.setParameter("ecc01id", ecc01id);
		return query.getListTableMap();
	}


}
//meta-sis-eyJkZXNjciI6IlNBUCAtIEZpY2hhIGRvIENJQVAiLCJ0aXBvIjoicmVsYXRvcmlvIn0=