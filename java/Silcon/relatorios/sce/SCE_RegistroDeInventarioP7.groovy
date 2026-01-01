package Silcon.relatorios.sce;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle;

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abm40
import sam.model.entities.bc.Bcb10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

/**
 * Classe para relatório de Registro de Inventário Modelo P7
 * @author Samuel André
 * @since 12/03/2020
 * @version 1.0
 * @copyright Multitec Sistemas
 *
 */
public class SCE_RegistroDeInventarioP7 extends RelatorioBase {
	
	BigDecimal grau1Total = 0;
	BigDecimal grau2Total = 0;
	BigDecimal grupoTotal = 0;
	
	String grau1Ant = null;
	String grau2Ant = null;
	String grupoAnt = null;
	
	List<TableMap> map2 = new ArrayList<TableMap>();
	List<String> unids = new ArrayList<String>();
	
	/**
	 * Método principal
	 * @return String - Nome do Relatório
	 */
	@Override
	public String getNomeTarefa() {
		return "SCE - Registro de Inventário Modelo P7";
	}
	
	/**
	 * Método Criar valores iniciais
	 * @return Map - Filtros do Front-end
	 */
	@Override
	public Map<String, Object> criarValoresIniciais(){
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		
		Aac10 aac10 = getVariaveis().getAac10();
		
		LocalDate datas = MDate.date();
		filtrosDefault.put("livroNum", "0");
		filtrosDefault.put("livroPag", "0");
		filtrosDefault.put("impressao", "0");
		filtrosDefault.put("imprimir", "0");
		filtrosDefault.put("resumo", "0");
		filtrosDefault.put("data", datas);
		filtrosDefault.put("rascunho", true);
		filtrosDefault.put("repLeg1", aac10.getAac10rNome());
		filtrosDefault.put("contab1", aac10.getAac10cNome());
		filtrosDefault.put("contab2", aac10.getAac10cCrc());
		
		LocalDate data = MDate.date();
		Aag0201 municipio = getSession().createCriteria(Aag0201.class)
							.addWhere(Criterions.eq("aag0201id", aac10.getAac10municipio().getAag0201id()))
							.get();
		
		String dataTermo = municipio.getAag0201nome() +", "+
						   data.getDayOfMonth() + " de " +
						   data.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt")) + " de " +
						   data.getYear();
		
		filtrosDefault.put("dataTermo", dataTermo);
		
		filtrosDefault.put("token", obterUsuarioLogado().aab10hash);
		
		filtrosDefault.put("whereGC", getVariaveis().getMapeamentoGc(Bcb10.class));
		
		return Utils.map("filtros", filtrosDefault);
	}

	/**
	 * Método Principal
	 * @return DadosParaDownload - Dados em PDF
	 */
	@Override
	public DadosParaDownload executar() {
		List<Long> idsGrupos = getListLong("grupos");
		Integer livroNum = getInteger("livroNum");
		Integer livroPag = getInteger("livroPag");
		Integer impressao = getInteger("impressao");
		Integer imprimir = getInteger("imprimir");
		Integer resumo = getInteger("resumo");
		Boolean totUniMed = get("totUniMed");
		Boolean totQtd = get("totQtd");
		Long inventario = getLong("inventario");
		Boolean rascunho = get("rascunho");
		
		String nomeRelatorio = imprimir == 0 ? "SCE_RegistroDeInventarioP7_R2" : "SCE_RegistroDeInventarioP7_R1";

		params.put("TITULO_RELATORIO", "REGISTRO DE INVENTARIO");
		params.put("PAGINA_FOLHA", impressao == 0 ? "Página:" : "Folha:");
		params.put("NUMERO_PAGINA", livroPag);
		
		TableMapDataSource dados = buscarDadosDoRelatorio(idsGrupos, livroNum, livroPag, impressao, imprimir, resumo, totUniMed, totQtd, inventario, rascunho);

		JasperReport report = carregarArquivoRelatorio(nomeRelatorio);
		JasperPrint print = processarRelatorio(report, dados);
		byte[] bytes = convertPrintToPDF(print);
		
		DadosParaDownload dadosParaDownload = criarDadosParaDownload(bytes)
		
		Integer numPaginasRelatorio = print.getPages() != null ? print.getPages().size() : 0;
		
		posExecucaoRelatorio(dadosParaDownload, numPaginasRelatorio, inventario, rascunho, imprimir, livroNum, livroPag);
				
		return dadosParaDownload;
	}
	
