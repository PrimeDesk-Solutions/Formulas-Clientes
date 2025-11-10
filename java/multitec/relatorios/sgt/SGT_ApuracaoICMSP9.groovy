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

class SGT_ApuracaoICMSP9 extends RelatorioBase {
	private final int LIVRO = 0;
	private final int TERMO_ABERTURA = 1;
	private final int TERMO_ENCERRAMENTO = 2;
	
	private final int ICMS = 0;
	private final int ICMS_ST = 1;
	
	private String txIcms;
	private String txIcmsSt;
	private String bcIcms;
	private String bcIcmsSt;
	private String icms;
	private String icmsSt;
	private String isentasIcms;
	private String isentasIcmsSt;
	private String outrasIcms;
	private String outrasIcmsSt;
	
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
		return "SGT - Apuração ICMS P9";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		configurarDadosResponsavel(filtrosDefault);
		configurarLivroEPagina(filtrosDefault);
		filtrosDefault.put("referencia", DateUtils.formatDate(MDate.date(), "MM/yyyy"));
		filtrosDefault.put("modelo", 0);
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
		int modelo = get("modelo");
		boolean resumo = get("resumo")
		List<Long> idEstados = getListLong("estados");
		Long tipoApur = getLong("apuracao")
		
		params.put("TITULO_RELATORIO", "Registro de Apuração do ICMS");
		if(imprimir == LIVRO){
			params.put("PERIODO", "MÊS/ANO : " + DateUtils.formatDate(referencia, "MM/yyyy"));
			params.put("RASCUNHO", isRascunho);
			params.put("PAGINA_FOLHA", "PÁGINA");
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
			params.put("AAC10RS", variaveis.aac10.getAac10rs());
			params.put("AAC10NI", variaveis.aac10.getAac10ni());
			params.put("AAC10IE", obterIEEmpresa());
			params.put("IS_ICMS", modelo == ICMS);
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
			dados = obterLivro(referencia, modelo, idEstados, tipoApur);
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		JasperReport report = carregarArquivoRelatorio(imprimir == LIVRO ? (modelo == ICMS ? "SGT_ApuracaoICMSP9_R2" : "SGT_ApuracaoICMSP9_R3") : "SGT_ApuracaoICMSP9_R1");
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
				edd10.setEdd10livro(Edd10.LIVRO_APURACAO_ICMS_P9);
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
	
	//tipo 0-ICMS 1-ICMS ST
	private List<TableMap> obterLivro(LocalDate referencia, int tipo, List<Long> idEstados, Long tipoApur) {
		List<TableMap> listTM = new ArrayList();
		
		if (tipo == 0) { 		//ICMS
			listTM = buscarDadosLivroICMS(referencia.monthValue, referencia.year, tipoApur);
		} else if(tipo == 1) { 	//ICMS ST
			listTM = buscarDadosLivroICMSST(referencia.monthValue, referencia.year, tipoApur, idEstados);
		}
		
		if(Utils.isEmpty(listTM)) return null;
		
		return listTM;
	}
	
	private List<TableMap> buscarDadosLivroICMS(int mes, int ano, Long tipoApur) {
		params.put("sgt_apuracaoIcmsP9r2s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S1"));
		params.put("sgt_apuracaoIcmsP9r2s2Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S2"));
		params.put("sgt_apuracaoIcmsP9r2s3Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3"));
		params.put("sgt_apuracaoIcmsP9r2s3s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3S1"));
		params.put("sgt_apuracaoIcmsP9r2s3s2Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3S2"));
		params.put("sgt_apuracaoIcmsP9r2s3s3Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3S3"));
		params.put("sgt_apuracaoIcmsP9r2s3s4Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3S4"));
		params.put("sgt_apuracaoIcmsP9r2s3s5Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S3S5"));
		params.put("sgt_apuracaoIcmsP9r2s4Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S4"));
		params.put("sgt_apuracaoIcmsP9r2s4s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R2S4S1"));
		
		List<TableMap> lctosEntrada = buscarLctosGroupByCFOP(mes, ano, true);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(lctosEntrada);
		params.put("sgt_apuracaoIcmsP9r2s1DS", dataSetSub1);
		
		List<TableMap> lctosSaida = buscarLctosGroupByCFOP(mes, ano, false);
		TableMapDataSource dataSetSub2 = new TableMapDataSource(lctosSaida);
		params.put("sgt_apuracaoIcmsP9r2s2DS", dataSetSub2);
		
		List<TableMap> apuracoes = buscarApuracaoPelaUK(mes, ano, tipoApur);
		for (int i = 0; i < apuracoes.size(); i++) {
			TableMap apuracao = apuracoes.get(i);
			TableMap edb01json = apuracao.getTableMap("edb01json");
			
			apuracao.put("deb_imp", edb01json.getBigDecimal_Zero(debImp));
			apuracao.put("outros_deb", edb01json.getBigDecimal_Zero(outrosDeb));
			apuracao.put("estorno_cred", edb01json.getBigDecimal_Zero(estCred));
			
			BigDecimal campo004 = edb01json.getBigDecimal_Zero(debImp).add(edb01json.getBigDecimal_Zero(outrosDeb))
								 .add(edb01json.getBigDecimal_Zero(estCred));
			apuracao.put("campo004", campo004);
			
			apuracao.put("cred_imp", edb01json.getBigDecimal_Zero(credImp));
			apuracao.put("outros_cred", edb01json.getBigDecimal_Zero(outrosCred));
			apuracao.put("estorno_deb", edb01json.getBigDecimal_Zero(estDeb));
			
			BigDecimal campo008 = edb01json.getBigDecimal_Zero(credImp).add(edb01json.getBigDecimal_Zero(outrosCred))
								 .add(edb01json.getBigDecimal_Zero(estDeb));
			apuracao.put("campo008", campo008);
			
			apuracao.put("cred_ant", edb01json.getBigDecimal_Zero(credAnt));
			
			BigDecimal campo010 = campo008.add(edb01json.getBigDecimal_Zero(credAnt));
			apuracao.put("campo010", campo010);
			
			BigDecimal campo011 = campo004.compareTo(campo010) > 0 ? campo004.subtract(campo010) : BigDecimal.ZERO;
			apuracao.put("campo011", campo011);
			
			apuracao.put("deducoes", edb01json.getBigDecimal_Zero(deducoes));
			
			BigDecimal campo013 = campo011.subtract(edb01json.getBigDecimal_Zero(deducoes));
			apuracao.put("campo013", campo013);
			
			BigDecimal campo014 = campo004.compareTo(campo010) <= 0 ? campo010.subtract(campo004) : BigDecimal.ZERO;
			apuracao.put("campo014", campo014);
		}
		TableMapDataSource dataSetSub3 = new TableMapDataSource(apuracoes);
		params.put("sgt_apuracaoIcmsP9r2s3DS", dataSetSub3);
		
		Edb01 edb01 = buscarApuracaoPelaUKSemUF(mes, ano, tipoApur);
		List<TableMap> sub3_1 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 1);
		TableMapDataSource dataSetSub3_1 = new TableMapDataSource(sub3_1);
		params.put("sgt_apuracaoIcmsP9r2s3s1DS", dataSetSub3_1);
		
		List<TableMap> sub3_2 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 2);
		TableMapDataSource dataSetSub3_2 = new TableMapDataSource(sub3_2);
		params.put("sgt_apuracaoIcmsP9r2s3s2DS", dataSetSub3_2);
		
		List<TableMap> sub3_3 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 3);
		TableMapDataSource dataSetSub3_3 = new TableMapDataSource(sub3_3);
		params.put("sgt_apuracaoIcmsP9r2s3s3DS", dataSetSub3_3);
		
		List<TableMap> sub3_4 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 4);
		TableMapDataSource dataSetSub3_4 = new TableMapDataSource(sub3_4);
		params.put("sgt_apuracaoIcmsP9r2s3s4DS", dataSetSub3_4);
		
		List<TableMap> sub3_5 = buscarOcorrenciaPeloTipoSubItem(edb01 != null ? edb01.edb01id : null, 5);
		TableMapDataSource dataSetSub3_5 = new TableMapDataSource(sub3_5);
		params.put("sgt_apuracaoIcmsP9r2s3s5DS", dataSetSub3_5);
		
		List<TableMap> apuracoesSub4 = buscarApuracaoPelaUK(mes, ano, tipoApur);
		TableMapDataSource dataSetSub4 = new TableMapDataSource(apuracoesSub4);
		params.put("sgt_apuracaoIcmsP9r2s4DS", dataSetSub4);
		
		List<TableMap> listMapTxIcms = new ArrayList();
		for(int cont = 0; cont < 2; cont++) {
			List<TableMap> tmSub4_1 = buscarLctosGroupByTxIcms(mes, ano, cont == 0); //cont = 0 -> entrada / cont = 1 -> saída
			for(int i=0; i < tmSub4_1.size(); i++) {
				TableMap mapTxIcms = new TableMap();
				mapTxIcms.put(cont == 0 ? "txIcms" : "txIcms", tmSub4_1.get(i).getBigDecimal("txIcms"));
				mapTxIcms.put(cont == 0 ? "bcIcmsE" : "bcIcmsS", tmSub4_1.get(i).getBigDecimal("bcIcms"));
				mapTxIcms.put(cont == 0 ? "icmsE" : "icmsS", tmSub4_1.get(i).getBigDecimal("icms"));
				listMapTxIcms.add(mapTxIcms);
			}
		}
		
		List<TableMap> listMapSub4_1 = new ArrayList();
		for(TableMap tmTxIcms : listMapTxIcms) {
			TableMap mapSub4_1 = new TableMap();
			mapSub4_1.put("txIcms", tmTxIcms.getBigDecimal_Zero("txIcms"));
			mapSub4_1.put("bcIcmsE", tmTxIcms.getBigDecimal_Zero("bcIcmsE"));
			mapSub4_1.put("icmsE", tmTxIcms.getBigDecimal_Zero("icmsE"));
			mapSub4_1.put("bcIcmsS", tmTxIcms.getBigDecimal_Zero("bcIcmsS"));
			mapSub4_1.put("icmsS", tmTxIcms.getBigDecimal_Zero("icmsS"));
			listMapSub4_1.add(mapSub4_1);
		}
		TableMapDataSource dataSetSub4_1 = new TableMapDataSource(listMapSub4_1);
		params.put("sgt_apuracaoIcmsP9r2s4s1DS", dataSetSub4_1);
		
		List<String> listTmPrincipal = new ArrayList();
		TableMap tm = new TableMap();
		tm.put("link", 0);
		listTmPrincipal.add(tm);
		
		return listTmPrincipal; 
	}
	
	private List<TableMap> buscarDadosLivroICMSST(int mes, int ano, Long tipoApur, List<Long> idEstados) {
		params.put("sgt_apuracaoIcmsP9r3s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S1"));
		params.put("sgt_apuracaoIcmsP9r3s2Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S2"));
		params.put("sgt_apuracaoIcmsP9r3s3Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3"));
		params.put("sgt_apuracaoIcmsP9r3s3s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3S1"));
		params.put("sgt_apuracaoIcmsP9r3s3s2Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3S2"));
		params.put("sgt_apuracaoIcmsP9r3s3s3Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3S3"));
		params.put("sgt_apuracaoIcmsP9r3s3s4Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3S4"));
		params.put("sgt_apuracaoIcmsP9r3s3s5Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S3S5"));
		params.put("sgt_apuracaoIcmsP9r3s4Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S4"));
		params.put("sgt_apuracaoIcmsP9r3s4s1Stream", carregarArquivoRelatorio("SGT_ApuracaoICMSP9_R3S4S1"));
		
		
		List<String> listTmPrincipal = buscarUFLctosByMesAnoGroupByCFOPByUF(mes, ano, idEstados);
		
		List<TableMap> lctosEntrada = buscarLctosGroupByCFOPByUF(mes, ano, true, idEstados);
		TableMapDataSource dataSetSub1 = new TableMapDataSource(lctosEntrada);
		params.put("sgt_apuracaoIcmsP9r3s1DS", dataSetSub1);
		
		List<TableMap> lctosSaida = buscarLctosGroupByCFOPByUF(mes, ano, false, idEstados);
		TableMapDataSource dataSetSub2 = new TableMapDataSource(lctosSaida);
		params.put("sgt_apuracaoIcmsP9r3s2DS", dataSetSub2);
		
		List<TableMap> apuracoes = buscarApuracaoPelaUKByUF(mes, ano, tipoApur, idEstados);
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
		params.put("sgt_apuracaoIcmsP9r3s3DS", dataSetSub3);
		
		List<Long> edb01ids = buscarIdApuracoesIcmsST(mes, ano, tipoApur, idEstados);
		List<TableMap> sub3_1 = buscarOcorrenciaPeloTipoSubItem(Utils.isEmpty(edb01ids) ? null : edb01ids, 1, idEstados);
		TableMapDataSource dataSetSub3_1 = new TableMapDataSource(sub3_1);
		params.put("sgt_apuracaoIcmsP9r3s3s1DS", dataSetSub3_1);
		
		List<TableMap> sub3_2 = buscarOcorrenciaPeloTipoSubItem(Utils.isEmpty(edb01ids) ? null : edb01ids, 2, idEstados);
		TableMapDataSource dataSetSub3_2 = new TableMapDataSource(sub3_2);
		params.put("sgt_apuracaoIcmsP9r3s3s2DS", dataSetSub3_2);
		
		List<TableMap> sub3_3 = buscarOcorrenciaPeloTipoSubItem(Utils.isEmpty(edb01ids) ? null : edb01ids, 3, idEstados);
		TableMapDataSource dataSetSub3_3 = new TableMapDataSource(sub3_3);
		params.put("sgt_apuracaoIcmsP9r3s3s3DS", dataSetSub3_3);
		
		List<TableMap> sub3_4 = buscarOcorrenciaPeloTipoSubItem(Utils.isEmpty(edb01ids) ? null : edb01ids, 4, idEstados);
		TableMapDataSource dataSetSub3_4 = new TableMapDataSource(sub3_4);
		params.put("sgt_apuracaoIcmsP9r3s3s4DS", dataSetSub3_4);
		
		List<TableMap> sub3_5 = buscarOcorrenciaPeloTipoSubItem(Utils.isEmpty(edb01ids) ? null : edb01ids, 5, idEstados);
		TableMapDataSource dataSetSub3_5 = new TableMapDataSource(sub3_5);
		params.put("sgt_apuracaoIcmsP9r3s3s5DS", dataSetSub3_5);
		
		List<TableMap> apuracoesSub4 = buscarApuracaoPelaUKByUF(mes, ano, tipoApur, idEstados);
		TableMapDataSource dataSetSub4 = new TableMapDataSource(apuracoesSub4);
		params.put("sgt_apuracaoIcmsP9r3s4DS", dataSetSub4);
		
		List<TableMap> listMapTxIcms = new ArrayList();
		for(int cont = 0; cont < 2; cont++) {
			List<TableMap> tmSub4_1 = buscarLctosGroupByTxIcmsST(mes, ano, cont == 0, idEstados); //cont = 0 -> entrada / cont = 1 -> saída
			for(int i=0; i < tmSub4_1.size(); i++) {
				TableMap mapTxIcms = new TableMap();
				mapTxIcms.put("aag02uf", tmSub4_1.get(i).getString("aag02uf"));
				mapTxIcms.put("txIcmsSt", tmSub4_1.get(i).getBigDecimal_Zero("txicmsst"));
				mapTxIcms.put(cont == 0 ? "bcIcmsE" : "bcIcmsS", tmSub4_1.get(i).getBigDecimal("bcIcms"));
				mapTxIcms.put(cont == 0 ? "icmsE" : "icmsS", tmSub4_1.get(i).getBigDecimal("icms"));
				listMapTxIcms.add(mapTxIcms);
			}
		}
		
		List<TableMap> listMapSub4_1 = new ArrayList();
		for(TableMap tmTxIcms : listMapTxIcms) {
			TableMap mapSub4_1 = new TableMap();
			mapSub4_1.put("aag02uf", tmTxIcms.getString("aag02uf"));
			mapSub4_1.put("txIcmsSt", tmTxIcms.getBigDecimal_Zero("txIcmsSt"));
			mapSub4_1.put("bcIcmsE", tmTxIcms.getBigDecimal_Zero("bcIcmsE"));
			mapSub4_1.put("icmsE", tmTxIcms.getBigDecimal_Zero("icmsE"));
			mapSub4_1.put("bcIcmsS", tmTxIcms.getBigDecimal_Zero("bcIcmsS"));
			mapSub4_1.put("icmsS", tmTxIcms.getBigDecimal_Zero("icmsS"));
			listMapSub4_1.add(mapSub4_1);
		}
		TableMapDataSource dataSetSub4_1 = new TableMapDataSource(listMapSub4_1);
		params.put("sgt_apuracaoIcmsP9r3s4s1DS", dataSetSub4_1);
		
		return listTmPrincipal;
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
		select.append(", SUM(jGet(eaa0103json." + bcIcms + ")::numeric) As bc_icms ");
		select.append(", SUM(jGet(eaa0103json." + icms + ")::numeric) As icms ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcms + ")::numeric) As isentas_icms ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcms + ")::numeric) As outras_icms ");
		
		String whereCFOP = isEntrada ?  " AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : " AND (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') ";
		
		String sql = " SELECT SUBSTR(aaj15codigo, 1, 1) as grupo, aaj15codigo, aaj15descr, SUM(eaa0103totDoc) As vlr_contabil " + select.toString() +
 					 " FROM Eaa0103 " +
  					 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
					 " INNER JOIN Aaj15 ON eaa0103cfop = aaj15id " +
					 " WHERE eaa01esMov = :esMov " +
					 " AND eaa01clasDoc = :clasDoc " +
					 " AND eaa01iLivroFisc = :livroFisc" +
					 " AND " + field + " = :numMeses " + whereCFOP +
					 " AND eaa0103retInd = 0 " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class) +
					 " GROUP BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr " +
					 " ORDER BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr";
		
		return getSession().createQuery(sql).setParameters("esMov", isEntrada ? Eaa01.ESMOV_ENTRADA : Eaa01.ESMOV_SAIDA,
										                   "clasDoc", Eaa01.CLASDOC_SRF,
										                   "livroFisc", Eaa01.SIM,
										                   "numMeses", numMeses)
							                .getListTableMap();
	}
	
	private List<TableMap> buscarLctosGroupByCFOPByUF(int mes, int ano, boolean isEntrada, List<Long> idEstados) {
		String field = Fields.numMeses(Fields.month("eaa01esData").toString(), Fields.year("eaa01esData").toString()).toString();
		int numMeses = (ano * 12) + mes;
		
		StringBuilder select = new StringBuilder("");
		select.append(", SUM(eaa0103totDoc) As vlr_contabil ");
		select.append(", SUM(jGet(eaa0103json." + bcIcmsSt + ")::numeric) As bc_icms_st ");
		select.append(", SUM(jGet(eaa0103json." + icmsSt + ")::numeric) As icms_st ");
		select.append(", SUM(jGet(eaa0103json." + isentasIcmsSt + ")::numeric) As isentas_icms_st ");
		select.append(", SUM(jGet(eaa0103json." + outrasIcmsSt + ")::numeric) As outras_icms_st ");
		
		String whereCFOP = isEntrada ?  " AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : " AND (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') ";
		
		String sql = " SELECT SUBSTR(aaj15codigo, 1, 1) as grupo, aaj15codigo, aaj15descr " +
					 select.toString() +
					 " FROM Eaa0103 " +
					 " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
					 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
					 " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
					 " INNER JOIN Aag02 ON aag02id = aag0201uf " +
					 " INNER JOIN Aaj15 ON eaa0103cfop = aaj15id " +
					 " WHERE eaa01esMov = :esMov " +
					 " AND eaa01clasDoc = :clasDoc " +
					 " AND eaa01iLivroFisc = :livroFisc " +
					 " AND " + field + " = :numMeses " + whereCFOP +
					 " AND eaa0103retInd = 0 " +
					 " AND aag02id IN(:idEstados) " +
					 getSamWhere().getWherePadrao("AND", Eaa01.class) +
					 " GROUP BY SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr, aag02uf " +
					 " ORDER BY aag02uf, SUBSTR(aaj15codigo, 1, 1), aaj15codigo, aaj15descr";
										
		return getSession().createQuery(sql).setParameters("esMov", isEntrada ? Eaa01.ESMOV_ENTRADA : Eaa01.ESMOV_SAIDA,
										                   "clasDoc", Eaa01.CLASDOC_SRF,
										                   "livroFisc", Eaa01.SIM,
										                   "numMeses", numMeses,
										                   "idEstados", idEstados)
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
	
	public List<TableMap> buscarApuracaoPelaUKByUF(Integer mes, Integer ano, Long tipoApur, List<Long> idEstados){
		return getSession().createQuery(" SELECT * FROM Edb01 ",
										" INNER JOIN Aag02 ON aag02id = edb01uf",
										" LEFT JOIN Edd01 ON edd01id = edb01gr",
										" WHERE edb01tipo = :tipoApur ",
										" AND edb01ano = :ano ",
										" AND edb01mes = :mes",
										" AND aag02id IN (:idEstados) ",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "ano", ano,
							   "mes", mes,
							   "idEstados", idEstados)
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
	
	public List<Long> buscarIdApuracoesIcmsST(Integer mes, Integer ano, Long tipoApur, List<Long> idEstados){
		return getSession().createQuery(" SELECT edb01id FROM Edb01 ",
										" INNER JOIN Aag02 ON aag02id = edb01uf",
										" WHERE edb01tipo = :tipoApur ",
										" AND edb01ano = :ano ",
										" AND edb01mes = :mes",
										" AND aag02id IN (:idEstados) ",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "ano", ano,
							   "mes", mes,
							   "idEstados", idEstados)
				.getList(ColumnType.LONG);
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
	
	public List<TableMap> buscarOcorrenciaPeloTipoSubItem(Long tipoApur, int tipoSubItem, List<Long> idEstados){
		String subItem = tipoSubItem == 0 ? "002" : tipoSubItem == 1 ? "003" : tipoSubItem == 2 ? "007" : tipoSubItem == 3 ? "008" : "014";
		return getSession().createQuery(" SELECT edb0101obs, edb0101giaSI, edb0101valor FROM Edb0101 ",
										" INNER JOIN Edb01 ON edb01id = edb0101apur",
										" INNER JOIN Aag02 ON aag02id = edb01uf",
										" WHERE edb0101apur = :tipoApur ",
										" AND SUBSTR(edb0101giaSI, 1, 3) = :subItem",
										" AND aag02id IN (:idEstados) ",
										getSamWhere().getWherePadrao("AND", Edb01.class))
				.setParameters("tipoApur", tipoApur,
							   "subItem", subItem,
							   "idEstados", idEstados)
				.getListTableMap();
	}
	
	public List<TableMap> buscarLctosGroupByTxIcms(Integer mes, Integer ano, boolean isEntrada) {
		String whereCFOP = isEntrada ? " AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : " AND (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') ";
		
		String sql = " SELECT jGet(eaa0103json." + txIcms + ")::numeric AS txIcms, SUM(jGet(eaa0103json." + bcIcms + ")::numeric) AS bcIcms, SUM(jGet(eaa0103json." + icms + ")::numeric) AS icms " + 
      				 " FROM Eaa0103 " +
					 " INNER JOIN Eaa01 ON eaa01id = eaa0103doc" +
					 " INNER JOIN Abb01 ON abb01id = eaa01central" +
					 " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop" +
					 " WHERE (DATE_PART('YEAR', abb01data) * 12 + DATE_PART('MONTH', abb01data)) = :numMeses " +
					 whereCFOP + getSamWhere().getWherePadrao("AND", Eaa01.class) +
					 " GROUP BY jGet(eaa0103json." + txIcms + ")::numeric " +
					 " ORDER BY jGet(eaa0103json." + txIcms + ")::numeric ";
				
		def numMeses = (ano * 12) + mes;
		
		return getSession().createQuery(sql)
							.setParameters("numMeses", numMeses)
							.getListTableMap();
	}
	
	public List<TableMap> buscarLctosGroupByTxIcmsST(Integer mes, Integer ano, boolean isEntrada, List<Long> idEstados) {
		String where = isEntrada ? " AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%') " : " AND (aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') ";
		
		String sql = " SELECT aag02uf, jGet(eaa0103json." + txIcmsSt + ")::numeric AS txIcmsSt, SUM(jGet(eaa0103json." + bcIcms + ")::numeric) AS bcIcms, SUM(jGet(eaa0103json." + icms + ")::numeric) AS icms " + 
 					 " FROM Eaa0103 " +
					 " INNER JOIN Eaa01 ON eaa01id = eaa0103doc" +
					 " INNER JOIN Abb01 ON abb01id = eaa01central" +
					 " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop" +
					 " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
					 " INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
					 " INNER JOIN Aag02 ON aag02id = aag0201uf " +
					 " WHERE (DATE_PART('YEAR', abb01data) * 12 + DATE_PART('MONTH', abb01data)) = :numMeses " +
					 " AND aag02id IN (:idEstados)" +
					 where + getSamWhere().getWherePadrao("AND", Eaa01.class) +
					 " GROUP BY aag02uf, jGet(eaa0103json." + txIcmsSt + ")::numeric " +
					 " ORDER BY aag02uf, jGet(eaa0103json." + txIcmsSt + ")::numeric ";
		
		def numMeses = (ano * 12) + mes;
		
		return getSession().createQuery(sql)
						   .setParameters("numMeses", numMeses,
							              "idEstados", idEstados)
						   .getListTableMap();
	}
	
	private Edd10 obterUltimoLivro() {
		return getSession().createCriteria(Edd10.class)
				           .addWhere(Criterions.eq("edd10livro", Edd10.LIVRO_APURACAO_ICMS_P9))
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
						   .setParameters("edd10livro", Edd10.LIVRO_APURACAO_ICMS_P9,
								   	      "edd10num", edd10num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery("SELECT MAX(edd10num) FROM Edd10 WHERE edd10livro = :edd10livro",
									           getSamWhere().getWherePadrao("AND", Edd10.class))
				  				  .setParameter("edd10livro", Edd10.LIVRO_APURACAO_ICMS_P9)
								  .getUniqueResult(ColumnType.INTEGER);
		return num == null ? 1 : num + 1;
	}
	
	private TableMap obterDadosTermoAberturaOrEncerramento() {
		return getSession().createQuery(" SELECT aac10rs, aac10endereco, aac10numero, aac10complem, aac10bairro, aac10ni, aac1002ie, aac10rjcnumero, aac10rjcdata, aac10nirenumero, aac10niredata, aag0201nome, aag02uf, aac10cep " +
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
	
	private List<String> buscarUFLctosByMesAnoGroupByCFOPByUF(Integer mes, Integer ano, List<Long> idEstados) {
		String where = " AND (aaj15codigo LIKE '1%' OR aaj15codigo LIKE '2%' OR aaj15codigo LIKE '3%' OR aaj15codigo LIKE '5%' OR aaj15codigo LIKE '6%' OR aaj15codigo LIKE '7%') ";
		
		def numMeses = (ano * 12) + mes;
		
		return getSession().createQuery(" SELECT aag02uf" +
										" FROM Eaa0103 "+
										" INNER JOIN Eaa01 ON eaa01id = eaa0103doc",
										" INNER JOIN Abb01 ON abb01id = eaa01central",
										" INNER JOIN Aaj15 ON aaj15id = eaa0103cfop",
										" INNER JOIN Eaa0101 ON eaa0101doc = eaa01id ",
										" INNER JOIN Aag0201 ON aag0201id = eaa0101municipio ",
										" INNER JOIN Aag02 ON aag02id = aag0201uf ",
										" WHERE (DATE_PART('YEAR', abb01data) * 12 + DATE_PART('MONTH', abb01data)) = :numMeses ",
										" AND eaa01cancData IS NULL ",
										" AND aag02id IN (:idEstados) ",
										where,
										getSamWhere().getWherePadrao("AND", Eaa01.class))
							.setParameters("numMeses", numMeses,
										  "idEstados", idEstados)
							.getList(ColumnType.STRING);
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
		selecionarAlinhamento("0040"); //0040 - Apuração LF ICMS
		
		debImp = getCampo("0040", "0", "debImp");
		outrosDeb = getCampo("0040", "0", "outrosDeb");
		estCred = getCampo("0040", "0", "estCred");
		credImp = getCampo("0040", "0", "credImp");
		outrosCred = getCampo("0040", "0", "outrosCred");
		estDeb = getCampo("0040", "0", "estDeb");
		credAnt = getCampo("0040", "0", "credAnt");
		deducoes = getCampo("0040", "0", "deducoes");
		subTotSai = getCampo("0040", "0", "subTotSai");
		subTotEnt = getCampo("0040", "0", "subTotEnt");
		total = getCampo("0040", "0", "total");
		sdoDevedor = getCampo("0040", "0", "sdaDevedor");
		sdoCredor = getCampo("0040", "0", "sdoCredor");
		impRecolher = getCampo("0040", "0", "impRecolher");
	}
	
	private void buscarCamposAlinhamentoEaa0103() {
		selecionarAlinhamento("0082"); //0082 - Resumo de Apuração ICMS P9  
		
		txIcms = getCampo("0082", "0", "txIcms");
		txIcmsSt = getCampo("0082", "0", "txIcmsSt");
		bcIcms = getCampo("0082", "0", "bcIcms");
		bcIcmsSt = getCampo("0082", "0", "bcIcmsSt");
		icms = getCampo("0082", "0", "icms");
		icmsSt = getCampo("0082", "0", "icmsSt");
		isentasIcms = getCampo("0082", "0", "isentasIcms");
		isentasIcmsSt = getCampo("0082", "0", "isentasIcmsSt");
		outrasIcms = getCampo("0082", "0", "outrasIcms");
		outrasIcmsSt = getCampo("0082", "0", "outrasIcmsSt");
	}
}
//meta-sis-eyJkZXNjciI6IlNHVCAtIEFwdXJhw6fDo28gSUNNUyBQOSIsInRpcG8iOiJyZWxhdG9yaW8ifQ==