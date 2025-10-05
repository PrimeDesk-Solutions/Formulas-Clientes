package Atilatte.relatorios.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

import org.springframework.http.MediaType;

import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import net.sf.jasperreports.engine.JRException
import net.sf.jasperreports.engine.JasperExportManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import sam.core.variaveis.MDate
import sam.dicdados.Parametros
import sam.dto.sgc.ConfigGrausCodigoContaContabil
import sam.model.entities.ab.Aba01
import sam.model.entities.ab.Abc10
import sam.model.entities.eb.Eba40;
import sam.model.entities.eb.Ebb02
import sam.model.entities.eb.Ebb03
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SGC_BalanceteDeVerificacao extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SGC - Balancete de Verificação";
	}
	
	@Override
	public DadosParaDownload executar() {
		List<Long> idsContas = getListLong("contas");
		LocalDate[] datas = new LocalDate[2];
		datas[0] = DateUtils.parseDate("01/" + getString("dataInicial"));
		datas[1] = DateUtils.parseDate("01/" + getString("dataFinal"));
		Integer impressao = getInteger("impressao");

		Integer detalhamento = getInteger("detalhamento");
		boolean movimentacaoMes = get("movimentacaoMes");
		boolean ordemAlfabeticaContas = get("ordemAlfabeticaContas");
		boolean saltarPaginaGrau1Conta = get("saltarPaginaGrau1Conta");
		boolean sdoAntesEncerramento = getInteger("origemValores") == 1;
		
		params.put("TITULO_RELATORIO", "Balancete de Verificação");
		params.put("NOME_EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("NUM_PAGINAS", getInteger("pagina"));
		params.put("IS_SINTETICO", detalhamento.equals(1));
		
		LocalDate dataInicial = datas[0];
		dataInicial = dataInicial.withDayOfMonth(1);
		
		LocalDate dataFinal = datas[1];
		dataFinal = dataFinal.withDayOfMonth(1);
		
		LocalDate dataAnterior = LocalDate.of(dataInicial.getYear(), dataInicial.getMonthValue(), dataInicial.getDayOfMonth());
		dataAnterior = dataAnterior.plusMonths(-1);
		
		params.put("PERIODO_SALDO_ANTERIOR", dataAnterior.format(DateTimeFormatter.ofPattern("MMMM/yyyy")));
		params.put("PERIODO_SALDO_ATUAL", dataFinal.format(DateTimeFormatter.ofPattern("MMMM/yyyy")));
				
		List<String> codigosEmpresas = get("codigos");
		if(codigosEmpresas == null || codigosEmpresas.isEmpty())throw new ValidacaoException("Necessário ter os códigos das empresas no plano de contas atualizados e selecionados.");
		
		if(sdoAntesEncerramento){ //Se for acumulado antes do encerramento a data inicial é a data final, a data inicial vinda do parâmetro é ignorada
			dataInicial = dataFinal;
		}
		
		List<TableMap> dados = obterDadosBalanceteVerificacao(dataInicial, dataFinal, codigosEmpresas, idsContas, ordemAlfabeticaContas, detalhamento, sdoAntesEncerramento);
		
		JasperReport report = carregarArquivoRelatorio(movimentacaoMes ? "SGC_BalanceteDeVerificacao_R2" : "SGC_BalanceteDeVerificacao_R1");
		
		if(saltarPaginaGrau1Conta){
			if(report.getGroups() != null && report.getGroups().length >= 0){
				String nomeGrupo = "grau1";
				for(int i = 0; i < report.getGroups().length; i++ ){
										
					if(report.getGroups()[i].getName().equalsIgnoreCase(nomeGrupo)){
						report.getGroups()[i].setStartNewPage(true);
					}else {
						report.getGroups()[i].setStartNewPage(false);
					}
				}
			}
		}
		
		JasperPrint print = processarRelatorio(report, dados);
		
		Integer numPaginaRelatorio = print.getPages() != null ? print.getPages().size() : 0;
		gravarPaginaDoLivro(numPaginaRelatorio);
		
		byte[] bytes;
		try {
			bytes = JasperExportManager.exportReportToPdf(print);
		} catch (JRException e) {
			throw new RuntimeException("Erro ao gerar o relatório da classe "+ this.getClass().getName(), e);
		}
		//return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);

		if(impressao == 0 && movimentacaoMes ){
			return gerarPDF("SGC_BalanceteDeVerificacao_R2", dados);
		}else if(impressao == 0 && !movimentacaoMes){
			return gerarPDF("SGC_BalanceteDeVerificacao_R1", dados)
		}else if(impressao == 1 && movimentacaoMes){
			return gerarXLSX("SGC_BalanceteDeVerificacao_R2", dados);
		}else{
			return gerarXLSX("SGC_BalanceteDeVerificacao_R1", dados);
		}


	}
	
	private List<TableMap> obterDadosBalanceteVerificacao(LocalDate dataInicial, LocalDate dataFinal, List<String> codigosEmpresas, List<Long> idsContasFiltro, boolean ordemAlfabeticaContas, Integer detalhamento, boolean sdoAntesEncerramento) {
		Integer numMesesInicial = DateUtils.numMeses(dataInicial.getMonthValue(), dataFinal.getYear());
		Integer numMesesFinal = DateUtils.numMeses(dataFinal.getMonthValue(), dataFinal.getYear());
		
		String estruturaCtas = getAcessoAoBanco().buscarParametro("ESTRCODCONTA", "ABC10");
		ConfigGrausCodigoContaContabil configGrausCodigoContaContabil = ConfigGrausCodigoContaContabil.obterGrausDigitosEstruturaCodigos(estruturaCtas);
		Integer grauEmpresa = getAcessoAoBanco().buscarParametro("GRAUEMPRESA", "ABC10");
		if(grauEmpresa == null)throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro Abc10_GrauEmpresa");
		configGrausCodigoContaContabil.setGrauEmpresa(grauEmpresa);
		
		List<Long> idsContas = new ArrayList<>();
		
		List<TableMap> listTMPrincipal = obterContasMenorGrauECodigosEmpresas(idsContasFiltro, codigosEmpresas, ordemAlfabeticaContas, configGrausCodigoContaContabil);
		
		if(listTMPrincipal != null && !listTMPrincipal.isEmpty()) {
			listTMPrincipal.forEach({ tm ->
				tm.put("grau1", "");						//Primeiro grau da conta
				tm.put("saldoanterior", BigDecimal.ZERO);	//Saldo anterior de cada conta
				tm.put("vlrdeb", BigDecimal.ZERO);			//valor de débito
				tm.put("vlrcred", BigDecimal.ZERO);			//valor de crédito
				tm.put("saldomes", BigDecimal.ZERO);		//Saldo do mes de cada conta (somente aparece c o usuário escolher balancete mensal)
				tm.put("saldoatual", BigDecimal.ZERO);		//Saldo atual de cada conta
				tm.put("imprimirGrau", false);				//Indica se o grau será impresso
				
				idsContas.add(tm.getLong("abc10id"));
			});
		}
		
		//Obtendo saldos anteriores
		Map<Long, BigDecimal> mapSaldosAnteriores = new HashMap<>();
		List<TableMap> listTMSaldosAnteriores = buscarSdoAnteriorCtas(dataInicial.getMonthValue(), dataInicial.getYear(), idsContas);
		if(listTMSaldosAnteriores != null && !listTMSaldosAnteriores.isEmpty()) {
			listTMSaldosAnteriores.forEach({ tm ->
				mapSaldosAnteriores.put(tm.getLong("abc10id"), tm.getBigDecimal_Zero("saldoAnterior"));
			});
		}
		listTMSaldosAnteriores = null;
		
		//Obtendo débito e crédito a partir dos saldos
		Map<Long, TableMap> mapDebitosCreditos = new HashMap<>();
		List<TableMap> listTMDebitosCreditos = sdoAntesEncerramento ? buscarDebitoCreditoAntesEncer(numMesesInicial, numMesesFinal, idsContas) :
																	  buscarDebitoCreditoCorrentes(numMesesInicial, numMesesFinal, idsContas);
		for(TableMap tm : listTMDebitosCreditos) {
			mapDebitosCreditos.put(tm.getLong("cta"), tm);
		}
		
		List<TableMap> listSaldos = sdoAntesEncerramento ? buscarSaldosEncerramentoNoPeriodo(dataFinal.getMonthValue(), dataFinal.getYear(), idsContas) :
														   buscarSaldosCorrente(dataFinal.getMonthValue(), dataFinal.getYear(), idsContas);
		for(TableMap tm : listSaldos) {
			TableMap tmAux = mapDebitosCreditos.get(tm.getLong("cta"));
			if(tmAux == null) tmAux = new TableMap();
			tmAux.put("saldo", tm.getBigDecimal_Zero("saldo"));
				
			mapDebitosCreditos.put(tm.getLong("cta"), tmAux);
		}
		
		Set<String> setCtasAteGrauEmpresaComMovimentacao = new HashSet<String>();
		if(listTMPrincipal != null && !listTMPrincipal.isEmpty()) {
			for(TableMap tmPrincipal : listTMPrincipal) {
				Long abc10id = tmPrincipal.getLong("abc10id");
				
				/**
				 * Saldo Anterior
				 */
				BigDecimal saldoAnterior = mapSaldosAnteriores.get(abc10id);
				if(saldoAnterior == null)saldoAnterior = BigDecimal.ZERO;
				tmPrincipal.put("saldoanterior", saldoAnterior);
				
				/**
				 * Débito, Crédito e Saldo do Mês
				 */
				BigDecimal debito = BigDecimal.ZERO;
				BigDecimal credito = BigDecimal.ZERO;
				BigDecimal saldoMes = BigDecimal.ZERO;
				BigDecimal saldoAtual = BigDecimal.ZERO;
				
				TableMap tm = mapDebitosCreditos.get(abc10id);
				if(tm != null) {
					debito = tm.getBigDecimal_Zero("vlrdeb");
					credito = tm.getBigDecimal_Zero("vlrcred");
					saldoMes = debito.subtract(credito);
					
					saldoAtual = tm.getBigDecimal("saldo");
					if(saldoAtual == null) {
						saldoAtual = saldoAnterior.add(saldoMes);
					}
					
				}else {
					saldoAtual = saldoAnterior.add(debito).subtract(credito)
				}
				
				tmPrincipal.put("vlrdeb", debito);
				tmPrincipal.put("vlrcred", credito);
				tmPrincipal.put("saldomes", saldoMes);
				tmPrincipal.put("saldoatual", saldoAtual);
				

				//Se houver saldoAnterior ou débito ou crédito a conta será impressa e seus graus superiores será guardado em um set
				//pois mesmo que a soma das contas cheias dê zero o grupo deve ser exibido
				String codigo = tmPrincipal.getString("abc10codigo");
				if(saldoAnterior.compareTo(BigDecimal.ZERO) != 0 || debito.compareTo(BigDecimal.ZERO) != 0 || credito.compareTo(BigDecimal.ZERO) != 0) {
					tmPrincipal.put("imprimirGrau", true);
					setCtasAteGrauEmpresaComMovimentacao.add(codigo);
					
					if(codigo.length() == configGrausCodigoContaContabil.getQtdDigUltimoGrau()) {
						for(int grau = 0; grau < configGrausCodigoContaContabil.getQtGrau(); grau++) {
							setCtasAteGrauEmpresaComMovimentacao.add(codigo.substring(0, configGrausCodigoContaContabil.getQtDig()[grau]));
						}
					}
				}
				codigo = codigo.length() > 1 ? codigo.substring(0, 1) : codigo;
			}
			
			//Seta nas nas contas de até o grau da empresa se as mesmas devem ser exibidas ou não
			//O set "setCtasAteGrauEmpresaComMovimentacao" guarda todos os graus que têm contas cheias com saldo, debito ou crédito  > 0
			//mesmo que os graus superiores tenham valor zerados devido a soma
			for(TableMap tmPrincipal : listTMPrincipal) {
				String codigo = tmPrincipal.getString("abc10codigo");
				
				tmPrincipal.put("grau1", codigo.substring(0, 1));
						
				if(codigo.length() < configGrausCodigoContaContabil.getQtdDigUltimoGrau()) {
					tmPrincipal.put("imprimirGrau", setCtasAteGrauEmpresaComMovimentacao.contains(codigo));
				}
				
				if(codigo.length() == configGrausCodigoContaContabil.getQtdDigUltimoGrau() && detalhamento.equals(1)) { //Sintético
					tmPrincipal.put("imprimirGrau", false);
				}
			}
		}
		
		retiraDadosValoresZerados(listTMPrincipal);
		
		return listTMPrincipal;
	}
	
	private void retiraDadosValoresZerados(List<TableMap> dados) {
		if(dados == null || dados.size() == 0) return;
		
		for(int i = dados.size()-1; i >= 0; i--) {
			TableMap tm = dados.get(i);
			def vlrdeb = tm.getBigDecimal_Zero("vlrdeb");
			def vlrcred = tm.getBigDecimal_Zero("vlrcred");
			def saldoanterior = tm.getBigDecimal_Zero("saldoanterior");
			def saldoatual = tm.getBigDecimal_Zero("saldoatual");
			
			if(vlrdeb == 0.0 && vlrcred == 0.0 && saldoanterior == 0.0 && saldoatual == 0.0) {
				dados.remove(i);
			}
		}
	}
	
	private List<TableMap> buscarDebitoCreditoCorrentes(Integer numMesesInicial, Integer numMesesFinal, List<Long> idsContas) {
		String sql = " SELECT ebb02cta as cta, SUM(ebb02deb) as vlrdeb, SUM(ebb02cred) as vlrcred" +
					 " FROM Ebb02" +
					 " WHERE " + Fields.numMeses("ebb02mes", "ebb02ano") + " BETWEEN :numMesesInicial AND :numMesesFinal" +
					 " AND ebb02cta IN (:idsContas)" +
					 samWhere.getWhereGc("AND", Ebb02.class) +
					 " GROUP BY ebb02cta";
					 
		return session.createQuery(sql)
					  .setParameters("idsContas", idsContas,
									   "numMesesInicial", numMesesInicial,
									   "numMesesFinal", numMesesFinal)
					  .getListTableMap();
	}
	
	private List<TableMap> buscarDebitoCreditoAntesEncer(Integer numMesesInicial, Integer numMesesFinal, List<Long> idsContas) {
		String sql = " SELECT ebb03cta as cta, SUM(ebb03deb) as vlrdeb, SUM(ebb03cred) as vlrcred" +
					 " FROM Ebb03" +
					 " WHERE " + Fields.numMeses("ebb03mes", "ebb03ano") + " BETWEEN :numMesesInicial AND :numMesesFinal" +
					 " AND ebb03cta IN (:idsContas)" +
					 samWhere.getWhereGc("AND", Ebb03.class) +
					 " GROUP BY ebb03cta";
					 
		return session.createQuery(sql)
					  .setParameters("idsContas", idsContas,
									 "numMesesInicial", numMesesInicial,
									 "numMesesFinal", numMesesFinal)
					  .getListTableMap();
	}
	
	private List<TableMap> buscarSaldosEncerramentoNoPeriodo(Integer mes, Integer ano, List<Long> idsContas) {
		String sql = " SELECT ebb03cta as cta, ebb03saldo as saldo " +
					  " FROM Ebb03 " +
					 " WHERE " + Fields.numMeses("ebb03mes", "ebb03ano") + " = :numMeses" +
					 " AND ebb03cta IN (:idsContas)" +
					 samWhere.getWhereGc("AND", Ebb03.class);
								   
		return session.createQuery(sql)
					  .setParameters("idsContas", idsContas,
									 "numMeses", Criterions.valNumMeses(mes, ano))
					  .getListTableMap();
	}
	
	private List<TableMap> buscarSaldosCorrente(Integer mes, Integer ano, List<Long> idsContas) {
		String sql = " SELECT ebb02cta as cta, ebb02saldo as saldo " +
					  " FROM Ebb02 " +
					 " WHERE " + Fields.numMeses("ebb02mes", "ebb02ano") + " = :numMeses" +
					 " AND ebb02cta IN (:idsContas)" +
					 samWhere.getWhereGc("AND", Ebb02.class);
								   
		return session.createQuery(sql)
					  .setParameters("idsContas", idsContas,
									 "numMeses", Criterions.valNumMeses(mes, ano))
					  .getListTableMap();
	}
	
	private List<TableMap> buscarSdoAnteriorCtas(Integer mes, Integer ano, List<Long> idsContas) {
		String sql = " SELECT abc10id," +
					 " (SELECT ebb02saldo FROM Ebb02" +
					 " WHERE ebb02cta = abc10id " +
					 " AND " + Fields.numMeses("ebb02mes", "ebb02ano") + " < :numMeses" +
					 samWhere.getWhereGc("AND", Ebb02.class) +
					 " ORDER BY ebb02ano DESC, ebb02mes DESC" +
					 " LIMIT 1) AS saldoAnterior" +
					 " FROM Abc10" +
					 " WHERE abc10id IN (:idsContas)" +
					 samWhere.getWhereGc("AND", Abc10.class) +
					 " ORDER BY abc10codigo";
										   
		return session.createQuery(sql)
					  .setParameters("numMeses", Criterions.valNumMeses(mes, ano),
									   "idsContas", idsContas)
					  .getListTableMap();
	}
	
	private List<TableMap> obterContasMenorGrauECodigosEmpresas(List<Long> idsContas, List<String> codigosEmpresasPlano, boolean ordemAlfabeticaContas, ConfigGrausCodigoContaContabil configGrausCodigoContaContabil) {
		int qtdDigAnteriorGrauEmpresa = configGrausCodigoContaContabil.getQtdDigAnteriorGrauEmpresa();
		int digitosGrauEmpresa = configGrausCodigoContaContabil.getDigitosGrauEmpresa();
		
		String strContas = idsContas != null && !idsContas.isEmpty() ? " abc10id IN (:idsContas) AND " : "";
		
		String strOrderBy = ordemAlfabeticaContas ? " ORDER BY " + Fields.substring("abc10codigo", 1, qtdDigAnteriorGrauEmpresa) + ", abc10nome" : " ORDER BY abc10codigo";
		
		Query query = session.createQuery(" SELECT abc10id, abc10codigo, abc10nome, abc10reduzido",
										  " FROM Abc10",
										  " WHERE ( ", strContas,
										  " (", Fields.length("abc10codigo"), " < 7 ",
										  " OR ", Fields.substring("abc10codigo", qtdDigAnteriorGrauEmpresa+1, digitosGrauEmpresa), " IN (:codigosEmpresaPlano) ) )",
										  samWhere.getWhereGc("AND", Abc10.class),
										  strOrderBy)
							 .setParameter("codigosEmpresaPlano", codigosEmpresasPlano);
		
		if(idsContas != null && !idsContas.isEmpty()) query.setParameter("idsContas", idsContas);
		
		return query.getListTableMap();
	}
	
	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();
		
		configurarDataInicial(filtrosDefault);
		
		configurarPagina(filtrosDefault);
		
		filtrosDefault.put("anexarBalanceteAoDiario", false);
		
		filtrosDefault.put("detalhamento", "0");	//0-Análitico 1-Sintético
		
		filtrosDefault.put("movimentacaoMes", false);
		
		filtrosDefault.put("ordemAlfabeticaContas", false);
		
		filtrosDefault.put("saltarPaginaGrau1Conta", false);
		
		filtrosDefault.put("origemValores", "0");

		filtrosDefault.put("impressao", "0");
		
		configurarCodigosEmprAtualizadas(filtrosDefault);
		
		configurarMsgAtualizarCtas(filtrosDefault);
		
		return Utils.map("filtros", filtrosDefault);
	}
	
	private void configurarDataInicial(Map<String, Object> filtrosDefault) {
		LocalDate data = MDate.date();
		LocalDate[] datas = DateUtils.getStartAndEndMonth(data);
		filtrosDefault.put("dataInicial", datas[0].format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("dataFinal", datas[1].format(DateTimeFormatter.ofPattern("MM/yyyy")));
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
	
	private Eba40 obterUltimoLivro() {
		return getSession().createCriteria(Eba40.class)
				.addWhere(Criterions.eq("eba40livro", Eba40.LIVRO_DIARIO))
				.addWhere(Criterions.eq("eba40termos", Eba40.NAO))
				.addWhere(getSamWhere().getCritPadrao(Eba40.class))
				.setOrder("eba40num DESC")
				.setMaxResults(1)
				.get();
	}
	
	private void configurarPagina(Map<String, Object> filtrosDefault) {
		Eba40 eba40 = obterUltimoLivro();
		Integer ultimaPagina = eba40 != null ? eba40.getEba40ultPag() : 0;
		filtrosDefault.put("ultimaPagina", ultimaPagina);
		filtrosDefault.put("pagina", 0);
	}
	
	private void gravarPaginaDoLivro(Integer numPaginaRelatorio) {
		try {
			boolean anexarBalanceteAoDiario = get("anexarBalanceteAoDiario");
			
			if(anexarBalanceteAoDiario) {
				Eba40 eba40 = obterUltimoLivro();
				if(eba40 != null) {
					Integer numeroPaginas = getInteger("pagina") + numPaginaRelatorio;
					
					eba40.setEba40ultPag(numeroPaginas);
					getSamWhere().setDefaultValues(eba40);
					getSession().persist(eba40);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
//meta-sis-eyJkZXNjciI6IlNHQyAtIEJhbGFuY2V0ZSBkZSBWZXJpZmljYcOnw6NvIiwidGlwbyI6InJlbGF0b3JpbyJ9