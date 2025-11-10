package multitec.relatorios.sgt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ea.Eaa01
import sam.model.entities.eb.Eba40;
import sam.model.entities.ed.Edd10
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource

public class SGT_LivroServico_51_54_56 extends RelatorioBase {
	private final int LIVRO = 0;
	private final int TERMO_ABERTURA = 1;
	private final int TERMO_ENCERRAMENTO = 2;
	
	private final int MODELO_51 = 0;
	private final int MODELO_54 = 1;
	private final int MODELO_56 = 2;
	
	private String obs;
	private String aliqIss;
	private String iss;
	private String bcIss;
	private String isento;
	private String remDev;
	private String servicos;
	private String materiais;
	private String subEmp;
	
	@Override
	public String getNomeTarefa() {
		return "SGT - Livro Serviços 51/54/56";
	}

	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		
		int imprimir = getInteger("imprimir");
		boolean isRascunho = get("rascunho");
		int modelo = get("modelo");
		
		LocalDate data = DateUtils.parseDate("01/" + getString("dataPeriodo"));
		LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
		
		selecionarAlinhamento("0081");
		
		obs = getCampo("0", "obs");
		aliqIss = getCampo("0", "aliqIss");
		bcIss = getCampo("0", "bcIss");
		iss = getCampo("0", "iss");
		isento = getCampo("0", "isento");
		remDev = getCampo("0", "remDev");
		servicos = getCampo("0", "servicos");
		materiais = getCampo("0", "materiais");
		subEmp = getCampo("0", "subEmp");
		
		params.put("TITULO_RELATORIO", "Registro de Notas Fiscais de Serviços Prestados - Imposto Sobre Serviços");
		if(imprimir == LIVRO){
			params.put("AAC10RS", variaveis.aac10.getAac10rs());
			params.put("AAC10NI", variaveis.aac10.getAac10ni());
			params.put("AAC10IE", obterIEEmpresa());
			params.put("PERIODO", "MÊS/ANO: " + DateUtils.formatDate(data, "MM/yyyy"));
			params.put("RASCUNHO", isRascunho);
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
			params.put("PAGINA_FOLHA", "Página");
			if(modelo == MODELO_56) {
				params.put("AAC10ENDERECO", variaveis.aac10.getAac10endereco());
				params.put("AAG02UF", obterUFMunicipioEmpresa());
			}
		}else {
			params.put("NUMERO_PAGINA", imprimir == TERMO_ABERTURA ? 0 : getInteger("pagina"));
			params.put("NOME_LIVRO", modelo == MODELO_51 ? "' REGISTRO DE NOTAS FISCAIS DE SERVIÇOS PRESTADOS - IMPOSTO SOBRE SERVIÇOS MODELO 51 '" :
									 modelo == MODELO_54 ? "' REGISTRO DE NOTAS FISCAIS DE SERVIÇOS PRESTADOS - IMPOSTO SOBRE SERVIÇOS MODELO 54 '" :
									 					   "' REGISTRO DE NOTAS FISCAIS DE SERVIÇOS TOMADOS OU INTERMEDIADOS DE TERCEIROS - MODELO 56 '");
			params.put("SUB_TITULO_RELATORIO", imprimir == TERMO_ABERTURA ? "T E R M O    D E    A B E R T U R A" : "T E R M O    D E    E N C E R R A M E N T O");
			params.put("SERVIU_SERVIRA", imprimir == TERMO_ABERTURA ? " e que servirá" : " e que serviu");
			params.put("NUM_PAGINAS", getInteger("pagina"));
			params.put("PAGINA_FINAL", getInteger("pagina") + 1);
			params.put("NUMERO_LIVRO", getInteger("livro"));
			params.put("PAG_FOLHA", "páginas ");
			params.put("DATA_TERMO", getString("data"));
			params.put("ASSINATURA11", getString("representante1"));
			params.put("ASSINATURA12", getString("representante2"));
			params.put("ASSINATURA13", getString("representante3"));
			params.put("ASSINATURA21", getString("assinatura1"));
			params.put("ASSINATURA22", getString("assinatura2"));
			params.put("ASSINATURA23", getString("assinatura3"));
		}
		
		JasperReport report = null;
		
		List<TableMap> dados = new ArrayList<>();
		if(imprimir == LIVRO) {
			dados = obterLivro(datas[0], datas[1], modelo);
			report = carregarArquivoRelatorio(modelo == MODELO_51 ? "SGT_LivroServico_51_54_56_R2" : (modelo == MODELO_54 ? "SGT_LivroServico_51_54_56_R3" : "SGT_LivroServico_51_54_56_R4"));
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
			report = carregarArquivoRelatorio(modelo == MODELO_56 ? "SGT_LivroServico_51_54_56_R5" : "SGT_LivroServico_51_54_56_R1");
		}
		
