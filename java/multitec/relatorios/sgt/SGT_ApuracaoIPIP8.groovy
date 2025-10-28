package multitec.relatorios.sgt

import java.time.LocalDate

import org.springframework.http.MediaType

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aag0201
import sam.model.entities.ea.Eaa01
import sam.model.entities.ed.Edb01
import sam.model.entities.ed.Edd10
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource

class SGT_ApuracaoIPIP8 extends RelatorioBase {
	private final int LIVRO = 0;
	private final int TERMO_ABERTURA = 1;
	private final int TERMO_ENCERRAMENTO = 2;
	
	private String vlrContabil;
	private String bcIpi;
	private String ipi;
	private String isentasIpi;
	private String outrasIpi;
	private String credAnt;
	private String credImp;
	private String debImp;
	private String deducoes;
	private String estCred;
	private String estDeb;
	private String impRecolher;
	private String outrosCred;
	private String outrosDeb;
	private String sdoCredor;
	private String sdoDevedor;
	private String subTotEnt;
	private String subTotSai;
	private String total;

	@Override
	public String getNomeTarefa() {
		return "SGT - Apuração IPI P8";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		configurarDadosResponsavel(filtrosDefault);
		configurarLivroEPagina(filtrosDefault);
		filtrosDefault.put("referencia", DateUtils.formatDate(MDate.date(), "MM/yyyy"));
		filtrosDefault.put("imprimir", 0);
		filtrosDefault.put("rascunho", true);
		return Utils.map("filtros", filtrosDefault);
	}


	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10rs());
		
		int imprimir = getInteger("imprimir");
		String ref = getString("referencia");
		LocalDate referencia = MDate.date(); 
		if(!StringUtils.isNullOrEmpty(ref)) referencia = LocalDate.parse("01/"+ref, "dd/MM/yyyy");
		boolean isRascunho = get("rascunho");
		boolean resumo = get("resumo")
		Long tipoApur = getLong("apuracao")
		
		params.put("TITULO_RELATORIO", "Registro de Apuração do IPI");
		if(imprimir == LIVRO){
			params.put("PERIODO", "MÊS/ANO : " + DateUtils.formatDate(referencia, "MM/yyyy"));
			params.put("RASCUNHO", isRascunho);
			params.put("PAGINA_FOLHA", "PÁGINA");
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
			params.put("AAC10RS", variaveis.aac10.getAac10rs());
			params.put("AAC10NI", variaveis.aac10.getAac10ni());
			params.put("AAC10IE", obterIEEmpresa());
		}else {
			params.put("NUMERO_PAGINA", imprimir == TERMO_ABERTURA ? 0 : getInteger("pagina"));
			params.put("PAGINA_FOLHA", "Página :");
			params.put("SUB_TITULO_RELATORIO", imprimir == TERMO_ABERTURA ? "T E R M O    D E    A B E R T U R A" : "T E R M O    D E    E N C E R R A M E N T O");
			params.put("SERVIU_SERVIRA", imprimir == TERMO_ABERTURA ? " e que servirá" : " e que serviu");
			params.put("NUM_PAGINAS", getInteger("pagina"));
			params.put("PAGINA_FINAL", getInteger("pagina") + 1);
			params.put("NUMERO_LIVRO", getInteger("livro"));
			params.put("PAG_FOLHA", "páginas ");
			params.put("DATA_TERMO", getString("dataTermo"));
			params.put("ASSINATURA11", getString("representante1"));
			params.put("ASSINATURA12", getString("representante2"));
			params.put("ASSINATURA13", getString("representante3"));
			params.put("ASSINATURA21", getString("assinatura1"));
			params.put("ASSINATURA22", getString("assinatura2"));
			params.put("ASSINATURA23", getString("assinatura3"));
		}
		
		buscarCamposAlinhamentoICMS();
		
		buscarCamposAlinhamentoEaa0103()
		
		List<TableMap> dados = new ArrayList<>();
		if(imprimir == LIVRO) {
			dados = obterLivro(referencia, tipoApur);
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		JasperReport report = carregarArquivoRelatorio(imprimir == LIVRO ? "SGT_ApuracaoIPIP8_R2" : "SGT_ApuracaoIPIP8_R1");
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
			
			Edd10 edd10 = obterUltimoLivro() != null ? obterUltimoLivro() : new Edd10();
			if(edd10.getEdd10id() == null) {
				edd10.setEdd10livro(Edd10.LIVRO_APURACAO_IPI_P8);
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
	
	private List<TableMap> obterLivro(LocalDate referencia, Long tipoApur) {
		List<TableMap> listTM = new ArrayList();
		
		params.put("sgt_apuracaoIpiP8r2s1Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S1"));
		params.put("sgt_apuracaoIpiP8r2s2Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S2"));
		params.put("sgt_apuracaoIpiP8r2s3Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3"));
		params.put("sgt_apuracaoIpiP8r2s3s1Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3S1"));
		params.put("sgt_apuracaoIpiP8r2s3s2Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3S2"));
		params.put("sgt_apuracaoIpiP8r2s3s3Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3S3"));
		params.put("sgt_apuracaoIpiP8r2s3s4Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3S4"));
		params.put("sgt_apuracaoIpiP8r2s3s5Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S3S5"));
		params.put("sgt_apuracaoIpiP8r2s4Stream", carregarArquivoRelatorio("SGT_ApuracaoIPIP8_R2S4"));
		
		def mes = referencia.monthValue;
		def ano = referencia.year;
		
		List<TableMap> lctosEntrada = buscarLctosGroupByCFOP(mes, ano, true);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(lctosEntrada);
		params.put("sgt_apuracaoIpiP8r2s1DS", dataSetSub1);
		
		List<TableMap> lctosSaida = buscarLctosGroupByCFOP(mes, ano, false);
		TableMapDataSource dataSetSub2 = new TableMapDataSource(lctosSaida);
		params.put("sgt_apuracaoIpiP8r2s2DS", dataSetSub2);
		
		List<TableMap> apuracoes = buscarApuracaoPelaUK(mes, ano, tipoApur);
		for (int i = 0; i < apuracoes.size(); i++) {
			TableMap apuracao = apuracoes.get(i);
			TableMap edb01json = apuracao.getTableMap("edb01json");
			BigDecimal campo004 = edb01json.getBigDecimal_Zero(debImp).add(edb01json.getBigDecimal_Zero(outrosDeb))
								 .add(edb01json.getBigDecimal_Zero(estCred));
			apuracao.put("campo004", campo004);
			
			BigDecimal campo008 = edb01json.getBigDecimal_Zero(credImp).add(edb01json.getBigDecimal_Zero(outrosCred))
								 .add(edb01json.getBigDecimal_Zero(estDeb));
			apuracao.put("campo008", campo008);
			
			BigDecimal campo010 = campo008.add(edb01json.getBigDecimal_Zero(credAnt));
			apuracao.put("campo010", campo010);
			
			BigDecimal campo011 = campo004.compareTo(campo010) > 0 ? campo004.subtract(campo010) : BigDecimal.ZERO;
			apuracao.put("campo011", campo011);
			
			BigDecimal campo013 = campo011.subtract(edb01json.getBigDecimal_Zero(deducoes));
			apuracao.put("campo013", campo013);
			
			BigDecimal campo014 = campo004.compareTo(campo010) <= 0 ? campo010.subtract(campo004) : BigDecimal.ZERO;
			apuracao.put("campo014", campo014);
		}
		TableMapDataSource dataSetSub3 = new TableMapDataSource(apuracoes);
		params.put("sgt_apuracaoIpiP8r2s3DS", dataSetSub3);
		
		Edb01 edb01 = buscarApuracaoPelaUKSemUF(mes, ano, tipoApur);
		List<TableMap> sub3_1 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 1);
		TableMapDataSource dataSetSub3_1 = new TableMapDataSource(sub3_1);
		params.put("sgt_apuracaoIpiP8r2s3s1DS", dataSetSub3_1);
		
		List<TableMap> sub3_2 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 2);
		TableMapDataSource dataSetSub3_2 = new TableMapDataSource(sub3_2);
		params.put("sgt_apuracaoIpiP8r2s3s2DS", dataSetSub3_2);
		
		List<TableMap> sub3_3 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 3);
		TableMapDataSource dataSetSub3_3 = new TableMapDataSource(sub3_3);
		params.put("sgt_apuracaoIpiP8r2s3s3DS", dataSetSub3_3);
		
		List<TableMap> sub3_4 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 4);
		TableMapDataSource dataSetSub3_4 = new TableMapDataSource(sub3_4);
		params.put("sgt_apuracaoIpiP8r2s3s4DS", dataSetSub3_4);
		
		List<TableMap> sub3_5 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 5);
		TableMapDataSource dataSetSub3_5 = new TableMapDataSource(sub3_5);
		params.put("sgt_apuracaoIpiP8r2s3s5DS", dataSetSub3_5);
		
		List<TableMap> apuracoesSub4 = buscarApuracaoPelaUK(mes, ano, tipoApur);
		TableMapDataSource dataSetSub4 = new TableMapDataSource(apuracoesSub4);
		params.put("sgt_apuracaoIpiP8r2s4DS", dataSetSub4);
		
		List<String> listTmPrincipal = new ArrayList();
		TableMap tm = new TableMap();
		tm.put("link", 0);
		listTmPrincipal.add(tm);
		
		return listTmPrincipal;
		
		return listTM;
	}
	
	private void configurarDadosResponsavel(Map<String, Object> filtrosDefault) {
		Aac10 aac10 = obterEmpresaAtiva();
		filtrosDefault.put("representante1", aac10.getAac10rNome());
		filtrosDefault.put("representante2", aac10.getAac10rCpf());
		filtrosDefault.put("assinatura1", aac10.getAac10cNome());
		filtrosDefault.put("assinatura2", aac10.getAac10cCrc());
		
		Aag0201 aag0201 = obterMunicipio(aac10.getAac10municipio().getAag0201id());
		String	municipio = aag0201 != null ? aag0201.getAag0201nome() + ", " : ", ";
		String dataTermo = StringUtils.concat(municipio, DateUtils.formatDate(MDate.date(), "dd 'de' MMMM 'de' yyyy"), ".");
		filtrosDefault.put("dataTermo", dataTermo);
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
	
	private List<TableMap> buscarLctosGroupByCFOP(int mes, int ano, boolean isEntrada) {
		String field = Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()).toString();
		int numMeses = (ano * 12) + mes;
		
		StringBuilder select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + vlrContabil + ")::numeric) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIpi + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + ipi + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIpi + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIpi + ")::numeric) As outras_icms ");
		
		return getSession().createQuery(" SELECT SUBSTR(aaj15codigo, 1, 1) as grupo, aaj15codigo, aaj15descr ",
										select.toString(),
										" FROM Eaa0103 ",
										" INNER JOIN Eaa01 ON eaa0103doc = eaa01id ",
										" INNER JOIN Aaj15 ON eaa0103cfop = aaj15id ",
										" WHERE eaa01esMov = :esMov ",
										" AND eaa01clasDoc = :clasDoc ",
										" AND eaa01iLivroFisc = :livroFisc",
										" AND " + field + " = :numMeses ",
										" AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') ",
										" AND eaa0103retInd = 0 ",
										getSamWhere().getWherePadrao("AND", Eaa01.class),
										" GROUP BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr ",
										" ORDER BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr")
							.setParameters("esMov", isEntrada ? Eaa01.ESMOV_ENTRADA : Eaa01.ESMOV_SAIDA,
										  "clasDoc", Eaa01.CLASDOC_SRF,
										  "livroFisc", Eaa01.SIM,
										  "numMeses", numMeses)
							.getListTableMap();
	}
	
	public List<TableMap> buscarApuracaoPelaUK(Integer mes, Integer ano, Long tipoApur){
		return getSession().createQuery(" SELECT * FROM Edb01 ",
										" LEFT JOIN Edd01 ON edd01id = edb01gr",
										" WHERE edb01tipo = :tipoApur ",
										" AND edb01ano = :ano ",
										" AND edb01mes = :mes",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "ano", ano,
							   "mes", mes)
				.getListTableMap();
	}
	
	public Edb01 buscarApuracaoPelaUKSemUF(Integer mes, Integer ano, Long tipoApur){
		return getSession().createQuery(" SELECT * FROM Edb01 ",
										" LEFT JOIN Edd01 ON edd01id = edb01gr",
										" WHERE edb01tipo = :tipoApur ",
										" AND edb01ano = :ano ",
										" AND edb01mes = :mes",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "ano", ano,
							   "mes", mes)
				.getUniqueResult(ColumnType.ENTITY);
	}
	
	public List<TableMap> buscarOcorrenciaPeloTipoSubItem(Long tipoApur, int tipoSubItem){
		String subItem = tipoSubItem == 0 ? "002" : tipoSubItem == 1 ? "003" : tipoSubItem == 2 ? "007" : tipoSubItem == 3 ? "008" : "014";
		return getSession().createQuery(" SELECT edb0101obs, edb0101giaSI, edb0101valor FROM Edb0101 ",
										" INNER JOIN Edb01 ON edb01id = edb0101apur",
										" WHERE edb0101apur = :tipoApur ",
										" AND SUBSTR(edb0101giaSI, 1, 3) = :subItem",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "subItem", subItem)
				.getListTableMap();
	}
	
	private Edd10 obterUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
				.addWhere(Criterions.eq("edd10livro", Edd10.LIVRO_APURACAO_IPI_P8))
				.addWhere(Criterions.eq("edd10termos", Edd10.NAO))
				.addWhere(getSamWhere().getCritPadrao(Edd10.class))
				.setOrder("edd10num DESC")
				.setMaxResults(1)
				.get();
	}
	
	private Edd10 buscarLivroPeloNum(Integer edd10num) {
		return getSession().createQuery(" SELECT edd10id, edd10termos ",
										" FROM Edd10 WHERE edd10livro = :edd10livro AND edd10num = :edd10num ", 
										getSamWhere().getWherePadrao("AND", Edd10.class))
						   .setParameters("edd10livro", Edd10.LIVRO_APURACAO_IPI_P8,
								   	      "edd10num", edd10num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery(
									"SELECT MAX(edd10num) FROM Edd10 WHERE edd10livro = :edd10livro",
									getSamWhere().getWherePadrao("AND", Edd10.class))
				  .setParameter("edd10livro", Edd10.LIVRO_APURACAO_IPI_P8)
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
	
	private void buscarCamposAlinhamentoICMS() {
		selecionarAlinhamento("0080");
		credAnt = getCampo("0", "credAnt");
		credImp = getCampo("0", "credImp");
		debImp = getCampo("0", "debImp");
		deducoes = getCampo("0", "deducoes");
		estCred = getCampo("0", "estCred");
		estDeb = getCampo("0", "estDeb");
		impRecolher = getCampo("0", "impRecolher");
		outrosCred = getCampo("0", "outrosCred");
		outrosDeb = getCampo("0", "outrosDeb");
		sdoCredor = getCampo("0", "sdoCredor");
		sdoDevedor = getCampo("0", "sdaDevedor");
		subTotEnt = getCampo("0", "subTotEnt");
		subTotSai = getCampo("0", "subTotSai");
		total = getCampo("0", "total");
	}
	
	private void buscarCamposAlinhamentoEaa0103() {
		selecionarAlinhamento("0002");
		vlrContabil = getCampo("0", "vlrContabil");
		bcIpi = getCampo("0", "bcIcms");
		ipi = getCampo("0", "icms");
		isentasIpi = getCampo("0", "isentasImcs");
		outrasIpi = getCampo("0", "outrasIcms");
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIEFwdXJhw6fDo28gSVBJIFA4IiwidGlwbyI6InJlbGF0b3JpbyJ9