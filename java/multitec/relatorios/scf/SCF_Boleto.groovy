package multitec.relatorios.scf;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils;
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils;
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.model.entities.aa.Aac10
import sam.model.entities.da.Daa01
import sam.server.samdev.relatorio.DadosParaDownload;
import sam.server.samdev.relatorio.RelatorioBase;
import sam.server.samdev.utils.Parametro

public class SCF_Boleto extends RelatorioBase{

	@Override
	public String getNomeTarefa() {
		return "SCF - Emitir Boleto";
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
		List<TableMap> dados = new ArrayList<>();

		List<Long> codigoBanco = getListLong("banco");
		Integer movimento = getInteger("movimento");
		LocalDate dataProc = getLocalDate("dataProc");
		String carteira = getString("carteira");
		String aceite = getString("aceite");
		String instrucao1 = getString("instrucao1");
		String instrucao2 = getString("instrucao2");
		String instrucao3 = getString("instrucao3");
		String sacadorAvalista = getString("sacadorAvalista");
		String cnpj = getString("cnpj");
		Integer numeroInicial = getInteger("numeroInicial");
		Integer numeroFinal = getInteger("numeroFinal");
		List<Long> entidade = getListLong("entidade");
		LocalDate[] dataVenc = getIntervaloDatas("dataVenc");
		Long eaa01id = getLong("eaa01id");

		params.put("dataProc", dataProc != null ? dataProc.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null);
		params.put("aceite", aceite);
		params.put("carteira", carteira);
		params.put("instrucoes1", instrucao1);
		params.put("instrucoes2", instrucao2);
		params.put("instrucoes3", instrucao3);
		params.put("sacadorAvalista", sacadorAvalista);
		params.put("cnpj", cnpj);
		params.put("logoBanco", "LOGO_BANCO  |  999-9");

		Aac10 aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());

		List<TableMap> daa01s = null;
		if(eaa01id == null) {
			daa01s = buscarDadosBoleto(codigoBanco, movimento, numeroInicial, numeroFinal, entidade, dataVenc);
		}else {
			List<Long> daa01ids = buscarIdsDocsSCFPeloIdDocSRF(eaa01id);
			if(daa01ids == null || daa01ids.size() == 0) return null;
			daa01s = buscarDadosBoletoPelosIds(daa01ids);
		}

		for (TableMap daa01 : daa01s) {
			TableMap tm = new TableMap();
			carteira = carteira == null ? daa01.getString("carteira") : carteira;
			String campoLivre = campoLivrePorBanco(daa01.getString("abf01numero"), daa01.getString("abf01agencia"), daa01.getString("abf01conta"), daa01.getLong("daa01nossoNum"), carteira);
			Long fatorVencimento = definirFatorVencimento(daa01.getDate("daa01dtVctoN"));

			tm.put("abe01nome", daa01.getString("abe01nome"));
			tm.put("abe01ni", daa01.getString("abe01ni"));
			tm.put("daa01dtVctoN", daa01.getDate("daa01dtVctoN"));
			tm.put("daa01valor", daa01.getBigDecimal("daa01valor"));
			tm.put("daa01nossonum", daa01.getLong("daa01nossoNum"));
			tm.put("daa01nossonumdv", daa01.getString("daa01nossonumdv"));
			tm.put("abb01data", daa01.getDate("abb01data"));
			tm.put("abb01num", daa01.getInteger("abb01num"));
			tm.put("desconto", daa01.getBigDecimal("desconto"));
			tm.put("aac10rs", aac10.getAac10rs());
			tm.put("aac10ni", aac10.getAac10ni());
			tm.put("abf01agencia", daa01.getString("abf01agencia"));
			tm.put("aah01nome", daa01.getString("aah01nome"));
			tm.put("aah01nome", daa01.getString("aah01nome"));
			tm.put("abf01agencia", daa01.getString("abf01agencia"));
			tm.put("abf01conta", daa01.getString("abf01conta"));
			tm.put("abf01digconta", daa01.getString("abf01digconta"));
			tm.put("aah01nome", daa01.getString("aah01nome"));
			tm.put("abe0101endereco", daa01.getString("abe0101endereco"));
			tm.put("abe0101numero", daa01.getString("abe0101numero"));
			tm.put("abe0101bairro", daa01.getString("abe0101bairro"));
			tm.put("abb01parcela", daa01.getString("abb01parcela"));
			tm.put("aag0201nome", daa01.getString("aag0201nome"));
			tm.put("aag02uf", daa01.getString("aag02uf"));
			tm.put("abe0101cep", daa01.getString("abe0101cep"));
			String codigoBarras = montarCodigoBarras(fatorVencimento, daa01.getString("abf01numero"), campoLivre, daa01.getBigDecimal("daa01valor"));
			tm.put("cb_leitor", codigoBarras);
			tm.put("cb_digitavel", montarLinhaDigitavel(fatorVencimento, daa01.getString("abf01numero"), campoLivre, daa01.getBigDecimal("daa01valor"), codigoBarras.subSequence(4, 5)));

			dados.add(tm)
		}