		JasperPrint print = processarRelatorio(report, dados);

		Integer numPaginaRelatorio = print.getPages() != null ? print.getPages().size() : 0;
		
		comporDadosDoLivroParaGravar(numPaginaRelatorio);
		
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
		configurarDadosResponsavel(filtrosDefault);
		configurarLivroEPagina(filtrosDefault);
		configurarDataInicial(filtrosDefault);
		filtrosDefault.put("modelo", 0);
		filtrosDefault.put("imprimir", 0);
		filtrosDefault.put("rascunho", true);
		return Utils.map("filtros", filtrosDefault);
	}

	private void configurarDadosResponsavel(Map<String, Object> filtrosDefault) {
		Aac10 aac10 = getVariaveis().getAac10();
		filtrosDefault.put("representante1", aac10.getAac10rNome());
		filtrosDefault.put("representante2", aac10.getAac10rCpf());
		filtrosDefault.put("assinatura1", aac10.getAac10cNome());
		filtrosDefault.put("assinatura2", aac10.getAac10cCrc());
		
		Aag0201 aag0201 = obterMunicipio(aac10.getAac10municipio().getAag0201id());
		String	municipio = aag0201 != null ? aag0201.getAag0201nome() + ", " : ", ";
		String dataTermo = StringUtils.concat(municipio, DateUtils.formatDate(MDate.date(), "dd 'de' MMMM 'de' yyyy"), ".");
		filtrosDefault.put("data", dataTermo);
	}
	
	private void configurarLivroEPagina(Map<String, Object> filtrosDefault) {
		Integer livro = null;
		Integer pagina = null;
		
		Edd10 edd10 = obterUltimoLivro();
		if(edd10 != null) {
			livro = edd10.getEdd10num();
			pagina = edd10.getEdd10ultPag();
		} else {
			livro = obterProximoNum();
			pagina = 1;
		}
		
		filtrosDefault.put("livro", livro);
		filtrosDefault.put("pagina", pagina);
	}
	
	private List<TableMap> obterLivro(LocalDate dataIni, LocalDate dataFin, int modelo) {
		List<TableMap> listTM = null;
		if(modelo == MODELO_51) {
			listTM = obterLivroModelo51(dataIni, dataFin);
		}else if(modelo == MODELO_54) {
			listTM = obterLivroModelo54(dataIni, dataFin);
		}else {
			listTM = obterLivroModelo56(dataIni, dataFin); 
		}
		
		return listTM;
	}
	
	private List<TableMap> obterLivroModelo51(LocalDate dataIni, LocalDate dataFin) {
		params.put("sgt_livroServico_51_54_56_r2s1Stream", carregarArquivoRelatorio("SGT_LivroServico_51_54_56_R2S1"));
		
		List<TableMap> listTM = buscarNotasServicoModelo51(dataIni, dataFin);
		if(Utils.isEmpty(listTM)) return null;
		
		//Sub1: Resumo por alíquotas
		List<TableMap> listTMNotasServicoPorAliquotas = buscarNotasServicoPorAliquotas(dataIni, dataFin);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(listTMNotasServicoPorAliquotas);
		params.put("sgt_livroServico_51_54_56_r2s1DS", dataSetSub1);
		
		return listTM;
	}
	
	private List<TableMap> obterLivroModelo54(LocalDate dataIni, LocalDate dataFin) {
		params.put("sgt_livroServico_51_54_56_r3s1Stream", carregarArquivoRelatorio("SGT_LivroServico_51_54_56_R3S1"));
		
		List<TableMap> listTM = buscarNotasServicoModelo54(dataIni, dataFin);
		if(Utils.isEmpty(listTM)) return null;
		
		for(TableMap tm : listTM) {
			BigDecimal serv = tm.getBigDecimal("serv");
			if(serv == null) serv = BigDecimal.ZERO;
			BigDecimal mat = tm.getBigDecimal("mat");
			if(mat == null) mat = BigDecimal.ZERO;
			BigDecimal subemp = tm.getBigDecimal("subemp");
			if(subemp == null) subemp = BigDecimal.ZERO;
			BigDecimal fatura = serv + mat + subemp;
			tm.put("fatura", fatura);
		}
		
		//Sub1: Resumo por alíquotas
		List<TableMap> listTMNotasServicoPorAliquotas = buscarNotasServicoPorAliquotas(dataIni, dataFin);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(listTMNotasServicoPorAliquotas);
		params.put("sgt_livroServico_51_54_56_r3s1DS", dataSetSub1);
		
		return listTM;
	}
	
	private List<TableMap> obterLivroModelo56(LocalDate dataIni, LocalDate dataFin) {
		params.put("sgt_livroServico_51_54_56_r4s1Stream", carregarArquivoRelatorio("SGT_LivroServico_51_54_56_R4S1"));
		
		List<TableMap> listTM = buscarNotasServicoModelo56(dataIni, dataFin);
		if(Utils.isEmpty(listTM)) return null;
		
		Long municipioEmpresa = getVariaveis().getAac10().getAac10municipio() == null ? null : getVariaveis().getAac10().getAac10municipio().getIdValue();
		
		for(TableMap tm : listTM) {
			Long eaa0101municipio = tm.getLong("eaa0101municipio");
			Integer municipio = municipioEmpresa == null ? 2 : (municipioEmpresa.equals(eaa0101municipio) ? 1 : 2);
			tm.put("municipio", municipio);
			
			BigDecimal serv = tm.getBigDecimal("serv");
			if(serv == null) serv = BigDecimal.ZERO;
			BigDecimal mat = tm.getBigDecimal("mat");
			if(mat == null) mat = BigDecimal.ZERO;
			BigDecimal subemp = tm.getBigDecimal("subemp");
			if(subemp == null) subemp = BigDecimal.ZERO;
			BigDecimal fatura = serv + mat + subemp;
			tm.put("fatura", fatura);
		}
		
		//Sub1: Resumo por alíquotas
		List<TableMap> listTMNotasServicoTomadosPorAliquotas = buscarNotasServicoTomadosPorAliquotas(dataIni, dataFin);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(listTMNotasServicoTomadosPorAliquotas);
		params.put("sgt_livroServico_51_54_56_r4s1DS", dataSetSub1);
		
		return listTM;
	}
	
	private List<TableMap> buscarNotasServicoModelo51(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		if(obs != null)select.append(", jGet(eaa01json." + obs + ") As obs ");
		select.append(", jGet(eaa0103json." + aliqIss + ")::numeric As tx_iss ");
		select.append(", SUM(jGet(eaa0103json." + bcIss + ")::numeric) As bc_iss ");
		select.append(", SUM(jGet(eaa0103json." + iss + ")::numeric) As iss ");
		select.append(", SUM(jGet(eaa0103json." + isento + ")::numeric) As isento ");
		select.append(", SUM(jGet(eaa0103json." + remDev + ")::numeric) As remdev ");
		
		return getSession().createQuery(" SELECT eaa01id, abb01num, abb01serie, abb01data ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroServ > :livroServ",
										" AND abb01data BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY eaa01id, abb01num, abb01serie, abb01data, ",
										(obs == null ? "" : " jGet(eaa01json." + obs + "), "),
										" jGet(eaa0103json." + aliqIss + ") ",
										" ORDER BY abb01data, abb01num, abb01serie")
						   .setParameters("esMov", Eaa01.ESMOV_SAIDA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroServ", Eaa01.ILIVROSERV_NAO,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarNotasServicoPorAliquotas(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(" jGet(eaa0103json." + aliqIss + ")::numeric As tx_iss ");
		select.append(", SUM(jGet(eaa0103json." + bcIss + ")::numeric) As bc_iss ");
		select.append(", SUM(jGet(eaa0103json." + iss + ")::numeric) As iss ");
				
		return getSession().createQuery(" SELECT ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroServ > :livroServ ",
										" AND abb01data BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY jGet(eaa0103json." + aliqIss + ") ",
										" ORDER BY jGet(eaa0103json." + aliqIss + ") ")
						   .setParameters("esMov", Eaa01.ESMOV_SAIDA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroServ", Eaa01.ILIVROSERV_NAO,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarNotasServicoModelo54(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqIss + ")::numeric As tx_iss ");
		select.append(", SUM(jGet(eaa0103json." + bcIss + ")::numeric) As bc_iss ");
		select.append(", SUM(jGet(eaa0103json." + iss + ")::numeric) As iss ");
		select.append(", SUM(eaa0103totDoc) As serv ");
		select.append(", SUM(jGet(eaa0103json." + materiais + ")::numeric) As mat ");
		select.append(", SUM(jGet(eaa0103json." + subEmp + ")::numeric) As subemp ");
		
		return getSession().createQuery(" SELECT eaa01id, abb01num, abb01data ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroServ > :livroServ ",
										" AND abb01data BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY eaa01id, abb01num, abb01data, ",
										" jGet(eaa0103json." + aliqIss + ") ",
										" ORDER BY abb01data, abb01num")
						   .setParameters("esMov", Eaa01.ESMOV_SAIDA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroServ", Eaa01.ILIVROSERV_NAO,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarNotasServicoModelo56(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		if(obs != null)select.append(", jGet(eaa01json." + obs + ") As obs ");
		select.append(", jGet(eaa0103json." + aliqIss + ")::numeric As tx_iss ");
		select.append(", SUM(jGet(eaa0103json." + bcIss + ")::numeric) As bc_iss ");
		select.append(", SUM(jGet(eaa0103json." + iss + ")::numeric) As iss ");
		select.append(", SUM(eaa0103totDoc) As serv ");
		select.append(", SUM(jGet(eaa0103json." + materiais + ")::numeric) As mat ");
		select.append(", SUM(jGet(eaa0103json." + subEmp + ")::numeric) As subemp ");
		
		return getSession().createQuery(" SELECT eaa01id, abb01num, eaa01esData, abb01serie, aah01na, eaa0102ni, eaa0101municipio, aaj05federal ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" LEFT JOIN Eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 ",
										" LEFT JOIN Aaj05 ON eaa0103codServ = aaj05id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroServ > :livroServ ",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY eaa01id, abb01num, eaa01esData, abb01serie, aah01na, eaa0102ni, eaa0101municipio, aaj05federal, ",
										(obs == null ? "" : " jGet(eaa01json." + obs + "), "),
										" jGet(eaa0103json." + aliqIss + ") ",
										" ORDER BY eaa01esData, abb01num, aaj05federal, ",
										" jGet(eaa0103json." + aliqIss + ") ")
						   .setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroServ", Eaa01.ILIVROSERV_NAO,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarNotasServicoTomadosPorAliquotas(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqIss + ")::numeric As tx_iss ");
		select.append(", SUM(jGet(eaa0103json." + bcIss + ")::numeric) As bc_iss ");
		select.append(", SUM(jGet(eaa0103json." + iss + ")::numeric) As iss ");
		
		return getSession().createQuery(" SELECT aaj05federal ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" LEFT JOIN Aaj05 ON eaa0103codServ = aaj05id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroServ > :livroServ ",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY aaj05federal, ",
										" jGet(eaa0103json." + aliqIss + ") ",
										" ORDER BY ",
										" jGet(eaa0103json." + aliqIss + "), ",
										" aaj05federal ")
						   .setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroServ", Eaa01.ILIVROSERV_NAO,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private void configurarDataInicial(Map<String, Object> filtrosDefault) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "DATAATUAL"))
				.addWhere(Criterions.eq("aba01aplic", "EA"))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Aba01.class)))
				.setMaxResults(1)
				.get();
		LocalDate data = null;
		if(aba01 != null) {
			try {
				data = DateUtils.parseDate(aba01.getAba01conteudo());
			} catch (Exception e) {
				//ignorar
			}
		}
		if(data == null) {
			data = MDate.date();
		}
		filtrosDefault.put("dataPeriodo", data.format(DateTimeFormatter.ofPattern("MM/yyyy")));
	}
	
	private void comporDadosDoLivroParaGravar(Integer numPaginaRelatorio) {
		boolean isRascunho = get("rascunho");
		int imprimir = getInteger("imprimir");
		
		if(!isRascunho) {
			boolean isTermoEncerramento = false;
			
			Integer numeroLivro = getInteger("livro");
			Integer numeroPaginas = null;
			if(imprimir == LIVRO) {
				numeroPaginas = getInteger("pagina") + numPaginaRelatorio;
			} else {
				numeroLivro = getInteger("livro") + 1;
				numeroPaginas = 1;
				isTermoEncerramento = imprimir == TERMO_ENCERRAMENTO;
			}
			
			gravarDadosDoLivro(numeroLivro, numeroPaginas, isTermoEncerramento);
		}
	}
	
	private void gravarDadosDoLivro(Integer numeroLivro, Integer numeroPaginas, boolean isTermoEncerramento) {
		try {
			validacoesDoLivro(numeroLivro);
			
			Edd10 edd10 = obterUltimoLivro() != null ? obterUltimoLivro() : new Eba40();
			if(edd10.getEdd10id() == null) {
				edd10.setEdd10livro(Edd10.LIVRO_SERVICOS_515456);
				edd10.setEdd10num(numeroLivro);
				getSamWhere().setDefaultValues(edd10);
			}
			
			edd10.setEdd10ultPag(numeroPaginas);
			edd10.setEdd10termos(isTermoEncerramento ? 1 : 0);
			
			getSession().persist(edd10);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void validacoesDoLivro(Integer edd10num) throws Exception {
		Edd10 ultimoLivro = obterUltimoLivro();
		if(ultimoLivro != null) {
			if(!ultimoLivro.getEdd10num().equals(edd10num)) {
				throw new ValidacaoException("Existe livro em aberto. Não será possivel gravar o número informado.");
			}
		}
		
		Edd10 edd10existe = buscarLivroPeloNum(edd10num);
		if(edd10existe != null && edd10existe.getEdd10termos() == Edd10.SIM) {
			throw new ValidacaoException("O livro informado consta como encerrado.");
		}
	}
	
	private Edd10 buscarLivroPeloNum(Integer edd10num) {
		return getSession().createQuery(" SELECT edd10id, edd10termos ",
										" FROM Edd10 WHERE edd10livro = :edd10livro AND edd10num = :edd10num ", 
										getSamWhere().getWherePadrao("AND", Edd10.class))
						   .setParameters("edd10livro", Edd10.LIVRO_SERVICOS_515456,
								   	      "edd10num", edd10num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Edd10 obterUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
				.addWhere(Criterions.eq("edd10livro", Edd10.LIVRO_SERVICOS_515456))
				.addWhere(Criterions.eq("edd10termos", Edd10.NAO))
				.addWhere(getSamWhere().getCritPadrao(Edd10.class))
				.setOrder("edd10num DESC")
				.setMaxResults(1)
				.get();
	}
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery(
									"SELECT MAX(edd10num) FROM Edd10 WHERE edd10livro = :edd10livro",
									getSamWhere().getWherePadrao("AND", Edd10.class))
				  .setParameter("edd10livro", Edd10.LIVRO_SERVICOS_515456)
	  	   		  .getUniqueResult(ColumnType.INTEGER);
		return num == null ? 1 : num + 1;
	}
	
	private TableMap obterDadosTermoAberturaOrEncerramento() {
		return getSession().createQuery(
				" SELECT aac10rs, aac10endereco, aac10numero, aac10complem, aac10bairro, aac10ni, aac1002ie, aac10rjcnumero, aac10rjcdata, aac10nirenumero, aac10niredata, aag0201nome, aag02uf, aac10cep " +
				" FROM Aac10 "+
				" LEFT JOIN Aac1002 ON aac1002empresa = aac10id AND aac1002empresa = :idAac10 "+
				" LEFT JOIN Aag0201 ON aag0201id = aac10municipio " +
				" LEFT JOIN Aag02 ON aag02id = aag0201uf " +
				" WHERE aac10id = :idAac10 ")
			.setParameter("idAac10", getVariaveis().getAac10().getAac10id())
			.setMaxResult(1)
			.getUniqueTableMap();
	}
	
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	private String obterIEEmpresa() {
		Aag0201 aag0201 = variaveis.aac10.getAac10municipio();
		if(aag0201 == null) return null;
		
		aag0201 = getSession().createCriteria(Aag0201.class)
			.addFields("aag0201id, aag0201uf")
			.addWhere(Criterions.eq("aag0201id", aag0201.getIdValue()))
			.get();
		
		return getSession().createCriteria(Aac1002.class)
			.addFields("aac1002ie")
			.addWhere(Criterions.eq("aac1002empresa", variaveis.aac10.getIdValue()))
			.addWhere(Criterions.eq("aac1002uf", aag0201.getAag0201uf().getIdValue()))
			.setMaxResults(1)
			.get(ColumnType.STRING);
	}
	
	private String obterUFMunicipioEmpresa() {
		Aag0201 aag0201 = variaveis.aac10.getAac10municipio();
		if(aag0201 == null) return null;
		
		aag0201 = getSession().createCriteria(Aag0201.class)
			.addFields("aag0201id, aag0201uf, aag02id, aag02uf")
			.addJoin(Joins.part("aag0201uf"))
			.addWhere(Criterions.eq("aag0201id", aag0201.getIdValue()))
			.get();
			
		return aag0201 != null ? aag0201.getAag0201uf().getAag02uf() : null;
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIExpdnJvIFNlcnZpw6dvcyA1MS81NC81NiIsInRpcG8iOiJyZWxhdG9yaW8ifQ==