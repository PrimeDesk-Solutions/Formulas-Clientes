package multitec.baseDemo

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class SRF_NFe extends FormulaBase {
	
	private Eaa01 eaa01;
	private Integer formaEmis;
	private LocalDate contDt;
	private LocalTime contHr;
	private String contJust;
	private Aac10 empresa;
	
	private Eaa0101 endPrinc;
	private Eaa0101 endEntre;
	private Eaa0101 endSaida;
	private Eaa0102 eaa0102;
	private ElementXml nfe;
	private ElementXml infNfe;
	private ElementXml ide;
	private ElementXml NFref;
	private ElementXml refNF;
	private ElementXml emit;
	private ElementXml enderEmit;
	private ElementXml dest;
	private ElementXml enderDest;
	private ElementXml retirada;
	private ElementXml entrega;
	private ElementXml autXML;
	private ElementXml det;
	private ElementXml prod;
	private ElementXml DI;
	private ElementXml adi;
	private ElementXml detExport;
	private ElementXml rastro;
	private ElementXml comb;
	private ElementXml CIDE;
	private ElementXml imposto;
	private ElementXml ISSQN;
	private ElementXml ICMS;
	private ElementXml ICMSSN101;
	private ElementXml ICMSSN102;
	private ElementXml ICMSSN201;
	private ElementXml ICMSSN202;
	private ElementXml ICMSSN500;
	private ElementXml ICMSSN900;
	private ElementXml ICMSST;
	private ElementXml ICMS00;
	private ElementXml ICMS10;
	private ElementXml ICMS20;
	private ElementXml ICMS30;
	private ElementXml ICMS40;
	private ElementXml ICMS51;
	private ElementXml ICMS60;
	private ElementXml ICMS70;
	private ElementXml ICMS90;
	private ElementXml IPI;
	private ElementXml IPITrib;
	private ElementXml IPINT;
	private ElementXml II;
	private ElementXml PIS;
	private ElementXml PISAliq;
	private ElementXml PISQtde;
	private ElementXml PISNT;
	private ElementXml PISOutr;
	private ElementXml PISST;
	private ElementXml COFINS;
	private ElementXml COFINSAliq;
	private ElementXml COFINSQtde;
	private ElementXml COFINSNT;
	private ElementXml COFINSOutr;
	private ElementXml COFINSST;
	private ElementXml ICMSUFDest;
	private ElementXml impostoDevol;
	private boolean isProducao;
	private boolean gerarISSQNTot;
	private int idDest = 0;
	private int indIEDest = 0;
	
	private String versaoXMLLayoutNFe = "4.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ARQUIVOS_DE_NFE;
	}
	
	@Override
	public void executar() {
		eaa01 	  = get("eaa01");
		formaEmis = get("formaEmis");
		contDt 	  = get("contDt");
		contHr	  = get("contHr");
		contJust  = get("contJust");
		empresa   = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		
		// Forma de Emissão
		Integer formaEmissao = contDt == null ? 1 : formaEmis;

		// Gera a chave da NFe
		Long cNF = NFeUtils.gerarCodigoNumerico(eaa01.eaa01central.abb01num, eaa01.eaa01id);
		String chaveNfe = NFeUtils.gerarChaveDeAcesso(eaa01, empresa, formaEmissao, cNF);
		
		/** GERAÇÃO DA NOTA FISCAL ELETRÔNICA */
		TableMap camposLivresDoc = eaa01.eaa01json;
		
		// Data de produção
		LocalDate dtProdNFe = getAcessoAoBanco().buscarParametro("NFeDataProducao", "EA");
		isProducao = dtProdNFe == null ? false : (DateUtils.dateDiff(dtProdNFe, eaa01.eaa01central.abb01data, ChronoUnit.DAYS) >= 0);
		
		/** NFe - Nota Fiscal Eletrônica */
		nfe = NFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/nfe");
		nfe.addCaracterSubstituir('\n'.toCharacter(), "");
		nfe.addCaracterSubstituir('<'.toCharacter(), "&lt;");
		nfe.addCaracterSubstituir('>'.toCharacter(), "&gt;");
		nfe.addCaracterSubstituir('&'.toCharacter(), "&amp;");
		nfe.addCaracterSubstituir('"'.toCharacter(), "&quot;");
		nfe.addCaracterSubstituir('\''.toCharacter(), "&#39;");
		
		/** infNFe - Dados da NFe (A01) */
		infNfe = nfe.addNode("infNFe");
		infNfe.setAttribute("Id", "NFe" + chaveNfe);
		infNfe.setAttribute("versao", versaoXMLLayoutNFe);
		
		/** ide - Identificação da NFe (B01) */
		ide = infNfe.addNode("ide");
		ide.addNode("cUF", empresa.aac10municipio.aag0201uf.aag02ibge, true);
		ide.addNode("cNF", StringUtils.ajustString(cNF, 8), true);
		ide.addNode("natOp", eaa01.eaa01operDescr, true, 60);
		ide.addNode("mod", eaa01.eaa01central.abb01tipo.aah01modelo, true);
		ide.addNode("serie", eaa01.eaa01central.abb01serie == null ? 0 : eaa01.eaa01central.abb01serie.length() <= 3 ? eaa01.eaa01central.abb01serie : eaa01.eaa01central.abb01serie.substring(0, 3), true);
		ide.addNode("nNF", eaa01.eaa01central.abb01num, true);
		ide.addNode("dhEmi", NFeUtils.dataFormatoUTC(eaa01.eaa01central.abb01data, eaa01.eaa01central.abb01operHora, empresa.aac10fusoHorario), true);
		ide.addNode("dhSaiEnt", eaa01.eaa01esData == null ? null : NFeUtils.dataFormatoUTC(eaa01.eaa01esData, eaa01.eaa01esHora != null ? eaa01.eaa01esHora : eaa01.eaa01central.abb01operHora, empresa.acc10fusoHorario), false);
		ide.addNode("tpNF", eaa01.eaa01esMov, true);
		ide.addNode("procEmi", 0, true);
		ide.addNode("verProc", "SAM4_" + Utils.getVersao(), true);
		
		/** Gera o XML */
		String dados = NFeUtils.gerarXML(nfe);
		put("chaveNfe", chaveNfe);
		put("dados", dados);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==