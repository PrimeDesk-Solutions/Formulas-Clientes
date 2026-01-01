package multitec.relatorios.sgc;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.MediaType;

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multiorm.criteria.join.Joins;
import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.DecimalUtils;
import br.com.multitec.utils.Extenso
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
import sam.model.entities.eb.Eba10
import sam.model.entities.eb.Eba30;
import sam.model.entities.eb.Eba40;
import sam.model.entities.eb.Ebb02;
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SGC_BalancoPatrimonialDRE extends RelatorioBase{

	public static final int BALANCO_PATRIMONIAL = 0;
	public static final int DRE = 1;
	public static final int DRE_MENSAL = 2;
	public static final int NOTAS_EXPLICATIVAS = 3;
	private Map<String, Object> mapCodContas = new HashMap();

	@Override
	public String getNomeTarefa() {
		return "SGC - Balanço Patrimonial e DRE";
	}

	@Override
	public DadosParaDownload executar() {

		/**
		 * MAPA DE CODIGOS DE CONTAS X CODIGOS DE AGRUPAMENTO DE CONTAS PARA DRE
		 * Ex.: "Codigo da Conta", "Codigo do agrupamento de Contas"
		 */

		mapCodContas = Utils.map(
			"30","99010130",
			"32","99010132",
			"33","99010133",
			"35","99010135",
			"36","99010136"
		);

		Integer pagina = getInteger("pagina");

		String data1 = getString("data1");
		String data2 = getString("data2");

		Integer dataRef1 = DateUtils.numMeses(Integer.parseInt(data1.split("/")[0]), Integer.parseInt(data1.split("/")[1]));

		Integer dataRef2 = 0;
		if(data1.compareTo(data2) != 0) {
			dataRef2 = DateUtils.numMeses(Integer.parseInt(data2.split("/")[0]), Integer.parseInt(data2.split("/")[1]));
		}

		String representante1 = getString("representante1");
		String representante2 = getString("representante2");
		String representante3 = getString("representante3");

		String assinatura1 = getString("assinatura1");
		String assinatura2 = getString("assinatura2");
		String assinatura3 = getString("assinatura3");

		Integer emitir = getInteger("emitir");

		boolean termoAtivo = get("termoAtivo");
		boolean termoPassivo = get("termoPassivo");
		boolean termoDRE = get("termoDRE");
		boolean termoNotasExplicativas = get("termoNotasExplicativas");

		boolean assAtivo = get("assAtivo");
		boolean assPassivo = get("assPassivo");
		boolean assDRE = get("assDRE");
		boolean assNotasExplicativas = get("assNotasExplicativas");

		String positivo = getString("positivo");
		String negativo = getString("negativo");

		boolean consideraGrauEmpresa = get("consideraGrauEmpresa");

		Aac10 aac10 = getVariaveis().getAac10();

		String endereco = null;
    	if(aac10.getAac10endereco() != null){
    		if(aac10.getAac10numero() != null){
    			endereco = aac10.getAac10endereco() + ", " + aac10.getAac10numero();
    		}else{
    			endereco = aac10.getAac10endereco();
    		}
    	}

        String bairroCompl = null;
        if(aac10.getAac10bairro() != null){
        	if(aac10.getAac10complem() != null){
        		bairroCompl = aac10.getAac10bairro() + " - " + aac10.getAac10complem();
        	}
        	else{
        		bairroCompl = aac10.getAac10bairro();
        	}
        }

        String cidadeUf = null;
    	if(aac10.getAac10municipio() != null){
    		Aag0201 aag0201 = getSession().createCriteria(Aag0201.class).addJoin(Joins.fetch("aag0201uf")).addWhere(Criterions.eq("aag0201id", aac10.getAac10municipio().getIdValue())).get();
    		if(aag0201 != null && aag0201.getAag0201nome() != null){
        		if(aag0201.getAag0201uf() != null){
        			if(aag0201.getAag0201uf().getAag02uf() != null){
        				cidadeUf = aag0201.getAag0201nome() + " / " + aag0201.getAag0201uf().getAag02uf();
        			}
        		}else{
        			cidadeUf = aag0201.getAag0201nome();
        		}
    		}
    	}

    	String cep = null;
    	if(aac10.getAac10cep() != null){
    		cep = "C.E.P.: " + aac10.getAac10cep();
    	}

		params.put("EMP_RS", aac10.getAac10rs());
		params.put("EMP_ENDERECO", endereco);
		params.put("EMP_BAIRRO_COMPL", bairroCompl);
		params.put("EMP_CIDADE_UF", cidadeUf);
		params.put("EMP_CEP", cep);
		params.put("EMP_NI", aac10.getAac10ni());
        params.put("NUMERO_PAGINA", pagina);
        params.put("TIPO_INSCRICAO", aac10.getAac10ti() == 1 ? "C.P.F.:" : "C.N.P.J.:");
        params.put("DATA", data1);

        params.put("DT_REF1", data1);
        params.put("DT_REF2", data2);

        params.put("ASSINATURA11", representante1);
        params.put("ASSINATURA12", representante2);
        params.put("ASSINATURA13", representante3);
        params.put("ASSINATURA21", assinatura1);
        params.put("ASSINATURA22", assinatura2);
        params.put("ASSINATURA23", assinatura3);

        ConfigGrausCodigoContaContabil configGrausCodigoContaContabil = ConfigGrausCodigoContaContabil.obterGrausDigitosEstruturaCodigos(obterEstruturaContas());
		Integer grauEmpresa = obterGrauEmpresa();
		if(grauEmpresa == null)throw new ValidacaoException("Não foi encontrado o conteúdo do parâmetro Abc10_GrauEmpresa");
		configGrausCodigoContaContabil.setGrauEmpresa(grauEmpresa);

        List<TableMap> dados = null;
        JasperReport report = null;
        if(emitir == BALANCO_PATRIMONIAL) {
        	dados = obterDadosBalancoPatrimonial(termoAtivo, termoPassivo, assAtivo, assPassivo, configGrausCodigoContaContabil, consideraGrauEmpresa, dataRef1, dataRef2);
        	report = carregarArquivoRelatorio(dataRef2 == 0 ? "SGC_BalancoPatrimonialDRE_R1" : "SGC_BalancoPatrimonialDRE_R4");
        }else if(emitir == DRE || emitir == DRE_MENSAL) {
        	dados = obterDadosDRE(termoDRE, assDRE, configGrausCodigoContaContabil, consideraGrauEmpresa, emitir == DRE_MENSAL, dataRef1, dataRef2, positivo, negativo);
        	report = carregarArquivoRelatorio(dataRef2 == 0 ? "SGC_BalancoPatrimonialDRE_R2" : "SGC_BalancoPatrimonialDRE_R5");
        }else { //NOTAS_EXPLICATIVAS
        	dados = obterDadosNotasExplicativas(termoNotasExplicativas, assNotasExplicativas, dataRef1);
        	report = carregarArquivoRelatorio("SGC_BalancoPatrimonialDRE_R3");
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
		return new DadosParaDownload(bytes, this.getClass().getSimpleName() + ".pdf", MediaType.APPLICATION_PDF);
	}

	private List<TableMap> obterDadosBalancoPatrimonial(boolean termoAtivo, boolean termoPassivo, boolean assAtivo, boolean assPassivo, ConfigGrausCodigoContaContabil configGrausCodigoContaContabil, boolean consideraGrauEmpresa, Integer dataRef1, Integer dataRef2) {
		params.put("TITULO_RELATORIO", "B A L A N Ç O  P A T R I M O N I A L");

		params.put("IMP_TERMO_ATIVO", termoAtivo ? 1 : 0);
		params.put("IMP_TERMO_PASSIVO", termoPassivo ? 1 : 0);
		params.put("IMP_ASS_ATIVO", assAtivo ? 1 : 0);
		params.put("IMP_ASS_PASSIVO", assPassivo ? 1 : 0);

		if(configGrausCodigoContaContabil.getQtGrau() != 6 || configGrausCodigoContaContabil.getGrauEmpresa() != 5) throw new ValidacaoException("Balanço Patrimonial desenvolvido para plano de contas com 6 graus e grau da empresa sendo o 5.");

		List<Abc10> abc10s = buscarContasBalancoPatrimonial(configGrausCodigoContaContabil, consideraGrauEmpresa);

		if(abc10s == null || abc10s.size() == 0)throw new ValidacaoException("Não foram encontradas contas de ativo e passivo para se gerar o Balanço Patrimonial.");

		List<TableMap> dadosPrincipal = new ArrayList<>();

		String codigo = "";
		Long idAbc10 = 0L;
		Integer tamCodigo = 0;
		Abc10 abc10Grau1 = null;

		String[] graus = ["", "", "", "", ""]; //Até 5 graus

		boolean setouTermo = false;

		if(abc10s != null && abc10s.size() > 0) {
			for(Abc10 abc10 : abc10s) {
				codigo = abc10.getAbc10codigo();
				idAbc10 = abc10.getAbc10id();
				tamCodigo = codigo.length();

				for(int i = 0; i < graus.length; i++) { //Até quinto grau
					if(tamCodigo.equals(configGrausCodigoContaContabil.getQtDig()[i])){
						if(i == 0) abc10Grau1 = abc10;

						graus[i] = codigo;
						for(int j = i+1; j < graus.length; j++) {
							graus[j] = "";
						}
					}
				}

				BigDecimal saldo1 = buscarSdoCta(dataRef1, idAbc10,false);
				BigDecimal saldo2 = dataRef2 > 0 ? buscarSdoCta(dataRef2, idAbc10,false) : BigDecimal.ZERO;

			    if(!setouTermo && abc10.getAbc10class().equals(Abc10.CLASS_ATIVA)) {
			    	Extenso extenso = new Extenso(saldo1.abs());
			    	NumberFormat format = NumberFormat.getIntegerInstance();
			    	format.setMinimumFractionDigits(2);
			    	format.setGroupingUsed(true);
			    	params.put("TERMO", "RECONHECEMOS A EXATIDÃO DO PRESENTE BALANÇO PATRIMONIAL TOTALIZANDO O ATIVO E O " +
					           "PASSIVO A IMPORTÂNCIA SUPRA DE R\$ " + format.format((DecimalUtils.create(saldo1).round(2)).get()) + " (" + extenso + ").");
					setouTermo = true;
			    }

			    if(saldo1 != null){
			    	if(saldo1.compareTo(BigDecimal.ZERO) != 0){
					    if(abc10Grau1 != null && abc10Grau1.getAbc10class().equals(Abc10.CLASS_PASSIVA)){
					    	saldo1 = saldo1.multiply(new BigDecimal(-1)); // Invertendo sinal para as contas do passivo
					    }

					    comporMap(dadosPrincipal, graus, abc10.getAbc10nome(), saldo1);
			    	}
			    }

			    if(dataRef2 > 0){
			    	if(saldo2.compareTo(BigDecimal.ZERO) != 0){
					    if(abc10Grau1 != null && abc10Grau1.getAbc10class().equals(Abc10.CLASS_PASSIVA)){
					    	saldo2 = saldo2.multiply(new BigDecimal(-1)); // Invertendo sinal para as contas do passivo
					    }

					    if(saldo1.compareTo(BigDecimal.ZERO) == 0)comporMap(dadosPrincipal, graus, abc10.getAbc10nome(), saldo1);
					    comporMap(dadosPrincipal, saldo2);
			    	}
			    }
			}
		}

		if(dadosPrincipal == null || dadosPrincipal.size() == 0) throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório. Verifique cadastros, contas, lançamentos, período e parâmetros.");

		return dadosPrincipal;
	}


	public BigDecimal buscarSdoCta(Integer dataRef, Long abc10id, Boolean isMensal) {

		BigDecimal saldo = 0.0

		if (isMensal) {

			saldo = session.createQuery("SELECT ebb02deb - ebb02cred AS saldo  FROM Ebb02",
					" WHERE ebb02cta = :abc10id ",
					" AND ", Fields.numMeses("ebb02mes", "ebb02ano"), " = :numMeses",
					samWhere.getWhereGc("AND", Ebb02.class),
					" ORDER BY ebb02ano DESC, ebb02mes DESC")
					.setParameters("numMeses", dataRef,
							"abc10id", abc10id)
					.setMaxResult(1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);

		} else {
			saldo = session.createQuery(" SELECT ebb02saldo FROM Ebb02",
					" WHERE ebb02cta = :abc10id ",
					" AND ", Fields.numMeses("ebb02mes", "ebb02ano"), " <= :numMeses",
					samWhere.getWhereGc("AND", Ebb02.class),
					" ORDER BY ebb02ano DESC, ebb02mes DESC")
					.setParameters("numMeses", dataRef,
							"abc10id", abc10id)
					.setMaxResult(1)
					.getUniqueResult(ColumnType.BIG_DECIMAL);

		}
		return saldo != null ? saldo : BigDecimal.ZERO;
	}

	private List<Abc10> buscarContasBalancoPatrimonial(ConfigGrausCodigoContaContabil configGrausCodigoContaContabil, boolean consideraGrauEmpresa) {
		int qtdDigitos = consideraGrauEmpresa ? configGrausCodigoContaContabil.getDigitosGrauEmpresa() : configGrausCodigoContaContabil.getQtdDigAnteriorGrauEmpresa();

		return session.createQuery(" SELECT abc10id, abc10codigo, abc10nome, abc10class",
								   " FROM Abc10",
								   " WHERE abc10class IN (:class)",
								   " AND ", Fields.length("abc10codigo"), " <= :qtdDigitos",
								   samWhere.getWhereGc("AND", Abc10.class),
								   " ORDER BY abc10codigo")
					  .setParameters("class", [Abc10.CLASS_ATIVA, Abc10.CLASS_PASSIVA],
							  		 "qtdDigitos", qtdDigitos)
					  .getList(ColumnType.ENTITY);
	}

	private void comporMap(List<TableMap> mapDados, String grau1, String grau2, String grau3, String grau4, String grau5, String nome, BigDecimal saldo1) {
		String[] graus = [grau1, grau2, grau3, grau4, grau5];
		comporMap(mapDados, graus, nome, saldo1);
	}

	private void comporMap(List<TableMap> mapDados, String[] graus, String nome, BigDecimal saldo1) {
		TableMap tm = new TableMap();

		for(int i = 0; i < graus.length; i++) {
			tm.put("grau"+ (i+1), graus[i]);
		}
		tm.put("nome", nome);
		tm.put("saldo1", saldo1);

		mapDados.add(tm);
	}
	private void comporMap(List<TableMap> mapDados, BigDecimal saldo2) {
		int row = mapDados.size()-1;
		TableMap tm = mapDados.get(row);
		tm.put("saldo2", saldo2);
		mapDados.add(row, tm);
	}

	private List<TableMap> obterDadosDRE(boolean termoDRE, boolean assDRE, ConfigGrausCodigoContaContabil configGrausCodigoContaContabil, boolean consideraGrauEmpresa, boolean isMensal, Integer dataRef1, Integer dataRef2, String strPositivo, String strNegativo) {
		params.put("TITULO_RELATORIO", "DEMONSTRATIVO DO RESULTADO DO EXERCÍCIO");

		params.put("IMP_TERMO", termoDRE ? 1 : 0);
		params.put("IMP_ASS", assDRE ? 1 : 0);

		if(configGrausCodigoContaContabil.getQtGrau() != 6 || configGrausCodigoContaContabil.getGrauEmpresa() != 5) throw new ValidacaoException("DRE desenvolvida para plano de contas com 6 graus e grau da empresa sendo o 5.");

		List<Abc10> abc10s = buscarContasDRE(configGrausCodigoContaContabil, consideraGrauEmpresa);

		if(abc10s == null || abc10s.size() == 0)throw new ValidacaoException("Não foram encontradas contas resultado para se gerar a DRE.");

		List<TableMap> dadosPrincipal = new ArrayList<>();

		String ultGrupo = null;
		String codigo = "";
		Long idAbc10 = 0L;
		Integer tamCodigo = 0;

		String[] graus = ["", "", "", "", ""]; //Até 5 graus

		String grau1Grau2 = null;
		String grau3Grau2 = null;
		String grau4Grau2 = null;
		String grau5Grau2 = null;
		String codigoGrau2 = null;
		String nomeGrau2 = null;
		BigDecimal saldoGrau2_1 = null;
		BigDecimal saldoGrau2_2 = null;

		if(abc10s != null && abc10s.size() > 0) {
			for(Abc10 abc10 : abc10s) {
				codigo = abc10.getAbc10codigo();
				idAbc10 = abc10.getAbc10id();
				tamCodigo = codigo.length();

				for(int i = 0; i < graus.length; i++) { //Até quinto grau
					if(tamCodigo.equals(configGrausCodigoContaContabil.getQtDig()[i])){
						graus[i] = codigo;
						for(int j = i+1; j < graus.length; j++) {
							graus[j] = "";
						}

						if(i == 0) ultGrupo = codigo;

						if(i == 1) {
							//O grau 2 deve ser manipulado para que seja impresso após os graus 3 e 4
		                    if(tamCodigo.equals(2)){
								if(codigoGrau2 != null || nomeGrau2 != null || (saldoGrau2_1 != null || saldoGrau2_2 != null)){
									if(saldoGrau2_1.compareTo(BigDecimal.ZERO) != 0){
										comporMap(dadosPrincipal, grau1Grau2, codigoGrau2, grau3Grau2, grau4Grau2, grau5Grau2, nomeGrau2, saldoGrau2_1);
									}

									if(saldoGrau2_2.compareTo(BigDecimal.ZERO) != 0){
										if(saldoGrau2_1.compareTo(BigDecimal.ZERO) == 0)comporMap(dadosPrincipal, grau1Grau2, codigoGrau2, grau3Grau2, grau4Grau2, grau5Grau2, nomeGrau2, saldoGrau2_1);
										comporMap(dadosPrincipal, saldoGrau2_2);
									}
								}
							}
						}
					}
				}

				BigDecimal saldo1 = buscarSdoCta(dataRef1, idAbc10, isMensal);
				BigDecimal saldo2 = dataRef2 > 0 ? buscarSdoCta(dataRef2, idAbc10, isMensal) : BigDecimal.ZERO;

				String novoNome = null;

				// Se for DRE altera o nome e o saldo das contas
				if(mapCodContas != null && tamCodigo.equals(configGrausCodigoContaContabil.getQtDig()[1])) {
					Eba10 eba10 = buscarAgrupamentoDeContas(mapCodContas.get(codigo));
					if(eba10 != null) {
						List<Long> idsAbc10 = buscarContasDoAgrupamento(eba10.getEba10id());

						BigDecimal saldoAgrup1 = 0;
						BigDecimal saldoAgrup2 = 0;

						if(idsAbc10 != null && idsAbc10.size() > 0) {
							for(Long abc10id : idsAbc10) {
								saldoAgrup1 += buscarSdoCta(dataRef1, abc10id, isMensal);
								saldoAgrup2 += dataRef2 > 0 ? buscarSdoCta(dataRef2, abc10id, isMensal) : BigDecimal.ZERO;
							}
						}

						Long idAgrupam = eba10.getEba10id();
						novoNome = eba10.getEba10nome();
						saldo1 = saldoAgrup1 != null ? saldoAgrup1 : 0.0;
						saldo2 = saldoAgrup2 != null ? saldoAgrup2 : 0.0;
					}
				}

			    if(saldo1 != null){
			    	if(saldo1.compareTo(BigDecimal.ZERO) != 0){
				    	saldo1 = saldo1.multiply(new BigDecimal(-1)); // Invertendo sinal para as contas de resultado
			    	}
			    }
			    if(saldo2 != null){
			    	if(saldo2.compareTo(BigDecimal.ZERO) != 0){
				    	saldo2 = saldo2.multiply(new BigDecimal(-1)); // Invertendo sinal para as contas de resultado
			    	}
			    }

			    if(tamCodigo.equals(configGrausCodigoContaContabil.getQtDig()[1])){ //grau 2
			    	grau1Grau2 = graus[0];
			    	grau3Grau2 = graus[2];
			    	grau4Grau2 = graus[3];
			    	grau5Grau2 = graus[4];
			    	codigoGrau2 = codigo;
			    	nomeGrau2 = novoNome != null ? novoNome : abc10.getAbc10nome();
			    	saldoGrau2_1 = saldo1;
			    	saldoGrau2_2 = saldo2;
				}else{
					if(saldo1.compareTo(BigDecimal.ZERO) != 0){
					    comporMap(dadosPrincipal, graus, abc10.getAbc10nome(), saldo1);
					}

					if(saldo2.compareTo(BigDecimal.ZERO) != 0){
						if(saldo1.compareTo(BigDecimal.ZERO) == 0)comporMap(dadosPrincipal, graus, abc10.getAbc10nome(), saldo1);
						comporMap(dadosPrincipal, saldo2);
					}
				}
			}

			if(codigoGrau2 != null || nomeGrau2 != null || (saldoGrau2_1 != null || saldoGrau2_2 != null)){
				if(saldoGrau2_1.compareTo(BigDecimal.ZERO) != 0){
					comporMap(dadosPrincipal, grau1Grau2, codigoGrau2, grau3Grau2, grau4Grau2, grau5Grau2, nomeGrau2, saldoGrau2_1);
				}
				if(saldoGrau2_2.compareTo(BigDecimal.ZERO) != 0){
					if(saldoGrau2_1.compareTo(BigDecimal.ZERO) == 0)comporMap(dadosPrincipal, grau1Grau2, codigoGrau2, grau3Grau2, grau4Grau2, grau5Grau2, nomeGrau2, saldoGrau2_1);
					comporMap(dadosPrincipal, saldoGrau2_2);
				}
			}
		}

		BigDecimal totalDRE = buscarSdoResultado(dataRef1, isMensal);

		Extenso extenso = new Extenso(totalDRE.abs());
    	NumberFormat format = NumberFormat.getIntegerInstance();
    	format.setMinimumFractionDigits(2);
    	format.setGroupingUsed(true);
    	String termo = "RECONHECEMOS A EXATIDÃO DA PRESENTE DEMONSTRAÇÃO DE RESULTADO APRESENTANDO ";
    	if(totalDRE.compareTo(BigDecimal.ZERO) < 0){ //se total for negativo é lucro, senão prejuízo
    		params.put("TERMO", termo + (strPositivo == null ? "LUCRO LÍQUIDO" : strPositivo) +
						       " DE R\$ " + format.format((DecimalUtils.create(totalDRE).round(2)).get().abs()) + " (" + extenso + ").");
		}else{
			params.put("TERMO", termo + (strNegativo == null ? "PREJUÍZO" : strNegativo) +
					           " DE R\$ " + format.format((DecimalUtils.create(totalDRE).round(2)).get().abs()) + " (" + extenso + ").");
		}

    	params.put("ULTIMO_GRUPO", ultGrupo);

		if(dadosPrincipal == null || dadosPrincipal.size() == 0) throw new ValidacaoException("Nenhum registro encontrado para gerar o relatório. Verifique cadastros, contas, agrupamentos, lançamentos, período e parâmetros.");

		return dadosPrincipal;
	}

	private List<Abc10> buscarContasDRE(ConfigGrausCodigoContaContabil configGrausCodigoContaContabil, boolean consideraGrauEmpresa) {
		int qtdDigitos = consideraGrauEmpresa ? configGrausCodigoContaContabil.getDigitosGrauEmpresa() : configGrausCodigoContaContabil.getQtdDigAnteriorGrauEmpresa();

		return session.createQuery(" SELECT abc10id, abc10codigo, abc10nome, abc10class",
								   " FROM Abc10",
								   " WHERE abc10class = :class",
								   " AND ", Fields.length("abc10codigo"), " <= :qtdDigitos",
								   samWhere.getWhereGc("AND", Abc10.class),
								   " ORDER BY abc10codigo")
					  .setParameters("class", Abc10.CLASS_RESULTADO,
							  		 "qtdDigitos", qtdDigitos)
					  .getList(ColumnType.ENTITY);
	}

	public BigDecimal buscarSdoResultado(Integer dataRef, boolean isMensal) {
		BigDecimal saldo =  session.createQuery(" SELECT SUM(ebb02deb - ebb02cred) AS saldo FROM Ebb02",
												" INNER JOIN Abc10 ON ebb02cta = abc10id",
												" WHERE abc10class = :class",
												" AND abc10reduzido > 0",
												" AND ", Fields.numMeses("ebb02mes", "ebb02ano"), isMensal ? " = " : " <= ", ":numMeses",
												samWhere.getWhereGc("AND", Ebb02.class))
								    .setParameters("class", Abc10.CLASS_RESULTADO,
								    			   "numMeses", dataRef)
								  	.getUniqueResult(ColumnType.BIG_DECIMAL);

		return saldo != null ? saldo : BigDecimal.ZERO;
	}

	private List<TableMap> obterDadosNotasExplicativas(boolean termoNotasExplicativas, boolean assNotasExplicativas, Integer dataRef1) {
		params.put("TITULO_RELATORIO", "NOTAS EXPLICATIVAS");

		params.put("IMP_TERMO", termoNotasExplicativas ? 1 : 0);
		params.put("IMP_ASS", assNotasExplicativas ? 1 : 0);

		params.put("TERMO", "RECONHECEMOS A EXATIDÃO DAS NOTAS EXPLICATIVAS ACIMA DESCRITAS.");

		List<TableMap> dadosPrincipal = buscarNotasExplicativas(dataRef1);

		if(dadosPrincipal == null || dadosPrincipal.size() == 0)throw new ValidacaoException("Não foram encontradas notas explicativas na data informada.");

		return dadosPrincipal;
	}

	private List<TableMap> buscarNotasExplicativas(Integer dataRef) {
		return session.createQuery(" SELECT eba30titulo, eba30texto",
				   				   " FROM Eba30",
				   				   " WHERE ", Fields.numMeses("eba30mes", "eba30ano"), " = :numMeses",
				   				   samWhere.getWhereGc("AND", Eba30.class))
					  .setParameter("numMeses", dataRef)
					  .getListTableMap();
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<>();

		configurarPagina(filtrosDefault);

		filtrosDefault.put("anexarBalanceteAoDiario", false);

		configurarDatas(filtrosDefault);

		configurarDadosResponsavel(filtrosDefault);

		filtrosDefault.put("emitir", "0");

		configurarOpcoesImpressao(filtrosDefault);

		filtrosDefault.put("positivo", "LUCRO LÍQUIDO");
		filtrosDefault.put("negativo", "PREJUÍZO");

		configurarCheckDetalhamentoGrau(filtrosDefault);

		configurarMsgAtualizarCtas(filtrosDefault);

		return Utils.map("filtros", filtrosDefault);
	}

	private void configurarDadosResponsavel(Map<String, Object> filtrosDefault) {
		Aac10 aac10 = getVariaveis().getAac10();
		filtrosDefault.put("representante1", aac10.getAac10rNome());
		filtrosDefault.put("representante2", aac10.getAac10rCpf());
		filtrosDefault.put("assinatura1", aac10.getAac10cNome());
		filtrosDefault.put("assinatura2", aac10.getAac10cCrc());
	}

	private Eba40 obterUltimoLivro() {
		return getSession().createCriteria(Eba40.class)
				.addWhere(Criterions.eq("eba40livro", Eba40.LIVRO_DIARIO))
				.addWhere(Criterions.eq("eba40termos", Eba40.NAO))
				.addWhere(Criterions.where(getSamWhere().getWherePadrao("", Eba40.class)))
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

	private void configurarDatas(Map<String, Object> filtrosDefault) {
		LocalDate data = MDate.date();
		filtrosDefault.put("data1", data.format(DateTimeFormatter.ofPattern("MM/yyyy")));
		filtrosDefault.put("data2", data.format(DateTimeFormatter.ofPattern("MM/yyyy")));
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

	private void configurarCheckDetalhamentoGrau(Map<String, Object> filtrosDefault) {
		Integer grauEmpresa = obterGrauEmpresa();
		filtrosDefault.put("labelDetalhamentoGrau", "Considerar detalhamento até " + (grauEmpresa != null ? grauEmpresa : "") + "º grau.");
		filtrosDefault.put("consideraGrauEmpresa", false);
	}

	private void configurarOpcoesImpressao(Map<String, Object> filtrosDefault) {
		filtrosDefault.put("termoAtivo", false);
		filtrosDefault.put("termoPassivo", true);
		filtrosDefault.put("termoDRE", true);
		filtrosDefault.put("termoNotasExplicativas", false);

		filtrosDefault.put("assAtivo", false);
		filtrosDefault.put("assPassivo", true);
		filtrosDefault.put("assDRE", true);
		filtrosDefault.put("assNotasExplicativas", true);
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

	private Eba10 buscarAgrupamentoDeContas(String codGrupo) {
		String sql = "SELECT * FROM Eba10 WHERE eba10codigo = :codGrupo " + obterWherePadrao("Eba10");
		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("codGrupo", codGrupo));
	}

	private List<Long> buscarContasDoAgrupamento(Long eba10id) {
		return getSession().createCriteria("Eba1001")
						   .addFields("eba1001cta")
						   .addWhere(Criterions.eq("eba1001agrup", eba10id))
						   .getList(ColumnType.LONG);
	}

	private BigDecimal buscarSaldoPorAgrupamentoDeContas(List<Long> idsContas, Integer dataRef) {
		String sql = "SELECT SUM(ebb02saldo) FROM Ebb02 " +
					 "WHERE ebb02cta IN (:idsContas) " +
					 "AND" + Fields.numMeses("ebb02mes", "ebb02ano") + " <= :numMeses " +
					 obterWherePadrao("Ebb02") +
					 "GROUP BY ebb02ano, ebb02mes " +
					 "ORDER BY ebb02ano DESC, ebb02mes DESC";

		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idsContas", idsContas), Parametro.criar("numMeses", dataRef));
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
//meta-sis-eyJkZXNjciI6IlNHQyAtIEJhbGFuw6dvIFBhdHJpbW9uaWFsIGUgRFJFIiwidGlwbyI6InJlbGF0b3JpbyJ9