		//return gerarPDF("SCF_Boleto", dados);
		return gerarPDF("IDM_BOLETO", dados);

	}

	private List<TableMap> buscarDadosBoleto(List<Long> codigoBanco, Integer movimento, Integer numeroInicial, Integer numeroFinal, List<Long> entidade, LocalDate[] dataVenc) {
		String whereIdCodigoBanco = codigoBanco != null && codigoBanco.size() > 0 ? " and abf01.abf01id IN (:idCodigoBanco)": "";
		Parametro parametroBanco = codigoBanco != null && codigoBanco.size() > 0 ? Parametro.criar("idCodigoBanco", codigoBanco) : null;

		String whereMovimento = movimento != null ? " and daa0102.daa0102movim = :idMovimento": "";
		Parametro parametroMovimento = movimento != null ? Parametro.criar("idMovimento", movimento) : null;

		String whereNumero = numeroInicial != null && numeroFinal != null ? " and abb01.abb01num >= '" + numeroInicial + "' and abb01.abb01num <= '" + numeroFinal + "'": "";
		String whereVencimento = dataVenc != null && dataVenc[0] != null && dataVenc[1] != null ? " and daa01.daa01dtVctoN >= '" + dataVenc[0] + "' and daa01.daa01dtVctoN <= '" + dataVenc[1] + "'": "";

		String sql = "SELECT abe01.abe01codigo, abe0101endereco, abe0101numero, abe0101bairro, abe0101municipio, aag0201nome, aag02uf, abe0101cep, abe01.abe01nome, abe01.abe01ni, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01nossonum,daa01nossonumdv, abb01.abb01num, " +
				" abb01data, abb01num, abb01parcela, abf01.abf01agencia, abf01.abf01conta, abf01digconta,abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim,abf15codigo as carteira,cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto,cast(abf01json ->> 'aceito' as character varying(1)) as aceite,cast(abf01json ->> 'primeira_instrucao' as character varying(2)) as instrucao1,cast(abf01json ->> 'carteira' as character varying(3)) as carteira " +
				" FROM daa01 daa01 " +
				" INNER JOIN abb01 abb01 ON abb01id = daa01central " +
				" INNER JOIN abe01 abe01 ON abe01id = abb01ent " +
				" inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 " +
				" inner join aag0201 on abe0101municipio = aag0201id "+
				" inner join aag02 on aag0201uf = aag02id "+
				" INNER JOIN abf01 abf01 ON abf01id = daa01banco " +
				" LEFT JOIN Aah01 ON aah01id = abb01tipo " +
				" LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id " +
				"inner join abf15 on abf15id = daa01port "+
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				whereIdCodigoBanco +
				whereMovimento +
				whereNumero +
				whereVencimento+
				"order by abb01.abb01num ";


		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, parametroBanco, parametroMovimento);
		return receberDadosRelatorio;
	}

	private List<Long> buscarIdsDocsSCFPeloIdDocSRF(Long eaa01id) {
		String sql = " SELECT daa01id " +
				" FROM Daa01 " +
				" INNER JOIN Abb0102 ON daa01central = abb0102doc " +
				" INNER JOIN Abb01 ON daa01central = abb01id " +
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				" AND abb0102central = (SELECT eaa01central FROM Eaa01 WHERE eaa01id = :eaa01id) " +
				" AND daa01rp = 0 AND daa01previsao = 0 AND daa01banco IS NOT NULL " +
				" AND abb01quita = 0 ";

		List<Long> daa01ids = getAcessoAoBanco().obterListaDeLong(sql, Parametro.criar("eaa01id", eaa01id));
		return daa01ids;
	}

	private List<TableMap> buscarDadosBoletoPelosIds(List<Long> daa01ids) {
		String sql = "SELECT abe01.abe01codigo, abe0101endereco, abe0101numero, abe0101bairro, abe0101municipio, aag0201nome, aag02uf, abe0101cep, abe01.abe01nome, abe01.abe01ni, daa01.daa01dtVctoN, daa01.daa01valor, daa01.daa01nossonum,daa01nossonumdv, abb01.abb01num, " +
				" abb01data, abb01num, abb01parcela, abf01.abf01agencia, abf01.abf01conta, abf01digconta,abf01.abf01numero, aah01.aah01nome, daa0102.daa0102movim,abf15codigo as carteira,cast(daa01json ->> 'desconto' as numeric(18,6)) as desconto,cast(abf01json ->> 'aceito' as character varying(1)) as aceite,cast(abf01json ->> 'primeira_instrucao' as character varying(2)) as instrucao1,cast(abf01json ->> 'carteira' as character varying(3)) as carteira " +
				" FROM daa01 daa01 " +
				" INNER JOIN abb01 abb01 ON abb01id = daa01central " +
				" INNER JOIN abe01 abe01 ON abe01id = abb01ent " +
				" inner join abe0101 on abe0101ent = abe01id and abe0101principal = 1 " +
				" inner join aag0201 on abe0101municipio = aag0201id "+
				" inner join aag02 on aag0201uf = aag02id "+
				" INNER JOIN abf01 abf01 ON abf01id = daa01banco " +
				" LEFT JOIN Aah01 ON aah01id = abb01tipo " +
				" LEFT JOIN daa0102 daa0102 on daa0102.daa0102doc = daa01id " +
				"inner join abf15 on abf15id = daa01port "+
				getSamWhere().getWherePadrao(" WHERE ", Daa01.class) +
				" AND daa01id IN (:daa01ids) " +
				" ORDER BY daa01dtVctoN, daa01id";

		List<TableMap> receberDadosRelatorio = getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("daa01ids", daa01ids));
		return receberDadosRelatorio;
	}

	private String montarCodigoBarras(Long fatorVencimento, String numeroBanco, String campoLivre, BigDecimal valor) {

		StringBuilder cbLeitor = new StringBuilder();
		cbLeitor.append(formatarCampo(numeroBanco, 3));												            //01-03 - Identificação do banco
		cbLeitor.append(9);																						//04-04 - Código da moeda 9-Real
		cbLeitor.append(formatarCampo("" + fatorVencimento, 4));												//06-09 - Fator de vencimento
		cbLeitor.append(formatarCampo("" + valor.multiply(100).intValue(), 10));		                        //10-19 - Valor nominal do t�tulo
		cbLeitor.append(campoLivre);																			//20-44 - Campo livre

		int dv = modulo11DigitoGeral(cbLeitor.toString());														//05-05 - Dígito verificador
		String codLeitor = cbLeitor.toString().substring(0, 4) + dv + cbLeitor.toString().substring(4);

		return codLeitor;
	}

	private String montarLinhaDigitavel(Long fatorVencimento, String numeroBanco, String campoLivre, BigDecimal valor, String digVerificador) {
		StringBuilder cbDigitavel = new StringBuilder();
		cbDigitavel.append(formatarCampo(numeroBanco, 3));											//01-03 - Identificação do banco
		cbDigitavel.append(9);																		//04-04 - Código da moeda 9-Real
		cbDigitavel.append(campoLivre.substring(0, 5));												//05-09 - 5 primeiras posições do campo livre
		cbDigitavel.append(modulo10(cbDigitavel.toString(), numeroBanco));							//10-10 - Dígito verificador do primeiro campo
		cbDigitavel.append(campoLivre.substring(5, 15));											//11-20 - 6 a 15 posiçãoo do campo livre
		cbDigitavel.append(modulo10(campoLivre.substring(5, 15), numeroBanco));						//21-21 - Dígito verificador do segundo campo
		cbDigitavel.append(campoLivre.substring(15));												//22-31 - 16 a 25 posição do campo livre
		cbDigitavel.append(modulo10(campoLivre.substring(15), numeroBanco));						//32-32 - Dígito verificador do terceiro campo
		cbDigitavel.append(digVerificador);															//33-33 - Dígito verificador geral
		cbDigitavel.append(fatorVencimento);														//34-37 - Fator de vencimento
		cbDigitavel.append(formatarCampo("" + valor.multiply(100).intValue(), 10));	                //38-47 - Valor nominal do t�tulo

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

	private String campoLivrePorBanco(String numeroBanco, String agencia, String conta, Long nossoNumero, String carteira) {

		StringBuilder campoLivre = new StringBuilder("");

		if(numeroBanco == "237") { //Bradesco
			campoLivre.append(formatarCampo(agencia, 4));											                //20-23 - Agência
			campoLivre.append(formatarCampo("" + carteira, 2));													    //24-25 - Carteira
			campoLivre.append(StringUtils.ajustString(StringUtils.ajustString(nossoNumero, 11, '0', true), 2));	    //26-27	- Ano do Nosso número (dois primeiros digitos do nosso numero)
			campoLivre.append(formatarCampo("" + nossoNumero, 9));									                //28-36 - Nosso Número
			campoLivre.append(formatarCampo(conta, 7));											                    //37-43	- Conta do cedente
			campoLivre.append(0);																				    //44-44 - Zero fixo
		}

		return campoLivre.toString();
	}

	private String formatarCampo(String valor, int tamanho) {
		String campo = StringUtils.extractNumbers(valor);
		campo = StringUtils.ajustString(campo, tamanho, '0', true);

		return campo;
	}

	private Long definirFatorVencimento(LocalDate data) {
		// Novo calculo de fator de vencimento a partir do dia 22/02/2025
		LocalDate dtRef = DateUtils.parseDate("22/02/2025");
		Long fator;

		if(data >= LocalDate.of(2025,2,22)){ // Novo Calculo
			def novoContador = 1000
			def diferencaDatas = DateUtils.dateDiff(dtRef, data, ChronoUnit.DAYS )
			fator = novoContador + diferencaDatas
		}else{ // Calculo Antigo (Apagar depois do dia 22/02)
			LocalDate dataBase = DateUtils.parseDate("07/10/1997");
			fator = DateUtils.dateDiff(dataBase, data, ChronoUnit.DAYS);
		}

		return fator;
	}

	private Integer modulo11DigitoGeral(String codBarras) {
		int dv = 0;
		int soma = 0;
		int peso = 2;
		for(int i = 43; i > 0; i--) {
			Integer num = Integer.parseInt(codBarras.substring(i-1, i));
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

		for(int i = 25; i > 0; i--) {
			int num = Integer.parseInt(codigo.substring(i-1, i));
			num = num * peso;

			if(num >= 10) {
				String n = ""+num;
				int a = Integer.parseInt(n.substring(0, 1));
				int b = Integer.parseInt(n.substring(1));
				soma = soma + (a + b);
			}else {
				soma = soma + num;
			}

			peso = peso == 2 ? 1 : 2;
		}

		if(numeroBanco == "237") {
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
}
//meta-sis-eyJkZXNjciI6IlNDRiAtIEVtaXRpciBCb2xldG8iLCJ0aXBvIjoicmVsYXRvcmlvIn0=