	private void posExecucaoRelatorio(DadosParaDownload dadosParaDownload, Integer numPaginasRelatorio, Long inventario, Boolean rascunho, Integer imprimir, Integer livroNum, Integer livroPag) {
		if(!rascunho) {
			
			Bcb10 bcb10 = getSession().createCriteria(Bcb10.class)
									  .addWhere(Criterions.eq("bcb10id", inventario))
									  .addWhere(getSamWhere().getCritPadrao(Bcb10.class))
									  .get(ColumnType.ENTITY);
			
			if(imprimir == 0) {
				Integer numeroPaginas = livroPag + numPaginasRelatorio;
				
				bcb10.setBcb10livro(livroNum);
				bcb10.setBcb10pag(numeroPaginas);
				getSession().persist(bcb10);
				
				dadosParaDownload.putHeader("paginas", numeroPaginas+"");
				dadosParaDownload.putHeader("livro", livroNum+"");
				
			}else if(imprimir == 2) {
				Integer numeroPaginas = livroPag + numPaginasRelatorio;
				
				bcb10.setBcb10livro(livroNum);
				bcb10.setBcb10pag(numeroPaginas);
				getSession().persist(bcb10);
				
				dadosParaDownload.putHeader("paginas", numeroPaginas+"");
				dadosParaDownload.putHeader("livro", livroNum+"");
			}
		}
	}
		
	/**
	 * Método Principal - Buscar dados do relatorio
	 * @return TableMapDataSource
	 */
	private TableMapDataSource buscarDadosDoRelatorio(List<Long> idsGrupos, Integer livroNum, Integer livroPag, Integer impressao, Integer imprimir, Integer resumo, Boolean totUniMed, Boolean totQtd, Long inventario, Boolean rascunho){
		// Lista principal com os dados para impressão
		List<TableMap> listInventario = new ArrayList<TableMap>();
		
		// Imprime o Livro
		if(imprimir == 0) {
			listInventario = buscarLivro(idsGrupos, resumo, totUniMed, totQtd, inventario);
		
		// Imprime os Termos de Abertura ou Encerramento
		}else {
			Bcb10 bcb10 = getSession().get(Bcb10.class, inventario);
			
			// Preenche os dados da empresa emissora	
			Aac10 aac10 = getVariaveis().getAac10();
			TableMap termo = new TableMap();
			termo.put("aac10rs", aac10.getAac10rs());
			termo.put("aac10endereco", aac10.getAac10endereco());
			termo.put("aac10numero", aac10.getAac10numero());
			termo.put("aac10complem", aac10.getAac10complem());
			termo.put("aac10bairro", aac10.getAac10bairro());
			termo.put("aac10cep", aac10.getAac10cep());
			
			Aag0201 aag0201 = getSession().createCriteria(Aag0201.class)
							 .addWhere(Criterions.eq("aag0201id", aac10.getAac10municipio().getAag0201id()))
							 .get();
								
			Aag02 aag02 = getSession().createCriteria(Aag02.class)
						 .addWhere(Criterions.eq("aag02id", aag0201.getAag0201uf().getAag02id()))
						 .get();
			
			termo.put("aag0201nome", aag0201.getAag0201nome());
			termo.put("aag02nome", aag02.getAag02nome());
			termo.put("aac10ni", aac10.getAac10ni());
			termo.put("aac10ie", getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aag02.aag02id));
			termo.put("aac10rjcNumero", aac10.getAac10rjcNumero());
			termo.put("aac10rjcdata", aac10.getAac10rjcData());
			termo.put("aac10nireNumero", aac10.getAac10nireNumero());
			termo.put("aac10nireData", aac10.getAac10nireData());
			
			listInventario.add(0, termo);
			
			String msg = null;
			String subTitulo = null;
			
			if(imprimir == 1){
				subTitulo = "TERMO DE ABERTURA";
				msg = "Contém este livro " + livroPag + (impressao == 0 ? " página(s) " : " folha(s) ") +
					  "da escrituração relativa a situação de " + bcb10.getBcb10data().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
					  " e servirá para o lançamento das operações próprias do estabelecimento do contribuinte abaixo identificado.";
			}else{
				subTitulo = "TERMO DE ENCERRAMENTO";
				msg = "Nesta data, procedemos ao Encerramento do presente Livro, de nr " + livroNum +
					  ", constituído por formulários com " + (livroPag+1) + (impressao == 0 ? " página(s) " : " folha(s) ") +
					  ", contendo a escrituração relativa a situação de " + bcb10.getBcb10data().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
					  ", conforme lei 6374/89 art 67, Paragrafo 1º Convênio 57/95, clausula 23º do estabelecimento do contribuinte abaixo identificado.";
					  
				params.put("NUMERO_PAGINA", livroPag+1);
			}
			
			params.put("SUB_TITULO_RELATORIO", subTitulo);
			params.put("MSG", msg);
			params.put("DATA_TERMO", getString("dataTermo"));
			params.put("ASSINATURA11", getString("repLeg1"));
			params.put("ASSINATURA12", getString("repLeg2"));
			params.put("ASSINATURA13", getString("repLeg3"));
			params.put("ASSINATURA21", getString("contab1"));
			params.put("ASSINATURA22", getString("contab2"));
			params.put("ASSINATURA23", getString("contab3"));
		}		
		
		TableMapDataSource dsPrincipal = new TableMapDataSource(listInventario);		
		return dsPrincipal;
	}
	
	/**
	 * Método Principal - Buscar dados para compor Livro
	 * @return List<TableMap> com dados do livro
	 */
	private List<TableMap> buscarLivro(List<Long> idsGrupos, Integer resumo, Boolean totUniMed, Boolean totQtd, Long inventario) {
		// Busca todos os registros de inventario
		List<TableMap> map1 = buscarItensPorGrupoEData(idsGrupos, inventario);
		Bcb10 bcb10 = getSession().get(Bcb10.class, inventario);
		
		if(map1.size() > 0) {
			for(int i=0; i < map1.size(); i++) {
				
				// Cria os graus e grupos
				String grau1Atual = map1.get(i).getString("abm40codigo").substring(0, 2);
				String grau2Atual = map1.get(i).getString("abm40codigo").substring(0, 4);
				String grupoAtual = map1.get(i).getString("abm40codigo");
				
				// Verifica se se existe algum grau especificado, se não, cria o primeiro grupo e adiciona o primeiro item
				if(grau1Ant == null || grau2Ant == null || grupoAnt == null) {
					map2.add(map1.get(i));
					
					grau1Ant = grau1Atual;
					grau2Ant = grau2Atual;
					grupoAnt = grupoAtual;
					
					somarTotais(map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"));
					
				} else {
					// Verifica se o codigo grupo Anterior é igual ao codigo grupo atual
					if(grupoAnt.equals(grupoAtual)) {
						map2.add(map1.get(i));
						
						grau1Ant = grau1Atual;
						grau2Ant = grau2Atual;
						grupoAnt = grupoAtual;
						
						somarTotais(map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"));
						
					// Se o codigo grupo anterior for diferente do atual, totaliza o grupo anterior
					} else {
						String descrGrupo = buscarDescricaoPorCodigo(grupoAnt);
						comporLinhaTotal(grupoAnt + " - " + descrGrupo, "descTotComp", "totComp", grupoTotal);
						if(totUniMed) totalizarUnidadeMedida(inventario, grupoAnt, grupoTotal, false);

						//Verifica se o codigo do Grau 2 Anterior é igual ao grau 2 atual, se sim, totaliza o grau 1 anterior e o grau 2 anterior
						if(grau2Ant.equals(grau2Atual)) {
							grau1Ant = grau1Atual;
							grau2Ant = grau2Atual;
							
							somarTotais(map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"), 0)

						//Grau 2 Anterior é diferenete ao grau 2 atual
						}else {
							
							if(!grau1Ant.equals(grau1Atual)) {
								String descrGrau1 = buscarDescricaoPorCodigo(grau1Ant);
								comporLinhaTotal(grau1Ant + " - " + descrGrau1, "descTotGrau1", "totGrau1", grau1Total);
								if(totUniMed) totalizarUnidadeMedida(inventario, grau1Ant, grau1Total, true);
								grau1Total = 0;
								somarTotais(map1.get(i).getBigDecimal("bcb11total"), 0, 0);
								
							} else {
								String descrGrau2 = buscarDescricaoPorCodigo(grau2Ant);
								comporLinhaTotal(grau2Ant + " - " + descrGrau2, "descTotGrau1", "totGrau1", grau2Total);
								if(totUniMed) totalizarUnidadeMedida(inventario, grau2Ant, grau2Total, true);
								grau2Total = 0;
								somarTotais(map1.get(i).getBigDecimal("bcb11total"), map1.get(i).getBigDecimal("bcb11total"), 0)
							}
						}

						grupoTotal = 0;
						grau1Ant = grau1Atual;
						grau2Ant = grau2Atual;
						grupoAnt = grupoAtual;
						map2.add(map1.get(i));
						somarTotais(0, 0, map1.get(i).getBigDecimal("bcb11total"));
					}
				}
				
				// Verifica se é a ultima linha para criação dos totalizadores
				if ((map1.size()-1) == i) {
					String descrGrupo = buscarDescricaoPorCodigo(grupoAnt);
					String descrGrau1 = buscarDescricaoPorCodigo(grau1Ant);
					String descrGrau2 = buscarDescricaoPorCodigo(grau2Ant);
					
					// Compor Graus e Grupo e Totalizar unidade de medidas
					comporLinhaTotal(grupoAnt + " - " + descrGrupo, "descTotComp", "totComp", grupoTotal);
					if(totUniMed) totalizarUnidadeMedida(inventario, grupoAnt, grupoTotal, false);
					comporLinhaTotal(grau2Ant + " - " + descrGrau2, "descTotGrau2", "totGrau2", grau2Total)
					if(totUniMed) totalizarUnidadeMedida(inventario, grau2Ant, grau2Total, false);
					comporLinhaTotal(grau1Ant + " - " + descrGrau1, "descTotGrau1", "totGrau1", grau1Total)
					if(totUniMed) totalizarUnidadeMedida(inventario, grau1Ant, grau1Total, true);
					
					if(resumo != 0) {
						map2.add(new TableMap(Utils.map("abm40codigo", grupoAnt, "resumo_grupos", "Resumo dos Grupos de Inventário")));
						List<TableMap> listGrupos = buscarResumoDosGrupos(inventario, idsGrupos, resumo);
						for(int j = 0; j < listGrupos.size(); j++) {
							String descricao = listGrupos.get(j).getString("abm40codigo") + " - " + buscarDescricaoPorCodigo(listGrupos.get(j).getString("abm40codigo"));
							comporLinhaTotal(descricao, "descricao", "total", listGrupos.get(j).getBigDecimal("total"))
						}
					}
										
				// Adiciona os demais grupos e itens
				}
			}
		}
				
		params.put("FIRMA", getVariaveis().getAac10().getAac10rs());
		params.put("CNPJ", getVariaveis().getAac10().getAac10ni());
		String ie = buscarInscricaoEstadualEmpresa();
		params.put("IE", ie);
		params.put("PERIODO", bcb10.getBcb10data().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		
		return map2;
	}
	
	/**
	 * Métodos Diversos - Buscar itens do inventario
	 * @return Lista de TableMaps com os itens
	 */
	private List<TableMap> buscarItensPorGrupoEData(List<Long> idsGrupos, Long inventario){
		String whereData = inventario != null ? " WHERE bcb10id = :inventario " : "";
		String whereAbm40 = idsGrupos != null && idsGrupos.size() > 0 ? " AND abm40id in (:idsGrupos) " : "";
		
		String sql = " SELECT abm40codigo, abm40descr, abm01tipo, abm01codigo, abm01descr, abm01na, bcb11unid, bcb11ncm, bcb11qtde, bcb11unit, bcb11total, bcb10data " +
					 " FROM Bcb11 " +
					 " INNER JOIN Abm40 ON bcb11grupo = abm40id " +
					 " INNER JOIN Abm01 ON bcb11item = abm01id " +
					 " INNER JOIN Bcb10 ON bcb11inv = bcb10id " +
					   whereData + whereAbm40 + getSamWhere().getWherePadrao("AND", Bcb10.class) +
					 " ORDER BY abm40codigo, abm01tipo, abm01codigo";
					 
		Query query = getSession().createQuery(sql);
		if(idsGrupos != null && idsGrupos.size() > 0) query.setParameter("idsGrupos", idsGrupos);
		if(inventario != null) query.setParameter("inventario", inventario);
		
		return query.getListTableMap();
	}
	
	/**
	 * Métdodos Diversos - Buscar total das unidades de medida
	 * @return Lista de TableMaps com os totais
	 */
	private List<TableMap> buscarTotalPorUnidadeMedidaEGrau(String abm40codigo, String grau, Long inventario){
		String subStrGrau = grau.length() == 2 ? " WHERE substring(abm40codigo,1,2) = '" + grau + "' " : grau.length() == 4 ? " WHERE substring(abm40codigo,1,4) = '" + grau + "' " : " WHERE abm40codigo = '" + abm40codigo + "' ";
		String whereData = inventario != null ? " AND bcb10id = :inventario" : "";
		
		String sql = " SELECT bcb11unid, SUM(bcb11qtde) as qtd, SUM(bcb11total) as totUnid " +
					 " FROM Bcb11 " + 
					 " INNER JOIN Abm40 ON bcb11grupo = abm40id " + 
					 " INNER JOIN Abm01 ON bcb11item = abm01id " + 
					 " INNER JOIN Bcb10 ON bcb11inv = bcb10id " + 
					   subStrGrau + whereData + getSamWhere().getWherePadrao("AND", Abm40.class) +
					 " GROUP BY bcb11unid";
		
		return getSession().createQuery(sql).setParameter("inventario", inventario).getListTableMap();
	}
	
	/**
	 * Métodos Diversos - Buscar resumo dos grupos
	 * @return Lista de TableMaps com o resumo dos grupos
	 */
	private List<TableMap> buscarResumoDosGrupos(Long inventario, List<Long> idsGrupos, Integer resumo){
		String whereData = inventario != null ? " WHERE bcb10id = :inventario " : "";
		String whereAbm40 = idsGrupos != null && idsGrupos.size() > 0 ? " AND abm40id in (:idsGrupos) " : "";
		
		String sql1 = " (SELECT substring(abm40codigo,1,2) AS abm40codigo, SUM(bcb11total) AS total " +
					  " FROM Bcb11 " +
					  " INNER JOIN Abm40 ON bcb11grupo = abm40id " +
					  " INNER JOIN Abm01 ON bcb11item = abm01id " +
					  " INNER JOIN Bcb10 ON bcb11inv = bcb10id " +
					    whereData + whereAbm40 + getSamWhere().getWherePadrao("AND", Abm40.class) +
					  " GROUP BY substring(abm40codigo,1,2) ORDER BY abm40codigo)";
					  
		String sql2 = " (SELECT substring(abm40codigo,1,4) AS abm40codigo, SUM(bcb11total) AS total " +
					  " FROM Bcb11 " +
					  " INNER JOIN Abm40 ON bcb11grupo = abm40id " +
					  " INNER JOIN Abm01 ON bcb11item = abm01id " +
					  " INNER JOIN Bcb10 ON bcb11inv = bcb10id " +
					    whereData + whereAbm40 + getSamWhere().getWherePadrao("AND", Abm40.class) +
					  " GROUP BY substring(abm40codigo,1,4) ORDER BY abm40codigo)";
		
		String sql = resumo == 1 ? sql1 : resumo == 2 ? sql2 : (sql1 + " UNION " + sql2 + " ORDER BY abm40codigo");
					
		Query query = getSession().createQuery(sql);
		if(idsGrupos != null && idsGrupos.size() > 0) query.setParameter("idsGrupos", idsGrupos);
		if(inventario != null) query.setParameter("inventario", inventario);
		
		return query.getListTableMap();
	}
	
	/**
	 * Métodos Diversos - Buscar descrição do grupo
	 * @return String com a descrição
	 */
	private String buscarDescricaoPorCodigo(String abm40codigo) {
		return getSession().createCriteria(Abm40.class)
						   .addFields("abm40descr")
						   .addWhere(Criterions.eq("abm40codigo", abm40codigo))
						   .get(ColumnType.STRING);
	}
	
	/**
	 * Métodos Diversos - Somar os totais dos grupos
	 */
	private void somarTotais(grau1, grau2, grupo) {
		this.grupoTotal += grupo;
		this.grau1Total += grau1;
		this.grau2Total += grau2;
	}
	
	/**
	 * Méodos Diversos - Totalizar Unidade de Medida
	 */
	private void totalizarUnidadeMedida(Long inventario, String grau, BigDecimal total, boolean isGraus) {
		List<TableMap> uniMedTot = buscarTotalPorUnidadeMedidaEGrau(this.grupoAnt, grau, inventario);
		for(TableMap unimed : uniMedTot) {
			if(isGraus && !unids.contains(unimed.getString("bcb11unid"))) continue;
			TableMap mapUnimed = new TableMap();
			mapUnimed.put("abm40codigo", this.grupoAnt);
			mapUnimed.put("descrUniMed", unimed.getString("bcb11unid"));
			mapUnimed.put("qtdUniMed", unimed.getBigDecimal("qtd"));
			mapUnimed.put("totUniMed", unimed.getBigDecimal("totUnid"));
			map2.add(mapUnimed);
			if(!unids.contains(unimed.getString("bcb11unid"))) unids.add(unimed.getString("bcb11unid"));
		}
		
		comporLinhaTotal(grau, "descTotCompUniMed", "totCompUniMed", total);
	}
	
	/**
	 * Métodos Diversos - Compor as linhas totalizadoras
	 */
	private void comporLinhaTotal(String descricao, String coluna1, String coluna2, BigDecimal total) {
		TableMap tableMap = new TableMap();
		tableMap.put("abm40codigo", grupoAnt);
		tableMap.put(coluna1, "TOTAL DE " + descricao);
		tableMap.put(coluna2, total);
		map2.add(tableMap);
	}
	
	private String buscarInscricaoEstadualEmpresa() {
		Aac10 empresa = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		return getAcessoAoBanco().buscarIEEmpresaPorEstado(empresa.aac10id, empresa.aac10municipio.aag0201uf.aag02id);
	}
}
//meta-sis-eyJkZXNjciI6IlNDRSAtIFJlZ2lzdHJvIGRlIEludmVudMOhcmlvIE1vZGVsbyBQNyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==