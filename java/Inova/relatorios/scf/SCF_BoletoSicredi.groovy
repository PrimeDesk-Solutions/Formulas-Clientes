/*
ULTIMA ALTERACAO: 25/06/2026	13:28
AUTOR: NAGYLA
*/

package Inova.relatorios.scf;

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Email
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aab1008
import sam.model.entities.aa.Aac10
import sam.model.entities.ab.Abe0101
import sam.model.entities.da.Daa01
import sam.server.cas.service.CAS1010Service
import sam.server.samdev.relatorio.DadosParaDownload
import sam.server.samdev.relatorio.RelatorioBase
import sam.server.samdev.relatorio.TableMapDataSource
import sam.server.samdev.utils.Parametro

import javax.mail.util.ByteArrayDataSource
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SCF_BoletoSicredi extends RelatorioBase {
	@Override
	public String getNomeTarefa() {
		return "SCF - Boleto Sicredi - Eltech";
	}

	@Override
	public Map<String, Object> criarValoresIniciais() {
		Map<String, Object> filtrosDefault = new HashMap<String, Object>();
		LocalDate dataProc = MDate.date();
		filtrosDefault.put("dataProc", dataProc);
		filtrosDefault.put("numeroInicial", "000000000");
		filtrosDefault.put("numeroFinal", "999999999");

		return Utils.map("filtros", filtrosDefault);
	}

	@Override
	public DadosParaDownload executar() {
		Integer movimento = getInteger("movimento");
		LocalDate dataProc = getLocalDate("dataProc") != null ? getLocalDate("dataProc") : MDate.date();
		String carteira = getString("carteira");
		String aceite = getString("aceite");
		String instrucao1 = getString("instrucao1");
		String instrucao2 = getString("instrucao2");
		String instrucao3 = getString("instrucao3");
		String sacadorAvalista = getString("sacadorAvalista");
		String cnpj = getString("cnpj");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidades = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		Long eaa01id = getLong("eaa01id");
		Long daa01id = getLong("idDoc");
		Long aab1008id = getLong("email");
		boolean enviaEmail = getBoolean("enviaEmail");
		String emailAssunto = getString("emailAssunto")

		// Filtros Do Script Tela SCF0101
		Long abe01id = getLong("abe01id")
		Long aah01id = getLong("aah01id")
		String parcela = getString("parcela") != "null" ? getString("parcela") : null

		params.put("dataProc", dataProc != null ? dataProc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
		params.put("instrucoes1", instrucao1);
		params.put("instrucoes2", instrucao2);
		params.put("instrucoes3", instrucao3);
		params.put("sacadorAvalista", sacadorAvalista);
		params.put("cnpj", cnpj);
		params.put("logoBanco", "LOGO_BANCO  |  999-9");

		File pasta = new File(".")
		String nomeClasse = getClass().getPackage().getName().replace(".", "/")
		String logo = pasta.getCanonicalPath().replace("\\", "/") + "/samdev/resources/" + nomeClasse + "/LogoSicredi.png"
		adicionarParametro("LOGOSICRED", logo)

		List<TableMap> dados = new ArrayList<>();
		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());

		List<TableMap> daa01s = null;
		List<Long> daa01ids = new ArrayList<Long>()

		if (eaa01id == null) {
			if (daa01id != null) {
				daa01ids.add(daa01id);
				daa01s = buscarDadosBoletoPelosIds(daa01ids);
			} else {
				daa01s = buscarDadosBoleto(carteira, aceite, movimento, numeroInicial, numeroFinal, entidades, dataVenc, abe01id, aah01id, parcela);
			}
		} else {
			daa01ids = buscarIdsDocsSCFPeloIdDocSRF(eaa01id);
			if (daa01ids == null || daa01ids.size() == 0) return null;
			daa01s = buscarDadosBoletoPelosIds(daa01ids);
		}

		String aac10endereco = ""
		aac10endereco += aac10.getAac10endereco()
		aac10endereco += ", "+ aac10.getAac10numero()
		aac10endereco += " " + aac10.getAac10bairro()
		aac10endereco += " " + aac10.getAac10municipio().getAag0201nome()
		aac10endereco += "/" + aac10.getAac10municipio().getAag0201uf().getAag02nome()

		for (TableMap daa01 : daa01s) {
			TableMap tm = new TableMap();
			tm.put("aac10endereco", aac10endereco.toUpperCase())
			TableMap json = daa01.getTableMap("abf01json");
			TableMap daa01Json = daa01.getTableMap("daa01json");

			carteira = json.getString("carteira");
			String posto = json.getString("posto_cooperativa")
			String identificador = json.getString("cod_identificador")
			String convenio = getString("cod_empresa");
			if (json != null) convenio = json.getString("cod_convenio");

			Integer INTcarteira = 0;
			String aux = carteira;
			if ("A".equals(carteira) || "1".equals(carteira)) {
				INTcarteira = 1;
			}
			carteira = Integer.toString(INTcarteira)
			String nossoNumero = daa01.getString("daa01nossoNum")
			nossoNumero = nossoNumero.substring(0, 2) + "/" + nossoNumero.substring(2);


			String campoLivre = campoLivrePorBanco(daa01.getString("abf01numero"), daa01.getString("abf01agencia"), daa01.getString("abf01conta"),posto, identificador,nossoNumero.toString(),daa01.getString("daa01nossonumdv"),daa01.getBigDecimal("daa01valor"));
			carteira = aux
			Long fatorVencimento = definirFatorVencimento(daa01.getDate("daa01dtVctoN"));

			tm.put("abe01id", daa01.getString("abe01id"));
			tm.put("abe01codigo", daa01.getString("abe01codigo"));
			tm.put("abe01nome", daa01.getString("abe01nome"));
			tm.put("abe01ni", daa01.getString("abe01ni"));
			tm.put("abe0101endereco", daa01.getString("abe0101endereco").toUpperCase());
			tm.put("abe0101numero", daa01.getString("abe0101numero"));
			tm.put("abe0101bairro", daa01.getString("abe0101bairro"));
			tm.put("aag0201nome", daa01.getString("aag0201nome"));
			tm.put("aag02uf", daa01.getString("aag02uf"));
			tm.put("daa01dtVctoN", daa01.getDate("daa01dtVctoN"));
			tm.put("daa01valor", daa01.getBigDecimal("daa01valor"));
//			tm.put("daa01nossoNum", daa01.getString("daa01nossoNum"));
			tm.put("daa01nossoNum", nossoNumero);
			tm.put("daa01nossoNumDv", daa01.getLong("daa01nossoNumDv"));
			tm.put("abb01data", daa01.getDate("abb01data"));
			tm.put("abb01num", daa01.getInteger("abb01num"));
			tm.put("abb01parcela", daa01.getString("abb01parcela"));
			tm.put("juros", daa01.getString("juros"));
			tm.put("multa", daa01.getString("multa"));
			tm.put("aac10rs", aac10.getAac10rs());
			tm.put("aac10ni", aac10.getAac10ni());
			tm.put("abf01agencia", daa01.getString("abf01agencia"));
			tm.put("abf01digagencia", daa01.getString("abf01digagencia"));
			tm.put("abf01conta", daa01.getString("abf01conta"));
			tm.put("abf01digconta", daa01.getString("abf01digconta"));
			tm.put("carteira", daa01.getString("carteira"));
			tm.put("identificador", daa01.getString("identificador"));
			tm.put("posto", daa01.getString("posto"));
			tm.put("aceite", daa01.getString("aceite"));
			tm.put("mens1_boleto", json.getString("mens1_boleto"));
			tm.put("mens2_boleto", json.getString("mens2_boleto"));
			tm.put("cod_identificador", json.getString("cod_identificador"));
			tm.put("aah01nome", daa01.getString("aah01nome"));
			String codigoBarras = montarCodigoBarras(fatorVencimento, daa01.getString("abf01numero"), campoLivre, daa01.getBigDecimal("daa01valor"));
			tm.put("codigoBarras", codigoBarras);
			String dvGeral = codigoBarras.substring(4, 5);
			String dvLivre = campoLivre.substring(24)
			tm.put("codigoLinhaDigitavel", montarLinhaDigitavel(carteira, daa01.getString("daa01nossonum"), daa01.getString("daa01nossonumdv"), daa01.getString("abf01agencia"), posto, identificador, dvGeral, dvLivre, fatorVencimento, daa01.getString("abf01numero"), campoLivre, daa01.getBigDecimal("daa01valor"), codigoBarras.subSequence(4, 5)));

			TableMap endereco = buscarEndereco(daa01.getLong("abe01id"))
			String enderecoCompleto = ""
			if(endereco != null ) {
				enderecoCompleto += endereco.getString("abe0101endereco") == null ? "" :  endereco.getString("abe0101endereco") +", "
				enderecoCompleto += endereco.getString("abe0101numero") == null ? "" :  endereco.getString("abe0101numero") + " "
				enderecoCompleto += endereco.getString("abe0101bairro") == null ? "" : endereco.getString("abe0101bairro") + " "
				enderecoCompleto += endereco.getString("aag0201nome") == null ? "" : endereco.getString("aag0201nome") + "/"
				enderecoCompleto += endereco.getString("aag02nome") == null ? "" : endereco.getString("aag02nome") + " "
				enderecoCompleto += endereco.getString("abe0101cep") == null ? "" : endereco.getString("abe0101cep") + " "

			}
			tm.put("endereco", (Normalizer.normalize(enderecoCompleto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]","")).toUpperCase())

			dados.add(tm)
		}

		TableMapDataSource dadosEnviar = new TableMapDataSource(dados);

		if (enviaEmail && aab1008id != null) {
			for (int i =0; i < dadosEnviar.recordCount; i++) {
				TableMap tm = dadosEnviar.records.get(i);
				Long idEnt = tm.getLong("abe01id");
				Abe0101 abe0101 = buscarEnderecoCobranca(idEnt);
				if (abe0101 == null || abe0101.abe0101eMail == null) continue;

				JasperReport report = carregarArquivoRelatorio("SCF_BoletoSicredi");
				List<TableMap> dadosReciboParaEmail = new ArrayList<>()
				dadosReciboParaEmail.add(tm)
				TableMapDataSource reciboParaEmail = new TableMapDataSource(dados);
				JasperPrint print = processarRelatorio(report, reciboParaEmail);

				Aab1008 aab1008 = buscarEnderecoRemetente(aab1008id)
				if (aab1008 == null) continue

				Email email = new Email(aab1008);
				email.assunto = emailAssunto;

				StringBuilder strCorpo = new StringBuilder("");
				strCorpo.append("Esta mensagem refere-se ao boleto sicred:");

				byte[] bytes = convertPrintToPDF(print);
				ByteArrayDataSource arquivoPdf = new ByteArrayDataSource(bytes, "application/pdf");
				String nomeArquivoPdf = "Boleto Sicred - " + tm.getString("abe01codigo") + " - " + tm.getString("abe01nome") + ".pdf";

				email.anexar(arquivoPdf, nomeArquivoPdf, nomeArquivoPdf);
				strCorpo.append("<p><p>");

				email.setCorpoMsg(strCorpo.toString());
				email.setEmailDestinoPara(abe0101.abe0101eMail);

				if (aab1008.aab1008assinatura != null) {
					CAS1010Service cas1010Service = instanciarService(CAS1010Service.class);
					email.adicionarAssinatura(cas1010Service.converterAssinaturaDoEmailEmInputStream(aab1008.aab1008assinatura));
				}

				email.enviar();
			}
		}

		return gerarPDF("SCF_BoletoSicredi", dados);
	}

	private TableMap buscarEndereco(Long id){
		String sql = " select abe0101endereco, abe0101numero, abe0101bairro, aag0201nome,aag02nome,abe0101cep from abe0101 "+
				" inner join abe01 on abe01id = abe0101ent  "+
				" inner join aag0201 on aag0201id = abe0101municipio "+
				" inner join aag02 on aag02id = aag0201uf "+
				" where abe01id = :id and abe0101principal = 1"
		Parametro paramId = Parametro.criar("id", id)
		return getAcessoAoBanco().buscarUnicoTableMap(sql,paramId)
	}

	private List<TableMap> buscarDadosBoleto(String carteira, String aceite, Integer movimento, Integer numeroInicial, Integer numeroFinal, List<Long> entidades, LocalDate[] dataVenc, Long abe01id, Long aah01id, String parcela) {

		String whereCarteira = carteira != null && carteira != "" ? " and abf01json->>'carteira' = :carteira" : "";
		String whereAceite = aceite != null && aceite != "" ? " and abf01json->>'aceite' =  :aceite" : "";
		String whereMovimento = movimento != null ? " and daa0102movim = :idMovimento" : "";
		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'" : "";
		String whereEntidades = entidades != null && entidades.size() > 0 ? " and abe01.abe01id in (:entidades) " : ""
		String whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " and daa01.daa01dtVctoN >= '" + dataVenc[0] + "' and daa01.daa01dtVctoN <= '" + dataVenc[1] + "'" : "";

		String whereAbe01id = abe01id != null ? " and abe01.abe01id = :abe01id \n" : ""
		String whereAah01id = aah01id != null ? " and aah01id = :aah01id \n" : ""
		String whereParcela = parcela != null ? " and abb01.abb01parcela = :parcela \n" : ""

		Parametro p1 = carteira != null && carteira != "" ? Parametro.criar("carteira", carteira) : null;
		Parametro p2 = aceite != null && aceite != "" ? Parametro.criar("aceite", aceite) : null;
		Parametro p3 = movimento != null ? Parametro.criar("idMovimento", movimento) : null;
		Parametro p4 = entidades != null && entidades.size() > 0 ? Parametro.criar("entidades", entidades) : null

		Parametro p5 = abe01id != null ? Parametro.criar("abe01id", abe01id) : null;
		Parametro p6 = aah01id != null ? Parametro.criar("aah01id", aah01id) : null;
		Parametro p7 = parcela != null ? Parametro.criar("parcela", parcela) : null;

		String sql = " SELECT abe01.abe01codigo, abe01.abe01id, abe01.abe01nome, abe01.abe01ni, abe0101.abe0101endereco, abe0101.abe0101numero, abe0101.abe0101bairro, aag0201.aag0201nome, aag02.aag02uf, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01nossoNum, daa01.daa01nossoNumDv, abb01.abb01num, abb01.abb01parcela, daa01.daa01json, \n" +
				" abb01.abb01data, abb01.abb01num, abf01.abf01agencia, abf01.abf01digagencia, abf01.abf01conta, abf01.abf01digconta, abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim, abf01.abf01json, abf01json->>'aceite' as aceite,abf01json->>'carteira' as carteira, abf01json->>'cod_identificador' as identificador, abf01json->>'posto_cooperativa' as posto,daa01json->>'juros' as juros, daa01json->>'multa' as multa \n" +
				" FROM daa01 daa01 \n" +
				" INNER JOIN abb01 abb01 ON abb01id = daa01central \n" +
				" INNER JOIN abe01 abe01 ON abe01id = abb01ent \n" +
				" LEFT JOIN abe0101 abe0101 ON abe01id = abe0101ent \n" +
				" INNER JOIN aag0201 aag0201 ON aag0201id = abe0101municipio \n" +
				" INNER JOIN aag02 aag02 ON aag02id = aag0201uf \n" +
				" INNER JOIN abf01 abf01 ON abf01id = daa01banco \n" +
				" LEFT JOIN Aah01 ON aah01id = abb01tipo \n" +
				" LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id \n" +
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				"and abf01numero = '748' \n" +
				"AND abe0101cobranca = 1 \n" +
				whereCarteira +
				whereAceite +
				whereMovimento +
				whereNumero +
				whereVencimento +
				whereEntidades +
				whereAbe01id +
				whereAah01id +
				whereParcela +
				" order by abb01num, abb01parcela"

		return getAcessoAoBanco().buscarListaDeTableMap(sql, p1, p2, p3, p4, p5, p6, p7);
	}

	private List<Long> buscarIdsDocsSCFPeloIdDocSRF(Long eaa01id) {
		String sql = " SELECT daa01id\n " +
				" FROM Daa01 \n" +
				" INNER JOIN Abb0102 ON daa01central = abb0102doc \n" +
				" INNER JOIN Abb01 ON daa01central = abb01id \n" +
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				" AND abb0102central = (SELECT eaa01central FROM Eaa01 WHERE eaa01id = :eaa01id) \n" +
				" AND daa01rp = 0 AND daa01previsao = 0 AND daa01banco IS NOT NULL \n" +
				" AND abb01quita = 0 ";

		List<Long> daa01ids = getAcessoAoBanco().obterListaDeLong(sql, Parametro.criar("eaa01id", eaa01id));
		return daa01ids;
	}

	private List<TableMap> buscarDadosBoletoPelosIds(List<Long> daa01ids) {
		String sql = " SELECT abe01.abe01codigo, abe01.abe01nome, abe01.abe01ni, abe0101.abe0101endereco, abe0101.abe0101numero, abe0101.abe0101bairro, aag0201.aag0201nome, aag02.aag02uf, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01nossoNum,daa01.daa01nossoNumDv, abb01.abb01num, abb01.abb01parcela, daa01.daa01json, " +
				" abb01.abb01data, abb01.abb01num, abf01.abf01agencia, abf01.abf01digagencia, abf01.abf01conta, abf01.abf01digconta, abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim, abf01.abf01json, abf01json->>'aceite' as aceite ,abf01json->>'carteira' as carteira, abf01json->>'cod_identificador' as identificador, abf01json->>'posto_cooperativa' as posto " +
				" FROM daa01 daa01 \n" +
				" INNER JOIN abb01 abb01 ON abb01id = daa01central \n" +
				" INNER JOIN abe01 abe01 ON abe01id = abb01ent \n" +
				" LEFT JOIN abe0101 abe0101 ON abe01id = abe0101ent \n" +
				" INNER JOIN aag0201 aag0201 ON aag0201id = abe0101municipio \n" +
				" INNER JOIN aag02 aag02 ON aag02id = aag0201uf \n" +
				" INNER JOIN abf01 abf01 ON abf01id = daa01banco \n" +
				" LEFT JOIN Aah01 ON aah01id = abb01tipo \n" +
				" LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id \n" +
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				" AND daa01id IN (:daa01ids) AND abe0101cobranca = 1 \n" +
				" ORDER BY daa01dtVctoN, daa01id";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("daa01ids", daa01ids));
		return receberDadosRelatorio;
	}

	private String montarCodigoBarras(Long fatorVencimento, String numeroBanco, String campoLivre, BigDecimal valor) {

		StringBuilder cbLeitor = new StringBuilder();
		cbLeitor.append("748"); // 1-3 = identificação do banco
		cbLeitor.append("9");//4 - código da moeda
		cbLeitor.append(formatarCampo("" + fatorVencimento, 4));//6-9 = fator de vencimento
		cbLeitor.append(formatarCampo("" + valor.multiply(100).intValue(), 10));//10-19 = valor
		cbLeitor.append(formatarCampo(campoLivre , 25));//20-44 = campo livre

		int dvGeral = modulo11DigitoGeral(cbLeitor.toString());
		String codLeitor = cbLeitor.toString().substring(0, 4) + dvGeral + cbLeitor.toString().substring(4);


		return codLeitor;
	}

	private String montarLinhaDigitavel(String carteira, String nossoNumero, String nossoNumeroDV, String agencia, String posto, String identificador, String dvGeral, String dvLivre, Long fatorVencimento, String numeroBanco, String campoLivre, BigDecimal valor, String digVerificador) {
		StringBuilder cbDigitavel = new StringBuilder();
		cbDigitavel.append("748"); // banco
		cbDigitavel.append(9); // moeda
		cbDigitavel.append(1); // tipo cobrança
		cbDigitavel.append(1); // carteira (verificar se tem q validar com o parametro carteira de quando for A jogar 1, por enquanto deixar 1)
		cbDigitavel.append(formatarCampo(nossoNumero,8).substring(0, 3)); // nosso numero
		cbDigitavel.append(modulo10(cbDigitavel.toString(), numeroBanco)); // DV 1º campo

		cbDigitavel.append(formatarCampo(nossoNumero,8).substring(3, 8)); // nosso numero
		cbDigitavel.append(nossoNumeroDV); // nosso num dv (usar o parametro nossoNumeroDV, por enquanto deixar 0)
		cbDigitavel.append(formatarCampo(agencia, 4)); // cooperativa-agencia beneficiaria
		cbDigitavel.append(modulo10(cbDigitavel.substring(10), numeroBanco)); // DV 2º campo

		cbDigitavel.append(formatarCampo(posto, 2)); // posto da corporativa
		cbDigitavel.append(formatarCampo(identificador, 5)) // codigo do beneficiário
		cbDigitavel.append(1); // VALOR
		cbDigitavel.append(0); // fixo
		cbDigitavel.append(dvLivre); // dvCampo livre
		cbDigitavel.append(modulo10(cbDigitavel.substring(21), numeroBanco)); // DV 3º campo

		cbDigitavel.append(dvGeral) // DV Geral

		cbDigitavel.append(fatorVencimento); // fator vencimento
		cbDigitavel.append(formatarCampo("" + valor.multiply(100).intValue(), 10)); //valor

		String codDig = cbDigitavel.toString().substring(0, 5) + ".";
		codDig = codDig + cbDigitavel.toString().substring(5, 10) + " ";
		codDig = codDig + cbDigitavel.toString().substring(10, 15) + ".";
		codDig = codDig + cbDigitavel.toString().substring(15, 21) + " ";
		codDig = codDig + cbDigitavel.toString().substring(21, 26) + ".";
		codDig = codDig + cbDigitavel.toString().substring(26, 32) + " ";
		codDig = codDig + cbDigitavel.toString().substring(32, 33) + " ";
		codDig = codDig + cbDigitavel.toString().substring(33);

		return codDig;
	}

	private String campoLivrePorBanco(String numeroBanco, String agencia, String conta, String posto, String identificador, String nossoNumero, String nossoNumeroDV, BigDecimal valor) {

		StringBuilder campoLivre = new StringBuilder("");
		if (numeroBanco == "748") { //Sicred
			campoLivre.append(1)// 1 = tipo de cobrança
			campoLivre.append(1);// 2 = carteira
			campoLivre.append(formatarCampo("" + nossoNumero + nossoNumeroDV, 9));//3-11 = nosso numero
			campoLivre.append(formatarCampo(agencia, 4));// 12 - 15 = cooperativa-agencia beneficiaria
			campoLivre.append(formatarCampo(posto, 2))//16- 17 = posto da corporativa
			campoLivre.append(formatarCampo(identificador, 5))//18 - 22 = codigo do beneficiário
			if (valor != null && valor != "" && valor != 0) {
				campoLivre.append("10")// 23
			}
			else {
				campoLivre.append("00")// 23
			}
			campoLivre.append(formatarCampo(modulo11DigitoCLivre(campoLivre.toString()).toString(),1))// 25 = digito verificador do campo livre calcludo por modulo 11

		}
		return campoLivre.toString();
	}

	private String formatarCampo(String valor, int tamanho) {
		String campo = StringUtils.extractNumbers(valor);
		campo = StringUtils.ajustString(campo, tamanho, '0', true);

		return campo;
	}

	private Long definirFatorVencimento(LocalDate data) {
		LocalDate dataBase = DateUtils.parseDate("07/10/1997");
		Long fator = DateUtils.dateDiff(dataBase, data, ChronoUnit.DAYS);

		if (fator > 9999) {
			fator = fator - 10000;
			fator = fator + 1000;
		}

		return fator;
	}

	private Integer modulo11DigitoCLivre(String codBarras) {
		int dv = 0;
		int soma = 0;
		int peso = 2;

		for(int i = 24; i > 0; i--) {
			int num = Integer.parseInt(codBarras.substring(i-1, i));
			num = num * peso;
			soma = soma + num;

			peso = peso == 9 ? 2 : peso+1;
		}
		dv = soma / 11;
		dv = dv * 11;
		dv = soma - dv;

		if(dv == 0 || dv == 1) {
			dv = 0;
		}else{
			dv = 11 - dv;
		}

		return dv;
	}

	private int modulo11DigitoGeral(String codBarras) {
		int dv = 0;
		int soma = 0;
		int peso = 2;

		for(int i = 43; i > 0; i--) {
			int num = Integer.parseInt(codBarras.substring(i-1, i));
			num = num * peso;
			soma = soma + num;

			peso = peso == 9 ? 2 : peso+1;
		}
		dv = soma % 11;

		if(dv == 0 || dv == 1 || dv == 10) {
			dv = 1;
		}else {
			dv = 11 - dv;
		}

		return dv;
	}

	private int modulo10(String codigo, String numeroBanco) {
		int dv = 0;
		int soma = 0;
		int peso = 2;
		codigo = StringUtils.ajustString(codigo, 25, '0', true);

		for (int i = 25; i > 0; i--) {
			int num = Integer.parseInt(codigo.substring(i - 1, i));
			num = num * peso;

			if (num >= 10) {
				String n = "" + num;
				int a = Integer.parseInt(n.substring(0, 1));
				int b = Integer.parseInt(n.substring(1));
				soma = soma + (a + b);
			} else {
				soma = soma + num;
			}

			peso = peso == 2 ? 1 : 2;
		}

		if (numeroBanco == "748") {
			int multiplo = soma % 10; //Multiplo de 10
			if(multiplo > 0) {
				multiplo = (soma - (soma % 10)) + 10;
				dv = multiplo - soma;
			}else {
				dv = multiplo;
			}
		}

		return dv;
	}

	// Buscar Endereco Cobranca
	public Abe0101 buscarEnderecoCobranca(Long idEnt) {
		return getSession().createCriteria(Abe0101.class)
				.addWhere(Criterions.eq("abe0101ent", idEnt))
				.addWhere(Criterions.eq("abe0101cobranca", 1))
				.setMaxResults(1)
				.get();
	}

	// Buscar Endereco Remetente
	public Aab1008 buscarEnderecoRemetente(Long aab1008id) {
		return getSession().createCriteria(Aab1008.class)
				.addWhere(Criterions.eq("aab1008id", aab1008id))
				.addWhere(Criterions.eq("aab1008empresa", obterEmpresaAtiva().getAac10id()))
				.get();
	}
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEJvbGV0byBTaWNyZWRpIC0gRWx0ZWNoIiwidGlwbyI6InJlbGF0b3JpbyJ9