package multitec.formulas.sgt.efd;

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.fields.Fields
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Scale
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.TextFile
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aac13
import sam.model.entities.aa.Aag01
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aah01
import sam.model.entities.aa.Aah20
import sam.model.entities.aa.Aaj03
import sam.model.entities.aa.Aaj04
import sam.model.entities.aa.Aaj05
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj15
import sam.model.entities.aa.Aaj16
import sam.model.entities.aa.Aaj17
import sam.model.entities.aa.Aaj18
import sam.model.entities.aa.Aaj19
import sam.model.entities.aa.Aaj29
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb10
import sam.model.entities.ab.Abb11
import sam.model.entities.ab.Abb20
import sam.model.entities.ab.Abb40
import sam.model.entities.ab.Abc10
import sam.model.entities.ab.Abd10
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe0102
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm0103
import sam.model.entities.ab.Abm12
import sam.model.entities.ab.Abm13
import sam.model.entities.ab.Abm1301
import sam.model.entities.ab.Abm40
import sam.model.entities.bc.Bcb10
import sam.model.entities.bc.Bcb11
import sam.model.entities.bc.Bcc01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa01031
import sam.model.entities.ea.Eaa01034
import sam.model.entities.ea.Eaa0105
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ec.Eca01
import sam.model.entities.ec.Eca0101
import sam.model.entities.ec.Ecb01
import sam.model.entities.ec.Ecb0101
import sam.model.entities.ec.Ecc01
import sam.model.entities.ec.Ecc0101
import sam.model.entities.ec.Ecc0102
import sam.model.entities.ed.Edb01
import sam.model.entities.ed.Edb0101
import sam.model.entities.ed.Edb01011
import sam.model.entities.ed.Edb0102
import sam.model.entities.ed.Edb0103
import sam.model.entities.ed.Edb0105
import sam.model.entities.ed.Edd0201
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro

public class Leiaute16_2022_ICMS_IPI extends FormulaBase {
	private LocalDate dtInicial;
	private LocalDate dtFinal;
	private Integer arqSubstituto;
	private LocalDate dtInventario;
	
	private static final String alinEFD = "0050";
	private static final String alinApurIcms = "0030";
	private static final String alinApurIcmsSt = "0033";
	private static final String alinApurIpi = "0032";
	private static final String alinApurDifal = "0031";
	
	private static final Integer versaoLeiaute = 16;
	
	private TextFile txt1;
	private TextFile txt2;
	private static final DateTimeFormatter ddMMyyyy = DateTimeFormatter.ofPattern("ddMMyyyy");
	
	private int qtLinBloco0, qtLin0002, qtLin0015, qtLin0150, qtLin0175, qtLin0190, qtLin0200, qtLin0205, qtLin0206, qtLin0210, qtLin0220, qtLin0300, qtLin0305, qtLin0400, qtLin0450, qtLin0460, qtLin0500, qtLin0600, 
				qtLinBlocoB, 
				qtLinBlocoC, qtLinC100, qtLinC101, qtLinC105, qtLinC110, qtLinC111, qtLinC112, qtLinC113, qtLinC114, qtLinC115, qtLinC120, qtLinC130, qtLinC140, qtLinC141, qtLinC160, 
				qtLinC170, qtLinC171, qtLinC172, qtLinC173, qtLinC174, qtLinC175, qtLinC177, qtLinC178, qtLinC179, qtLinC190, qtLinC191, qtLinC195, qtLinC197, 
				qtLinC300, qtLinC310, qtLinC320, qtLinC321, qtLinC350, qtLinC370, qtLinC390, qtLinC500, qtLinC590, qtLinC800, qtLinC850, qtLinC860, qtLinC890, 
				qtLinBlocoD, qtLinD100, qtLinD101, qtLinD190, qtLinD195, qtLinD197, qtLinD500, qtLinD590, 
				qtLinBlocoE, qtLinE100, qtLinE110, qtLinE111, qtLinE112, qtLinE113, qtLinE115, qtLinE116, qtLinE500, qtLinE510, qtLinE520, qtLinE530, qtLinE531, qtLinE200,
				qtLinE210, qtLinE220, qtLinE230, qtLinE240, qtLinE250, qtLinE300, qtLinE310, qtLinE311, qtLinE312, qtLinE313, qtLinE316,
				qtLinBlocoG, qtLinG110, qtLinG125, qtLinG126, qtLinG130, qtLinG140, 
				qtLinBlocoH, qtLinH005, qtLinH010, qtLinH020, 
				qtLinBlocoK, qtLinK100, qtLinK200, qtLinK210, qtLinK215, qtLinK220, qtLinK230, qtLinK235, qtLinK250, qtLinK255, qtLinK280, 
				qtLinBloco1, qtLin1010, qtLin1100, qtLin1105, qtLin1110, qtLin1400,	qtLin1900, qtLin1910, qtLin1920, qtLin1921, qtLin1922, qtLin1923, qtLin1925, qtLin1926, qtLin1960;

	private Aac10 aac10;
	private Aac13 aac13;
	private String perfil;
	private Integer mes;
	private Integer ano;
	
	private List<TableMap> entidades;
	private List<TableMap> abe01s;
	private Set<Long> set0190;
	private Set<Long> set0200;
	private Map<Long, Map<Long, BigDecimal>> map0220;
	private Set<String> abc10s;
	private Map<Long, List<TableMap>> map0210s;
	private Set<Long> set0300;
	private TableMap map0400;
	private TableMap map0450;
	private TableMap map0460;
	private Set<Long> eaa01sE510;
	
