package multitec.relatorios.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
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
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01
import sam.model.entities.eb.Eba40;
import sam.model.entities.eb.Ebb02;
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.relatorio.TableMapDataSource;

public class SGC_LivroCaixa extends RelatorioBase {
	private final int LIVRO_CAIXA = 0;
	private final int TERMO_ABERTURA = 1;
	
	@Override
	public String getNomeTarefa() {
		return "SGC - Livro Caixa";
	}

	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		params.put("sgc_livroCaixar2s1Stream", carregarArquivoRelatorio("SGC_LivroCaixa_R2_S1"));
		
		int imprimir = getInteger("imprimir");
		LocalDate[] datas = new LocalDate[2];
		datas[0] = DateUtils.parseDate(getString("dataInicial"));
		datas[1] = DateUtils.parseDate(getString("dataFinal"));
		boolean isRascunho = get("rascunho");
		
		if(imprimir == LIVRO_CAIXA){
			params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
			params.put("RASCUNHO", isRascunho);
			params.put("NUMERO_PAGINA",  isRascunho ? 0 : getInteger("pagina"));
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
		if(imprimir == LIVRO_CAIXA) {
			dados = obterLivroCaixa(datas[0], datas[1]);
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		if(dados == null || dados.size() == 0) throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");
		
		JasperReport report = carregarArquivoRelatorio(imprimir == LIVRO_CAIXA ? "SGC_LivroCaixa_R2" : "SGC_LivroCaixa_R1");
		JasperPrint print = processarRelatorio(report, dados);

		if(print == null || print.getPages().size() == 0) throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");

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
		configurarMsgAtualizarCtas(filtrosDefault);
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
	
	private List<TableMap> obterLivroCaixa(LocalDate dataIni, LocalDate dataFin) {
		//Soma dos lançamentos do período
		Map<String, TableMap> mapRelatorio = new TreeMap<>();
		Map<String, TableMap> mapSub = new TreeMap<>();
		
		//Busca o saldo anterior de cada conta e armazena no Map
		Map<Long, BigDecimal> mapSaldos = new TreeMap<>();
		buscarSaldoAnteriorDasContas(dataIni, mapSaldos, mapSub);
		
		BigDecimal saldoAtual = DecimalUtils.create(params.get("SDO_ANTERIOR")).get();
		
		//Soma à débito
		List<TableMap> lctosDebito = buscarLctosLivroCaixaPeloPeriodoIniFin(true, dataIni, dataFin);
		comporMapeamento(mapRelatorio, mapSaldos, lctosDebito, true, mapSub);
		
		//Soma à crédito
		List<TableMap> lctosCredito = buscarLctosLivroCaixaPeloPeriodoIniFin(false, dataIni, dataFin);
		comporMapeamento(mapRelatorio, mapSaldos, lctosCredito, false, mapSub);
		
		List<TableMap> relatorio = new ArrayList<>();
		if(mapRelatorio.size() > 0) {
			for(String key : mapRelatorio.keySet()) {
				BigDecimal saida = mapRelatorio.get(key).get("saida") != null ? DecimalUtils.create(mapRelatorio.get(key).get("saida")).get() : BigDecimal.ZERO;
				BigDecimal entrada = mapRelatorio.get(key).get("entrada") != null ? DecimalUtils.create(mapRelatorio.get(key).get("entrada")).get() : BigDecimal.ZERO;
				
				saldoAtual = saldoAtual.add(entrada);
				saldoAtual = saldoAtual.subtract(saida);
				
				TableMap row = mapRelatorio.get(key);
				row.put("saldoAtual", saldoAtual);
				
				relatorio.add(mapRelatorio.get(key));
			}
		}
				
		List<TableMap> subRel = new ArrayList<>();
		for(String key : mapSub.keySet()) {
			BigDecimal sdoAnt = mapSub.get(key).get("sdoAnterior") != null ? DecimalUtils.create(mapSub.get(key).get("sdoAnterior")).get() : BigDecimal.ZERO;
			BigDecimal entrada = mapSub.get(key).get("entrada") != null ? DecimalUtils.create(mapSub.get(key).get("entrada")).get() : BigDecimal.ZERO;
			BigDecimal saida = mapSub.get(key).get("saida") != null ? DecimalUtils.create(mapSub.get(key).get("saida")).get() : BigDecimal.ZERO;

			BigDecimal sdoAtual = sdoAnt.add(entrada).subtract(saida);
			
			TableMap row = mapSub.get(key);
			row.put("sdoAtual",  sdoAtual);
			
			subRel.add(mapSub.get(key));
		}	
		
		//TODO passar os dados do sub relatorio
		TableMapDataSource dataSetSub = new TableMapDataSource(subRel);
		params.put("sgc_livroCaixar2s1DS", dataSetSub);
				
		return relatorio;
	}
	
	private Map<String, TableMap> comporMapeamento(Map<String, TableMap> mapRelatorio, Map<Long, BigDecimal> mapSaldos, List<TableMap> lctos, boolean debito, Map<String, TableMap> mapSub) {
		if(lctos.size() > 0) {
			for(TableMap lcto : lctos) {
				TableMap row = new TableMap();
				row.put("ebb05data", lcto.get("ebb05data"));
				row.put("ebb05historico", lcto.get("ebb05historico"));
				row.put("ctaCredito", lcto.get("ctaCredito"));
				row.put("ctaDebito", lcto.get("ctaDebito"));
				
				row.put("sdoAnterior", debito ? mapSaldos.get(lcto.get("abc10idDeb")) : mapSaldos.get(lcto.get("abc10idCred")));
				
				BigDecimal valor = lcto.get("ebb05valor");
				row.put(debito ? "entrada" : "saida", valor);
				
				String conta = debito ? lcto.get("ctaDebito") : lcto.get("ctaCredito");
				
				String date = lcto.get("ebb05data").toString();
				
				String key = date + "/" + lcto.get("ebb05num") + "/" + conta;
				
				mapRelatorio.put(key, row);
				
				//Resumo
				TableMap rowSub = mapSub.get(conta);
				BigDecimal valSub = mapSub.get(conta).get(debito ? "entrada" : "saida") != null ? DecimalUtils.create(mapSub.get(conta).get(debito ? "entrada" : "saida")).get().add(valor) : valor;
				rowSub.put(debito ? "entrada" : "saida", valSub);
				
				mapSub.put(conta, rowSub);
			}
		}
		
		return mapRelatorio;
	}
	
	private Map<Long, BigDecimal> buscarSaldoAnteriorDasContas(LocalDate dataIni, Map<Long, BigDecimal> mapSaldos, Map<String, TableMap> mapSub){
		BigDecimal sdoAnterior = BigDecimal.ZERO;
		
		List<TableMap> sdoContas = buscarSdoAnteriorDaContaLivroCaixa(dataIni.getMonthValue(), dataIni.getYear());
		for(TableMap sdo : sdoContas) {
			BigDecimal saldo = sdo.getBigDecimal("SALDO");
			
			if(dataIni.getDayOfMonth() > 1) {
				LocalDate dtInicial = LocalDate.of(dataIni.getYear(), dataIni.getMonthValue(), 1);
				LocalDate dtFinal = dataIni.minus(1, ChronoUnit.DAYS);
				
				Long cta = sdo.getLong("ebb02cta");
				BigDecimal somaLctosDeb = buscarSomaLctosADebitoLivroCaixa(cta, dtInicial, dtFinal);
				BigDecimal somaLctosCred = buscarSomaLctosACreditoLivroCaixa(cta, dtInicial, dtFinal);
				
				saldo = saldo.add(somaLctosDeb).subtract(somaLctosCred);
				
			}
			
			mapSaldos.put(sdo.getLong("ebb02cta"), saldo);
			sdoAnterior = sdoAnterior.add(sdo.get("SALDO"));
			
			
			//Resumo 
			String conta = StringUtils.concat(sdo.get("abc10codigo"), "-", sdo.get("abc10nome"));
			
			TableMap rowSub = mapSub.get(conta) != null ? mapSub.get(conta) : new TableMap();
			rowSub.put("conta", conta);
			rowSub.put("sdoAnterior", saldo);
			rowSub.put("sdoAtual", saldo);
						
			mapSub.put(conta, rowSub);
		}
		
		params.put("SDO_ANTERIOR", sdoAnterior);
		
		return mapSaldos;
	}
	
	private void configurarDataInicial(Map<String, Object> filtrosDefault) {
		LocalDate data = MDate.date();
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
			if(imprimir == LIVRO_CAIXA) {
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
				eba40.setEba40livro(Eba40.LIVRO_CAIXA);
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
		return getSession().createQuery(" SELECT eba40id, eba40termos ",
										"FROM Eba40 WHERE eba40livro = :eba40livro AND eba40num = :eba40num ",
										getSamWhere().getWherePadrao("AND", Eba40.class))
						   .setParameters("eba40livro", Eba40.LIVRO_CAIXA,
								   	      "eba40num", eba40num)
						   .setMaxResult(1)
						   .getUniqueResult(ColumnType.ENTITY);
	}
	
	private Eba40 obterUltimoLivro() {
		return getSession().createCriteria(Eba40.class)
				.addWhere(Criterions.eq("eba40livro", Eba40.LIVRO_CAIXA))
				.addWhere(Criterions.eq("eba40termos", Eba40.NAO))
				.addWhere(getSamWhere().getCritPadrao(Eba40.class))
				.setOrder("eba40num DESC")
				.setMaxResults(1)
				.get();
	}
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery(
									"SELECT MAX(eba40num) FROM Eba40 WHERE eba40livro = :eba40livro",
									getSamWhere().getWherePadrao("AND", Eba40.class))
				  .setParameter("eba40livro", Eba40.LIVRO_CAIXA)
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
		
	private List<TableMap> buscarLctosLivroCaixaPeloPeriodoIniFin(boolean debito, LocalDate ebb05dataIni, LocalDate ebb05dataFin){
		String whereCaixa = debito ? " abc10deb.abc10iLivCx = :caixa " : " abc10cred.abc10iLivCx = :caixa ";
		
		return getSession().createQuery(" SELECT ebb05id, ebb05historico, ebb05valor, ebb05data, ebb05num, abc10deb.abc10id AS abc10idDeb, abc10cred.abc10id AS abc10idCred, ",
				 						"abc10deb.abc10codigo || '-' ||abc10deb.abc10nome AS ctaDebito, ",
				 						"abc10cred.abc10codigo || '-' ||abc10cred.abc10nome AS ctaCredito ",
										" FROM Ebb05",
										" INNER JOIN Abc10 AS abc10deb ON abc10deb.abc10id = ebb05deb",
										" INNER JOIN Abc10 AS abc10cred ON abc10cred.abc10id = ebb05cred",
										" WHERE ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin AND",
										whereCaixa,
										getSamWhere().getWherePadrao("AND", Ebb05.class),
										" ORDER BY ebb05data, ebb05num")
						   .setParameters("ebb05dataIni", ebb05dataIni,
									   	  "ebb05dataFin", ebb05dataFin,
									   	  "caixa", 1)
						   .getListTableMap();
	}
	
	private List<TableMap> buscarSdoAnteriorDaContaLivroCaixa(Integer mes, Integer ano){
		return getSession().createQuery(" SELECT ebb02cta, abc10codigo, abc10nome, SUM(ebb02deb - ebb02cred) as SALDO ",
										" FROM Ebb02 ",
										" INNER JOIN Abc10 ON abc10id = ebb02cta ",
										" WHERE abc10iLivCx = :caixa AND ",
										Fields.numMeses("ebb02mes", "ebb02ano"), " < :numMeses",
										getSamWhere().getWherePadrao("AND", Ebb02.class),
										" GROUP BY ebb02cta, abc10codigo, abc10nome ")
						   .setParameters("numMeses", Criterions.valNumMeses(mes, ano),
								          "caixa", 1)
						   .getListTableMap();
	}
	
	private BigDecimal buscarSomaLctosADebitoLivroCaixa(Long abc10id, LocalDate ebb05dataIni, LocalDate ebb05dataFin){
		BigDecimal saldo =  getSession().createQuery(" SELECT SUM(ebb05valor) AS valor ",
													 " FROM Ebb05",
													 " INNER JOIN Abc10 ON abc10id = ebb05deb ",
													 " WHERE ebb05deb = :abc10id AND abc10iLivCx = :caixa ",
													 " AND ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin ",
													 getSamWhere().getWherePadrao("AND", Ebb05.class))
					       				.setParameters("ebb05dataIni", ebb05dataIni,
					       							   "ebb05dataFin", ebb05dataFin,
					       							   "abc10id", abc10id,
					       							   "caixa", 1)
										.setMaxResult(1)
					       				.getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	private BigDecimal buscarSomaLctosACreditoLivroCaixa(Long abc10id, LocalDate ebb05dataIni, LocalDate ebb05dataFin){
		BigDecimal saldo = getSession().createQuery(" SELECT SUM(ebb05valor) * -1 AS valor ",
													" FROM Ebb05",
													" INNER JOIN Abc10 ON abc10id = ebb05cred ",
													" WHERE ebb05cred = :abc10id AND abc10iLivCx = :caixa ",
													" AND ebb05data BETWEEN :ebb05dataIni AND :ebb05dataFin ",
													getSamWhere().getWherePadrao("AND", Ebb05.class))
						   			   .setParameters("ebb05dataIni", ebb05dataIni,
						   					   	      "ebb05dataFin", ebb05dataFin,
						   					   	      "abc10id", abc10id,
						   					   	      "caixa", 1)
									   .setMaxResult(1)
						   			   .getUniqueResult(ColumnType.BIG_DECIMAL);
		
		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
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
//meta-sis-eyJkZXNjciI6IlNHQyAtIExpdnJvIENhaXhhIiwidGlwbyI6InJlbGF0b3JpbyJ9