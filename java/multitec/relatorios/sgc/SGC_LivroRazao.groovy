package multitec.relatorios.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.Query;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.StringUtils;
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.dto.sgc.ConfigGrausCodigoContaContabil
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01;
import sam.model.entities.ab.Abc10;
import sam.model.entities.eb.Eba40;
import sam.model.entities.eb.Ebb02;
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SGC_LivroRazao extends RelatorioBase {
	private final int LIVRO_RAZAO = 0;
	private final int TERMO_ABERTURA = 1;
	private final int SALTO_LINHAS = 0; 
	private final int ORIENTACAO_RETRATO = 0;
	
	@Override
	public String getNomeTarefa() {
		return "SGC - Livro Razão";
	}

	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		int imprimir = getInteger("imprimir");
		int saltarPor = getInteger("saltarPor");
		int orientacao = getInteger("orientacao");
		Integer valSaltoLinha = getInteger("saltarPagina");
		LocalDate[] datas = new LocalDate[2];
		datas[0] = DateUtils.parseDate(getString("dataInicial"))
		datas[1] = DateUtils.parseDate(getString("dataFinal"))
		boolean isRascunho = get("rascunho");
		
		if(imprimir == LIVRO_RAZAO){
			params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
			params.put("RASCUNHO", isRascunho);
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
			
			String saltoLinha = "";
			if(saltarPor == SALTO_LINHAS) {
				for(int i = 0; i < valSaltoLinha; i++){
					saltoLinha += " \n";  //(alt + 255) com \n para pular linha no relatório
				}
			}
			params.put("SALTO_LINHA", saltoLinha);
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
		
		List<Long> idsContas = getListLong("contas");
		
		List<String> codigosEmpresas = get("codigos");
		if(codigosEmpresas == null || codigosEmpresas.isEmpty())throw new ValidacaoException("Necessário ter os códigos das empresas no plano de contas atualizados e selecionados.");

		List<TableMap> dados = new ArrayList<>();
		if(imprimir == LIVRO_RAZAO) {
			dados = obterLivroRazao(datas[0], datas[1], idsContas, codigosEmpresas, get("ctaComMovimento"));
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		if(dados == null || dados.size() == 0)throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");
		
		JasperReport report = carregarArquivoRelatorio(imprimir == LIVRO_RAZAO ? orientacao == ORIENTACAO_RETRATO ? "SGC_LivroRazao_R2" : "SGC_LivroRazao_R3": "SGC_LivroRazao_R1");
		
		if(saltarPor != SALTO_LINHAS) {
			if(report.getGroups() != null && report.getGroups().length >= 0){
				String nomeGrupo = "grupoCtaContabil";
				for(int i = 0; i < report.getGroups().length; i++ ){
					if(report.getGroups()[i].getName().equalsIgnoreCase(nomeGrupo)){
						report.getGroups()[i].setStartNewPage(true);
					}
				}
			}
		}
		
		JasperPrint print = processarRelatorio(report, dados);

		if(print == null || print.getPages().size() == 0)throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");
		
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
		configurarCodigosEmprAtualizadas(filtrosDefault);
		configurarMsgAtualizarCtas(filtrosDefault);
		filtrosDefault.put("rascunho", true);
		filtrosDefault.put("ctaComMovimento", false);
		filtrosDefault.put("saltarPor", "0");
		filtrosDefault.put("saltarPagina", 0);
		filtrosDefault.put("orientacao", "0");
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
		
		filtrosDefault.put("imprimir", 0);
	}
	
	private void configurarLivroEPagina(Map<String, Object> filtrosDefault) {
		Integer livro = null;
		Integer pagina = null;
		
		Eba40 eba40 = obterUltimoLivro();
		if(eba40 != null) {
			livro = eba40.getEba40num();
			pagina = eba40.getEba40ultPag();
		} else {
			livro = obterProximoNum();
			pagina = 1;
		}
		
		filtrosDefault.put("livro", livro);
		filtrosDefault.put("pagina", pagina);
	}
	
	private void configurarDataInicial(Map<String, Object> filtrosDefault) {
		LocalDate data = MDate.date();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
		filtrosDefault.put("dataInicial", datas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		filtrosDefault.put("dataFinal", datas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	}
	
	private void configurarCodigosEmprAtualizadas(Map<String, Object> filtrosDefault) {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "EMPRATUALIZADAS"))
				.addWhere(Criterions.eq("aba01aplic", "EB"))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.setMaxResults(1)
				.get();
		String emprAtualizadas = null;
		if(aba01 != null) {
			try {
				emprAtualizadas = aba01.getAba01conteudo();
			} catch (Exception e) {
				//ignorar
			}
		}

		List<TableMap> mapCodigos = new ArrayList<>();
		List<String> codigos = new ArrayList<>();
		if(emprAtualizadas != null && !emprAtualizadas.isEmpty()) {
			StringTokenizer strToken = new StringTokenizer(emprAtualizadas, ";");
			while(strToken.hasMoreTokens()){
				String codigo = strToken.nextToken();
				TableMap tm = new TableMap();
				tm.put("codigo", codigo);
				mapCodigos.add(tm);

				codigos.add(codigo);
			}
		}

		filtrosDefault.put("mapCodigos", mapCodigos);

		filtrosDefault.put("codigos", codigos);
	}
	
	private List<TableMap> obterLivroRazao(LocalDate dataIni, LocalDate dataFin, List<Long> idsContas, List<String> quintoGrau, boolean ctaComMov){
		ConfigGrausCodigoContaContabil configGrausCodigoContaContabil = ConfigGrausCodigoContaContabil.obterGrausDigitosEstruturaCodigos(obterEstruturaContas());
		Integer grauEmpresa = obterGrauEmpresa();
		if(grauEmpresa == null)throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro Abc10_GrauEmpresa");
		configGrausCodigoContaContabil.setGrauEmpresa(grauEmpresa);
		int qtdDig = configGrausCodigoContaContabil.getQtDig()[configGrausCodigoContaContabil.getQtGrau()-1];
		
		List<Long> abc10ids = obterIdsDasContas(idsContas, quintoGrau, configGrausCodigoContaContabil);
		
		//Busca o saldo anterior de cada conta e armazena no Map
		Map<Long, BigDecimal> mapSaldos = buscarSaldoAnteriorDasContas(dataIni, abc10ids);
		
		//Soma à débito
		List<TableMap> lctosDebito = buscarLctosDebitoPeloPeriodoIniFinEContas(dataIni, dataFin, abc10ids);
		
		//Soma à crédito
		List<TableMap> lctosCredito = buscarLctosCreditoPeloPeriodoIniFinEContas(dataIni, dataFin, abc10ids);
		
		//Map de Conta e Lcto (o lcto é um TableMap com: abc10codigo, abc10nome, abc10reduzido, ebb05historico, ebb05data, cpartida, debito, credito, saldoAnterior, saldoAtual)
		Map<String, TableMap> mapRelatorio = new TreeMap<>();
		comporMapeamento(mapRelatorio, mapSaldos, lctosDebito, lctosCredito);
		
		//Se a opção de emitir somente conta com movimento, emite somente as contas com lançamentos ou que o saldo anterior seja diferente de zero
		boolean ctaComMovimento = get("ctaComMovimento");
		if(!ctaComMovimento) {
			List<Abc10> abc10s = getSession().createCriteria(Abc10.class, "abc10id, abc10codigo, abc10nome, abc10reduzido", Criterions.in("abc10id", mapSaldos.keySet())).getList(ColumnType.ENTITY);
			for(Abc10 abc10 : abc10s) {
				List<String> codigos = mapRelatorio.entrySet().stream().map({ entry -> entry.getKey().substring(0, qtdDig)}).distinct().collect(Collectors.toList());
				if(codigos.contains(abc10.getAbc10codigo())) continue;
				
				if(mapSaldos.get(abc10.getAbc10id()).compareTo(BigDecimal.ZERO) == 0) continue;
					
				String key = abc10.getAbc10codigo();
				
				TableMap row = new TableMap();
				row.put("abc10codigo", abc10.getAbc10codigo());
				row.put("abc10nome", abc10.getAbc10nome());
				row.put("abc10reduzido", abc10.getAbc10reduzido());
				row.put("saldoAnterior", mapSaldos.get(abc10.getAbc10id()));
				
				mapRelatorio.put(key, row);
			}
		}
		
		List<TableMap> relatorio = new ArrayList<>();
		if(mapRelatorio.size() > 0) {

			BigDecimal somaSdoAtual = BigDecimal.ZERO;
			Set<String> setKeyMap = new HashSet<String>();
			for(String key : mapRelatorio.keySet()) {
				
				BigDecimal saldoAnterior = mapRelatorio.get(key).get("saldoAnterior") != null ? DecimalUtils.create(mapRelatorio.get(key).get("saldoAnterior")).get() : BigDecimal.ZERO;
				BigDecimal debito = mapRelatorio.get(key).get("debito") != null ? DecimalUtils.create(mapRelatorio.get(key).get("debito")).get() : BigDecimal.ZERO;
				BigDecimal credito = mapRelatorio.get(key).get("credito") != null ? DecimalUtils.create(mapRelatorio.get(key).get("credito")).get() : BigDecimal.ZERO;
				
				BigDecimal saldoAtual;
				if(!setKeyMap.contains(key.substring(0, qtdDig))) {
					saldoAtual = saldoAnterior.add(debito).subtract(credito);
					
					setKeyMap.add(key.substring(0, qtdDig));
				}else {
					saldoAtual = somaSdoAtual.add(debito).subtract(credito);
				}
				
				TableMap row = mapRelatorio.get(key);
				row.put("saldoAtual", saldoAtual);
				
				somaSdoAtual = saldoAtual;
				
				relatorio.add(mapRelatorio.get(key));
			}
		}
				
		return relatorio;
	}
	
	private Map<Long, BigDecimal> buscarSaldoAnteriorDasContas(LocalDate dataIni, List<Long> abc10ids){
		Map<Long, BigDecimal> mapSaldos = new TreeMap<>();
		for(Long abc10id : abc10ids) {
			BigDecimal saldoAnterior = buscarSdoAnteriorDaCta(dataIni.getMonthValue(), dataIni.getYear(), abc10id);
			
			if(dataIni.getDayOfMonth() > 1) {
				LocalDate dataInicial = LocalDate.of(dataIni.getYear(), dataIni.getMonthValue(), 1);
				LocalDate dataFinal = dataIni.minus(1, ChronoUnit.DAYS); 
				
				BigDecimal somaLctosDeb = buscarSomaLctosADebito(abc10id, dataInicial, dataFinal);
				BigDecimal somaLctosCred = buscarSomaLctosACredito(abc10id, dataInicial, dataFinal);
				
				saldoAnterior = saldoAnterior.add(somaLctosDeb).subtract(somaLctosCred);
			}
			
			mapSaldos.put(abc10id, saldoAnterior);
		}
		
		return mapSaldos;
	}
	
	private Map<String, TableMap> comporMapeamento(Map<String, TableMap> mapRelatorio, Map<Long, BigDecimal> mapSaldos, List<TableMap> lctosDebito, List<TableMap> lctosCredito) {
		lctosDebito.forEach({ deb ->
			TableMap row = new TableMap();
			row.put("abc10codigo", deb.get("abc10codigoDeb"));
			row.put("abc10nome", deb.get("abc10nomeDeb"));
			row.put("abc10reduzido", deb.get("abc10reduzidoDeb"));
			row.put("ebb05data", deb.get("ebb05data"));
			row.put("ebb05historico", deb.get("ebb05historico"));
			row.put("cpartida", deb.get("cpartida"));
			row.put("debito", deb.get("ebb05valor"));
			row.put("saldoAnterior", mapSaldos.get(deb.get("abc10idDeb")));
			
			String key = StringUtils.concat(deb.get("abc10codigoDeb"), "-", deb.get("ebb05data"), "-", deb.get("ebb05id"));
			
			mapRelatorio.put(key, row);
		});
		
		lctosCredito.forEach({ cred ->
			TableMap row = new TableMap();
			row.put("abc10codigo", cred.get("abc10codigoCred"));
			row.put("abc10nome", cred.get("abc10nomeCred"));
			row.put("abc10reduzido", cred.get("abc10reduzidoCred"));
			row.put("ebb05data", cred.get("ebb05data"));
			row.put("ebb05historico", cred.get("ebb05historico"));
			row.put("cpartida", cred.get("cpartida"));
			row.put("credito", cred.get("ebb05valor"));
			row.put("saldoAnterior", mapSaldos.get(cred.get("abc10idCred")));
			
			String key = StringUtils.concat(cred.get("abc10codigoCred"), "-", cred.get("ebb05data"), "-", cred.get("ebb05id"));
			
			mapRelatorio.put(key, row);
		});
		
		return mapRelatorio;
	} 
	
	private void comporDadosDoLivroParaGravar(Integer numPaginaRelatorio) {
		boolean isRascunho = get("rascunho");
		int imprimir = getInteger("imprimir");
		
		if(!isRascunho) {
			boolean isTermoEncerramento = false;
			
			Integer numeroLivro = getInteger("livro");
			Integer numeroPaginas = null;
			if(imprimir == LIVRO_RAZAO) {
				numeroPaginas = getInteger("pagina") + numPaginaRelatorio;
			} else if (!(imprimir == TERMO_ABERTURA)){
				numeroLivro = getInteger("livro") + 1;
				numeroPaginas = 1;
				isTermoEncerramento = true;
			}
			
			gravarDadosDoLivro(numeroLivro, numeroPaginas, isTermoEncerramento);
		}
	}
	
	private void gravarDadosDoLivro(Integer numeroLivro, Integer numeroPaginas, boolean isTermoEncerramento) {
		try {
			validacoesDoLivro(numeroLivro);
			
			Eba40 eba40 = obterUltimoLivro() != null ? obterUltimoLivro() : new Eba40();
			if(eba40.getEba40id() == null) {
				eba40.setEba40livro(Eba40.LIVRO_RAZAO);
				eba40.setEba40num(numeroLivro);
				getSamWhere().setDefaultValues(eba40);
			}
			
			eba40.setEba40ultPag(numeroPaginas);
			eba40.setEba40termos(isTermoEncerramento ? 1 : 0);
			
			getSession().persist(eba40);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void validacoesDoLivro(Integer eba40num) throws Exception {
		Eba40 ultimoLivro = obterUltimoLivro();
		if(ultimoLivro != null) {
			if(!ultimoLivro.getEba40num().equals(eba40num)) {
				throw new ValidacaoException("Existe livro em aberto. Não será possivel gravar o número informado.");
			}
		}
		
		Eba40 eba40existe = buscarLivroPeloNum(eba40num);
		if(eba40existe != null && eba40existe.getEba40termos() == Eba40.SIM) {
			throw new ValidacaoException("O livro informado consta como encerrado.");
		}
	}
	
	private Eba40 buscarLivroPeloNum(Integer eba40num) {
		return getSession().createQuery(" SELECT eba40id, eba40termos " +
										"FROM Eba40 WHERE eba40livro = :eba40livro AND eba40num = :eba40num" + 
										getSamWhere().getWherePadrao("AND", Eba40.class))
						   .setParameters("eba40livro", Eba40.LIVRO_RAZAO,
								   	      "eba40num", eba40num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Eba40 obterUltimoLivro() {
		return getSession().createCriteria(Eba40.class)
				.addWhere(Criterions.eq("eba40livro", Eba40.LIVRO_RAZAO))
				.addWhere(Criterions.eq("eba40termos", Eba40.NAO))
				.addWhere(getSamWhere().getCritPadrao(Eba40.class))
				.setOrder("eba40num DESC")
				.setMaxResults(1)
				.get();
	}
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery(
									"SELECT MAX(eba40num) FROM Eba40 WHERE eba40livro = :eba40livro" + 
									getSamWhere().getWherePadrao("AND", Eba40.class))
				  .setParameter("eba40livro", Eba40.LIVRO_RAZAO)
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
	
	private Integer obterGrauEmpresa() {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "GRAUEMPRESA"))
				.addWhere(Criterions.eq("aba01aplic", "ABC10"))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
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
	
	private BigDecimal buscarSdoAnteriorDaCta(Integer mes, Integer ano, Long abc10id) {
		BigDecimal saldo =  getSession().createQuery(" SELECT ebb02saldo FROM Ebb02",
													 " WHERE ebb02cta = :abc10id AND ",
													 Fields.numMeses("ebb02mes", "ebb02ano"), " < :numMeses ",
													 getSamWhere().getWherePadrao("AND", Ebb02.class),
													 " ORDER BY ebb02ano DESC, ebb02mes DESC ")
								  	   .setParameters("numMeses", Criterions.valNumMeses(mes, ano),
								  			   		  "abc10id", abc10id)
								  	   .setMaxResult(1)
								  	   .getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	private String obterEstruturaContas() {
		Aba01 aba01 = getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", "ESTRCODCONTA"))
				.addWhere(Criterions.eq("aba01aplic", "ABC10"))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.setMaxResults(1)
				.get();
		String estruturaContas = null;
		if(aba01 != null) {
			estruturaContas = aba01.getAba01conteudo();
		}
		return estruturaContas;
	}
	
	private List<Long> obterIdsDasContas(List<Long> idsContas, List<String> codigosEmpresasPlano, ConfigGrausCodigoContaContabil configGrausCodigoContaContabil) {
		int qtdDigAnteriorGrauEmpresa = configGrausCodigoContaContabil.getQtdDigAnteriorGrauEmpresa();
		int digitosGrauEmpresa = configGrausCodigoContaContabil.getDigitosGrauEmpresa();
		int qtdDig = configGrausCodigoContaContabil.getQtDig()[configGrausCodigoContaContabil.getQtGrau()-1];
		
		String strContas = idsContas != null && !idsContas.isEmpty() ? " abc10id IN (:idsContas) AND " : "";
		
		Query query = session.createQuery(" SELECT abc10id",
										  " FROM Abc10",
										  " WHERE ", strContas, 
										  Fields.length("abc10codigo"), " = ", qtdDig,
										  " AND ", Fields.substring("abc10codigo", qtdDigAnteriorGrauEmpresa+1, digitosGrauEmpresa), " IN (:codigosEmpresaPlano) ",
										  getSamWhere().getWhereGc("AND", Abc10.class))
							 .setParameter("codigosEmpresaPlano", codigosEmpresasPlano);
		
		if(idsContas != null && !idsContas.isEmpty()) query.setParameter("idsContas", idsContas);
		
		return query.getList(ColumnType.LONG);
	}
	
	private BigDecimal buscarSomaLctosADebito(Long abc10id, LocalDate ebb05dataIni, LocalDate ebb05dataFin){
		BigDecimal saldo =  getSession().createQuery(" SELECT SUM(ebb05valor) AS valor",
													 " FROM Ebb05",
													 " INNER JOIN Abc10 ON abc10id = ebb05deb",
													 " WHERE ebb05deb = :abc10id",
													 " AND ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin",
													 getSamWhere().getWherePadrao("AND", Ebb05.class))
				.setParameters("ebb05dataIni", ebb05dataIni,
							   "ebb05dataFin", ebb05dataFin,
							   "abc10id", abc10id)
				.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	private BigDecimal buscarSomaLctosACredito(Long abc10id, LocalDate ebb05dataIni, LocalDate ebb05dataFin){
		BigDecimal saldo = getSession().createQuery(" SELECT SUM(ebb05valor) * -1 AS valor",
													" FROM Ebb05",
													" INNER JOIN Abc10 ON abc10id = ebb05cred",
													" WHERE ebb05cred = :abc10id",
													" AND ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin",
													getSamWhere().getWherePadrao("AND", Ebb05.class))
				.setParameters("ebb05dataIni", ebb05dataIni,
   					   	       "ebb05dataFin", ebb05dataFin,
						   	   "abc10id", abc10id)
				.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	private List<TableMap> buscarLctosDebitoPeloPeriodoIniFinEContas(LocalDate ebb05dataIni, LocalDate ebb05dataFin, List<Long> abc10ids){
		return getSession().createQuery(" SELECT ebb05id, ebb05historico, ebb05valor, ebb05data, ebb05num, abc10deb.abc10id AS abc10idDeb, abc10deb.abc10codigo AS abc10codigoDeb, abc10deb.abc10nome AS abc10nomeDeb, abc10deb.abc10reduzido AS abc10reduzidoDeb, cpartida.abc10codigo AS cpartida",
										" FROM Ebb05",
										" INNER JOIN Abc10 AS cpartida ON cpartida.abc10id = ebb05cred",
										" INNER JOIN Abc10 AS abc10deb ON abc10deb.abc10id = ebb05deb",
										" WHERE ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin AND",
										" ebb05deb IN (:abc10ids)",
										getSamWhere().getWherePadrao("AND", Ebb05.class),
										" ORDER BY abc10deb.abc10codigo, ebb05data, ebb05num")
						   .setParameters("ebb05dataIni", ebb05dataIni,
									   	  "ebb05dataFin", ebb05dataFin,
									   	  "abc10ids", abc10ids)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarLctosCreditoPeloPeriodoIniFinEContas(LocalDate ebb05dataIni, LocalDate ebb05dataFin, List<Long> abc10ids){
		return getSession().createQuery(" SELECT ebb05id, ebb05historico, ebb05valor, ebb05data, ebb05num, abc10cred.abc10id AS abc10idCred, abc10cred.abc10codigo AS abc10codigoCred, abc10cred.abc10nome AS abc10nomeCred, abc10cred.abc10reduzido AS abc10reduzidoCred, cpartida.abc10codigo AS cpartida",
										" FROM Ebb05",
										" INNER JOIN Abc10 AS cpartida ON cpartida.abc10id = ebb05deb",
										" INNER JOIN Abc10 AS abc10cred ON abc10cred.abc10id = ebb05cred",
										" WHERE ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin AND",
										" ebb05cred IN (:abc10ids)",
										getSamWhere().getWherePadrao("AND", Ebb05.class),
										" ORDER BY abc10cred.abc10codigo, ebb05data, ebb05num")
						   .setParameters("ebb05dataIni", ebb05dataIni,
									   	  "ebb05dataFin", ebb05dataFin,
									   	  "abc10ids", abc10ids)
						   .getListTableMap();
	}
	
	private Aba01 getParametro(br.com.multitec.utils.dicdados.Parametro param) {
		return getSession().createCriteria(Aba01.class)
				.addWhere(Criterions.eq("aba01param", param.getParam()))
				.addWhere(Criterions.eq("aba01aplic", param.getAplic()))
				.addWhere(getSamWhere().getCritPadrao(Aba01.class))
				.get();
	}
	
	private void configurarMsgAtualizarCtas(Map<String, Object> filtrosDefault) {
		Aba01 aba01_EB_ATUALIZARCTAS = getParametro(Parametros.EB_ATUALIZARCTAS);
		String atualizarCtas = null;
		if(aba01_EB_ATUALIZARCTAS != null && aba01_EB_ATUALIZARCTAS.getString() != null && aba01_EB_ATUALIZARCTAS.getInteger() == 1) {
			atualizarCtas = "É necessário atualizar os saldos das contas contábeis.";
		}
		filtrosDefault.put("atualizarCtas", atualizarCtas);
	}
}
//meta-sis-eyJkZXNjciI6IlNHQyAtIExpdnJvIFJhesOjbyIsInRpcG8iOiJyZWxhdG9yaW8ifQ==