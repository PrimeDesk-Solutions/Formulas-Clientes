package multitec.relatorios.sgp;

import java.time.LocalDate

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

/**
 * Classe para Relatorio SGP - Status do Projeto
 * @author Samuel André
 * @since 30/08/2019
 * @version 1.0
 *
 */

public class SGP_StatusDoProjeto extends RelatorioBase {
	/**
	 * Método principal
	 * @return String - Nome do Relatório
	 */
	@Override
	public String getNomeTarefa() {
		return "SGP - Status do Projeto";
	}
	
	/**
	 * Método Criar valores iniciais
	 * @return Map - Filtros do Front-end
	 */
	@Override
	public Map<String, Object> criarValoresIniciais(){
		def filtrosDefault = new HashMap<String, Object>()
		def datas = DateUtils.getStartAndEndMonth(MDate.date())
		filtrosDefault.put("ordenacao", "0")
		filtrosDefault.put("periodo", datas)
		return Utils.map("filtros", filtrosDefault)
	}
	
	/**
	 * Método Principal
	 * @return gerarPDF - Dados em PDF
	 */
	@Override
	public DadosParaDownload executar() {
		def idsProjetos = getListLong("projetos")
		def idsEntidades = getListLong("entidades")
		def ordenacao = getInteger("ordenacao")
		def periodo = getIntervaloDatas("periodo")
		def nomeRelatorio = "SGP_StatusDoProjeto"
		
		def aac10 = obterEmpresaAtiva()
		
		// Monta a String com o endereço da empresa
		def endereco = null;
		if(aac10.getAac10endereco() != null) {
			if(aac10.getAac10numero() != null) {
				endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
			}else {
				endereco = aac10.getAac10endereco();
			}
			if(aac10.getAac10bairro() != null) {
				endereco += " - " + aac10.getAac10bairro();
			}
			if(aac10.getAac10complem() != null) {
				endereco += " - " + aac10.getAac10complem();
			}
		}
		
		// Monta a String com o telefone da empresa
		def fone = null;
		if(aac10.getAac10fone() != null) {
			if(aac10.getAac10dddFone() != null) {
				fone = "(" + aac10.getAac10dddFone() + ") " + aac10.getAac10fone();
			}else {
				fone = aac10.getAac10fone();
			}
		}
		
		// Seta os parametros principais
		adicionarParametro("TITULO_RELATORIO", "RELATÓRIO DE STATUS DO PROJETO");
		adicionarParametro("NOME_EMP", aac10.getAac10na());
		adicionarParametro("ENDERECO_EMP", endereco);
		adicionarParametro("FONE_EMP", fone);
		
		def statusDoProjeto = buscarDadosRelatorio(idsProjetos, idsEntidades, periodo, ordenacao);
		
		return gerarPDF(nomeRelatorio, statusDoProjeto);
	}
	
	/**
	 * Método para buscar os dados no banco
	 * @param idsProjetos
	 * @param idsEntidades
	 * @param periodo
	 * @param ordenacao
	 * @return TableMapDataSource - Dados para o Relatorio
	 */
	private TableMapDataSource buscarDadosRelatorio(idsProjetos, idsEntidades, periodo, ordenacao){
		
		// Inicia os Mapas
		def listProjetos = buscarDadosDoProjetoPorPeriodo(idsProjetos, idsEntidades, periodo, ordenacao);
		def mapProjetos = new ArrayList<TableMap>();
		
		//Prepara o mapa com os dados do projeto
		def linha = 0;
		if(listProjetos != null && listProjetos.size() > 0) {
			for(int i = 0; i < listProjetos.size(); i++) {
				
				def bgb01id = listProjetos.get(i).getLong("bgb01id");
				def key = linha + "/" + bgb01id;
				
				// Verifica se o Projeto está dentro do prazo
				def data = MDate.date().toString()
				def prevFim = listProjetos.get(i).getString("prevfim");
				def compData = prevFim.compareTo(data);
				def prazo = compData < 0 ? "Atrasado" : "Dentro do Prazo";
				
				// Busca a conclusão do projeto em porcentagem
				def totalFase = buscarQtdFasePorSituacao(bgb01id, [0,1,2]);
				def fasesConc = buscarQtdFasePorSituacao(bgb01id, [2]);
				def porctConc = (fasesConc / totalFase) * 100; 
				
				// Busca os custos do projeto
				def custoPrev = buscarCustosDoProjetoPorCampo("bgb0102111cp", bgb01id);
				def custoReal = buscarCustosDoProjetoPorCampo("bgb0102111cr", bgb01id);
				def desvio    = custoPrev - custoReal;
				
				comporLinhaMapa(mapProjetos, bgb01id, key, prazo, porctConc, custoPrev, custoReal, desvio, listProjetos.get(i).getString("bgb01nome"),	
								listProjetos.get(i).getString("bgb0101resp"), listProjetos.get(i).getString("abe01nome"), 
								listProjetos.get(i).getString("bgb01local"), listProjetos.get(i).getDate("previni"), listProjetos.get(i).getDate("prevfim"), 
								listProjetos.get(i).getDate("realini"), listProjetos.get(i).getDate("realfim"), listProjetos.get(i).getInteger("bgb01sit"), 
								listProjetos.get(i).getString("bgb01descr"), listProjetos.get(i).getString("bgb01implic"), listProjetos.get(i).getString("bgb01anot"), 
								listProjetos.get(i).getInteger("abb01num"));
				linha++;
			}
		}
		
		def mapResponsaveis = new ArrayList<TableMap>();
		if(mapProjetos != null && mapProjetos.size() > 0) {
			for(int i = 0; i < mapProjetos.size(); i++ ) {
				def bgb01id = mapProjetos.get(i).getLong("bgb01id")
				def key = i + "/" + bgb01id;
				
				def listResp = buscarResponsaveisPorProjeto(bgb01id);
				
				for (int j = 0; j < listResp.size(); j++ ) {
					def tmResp = new TableMap();
					tmResp.put("key", key)
					tmResp.put("bga03nome", listResp.get(j).getString("bga03nome"));
					tmResp.put("aae01codigo", listResp.get(j).getString("aae01codigo"));
					tmResp.put("aae01descr", listResp.get(j).getString("aae01descr"));
					tmResp.put("bga03email", listResp.get(j).getString("bga03email"));
					tmResp.put("bga03ddd", listResp.get(j).getString("bga03ddd"));
					tmResp.put("bga03fone", listResp.get(j).getString("bga03fone"));
					
					mapResponsaveis.add(tmResp);
				}
				
				
			}
		}
		
		//Busca as fases de cada Projeto e adiciona no mapa de Fases.
		def mapFasesAtividade = new ArrayList<TableMap>();
		def countItem = 0;
		if(mapProjetos != null && mapProjetos.size() > 0) {
			for(int i = 0; i < mapProjetos.size(); i++) {
				countItem = 0;
	
				def bgb01id = mapProjetos.get(i).getLong("bgb01id");
				def key = i + "/" + bgb01id;
																							
				def listFases = buscarFasesPorProjeto(bgb01id);
	
				if(listFases != null && listFases.size() > 0) {
					for(int j = 0; j < listFases.size(); j++) {
						
						def mapFase = new TableMap();
						
						def bgb0102id = listFases.get(j).getLong("bgb0102id");
						
						// Busca a conclusão da Fase em porcentagem
						def totalAtiv = buscarQtdAtividadePorSituacao(bgb0102id, [0,1,2]);
						def ativsConc = buscarQtdAtividadePorSituacao(bgb0102id, [2]);
						def porctConc = (ativsConc / totalAtiv) * 100;
						
						// Busca os custos da fase
						def custoReal = buscarCustosDaFasePorCampo("bgb0102111cr", bgb0102id);
						def custoPrev = buscarCustosDaFasePorCampo("bgb0102111cp", bgb0102id);
						def custoDesv = custoPrev - custoReal;
						
						// Verifica porcentagem do custo
						def custoProj = mapProjetos.get(i).getBigDecimal("custoReal");
						def porctTot  = custoProj != new BigDecimal(0) ? (custoReal / custoProj) * 100 : new BigDecimal(0);
						
						mapFase.put("count", countItem);
						mapFase.put("key", key);
						mapFase.put("porctConc", porctConc);
						mapFase.put("bgb0102nome", listFases.get(j).getString("bgb0102nome"));
						mapFase.put("bgb0102sit", listFases.get(j).getInteger("bgb0102sit"));
						mapFase.put("bgb0102anot", listFases.get(j).getString("bgb0102anot"));
						mapFase.put("bga03nome", listFases.get(j).getString("bga03nome"));
						mapFase.put("realfim", listFases.get(j).getDate("realfim"));
						mapFase.put("custoReal", custoReal);
						mapFase.put("custoPrev", custoPrev);
						mapFase.put("custoDesv", custoDesv);
						mapFase.put("porctTot", porctTot);
						
						countItem++;
						mapFasesAtividade.add(mapFase);
						
						// Busca as atividades de cada fase
						def listAtividades = buscarAtividadesPorFase(bgb0102id);
						if(listAtividades != null && listAtividades.size() > 0) {
							for(int k = 0; k < listAtividades.size(); k++) {
								def mapAtividade = new TableMap();
								
								def bgb01021id = listAtividades.get(k).getLong("bgb01021id");
								def custoRAtiv = buscarCustoDaAtividadePorCampo(bgb01021id, "bgb0102111cr");
								def custoPAtiv = buscarCustoDaAtividadePorCampo(bgb01021id, "bgb0102111cp");
								def custoDAtiv = custoRAtiv - custoPAtiv;
								
								mapAtividade.put("count", countItem);
								mapAtividade.put("key", key);
								mapAtividade.put("porctConc", porctConc);
								mapAtividade.put("bga01nome", listAtividades.get(k).getString("bga01nome"));
								mapAtividade.put("bgb0102sit", listAtividades.get(k).getInteger("bgb01021sit"));
								mapAtividade.put("bgb0102anot", listAtividades.get(k).getString("bgb01021anot"));
								mapAtividade.put("realfim", listAtividades.get(k).getDate("realfim"));
								mapAtividade.put("bga03nome", listAtividades.get(k).getString("bga03nome"));
								mapAtividade.put("custoReal", custoRAtiv);
								mapAtividade.put("custoPrev", custoPAtiv);
								mapAtividade.put("custoDesv", custoDAtiv);
								mapAtividade.put("porctTot", porctTot);
								
								countItem++;
								mapFasesAtividade.add(mapAtividade);
							}
						}
					}
				}
			}
		}
		
		//Busca as anotações de cada Projeto e adiciona no mapa de Anotações.
		def mapAnotacoes = new ArrayList<TableMap>();
		if(mapProjetos != null && mapProjetos.size() > 0) {
			for(int i = 0; i < mapProjetos.size(); i++) {
				countItem = 0;
	
				def bgb01id = mapProjetos.get(i).getLong("bgb01id");
				def key = i + "/" + bgb01id;
																							
				def listAnotacoes = buscarAnotacoesPorProjeto(bgb01id);
	
				if(listAnotacoes != null && listAnotacoes.size() > 0) {
					for(int j = 0; j < listAnotacoes.size(); j++) {
						
						def mapAnotacao = new TableMap();
																		
						mapAnotacao.put("count", countItem);
						mapAnotacao.put("key", key);
						mapAnotacao.put("bgb0102nome", listAnotacoes.get(j).getString("bgb0102nome"));
						mapAnotacao.put("bgb01021nome", listAnotacoes.get(j).getString("bgb01021nome"));
						mapAnotacao.put("bgc01data", listAnotacoes.get(j).getDate("bgc01data"));
						mapAnotacao.put("bgc01descr", listAnotacoes.get(j).getString("bgc01descr"));
						
						countItem++;
						mapAnotacoes.add(mapAnotacao);
					}
				}
			}
		}
		
		// Cria os sub-relatórios
		def dsPrincipal = new TableMapDataSource(mapProjetos);
		dsPrincipal.addSubDataSource("DsSub1", mapFasesAtividade, "key", "key");
		dsPrincipal.addSubDataSource("DsSub2", mapFasesAtividade, "key", "key");
		dsPrincipal.addSubDataSource("DsSub3", mapAnotacoes, "key", "key");
		dsPrincipal.addSubDataSource("DsSub4", mapResponsaveis, "key", "key");
		
		adicionarParametro("StreamSub1", carregarArquivoRelatorio("SGP_StatusDoProjeto_S1"))
		adicionarParametro("StreamSub2", carregarArquivoRelatorio("SGP_StatusDoProjeto_S2"))
		adicionarParametro("StreamSub3", carregarArquivoRelatorio("SGP_StatusDoProjeto_S3"))
		adicionarParametro("StreamSub4", carregarArquivoRelatorio("SGP_StatusDoProjeto_S4"))
		
		
		return dsPrincipal;
	}
	
	//Compõe as linhas do mapa
	private void comporLinhaMapa(mapa, bgb01id, key, prazo, porctConc, custoPrev, custoReal, desvio, bgb01nome, bgb0101resp, 
						abe01nome, bgb01local, previni, prevfim, realini, realfim, bgb01sit, bgb01descr, bgb01implic, bgb01anot, abb01num) {
								 
		TableMap map = new TableMap();
		
		map.put("bgb01id", bgb01id);
		map.put("key", key);
		map.put("prazo", prazo);
		map.put("porctConc", porctConc);
		map.put("custoPrev", custoPrev);
		map.put("custoReal", custoReal);
		map.put("desvio", desvio);
		map.put("bgb01nome", bgb01nome);
		map.put("bgb0101resp", bgb0101resp);
		map.put("abe01nome", abe01nome);
		map.put("bgb01local", bgb01local);
		map.put("previni", previni);
		map.put("prevfim", prevfim);
		map.put("realini", realini);
		map.put("realfim", realfim);
		map.put("bgb01sit", bgb01sit);
		map.put("bgb01descr", bgb01descr);
		map.put("bgb01implic", bgb01implic);
		map.put("bgb01anot", bgb01anot);
		map.put("abb01numProj", abb01num);
		
		mapa.add(map);
	}
	
	/**
	 * Busca os dados no banco de dados
	 * @param idsProjetos
	 * @param idsEntidades
	 * @param periodo
	 * @param ordenacao
	 * @return List<TableMap> - Dados do banco
	 */
	private List<TableMap> buscarDadosDoProjetoPorPeriodo(idsProjetos, idsEntidades, periodo, ordenacao) {
		def whereProjetos  = idsProjetos != null && !idsProjetos.isEmpty() ? " AND bgb01id IN (:idsProjetos) " : "";
		def whereEntidades = idsEntidades != null && !idsEntidades.isEmpty() ? " AND abe01id IN (:idsEntidades) " : "";
		def whereData = periodo != null ? "WHERE bgb01021prevIni > '"+ periodo[0] +"'" : "";
		def order = ordenacao == 0 ? " ORDER BY abb01num " : ordenacao == 1 ? " ORDER BY bgb01nome " : " ORDER BY abe01nome";
		
		def sql = "SELECT bgb01id, abb01num, bgb01nome, abe01nome, bgb01local, MIN(bgb01021prevIni) AS previni, " +
				  "MAX(bgb01021prevFin) AS prevfim, MIN(bgb01021realIni) AS realini, MAX(bgb01021realFin) AS realfim, bgb01sit, " +
				  "bgb01descr, bgb01implic, bgb01anot FROM bgb01 " +
				  "  INNER JOIN abb01 abb01 on bgb01.bgb01central = abb01.abb01id " +
				  "LEFT JOIN bgb0101 ON bgb0101proj = bgb01id " +
				  "LEFT JOIN abe01 ON abe01id = abb01.abb01ent "+
				  "LEFT JOIN bgb0102 ON bgb0102proj = bgb01id " +
				  "LEFT JOIN bgb01021 ON bgb01021fase = bgb0102id " +
				   whereData + whereProjetos + whereEntidades + obterWherePadrao("bgb01") +
				  " GROUP BY bgb01id, abb01num, bgb01nome, abe01nome" +
				  order;
		
		def param1, param2
		if(idsProjetos != null && !idsProjetos.isEmpty()) 
			param1 = Parametro.criar("idsProjetos", idsProjetos)
		if(idsEntidades != null && !idsEntidades.isEmpty()) 
			param2 = Parametro.criar("idsEntidades", idsEntidades)
			
		return getAcessoAoBanco().buscarListaDeTableMap(sql, param1, param2)
		
	}
	
