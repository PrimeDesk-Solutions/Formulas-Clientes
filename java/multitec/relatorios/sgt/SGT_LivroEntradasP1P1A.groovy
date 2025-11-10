package multitec.relatorios.sgt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
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

public class SGT_LivroEntradasP1P1A extends RelatorioBase {
	private final int LIVRO = 0;
	private final int TERMO_ABERTURA = 1;
	private final int TERMO_ENCERRAMENTO = 2;
	
	private final int P1 = 0;
	private final int P1A = 1;
	
	private String obs;
	private String aliqIcms;
	private String bcIcms;
	private String icms;
	private String isentasIcms;
	private String outrasIcms;
	private String bcIpi;
	private String ipi;
	private String isentasIpi;
	private String outrasIpi;
	private String vlrGia;
	private String icmsST;
	
	@Override
	public String getNomeTarefa() {
		return "SGT - Livro Entradas P1/P1A";
	}

	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		
		int imprimir = getInteger("imprimir");
		LocalDate[] datas = new LocalDate[2];
		datas[0] = DateUtils.parseDate(getString("dataInicial"));
		datas[1] = DateUtils.parseDate(getString("dataFinal"));
		boolean isRascunho = get("rascunho");
		int modelo = get("modelo");
		boolean entidade = get("entidade");
		boolean comIPI = get("comIPI");
		comIPI = modelo == P1 ? true : comIPI;
		boolean resumo = get("resumo")
		
		selecionarAlinhamento("0080");
		
		obs = getCampo("0", "obs");
		aliqIcms = getCampo("0", "aliqIcms");
		bcIcms = getCampo("0", "bcIcms");
		icms = getCampo("0", "icms");
		isentasIcms = getCampo("0", "isentasIcms");
		outrasIcms = getCampo("0", "outrasIcms");
		bcIpi = getCampo("0", "bcIpi");
		ipi = getCampo("0", "ipi");
		isentasIpi = getCampo("0", "isentasIpi");
		outrasIpi = getCampo("0", "outrasIpi");
		vlrGia = getCampo("0", "vlrGia");
		icmsST = getCampo("0", "icmsST");
		
		if(imprimir == LIVRO){
			params.put("TITULO_RELATORIO", "Registro de Entradas");
			params.put("AAC10RS", variaveis.aac10.getAac10rs());
			params.put("AAC10NI", variaveis.aac10.getAac10ni());
			params.put("AAC10IE", obterIEEmpresa());
			params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
			params.put("RASCUNHO", isRascunho);
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
			params.put("IMPRIME_DADOS_ENT", entidade);
			params.put("IMPR_RES_CFOP_ALIQ", resumo);
			params.put("COM_IPI", comIPI);
		}else {
			params.put("NUMERO_PAGINA", imprimir == TERMO_ABERTURA ? 0 : getInteger("pagina"));
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
		
		List<TableMap> dados = new ArrayList<>();
		if(imprimir == LIVRO) {
			dados = obterLivro(datas[0], datas[1]);
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		JasperReport report = carregarArquivoRelatorio(imprimir == LIVRO ? (modelo == P1 ? "SGT_LivroEntradasP1P1A_R2" : "SGT_LivroEntradasP1P1A_R3") : "SGT_LivroEntradasP1P1A_R1");
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
		filtrosDefault.put("entidade", true);
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
	
	private List<TableMap> obterLivro(LocalDate dataIni, LocalDate dataFin) {
		params.put("sgt_livroEntradasP1P1Ar2s1Stream", carregarArquivoRelatorio("SGT_LivroEntradasP1P1A_R2S1"));
		params.put("sgt_livroEntradasP1P1Ar2s2Stream", carregarArquivoRelatorio("SGT_LivroEntradasP1P1A_R2S2"));
		params.put("sgt_livroEntradasP1P1Ar2s3Stream", carregarArquivoRelatorio("SGT_LivroEntradasP1P1A_R2S3"));
		
		List<TableMap> listTM = buscarLctosEntrada(dataIni, dataFin);
		if(Utils.isEmpty(listTM)) return null;
			
		long chave = 1l;
		for(TableMap tm : listTM) {
			tm.put("chave", chave++);
			zerarValores(tm);
		}
		
		//Sub 1: Demonstrativo por estado
		List<TableMap> listTMDemonstrativoPorEstado = buscarLctosEntradaGroupByUF(dataIni, dataFin);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(listTMDemonstrativoPorEstado);
		params.put("sgt_livroEntradasP1P1Ar2s1DS", dataSetSub1);
		
		//Sub 2: Resumo por CFOP
		List<TableMap> listTMResumoCFOP = buscarLctosEntradaGroupByCFOP(dataIni, dataFin);
		TableMapDataSource dataSetSub2 = new TableMapDataSource(listTMResumoCFOP);
		params.put("sgt_livroEntradasP1P1Ar2s2DS", dataSetSub2);
		
		//Sub 3: Resumo por CFOP e alíquota de ICMS
		List<TableMap> listTMResumoCFOPAliqICMS = buscarLctosEntradaGroupByCFOPAndAliquotaICMS(dataIni, dataFin);
		TableMapDataSource dataSetSub3 = new TableMapDataSource(listTMResumoCFOPAliqICMS);
		params.put("sgt_livroEntradasP1P1Ar2s3DS", dataSetSub3);
		
		return listTM;
	}
	
	private void zerarValores(TableMap tm) {
		LocalDate eaa01cancData = tm.getDate("eaa01cancData");
		if(eaa01cancData == null) return;
		
		tm.put("tx_icms", BigDecimal.ZERO);
		tm.put("vlr_contabil", BigDecimal.ZERO);
		tm.put("bc_icms", BigDecimal.ZERO);
		tm.put("icms", BigDecimal.ZERO);
		tm.put("isentas_icms", BigDecimal.ZERO);
		tm.put("outras_icms", BigDecimal.ZERO);
		tm.put("bc_ipi", BigDecimal.ZERO);
		tm.put("ipi", BigDecimal.ZERO);
		tm.put("isentas_ipi", BigDecimal.ZERO);
		tm.put("outras_ipi", BigDecimal.ZERO);
		tm.put("vlr_gia", BigDecimal.ZERO);
	}
	
	private List<TableMap> buscarLctosEntrada(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		if(obs != null)select.append(", jGet(eaa01json." + obs + ") As obs ");
		select.append(", jGet(eaa0103json." + aliqIcms + ")::numeric As tx_icms ");
		select.append(", SUM(eaa0103totDoc) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcms + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcms + ")::numeric) As outras_icms ");
		select.append(", SUM(jGet(eaa0103json." + bcIpi + ")::numeric) As bc_ipi ");
		select.append(", SUM(jGet(eaa0103json." + ipi + ")::numeric) As ipi ");
		select.append(", SUM(jGet(eaa0103json." + isentasIpi + ")::numeric) As isentas_ipi ");
		select.append(", SUM(jGet(eaa0103json." + outrasIpi + ")::numeric) As outras_ipi ");
		if(vlrGia != null)select.append(", SUM(jGet(eaa0103json." + vlrGia + ")::numeric) As vlr_gia ");
		
		return getSession().createQuery(" SELECT eaa01id, aaj15codigo, eaa01esData, abb01num, abb01serie, abb01data, eaa01cancData, aah01na, ",
										" eaa0102codigo, eaa0102nome, eaa0102ie, eaa0102ni, aag02uf ",
									    select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Abb01 ON eaa01central = abb01id ",
										" INNER JOIN Aah01 ON abb01tipo = aah01id ",
										" INNER JOIN Aaj15 ON eaa0103cfop = aaj15id ",
										" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id ",
										" LEFT JOIN Eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 ",
										" LEFT JOIN Aag0201 ON eaa0101municipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroFisc = :livroFisc",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ", 
										" AND eaa0103retInd <= 2 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY eaa01id, aaj15codigo, eaa01esData, abb01num, abb01serie, abb01data, eaa01cancData, aah01na, ",
										" eaa0102codigo, eaa0102nome, eaa0102ie, eaa0102ni, aag02uf, ",
										(obs == null ? "" : " jGet(eaa01json." + obs + "), "),
										" jGet(eaa0103json." + aliqIcms + ")::numeric ",
										" ORDER BY eaa01esData, abb01num, aaj15codigo")
						   .setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroFisc", Eaa01.SIM,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarLctosEntradaGroupByUF(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(", SUM(eaa0103totDoc) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcms + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcms + ")::numeric) As outras_icms ");
		select.append(", SUM(jGet(eaa0103json." + bcIpi + ")::numeric) As bc_ipi ");
		select.append(", SUM(jGet(eaa0103json." + ipi + ")::numeric) As ipi ");
		select.append(", SUM(jGet(eaa0103json." + isentasIpi + ")::numeric) As isentas_ipi ");
		select.append(", SUM(jGet(eaa0103json." + outrasIpi + ")::numeric) As outras_ipi ");
		select.append(", SUM(jGet(eaa0103json." + icmsST + ")::numeric) As icms_st ");
		
		return getSession().createQuery(" SELECT aag02uf ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" LEFT JOIN Eaa0101 ON eaa0101doc = eaa01id AND eaa0101principal = 1 ",
										" LEFT JOIN Aag0201 ON eaa0101municipio = aag0201id ",
										" LEFT JOIN Aag02 ON aag0201uf = aag02id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroFisc = :livroFisc",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ",
										" AND eaa0103retInd <= 2 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY aag02uf ",
										" ORDER BY aag02uf")
							.setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroFisc", Eaa01.SIM,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
							.getListTableMap();
	}
	
	private List<TableMap> buscarLctosEntradaGroupByCFOP(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(", SUM(eaa0103totDoc) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcms + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcms + ")::numeric) As outras_icms ");
		select.append(", SUM(jGet(eaa0103json." + bcIpi + ")::numeric) As bc_ipi ");
		select.append(", SUM(jGet(eaa0103json." + ipi + ")::numeric) As ipi ");
		select.append(", SUM(jGet(eaa0103json." + isentasIpi + ")::numeric) As isentas_ipi ");
		select.append(", SUM(jGet(eaa0103json." + outrasIpi + ")::numeric) As outras_ipi ");
		
		return getSession().createQuery(" SELECT SUBSTR(aaj15codigo, 1, 1) as grupo, aaj15codigo, aaj15descr ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Aaj15 ON eaa0103cfop = aaj15id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroFisc = :livroFisc",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ",
										" AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') ",
										" AND eaa0103retInd <= 2 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr ",
										" ORDER BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr")
							.setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroFisc", Eaa01.SIM,
										  "dataIni", dataIni,
										  "dataFin", dataFin)
							.getListTableMap();
	}
	
	private List<TableMap> buscarLctosEntradaGroupByCFOPAndAliquotaICMS(LocalDate dataIni, LocalDate dataFin) {
		StringBuilder select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqIcms + ")::numeric As tx_icms ");
		select.append(", SUM(eaa0103totDoc) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcms + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcms + ")::numeric) As outras_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIpi + ")::numeric) As outras_ipi ");
		
		return getSession().createQuery(" SELECT SUBSTR(aaj15codigo, 1, 1) as grupo, aaj15codigo, aaj15descr ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Aaj15 ON eaa0103cfop = aaj15id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroFisc = :livroFisc",
										" AND eaa01esData BETWEEN :dataIni AND :dataFin ",
										" AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') ",
										" AND eaa0103retInd <= 2 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr, ",
										" jGet(eaa0103json." + aliqIcms + ")::numeric ",
										" ORDER BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr, ",
										" jGet(eaa0103json." + aliqIcms + ")::numeric ")
							.setParameters("esMov", Eaa01.ESMOV_ENTRADA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroFisc", Eaa01.SIM,
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
		LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
		filtrosDefault.put("dataInicial", datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		filtrosDefault.put("dataFinal", datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

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
				edd10.setEdd10livro(Edd10.LIVRO_ENTRADAS_P1P1A);
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
						   .setParameters("edd10livro", Edd10.LIVRO_ENTRADAS_P1P1A,
								   	      "edd10num", edd10num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Edd10 obterUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
				.addWhere(Criterions.eq("edd10livro", Edd10.LIVRO_ENTRADAS_P1P1A))
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
				  .setParameter("edd10livro", Edd10.LIVRO_ENTRADAS_P1P1A)
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
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIExpdnJvIEVudHJhZGFzIFAxL1AxQSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==