	private List<String> modelosC100 = Utils.list("01", "1B", "04", "55");
	private List<String> modelosC300 = Utils.list("02");
	private List<String> modelosC350 = Utils.list("02");
	private List<String> modelosC500 = Utils.list("06", "66", "28", "29");
	private List<String> modelosC800 = Utils.list("59");
	private List<String> modelosD100 = Utils.list("07", "08", "09", "10", "11", "26", "27", "57", "67");
	private List<String> modelosD500 = Utils.list("21", "22");
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGT_EFD;
	}

	@Override
	public void executar() {
		dtInicial = get("dtInicial");
		dtFinal = get("dtFinal");
		arqSubstituto = get("arqSubstituto");
		dtInventario = get("dtInventario");
		if(dtInventario != null) {
			dtInventario = dtInventario.withDayOfMonth(dtInventario.lengthOfMonth());
		}
		
		mes = dtInicial.getMonthValue();
		ano = dtInicial.getYear();
		
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().getAac10id());
		if(aac10.aac10municipio.aag0201json == null) aac10.aac10municipio.aag0201json = new TableMap();
		if(aac10.aac10municipio == null) throw new ValidacaoException("Necessário informar o município no cadastro da empresa.");
		if(aac10.aac10endereco == null) throw new ValidacaoException("Necessário informar o endereço no cadastro da empresa.");
		
		aac13 = aac10.aac13fiscal;
		if(aac13 == null) throw new ValidacaoException("Necessário informar as informações fiscais no cadastro da empresa.");
		if(aac13.aac13tipoAtiv == null) throw new ValidacaoException("Necessário informar o tipo de atividade nas informações fiscais do cadastro da empresa.");
		
		perfil = aac13.aac13perfil;
		if(perfil == null) throw new ValidacaoException("Necessário informar o perfil nas informações fiscais do cadastro da empresa.");
		
		selecionarAlinhamento(alinEFD);
		selecionarAlinhamento(alinApurIcms);
		selecionarAlinhamento(alinApurIcmsSt);
		selecionarAlinhamento(alinApurIpi);
		selecionarAlinhamento(alinApurDifal);
		
		txt1 = new TextFile("|");
		txt2 = new TextFile("|");
		
		inicializarContadores();
		
		gerarAberturaBloco0();
		
		gerarBlocoB();
		
		gerarBlocoC();
		
		gerarBlocoD();
		
		gerarBlocoE();
		
		gerarBlocoG();
		
		gerarBlocoH();
		
		gerarBlocoK();
		
		gerarBloco1();
		
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
		enviarStatusProcesso("Gerando Inicio Bloco 0...");
		
		/**
		 * REGISTRO 0000: Abertura do arquivo digital e identificação da entidade 
		 */
		txt1.print("0000");
		txt1.print(versaoLeiaute, 3);
		txt1.print(0);
		txt1.print(dtInicial.format(ddMMyyyy));
		txt1.print(dtFinal.format(ddMMyyyy));
		txt1.print(aac10.aac10rs);
		txt1.print(aac10.aac10ti.equals(0) ? StringUtils.extractNumbers(aac10.aac10ni) : null);
		txt1.print(aac10.aac10ti.equals(1) ? StringUtils.extractNumbers(aac10.aac10ni) : null);
		txt1.print(aac10.aac10municipio == null ? null : aac10.aac10municipio.aag0201uf == null ? null : aac10.aac10municipio.aag0201uf.aag02uf);
		
		def ie = aac10.aac10municipio != null && aac10.aac10municipio.aag0201uf != null ? getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aac10.aac10municipio.aag0201uf.aag02id) : null;
		txt1.print(inscrEstadual(ie));
		
		txt1.print(aac10.aac10municipio == null ? 0 : aac10.aac10municipio.aag0201ibge, 7, '0', true);
		txt1.print(StringUtils.extractNumbers(aac10.aac10im));
		txt1.print(StringUtils.extractNumbers(aac10.aac10suframa));
		txt1.print(aac13.aac13perfil);
		txt1.print(aac13.aac13tipoAtiv.equals(0) ? 0 : 1);
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
		 * REGISTRO 0002: Classificação do Estabelecimento Industrial ou Equiparado a Industrial 
		 */
		if(aac13.aac13tipoAtiv.equals(0)) {
			txt1.print("0002");
			txt1.print(aac13.aac13clasIpi, 2);
			txt1.newLine();
			qtLin0002++;
			qtLinBloco0++;
		}
		
		/** 
		 * REGISTRO 0005: Dados Complementares da Entidade
		 */
		txt1.print("0005");
		txt1.print(aac10.aac10fantasia);
		txt1.print(aac10.aac10cep);
		txt1.print(aac10.aac10endereco);
		txt1.print(aac10.aac10numero);
		txt1.print(aac10.aac10complem);
		txt1.print(aac10.aac10bairro);
		txt1.print(aac10.aac10fone == null ? null : aac10.aac10dddFone == null ? aac10.aac10fone : aac10.aac10dddFone + aac10.aac10fone);
		txt1.print(null);
		txt1.print(aac10.aac10email);
		txt1.newLine();
		qtLinBloco0++;

		/** 
		 * REGISTRO 0015: Dados do Contribuinte Substituto
		 */
		def aac1002s = getSession().createCriteria(Aac1002.class).addJoin(Joins.fetch("aac1002uf")).addWhere(Criterions.eq("aac1002empresa", aac10.aac10id)).getList();
		if(aac1002s != null && aac1002s.size() > 0) {
			for(Aac1002 aac1002 : aac1002s) {
				if(aac10.aac10municipio.aag0201uf == aac1002.aac1002uf) continue;
				
				txt1.print("0015");
				txt1.print(aac1002 == null ? null : aac1002.aac1002uf.aag02uf);
				txt1.print(inscrEstadual(aac1002.aac1002ie));
				txt1.newLine();
				
				qtLinBloco0++;
				qtLin0015++;
			}
		}

		/** 
		 * REGISTRO 0100: Dados do Contabilista
		 */
		txt1.print("0100");
		txt1.print(aac10.aac10cNome);
		txt1.print(StringUtils.extractNumbers(aac10.aac10cCpf), 11, '0', true);
		String crc = aac10.aac10cCrc == null ? "" : aac10.aac10cCrc;
		txt1.print(crc.length() > 15 ? crc.substring(0, 15) : crc);
		txt1.print(aac10.aac10cCnpj == null ? null : StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10cCnpj), 14, '0', true));
		txt1.print(aac10.aac10cCep);
		txt1.print(aac10.aac10cEndereco);
		txt1.print(aac10.aac10cNumero);
		txt1.print(aac10.aac10cComplem);
		txt1.print(aac10.aac10cBairro);
		txt1.print(aac10.aac10cFone == null ? null : aac10.aac10cDddFone == null ? aac10.aac10cFone : aac10.aac10cDddFone + aac10.aac10cFone);
		txt1.print(null);
		txt1.print(aac10.aac10cEmail);
		txt1.print(aac10.aac10cMunicipio == null ? null : aac10.aac10cMunicipio.aag0201ibge, 7, '0', true);
		txt1.newLine();
		qtLinBloco0++;

		/**
		 * REGISTRO 0150: Tabela de Cadastro do Participante
		 * REGISTRO 0175: Alteração da Tabela de Cadastro de Participante
		 */
		entidades = new ArrayList<TableMap>();
		abe01s = new ArrayList<TableMap>(); 
		
		/**
		 * REGISTRO 0190: Identificação das unidades de medida
		 */
		set0190 = new HashSet<Long>();

		/**
		 * REGISTRO 0200: Tabela de Identificação do item (Produto e Serviços)
		 * REGISTRO 0205: Alteração do Item -> Não geramos este arquivo
		 * REGISTRO 0206: Código de Produto conforme Tabela Publicada pela ANP (Combustíveis)
		 * REGISTRO 0210: Consumo Específico Padronizado
		 * REGISTRO 0220: Fatores de Conversão de Unidades
		 */
		set0200 = new HashSet<Long>();
		map0210s = new HashMap<Long, List<TableMap>>();
		map0220 = new HashMap<Long, Map<Long, BigDecimal>>();

		/**
		 * REGISTRO 0300: Cadastro de Bens do CIAP
		 * REGISTRO 0305: Utilização do Bem
		 */
		set0300 = new HashSet<Long>();
		
		/**
		 * REGISTRO 0400: Tabela de Natureza da Operação/Prestação
		 */
		map0400 = new TableMap();

		/**
		 * REGISTRO 0450: Tabela de Informação Complementar do Documento Fiscal
		 */
		map0450 = new TableMap();

		/**
		 * REGISTRO 0500: Plano de Contas Contábeis
		 */
		abc10s = new HashSet<String>();
		
		/**
		 * REGISTRO 0600: Centros de Custo
		 * montado a partir do 0300, no final do arquivo
		 */
		
		/**
		 * REGISTRO 0460: Tabela de Observações do Lançamento Fiscal
		 */
		map0460 = new TableMap();

		//Guarda o id dos documentos que serão utilizados no C170 e das NFe Própria para serem utilizadas no registro E510
		eaa01sE510 = new HashSet<Long>();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * *  BLOCO B: ESCRITURAÇÃO E APURAÇÃO DO ISS  * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoB() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco B...");
		
		/** 
		 * REGISTRO B001 - Abertura Bloco B 
		 */
		txt2.print("B001");
		txt2.print("1");
		txt2.newLine();
		qtLinBlocoB++;
		
		/** 
		 * REGISTRO B990 - Encerramento do Bloco C 
		 */
		qtLinBlocoB++;
		
		txt2.print("B990");
		txt2.print(qtLinBlocoB);
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
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco C...");
		
		/** 
		 * REGISTRO C001 - Abertura Bloco C
		 */
		txt2.print("C001");
		txt2.print(contemDadosBlocoC() ? 0 : 1);
		txt2.newLine();
		qtLinBlocoC++;
		
		gerarC100();
		
		gerarC300();
		
		gerarC500();
		
		gerarC800();

		/**
		 * REGISTRO C990 - Encerramento do Bloco C
		 */
		qtLinBlocoC++;
		
		txt2.print("C990");
		txt2.print(qtLinBlocoC);
		txt2.newLine();
	}
	
	
	def gerarC100() {
		/**
		 * REGISTRO C100: Nota Fiscal (Código 01), Nota Fiscal Avulsa (Código 1B), Nota Fiscal de Produtor (Código 04) e NFE (Código 55)
		 */
		def pagina = 0;
		List<Eaa01> eaa01s = buscarDocumentosPorModelo(modelosC100, pagina);
		if(eaa01s != null && eaa01s.size() > 0) {
			validacoes(eaa01s);
			
			while(eaa01s.size() > 0) {
				verificarProcessoCancelado();
				
				for(Eaa01 eaa01 : eaa01s) {
					if(!gerarRegByPerfil(perfil, "C100", eaa01.eaa01esMov)) continue;
					
					Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
					
					verificarProcessoCancelado();
					enviarStatusProcesso("Compondo registro C100 - Documento: " + abb01.abb01num);

					Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
					Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
					Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
					TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
					
					def modelo = aah01.aah01modelo;
					def isNFePropria = modelo.equals("55") && eaa01.eaa01emissao == Eaa01.SIM;
					def serie = formatarSerie(abb01.abb01serie, modelo);
					
					//Se a situação for 02, 03, 04, 05, 06 ou 07 os registros filhos não precisam ser gerados
					Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
					boolean isSit06ou07 = "06".equals(aaj03.aaj03efd) || "07".equals(aaj03.aaj03efd);
					boolean geraFilhos = !"02".equals(aaj03.aaj03efd) && !"03".equals(aaj03.aaj03efd) && !"04".equals(aaj03.aaj03efd) && !"05".equals(aaj03.aaj03efd);
					
					if(!geraFilhos) {
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
						txt2.print("C100");
						txt2.print(eaa01.eaa01esMov);
						txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
						txt2.print(gerarCodigoEntidade(abe01, eaa0102.eaa0102ie));
						txt2.print(modelo);
						txt2.print(aaj03.aaj03efd);
						txt2.print(serie);
						txt2.print(abb01.abb01num);
						txt2.print(eaa01.eaa01nfeChave);
						txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
						txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.compareTo(dtFinal) > 0 ? null : eaa01.eaa01esData.format(ddMMyyyy));
						txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
						txt2.print(verificarTipoPgto(eaa01, abb01));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_DESC")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_ABAT_NT")), 2));
						txt2.print(formatarValor(eaa01.eaa01totItens, 2));
						txt2.print(eaa0102.eaa0102frete == null ? 9 : eaa0102.eaa0102frete);
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_FRT")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_SEG")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_OUT_DA")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_BC_ICMS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_ICMS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_BC_ICMS_ST")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_ICMS_ST")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_IPI")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_PIS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_COFINS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_PIS_ST")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C100", "VL_COFINS_ST")), 2));
						txt2.newLine();
	
						eaa01sE510.add(eaa01.getEaa01id());
					}
					qtLinBlocoC++;
					qtLinC100++;
					
					if(geraFilhos) {
						/**
						 * REGISTRO C101: Informação Complementar dos Documentos Fiscais quando das Operações Interestaduais destinadas a consumidor final
						 * não contribuinte EC 87/15
						 */
						def ufEntrega = buscarUfEnderecoEntrega(eaa01.eaa01id);
						if(gerarRegByPerfil(perfil, "C101", eaa01.eaa01esMov) && modelo.equals("55") && !ufEntrega.equals(aac10.aac10municipio.aag0201uf.aag02uf) && eaa0102.eaa0102consFinal == 1 && eaa0102.eaa0102contribIcms == 0) {
							if(!jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_FCP_UF_DEST")).equals(0) || !jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_ICMS_UF_DEST")).equals(0) || !jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_ICMS_UF_REM")).equals(0)){
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C101 - Documento: " + abb01.abb01num);
								
								txt2.print("C101");
								txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_FCP_UF_DEST")), 2));
								txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_ICMS_UF_DEST")), 2));
								txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C101", "VL_ICMS_UF_REM")), 2));
								txt2.newLine();
								
								qtLinBlocoC++;
								qtLinC101++;
							}
						}
						
						/**
						 * REGISTRO C110: Informação complementar da Nota Fiscal (Código 01, 1B, 04 e 55)
						 */
						if(gerarRegByPerfil(perfil, "C110", eaa01.eaa01esMov)) {
							def mapRegistroC110 = comporRegistrosC110eFilhos(isNFePropria, eaa01, eaa0102, aac10, map0450, perfil);
							if(mapRegistroC110 != null && mapRegistroC110.size() > 0) {
								for(int j = 0; j < mapRegistroC110.size(); j++) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C110 - Documento: " + abb01.abb01num);
	
									txt2.print(mapRegistroC110.get(j).getString("reg"));
									txt2.print(mapRegistroC110.get(j).getString("cod_inf"));
									txt2.print(mapRegistroC110.get(j).getString("txt_compl"));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC110++;
	
									/**
									 * REGISTRO C111: Processo Referenciado
									 */
									List<TableMap> mapRegistroC111 = mapRegistroC110.get(j).get("mapRegistroC111");
									if(mapRegistroC111 != null && mapRegistroC111.size() > 0) {
										for(int k = 0; k < mapRegistroC111.size(); k++) {
											verificarProcessoCancelado();
											enviarStatusProcesso("Gerando registro C111 - Documento: " + abb01.abb01num);
											
											txt2.print(mapRegistroC111.get(k).getString("reg"));
											txt2.print(mapRegistroC111.get(k).getString("num_proc"));
											txt2.print(mapRegistroC111.get(k).getInteger("ind_proc"));
											txt2.newLine();
											qtLinBlocoC++;
											qtLinC111++;
										}
									}
	
									/**
									 * REGISTRO C112: Documento de arrecadação referenciado
									 */
									List<TableMap> mapRegistroC112 = mapRegistroC110.get(j).get("mapRegistroC112");
									if(mapRegistroC112 != null && mapRegistroC112.size() > 0) {
										for(int k = 0; k < mapRegistroC112.size(); k++) {
											verificarProcessoCancelado();
											enviarStatusProcesso("Gerando registro C112 - Documento: " + abb01.abb01num);
											
											txt2.print(mapRegistroC112.get(k).getString("reg"));
											txt2.print(mapRegistroC112.get(k).getString("cod_da"));
											txt2.print(mapRegistroC112.get(k).getString("uf"));
											txt2.print(mapRegistroC112.get(k).getString("num_da"));
											txt2.print(mapRegistroC112.get(k).getString("cod_aut"));
											txt2.print(mapRegistroC112.get(k).getString("vl_da"));
											txt2.print(mapRegistroC112.get(k).getString("dt_vcto"));
											txt2.print(mapRegistroC112.get(k).getString("dt_pgto"));
											txt2.newLine();
											qtLinBlocoC++;
											qtLinC112++;
										}
									}
	
									/**
									 * REGISTRO C113: Documento fiscal referenciado
									 */
									List<TableMap> mapRegistroC113 = mapRegistroC110.get(j).get("mapRegistroC113");
									if(mapRegistroC113 != null && mapRegistroC113.size() > 0) {
										for(int k = 0; k < mapRegistroC113.size(); k++) {
											verificarProcessoCancelado();
											enviarStatusProcesso("Gerando registro C113 - Documento: " + abb01.abb01num);
											
											txt2.print(mapRegistroC113.get(k).getString("reg"));
											txt2.print(mapRegistroC113.get(k).getInteger("ind_oper"));
											txt2.print(mapRegistroC113.get(k).getInteger("ind_emit"));
											txt2.print(mapRegistroC113.get(k).getString("cod_part"));
											txt2.print(mapRegistroC113.get(k).getString("cod_mod"));
											txt2.print(mapRegistroC113.get(k).getString("ser"));
											txt2.print(mapRegistroC113.get(k).getString("sub"));
											txt2.print(mapRegistroC113.get(k).getInteger("num_doc"));
											txt2.print(mapRegistroC113.get(k).getString("dt_doc"));
											txt2.print(mapRegistroC113.get(k).getString("chv_doce"));
											txt2.newLine();
											qtLinBlocoC++;
											qtLinC113++;
										}
									}
	
									/**
									 * REGISTRO C114: Cupom fiscal referenciado
									 */
									List<TableMap> mapRegistroC114 = mapRegistroC110.get(j).get("mapRegistroC114");
									if(mapRegistroC114 != null && mapRegistroC114.size() > 0) {
										for(int k = 0; k < mapRegistroC114.size(); k++) {
											verificarProcessoCancelado();
											enviarStatusProcesso("Gerando registro C114 - Documento: " + abb01.abb01num);
											
											txt2.print(mapRegistroC114.get(k).getString("reg"));
											txt2.print(mapRegistroC114.get(k).getString("cod_mod"));
											txt2.print(mapRegistroC114.get(k).getString("ecf_fab"));
											txt2.print(mapRegistroC114.get(k).getString("ecf_cx"));
											txt2.print(mapRegistroC114.get(k).getInteger("num_doc"));
											txt2.print(mapRegistroC114.get(k).getString("dt_doc"));
											txt2.newLine();
											qtLinBlocoC++;
											qtLinC114++;
										}
									}
	
									/**
									 * REGISTRO C115: Local da coleta e/ou entrega (01, 1B e 04)
									 */
									List<TableMap> mapRegistroC115 = mapRegistroC110.get(j).get("mapRegistroC115");
									if(mapRegistroC115 != null && mapRegistroC115.size() > 0) {
										for(int k = 0; k < mapRegistroC115.size(); k++) {
											verificarProcessoCancelado();
											enviarStatusProcesso("Gerando registro C115 - Documento: " + abb01.abb01num);
											
											txt2.print(mapRegistroC115.get(k).getString("reg"));
											txt2.print(mapRegistroC115.get(k).getInteger("ind_carga"));
											txt2.print(mapRegistroC115.get(k).getString("cnpj_col"));
											txt2.print(mapRegistroC115.get(k).getString("ie_col"));
											txt2.print(mapRegistroC115.get(k).getString("cpf_col"));
											txt2.print(mapRegistroC115.get(k).getString("cod_mun_col"));
											txt2.print(mapRegistroC115.get(k).getString("cnpj_entg"));
											txt2.print(mapRegistroC115.get(k).getString("ie_entg"));
											txt2.print(mapRegistroC115.get(k).getString("cpf_entg"));
											txt2.print(mapRegistroC115.get(k).getString("cod_mun_entg"));
											txt2.newLine();
											qtLinBlocoC++;
											qtLinC115++;
										}
									}
								}
							}
							mapRegistroC110 = null;
						}
						
						/**
						 * REGISTRO C120: Operações de Importação (Código 01)
						 */
						if(gerarRegByPerfil(perfil, "C120", eaa01.eaa01esMov) && eaa01.eaa01esMov == 0 && (modelo.equals("01") || modelo.equals("55"))) {
							def eaa01034s = buscarDeclaracoesDeImportacao(eaa01.eaa01id);
							for(Eaa01034 eaa01034 : eaa01034s) {
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C120 - Documento: " + abb01.abb01num);
								
								txt2.print("C120");
								txt2.print(eaa01034.eaa01034decSimp);
								txt2.print(eaa01034.eaa01034num);
								txt2.print(formatarValor(eaa01034.eaa01034pis, 2));
								txt2.print(formatarValor(eaa01034.eaa01034cofins, 2));
								txt2.print(eaa01034.eaa01034drawback);
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC120++;
							}
						}
						
						/**
						 * REGISTRO C130: ISSQN, IRRF e Previdência Social
						 */
						if(gerarRegByPerfil(perfil, "C130", eaa01.eaa01esMov) && eaa01.eaa01esMov == 1 && 
							(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_ISSQN")) > 0 || jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_IRRF")) > 0 || jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_PREV")) > 0)) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C130 - Documento: " + abb01.abb01num);
							
							txt2.print("C130");
							txt2.print(formatarValor(eaa01.eaa01totItens, 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_ISSQN")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_ISSQN")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_IRRF")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_IRRF")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_BC_PREV")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C130", "VL_PREV")), 2));
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC130++;
						}
	
						/**
						 * REGISTRO C140: Fatura (Código 01)
						 */
						List<Eaa0113> eaa0113s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0113 WHERE eaa0113clasParc = 0 AND eaa0113doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
						if(gerarRegByPerfil(perfil, "C140", eaa01.eaa01esMov) && modelo.equals("01") && eaa0113s != null && eaa0113s.size() > 0) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C140 - Documento: " + abb01.abb01num);
							
							def valorTitulo = new BigDecimal(0);
							for(Eaa0113 eaa0113 : eaa0113s) valorTitulo = valorTitulo.add(eaa0113.eaa0113valor);
							Aah01 aah01Fat = getSession().get(Aah01.class, eaa0113s.get(0).eaa0113tipo.aah01id);
							
							txt2.print("C140");
							txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
							txt2.print("99");
							txt2.print(aah01Fat.aah01nome);
							txt2.print(abb01.abb01num);
							txt2.print(eaa0113s == null ? 0 : eaa0113s.size());
							txt2.print(formatarValor(valorTitulo, 2));
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC140++;
	
							/**
							 * REGISTRO C141: Vencimento da Fatura (Código 01)
							 */
							if(gerarRegByPerfil(perfil, "C141", eaa01.eaa01esMov)) {
								for(Eaa0113 eaa0113 : eaa0113s) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C141 - Documento: " + abb01.abb01num);
									
									txt2.print("C141");
									txt2.print(qtLinC141);
									txt2.print(eaa0113.eaa0113dtVctoN == null ? null : eaa0113.eaa0113dtVctoN.format(ddMMyyyy));
									txt2.print(formatarValor(eaa0113.eaa0113valor, 2));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC141++;
								}
							}
						}
	
						/**
						 * REGISTRO C160: Volumes Transportados (Código 01 e 04) - Exceto Combústiveis
						 */
						if(gerarRegByPerfil(perfil, "C160", eaa01.eaa01esMov) && eaa01.eaa01esMov == 1 && (modelo.equals("01") || modelo.equals("04")) && !jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C160", "QTD_VOL")).equals(0)) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C160 - Documento: " + abb01.abb01num);
							
							txt2.print("C160");
	
							Abe01 despacho = getSession().get(Abe01.class, eaa0102.eaa0102despacho.abe01id);
							if(despacho != null && !despacho.abe01ni.equals(aac10.aac10ni)) {
								txt2.print(gerarCodigoEntidade(despacho, despacho.abe01ie));
							}else {
								txt2.print(null);
							}
	
							Aah20 aah20 = getSession().get(Aah20.class, eaa0102.eaa0102veiculo.aah20id);
							txt2.print(aah20 == null ? null : aah20.aah20placa);
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C160", "QTD_VOL")), 0));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C160", "PESO_BRT")), 2));
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C160", "PESO_LIQ")), 2));
							txt2.print(aah20 == null ? null : aah20.aah20ufPlaca);
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC160++;
						}
						
						/**
						 * REGISTRO C170: Itens do Documento (Código 01, 1B, 04 e 55)
						 */
						def eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id ORDER BY eaa0103seq", Parametro.criar("eaa01id", eaa01.eaa01id));
						if(!isSit06ou07 && gerarRegByPerfil(perfil, "C170", eaa01.eaa01esMov) && !isNFePropria && eaa0103s != null && eaa0103s.size() > 0) {
							for(Eaa0103 eaa0103 : eaa0103s) {
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C170 - Documento: " + abb01.abb01num);
								
								TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
								
								Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
								comporRegistro0200(abm01);
	
								txt2.print("C170");
								txt2.print(eaa0103.eaa0103seq);
								txt2.print(eaa0103.eaa0103codigo);
								txt2.print(eaa0103.eaa0103complem);
								txt2.print(formatarValor(eaa0103.eaa0103qtComl, 5));
	
								if(eaa0103.eaa0103umComl != null) {
									Aam06 aam06 = getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id);
									txt2.print(aam06.aam06codigo);
									set0190.add(eaa0103.eaa0103umComl.aam06id);
									comporRegistro0220(abm01, aam06, eaa01.eaa01esMov == 1);
								}else {
									txt2.print(null);
								}
	
								txt2.print(formatarValor(eaa0103.eaa0103total, 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_DESC")), 2));
								txt2.print(eaa01.eaa01iSCE == 0 ? 0 : 1);
								
								Aaj10 aaj10 = eaa0103.eaa0103cstIcms != null ? getSession().get(Aaj10.class, eaa0103.eaa0103cstIcms.aaj10id) : null;
								txt2.print(aaj10 == null ? null : aaj10.aaj10codigo);
								
								Aaj15 aaj15 = eaa0103.eaa0103cfop != null ? getSession().get(Aaj15.class, eaa0103.eaa0103cfop.aaj15id) : null;
								txt2.print(aaj15 == null ? null : aaj15.aaj15codigo);
	
								Abb10 abb10 = abb01.abb01operCod != null ? getSession().get(Abb10.class, abb01.abb01operCod.abb10id) : null;
								txt2.print(abb10 == null ? null : abb10.abb10codigo);
								if(abb10 != null) map0400.put(abb10.abb10codigo, abb10.abb10descr);
	
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_BC_ICMS")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_ICMS")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_ICMS")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_BC_ICMS_ST")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_ST")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_ICMS_ST")), 2));
								txt2.print("0");
								
								Aaj11 aaj11 = eaa0103.eaa0103cstIpi != null ? getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id) : null;
								txt2.print(aaj11 == null ? null : aaj11.aaj11codigo);
								txt2.print(null); //txt2.print(eaa0103.eaa0103codEnqIpi);
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_BC_IPI")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_IPI")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_IPI")), 2));
								
								Aaj12 aaj12 = eaa0103.eaa0103cstPis != null ? getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id) : null;
								txt2.print(aaj12 == null ? null : aaj12.aaj12codigo);
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_BC_PIS")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_PIS_P")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "QUANT_BC_PIS")), 3));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_PIS_R")), 4));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_PIS")), 2));
								
								Aaj13 aaj13 = eaa0103.eaa0103cstCofins != null ? getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id) : null;
								txt2.print(aaj13 == null ? null : aaj13.aaj13codigo);
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_BC_COFINS")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_COFINS_P")), 2));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "QUANT_BC_COFINS")), 3));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "ALIQ_COFINS_R")), 4));
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_COFINS")), 2));
								
								if(eaa0103.eaa0103cta != null) {
									Abc10 abc10 = getSession().get(Abc10.class, eaa0103.eaa0103cta.abc10id);
									
									txt2.print(abc10.abc10codigo);
									abc10s.add(abc10.abc10codigo);
								}else {
									txt2.print(null);
								}
																
								txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C170", "VL_ABAT_NT")), 2));
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC170++;
	
								/**
								 * REGISTRO C172: Operações com ISSQN (Código 01)
								 */
								if(gerarRegByPerfil(perfil, "C172", eaa01.eaa01esMov) && eaa01.eaa01esMov == 1 && modelo.equals("01") && !jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C172", "VL_BC_ISSQN")).equals(0)){
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C172 - Documento: " + abb01.abb01num);
									
									txt2.print("C172");
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C172", "VL_BC_ISSQN")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C172", "ALIQ_ISSQN")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C172", "VL_ISSQN")), 2));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC172++;
								}
	
								/**
								 * RegistroC179: Informações complementares ST (Código 01)
								 */
								if(gerarRegByPerfil(perfil, "C179", eaa01.eaa01esMov) && eaa01.eaa01esMov == 1 && modelo.equals("01") && !jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "BC_ST_ORIG_DEST")).equals(0)){
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C179 - Documento: " + abb01.abb01num);
									
									txt2.print("C179");
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "BC_ST_ORIG_DEST")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "ICMS_ST_REP")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "ICMS_ST_COMPL")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "BC_RET")), 2));
									txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C179", "ICMS_RET")), 2));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC179++;
								}
							}
						}
	
						
						/**
						 * REGISTRO C190: Registro Analítico do Documento
						 */
						if(gerarRegByPerfil(perfil, "C190", eaa01.eaa01esMov)) {
							List<TableMap> listEaa0103 = buscarResumoValoresC190(eaa01.eaa01id, getCampo(alinEFD, "C190", "ALIQ_ICMS"), getCampo(alinEFD, "C190", "VL_BC_ICMS"), getCampo(alinEFD, "C190", "VL_ICMS"), getCampo(alinEFD, "C190", "VL_BC_ICMS_ST"), getCampo(alinEFD, "C190", "VL_ICMS_ST"), getCampo(alinEFD, "C190", "VL_RED_BC"), getCampo(alinEFD, "C190", "VL_IPI"));
							if(listEaa0103 != null && listEaa0103.size() > 0) {
								for(TableMap tm : listEaa0103) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C190 - Documento: " + abb01.abb01num);
									
									txt2.print("C190");
									txt2.print(tm.getString("aaj10codigo"));
									txt2.print(tm.getString("aaj15codigo"));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "ALIQ_ICMS")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_BC_ICMS")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_ICMS")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_BC_ICMS_ST")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_ICMS_ST")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_RED_BC")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C190", "VL_IPI")), 2));
									txt2.print(null);
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC190++;
								}
							}
						}
	
						/**
						 * REGISTRO C195: Observações do Lançamento Fiscal (Código 01, 1B e 55)
						 */
						if(gerarRegByPerfil(perfil, "C195", eaa01.eaa01esMov) && (modelo.equals("01") || modelo.equals("1B") || modelo.equals("55") || modelo.equals("04"))) {
							def eaa01031s = buscarLancamentosFiscaisDocumento(eaa01.eaa01id);
							for(Eaa01031 eaa01031 : eaa01031s) {
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C195 - Documento: " + abb01.abb01num);
	
								def cod = "" + (map0460 == null ? 1 : map0460.size()+1);
								
								def obs = eaa01031.eaa01031obs == null ? "Documento com ajuste fiscal" : eaa01031.eaa01031obs;
								map0460.put(cod, obs);
									
								txt2.print("C195");
								txt2.print(cod);
								txt2.print(eaa01031.eaa01031obsComplem);
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC195++;
	
								/**
								 * REGISTRO C197: Outras obrigações tributárias, ajustes e informações de valores provenientes de documento fiscal
								 */
								if(gerarRegByPerfil(perfil, "C197", eaa01.eaa01esMov) && eaa01.eaa01esMov != null) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C197 - Documento: " + abb01.abb01num);
									
									txt2.print("C197");
									
									Aaj16 aaj16 = getSession().get(Aaj16.class, eaa01031.eaa01031codAjuste.aaj16id);
									txt2.print(aaj16 == null ? null : aaj16.aaj16codigo);
									txt2.print(eaa01031.eaa01031descr);
	
									Abm01 abm01 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abm01 INNER JOIN Eaa0103 ON eaa0103item = abm01id INNER JOIN Eaa01031 ON Eaa01031item = eaa0103item WHERE eaa01031id = :eaa01031id", Parametro.criar("eaa01031id", eaa01031.eaa01031id));
									if(abm01 != null) {
										txt2.print(abm01.abm01codigo);
										comporRegistro0200(abm01);
									}else {
										txt2.print(null);
									}
	
									txt2.print(formatarValor(eaa01031.eaa01031icmsBc, 2));
									txt2.print(formatarValor(eaa01031.eaa01031icmsTx, 2));
									txt2.print(formatarValor(eaa01031.eaa01031icms, 2));
									txt2.print(formatarValor(eaa01031.eaa01031icmsOutras, 2));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC197++;
								}
							}
						}
					}
				}
				
				pagina++;
				eaa01s = buscarDocumentosPorModelo(modelosC100, pagina);
			}
		}
	}
	
	def gerarC300() {
		/**
		 * REGISTRO C300: Resumo Diário das Notas Fiscais de Venda a Consumidor (Código 02)
		 */
		if(gerarRegByPerfil(perfil, "C300", 1)) {
			def mapDocumentosC300 = new HashMap<String, TableMap>();
			def pagina = 0;
			
			List<Eaa01> eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC300, pagina);
			while(eaa01s.size() > 0) {
				for(Eaa01 eaa01 : eaa01s) {
					Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
					Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
					
					verificarProcessoCancelado();
					enviarStatusProcesso("Compondo registro C300 - Documento: " + abb01.abb01num);
					
					TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
					
					def key = abb01.abb01data + "/" + abb01.abb01serie; 
					
					def tm = new TableMap();
					
					if(mapDocumentosC300 == null || mapDocumentosC300.get(key).getInteger("num_doc_ini") == null || mapDocumentosC300.get(key).getInteger("num_doc_ini") > abb01.abb01num) {
						tm.put("num_doc_ini", abb01.abb01num);
					}
					if(mapDocumentosC300 == null || mapDocumentosC300.get(key).getInteger("num_doc_fin") == null || mapDocumentosC300.get(key).getInteger("num_doc_fin") < abb01.abb01num) {
						tm.put("num_doc_fin", abb01.abb01num);
					}
					
					tm.put("cod_mod", aah01.aah01modelo);
					tm.put("ser", abb01.abb01serie);
					tm.put("dt_doc", abb01.abb01data);
					
					if(eaa01.eaa01cancData != null) {
						Set<Integer> setCanc = mapDocumentosC300.get(key).get("cancelados");
						if(setCanc == null) setCanc = new HashSet<Integer>();
						
						setCanc.add(abb01.abb01num);
						tm.put("cancelados", setCanc);
						
					}else {
						tm.put("vl_doc", mapDocumentosC300.get(key).getBigDecimal_Zero("vl_doc").add(eaa01.eaa01totDoc));
						tm.put("vl_pis", mapDocumentosC300.get(key).getBigDecimal_Zero("vl_pis").add(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C300", "VL_PIS"))));
						tm.put("vl_cofins", mapDocumentosC300.get(key).getBigDecimal_Zero("vl_cofins").add(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C300", "VL_COFINS"))));
					}
					
					mapDocumentosC300.put(key, tm);
				}
				pagina++;
				eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC300, pagina);
			}
			
			/**
			 * REGISTRO C300: Resumo Diário das Notas Fiscais de Venda a Consumidor (Código 02)
			 */
			if(mapDocumentosC300 != null && mapDocumentosC300.size() > 0) {
				for(String keyC300 : mapDocumentosC300.keySet()) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro C300");
					
					def numInicial = mapDocumentosC300.get(keyC300).getInteger("num_doc_ini");
					def numFinal = mapDocumentosC300.get(keyC300).getInteger("num_doc_fin");
					def dataDoc = mapDocumentosC300.get(keyC300).getDate("dt_doc");
					
					txt2.print("C300");
					txt2.print(mapDocumentosC300.get(keyC300).getString("cod_mod"));
					txt2.print(mapDocumentosC300.get(keyC300).getString("ser"));
					txt2.print(numInicial);
					txt2.print(numFinal);
					txt2.print(dataDoc.format(ddMMyyyy));
					txt2.print(formatarValor(mapDocumentosC300.get(keyC300).getBigDecimal_Zero("vl_doc"), 2));
					txt2.print(formatarValor(mapDocumentosC300.get(keyC300).getBigDecimal_Zero("vl_pis"), 2));
					txt2.print(formatarValor(mapDocumentosC300.get(keyC300).getBigDecimal_Zero("vl_cofins"), 2));
					txt2.print(null);
					txt2.newLine();
					qtLinBlocoC++;
					qtLinC300++;
					
					/**
					 * REGISTRO C310: Documentos Cancelados de Notas Fiscais de Venda a Consumidor (Código 02)
					 */
					if(gerarRegByPerfil(perfil, "C310", 1)) {
						Set<Integer> setCanc = mapDocumentosC300.get(keyC300).get("cancelados");
						if(setCanc != null && setCanc.size() > 0) {
							for(Integer num : setCanc) {
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C310");
								
								txt2.print("C310");
								txt2.print(num);
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC310++;
							}
						}
					}
					
					/**
					 * REGISTRO C320: Registro Analítico do Resumo Diário das Notas Fiscais de Venda a Consumidor (Código 02)
					 */
					if(gerarRegByPerfil(perfil, "C320", 1)) {
						def mapC320 = new HashMap<String, TableMap>();
						def mapItens = new HashMap<String, Set<Long>>();
						
						def rsC320 = buscarDocumentosEFDRegistroC320(modelosC300, dataDoc, numInicial, numFinal, getCampo(alinEFD, "C320", "ALIQ_ICMS"), getCampo(alinEFD, "C320", "VL_BC_ICMS"), getCampo(alinEFD, "C320", "VL_ICMS"), getCampo(alinEFD, "C320", "VL_RED_BC"));
						
						for(int i = 0; i < rsC320.size(); i++) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Compondo registro C320");
							
							def keyC320 = rsC320.get(i).getString("aaj10codigo") + "/" + rsC320.get(i).getString("aaj15codigo") + "/" + rsC320.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C320", "ALIQ_ICMS"));
							
							def tm = new TableMap();
							tm.put("cst_icms", rsC320.get(i).getString("aaj10codigo"));
							tm.put("cfop", rsC320.get(i).getString("ab13codigo"));
							tm.put("aliq_icms", rsC320.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C320" , "ALIQ_ICMS")));
							
							def vlr_opr = mapC320.get(keyC320).getBigDecimal_Zero("vl_opr");
							tm.put("vl_opr", vlr_opr.add(rsC320.get(i).getBigDecimal_Zero("eaa0103totDoc")));
							
							def vlr_bc_icms = mapC320.get(keyC320).getBigDecimal_Zero("vl_bc_icms");
							tm.put("vl_bc_icms", vlr_bc_icms.add(rsC320.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C320", "VL_BC_ICMS"))));
							
							def vl_icms = mapC320.get(keyC320).getBigDecimal_Zero("vl_icms");
							tm.put("vl_icms", vl_icms.add(rsC320.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C320", "VL_ICMS"))));
							
							def vl_red_bc = mapC320.get(keyC320).getBigDecimal_Zero("vl_red_bc");
							tm.put("vl_red_bc", vl_red_bc.add(rsC320.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C320", "VL_RED_BC"))));
							
							mapC320.put(keyC320, tm);
							
							Set<Long> itens = mapItens.get(keyC320);
							if(itens == null) itens = new HashSet<Long>();
							itens.add(rsC320.get(i).getLong("eaa0103id"));
							mapItens.put(keyC320, itens);
						}
						rsC320 = null;
					
						for(String keyC320 : mapC320.keySet()) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C320");
							
							txt2.print("C320");
							txt2.print(mapC320.get(keyC320).getString("cst_icms"));
							txt2.print(mapC320.get(keyC320).getString("cfop"));
							txt2.print(formatarValor(mapC320.get(keyC320).getBigDecimal_Zero("aliq_icms"), 2));
							txt2.print(formatarValor(mapC320.get(keyC320).getBigDecimal_Zero("vl_opr"), 2));
							txt2.print(formatarValor(mapC320.get(keyC320).getBigDecimal_Zero("vl_bc_icms"), 2));
							txt2.print(formatarValor(mapC320.get(keyC320).getBigDecimal_Zero("vl_icms"), 2));
							txt2.print(formatarValor(mapC320.get(keyC320).getBigDecimal_Zero("vl_red_bc"), 2));
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC320++;
							
							/**
							 * REGISTRO C321: Itens do Resumo Diário dos Documentos (Código 02)
							 */
							if(gerarRegByPerfil(perfil, "C321", 1)) {
								def mapC321 = new HashMap<String, TableMap>();
								def rsC321 = buscarDocumentosEFDRegistroC321(mapItens.get(keyC320), getCampo(alinEFD, "C321", "VL_DESC"), getCampo(alinEFD, "C321", "VL_BC_ICMS"), getCampo(alinEFD, "C321", "VL_ICMS"), getCampo(alinEFD, "C321", "VL_PIS"), getCampo(alinEFD, "C321", "VL_COFINS"));
								for(int i = 0; i < rsC321.size(); i++) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Compondo registro C321");
									
									def keyC321 = rsC321.get(i).getLong("abm01id");
									
									def tm = new TableMap();
									
									tm.put("item", rsC321.get(i).getLong("abm01id"));
									tm.put("cod_item", rsC321.get(i).getString("abm01codigo"));
									tm.put("unid", rsC321.get(i).getString("aam06codigo"));
									tm.put("aam06id", rsC321.get(i).getLong("aam06id"));
									
									def qtd = mapC321.get(keyC321).getBigDecimal_Zero("qtd");
									tm.put("qtd", qtd.add(rsC321.get(i).getBigDecimal_Zero("eaa0103qtComl")));
									
									def vl_item = mapC321.get(keyC321).getBigDecimal_Zero("vl_item");
									tm.put("vl_item", vl_item.add(rsC321.get(i).getBigDecimal_Zero("eaa0103totDoc")));
									
									def vl_desc = mapC321.get(keyC321).getBigDecimal_Zero("vl_desc");
									tm.put("vl_desc", vl_desc.add(rsC321.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C321", "VL_DESC"))));
									
									def vl_bc_icms = mapC321.get(keyC321).getBigDecimal_Zero("vl_bc_icms");
									tm.put("vl_bc_icms", vl_bc_icms.add(rsC321.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C321", "VL_BC_ICMS"))));
									
									def vl_icms = mapC321.get(keyC321).getBigDecimal_Zero("vl_icms");
									tm.put("vl_icms", vl_icms.add(rsC321.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C321", "VL_ICMS"))));
									
									def vl_pis = mapC321.get(keyC321).getBigDecimal_Zero("vl_pis");
									tm.put("vl_pis", vl_pis.add(rsC321.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C321", "VL_PIS"))));
									
									def vl_cofins = mapC321.get(keyC321).getBigDecimal_Zero("vl_cofins");
									tm.put("vl_cofins", vl_cofins.add(rsC321.get(i).getBigDecimal_Zero(getCampo(alinEFD, "C321", "VL_COFINS"))));
									
									mapC321.put(keyC321, tm);
								}
							
								for(String keyC321 : mapC321.keySet()) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C321");
									
									txt2.print("C321");
									
									txt2.print(mapC321.get(keyC321).getString("cod_item"));
									if(mapC321.get(keyC321).getLong("item") != null) comporRegistro0200(getSession().get(Abm01.class, mapC321.get(keyC321).getLong("item")));
									
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("qtd"), 3));
									
									def unid = mapC321.get(keyC321).getString("unid");
									txt2.print(unid);
									if(unid != null) {
										set0190.add(mapC321.get(keyC321).getLong("aam06id"));
										
										Abm01 abm01 = getSession().get(Abm01.class, mapC321.get(keyC321).getLong("item"));
										Aam06 aam06 = getSession().get(Aam06.class, mapC321.get(keyC321).getLong("aam06id"));
										comporRegistro0220(abm01, aam06, true);
									}
									
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_item"), 2));
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_desc"), 2));
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_bc_icms"), 2));
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_icms"), 2));
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_pis"), 2));
									txt2.print(formatarValor(mapC321.get(keyC321).getBigDecimal_Zero("vl_cofins"), 2));
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC321++;
								}
							}
						}
					}
				}
			}
		}
		
		/**
		 * REGISTRO C350 - Nota Fiscal de Venda a Consumidor (Código 02)
		 */
		if(gerarRegByPerfil(perfil, "C350", 1)) {
			def pagina = 0;
			List<Eaa01> eaa01s = buscarDocumentosPorMovimentoModelo(1, 1, modelosC350, pagina)
			while(eaa01s.size() > 0) {
				for(Eaa01 eaa01 : eaa01s) {
					Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
					Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
					Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
					TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
					
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro C350 - Documento: " + abb01.abb01num);
					
					txt2.print("C350");
					txt2.print(abb01.abb01serie);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(abb01.abb01data.format(ddMMyyyy));
					txt2.print(StringUtils.extractNumbers(eaa0102.eaa0102ni));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C350", "VL_DESC")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C350", "VL_PIS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C350", "VL_COFINS")), 2));
					txt2.print(null);
					txt2.newLine();
					qtLinBlocoC++;
					qtLinC350++;
					
					/**
					 * REGISTRO C370 - Itens do documento (Código 02)
					 */
					
					def eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id ORDER BY eaa0103seq", Parametro.criar("eaa01id", eaa01.eaa01id));
					if(gerarRegByPerfil(perfil, "C370", 1) && eaa0103s != null && eaa0103s.size() > 0) {
						for(Eaa0103 eaa0103 : eaa0103s) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C370 - Documento: " + abb01.abb01num);
							
							TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
							
							Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
							comporRegistro0200(abm01);
							
							txt2.print("C370");
							txt2.print(eaa0103.eaa0103seq);
							txt2.print(abm01.abm01codigo);
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C370", "QTD")), 3));
							
							if(eaa0103.eaa0103umComl != null) {
								Aam06 aam06 = getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id);
								txt2.print(aam06.aam06codigo);
								set0190.add(eaa0103.eaa0103umComl.aam06id);
								comporRegistro0220(abm01, aam06, true);
							}else {
								txt2.print(null);
							}
														
							txt2.print(formatarValor(eaa0103.eaa0103total, 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "C370", "VL_DESC")), 2));
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC370++;
						}
					}
					
					/**
					 * REGISTRO C390 - Registro Analítico das Notas Fiscais de Venda a Consumidor (Código 02)
					 */
					if(gerarRegByPerfil(perfil, "C390", 1)) {
						def eaa0102rs = buscarResumoValoresC390(eaa01.eaa01id, getCampo(alinEFD, "C390", "ALIQ_ICMS"), getCampo(alinEFD, "C390", "VL_BC_ICMS"), getCampo(alinEFD, "C390", "VL_ICMS"), getCampo(alinEFD, "C390", "VL_RED_BC"));
						if(eaa0102rs != null && eaa0102rs.size() > 0) {
							for(TableMap tm : eaa0102rs) {
								verificarProcessoCancelado();
								enviarStatusProcesso("Gerando registro C390 - Documento: " + abb01.abb01num);
								
								txt2.print("C390");
								txt2.print(tm.getString("aaj10codigo"));
								txt2.print(tm.getString("aaj15codigo"));
								txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C390", "ALIQ_ICMS")), 2));
								txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
								txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C390", "VL_BC_ICMS")), 2));
								txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C390", "VL_ICMS")), 2));
								txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C390", "VL_RED_BC")), 2));
								txt2.print(null);
								txt2.newLine();
								qtLinBlocoC++;
								qtLinC390++;
							}
						}
					}
				}
				pagina++;
				eaa01s = buscarDocumentosPorMovimentoModelo(1, 1, modelosC350, pagina)
			}
		}
	}
	
	def gerarC500() {
		/**
		 * REGISTRO C500: NF de Energia Elétrica (Código 06), NF de Energia Elétrica Eletronica - NF3e (Cod 66) 
		 * NF de Fornecimento de água canalizada (29) e NF Gás (28)
		 */
		def pagina = 0;
		
		List<Eaa01> eaa01s = buscarDocumentosPorMovimentoModelo(0, 0, modelosC500, pagina);
		while(eaa01s.size() > 0) {
			for(Eaa01 eaa01 : eaa01s) {
				if(!gerarRegByPerfil(perfil, "C500", eaa01.eaa01esMov)) continue;
				
				Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
				Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
				TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
				
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro C500 - Documento: " + abb01.abb01num);
				
				if(eaa01.eaa01sitDoc == null) throw new ValidacaoException("A situação do documento não foi informada. Documento de entrada: " + abb01.abb01num);
				
				Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
				
				def modelo = aah01.aah01modelo;
				def serie = formatarSerie(abb01.abb01serie, modelo);
				
				if("02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd)) {
					txt2.print("C500");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(null);
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
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
					txt2.print("C500");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(gerarCodigoEntidade(abe01, eaa0102.eaa0102ie));
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_DESC")), 2));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_TERC")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_DA")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_BC_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_BC_ICMS_ST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_ICMS_ST")), 2));
					txt2.print(null);
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_PIS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C500", "VL_COFINS")), 2));
					txt2.print(null);
					txt2.print(null);
					txt2.print(eaa01.eaa01nfeChave);
					txt2.print(modelo.equals("66") ? "1" : null);
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
				}
				qtLinBlocoC++;
				qtLinC500++;
				
				/**
				 * REGISTRO C590 - Registro Analítico do Documento - Nota Fiscal/Conta de Energia Elétrica (Código 06) e Nota Fiscal Consumo Fornecimento de Gás (Código 28)
				 */
				if(gerarRegByPerfil(perfil, "C590", eaa01.eaa01esMov) && !"02".equals(aaj03.aaj03efd) && !"03".equals(aaj03.aaj03efd)) {
					List<TableMap> eaa0103rs = buscarResumoValoresC590(eaa01.eaa01id, getCampo(alinEFD, "C590", "ALIQ_ICMS"), getCampo(alinEFD, "C590", "VL_BC_ICMS"), getCampo(alinEFD, "C590", "VL_ICMS"), getCampo(alinEFD, "C590", "VL_BC_ICMS_ST"), getCampo(alinEFD, "C590", "VL_ICMS_ST"), getCampo(alinEFD, "C590", "VL_RED_BC"));
					if(eaa0103rs != null && eaa0103rs.size() > 0) {
						for(TableMap tm : eaa0103rs) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C590 - Documento: " + abb01.abb01num);
							
							txt2.print("C590");
							txt2.print(tm.getString("aaj10codigo"));
							txt2.print(tm.getString("aaj15codigo"));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "ALIQ_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "VL_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "VL_BC_ICMS_ST")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "VL_ICMS_ST")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C590", "VL_RED_BC")), 2));
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC590++;
						}
					}
				}
			}
			pagina++;
			eaa01s = buscarDocumentosPorMovimentoModelo(0, 0, modelosC500, pagina);
		}
	}
	
	def gerarC800() {
		/**
		 * REGISTRO C800: Cupom Fiscal Eletrônico - SAT (Código 59)
		 */
		if(gerarRegByPerfil(perfil, "C800", 1)) {
			def pagina = 0;
			List<Eaa01> eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, pagina);
			while(eaa01s.size() > 0) {
				for(Eaa01 eaa01 : eaa01s) {
					Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
					Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
					Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
					Abd10 abd10 = getSession().get(Abd10.class, eaa01.eaa01cfEF.abd10id);
					TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
					
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro C800 - Documento: " + abb01.abb01num);
					
					if(eaa01.eaa01sitDoc == null) throw new ValidacaoException("A situação do documento não foi informada. Documento de entrada: " + abb01.abb01num);
					
					Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
					
					if("02".equals(aaj03.aaj03efd) || "03".equals(aaj03.aaj03efd)) {
						txt2.print("C800");
						txt2.print(aah01.aah01modelo);
						txt2.print(aaj03.aaj03efd);
						txt2.print(abb01.abb01num);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(abd10.abd10serieFabr);
						txt2.print(eaa01.eaa01nfeChave);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
						txt2.newLine();
					}else{
						txt2.print("C800");
						txt2.print(aah01.aah01modelo);
						txt2.print(aaj03.aaj03efd);
						txt2.print(abb01.abb01num);
						txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
						txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_PIS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_COFINS")), 2));
						txt2.print(eaa0102.eaa0102ni != null ? StringUtils.extractNumbers(eaa0102.eaa0102ni) : null);
						txt2.print(abd10.abd10serieFabr);
						txt2.print(eaa01.eaa01nfeChave);
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_DESC")), 2));
						txt2.print(formatarValor(eaa01.eaa01totItens, 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_OUT_DA")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_ICMS")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_PIS_ST")), 2));
						txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "C800", "VL_COFINS_ST")), 2));
						txt2.newLine();
					}
					qtLinBlocoC++;
					qtLinC800++;
					
					
					/**
					 * REGISTRO C850 - Registro Analítico do CF-e SAT
					 */
					if(gerarRegByPerfil(perfil, "C850", 1)) {
						if(eaa01.eaa01cancData == null){
							List<TableMap> eaa0103rs = buscarResumoValoresC850(eaa01.eaa01id, getCampo(alinEFD, "C850", "ALIQ_ICMS"), getCampo(alinEFD, "C850", "VL_BC_ICMS"), getCampo(alinEFD, "C850", "VL_ICMS"));
							if(eaa0103rs != null && eaa0103rs.size() > 0) {
								for(TableMap tm : eaa0103rs) {
									verificarProcessoCancelado();
									enviarStatusProcesso("Gerando registro C850 - Documento: " + abb01.abb01num);
									
									txt2.print("C850");
									txt2.print(tm.getString("aaj10codigo"));
									txt2.print(tm.getString("aaj15codigo"));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C850", "ALIQ_ICMS")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C850", "VL_BC_ICMS")), 2));
									txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C850", "VL_ICMS")), 2));
									txt2.print(null);
									txt2.newLine();
									qtLinBlocoC++;
									qtLinC850++;
								}
							}
						}
					}
				}
				pagina++;
				eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, pagina);
			}
		}
		
		if(gerarRegByPerfil(perfil, "C860", 1)) {
			def mapSAT = new HashMap<String, TableMap>();

			def pagina = 0;
			List<Eaa01> eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, pagina);
			while(eaa01s.size() > 0) {
				for(Eaa01 eaa01 : eaa01s) {
					Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
					Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
					Abd10 abd10 = getSession().get(Abd10.class, eaa01.eaa01cfEF.abd10id);
					
					verificarProcessoCancelado();
					enviarStatusProcesso("Compondo registro C860 - Documento: " + abb01.abb01num);
					
					def key = aah01.aah01modelo + "/" + abd10.abd10serieFabr + "/" + abb01.abb01data;
					
					def tm = new TableMap();
					
					tm.put("aah01modelo", aah01.aah01modelo);
					tm.put("abd10serieFabr", abd10.abd10serieFabr);
					tm.put("abb01data", abb01.abb01data);
					
					if(mapSAT.get(key).getInteger("numI") == null || mapSAT.get(key).getInteger("numI") > abb01.abb01num) {
						tm.put("numI", abb01.abb01num);
					}
					if(mapSAT.get(key).getInteger("numF") == null || mapSAT.get(key).getInteger("numF") < abb01.abb01num) {
						tm.put("numF", abb01.abb01num);
					}
					
					tm.put("abb01data", abb01.abb01data);
					
					Set<Long> eaa01Ids = mapSAT.get(key).get("eaa01Ids");
					if(eaa01Ids == null) eaa01Ids = new HashSet<>();
					eaa01Ids.add(eaa01.eaa01id);
					tm.put("eaa01Ids", eaa01Ids);
					
					mapSAT.put(key, tm);
				}
				pagina++;
				eaa01s = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, pagina);
			}
			
			def setSAT = new TreeSet<>();
			setSAT.addAll(mapSAT.keySet());
			for(String key : setSAT) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro C860");
				
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
				 * REGISTRO C890: Resumo Diário do CF-e SAT
				 */
				if(gerarRegByPerfil(perfil, "C890", 1)) {
					def eaa0103rs = buscarResumoValoresC890(mapSAT.get(key).get("ea01Ids"), getCampo(alinEFD, "C890", "ALIQ_ICMS"), getCampo(alinEFD, "C890", "VL_BC_ICMS"), getCampo(alinEFD, "C890", "VL_ICMS"));
					if(eaa0103rs != null && eaa0103rs.size() > 0) {
						for(TableMap tm : eaa0103rs) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro C890");
							
							txt2.print("C890");
							txt2.print(tm.getString("aaj10codigo"));
							txt2.print(tm.getString("aaj15codigo"));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C890", "ALIQ_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C890", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "C890", "VL_ICMS")), 2));
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoC++;
							qtLinC890++;
						}
					}
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
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco D...");
		
		/**
		 * REGISTRO D001 - Abertura Bloco D
		 */
		txt2.print("D001");
		txt2.print(contemDadosBlocoD() ? 0 : 1);
		txt2.newLine();
		qtLinBlocoD++;
		
		/**
		 * REGISTRO D100 - Nota Fiscal de Serviço de Transporte (Código 07) e Conhecimentos de Transporte Rodoviário de Cargas (Código 08),
		 */
		def pagina = 0;
		def eaa01s = buscarDocumentosPorModelo(modelosD100, pagina);
		while(eaa01s.size() > 0) {
			for(Eaa01 eaa01 : eaa01s) {
				if(!gerarRegByPerfil(perfil, "D100", eaa01.eaa01esMov)) continue;
				
				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
				Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
				
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro D100 - Documento: " + abb01.abb01num);
				
				if(eaa01.eaa01sitDoc == null) {
					if(eaa01.eaa01esMov == 0) {
						throw new ValidacaoException("A situação do documento não foi informada. Documento de entrada: " + abb01.abb01num);
					}else {
						throw new ValidacaoException("A situação do documento não foi informada. Documento de saída: " + abb01.abb01num);
					}
				}
				
				def modelo = aah01.aah01modelo;
				def serie = formatarSerie(abb01.abb01serie, modelo);
				
				Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				Abd10 abd10 = eaa01.eaa01cfEF != null ? getSession().get(Abd10.class, eaa01.eaa01cfEF.abd10id) : null;
				Aaj03 aaj03 = getSession().get(Aaj03.class, eaa01.eaa01sitDoc.aaj03id);
				TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
				
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
					txt2.print(null);
					txt2.print(null);
					txt2.newLine();
					
				}else {
					txt2.print("D100");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(gerarCodigoEntidade(abe01, eaa0102.eaa0102ie));
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(eaa01.eaa01nfeChave);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
					txt2.print(null);
					txt2.print(null);
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D100", "VL_DESC")), 2));
					txt2.print(eaa0102.eaa0102frete == null ? 9 : eaa0102.eaa0102frete);
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D100", "VL_BC_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D100", "VL_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D100", "VL_NT")), 2));
					
					String obs = eaa01.eaa01obsFisco == null ? null : eaa01.eaa01obsFisco.length() > 255 ? eaa01.eaa01obsFisco.substring(0, 255).trim() : eaa01.eaa01obsFisco.trim();
					def cod = null;
					if(obs != null) {
						obs = obs.replace('|', '-');
						cod = map0450 == null ? "1" : ""+(map0450.size()+1);
						map0450.put(cod, obs);
					}
					txt2.print(cod);
					
					txt2.print(null);
					
					if(modelo.equals("57") || modelo.equals("67") || modelo.equals("63")){
						Aag0201 munOrig = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aag0201 INNER JOIN Eaa0101 ON eaa0101municipio = aag0201id WHERE eaa0101saida = 1 AND eaa0101doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
						Aag0201 munDest = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aag0201 INNER JOIN Eaa0101 ON eaa0101municipio = aag0201id WHERE eaa0101entrega = 1 AND eaa0101doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
						
						def ufOrig = munOrig != null && munOrig.aag0201uf != null ? getSession().get(Aag02.class, munOrig.aag0201uf.aag02id) : null;
						def ufDest = munDest != null && munDest.aag0201uf != null ? getSession().get(Aag02.class, munDest.aag0201uf.aag02id) : null;
						
						txt2.print(munOrig == null ? null : ufOrig.aag02uf.equalsIgnoreCase("EX") ? "9999999" : munOrig.aag0201ibge);
						txt2.print(ufDest == null ? null : ufDest.aag02uf.equalsIgnoreCase("EX") ? "9999999" : munDest.aag0201ibge);
					}else{
						txt2.print(null);
						txt2.print(null);
					}
					
					txt2.newLine();
				}
				qtLinBlocoD++;
				qtLinD100++;
				
				/**
				 * REGISTRO D101: Informação Complementar dos Documentos Fiscais quando das Operações Interestaduais destinadas a consumidor final não contribuinte EC 87/15
				 */
				if(gerarRegByPerfil(perfil, "D101", eaa01.eaa01esMov) && (modelo.equals("57") || modelo.equals("67")) && (ufDest != null && !ufDest.aag02uf.equals(aac10.aac10municipio.aag0201uf.aag02uf)) && eaa0102.eaa0102consFinal == 1 && eaa0102.eaa0102contribIcms == 0) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro D101 - Documento: " + abb01.abb01num);
					
					txt2.print("D101");
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D101", "VL_FCP_UF_DEST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D101", "VL_ICMS_UF_DEST")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D101", "VL_ICMS_UF_REM")), 2));
					txt2.newLine();
					
					qtLinBlocoD++;
					qtLinD101++;
				}
				
				
				/**
				 * REGISTRO D190 - Registro Analítico do Documento (Código 07, 08, 8B, 09, 10, 11, 26, 27, 57 e 67)
				 */
				if(gerarRegByPerfil(perfil, "D190", eaa01.eaa01esMov) && !"02".equals(aaj03.aaj03efd) && !"03".equals(aaj03.aaj03efd)) {
					def eaa0103s = buscarResumoValoresD190(eaa01.eaa01id, getCampo(alinEFD, "D190", "ALIQ_ICMS"), getCampo(alinEFD, "D190", "VL_BC_ICMS"), getCampo(alinEFD, "D190", "VL_ICMS"), getCampo(alinEFD, "D190", "VL_RED_BC"));
					if(eaa0103s != null && eaa0103s.size() > 0) {
						for(TableMap tm : eaa0103s) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro D190 - Documento: " + abb01.abb01num);
							
							txt2.print("D190");
							txt2.print(tm.getString("aaj10codigo"));
							txt2.print(tm.getString("aaj15codigo"));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D190", "ALIQ_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D190", "VL_BC_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D190", "VL_ICMS")), 2));
							txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D190", "VL_RED_BC")), 2));
							txt2.print(null);
							txt2.newLine();
							qtLinBlocoD++;
							qtLinD190++;
						}
					}
				}
				
				/**
				 * REGISTRO D195: Observações do Lançamento Fiscal
				 */
				if(gerarRegByPerfil(perfil, "D195", eaa01.eaa01esMov)){
					def eaa01031s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa01031 INNER JOIN Eaa0103 ON eaa01031item = eaa0103id WHERE eaa0103doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
					for(Eaa01031 eaa01031 : eaa01031s) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro D195 - Documento: " + abb01.abb01num);
						
						def cod = "" + (map0460 == null ? 1 : map0460.size()+1);
						
						def obs = eaa01031.eaa01031obs == null ? "Documento com ajuste fiscal" : eaa01031.eaa01031obs;
						map0460.put(cod, obs);
						
						txt2.print("D195");
						txt2.print(cod);
						txt2.print(eaa01031.eaa01031obsComplem);
						txt2.newLine();
						qtLinBlocoD++;
						qtLinD195++;
						
						/**
						 * REGISTRO D197: Outras obrigações tributárias, ajustes e informações de valores provenientes de documento fiscal
						 */
						if(gerarRegByPerfil(perfil, "D197", eaa01.eaa01esMov) && eaa01031.eaa01031codAjuste != null) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro D197 - Documento: " + abb01.abb01num);
							
							Aaj16 aaj16 = getSession().get(Aaj16.class, eaa01031.eaa01031codAjuste.aaj16id);
							txt2.print("D197");
							txt2.print(aaj16 == null ? null : aaj16.aaj16codigo);
							txt2.print(eaa01031.eaa01031descr);
							
							Eaa0103 eaa0103 = getSession().get(Eaa0103.class, eaa01031.eaa01031item.eaa0103id);
							Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
							
							if(abm01 != null) {
								txt2.print(abm01.abm01codigo);
								if(abm01 != null) comporRegistro0200(abm01);
							}else {
								txt2.print(null);
							}
							
							txt2.print(formatarValor(eaa01031.eaa01031icmsBc, 2));
							txt2.print(formatarValor(eaa01031.eaa01031icmsTx, 2));
							txt2.print(formatarValor(eaa01031.eaa01031icms, 2));
							txt2.print(formatarValor(eaa01031.eaa01031icmsOutras, 2));
							txt2.newLine();
							qtLinBlocoD++;
							qtLinD197++;
						}
					}
				}
			}
			pagina++;
			eaa01s = buscarDocumentosPorModelo(modelosD100, pagina);
		}
	
		
		/**
		 * Registro D500 - Nota Fiscal de Serviço de Comunicação (Código 21) e Nota Fiscal de Serviço de Telecomunicação (Código 22)
		 */
		pagina = 0;
		eaa01s = buscarDocumentosPorModelo(modelosD500, pagina);
		while(eaa01s.size() > 0) {
			for(Eaa01 eaa01 : eaa01s) {
				if(!gerarRegByPerfil(perfil, "D500", eaa01.eaa01esMov)) continue;
				Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
				
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro D500 - Documento: " + abb01.abb01num);
				
				Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
				Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
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
					txt2.print(null);
					txt2.print(null);
					txt2.newLine();
					
				}else {
					txt2.print("D500");
					txt2.print(eaa01.eaa01esMov);
					txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
					txt2.print(gerarCodigoEntidade(abe01, eaa0102.eaa0102ie));
					txt2.print(modelo);
					txt2.print(aaj03.aaj03efd);
					txt2.print(serie);
					txt2.print(null);
					txt2.print(abb01.abb01num);
					txt2.print(abb01.abb01data == null ? null : abb01.abb01data.format(ddMMyyyy));
					txt2.print(eaa01.eaa01esData == null ? null : eaa01.eaa01esData.format(ddMMyyyy));
					txt2.print(formatarValor(eaa01.eaa01totDoc, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_DESC")), 2));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(eaa01.eaa01totItens, 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_TERC")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_DA")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_BC_ICMS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_ICMS")), 2));
					
					String obs = eaa01.eaa01obsFisco == null ? null : eaa01.eaa01obsFisco.length() > 255 ? eaa01.eaa01obsFisco.substring(0, 255).trim() : eaa01.eaa01obsFisco.trim();
					def cod = null;
					if(obs != null) {
						obs = obs.replace('|', '-');
						cod = map0450 == null ? "1" : ""+(map0450.size()+1);
						map0450.put(cod, obs);
					}
					txt2.print(cod);
					
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_PIS")), 2));
					txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "D500", "VL_COFINS")), 2));
					txt2.print(null);
					txt2.print(null);
					txt2.newLine();
				}
				qtLinBlocoD++;
				qtLinD500++;
				
				/**
				 * REGISTRO D590: Registro Analítico do Documento (Código 21 e 22)
				 */
				if(gerarRegByPerfil(perfil, "D590", eaa01.eaa01esMov) && !"02".equals(aaj03.aaj03efd) && !"03".equals(aaj03.aaj03efd)) {
					def eaa0103s = buscarResumoValoresD590(eaa01.eaa01id, getCampo(alinEFD, "D590", "ALIQ_ICMS"), getCampo(alinEFD, "D590", "VL_BC_ICMS"), getCampo(alinEFD, "D590", "VL_ICMS"), getCampo(alinEFD, "D590", "VL_BC_ICMS_ST"), getCampo(alinEFD, "D590", "VL_ICMS_ST"), getCampo(alinEFD, "D590", "VL_RED_BC"));
					for(TableMap tm : eaa0103s) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro D590 - Documento: " + abb01.abb01num);
						
						txt2.print("D590");
						txt2.print(tm.getString("aaj10codigo"));
						txt2.print(tm.getString("aaj15codigo"));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "ALIQ_ICMS")), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero("eaa0103totDoc"), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "VL_BC_ICMS")), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "VL_ICMS")), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "VL_BC_ICMS_ST")), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "VL_ICMS_ST")), 2));
						txt2.print(formatarValor(tm.getBigDecimal_Zero(getCampo(alinEFD, "D590", "VL_RED_BC")), 2));
						txt2.print(null);
						txt2.newLine();
						qtLinBlocoD++;
						qtLinD590++;
					}
				}
			}
			pagina++;
			eaa01s = buscarDocumentosPorModelo(modelosD500, pagina);
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
	 * * * * * * * * * * * * * * * * * * * * *  BLOCO E: APURAÇÃO DO ICMS E DO IPI * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoE() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando bloco E");
		
		/**
		 * REGISTRO E001 - Abertura Bloco E
		 */
		txt2.print("E001");
		txt2.print(0);
		txt2.newLine();
		qtLinBlocoE++;
		
		/**
		 * REGISTRO E100 - Período da Apuração do ICMS
		 */
		Edb01 edb01 = buscarApuracoes(ano, mes, "011");
		if(edb01 == null)throw new ValidacaoException("Necessário fazer a apuração do ICMS para geração da EFD. Processo cancelado.");
		
		txt2.print("E100");
		txt2.print(dtInicial.format(ddMMyyyy));
		txt2.print(dtFinal.format(ddMMyyyy));
		txt2.newLine();
		qtLinE100++;
		qtLinBlocoE++;
			
			
		/**
		 * REGISTRO E110 - Apuração do ICMS - Operações Próprias
		 */
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando registro E110");
		
		TableMap jsonEdb01 = edb01.edb01json == null ? new TableMap() : edb01.edb01json;
		
		txt2.print("E110");
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "debSaidas")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "debAjustes")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "debAjustesApur")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "estornoCred")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "credEntradas")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "credAjustes")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "credAjustesApur")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "estornoDeb")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "saldoCredorAnt")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "saldo")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "deducoes")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "saldoDevedor")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "saldoCredor")), 2));
		txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcms, "0", "valoresExtra")), 2));
		txt2.newLine();
		qtLinE110++;
		qtLinBlocoE++;
			
		/**
		 * REGISTRO E111 - Ajuste/Benefício/Incentivo da Apuração do ICMS
		 */
		def edb0101s = buscarAjustesApuracoes(edb01.edb01id);
		for(Edb0101 edb0101 : edb0101s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E111");
			
			txt2.print("E111");
			
			Aaj17 aaj17 = getSession().get(Aaj17.class, edb0101.edb0101ajuste.aaj17id);
			txt2.print(aaj17.aaj17codigo);
			txt2.print(edb0101.edb0101descrComp);
			txt2.print(formatarValor(edb0101.edb0101valor, 2));
			txt2.newLine();
			qtLinE111++;
			qtLinBlocoE++;

			/**
			 * REGISTRO E112 - Informações Adicionais dos Ajustes da Apuração do ICMS
			 */
			def edb01011s = buscarInformacoesAdicionais(edb0101.edb0101id);
			
			for(Edb01011 edb01011 : edb01011s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E112");
				
				txt2.print("E112");
				txt2.print(edb01011.edb01011daNum);
				
				Abb40 abb40 = edb01011.edb01011processo == null ? null : getSession().get(Abb40.class, edb01011.edb01011processo.abb40id);
				txt2.print(abb40 == null ? null : abb40.abb40num);
				txt2.print(abb40 == null ? null : abb40.abb40indProc);
				txt2.print(abb40 == null ? null : abb40.abb40descr == null ? null : abb40.abb40descr > 255 ? abb40.abb40descr.substring(0, 255) : abb40.abb40descr);
				txt2.print(null);
				txt2.newLine();
				qtLinE112++;
				qtLinBlocoE++;
			}
		}
		
		/**
		 * REGISTRO E115 - Informações Adicionais da Apuração - Valores Declaratórios
		 */
		def edb0103s = buscarValoresDeclaratorios(edb01.edb01id);
		for(Edb0103 edb0103 : edb0103s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E115");
			
			txt2.print("E115");
			
			Aaj18 aaj18 = edb0103.edb0103info == null ? null : getSession().get(Aaj18.class, edb0103.edb0103info.aaj18id)
			txt2.print(aaj18 == null ? null : aaj18.aaj18codigo);
			txt2.print(formatarValor(edb0103.edb0103valor, 2));
			txt2.print(edb0103.edb0103descrComp);
			txt2.newLine();
			qtLinE115++;
			qtLinBlocoE++;
		}
		
		/**
		 * REGISTRO E116 - Obrigações do ICMS a Recolher - Operações próprias
		 */
		def edb0102s = buscarObrigacoesARecolher(edb01.edb01id);
		for(Edb0102 edb0102 : edb0102s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E116");
			
			txt2.print("E116");
			
			Aaj29 aaj29 = getSession().get(Aaj29.class, edb0102.edb0102obrig.aaj29id);
			txt2.print(aaj29 == null ? null : aaj29.aaj29codigo);
			txt2.print(formatarValor(edb0102.edb0102valor, 2));
			txt2.print(edb0102.edb0102dtVcto == null ? null : edb0102.edb0102dtVcto.format(ddMMyyyy));
			txt2.print(edb0102.edb0102codRec);
			
			Abb40 abb40 = edb0102.edb0102processo == null ? null : getSession().get(Abb40.class, edb0102.edb0102processo.abb40id);
			txt2.print(abb40 == null ? null : abb40.abb40num);
			txt2.print(abb40 == null ? null : abb40.abb40indProc);
			txt2.print(abb40 == null ? null : abb40.abb40descr);
			txt2.print(edb0102.edb0102descrComp);
			txt2.print(StringUtils.ajustString(edb01.edb01mes, 2) + edb01.edb01ano);
			txt2.newLine();
			qtLinE116++;
			qtLinBlocoE++;
		}
		
		/**
		 * REGISTRO E200 - Período da Apuração do ICMS - Substituição Tributária
		 */
		def edb01s = buscarApuracoesEFD(ano, mes, "012");
		for(Edb01 edb01st : edb01s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E200");
			
			Aag02 aag02 = getSession().get(Aag02.class, edb01st.edb01uf.aag02id);
			txt2.print("E200");
			txt2.print(aag02.aag02uf);
			txt2.print(dtInicial.format(ddMMyyyy));
			txt2.print(dtFinal.format(ddMMyyyy));
			txt2.newLine();
			qtLinE200++;
			qtLinBlocoE++;
			
			/**
			 * REGISTRO E210 - Apuração do ICMS - Substituição Tributária
			 */
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E210");
			
			jsonEdb01 = edb01st.edb01json == null ? new TableMap() : edb01st.edb01json;
			
			txt2.print("E210");
			txt2.print(isComOperacaoesST(edb01) ? "1" : "0");
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "credAnt")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "devolucao")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ressarcimento")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "outrosCred")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ajustesCred")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "retencao")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "outrosDeb")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ajustesDeb")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldo")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "deducoes")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldoDevedor")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldoCredor")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "extra")), 2));
			txt2.newLine();
			qtLinE210++;
			qtLinBlocoE++;
			
			/**
			 * REGISTRO E220 - Ajuste/Benefício/Incentivo da Apuração do ICMS Subtituição Tributária
			 */
			edb0101s = buscarAjustesApuracoes(edb01st.edb01id);
			for(Edb0101 edb0101 : edb0101s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E220");
				
				txt2.print("E220");
				
				Aaj17 aaj17 = getSession().get(Aaj17.class, edb0101.edb0101ajuste.aaj17id);
				txt2.print(aaj17.aaj17codigo);
				txt2.print(edb0101.edb0101descrComp);
				txt2.print(formatarValor(edb0101.edb0101valor, 2));
				txt2.newLine();
				qtLinE220++;
				qtLinBlocoE++;
				
				/**
				 * REGISTRO E230 - Informações Adicionais dos Ajustes da Apuração do ICMS Substituição Tributária
				 */
				def edb01011s = buscarInformacoesAdicionais(edb0101.edb0101id);
				for(Edb01011 edb01011 : edb01011s) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro E230");
					
					txt2.print("E230");
					txt2.print(edb01011.edb01011daNum);
					
					Abb40 abb40 = edb01011.edb01011processo == null ? null : getSession().get(Abb40.class, edb01011.edb01011processo.abb40id);
					txt2.print(abb40 == null ? null : abb40.abb40num);
					txt2.print(abb40 == null ? null : abb40.abb40indProc);
					txt2.print(abb40 == null ? null : abb40.abb40descr);
					txt2.print(null);
					txt2.newLine();
					qtLinE230++;
					qtLinBlocoE++;
				}
			}
			
			/**
			 * REGISTRO E250 - Obrigações do ICMS a Recolher - Substituição Tributária
			 */
			edb0102s = buscarObrigacoesARecolher(edb01st.edb01id);
			for(Edb0102 edb0102 : edb0102s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E250");
				
				txt2.print("E250");
				
				Aaj29 aaj29 = edb0102.edb0102obrig == null ? null : getSession().get(Aaj29.class, edb0102.edb0102obrig.aaj29id);
				txt2.print(aaj29 == null ? null : aaj29.aaj29codigo);
				txt2.print(formatarValor(edb0102.edb0102valor, 2));
				txt2.print(edb0102.edb0102dtVcto == null ? null : edb0102.edb0102dtVcto.format(ddMMyyyy));
				txt2.print(edb0102.edb0102codRec);
				
				Abb40 abb40 = edb0102.edb0102processo == null ? null : getSession().get(Abb40.class, edb0102.edb0102processo.abb40id);
				txt2.print(abb40 == null ? null : abb40.abb40num);
				txt2.print(abb40 == null ? null : abb40.abb40indProc);
				txt2.print(abb40 == null ? null : abb40.abb40descr);
				txt2.print(null);
				txt2.print(StringUtils.ajustString(edb01st.edb01mes, 2) + edb01st.edb01ano);
				txt2.newLine();
				qtLinE250++;
				qtLinBlocoE++;
			}
		}
		
		
		/**
		 * REGISTRO E300 - Período de Apuração do ICMS Diferencial de Alíquota - UF Origem/DestinoEC 87/15
		 */
		edb01s = buscarApuracoesEFD(ano, mes, "014");
		for(Edb01 edb01dif : edb01s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E300");
			
			txt2.print("E300");
			
			Aag02 aag02 = getSession().get(Aag02.class, edb01dif.edb01uf.aag02id);
			txt2.print(aag02.aag02uf);
			txt2.print(dtInicial.format(ddMMyyyy));
			txt2.print(dtFinal.format(ddMMyyyy));
			txt2.newLine();
			qtLinE300++;
			qtLinBlocoE++;
			
			/**
			 * REGISTRO E310 - Apuração do ICMS Diferencial de Alíquota - UF Origem/Destino EC 87/15
			 */
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro E310");
			
			jsonEdb01 = edb01dif.edb01json == null ? new TableMap() : edb01dif.edb01json;
			
			txt2.print("E310");
			txt2.print(isComOperacaoesDifAliq(edb01dif) ? "1" : "0");
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredAntDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "debDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuDebDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "credDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuCredDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoDevDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "deducoesDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "vlrRecolDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "extraDifal")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredAntFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "debFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuDebFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "credFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuCredFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoDevFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "deducoesFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "vlrRecolFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredFcp")), 2));
			txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "extraFcp")), 2));
			txt2.newLine();
			qtLinE310++;
			qtLinBlocoE++;
			
			/**
			 * REGISTRO E311 - Ajuste/Benefício/Incentivo da Apuração do ICMS Diferencial Alíquota
			 */
			edb0101s = buscarAjustesApuracoes(edb01dif.edb01id);
			for(Edb0101 edb0101 : edb0101s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E311");
				
				txt2.print("E311");
				Aaj17 aaj17 = getSession().get(Aaj17.class, edb0101.edb0101ajuste.aaj17id);
				txt2.print(aaj17.aaj17codigo);
				txt2.print(edb0101.edb0101descrComp);
				txt2.print(formatarValor(edb0101.edb0101valor, 2));
				txt2.newLine();
				qtLinE311++;
				qtLinBlocoE++;
				
				/**
				 * REGISTRO E312 - Informações Adicionais dos Ajustes
				 */
				def edb01011s = buscarInformacoesAdicionais(edb0101.edb0101id);
				for(Edb01011 edb01011 : edb01011s) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro E312");
					
					if(edb01011.edb01011daNum == null && edb01011.edb01011processo == null) continue;
					
					Abb40 abb40 = edb01011.edb01011processo == null ? null : getSession().get(Abb40.class, edb01011.edb01011processo.abb40id);
					
					txt2.print("E312");
					txt2.print(edb01011.edb01011daNum);
					
					if(abb40 != null) {
						txt2.print(abb40.abb40num);
						txt2.print(abb40.abb40indProc);
						txt2.print(abb40.abb40descr == null ? null : abb40.abb40descr.length() > 255 ? abb40.abb40descr.substring(0, 255) : abb40.abb40descr);
					}else {
						txt2.print(null);
						txt2.print(null);
						txt2.print(null);
					}
					
					txt2.print(null);
					txt2.newLine();
					qtLinE312++;
					qtLinBlocoE++;
				}
			}
					
			/**
			 * REGISTRO E316 - Obrigações do ICMS Recolhido ou a Recolher
			 */
			edb0102s = buscarObrigacoesARecolher(edb01dif.edb01id);
			for(Edb0102 edb0102 : edb0102s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E316");
				
				Aaj29 aaj29 = edb0102.edb0102obrig == null ? null : getSession().get(Aaj29.class, edb0102.edb0102obrig.aaj29id);
				Abb40 abb40 = edb0102.edb0102processo == null ? null : getSession().get(Abb40.class, edb0102.edb0102processo.abb40id);
				
				txt2.print("E316");
				txt2.print(aaj29 == null ? null : aaj29.aaj29codigo);
				txt2.print(formatarValor(edb0102.edb0102valor, 2));
				txt2.print(edb0102.edb0102dtVcto == null ? null : edb0102.edb0102dtVcto.format(ddMMyyyy));
				txt2.print(edb0102.edb0102codRec);
				txt2.print(abb40 == null ? null : abb40.abb40num);
				txt2.print(abb40 == null ? null : abb40.abb40indProc);
				txt2.print(abb40 == null ? null : abb40.abb40descr);
				txt2.print(null);
				txt2.print(StringUtils.ajustString(edb01dif.edb01mes, 2) + edb01dif.edb01ano);
				txt2.newLine();
				qtLinE316++;
				qtLinBlocoE++;
			}
		}
		
		
		/**
		 * REGISTRO E500 - Período de Apuração do IPI
		 */
		if(aac13.aac13tipoAtiv.equals(0)) {
			edb01s = buscarApuracoesEFD(ano, mes, "013");
			if(edb01s == null || edb01s.size() == 0) throw new ValidacaoException("Necessário fazer a apuração do IPI para geração da EFD. Processo cancelado.");
			
			for(Edb01 edb01ipi : edb01s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E500");
				
				txt2.print("E500");
				txt2.print(0);
				txt2.print(dtInicial.format(ddMMyyyy));
				txt2.print(dtFinal.format(ddMMyyyy));
				txt2.newLine();
				qtLinE500++;
				qtLinBlocoE++;
				
				/**
				 * REGISTRO E510 - Consolidação dos Valores do IPI
				 */
				List<TableMap> rsE510 = buscarResumoValoresE510(eaa01sE510, getCampo(alinEFD, "E510", "VL_BC_IPI"), getCampo(alinEFD, "E510", "VL_IPI"));
				if(rsE510 != null && rsE510.size() > 0) {
					for(int i = 0; i < rsE510.size(); i++) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro E510");
						
						txt2.print("E510");
						txt2.print(rsE510.get(i).getString("aaj15codigo"));
						txt2.print(rsE510.get(i).getString("aaj11codigo"));
						txt2.print(formatarValor(rsE510.get(i).getBigDecimal_Zero("eaa0103totDoc"), 2));
						txt2.print(formatarValor(rsE510.get(i).getBigDecimal_Zero(getCampo(alinEFD, "E510", "VL_BC_IPI")), 2));
						txt2.print(formatarValor(rsE510.get(i).getBigDecimal_Zero(getCampo(alinEFD, "E510", "VL_IPI")), 2));
						txt2.newLine();
						qtLinE510++;
						qtLinBlocoE++;
					}
				}
				
				/**
				 * REGISTRO E520 - Apuração do IPI
				 */
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro E520");
				
				jsonEdb01 = edb01ipi.edb01json == null ? new TableMap() : edb01ipi.edb01json;
				
				txt2.print("E520");
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "credAnt")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "debSaidas")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "credEntradas")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "outrosDeb")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "outrosCred")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "saldoCredor")), 2));
				txt2.print(formatarValor(jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIpi, "0", "saldoDevedor")), 2));
				txt2.newLine();
				qtLinE520++;
				qtLinBlocoE++;
				
				/**
				 * REGISTRO E530 - Ajuste da Apuração do IPI
				 */
				def edb0105s = buscarAjustesIPI(edb01ipi.edb01id);
				for(Edb0105 edb0105 : edb0105s) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro E530");
					
					txt2.print("E530");
					
					Aaj19 aaj19 = edb0105.edb0105ajuste == null ? null : getSession().get(Aaj19.class, edb0105.edb0105ajuste.aaj19id);
					txt2.print(aaj19 == null ? null : aaj19.aaj19natureza);
					txt2.print(formatarValor(edb0105.edb0105valor, 2));
					txt2.print(aaj19 == null ? null : aaj19.aaj19codigo);
					txt2.print(edb0105.edb0105origem);
	
					Abb40 abb40 = edb0105.edb0105processo == null ? null : getSession().get(Abb40.class, edb0105.edb0105processo.abb40id);
					txt2.print(abb40 == null ? null : abb40.abb40num);
					txt2.print(edb0105.edb0105descr);
					txt2.newLine();
					qtLinE530++;
					qtLinBlocoE++;
				}
			}
		}
		
		
		/**
		 * REGISTRO E990 - Encerramento do Bloco E
		 */
		qtLinBlocoE++;
		
		txt2.print("E990");
		txt2.print(qtLinBlocoE);
		txt2.newLine();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * *  BLOCO G: CIAP  * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoG() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco G");
		
		/**
		 * REGISTRO G001 - Abertura Bloco G
		 */
		def ecc01s = buscarCIAPPorPeriodoOuBaixados(mes, ano);
		
		txt2.print("G001");
		txt2.print(ecc01s != null && ecc01s.size() > 0 ? "0" : "1");
		txt2.newLine();
		qtLinBlocoG++;
		
		if(ecc01s != null && ecc01s.size() > 0) {
			
			/**
			 * Dados dos valores do CIAP
			 */
			def sdoInicial = BigDecimal.ZERO;
			def totalIcmsAprop = BigDecimal.ZERO;
			def outrosCreditos = BigDecimal.ZERO;
			def totalSaidas = BigDecimal.ZERO;
			def totalTribExp = BigDecimal.ZERO;
			
			def ecc0101Valores = buscarValoresDoFatorVariavelCIAP(mes, ano);
			if(ecc0101Valores != null) {
				totalSaidas = ecc0101Valores.ecc0101json.getBigDecimal_Zero("totalSaidas");
				
				Ecc01 ecc01 = getSession().get(Ecc01.class, ecc0101Valores.ecc0101ficha.ecc01id);
				
				if(ecc01.MODELO_MODELO_B_60) {
					def isentas = ecc0101Valores.ecc0101json.getBigDecimal_Zero(getCampo(alinEFD, "G001", "isentas"));
					def ipi = ecc0101Valores.ecc0101json.getBigDecimal_Zero(getCampo(alinEFD, "G001", "ipi"));
					totalTribExp = isentas.add(ipi);
					
				}else {
					def icms = ecc0101Valores.ecc0101json.getBigDecimal_Zero(getCampo(alinEFD, "G001", "icms"));
					def outras = ecc0101Valores.ecc0101json.getBigDecimal_Zero(getCampo(alinEFD, "G001", "outras"));
					totalTribExp = icms.add(outras);
				}
			}
			
			def mapG125 = new ArrayList<TableMap>();
			int linG125 = 0;
			for(Ecc01 ecc01 : ecc01s) {
				Ecc0101 ecc0101 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Ecc0101 WHERE ecc0101ficha = :ecc01id AND ecc0101mes = :mes AND ecc0101ano = :ano", Parametro.criar("ecc01id", ecc01.ecc01id), Parametro.criar("mes", mes), Parametro.criar("ano", ano));
				
				Abb20 abb20 = getSession().get(Abb20.class, ecc01.ecc01bem.abb20id);
				set0300.add(abb20.abb20id);
				
				TableMap jsonEcc0101 = ecc0101.ecc0101json != null ? ecc0101.ecc0101json : new TableMap(); 
								
				def tipoMov = definirTipoMov(ecc01, ecc0101);
				
				LocalDate dataMov = null;
				if(tipoMov.equals("SI") || tipoMov.equals("AT") || tipoMov.equals("PE") || tipoMov.equals("OT")) {
					dataMov = dtInicial;
				}else {
					dataMov = dtFinal;
				}
				
				def tm = new TableMap();
				tm.put("abb20codigo", abb20.abb20codigo);
				tm.put("dataMov", dataMov);
				tm.put("tipoMov", tipoMov.equals("BA") || tipoMov.equals("AT") || tipoMov.equals("PE") || tipoMov.equals("OT") ? "SI" : tipoMov);
				tm.put("icmsPropria", ecc01.ecc01clasIcms == 0 ? ecc01.ecc01clasIcms : null);
				tm.put("icmsST", ecc01.ecc01clasIcms == 1 ? ecc01.ecc01clasIcms : null);
				tm.put("icmsFrete", ecc01.ecc01clasIcms == 2 ? ecc01.ecc01clasIcms : null);
				tm.put("icmsAliq", ecc01.ecc01clasIcms == 3 ? ecc01.ecc01clasIcms : null);
				tm.put("parcela", ecc0101 == null ? 0 : ecc0101.ecc0101parcela);
				tm.put("icmsApropriacao", ecc0101 == null ? 0 : jsonEcc0101.getBigDecimal_Zero("icmsmes"));
				tm.put("abb20", abb20);
				
				if(tm.getString("tipoMov").equals("SI")) sdoInicial = sdoInicial.add(ecc0101.ecc0101icms);
				if(ecc0101 != null) {
					totalIcmsAprop = jsonEcc0101.getBigDecimal_Zero("icmsmes") != null ? totalIcmsAprop.add(jsonEcc0101.getBigDecimal_Zero("icmsmes")) : totalIcmsAprop;
				}
				
				/**
				 * Registro G126
				 */
				def mapG126 = new ArrayList();
				if(ecc0101 != null) {
					List<Ecc0102> ecc0102s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Ecc0102 WHERE ecc0102ficha = :ecc01id AND ecc0102parcela = :parcela", 
						                                        Parametro.criar("ecc01id", ecc01.ecc01id), 
																Parametro.criar("parcela", ecc0101.ecc0101parcela));
					int linG126 = 0;
					for(Ecc0102 ecc0102 : ecc0102s) {
						def map = new TableMap();
						map.put("icmsMes", ecc0102.ecc0102icms);
						map.put("tribexp", ecc0102.ecc0102tribExp);
						map.put("saidas", ecc0102.ecc0102saidas);
						
						def indice = new BigDecimal(0);
						if(!ecc0102.ecc0102saidas.equals(0)) {
							indice = ecc0102.ecc0102tribExp.divide(ecc0102.ecc0102saidas, Scale.ROUND_4);
						}
						map.put("indice", indice);
						
						def outCred = ecc0102.ecc0102icms.multiply(indice);
						map.put("outrosCred", outCred);
						
						outrosCreditos = outrosCreditos.add(outCred);
					}
				}
				
				tm.put("mapG126", mapG126);
				mapG125.add(tm);
				linG125++;
				
				if(tipoMov.equals("BA") || tipoMov.equals("AT") || tipoMov.equals("PE") || tipoMov.equals("OT")) { //Baixados
					if(tipoMov.equals("AT") || tipoMov.equals("PE") || tipoMov.equals("OT")) {
						dataMov = abb20.abb20baixa;
					}else {
						dataMov = dtFinal;
					}
					
					tm = new TableMap();
					tm.put("abb20codigo", abb20.abb20codigo)
					tm.put("dataMov", dataMov)
					tm.put("tipoMov", tipoMov)
					tm.put("abb20", abb20)
					mapG125.add(tm);
					linG125++;
				}
			}
			
			/**
			 * REGISTRO G110 - ICMS - Ativo Permanemte - CIAP
			 */
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro G110");
			
			txt2.print("G110");
			txt2.print(dtInicial.format(ddMMyyyy));
			txt2.print(dtFinal.format(ddMMyyyy));
			txt2.print(formatarValor(sdoInicial, 2));
			txt2.print(formatarValor(totalIcmsAprop, 2));
			txt2.print(formatarValor(totalTribExp, 2));
			txt2.print(formatarValor(totalSaidas, 2));
			
			def indice = new BigDecimal(0);
			if(!totalSaidas.equals(BigDecimal.ZERO)) {
				indice = totalTribExp.divide(totalSaidas, Scale.ROUND_6);
			}
			txt2.print(formatarValor(indice, 6));
			
			txt2.print(formatarValor(totalIcmsAprop.multiply(indice), 2));
			txt2.print(formatarValor(outrosCreditos, 2));
			txt2.newLine();
			qtLinG110++;
			qtLinBlocoG++;
			
			/**
			 * REGISTRO G125 - Movimentação de Bem ou Componente do Ativo Imobilizado
			 */
			for(int i = 0; i < mapG125.size(); i++) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro G125");
				
				def tipoMov = mapG125.get(i).getString("tipoMov");
				 
				txt2.print("G125");
				txt2.print(mapG125.get(i).getString("abb20codigo"));
				txt2.print(mapG125.get(i).getDate("dataMov").format(ddMMyyyy));
				txt2.print(mapG125.get(i).getString("tipoMov"));
				txt2.print(formatarValor(mapG125.get(i).getBigDecimal_Zero("icmsPropria"), 2));
				txt2.print(formatarValor(mapG125.get(i).getBigDecimal_Zero("icmsST"), 2));
				txt2.print(formatarValor(mapG125.get(i).getBigDecimal_Zero("icmsFrete"), 2));
				txt2.print(formatarValor(mapG125.get(i).getBigDecimal_Zero("icmsAliq"), 2));
				txt2.print(mapG125.get(i).getInteger("parcela"));
				txt2.print(formatarValor(mapG125.get(i).getBigDecimal_Zero("icmsApropriacao"), 2));
				txt2.newLine();
				qtLinG125++;
				qtLinBlocoG++;
				

				/**
				 * REGISTRO G126 - Outros Créditos CIAP
				 */
				List<TableMap> mapG126 = mapG125.get(i).get("mapG126");
				if(mapG126 != null && mapG126.size() > 0) {
					for(int j = 0; j < mapG126.size(); j++) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro G126");
						
						txt2.print("G126");
						txt2.print(dtInicial.format(ddMMyyyy));
						txt2.print(dtFinal.format(ddMMyyyy));
						txt2.print(mapG125.get(i).getInteger("parcela"));
						txt2.print(formatarValor(mapG126.get(j).getBigDecimal_Zero("icmsmes"), 2));
						txt2.print(formatarValor(mapG126.get(j).getBigDecimal_Zero("tribexp"), 2));
						txt2.print(formatarValor(mapG126.get(j).getBigDecimal_Zero("saidas"), 2));
						txt2.print(formatarValor(mapG126.get(j).getBigDecimal_Zero("indice"), 4));
						txt2.print(formatarValor(mapG126.get(j).getBigDecimal_Zero("outrosCred"), 2));
						txt2.newLine();
						qtLinG126++;
						qtLinBlocoG++;
					}
				}
				
				/**
				 * REGISTRO G130 - Identificação do Documento Fiscal
				 */
				if(tipoMov.equals("MC") || tipoMov.equals("IM") || tipoMov.equals("IA") || tipoMov.equals("AT")) {
					Abb20 abb20 = mapG125.get(i).get("abb20");

					Abb01 abb01 = null;
					if(tipoMov.equals("AT")) { //Baixa
						abb01 = getSession().get(Abb01.class, abb20.abb20centralBx.abb01id);
					}else {
						abb01 = getSession().get(Abb01.class, abb20.abb20central.abb01id);
					}
					
					Eaa01 eaa01 = getSession().get(Eaa01.class, Criterions.eq("eaa01central", abb01.abb01id));
					if(eaa01 != null) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro G130");
						
						
						TableMap jsonEaa01 = eaa01.eaa01json != null ? eaa01.eaa01json : new TableMap();
						
						Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
						Abe01 abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
						Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
						
						txt2.print("G130");
						txt2.print(eaa01.eaa01emissao == Eaa01.SIM ? 0 : 1);
						txt2.print(gerarCodigoEntidade(abe01, eaa0102.eaa0102ie));
						txt2.print(aah01.aah01modelo);
						txt2.print(abb01.abb01serie);
						txt2.print(abb01.abb01num);
						txt2.print(eaa01.eaa01nfeChave);
						txt2.print(abb01.abb01data.format(ddMMyyyy));
						txt2.print(null);
						txt2.newLine();
						qtLinG130++;
						qtLinBlocoG++;
						
						/**
						 * REGISTRO G140 - Identificação do Item do Documento Fiscal
						 */
							
						Eaa0103 eaa0103 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id AND eaa0103seq = :seq", 
							                                 Parametro.criar("eaa01id", eaa01.eaa01id), 
															 Parametro.criar("seq", abb20.abb20seqItem));
						if(eaa0103 != null) {
							verificarProcessoCancelado();
							enviarStatusProcesso("Gerando registro G140");
							
							TableMap jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
							
							Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
							txt2.print("G140");
							txt2.print(eaa0103.eaa0103seq);
							txt2.print(abm01.abm01codigo);
							
							txt2.print(formatarValor(jsonEaa01.getBigDecimal_Zero(getCampo(alinEFD, "G140", "QTD")), 5));

							if(eaa0103.eaa0103umComl != null) {
								Aam06 aam06 = getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id);
								txt2.print(aam06.aam06codigo);
								set0190.add(eaa0103.eaa0103umComl.aam06id);
								comporRegistro0220(abm01, aam06, eaa01.eaa01esMov == 1);
							}else {
								txt2.print(null);
							}
															
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "G140", "VL_ICMS_OP_APLICADO")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "G140", "VL_ICMS_ST_APLICADO")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "G140", "VL_ICMS_FRT_APLICADO")), 2));
							txt2.print(formatarValor(jsonEaa0103.getBigDecimal_Zero(getCampo(alinEFD, "G140", "VL_ICMS_DIF_APLICADO")), 2));
							
							txt2.newLine();
							qtLinG140++;
							qtLinBlocoG++;
							
							comporRegistro0200(abm01);
						}
					}
				}
			}
		}
		
		/**
		 * REGISTRO G990 - Encerramento do Bloco G
		 */
		qtLinBlocoG++;
		
		txt2.print("G990");
		txt2.print(qtLinBlocoG);
		txt2.newLine();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * *  BLOCO H: INVENTÁRIO FÍSICO * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoH() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando bloco H");
		
		/** Registro H010 */
		def totalEstoque = new BigDecimal(0);
		def mapH010 = new ArrayList<TableMap>();
		
		/**
		 * Dados do inventário
		 */
		List<Bcb11> bcb11s = null;
		if(dtInventario != null) {
			bcb11s = buscarItensInventarioPorData();
			
			if(bcb11s != null && bcb11s.size() > 0) {
				int linha = 0;
				for(Bcb11 bcb11 : bcb11s) {
					
					Abm01 abm01 = getSession().get(Abm01.class, bcb11.bcb11item.abm01id);
					if(abm01 != null) {
						Abe01 abe01 = bcb11.bcb11ent == null ? null : getSession().get(Abe01.class, bcb11.bcb11ent.abe01id);
	
						if(bcb11.bcb11qtde.equals(0)) continue;
						comporRegistro0200(abm01);
						
						def tm = new TableMap();
						tm.put("id_item", abm01.abm01id);
						tm.put("cod_item", abm01.abm01codigo);
						tm.put("unid", bcb11.bcb11unid);
						tm.put("qtd", formatarValor(bcb11.bcb11qtde, 3));
						tm.put("vl_unit", formatarValor(bcb11.bcb11unit, 6));
						
						def total = bcb11.bcb11total;
						tm.put("vl_item", formatarValor(total, 2));
						
						Abm40 abm40 = getSession().get(Abm40.class, bcb11.bcb11grupo.abm40id);
						tm.put("ind_prop", abm40.abm40posse);
						
						if(abm40.abm40posse == 0 || abe01 == null) {
							tm.put("cod_part", null);
						}else {
							tm.put("cod_part", gerarCodigoEntidade(abe01, abe01.abe01ie));
						}
						tm.put("txt_compl", abm01.abm01complem);
						tm.put("vl_item_ir", bcb11.bcb11total);
						
						Abc10 abc10 = abm40.abm40cta == null ? null : getSession().get(Abc10.class, abm40.abm40cta.abc10id);
						if(abc10 != null) {
							tm.put("cod_cta", abc10.abc10codigo);
							abc10s.add(abc10.abc10codigo);
						}
						
						totalEstoque = totalEstoque.add(total);
						mapH010.add(tm);
						linha++;
					}
				}
			}
		}
		
		
		/**
		 * REGISTRO H001 - Abertura Bloco H
		 */
		Bcb10 bcb10 = dtInventario == null ? null :
		              getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Bcb10 WHERE (EXTRACT(MONTH FROM bcb10data)) = :mes AND (EXTRACT(YEAR FROM bcb10data)) = :ano " + obterWherePadrao("Bcb10"),
						                 Parametro.criar("mes", dtInventario.getMonthValue()),
						                 Parametro.criar("ano", dtInventario.getYear()));
		
        def gerarBlocoH = bcb10 != null && (mapH010.size() > 0 || dtInicial.getMonthValue() == 2);
					   
		txt2.print("H001");
		txt2.print(gerarBlocoH ? 0 : 1);
		txt2.newLine();
		qtLinBlocoH++;
		
		if(gerarBlocoH) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro H005");
			
			/**
			 * REGISTRO H005 - Totais do Inventário
			 */
			txt2.print("H005");
			txt2.print(dtInventario.format(ddMMyyyy));
			txt2.print(formatarValor(totalEstoque, 2));
			txt2.print(bcb10.bcb10motivo, 2);
			txt2.newLine();
			qtLinH005++;
			qtLinBlocoH++;
			
			/**
			 * REGISTRO H010 - Inventário
			 */
			if(mapH010.size() > 0) {
				for(int i = 0; i < mapH010.size(); i++) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro H010");
					
					txt2.print("H010");
					txt2.print(mapH010.get(i).getString("cod_item"));
					txt2.print(mapH010.get(i).getString("unid"));
					txt2.print(mapH010.get(i).getString("qtd"));
					txt2.print(mapH010.get(i).getString("vl_unit"));
					txt2.print(mapH010.get(i).getString("vl_item"));
					txt2.print(mapH010.get(i).getInteger("ind_prop"));
					txt2.print(mapH010.get(i).getString("cod_part"));
					txt2.print(mapH010.get(i).getString("txt_compl"));
					txt2.print(mapH010.get(i).getString("cod_cta"));
					txt2.print(formatarValor(mapH010.get(i).getBigDecimal_Zero("vl_item_ir"), 2));
					txt2.newLine();
					qtLinH010++;
					qtLinBlocoH++;
					
					/**
					 * REGISTRO H020 - Informação Complementar do Inventário
					 */
					def edd0201s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Edd0201 " +
								 											 " INNER JOIN Edd02 ON edd02id = edd0201ci " +  
                                                                             " WHERE edd02mes = :mes AND edd02ano = :ano AND " +
																			 " edd0201item = :abm01id " + 
						                                                     obterWherePadrao("Edd02"), Parametro.criar("mes", dtInventario.getMonthValue()), 
																			                            Parametro.criar("ano", dtInventario.getYear()), 
																										Parametro.criar("abm01id", mapH010.get(i).getLong("id_item")));
					
					for(Edd0201 edd0201 : edd0201s) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro H020");
						
						txt2.print("H020");
						
						Aaj10 aaj10 = getSession().get(Aaj10.class, edd0201.edd0201cstIcms.aaj10id);
						txt2.print(aaj10 == null ? null : aaj10.aaj10codigo);
						
						txt2.print(formatarValor(edd0201.edd0201bcIcms, 2));
						txt2.print(formatarValor(edd0201.edd0201icms, 2));
						txt2.newLine();
						qtLinH020++;
						qtLinBlocoH++;
					}
				}
			}
		}
		
		/**
		 * REGISTRO H990 - Encerramento do Bloco H
		 */
		qtLinBlocoH++;
		
		txt2.print("H990");
		txt2.print(qtLinBlocoH);
		txt2.newLine();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * BLOCO K: CONTROLE DA PRODUÇÃO E ESTOQUE * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBlocoK() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando Bloco K");
		
		/**
		 * REGISTRO K001 - Abertura Bloco K
		 */
		
		Set<Long> abm20idsSPP = new HashSet<Long>();
		List<Long> abm20idsEntradaSPP = buscarPLEsK230();
		abm20idsSPP.addAll(abm20idsEntradaSPP);
		
		def rsK220s = buscarLctos_K220();
		def rsK230s = buscarLctos_K230(abm20idsSPP);
		def rsK250s = buscarLctos_K250();
		
		List<Bcb11> bcb11s = buscarItensInventarioPorDataEClass();
		List<Bcc01> bcc01s = buscarCorrecoesPorAnoMesClass();
		
		boolean comMovimentoBlocoK = bcc01s.size() > 0 || bcb11s.size() > 0 || (rsK230s != null && rsK230s.size() > 0) || (rsK250s != null && rsK250s.size() > 0);
		
		txt2.print("K001");
		txt2.print(comMovimentoBlocoK ? "0" : "1");
		txt2.newLine();
		qtLinBlocoK++;
		
		if(comMovimentoBlocoK){
			/**
			 * REGISTRO K100 - Período de Apuração
			 */
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro K100");
			
			txt2.print("K100");
			txt2.print(dtInicial.format(ddMMyyyy));
			txt2.print(dtFinal.format(ddMMyyyy));
			txt2.newLine();
			qtLinK100++;
			qtLinBlocoK++;
			
			/**
			 * REGISTRO K200 - Estoque Escriturado
			 */
			if(bcb11s != null && bcb11s.size() > 0) {
				int linha = 0;
				for(Bcb11 bcb11 : bcb11s) {
					Abm01 abm01 = getSession().get(Abm01.class, bcb11.bcb11item.abm01id);
					if(abm01 != null) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro K200");
						
						Abe01 abe01 = bcb11.bcb11ent == null ? null : getSession().get(Abe01.class, bcb11.bcb11ent.abe01id);

						if(bcb11.bcb11qtde.equals(0)) continue;
						comporRegistro0200(abm01);
						
						txt2.print("K200");
						txt2.print(dtFinal.format(ddMMyyyy));
						txt2.print(abm01.abm01codigo);
						txt2.print(formatarValor(bcb11.bcb11qtde, 3));
						
						Abm40 abm40 = getSession().get(Abm40.class, bcb11.bcb11grupo.abm40id);
						txt2.print(abm40.abm40posse);
						
						if(abm40.abm40posse == 0 || abe01 == null) {
							txt2.print(null);
						}else {
							txt2.print(gerarCodigoEntidade(abe01, abe01.abe01ie));
						}
						
						txt2.newLine();
						qtLinK200++;
						qtLinBlocoK++;
						linha++;
					}
				}
			}
			
			
			/**
			 * REGISTRO K220 - Outras Movimentações Internas entre Mercadorias
			 */
			def mapK220 = new HashMap<String, TableMap>();
			for(int i = 0; i < rsK220s.size(); i++) {
				
				def key = rsK220s.get(i).getDate("bcc01data") + "/" + rsK220s.get(i).getLong("bcc01origItem") + "/" + rsK220s.get(i).getLong("abm01id");
				
				def tm = new TableMap();
				tm.put("data", rsK220s.get(i).getDate("bcc01data"));
				tm.put("itemOrigem", rsK220s.get(i).getLong("bcc01origItem"));
				tm.put("itemDestino", rsK220s.get(i).getLong("abm01id"));
				
				def qtOrigem = rsK220s.get(i).getBigDecimal_Zero("bcc01origQt");
				tm.put("qtdOrigem", mapK220.get(key).getBigDecimal_Zero("qtdOrigem").add(qtOrigem));
				tm.put("qtdDestino", mapK220.get(key).getBigDecimal_Zero("qtdDestino").add(rsK220s.get(i).getBigDecimal_Zero("bcc01qt")));
				
				mapK220.put(key, tm);
			}
			
			for(Object key : mapK220.keySet()) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro K220");
						
				Abm01 abm01Origem = getSession().get(Abm01.class, mapK220.get(key).getLong("itemOrigem"));
				comporRegistro0200(abm01Origem);
				
				Abm01 abm01Destino = getSession().get(Abm01.class, mapK220.get(key).getLong("itemDestino"));
				comporRegistro0200(abm01Destino);

				txt2.print("K220");
				txt2.print(mapK220.get(key).getDate("data").format(ddMMyyyy));
				txt2.print(abm01Origem.abm01codigo);
				txt2.print(abm01Destino.abm01codigo);
				txt2.print(formatarValor(mapK220.get(key).getBigDecimal_Zero("qtdOrigem"), 6));
				txt2.print(formatarValor(mapK220.get(key).getBigDecimal_Zero("qtdDestino"), 6));
				txt2.newLine();
				qtLinK220++;
				qtLinBlocoK++;
			}

			/**
			 * REGISTRO K230 - Itens Produzidos
			 */
			for(int i = 0; i < rsK230s.size(); i++){
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro K230");
				
				Abm01 abm01Principal = getSession().get(Abm01.class, rsK230s.get(i).getLong("abm01id"));
				comporRegistro0200(abm01Principal);
				
				txt2.print("K230");
				txt2.print(rsK230s.get(i).getDate("abb01data") == null ? null : rsK230s.get(i).getDate("abb01data").format(ddMMyyyy));
				txt2.print(rsK230s.get(i).getDate("bcc01data").format(ddMMyyyy));
				txt2.print(rsK230s.get(i).getInteger("abb01num"));
				txt2.print(rsK230s.get(i).getString("abm01codigo"));
				txt2.print(formatarValor(rsK230s.get(i).getBigDecimal_Zero("bcc01qt"), 6));
				txt2.newLine();
				qtLinK230++;
				qtLinBlocoK++;
				
				/**
				 * REGISTRO K235 - Insumos Consumidos
				 */
				def rsK235s = buscarLctosECF_K235(rsK230s.get(i).getString("bcc01aglut"));
				for(int j = 0; j < rsK235s.size(); j++){
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro K235");
					
					Abm01 abm01componente = getSession().get(Abm01.class, rsK235s.get(i).getLong("abm01id"));
					comporRegistro0200(abm01componente);
					
					txt2.print("K235");
					txt2.print(rsK235s.get(j).getDate("bcc01data").format(ddMMyyyy));
					txt2.print(abm01componente.abm01codigo);
					txt2.print(formatarValor(rsK235s.get(j).getBigDecimal_Zero("bcc01qt"), 6));
					txt2.print(null);
					txt2.newLine();
					qtLinK235++;
					qtLinBlocoK++;
				}
			}
				
			/**
			 * REGISTRO K250 - Industrialização EFetuada por Terceiros - Itens Produzidos
			 */
			for(int i = 0; i < rsK250s.size(); i++){
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro K250");
				
				Abm01 abm01Principal = getSession().get(Abm01.class, rsK250s.get(i).getLong("abm01id"));
				Set<Long> setCompFormula = new HashSet<>();
				
				txt2.print("K250");
				txt2.print(rsK250s.get(i).getDate("bcc01data").format(ddMMyyyy));
				txt2.print(abm01Principal.abm01codigo);
				txt2.print(formatarValor(rsK250s.get(i).getBigDecimal_Zero("bcc01qt"), 6));
				txt2.newLine();
				qtLinK250++;
				qtLinBlocoK++;
				
				/**
				 * REGISTRO K255 - Insumos Consumidos
				 */
				List<TableMap> rsK255s = buscarLctos_K255(rsK250s.get(i).getLong("abb01tipo"), rsK250s.get(i).getInteger("abb01num"));
				for(int j = 0; j < rsK255s.size(); j++){
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro K255");
					
					Abm01 abm01componente = getSession().get(Abm01.class, rsK255s.get(j).getLong("abm01id"));
					
					txt2.print("K255");
					txt2.print(rsK255s.get(j).getDate("bcc01data").format(ddMMyyyy));
					txt2.print(abm01componente.abm01codigo);
					txt2.print(formatarValor(rsK255s.get(j).getBigDecimal_Zero("bcc01qt"), 6));
					txt2.print(null);
					txt2.newLine();
					qtLinK255++;
					qtLinBlocoK++;
				}
			}
		}
		
		
		/**
		 * REGISTRO K990 - Encerramento do Bloco K
		 */
		qtLinBlocoK++;
		
		txt2.print("K990");
		txt2.print(qtLinBlocoK);
		txt2.newLine();
	}
	
	/**
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * BLOCO 1: OUTRAS INFORMAÇÕES * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 */
	def gerarBloco1() {
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando bloco 1");
		
		boolean contem1100 = contemDados1100();
		boolean contem1400 = contemDados1400(getCampo(alinEFD, "1400", "VALOR"));
		
		boolean comMovimentoBloco1 = contem1100 || contem1400;
		
		/**
		 * REGISTRO 1001 - Abertura Bloco 1
		 */
		txt2.print("1001");
		txt2.print(versaoLeiaute >= 6 ? "0" : comMovimentoBloco1 ? "0" : "1");
		txt2.newLine();
		qtLinBloco1++;
		
		/**
		 * REGISTRO 1010: Obrigatoriedade de Registros do Bloco 1
		 */
		txt2.print("1010");
		txt2.print(contem1100 ? "S" : "N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print(contem1400 ? "S" : "N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");
		txt2.print("N");	
		txt2.newLine();
		qtLin1010++;
		qtLinBloco1++;
		
		/**
		 * REGISTRO 1100: Registro de Informações sobre Exportação
		 */
		def eaa01s = buscarDocumentosRegistro1100();
		for(Eaa01 eaa01 : eaa01s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 1100");
			
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
			Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
			
			def eaa0104s = buscarEaa0104sPorEaa01(eaa01.eaa01id);
			for(TableMap eaa0104 : eaa0104s) {
				txt2.print("1100");
				txt2.print(eaa0104.getInteger("eaa0104tipo"));
				txt2.print(StringUtils.extractNumbers(eaa0104.getString("eaa0104num")));
				txt2.print(eaa0104.getDate("eaa0104data") == null ? null : eaa0104.getDate("eaa0104data").format(ddMMyyyy));
				txt2.print(eaa0104.getInteger("eaa0104nat").equals(0) || eaa0104.getInteger("eaa0104nat").equals(2) ? 0 : 1);
				txt2.print(StringUtils.extractNumbers(eaa0104.getString("eaa01041num")));
				txt2.print(eaa0104.getDate("eaa01041data") == null ? null : eaa0104.getDate("eaa01041data").format(ddMMyyyy));
				txt2.print(eaa0104.getString("eaa0104ceNum"));
				txt2.print(eaa0104.getDate("eaa0104ceData") == null ? null : eaa0104.getDate("eaa0104ceData").format(ddMMyyyy));
				txt2.print(eaa0104.getDate("eaa0104dtAverb") == null ? null : eaa0104.getDate("eaa0104dtAverb").format(ddMMyyyy));
				txt2.print(eaa0104.getInteger("eaa0104ceTipo"), 2, '0', true);
				
				Aag01 aag01 = eaa0104.getLong("eaa0104pais") == null ? null : getSession().get(Aag01.class, eaa0104.getLong("eaa0104pais"));
				txt2.print(aag01 == null ? null : aag01.aag01siscomex);
				txt2.newLine();
				qtLin1100++;
				qtLinBloco1++;
				
				/**
				 * REGISTRO 1105 - Documentos Fiscais de Exportação
				 */
				
				def eaa0103s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0103 WHERE eaa0103doc = :eaa01id ORDER BY eaa0103seq", Parametro.criar("eaa01id", eaa01.eaa01id));
				if(eaa0103s != null && eaa0103s.size() > 0) {
					for(Eaa0103 eaa0103 : eaa0103s) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro 1105");
						
						Abm01 abm01 = getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id);
						comporRegistro0200(abm01);
						
						txt2.print("1105");
						txt2.print(aah01.aah01modelo);
						txt2.print(abb01.abb01serie);
						txt2.print(abb01.abb01num);
						txt2.print(eaa01.eaa01nfeChave);
						txt2.print(abb01.abb01data.format(ddMMyyyy));
						txt2.print(abm01.abm01codigo);
						txt2.newLine();
						qtLin1105++;
						qtLinBloco1++;
					}
				}
			}
		}
		
		/**
		 * REGISTRO 1400 - Informação sobre Valores Agregados
		 */
		def rsEaa01s = buscarDocumentosRegistro1400(getCampo(alinEFD, "1400", "VALOR"));
		if(rsEaa01s != null && rsEaa01s.size() > 0) {
			for(int i = 0; i < rsEaa01s.size(); i++) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro 1400");
				
				txt2.print("1400");
				txt2.print(rsEaa01s.get(i).getString("eaa0102codIPM"));
				txt2.print(rsEaa01s.get(i).getString("aag0201ibge"));
				txt2.print(formatarValor(rsEaa01s.get(i).getBigDecimal_Zero("valor"), 2));
				txt2.newLine();
				qtLin1400++;
				qtLinBloco1++;
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
		verificarProcessoCancelado();
		
		/**
		 * REGISTRO 0150: Tabela de Cadastro do Participante
		 */
		enviarStatusProcesso("Gerando registro 0150");
		
		for(TableMap tmEnt : abe01s) { 
			verificarProcessoCancelado();
			
			Abe01 abe01 = tmEnt.get("abe01");
			
			Abe0101 abe0101 = selecionarEnderecoPorIE(abe01, tmEnt.getString("ie"));

			txt1.print("0150");
			txt1.print(tmEnt.getString("codigo"));
			txt1.print(abe01.abe01nome);
			txt1.print(abe0101.abe0101pais == null ? null : abe0101.abe0101pais.aag01ibge);
			txt1.print(abe01.abe01ti.equals(0) ? StringUtils.ajustString(StringUtils.extractNumbers(abe01.abe01ni), 14) : null);
			txt1.print(abe01.abe01ti.equals(1) ? StringUtils.ajustString(StringUtils.extractNumbers(abe01.abe01ni), 11) : null);
			txt1.print(inscrEstadual(tmEnt.getString("ie")));
			txt1.print(abe0101.abe0101municipio == null ? null : abe0101.abe0101municipio.aag0201ibge);
			txt1.print(retirarMascara(abe01.abe01suframa));
			txt1.print(abe0101.abe0101endereco);
			txt1.print(abe0101.abe0101numero);
			txt1.print(abe0101.abe0101complem);
			txt1.print(abe0101.abe0101bairro);
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0150++;
			
			/**
			 * REGISTRO 0175: Alteração da Tabela de Cadastro de Participante
			 */
			def abe0102s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abe0102 WHERE abe0102ent = :abe01id", Parametro.criar("abe01id", abe01.abe01id));
			if(abe0102s != null && abe0102s.size() > 0) {
				for(Abe0102 abe0102 : abe0102s) {
					int nrCampo = 0;
					String conteudo = null;
					if(abe0102.abe0102campo.equalsIgnoreCase("abe01nome")) {
						nrCampo = 3;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe0101pais")) {
						nrCampo = 4;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe01.abe01ti.equals(0) && abe0102.abe0102campo.equalsIgnoreCase("aa80ni")) {
						nrCampo = 5;
						conteudo = StringUtils.extractNumbers(abe0102.abe0102contAnt);
					}else if(abe01.abe01ti.equals(1) && abe0102.abe0102campo.equalsIgnoreCase("aa80ni")) {
						nrCampo = 6;
						conteudo = StringUtils.extractNumbers(abe0102.abe0102contAnt);
					}else if(abe0102.abe0102campo.equalsIgnoreCase("aag0201ibge")) {
						nrCampo = 8;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe01suframa")) {
						nrCampo = 9;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe0101endereco")) {
						nrCampo = 10;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe0101numero")) {
						nrCampo = 11;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe0101complem")) {
						nrCampo = 12;
						conteudo = abe0102.abe0102contAnt;
					}else if(abe0102.abe0102campo.equalsIgnoreCase("abe0101bairro")) {
						nrCampo = 13;
						conteudo = abe0102.abe0102contAnt;
					}

					if(nrCampo > 0 && (abe0102.abe0102data.compareTo(dtInicial) >= 0 && abe0102.abe0102data.compareTo(dtFinal) <= 0) && abe0102.abe0102contAnt != null) {
						verificarProcessoCancelado();
						enviarStatusProcesso("Gerando registro 0175");
						
						txt1.print("0175");
						txt1.print(abe0102.abe0102data.format(ddMMyyyy));
						txt1.print(nrCampo, 2);
						txt1.print(conteudo.trim());
						txt1.newLine();
						
						qtLinBloco0++;
						qtLin0175++;
					}
				}
			}
		}

		/**
		 * REGISTRO 0190: Identificação das unidades de medida (obtenção dos dados)
		 */
		for(Long aam06id : set0190) {
			Aam06 aam06 = getSession().get(Aam06.class, aam06id);
			if(aam06 == null)continue;
			
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
		for(Long abm01id : set0200) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0200");
			
			Abm01 abm01 = getSession().get(Abm01.class, abm01id);
			Abm0101 abm0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", abm01.abm01id), Criterions.eq("abm0101empresa", aac10.aac10id));
			if(abm0101 == null) throw new ValidacaoException("Necessário informar as configurações do item " + abm01.abm01codigo + " para a empresa " + aac10.aac10codigo);
			Abm12 abm12 = abm0101.abm0101fiscal == null ? null : getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id);
			
			txt1.print("0200");
			txt1.print(abm01.abm01codigo);
			txt1.print(abm01.abm01descr);
			txt1.print(abm01.abm01gtin);
			txt1.print(null);
			
			Aam06 aam06umu = abm01.abm01umu != null ? getSession().get(Aam06.class, abm01.abm01umu.aam06id) : null;
			txt1.print(aam06umu == null ? null : aam06umu.aam06codigo);
			
			txt1.print(abm12 == null ? null : abm12.abm12tipo, 2);

			Abg01 abg01 = abm0101.abm0101ncm == null ? null : getSession().get(Abg01.class, abm0101.abm0101ncm.abg01id);
			String ncm = abg01 == null ? null : abg01.abg01codigo; 
			txt1.print(ncm == null ? null : ncm.indexOf("/") == -1 ? ncm : ncm.substring(0, ncm.indexOf("/")));
			txt1.print(ncm == null ? null : ncm.indexOf("/") == -1 ? null : ncm.substring(ncm.indexOf("/") +1));

			txt1.print(ncm == null ? null : ncm.substring(0, 2));
			
			Aaj05 aaj05 = abm12 == null ? null : abm12.abm12codServ == null ? null : getSession().get(Aaj05.class, abm12.abm12codServ.aaj05id);
			txt1.print(aaj05 == null ? null : aaj05.aaj05codigo);
			
			txt1.print(aac10.aac10municipio.aag0201json == null ? null : formatarValor(aac10.aac10municipio.aag0201json.getBigDecimal_Zero("AliqInterna") , 2));
			txt1.print(abm0101.abm0101cest);	
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0200++;
			
			/**
			 * REGISTRO 0205: Alteração do Item
			 */
			def abm0103s = buscarAlteracoesParaEFDPorItem(abm01.abm01id, mes, ano);
			for(Abm0103 abm0103 : abm0103s) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro 0205");
				
				def dtAnterior = buscarDataAnteriorDaAlteracaoPorItem(abm01.abm01id, abm0103.abm0103data);
				def dtInicial;
				if(dtAnterior == null) {
					dtInicial = MDate.date().of(2000, 1, 1); //01/01/2000
				}else {
					dtInicial = dtAnterior.withDayOfMonth(1);
				}
				
				def dtFinal = abm0103.abm0103data;
				
				if(dtFinal.compareTo(this.dtFinal) == 0) {
					dtFinal = dtFinal.minus(1);
				}
				
				txt1.print("0205");
				txt1.print(abm0103.abm0103campo.equals("abm01descr") ? abm0103.abm0103contAnt : null);
				txt1.print(dtInicial.format(ddMMyyyy));
				txt1.print(dtFinal.format(ddMMyyyy));
				txt1.print(abm0103.abm0103campo.equals("abm01codigo") ? abm0103.abm0103contAnt : null);
				txt1.newLine();
				
				atualizarDataEnvioAEFD(abm0103, mes, ano);
				
				qtLinBloco0++;
				qtLin0205++;
			}
			
			/**
			 * REGISTRO 0206: Código de Produto Conforme Tabela Publicada pela ANP (Combustíveis)
			 */
			if(abm12 != null && abm12.abm12codANP != null) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro 0206");
				
				Aaj04 aaj04 = getSession().get(Aaj04.class, abm12.abm12codANP.aaj04id);
				txt1.print("0206");
				txt1.print(aaj04.aaj04codigo);
				txt1.newLine();
				
				qtLinBloco0++;
				qtLin0206++;
			}
			
			/**
			 * REGISTRO 0220: Fatores de Conversão de Unidades
			 */
			Map<Long, BigDecimal> reg0220 = map0220.get(abm01.abm01id);
			if(reg0220 != null && reg0220.size() > 0){
				for(Long idAam06 : reg0220.keySet()) {
					verificarProcessoCancelado();
					enviarStatusProcesso("Gerando registro 0220");
					
					Aam06 aam06 = getSession().get(Aam06.class, idAam06);
					
					txt1.print("0220");
					txt1.print(aam06.aam06codigo);
					txt1.print(formatarValor(reg0220.get(idAam06), 6));
					txt1.print(null);
					txt1.newLine();
					
					qtLinBloco0++;
					qtLin0220++;
				}
			}
		}
		
		/**
		 * REGISTRO 0300: Cadastro de Bens Imobilizados
		 */
		def setBemPrincipal = new HashSet<Long>();
		for(Long abb20id : set0300) {
			Abb20 abb20 = getSession().get(Abb20.class, abb20id);
			Abb20 bem = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abb20 WHERE abb20codigo = :abb20codigo " + obterWherePadrao("Abb20"), 
				                           Parametro.criar("abb20codigo", abb20.abb20codigo.substring(0, 7).concat("000")));
									   
			if(bem != null) setBemPrincipal.add(bem.abb20id);
		}
		for(Long ab25id : setBemPrincipal) {
			set0300.add(ab25id);
		}
		
		def abb11s = new HashSet<Abb11>();
		
		def setAbb20s = new TreeSet<Long>();
		setAbb20s.addAll(set0300);
		for(Long abb20id : setAbb20s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0300");
			
			Abb20 abb20 = getSession().get(Abb20.class, abb20id)
			if(abb20 == null) continue;
			
			boolean isPrincipal = abb20.abb20codigo.substring(7).equals("000");
			boolean isBem = isPrincipal ? true : abb20.abb20construcao.equals(0);
					
			txt1.print("0300");
			txt1.print(abb20.abb20codigo);
			txt1.print(isBem ? 1 : 2);
			
			def descricao = abb20.abb20nome;
			if(abb20.abb20descr != null) {
				descricao = descricao == null ? abb20.abb20descr : descricao + " " + abb20.abb20descr;
			}
			descricao = descricao.length() > 255 ? descricao.substring(0, 255) : descricao;
			txt1.print(descricao.trim());
			
			txt1.print(isPrincipal ? null : buscarBemPrincipal(abb20.abb20codigo));
			
			String codConta = null;
			Ecb01 ecb01 = getSession().get(Ecb01.class, Criterions.eq("ecb01bem", abb20.abb20id));
			if(ecb01 != null) {
				Ecb0101 ecb0101 = buscarUltimaReclassificacao(ecb01.ecb01id, mes, ano);
				
				Eca01 eca01 = getSession().get(Eca01.class, ecb0101.ecb0101clas.eca01id);
				Eca0101 eca0101 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eca0101 WHERE eca0101clas = :eca01id AND eca0101seq = :seq", 
					                                 Parametro.criar("eca01id", eca01.eca01id), 
													 Parametro.criar("seq", eca01.eca01seqBem));
												 
				Abc10 abc10 = getSession().get(Abc10.class, eca0101.eca0101cta.abc10id);
				
				if(ecb0101 != null) codConta = abc10 == null ? null : abc10.abc10codigo;
			}
			if(codConta == null) {
				Ecc01 ecc01 = getSession().get(Ecc01.class, Criterions.eq("ecc01bem", abb20.abb20id));
				if(ecc01 != null) {
					Abc10 abc10 = getSession().get(Abc10.class, ecc01.ecc01cta.abc10id);
					codConta = abc10 == null ? null : abc10.abc10codigo;
				}
			}
			
			txt1.print(codConta);
			if(codConta != null) abc10s.add(codConta);
			
			def parcelas = null;
			Ecc01 ecc01 = getSession().get(Ecc01.class, Criterions.eq("ecc01bem", abb20.abb20id));
			if(ecc01 != null) {
				List<Ecc0101> ecc0101s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Ecc0101 WHERE ecc0101ficha = :ecc01id", Parametro.criar("ecc01id", ecc01.ecc01id));
				if(ecc0101s != null && ecc0101s.size() > 0) parcelas = ecc0101s.size();
			}
			txt1.print(parcelas);
			
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0300++;
			
			/**
			 * REGISTRO 0305: Utilização do Bem
			 */
			if(isBem) {
				verificarProcessoCancelado();
				enviarStatusProcesso("Gerando registro 0305");
				
				Abb11 abb11 = getSession().get(Abb11.class, abb20.abb20depto.abb11id);
				txt1.print("0305");
				txt1.print(abb11 == null ? null : abb11.abb11codigo);
				txt1.print(abb20.abb20funcao);
				txt1.print(abb20.abb20vidaUtil == 0 ? null : abb20.abb20vidaUtil);
				txt1.newLine();
				
				abb11s.add(abb11);
				qtLinBloco0++;
				qtLin0305++;
			}
		}
		
		/**
		 * REGISTRO 0400: Tabela de Natureza da Operação/Prestação
		 */
		for(String abb10codigo : map0400.keySet()) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0400");
			
			txt1.print("0400");
			txt1.print(abb10codigo);
			txt1.print(map0400.get(abb10codigo));
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0400++;
		}

		/**
		 * REGISTRO 0450: Tabela de Informação Complementar do Documento Fiscal
		 */
		def set0450Ordenado = new TreeSet<String>();
		set0450Ordenado.addAll(map0450.keySet());
		for(String codigo : set0450Ordenado) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0450");
			
			txt1.print("0450");
			txt1.print(codigo);
			
			String obs = map0450.get(codigo);
			txt1.print(obs == null ? null : obs.length() > 255 ? obs.substring(0, 255).trim() : obs.trim());
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0450++;
		}

		/**
		 * REGISTRO 0460: Tabela de Observações do Lançamento Fiscal
		 */
		def set0460Ordenado = new TreeSet<String>();
		set0460Ordenado.addAll(map0460.keySet());
		for(String codigo : set0460Ordenado) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0460");
			
			txt1.print("0460");
			txt1.print(codigo);
			
			String obs = map0460.get(codigo);
			txt1.print(obs == null ? null : obs.length() > 255 ? obs.substring(0, 255).trim() : obs.trim());
			
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0460++;
		}

		/**
		 * REGISTRO 0500: Plano de Contas Contábeis
		 */
		for(String abc10codigo : abc10s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0500");
			
			Abc10 abc10 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abc10 WHERE abc10codigo = :abc10codigo " + obterWherePadrao("Abc10"), Parametro.criar("abc10codigo", abc10codigo));

			txt1.print("0500");
			txt1.print(dtInicial.format(ddMMyyyy));
			
			switch (abc10.abc10ecdNat) {
				case 1:  txt1.print("01"); break;
				case 2:  txt1.print("02"); break;
				case 3:  txt1.print("03"); break;
				case 4:  txt1.print("04"); break;
				case 5:  txt1.print("04"); break;
				case 6:  txt1.print("05"); break;
				case 9:  txt1.print("09"); break;
				default: txt1.print(null); break;
			}
			
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
			txt1.newLine();
			
			qtLinBloco0++;
			qtLin0500++;
		}
		
		/**
		 * REGISTRO 0600: Centro de Custos
		 */
		for(Abb11 abb11 : abb11s) {
			verificarProcessoCancelado();
			enviarStatusProcesso("Gerando registro 0600");
			
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
		verificarProcessoCancelado();
		enviarStatusProcesso("Gerando bloco 9");
		
		int qtLinBloco9 = 0;
		int qtLin9900 = 0;
		
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
		gerarRegistro9900(txt2, "0000", 1); qtLin9900++;
		gerarRegistro9900(txt2, "0001", 1); qtLin9900++;
		if(qtLin0002 > 0)qtLin9900++; gerarRegistro9900(txt2, "0002", qtLin0002);
		gerarRegistro9900(txt2, "0005", 1); qtLin9900++;
		if(qtLin0015 > 0)qtLin9900++; gerarRegistro9900(txt2, "0015", qtLin0015);
		gerarRegistro9900(txt2, "0100", 1); qtLin9900++;
		if(qtLin0150 > 0)qtLin9900++; gerarRegistro9900(txt2, "0150", qtLin0150);
		if(qtLin0175 > 0)qtLin9900++; gerarRegistro9900(txt2, "0175", qtLin0175);
		if(qtLin0190 > 0)qtLin9900++; gerarRegistro9900(txt2, "0190", qtLin0190);
		if(qtLin0200 > 0)qtLin9900++; gerarRegistro9900(txt2, "0200", qtLin0200);
		if(qtLin0205 > 0)qtLin9900++; gerarRegistro9900(txt2, "0205", qtLin0205);
		if(qtLin0206 > 0)qtLin9900++; gerarRegistro9900(txt2, "0206", qtLin0206);
		if(qtLin0210 > 0)qtLin9900++; gerarRegistro9900(txt2, "0210", qtLin0210);
		if(qtLin0220 > 0)qtLin9900++; gerarRegistro9900(txt2, "0220", qtLin0220);
		if(qtLin0300 > 0)qtLin9900++; gerarRegistro9900(txt2, "0300", qtLin0300);
		if(qtLin0305 > 0)qtLin9900++; gerarRegistro9900(txt2, "0305", qtLin0305);
		if(qtLin0400 > 0)qtLin9900++; gerarRegistro9900(txt2, "0400", qtLin0400);
		if(qtLin0450 > 0)qtLin9900++; gerarRegistro9900(txt2, "0450", qtLin0450);
		if(qtLin0460 > 0)qtLin9900++; gerarRegistro9900(txt2, "0460", qtLin0460);
		if(qtLin0500 > 0)qtLin9900++; gerarRegistro9900(txt2, "0500", qtLin0500);
		if(qtLin0600 > 0)qtLin9900++; gerarRegistro9900(txt2, "0600", qtLin0600);
		gerarRegistro9900(txt2, "0990", 1); qtLin9900++;

		//BLOCO B
		gerarRegistro9900(txt2, "B001", 1); qtLin9900++;
		gerarRegistro9900(txt2, "B990", 1); qtLin9900++;
		
		//BLOCO C
		gerarRegistro9900(txt2, "C001", 1); qtLin9900++;
		
		//C100
		if(qtLinC100 > 0)qtLin9900++; gerarRegistro9900(txt2, "C100", qtLinC100);
		if(qtLinC101 > 0)qtLin9900++; gerarRegistro9900(txt2, "C101", qtLinC101);
		if(qtLinC105 > 0)qtLin9900++; gerarRegistro9900(txt2, "C105", qtLinC105);
		if(qtLinC110 > 0)qtLin9900++; gerarRegistro9900(txt2, "C110", qtLinC110);
		if(qtLinC111 > 0)qtLin9900++; gerarRegistro9900(txt2, "C111", qtLinC111);
		if(qtLinC112 > 0)qtLin9900++; gerarRegistro9900(txt2, "C112", qtLinC112);
		if(qtLinC113 > 0)qtLin9900++; gerarRegistro9900(txt2, "C113", qtLinC113);
		if(qtLinC114 > 0)qtLin9900++; gerarRegistro9900(txt2, "C114", qtLinC114);
		if(qtLinC115 > 0)qtLin9900++; gerarRegistro9900(txt2, "C115", qtLinC115);
		if(qtLinC120 > 0)qtLin9900++; gerarRegistro9900(txt2, "C120", qtLinC120);
		if(qtLinC130 > 0)qtLin9900++; gerarRegistro9900(txt2, "C130", qtLinC130);
		if(qtLinC140 > 0)qtLin9900++; gerarRegistro9900(txt2, "C140", qtLinC140);
		if(qtLinC141 > 0)qtLin9900++; gerarRegistro9900(txt2, "C141", qtLinC141);
		if(qtLinC160 > 0)qtLin9900++; gerarRegistro9900(txt2, "C160", qtLinC160);
		if(qtLinC170 > 0)qtLin9900++; gerarRegistro9900(txt2, "C170", qtLinC170);
		if(qtLinC171 > 0)qtLin9900++; gerarRegistro9900(txt2, "C171", qtLinC171);
		if(qtLinC172 > 0)qtLin9900++; gerarRegistro9900(txt2, "C172", qtLinC172);
		if(qtLinC173 > 0)qtLin9900++; gerarRegistro9900(txt2, "C173", qtLinC173);
		if(qtLinC174 > 0)qtLin9900++; gerarRegistro9900(txt2, "C174", qtLinC174);
		if(qtLinC175 > 0)qtLin9900++; gerarRegistro9900(txt2, "C175", qtLinC175);
		if(qtLinC177 > 0)qtLin9900++; gerarRegistro9900(txt2, "C177", qtLinC177);
		if(qtLinC178 > 0)qtLin9900++; gerarRegistro9900(txt2, "C178", qtLinC178);
		if(qtLinC179 > 0)qtLin9900++; gerarRegistro9900(txt2, "C179", qtLinC179);
		if(qtLinC190 > 0)qtLin9900++; gerarRegistro9900(txt2, "C190", qtLinC190);
		if(qtLinC191 > 0)qtLin9900++; gerarRegistro9900(txt2, "C191", qtLinC191);
		if(qtLinC195 > 0)qtLin9900++; gerarRegistro9900(txt2, "C195", qtLinC195);
		if(qtLinC197 > 0)qtLin9900++; gerarRegistro9900(txt2, "C197", qtLinC197);
		//C300
		if(qtLinC300 > 0)qtLin9900++; gerarRegistro9900(txt2, "C300", qtLinC300);
		if(qtLinC310 > 0)qtLin9900++; gerarRegistro9900(txt2, "C310", qtLinC310);
		if(qtLinC320 > 0)qtLin9900++; gerarRegistro9900(txt2, "C320", qtLinC320);
		if(qtLinC321 > 0)qtLin9900++; gerarRegistro9900(txt2, "C321", qtLinC321);
		if(qtLinC350 > 0)qtLin9900++; gerarRegistro9900(txt2, "C350", qtLinC350);
		if(qtLinC370 > 0)qtLin9900++; gerarRegistro9900(txt2, "C370", qtLinC370);
		if(qtLinC390 > 0)qtLin9900++; gerarRegistro9900(txt2, "C390", qtLinC390);
		//C500
		if(qtLinC500 > 0)qtLin9900++; gerarRegistro9900(txt2, "C500", qtLinC500);
		if(qtLinC590 > 0)qtLin9900++; gerarRegistro9900(txt2, "C590", qtLinC590);
		//C800
		if(qtLinC800 > 0)qtLin9900++; gerarRegistro9900(txt2, "C800", qtLinC800);
		if(qtLinC850 > 0)qtLin9900++; gerarRegistro9900(txt2, "C850", qtLinC850);
		if(qtLinC860 > 0)qtLin9900++; gerarRegistro9900(txt2, "C860", qtLinC860);
		if(qtLinC890 > 0)qtLin9900++; gerarRegistro9900(txt2, "C890", qtLinC890);
		gerarRegistro9900(txt2, "C990", 1); qtLin9900++;
		
		//BLOCO D
		gerarRegistro9900(txt2, "D001", 1); qtLin9900++;
		//D100
		if(qtLinD100 > 0)qtLin9900++; gerarRegistro9900(txt2, "D100", qtLinD100);
		if(qtLinD101 > 0)qtLin9900++; gerarRegistro9900(txt2, "D101", qtLinD101);
		if(qtLinD190 > 0)qtLin9900++; gerarRegistro9900(txt2, "D190", qtLinD190);
		if(qtLinD195 > 0)qtLin9900++; gerarRegistro9900(txt2, "D195", qtLinD195);
		if(qtLinD197 > 0)qtLin9900++; gerarRegistro9900(txt2, "D197", qtLinD197);
		//D500
		if(qtLinD500 > 0)qtLin9900++; gerarRegistro9900(txt2, "D500", qtLinD500);
		if(qtLinD590 > 0)qtLin9900++; gerarRegistro9900(txt2, "D590", qtLinD590);
		gerarRegistro9900(txt2, "D990", 1); qtLin9900++;
		
		//BLOCO E
		gerarRegistro9900(txt2, "E001", 1); qtLin9900++;
		if(qtLinE100 > 0)qtLin9900++; gerarRegistro9900(txt2, "E100", qtLinE100);
		if(qtLinE110 > 0)qtLin9900++; gerarRegistro9900(txt2, "E110", qtLinE110);
		if(qtLinE111 > 0)qtLin9900++; gerarRegistro9900(txt2, "E111", qtLinE111);
		if(qtLinE112 > 0)qtLin9900++; gerarRegistro9900(txt2, "E112", qtLinE112);
		if(qtLinE113 > 0)qtLin9900++; gerarRegistro9900(txt2, "E113", qtLinE113);
		if(qtLinE115 > 0)qtLin9900++; gerarRegistro9900(txt2, "E115", qtLinE115);
		if(qtLinE116 > 0)qtLin9900++; gerarRegistro9900(txt2, "E116", qtLinE116);
		if(qtLinE200 > 0)qtLin9900++; gerarRegistro9900(txt2, "E200", qtLinE200);
		if(qtLinE210 > 0)qtLin9900++; gerarRegistro9900(txt2, "E210", qtLinE210);
		if(qtLinE220 > 0)qtLin9900++; gerarRegistro9900(txt2, "E220", qtLinE220);
		if(qtLinE230 > 0)qtLin9900++; gerarRegistro9900(txt2, "E230", qtLinE230);
		if(qtLinE240 > 0)qtLin9900++; gerarRegistro9900(txt2, "E240", qtLinE240);
		if(qtLinE250 > 0)qtLin9900++; gerarRegistro9900(txt2, "E250", qtLinE250);
		if(qtLinE300 > 0)qtLin9900++; gerarRegistro9900(txt2, "E300", qtLinE300);
		if(qtLinE310 > 0)qtLin9900++; gerarRegistro9900(txt2, "E310", qtLinE310);
		if(qtLinE311 > 0)qtLin9900++; gerarRegistro9900(txt2, "E311", qtLinE311);
		if(qtLinE312 > 0)qtLin9900++; gerarRegistro9900(txt2, "E312", qtLinE312);
		if(qtLinE313 > 0)qtLin9900++; gerarRegistro9900(txt2, "E313", qtLinE313);
		if(qtLinE316 > 0)qtLin9900++; gerarRegistro9900(txt2, "E316", qtLinE316);
		if(qtLinE500 > 0)qtLin9900++; gerarRegistro9900(txt2, "E500", qtLinE500);
		if(qtLinE510 > 0)qtLin9900++; gerarRegistro9900(txt2, "E510", qtLinE510);
		if(qtLinE520 > 0)qtLin9900++; gerarRegistro9900(txt2, "E520", qtLinE520);
		if(qtLinE530 > 0)qtLin9900++; gerarRegistro9900(txt2, "E530", qtLinE530);
		if(qtLinE531 > 0)qtLin9900++; gerarRegistro9900(txt2, "E531", qtLinE531);
		gerarRegistro9900(txt2, "E990", 1); qtLin9900++;
		
		//BLOCO G
		gerarRegistro9900(txt2, "G001", 1); qtLin9900++;
		if(qtLinG110 > 0)qtLin9900++; gerarRegistro9900(txt2, "G110", qtLinG110);
		if(qtLinG125 > 0)qtLin9900++; gerarRegistro9900(txt2, "G125", qtLinG125);
		if(qtLinG126 > 0)qtLin9900++; gerarRegistro9900(txt2, "G126", qtLinG126);
		if(qtLinG130 > 0)qtLin9900++; gerarRegistro9900(txt2, "G130", qtLinG130);
		if(qtLinG140 > 0)qtLin9900++; gerarRegistro9900(txt2, "G140", qtLinG140);
		gerarRegistro9900(txt2, "G990", 1); qtLin9900++;
		
		//BLOCO H
		gerarRegistro9900(txt2, "H001", 1); qtLin9900++;
		if(qtLinH005 > 0)qtLin9900++; gerarRegistro9900(txt2, "H005", qtLinH005);
		if(qtLinH010 > 0)qtLin9900++; gerarRegistro9900(txt2, "H010", qtLinH010);
		if(qtLinH020 > 0)qtLin9900++; gerarRegistro9900(txt2, "H020", qtLinH020);
		gerarRegistro9900(txt2, "H990", 1); qtLin9900++;
		
		//BLOCO K
		gerarRegistro9900(txt2, "K001", 1); qtLin9900++;
		if(qtLinK100 > 0)qtLin9900++; gerarRegistro9900(txt2, "K100", qtLinK100);
		if(qtLinK200 > 0)qtLin9900++; gerarRegistro9900(txt2, "K200", qtLinK200);
		if(qtLinK210 > 0)qtLin9900++; gerarRegistro9900(txt2, "K210", qtLinK210);
		if(qtLinK215 > 0)qtLin9900++; gerarRegistro9900(txt2, "K215", qtLinK215);
		if(qtLinK220 > 0)qtLin9900++; gerarRegistro9900(txt2, "K220", qtLinK220);
		if(qtLinK230 > 0)qtLin9900++; gerarRegistro9900(txt2, "K230", qtLinK230);
		if(qtLinK235 > 0)qtLin9900++; gerarRegistro9900(txt2, "K235", qtLinK235);
		if(qtLinK250 > 0)qtLin9900++; gerarRegistro9900(txt2, "K250", qtLinK250);
		if(qtLinK255 > 0)qtLin9900++; gerarRegistro9900(txt2, "K255", qtLinK255);
		if(qtLinK280 > 0)qtLin9900++; gerarRegistro9900(txt2, "K280", qtLinK280);
		gerarRegistro9900(txt2, "K990", 1); qtLin9900++;

		//BLOCO 1
		gerarRegistro9900(txt2, "1001", 1); qtLin9900++;
		if(qtLin1010 > 0)qtLin9900++; gerarRegistro9900(txt2, "1010", qtLin1010);
		if(qtLin1100 > 0)qtLin9900++; gerarRegistro9900(txt2, "1100", qtLin1100);
		if(qtLin1105 > 0)qtLin9900++; gerarRegistro9900(txt2, "1105", qtLin1105);
		if(qtLin1110 > 0)qtLin9900++; gerarRegistro9900(txt2, "1110", qtLin1110);
		if(qtLin1400 > 0)qtLin9900++; gerarRegistro9900(txt2, "1400", qtLin1400);
		if(qtLin1900 > 0)qtLin9900++; gerarRegistro9900(txt2, "1900", qtLin1900);
		if(qtLin1910 > 0)qtLin9900++; gerarRegistro9900(txt2, "1910", qtLin1910);
		if(qtLin1920 > 0)qtLin9900++; gerarRegistro9900(txt2, "1920", qtLin1920);
		if(qtLin1921 > 0)qtLin9900++; gerarRegistro9900(txt2, "1921", qtLin1921);
		if(qtLin1922 > 0)qtLin9900++; gerarRegistro9900(txt2, "1922", qtLin1922);
		if(qtLin1923 > 0)qtLin9900++; gerarRegistro9900(txt2, "1923", qtLin1923);
		if(qtLin1925 > 0)qtLin9900++; gerarRegistro9900(txt2, "1925", qtLin1925);
		if(qtLin1926 > 0)qtLin9900++; gerarRegistro9900(txt2, "1926", qtLin1926);
		if(qtLin1960 > 0)qtLin9900++; gerarRegistro9900(txt2, "1960", qtLin1960);
		gerarRegistro9900(txt2, "1990", 1); qtLin9900++;
		
		//BLOCO 9
		gerarRegistro9900(txt2, "9001", 1); qtLin9900++;
		if(qtLin9900 > 0)qtLin9900++; gerarRegistro9900(txt2, "9900", qtLin9900 + 2); //2= 9990 e 9999
		gerarRegistro9900(txt2, "9990", 1); qtLin9900++;
		gerarRegistro9900(txt2, "9999", 1); qtLin9900++;
		
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
		txt2.print(qtLinBloco0 + qtLinBlocoB + qtLinBlocoC + qtLinBlocoD + qtLinBlocoE + qtLinBlocoG + qtLinBlocoH + qtLinBlocoK + qtLinBloco1 + qtLinBloco9);
		txt2.newLine();
	}
	
	private String definirTipoMov(Ecc01 ecc01, Ecc0101 ecc0101){
		Abb20 abb20 = getSession().get(Abb20.class, ecc01.ecc01bem.abb20id);
		Ecb01 ecb01 = getSession().get(Ecb01.class, Criterions.eq("ecb01bem", abb20.abb20id));
		
		def tipo = null;
		
		if(ecc0101 == null && abb20.abb20baixa != null) { 															//Significa que o bem não tem mais ciap, foi baixado
			if(abb20.abb20clasBx == 0){																				//AT – Alienação ou Transferência: quando o bem estiver baixado e a data da baixa estiver contida no período informado na tela e a classificação da baixa for igual a 0
				tipo = "AT";
			}else if(abb20.abb20clasBx == 1){																		//PE – Perecimento, Extravio ou Deterioração: quando o bem estiver baixado e a data da baixa estiver contida no período informado na tela e a classificação da baixa for igual a 1
				tipo = "PE";
			}else {
				tipo = "OT";																						//OT – Outras Saídas do Imobilizado: quando o bem estiver baixado e a data da baixa estiver contida no período informado na tela e a classificação da baixa for igual a 2
			}
			
		}else {
			def ecc0101s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Ecc0101 WHERE ecc0101ficha = :ecc01id", Parametro.criar("ecc01id", ecc01.ecc01id));
			
			if(ecc0101.ecc0101parcela > 1) { 																		//SI – Saldo Inicial: quando a parcela for maior que 1
				tipo = "SI";
				
			}else if(ecc0101.ecc0101parcela == 1) {
				tipo = "IM";	 																					//IM – Imobilização: quando a parcela for igual a 1
				if(abb20.abb20construcao == 1 && !abb20.abb20codigo.substring(7).equals("000")) tipo = "IA";		//IA – Imobilização em Andamento: quando a parcela for igual a 1 e for bem em construção
				if(ecb01 != null && ecb01.ecb01ativoCirc == 1) tipo = "MC";											//MC – Imobilização de Ativo Circulante: quando a parcela for igual a 1 e a origem do bem for do ativo circulante
			
			}else if(ecc0101.ecc0101parcela == ecc0101s.size()) {													//BA – Baixa (fim da apropriação): quando for a última parcela
				tipo = "BA";
				
			}
		}

		//CI – Conclusão de Imobilização em Andamento: não implementado no SAM. No processo de agrupamento as fichas CIAP continuam vinculadas aos componentes que deram origem ao bem construído.
		
		return tipo;
	}
	
	private void gerarRegistro9900(TextFile txt, String registro, int qtdLinhas) {
		if(qtdLinhas > 0) {
			txt.print("9900");
			txt.print(registro);
			txt.print(qtdLinhas);
			txt.newLine();
		}
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
	
	//Havendo uma entidade com mais de uma IE será gerado o código + IE pois a entidade deve se repetir no registro 0150
	private String gerarCodigoEntidade(Abe01 abe01, String ie) {
		String codigo = abe01.abe01codigo;

		boolean temCodigo = false;
		boolean temIE = false;
		for(TableMap ent : abe01s) {
			if(ent.getString("codigo").equals(abe01.abe01codigo)){
				temCodigo = true;
				
				if(ent.getString("ie").equals(ie)) {
					 temIE = true;
				}
			}
		}
		
		if(!temIE) {
			if(temCodigo)codigo = codigo + "_" + ie;
			
			TableMap tm = new TableMap();
			tm.put("codigo", abe01.abe01codigo);
			tm.put("ie", ie);
			entidades.add(tm);
			
			entidadesPara0150(codigo, ie, abe01);
		}
		
		return codigo;
	}
	
	private void entidadesPara0150(String codigo, String ie, Abe01 abe01) {
		boolean naoTemAEntidade = true;
		
		for(TableMap ent : abe01s) {
			if(ent.getString("codigo").equals(codigo) && ent.getString("ie").equals(ie)){
				naoTemAEntidade = false;
			}
		}
		
		if(naoTemAEntidade) {
			//Map para o registro 0150
			TableMap map0150 = new TableMap();
			map0150.put("codigo", codigo);
			map0150.put("ie", ie);
			map0150.put("abe01", abe01);
			abe01s.add(map0150);
		}
	}
	
	private Abe0101 selecionarEnderecoPorIE(Abe01 abe01, String ie) {
		Abe0101 abe0101 = null;
		
		if(!ie.equals(abe01.abe01ie)) {
			abe0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", abe01.abe01id), Criterions.eq("abe0101ie", ie));
		}
		
		if(abe0101 == null) abe0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", abe01.abe01id), Criterions.eq("abe0101principal", Abe01.SIM));a
		
		return abe0101;
	}
	
	private boolean contemDadosBlocoC() {
		def c100 = buscarDocumentosPorModelo(modelosC100, 0);
		if(c100 != null && c100.size() > 0) return true;
	
		def c300 = buscarDocumentosPorModelo(modelosC300, 0)
		if(c300 != null && c300.size() > 0) return true;
	
		def c500 = buscarDocumentosPorMovimentoModelo(0, 0, modelosC500, 0);
		if(c500 != null && c500.size() > 0) return true;
	
		def c800 = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, 0);
		if(c800 != null && c800.size() > 0) return true;

		def c860 = buscarDocumentosPorMovimentoModelo(1, 0, modelosC800, 0)
		if(c860 != null && c860.size() > 0) return true;
		
		return false;
	}
	
	private boolean contemDadosBlocoD() {
		def d100 = buscarDocumentosPorModelo(modelosD100, 0);
		if(d100 != null && d100.size() > 0) return true;
				
		def d500 = buscarDocumentosPorModelo(modelosD500, 0);
		if(d500 != null && d500.size() > 0) return true;
		
		return false;
	}
	
	private void validacoes(List<Eaa01> eaa01s) {
		for(Eaa01 eaa01 : eaa01s) {
			Eaa0102 eaa0102 = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
			if(abb01.abb01ent == null) throw new ValidacaoException("A entidade do documento não foi informada. Documento de " + (eaa01.eaa01esMov == 0 ? "entrada" : "saída") + ": " + abb01.abb01num);
			
			if(eaa01.eaa01sitDoc == null) throw new ValidacaoException("A situação do documento não foi informada. Documento de " + (eaa01.eaa01esMov == 0 ? "entrada" : "saída") + ": " + abb01.abb01num);

			def abe0101 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe0101 WHERE abe0101ent = :ent AND abe0101principal = 1", Parametro.criar("ent", abb01.abb01ent.abe01id));
			if(abe0101 == null) throw new ValidacaoException("O documento de " + (eaa01.eaa01esMov == 0 ? "entrada" : "saída") + " de número " + abb01.abb01num + " não contém o endereço da entidade.");
		}
	}
	
	def gerarRegByPerfil(String perfil, String registro, int operacao) {
		Map<String, TableMap> mapRegistros = perfil.equals("A") ? comporPerfilA() : perfil.equals("B") ? comporPerfilB() : comporPerfilC();
		return mapRegistros.get(registro).getBoolean(operacao == 0 ? "E" : "S");
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
		if(palavra == null) return "";
		palavra = palavra.replaceAll("[.]", "");
		palavra = palavra.replaceAll("[/]", "");
		palavra = palavra.replaceAll("[-]", "");
		return palavra;
	}
	
	private String formatarValor(BigDecimal valor, int casasDecimais) {
		if(valor == null) return null;
		
		valor = valor.round(casasDecimais);
		
		def format = NumberFormat.getInstance(new Locale("pt", "BR"));
		format.setGroupingUsed(false);
		format.setMinimumFractionDigits(casasDecimais);
		format.setMaximumFractionDigits(casasDecimais);
		
		return format.format(valor);
	}
	
	def verificarTipoPgto(Eaa01 eaa01, Abb01 abb01){
		def eaa0113s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0113 WHERE eaa0113doc = :eaa0113doc", Parametro.criar("eaa0113doc", eaa01.eaa01id));
		
		if(eaa0113s == null || eaa0113s.size() == 0)return 2; //Outros
			
		if(eaa0113s.size() > 1) return 1; //A prazo

		for(Eaa0113 eaa0113 : eaa0113s){
			long dif = DateUtils.dateDiff(abb01.abb01data, eaa0113.eaa0113dtVctoN, ChronoUnit.DAYS);
			if(dif <= 0) return 0; //A vista
			return 1; //A prazo
		}
		
		return 2; //Outros
	}
	
	List<TableMap> comporRegistrosC110eFilhos(Boolean isNFePropria, Eaa01 eaa01, Eaa0102 eaa0102, Aac10 aac10, TableMap map0450, String perfil) {
		/**
		 * Registro C110: Informação complementar
		 */
		def mapRegistroC110 = new ArrayList<TableMap>();

		if(eaa01.eaa01obsFisco != null) comporRegistroC110(mapRegistroC110, eaa01.eaa01obsFisco, map0450);

		/**
		 * Registro C111: Processo Referenciado
		 */
		if(gerarRegByPerfil(perfil, "C111", eaa01.eaa01esMov)) {
			def mapRegistroC111 = new ArrayList<TableMap>();
			
			List<Abb40> abb40s = eaa0102.eaa0102processo == null ? null : getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abb40 WHERE abb40id = :abb40id", Parametro.criar("abb40id", eaa0102.eaa0102processo.abb40id));
			if(abb40s != null && abb40s.size() > 0) {
				int linhaC111 = 0;
				for(Abb40 abb40 : abb40s) {
					def tm = new TableMap();
					tm.put("reg", "C111");
					tm.put("num_proc", abb40.abb40num);
					tm.put("ind_proc", abb40.abb40indProc);
					mapRegistroC111.add(tm);
				}
	
				if(eaa01.eaa01obsFisco == null) comporRegistroC110(mapRegistroC110, "Documento com processo referenciado", map0450);
				def tm = new TableMap();
				tm.put("mapRegistroC111", mapRegistroC111);
				mapRegistroC110.add(tm);
			}
		}

		/**
		 * Registro C112: Documento de Arrecadação Referenciado
		 */
		if(gerarRegByPerfil(perfil, "C112", eaa01.eaa01esMov)) {
			def mapRegistroC112 = new ArrayList<TableMap>();
			
			def eaa0105s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Eaa0105 AS eaa0105 INNER JOIN FETCH eaa0105.eaa0105uf AS aag02 WHERE eaa0105doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
			if(eaa0105s != null && eaa0105s.size() > 0) {
				int linhaC112 = 0;
				for(Eaa0105 eaa0105 : eaa0105s) {
					def tm = new TableMap();
					tm.put("reg", "C112");
					tm.put("cod_da", eaa0105.eaa0105tipo);
					tm.put("uf", eaa0105.eaa0105uf.aag02uf);
					tm.put("num_da", eaa0105.eaa0105num);
					tm.put("cod_aut", eaa0105.eaa0105autBanc);
					tm.put("vl_da", formatarValor(eaa0105.eaa0105valor, 2));
					tm.put("dt_vcto", eaa0105.eaa0105dtVcto.format(ddMMyyyy));
					tm.put("dt_pgto", eaa0105.eaa0105dtPgto.format(ddMMyyyy));
				}
	
				if(eaa01.eaa01obsFisco == null) comporRegistroC110(mapRegistroC110, "Documento com arrecadação referenciado", map0450);
				def tm = new TableMap();
				tm.put("mapRegistroC112", mapRegistroC112)
				mapRegistroC110.add(tm);
			}
		}

		/**
		 * Registro C113: Documento Fiscal Referenciado
		 * Registro C114: Cupom Fiscal Referenciado
		 */
		def mapRegistroC113 = new ArrayList<TableMap>();
		def mapRegistroC114 = new ArrayList<TableMap>();
		def docsRef = buscarDocumentosReferenciadosPorModelo(eaa01.eaa01id, "55");
		if((gerarRegByPerfil(perfil, "C113", eaa01.eaa01esMov) || gerarRegByPerfil(perfil, "C114", eaa01.eaa01esMov)) && (docsRef != null && docsRef.size() > 0)) {
			for(Long eaa01id : docsRef) {
				Eaa01 doc = getSession().get(Eaa01.class, eaa01id);
				Abb01 centDoc = getSession().get(Abb01.class, doc.eaa01central.abb01id);
				Aah01 tipoDoc = getSession().get(Aah01.class, centDoc.abb01tipo.aah01id);
				Abe01 entiDoc = getSession().get(Abe01.class, centDoc.abb01ent.abe01id);
				Eaa0102 eaa0102Doc = getSession().get(Eaa0102.class, Criterions.eq("eaa0102doc", eaa01.eaa01id));
				
				Abd10 abd10 = eaa01.eaa01cfEF != null ? getSession().get(Abd10.class, eaa01.eaa01cfEF.abd10id) : null;
				if(abd10 != null) {	//Documento Fiscal Referenciado
					def tm = new TableMap();
					tm.put("reg", "C113");
					tm.put("ind_oper", doc.eaa01esMov);
					tm.put("ind_emit", doc.eaa01emissao == Eaa01.SIM ? 0 : 1);
					tm.put("cod_part", gerarCodigoEntidade(entiDoc, eaa0102Doc.eaa0102ie));
					tm.put("cod_mod", tipoDoc.aah01modelo);
					tm.put("ser", formatarSerie(centDoc.abb01serie, tipoDoc.aah01modelo));
					tm.put("num_doc", centDoc.abb01num);
					tm.put("dt_doc", centDoc.abb01data.format(ddMMyyyy));
					tm.put("chv_doce", doc.eaa01nfeChave);
					
					mapRegistroC113.add(tm);

				}else {										//Cupom Fiscal Referenciado
					if(eaa01.eaa01esMov == 1 || (eaa01.eaa01esMov == 0 && Utils.jsBoolean(eaa01.eaa01emissao))) {
						def docModelo = tipoDoc.aah01modelo;
						if(docModelo.equals("02") || docModelo.equals("2D") || docModelo.equals("2E")) {
							def tm = new TableMap();
							tm.put("reg", "C114");
							tm.put("cod_mod", docModelo)
							tm.put("ecf_fab", retirarMascara(abd10.abd10serieFabr));
							tm.put("ecf_cx", retirarMascara(abd10.abd10caixa));
							tm.put("num_doc", centDoc.abb01num);
							tm.put("dt_doc", centDoc.abb01data.format(ddMMyyyy));
							mapRegistroC114.add(tm);
						}
					}
				}
			}

			//Observações
			if(eaa01.eaa01obsFisco == null && (mapRegistroC113 != null && mapRegistroC113.size() > 0)) comporRegistroC110(mapRegistroC110, "Documento com documento fiscal referenciado", map0450);
			if(mapRegistroC113 != null && mapRegistroC113.size() > 0) {
				def tm = new TableMap();
				tm.put("mapRegistroC113", mapRegistroC113);
				mapRegistroC110.add(tm);
			}
			
			if(eaa01.eaa01obsFisco == null && (mapRegistroC114 != null && mapRegistroC114.size() > 0)) comporRegistroC110(mapRegistroC110, "Documento com cupom fiscal referenciado", map0450);
			if(mapRegistroC114 != null && mapRegistroC114.size() > 0) {
				def tm = new TableMap();
				tm.put("mapRegistroC114", mapRegistroC114)
				mapRegistroC110.add(tm);
			}
		}
		
		
		/**
		 * Registro C115: Local da Coleta e Entrega (Código 01, 1B e 04)
		 * Documentos de Saída com modelo 01, 1B e 04 e
		 * endereço de entrega seja diferente do endereço do destinatário ou endereço de saida seja diferente do endereço do emitente
		 */
		if(gerarRegByPerfil(perfil, "C115", eaa01.eaa01esMov)) {
			def mapRegistroC115 = new ArrayList<TableMap>();
			
			Abb01 abb01 = getSession().get(Abb01.class, eaa01.eaa01central.abb01id);
			Aah01 aah01 = getSession().get(Aah01.class, abb01.abb01tipo.aah01id);
			Eaa0101 endPrinc = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eaa0101 WHERE eaa0101principal = 1 AND eaa0101doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
			Eaa0101 endEntre = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Eaa0101 WHERE eaa0101entrega = 1 AND eaa0101doc = :eaa01id", Parametro.criar("eaa01id", eaa01.eaa01id));
			
			def codIbgePrinc = endPrinc == null ? null : endPrinc.eaa0101municipio == null ? null : getAcessoAoBanco().obterString("SELECT aag0201ibge FROM Aag0201 WHERE aag0201id = :aag0201id", Parametro.criar("aag0201id", endPrinc.eaa0101municipio.aag0201id));
			def codIbgeEntre = endEntre == null ? null : endEntre.eaa0101municipio == null ? null : getAcessoAoBanco().obterString("SELECT aag0201ibge FROM Aag0201 WHERE aag0201id = :aag0201id", Parametro.criar("aag0201id", endEntre.eaa0101municipio.aag0201id));
			
			if(eaa01.eaa01esMov == 1 && !aah01.aah01modelo.equals("55") && ((endEntre != null && !endEntre.eaa0101endereco.equals(endPrinc.eaa0101endereco)) || (endPrinc != null && !endPrinc.eaa0101endereco.equals(aac10.aac10endereco)))) {
				def tm = new TableMap();
				tm.put("reg", "C115");
				tm.put("ind_carga", eaa0102.eaa0102modFrete);
				tm.put("cnpj_col", eaa0102.eaa0102ni == null ? null : eaa0102.eaa0102ti == 0 ? StringUtils.ajustString(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14, '0', true) : null);
				tm.put("ie_col", inscrEstadual(eaa0102.eaa0102ie));
				tm.put("cpf_col", eaa0102.eaa0102ni == null ? null : eaa0102.eaa0102ti == 1 ? StringUtils.ajustString(StringUtils.extractNumbers(eaa0102.eaa0102ni), 11, '0', true) : null);
				tm.put("cod_mun_col", codIbgePrinc);
				tm.put("cnpj_entg", endEntre.eaa0101ni == null ? null : endEntre.eaa0101ti == 0 ? StringUtils.ajustString(StringUtils.extractNumbers(endEntre.eaa0101ni), 14, '0', true) : null);
				tm.put("ie_entg", inscrEstadual(endEntre.eaa0101ie));
				tm.put("cpf_entg", endEntre.eaa0101ni == null ? null : endEntre.eaa0101ti == 1 ? StringUtils.ajustString(StringUtils.extractNumbers(endEntre.eaa0101ni), 11, '0', true) : null);
				tm.put("cod_mun_entg", codIbgeEntre);
				mapRegistroC115.add(tm);
				
				comporRegistroC110(mapRegistroC110, "Documento com local de coleta/entrega", map0450);
				def tm2 = new TableMap();
				tm2.put("mapRegistroC115", mapRegistroC115);
				mapRegistroC110.add(tm2);
			}
		}

		return mapRegistroC110;
	}
	
	def comporRegistroC110(List<TableMap> mapRegistroC110, String texto, Map<String, String> map0450) {
		if(texto != null) {
			texto = texto.replace('|', '-');
			
			while(texto.length() > 0) {
				def codigo = ""+(map0450.size()+1);
				
				def descricao = "";
				if(texto.length() > 255) {
					descricao = texto.substring(0, 255);
					texto = texto.substring(255);
				}else {
					descricao = texto;
					texto = "";
				}
				map0450.put(codigo, descricao.trim());
				
				def descrCompl = "";
				if(texto.length() > 255) {
					descrCompl = texto.substring(0, 255);
					texto = texto.substring(255);
				}else {
					descrCompl = texto;
					texto = "";
				}
				
				def linha = mapRegistroC110.size();
				def tm = new TableMap();
				tm.put("reg", "C110");
				tm.put("cod_inf", codigo);
				tm.put("txt_compl", descrCompl.trim());
				mapRegistroC110.add(tm);
			}
		}
	}
	
	def comporRegistro0200(Abm01 abm01) {
		if(abm01 == null)return;
		set0200.add(abm01.abm01id);
		if(abm01.abm01umu != null)set0190.add(abm01.abm01umu.aam06id); //adiciona a umu pois usa no registro 0200
	}
	
	def comporRegistro0220(Abm01 abm01, Aam06 aam06, boolean isSaida) {
		if(abm01 == null) return;
		if(aam06 == null) return;
		if(abm01.abm01umu == null) return;

		if(abm01.abm01umu.aam06id == aam06.aam06id) return;
		
		def Map<Long, BigDecimal> mapUnid = new HashMap<Long, BigDecimal>();

		Abm0101 abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;
		Abm13 abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;
		if(abm13 != null) {
			if(isSaida) {
				if(abm13 != null) {
					mapUnid.put(aam06.aam06id, abm13.abm13fcUV_Zero);
				}
			}else {
				List<Abm1301> abm1301s = getAcessoAoBanco().buscarListaDeRegistros("SELECT * FROM Abm1301 WHERE abm1301cc = :abm13id", Parametro.criar("abm13id", abm13.abm13id));
				if(abm1301s != null && abm1301s.size() > 0) {
					for(Abm1301 abm1301 : abm1301s) {
						if(abm1301.abm1301umc.aam06id == aam06.aam06id) {
							Aam06 aam06UMC = getSession().get(Aam06.class, abm1301.abm1301umc.aam06id);
							mapUnid.put(aam06.aam06id, abm1301.abm1301fcCU);
						}
					}
				}
			}
		}

		map0220.put(abm01.abm01id, mapUnid);
	}
	
	List<Eaa01> buscarDocumentosPorModelo(List<String> modelos, Integer pagina) {
		def sql = " SELECT * FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) " +
				  " OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND aah01modelo IN (:modelos) " +
				  " AND eaa01iEfdIcms = 1 " + obterWherePadrao("eaa01");
				  
		return getAcessoAoBanco().buscarListaDeRegistros(sql, true, pagina, Parametro.criar("dtInicial", dtInicial), 
			                                                                Parametro.criar("dtFinal", dtFinal), 
																			Parametro.criar("modelos", modelos));
	}
	
	List<Eaa01> buscarDocumentosPorMovimentoModelo(Integer mov, Integer cancelados, List<String> modelos, Integer pagina) {
		def coluna = mov == 0 ? "eaa01esData" : "abb01data";
		def canc = cancelados == 0 ? "" : cancelados == 1 ? " AND eaa01cancData IS NULL " : " AND eaa01cancData IS NOT NULL ";
		
		def sql = " SELECT * FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " WHERE eaa01esMov = :mov AND eaa01iEfdIcms = 1 " +
				  " AND "+coluna+" BETWEEN :dtInicial AND :dtFinal " +
				    canc + " AND aah01modelo IN (:modelos) " + 
					obterWherePadrao("eaa01");
				  
		return getAcessoAoBanco().buscarListaDeRegistros(sql, true, pagina, Parametro.criar("mov", mov), 
			                                                                Parametro.criar("modelos", modelos), 
																			Parametro.criar("dtInicial", dtInicial), 
																			Parametro.criar("dtFinal", dtFinal));
	}
	
	List<Eaa01> buscarDocumentosRegistro1100() {
		def sql = " SELECT * FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
			 	  " INNER JOIN Eaa0104 ON eaa0104doc = eaa01id " +
				  " WHERE eaa0104dtAverb BETWEEN :dtInicial AND :dtFinal " +
				    obterWherePadrao("Eaa01");
				   
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                  Parametro.criar("dtFinal", dtFinal));
	}
	
	List<TableMap> buscarDocumentosRegistro1400(String cpoVlr) {
		def sql = " SELECT eaa0102codIPM, aag0201ibge, SUM(jGet(eaa01json." + cpoVlr + ")::numeric) as valor " +
				  " FROM Eaa01 " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
				  " INNER JOIN Eaa0102 ON eaa0102doc = eaa01id " +
				  " LEFT JOIN Aag0201 ON aag0201id = eaa0101municipio " +
				  " WHERE ((eaa01esMov = 0 AND eaa01esData BETWEEN :dtInicial AND :dtFinal) " +
				  " OR (eaa01esMov = 1 AND abb01data BETWEEN :dtInicial AND :dtFinal)) " +
				  " AND jGet(eaa01json." + cpoVlr + ")::numeric > 0 " +
				  " AND eaa0102codIPM IS NOT NULL " +
				  " AND eaa01cancData IS NULL " +
				    obterWherePadrao("Eaa01") +
				  " GROUP BY eaa0102codIPM, aag0201ibge " +
				  " ORDER BY eaa0102codIPM, aag0201ibge";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("dtInicial", dtInicial), 
			                                                 Parametro.criar("dtFinal", dtFinal));
	}
	
	String buscarUfEnderecoEntrega(Long eaa01id) {
		def sql = " SELECT aag02uf FROM Aag02 " +
				  " INNER JOIN Aag0201 ON aag0201uf = aag02id " +
				  " INNER JOIN Eaa0101 ON eaa0101municipio = aag0201id " +
				  " WHERE eaa0101principal = 1 " +
				  " AND eaa0101doc = :eaa01id ";
				  
		return getAcessoAoBanco().obterString(sql, Parametro.criar("eaa01id", eaa01id))
	}
	
	List<Long> buscarDocumentosReferenciadosPorModelo(Long eaa01id, String modelo) {
		return getSession().createQuery(" SELECT DISTINCT eaa01id" +
										" FROM Eaa01033" +
										" INNER JOIN Eaa0103 eaa0103Ref ON eaa01033itemDoc = eaa0103Ref.eaa0103id" +
										" INNER JOIN Eaa01 ON eaa0103Ref.eaa0103doc = eaa01id" +
										" INNER JOIN Abb01 ON abb01id = eaa01central" +
										" INNER JOIN Aah01 ON aah01id = abb01tipo" +
										" INNER JOIN Eaa0103 eaa0103item ON eaa01033item = eaa0103item.eaa0103id" +
										" WHERE eaa0103item.eaa0103doc = :eaa01id AND aah01modelo = :modelo")
						   .setParameter("eaa01id", eaa01id)
						   .setParameter("modelo", modelo)
						   .getList(ColumnType.LONG);
	}
	
	List<Eaa01034> buscarDeclaracoesDeImportacao(Long eaa01id) {
		return getSession().createQuery(" SELECT * FROM Eaa01034 " +
										" INNER JOIN Eaa0103 ON eaa0103id = eaa01034item " +
										" INNER JOIN Eaa01 ON eaa01id = :eaa01id")
						   .setParameter("eaa01id", eaa01id)
						   .getList(ColumnType.ENTITY);
	}
	
	List<TableMap> buscarResumoValoresC190(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2, String cpoVlr3, String cpoVlr4, String cpoVlr5, String cpoVlr6) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) As " + cpoVlr3);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr4 + ")::numeric) As " + cpoVlr4);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr5 + ")::numeric) As " + cpoVlr5);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr6 + ")::numeric) As " + cpoVlr6);

		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) as eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " +
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<Eaa01031> buscarLancamentosFiscaisDocumento(Long eaa01id) {
		def sql = " SELECT * FROM Eaa01031 " +
				  " INNER JOIN Eaa0103 ON eaa0103id = eaa01031item " +
				  " WHERE eaa0103doc = :eaa01id";
				  
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<TableMap> buscarDocumentosEFDRegistroC320(List<String> modelo, LocalDate dtEmis, Integer numI, Integer numF, String aliqIcms, String vl_bc_icms, String vl_icms, String vl_red_bc) {
		StringBuilder select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + aliqIcms + ")::numeric as " + aliqIcms);
		select.append(", jGet(eaa0103json." + vl_bc_icms + ")::numeric as " + vl_bc_icms);
		select.append(", jGet(eaa0103json." + vl_icms + ")::numeric as " + vl_icms);
		select.append(", jGet(eaa0103json." + vl_red_bc + ")::numeric as " + vl_red_bc);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, eaa0103id, eaa0103totDoc " + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Eaa01 ON eaa0103doc = eaa01id " +
				  " INNER JOIN Abb01 ON abb01id = eaa01central " +
				  " INNER JOIN Aah01 ON aah01id = abb01tipo " +
				  " LEFT JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " INNER JOIN Abm01 ON abm01id = eaa0103item " +
				  " WHERE eaa01esMov = :oper AND eaa01cancData IS NULL " +
				  " AND abb01data = :dtEmis " +
				  " AND abb01num BETWEEN :numI AND :numF " +
				  " AND aah01modelo IN (:modelo) " + obterWherePadrao("Eaa01");
					 
		Query query = getSession().createQuery(sql);
		query.setParameter("dtEmis", dtEmis);
		query.setParameter("oper", 1);
		query.setParameter("numI", numI);
		query.setParameter("numF", numF);
		
		return query.getListTableMap();
	}
	
	List<TableMap> buscarDocumentosEFDRegistroC321(Set<Long> eaa0103ids, String vl_desc, String vl_bc_icms, String vl_icms, String vl_pis, String vl_cofins) {
		def select = new StringBuilder("");
		select.append(", jGet(eaa0103json." + vl_desc + ")::numeric");
		select.append(", jGet(eaa0103json." + vl_bc_icms + ")::numeric");
		select.append(", jGet(eaa0103json." + vl_icms + ")::numeric");
		select.append(", jGet(eaa0103json." + vl_pis + ")::numeric");
		select.append(", jGet(eaa0103json." + vl_cofins + ")::numeric");
		
		def sql = " SELECT abm01id, abm01tipo, abm01codigo, aam06id, aam06codigo, eaa0103qtComl, eaa0103totDoc " + select.toString() +
				  " FROM Eaa0103 INNER JOIN Eaa01 ON Eaa0103doc = eaa01id " +
				  " INNER JOIN Abm01 ON abm01id = eaa0103item " +
				  " INNER JOIN Aam06 ON aam06id = eaa0103umComl " +
				  " WHERE eaa0103id IN (:eaa0103ids) " +
				  " ORDER BY abm01tipo, abm01codigo";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa0103ids", eaa0103ids));
	}
	
	List<TableMap> buscarResumoValoresC390(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2, String cpoVlr3) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) As " + cpoVlr3);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) as eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " LEFT JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " +
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<TableMap> buscarResumoValoresC590(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2, String cpoVlr3, String cpoVlr4, String cpoVlr5) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) As " + cpoVlr3);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr4 + ")::numeric) As " + cpoVlr4);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr5 + ")::numeric) As " + cpoVlr5);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) As eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric As " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " LEFT JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " LEFT JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " + 
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<TableMap> buscarResumoValoresC850(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) As eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " +
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<TableMap> buscarResumoValoresC890(Set<Long> eaa01Ids, String cpoTxIcms, String cpoVlr1, String cpoVlr2) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) As eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms +  select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc IN (:eaa01Ids) " +
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01Ids", eaa01Ids));
	}
	
	List<TableMap> buscarResumoValoresD190(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2, String cpoVlr3) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) As " + cpoVlr3);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) As eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " +
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<TableMap> buscarResumoValoresD590(Long eaa01id, String cpoTxIcms, String cpoVlr1, String cpoVlr2, String cpoVlr3, String cpoVlr4, String cpoVlr5) {
		def select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpoVlr1 + ")::numeric) As " + cpoVlr1);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr2 + ")::numeric) As " + cpoVlr2);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr3 + ")::numeric) As " + cpoVlr3);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr4 + ")::numeric) As " + cpoVlr4);
		select.append(", SUM(jGet(eaa0103json." + cpoVlr5 + ")::numeric) As " + cpoVlr5);
		
		def sql = " SELECT aaj10codigo, aaj15codigo, SUM(eaa0103totDoc) as eaa0103totDoc, jGet(eaa0103json." + cpoTxIcms + ")::numeric as " + cpoTxIcms + select.toString() +
				  " FROM Eaa0103 " +
				  " INNER JOIN Aaj10 ON aaj10id = eaa0103cstIcms " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa0103doc = :eaa01id " + 
				  " GROUP BY aaj10codigo, aaj15codigo, jGet(eaa0103json." + cpoTxIcms + ")::numeric";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	List<Ecc01> buscarCIAPPorPeriodoOuBaixados(Integer mes, Integer ano) {
		def sql = " SELECT * FROM Ecc01 " +
				  " INNER JOIN Abb20 ON abb20id = ecc01bem " +
				  " WHERE (EXTRACT(MONTH FROM abb20baixa) = :mes " + 
				  " AND EXTRACT(YEAR FROM abb20baixa) = :ano) " + 
				  " OR ecc01id IN (SELECT ecc01id FROM Ecc0101 " +
				  " INNER JOIN Ecc01 ON ecc01id = ecc0101ficha " +
				  " WHERE ecc0101mes = :mes AND ecc0101ano = :ano " + obterWherePadrao("Ecc01") + ")";

		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("mes", mes), 
			                                                  Parametro.criar("ano", ano));
	}
	
	Ecc0101 buscarValoresDoFatorVariavelCIAP(Integer ecc0101mes, Integer ecc0101ano){
		def sql = " SELECT * FROM Ecc0101 " +
				  " INNER JOIN Ecc01 ON ecc01id = ecc0101ficha " +
				  " WHERE ecc0101mes = :mes AND ecc0101ano = :ano " +
				  " AND ecc01variavel = :tipo " + obterWherePadrao("Ecc01", "AND");
		
		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("mes", ecc0101mes), 
			                                               Parametro.criar("ano", ecc0101ano), 
														   Parametro.criar("tipo", 1));
	}
	
	List<Bcb11> buscarItensInventarioPorData() {
		def sql = " SELECT * FROM Bcb11 " +
				  " INNER JOIN Bcb10 ON bcb11inv = bcb10id " +
				  " INNER JOIN Abm01 ON abm01id = bcb11item " +
				  " WHERE bcb10data BETWEEN :dataInicial AND :dataFinal " + 
				  obterWherePadrao("Bcb10") +
				  " ORDER BY abm01tipo, abm01codigo";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("dataInicial", dtInventario), 
			                                                  Parametro.criar("dataFinal", dtInventario));
	}
	
	List<Bcb11> buscarItensInventarioPorDataEClass() {
		def sql = " SELECT * FROM Bcb11 " +
				  " INNER JOIN Bcb10 ON bcb10id = bcb11inv " +
				  " INNER JOIN Abm01 ON abm01id = bcb11item " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " INNER JOIN Abm12 ON abm0101fiscal = abm12id " +
				  " WHERE bcb10data BETWEEN :dataInicial AND :dataFinal " +
				  " AND abm0101empresa = :aac10id " +
				  " AND abm12tipo IN (:tipo) " +
				    obterWherePadrao("Bcb10") +
				  " ORDER BY abm12tipo, abm01tipo, abm01codigo";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("aac10id", aac10.aac10id), 
			                                                  Parametro.criar("dataInicial", dtInicial), 
															  Parametro.criar("dataFinal", dtFinal), 
															  Parametro.criar("tipo", Utils.list(0, 1, 2, 3, 4, 5, 6, 10)));
	}
	
	public List<TableMap> buscarLctos_K220() {
		def sql = " SELECT bcc01id, bcc01data, bcc01qt, abm01id, abm01tipo, abm01codigo, bcc01origItem, bcc01origQt " +
				  " FROM Bcc01 " +
				  " INNER JOIN Abm01 ON bcc01item = abm01id " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " INNER JOIN Abm12 ON abm0101fiscal = abm12id " +
				  " WHERE bcc01mov = :mov " +
				  " AND bcc01data BETWEEN :dataInicial AND :dataFinal " +
				  " AND bcc01origItem IS NOT NULL " +
				  " AND abm0101empresa = :aac10id " +
				  " AND abm12tipo IN (:tipo) " +
				    obterWherePadrao("Bcc01") +
				  " ORDER BY bcc01data, bcc01id";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("aac10id", aac10.aac10id), 
			                                                 Parametro.criar("dataInicial", dtInicial), 
															 Parametro.criar("dataFinal", dtFinal), 
			                                                 Parametro.criar("tipo", Utils.list(0, 1, 2, 3, 4, 5, 10)), 
															 Parametro.criar("mov", 0));
	}
	
	public List<Bcc01> buscarCorrecoesPorAnoMesClass() {
		def sql = " SELECT * FROM Bcc01 " +
				  " INNER JOIN Abm01 ON abm01id = bcc01item " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " INNER JOIN Abm12 ON abm0101fiscal = abm12id " +
				  " WHERE bcc01data BETWEEN :dataInicial AND :dataFinal " +
			  	  " AND abm0101empresa = :aac10id " +
				  " AND abm12tipo IN (:tipo) " +
				  " AND bcc01dtInv IS NOT NULL " +
				    obterWherePadrao("Bcc01") +
				  " ORDER BY bcc01data, abm01codigo, abm01tipo, abm01codigo";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("aac10id", aac10.aac10id), 
			                                                 Parametro.criar("dataInicial", dtInicial), 
															 Parametro.criar("dataFinal", dtFinal), 
                                                             Parametro.criar("tipo", Utils.list(0, 1, 2, 3, 4, 5, 10)));
	}
	
	List<TableMap> buscarLctos_K230(Set<Long> abm20ids) {
		def whereAbm20 = abm20ids != null && abm20ids.size() > 0 ? " AND bcc01ple IN (:abm20ids) " : "";
		
		def sql = " SELECT abb01data, bcc01data, abb01num, bcc01qt, bcc01aglut, abm01id, abm01tipo, abm01codigo " +
				  " FROM Bcc01 " +
				  " INNER JOIN Abm01 ON abm01id = bcc01item " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " INNER JOIN Abm12 ON abm0101fiscal = abm12id " +
				  " LEFT JOIN Abb01 ON abb01id = bcc01central " +
				  " WHERE bcc01data BETWEEN :dataInicial AND :dataFinal " +
				  " AND bcc01producao = :prod " +
				  " AND bcc01mov = :mov " +
				  " AND abm0101empresa = :aac10id " +
				  " AND abm12tipo IN (:tipo) " +
				    whereAbm20 + obterWherePadrao("Bcc01") +
				  " ORDER BY abb01data, bcc01data, bcc01id";
					 
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("aac10id", aac10.aac10id), 
			                                                 Parametro.criar("dataInicial", dtInicial), 
															 Parametro.criar("dataFinal", dtFinal), 
              												 Parametro.criar("tipo", Utils.list(3, 4)), 
															 Parametro.criar("mov", Bcc01.MOV_ENTRADA), 
															 Parametro.criar("prod", Bcc01.PRODUCAO_PRODUCAO_PROPRIA), 
															 abm20ids != null && abm20ids.size() > 0 ? Parametro.criar("abm20ids", abm20ids) : null);
	}
	
	public List<Long> buscarPLEsK230() {
		def sql = " SELECT abp10epfPle FROM Abp10 WHERE abp10epfPle IS NOT NULL " + obterWherePadrao("Abp10");
		return getSession().createQuery(sql).getList(ColumnType.LONG);
	}
	
	public List<TableMap> buscarLctos_K250() {
		def sql = " SELECT abb01data, bcc01data, abb01tipo, abb01num, bcc01qt, bcc01aglut, abm01id, abm01tipo, abm01codigo " +
				  " FROM Bcc01 " +
				  " INNER JOIN Abm01 ON bcc01item = abm01id " +
				  " INNER JOIN Abm0101 ON abm0101item = abm01id " +
				  " INNER JOIN Abm12 ON abm0101fiscal = abm12id " +
				  " LEFT JOIN Abb01 ON abb01id = bcc01central " +
				  " WHERE bcc01data BETWEEN :dataInicial AND :dataFinal " +
				  " AND bcc01mov = :mov AND bcc01producao = :producao " +
				  " AND abm0101empresa = :aac10id " +
				  " AND abm12tipo IN (:tipo) " +
				    obterWherePadrao("Bcc01") +
				  " ORDER BY abb01data, bcc01data, bcc01id";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("aac10id", aac10.aac10id), 
			                                                 Parametro.criar("dataInicial", dtInicial), 
															 Parametro.criar("dataFinal", dtFinal), 
			                                                 Parametro.criar("tipo", Utils.list(3, 4)), 
															 Parametro.criar("mov", Bcc01.MOV_ENTRADA), 
															 Parametro.criar("producao", Bcc01.PRODUCAO_INDUSTRIALIZADO_POR_TERCEIROS));
	}
	
	private boolean contemDados1100() {
		List<Eaa01> bloco1 = buscarDocumentosRegistro1100();
		if(bloco1 != null && bloco1.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	boolean contemDados1400(String cpoVlr) {
		def bloco1 = buscarDocumentosRegistro1400(cpoVlr);
		if(bloco1 != null && bloco1.size() > 0) {
			return true;
		}

		return false;
	}
	
	List<Abm0103> buscarAlteracoesParaEFDPorItem(Long abm01id, int mes, int ano) {
		def sql = " SELECT * FROM Abm0103 " +
				  " WHERE abm0103item = :abm01id " +
				  " AND "+ Fields.numMeses(Fields.month("abm0103data").toString(false), Fields.year("abm0103data").toString(false)) + " <= :numMeses " +
				  " AND (" + Fields.numMeses(Fields.month("abm0103dtEfdIcms").toString(false), Fields.year("abm0103dtEfdIcms").toString(false)) + " = :numMeses " +
				  " OR abm0103dtEfdIcms IS NULL) ORDER BY abm0103data";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("abm01id", abm01id), 
			                                                  Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)));
	}
	
	public LocalDate buscarDataAnteriorDaAlteracaoPorItem(Long abm01id, LocalDate data) {
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
	
	public String buscarBemPrincipal(String codigo) {
		def sql = "SELECT abb20codigo FROM Abb20 WHERE abb20codigo = :codigo " + obterWherePadrao("Abb20");

		return getAcessoAoBanco().obterString(sql, Parametro.criar("codigo", codigo.substring(0, 7) + "000"));
	}
	
	public Ecb0101 buscarUltimaReclassificacao(Long ecb01id, Integer mes, Integer ano) {
		def sql = " SELECT * FROM Ecb0101 " +
				  " INNER JOIN Ecb01 ON ecb0101imob = ecb01id " +
				  " WHERE ecb0101imob = :ecb01id AND " + Fields.numMeses("ecb0101mes", "ecb0101ano") + "<= :numMeses " +
				    obterWherePadrao("Ecb01") +
				  " ORDER BY ecb0101ano DESC, ecb0101mes DESC ";
				  
		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("ecb01id", ecb01id), 
			                                               Parametro.criar("numMeses", DateUtils.numMeses(mes, ano)));
	}
	
	public List<TableMap> buscarItemDaComposicaoPorBom(Long abm01id, String abp20bomCodigo) {
		def sql = " SELECT abm01id, abm01tipo, abm01codigo, SUM(abp20011qt) as qt, MAX(abp20011perda) as perda " +
				  " FROM Abp20011 " +
		  		  " INNER JOIN Abm01 ON abp20011item = abm01id " +
				  " INNER JOIN Abp2001 ON abp20011proc = abp2001id " +
				  " INNER JOIN Abp20 ON abp2001comp = abp20id " +
				  " WHERE abp20item = :abm01id " +
				  " AND abp20bomCodigo = :abp20bomCodigo " +
				    obterWherePadrao("Abp20") +
				  " GROUP BY abm01id, abm01tipo, abm01codigo";
			  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("abm01id", abm01id), 
			                                                 Parametro.criar("abp20bomCodigo", abp20bomCodigo));
	}
	
	public List<TableMap> buscarLctosECF_K235(Long bcc01aglut) {
		def sql = " SELECT MAX(bcc01data) as bcc01data, SUM(bcc01qt) as bcc01qt, abm01id, abm01tipo, abm01codigo " +
				  " FROM Bcc01 " +
				  " INNER JOIN Abm01 ON bcc01item = abm01id " +
				  " WHERE bcc01mov = :mov " +
				  " AND bcc01aglut = :bcc01aglut " +
				    obterWherePadrao("Bcc01") +
				  " GROUP BY abm01id, abm01tipo, abm01codigo" +
				  " ORDER BY bcc01data, abm01codigo";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("bcc01aglut", bcc01aglut), 
			                                                 Parametro.criar("mov", Bcc01.MOV_SAIDA));
	}
	
	public List<TableMap> buscarLctos_K255(Long tipo, Integer num) {
		def sql = " SELECT MAX(bcc01data) as bcc01data, SUM(bcc01qt) as bcc01qt, abm01id, abm01tipo, abm01codigo " +
				  " FROM Bcc01 " +
				  " INNER JOIN Abm01 ON bcc01item = abm01id " +
				  " INNER JOIN Abm20 ON bcc01ple = abm20id " +
				  " LEFT JOIN Abb01 ON abb01id = bcc01central " +
				  " WHERE bcc01mov = :mov " +
				  " AND abb01tipo = :tipo " +
				  " AND abb01num = :num " +
				    obterWherePadrao("Bcc01") +
				  " GROUP BY abm01id, abm01tipo, abm01codigo" +
				  " ORDER BY bcc01data, abm01codigo";
				  
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("mov", Bcc01.MOV_SAIDA), 
			                                                 Parametro.criar("tipo", tipo), 
			                                                 Parametro.criar("num", num));
	}
	
	private List<TableMap> buscarEaa0104sPorEaa01(Long eaa01id) {
		def sql = " SELECT eaa0104tipo, eaa0104num, eaa0104data, eaa0104nat, eaa01041num, eaa01041data, eaa0104ceNum, eaa0104ceData, eaa0104dtAverb, eaa0104ceTipo, eaa0104pais " +
				  " FROM Eaa01041 " +
				  " INNER JOIN Eaa0104 ON eaa0104id = eaa01041de " +
				  " INNER JOIN Eaa01 ON eaa01id = eaa0104doc " +
				  " WHERE eaa0104doc = :eaa01id " +
				   obterWherePadrao("Eaa01");
				   
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}
	
	private Edb01 buscarApuracoes(Integer ano, Integer mes, String codigo) {
		def sql = " SELECT * FROM Edb01 " +
				  " INNER JOIN Aaj28 ON aaj28id = edb01tipo " +
		   		  " WHERE edb01ano = :ano " +
				  " AND edb01mes = :mes " +
				  " AND aaj28codigo = :codigo " +
				   obterWherePadrao("Edb01");
		
		return getAcessoAoBanco().buscarRegistroUnico(sql, Parametro.criar("ano", ano), 
			                                               Parametro.criar("mes", mes), 
														   Parametro.criar("codigo", codigo));
	}
	
	private List<Edb0101> buscarAjustesApuracoes(Long edb01id) {
		def sql = " SELECT * FROM Edb0101 WHERE edb0101apur = :edb01id ";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb01id", edb01id));
	}
	
	private List<Edb01011> buscarInformacoesAdicionais(Long edb0101id) {
		def sql = " SELECT * FROM Edb01011 WHERE edb01011ajuste = :edb0101id ";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb0101id", edb0101id));
	}
	
	private List<Edb0103> buscarValoresDeclaratorios(Long edb01id) {
		def sql = " SELECT * FROM Edb0103 WHERE edb0103apur = :edb01id ";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb01id", edb01id));
	}
	
	private List<Edb0102> buscarObrigacoesARecolher(Long edb01id) {
		def sql = " SELECT * FROM Edb0102 WHERE edb0102apur = :edb01id ";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb01id", edb01id));
	}
	
	private List<Edb0105> buscarAjustesIPI(Long edb01id) {
		def sql = " SELECT * FROM Edb0105 WHERE edb0105apur = :edb01id ";
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("edb01id", edb01id));
	}
	
	private List<Edb01> buscarApuracoesEFD(Integer ano, Integer mes, String codigo) {
		def sql = " SELECT * FROM Edb01 " +
				  " INNER JOIN Aaj28 ON aaj28id = edb01tipo " +
		   		  " WHERE edb01ano = :ano " +
				  " AND edb01mes = :mes " +
				  " AND aaj28codigo = :codigo " +
				   obterWherePadrao("Edb01");
		
		return getAcessoAoBanco().buscarListaDeRegistros(sql, Parametro.criar("ano", ano), 
			                                                  Parametro.criar("mes", mes), 
															  Parametro.criar("codigo", codigo));
	}
	
	private boolean isComOperacaoesST(Edb01 edb01) {
		TableMap jsonEdb01 = edb01.edb01json != null ? edb01.edb01json : new TableMap();

		if(!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "credAnt")).equals(0) || 
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "devolucao")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ressarcimento")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "outrosCred")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ajustesCred")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "retencao")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "outrosDeb")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "ajustesDeb")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldo")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "deducoes")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldoDevedor")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "saldoCredor")).equals(0) ||
		   !jsonEdb01.getBigDecimal_Zero(getCampo(alinApurIcmsSt, "0", "extra")).equals(0)) {
			
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isComOperacaoesDifAliq(Edb01 edb01) {
		TableMap jsonEdb01 = edb01.edb01json != null ? edb01.edb01json : new TableMap();

		if(!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredAntDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "debDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuDebDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "credDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuCredDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoDevDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "deducoesDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "vlrRecolDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "extraDifal")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredAntFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "debFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuDebFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "credFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "ajuCredFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoDevFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "deducoesFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "vlrRecolFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "sdoCredFcp")).equals(0) ||
			!jsonEdb01.getBigDecimal_Zero(getCampo(alinApurDifal, "0", "extraFcp")).equals(0)) {
			 
			 return true;
		 }else {
			 return false;
		 }
	}
	
	private List<TableMap> buscarResumoValoresE510(Set<Long> eaa01s, String cpo1, String cpo2) {
		if(eaa01s == null || eaa01s.size() == 0) return null;
		
		StringBuilder select = new StringBuilder("");
		select.append(", SUM(jGet(eaa0103json." + cpo1 + ")::numeric) As " + cpo1); 
		select.append(", SUM(jGet(eaa0103json." + cpo2 + ")::numeric) As " + cpo2);
		
		def sql = " SELECT aaj11codigo, aaj15codigo, SUM(eaa0103totDoc) as eaa0103totDoc " + select.toString() +
				  " FROM Eaa0103 INNER JOIN Eaa01 ON eaa01id = eaa0103doc " + 
				  " INNER JOIN Aaj11 ON aaj11id = eaa0103cstIpi " +
				  " INNER JOIN Aaj15 ON aaj15id = eaa0103cfop " +
				  " WHERE eaa01id IN (:eaa01s) AND jGet(eaa0103json." + cpo1 + ")::numeric > 0 " +
				  " GROUP BY aaj11codigo, aaj15codigo  " +
				  " ORDER BY aaj11codigo, aaj15codigo";
		
		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01s", eaa01s));
	}
	
	private void inicializarContadores() {
		qtLinBloco0 = 0;
		qtLin0002 = 0;
		qtLin0015 = 0;
		qtLin0150 = 0;
		qtLin0175 = 0;
		qtLin0190 = 0;
		qtLin0200 = 0;
		qtLin0205 = 0;
		qtLin0206 = 0;
		qtLin0210 = 0;
		qtLin0220 = 0;
		qtLin0300 = 0;
		qtLin0305 = 0;
		qtLin0400 = 0;
		qtLin0450 = 0;
		qtLin0460 = 0;
		qtLin0500 = 0;
		qtLin0600 = 0;
		qtLinBlocoB = 0;
		qtLinBlocoC = 0;
		qtLinC100 = 0;
		qtLinC101 = 0;
		qtLinC105 = 0;
		qtLinC110 = 0;
		qtLinC111 = 0;
		qtLinC112 = 0;
		qtLinC113 = 0;
		qtLinC114 = 0;
		qtLinC115 = 0;
		qtLinC120 = 0;
		qtLinC130 = 0;
		qtLinC140 = 0;
		qtLinC141 = 0;
		qtLinC160 = 0;
		qtLinC170 = 0;
		qtLinC171 = 0;
		qtLinC172 = 0;
		qtLinC173 = 0;
		qtLinC174 = 0;
		qtLinC175 = 0;
		qtLinC177 = 0;
		qtLinC178 = 0;
		qtLinC179 = 0;
		qtLinC190 = 0;
		qtLinC191 = 0;
		qtLinC195 = 0;
		qtLinC197 = 0;
		qtLinC300 = 0;
		qtLinC310 = 0;
		qtLinC320 = 0;
		qtLinC321 = 0;
		qtLinC350 = 0;
		qtLinC370 = 0;
		qtLinC390 = 0;
		qtLinC500 = 0;
		qtLinC590 = 0;
		qtLinC800 = 0;
		qtLinC850 = 0;
		qtLinC860 = 0;
		qtLinC890 = 0;
		qtLinBlocoD = 0;
		qtLinD100 = 0;
		qtLinD101 = 0;
		qtLinD190 = 0;
		qtLinD195 = 0;
		qtLinD197 = 0;
		qtLinD500 = 0;
		qtLinD590 = 0;
		qtLinBlocoE = 0;
		qtLinE100 = 0;
		qtLinE110 = 0;
		qtLinE111 = 0;
		qtLinE112 = 0;
		qtLinE113 = 0;
		qtLinE115 = 0;
		qtLinE116 = 0;
		qtLinBlocoG = 0;
		qtLinG110 = 0;
		qtLinG125 = 0;
		qtLinG126 = 0;
		qtLinG130 = 0;
		qtLinG140 = 0;
		qtLinBlocoH = 0;
		qtLinH005 = 0;
		qtLinH010 = 0;
		qtLinH020 = 0;
		qtLinBlocoK = 0;
		qtLinK100 = 0;
		qtLinK200 = 0;
		qtLinK210 = 0;
		qtLinK215 = 0;
		qtLinK220 = 0;
		qtLinK230 = 0;
		qtLinK235 = 0;
		qtLinK250 = 0;
		qtLinK255 = 0;
		qtLinK280 = 0;
		qtLinBloco1 = 0;
		qtLin1010 = 0;
		qtLin1100 = 0;
		qtLin1105 = 0;
		qtLin1110 = 0;
		qtLin1400 = 0;
		qtLin1900 = 0;
		qtLin1910 = 0;
		qtLin1920 = 0;
		qtLin1921 = 0;
		qtLin1922 = 0;
		qtLin1923 = 0;
		qtLin1925 = 0;
		qtLin1926 = 0;
		qtLin1960 = 0;
		qtLinE500 = 0;
		qtLinE510 = 0;
		qtLinE520 = 0;
		qtLinE530 = 0;
		qtLinE531 = 0;
		qtLinE200 = 0;
		qtLinE210 = 0;
		qtLinE220 = 0;
		qtLinE230 = 0;
		qtLinE240 = 0;
		qtLinE250 = 0;
		qtLinE300 = 0;
		qtLinE310 = 0;
		qtLinE311 = 0;
		qtLinE312 = 0;
		qtLinE313 = 0;
		qtLinE316 = 0;
	}
	
	private Map<String, TableMap> comporPerfilA() {
		Map<String, TableMap> mapPerfilA = new HashMap();
		
		TableMap C100 = new TableMap(); C100.put("E", true); C100.put("S", true); mapPerfilA.put("C100", C100);
		TableMap C101 = new TableMap(); C101.put("E", true); C101.put("S", true); mapPerfilA.put("C101", C101);
		TableMap C105 = new TableMap(); C105.put("E", true); C105.put("S", true); mapPerfilA.put("C105", C105);
		TableMap C110 = new TableMap(); C110.put("E", true); C110.put("S", true); mapPerfilA.put("C110", C110);
		TableMap C111 = new TableMap(); C111.put("E", true); C111.put("S", true); mapPerfilA.put("C111", C111);
		TableMap C112 = new TableMap(); C112.put("E", true); C112.put("S", true); mapPerfilA.put("C112", C112);
		TableMap C113 = new TableMap(); C113.put("E", true); C113.put("S", true); mapPerfilA.put("C113", C113);
		TableMap C114 = new TableMap(); C114.put("E", true); C114.put("S", true); mapPerfilA.put("C114", C114);
		TableMap C115 = new TableMap(); C115.put("E", false); C115.put("S", true); mapPerfilA.put("C115", C115);
		TableMap C120 = new TableMap(); C120.put("E", true); C120.put("S", false); mapPerfilA.put("C120", C120);
		TableMap C130 = new TableMap(); C130.put("E", false); C130.put("S", true); mapPerfilA.put("C130", C130);
		TableMap C140 = new TableMap(); C140.put("E", true); C140.put("S", true); mapPerfilA.put("C140", C140);
		TableMap C141 = new TableMap(); C141.put("E", true); C141.put("S", true); mapPerfilA.put("C141", C141);
		TableMap C160 = new TableMap(); C160.put("E", false); C160.put("S", true); mapPerfilA.put("C160", C160);
		TableMap C170 = new TableMap(); C170.put("E", true); C170.put("S", true); mapPerfilA.put("C170", C170);
		TableMap C171 = new TableMap(); C171.put("E", true); C171.put("S", false); mapPerfilA.put("C171", C171);
		TableMap C172 = new TableMap(); C172.put("E", false); C172.put("S", true); mapPerfilA.put("C172", C172);
		TableMap C173 = new TableMap(); C173.put("E", true); C173.put("S", true); mapPerfilA.put("C173", C173);
		TableMap C174 = new TableMap(); C174.put("E", false); C174.put("S", true); mapPerfilA.put("C174", C174);
		TableMap C175 = new TableMap(); C175.put("E", true); C175.put("S", true); mapPerfilA.put("C175", C175);
		TableMap C177 = new TableMap(); C177.put("E", false); C177.put("S", true); mapPerfilA.put("C177", C177);
		TableMap C178 = new TableMap(); C178.put("E", false); C178.put("S", true); mapPerfilA.put("C178", C178);
		TableMap C179 = new TableMap(); C179.put("E", false); C179.put("S", true); mapPerfilA.put("C179", C179);
		TableMap C190 = new TableMap(); C190.put("E", true); C190.put("S", true); mapPerfilA.put("C190", C190);
		TableMap C191 = new TableMap(); C191.put("E", true); C191.put("S", true); mapPerfilA.put("C191", C191);
		TableMap C195 = new TableMap(); C195.put("E", true); C195.put("S", true); mapPerfilA.put("C195", C195);
		TableMap C197 = new TableMap(); C197.put("E", true); C197.put("S", true); mapPerfilA.put("C197", C197);
		TableMap C300 = new TableMap(); C300.put("E", false); C300.put("S", false); mapPerfilA.put("C300", C300);
		TableMap C310 = new TableMap(); C310.put("E", false); C310.put("S", false); mapPerfilA.put("C310", C310);
		TableMap C320 = new TableMap(); C320.put("E", false); C320.put("S", false); mapPerfilA.put("C320", C320);
		TableMap C321 = new TableMap(); C321.put("E", false); C321.put("S", false); mapPerfilA.put("C321", C321);
		TableMap C350 = new TableMap(); C350.put("E", false); C350.put("S", true); mapPerfilA.put("C350", C350);
		TableMap C370 = new TableMap(); C370.put("E", false); C370.put("S", true); mapPerfilA.put("C370", C370);
		TableMap C390 = new TableMap(); C390.put("E", false); C390.put("S", true); mapPerfilA.put("C390", C390);
		TableMap C500 = new TableMap(); C500.put("E", true); C500.put("S", true); mapPerfilA.put("C500", C500);
		TableMap C590 = new TableMap(); C590.put("E", true); C590.put("S", true); mapPerfilA.put("C590", C590);
		TableMap C800 = new TableMap(); C800.put("E", false); C590.put("S", true); mapPerfilA.put("C800", C800);
		TableMap C850 = new TableMap(); C850.put("E", false); C850.put("S", true); mapPerfilA.put("C850", C850);
		TableMap C860 = new TableMap(); C860.put("E", false); C860.put("S", false); mapPerfilA.put("C860", C860);
		TableMap C890 = new TableMap(); C890.put("E", false); C890.put("S", false); mapPerfilA.put("C890", C890);
		TableMap D100 = new TableMap(); D100.put("E", true); D100.put("S", true); mapPerfilA.put("D100", D100);
		TableMap D101 = new TableMap(); D101.put("E", true); D101.put("S", true); mapPerfilA.put("D101", D101);
		TableMap D190 = new TableMap(); D190.put("E", true); D190.put("S", true); mapPerfilA.put("D190", D190);
		TableMap D195 = new TableMap(); D195.put("E", true); D195.put("S", true); mapPerfilA.put("D195", D195);
		TableMap D197 = new TableMap(); D197.put("E", true); D197.put("S", true); mapPerfilA.put("D197", D197);
		TableMap D500 = new TableMap(); D500.put("E", true); D500.put("S", true); mapPerfilA.put("D500", D500);
		TableMap D590 = new TableMap(); D590.put("E", true); D590.put("S", true); mapPerfilA.put("D590", D590);
		
		return mapPerfilA;
	}
	
	private Map<String, TableMap> comporPerfilB() {
		Map<String, TableMap> mapPerfilB = new HashMap();
		
		TableMap C100 = new TableMap(); C100.put("E", true); C100.put("S", true); mapPerfilB.put("C100", C100);
		TableMap C101 = new TableMap(); C101.put("E", true); C101.put("S", true); mapPerfilB.put("C101", C101);
		TableMap C105 = new TableMap(); C105.put("E", true); C105.put("S", true); mapPerfilB.put("C105", C105);
		TableMap C110 = new TableMap(); C110.put("E", true); C110.put("S", true); mapPerfilB.put("C110", C110);
		TableMap C111 = new TableMap(); C111.put("E", true); C111.put("S", true); mapPerfilB.put("C111", C111);
		TableMap C112 = new TableMap(); C112.put("E", true); C112.put("S", true); mapPerfilB.put("C112", C112);
		TableMap C113 = new TableMap(); C113.put("E", true); C113.put("S", true); mapPerfilB.put("C113", C113);
		TableMap C114 = new TableMap(); C114.put("E", true); C114.put("S", true); mapPerfilB.put("C114", C114);
		TableMap C115 = new TableMap(); C115.put("E", false); C115.put("S", true); mapPerfilB.put("C115", C115);
		TableMap C120 = new TableMap(); C120.put("E", true); C120.put("S", false); mapPerfilB.put("C120", C120);
		TableMap C130 = new TableMap(); C130.put("E", false); C130.put("S", true); mapPerfilB.put("C130", C130);
		TableMap C140 = new TableMap(); C140.put("E", true); C140.put("S", true); mapPerfilB.put("C140", C140);
		TableMap C141 = new TableMap(); C141.put("E", true); C141.put("S", true); mapPerfilB.put("C141", C141);
		TableMap C160 = new TableMap(); C160.put("E", false); C160.put("S", true); mapPerfilB.put("C160", C160);
		TableMap C170 = new TableMap(); C170.put("E", true); C170.put("S", true); mapPerfilB.put("C170", C170);
		TableMap C171 = new TableMap(); C171.put("E", true); C171.put("S", false); mapPerfilB.put("C171", C171);
		TableMap C172 = new TableMap(); C172.put("E", false); C172.put("S", true); mapPerfilB.put("C172", C172);
		TableMap C173 = new TableMap(); C173.put("E", true); C173.put("S", true); mapPerfilB.put("C173", C173);
		TableMap C174 = new TableMap(); C174.put("E", false); C174.put("S", true); mapPerfilB.put("C174", C174);
		TableMap C175 = new TableMap(); C175.put("E", true); C175.put("S", true); mapPerfilB.put("C175", C175);
		TableMap C177 = new TableMap(); C177.put("E", false); C177.put("S", true); mapPerfilB.put("C177", C177);
		TableMap C178 = new TableMap(); C178.put("E", false); C178.put("S", true); mapPerfilB.put("C178", C178);
		TableMap C179 = new TableMap(); C179.put("E", false); C179.put("S", true); mapPerfilB.put("C179", C179);
		TableMap C190 = new TableMap(); C190.put("E", true); C190.put("S", true); mapPerfilB.put("C190", C190);
		TableMap C191 = new TableMap(); C191.put("E", true); C191.put("S", true); mapPerfilB.put("C191", C191);
		TableMap C195 = new TableMap(); C195.put("E", true); C195.put("S", true); mapPerfilB.put("C195", C195);
		TableMap C197 = new TableMap(); C197.put("E", true); C197.put("S", true); mapPerfilB.put("C197", C197);
		TableMap C300 = new TableMap(); C300.put("E", false); C300.put("S", true); mapPerfilB.put("C300", C300);
		TableMap C310 = new TableMap(); C310.put("E", false); C310.put("S", true); mapPerfilB.put("C310", C310);
		TableMap C320 = new TableMap(); C320.put("E", false); C320.put("S", true); mapPerfilB.put("C320", C320);
		TableMap C321 = new TableMap(); C321.put("E", false); C321.put("S", true); mapPerfilB.put("C321", C321);
		TableMap C350 = new TableMap(); C350.put("E", false); C350.put("S", false); mapPerfilB.put("C350", C350);
		TableMap C370 = new TableMap(); C370.put("E", false); C370.put("S", false); mapPerfilB.put("C370", C370);
		TableMap C390 = new TableMap(); C390.put("E", false); C390.put("S", false); mapPerfilB.put("C390", C390);
		TableMap C500 = new TableMap(); C500.put("E", true); C500.put("S", false); mapPerfilB.put("C500", C500);
		TableMap C590 = new TableMap(); C590.put("E", true); C590.put("S", false); mapPerfilB.put("C590", C590);
		TableMap C800 = new TableMap(); C800.put("E", false); C590.put("S", false); mapPerfilB.put("C800", C800);
		TableMap C850 = new TableMap(); C850.put("E", false); C850.put("S", false); mapPerfilB.put("C850", C850);
		TableMap C860 = new TableMap(); C860.put("E", false); C860.put("S", true); mapPerfilB.put("C860", C860);
		TableMap C890 = new TableMap(); C890.put("E", false); C890.put("S", true); mapPerfilB.put("C890", C890);
		TableMap D100 = new TableMap(); D100.put("E", true); D100.put("S", true); mapPerfilB.put("D100", D100);
		TableMap D101 = new TableMap(); D101.put("E", true); D101.put("S", true); mapPerfilB.put("D101", D101);
		TableMap D190 = new TableMap(); D190.put("E", true); D190.put("S", true); mapPerfilB.put("D190", D190);
		TableMap D195 = new TableMap(); D195.put("E", true); D195.put("S", true); mapPerfilB.put("D195", D195);
		TableMap D197 = new TableMap(); D197.put("E", true); D197.put("S", true); mapPerfilB.put("D197", D197);
		TableMap D500 = new TableMap(); D500.put("E", true); D500.put("S", false); mapPerfilB.put("D500", D500);
		TableMap D590 = new TableMap(); D590.put("E", true); D590.put("S", false); mapPerfilB.put("D590", D590);
		
		return mapPerfilB;
	}
	
	private Map<String, TableMap> comporPerfilC() {
		Map<String, TableMap> mapPerfilC = new HashMap();
		
		TableMap C100 = new TableMap(); C100.put("E", true); C100.put("S", true); mapPerfilC.put("C100", C100);
		TableMap C101 = new TableMap(); C101.put("E", true); C101.put("S", true); mapPerfilC.put("C101", C101);
		TableMap C105 = new TableMap(); C105.put("E", false); C105.put("S", false); mapPerfilC.put("C105", C105);
		TableMap C110 = new TableMap(); C110.put("E", false); C110.put("S", false); mapPerfilC.put("C110", C110);
		TableMap C111 = new TableMap(); C111.put("E", false); C111.put("S", false); mapPerfilC.put("C111", C111);
		TableMap C112 = new TableMap(); C112.put("E", false); C112.put("S", false); mapPerfilC.put("C112", C112);
		TableMap C113 = new TableMap(); C113.put("E", false); C113.put("S", false); mapPerfilC.put("C113", C113);
		TableMap C114 = new TableMap(); C114.put("E", false); C114.put("S", false); mapPerfilC.put("C114", C114);
		TableMap C115 = new TableMap(); C115.put("E", false); C115.put("S", false); mapPerfilC.put("C115", C115);
		TableMap C120 = new TableMap(); C120.put("E", false); C120.put("S", false); mapPerfilC.put("C120", C120);
		TableMap C130 = new TableMap(); C130.put("E", false); C130.put("S", false); mapPerfilC.put("C130", C130);
		TableMap C140 = new TableMap(); C140.put("E", false); C140.put("S", false); mapPerfilC.put("C140", C140);
		TableMap C141 = new TableMap(); C141.put("E", false); C141.put("S", false); mapPerfilC.put("C141", C141);
		TableMap C160 = new TableMap(); C160.put("E", false); C160.put("S", false); mapPerfilC.put("C160", C160);
		TableMap C170 = new TableMap(); C170.put("E", false); C170.put("S", false); mapPerfilC.put("C170", C170);
		TableMap C171 = new TableMap(); C171.put("E", false); C171.put("S", false); mapPerfilC.put("C171", C171);
		TableMap C172 = new TableMap(); C172.put("E", false); C172.put("S", false); mapPerfilC.put("C172", C172);
		TableMap C173 = new TableMap(); C173.put("E", false); C173.put("S", false); mapPerfilC.put("C173", C173);
		TableMap C174 = new TableMap(); C174.put("E", false); C174.put("S", false); mapPerfilC.put("C174", C174);
		TableMap C175 = new TableMap(); C175.put("E", false); C175.put("S", false); mapPerfilC.put("C175", C175);
		TableMap C177 = new TableMap(); C177.put("E", false); C177.put("S", false); mapPerfilC.put("C177", C177);
		TableMap C178 = new TableMap(); C178.put("E", false); C178.put("S", false); mapPerfilC.put("C178", C178);
		TableMap C179 = new TableMap(); C179.put("E", false); C179.put("S", false); mapPerfilC.put("C179", C179);
		TableMap C190 = new TableMap(); C190.put("E", true); C190.put("S", true); mapPerfilC.put("C190", C190);
		TableMap C191 = new TableMap(); C191.put("E", true); C191.put("S", true); mapPerfilC.put("C191", C191);
		TableMap C195 = new TableMap(); C195.put("E", true); C195.put("S", true); mapPerfilC.put("C195", C195);
		TableMap C197 = new TableMap(); C197.put("E", true); C197.put("S", true); mapPerfilC.put("C197", C197);
		TableMap C300 = new TableMap(); C300.put("E", false); C300.put("S", true); mapPerfilC.put("C300", C300);
		TableMap C310 = new TableMap(); C310.put("E", false); C310.put("S", true); mapPerfilC.put("C310", C310);
		TableMap C320 = new TableMap(); C320.put("E", false); C320.put("S", true); mapPerfilC.put("C320", C320);
		TableMap C321 = new TableMap(); C321.put("E", false); C321.put("S", false); mapPerfilC.put("C321", C321);
		TableMap C350 = new TableMap(); C350.put("E", false); C350.put("S", false); mapPerfilC.put("C350", C350);
		TableMap C370 = new TableMap(); C370.put("E", false); C370.put("S", false); mapPerfilC.put("C370", C370);
		TableMap C390 = new TableMap(); C390.put("E", false); C390.put("S", false); mapPerfilC.put("C390", C390);
		TableMap C500 = new TableMap(); C500.put("E", true); C500.put("S", false); mapPerfilC.put("C500", C500);
		TableMap C590 = new TableMap(); C590.put("E", true); C590.put("S", false); mapPerfilC.put("C590", C590);
		TableMap C800 = new TableMap(); C800.put("E", false); C590.put("S", false); mapPerfilC.put("C800", C800);
		TableMap C850 = new TableMap(); C850.put("E", false); C850.put("S", false); mapPerfilC.put("C850", C850);
		TableMap C860 = new TableMap(); C860.put("E", false); C860.put("S", true); mapPerfilC.put("C860", C860);
		TableMap C890 = new TableMap(); C890.put("E", false); C890.put("S", true); mapPerfilC.put("C890", C890);
		TableMap D100 = new TableMap(); D100.put("E", true); D100.put("S", true); mapPerfilC.put("D100", D100);
		TableMap D101 = new TableMap(); D101.put("E", true); D101.put("S", true); mapPerfilC.put("D101", D101);
		TableMap D190 = new TableMap(); D190.put("E", true); D190.put("S", true); mapPerfilC.put("D190", D190);
		TableMap D195 = new TableMap(); D195.put("E", true); D195.put("S", true); mapPerfilC.put("D195", D195);
		TableMap D197 = new TableMap(); D197.put("E", true); D197.put("S", true); mapPerfilC.put("D197", D197);
		TableMap D500 = new TableMap(); D500.put("E", true); D500.put("S", true); mapPerfilC.put("D500", D500);
		TableMap D590 = new TableMap(); D590.put("E", true); D590.put("S", true); mapPerfilC.put("D590", D590);
		
		return mapPerfilC;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMDYifQ==