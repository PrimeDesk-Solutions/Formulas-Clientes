package multitec.formulas.sgt.efd;

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac13
import sam.model.entities.aa.Aag01
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aah01
import sam.model.entities.aa.Aaj03
import sam.model.entities.aa.Aaj04
import sam.model.entities.aa.Aaj05
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj15
import sam.model.entities.aa.Aaj20
import sam.model.entities.aa.Aaj30
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb10
import sam.model.entities.ab.Abb11
import sam.model.entities.ab.Abb40
import sam.model.entities.ab.Abb4001
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abg02
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm0103
import sam.model.entities.ab.Abm12
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa01034
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ec.Eca01
import sam.model.entities.ec.Eca0101
import sam.model.entities.ec.Ecb0101
import sam.model.entities.ed.Edb10
import sam.model.entities.ed.Edb1001
import sam.model.entities.ed.Edb10011
import sam.model.entities.ed.Edb100111
import sam.model.entities.ed.Edb11
import sam.model.entities.ed.Edb12
import sam.model.entities.ed.Edb13
import sam.model.entities.ed.Edb14
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

public class Leiaute15_2021_Contribuicoes extends FormulaBase {
	private static final DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy");
	private static final DateTimeFormatter MMyyyy = DateTimeFormatter.ofPattern("MMyyyy");

	private LocalDate dtInicial;
	private LocalDate dtFinal;
	private Integer situacao;
	private String numRecibo;
	private BigDecimal aliqPisF150;
	private BigDecimal aliqCofinsF150;
	private Aac10 empresaMatriz;
	private Aac13 aac13;

	private TextFile txt1;
	private TextFile txt2;

	private Integer mes;
	private Integer ano;

	private Set<Abb40> abb40sReg1010 = new HashSet<Abb40>();
	private Set<Abb40> abb40sReg1020 = new HashSet<Abb40>();
	private Map<String, TableMap> map0150 = new HashMap();
	private Map<String, TableMap> map0190 = new HashMap();
	private Map<String, TableMap> map0200 = new HashMap();
	private Map<String, TableMap> map0400 = new HashMap();
	private Map<String, TableMap> map0450 = new HashMap();
	private Set<String> abc10s = new HashSet<String>();
	private Set<Abb11> abb11s = new HashSet<Abb11>();

	private List<Aac10> aac10s;
	private Edb10 edb10;

	private Boolean contemDadosBlocoP;
	private Boolean gerouF550;

	private List<String> modelosC100 = Utils.list("01","1B","04","55","65");
	private List<String> modelosC300 = Utils.list("02","2D","2E","59");
	private List<String> modelosC500 = Utils.list("06", "66", "28", "29");
	private List<String> modelosC800 = Utils.list("59");
	private List<String> modelosD100 = Utils.list("07", "08", "09", "10", "11", "26", "27", "57");
	private List<String> modelosD500 = Utils.list("21", "22");
	
	private static final Integer versaoLeiaute = 6;
	private static final String alinEFD = "0051";