	// Buscar quantidade de faser por situação
	private BigDecimal buscarQtdFasePorSituacao(bgb01id, situacao) {
		def sql = "SELECT COUNT(Bgb0102) FROM Bgb0102 " +
				  "INNER JOIN Bgb01 ON bgb01id = bgb0102proj " +
				  "WHERE bgb0102sit IN (:situacao) " +
				  "AND bgb01id = :bgb01id";

		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("bgb01id", bgb01id), Parametro.criar("situacao", situacao))
	}
	
	// Buscar custos do projeto por campo
	private BigDecimal buscarCustosDoProjetoPorCampo(campo, bgb01id) {
		def sql = "SELECT COALESCE(SUM("+ campo +"), 0) FROM Bgb0102111 AS bgb0102111 " +
				  "INNER JOIN Bgb010211 AS bgb010211 ON bgb010211id = bgb0102111ia " +
			      "INNER JOIN Bgb01021 AS bgb01021 ON bgb01021id = bgb010211ativ " +
				  "INNER JOIN Bgb0102 AS bgb0102 ON bgb0102id = bgb01021fase " +
				  "INNER JOIN Bgb01 AS bgb01 ON bgb01id = bgb0102proj " +
				  "WHERE  bgb01id = :bgb01id";
					 
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("bgb01id", bgb01id))
	}
	
	// Buscar custos da cada fase por campo
	private BigDecimal buscarCustosDaFasePorCampo(campo, bgb0102id) {
		def sql = "SELECT COALESCE(SUM("+ campo +"), 0) FROM Bgb0102111 AS bgb0102111 " +
				  "INNER JOIN Bgb010211 AS bgb010211 ON bgb010211id = bgb0102111ia " +
				  "INNER JOIN Bgb01021 AS bgb01021 ON bgb01021id = bgb010211ativ " +
				  "INNER JOIN Bgb0102 AS bgb0102 ON bgb0102id = bgb01021fase " +
				  "WHERE  bgb0102id = :bgb0102id";
					 
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("bgb0102id", bgb0102id))
	}
	
	// Buscar custo de cada atividade por campo
	private BigDecimal buscarCustoDaAtividadePorCampo(bgb01021id, campo) {
		def sql = "SELECT SUM("+ campo +") FROM Bgb0102111 " +
				  "INNER JOIN Bgb010211 ON bgb010211id = bgb0102111ia " +
				  "INNER JOIN Bgb01021 ON bgb01021id = bgb010211ativ " +
				  "WHERE bgb01021id = :bgb01021id";
		
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("bgb01021id", bgb01021id));
	}
	
	// Buscar fases do projeto
	private List<TableMap> buscarFasesPorProjeto(bgb01id) {
		def sql = "SELECT bgb0102id, bgb0102seq, bgb0102nome, bgb0102descr, bgb0102implic, bgb0102sit, bga03nome, " +
				  "bgb0102anot, bgb0102obs, MAX(bgb01021realFin) AS realfim FROM Bgb0102 " +
				  "INNER JOIN Bgb01021 ON bgb01021fase = bgb0102id " +
			      "INNER JOIN Bgb01 ON bgb01id = bgb0102proj " +
				  "LEFT JOIN Bga03 ON bga03id = bgb0102resp " +
				  "WHERE bgb01id = :bgb01id " +
				  "GROUP BY bgb0102id, bga03nome " +
				  "ORDER BY bgb0102seq";
					 
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("bgb01id", bgb01id))
	}
	
	// Busca as atividades por fase
	private List<TableMap> buscarAtividadesPorFase(bgb0102id) {
		def sql = "SELECT bgb01021id, bga01nome, bgb01021prevIni, bgb01021prevFin, bga03nome, " + 
				  "bgb01021realIni, bgb01021realFin AS realfim FROM Bgb01021 " +
				  "INNER JOIN Bga01 ON bgb01021ativ = bga01id " +
				  "LEFT JOIN Bga03 ON bga03id = bgb01021resp " +
				  "WHERE bgb01021fase = :bgb0102id " +
				  "ORDER BY bgb01021id";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("bgb0102id", bgb0102id));
	}
	
	// Buscar anotações do projeto
	private List<TableMap> buscarAnotacoesPorProjeto(bgb01id) {
		def sql = "SELECT bgb0102nome, bgb01021nome, bgc01data, bgc01descr FROM bgb01021 " +
				  "INNER JOIN bgc01 ON bgc01cap = bgb01021cap " +
				  "INNER JOIN bgb0102 ON bgb0102id = bgb01021fase " +
				  "INNER JOIN bgb01 ON bgb01id = bgb0102proj " +
				  "WHERE bgb01id = :bgb01id " +
				  "GROUP BY bgb0102nome, bgb01021nome, bgc01data, bgc01descr " +
				  "ORDER BY bgc01data";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("bgb01id", bgb01id));
	}
	
	// Buscar quantidade de atividades de cada fase por situação
	private Integer buscarQtdAtividadePorSituacao(bgb0102id, situacao) {
		def sql = "SELECT COUNT(Bgb01021) FROM Bgb01021 " +
				  "INNER JOIN Bgb0102 ON bgb0102id = bgb01021fase " +
				  "WHERE bgb01021sit IN (:situacao) " +
				  "AND bgb0102id = :bgb0102id";
				  
		return getAcessoAoBanco().obterInteger(sql, Parametro.criar("situacao", situacao), Parametro.criar("bgb0102id", bgb0102id));
	}
	// buscar responsaveis  do projeto
	private List<TableMap> buscarResponsaveisPorProjeto(bgb01id) {
		def sql =   "SELECT bga03nome, aae01codigo, aae01descr, bga03email, bga03ddd, bga03fone FROM bga03 " +
					"INNER JOIN aae01 ON aae01id = bga03clas " +
					"INNER JOIN bgb0101 ON bgb0101resp = bga03id " +
					"INNER JOIN bgb01 ON bgb01id = bgb0101proj " +
					"WHERE bgb01id = :bgb01id";
					
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("bgb01id", bgb01id));
	}
}
//meta-sis-eyJkZXNjciI6IlNHUCAtIFN0YXR1cyBkbyBQcm9qZXRvIiwidGlwbyI6InJlbGF0b3JpbyJ9