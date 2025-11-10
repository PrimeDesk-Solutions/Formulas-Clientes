package multitec.relatorios.cgs;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sam.dto.sgc.ConfigGrausCodigoContaContabil
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abc10;
import sam.model.entities.eb.Eba40;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class CGS_ContasContabeis extends RelatorioBase {
	private final int ORDENAR_CODIGO = 0; 
	
	@Override
	public String getNomeTarefa() {
		return "CGS - Contas Contábeis";
	}

	@Override
	public DadosParaDownload executar() {
		boolean anexarDiario = get("anexarDiario");
		
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("TITULO_RELATORIO", "Contas Contábeis");
		params.put("PAGINA_FOLHA", "Página:");
		params.put("PAGINA_INICIAL", anexarDiario ? getInteger("pagina") : 0);
		params.put("ANEXAR_DIARIO", anexarDiario);
		
		ConfigGrausCodigoContaContabil configGrausCodigoContaContabil = ConfigGrausCodigoContaContabil.obterGrausDigitosEstruturaCodigos(obterEstruturaContas());
		Integer grauEmpresa = obterGrauEmpresa();
		if(grauEmpresa == null)throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro Abc10_GrauEmpresa");
		configGrausCodigoContaContabil.setGrauEmpresa(grauEmpresa);
		
		List<Long> idsContas = getListLong("contas");
		
		List<TableMap> dados = new ArrayList<>();
		int ordem = getInteger("ordem");
		if(ordem == ORDENAR_CODIGO) {
			dados = obterContasContabeisOrdenadosPeloCodigo(idsContas);
		}else {
			dados = obterContasContabeisOrdermAlfabetica(idsContas, configGrausCodigoContaContabil);
		}
		
		JasperReport report = carregarArquivoRelatorio("CGS_ContasContabeis");
		JasperPrint print = processarRelatorio(report, dados);

		if(print == null || print.getPages().size() == 0)throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");
		
		Integer numPaginaRelatorio = print.getPages() != null ? print.getPages().size() : 0;
		
		gravarDadosDoLivro(numPaginaRelatorio);
		
		byte[] bytes;
		try {
			bytes = JasperExportManager.exportReportToPdf(print);
		} catch (JRException e) {
			throw new RuntimeException("Erro ao gerar o relatório da classe "+ this.getClass().getName(), e);
		}
		return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		
		Eba40 eba40 = obterUltimaPaginaDoLivro();
		filtrosDefault.put("pagina", eba40 != null ? eba40.getEba40ultPag() : 0);
		
		filtrosDefault.put("anexarDiario", false);
		
		filtrosDefault.put("ordem", "0");
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	private List<TableMap> obterContasContabeisOrdermAlfabetica(List<Long> idsContas, ConfigGrausCodigoContaContabil configGrausCodigoContaContabil){
		int qtdDigAnteriorGrauEmpresa = configGrausCodigoContaContabil.getQtdDigAnteriorGrauEmpresa();
		int qtdDig = configGrausCodigoContaContabil.getQtDig()[configGrausCodigoContaContabil.getQtGrau()-1];
		int grau6 = configGrausCodigoContaContabil.getQtDig()[qtdDigAnteriorGrauEmpresa-1];
		
		List<TableMap> lista = new ArrayList<>();
		
		List<TableMap> ctasContabeis = obterContasContabeisOrdenadosPeloCodigo(idsContas);
		for(TableMap contas : ctasContabeis) {
			if(contas.get("abc10codigo").toString().length() < qtdDig) lista.add(contas);
			
			//Caso o código seja de 6ºGrau 
			if(contas.get("abc10codigo").toString().length() == grau6) {
				List<TableMap> ctasGrau6OrdemAlfabetica = buscarContaContabilOrdenadoPeloNome(idsContas, configGrausCodigoContaContabil);
				if(ctasGrau6OrdemAlfabetica != null && ctasGrau6OrdemAlfabetica.size() > 0){
					for(TableMap ctaGrau6 : ctasGrau6OrdemAlfabetica) {
						String codigo = ctaGrau6.get("abc10codigo");
						if(codigo.substring(0, grau6).equals(contas.get("abc10codigo"))) {
							TableMap cta = new TableMap();	
							cta.put("abc10codigo", codigo);
							cta.put("abc10nome", ctaGrau6.get("abc10nome"));
							cta.put("abc10reduzido", ctaGrau6.get("abc10reduzido"));
							
							lista.add(cta);
						}
					}
				}
			}
		}
		
		return lista; 
	}
	
	private List<TableMap> buscarContaContabilOrdenadoPeloNome(List<Long> idsContas,  ConfigGrausCodigoContaContabil configGrausCodigoContaContabil){
		int qtdDig = configGrausCodigoContaContabil.getQtDig()[configGrausCodigoContaContabil.getQtGrau()-1];
		int qtGrau = configGrausCodigoContaContabil.getQtGrau();
		
		String whereCtas = idsContas != null && !idsContas.isEmpty() ? " abc10id IN (:idsContas) AND " : "";
		
		Query query = session.createQuery(" SELECT abc10codigo, abc10nome, abc10reduzido",
										  " FROM Abc10",
										  " WHERE ", whereCtas,
										  Fields.length("abc10codigo"), " = ", qtdDig,
										  " AND ", Fields.substring("abc10codigo", 0, qtGrau + 2), " IN (SELECT abc10codigo ", 
																		  						  		 "FROM Abc10 abc10 ", 
																		  						  		 "WHERE " , whereCtas, " LENGTH(abc10codigo) = ", qtGrau+1, 
																		  						  		 " ORDER BY abc10codigo) ",
										  samWhere.getWhereGc("AND", Abc10.class),
										  " ORDER BY abc10nome ");
		
		if(idsContas != null && !idsContas.isEmpty()) query.setParameter("idsContas", idsContas);
		
		return query.getListTableMap();
	}

	private List<TableMap> obterContasContabeisOrdenadosPeloCodigo(List<Long> idsContas) {
		String whereCtas = idsContas != null && !idsContas.isEmpty() ? " AND abc10id IN (:idsContas) " : "";
		
		Query query = getSession().createQuery("SELECT abc10codigo, abc10nome, abc10reduzido ",
											   "FROM Abc10 ",
											   getSamWhere().getWherePadrao("WHERE", Abc10.class),
											   whereCtas,
 											   " ORDER BY abc10codigo");
		
		if(idsContas != null && !idsContas.isEmpty()) query.setParameter("idsContas", idsContas);
		
		return query.getListTableMap();
	}

	private Eba40 obterUltimaPaginaDoLivro() {
		return getSession().createQuery(
								"SELECT eba40id, eba40ultPag, eba40num FROM Eba40 WHERE eba40livro = :livro AND eba40termos = :termo ",
								getSamWhere().getWherePadrao("AND", Eba40.class),
								" ORDER BY eba40livro desc")
					.setParameters("livro", Eba40.LIVRO_DIARIO,
								   "termo", Eba40.NAO)
					.setMaxResult(1)
					.getUniqueResult(ColumnType.ENTITY);
	}
	
	private void gravarDadosDoLivro(Integer numPaginaRelatorio) {
		try {
			boolean isAnexar = get("anexarDiario");
			
			if(isAnexar) {
				Integer numeroPaginas = getInteger("pagina") + numPaginaRelatorio;
				
				Eba40 eba40 = obterUltimaPaginaDoLivro();
				if(eba40 == null) throw new Exception("Não foi encontrado livro diário em aberto.");
				
				//Como já existe o livro, só atualiza a página e o termo
				eba40.setEba40ultPag(numeroPaginas);
				eba40.setEba40termos(0);
				
				getSession().persist(eba40);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Integer obterGrauEmpresa() {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "GRAUEMPRESA"))
				.addWhere(Criterions.eq("aba01aplic", "ABC10"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.setMaxResults(1)
				.get();
		Integer grauEmpresa = null;
		if(aba01 != null) {
			try {
				grauEmpresa = Integer.parseInt(aba01.getAba01conteudo());
			} catch (Exception e) {
				//ignorar
			}
		}
		return grauEmpresa;
	}
	
	private String obterEstruturaContas() {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "ESTRCODCONTA"))
				.addWhere(Criterions.eq("aba01aplic", "ABC10"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.setMaxResults(1)
				.get();
		String estruturaContas = null;
		if(aba01 != null) {
			estruturaContas = aba01.getAba01conteudo();
		}
		return estruturaContas;
	}
}
//meta-sis-eyJkZXNjciI6IkNHUyAtIENvbnRhcyBDb250w6FiZWlzIiwidGlwbyI6InJlbGF0b3JpbyJ9