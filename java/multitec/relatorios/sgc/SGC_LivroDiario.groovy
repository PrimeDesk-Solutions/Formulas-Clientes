package multitec.relatorios.sgc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter

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
import sam.dicdados.Parametros
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Aba01
import sam.model.entities.eb.Eba40;
import sam.model.entities.eb.Ebb05;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;

public class SGC_LivroDiario extends RelatorioBase {
	private final int DIARIO_GERAL = 0;
	private final int TERMO_ABERTURA = 1;
	
	@Override
	public String getNomeTarefa() {
		return "SGC - Livro Diário";
	}

	@Override
	public DadosParaDownload executar() {
		params.put("EMPRESA", getVariaveis().getAac10().getAac10na());
		
		int imprimir = getInteger("imprimir");
		LocalDate[] datas = new LocalDate[2];
		datas[0] = DateUtils.parseDate(getString("dataInicial"));
		datas[1] = DateUtils.parseDate(getString("dataFinal"));
		boolean isRascunho = get("rascunho");
		
		if(imprimir == DIARIO_GERAL){
			params.put("PERIODO", DateUtils.formatDate(datas[0]) + " à " + DateUtils.formatDate(datas[1]));
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
		if(imprimir == DIARIO_GERAL) {
			dados = obterDadosDiarioGeral(datas);
		} else {
			dados.add(obterDadosTermoAberturaOrEncerramento());
		}
		
		if(dados == null || dados.size() == 0) throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório");
		
		JasperReport report = carregarArquivoRelatorio(imprimir == DIARIO_GERAL ? "SGC_LivroDiario_R1" : "SGC_LivroDiario_R2");
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
			if(imprimir == DIARIO_GERAL) {
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
				eba40.setEba40livro(Eba40.LIVRO_DIARIO);
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
										"FROM Eba40 WHERE eba40livro = :eba40livro AND eba40num = :eba40num",
										getSamWhere().getWherePadrao("AND", Eba40.class))
			.setParameters("eba40livro", Eba40.LIVRO_DIARIO,
			   			   "eba40num", eba40num)
			.setMaxResult(1)
		    .getUniqueResult(ColumnType.ENTITY);
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
	
	private Integer obterProximoNum() {
		Integer num = getSession().createQuery(
									"SELECT MAX(eba40num) FROM Eba40 WHERE eba40livro = :eba40livro", 
									getSamWhere().getWherePadrao("AND", Eba40.class))
				  .setParameter("eba40livro", Eba40.LIVRO_DIARIO)
	  	   		  .getUniqueResult(ColumnType.INTEGER);
		return num == null ? 1 : num + 1;
	}
	
	private Aag0201 obterMunicipio(Long aac10municipio) {
		return getSession().createCriteria(Aag0201.class)
				.addWhere(Criterions.eq("aag0201id", aac10municipio))
				.get();
	}
	
	private List<TableMap> obterDadosDiarioGeral(LocalDate[] datas){
		return getSession().createQuery(
				" SELECT ebb05data, abc10deb.abc10codigo as abc10debito, abc10cred.abc10codigo as abc10credito, ebb05historico, ebb05valor ",
				" FROM Ebb05 ",
				" INNER JOIN Abc10 abc10deb ON ebb05deb = abc10deb.abc10id ",
				" INNER JOIN Abc10 abc10cred ON ebb05cred = abc10cred.abc10id ",
				" WHERE ebb05data BETWEEN :dtInicial AND :dtFinal",
				" " + getSamWhere().getWherePadrao("AND", Ebb05.class),
				" ORDER BY ebb05data, ebb05num")
			.setParameter("dtInicial", datas[0])
			.setParameter("dtFinal", datas[1])
			.getListTableMap();
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
//meta-sis-eyJkZXNjciI6IlNHQyAtIExpdnJvIERpw6FyaW8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=