	private int qtLinBloco0, qtLin0111, qtLin0120, qtLin0140, qtLin0145, qtLin0150, qtLin0190, qtLin0200, qtLin0205, qtLin0206, qtLin0400, qtLin0450, qtLin0460, qtLin0500, qtLin0600,
	            qtLinBlocoA, qtLinA010, qtLinA100, qtLinA110, qtLinA111, qtLinA120, qtLinA170, 
				qtLinBlocoC, qtLinC010, qtLinC100, qtLinC110, qtLinC111, qtLinC120, qtLinC170, qtLinC175, qtLinC395, qtLinC396, qtLinC500, qtLinC501, qtLinC505, qtLinC509, qtLinC860, qtLinC870, qtLinC890,
	            qtLinBlocoD, qtLinD010, qtLinD100, qtLinD101, qtLinD105, qtLinD111, qtLinD500, qtLinD501, qtLinD505, qtLinD509, 
				qtLinBlocoF, qtLinF010, qtLinF100, qtLinF111, qtLinF120, qtLinF130, qtLinF150, qtLinF550, qtLinF559, qtLinF600, qtLinF700, qtLinF800, 
				qtLinBlocoM, 
				qtLinBlocoP, qtLinP010, qtLinP100, qtLinP200, qtLinP210, 
				qtLinBloco1, qtLin1010, qtLin1020, qtLin1900,
	            qtLinBloco9, qtLin9900;

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_EFD;
	}

	@Override
	public void executar() {
		dtInicial = get("dtInicial");
		dtFinal = get("dtFinal");
		situacao = get("situacao");
		numRecibo = get("numRecibo");
		aliqPisF150 = get("aliqPisF150");
		aliqCofinsF150 = get("aliqCofinsF150");
		
		buscarEmpresaMatriz();
		
		aac13 = empresaMatriz.aac13fiscal;
		if(aac13 == null) throw new ValidacaoException("Necessário informar as informações fiscais no cadastro da empresa matriz: " + empresaMatriz.aac10codigo + ".");

		selecionarAlinhamento(alinEFD);

		txt1 = new TextFile("|");
		txt2 = new TextFile("|");

		mes = dtInicial.getMonthValue();
		ano = dtFinal.getYear();

		/**
		 * Verifica se a receita bruta do período foi apurada
		 */
		edb10 = buscarApuracaoDeReceitaPorMesAno(mes, ano);
		if(edb10 == null) throw new ValidacaoException("Necessário fazer a apuração da receita do período - SGT1510.");

		aac10s = Utils.list(empresaMatriz);
		buscarEmpresasFiliais();
		
		inicializarContadores();

		gerarAberturaBloco0();

		gerarBlocoA();

		gerarBlocoC();

		gerarBlocoD();

		gerarBlocoF();

		gerarBlocoM();

		gerarBlocoP();

		gerarBloco1()

		gerarFechamentoBloco0();

		gerarBloco9();
		
		TextFile efd = new TextFile();
		efd.print(txt1);
		efd.print(txt2);
		
		put("dadosArquivo", efd);
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * *  BLOCO 0: ABERTURA, IDENTIFICAÇÃO E REFERÊNCIAS * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarAberturaBloco0() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco 0...");
		
		/**
		 * REGISTRO 0000: Abertura do arquivo digital e identificação da entidade
		 */
		txt1.print("0000");
		txt1.print(versaoLeiaute, 3);
		txt1.print(numRecibo == null ? 0 : 1);
		txt1.print(situacao == 9 ? null : situacao);
		txt1.print(numRecibo);
		txt1.print(dtInicial.format(ddMMyyyy));
		txt1.print(dtFinal.format(ddMMyyyy));
		txt1.print(empresaMatriz.aac10rs);
		txt1.print(StringUtils.extractNumbers(empresaMatriz.aac10ni));
		txt1.print(empresaMatriz.aac10municipio == null ? null : empresaMatriz.aac10municipio.aag0201uf.aag02uf);
		txt1.print(empresaMatriz.aac10municipio == null ? 0 : empresaMatriz.aac10municipio.aag0201ibge, 7, '0', true);
		txt1.print(retirarMascara(empresaMatriz.aac10suframa));
		txt1.print(aac13.aac13natPJ, 2);
		txt1.print(aac13.aac13tipoAtiv);
		txt1.newLine();
		qtLinBloco0++;

		/**
		 * REGISTRO 0001: Abertura do Bloco 0
		 */
		txt1.print("0001");
		txt1.print(0);
		txt1.newLine();
		qtLinBloco0++;

		/**
		 * REGISTRO 0100: Dados do Contabilista (obtenção dos dados)
		 */
		txt1.print("0100");
		txt1.print(empresaMatriz.aac10cNome);
		txt1.print(StringUtils.extractNumbers(empresaMatriz.aac10cCpf), 11, '0', true);
		String crc = empresaMatriz.aac10cCrc == null ? "" : empresaMatriz.aac10cCrc;
		txt1.print(crc.length() > 15 ? crc.substring(0, 15) : crc);
		txt1.print(empresaMatriz.aac10cCnpj == null ? null : StringUtils.ajustString(StringUtils.extractNumbers(empresaMatriz.aac10cCnpj), 14, '0', true));
		txt1.print(empresaMatriz.aac10cCep == null ? null : empresaMatriz.aac10cCep);
		txt1.print(empresaMatriz.aac10cEndereco);
		txt1.print(empresaMatriz.aac10cNumero);
		txt1.print(empresaMatriz.aac10cComplem);
		txt1.print(empresaMatriz.aac10cBairro);
		txt1.print(empresaMatriz.aac10cFone == null ? null : empresaMatriz.aac10cDddFone == null ? empresaMatriz.aac10cFone : empresaMatriz.aac10cDddFone + empresaMatriz.aac10cFone);
		txt1.print(null);
		txt1.print(empresaMatriz.aac10cEmail);
		txt1.print(empresaMatriz.aac10cMunicipio == null ? null : empresaMatriz.aac10cMunicipio.aag0201ibge, 7, '0', true);
		txt1.newLine();
		qtLinBloco0++;

		/**
		 * REGISTRO 0110: Regimes de Apuração da Contribuição Social e de Apropriação de Crédito
		 */
		txt1.print("0110");
		txt1.print(edb10.edb10incidTrib);
		txt1.print(aac13.aac13metAprop == 0 ? null : aac13.aac13metAprop);
		txt1.print(aac13.aac13contribApur);
		txt1.print(null);
		txt1.newLine();
		qtLinBloco0++;

		/**
		 * REGISTRO 0111: Tabela de Receita Bruta Mensal para Fins de Rateio de Créditos Comuns
		 */
		if(aac13.aac13metAprop == 2){
			txt1.print("0111");
			txt1.print(formatarValor(edb10.edb10rbTrib, 2));
			txt1.print(formatarValor(edb10.edb10rbNaoTrib, 2));
			txt1.print(formatarValor(edb10.edb10rbExp, 2));
			txt1.print(formatarValor(edb10.edb10rbCumul, 2));
			txt1.print(formatarValor(edb10.edb10rbTrib.add(edb10.edb10rbNaoTrib).add(edb10.edb10rbExp).add(edb10.edb10rbCumul), 2));
			txt1.newLine();
			qtLin0111++;
			qtLinBloco0++;
		}
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * BLOCO A: DOCUMENTOS FISCAIS - SERVIÇOS  * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoA() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco A...");
		
		/**
		 * REGISTRO A - Abertura Bloco A
		 */

		boolean contemDadosBlocoA = contemDadosBlocoA(aac10s);

		txt2.print("A001");
		txt2.print(contemDadosBlocoA ? 0 : 1);
		txt2.newLine();
		qtLinBlocoA++;

		/**
		 * REGISTRO A010 - Identificação do Estabelecimento
		 */
		if(contemDadosBlocoA) {
			Set<Long> setGrupoEA = new HashSet<Long>(); //Grupos centralizadores que já foram gerados EFD

			for(aac10 in aac10s) {
				Long gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
				if(setGrupoEA.contains(gcEA)) continue;
				setGrupoEA.add(gcEA);

				List<Long> eaa01Ids = new ArrayList<Eaa01>();
				
				def pagina = 0;
				List<Long> eaa01sEnt = buscarDocumentosParaEFDEntradaA100(gcEA, pagina);
				while(eaa01sEnt.size() > 0) {
					for(eaa01 in eaa01sEnt) {
						eaa01Ids.add(eaa01);
					}
					pagina++;
					eaa01sEnt = buscarDocumentosParaEFDEntradaA100(gcEA, pagina);

				}
				pagina = 0;
				List<Long> eaa01sSai = buscarDocumentosParaEFDSaidaA100(gcEA, pagina);
				while(eaa01sSai.size() > 0) {
					for(eaa01 in eaa01sSai) {
						eaa01Ids.add(eaa01);
					}
					pagina++;
					eaa01sSai = buscarDocumentosParaEFDSaidaA100(gcEA, pagina);
				}
				
				if(eaa01Ids == null || eaa01Ids.size() == 0) continue;

				txt2.print("A010");
				txt2.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt2.newLine();
				qtLinBlocoA++;
				qtLinA010++;
				
				/**
				 * REGISTRO A100: Nota Fiscal de Serviço
 				*/
				for(Long eaa01id : eaa01Ids) {
					Eaa01 eaa01 = getSession().get(Eaa01.class, eaa01id);
					if(eaa01 != null) {
						validacoes(eaa01);

						Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
						Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
						
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro A100 - Documento: " + abb01.abb01num);
						
						Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
						Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
						TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
						
						boolean situacaoDocumentoEspecial = "02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd) || "04".equals(aaj03.aaj03efd) || "05".equals(aaj03.aaj03efd);

						def serie = formatarSerie(abb01.abb01serie, aah01.aah01modelo);

						if(situacaoDocumentoEspecial) {
							txt2.print("A100");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(null);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.newLine();

						}else {
							Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

							txt2.print("A100");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(abe01.abe01codigo);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(eaa01.eaa01nfeChave);
							txt2.print(eaa01.eaa01esMov == 0 ? eaa01.eaa01esData == null ? abb01.abb01data.format(ddMMyyyy) : eaa01.eaa01esData.format(ddMMyyyy) : abb01.abb01data.format(ddMMyyyy));
							txt2.print(eaa01.eaa01esMov == 0 ? eaa01.eaa01esData == null ? abb01.abb01data.format(ddMMyyyy) : eaa01.eaa01esData.format(ddMMyyyy) : abb01.abb01data.format(ddMMyyyy));
							txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
							txt2.print(verificarTipoPgtoAntiga(eaa01, abb01));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_DESC")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_BC_PIS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_PIS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_BC_COFINS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_COFINS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_PIS_RET")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_COFINS_RET")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("A100", "VL_ISS")), 2));
							txt2.newLine();

							comporRegistro0150(aac10, abe01.abe01id);
						}

						qtLinBlocoA++;
						qtLinA100++;

						if(!situacaoDocumentoEspecial) {
							/**
							 * REGISTRO A110: Informação complementar da Nota Fiscal
							 */
							def mapRegistroA110 = comporRegistroInfComplementar(aac10, eaa01.eaa01obsFisco);
							if(mapRegistroA110 != null && mapRegistroA110.size() > 0) {
								for(int j = 0; j < mapRegistroA110.size(); j++) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro A110 - Documento: " + abb01.abb01num);
									
									txt2.print("A110");
									txt2.print(mapRegistroA110.get(j).getString("cod_inf"));
									txt2.print(mapRegistroA110.get(j).getString("txt_compl"));
									txt2.newLine();
									qtLinBlocoA++;
									qtLinA110++;
								}
							}

							/**
							 * REGISTRO A111: Processo Referenciado
							 */
							Abb40 abb40 = eaa0102.eaa0102processo;
							if(abb40 != null) {
								abb40 = getSession().get(Abb40.class, eaa0102.eaa0102processo.abb40id);
								
								if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
								if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro A111 - Documento: " + abb01.abb01num);
								
								txt2.print("A111");
								txt2.print(abb40.abb40num);
								txt2.print(abb40.abb40indProc);
								txt2.newLine();
								qtLinBlocoA++;
								qtLinA111++;
							}

							/**
							 * REGISTRO A120: Operações de Importação (Código 01)
							 */
							if(eaa01.eaa01esMov == 0) {
								List<Eaa01034> eaa01034s = buscarDeclaracoesDeImportacao(eaa01.eaa01id);
								for(Eaa01034 eaa01034 : eaa01034s) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro A120 - Documento: " + abb01.abb01num);
									
									txt2.print("A120");
									txt2.print(formatarValor(eaa01034.eaa01034total, 2));
									txt2.print(formatarValor(eaa01034.eaa01034bcPis, 2));
									txt2.print(formatarValor(eaa01034.eaa01034pis, 2));
									txt2.print(eaa01034.eaa01034pgtoPis == null ? null : eaa01034.eaa01034pgtoPis.format(ddMMyyyy));
									txt2.print(formatarValor(eaa01034.eaa01034bcCofins, 2));
									txt2.print(formatarValor(eaa01034.eaa01034cofins, 2));
									txt2.print(eaa01034.eaa01034pgtoCofins == null ? null : eaa01034.eaa01034pgtoCofins);
									txt2.print(eaa01034.eaa01034servExt);
									txt2.newLine();
									qtLinBlocoA++;
									qtLinA120++;
								}
							}

							/**
							 * REGISTRO A170: Itens do Documento
							 */
							List<Eaa0103> eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
							if(eaa0103s != null && eaa0103s.size() > 0) {
								for(eaa0103 in eaa0103s) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro A170 - Documento: " + abb01.abb01num);
									
									TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
									
									Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
									comporRegistro0200(aac10, abm01.abm01id);

									Aam06 aam06 = abm01.abm01umu != null ? getSession().get(Aam06.class, abm01.abm01umu.aam06id) : null;
									if(aam06 != null) comporRegistro0190(aac10, aam06);

									txt2.print("A170");
									txt2.print(eaa0103.eaa0103seq);
									txt2.print(abm01.abm01codigo);
									txt2.print(eaa0103.eaa0103complem);
									txt2.print(formatarValor(eaa0103.eaa0103totDoc, 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_DESC")), 2));

									Aaj30 aaj30 = eaa0103.eaa0103codBcCred != null ? getSession().get(Aaj30.class, eaa0103.eaa0103codBcCred.aaj30id) : null;
									txt2.print(aaj30 == null ? null : aaj30.aaj30codigo);
									
									txt2.print(eaa0103.eaa0103origemCred == 0 ? null : eaa0103.eaa0103origemCred == 1 ? 0 : 1);
									txt2.print(selecionarCSTPis(eaa01.eaa01esMov, eaa0103, jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_BC_PIS"))));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_BC_PIS")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "ALIQ_PIS")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_PIS")), 2));
									txt2.print(selecionarCSTCofins(eaa01.eaa01esMov, eaa0103, jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_BC_COFINS"))));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_BC_COFINS")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "ALIQ_COFINS")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("A170", "VL_COFINS")), 2));
									
									Abc10 abc10 = eaa0103.eaa0103cta == null ? null : getSession().get(Abc10.class, eaa0103.eaa0103cta.abc10id);
									txt2.print(abc10 == null ? null : abc10.abc10codigo);
									if(abc10 != null) abc10s.add(abc10.abc10codigo);
									
									txt2.print(null);
									txt2.newLine();
									qtLinBlocoA++;
									qtLinA170++;
									
									
								}
							}
						}
					}
				}
			}
		}

		/**
		 * REGISTRO A990 - Encerramento do Bloco A
		 */
		qtLinBlocoA++;

		txt2.print("A990");
		txt2.print(qtLinBlocoA);
		txt2.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * *  BLOCO C: DOCUMENTOS FISCAIS I - MERCADORIAS (ICMS/IPI) * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoC() {

		/**
		 * REGISTRO C001 - Abertura Bloco C
		 */
		boolean contemDadosBlocoC = verificarSeContemDadosBlocoC(aac10s);

		txt2.print("C001");
		txt2.print(contemDadosBlocoC ? 0 : 1);
		txt2.newLine();
		qtLinBlocoC++;

		/**
		 * REGISTRO C010 - Identificação do Estabelecimento
		 */
		if(contemDadosBlocoC) {
			Set<Long> setGruposEA = new HashSet<>(); //GC ja gerado

			for(Aac10 aac10 : aac10s) {
				Long gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
				if(setGruposEA.contains(gcEA))continue;
				setGruposEA.add(gcEA);

				boolean temMovBlocoCparaAEmpresa = verificarSeContemDadosBlocoC(aac10);
				if(!temMovBlocoCparaAEmpresa) continue;

				txt2.print("C010");
				txt2.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt2.print("2");
				txt2.newLine();
				qtLinBlocoC++;
				qtLinC010++;

				gerarC100(aac10, gcEA);
				gerarC300(aac10, gcEA);
				gerarC500(aac10, gcEA);
				gerarC800(aac10, gcEA);
			}
		}

		/**
		 * REGISTRO C990 - Encerramento do Bloco C
		 */
		qtLinBlocoC++;

		txt2.print("C990");
		txt2.print(qtLinBlocoC);
		txt2.newLine();
	}

	def gerarC100(Aac10 aac10, Long gcEA) {
		/**
		 * REGISTRO C100: Nota Fiscal (Código 01), Nota Fiscal Avulsa (Código 1B), Nota Fiscal de Produtor (Código 04), NFE (Código 55) e NFCe (Código 65)
		 *
		 * - Documentos cuja situação seja 02, 03, 04 ou 05 preencher somente os campos: reg, ind_oper, ind_emit, cod_mod, cod_sit,
		 * ser e num_doc e os demais campos deixar vazio "||" e NÃO informar registros filhos. Se for NFe de emissão própria e na situação
		 * 02 ou 03, preencher também o campo chv_nfe.
		 */
		Set<Long> eaa01IdsC100 = new HashSet<Long>();

		List<Eaa01> eaa01sEnt = buscarDocumentosC100PCEntrada(gcEA, modelosC100);
		for(Eaa01 eaa01 : eaa01sEnt) {
			eaa01IdsC100.add(eaa01.eaa01id);
		}

		List<Eaa01> eaa01sSai = buscarDocumentosC100PCSaída(gcEA, modelosC100);
		for(Eaa01 eaa01 : eaa01sSai) {
			eaa01IdsC100.add(eaa01.eaa01id);
		}

		for(Long eaa01id : eaa01IdsC100) {
			Eaa01 eaa01 = getSession().get(Eaa01.class, eaa01id);
			if(eaa01 != null) {
				validacoes(eaa01);
				
				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
				Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
				TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
				
				def modelo = aah01.aah01modelo;
				boolean situacaoDocumentoEspecial = "02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd) || "04".equals(aaj03.aaj03efd) || "05".equals(aaj03.aaj03efd);
				def serie = formatarSerie(abb01.abb01serie, modelo);

				if(situacaoDocumentoEspecial) {
					txt2.print("C100");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(null);
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(abb01.abb01num);
					txt2.print(eaa01.eaa01nfeChave);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.newLine();

				}else {
					Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

					txt2.print("C100");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(abe01.abe01codigo);
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(abb01.abb01num);
					txt2.print(eaa01.eaa01nfeChave);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.compareTo(dtFinal) > 0 ? null : eaa01.eaa01esData.format(ddMMyyyy));
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(verificarTipoPgto(eaa01, abb01));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_DESC")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_ABAT_NT")), 2));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(eaa0102.eaa0102frete == null ? 9 : eaa0102.eaa0102frete);
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_FRT")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_SEG")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_OUT_DA")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_BC_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_BC_ICMS_ST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_ICMS_ST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_IPI")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_PIS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_COFINS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_PIS_ST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C100", "VL_COFINS_ST")), 2));
					txt2.newLine();

					comporRegistro0150(aac10, abe01.abe01id);
				}
				qtLinBlocoC++;
				qtLinC100++;

				if(!situacaoDocumentoEspecial) {
					/**
					 * REGISTRO C110: Informação complementar da Nota Fiscal (Código 01, 1B, 04 e 55)
					 */
					def mapRegistroC110 = comporRegistroInfComplementar(aac10, eaa01.eaa01obsFisco);
					if(mapRegistroC110 != null && mapRegistroC110.size() > 0) {
						for(int j = 0; j < mapRegistroC110.size(); j++) {
							txt2.print("C110");
							txt2.print(mapRegistroC110.get(j).getString("cod_inf"));
							txt2.print(mapRegistroC110.get(j).getString("txt_compl"));
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC110++;
						}
					}

					/**
					 * REGISTRO C111: Processo Referenciado
					 */
					Abb40 abb40 = eaa0102.eaa0102processo;
					if(abb40 != null) {
						abb40 = getSession().get(Abb40.class, eaa0102.eaa0102processo.abb40id);
						if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
						if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

						txt2.print("C111");
						txt2.print(abb40.abb40num);
						txt2.print(abb40.abb40indProc);
						txt2.newLine();
						qtLinBlocoC++;
						qtLinC111++;
					}


					/**
					 * REGISTRO C120: Operações de Importação
					 * Documentos de entrada
					 */
					if(eaa01.eaa01esMov == 0) {
						List<Eaa01034> eaa01034s = buscarDeclaracoesDeImportacao(eaa01.eaa01id);
						for(Eaa01034 eaa01034 : eaa01034s) {
							txt2.print("C120");
							txt2.print(eaa01034.eaa01034decSimp);
							txt2.print(retirarMascara(eaa01034.eaa01034num));
							txt2.print(formatarValor(eaa01034.eaa01034pis, 2));
							txt2.print(formatarValor(eaa01034.eaa01034cofins, 2));
							txt2.print(eaa01034.eaa01034drawback);
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC120++;
						}
					}

					/**
					 * REGISTRO C170: Itens do Documento (Código 01, 1B, 04 e 55)
					 */
					List<Eaa0103> eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa0103doc", Parametro.criar("eaa0103doc", eaa01.eaa01id));
					if(eaa0103s != null && eaa0103s.size() > 0) {
						for(Eaa0103 eaa0103 : eaa0103s) {
							Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
							comporRegistro0200(aac10, abm01.abm01id);

							TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
							
							txt2.print("C170");
							txt2.print(eaa0103.eaa0103seq);
							txt2.print(eaa0103.eaa0103codigo);
							txt2.print(eaa0103.eaa0103complem);
							txt2.print(formatarValor(eaa0103.eaa0103qtComl, 5));

							Aam06 aam06 = eaa0103.eaa0103umComl != null ? getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id) : null
							txt2.print(aam06 == null ? null : aam06.aam06codigo);
							comporRegistro0190(aac10, aam06);

							txt2.print(formatarValor(eaa0103.eaa0103total, 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_DESC")), 2));

							txt2.print(movimentouEstoque(eaa01, eaa0103) ? 0 : 1);

							Aaj10 aaj10 = eaa0103.eaa0103cstIcms == null ? null : getSession().get(Aaj10.class, eaa0103.eaa0103cstIcms.aaj10id);
							txt2.print(aaj10 == null ? null : aaj10.aaj10codigo);

							Aaj15 aaj15 = eaa0103.eaa0103cfop == null ? null : getSession().get(Aaj15.class, eaa0103.eaa0103cfop.aaj15id);
							txt2.print(aaj15 == null ? null : aaj15.aaj15codigo);

							Abb10 abb10 = abb01.abb01operCod == null ? null : getSession().get(Abb10.class, abb01.abb01operCod.abb10id);
							txt2.print(abb10 == null ? null : abb10.abb10codigo);
							if(abb10 != null) comporRegistro0400(aac10, abb10.abb10id);

							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_ICMS_ST")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_ST")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_ICMS_ST")), 2));
							txt2.print("0");

							Aaj11 aaj11 = eaa0103.eaa0103cstIpi == null ? null : getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id);
							txt2.print(aaj11 == null ? null : aaj11.aaj11codigo);
							txt2.print(eaa0103.eaa0103codEnqIpi);
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_IPI")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_IPI")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_IPI")), 2));
							
							txt2.print(selecionarCSTPis(eaa01.eaa01esMov, eaa0103, jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_PIS"))));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_PIS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_PIS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "QUANT_BC_PIS")), 3, false));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_PIS_QUANT")), 4, false));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_PIS")), 2));
							
							txt2.print(selecionarCSTCofins(eaa01.eaa01esMov, eaa0103, jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_COFINS"))));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_BC_COFINS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_COFINS")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "QUANT_BC_COFINS")), 3, false));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "ALIQ_COFINS_QUANT")), 4, false));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C170", "VL_COFINS")), 2));
							
							Abc10 abc10 = eaa0103.eaa0103cta == null ? null : getSession().get(Abc10.class, eaa0103.eaa0103cta.abc10id);
							txt2.print(abc10 == null ? null : abc10.abc10codigo);
							if(abc10 != null) abc10s.add(abc10.abc10codigo);
							
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC170++;
						}
					}

					/**
					 * REGISTRO C175: Registro Analítico do Documento (65)
					 */
					if(modelo.equals("65") && (eaa0103s != null && eaa0103s.size() > 0)) {
						def rsC175s = buscarResumoValoresC175PC(eaa01.eaa01id, "eaa0103totDoc", getCampo("C175", "VL_DESC"), getCampo("C175", "VL_BC_PIS"), getCampo("C175", "ALIQ_PIS"), getCampo("C175", "QUANT_BC_PIS"), 
							                                                                    getCampo("C175", "ALIQ_PIS_QUANT"), getCampo("C175", "VL_PIS"), getCampo("C175", "VL_BC_COFINS"), getCampo("C175", "ALIQ_COFINS"), 
																								getCampo("C175", "QUANT_BC_COFINS"), getCampo("C175", "ALIQ_COFINS_QUANT"), getCampo("C175", "VL_COFINS"));

						if(rsC175s != null && rsC175s.size() > 0) {
							for(int i = 0; i < rsC175s.size(); i++) {
								txt2.print("C175");
								txt2.print(rsC175s.get(i).getString("aaj15codigo"));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal("eaa0103totDoc"), 2));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "VL_DESC")), 2, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "VL_BC_PIS")), 2, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "ALIQ_PIS")), 4, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "QUANT_BC_PIS")), 3, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "ALIQ_PIS_QUANT")), 4, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "VL_PIS")), 2, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "VL_BC_COFINS")), 2, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "ALIQ_COFINS")), 4, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "QUANT_BC_COFINS")), 3, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "ALIQ_COFINS_QUANT")), 4, false));
								txt2.print(formatarValor(rsC175s.get(i).getBigDecimal(getCampo("C175", "VL_COFINS")), 2, false));
								
								String abc10codigo = rsC175s.get(i).getString("abc10codigo");
								txt2.print(abc10codigo);
								if(abc10codigo != null) abc10s.add(abc10codigo);
								
								txt2.print(null);
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC175++;
							}
						}
					}
				}
			}
		}
	}
	
	def gerarC300(Aac10 aac10, Long gcEA) {
		/**
		 * REGISTRO C395: Notas Fiscais de Venda a Consumidor (Código 02, 2D, 2E e 59)
		 */
		List<Eaa01> eaa01s = buscarDocumentosC395PC(gcEA, modelosC300);
		if(eaa01s != null && eaa01s.size() > 0) {
			for(Eaa01 eaa01 : eaa01s) {
				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);

				def serie = formatarSerie(abb01.abb01serie, aah01.aah01modelo);

				txt2.print("C395");
				txt2.print(aah01.aah01modelo);
				txt2.print(abe01.abe01codigo);
				txt2.print(serie);
				txt2.print(null);
				txt2.print(abb01.abb01num);
				txt2.print(abb01.abb01data.format(ddMMyyyy));
				txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
				txt2.newLine();
				qtLinBlocoC++;
				qtLinC395++;

				comporRegistro0150(aac10, abe01.abe01id);

				/**
				 * REGISTRO C396 - Itens do documento
				 */
				List<Eaa0103> eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa0103doc", Parametro.criar("eaa0103doc", eaa01.eaa01id));
				if(eaa0103s != null && eaa0103s.size() > 0) {
					for(Eaa0103 eaa0103 : eaa0103s) {
						Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
						Aam06 aam06 = getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id);
						comporRegistro0200(aac10, abm01.abm01id);
						comporRegistro0190(aac10, aam06);

						TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
						
						txt2.print("C396");
						txt2.print(abm01.abm01codigo);
						txt2.print(formatarValor(eaa0103.eaa0103totDoc, 2));
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "VL_DESC")), 2, false));

						Aaj30 aaj30 = eaa0103.eaa0103codBcCred == null ? null : getSession().get(Aaj30.class, eaa0103.eaa0103codBcCred.aaj30id);
						txt2.print(aaj30 == null ? null : aaj30.aaj30codigo);

						Aaj12 aaj12 = eaa0103.eaa0103cstPis == null ? null : getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id);
						txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "VL_BC_PIS")), 2));
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "ALIQ_PIS")), 4));
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "VL_PIS")), 2));

						Aaj13 aaj13 = eaa0103.eaa0103cstCofins == null ? null : getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id);
						txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "VL_BC_COFINS")), 2));
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "ALIQ_COFINS")), 4));
						txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo("C396", "VL_COFINS")), 2));
						
						Abc10 abc10 = eaa0103.eaa0103cta == null ? null : getSession().get(Abc10.class, eaa0103.eaa0103cta.abc10id);
						txt2.print(abc10 == null ? null : abc10.abc10codigo);
						if(abc10 != null) abc10s.add(abc10.abc10codigo);
						
						txt2.newLine();
						qtLinBlocoC++;
						qtLinC396++;
					}
				}
			}
		}
	}
	
	def gerarC500(Aac10 aac10, Long gcEA) {
		/**
		 * REGISTRO C500: Nota Fiscal/Conta de Energia Elétrica (Código 06),
		 * Nota Fiscal de Energia Elétrica Eletrônica – NF3e (Código 66),
		 * Nota Fiscal/Conta de fornecimento D’água Canalizada (Código 29),
		 * Nota Fiscal/Consumo Fornecimento de Gás (Código 28) e NF-e (Código 55) – Documentos de Entrada / Aquisição com Crédito
		 */

		List<Eaa01> eaa01s = buscarDocumentosC500PC(gcEA, modelosC500);
		if(eaa01s != null && eaa01s.size() > 0) {
			for(Eaa01 eaa01 : eaa01s) {
				validacoes(eaa01);

				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
				Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
				Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
				
				def modelo = aah01.aah01modelo;
				def serie = formatarSerie(abb01.abb01serie, modelo);

				if("02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd)) {
					txt2.print("C500");
					txt2.print(null);
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.print(null);
					txt2.newLine();

				}else {
					Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
					txt2.print("C500");
					txt2.print(abe01.abe01codigo);
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C500", "VL_ICMS")), 2));
					txt2.print(null);
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C500", "VL_PIS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("C500", "VL_COFINS")), 2));
					txt2.print(eaa01.eaa01nfeChave);
					txt2.newLine();

					comporRegistro0150(aac10, abe01.abe01id);
				}
				qtLinBlocoC++;
				qtLinC500++;

				/**
				 * REGISTRO C501 - Complemento da operação - PIS
				 */
				def rsC501s = buscarResumoValoresC501PC(eaa01.eaa01id, getCampo("C501", "ALIQ_PIS"), getCampo("C501", "VL_BC_PIS"), getCampo("C501", "VL_PIS"));
				if(rsC501s != null && rsC501s.size() > 0) {
					for(int i = 0; i < rsC501s.size(); i++) {
						txt2.print("C501");
						txt2.print(selecionarCSTPis(rsC501s.get(i).getString("cstPis"), rsC501s.get(i).getBigDecimal(getCampo("C501", "VL_BC_PIS"))));
						txt2.print(formatarValor(rsC501s.get(i).getBigDecimal("eaa0103totDoc"), 2));
						txt2.print(rsC501s.get(i).getString("codBCCred"));
						txt2.print(formatarValor(rsC501s.get(i).getBigDecimal(getCampo("C501", "VL_BC_PIS")), 2));
						txt2.print(formatarValor(rsC501s.get(i).getBigDecimal(getCampo("C501", "ALIQ_PIS")), 4));
						txt2.print(formatarValor(rsC501s.get(i).getBigDecimal(getCampo("C501", "VL_PIS")), 2));
						
						String abc10codigo = rsC501s.get(i).getString("abc10codigo");
						txt2.print(abc10codigo);
						if(abc10codigo != null) abc10s.add(abc10codigo);
						
						txt2.newLine();
						qtLinBlocoC++;
						qtLinC501++;
					}
				}

				/**
				 * REGISTRO C505 - Complemento da operação - COFINS
				 */
				def rsC505s = buscarResumoValoresC505PC(eaa01.eaa01id, getCampo("C505", "ALIQ_COFINS"), getCampo("C505", "VL_BC_COFINS"), getCampo("C505", "VL_COFINS"));
				if(rsC505s != null && rsC505s.size() > 0) {
					for(int i = 0; i < rsC505s.size(); i++) {
						txt2.print("C505");
						txt2.print(selecionarCSTCofins(rsC505s.get(i).getString("cstCofins"), rsC505s.get(i).getBigDecimal(getCampo("C505", "VL_BC_COFINS"))));
						txt2.print(formatarValor(rsC505s.get(i).getBigDecimal("eaa0103totDoc"), 2));
						txt2.print(rsC505s.get(i).getString("codBCCred"));
						txt2.print(formatarValor(rsC505s.get(i).getBigDecimal(getCampo("C505", "VL_BC_COFINS")), 2));
						txt2.print(formatarValor(rsC505s.get(i).getBigDecimal(getCampo("C505", "ALIQ_COFINS")), 4));
						txt2.print(formatarValor(rsC505s.get(i).getBigDecimal(getCampo("C505", "VL_COFINS")), 2));
						
						String abc10codigo = rsC505s.get(i).getString("abc10codigo"); 
						txt2.print(abc10codigo);
						if(abc10codigo != null) abc10s.add(abc10codigo);
						
						txt2.newLine();
						qtLinBlocoC++;
						qtLinC505++;
					}
				}

				/**
				 * REGISTRO C509: Processo Referenciado
				 */
				Abb40 abb40 = eaa0102.eaa0102processo;
				if(abb40 != null) {
					abb40 = getSession().get(Abb40.class, eaa0102.eaa0102processo.abb40id);
					if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
					if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

					txt2.print("C509");
					txt2.print(abb40.abb40num);
					txt2.print(abb40.abb40indProc);
					txt2.newLine();
					qtLinBlocoC++;
					qtLinC509++;
				}

			}
		}
	}
	
	def gerarC800(Aac10 aac10, Long gcEA) {
		
		List<TableMap> rsC860s = buscarDocumentosC860PC(gcEA, modelosC800);
		def mapSAT = new HashMap<String, TableMap>();
		if(rsC860s != null && rsC860s.size() > 0) {
			for(int i = 0; i < rsC860s.size(); i++) {
				def key = rsC860s.get(i).getString("aah01modelo") + "/" + rsC860s.get(i).getString("abd10serieFabr") + "/" + rsC860s.get(i).getDate("abb01data");
				def tm = new TableMap();
				tm.put("aah01modelo", rsC860s.get(i).getString("aah01modelo"));
				tm.put("abd10serieFabr", rsC860s.get(i).getString("abd10serieFabr"));
				tm.put("abb01data", rsC860s.get(i).getString("abb01data"));
				if(mapSAT.get(key).getInteger("numI") == null || mapSAT.get(key).getInteger("numI") > rsC860s.get(i).getInteger("abb01num")) {
					tm.put("numI", rsC860s.get(i).getInteger("abb01num"));
				}
				if(mapSAT.get(key).getInteger("numF") == null || mapSAT.get(key).getInteger("numF") < rsC860s.get(i).getInteger("abb01num")) {
					tm.put("numF", rsC860s.get(i).getInteger("abb01num"));
				}
				def eaa01IdsDoDia = mapSAT.get(key).get("eaa01Ids");
				if(eaa01IdsDoDia == null) eaa01IdsDoDia = new HashSet<Long>();
				eaa01IdsDoDia.add(rsC860s.get(i).getLong("eaa01id"));
				tm.put("eaa01Ids", eaa01IdsDoDia);
				mapSAT.put(key, tm);
			}
		}

		for(String key : mapSAT.keySet()) {
			txt2.print("C860");
			txt2.print(mapSAT.get(key).getString("aah01modelo"));
			txt2.print(mapSAT.get(key).getString("abd10serieFabr"));
			txt2.print(mapSAT.get(key).getDate("abb01data").format(ddMMyyyy));
			txt2.print(mapSAT.get(key).getInteger("numI"));
			txt2.print(mapSAT.get(key).getInteger("numF"));
			txt2.newLine();
			qtLinBlocoC++;
			qtLinC860++;

			/**
			 * REGISTRO C870: Resumo Diário do CF-e por Equipamento
			 */
			def rsC870s = buscarValoresC870PC(mapSAT.get(key).get("eaa01Ids"), getCampo("C870", "VL_DESC"), getCampo("C870", "VL_BC_PIS"), getCampo("C870", "ALIQ_PIS"), getCampo("C870", "VL_PIS"), getCampo("C870", "VL_BC_COFINS"), getCampo("C870", "ALIQ_COFINS"), getCampo("C870", "VL_COFINS"));

			def mapC870 = new HashMap<String, TableMap>();
			if(rsC870s != null && rsC870s.size() > 0) {
				for(int i = 0; i < rsC870s.size(); i++) {
					def agrup = rsC870s.get(i).getLong("abm01id") + "/" + rsC870s.get(i).getString("aaj15codigo") + "/" + rsC870s.get(i).getString("abc10codigo") + "/" + 
					            rsC870s.get(i).getString("cstPis") + "/" + rsC870s.get(i).getBigDecimal(getCampo("C870", "ALIQ_PIS")) + "/" + rsC870s.get(i).getString("cstCofins") + "/" + rsC870s.get(i).getBigDecimal(getCampo("C870", "ALIQ_COFINS"));
					def tm = new TableMap();

					tm.put("abm01id", rsC870s.get(i).getLong("abm01id"));
					tm.put("abm01tipo", rsC870s.get(i).getInteger("abm01tipo"));
					tm.put("abm01codigo", rsC870s.get(i).getString("abm01codigo"));
					tm.put("aam06id", rsC870s.get(i).getLong("aam06id"));
					tm.put("aaj15codigo", rsC870s.get(i).getString("aaj15codigo"));

					tm.put("vlItem", mapC870.get(agrup).getBigDecimal("vlItem").add(rsC870s.get(i).getBigDecimal("eaa0103totDoc")));
					tm.put("vlDesc", mapC870.get(agrup).getBigDecimal("vlDesc").add(rsC870s.get(i).getBigDecimal(getCampo("C870", "VL_DESC"))));
					tm.put("cstPis", rsC870s.get(i).getString("cstPis"));

					tm.put("bcPis", mapC870.get(agrup).getBigDecimal("bcPis").add(rsC870s.get(i).getBigDecimal(getCampo("C870", "VL_BC_PIS"))));
					tm.put("aliqPis", rsC870s.get(i).getBigDecimal(getCampo("C870", "ALIQ_PIS")));
					tm.put("vlPis", mapC870.get(agrup).getBigDecimal("vlPis").add(rsC870s.get(i).getBigDecimal(getCampo("C870", "VL_PIS"))));
					tm.put("cstCof", rsC870s.get(i).getString("cstCofins"));

					tm.put("bcCof", mapC870.get(agrup).getBigDecimal("bcCof").add(rsC870s.get(i).getBigDecimal(getCampo("C870", "VL_BC_COFINS"))));
					tm.put("aliqCof", rsC870s.get(i).getBigDecimal(getCampo("C870", "ALIQ_COFINS")));
					tm.put("vlCof", mapC870.get(agrup).getBigDecimal("vlCof").add(rsC870s.get(i).getBigDecimal(getCampo("C870", "VL_COFINS"))));
					
					tm.put("cta", mapC870.get(agrup).getString("abc10codigo"));

					mapC870.put(agrup, tm);
				}
			}

			for(String keyC870 : mapC870.keySet()) {
				txt2.print("C870");
				txt2.print(mapC870.get(keyC870).getString("abm01codigo"));
				txt2.print(mapC870.get(keyC870).getString("aaj15codigo"));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("vlItem"), 2));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("vlDesc"), 2));
				txt2.print(mapC870.get(keyC870).getString("cstPis"));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("bcPis"), 2));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("aliqPis"), 4));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("vlPis"), 2));
				txt2.print(mapC870.get(keyC870).getString("cstCof"));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("bcCof"), 2));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("aliqCof"), 4));
				txt2.print(formatarValor(mapC870.get(keyC870).getBigDecimal("vlCof"), 2));
				
				String abc10codigo = mapC870.get(keyC870).getString("cta");
				txt2.print(abc10codigo);
				txt2.newLine();
				qtLinBlocoC++;
				qtLinC870++;

				comporRegistro0200(aac10, mapC870.get(keyC870).getLong("abm01id"));

				Aam06 aam06 = getSession().get(Aam06.class, mapC870.get(keyC870).getLong("aam06id"));
				comporRegistro0190(aac10, aam06);
				
				if(abc10codigo != null) abc10s.add(abc10codigo);
			}

			/**
			 * REGISTRO C890: Processo Referenciado
			 */
			for(Long eaa01id : mapSAT.get(key).get("eaa01Ids")) {
				Abb40 abb40 = buscarProcessoReferenciado(eaa01id);
				if(abb40 != null) {
					if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
					if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

					txt2.print("C890");
					txt2.print(abb40.abb40num);
					txt2.print(abb40.abb40indProc);
					qtLinBlocoC++;
					qtLinC890++;
				}
			}
		}
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * *  BLOCO D: DOCUMENTOS FISCAIS II - SERVIÇOS (ICMS) * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoD() {
		/**
		 * REGISTRO D001 - Abertura Bloco D
		 */
		boolean contemDadosBlocoD = verificarSeContemDadosBlocoD(aac10s);

		txt2.print("D001");
		txt2.print(contemDadosBlocoD ? 0 : 1);
		txt2.newLine();
		qtLinBlocoD++;

		/**
		 * REGISTRO D010 - Identificação do Estabelecimento
		 */
		if(contemDadosBlocoD) {
			def setEmpresasEA = new HashSet<Long>(); //Empresas centralizadoras que já foram para EFD

			for(Aac10 aac10 : aac10s) {
				def gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
				if(setEmpresasEA.contains(gcEA))continue;
				setEmpresasEA.add(gcEA);

				boolean temMovBlocoDparaAEmpresa = verificarSeContemDadosBlocoD(aac10);
				if(!temMovBlocoDparaAEmpresa) continue;

				txt2.print("D010");
				txt2.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt2.newLine();
				qtLinBlocoD++;
				qtLinD010++;


				/**
				 * REGISTRO D100 - Nota Fiscal de Serviço de Transporte (Código 07) e Conhecimentos de Transporte Rodoviário de Cargas (Código 08),
				 * Conhecimento de Transporte de Cargas Avulso (Código 09), Aéreo (Código 10), Ferroviário de Cargas (Código 11) e
				 * Multimodal de Cargas (Código 26), Nota Fiscal de Transporte Ferroviário de Carga (Código 27) e
				 * Conhecimento de Transporte Eletrônico - CT-e (Código 57)
				 *
				 * Observações:
				 * - Documentos cuja situação seja 02 ou 03 preencher somente os campos: reg, ind_oper, ind_emit, cod_mod, cod_sit,
				 * ser, sub e num_doc e os demais campos deixar vazio "||" e NÃO informar registros filhos.
				 */
				Set<Long> eaa01Ids = new HashSet<Long>();

				List<Eaa01> eaa01sEnt = buscarDocumentosD100PC(0, gcEA, modelosD100);
				for(Eaa01 eaa01 : eaa01sEnt) {
					eaa01Ids.add(eaa01.eaa01id);
				}

				for(Long eaa01id : eaa01Ids) {
					Eaa01 eaa01 = getSession().get(Eaa01.class, eaa01id);
					if(eaa01 != null) {
						validacoes(eaa01);

						Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
						Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
						Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
						Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
						TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
						
						def modelo = aah01.aah01modelo;
						def serie = formatarSerie(abb01.abb01serie, modelo);

						if("02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd)) {
							txt2.print("D100");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(null);
							txt2.print(modelo);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(eaa01.eaa01nfeChave);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.newLine();

						}else {
							Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

							txt2.print("D100");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(abe01.abe01codigo);
							txt2.print(modelo);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(eaa01.eaa01nfeChave);
							txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
							txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
							txt2.print(eaa0102.eaa0102cteTipo);
							txt2.print(null);
							txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D100", "VL_DESC")), 2));
							txt2.print(eaa0102.eaa0102frete == null ? 9 : eaa0102.eaa0102frete);
							txt2.print(formatarValor(eaa01.eaa01totItens, 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D100", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D100", "VL_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D100", "VL_NT")), 2));

							def obs = eaa01.eaa01obsFisco == null ? null : eaa01.eaa01obsFisco.length() > 255 ? eaa01.eaa01obsFisco.substring(0, 255) : eaa01.eaa01obsFisco;
							def codInf = null;
							if(obs != null) {
								codInf = StringUtils.ajustString(map0450 == null ? "1" : ""+(map0450.size()+1), 6, '0', true);
								comporRegistro0450(aac10, codInf, obs);
							}
							txt2.print(codInf);

							Abc10 abc10= selecionarCtaContabilDoDocto(eaa01.eaa01id);
							if(abc10 != null) {
								txt2.print(abc10.abc10codigo);
								abc10s.add(abc10.abc10codigo);
							}else {
								txt2.print(null);
							}

							txt2.newLine();

							comporRegistro0150(aac10, abe01.abe01id);
						}
						qtLinBlocoD++;
						qtLinD100++;

						/**
						 * REGISTRO D101 - Complemento do documento - PIS
						 */
						def rsD101s = buscarResumoValoresD101PC(eaa01.eaa01id, getCampo("D101", "ALIQ_PIS"), getCampo("D101", "VL_BC_PIS"), getCampo("D101", "VL_PIS"));
						if(rsD101s != null && rsD101s.size() > 0) {
							for(int i = 0; i < rsD101s.size(); i++) {
								txt2.print("D101");
								txt2.print(rsD101s.get(i).getInteger("eaa0102cteNatFrete") == null ? "9" : rsD101s.get(i).getInteger("eaa0102cteNatFrete"));
								txt2.print(formatarValor(rsD101s.get(i).getBigDecimal("eaa0103totDoc"), 2));
								txt2.print(selecionarCSTPis(rsD101s.get(i).getString("cstPis"), rsD101s.get(i).getBigDecimal(getCampo("D101", "VL_BC_PIS"))));
								txt2.print(rsD101s.get(i).getString("codBCCred"));
								txt2.print(formatarValor(rsD101s.get(i).getBigDecimal(getCampo("D101", "VL_BC_PIS")), 2));
								txt2.print(formatarValor(rsD101s.get(i).getBigDecimal(getCampo("D101", "ALIQ_PIS")), 4));
								txt2.print(formatarValor(rsD101s.get(i).getBigDecimal(getCampo("D101", "VL_PIS")), 2));
								
								String abc10codigo = rsD101s.get(i).getString("abc10codigo");
								txt2.print(abc10codigo);
								if(abc10codigo != null) abc10s.add(abc10codigo);
								
								txt2.newLine();
								qtLinBlocoD++;
								qtLinD101++;
							}
						}

						/**
						 * REGISTRO D105 - Complemento do documento - COFINS
						 */
						def rsD105s = buscarResumoValoresD105PC(eaa01.eaa01id, getCampo("D105", "ALIQ_COFINS"), getCampo("D105", "VL_BC_COFINS"), getCampo("D105", "VL_COFINS"));
						if(rsD105s != null && rsD105s.size() > 0) {
							for(int i = 0; i < rsD105s.size(); i++) {
								txt2.print("D105");
								txt2.print(rsD105s.get(i).getInteger("eaa0102cteNatFrete") == null ? "9" : rsD105s.get(i).getInteger("eaa0102cteNatFrete"));
								txt2.print(formatarValor(rsD105s.get(i).getBigDecimal("eaa0103totDoc"), 2));
								txt2.print(selecionarCSTCofins(rsD105s.get(i).getString("cstCofins"), rsD105s.get(i).getBigDecimal(getCampo("D105", "VL_BC_COFINS"))));
								txt2.print(rsD105s.get(i).getString("codBCCred"));
								txt2.print(formatarValor(rsD105s.get(i).getBigDecimal(getCampo("D105", "VL_BC_COFINS")), 2));
								txt2.print(formatarValor(rsD105s.get(i).getBigDecimal(getCampo("D105", "ALIQ_COFINS")), 4));
								txt2.print(formatarValor(rsD105s.get(i).getBigDecimal(getCampo("D105", "VL_COFINS")), 2));

								String abc10codigo = rsD105s.get(i).getString("abc10codigo");
								txt2.print(abc10codigo);
								if(abc10codigo != null) abc10s.add(abc10codigo);
								
								txt2.newLine();
								qtLinBlocoD++;
								qtLinD105++;
							}
						}

						/**
						 * REGISTRO D111 - Processo Referenciado
						 */
						Abb40 abb40 = buscarProcessoReferenciado(eaa01id);
						if(abb40 != null) {
							if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
							if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

							txt2.print("D111");
							txt2.print(abb40.abb40num);
							txt2.print(abb40.abb40indProc);
							qtLinBlocoD++;
							qtLinD111++;
						}
					}
				}

				/**
				 * Registro D500 - Nota Fiscal de Serviço de Comunicação (Código 21) e Nota Fiscal de Serviço de Telecomunicação (Código 22)
				 *
				 * Observações:
				 * - Documentos cuja situação seja 02 ou 03 preencher somente os campos: reg, ind_oper, ind_emit, cod_mod, cod_sit,
				 * ser, sub e num_doc e os demais campos deixar vazio "||" e NÃO informar registros filhos.
				 */
				List<Eaa01> eaa01s = buscarDocumentosD500PC(gcEA, modelosD500);
				if(eaa01s != null && eaa01s.size() > 0) {
					for(Eaa01 eaa01 : eaa01s) {

						Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
						Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
						Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
						TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
						
						def modelo = aah01.aah01modelo;
						def serie = formatarSerie(abb01.abb01serie, modelo);

						if("02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd)) {
							txt2.print("D500");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(null);
							txt2.print(modelo);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.print(null);
							txt2.newLine();

						}else {
							Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

							txt2.print("D500");
							txt2.print(eaa01.eaa01esMov);
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print(abe01.abe01codigo);
							txt2.print(modelo);
							txt2.print(aaj03.aaj03efd);
							txt2.print(serie);
							txt2.print(null);
							txt2.print(abb01.abb01num);
							txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
							txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
							txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_DESC")), 2));
							txt2.print(formatarValor(eaa01.eaa01totItens, 2));
							txt2.print(formatarValor(eaa01.eaa01totItens, 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_TERC")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_DA")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_ICMS")), 2));

							def obs = eaa01.eaa01obsFisco == null ? null : eaa01.eaa01obsFisco.length() > 255 ? eaa01.eaa01obsFisco.substring(0, 255) : eaa01.eaa01obsFisco;
							def codInf = null;
							if(obs != null) {
								codInf = StringUtils.ajustString(map0450 == null ? "1" : ""+(map0450.size()+1), 6, '0', true);
								comporRegistro0450(aac10, codInf, obs);
							}
							txt2.print(codInf);

							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_PIS")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo("D500", "VL_COFINS")), 2));
							txt2.newLine();

							comporRegistro0150(aac10, abe01.abe01id);
						}
						qtLinBlocoD++;
						qtLinD500++;

						/**
						 * REGISTRO D501 - Complemento da operação - PIS
						 */
						List<Eaa01> rsD501s = buscarResumoValoresD501PC(eaa01.eaa01id, getCampo("D501", "ALIQ_PIS"), getCampo("D501", "VL_BC_PIS"), getCampo("D501", "VL_PIS"));
						if(rsD501s != null && rsD501s.size() > 0) {
							for(int i = 0; i < rsD501s.size(); i++) {
								if(getCampo("D501", "VL_BC_PIS") == null || rsD501s.get(i).getBigDecimal(getCampo("D501", "VL_BC_PIS")).equals(0)) continue;

								txt2.print("D501");
								txt2.print(selecionarCSTPis(rsD501s.get(i).getString("cstPis"), rsD501s.get(i).getBigDecimal(getCampo("D501", "VL_BC_PIS"))));
								txt2.print(formatarValor(rsD501s.get(i).getBigDecimal("eaa0103totDoc"), 2));
								txt2.print(rsD501s.get(i).getString("codBCCred"));
								txt2.print(formatarValor(rsD501s.get(i).getBigDecimal(getCampo("D501", "VL_BC_PIS")), 2));
								txt2.print(formatarValor(rsD501s.get(i).getBigDecimal(getCampo("D501", "ALIQ_PIS")), 4));
								txt2.print(formatarValor(rsD501s.get(i).getBigDecimal(getCampo("D501", "VL_PIS")), 3));
								
								String abc10codigo = rsD501s.get(i).getString("abc10codigo");
								txt2.print(abc10codigo);
								if(abc10codigo != null) abc10s.add(abc10codigo);
								
								txt2.newLine();
								qtLinBlocoD++;
								qtLinD501++;
							}
						}

						/**
						 * REGISTRO D505 - Complemento da operação - COFINS
						 */
						def rsD505s = buscarResumoValoresD505PC(eaa01.eaa01id, getCampo("D505", "ALIQ_COFINS"), getCampo("D505", "VL_BC_COFINS"), getCampo("D505", "VL_COFINS"));
						if(rsD505s != null && rsD505s.size() > 0) {
							for(int i = 0; i < rsD505s.size(); i++) {
								if(getCampo("D505", "VL_BC_COFINS") == null || rsD505s.get(i).getBigDecimal(getCampo("D505", "VL_BC_COFINS")).equals(0)) continue;

								txt2.print("D505");
								txt2.print(selecionarCSTCofins(rsD505s.get(i).getString("cstCofins"), rsD505s.get(i).getBigDecimal(getCampo("D505", "VL_BC_COFINS"))));
								txt2.print(formatarValor(rsD505s.get(i).getBigDecimal("eaa0103totDoc"), 2));
								txt2.print(rsD505s.get(i).getString("codBCCred"));
								txt2.print(formatarValor(rsD505s.get(i).getBigDecimal(getCampo("D505", "VL_BC_COFINS")), 2));
								txt2.print(formatarValor(rsD505s.get(i).getBigDecimal(getCampo("D505", "ALIQ_COFINS")), 4));
								txt2.print(formatarValor(rsD505s.get(i).getBigDecimal(getCampo("D505", "VL_COFINS")), 3));
								
								String abc10codigo = rsD505s.get(i).getString("abc10codigo");
								txt2.print(abc10codigo);
								if(abc10codigo != null) abc10s.add(abc10codigo);
								
								txt2.newLine();
								qtLinBlocoD++;
								qtLinD505++;
							}
						}
						/**
						 * REGISTRO D509 – Processo Referenciado
						 */
						Abb40 abb40 = buscarProcessoReferenciado(eaa01.eaa01id);
						if(abb40 != null) {
							if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
							if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

							txt2.print("509");
							txt2.print(abb40.abb40num);
							txt2.print(abb40.abb40indProc);
							qtLinBlocoD++;
							qtLinD509++;
						}

					}
				}
			}
		}

		/**
		 * REGISTRO D990 - Encerramento do Bloco D
		 */
		qtLinBlocoD++;

		txt2.print("D990");
		txt2.print(qtLinBlocoD);
		txt2.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * *   BLOCO F: DEMAIS DOCUMENTOS E OPERAÇÕES  * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoF() {
		def mapF120 = comporF120();
		def mapF130 = comporF130();

		/**
		 * REGISTRO F001 - Abertura Bloco F
		 */
		boolean contemDadosBlocoF = verificarSeContemDadosBlocoF(aac10s, mapF120, mapF130);

		txt2.print("F001");
		txt2.print(contemDadosBlocoF ? 0 : 1);
		txt2.newLine();
		qtLinBlocoF++;

		/**
		 * REGISTRO F010 - Identificação do Estabelecimento
		 */
		gerouF550 = false;

		if(contemDadosBlocoF) {
			def setGrupoEA = new HashSet<Long>();
			def setGrupoEC = new HashSet<Long>();
			def setGrupoED = new HashSet<Long>();

			for(Aac10 aac10 : aac10s) {
				def gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
				if(setGrupoEA.contains(gcEA)) continue;

				def gcEC = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EC");
				if(setGrupoEC.contains(gcEC)) continue;

				def gcED = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "ED");
				if(setGrupoED.contains(gcED)) continue;

				def temMovBlocoFparaAEmpresa = verificarSeContemDadosBlocoF(aac10, mapF120, mapF130);
				if(!temMovBlocoFparaAEmpresa) continue;

				txt2.print("F010");
				txt2.print(StringUtils.extractNumbers(aac10.aac10ni));
				txt2.newLine();
				qtLinBlocoF++;
				qtLinF010++;


				/**
				 * REGISTRO F100 - Documentos e Operações Geradoras de Contribuição e Créditos
				 */
				List<Edb11> edb11s = buscarOperacoesCreditoPorGC(gcED);
				for(Edb11 edb11 : edb11s){
					Abe01 abe01 = edb11.edb11ent == null ? null : getSession().get(Abe01.class, edb11.edb11ent.abe01id);
					Abm01 abm01 = edb11.edb11item == null ? null : getSession().get(Abm01.class, edb11.edb11item.abm01id);
					Aam06 aam06 = abm01 != null && abm01.abm01umu != null ? getSession().get(Abm01.class, abm01.abm01umu.aam06id) : null;

					txt2.print("F100");
					txt2.print(edb11.edb11oper);
					txt2.print(abe01 == null ? null : abe01.abe01codigo);
					txt2.print(abm01 == null ? null : abm01.abm01codigo);
					txt2.print(edb11.edb11data.format(ddMMyyyy));
					txt2.print(formatarValor(edb11.edb11valor, 2));

					Aaj12 aaj12 = edb11.edb11cstPis == null ? null : getSession().get(Aaj12.class, edb11.edb11cstPis.aaj12id);
					txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);
					txt2.print(formatarValor(edb11.edb11bcPis, 4));
					txt2.print(formatarValor(edb11.edb11aliqPis, 4));
					txt2.print(formatarValor(edb11.edb11pis, 2));

					Aaj13 aaj13 = edb11.edb11cstCofins == null ? null : getSession().get(Aaj13.class, edb11.edb11cstCofins.aaj13id);
					txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);
					txt2.print(formatarValor(edb11.edb11bcCofins, 4));
					txt2.print(formatarValor(edb11.edb11aliqCofins, 4));
					txt2.print(formatarValor(edb11.edb11cofins, 2));

					Aaj30 aaj30 = edb11.edb11codBcCred == null ? null : getSession().get(Aaj30.class, edb11.edb11codBcCred.aaj30id);
					txt2.print(aaj30 == null ? null : aaj30.aaj30codigo);
					txt2.print(edb11.edb11origem == 1 ? 0 : 1);

					Abc10 abc10 = edb11.edb11cta == null ? null : getSession().get(Abc10.class, edb11.edb11cta.abc10id);
					txt2.print(abc10 == null ? null : abc10.abc10codigo);
					if(abc10 != null) abc10s.add(abc10.abc10codigo);

					Abb11 abb11 = edb11.edb11depto == null ? null : getSession().get(Abb11.class, edb11.edb11depto.abb11id);
					txt2.print(abb11 == null ? null : abb11.abb11codigo);
					txt2.print(edb11.edb11descr);
					txt2.newLine();
					qtLinBlocoF++;
					qtLinF100++;

					if(abe01 != null) comporRegistro0150(aac10, abe01.abe01id);
					if(abm01 != null) comporRegistro0200(aac10, abm01.abm01id);
					comporRegistro0190(aac10, aam06);
					if(abb11 != null) abb11s.add(abb11);
				}


				/**
				 * REGISTRO F120 - Bens que Geram Créditos com Base na Depreciação
				 */
				if(mapF120 != null && mapF120.size() > 0) {
					for(int i = 0; i < mapF120.size(); i++) {
						Aac10 empresa = mapF120.get(i).get("empresa");
						if(!empresa.aac10id.equals(aac10.aac10id))continue;

						def depreciacao = mapF120.get(i).getBigDecimal("vlrDepreciacao");
						def pPis = mapF120.get(i).getBigDecimal("pPis");
						def pCofins = mapF120.get(i).getBigDecimal("pCofins");

						txt2.print("F120");
						txt2.print(mapF120.get(i).getString("codBCCred"));
						txt2.print(mapF120.get(i).getInteger("ident"), 2);
						txt2.print(mapF120.get(i).getInteger("origem"));
						txt2.print(mapF120.get(i).getInteger("utilizacao"));
						txt2.print(formatarValor(depreciacao, 2));
						txt2.print(formatarValor(new BigDecimal(0), 2));

						Aaj12 aaj12 = edb10.edb10docCSTPis == null ? null : getSession().get(Aaj12.class, edb10.edb10docCSTPis.aaj12id);
						txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);

						txt2.print(formatarValor(depreciacao, 2));
						txt2.print(formatarValor(pPis, 4));
						txt2.print(formatarValor(depreciacao.multiply(pPis).divide(100).round(2), 2));

						Aaj13 aaj13 = edb10.edb10docCSTCofins == null ? null : getSession().get(Aaj13.class, edb10.edb10docCSTCofins.aaj13id);
						txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);

						txt2.print(formatarValor(depreciacao, 2));
						txt2.print(formatarValor(pCofins, 4));
						txt2.print(formatarValor(depreciacao.multiply(pCofins).divide(100).round(2), 2));
						txt2.print(mapF120.get(i).getString("conta"));

						Abb11 abb11 = mapF120.get(i).get("depto");
						txt2.print(abb11 == null ? null : abb11.abb11codigo);
						txt2.print(mapF120.get(i).getString("descricao"));
						txt2.newLine();
						qtLinBlocoF++;
						qtLinF120++;

						if(abb11 != null) abb11s.add(abb11);
					}
				}

				/**
				 * REGISTRO F130 - Bens que Geram Créditos com Base no Valor de Aquisição
				 */
				if(mapF130 != null && mapF130.size() > 0) {
					for(int i = 0; i < mapF130.size(); i++) {
						Aac10 empresa = mapF130.get(i).get("empresa");
						if(!empresa.aac10id.equals(aac10.aac10id))continue;

						def valor = mapF130.get(i).getBigDecimal("valor");
						def pPis = mapF130.get(i).getBigDecimal("pPis");
						def pCofins = mapF130.get(i).getBigDecimal("pCofins");

						txt2.print("F130");
						txt2.print(mapF130.get(i).getString("codBCCred"));
						txt2.print(mapF130.get(i).getInteger("ident"), 2);
						txt2.print(mapF130.get(i).getInteger("origem"));
						txt2.print(mapF130.get(i).getInteger("utilizacao"));
						txt2.print(mapF130.get(i).getDate("aquis") == null ? null : mapF130.get(i).getDate("aquis").format(MMyyyy));
						txt2.print(formatarValor(valor, 2));
						txt2.print(formatarValor(new BigDecimal(0), 2));
						txt2.print(formatarValor(valor, 2));

						def numParc = mapF130.get(i).getInteger("numParc");
						switch (numParc) {
							case 1: 	txt2.print("1"); break;
							case 12: 	txt2.print("2"); break;
							case 24: 	txt2.print("3"); break;
							case 48: 	txt2.print("4"); break;
							case 6:	 	txt2.print("5"); break;
							default :	txt2.print("9");
						}

						Aaj12 aaj12 = edb10.getEdb10regCSTPis() == null ? null : getSession().get(Aaj12.class, edb10.edb10regCSTPis.aaj12id);
						txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);

						def bc = valor.divide(numParc).round(2);
						txt2.print(formatarValor(bc, 2));
						txt2.print(formatarValor(pPis, 4));
						txt2.print(formatarValor(bc.multiply(pPis).divide(100).round(2), 2));

						Aaj13 aaj13 = edb10.edb10regCSTCofins == null ? null : getSession().get(Aaj13.class, edb10.edb10regCSTCofins.aaj13id);
						txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);

						txt2.print(formatarValor(bc, 2));
						txt2.print(formatarValor(pCofins, 4));
						txt2.print(formatarValor(bc.multiply(pCofins).divide(100).round(2), 2));
						txt2.print(mapF130.get(i).getString("conta"));

						Abb11 abb11 = mapF130.get(i).get("depto");
						txt2.print(abb11 == null ? null : abb11.abb11codigo);
						txt2.print(mapF130.get(i).getString("descricao"));
						txt2.newLine();
						qtLinBlocoF++;
						qtLinF130++;

						if(abb11 != null) abb11s.add(abb11);
					}
				}

				/**
				 * REGISTRO F150 - Crédito Presumido Sobre Estoque de Abertura
				 */
				def valor = aac10.aac10json == null ? null : aac10.aac10json.getBigDecimal("estAbertValor");
				if(valor != null && !valor.equals(0)){
					def dtEst = aac10.aac10json.getDate("estAbertMesAno");
					if(dtEst != null){
						def data = dtEst.withDayOfMonth(1);

						def qtMeses = DateUtils.dateDiff(data, dtInicial, ChronoUnit.MONTHS);
						if(qtMeses >= 0 && qtMeses < 12) {
							txt2.print("F150");
							txt2.print("18");
							txt2.print(formatarValor(valor, 2));
							txt2.print(formatarValor(new BigDecimal(0), 2));
							txt2.print(formatarValor(valor, 2));
							def bcMensal = round(valor / 12, 2);
							txt2.print(formatarValor(bcMensal, 2));

							Aaj12 aaj12 = edb10.edb10regCSTPis == null ? null : getSession().get(Aaj12.class, edb10.edb10regCSTPis.aaj12id);
							txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);

							txt2.print(formatarValor(aliqPisF150, 4));
							
							txt2.print(formatarValor(round((bcMensal * aliqPisF150) / 100, 2), 2));

							Aaj13 aaj13 = edb10.edb10regCSTCofins == null ? null : getSession().get(Aaj13.class, edb10.edb10regCSTCofins.aaj13id);
							txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);

							txt2.print(formatarValor(aliqCofinsF150, 4));
							txt2.print(formatarValor(bcMensal.multiply(aliqCofinsF150).divide(100).round(2), 2));
							txt2.print(null);
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoF++;
							qtLinF150++;
						}
					}
				}


				/**
				 * REGISTRO F550 - Consolidação das Operações da Pessoa Jurídica Submetida ao Regime de Tributação com Base
				 * no Lucro Presumido - Incidência do Pis/Pasep e da Cofins pelo Regime de Competência
				 */
				if(edb10.edb10incidTrib == 2) {
					if(!setGrupoEA.contains(gcEA)) {
						def rsF550s = buscarDocumentosF550PC(gcEA, getCampo("F550", "ALIQ_PIS"), getCampo("F550", "ALIQ_COFINS"), getCampo("F550", "VL_REC_COMP"), getCampo("F550", "VL_DESC_PIS"), getCampo("F550", "VL_BC_PIS"), getCampo("F550", "VL_PIS"), getCampo("F550", "VL_DESC_COFINS"), getCampo("F550", "VL_BC_COFINS"), getCampo("F550", "VL_COFINS"));

						def mapF550 = new HashMap<String, TableMap>();
						if(rsF550s != null && rsF550s.size() > 0) {
							for(int n = 0; n < rsF550s.size(); n++) {
								Eaa01 eaa01 = getSession().get(Eaa01.class, rsF550s.get(n).getLong("eaa01id"));
								Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
								Abb40 abb40 = eaa0102.eaa0102processo == null ? null : getSession().get(Abb40.class, eaa0102.eaa0102processo.abb40id);

								def agrup = rsF550s.get(n).getString("aaj12codigo") + "/" + rsF550s.get(n).getBigDecimal(getCampo("F550", "ALIQ_PIS")) + "/" + rsF550s.get(n).getString("aaj13codigo") + "/" + rsF550s.get(n).getBigDecimal(getCampo("F550", "ALIQ_COFINS")) + "/" + rsF550s.get(n).getString("aah01modelo") + "/" + rsF550s.get(n).getString("aaj30codigo");

								TableMap tm = new TableMap();

								def pis = mapF550.get(agrup).getBigDecimal("pis") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("pis");
								def cof = mapF550.get(agrup).getBigDecimal("cof") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("cof");
								def bcPis = mapF550.get(agrup).getBigDecimal("bcPis") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("bcPis");
								def bcCof = mapF550.get(agrup).getBigDecimal("bcCof") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("bcCof");
								def descPis = mapF550.get(agrup).getBigDecimal("descPis") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("descPis");
								def descCof = mapF550.get(agrup).getBigDecimal("descCof") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("descCof");
								def vlRecComp = mapF550.get(agrup).getBigDecimal("vlRecComp") == null ? new BigDecimal(0) : mapF550.get(agrup).getBigDecimal("vlRecComp");

								tm.put("vlRecComp", vlRecComp.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_REC_COMP"))));
								tm.put("cstPis", rsF550s.get(n).getString("aaj12codigo"));
								tm.put("descPis", descPis.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_DESC_PIS"))));
								tm.put("bcPis", bcPis.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_BC_PIS"))));
								tm.put("aliqPis", rsF550s.get(n).getBigDecimal(getCampo("F550", "ALIQ_PIS")));
								tm.put("pis", pis.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_PIS"))));
								tm.put("cstCof", rsF550s.get(n).getString("aaj13codigo"));
								tm.put("descCof", descCof.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_DESC_COFINS"))));
								tm.put("bcCof", bcCof.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_BC_COFINS"))));
								tm.put("aliqCof", rsF550s.get(n).getBigDecimal(getCampo("F550", "ALIQ_COFINS")));
								tm.put("cof", cof.add(rsF550s.get(n).getBigDecimal(getCampo("F550", "VL_COFINS"))));
								tm.put("ab15modelo", rsF550s.get(n).getString("aah01modelo"));
								tm.put("ab13codigo", rsF550s.get(n).getString("aaj30codigo"));
								tm.put("abb40", abb40 == null ? null : abb40);
								
								Abc10 abc10 = selecionarCtaContabilDoDocto(eaa01.eaa01id)
								tm.put("ctaContabil", abc10 == null ? null : abc10.abc10codigo);
								if(abc10 != null)abc10s.add(abc10.abc10codigo);
								
								mapF550.put(agrup, tm);
							}
						}

						for(String keyF550 : mapF550.keySet()) {
							gerouF550 = true;

							txt2.print("F550");
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("vlRecComp"), 2));
							txt2.print(mapF550.get(keyF550).getString("cstPis"), 2, '0', true);
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("descPis"), 2));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("bcPis"), 2));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("aliqPis"), 4));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("pis"), 2));
							txt2.print(mapF550.get(keyF550).getString("cstCof"), 2, '0', true);
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("descCof"), 2));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("bcCof"), 2));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("aliqCof"), 4));
							txt2.print(formatarValor(mapF550.get(keyF550).getBigDecimal("cof"), 2));
							txt2.print(mapF550.get(keyF550).getString("aah01modelo"), 2, '0', true);
							txt2.print(mapF550.get(keyF550).getString("aaj30codigo"), 4, '0', true);
							txt2.print(mapF550.get(keyF550).getString("ctaContabil"));
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoF++;
							qtLinF550++;
						}

						for(String keyF550 : mapF550.keySet()) {
							if(mapF550.get(keyF550).get("abb40") == null) continue;

							Abb40 abb40 = mapF550.get(keyF550).get("abb40");
							if(abb40.abb40tipo == 1) abb40sReg1010.add(abb40);
							if(abb40.abb40tipo == 0) abb40sReg1020.add(abb40);

							txt2.print("F559");
							txt2.print(abb40.abb40num);
							txt2.print(abb40.abb40indProc);
							txt2.newLine();
							qtLinBlocoF++;
							qtLinF559++;
						}
					}
				}

				/**
				 * REGISTRO F600 - Contribuição Retida na Fonte
				 */
				if(!setGrupoED.contains(gcED)) {
					def edb12s = buscarRetencoesPorPeriodo(gcED, mes, ano);
					for(Edb12 edb12 : edb12s) {
						txt2.print("F600");
						txt2.print(edb12.edb12indNat, 2);
						txt2.print(edb12.edb12data.format(ddMMyyyy));
						txt2.print(formatarValor(edb12.edb12bc, 4));
						txt2.print(formatarValor(edb12.edb12valor, 2));
						txt2.print(edb12.edb12codRec);
						txt2.print(edb12.edb12natRec);
						txt2.print(StringUtils.extractNumbers(edb12.edb12cnpj));
						txt2.print(formatarValor(edb12.edb12pis, 2));
						txt2.print(formatarValor(edb12.edb12cofins, 2));
						txt2.print(edb12.edb12declarante);
						txt2.newLine();
						qtLinBlocoF++;
						qtLinF600++;
					}
				}

				/**
				 * REGISTRO F700 - Deduções Diversas
				 */
				if(!setGrupoED.contains(gcED)) {
					def edb13s = buscarDeducoesPorPeriodo(gcED, mes, ano);
					for(Edb13 edb13 : edb13s) {
						txt2.print("F700");
						txt2.print(edb13.edb13origem, 2);
						txt2.print(edb13.edb13natDed);
						txt2.print(formatarValor(edb13.edb13pis, 2));
						txt2.print(formatarValor(edb13.edb13cofins, 2));
						txt2.print(formatarValor(edb13.edb13bc, 2));
						txt2.print(StringUtils.extractNumbers(edb13.edb13cnpj));
						txt2.print(edb13.edb13complem);
						txt2.newLine();
						qtLinBlocoF++;
						qtLinF700++;
					}
				}

				/**
				 * REGISTRO F800 - Créditos Decorrentes de Incorporação, Fusão e Cisão
				 */
				if(!setGrupoED.contains(gcED)) {
					def edb14s = buscarCreditosPorPeriodo(gcED, mes, ano);
					for(Edb14 edb14 : edb14s) {
						txt2.print("F800");
						txt2.print(edb14.edb14indNat, 2);
						txt2.print(edb14.edb14data.format(ddMMyyyy));
						txt2.print(StringUtils.extractNumbers(edb14.edb14cnpj));
						txt2.print(StringUtils.ajustString(edb14.edb14mesApur, 2) + StringUtils.ajustString(edb14.edb14anoApur, 4));
						txt2.print(edb14.edb14codCred);
						txt2.print(formatarValor(edb14.edb14pis, 2));
						txt2.print(formatarValor(edb14.edb14cofins, 2));
						txt2.print(formatarValor(edb14.edb14perc, 2));
						txt2.newLine();
						qtLinBlocoF++;
						qtLinF800++;
					}
				}

				setGrupoEA.add(gcEA);
				setGrupoED.add(gcED);
			}
		}


		/**
		 * REGISTRO F990 - Encerramento do Bloco F
		 */
		qtLinBlocoF++;

		txt2.print("F990");
		txt2.print(qtLinBlocoF);
		txt2.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * *   BLOCO M: APURAÇÃO DO PIS/COFINS   * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoM() {
		/**
		 * REGISTRO M001 - Abertura Bloco M
		 */
		txt2.print("M001");
		txt2.print(1);
		txt2.newLine();
		qtLinBlocoM++;

		/**
		 * REGISTRO M990 - Encerramento do Bloco M
		 */
		qtLinBlocoM++;

		txt2.print("M990");
		txt2.print(qtLinBlocoM);
		txt2.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * *   BLOCO P: CONTRIBUIÇÃO PREVIDENCIÁRIA SOBRE A RECEITA BRUTA  * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoP() {
		contemDadosBlocoP = contemDadosBlocoP();
		if(contemDadosBlocoP) {
			/**
			 * REGISTRO P001 - Abertura Bloco P
			 */
			txt2.print("P001");
			txt2.print(contemDadosBlocoP ? 0 : 1);
			txt2.newLine();
			qtLinBlocoP++;

			if(contemDadosBlocoP) {
				def totalContrApurada = new BigDecimal(0);

				/**
				 * REGISTRO P010 - Identificação do Estabelecimento
				 */
				List<Edb1001> edb1001s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Edb1001 WHERE edb1001apur = :edb10id", Parametro.criar("edb10id", edb10.edb10id));
				for(Edb1001 edb1001 : edb1001s) {
					txt2.print("P010");

					Aac10 empresa = getSession().get(Aac10.class, edb1001.edb1001empresa.aac10id);
					txt2.print(StringUtils.extractNumbers(empresa.aac10ni));
					txt2.newLine();
					qtLinBlocoP++;
					qtLinP010++;

					/**
					 * REGISTRO P100 - Contribuição Previdenciária sobre a Receita Bruta
					 */
					List<Edb10011> edb10011s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Edb10011 WHERE edb10011emp = :edb1001id", Parametro.criar("edb1001id", edb1001.edb1001id));
					for(Edb10011 edb10011 : edb10011s) {
						txt2.print("P100");
						txt2.print(dtInicial.format(ddMMyyyy));
						txt2.print(dtFinal.format(ddMMyyyy));
						txt2.print(formatarValor(edb1001.edb1001rb, 2));

						Abg02 abg02 = getSession().get(Abg02.class, edb10011.edb10011ativ.abg02id);
						txt2.print(abg02.abg02codigo);
						txt2.print(formatarValor(edb10011.edb10011rb, 2));
						txt2.print(formatarValor(edb10011.edb10011exc, 2));

						def bc = edb10011.edb10011rb.subtract(edb10011.edb10011exc);
						txt2.print(formatarValor(bc, 2));
						txt2.print(formatarValor(edb10011.edb10011aliq, 4));

						def contribuicao = bc.multiply(edb10011.edb10011aliq.divide(100).round(6));
						txt2.print(formatarValor(contribuicao, 2));

						Abc10 abc10 = edb10011.edb10011cta == null ? null : getSession().get(Abc10.class, edb10011.edb10011cta.abc10id);
						if(abc10 != null) {
							txt2.print(abc10.abc10codigo);
							abc10s.add(abc10.abc10codigo);
						} else {
							txt2.print(null);
						}
						
						txt2.print(null);
						txt2.newLine();
						qtLinBlocoP++;
						qtLinP100++;

						totalContrApurada = totalContrApurada.add(contribuicao);
					}
				}

				/**
				 * REGISTRO P200 - Consolidação
				 */
				txt2.print("P200");
				txt2.print(dtInicial.format(MMyyyy));
				txt2.print(formatarValor(totalContrApurada, 2));

				def reducoes = buscarValorAjustesPorTipoPeriodo(0, edb10.edb10id);
				txt2.print(formatarValor(reducoes, 2));

				def acrescimos = buscarValorAjustesPorTipoPeriodo(1, edb10.edb10id);
				txt2.print(formatarValor(acrescimos, 2));

				def total = totalContrApurada.subtract(reducoes).add(acrescimos);
				txt2.print(formatarValor(total, 2));

				txt2.print(edb10.edb10dctf);
				txt2.newLine();
				qtLinBlocoP++;
				qtLinP200++;

				List<Edb100111> edb100111s = buscarAjustesPorApuracao(edb10.edb10id);
				for(Edb100111 edb100111 : edb100111s) {
					txt2.print("P210");
					txt2.print(edb100111.edb100111tipo);
					txt2.print(formatarValor(edb100111.edb100111valor, 2));
					txt2.print(edb100111.edb100111efdCodAj, 2);

					Abb40 abb40 = edb100111.edb100111processo;
					if(abb40 != null) {
						abb40 = getSession().get(Abb40.class, edb100111.edb100111processo.abb40id);
						txt2.print(abb40.abb40num);
					}else {
						txt2.print(null);
					}
					
					txt2.print(edb100111.edb100111descr);
					txt2.print(edb100111.edb100111data == null ? null : edb100111.edb100111data.format(ddMMyyyy));
					txt2.newLine();
					qtLinBlocoP++;
					qtLinP210++;
				}
			}


			/**
			 * REGISTRO P990 - Encerramento do Bloco P
			 */
			qtLinBlocoP++;

			txt2.print("P990");
			txt2.print(qtLinBlocoP);
			txt2.newLine();
		}
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * *   BLOCO 1: OPERAÇÕES EXTEMPORÂNEAS  * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBloco1() {
		/**
		 * REGISTRO 1001 - Abertura Bloco 1
		 */
		txt2.print("1001");
		txt2.print(contemDadosBloco1() ? 0 : 1);
		txt2.newLine();
		qtLinBloco1++;

		/**
		 * REGISTRO 1010 - Processo Referenciado - Ação Judicial
		 */
		if(abb40sReg1010 != null && abb40sReg1010.size() > 0) {
			for(Abb40 abb40 : abb40sReg1010) {
				Abb40 abb4001s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abb4001 WHERE abb4001processo = :abb40id", Parametro.criar("abb40id", abb40.abb40id));
				for(Abb4001 abb4001 : abb4001s) {
					txt2.print("1010");
					txt2.print(abb40.abb40num);
					txt2.print(abb40.abb40secao);
					txt2.print(abb40.abb40vara);
					txt2.print(abb40.abb40natJud, 2);
					txt2.print(abb40.abb40resumo);
					txt2.print(abb4001.abb4001data == null ? null : abb4001.abb4001data.format(ddMMyyyy));
					txt2.newLine();
					qtLinBloco1++;
					qtLin1010++;
				}
			}
		}

		/**
		 * REGISTRO 1020 - Processo Referenciado - Processo judicial
		 */
		if(abb40sReg1020 != null && abb40sReg1020.size() > 0) {
			for(Abb40 abb40 : abb40sReg1020) {
				Abb40 abb4001s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abb4001 WHERE abb4001processo = :abb40id", Parametro.criar("abb40id", abb40.abb40id));
				for(Abb4001 abb4001 : abb4001s) {
					txt2.print("1020");
					txt2.print(abb40.abb40num);
					txt2.print(abb40.abb40natAdm, 2);
					txt2.print(abb4001.abb4001data == null ? null : abb4001.abb4001data.format(ddMMyyyy));
					txt2.newLine();
					qtLinBloco1++;
					qtLin1020++;
				}
			}
		}

		/**
		 * REGISTRO 1900 - Consolidação dos Documentos Emitidos por PJ Lucro Presumido – Regime de Caixa ou Competência
		 */
		if(gerouF550) {
			for(Aac10 aac10 : aac10s) {
				def gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");

				/** Documentos do F550 */
				def rs1900s = buscarDocumentos1900PC(gcEA, getCampo("1900", "VL_TOT_REC"));
				if(rs1900s != null && rs1900s.size() > 0) {
					for(int i = 0; i < rs1900s.size(); i++) {
						txt2.print("1900");
						txt2.print(StringUtils.extractNumbers(aac10.aac10ni), 14, '0', true);
						txt2.print(rs1900s.get(i).getString("aah01modelo"), 2, '0', true);
						txt2.print(formatarSerie(rs1900s.get(i).getString("abb01serie"), rs1900s.get(i).getString("aah01modelo")));
						txt2.print(null);

						Aaj03 aaj03 = getSession().get(Aaj03.class, rs1900s.get(i).getLong("eaa01sitDoc"));
						txt2.print(aaj03 == null ? null : !aaj03.aaj03efd.equals("00") && !aaj03.aaj03efd.equals("02") ? "99" : aaj03.aaj03efd);

						txt2.print(formatarValor(rs1900s.get(i).getBigDecimal(getCampo("1900", "VL_TOT_REC")), 2));
						txt2.print(rs1900s.get(i).getInteger("qtd"));
						txt2.print(rs1900s.get(i).getString("aaj12codigo"), 2, '0', true);
						txt2.print(rs1900s.get(i).getString("aaj13codigo"), 2, '0', true);
						txt2.print(rs1900s.get(i).getString("aaj15codigo"), 4, '0', true);
						txt2.print(null);
						txt2.print(null);
						txt2.newLine();
						qtLinBloco1++;
						qtLin1900++;
					}
				}
			}
		}

		/**
		 * REGISTRO 1990 - Encerramento do Bloco 1
		 */
		qtLinBloco1++;

		txt2.print("1990");
		txt2.print(qtLinBloco1);
		txt2.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * CONTINUAÇÃO * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * *  BLOCO 0: ABERTURA, IDENTIFICAÇÃO E REFERÊNCIAS * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarFechamentoBloco0() {
		/**
		 * REGISTRO 0140: Estabelecimentos
		 */
		for(Aac10 aac10 : aac10s) {
			txt1.print("0140");
			txt1.print(aac10.aac10codigo);
			txt1.print(aac10.aac10rs);
			txt1.print(StringUtils.extractNumbers(aac10.aac10ni));
			txt1.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201uf.aag02uf);

			def ie = getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id);
			txt1.print(inscrEstadual(ie));

			txt1.print(aac10.aac10municipio == null ? null : StringUtils.ajustString(aac10.aac10municipio.aag0201ibge, 7, '0', true));
			txt1.print(retirarMascara(aac10.aac10im));
			txt1.print(retirarMascara(aac10.aac10suframa));
			txt1.newLine();
			qtLin0140++;
			qtLinBloco0++;

			/**
			 * REGISTRO 0145: Regime de Apuração da Contribuição Previdenciária sobre a Receita Bruta
			 */
			if(contemDadosBlocoP) {
				if(edb10 != null && (!edb10.edb10rbTotal.equals(0) || !edb10.edb10rbAtivCP.equals(0) || !edb10.edb10rbAtivSemCP.equals(0))) {
					txt1.print("0145");
					txt1.print("2"); //ver algum critério ou colocar opção na tela
					txt1.print(formatarValor(edb10.edb10rbTotal, 2));
					txt1.print(formatarValor(edb10.edb10rbAtivCP, 2));
					txt1.print(formatarValor(edb10.edb10rbAtivSemCP, 2));
					txt1.print(null);
					txt1.newLine();
					qtLin0145++;
					qtLinBloco0++;
				}
			}

			/**
			 * REGISTRO 0150: Tabela de Cadastro do Participante
			 */
			for(String key : map0150.keySet()) {
				Aac10 empresa = map0150.get(key).get("aac10");
				if(!empresa.aac10id.equals(aac10.aac10id)) continue;

				Abe01 abe01 = getSession().get(Abe01.class, map0150.get(key).getLong("abe01id"));
				txt1.print("0150");
				txt1.print(abe01.abe01codigo);
				txt1.print(abe01.abe01nome);

				Abe0101 abe0101 = buscarEnderecoPrincipalEntidade(abe01.abe01id);
				txt1.print(abe0101.abe0101pais == null ? null : abe0101.abe0101pais.aag01ibge);
				txt1.print(abe01.abe01ti.equals(0) ? StringUtils.ajustString(StringUtils.extractNumbers(abe01.abe01ni), 14) : null);
				txt1.print(abe01.abe01ti.equals(1) ? StringUtils.ajustString(StringUtils.extractNumbers(abe01.abe01ni), 11) : null);
				txt1.print(inscrEstadual(abe01.abe01ie));
				txt1.print(abe0101.abe0101municipio ==  null ? null : abe0101.abe0101municipio.aag0201uf.aag02uf.equalsIgnoreCase("EX") ? "9999999" : abe0101.abe0101municipio.aag0201ibge);
				txt1.print(retirarMascara(abe01.abe01suframa));
				txt1.print(abe0101.abe0101endereco);
				txt1.print(abe0101.abe0101numero);
				txt1.print(abe0101.abe0101complem);
				txt1.print(abe0101.abe0101bairro);
				txt1.newLine();
				qtLinBloco0++;
				qtLin0150++;
			}

			/**
			 * REGISTRO 0190: Identificação das unidades de medida (obtenção dos dados)
			 */
			for(String key : map0190.keySet()) {
				Aac10 empresa = map0190.get(key).get("aac10");
				if(!empresa.aac10id.equals(aac10.aac10id)) continue;

				Aam06 aam06 = map0190.get(key).get("aam06");
				if(aam06 == null) continue;

				txt1.print("0190");
				txt1.print(aam06.aam06codigo);
				txt1.print(aam06.aam06descr);
				txt1.newLine();
				qtLinBloco0++;
				qtLin0190++;
			}

			/**
			 * REGISTRO 0200: Tabela de identificação do item (Produto e Serviços) (obtenção dos dados)
			 */
			for(String key : map0200.keySet()) {
				Aac10 empresa = map0200.get(key).get("aac10");
				if(!empresa.aac10id.equals(aac10.aac10id)) continue;

				Abm01 abm01 = getSession().get(Abm01.class, map0200.get(key).getLong("abm01id"));
				Abm0101 abm0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", abm01.abm01id), Criterions.eq("abm0101empresa", aac10.aac10id));
				if(abm0101 == null) throw new ValidacaoException("Necessário informar as configurações do item " + abm01.abm01codigo + " para a empresa " + aac10.aac10codigo);
				Abm12 abm12 = abm0101.abm0101fiscal == null ? null : getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id);
				
				txt1.print("0200");
				txt1.print(abm01.abm01codigo);
				txt1.print(abm01.abm01descr);
				txt1.print(abm01.abm01gtin);
				txt1.print(null);

				Aam06 aam06 = abm01.abm01umu != null ? getSession().get(Aam06.class, abm01.abm01umu.aam06id) : null;
				txt1.print(aam06 == null ? null : aam06.aam06codigo);

				txt1.print(abm12 == null ? null : abm12.abm12tipo, 2);

				Abg01 abg01 = abm0101.abm0101ncm == null ? null : getSession().get(Abg01.class, abm0101.abm0101ncm.abg01id);
				String ncm = abg01 == null ? null : abg01.abg01codigo; 
				txt1.print(ncm == null ? null : ncm.indexOf("/") == -1 ? ncm : ncm.substring(0, ncm.indexOf("/")));
				txt1.print(ncm == null ? null : ncm.indexOf("/") == -1 ? null : ncm.substring(ncm.indexOf("/") +1));
				txt1.print(ncm == null ? null : ncm.substring(0, 2));

				Aaj05 aaj05 = abm12 == null ? null : abm12.abm12codServ == null ? null : getSession().get(Aaj05.class, abm12.abm12codServ.aaj05id);
				txt1.print(aaj05 == null ? null : aaj05.aaj05codigo);
				
				txt1.print(aac10.aac10municipio.aag0201json == null ? null : formatarValor(aac10.aac10municipio.aag0201json.getBigDecimal_Zero("AliqInterna"), 2));
				txt1.newLine();

				qtLinBloco0++;
				qtLin0200++;

				/**
				 * REGISTRO 0205: Alteração do Item
				 */
				def map0205 = compor0205(abm01, dtFinal);
				for(String key0205 : map0205.keySet()) {
					txt1.print("0205");
					txt1.print(map0205.get(key0205).getString("descr"));
					txt1.print(map0205.get(key0205).getDate("dtInicial").format(ddMMyyyy));
					txt1.print(map0205.get(key0205).getDate("dtFinal").format(ddMMyyyy));
					txt1.print(map0205.get(key0205).getString("codigo"));
					txt1.newLine();
					qtLinBloco0++;
					qtLin0205++;
				}

				/**
				 * REGISTRO 0206: Código de Produto Conforme Tabela Publicada pela ANP (Combustíveis)
				 */
				if(abm0101.abm0101fiscal.abm12codANP != null) {
					Aaj04 aaj04 = getSession().get(Aaj04.class, abm0101.abm0101fiscal.abm12codANP.aaj04id);
					txt1.print("0206");
					txt1.print(aaj04.aaj04codigo);
					txt1.newLine();
					qtLinBloco0++;
					qtLin0206++;
				}
			}

			/**
			 * REGISTRO 0400: Tabela de Natureza da Operação/Prestação
			 */
			for(String key : map0400.keySet()) {
				Aac10 empresa = map0400.get(key).get("aac10");
				if(!empresa.aac10id.equals(aac10.aac10id)) continue;

				Abb10 abb10 = getSession().get(Abb10.class, map0400.get(key).getLong("abb10id"));
				txt1.print("0400");
				txt1.print(abb10.abb10codigo);
				txt1.print(abb10.abb10descr);
				txt1.newLine();
				qtLinBloco0++;
				qtLin0400++;
			}

			/**
			 * REGISTRO 0450: Tabela de Informação Complementar do Documento Fiscal
			 */
			for(String key : map0450.keySet()) {
				Aac10 empresa = map0450.get(key).get("aac10");
				if(!empresa.aac10id.equals(aac10.aac10id)) continue;

				txt1.print("0450");
				txt1.print(map0450.get(key).getString("codigo"));

				def obs = map0450.get(key).getString("descricao");
				txt1.print(obs == null ? null : obs.length() > 255 ? obs.substring(0, 255) : obs);
				txt1.newLine();
				qtLinBloco0++;
				qtLin0450++;
			}
		}

		/**
		 * REGISTRO 0500: Plano de Contas Contábeis
		 */
		for(String abc10codigo : abc10s) {
			Abc10 abc10 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abc10 WHERE abc10codigo = :abc10codigo " + obterWherePadrao("Abc10"), Parametro.criar("abc10codigo", abc10codigo));
			
			txt1.print("0500");
			txt1.print(dtInicial.format(ddMMyyyy));
			txt1.print(abc10.abc10ecdNat, 2);
			txt1.print(abc10.abc10reduzido == 0 ? "S" : "A");

			switch (abc10.abc10codigo.length()) {
				case 1:  txt1.print("1");  break;
				case 2:  txt1.print("2");  break;
				case 3:  txt1.print("3");  break;
				case 5:  txt1.print("4");  break;
				case 7:  txt1.print("5");  break;
				case 11: txt1.print("6");  break;
				default: txt1.print(null); break;
			}

			txt1.print(abc10.abc10codigo);
			txt1.print(abc10.abc10nome);

			Aaj20 aaj20 = abc10.abc10ctaRef == null ? null : getSession().get(Aaj20.class, abc10.abc10ctaRef.aaj20id);
			txt1.print(aaj20 == null ? null : aaj20.aaj20codigo);
			txt1.print(null);
			txt1.newLine();
			qtLinBloco0++;
			qtLin0500++;
		}

		/**
		 * REGISTRO 0600: Centro de Custos
		 */
		for(Abb11 abb11 : abb11s) {
			txt1.print("0600");
			txt1.print(dtInicial.format(ddMMyyyy));
			txt1.print(abb11.abb11codigo);
			txt1.print(abb11.abb11nome);
			txt1.newLine();
			qtLinBloco0++;
			qtLin0600++;
		}

		/**
		 * REGISTRO 0990: Encerramento do Bloco 0
		 */
		qtLinBloco0++;

		txt1.print("0990");
		txt1.print(qtLinBloco0);
		txt1.newLine();
	}

	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * BLOCO 9: CONTROLE E ENCERRAMENTO DO ARQUIVO DIGITAL * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBloco9() {
		/**
		 * REGISTRO 9001: Abertura do Bloco 9
		 */
		txt2.print("9001");
		txt2.print("0");
		txt2.newLine();
		qtLinBloco9++;

		/**
		 * REGISTRO 9900: Registros do Arquivo
		 */
		//BLOCO 0
		gerarRegistro9900("0000", 1); qtLin9900++;
		gerarRegistro9900("0001", 1); qtLin9900++;
		gerarRegistro9900("0100", 1); qtLin9900++;
		gerarRegistro9900("0110", 1); qtLin9900++;
		if(qtLin0111 > 0)qtLin9900++; gerarRegistro9900("0111", qtLin0111);
		if(qtLin0120 > 0)qtLin9900++; gerarRegistro9900("0120", qtLin0120);
		if(qtLin0140 > 0)qtLin9900++; gerarRegistro9900("0140", qtLin0140);
		if(qtLin0145 > 0)qtLin9900++; gerarRegistro9900("0145", qtLin0145);
		if(qtLin0150 > 0)qtLin9900++; gerarRegistro9900("0150", qtLin0150);
		if(qtLin0190 > 0)qtLin9900++; gerarRegistro9900("0190", qtLin0190);
		if(qtLin0200 > 0)qtLin9900++; gerarRegistro9900("0200", qtLin0200);
		if(qtLin0205 > 0)qtLin9900++; gerarRegistro9900("0205", qtLin0205);
		if(qtLin0206 > 0)qtLin9900++; gerarRegistro9900("0206", qtLin0206);
		if(qtLin0400 > 0)qtLin9900++; gerarRegistro9900("0400", qtLin0400);
		if(qtLin0450 > 0)qtLin9900++; gerarRegistro9900("0450", qtLin0450);
		if(qtLin0460 > 0)qtLin9900++; gerarRegistro9900("0460", qtLin0460);
		if(qtLin0500 > 0)qtLin9900++; gerarRegistro9900("0500", qtLin0500);
		if(qtLin0600 > 0)qtLin9900++; gerarRegistro9900("0600", qtLin0600);
		gerarRegistro9900("0990", 1); qtLin9900++;

		//BLOCO A
		gerarRegistro9900("A001", 1); qtLin9900++;
		if(qtLinA010 > 0)qtLin9900++; gerarRegistro9900("A010", qtLinA010);
		if(qtLinA100 > 0)qtLin9900++; gerarRegistro9900("A100", qtLinA100);
		if(qtLinA110 > 0)qtLin9900++; gerarRegistro9900("A110", qtLinA110);
		if(qtLinA111 > 0)qtLin9900++; gerarRegistro9900("A111", qtLinA111);
		if(qtLinA120 > 0)qtLin9900++; gerarRegistro9900("A120", qtLinA120);
		if(qtLinA170 > 0)qtLin9900++; gerarRegistro9900("A170", qtLinA170);
		gerarRegistro9900("A990", 1); qtLin9900++;

		//BLOCO C
		gerarRegistro9900("C001", 1); qtLin9900++;
		if(qtLinC010 > 0)qtLin9900++; gerarRegistro9900("C010", qtLinC010);
		//C100
		if(qtLinC100 > 0)qtLin9900++; gerarRegistro9900("C100", qtLinC100);
		if(qtLinC110 > 0)qtLin9900++; gerarRegistro9900("C110", qtLinC110);
		if(qtLinC111 > 0)qtLin9900++; gerarRegistro9900("C111", qtLinC111);
		if(qtLinC120 > 0)qtLin9900++; gerarRegistro9900("C120", qtLinC120);
		if(qtLinC170 > 0)qtLin9900++; gerarRegistro9900("C170", qtLinC170);
		if(qtLinC175 > 0)qtLin9900++; gerarRegistro9900("C175", qtLinC175);
		//C300
		if(qtLinC395 > 0)qtLin9900++; gerarRegistro9900("C395", qtLinC395);
		if(qtLinC396 > 0)qtLin9900++; gerarRegistro9900("C396", qtLinC396);
		//C500
		if(qtLinC500 > 0)qtLin9900++; gerarRegistro9900("C500", qtLinC500);
		if(qtLinC501 > 0)qtLin9900++; gerarRegistro9900("C501", qtLinC501);
		if(qtLinC505 > 0)qtLin9900++; gerarRegistro9900("C505", qtLinC505);
		//C860
		if(qtLinC860 > 0)qtLin9900++; gerarRegistro9900("C860", qtLinC860);
		if(qtLinC870 > 0)qtLin9900++; gerarRegistro9900("C870", qtLinC870);
		gerarRegistro9900("C990", 1); qtLin9900++;

		//BLOCO D
		gerarRegistro9900("D001", 1); qtLin9900++;
		if(qtLinD010 > 0)qtLin9900++; gerarRegistro9900("D010", qtLinD010);
		//D100
		if(qtLinD100 > 0)qtLin9900++; gerarRegistro9900("D100", qtLinD100);
		if(qtLinD101 > 0)qtLin9900++; gerarRegistro9900("D101", qtLinD101);
		if(qtLinD105 > 0)qtLin9900++; gerarRegistro9900("D105", qtLinD105);
		//D500
		if(qtLinD500 > 0)qtLin9900++; gerarRegistro9900("D500", qtLinD500);
		if(qtLinD501 > 0)qtLin9900++; gerarRegistro9900("D501", qtLinD501);
		if(qtLinD505 > 0)qtLin9900++; gerarRegistro9900("D505", qtLinD505);
		gerarRegistro9900("D990", 1); qtLin9900++;

		//BLOCO F
		gerarRegistro9900("F001", 1); qtLin9900++;
		if(qtLinF010 > 0)qtLin9900++; gerarRegistro9900("F010", qtLinF010);
		if(qtLinF100 > 0)qtLin9900++; gerarRegistro9900("F100", qtLinF100);
		if(qtLinF120 > 0)qtLin9900++; gerarRegistro9900("F120", qtLinF120);
		if(qtLinF130 > 0)qtLin9900++; gerarRegistro9900("F130", qtLinF130);
		if(qtLinF150 > 0)qtLin9900++; gerarRegistro9900("F150", qtLinF150);
		if(qtLinF550 > 0)qtLin9900++; gerarRegistro9900("F550", qtLinF550);
		if(qtLinF600 > 0)qtLin9900++; gerarRegistro9900("F600", qtLinF600);
		if(qtLinF700 > 0)qtLin9900++; gerarRegistro9900("F700", qtLinF700);
		if(qtLinF800 > 0)qtLin9900++; gerarRegistro9900("F800", qtLinF800);
		gerarRegistro9900("F990", 1); qtLin9900++;

		//BLOCO M
		gerarRegistro9900("M001", 1); qtLin9900++;
		gerarRegistro9900("M990", 1); qtLin9900++;

		//BLOCO P
		if(contemDadosBlocoP) {
			gerarRegistro9900("P001", 1); qtLin9900++;
			if(qtLinP010 > 0)qtLin9900++; gerarRegistro9900("P010", qtLinP010);
			if(qtLinP100 > 0)qtLin9900++; gerarRegistro9900("P100", qtLinP100);
			if(qtLinP200 > 0)qtLin9900++; gerarRegistro9900("P200", qtLinP200);
			if(qtLinP210 > 0)qtLin9900++; gerarRegistro9900("P210", qtLinP210);
			gerarRegistro9900("P990", 1); qtLin9900++;
		}

		//BLOCO 1
		gerarRegistro9900("1001", 1); qtLin9900++;
		if(qtLin1010 > 0)qtLin9900++; gerarRegistro9900("1010", qtLin1010);
		if(qtLin1020 > 0)qtLin9900++; gerarRegistro9900("1020", qtLin1020);
		if(qtLin1900 > 0)qtLin9900++; gerarRegistro9900("1900", qtLin1900);
		gerarRegistro9900("1990", 1); qtLin9900++;

		//BLOCO 9
		gerarRegistro9900("9001", 1); qtLin9900++;
		if(qtLin9900 > 0)qtLin9900++; gerarRegistro9900("9900", qtLin9900 + 2); //2= 9990 e 9999
		gerarRegistro9900("9990", 1); qtLin9900++;
		gerarRegistro9900("9999", 1); qtLin9900++;

		qtLinBloco9 = qtLinBloco9 + qtLin9900;

		/**
		 * REGISTRO 9990: Encerramento do Bloco 9
		 */
		txt2.print("9990");
		txt2.print(3 + qtLin9900); //3 - 9001, 9990 e 9999
		txt2.newLine();
		qtLinBloco9++;

		/**
		 * REGISTRO 9999: Encerramento do Arquivo Digital
		 */
		qtLinBloco9++;

		txt2.print("9999");
		txt2.print(qtLinBloco0 + qtLinBlocoA + qtLinBlocoC + qtLinBlocoD + qtLinBlocoF + qtLinBlocoM + qtLinBlocoP + qtLinBloco1 + qtLinBloco9);
		txt2.newLine();
	}

	def gerarRegistro9900(String registro, int qtdLinhas) {
		if(qtdLinhas > 0) {
			txt2.print("9900");
			txt2.print(registro);
			txt2.print(qtdLinhas);
			txt2.newLine();
		}
	}
	
	private String formatarSerie(String serie, String modelo){
		if(modelo == null) modelo = "";
		
		serie = retirarMascara(serie);
		
		if(serie.length() > 3) serie = serie.substring(0, 3);
		
		if(modelo.equals("55") || modelo.equals("65")) {
			serie = serie.length() == 0 ? "000" : serie;
		}
				
		return serie;
	}

	private String retirarMascara(String palavra){
		if(palavra == null)return "";
		palavra = palavra.replaceAll("[.]", "");
		palavra = palavra.replaceAll("[/]", "");
		palavra = palavra.replaceAll("[-]", "");
		return palavra;
	}

	private String formatarValor(BigDecimal valor, int casasDecimais) {
		return formatarValor(valor, casasDecimais, true);
	}
	private String formatarValor(BigDecimal valor, int casasDecimais, boolean isRequerido) {
		if(valor == null) return null;

		if(valor == 0 && !isRequerido) return null;

		valor = valor.round(casasDecimais);

		def format = NumberFormat.getInstance(new Locale("pt", "BR"));
		format.setGroupingUsed(false);
		format.setMinimumFractionDigits(casasDecimais);
		format.setMaximumFractionDigits(casasDecimais);

		return format.format(valor);
	}

	private String inscrEstadual(String ie) {
		if(ie != null) {
			if(ie.equalsIgnoreCase("ISENTO") || ie.equalsIgnoreCase("ISENTA")) {
				ie = null;
			}else{
				ie = StringUtils.extractNumbers(ie);
			}
		}
		
		return ie;
	}

	private boolean contemDadosBlocoA(List<Aac10> empresas) {
		for(Aac10 aac10 : empresas) {
			def gpoEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");

			def blocoA = buscarDocumentosParaEFDEntradaA100(gpoEA, 0);
			if(blocoA != null && blocoA.size() > 0) return true;

			blocoA = buscarDocumentosParaEFDSaidaA100(gpoEA, 0);
			if(blocoA != null && blocoA.size() > 0) return true;
		}
		return false;
	}

	private void validacoes(List<Eaa01> eaa01s) {
		for(Eaa01 eaa01 : eaa01s) {
			validacoes(eaa01);
		}
	}
	private void validacoes(Eaa01 eaa01) {
		Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);

		if(eaa01.eaa01sitDoc == null) {
			if(eaa01.eaa01esMov == 0) {
				throw new ValidacaoException("A situação do documento não foi informada. Documento de entrada: " + abb01.abb01num);
			}else {
				throw new ValidacaoException("A situação do documento não foi informada. Documento de saída: " + abb01.abb01num);
			}
		}
	}

	private int verificarTipoPgto(Eaa01 eaa01, Abb01 abb01){
		if(versaoLeiaute >= 3) {
			return verificarTipoPgtoAtual(eaa01, abb01);
		}else {
			return verificarTipoPgtoAntiga(eaa01, abb01);
		}
	}
	private int verificarTipoPgtoAtual(Eaa01 eaa01, Abb01 abb01){
		List<Eaa0113> eaa0113s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0113 WHERE eaa0113doc = :eaa0113doc", Parametro.criar("eaa0113doc", eaa01.eaa01id));
		if(eaa0113s == null || eaa0113s == 0) return 2; //Outros

		if(eaa0113s.size() > 1) return 1; //A prazo

		for(eaa0113 in eaa0113s){
			long dif = DateUtils.dateDiff(abb01.abb01data, eaa0113.eaa0113dtVctoN, ChronoUnit.DAYS);
			if(dif <= 0) return 0; //A vista
			return 1; //A prazo
		}

		return 2; //Outros
	}
	def verificarTipoPgtoAntiga(Eaa01 eaa01, Abb01 abb01){
		List<Eaa0113> eaa0113s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0113 WHERE eaa0113doc = :eaa0113doc", Parametro.criar("eaa0113doc", eaa01.eaa01id));

		if(eaa0113s == null || eaa0113s.size() == 0)return 9; //Sem pgto

		if(eaa0113s.size() > 1) return 1; //A prazo

		for(eaa0113 in eaa0113s){
			long dif = DateUtils.dateDiff(abb01.abb01data, eaa0113.eaa0113dtVctoN, ChronoUnit.DAYS);
			if(dif <= 0) return 0; //A vista
			return 1; //A prazo
		}

		return 9; //Sem pgto
	}

	def comporRegistro0150(Aac10 estabelecimento, Long abe01id) {
		if(abe01id == null) return;

		String key = estabelecimento.aac10id + "/" + abe01id;

		def tm = new TableMap();
		tm.put("aac10", estabelecimento);
		tm.put("abe01id", abe01id);

		map0150.put(key, tm);
	}

	def comporRegistro0190(Aac10 estabelecimento, Aam06 aam06) {
		if(aam06 == null)return;

		String key = estabelecimento.aac10id + "/" + aam06.aam06codigo.trim();

		def tm = new TableMap();
		tm.put("aac10", estabelecimento);
		tm.put("aam06", aam06);

		map0190.put(key, tm);
	}

	def comporRegistro0200(Aac10 empresa, Long abm01id) {
		if(abm01id == null)return;

		def key = empresa.aac10id + "/" + abm01id;

		def tm = new TableMap();
		tm.put("aac10", empresa);
		tm.put("abm01id", abm01id);

		map0200.put(key, tm);
	}

	def comporRegistro0400(Aac10 empresa, Long abb10id) {
		if(abb10id == null) return;

		def key = empresa.aac10id + "/" + abb10id;
		def tm = new TableMap();
		tm.put("aac10", empresa);
		tm.put("abb10id", abb10id);

		map0400.put(key, tm);
	}

	def comporRegistro0450(Aac10 empresa, String codigo, String descr) {
		if(codigo == null)return;

		def key = empresa.aac10id + "/" + codigo;

		def tm = new TableMap();
		tm.put("aac10", empresa);
		tm.put("codigo", StringUtils.ajustString(codigo, 6, '0', true));
		tm.put("descricao", descr.replace('|', '-'));

		map0450.put(key, tm);
	}

	List<TableMap> comporRegistroInfComplementar(Aac10 empresa, String texto) {
		def mapRegistro110 = new ArrayList<TableMap>()

		if(texto != null) {
			while(texto.length() > 0) {
				def codigo = StringUtils.ajustString(""+(map0450.size()+1), 6, '0', true);

				def descricao = "";
				if(texto.length() > 255) {
					descricao = texto.substring(0, 255);
					texto = texto.substring(255);
				}else {
					descricao = texto;
					texto = "";
				}

				comporRegistro0450(empresa, codigo, descricao);

				def descrCompl = "";
				if(texto.length() > 255) {
					descrCompl = texto.substring(0, 255);
					texto = texto.substring(255);
				}else {
					descrCompl = texto;
					texto = "";
				}

				def tm = new TableMap();
				tm.put("cod_inf", codigo);
				tm.put("txt_compl", descrCompl);
				mapRegistro110.add(tm);
			}
		}

		return mapRegistro110;
	}

	def selecionarCSTPis(Integer eaa01esMov, Eaa0103 eaa0103, BigDecimal baseCalculo) {
		Aaj12 aaj12 = eaa0103.eaa0103cstPis == null ? null : getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id);
		def cst = aaj12 == null ? null : aaj12.aaj12codigo;

		if(eaa01esMov == 0 && !baseCalculo.equals(0)) { //Entrada e BC != 0
			Aaj12 aaj12Apur = edb10.edb10docCSTPis == null ? null : getSession().get(Aaj12.class, edb10.edb10docCSTPis.aaj12id);
			cst = aaj12Apur == null ? cst : aaj12Apur.aaj12codigo;
		}
		return cst;
	}

	def selecionarCSTPis(String cstPis, BigDecimal baseCalculo) {
		if(!baseCalculo.equals(0) && edb10.edb10docCSTPis != null) {
			Aaj12 aaj12 = getSession().get(Aaj12.class, edb10.edb10docCSTPis.aaj12id);
			cstPis = aaj12.aaj12codigo;
		}
		return cstPis;
	}

	def selecionarCSTCofins(Integer eaa01esMov, Eaa0103 eaa0103, BigDecimal baseCalculo) {
		Aaj13 aaj13 = eaa0103.eaa0103cstCofins == null ? null : getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id);
		def cst = aaj13 == null ? null : aaj13.aaj13codigo;

		if(eaa01esMov == 0 && !baseCalculo.equals(0)) {
			Aaj13 aaj13Apur = edb10.edb10docCSTCofins == null ? null : getSession().get(Aaj13.class, edb10.edb10docCSTCofins.aaj13id);
			cst = aaj13Apur == null ? cst : aaj13Apur.aaj13codigo;
		}
		return cst;
	}

	def selecionarCSTCofins(String cstCofins, BigDecimal baseCalculo) {
		if(!baseCalculo.equals(0) && edb10.edb10docCSTCofins != null) {
			Aaj13 aaj13Apur = getSession().get(Aaj13.class, edb10.edb10docCSTCofins.aaj13id);
			cstCofins = aaj13Apur.aaj13codigo;
		}

		return cstCofins;
	}

	boolean verificarSeContemDadosBlocoC(Aac10 empresa) {
		List<Aac10> empresas = new ArrayList<Aac10>();
		empresas.add(empresa);
		return verificarSeContemDadosBlocoC(empresas);
	}

	boolean verificarSeContemDadosBlocoC(List<Aac10> empresas) {
		for(Aac10 aac10 : empresas) {
			Long gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoC = buscarDocumentosC100PCSaída(gc, modelosC100);
			if(blocoC != null && blocoC.size() > 0) {
				return true;
			}
			
			blocoC = buscarDocumentosC100PCEntrada(gc, modelosC100);
			if(blocoC != null && blocoC.size() > 0) {
				return true;
			}
		}

		for(Aac10 aac10 : empresas) {
			Long gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoC = buscarDocumentosC395PC(gc, modelosC300);
			if(blocoC != null && blocoC.size() > 0) {
				return true;
			}
		}

		for(Aac10 aac10 : empresas) {
			Long gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoC = buscarDocumentosC500PC(gc, modelosC500);
			if(blocoC != null && blocoC.size() > 0) {
				return true;
			}
		}

		for(Aac10 aac10 : empresas) {
			Long gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoC = buscarDocumentosC860PC(gc, modelosC800);
			if(blocoC != null && blocoC.size() > 0) {
				return true;
			}
		}
		return false;
	}

	boolean verificarSeContemDadosBlocoD(Aac10 empresa) {
		List<Aac10> empresas = new ArrayList<Aac10>();
		empresas.add(empresa);
		return verificarSeContemDadosBlocoD(empresas);
	}
	boolean verificarSeContemDadosBlocoD(List<Aac10> empresas) {
		for(Aac10 aac10 : empresas) {
			def gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoD = buscarDocumentosD100PC(0, gc, modelosD100);
			if(blocoD != null && blocoD.size() > 0) {
				return true;
			}
		}

		for(Aac10 aac10 : empresas) {
			def gc = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			def blocoD = buscarDocumentosD500PC(gc, modelosD500);
			if(blocoD != null && blocoD.size() > 0) {
				return true;
			}
		}

		return false;
	}

	boolean verificarSeContemDadosBlocoF(Aac10 aac10, List<TableMap> mapF120, List<TableMap> mapF130) {
		def aac10s = new ArrayList<Aac10>();
		aac10s.add(aac10);
		return verificarSeContemDadosBlocoF(aac10s, mapF120, mapF130);
	}
	boolean verificarSeContemDadosBlocoF(List<Aac10> aac10s, List<TableMap> mapF120, List<TableMap> mapF130) {
		for(Aac10 aac10 : aac10s) {
			def gcEA = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EA");
			List<Eaa01> blocoF = buscarDocumentosF100PC(gcEA);
			if(blocoF != null && blocoF.size() > 0) {
				return true;
			}

			def edb11s = buscarOperacoesCreditoPorGC(gcEA);
			if(edb11s != null && edb11s.size() > 0){
				return true;
			}
			
			if(aac10.aac10json == null) aac10.aac10json = new TableMap()

			def valor = aac10.aac10json.getBigDecimal("ESTABERTVALOR");
			if(valor != null && !valor.equals(0)){
				def dtEst = aac10.aac10json.getDate("ESTABERTMESANO");
				if(dtEst != null){
					def data = dtEst.withDayOfMonth(1);
					def qtMeses = DateUtils.dateDiff(data, dtInicial, ChronoUnit.MONTHS);
					if(qtMeses >= 0 && qtMeses < 12) {
						return true;
					}
				}
			}
		}

		if(mapF120 != null && mapF120.size() > 0) return true;
		if(mapF130 != null && mapF130.size() > 0)  return true;

		for(Aac10 aac10 : aac10s) {
			def gcED = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "ED");
			def rsF550s = buscarDocumentosF550PC(gcED, getCampo("F550", "ALIQ_PIS"), getCampo("F550", "ALIQ_COFINS"), getCampo("F550", "VL_REC_COMP"), getCampo("F550", "VL_DESC_PIS"), getCampo("F550", "VL_BC_PIS"), getCampo("F550", "VL_PIS"), getCampo("F550", "VL_DESC_COFINS"), getCampo("F550", "VL_BC_COFINS"), getCampo("F550", "VL_COFINS"));
			if(rsF550s != null && rsF550s.size() > 0) {
				return true;
			}

			def edb12s = buscarRetencoesPorPeriodo(gcED, mes, ano);
			if(edb12s != null && edb12s.size() > 0) {
				return true;
			}

			def edb13s = buscarDeducoesPorPeriodo(gcED, mes, ano);
			if(edb13s != null && edb13s.size() > 0) {
				return true;
			}

			def edb14s = buscarCreditosPorPeriodo(gcED, mes, ano);
			if(edb14s != null && edb14s.size() > 0) {
				return true;
			}
		}

		return false;
	}

	boolean contemDadosBlocoP() {
		List<Edb1001> edb1001s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Edb1001 WHERE edb1001apur = :edb10id", Parametro.criar("edb10id", edb10.edb10id));
		return edb1001s != null && edb1001s.size() > 0;
	}

	boolean contemDadosBloco1() {
		if(abb40sReg1010 != null && abb40sReg1010.size() > 0) return true;
		if(abb40sReg1020 != null && abb40sReg1020.size() > 0) return true;
		if(gerouF550) return true;
		return false;
	}

	private Integer selecionarOperacaoF100(Aaj12 cst) {
		if(cst == null) return null;
	
		Integer codigo = 0;
		try {
			codigo = Integer.parseInt(cst.getAaj12codigo());
		}catch(NumberFormatException err) {
			codigo = 0;
		}

		Integer oper = null;
		if(codigo >= 50 && codigo <= 66) {
			oper = 0;
		}else if(codigo <= 3 || codigo == 5) {
			oper = 1;
		}else if(codigo == 4 || (codigo >= 6 && codigo <= 9) || codigo == 49) {
			oper = 2;
		}

		return oper;
	}

	Aac10 buscarEmpresaMatriz() {
		Aac10 empresaAtiva = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());
		empresaMatriz = empresaAtiva.aac10matriz == null ? empresaAtiva : getAcessoAoBanco().obterEmpresa(empresaAtiva.aac10matriz.aac10id);
	}
	
	def buscarEmpresasFiliais() {
		def emps = getAcessoAoBanco().buscarListaDeTableMap("SELECT aac10id FROM Aac10 WHERE aac10matriz = :aac10id", Parametro.criar("aac10id", empresaMatriz.aac10id));

		for(TableMap tm : emps) {
			aac10s.add(getAcessoAoBanco().obterEmpresa(tm.getLong("aac10id")));
		}
	}

	Long buscarGrupoCentralizadorPorEmpresaTabela(Long aac10id, String tabela) {
		def sql = " SELECT aac01id FROM Aac01 WHERE aac01id IN " +
				" (SELECT aac1001gc FROM Aac1001 WHERE aac1001empresa = :aac10id AND UPPER(aac1001tabela) = :tabela) ";

		return getAcessoAoBanco().obterLong(sql, Parametro.criar("aac10id", aac10id), Parametro.criar("tabela", tabela));
	}

	List<Eaa0103> buscarItensDoDocumento(Long eaa01id) {
		return getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id", Parametro.criar("eaa01id", eaa01id));
	}

	Edb10 buscarApuracaoDeReceitaPorMesAno(Integer mes, Integer ano) {
		return getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Edb10 WHERE edb10mes = :mes AND edb10ano = :ano " + obterWherePadrao("Edb10"), 
			                      Parametro.criar("mes", mes), 
								  Parametro.criar("ano", ano));
	}

	List<Long> buscarDocumentosParaEFDEntradaA100(Long gc, Integer pagina) {
		def sql = " SELECT eaa01id FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " WHERE eaa01esMov = 0 " +
				  " AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				  " AND eaa0103codBcCred IS NOT NULL " +
				  " AND eaa01iEfdContrib = 1 " +
				  " AND eaa01iLivroServ = :livServ " +
				  " AND eaa01gc = :gc";

		return getAcessoAoBanco().obterListaDeLong(sql, true, pagina, Parametro.criar("dtInicial", dtInicial), 
			                                                          Parametro.criar("dtFinal", dtFinal), 
																	  Parametro.criar("livServ", Eaa01.ILIVROSERV_SIM), 
																	  Parametro.criar("gc", gc));
	}

	List<Long> buscarDocumentosParaEFDSaidaA100(Long gc, Integer pagina) {
		def sql = " SELECT * FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " WHERE eaa01esMov = 1 " +
				  " AND abb01data BETWEEN :dtInicial AND :dtFinal " +
				  " AND eaa0103clasReceita > 0 " +
				  " AND eaa01iEfdContrib = 1 " +
				  " AND eaa01iLivroServ = :livServ " +
				  " AND eaa01gc = :gc";

		return getAcessoAoBanco().obterListaDeLong(sql, true, pagina, Parametro.criar("dtInicial", dtInicial), 
			                                                          Parametro.criar("dtFinal", dtFinal), 
																	  Parametro.criar("livServ", Eaa01.ILIVROSERV_SIM), 
																	  Parametro.criar("gc", gc));
	}

	List<Eaa01034> buscarDeclaracoesDeImportacao(Long eaa01id) {
		def sql = " SELECT * FROM Eaa01034 " +
				" INNER JOIN Eaa0103 ON eaa0103id = eaa01034item " +
				" INNER JOIN Eaa01 ON eaa01id = :eaa01id";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<Eaa01> buscarDocumentosC100PCEntrada(Long gc, List<String> modelos) {
		def sql = " SELECT * FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
				" WHERE eaa01esMov = 0 " +
				" AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				" AND eaa0103codBcCred IS NOT NULL " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01iEfdContrib = 1 " +
				" AND eaa01gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
															  Parametro.criar("modelos", modelos), 
															  Parametro.criar("gc", gc));
	}

	List<Eaa01> buscarDocumentosC100PCSaída(Long gc, List<String> modelos) {
		def sql = " SELECT * FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
				" WHERE eaa01esMov = 1 " +
				" AND abb01data BETWEEN :dtInicial AND :dtFinal " +
				" AND eaa0103clasReceita > 0 " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01iEfdContrib = 1 " +
				" AND eaa01gc = :gc ";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
															  Parametro.criar("modelos", modelos), 
															  Parametro.criar("gc", gc));
	}

	List<Eaa01> buscarDocumentosC395PC(Long gc, List<String> modelos) {
		def sql = " SELECT DISTINCT * FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
				" WHERE eaa01esMov = :mov " +
				" AND abb01data BETWEEN :dtInicial AND :dtFinal " +
				" AND eaa01cancData IS NULL " +
				" AND eaa0103codBcCred IS NOT NULL " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
															  Parametro.criar("mov", Eaa01.ESMOV_ENTRADA), 
															  Parametro.criar("modelos", modelos), 
															  Parametro.criar("gc", gc));
	}

	List<Eaa01> buscarDocumentosC500PC(Long gc, List<String> modelos) {
		def sql = " SELECT DISTINCT * FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
				" WHERE eaa01esMov = :mov " +
				" AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				" AND eaa0103codBcCred IS NOT NULL " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
   		  	                                                  Parametro.criar("mov", Eaa01.ESMOV_ENTRADA), 
														      Parametro.criar("modelos", modelos), 
															  Parametro.criar("gc", gc));
	}

	List<TableMap> buscarDocumentosC860PC(Long gc, List<String> modelos) {
		def sql = " SELECT aah01modelo, abd10serieFabr, abb01data, abb01num, eaa01id " +
				" FROM Eaa01 " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" LEFT JOIN Abd10 ON abd10id = eaa01cfEF" +
				" WHERE eaa01esMov = :mov " +
				" AND abb01data BETWEEN :dtInicial AND :dtFinal " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01gc = :gc " +
				" ORDER BY aah01modelo, abd10serieFabr, abb01data, abb01num";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                 Parametro.criar("dtFinal", dtFinal), 
															 Parametro.criar("mov", Eaa01.ESMOV_ENTRADA), 
															 Parametro.criar("modelos", modelos), 
															 Parametro.criar("gc", gc));
	}

	public List<TableMap> buscarResumoValoresC175PC(Long eaa01id, String cpoVlr1, String cpoVlr2, String cpoVlr3, String aliqPis, String cpoVlr4, String cpoVlr5, String cpoVlr6, String cpoVlr7, String aliqCofins, String cpoVlr8, String cpoVlr9, String cpoVlr10) {
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqPis + ")::numeric AS " + aliqPis);
		select.append(", jGet(eaa0103json." + aliqCofins + ")::numeric AS " + aliqCofins);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) AS " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) AS " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) AS " + cpoVlr3);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr4 + ")::numeric) AS " + cpoVlr4);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr5 + ")::numeric) AS " + cpoVlr5);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr6 + ")::numeric) AS " + cpoVlr6);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr7 + ")::numeric) AS " + cpoVlr7);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr8 + ")::numeric) AS " + cpoVlr8);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr9 + ")::numeric) AS " + cpoVlr9);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr10 + ")::numeric) AS " + cpoVlr10);

		def sql = " SELECT aaj15codigo, aaj12codigo, abc10codigo, aaj13codigo" + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				  " LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				  " LEFT JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				  " WHERE eaa01id = :eaa01id " +
				  " GROUP BY aaj15codigo, aaj12codigo, abc10codigo, aaj13codigo, jGet(eaa0103json." + aliqPis + ")::numeric, jGet(eaa0103json." + aliqCofins + ")::numeric " +
				  " ORDER BY aaj15codigo, aaj12codigo, aaj13codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarResumoValoresC501PC(Long eaa01id, String aliqPis, String bcPis, String pis) {
		if(aliqPis == null) return new ArrayList<TableMap>(); 
			
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqPis + ")::numeric AS " + aliqPis);
		if(bcPis != null) select.append(", SUM(jGet(eaa0103json." + bcPis + ")::numeric) AS " + bcPis);
		if(pis != null) select.append(", SUM(jGet(eaa0103json." + pis + ")::numeric) AS " + pis);
		
		def sql = "SELECT aaj12codigo AS cstPis, aaj30codigo AS codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc " + select.toString() +
				" FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				" LEFT JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				" LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				" LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				" WHERE eaa01id = :eaa01id " +
				" GROUP BY aaj12codigo, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqPis + ") " +
				" ORDER BY aaj12codigo, aaj30codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarResumoValoresC505PC(Long eaa01id, String aliqCof, String bcCof, String cofins) {
		if(aliqCof == null) return new ArrayList<TableMap>();
		
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqCof + ") AS " + aliqCof);
		if(bcCof != null) select.append(", SUM(jGet(eaa0103json." + bcCof + ")::numeric) AS " + bcCof);
		if(cofins != null) select.append(", SUM(jGet(eaa0103json." + cofins + ")::numeric) AS " + cofins);

		def sql = " SELECT aaj13codigo as cstCofins, aaj30codigo as codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc" + select.toString() +
				" FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				" LEFT JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				" LEFT JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				" LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				" WHERE eaa01id = :eaa01id " +
				" GROUP BY aaj13codigo, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqCof + ") " +
				" ORDER BY aaj13codigo, aaj30codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarValoresC870PC(Set<Long> eaa01Ids, String cpoVlr1, String cpoVlr2, String cpoVlr3, String aliqPis, String cpoVlr4, String cpoVlr5, String aliqCofins) {
		def select = new StringBuilder("");
		if(cpoVlr1 != null)select.append(", jGet(eaa0103json." + cpoVlr1 + ")::numeric");
		if(cpoVlr2 != null)select.append(", jGet(eaa0103json." + cpoVlr2 + ")::numeric");
		if(cpoVlr3 != null)select.append(", jGet(eaa0103json." + cpoVlr3 + ")::numeric");
		if(cpoVlr4 != null)select.append(", jGet(eaa0103json." + cpoVlr4 + ")::numeric");
		if(cpoVlr5 != null)select.append(", jGet(eaa0103json." + cpoVlr5 + ")::numeric");
		if(aliqPis != null)select.append(", jGet(eaa0103json." + aliqPis + ")::numeric");
		if(aliqCofins != null)select.append(", jGet(eaa0103json." + aliqCofins + ")::numeric");

		def sql = "SELECT eaa0103doc, abm01id, abm01tipo, abm01codigo, aam06id, aaj15codigo, abc10codigo, eaa0103totDoc, aaj12codigo as cstPis, aaj13codigo as cstCofins" + select.toString() +
				" FROM Eaa0103 " +
				" INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				" INNER JOIN Abm01 ON abm01id = eaa0103item " +
				" INNER JOIN Aam06 ON aam06id = eaa0103umComl " +
				" LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				" LEFT JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				" LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				" LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				" WHERE eaa0103doc IN (:eaa01Ids) AND eaa01cancData IS NULL " +
				" ORDER BY eaa0103doc";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01Ids", eaa01Ids));
	}

	Abb40 buscarProcessoReferenciado(Long eaa01id) {
		def sql = " SELECT * FROM Abb40 " +
				" INNER JOIN Eaa0102 ON eaa0102processo = abb40id " +
				" INNER JOIN Eaa01 ON eaa01id = eaa0102doc " +
				" WHERE eaa01id = :eaa01id";

		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<Eaa01> buscarDocumentosD100PC(Integer mov, Long gc, List<String> modelos) {
		def entrada = "(eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal AND eaa0103codBcCred IS NOT NULL) ";
		def saida = "(eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal AND eaa0103clasReceita > 0) ";
		def where = mov == 0 ? entrada : mov == 1 ? saida : "(" + entrada + " OR " + saida + ")";

		def sql = " SELECT * FROM Eaa01 " +
				" INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" WHERE " + where +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01gc = :gc ";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
															  Parametro.criar("modelos", modelos), 
															  Parametro.criar("gc", gc));
	}
	
	Abc10 selecionarCtaContabilDoDocto(Long eaa01id) {
		def sql = " SELECT * FROM Abc10 " +
		          " INNER JOIN Eaa0103 ON abc10id = eaa0103cta " +
				  " WHERE eaa0103doc = :eaa01id " +
				  " ORDER BY eaa0103seq";

		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<Eaa01> buscarDocumentosD500PC(Long gc, List<String> modelos) {
		def sql = " SELECT * FROM Eaa01 " +
				" INNER JOIN Eaa0103 ON eaa01id = eaa0103doc " +
				" INNER JOIN Abb01 ON abb01id = eaa01central " +
				" INNER JOIN Aah01 ON aah01id = abb01tipo " +
				" WHERE eaa01esMov = :mov " +
				" AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				" AND eaa0103codBcCred IS NOT NULL " +
				" AND aah01modelo IN (:modelos) " +
				" AND eaa01gc = :gc";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                 Parametro.criar("dtFinal", dtFinal), 
															 Parametro.criar("modelos", modelos), 
															 Parametro.criar("mov", 0), 
															 Parametro.criar("gc", gc));
	}

	List<TableMap> buscarResumoValoresD101PC(Long eaa01id, String aliqPis, String bcPis, String pis) {
		if(aliqPis == null) return new ArrayList<TableMap>();
		
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqPis + ")::numeric AS " + aliqPis);
		select.append(", SUM(jGet(eaa0103json." + bcPis + ")::numeric) AS " + bcPis);
		select.append(", SUM(jGet(eaa0103json." + pis + ")::numeric) AS " + pis);

		def sql = " SELECT aaj12codigo as cstPis, eaa0102cteNatFrete, aaj30codigo as codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc" + select.toString() +
				" FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				" INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				" LEFT JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				" LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				" LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				" WHERE eaa01id = :eaa01id " +
				" GROUP BY aaj12codigo, eaa0102cteNatFrete, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqPis + ") " +
				" ORDER BY aaj12codigo, aaj30codigo, eaa0102cteNatFrete";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarResumoValoresD105PC(Long eaa01id, String aliqCof, String bcCof, String cofins) {
		if(aliqCof == null) return new ArrayList<TableMap>();
		
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqCof + ")::numeric AS " + aliqCof);
		select.append(", SUM(jGet(eaa0103json." + bcCof + ")::numeric) AS " + bcCof);
		select.append(", SUM(jGet(eaa0103json." + cofins + ")::numeric) AS " + cofins);

		def sql = " SELECT aaj13codigo as cstCofins, eaa0102cteNatFrete, aaj30codigo as codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc" + select.toString() +
			      " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " LEFT JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				  " LEFT JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				  " LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				  " WHERE eaa01id = :eaa01id " +
				  " GROUP BY aaj13codigo, eaa0102cteNatFrete, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqCof + ") " +
				  " ORDER BY aaj13codigo, aaj30codigo, eaa0102cteNatFrete";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarResumoValoresD501PC(Long eaa01id, String aliqPis, String bcPis, String pis) {
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqPis + ")::numeric AS " + aliqPis);
		select.append(", SUM(jGet(eaa0103json." + bcPis + ")::numeric) AS " + bcPis);
		select.append(", SUM(jGet(eaa0103json." + pis + ")::numeric) AS " + pis);

		def sql = " SELECT aaj12codigo as cstPis, aaj30codigo as codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc" + select.toString() +
				  " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				  " INNER JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				  " INNER JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				  " LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				  " WHERE eaa01id = :eaa01id " +
				  " GROUP BY aaj12codigo, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqPis + ") " +
				  " ORDER BY aaj12codigo, aaj30codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	List<TableMap> buscarResumoValoresD505PC(Long eaa01id, String aliqCof, String bcCof, String cofins) {
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqCof + ")::numeric AS " + aliqCof);
		select.append(", SUM(jGet(eaa0103json." + bcCof + ")::numeric AS " + bcCof);
		select.append(", SUM(jGet(eaa0103json." + cofins + ")::numeric AS " + cofins);

		def sql = " SELECT aaj13codigo as cstCofins, aaj30codigo as codBCCred, abc10codigo, SUM(eaa0103totDoc) as eaa0103totDoc" + select.toString() +
			 	  " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " INNER JOIN Aaj30 ON aaj30id = eaa0103codBcCred " +
				  " INNER JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				  " LEFT JOIN Abc10 ON abc10id = eaa0103cta " +
				  " WHERE eaa01id = :eaa01id " +
				  " GROUP BY aaj13codigo, aaj30codigo, abc10codigo, jGet(eaa0103json." + aliqCof + ") " +
				  " ORDER BY aaj13codigo, aaj30codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	public List<TableMap> comporF120() {
		def mapF120 = new ArrayList<TableMap>();
		def setGrupoEC = new HashSet<Long>();

		for(Aac10 aac10 : aac10s) {
			def gcEC = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EC");
			if(setGrupoEC.contains(gcEC))continue;
			setGrupoEC.add(gcEC);

			def rsEc03s = buscarImobilizacoesF120(gcEC, mes, ano);
			if(rsEc03s != null & rsEc03s.size() > 0) {
				for(int i = 0; i < rsEc03s.size(); i++) {
					//Classificação do período
					Eca01 eca01 = null;
					Ecb0101 ecb0101 = buscarClassificacaoDoPeriodo(rsEc03s.get(i).getLong("ecb01id"), mes, ano);
					if(ecb0101 != null) eca01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca01 WHERE eca01id = :id", Parametro.criar("id", ecb0101.ecb0101clas.eca01id));

					/**
					 * Dados da imobilização
					 */
					if(rsEc03s.get(i).getString("aaj30codigo") != null) {
						if(rsEc03s.get(i).getInteger("ecb01pisCofins") != 1) continue;

						def tm = new TableMap();
						tm.put("empresa", aac10);
						tm.put("vlrDepreciacao", rsEc03s.get(i).getBigDecimal("vlrDepr"));
						tm.put("codBCCred", rsEc03s.get(i).getString("aaj30codigo"));
						tm.put("ident", rsEc03s.get(i).getInteger("ecb01ident"));
						tm.put("origem", rsEc03s.get(i).getInteger("ecb01origem") == 0 ? 1 : 0);
						tm.put("utilizacao", rsEc03s.get(i).getInteger("ecb01utilizacao"));

						Eca0101 eca0101 = eca01 == null ? null : getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca0101 WHERE eca0101clas = :eca01id AND eca0101seq = :seq", 
							                                                        Parametro.criar("eca01id", eca01.eca01id), Parametro.criar("seq", eca01.eca01seqBem));
						Abc10 abc10 = eca0101 == null ? null : getSession().get(Abc10.class, eca0101.eca0101cta.abc10id);
						tm.put("conta", abc10 == null ? null : abc10.abc10codigo);

						Abb11 abb11 = ecb0101 == null ? null : ecb0101.ecb0101depto == null ? null : getSession().get(Abb11.class, ecb0101.ecb0101depto.abb11id);
						tm.put("depto", abb11);

						tm.put("descricao", rsEc03s.get(i).getString("abb20codigo") + " - " + rsEc03s.get(i).getString("abb20nome"));

						def pPis = rsEc03s.get(i).getBigDecimal("ecb01aliqPis");
						if(pPis.equals(0) && edb10 != null) pPis = edb10.edb10pPis;
						tm.put("pPis", pPis);

						def pCofins = rsEc03s.get(i).getBigDecimal("ecb01aliqCofins");
						if(pCofins.equals(0) && edb10 != null) pCofins = edb10.edb10pCofins;
						tm.put("pCofins", pCofins);
						mapF120.add(tm);

						if(abc10 != null) abc10s.add(abc10.abc10codigo);

						/**
						 * Dados da classificação
						 */
					}else {
						if(eca01 == null || eca01.eca01pisCofins != 1) continue;

						def tm = new TableMap();
						tm.put("empresa", aac10);
						tm.put("vlrDepreciacao", rsEc03s.get(i).getBigDecimal("vlrDepr"));

						Aaj30 aaj30 = eca01.eca01codBcCred == null ? null : getSession().get(Aaj30.class, eca01.eca01codBcCred.aaj30id);
						tm.put("codBCCred",  aaj30 == null ? null : aaj30.aaj30codigo);
						tm.put("ident", eca01.eca01ident);
						tm.put("origem", eca01.eca01origem == 0 ? 1 : 0);
						tm.put("utilizacao", eca01.eca01utilizacao);

						Eca0101 eca0101 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca0101 WHERE eca0101clas = :eca01id AND eca0101seq = :seq", Parametro.criar("eca01id", eca01.eca01id), Parametro.criar("seq", eca01.eca01seqBem));
						Abc10 abc10 = eca0101 == null ? null : getSession().get(Abc10.class, eca0101.eca0101cta.abc10id);
						tm.put("conta", abc10 == null ? null : abc10.abc10codigo);

						Abb11 abb11 = ecb0101 == null ? null : ecb0101.ecb0101depto == null ? null : getSession().get(Abb11.class, ecb0101.ecb0101depto.abb11id);
						tm.put("depto", abb11);

						tm.put("descricao", rsEc03s.get(i).getString("abb20codigo") + " - " + rsEc03s.get(i).getString("abb20nome"));

						def pPis = rsEc03s.get(i).getBigDecimal("ecb01aliqPis");
						if(pPis.equals(0) && edb10 != null) pPis = edb10.edb10pPis;
						tm.put("pPis", pPis);

						def pCofins = rsEc03s.get(i).getBigDecimal("ecb01aliqCofins");
						if(pCofins.equals(0) && edb10 != null) pCofins = edb10.edb10pCofins;
						tm.put("pCofins", pCofins);
						mapF120.add(tm);

						if(abc10 != null) abc10s.add(abc10.abc10codigo);
					}
				}
			}
		}

		return mapF120;
	}

	List<TableMap> comporF130() {
		def mapF130 = new ArrayList<TableMap>();
		def setGrupoEC = new HashSet<Long>();

		for(Aac10 aac10 : aac10s) {
			def gcEC = buscarGrupoCentralizadorPorEmpresaTabela(aac10.aac10id, "EC");
			if(setGrupoEC.contains(gcEC))continue;
			setGrupoEC.add(gcEC);

			def rsEc03s = buscarImobilizacoesF130(gcEC, mes, ano);
			if(rsEc03s != null && rsEc03s.size() > 0) {
				for(int i = 0; i < rsEc03s.size(); i++) {

					def numParc = rsEc03s.get(i).getInteger("ecb01parcPis");
					def dataAquisicao = rsEc03s.get(i).getDate("abb20aquis").withMonth(numParc);

					if(numParc == 0 || DateUtils.numMeses(dataAquisicao.getMonthValue(), dataAquisicao.getYear()) < DateUtils.numMeses(mes, ano)) continue;

					//Classificação do período
					Eca01 eca01 = null;
					Ecb0101 ecb0101 = buscarClassificacaoDoPeriodo(rsEc03s.get(i).getLong("ecb01id"), mes, ano);
					if(ecb0101 != null) eca01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca01 WHERE eca01id = :id", Parametro.criar("id", ecb0101.ecb0101clas.eca01id));
					
					/**
					 * Dados da imobilização
					 */
					if(rsEc03s.get(i).getString("aaj30codigo") != null) {
						if(rsEc03s.get(i).getInteger("ecb01pisCofins") != 2) continue;

						def tm = new TableMap();
						tm.put("empresa", aac10);
						tm.put("valor", rsEc03s.get(i).getBigDecimal("ecb01vlrAquis"));
						tm.put("aquis", rsEc03s.get(i).getDate("abb20aquis"));
						tm.put("numParc", rsEc03s.get(i).getInteger("ecb01parcPis"));

						tm.put("codBCCred", rsEc03s.get(i).getString("aaj30codigo"));
						tm.put("ident", rsEc03s.get(i).getInteger("ecb01ident"));
						tm.put("origem", rsEc03s.get(i).getInteger("ecb01origem") == 0 ? 1 : 0);
						tm.put("utilizacao", rsEc03s.get(i).getInteger("ecb01utilizacao"));

						Eca0101 eca0101 = eca01 == null ? null : getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca0101 WHERE eca0101clas = :eca01id AND eca0101seq = :seq", 
							                                                        Parametro.criar("eca01id", eca01.eca01id), Parametro.criar("seq", eca01.eca01seqBem));
																				
						Abc10 abc10 = eca0101 == null ? null : getSession().get(Abc10.class, eca0101.eca0101cta.abc10id);
						tm.put("conta", abc10 == null ? null : abc10.abc10codigo);
						if(abc10 != null) abc10s.add(abc10.abc10codigo);
						
						Abb11 abb11 = ecb0101 == null ? null : ecb0101.ecb0101depto == null ? null : getSession().get(Abb11.class, ecb0101.ecb0101depto.abb11id);
						tm.put("depto", abb11);

						tm.put("descricao", rsEc03s.get(i).getString("abb20codigo") + " - " + rsEc03s.get(i).getString("abb20nome"));

						def pPis = rsEc03s.get(i).getBigDecimal("ecb01aliqPis");
						if(pPis.equals(0) && edb10 != null) pPis = edb10.edb10pPis;
						tm.put("pPis", pPis);

						def pCofins = rsEc03s.get(i).getBigDecimal("ecb01aliqCofins");
						if(pCofins.equals(0) && edb10 != null) pCofins = edb10.edb10pCofins;
						tm.put("pCofins", pCofins);
						mapF130.add(tm);
						

						/**
						 * Dados da classificação
						 */
					}else {
						if(eca01 == null || eca01.eca01pisCofins != 2) continue;

						def tm = new TableMap();
						tm.put("empresa", aac10);
						tm.put("valor", rsEc03s.get(i).getBigDecimal("ecb01vlrAquis"));
						tm.put("aquis", rsEc03s.get(i).getDate("abb20aquis"));
						tm.put("numParc", rsEc03s.get(i).getInteger("ecb01parcPis"));

						Aaj30 aaj30 = eca01.eca01codBcCred == null ? null : getSession().get(Aaj30.class, eca01.eca01codBcCred.aaj30id);
						tm.put("codBCCred",  aaj30 == null ? null : aaj30.aaj30codigo);

						tm.put("ident", eca01.eca01ident);
						tm.put("origem", eca01.eca01origem == 0 ? 1 : 0);
						tm.put("utilizacao", eca01.eca01utilizacao);

						Eca0101 eca0101 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca0101 WHERE eca0101clas = :eca01id AND eca0101seq = :seq", 
							                                                     Parametro.criar("eca01id", eca01.eca01id), Parametro.criar("seq", eca01.eca01seqBem));
																			 
						Abc10 abc10 = eca0101 == null ? null : getSession().get(Abc10.class, eca0101.eca0101cta.abc10id);
						tm.put("conta", abc10 == null ? null : abc10.abc10codigo);
						if(abc10 != null) abc10s.add(abc10.abc10codigo);

						Abb11 abb11 = ecb0101 == null ? null : ecb0101.ecb0101depto == null ? null : getSession().get(Abb11.class, ecb0101.ecb0101depto.abb11id);
						tm.put("depto", abb11);

						tm.put("descricao", rsEc03s.get(i).getString("abb20codigo") + " - " + rsEc03s.get(i).getString("abb20nome"));

						def pPis = rsEc03s.get(i).getBigDecimal("ecb01aliqPis");
						if(pPis.equals(0) && edb10 != null) pPis = edb10.edb10pPis;
						tm.put("pPis", pPis);

						def pCofins = rsEc03s.get(i).getBigDecimal("ecb01aliqCofins");
						if(pCofins.equals(0) && edb10 != null) pCofins = edb10.edb10pCofins;
						tm.put("pCofins", pCofins);
						mapF130.add(tm);
					}
				}
			}
		}

		return mapF130;
	}
	
	boolean movimentouEstoque(Eaa01 eaa01, Eaa0103 eaa0103) {
		boolean movEstoque = !eaa01.eaa01iSCE_Zero;
		
		if(movEstoque) {
			def sql = "SELECT COUNT(*) FROM Eaa01038 " +
					  "INNER JOIN Eaa0103 ON eaa0103id = eaa01038item " +
					  "INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
					  "WHERE eaa01038item = :eaa0103id AND eaa01id = :eaa01id ";

            def result = getSession().createQuery(sql)
			   						 .setParameters("eaa0103id", eaa0103.eaa0103id, "eaa01id", eaa0103.eaa0103doc.eaa01id)
									 .getUniqueResult(ColumnType.INTEGER);
									 
            movEstoque = result > 0;
		}
		
		return movEstoque;
	}
	
	List<TableMap> buscarImobilizacoesF120(Long gcEC, Integer mes, Integer ano) {
		def sql = " SELECT ecb01id, abb20codigo, abb20nome, ecb01dtImob, ecb01parcPis, ecb01vlrAtual, abb20aquis, SUM(ecb0102deprec) as vlrDepr, " +
				  " aaj30codigo, ecb01pisCofins, ecb01ident, ecb01origem, ecb01utilizacao, ecb01aliqPis, ecb01aliqCofins " +
				  " FROM Ecb01 INNER JOIN Ecb0102 ON ecb01id = ecb0102imob " +
				  " INNER JOIN Abb20 ON abb20id = ecb01bem " +
				  " LEFT JOIN Aaj30 ON aaj30id = ecb01codBcCred " +
				  " WHERE (abb20baixa IS NULL OR (" + Fields.year("abb20baixa") + " * 12) + " + Fields.month("abb20baixa")+ " >= :dtReferencia) " +
				  " AND ecb0102mes = :mes AND ecb0102ano = :ano AND ecb01gc = :gcEC " +
				  " GROUP BY ecb01id, abb20codigo, abb20nome, ecb01dtImob, ecb01parcPis, ecb01vlrAtual, abb20aquis, " +
				  " aaj30codigo, ecb01pisCofins, ecb01ident, ecb01origem, ecb01utilizacao, ecb01aliqPis, ecb01aliqCofins";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtReferencia", DateUtils.numMeses(mes, ano)), 
			                                                 Parametro.criar("mes", mes), 
															 Parametro.criar("ano", ano), 
															 Parametro.criar("gcEC", gcEC));
	}

	Ecb0101 buscarClassificacaoDoPeriodo(Long ecb01id, Integer mes, Integer ano){
		def sql = " SELECT * FROM Ecb0101 " +
			  	  " WHERE ecb0101imob = :ecb01id " +
				  " AND " + Fields.numMeses("ecb0101mes", "ecb0101ano") + " <= :mesAno " +
				  " ORDER BY ecb0101ano DESC, ecb0101mes DESC";

		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("mesAno", DateUtils.numMeses(mes, ano)), 
			                                               Parametro.criar("ecb01id", ecb01id));
	}

	List<TableMap> buscarImobilizacoesF130(Long gcEC, Integer mes, Integer ano) {
		def sql = " SELECT ecb01id, abb20codigo, abb20nome, abb20aquis, ecb01parcPis, ecb01vlrAquis, aaj30codigo, ecb01pisCofins, ecb01ident, ecb01origem, ecb01utilizacao, ecb01aliqPis, ecb01aliqCofins " +
				  " FROM Ecb01 " +
				  " INNER JOIN Abb20 ON abb20id = ecb01bem " +
				  " LEFT JOIN Aaj30 ON aaj30id = ecb01codBcCred " +
				  " WHERE (abb20baixa IS NULL OR (" + Fields.year("abb20baixa") + " * 12) + " + Fields.month("abb20baixa") + " >= :dtReferencia) " +
				  " AND (" + Fields.year("abb20aquis") + " * 12) + " + Fields.month("abb20aquis") + " <= :dtReferencia " +
				  " AND ecb01parcPis > 0 AND ecb01gc = :gcEC " +
				  " ORDER BY abb20codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtReferencia", DateUtils.numMeses(mes, ano)), 
			                                                 Parametro.criar("gcEC", gcEC));
	}

	List<Eaa01> buscarDocumentosF100PC(Long gc) {
		def entrada = " (eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				      " AND eaa01id IN (SELECT eaa0103doc FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				      " WHERE eaa0103codBcCred IS NOT NULL AND eaa01gc = :gc " +
				      " GROUP BY eaa0103doc)) ";

		def saida = " (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal " +
				    " AND eaa01id IN (SELECT eaa0103doc FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " +
				    " WHERE eaa0103clasReceita > 0 AND eaa01gc = :gc " +
				    " GROUP BY eaa0103doc)) ";

		def sql = " SELECT * FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " WHERE (" + entrada + " OR " + saida + ")"
				  " AND eaa01gc = :gc ";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal), 
															  Parametro.criar("gc", gc));
	}

	List<Edb11> buscarOperacoesCreditoPorGC(Long gc) {
		def sql = " SELECT * FROM Edb11 " +
				  " WHERE edb11data BETWEEN :periodoIni AND :periodoFin " +
				  " AND edb11gc = :gc ";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("periodoIni", dtInicial), 
			                                                  Parametro.criar("periodoFin", dtFinal), 
															  Parametro.criar("gc", gc));
	}

	List<TableMap> buscarDocumentosF550PC(Long gc, String aliqPis, String aliqCof, String cpoVlr1, String cpoVlr2, String cpoVlr3, String cpoVlr4, String cpoVlr5, String cpoVlr6, String cpoVlr7) {
		def select = new StringBuilder("");
		select.append("jGet(eaa0103json." + aliqPis + ")::numeric AS aliqPis, ");
		select.append("jGet(eaa0103json." + aliqCof + ")::numeric AS aliqCof, ");
		select.append("jGet(eaa0103json." + cpoVlr1 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr2 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr3 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr4 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr5 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr6 + ")::numeric, ");
		select.append("jGet(eaa0103json." + cpoVlr7 + ")::numeric, ");

		def sql = " SELECT " + select + " aaj12codigo, aaj13codigo, aah01modelo, aaj15codigo, eaa01id " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				  " LEFT JOIN Aaj13 ON aaj13id = eaa0103cstPis " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa01esMov = 1 " +
				  " AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				  " AND eaa0103clasReceita > :clasRec " +
				  " AND eaa01gc = :gc " +
				  " ORDER BY eaa01id";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                 Parametro.criar("dtFinal", dtFinal), 
															 Parametro.criar("clasRec", 0), 
															 Parametro.criar("gc", gc));
	}

	List<Edb12> buscarRetencoesPorPeriodo(Long gc, Integer mes, Integer ano) {
		def sql = " SELECT * FROM Edb12 " +
			  	  " WHERE " + Fields.month("edb12data") + " = :mes AND " + Fields.year("edb12data") + " = :ano " +
				  " AND edb12gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("mes", mes), 
			                                                  Parametro.criar("ano", ano), 
															  Parametro.criar("gc", gc));
	}

	List<Edb13> buscarDeducoesPorPeriodo(Long gc, Integer mes, Integer ano) {
		def sql = " SELECT * FROM Edb13 " +
				  " WHERE edb13mes = :mes AND edb13ano = :ano " +
				  " AND edb13gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("mes", mes), 
			                                                  Parametro.criar("ano", ano), 
															  Parametro.criar("gc", gc));
	}

	List<Edb14> buscarCreditosPorPeriodo(Long gc, Integer mes, Integer ano) {
		def sql = "SELECT * FROM Edb14 " +
				  "WHERE " + Fields.month("edb14data") + " = :mes AND " + Fields.year("edb14data") + " = :ano " +
				  "AND edb14gc = :gc";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("mes", mes), 
			                                                  Parametro.criar("ano", ano), 
															  Parametro.criar("gc", gc));
	}

	BigDecimal buscarValorAjustesPorTipoPeriodo(int tipo, Long edb10id) {
		def sql = " SELECT SUM(edb100111valor) FROM Edb100111 " +
				  " INNER JOIN Edb10011 ON edb10011id = edb100111ativ " +
				  " INNER JOIN Edb1001 ON edb1001id = edb10011emp " +
				  " INNER JOIN Edb10 ON edb10id = edb1001apur " +
				  " WHERE edb10id = :edb10id " +
				  " AND edb100111tipo = :tipo " +
				  obterWherePadrao("Edb10");

		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("edb10id", edb10id), 
			                                           Parametro.criar("tipo", tipo));
	}

	List<Edb100111> buscarAjustesPorApuracao(Long edb10id) {
		def sql = " SELECT * FROM Edb100111 " +
				  " INNER JOIN Edb10011 ON edb10011id = edb100111ativ " +
				  " INNER JOIN Edb1001 ON edb1001id = edb10011emp " +
				  " INNER JOIN Edb10 ON edb10id = edb1001apur " +
				  " WHERE edb10id = :edb10id " +
				  obterWherePadrao("Edb10");

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb10id", edb10id));
	}

	List<TableMap> buscarDocumentos1900PC(Long gc, String cpoVlr1) {
		if(cpoVlr1 == null) return new ArrayList<TableMap>();
		
		def sql = " SELECT count(*) AS qtd, SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) AS " + cpoVlr1 + ", " +
				  " aaj12codigo, aaj13codigo, aah01modelo, aaj15codigo, abb01serie, eaa01sitDoc " +
				  " FROM Ea01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0103 ON eaa0103doc = eaa01id " +
				  " LEFT JOIN Aaj12 ON aaj12id = eaa0103cstPis " +
				  " LEFT JOIN Aaj13 ON aaj13id = eaa0103cstCofins " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa01esMov = 1 " +
				  " AND eaa01esData BETWEEN :dtInicial AND :dtFinal " +
				  " AND eaa0103clasReceita > 0 " +
				  " AND eaa01gc = :gc " +
				  " GROUP BY aaj12codigo, aaj13codigo, aah01modelo, aaj15codigo, abb01serie, eaa01sitDoc " +
				  " ORDER BY aaj12codigo, aaj13codigo";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                 Parametro.criar("dtFinal", dtFinal), 
															 Parametro.criar("gc", gc));
	}

	Abe0101 buscarEnderecoPrincipalEntidade(Long abe01id) {
		def sql = " SELECT * FROM Abe0101 WHERE abe0101principal = 1 AND abe0101ent = :abe01id ";

		Abe0101 abe0101 = getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("abe01id", abe01id));

		Aag01 aag01 = getSession().get(Aag01.class, abe0101.abe0101pais.aag01id);
		Aag0201 aag0201 = getSession().get(Aag0201.class, abe0101.abe0101municipio.aag0201id);
		Aag02 aag02 = getSession().get(Aag02.class, aag0201.aag0201uf.aag02id);

		aag0201.aag0201uf = aag02;
		abe0101.abe0101municipio = aag0201;
		abe0101.abe0101pais = aag01;

		return abe0101;
	}

	Map<String, TableMap> compor0205(Abm01 abm01, LocalDate dataFinal) {
		def map0205 = new HashMap<String, TableMap>();

		List<Abm0103> abm0103s = buscarAlteracoesParaEFDPorItem(abm01.abm01id);
		for(Abm0103 abm0103 : abm0103s) {
			def dtAnterior = buscarDataAnteriorDaAlteracaoPorItem(abm01.abm01id, abm0103.abm0103data);
			def dtInicial = LocalDate.of(2000, 1, 1);
			if(dtAnterior != null) dtInicial = dtAnterior.withDayOfMonth(1);
			def dtFinal = abm0103.abm0103data;
			if(dtFinal.compareTo(dataFinal) == 0) dtFinal.minusDays(1);

			def key = dtInicial.format(ddMMyyyy);

			def tm = new TableMap();
			tm.put("descr", abm0103.abm0103campo.equals("abm01descr") ? abm0103.abm0103contAnt : map0205.get(key) == null ? null : map0205.get(key).getString("descr"));
			tm.put("codigo", abm0103.abm0103campo.equals("abm01codigo") ? abm0103.abm0103contAnt : map0205.get(key) == null ? null : map0205.get(key).getString("codigo"));
			tm.put("dtInicial", dtInicial);
			tm.put("dtFinal", dtFinal);

			map0205.put(key, tm);

			atualizarDataEnvioAEFD(abm0103, mes, ano);
		}

		return map0205;
	}

	List<Abm0103> buscarAlteracoesParaEFDPorItem(Long abm01id) {
		String mesData = Fields.month("abm0103data").toString();
		String anoData = Fields.year("abm0103data").toString();
		
		String mesEFD = Fields.month("abm0103dtEfdContrib").toString();
		String anoEFD = Fields.year("abm0103dtEfdContrib").toString();
		
		String sql = " SELECT * FROM Abm0103 " +
			         " WHERE abm0103item = :abm01id " +
				     " AND " + Fields.numMeses(mesData, anoData) + " <= :numMeses " +
				     " AND (abm0103dtEfdContrib IS NULL OR " + Fields.numMeses(mesEFD, anoEFD) + " = :numMeses) " +
				     " ORDER BY abm0103data";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("abm01id", abm01id), 
			                                                  Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)));
	}

	LocalDate buscarDataAnteriorDaAlteracaoPorItem(Long abm01id, LocalDate data) {
		def sql = " SELECT abm0103data FROM Abm0103 " +
				  " WHERE abm0103item = :abm01id " +
				  " AND abm0103data < :data " +
				  " ORDER BY abm0103id DESC";

		return getAcessoAoBanco().obterDate(sql, Parametro.criar("abm01id", abm01id), 
			                                     Parametro.criar("data", data));
	}

	public void atualizarDataEnvioAEFD(Abm0103 abm0103, int mes, int ano) {
		def data = LocalDate.of(ano, mes, 1);
		abm0103.setAbm0103dtEfdIcms(data);
		getSession().persist(abm0103);
	}
	
	private void inicializarContadores() {
		qtLinBloco0 = 0;
		qtLin0111 = 0;
		qtLin0120 = 0;
		qtLin0140 = 0;
		qtLin0145 = 0;
		qtLin0150 = 0;
		qtLin0190 = 0;
		qtLin0200 = 0;
		qtLin0205 = 0;
		qtLin0206 = 0;
		qtLin0400 = 0;
		qtLin0450 = 0;
		qtLin0460 = 0;
		qtLin0500 = 0;
		qtLin0600 = 0;
		qtLinBlocoA = 0;
		qtLinA010 = 0;
		qtLinA100 = 0;
		qtLinA110 = 0;
		qtLinA111 = 0;
		qtLinA120 = 0;
		qtLinA170 = 0;
		qtLinBlocoC = 0;
		qtLinC010 = 0;
		qtLinC100 = 0;
		qtLinC110 = 0;
		qtLinC111 = 0;
		qtLinC120 = 0;
		qtLinC170 = 0;
		qtLinC175 = 0;
		qtLinC395 = 0;
		qtLinC396 = 0;
		qtLinC500 = 0;
		qtLinC501 = 0;
		qtLinC505 = 0;
		qtLinC509 = 0;
		qtLinC860 = 0;
		qtLinC870 = 0;
		qtLinC890 = 0;
		qtLinBlocoD = 0;
		qtLinD010 = 0;
		qtLinD100 = 0;
		qtLinD101 = 0;
		qtLinD105 = 0;
		qtLinD111 = 0;
		qtLinD500 = 0;
		qtLinD501 = 0;
		qtLinD505 = 0;
		qtLinD509 = 0;
		qtLinBlocoF = 0;
		qtLinF010 = 0;
		qtLinF100 = 0;
		qtLinF111 = 0;
		qtLinF120 = 0;
		qtLinF130 = 0;
		qtLinF150 = 0;
		qtLinF550 = 0;
		qtLinF559 = 0;
		qtLinF600 = 0;
		qtLinF700 = 0;
		qtLinF800 = 0;
		qtLinBlocoM = 0;
		qtLinBlocoP = 0;
		qtLinP010 = 0;
		qtLinP100 = 0;
		qtLinP200 = 0;
		qtLinP210 = 0;
		qtLinBloco1 = 0;
		qtLin1010 = 0;
		qtLin1020 = 0;
		qtLin1900 = 0;
		qtLinBloco9 = 0;
		qtLin9900 = 0;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDYifQ==