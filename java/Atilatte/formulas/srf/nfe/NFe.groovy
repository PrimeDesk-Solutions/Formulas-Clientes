package Atilatte.formulas.srf.nfe;

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.Scale
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.validator.MultiValidationException
import br.com.multitec.utils.validator.ValidationMessage
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aah01
import sam.model.entities.aa.Aah20
import sam.model.entities.aa.Aaj03
import sam.model.entities.aa.Aaj04
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj14
import sam.model.entities.aa.Aaj15
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abg0101
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa01033
import sam.model.entities.ea.Eaa01034
import sam.model.entities.ea.Eaa010341
import sam.model.entities.ea.Eaa01038
import sam.model.entities.ea.Eaa0104
import sam.model.entities.ea.Eaa0113
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils
import sam.server.samdev.utils.Parametro

class NFe extends FormulaBase {

	private Eaa01 eaa01;
	private Integer formaEmis;
	private LocalDate contDt;
	private LocalTime contHr;
	private String contJust;
	private Aac10 empresa;
	private boolean isProducao;
	private boolean gerarISSQNTot;
	private int idDest = 0;
	private int indIEDest = 0;
	private String modelo = null;

	private Eaa0101 endPrincipal;
	private Eaa0101 endEntrega;
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

		selecionarAlinhamento("02");
		if(getAlinhamento().size() == 0 ) throw new ValidacaoException("O alinhamento '02' para NFe não foi encontrado.");

		comporFilhosDocumento();

		validarDadosDaNFe();

		Integer formaEmissao = contDt == null ? 1 : formaEmis;

		/**
		 * CHAVE NFe
		 */
		Long cNF = NFeUtils.gerarCodigoNumerico(eaa01.eaa01central.abb01num, eaa01.eaa01id);
		String chaveNfe = NFeUtils.gerarChaveDeAcesso(eaa01, empresa, formaEmissao, cNF);

		TableMap jsonEaa01 = eaa01.eaa01json;

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
		ide.addNode("mod", modelo, true);
		ide.addNode("serie", eaa01.eaa01central.abb01serie == null ? 0 : eaa01.eaa01central.abb01serie.length() <= 3 ? eaa01.eaa01central.abb01serie : eaa01.eaa01central.abb01serie.substring(0, 3), true);
		ide.addNode("nNF", eaa01.eaa01central.abb01num, true);
		ide.addNode("dhEmi", NFeUtils.dataFormatoUTC(eaa01.eaa01central.abb01data, eaa01.eaa01central.abb01operHora, empresa.aac10fusoHorario), true);
		ide.addNode("dhSaiEnt", eaa01.eaa01esData == null ? null : NFeUtils.dataFormatoUTC(eaa01.eaa01esData, eaa01.eaa01esHora != null ? eaa01.eaa01esHora : eaa01.eaa01central.abb01operHora, empresa.aac10fusoHorario), false);
		ide.addNode("tpNF", eaa01.eaa01esMov, true);

		idDest = 1; //1-Interna
		Eaa0103 primeiroItem = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0103", Criterions.eq("eaa0103doc", eaa01.eaa01id), Criterions.eq("eaa0103seq", 1));
		def CFOPprincipal = primeiroItem.eaa0103cfop != null ? primeiroItem.eaa0103cfop.aaj15codigo.substring(0, 1) : "";
		if(CFOPprincipal.equals("1") || CFOPprincipal.equals("5")) { 		//Interna
			idDest = 1;
		}else if(CFOPprincipal.equals("2") || CFOPprincipal.equals("6")) {	//Interestadual
			idDest = 2;
		}else {																//Exterior
			idDest = 3;
		}
//		if(eaa0102.eaa0102consPres != 1){
//			Eaa0103 primeiroItem = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0103", Criterions.eq("eaa0103doc", eaa01.eaa01id), Criterions.eq("eaa0103seq", 1));
//			Aag02 uf = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endPrincipal.eaa0101municipio.aag0201uf.aag02id);
//			if(uf != null && uf.aag02uf.equals("EX")) { //3-Exterior
//				idDest = 3;
//			}else if((eaa01.eaa01esMov.equals(0) && primeiroItem.eaa0103cfop != null && primeiroItem.eaa0103cfop.aaj15codigo.length() > 0 && primeiroItem.eaa0103cfop.aaj15codigo.substring(0, 1).equals("2")) ||
//					(!uf.aag02uf.equalsIgnoreCase(empresa.aac10municipio.aag0201uf.aag02uf)))  { //2-Interestadual
//				idDest = 2;
//			}
//		}

		ide.addNode("idDest", idDest, true);

		ide.addNode("cMunFG", empresa.aac10municipio.aag0201ibge, true);
		ide.addNode("tpImp", 0, true);
		ide.addNode("tpEmis", formaEmissao, true);
		ide.addNode("cDV", chaveNfe.substring(43), true);
		ide.addNode("tpAmb", isProducao ? 1 : 2, true);


		ide.addNode("finNFe", eaa01.eaa01sitDoc == null ? null : eaa01.eaa01sitDoc.aaj03nfe, true);
		ide.addNode("indFinal", eaa0102.eaa0102consFinal, true);
		ide.addNode("indPres", eaa0102.eaa0102consPres, true);

		Integer indIntermed = Utils.list(2, 3, 4, 9).contains(eaa0102.eaa0102consPres) ? 0 : null;
		ide.addNode("indIntermed", indIntermed, false);
		ide.addNode("procEmi", 0, true);
		ide.addNode("verProc", "SAM4_" + Utils.getVersao(), true);

		if(formaEmissao == 1){
			ide.addNode("dhCont", null, false);
			ide.addNode("xJust", null, false);
		}else{
			ide.addNode("dhCont", NFeUtils.dataFormatoUTC(contDt, contHr, empresa.aac10fusoHorario), true);
			ide.addNode("xJust", contJust, true);
		}

		/** Documentos referenciadas */
		List<Long> docsRef = buscarDocumentosReferenciados(eaa01.eaa01id);
		if(docsRef != null && docsRef.size() > 0) {
			for(Long eaa01Ref : docsRef) {
				Eaa01 notaRef = getAcessoAoBanco().buscarRegistroUnicoById("Eaa01", eaa01Ref);
				if(notaRef != null) {
					Eaa0102 dadosGeraisRef = notaRef.eaa0102DadosGerais;
					Abb01 centralRef = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", notaRef.eaa01central.abb01id);
					Aah01 tipoRef = getAcessoAoBanco().buscarRegistroUnicoById("Aah01", eaa01.eaa01central.abb01tipo.aah01id);
					Eaa0101 endPrincipalRef = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", notaRef.eaa01id), Criterions.eq("eaa0101principal", 1));
					String modeloRef = tipoRef.aah01modelo;

					/** Modelo 55 - NFe ou 65-NFCe */
					if(modeloRef.equals("55") || modeloRef.equals("65") || modeloRef.equals("59")) {
						NFref = ide.addNode("NFref");
						NFref.addNode("refNFe", notaRef.eaa01nfeChave, true);

						/** Modelo 01, 1A e 02 - Notas normais */
					}else if((modeloRef.equals("01") || modeloRef.equals("02")) && dadosGeraisRef.eaa0102ti == 0) {
						NFref = ide.addNode("NFref");
						refNF = NFref.addNode("refNF");

						refNF.addNode("cUF", endPrincipalRef.eaa0101municipio.aag0201ibge, true);
						refNF.addNode("AAMM", NFeUtils.formatarData(centralRef.abb01data, "yyMM"), true);
						refNF.addNode("CNPJ", centralRef.abb01ent.abe01ni == null ? null : StringUtils.extractNumbers(centralRef.abb01ent.abe01ni), true);
						refNF.addNode("mod", modeloRef, true);
						refNF.addNode("serie", centralRef.abb01serie == null ? 0 : centralRef.abb01serie.length() <= 3 ? centralRef.abb01serie : centralRef.abb01serie.substring(0, 3), true);
						refNF.addNode("nNF", centralRef.abb01num, true);

						/** Modelo 04 - Produtor rural */
					}else if(modeloRef.equals("04") || (modeloRef.equals("01") && dadosGeraisRef.eaa0102ti == 1)) {
						ElementXml NFref = ide.addNode("NFref");
						ElementXml refNFP = NFref.addNode("refNFP");

						refNFP.addNode("cUF", endPrincipalRef.eaa0101municipio.aag0201ibge, true);
						refNFP.addNode("AAMM", NFeUtils.formatarData(centralRef.abb01data, "yyMM"), true);

						String ni = StringUtils.extractNumbers(centralRef.abb01ent.abe01ni);
						if(dadosGeraisRef.eaa0102ti == 0) {
							refNFP.addNode("CNPJ", StringUtils.ajustString(ni, 14), true);
						}else {
							refNFP.addNode("CPF", StringUtils.ajustString(ni, 11), true);
						}

						refNFP.addNode("IE", NFeUtils.formatarIE(dadosGeraisRef.eaa0102ie), true);
						refNFP.addNode("mod", modeloRef, true);
						refNFP.addNode("serie", centralRef.abb01serie == null ? 0 : centralRef.abb01serie.length() <= 3 ? centralRef.abb01serie : centralRef.abb01serie.substring(0, 3), true);
						refNFP.addNode("nNF", centralRef.abb01num, true);

						/** Modelo 57 - CTe */
					}else if(modeloRef.equals("57")) {
						ElementXml NFref = ide.addNode("NFref");
						NFref.addNode("refCTe", notaRef.eaa01nfeChave, true);

						/** Modelo 2B, 2C ou 2D - Cupom Fiscal */
					}else if(modeloRef.equals("2B") || modeloRef.equals("2C") || modeloRef.equals("2D")) {
						ElementXml NFref = ide.addNode("NFref");
						ElementXml refECF = NFref.addNode("refECF");

						refECF.addNode("mod", modeloRef, true);
						refECF.addNode("nECF", notaRef.eaa01cfEF.abd10caixa, true);
						refECF.addNode("nCOO", centralRef.abb01num, true);
					}
				}
			}
		}

		/** emit - Identificação do Emitente da Nota Fiscal eletrônica (C01) */
		emitente();

		/** dest - Identificação do Destinatário da Nota Fiscal Eletrônica (E01) */
		destinatario();

		/** det - Detalhamento de Produtos e Serviços da NF-e (H01) */
		item();

		/** total - Valores totais da NF-e (W01) */
		ElementXml total = infNfe.addNode("total");

		/** ICMSTot - Totais referentes ao ICMS (W02) */
		ElementXml ICMSTot = total.addNode("ICMSTot");
		ICMSTot.addNode("vBC", getCampo("328-W03","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("328-W03","vBC")), 2, false), true);
		ICMSTot.addNode("vICMS", getCampo("329-W04","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329-W04","vICMS")), 2, false), true);
		ICMSTot.addNode("vICMSDeson", getCampo("329.01-W04a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329.01-W04a","vICMSDeson")), 2, false), true);
		ICMSTot.addNode("vICMSUFDest", getCampo("245a.15-NA15","vICMSUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("245a.15-NA15","vICMSUFDest")), 2, false), true);
		ICMSTot.addNode("vFCPUFDest", getCampo("329.03-W04c","vFCPUFDest") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329.03-W04c","vFCPUFDest")), 2, true), false);
		//ICMSTot.addNode("vICMSUFDest", getCampo("329.05-W04e","vICMSUFDest") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329.05-W04e","vICMSUFDest")), 2, true), false);
		//ICMSTot.addNode("vICMSUFDest", null)
		ICMSTot.addNode("vICMSUFRemet", getCampo("329.07-W04g","vICMSUFRemet") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329.07-W04g","vICMSUFRemet")), 2, true), false);
		ICMSTot.addNode("vFCP", getCampo("329.02-W04b","vFCP") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("329.02-W04b","vFCP")), 2, false), true);
		ICMSTot.addNode("vBCST", getCampo("330-W05","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("330-W05","vBCST")), 2, false), true);
		ICMSTot.addNode("vST", getCampo("331-W06","vST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("331-W06","vST")), 2, false), true);
		ICMSTot.addNode("vFCPST", getCampo("331.01-W06a","vFCPST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("331.01-W06a","vFCPST")), 2, false), true);
		ICMSTot.addNode("vFCPSTRet", getCampo("331.02-W06b","vFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("331.02-W06b","vFCPSTRet")), 2, false), true);
		//ICMSTot.addNode("vProd", getCampo("332-W07","vProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal_Zero("tot_itens"), 2, false), true);
		ICMSTot.addNode("vProd", NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal_Zero("total_conv"), 2, false), true);
		ICMSTot.addNode("vFrete", getCampo("333-W08","vFrete") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("333-W08","vFrete")), 2, false), true);
		ICMSTot.addNode("vSeg", getCampo("334-W09","vSeg") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("334-W09","vSeg")), 2, false), true);
		ICMSTot.addNode("vDesc", getCampo("335-W10","vDesc") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("335-W10","vDesc")), 2, false), true);
		ICMSTot.addNode("vII", getCampo("336-W11","vII") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("336-W11","vII")), 2, false), true);
		ICMSTot.addNode("vIPI", getCampo("337-W12","vIPI") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("337-W12","vIPI")), 2, false), true);
		ICMSTot.addNode("vIPIDevol", getCampo("337.01-W12a","vIPIDevol") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("337.01-W12a","vIPIDevol")), 2, false), true);
		ICMSTot.addNode("vPIS", getCampo("338-W13","vPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("338-W13","vPIS")), 2, false), true);
		ICMSTot.addNode("vCOFINS", getCampo("339-W14","vCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("339-W14","vCOFINS")), 2, false), true);
		ICMSTot.addNode("vOutro", getCampo("340-W15","vOutro") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("340-W15","vOutro")), 2, false), true);
		ICMSTot.addNode("vNF", NFeUtils.formatarDecimal(eaa01.eaa01totDoc, 2, false), true);
		//ICMSTot.addNode("vNF", NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal_Zero("total_conv"), 2, false), true)
		ICMSTot.addNode("vTotTrib", getCampo("341a-W16a","vTotTrib") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("341a-W16a","vTotTrib")), 2, true), false);

		/** ISSQNtot - Totais referentes ao ISSQN (W01) */
		if(gerarISSQNTot) {
			ElementXml ISSQNtot = total.addNode("ISSQNtot");
			ISSQNtot.addNode("vServ", getCampo("343-W18","vServ") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("343-W18","vServ")), 2, true), false);
			ISSQNtot.addNode("vBC", getCampo("344-W19","vBC") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("344-W19","vBC")), 2, true), false);
			ISSQNtot.addNode("vISS", getCampo("345-W20","vISS") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("345-W20","vISS")), 2, true), false);
			ISSQNtot.addNode("vPIS", getCampo("346-W21","vPIS") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("346-W21","vPIS")), 2, true), false);
			ISSQNtot.addNode("vCOFINS", getCampo("347-W22","vCOFINS") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347-W22","vCOFINS")), 2, true), false);
			ISSQNtot.addNode("dCompet", eaa01.eaa01central.abb01data == null ? null : NFeUtils.formatarData(eaa01.eaa01central.abb01data), true);
			ISSQNtot.addNode("vDeducao", getCampo("347b-W22b","vDeducao") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347b-W22b","vDeducao")), 2, true), false);
			ISSQNtot.addNode("vOutro", getCampo("347c-W22c","vOutro") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347c-W22c","vOutro")), 2, true), false);
			ISSQNtot.addNode("vDescIncond", getCampo("347d-W22d","vDescIncond") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347d-W22d","vDescIncond")), 2, true), false);
			ISSQNtot.addNode("vDescCond", getCampo("347e-W22e","vDescCond") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347e-W22e","vDescCond")), 2, true), false);
			ISSQNtot.addNode("vISSRet", getCampo("347f-W22f","vISSRet") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("347f-W22f","vISSRet")), 2, true), false);
			ISSQNtot.addNode("cRegTrib", empresa.aac13fiscal.aac13regTrib == 0 ? null : empresa.aac13fiscal.aac13regTrib, false);
		}

		/** retTrib - Retenções de tributos (W23) */
		if((getCampo("349-W24","vRetPIS") != null || getCampo("350-W25","vRetCOFINS") != null || getCampo("351-W26","vRetCSLL") != null || getCampo("353-W28","vIRRF") != null || getCampo("355-W30","vRetPrev") != null) &&
				(jsonEaa01.getBigDecimal_Zero(getCampo("349-W24","vRetPIS")).compareTo(new BigDecimal(0)) != 0 || jsonEaa01.getBigDecimal_Zero(getCampo("350-W25","vRetCOFINS")).compareTo(new BigDecimal(0)) != 0 ||
						jsonEaa01.getBigDecimal_Zero(getCampo("351-W26","vRetCSLL")).compareTo(new BigDecimal(0)) != 0  || jsonEaa01.getBigDecimal_Zero(getCampo("353-W28","vIRRF")).compareTo(new BigDecimal(0)) != 0 ||
						jsonEaa01.getBigDecimal_Zero(getCampo("355-W30","vRetPrev")).compareTo(new BigDecimal(0)) != 0)) {

			ElementXml retTrib = total.addNode("retTrib");
			retTrib.addNode("vRetPIS", getCampo("349-W24","vRetPIS") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("349-W24","vRetPIS")), 2, true), false);
			retTrib.addNode("vRetCOFINS", getCampo("350-W25","vRetCOFINS") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("350-W25","vRetCOFINS")), 2, true), false);
			retTrib.addNode("vRetCSLL", getCampo("351-W26","vRetCSLL") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("351-W26","vRetCSLL")), 2, true), false);
			retTrib.addNode("vBCIRRF", getCampo("352-W27","vBCIRRF") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("352-W27","vBCIRRF")), 2, true), false);
			retTrib.addNode("vIRRF", getCampo("353-W28","vIRRF") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("353-W28","vIRRF")), 2, true), false);
			retTrib.addNode("vBCRetPrev", getCampo("354-W29","vBCRetPrev") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("354-W29","vBCRetPrev")), 2, true), false);
			retTrib.addNode("vRetPrev", getCampo("355-W30","vRetPrev") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("355-W30","vRetPrev")), 2, true), false);
		}

		/** transp - Informações do transporte da NF-e (X01) */
		ElementXml transp = infNfe.addNode("transp");
		transp.addNode("modFrete", eaa0102.eaa0102frete == null ? 9 : eaa0102.eaa0102frete, true);

		Abe01 despacho = eaa0102.eaa0102despacho;
		Abe0101 endDespPrincipal = despacho != null ? getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", despacho.abe01id), Criterions.eq("abe0101principal", 1)) : null;
		if(despacho != null) {

			/** transporta - Transportador (X03) */
			ElementXml transporta = transp.addNode("transporta");
			if(despacho.abe01ti == 1) {
				transporta.addNode("CPF", despacho.abe01ni == null ? null : StringUtils.extractNumbers(despacho.abe01ni), false);
			}else if(despacho.abe01ti == 0) {
				transporta.addNode("CNPJ", despacho.abe01ni == null ? null : StringUtils.extractNumbers(despacho.abe01ni), false);
			}
			transporta.addNode("xNome", despacho.abe01nome, false, 60);
			transporta.addNode("IE", eaa0102.eaa0102contribIcms == 0 ? null : NFeUtils.formatarIE(despacho.abe01ie), false);

			String ie = eaa01.eaa0102DadosGerais != null && eaa01.eaa0102DadosGerais.eaa0102contribIcms == 0 ? null : NFeUtils.formatarIE(despacho.abe01ie);
			//if(ie != null && !ie.equals("ISENTO")) {
			StringBuilder endereco = new StringBuilder();
			if(endDespPrincipal.abe0101endereco != null) endereco.append(endDespPrincipal.abe0101endereco);
			if(endDespPrincipal.abe0101numero != null) endereco.append("," + endDespPrincipal.abe0101numero);
			if(endDespPrincipal.abe0101bairro != null) endereco.append("-" + endDespPrincipal.abe0101bairro);
			if(endDespPrincipal.abe0101complem != null) endereco.append("-" + endDespPrincipal.abe0101complem);
			transporta.addNode("xEnder", endereco.toString().trim(), false, 60);

			if(endDespPrincipal.abe0101municipio == null) {
				transporta.addNode("xMun", null, false);
				transporta.addNode("UF", null, false);
			}else {
				Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endDespPrincipal.abe0101municipio.aag0201uf.aag02id);
				if(aag02.aag02uf.equals("EX")) {
					transporta.addNode("xMun", "EXTERIOR", false);
					transporta.addNode("UF", "EX", false);
				}else {
					transporta.addNode("xMun", endDespPrincipal.abe0101municipio.aag0201nome, false, 60);
					Aag02 uf = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endDespPrincipal.abe0101municipio.aag0201uf.aag02id);
					transporta.addNode("UF", uf.aag02uf, false);
				}
			}
			//}
		}

		/** retTransp - Retenção do ICMS do transporte (X11) */
		if(getCampo("370-X15","vICMSRet") != null && jsonEaa01.getBigDecimal_Zero(getCampo("370-X15","vICMSRet")).compareTo(new BigDecimal(0)) != 0) {
			ElementXml retTransp = transp.addNode("retTransp");
			retTransp.addNode("vServ", getCampo("367-X12","vServ") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("367-X12","vServ")), 2, false), true);
			retTransp.addNode("vBCRet", getCampo("368-X13","vBCRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("368-X13","vBCRet")), 2, false), true);
			retTransp.addNode("pICMSRet", getCampo("369-X14","pICMSRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("369-X14","pICMSRet")), 2, false), true);
			retTransp.addNode("vICMSRet", getCampo("370-X15","vICMSRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("370-X15","vICMSRet")), 2, false), true);
			retTransp.addNode("CFOP", null, false);
			retTransp.addNode("cMunFG", null, false);
		}

		/** veicTransp - Veículo (X18) */
		if(idDest != 2) {
			Aah20 veiculo1 = eaa0102.eaa0102veiculo;
			if(veiculo1 != null) {
				ElementXml veicTransp = transp.addNode("veicTransp");
				veicTransp.addNode("placa", veiculo1.aah20placa, true);
				veicTransp.addNode("UF", veiculo1.aah20ufPlaca, true);
				veicTransp.addNode("RNTC", veiculo1.aah20rntrc, false, 20);
			}

			/** reboque - Reboque (X22) */
			Aah20 reboque1 = eaa0102.eaa0102reboque1;
			if(reboque1 != null) {
				ElementXml reboque = transp.addNode("reboque");
				reboque.addNode("placa", reboque1.aah20placa, true);
				reboque.addNode("UF", reboque1.aah20ufPlaca, true);
				reboque.addNode("RNTC", reboque1.aah20rntrc, false, 20);
				reboque.addNode("vagao", null, false);
				reboque.addNode("balsa", null, false);

				Aah20 reboque2 = eaa0102.eaa0102reboque2;
				if(reboque2 != null) {
					reboque = transp.addNode("reboque");
					reboque.addNode("placa", reboque2.aah20placa, true);
					reboque.addNode("UF", reboque2.aah20ufPlaca, true);
					reboque.addNode("RNTC", reboque2.aah20rntrc, false, 20);
					reboque.addNode("vagao", null, false);
					reboque.addNode("balsa", null, false);

					Aah20 reboque3 = eaa0102.eaa0102reboque3;
					if(reboque3 != null) {
						reboque = transp.addNode("reboque");
						reboque.addNode("placa", reboque3.aah20placa, true);
						reboque.addNode("UF", reboque3.aah20ufPlaca, true);
						reboque.addNode("RNTC", reboque3.aah20rntrc, false, 20);
						reboque.addNode("vagao", null, false);
						reboque.addNode("balsa", null, false);
					}
				}
			}
		}

		/** vol - Volumes (X26) */
		ElementXml vol = transp.addNode("vol");
		vol.addNode("qVol", getCampo("382-X27","qVol") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("382-X27","qVol")), 0, false), false);
		vol.addNode("esp", eaa0102.eaa0102especie, false, 60);
		vol.addNode("marca", eaa0102.eaa0102marca, false, 60);
		vol.addNode("nVol", eaa0102.eaa0102numero, false, 60);
		vol.addNode("pesoL", getCampo("386-X31","pesoL") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("386-X31","pesoL")), 3, true), false);
		vol.addNode("pesoB", getCampo("387-X32","pesoB") == null ? null : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("387-X32","pesoB")), 3, true), false);

		/** cobr - Dados da cobrança (Y01) */
		List<Eaa0113> eaa0113s = buscarFinanceiroPorDocumento(eaa01.eaa01id);

		if(eaa0113s != null && eaa0113s.size() > 0) {
			ElementXml cobr = infNfe.addNode("cobr");

			/** fat - Fatura (Y02) */
			ElementXml fat = cobr.addNode("fat");
			fat.addNode("nFat", eaa01.eaa01central.abb01num, false);
			fat.addNode("vOrig", eaa01.eaa01totFinanc == null ? 0 : NFeUtils.formatarDecimal(eaa01.eaa01totFinanc, 2, false), true);
			fat.addNode("vDesc", getCampo("393-Y05","vDesc") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa01.getBigDecimal(getCampo("393-Y05","vDesc")), 2, false), true);
			fat.addNode("vLiq", eaa01.eaa01totFinanc == null ? 0 : NFeUtils.formatarDecimal(eaa01.eaa01totFinanc, 2, false), true);

			/** dup - Duplicata (Y07) */
			Integer nDup = 1;
			for(Eaa0113 eaa0113 : eaa0113s) {
				if (eaa0113.eaa0113dtVctoN != eaa01.eaa01central.abb01data) {
					if(!eaa0113.eaa0113clasParc.equals(0)) continue;
					if(eaa0113.eaa0113valor.compareTo(new BigDecimal(0)) < 0) continue;
					ElementXml dup = cobr.addNode("dup");
					dup.addNode("nDup", StringUtils.ajustString(nDup, 3, '0', true), false);
					dup.addNode("dVenc", NFeUtils.formatarData(eaa0113.eaa0113dtVctoN, "yyyy-MM-dd"), false);
					dup.addNode("vDup", NFeUtils.formatarDecimal(eaa0113.eaa0113valor, 2, false), true);
					nDup++;
				}
			}
		}

		/** pag - Informações de Pagamento (YA) */
		ElementXml pag = infNfe.addNode("pag");
		boolean temDadosPag = false;

		List<TableMap> eaa01131s = buscarFormasDePagamentoPorDocumento(eaa01.eaa01id);
		if(eaa01131s != null && eaa01131s.size() > 0){
			for(TableMap tmFp : eaa01131s){
				temDadosPag = true;

				ElementXml detPag = pag.addNode("detPag");
				detPag.addNode("indPag", verificarFormaPgto(eaa01), false);
				detPag.addNode("tPag", StringUtils.ajustString(tmFp.getString("abf40meioPgto"), 2), true);

				if(tmFp.getInteger("abf40meioPgto").equals(99)) {
					detPag.addNode("xPag", tmFp.getString("abf40descr"), false)
				}

				detPag.addNode("vPag", NFeUtils.formatarDecimal(tmFp.getBigDecimal("valor"), 2, false), true);
				if(tmFp.getInteger("abf40meioPgto").equals(3) || tmFp.getInteger("abf40meioPgto").equals(4)){
					ElementXml card = detPag.addNode("card");
					card.addNode("tpIntegra", "2", true);
				}
			}
		}

		BigDecimal totalParcelas = buscarTotalParcelado(eaa01.eaa01id);
		if(totalParcelas > 0 && (eaa01131s != [] || eaa01131s.size() > 0 )) {
			temDadosPag = true;

			ElementXml detPag = pag.addNode("detPag");
			detPag.addNode("indPag", verificarFormaPgto(eaa01), false);
			detPag.addNode("tPag", "15", true);
			detPag.addNode("vPag", NFeUtils.formatarDecimal(totalParcelas, 2, false), true);

		}

		if(eaa01131s == null || eaa01131s.size() == 0 ) temDadosPag = false;

		if(!temDadosPag) {
			ElementXml detPag = pag.addNode("detPag");
			detPag.addNode("tPag", "90", true);
			detPag.addNode("vPag", NFeUtils.formatarDecimal(new BigDecimal(0), 2, false), true);
		}

		/** infAdic - Informações Adicionais (Z01) */
		if((eaa01.eaa01obsFisco != null  || eaa01.eaa01obsContrib != null || eaa01.eaa01obsRetInd != null) || (eaa0102.eaa0102processo != null) ||
				(despacho != null && endDespPrincipal != null && endDespPrincipal.abe0101eMail != null)) {
			ElementXml infAdic = infNfe.addNode("infAdic");
			if(eaa01.eaa01obsFisco != null || eaa01.eaa01obsContrib != null || eaa01.eaa01obsRetInd != null) {
				infAdic.addNode("infAdFisco", eaa01.eaa01obsFisco, false, 2000);
				StringBuilder obsContrib = new StringBuilder();
				if(eaa01.eaa01obsContrib != null) obsContrib.append(eaa01.eaa01obsContrib);
				if(obsContrib.length() > 0) obsContrib.append(" ");
				if(eaa01.eaa01obsRetInd != null) obsContrib.append(eaa01.eaa01obsRetInd);
				infAdic.addNode("infCpl", obsContrib.toString().length() == 0 ? null : obsContrib.toString().trim(), false, 5000);
			}

			/** Enviamos o e-mail da transportadora para uso do MultiNFe */
			if(endDespPrincipal != null && endDespPrincipal.abe0101eMail != null) {
				ElementXml obsCont = infAdic.addNode("obsCont");
				obsCont.setAttribute("xCampo", "emailTransportadora");
				obsCont.addNode("xTexto", endDespPrincipal.abe0101eMail, true, 60);
			}

			/** Email dos representantes para uso do MultiNFe */
			if(eaa01.eaa01rep0 != null) {
				Abe0101 endRep = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", eaa01.eaa01rep0.abe01id), Criterions.eq("abe0101principal", 1))
				if(endRep != null && endRep.abe0101eMail != null){
					ElementXml obsCont = infAdic.addNode("obsCont");
					obsCont.setAttribute("xCampo", "emailTransportadora");
					obsCont.addNode("xTexto", endRep.abe0101eMail, true, 60);
				}
			}
			if(eaa01.eaa01rep1 != null) {
				Abe0101 endRep = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", eaa01.eaa01rep1.abe01id), Criterions.eq("abe0101principal", 1))
				if(endRep != null && endRep.abe0101eMail != null){
					ElementXml obsCont = infAdic.addNode("obsCont");
					obsCont.setAttribute("xCampo", "emailTransportadora");
					obsCont.addNode("xTexto", endRep.abe0101eMail, true, 60);
				}
			}
			if(eaa01.eaa01rep2 != null) {
				Abe0101 endRep = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", eaa01.eaa01rep2.abe01id), Criterions.eq("abe0101principal", 1))
				if(endRep != null && endRep.abe0101eMail != null){
					ElementXml obsCont = infAdic.addNode("obsCont");
					obsCont.setAttribute("xCampo", "emailTransportadora");
					obsCont.addNode("xTexto", endRep.abe0101eMail, true, 60);
				}
			}
			if(eaa01.eaa01rep3 != null) {
				Abe0101 endRep = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", eaa01.eaa01rep3.abe01id), Criterions.eq("abe0101principal", 1))
				if(endRep != null && endRep.abe0101eMail != null){
					ElementXml obsCont = infAdic.addNode("obsCont");
					obsCont.setAttribute("xCampo", "emailTransportadora");
					obsCont.addNode("xTexto", endRep.abe0101eMail, true, 60);
				}
			}
			if(eaa01.eaa01rep4 != null) {
				Abe0101 endRep = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", eaa01.eaa01rep4.abe01id), Criterions.eq("abe0101principal", 1))
				if(endRep != null && endRep.abe0101eMail != null){
					ElementXml obsCont = infAdic.addNode("obsCont");
					obsCont.setAttribute("xCampo", "emailTransportadora");
					obsCont.addNode("xTexto", endRep.abe0101eMail, true, 60);
				}
			}

			/** procRef - Processo referenciado (Z10) */
			if(eaa0102.eaa0102processo != null) {
				ElementXml procRef = infAdic.addNode("procRef");
				procRef.addNode("nProc", eaa0102.eaa0102processo.abb40num, true, 60);
				procRef.addNode("indProc", eaa0102.eaa0102processo.abb40indProc, true);
			}
		}

		/** exporta - Informações de comércio exterior (ZA) */
		List<Eaa0104> eaa0104s = buscarDeclaracoesDeExportacao(eaa01.eaa01id);
		for(Eaa0104 eaa0104 : eaa0104s) {
			if(eaa0104.eaa0104embLocal != null) {
				ElementXml exporta = infNfe.addNode("exporta");
				exporta.addNode("UFSaidaPais", eaa0104.eaa0104embUF == null ? null : eaa0104.eaa0104embUF.aag02uf, true);
				exporta.addNode("xLocExporta", eaa0104.eaa0104embLocal, true, 60);
				exporta.addNode("xLocDespacho", null, false);
			}
		}

		/** infRespTec - Informações do Responsável Técnico (ZD) */
		ElementXml infRespTec = infNfe.addNode("infRespTec");
		infRespTec.addNode("CNPJ", StringUtils.extractNumbers(empresa.aac10aCnpj), true);
		infRespTec.addNode("xContato", empresa.aac10aNome, true, 60);
		infRespTec.addNode("email", empresa.aac10aEmail, true, 60);
		infRespTec.addNode("fone", NFeUtils.ajustarFone(empresa.aac10aDddFone, empresa.aac10aFone), true);
		//infRespTec.addNode("idCSRT", "01", true);
		//infRespTec.addNode("hashCSRT", null, true);

		/** Gera o XML */
		String dados = NFeUtils.gerarXML(nfe);

		put("chaveNfe", chaveNfe);
		put("dados", dados);
//		put("tagsAssinar", "infNFe"); //Descomentar essa linha quando a nota precisar ser assinada pelo SAM
	}

	private void emitente() {
		/** emit - Identificação do Emitente da Nota Fiscal eletrônica (C01) */
		emit = infNfe.addNode("emit");
		if(empresa.aac10ti == 0) {
			emit.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10ni), 14), true);
		}else {
			emit.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10ni), 11), true);
		}
		emit.addNode("xNome", empresa.aac10rs, true, 60);
		emit.addNode("xFant", empresa.aac10fantasia, false, 60);

		/**	enderEmit - Endereço (C05) */
		enderEmit = emit.addNode("enderEmit");
		enderEmit.addNode("xLgr", empresa.aac10endereco, true, 60);
		enderEmit.addNode("nro", empresa.aac10numero, true);
		enderEmit.addNode("xCpl", empresa.aac10complem, false, 60);
		enderEmit.addNode("xBairro", empresa.aac10bairro, true, 60);
		enderEmit.addNode("cMun", empresa.aac10municipio.aag0201ibge, true);
		enderEmit.addNode("xMun", empresa.aac10municipio.aag0201nome, true, 60);
		enderEmit.addNode("UF", empresa.aac10municipio.aag0201uf.aag02uf, true);
		enderEmit.addNode("CEP", empresa.aac10cep, true);
		enderEmit.addNode("cPais", empresa.aac10pais.aag01bacen, true);
		enderEmit.addNode("xPais", empresa.aac10pais.aag01nome, false, 60);
		enderEmit.addNode("fone", NFeUtils.ajustarFone(empresa.aac10dddFone, empresa.aac10fone), false);

		Aac1002 ieEstado = buscarInscricaoEstadualPorEstado(empresa);
		emit.addNode("IE", NFeUtils.formatarIE(ieEstado.aac1002ie), true);
		emit.addNode("IEST", eaa0102.eaa0102ieST == null ? null : StringUtils.extractNumbers(eaa0102.eaa0102ieST), false);
		emit.addNode("IM", empresa.aac10im == null ? null : NFeUtils.formatarIE(empresa.aac10im), false);
		emit.addNode("CNAE", empresa.aac10im == null ? null : empresa.aac10cnae == null ? null : StringUtils.extractNumbers(empresa.aac10cnae), false);
		emit.addNode("CRT", empresa.aac13fiscal.aac13classTrib.aaj01nfe, true);
	}


	private void destinatario() {
		/** dest - Identificação do Destinatário da Nota Fiscal Eletrônica (E01) */
		indIEDest = 1;
		if(modelo.equals("55") || endPrincipal != null) {
			dest = infNfe.addNode("dest");
			Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endPrincipal.eaa0101municipio.aag0201uf.aag02id);
			if(aag02.aag02uf.equalsIgnoreCase("EX")) {
				dest.addNode("idEstrangeiro");
				dest.addNode("xNome", eaa0102.eaa0102nome, true);
			}else {
				if (isProducao != false) {
					if(eaa0102.eaa0102ti == 0) {
						dest.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(eaa0102.eaa0102ni), 14), true);
					}else {
						dest.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(eaa0102.eaa0102ni), 11), true);
					}
					dest.addNode("xNome", eaa0102.eaa0102nome, true);
				}else {
					dest.addNode("CNPJ", "99999999000191", true);
					dest.addNode("xNome", "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL", true);
				}
			}

			/** enderDest - Endereço do destinatário (E05) */
			enderDest = dest.addNode("enderDest");
			enderDest.addNode("xLgr", endPrincipal.eaa0101endereco, true, 60);
			enderDest.addNode("nro", endPrincipal.eaa0101numero, true);
			enderDest.addNode("xCpl", endPrincipal.eaa0101complem, false, 60);
			enderDest.addNode("xBairro", endPrincipal.eaa0101bairro, true, 60);

			if(aag02.aag02uf.equalsIgnoreCase("EX")) {
				enderDest.addNode("cMun", 9999999, true);
				enderDest.addNode("xMun", "EXTERIOR", true);
				enderDest.addNode("UF", "EX", true);
			}else {
				enderDest.addNode("cMun", endPrincipal.eaa0101municipio.aag0201ibge, true);
				enderDest.addNode("xMun", endPrincipal.eaa0101municipio.aag0201nome, true, 60);
				enderDest.addNode("UF", aag02.aag02uf, true);
			}

			enderDest.addNode("CEP", endPrincipal.eaa0101cep, false);
			enderDest.addNode("cPais", endPrincipal.eaa0101pais == null ? null : endPrincipal.eaa0101pais.aag01bacen, false);
			enderDest.addNode("xPais", endPrincipal.eaa0101pais == null ? null : endPrincipal.eaa0101pais.aag01nome, false, 60);
			enderDest.addNode("fone", NFeUtils.ajustarFone(endPrincipal.eaa0101ddd, endPrincipal.eaa0101fone), false);

			String IE = NFeUtils.formatarIE(eaa0102.eaa0102ie);

			if(modelo.equals("65") || eaa0102.eaa0102contribIcms == 0 || aag02.aag02uf.equalsIgnoreCase("EX")) {
				indIEDest = 9;
				if(aag02.aag02uf.equalsIgnoreCase("EX") || "ISENTO".equals(IE)) IE = null;
			}else {
				if(IE.equalsIgnoreCase("ISENTO") || IE.equalsIgnoreCase("ISENTA")) {
					indIEDest = 2;
				}
			}
			if(!isProducao) indIEDest = 2;

			dest.addNode("indIEDest", indIEDest, true);
			dest.addNode("IE", indIEDest == 2 ? null : IE, false);

			dest.addNode("ISUF", eaa0102.eaa0102suframa == null ? null : StringUtils.extractNumbers(eaa0102.eaa0102suframa), false);
			dest.addNode("IM", eaa0102.eaa0102im == null ? null : NFeUtils.formatarIE(eaa0102.eaa0102im), false);

			String email = eaa0102.eaa0102eMail;
			dest.addNode("email", email, false, 60);
		}

		/** retirada - Identificação do local de retirada (F01) */
		if(endSaida != null) {
			retirada = infNfe.addNode("retirada");
			if(endSaida.eaa0101ni != null) {
				if(endSaida.eaa0101ti == 0) {
					retirada.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(endSaida.eaa0101ni), 14), true);
				}else {
					retirada.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(endSaida.eaa0101ni), 11), true);
				}
			}else {
				retirada.addNode("CNPJ");
			}

			retirada.addNode("xNome", endSaida.eaa0101rs, false, 60);
			retirada.addNode("xLgr", endSaida.eaa0101endereco, true, 60);
			retirada.addNode("nro", endSaida.eaa0101numero, true);
			retirada.addNode("xCpl", endSaida.eaa0101complem, false, 60);
			retirada.addNode("xBairro", endSaida.eaa0101bairro, true, 60);

			Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endSaida.eaa0101municipio.aag0201uf.aag02id);
			if(aag02.aag02uf.equalsIgnoreCase("EX")) {
				retirada.addNode("cMun", 9999999, true);
				retirada.addNode("xMun", "EXTERIOR", true);
				retirada.addNode("UF", "EX", true);
			}else {
				retirada.addNode("cMun", endSaida.eaa0101municipio.aag0201ibge, true);
				retirada.addNode("xMun", endSaida.eaa0101municipio.aag0201nome, true, 60);
				retirada.addNode("UF", aag02.aag02uf, true);
			}

			retirada.addNode("CEP", endSaida.eaa0101cep, false);
			retirada.addNode("fone", NFeUtils.ajustarFone(endSaida.eaa0101ddd, endSaida.eaa0101fone), false);
			retirada.addNode("email", endSaida.eaa0101eMail, false, 60);

			retirada.addNode("IE", endSaida.eaa0101ie == null ? null : StringUtils.extractNumbers(endSaida.eaa0101ie), false);
		}

		/** entrega - Identificação do local de entrega (G01) */
		if(endEntrega != null) {
			Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endEntrega.eaa0101municipio.aag0201uf.aag02id);
			entrega = infNfe.addNode("entrega");
			if(endEntrega.eaa0101ni != null) {
				if(endEntrega.eaa0101ti == 0) {
					entrega.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(endEntrega.eaa0101ni), 14), true);
				}else {
					entrega.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(endEntrega.eaa0101ni), 11), true);
				}
			}else {
				entrega.addNode("CNPJ");
			}

			entrega.addNode("xNome", endEntrega.eaa0101rs, false, 60);
			entrega.addNode("xLgr", endEntrega.eaa0101endereco, true, 60);
			entrega.addNode("nro", endEntrega.eaa0101numero, true);
			entrega.addNode("xCpl", endEntrega.eaa0101complem, false, 60);
			entrega.addNode("xBairro", endEntrega.eaa0101bairro, true, 60);

			if(aag02.aag02uf.equalsIgnoreCase("EX")) {
				entrega.addNode("cMun", 9999999, true);
				entrega.addNode("xMun", "EXTERIOR", true);
				entrega.addNode("UF", "EX", true);
			}else {
				entrega.addNode("cMun", endEntrega.eaa0101municipio.aag0201ibge, true);
				entrega.addNode("xMun", endEntrega.eaa0101municipio.aag0201nome, true, 60);
				entrega.addNode("UF", aag02.aag02uf, true);
			}

			entrega.addNode("CEP", endEntrega.eaa0101cep, false);
			entrega.addNode("fone", NFeUtils.ajustarFone(endEntrega.eaa0101ddd, endEntrega.eaa0101fone), false);
			entrega.addNode("email", endEntrega.eaa0101eMail, false, 60);

			entrega.addNode("IE", endEntrega.eaa0101ie == null ? null : NFeUtils.formatarIE(endEntrega.eaa0101ie), false);
		}
	}


	private void item() {
		/** autXML - Autorização para obter XML (GA) */
		String niDest = StringUtils.extractNumbers(endPrincipal.eaa0101ni);
		if(empresa.aac10cCnpj != null && !niDest.equals(StringUtils.extractNumbers(empresa.aac10cCnpj))) {
			autXML = infNfe.addNode("autXML");
			autXML.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10cCnpj), 14), true);
		}
		if(empresa.aac10cCpf != null && !niDest.equals(StringUtils.extractNumbers(empresa.aac10cCpf))) {
			autXML = infNfe.addNode("autXML");
			autXML.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10cCpf), 11), true);
		}

		/** det - Detalhamento de Produtos e Serviços da NF-e (H01) */
		List<Eaa0103> eaa0103s = buscarItensDoDocumento(eaa01.eaa01id);
		TreeSet<Eaa0103> eaa0103sOrdem = new TreeSet<Eaa0103>(new Comparator<Eaa0103>(){
			public int compare(Eaa0103 o1, Eaa0103 o2) {
				return o1.eaa0103seq.compareTo(o2.eaa0103seq);
			}
		});
		eaa0103sOrdem.addAll(eaa0103s);

		gerarISSQNTot = false;

		int item = 1;
		for(Eaa0103 eaa0103 : eaa0103sOrdem) {
			TableMap jsonEaa0103 = eaa0103.eaa0103json;

			det = infNfe.addNode("det");
			det.setAttribute("nItem", item.toString());

			Abm01 abm01 = getAcessoAoBanco().buscarRegistroUnicoById("Abm01", eaa0103.eaa0103item.abm01id);
			Boolean temISS = abm01.abm01tipo < 3 ? false : eaa0103.eaa0103issExig.compareTo(0) > 0;

			/** prod - Produtos e Serviços da NF-e (I01) */
			prod = det.addNode("prod");
			prod.addNode("cProd", eaa0103.eaa0103codigo, true, 60);
			prod.addNode("cEAN", eaa0103.eaa0103gtin == null ? "SEM GTIN" : eaa0103.eaa0103gtin, true);
			prod.addNode("xProd", eaa0103.eaa0103descr, true, 120);

			String ncm = null;
			Abg01 abg01 = eaa0103.eaa0103ncm == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Abg01", eaa0103.eaa0103ncm.abg01id);
			if(abm01.abm01tipo == 2 && temISS) {
				ncm = "00";
			}else {
				ncm = abg01 == null ? null : abg01.abg01codigo;
				if(ncm != null) {
					ncm = ncm.indexOf("/") == -1 ? ncm : ncm.substring(0, ncm.indexOf("/"));
				}
			}

			prod.addNode("NCM", ncm, true);

			List<Abg0101> abg0101s = buscarNVEsPorNCM(abg01.abg01id);
			if(abg01 != null && abg0101s != null && abg0101s.size() > 0) {
				int i = 1;
				for(Abg0101 abg0101 : abg0101s) {
					if(i == 9) continue;
					prod.addNode("NVE", abg0101.abg0101codigo, false);
					i++;
				}
			}
			if(eaa0103.eaa0103cest != null) {
				prod.addNode("CEST", eaa0103.eaa0103cest, true);
				prod.addNode("indEscala", eaa0103.eaa0103prodRelev == 0 ? null : eaa0103.eaa0103prodRelev == 1 ? "N" : "S", false);
				prod.addNode("CNPJFab", StringUtils.extractNumbers(eaa0103.eaa0103cnpjFabr), false);
			}
			prod.addNode("cBenef", null, false);
			if(abm01.abm01tipo != 2) {
				prod.addNode("EXTIPI", ncm == null ? null : abg01.abg01codigo.indexOf("/") == -1 ? null : abg01.abg01codigo.substring(abg01.abg01codigo.indexOf("/") + 1,abg01.abg01codigo.length()), false);
				//interromper(abg01.abg01codigo.substring(abg01.abg01codigo.indexOf("/") + 1,abg01.abg01codigo.length()))
			}

			Aaj15 aaj45 = getAcessoAoBanco().buscarRegistroUnicoById("Aaj15", eaa0103.eaa0103cfop.aaj15id);
			prod.addNode("CFOP", aaj45.aaj15codigo, true);

			Aam06 aam06 = getAcessoAoBanco().buscarRegistroUnicoById("Aam06", eaa0103.eaa0103umComl.aam06id);
			prod.addNode("uCom", jsonEaa0103.getString("umv"), true, 6);

			//prod.addNode("qCom", eaa0103.eaa0103qtComl == null ? 0 : NFeUtils.formatarDecimal(eaa0103.eaa0103qtComl, 4, false), true);
			prod.addNode("qCom",  NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal("qt_convertida"),4, false), true);
			prod.addNode("vUnCom",  NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal("unitario_conv"),5, false), true);
			prod.addNode("vProd", jsonEaa0103.getBigDecimal_Zero("total_conv") == 0 ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal("total_conv"), 2, false), true);
			prod.addNode("cEANTrib", eaa0103.eaa0103gtinTrib == null ? "SEM GTIN" : eaa0103.eaa0103gtinTrib, true);

			//Aam06 aam06UMT = eaa0103.eaa0103umt == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aam06", eaa0103.eaa0103umt.aam06id);
			prod.addNode("uTrib", jsonEaa0103.getString("umv"), true, 6);

			prod.addNode("qTrib", jsonEaa0103.getBigDecimal("qt_convertida") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal("qt_convertida"),4, false), true);
			prod.addNode("vUnTrib",jsonEaa0103.getBigDecimal("unitario_conv") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal("unitario_conv"),5, false), true);
			prod.addNode("vFrete", getCampo("114-I15","vFrete") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("114-I15","vFrete")), 2, true), false);
			prod.addNode("vSeg", getCampo("115-I16","vSeg") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("115-I16","vSeg")), 2, true), false);
			prod.addNode("vDesc", getCampo("116-I17","vDesc") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("116-I17","vDesc")), 2, true), false);
			prod.addNode("vOutro", getCampo("116a-I17a","vOutro") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("116a-I17a","vOutro")), 2, true), false);
			prod.addNode("indTot", eaa0103.eaa0103soma, true);

			/** DI - Declaração da importação (I01) */
			List<Eaa01034> eaa01034s = buscarDeclaracaoDeImportacaoPorItem(eaa0103.eaa0103id);
			for(Eaa01034 eaa01034 : eaa01034s) {
				DI = prod.addNode("DI");
				DI.addNode("nDI", eaa01034.eaa01034num, true, 12);
				DI.addNode("dDI", eaa01034.eaa01034dtReg == null ? null : eaa01034.eaa01034dtReg.toString() , true);
				DI.addNode("xLocDesemb", eaa01034.eaa01034local, true, 60);
				Aag02 ufLocal = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", eaa01034.eaa01034ufLocal.aag02id);
				DI.addNode("UFDesemb", ufLocal == null ? null : ufLocal.aag02uf, true);
				DI.addNode("dDesemb", eaa01034.eaa01034dtDesemb == null ? null : eaa01034.eaa01034dtDesemb.toString(), true);
				DI.addNode("tpViaTransp", eaa01034.eaa01034viaTransp, true);
				DI.addNode("vAFRMM", NFeUtils.formatarDecimal(eaa01034.eaa01034afrmm, 2, false), false);
				DI.addNode("tpIntermedio", eaa01034.eaa01034formaImp, true);
				DI.addNode("CNPJ", eaa01034.eaa01034cnpjAdq == null ? null : StringUtils.extractNumbers(eaa01034.eaa01034cnpjAdq), false);
				Aag02 ufAdq = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", eaa01034.eaa01034ufAdq.aag02id);
				DI.addNode("UFTerceiro", ufAdq == null ? null : ufAdq.aag02uf, false);
				DI.addNode("cExportador", eaa01034.eaa01034codExp, true, 60);

				/** adi - Adições */
				List<Eaa010341> eaa010341s = buscarAdicoesPorDI(eaa01034.eaa01034id);
				for(Eaa010341 eaa010341 : eaa010341s) {
					if(!eaa010341.eaa010341seq.equals(eaa0103.eaa0103seq)) continue;
					adi = DI.addNode("adi");
					adi.addNode("nAdicao", eaa010341.eaa010341num, true);
					adi.addNode("nSeqAdic", eaa010341.eaa010341seq, true);
					adi.addNode("cFabricante", eaa010341.eaa010341codFabr, true, 60);
					adi.addNode("vDescDI", NFeUtils.formatarDecimal(eaa010341.eaa010341desc, 2, true), false);
					adi.addNode("nDraw", eaa01034.eaa01034drawback == null ? null : StringUtils.extractNumbers(eaa01034.eaa01034drawback), false);
				}
			}

			/** detExport - Grupo de Exportação (I03) */
			List<Eaa0104> eaa0104s = buscarDeclaracoesDeExportacao(eaa01.eaa01id);
			for(Eaa0104 eaa0104 : eaa0104s) {
				detExport = prod.addNode("detExport");
				detExport.addNode("nDraw", eaa0104.eaa0104drawback == null ? null : StringUtils.extractNumbers(eaa0104.eaa0104drawback), false);
			}

			/** Pedido de Compra (I05) */
			prod.addNode("xPed", eaa0103.eaa0103pcNum, false, 15);
			prod.addNode("nItemPed", eaa0103.eaa0103pcSeq != null ? StringUtils.ajustString(eaa0103.eaa0103pcSeq, 6) : eaa0103.eaa0103pcSeq, false);

			/** Grupo Diversos (I07) */
			prod.addNode("nFCI", null, false);

			/** Rastreabilidade de Produto (I80)  */
//			if(eaa0103.eaa0103rastrNfe == 1) {
//				Boolean temLote = false;
//				List<Eaa01038> eaa01038s = buscarRastreabilidadeDoItem(eaa0103.eaa0103id);
//				for(Eaa01038 eaa01038 : eaa01038s) {
//					if(eaa01038.eaa01038lote != null){
//						temLote = true;
//						break;
//					}
//				}
//
//				if(temLote){
//					List<TableMap> lctosItens = buscarLancamentosDoItem(eaa01.eaa01central.abb01id);
//					for(TableMap lcto : lctosItens) {
//						if(lcto.get("bcc01lote") != null){
//							rastro = prod.addNode("rastro");
//							rastro.addNode("nLote", lcto.get("bcc01lote"), true);
//							rastro.addNode("qLote", NFeUtils.formatarDecimal(lcto.get("bcc01qt"), 3, false), true);
//						     rastro.addNode("dFab", lcto.get("bcc01fabric") == null ? null : NFeUtils.formatarData(lcto.getDate("bcc01fabric")), true);
//						     rastro.addNode("dVal", lcto.get("bcc01validade") == null ? null : NFeUtils.formatarData(lcto.getDate("bcc01validade")), true);
//							rastro.addNode("dFab", lcto.get("bcc01fabric") == null ? null : lcto.getDate("bcc01fabric"), true);
//							rastro.addNode("dVal", lcto.get("bcc01validade") == null ? null : lcto.getDate("bcc01validade"), true);
//							rastro.addNode("cAgreg", null, false);
//						}
//					}
//				}
//			}

			/** comb - Detalhamento específico de combustíveis (L101) */
			Abm0101 configItem = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", eaa0103.eaa0103item.abm01id), Criterions.eq("abm0101empresa", empresa.aac10id));
			if(configItem != null && configItem.abm0101fiscal != null && configItem.abm0101fiscal.abm12codANP != null) {
				comb = prod.addNode("comb");
				Aaj04 aaj04 = configItem.abm0101fiscal.abm12codANP == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aaj04", configItem.abm0101fiscal.abm12codANP.aaj04id);
				comb.addNode("cProdANP", aaj04 == null ? null : aaj04.aaj04codigo, false, 9);
				comb.addNode("descANP", aaj04 == null ? null : aaj04.aaj04descr, false, 95);
				comb.addNode("pGLP", getCampo("162b2-LA03a","pGLP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162b2-LA03a","pGLP")), 4, true), false);
				comb.addNode("pGNn", getCampo("162b3-LA03b","pGNn") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162b3-LA03b","pGNn")), 4, true), false);
				comb.addNode("pGNi", getCampo("162b4-LA03c","pGNi") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162b4-LA03c","pGNi")), 4, true), false);
				comb.addNode("vPart", getCampo("162b5-LA03d","vPart") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162b5-LA03d","vPart")), 2, true), false);
				comb.addNode("CODIF", null, false);
				comb.addNode("qTemp", getCampo("162d-LA05","qTemp") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162d-LA05","qTemp")), 4, true), false);
				Aag02 uf = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endPrincipal.eaa0101municipio.aag0201uf.aag02id);
				comb.addNode("UFCons", uf.aag02uf, true);

				/** CIDE - CIDE (L105) */
				if(getCampo("162i-LA10","vCIDE") != null && jsonEaa0103.getBigDecimal_Zero(getCampo("162i-LA10","vCIDE")).compareTo(new BigDecimal(0)) > 0) {
					CIDE = comb.addNode("CIDE");
					CIDE.addNode("qBCProd", getCampo("162g-LA08","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162g-LA08","qBCProd")), 4, false), true);
					CIDE.addNode("vAliqProd", getCampo("162h-LA09","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162h-LA09","vAliqProd")), 4, false), true);
					CIDE.addNode("vCIDE", getCampo("162i-LA10","vCIDE") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("162i-LA10","vCIDE")), 2, false), true);
				}
			}

			/** imposto - Tributos incidentes no Produto ou Serviço (M01) */
			imposto = det.addNode("imposto");
			imposto.addNode("vTotTrib", getCampo("163a-M02","vTotTrib") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("163a-M02","vTotTrib")), 2, true), false);

			//Se houver ISS não gerar ICMS, IPI e II, e vice-versa

			/** ISSQN - ISSQN (U01)*/
			if(abm01.abm01tipo == 2 && temISS) {
				gerarISSQNTot = true;
				ISSQN = imposto.addNode("ISSQN");
				ISSQN.addNode("vBC", getCampo("320-U02","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("320-U02","vBC")), 2, false), true);
				ISSQN.addNode("vAliq", getCampo("321-U03","vAliq") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("321-U03","vAliq")), 2, false), true);
				ISSQN.addNode("vISSQN", getCampo("322-U04","vISSQN") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("322-U04","vISSQN")), 2, false), true);
				ISSQN.addNode("cMunFG", endPrincipal.eaa0101municipio.aag0201ibge == null ? null : endPrincipal.eaa0101municipio.aag0201ibge, true);
				ISSQN.addNode("cListServ", eaa0103.eaa0103codServ == null ? null : eaa0103.eaa0103codServ.aaj05federal, true);
				ISSQN.addNode("vDeducao", getCampo("324a-U07","vDeducao") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324a-U07","vDeducao")), 2, true), false);
				ISSQN.addNode("vOutro", getCampo("324b-U08","vOutro") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324b-U08","vOutro")), 2, true), false);
				ISSQN.addNode("vDescIncon", getCampo("324c-U09","vDescIncon") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324c-U09","vDescIncon")), 2, true), false);
				ISSQN.addNode("vDescCond", getCampo("324d-U10","vDescCond") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324d-U10","vDescCond")), 2, true), false);
				ISSQN.addNode("vISSRet", getCampo("324f-U11","vISSRet") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324f-U11","vISSRet")), 2, true), false);
				ISSQN.addNode("indISS", eaa0103.eaa0103issExig, true);
				ISSQN.addNode("cServico", eaa0103.eaa0103codServ == null ? null : eaa0103.eaa0103codServ.aaj05municipal, false);
				ISSQN.addNode("cMun", empresa.aac10municipio.aag0201ibge == null ? null : empresa.aac10municipio.aag0201ibge, false);
				ISSQN.addNode("cPais", endPrincipal.eaa0101pais.aag01bacen == null ? null : endPrincipal.eaa0101pais.aag01bacen, false);
				ISSQN.addNode("nProcesso", null, false);
				ISSQN.addNode("indIncentivo", eaa0103.eaa0103incFisc == 1 ? 1 : 2, true);

			}else {
				/** ICMS - ICMS Normal e ST (N01) */
				ICMS = imposto.addNode("ICMS");
				if(empresa.aac13fiscal.aac13classTrib.aaj01nfe.equals("1")) { //Simples nacional
					Aaj14 aaj14 = eaa0103.eaa0103csosn == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aaj14", eaa0103.eaa0103csosn.aaj14id);
					String csosn = aaj14 == null ? null : aaj14.aaj14codigo;

					if(csosn != null) {
						String orig = csosn.substring(0, 1);
						String cso = csosn.substring(1);

						if(cso.equals("101")) {
							/** ICMSSN101 - CSOSN = 101 - Tributada pelo SN com permissão de crédito */
							ICMSSN101 = ICMS.addNode("ICMSSN101");
							ICMSSN101.addNode("orig", orig, true);
							ICMSSN101.addNode("CSOSN", cso, true);
							ICMSSN101.addNode("pCredSN", getCampo("245.27-N29","pCredSN") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.27-N29","pCredSN")), 2, false), true);
							ICMSSN101.addNode("vCredICMSSN", getCampo("245.28-N30","vCredICMSSN") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.28-N30","vCredICMSSN")), 2, false), true);

						}else if(cso.equals("102") || cso.equals("103") || cso.equals("300") || cso.equals("400")) {
							/** ICMSSN102 - CSOSN = 102 - Tributada pelo SN sem permissão de crédito, 103 - Isenção do ICMS no SN para faixa de receita bruta, 300 - Imune, 400 - Não tributada pelo SN */
							ICMSSN102 = ICMS.addNode("ICMSSN102");
							ICMSSN102.addNode("orig", orig, true);
							ICMSSN102.addNode("CSOSN", cso, true);

						}else if(cso.equals("201")) {
							/** ICMSSN201 - CSOSN = 201 - Tributada pelo SN com permissão de crédito e com cobrança do ICMS por ST */
							ICMSSN201 = ICMS.addNode("ICMSSN201");
							ICMSSN201.addNode("orig", orig, true);
							ICMSSN201.addNode("CSOSN", cso, true);
							ICMSSN201.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
							ICMSSN201.addNode("pMVAST", getCampo("245.31-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.31-N19","tx_iva_st")), 2, true), false);
							ICMSSN201.addNode("pRedBCST", getCampo("224.32-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("224.32-N20","pRedBCST")), 2, true), false);
							ICMSSN201.addNode("vBCST", getCampo("245.33-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.33-N21","vBCST")), 2, false), true);
							ICMSSN201.addNode("pICMSST", getCampo("245.34-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.34-N22","pICMSST")), 2, false), true);
							ICMSSN201.addNode("vICMSST", getCampo("245.35-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.35-N23","vICMSST")), 2, false), true);
							ICMSSN201.addNode("vBCFCPST", getCampo("245.35w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.35w-N23a","vBCFCPST")), 2, true), false);
							ICMSSN201.addNode("pFCPST", getCampo("245.35x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.35x-N23b","pFCPST")), 4, true), false);
							ICMSSN201.addNode("vFCPST", getCampo("245.35y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.35y-N23d","vFCPST")), 2, true), false);
							ICMSSN201.addNode("pCredSN", getCampo("245.36-N29","pCredSN") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.36-N29","pCredSN")), 4, false), true);
							ICMSSN201.addNode("vCredICMSSN", getCampo("245.37-N30","vCredICMSSN") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.37-N30","vCredICMSSN")), 2, false), true);

						}else if(cso.equals("202") || cso.equals("203")) {
							/** ICMSSN202 - CSOSN = 202 - Tributada pelo SN sem permissão de crédito e com cobrança do ICMS por ST, 203 - Isenção do ICMS nos SN para faixa de receita bruta e com cobrança do ICMS por ST */
							ICMSSN202 = ICMS.addNode("ICMSSN202");
							ICMSSN202.addNode("orig", orig, true);
							ICMSSN202.addNode("CSOSN", cso, true);
							ICMSSN202.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
							ICMSSN202.addNode("pMVAST", getCampo("245.42-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.42-N19","tx_iva_st")), 2, true), false);
							ICMSSN202.addNode("pRedBCST", getCampo("224.43-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("224.43-N20","pRedBCST")), 2, true), false);
							ICMSSN202.addNode("vBCST", getCampo("245.44-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.44-N21","vBCST")), 2, false), true);
							ICMSSN202.addNode("pICMSST", getCampo("245.45-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.45-N22","pICMSST")), 2, false), true);
							ICMSSN202.addNode("vICMSST", getCampo("245.46-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.46-N23","vICMSST")), 2, false), true);
							ICMSSN202.addNode("vBCFCPST", getCampo("245.46w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.46w-N23a","vBCFCPST")), 2, true), false);
							ICMSSN202.addNode("pFCPST", getCampo("245.46x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.46x-N23b","pFCPST")), 4, true), false);
							ICMSSN202.addNode("vFCPST", getCampo("245.46y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.46y-N23d","vFCPST")), 2, true), false);

						}else if(cso.equals("500")) {
							/** ICMSSN500 = CSOSN = 500 - ICMS cobrado anteriormente por ST (substituído) ou por antecipação */
							ICMSSN500 = ICMS.addNode("ICMSSN500");
							ICMSSN500.addNode("orig", orig, true);
							ICMSSN500.addNode("CSOSN", cso, true);

//							if(getCampo("245.50-N26","vBCSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.50-N26","vBCSTRet")).equals(0)) {
							ICMSSN500.addNode("vBCSTRet", getCampo("245.50-N26","vBCSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.50-N26","vBCSTRet")), 2, false), true);
							ICMSSN500.addNode("pST", getCampo("245.50.0-N26a","pST") == null ? "0.00" : jsonEaa0103.getBigDecimal(getCampo("245.50.0-N26a","pST")).equals(0) ? "0.00" : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.50.0-N26a","pST")), 4, false), true);
							ICMSSN500.addNode("vICMSSubstituto", getCampo("245.50.1-N26b","vICMSSubstituto") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.50.1-N26b","vICMSSubstituto")), 2, false), true);
							ICMSSN500.addNode("vICMSSTRet", getCampo("245.51-N27","vICMSSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51-N27","vICMSSTRet")), 2, false), true);
//							}

							if(getCampo("245.51w-N27a","vBCFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.51w-N27a","vBCFCPSTRet")) .equals(0) ||
									getCampo("245.51y-N27f","vFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.51y-N27f","vFCPSTRet")).equals(0)) {

								ICMSSN500.addNode("vBCFCPSTRet", getCampo("245.51w-N27a","vBCFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51w-N27a","vBCFCPSTRet")), 2, false), true);
								ICMSSN500.addNode("pFCPSTRet", getCampo("245.51x-N27b","pFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51x-N27b","pFCPSTRet")), 4, false), true);
								ICMSSN500.addNode("vFCPSTRet", getCampo("245.51y-N27f","vFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51y-N27f","vFCPSTRet")), 2, false), true);
							}

							if(getCampo("245.51.3-N35","vBCEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.51.3-N35","vBCEfet")).equals(0) ||
									getCampo("245.51.5-N37","vICMSEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.51.5-N37","vICMSEfet")).equals(0)) {

								ICMSSN500.addNode("pRedBCEfet", getCampo("245.51.2-N34","pRedBCEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51.2-N34","pRedBCEfet")), 4, false), true);
								ICMSSN500.addNode("vBCEfet", getCampo("245.51.3-N35","vBCEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51.3-N35","vBCEfet")), 2, false), true);
								ICMSSN500.addNode("pICMSEfet", getCampo("245.51.4-N36","pICMSEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51.4-N36","pICMSEfet")), 4, false), true);
								ICMSSN500.addNode("vICMSEfet", getCampo("245.51.5-N37","vICMSEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.51.5-N37","vICMSEfet")), 2, false), true);
							}

						}else if(cso.equals("900")) {
							/** ICMSSN900 = CSOSN = 900 - Outros */
							ICMSSN900 = ICMS.addNode("ICMSSN900");
							ICMSSN900.addNode("orig", orig, true);
							ICMSSN900.addNode("CSOSN", cso, true);

							if(getCampo("245.56-N15","vBC") != null && !jsonEaa0103.getBigDecimal(getCampo("245.56-N15","vBC")).equals(0)){
								ICMSSN900.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, true);
								ICMSSN900.addNode("vBC", getCampo("245.56-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.56-N15","vBC")), 2, false), true);
								ICMSSN900.addNode("pRedBC", getCampo("245.57-N14","pRedBC") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.57-N14","pRedBC")), 2, true), false);
								ICMSSN900.addNode("pICMS", getCampo("245.58-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.58-N16","pICMS")), 2, false), true);
								ICMSSN900.addNode("vICMS", getCampo("245.59-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.59-N17","vICMS")), 2, false), true);
							}

							if(getCampo("245.63-N21","vBCST") != null && !jsonEaa0103.getBigDecimal(getCampo("245.63-N21","vBCST")).equals(0)){
								ICMSSN900.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
								ICMSSN900.addNode("pMVAST", getCampo("245.61-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.61-N19","tx_iva_st")), 2, true), false);
								ICMSSN900.addNode("pRedBCST", getCampo("245.62-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.62-N20","pRedBCST")), 2, true), false);
								ICMSSN900.addNode("vBCST", getCampo("245.63-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.63-N21","vBCST")), 2, false), true);
								ICMSSN900.addNode("pICMSST", getCampo("245.64-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.64-N22","pICMSST")), 2, false), true);
								ICMSSN900.addNode("vICMSST", getCampo("245.65-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.65-N23","vICMSST")), 2, false), true);
							}

							ICMSSN900.addNode("vBCFCPST", getCampo("245.65w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.65w-N23a","vBCFCPST")), 2, true), false);
							ICMSSN900.addNode("pFCPST", getCampo("245.65x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.65x-N23b","pFCPST")), 4, true), false);
							ICMSSN900.addNode("vFCPST", getCampo("245.65y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.65y-N23d","vFCPST")), 2, true), false);
							ICMSSN900.addNode("pCredSN", getCampo("245.52-N29","pCredSN") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.52-N29","pCredSN")), 4, true), false);
							ICMSSN900.addNode("vCredICMSSN", getCampo("245.53-N30","vCredICMSSN") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.53-N30","vCredICMSSN")), 2, true), false);
						}
					}

				}else {
					Aaj10 aaj10 = eaa0103.eaa0103cstIcms == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aaj10", eaa0103.eaa0103cstIcms.aaj10id);
					String cstIcms = aaj10 == null ? null : aaj10.aaj10codigo;
					if(cstIcms != null) {
						String orig = cstIcms.substring(0, 1);
						String cst = cstIcms.substring(1);

						if((cst.equals("41") || cst.equals("60")) && getCampo("245.20-N26","vBCSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.20-N26","vBCSTRet")).equals(0)) {
							/** ICMSST - CST = 41 e base de cálculo diferente de zero */

							ICMSST = ICMS.addNode("ICMSST");
							ICMSST.addNode("orig", orig, true);
							ICMSST.addNode("CST", cst, true);
							ICMSST.addNode("vBCSTRet", getCampo("245.20-N26","vBCSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.20-N26","vBCSTRet")), 2, false), true);

							ICMSST.addNode("pST", getCampo("245.20a-N26a","pST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.20a-N26a","pST")), 4, true), false);
							ICMSST.addNode("vICMSSubstituto", getCampo("245.20b-N26b","vICMSSubstituto") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.20b-N26b","vICMSSubstituto")), 2, false), true);

							ICMSST.addNode("vICMSSTRet", getCampo("245.21-N27","vICMSSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.21-N27","vICMSSTRet")), 2, false), true);

							if(getCampo("245.21b-N27a","vBCFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.21b-N27a","vBCFCPSTRet")).equals(0) ||
									getCampo("245.21c-N27b","pFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.21c-N27b","pFCPSTRet")).equals(0) ||
									getCampo("245.21d-N27d","vFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.21d-N27d","vFCPSTRet")).equals(0)){
								ICMSST.addNode("vBCFCPSTRet", getCampo("245.21b-N27a","vBCFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.21b-N27a","vBCFCPSTRet")), 2, false), true);
								ICMSST.addNode("pFCPSTRet", getCampo("245.21c-N27b","pFCPSTRet") == null ? "0.00" : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.21c-N27b","pFCPSTRet")), 2, false), true);
								ICMSST.addNode("vFCPSTRet", getCampo("245.21d-N27d","vFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.21d-N27d","vFCPSTRet")), 2, false), true);
							}

							ICMSST.addNode("vBCSTDest", getCampo("245.22-N31","vBCSTDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.22-N31","vBCSTDest")), 2, false), true);
							ICMSST.addNode("vICMSSTDest", getCampo("245.23-N32","vICMSSTDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.23-N32","vICMSSTDest")), 2, false), true);

							if(getCampo("245.23c-N35","vBCEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.23c-N35","vBCEfet")).equals(0) ||
									getCampo("245.23e-N37","vICMSEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("245.23e-N37","vICMSEfet")).equals(0)) {
								ICMSST.addNode("pRedBCEfet", getCampo("245.23b-N34","pRedBCEfet") == null ? "0.00" : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.23b-N34","pRedBCEfet")), 4, false), true);
								ICMSST.addNode("vBCEfet", getCampo("245.23c-N35","vBCEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.23c-N35","vBCEfet")), 2, false), true);
								ICMSST.addNode("pICMSEfet", getCampo("245.23d-N36","pICMSEfet") == null ? "0.00" : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.23d-N36","pICMSEfet")), 4, false), true);
								ICMSST.addNode("vICMSEfet", getCampo("245.23e-N37","vICMSEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.23e-N37","vICMSEfet")), 2, false), true);
							}

						}else if(cst.equals("00")) {
							/** ICMS00 - CST = 00 - Tributada integralmente (N02) */
							ICMS00 = ICMS.addNode("ICMS00");
							ICMS00.addNode("orig", orig, true);
							ICMS00.addNode("CST", cst, true);
							ICMS00.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, true);
							ICMS00.addNode("vBC", getCampo("169-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("169-N15","vBC")), 2, false), true);
							ICMS00.addNode("pICMS", getCampo("170-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("170-N16","pICMS")), 2, false), true);
							ICMS00.addNode("vICMS", getCampo("171-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("171-N17","vICMS")), 2, false), true);
							ICMS00.addNode("pFCP", getCampo("171.1-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("171.1-N17b","pFCP")), 4, true), false);
							ICMS00.addNode("vFCP", getCampo("171.2-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("171.2-N17c","vFCP")), 2, true), false);

						}else if(cst.equals("10")) {
							/** ICMS10 - CST = 10 - Tributada e com cobrança do ICMS por ST (N03) */
							ICMS10 = ICMS.addNode("ICMS10");
							ICMS10.addNode("orig", orig, true);
							ICMS10.addNode("CST", cst, true);
							ICMS10.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, true);
							ICMS10.addNode("vBC", getCampo("176-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("176-N15","vBC")), 2, false), true);
							ICMS10.addNode("pICMS", getCampo("177-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("177-N16","pICMS")), 2, false), true);
							ICMS10.addNode("vICMS", getCampo("178-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("178-N17","vICMS")), 2, false), true);
							ICMS10.addNode("vBCFCP", getCampo("178.1-N17a","vBCFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("178.1-N17a","vBCFCP")), 2, true), false);
							ICMS10.addNode("pFCP", getCampo("178.2-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("178.2-N17b","pFCP")), 4, true), false);
							ICMS10.addNode("vFCP", getCampo("178.3-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("178.3-N17c","vFCP")), 2, true), false);
							ICMS10.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
							ICMS10.addNode("pMVAST", getCampo("180-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("180-N19","pMVAST")), 2, true), false);
							ICMS10.addNode("pRedBCST", getCampo("181-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("181-N20","pRedBCST")), 2, true), false);
							ICMS10.addNode("vBCST", getCampo("182-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("182-N21","vBCST")), 2, false), true);
							ICMS10.addNode("pICMSST", getCampo("183-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("183-N22","pICMSST")), 2, false), true);
							ICMS10.addNode("vICMSST", getCampo("184-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("184-N23","vICMSST")), 2, false), true);
							ICMS10.addNode("vBCFCPST", getCampo("184.1-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("184.1-N23a","vBCFCPST")), 2, true), false);
							ICMS10.addNode("pFCPST", getCampo("184.2-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("184.2-N23b","pFCPST")), 4, true), false);
							ICMS10.addNode("vFCPST", getCampo("184.4-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("184.4-N23d","vFCPST")), 2, true), false);

						}else if(cst.equals("20")) {
							/** ICMS20 - CST = 20 - Com redução de base de cálculo (N04) */
							ICMS20 = ICMS.addNode("ICMS20");
							ICMS20.addNode("orig", orig, true);
							ICMS20.addNode("CST", cst, true);
							ICMS20.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, true);
							ICMS20.addNode("pRedBC", getCampo("189-N14","pRedBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("189-N14","pRedBC")), 2, false), true);
							ICMS20.addNode("vBC", getCampo("190-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("190-N15","vBC")), 2, false), true);
							ICMS20.addNode("pICMS", getCampo("191-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("191-N16","pICMS")), 2, false), true);
							ICMS20.addNode("vICMS", getCampo("192-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("192-N17","vICMS")), 2, false), true);
							ICMS20.addNode("vBCFCP", getCampo("192.w-N17a","vBCFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("192.w-N17a","vBCFCP")), 2, true), false);
							ICMS20.addNode("pFCP", getCampo("192.x-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("192.x-N17b","pFCP")), 4, true), false);
							ICMS20.addNode("vFCP", getCampo("192.y-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("192.y-N17c","vFCP")), 2, true), false);
							if(eaa0103.eaa0103motDesIcms > 0){
								ICMS20.addNode("vICMSDeson", getCampo("192.2-N28a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("192.2-N28a","vICMSDeson")), 2, false), true);
								ICMS20.addNode("motDesICMS", eaa0103.eaa0103motDesIcms, true);
							}

						}else if(cst.equals("30")) {
							/** ICMS30 - CST = 30 - Isenta ou não tributada e com cobrança do ICMS por ST (N05) */
							ICMS30 = ICMS.addNode("ICMS30");
							ICMS30.addNode("orig", orig, true);
							ICMS30.addNode("CST", cst, true);
							ICMS30.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcms, true);
							ICMS30.addNode("pMVAST", getCampo("197-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("197-N19","pMVAST")), 2, true), false);
							ICMS30.addNode("pRedBCST", getCampo("198-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("198-N20","pRedBCST")), 2, true), false);
							ICMS30.addNode("vBCST", getCampo("199-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("199-N21","vBCST")), 2, false), true);
							ICMS30.addNode("pICMSST", getCampo("200-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("200-N22","pICMSST")), 2, false), true);
							ICMS30.addNode("vICMSST", getCampo("201-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("201-N23","vICMSST")), 2, false), true);
							ICMS30.addNode("vBCFCPST", getCampo("201.w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("201.w-N23a","vBCFCPST")), 2, true), false);
							ICMS30.addNode("pFCPST", getCampo("201.x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("201.x-N23b","pFCPST")), 4, true), false);
							ICMS30.addNode("vFCPST", getCampo("201.y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("201.y-N23d","vFCPST")), 2, true), false);

							if(eaa0103.eaa0103motDesIcms > 0){
								ICMS30.addNode("vICMSDeson", getCampo("201.2-N28a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("201.2-N28a","vICMSDeson")), 2, false), true);
								ICMS30.addNode("motDesICMS", eaa0103.eaa0103motDesIcms, true);
							}

						}else if(cst.equals("40") || cst.equals("41") || cst.equals("50") || cst.equals("040") || cst.equals("041") || cst.equals("050") ) {
							/** ICMS40 - CST = 40-Isenta, 41-Não tributada, 50-Suspensão (N06) */
							ICMS40 = ICMS.addNode("ICMS40");
							ICMS40.addNode("orig", orig, true);
							ICMS40.addNode("CST", cst, true);

							if(eaa0103.eaa0103motDesIcms > 0){
								ICMS40.addNode("vICMSDeson", getCampo("204.01-N28a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("204.01-N28a","vICMSDeson")), 2, false), true);
								ICMS40.addNode("motDesICMS", eaa0103.eaa0103motDesIcms, true);
								ICMS40.addNode("indDeduzDeson", new BigDecimal(1), true);
							}


						}else if(cst.equals("51")) {
							/** ICMS51 - CST = 51 - Diferimento (N07) */
							ICMS51 = ICMS.addNode("ICMS51");
							ICMS51.addNode("orig", orig, true);
							ICMS51.addNode("CST", cst, true);
							ICMS51.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, false);

							Boolean temDiferimento = getCampo("211.02-N16b","pDif") != null && !jsonEaa0103.getBigDecimal(getCampo("211.02-N16b","pDif")).equals(0); //Se houver % de diferimento, gerar as tag mesmo que seja zero

							ICMS51.addNode("pRedBC", getCampo("209-N14","pRedBC") == null ? temDiferimento ? 0 : null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("209-N14","pRedBC")), 2, false), false);
							ICMS51.addNode("vBC", getCampo("210-N15","vBC") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("210-N15","vBC")), 2, false), false);
							ICMS51.addNode("pICMS", getCampo("211-N16","pICMS") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("211-N16","pICMS")), 2, false), false);
							ICMS51.addNode("vICMSOp", getCampo("211.01-N16a","vICMSOp") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("211.01-N16a","vICMSOp")), 2, false), false);
							ICMS51.addNode("pDif", getCampo("211.02-N16b","pDif") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("211.02-N16b","pDif")), 2, false), false);
							ICMS51.addNode("vICMSDif", getCampo("211.03-N16c","vICMSDif") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("211.03-N16c","vICMSDif")), 2, false), false);
							ICMS51.addNode("vICMS", getCampo("212-N17","vICMS") == null ? temDiferimento ? 0 : null  : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("212-N17","vICMS")), 2, false), false);
							ICMS51.addNode("vBCFCP", getCampo("212.w-N17a","vBCFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("212.w-N17a","vBCFCP")), 2, true), false);
							ICMS51.addNode("pFCP", getCampo("212.x-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("212.x-N17b","pFCP")), 4, true), false);
							ICMS51.addNode("vFCP", getCampo("212.y-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("212.y-N17c","vFCP")), 2, true), false);

						}else if(cst.equals("60")) {
							/** ICMS60 - CST = 60 - ICMS cobrado anteriormente por substituição tributária (N08) */
							ICMS60 = ICMS.addNode("ICMS60");
							ICMS60.addNode("orig", orig, true);
							ICMS60.addNode("CST", cst, true);

							if(getCampo("216-N26","vBCSTRet") != null) {
								ICMS60.addNode("vBCSTRet", getCampo("216-N26","vBCSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("216-N26","vBCSTRet")), 2, false), true);
								ICMS60.addNode("pST", getCampo("216.1-N26a","pST") == null ? "0.00" : jsonEaa0103.getBigDecimal(getCampo("216.1-N26a","pST")).equals(0) ? "0.00" : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("216.1-N26a","pST")), 4, false), true);
								ICMS60.addNode("vICMSSubstituto", getCampo("216.2-N26b","vICMSSubstituto") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("216.2-N26b","vICMSSubstituto")), 2, false), true);
								ICMS60.addNode("vICMSSTRet", getCampo("217-N27","vICMSSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217-N27","vICMSSTRet")), 2, false), true);
							}

							if(getCampo("217.w-N27a","vBCFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("217.w-N27a","vBCFCPSTRet")).equals(0) ||
									getCampo("217.y-N27d","vFCPSTRet") != null && !jsonEaa0103.getBigDecimal(getCampo("217.y-N27d","vFCPSTRet")).equals(0)) {
								ICMS60.addNode("vBCFCPSTRet", getCampo("217.w-N27a","vBCFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.w-N27a","vBCFCPSTRet")), 2, false), true);
								ICMS60.addNode("pFCPSTRet", getCampo("217.x-N27b","pFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.x-N27b","pFCPSTRet")), 4, false), true);
								ICMS60.addNode("vFCPSTRet", getCampo("217.y-N27d","vFCPSTRet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.y-N27d","vFCPSTRet")), 2, false), true);
							}

							if(getCampo("217.3-N35","vBCEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("217.3-N35","vBCEfet")).equals(0) ||
									getCampo("217.5-N37","vICMSEfet") != null && !jsonEaa0103.getBigDecimal(getCampo("217.5-N37","vICMSEfet")).equals(0)) {
								ICMS60.addNode("pRedBCEfet", getCampo("217.2-N34","pRedBCEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.2-N34","pRedBCEfet")), 4, false), true);
								ICMS60.addNode("vBCEfet", getCampo("217.3-N35","vBCEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.3-N35","vBCEfet")), 2, false), true);
								ICMS60.addNode("pICMSEfet", getCampo("217.4-N36","pICMSEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.4-N36","pICMSEfet")), 4, false), true);
								ICMS60.addNode("vICMSEfet", getCampo("217.5-N37","vICMSEfet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("217.5-N37","vICMSEfet")), 2, false), true);
							}

						}else if(cst.equals("70")) {
							/** ICMS70 - CST = 70 - Com redução de BC e cobrança do ICMS por ST (N09) */
							ICMS70 = ICMS.addNode("ICMS70");
							ICMS70.addNode("orig", orig, true);
							ICMS70.addNode("CST", cst, true);
							ICMS70.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcms, true);
							ICMS70.addNode("pRedBC", getCampo("222-N14","pRedBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("222-N14","pRedBC")), 2, false), true);
							ICMS70.addNode("vBC", getCampo("223-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("223-N15","vBC")), 2, false), true);
							ICMS70.addNode("pICMS", getCampo("224-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("224-N16","pICMS")), 2, false), true);
							ICMS70.addNode("vICMS", getCampo("225-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("225-N17","vICMS")), 2, false), true);
							ICMS70.addNode("vBCFCP", getCampo("225.1-N17a","vBCFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("225.1-N17a","vBCFCP")), 2, true), false);
							ICMS70.addNode("pFCP", getCampo("225.2-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("225.2-N17b","pFCP")), 4, true), false);
							ICMS70.addNode("vFCP", getCampo("225.3-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("225.3-N17c","vFCP")), 2, true), false);
							ICMS70.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
							ICMS70.addNode("pMVAST", getCampo("227-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("227-N19","pMVAST")), 2, true), false);
							ICMS70.addNode("pRedBCST", getCampo("228-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("228-N20","pRedBCST")), 2, true), false);
							ICMS70.addNode("vBCST", getCampo("229-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("229-N21","vBCST")), 2, false), true);
							ICMS70.addNode("pICMSST", getCampo("230-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("230-N22","pICMSST")), 2, false), true);
							ICMS70.addNode("vICMSST", getCampo("231-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("231-N23","vICMSST")), 2, false), true);
							ICMS70.addNode("vBCFCPST", getCampo("231.w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("231.w-N23a","vBCFCPST")), 2, true), false);
							ICMS70.addNode("pFCPST", getCampo("231.x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("231.x-N23b","pFCPST")), 4, true), false);
							ICMS70.addNode("vFCPST", getCampo("231.y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("231.y-N23d","vFCPST")), 2, true), false);

							if(eaa0103.eaa0103motDesIcms > 0){
								ICMS70.addNode("vICMSDeson", getCampo("231.2-N28a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("231.2-N28a","vICMSDeson")), 2, false), true);
								ICMS70.addNode("motDesICMS", eaa0103.eaa0103motDesIcms, true);
							}

						}else if(cst.equals("90")) {
							/** ICMS90 - CST = 90 - Outros (N10) */
							ICMS90 = ICMS.addNode("ICMS90");
							ICMS90.addNode("orig", orig, true);
							ICMS90.addNode("CST", cst, true);
							ICMS90.addNode("modBC", configItem.abm0101fiscal.abm12modBcIcmsST, true);
							ICMS90.addNode("vBC", getCampo("236-N15","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("236-N15","vBC")), 2, false), true);
							ICMS90.addNode("pRedBC", getCampo("237-N14","pRedBC") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("237-N14","pRedBC")), 2, true), false);
							ICMS90.addNode("pICMS", getCampo("238-N16","pICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("238-N16","pICMS")), 2, false), true);
							ICMS90.addNode("vICMS", getCampo("239-N17","vICMS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("239-N17","vICMS")), 2, false), true);
							ICMS90.addNode("vBCFCP", getCampo("239.w-N17a","vBCFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("239.w-N17a","vBCFCP")), 2, true), false);
							ICMS90.addNode("pFCP", getCampo("239.x-N17b","pFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("239.x-N17b","pFCP")), 4, true), false);
							ICMS90.addNode("vFCP", getCampo("239.y-N17c","vFCP") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("239.y-N17c","vFCP")), 2, true), false);

							if(getCampo("243-N21","vBCST") != null && !jsonEaa0103.getBigDecimal(getCampo("243-N21","vBCST")).equals(0)) {
								ICMS90.addNode("modBCST", configItem.abm0101fiscal.abm12modBcIcmsST, true);
								ICMS90.addNode("pMVAST", getCampo("241-N19","pMVAST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("241-N19","pMVAST")), 2, true), false);
								ICMS90.addNode("pRedBCST", getCampo("242-N20","pRedBCST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("242-N20","pRedBCST")), 2, true), false);
								ICMS90.addNode("vBCST", getCampo("243-N21","vBCST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("243-N21","vBCST")), 2, false), true);
								ICMS90.addNode("pICMSST", getCampo("244-N22","pICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("244-N22","pICMSST")), 2, false), true);
								ICMS90.addNode("vICMSST", getCampo("245-N23","vICMSST") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245-N23","vICMSST")), 2, false), true);
								ICMS90.addNode("vBCFCPST", getCampo("245.w-N23a","vBCFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.w-N23a","vBCFCPST")), 2, true), false);
								ICMS90.addNode("pFCPST", getCampo("245.x-N23b","pFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.x-N23b","pFCPST")), 4, true), false);
								ICMS90.addNode("vFCPST", getCampo("245.y-N23d","vFCPST") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.y-N23d","vFCPST")), 2, true), false);
							}

							if(eaa0103.eaa0103motDesIcms > 0){
								ICMS90.addNode("vICMSDeson", getCampo("245.2-N28a","vICMSDeson") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245.2-N28a","vICMSDeson")), 2, false), true);
								ICMS90.addNode("motDesICMS", eaa0103.eaa0103motDesIcms, true);
							}
						}
					}
				}

				/**  IPI - Imposto sobre Produtos Industrializados (O01) */
				String cstIpi = eaa0103.eaa0103cstIpi == null ? null : eaa0103.eaa0103cstIpi.aaj11codigo;
				if(cstIpi != null) {
					IPI = imposto.addNode("IPI");
					IPI.addNode("CNPJProd", null, false);
					IPI.addNode("cSelo", null, false, 60);
					IPI.addNode("qSelo", null, false);
					IPI.addNode("cEnq", eaa0103.eaa0103codEnqIpi, true);

					/** IPITrib - CST = 00-Entrada c/ recup de crédito, 49-Outras entradas, 50-Saída tributada, 99-Outras Saídas (O07) */
					if(cstIpi.equals("00") || cstIpi.equals("49") || cstIpi.equals("50") || cstIpi.equals("99")) {
						IPITrib = IPI.addNode("IPITrib");
						IPITrib.addNode("CST", cstIpi, true);

						if(getCampo("257-O13","pIPI") != null && jsonEaa0103.getBigDecimal_Zero(getCampo("257-O13","pIPI")).compareTo(new BigDecimal(0)) != 0) {
							IPITrib.addNode("vBC", getCampo("254-O10","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("254-O10","vBC")), 2, false), true);
							IPITrib.addNode("pIPI", getCampo("257-O13","pIPI") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("257-O13","pIPI")), 2, false), true);
						}else {
							IPITrib.addNode("qUnid", getCampo("255-O11","qUnid") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("255-O11","qUnid")), 4, false), true);
							IPITrib.addNode("vUnid", getCampo("256-O12","vUnid") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("256-O12","vUnid")), 4, false), true);
						}

						IPITrib.addNode("vIPI", getCampo("259-O14","vIPI") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("259-O14","vIPI")), 2, false), true);
					}

					/** IPINT - CST = 01, 02, 03, 04, 05, 51, 52, 53, 54 e 55 (O08) */
					if(cstIpi.equals("01") || cstIpi.equals("02") || cstIpi.equals("03") || cstIpi.equals("04") || cstIpi.equals("05") || cstIpi.equals("51") || cstIpi.equals("52") || cstIpi.equals("53") || cstIpi.equals("54") || cstIpi.equals("55")) {
						IPINT = IPI.addNode("IPINT");
						IPINT.addNode("CST", cstIpi, true);
					}
				}

				/** II - Imposto de Importação (P01) */
				if(eaa01.eaa01esMov == 0 && eaa0103.eaa0103cfop.aaj15codigo.substring(0, 1).equals("3")) {
					II = imposto.addNode("II");
					II.addNode("vBC", getCampo("263-P02","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("263-P02","vBC")), 2, false), true);
					II.addNode("vDespAdu", getCampo("264-P03","vDespAdu") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("264-P03","vDespAdu")), 2, false), true);
					II.addNode("vII", getCampo("265-P04","vII") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("265-P04","vII")), 2, false), true);
					II.addNode("vIOF", getCampo("266-P05","vIOF") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("266-P05","vIOF")), 2, false), true);
				}
			}

			/** PIS - PIS (Q01) */
			PIS = imposto.addNode("PIS");
			Aaj12 aaj12 = getAcessoAoBanco().buscarRegistroUnicoById("Aaj12", eaa0103.eaa0103cstPis.aaj12id);
			String cstPis = aaj12 == null ? null : aaj12.aaj12codigo;
			if(cstPis != null) {
				/** PISAliq - PIS tributado pela alíquota, CST = 01 e 02 (Q02) */
				if(cstPis.equals("01") || cstPis.equals("02")) {
					PISAliq = PIS.addNode("PISAliq");
					PISAliq.addNode("CST", cstPis, true);
					PISAliq.addNode("vBC", getCampo("270-Q07","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("270-Q07","vBC")), 2, false), true);
					PISAliq.addNode("pPIS", getCampo("271-Q08","pPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("271-Q08","pPIS")), 2, false), true);
					PISAliq.addNode("vPIS", getCampo("272-Q09","vPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("272-Q09","vPIS")), 2, false), true);
				}

				/** PISQtde - PIS tributado por quantidade, CST = 03 (Q03) */
				if(cstPis.equals("03")) {
					PISQtde = PIS.addNode("PISQtde");
					PISQtde.addNode("CST", cstPis, true);
					PISQtde.addNode("qBCProd", getCampo("275-Q10","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("275-Q10","qBCProd")), 4, false), true);
					PISQtde.addNode("vAliqProd", getCampo("276-Q11","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("276-Q11","vAliqProd")), 4, false), true);
					PISQtde.addNode("vPIS", getCampo("277-Q09","vPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("277-Q09","vPIS")), 2, false), true);
				}

				/** PISNT - PIS não-tributado, CST = 04, 05, 06, 07, 08 e 09 (Q04) */
				if(cstPis.equals("04") || cstPis.equals("05") || cstPis.equals("06") || cstPis.equals("07") || cstPis.equals("08") || cstPis.equals("09")) {
					PISNT = PIS.addNode("PISNT");
					PISNT.addNode("CST", cstPis, true);
				}

				/** PISOutr - PIS Outras operações, CST = 99 (Q05) */
				if(cstPis.equals("49") || cstPis.equals("50") || cstPis.equals("51") || cstPis.equals("52") ||
						cstPis.equals("53") || cstPis.equals("54") || cstPis.equals("55") || cstPis.equals("56") ||
						cstPis.equals("60") || cstPis.equals("61") || cstPis.equals("62") || cstPis.equals("63") ||
						cstPis.equals("64") || cstPis.equals("65") || cstPis.equals("66") || cstPis.equals("67") ||
						cstPis.equals("70") || cstPis.equals("71") || cstPis.equals("72") || cstPis.equals("73") ||
						cstPis.equals("74") || cstPis.equals("75") || cstPis.equals("98") || cstPis.equals("99")) {

					PISOutr = PIS.addNode("PISOutr");
					PISOutr.addNode("CST", cstPis, true);

					if(getCampo("283-Q08","pPIS") != null && jsonEaa0103.getBigDecimal_Zero(getCampo("283-Q08","pPIS")).compareTo(new BigDecimal(0)) != 0) {
						PISOutr.addNode("vBC", getCampo("282-Q07","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("282-Q07","vBC")), 2, false), true);
						PISOutr.addNode("pPIS", getCampo("283-Q08","pPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("283-Q08","pPIS")), 2, false), true);
					}else{
						PISOutr.addNode("qBCProd", getCampo("284-Q10","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("284-Q10","qBCProd")), 4, false), true);
						PISOutr.addNode("vAliqProd", getCampo("285-Q11","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("285-Q11","vAliqProd")), 4, false), true);
					}

					PISOutr.addNode("vPIS", getCampo("286-Q09","vPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("286-Q09","vPIS")), 2, false), true);
				}

				/** PISST - PIS ST (R01) */
				if((getCampo("289-R03","pPIS") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("289-R03","pPIS")).equals(0)) || (getCampo("291-R05","vAliqProd") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("291-R05","vAliqProd")).equals(0))) {
					PISST = imposto.addNode("PISST");

					if(getCampo("289-R03","pPIS") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("289-R03","pPIS")).equals(0)) {
						PISST.addNode("vBC", getCampo("288-R02","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("288-R02","vBC")), 2, false), true);
						PISST.addNode("pPIS", getCampo("289-R03","pPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("289-R03","pPIS")), 2, false), true);
					}else {
						PISST.addNode("qBCProd", getCampo("290-R04","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("290-R04","qBCProd")), 4, false), true);
						PISST.addNode("vAliqProd", getCampo("291-R05","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("291-R05","vAliqProd")), 4, false), true);
					}

					PISST.addNode("vPIS", getCampo("292-R06","vPIS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("292-R06","vPIS")), 2, false), true);
				}

				/** COFINS - COFINS (S01) */
				COFINS = imposto.addNode("COFINS");
				Aaj13 aaj13 = getAcessoAoBanco().buscarRegistroUnicoById("Aaj13", eaa0103.eaa0103cstCofins.aaj13id);
				String cstCofins = aaj13 == null ? null : aaj13.aaj13codigo;
				if(cstCofins != null) {
					/** COFINSAliq - COFINS tributado pela alíquota, CST = 01 ou 02 (S02) */
					if(cstCofins.equals("01") || cstCofins.equals("02")) {
						COFINSAliq = COFINS.addNode("COFINSAliq");
						COFINSAliq.addNode("CST", cstCofins, true);
						COFINSAliq.addNode("vBC", getCampo("296-S07","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("296-S07","vBC")), 2, false), true);
						COFINSAliq.addNode("pCOFINS", getCampo("297-S08","pCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("297-S08","pCOFINS")), 2, false), true);
						COFINSAliq.addNode("vCOFINS", getCampo("298-S11","vCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("298-S11","vCOFINS")), 2, false), true);
					}

					/** COFINSQtde - COFINS tributado por Qtde, CST = 03 (S03)*/
					if(cstCofins.equals("03")) {
						COFINSQtde = COFINS.addNode("COFINSQtde");
						COFINSQtde.addNode("CST", cstCofins, true);
						COFINSQtde.addNode("qBCProd", getCampo("301-S09","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("301-S09","qBCProd")), 4, false), true);
						COFINSQtde.addNode("vAliqProd", getCampo("302-S10","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("302-S10","vAliqProd")), 4, false), true);
						COFINSQtde.addNode("vCOFINS", getCampo("303-S11","vCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("303-S11","vCOFINS")), 2, false), true);
					}

					/** COFINSNT - COFINS não tributado, CST = 04, 05, 06, 07, 08 ou 09 (S04) */
					if(cstCofins.equals("04") || cstCofins.equals("05") || cstCofins.equals("06") || cstCofins.equals("07") || cstCofins.equals("08") || cstCofins.equals("09")) {
						COFINSNT = COFINS.addNode("COFINSNT");
						COFINSNT.addNode("CST", cstCofins, true);
					}

					/** COFINSOutr - COFINS outras operações, CST = 99 (S05) */
					if(cstCofins.equals("49") || cstCofins.equals("50") || cstCofins.equals("51") || cstCofins.equals("52") ||
							cstCofins.equals("53") || cstCofins.equals("54") || cstCofins.equals("55") || cstCofins.equals("56") ||
							cstCofins.equals("60") || cstCofins.equals("61") || cstCofins.equals("62") || cstCofins.equals("63") ||
							cstCofins.equals("64") || cstCofins.equals("65") || cstCofins.equals("66") || cstCofins.equals("67") ||
							cstCofins.equals("70") || cstCofins.equals("71") || cstCofins.equals("72") || cstCofins.equals("73") ||
							cstCofins.equals("74") || cstCofins.equals("75") || cstCofins.equals("98") || cstCofins.equals("99")) {

						COFINSOutr = COFINS.addNode("COFINSOutr");
						COFINSOutr.addNode("CST", cstCofins, true);

						if(getCampo("309-S08","pCOFINS") != null && jsonEaa0103.getBigDecimal_Zero(getCampo("309-S08","pCOFINS")).compareTo(new BigDecimal(0)) != 0) {
							COFINSOutr.addNode("vBC", getCampo("308-S07","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("308-S07","vBC")), 2, false), true);
							COFINSOutr.addNode("pCOFINS", getCampo("309-S08","pCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("309-S08","pCOFINS")), 2, false), true);
						}else{
							COFINSOutr.addNode("qBCProd", getCampo("310-S09","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("310-S09","qBCProd")), 4, false), true);
							COFINSOutr.addNode("vAliqProd", getCampo("311-S10","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("311-S10","vAliqProd")), 4, false), true);
						}

						COFINSOutr.addNode("vCOFINS", getCampo("312-S11","vCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("312-S11","vCOFINS")), 2, false), true);
					}
				}

				/** COFINSST - COFINS ST (T01) */
				if((getCampo("315-T03","pCOFINS") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("315-T03","pCOFINS")).equals(0)) || (getCampo("317-T05","vAliqProd") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("317-T05","vAliqProd")).equals(0))) {
					COFINSST = imposto.addNode("COFINSST");

					if(getCampo("315-T03","pCOFINS") != null && !jsonEaa0103.getBigDecimal_Zero(getCampo("315-T03","pCOFINS")).equals(0)) {
						COFINSST.addNode("vBC", getCampo("314-T02","vBC") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("314-T02","vBC")), 2, false), true);
						COFINSST.addNode("pCOFINS", getCampo("315-T03","pCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("315-T03","pCOFINS")), 2, false), true);
					}else {
						COFINSST.addNode("qBCProd", getCampo("316-T04","qBCProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("316-T04","qBCProd")), 4, false), true);
						COFINSST.addNode("vAliqProd", getCampo("317-T05","vAliqProd") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("317-T05","vAliqProd")), 4, false), true);
					}

					COFINSST.addNode("vCOFINS", getCampo("318-T06","vCOFINS") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("318-T06","vCOFINS")), 2, false), true);
				}

				/** ICMSUFDest - ICMS para UF do destinatário (NA) */
				if((getCampo("245a.03-NA03","vBCUFDest") != null && !jsonEaa0103.getBigDecimal(getCampo("245a.03-NA03","vBCUFDest")).equals(0)) ||
						(getCampo("245a.04-NA04","vBCFCPUFDest") != null && !jsonEaa0103.getBigDecimal(getCampo("245a.04-NA04","vBCFCPUFDest")).equals(0))) {
					if(idDest == 2 && eaa01.eaa0102DadosGerais.eaa0102contribIcms == 0 && indIEDest == 9 && !temISS){
						ICMSUFDest = imposto.addNode("ICMSUFDest");
						ICMSUFDest.addNode("vBCUFDest", getCampo("245a.03-NA03","vBCUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.03-NA03","vBCUFDest")), 2, false), true);
						ICMSUFDest.addNode("vBCFCPUFDest", getCampo("245a.04-NA04","vBCFCPUFDest") == null ? null : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.04-NA04","vBCFCPUFDest")), 2, false), false);
						ICMSUFDest.addNode("pFCPUFDest", getCampo("245a.05-NA05","pFCPUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.05-NA05","pFCPUFDest")), 2, false), true);
						ICMSUFDest.addNode("pICMSUFDest", getCampo("245a.07-NA07","pICMSUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.07-NA07","pICMSUFDest")), 2, false), true);
						ICMSUFDest.addNode("pICMSInter", getCampo("245a.09-NA09","pICMSInter") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.09-NA09","pICMSInter")), 2, false), true);
						ICMSUFDest.addNode("pICMSInterPart", getCampo("245a.11-NA11","pICMSInterPart") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.11-NA11","pICMSInterPart")), 2, false), true);
						ICMSUFDest.addNode("vFCPUFDest", getCampo("245a.13-NA13","vFCPUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.13-NA13","vFCPUFDest")), 2, false), true);
						ICMSUFDest.addNode("vICMSUFDest", getCampo("245a.15-NA15","vICMSUFDest") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.15-NA15","vICMSUFDest")), 2, false), true);
						ICMSUFDest.addNode("vICMSUFRemet", getCampo("245a.17-NA17","vICMSUFRemet") == null ? 0 : NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("245a.17-NA17","icmsremet")), 2, false), true);
					}
				}

				/** impostoDevol - Informação do Imposto devolvido */
				if(getCampo("324s-UA04","vIPIDevol") != null && !jsonEaa0103.getBigDecimal(getCampo("324s-UA04","vIPIDevol")).equals(0)) {
					Eaa01033 itemVda = getAcessoAoBanco().buscarRegistroUnicoByCriterion("eaa01033", Criterions.eq("eaa01033item", eaa0103.eaa0103id))
					if(itemVda != null) {
						BigDecimal qtdVda = itemVda.eaa01033itemDoc.eaa0103qtComl;
						BigDecimal qtdDevol = itemVda.eaa01033qtComl;
						BigDecimal percentual = qtdDevol.multiply(100);
						percentual = percentual.divide(qtdVda, Scale.ROUND_34);

						impostoDevol = det.addNode("impostoDevol");
						impostoDevol.addNode("pDevol", NFeUtils.formatarDecimal(percentual, 2, false), true);

						IPI = impostoDevol.addNode("IPI");
						IPI.addNode("vIPIDevol", NFeUtils.formatarDecimal(jsonEaa0103.getBigDecimal(getCampo("324s-UA04","vIPIDevol")), 2, true), true);
					}
				}

				/** infAdProd - Informações Adicionais (V01) */
				det.addNode("infAdProd", eaa0103.eaa0103infAdic, false, 500);
				item++;
			}
		}
	}

	private void validarDadosDaNFe() {

		MultiValidationException validations = new MultiValidationException();

		/** Aac10 - Emitente */
		if(empresa.aac10municipio == null) {
			validations.addToValidations(new ValidationMessage("Necessário informar o município no cadastro da empresa ativa."));
		} else {
			if(empresa.aac10municipio.aag0201ibge == null) validations.addToValidations(new ValidationMessage("Necessário informar o código IBGE da UF correspondente ao município da empresa ativa."));

			if(empresa.aac10municipio.aag0201uf == null) {
				validations.addToValidations(new ValidationMessage("Necessário informar a UF correspondente ao município da empresa ativa."));
			} else {
				if(!empresa.aac10municipio.aag0201uf.aag02uf.equalsIgnoreCase("EX")) {
					if(empresa.aac10municipio.aag0201ibge == null) validations.addToValidations(new ValidationMessage("Necessário informar o código IBGE do município do cadastro da empresa ativa."));
					if(empresa.aac10municipio.aag0201nome == null) validations.addToValidations(new ValidationMessage("Necessário informar o nome do município do cadastro da empresa ativa."));

					Aac1002 ieEmp = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Aac1002", Criterions.eq("aac1002empresa", empresa.aac10id), Criterions.eq("aac1002uf", empresa.aac10municipio.aag0201uf.aag02id)) ;
					if(ieEmp == null) validations.addToValidations(new ValidationMessage("Necessário informar a Inscrição estadual no cadastro da empresa ativa."));
				}
			}
		}
		if(empresa.aac10endereco == null) validations.addToValidations(new ValidationMessage("Necessário informar o endereço no cadastro da empresa ativa."));
		if(empresa.aac10cep == null) validations.addToValidations(new ValidationMessage("Necessário informar o CEP no cadastro da empresa ativa."));
		if(empresa.aac10rs == null) validations.addToValidations(new ValidationMessage("Necessário informar a razão social no cadastro da empresa ativa."));
		if(empresa.aac10bairro == null) validations.addToValidations(new ValidationMessage("Necessário informar o bairro no cadastro da empresa ativa."));
		if(empresa.aac10ni == null) validations.addToValidations(new ValidationMessage("Necessário informar o número de Inscrição no cadastro da empresa ativa."));

		/** Eaa01 - Nota */
		if(eaa01 == null) {
			validations.addToValidations(new ValidationMessage("Necessário informar um documento."));
		} else {
			if(eaa01.eaa01central == null) {
				validations.addToValidations(new ValidationMessage("Documento não encontrado na Central de Documentos."));
			} else {
				String documento = " Documento: " + eaa01.eaa01central.abb01num;

				if(eaa01.eaa01central.abb01tipo == null) validations.addToValidations(new ValidationMessage("Não foi informado o tipo do " + documento));

				modelo = eaa01.eaa01central.abb01tipo.aah01modelo;
				if(modelo == null) validations.addToValidations(new ValidationMessage("Não foi informado o modelo do " + documento));

				if(modelo.equals("55")){
					if(endEntrega == null) validations.addToValidations(new ValidationMessage("Não foi informado o endereço de entrega para o " + documento));
					if(endEntrega != null && endEntrega.eaa0101municipio == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o município do endereço de entrega."));
					if(endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201uf == null) validations.addToValidations(new ValidationMessage("O endereço de entrega do " + documento + ", não contém a UF do município."));

					Aag02 aag02 = endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201uf != null ? getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endEntrega.eaa0101municipio.aag0201uf.aag02id) : null;
					if(aag02 != null && !aag02.aag02uf.equalsIgnoreCase("EX")) {
						if(eaa01.eaa0102DadosGerais != null && eaa01.eaa0102DadosGerais.eaa0102ni == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o número de inscrição da entidade."));
						if(endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201ibge == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o código IBGE do município da entidade."));
						if(endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201nome == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o nome do município da entidade."));
						if(endEntrega != null && endEntrega.eaa0101bairro == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o bairro da entidade."));
						if(endEntrega != null && endEntrega.eaa0101endereco == null) validations.addToValidations(new ValidationMessage("O " + documento + " não contém o endereço da entidade."));
					}
				}

				if(eaa01.eaa01sitDoc == null) validations.addToValidations(new ValidationMessage("Necessário informar a situação do " + documento));
				Aaj03 sit = eaa01.eaa01sitDoc == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aaj03", eaa01.eaa01sitDoc.aaj03id);
				if(sit == null) validations.addToValidations(new ValidationMessage("Não foi informado a situação do " + documento));
				if(sit.aaj03nfe == null) validations.addToValidations(new ValidationMessage("O código da situação do documento para a NFe não foi informada. Situação: " + sit.aaj03codigo));

				if(eaa01.eaa0102DadosGerais == null) validations.addToValidations(new ValidationMessage("Não foi encontrado os dados gerais do " + documento));
				if(eaa01.eaa0102DadosGerais != null && eaa01.eaa0102DadosGerais.eaa0102nome == null) validations.addToValidations(new ValidationMessage("Não foi informado o nome da entidade no " + documento));

				List<Eaa0104> eaa0104s = buscarDeclaracoesDeExportacao(eaa01.eaa01id);
				for(Eaa0104 eaa0104 : eaa0104s) {
					if(eaa0104.eaa0104embLocal != null) {
						if(eaa0104.eaa0104embUF == null) validations.addToValidations(new ValidationMessage("Necessário informar o estado do local de embarque no " + documento));
					}
				}

				/** Notas referenciadas */
				List<Long> docsRef = buscarDocumentosReferenciados(eaa01.eaa01id);
				for(Long notaRefId : docsRef) {
					Eaa01 notaRef = getAcessoAoBanco().buscarRegistroUnicoById("Eaa01", notaRefId);
					if(notaRef != null) {
						Abb01 centralRef = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", notaRef.eaa01central.abb01id);
						if(centralRef == null) validations.addToValidations(new ValidationMessage("Central de documentos do " + documento + " não foi encontrada."));

						String modRef = centralRef.abb01tipo.aah01modelo;

						if(modRef.equals("04")) {
							Eaa0102 dadosGeraisRef = notaRef.eaa0102DadosGerais;
							Eaa0101 endPrincipalRef = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.eaa01id), Criterions.eq("eaa0101principal", 1));

							if(dadosGeraisRef == null) validations.addToValidations(new ValidationMessage("Não foi encontrado os dados gerais para o documento referenciado: " + centralRef.abb01num));
							if(endPrincipalRef == null) validations.addToValidations(new ValidationMessage("Não foi encontrado o endereço principal do documento referenciado: " + centralRef.abb01num));
							if(endPrincipalRef.eaa0101municipio == null) validations.addToValidations(new ValidationMessage("Documento referenciado sem município do emitente."));
							if(endPrincipalRef != null && endPrincipalRef.eaa0101ni == null) validations.addToValidations(new ValidationMessage("NF-e's referenciadas sem o número de inscrição do endereço principal."));
							if(dadosGeraisRef != null && dadosGeraisRef.eaa0102ie == null) validations.addToValidations(new ValidationMessage("NF-e's referenciadas sem a inscrição estadual do endereço principal."));
						}

						if(modRef.equals("55") && notaRef.eaa01nfeChave == null) validations.addToValidations(new ValidationMessage("As NF-e's referenciadas estão sem chave de acesso."));

						if(modRef.equals("57") && notaRef.eaa01nfeChave == null) validations.addToValidations(new ValidationMessage("As NF-e's referenciadas estão sem chave de acesso."));
					}
				}

				/** Veículos */
				Aah20 veiculo = eaa01.eaa0102DadosGerais.eaa0102veiculo;
				if(veiculo != null) {
					if(veiculo.aah20placa == null)	validations.addToValidations(new ValidationMessage("Necessário informar a placa do veículo 1 no " + documento + "."));
					if(veiculo.aah20ufPlaca == null) validations.addToValidations(new ValidationMessage("Necessário informar a UF da placa do veículo 1 no " + documento + "."));
				}

				/** Reboque (X22) */
				Aah20 reboque1 = eaa01.eaa0102DadosGerais.eaa0102reboque1;
				if(reboque1 != null) {
					if(reboque1.aah20placa == null) validations.addToValidations(new ValidationMessage("Necessário informar a placa do reboque 1 no " + documento + "."));
					if(reboque1.aah20ufPlaca == null) validations.addToValidations(new ValidationMessage("Necessário informar a UF da placa do reboque 1 no " + documento + "."));

					Aah20 reboque2 = eaa01.eaa0102DadosGerais.eaa0102reboque2;
					if(reboque2 != null) {
						if(reboque2.aah20placa == null) validations.addToValidations(new ValidationMessage("Necessário informar a placa do reboque 2 no " + documento + "."));
						if(reboque2.aah20ufPlaca == null) validations.addToValidations(new ValidationMessage("Necessário informar a UF da placa do reboque 2 no " + documento + "."));
					}
				}

				/** Despacho */
				Abe01 despacho = eaa01.eaa0102DadosGerais != null && eaa01.eaa0102DadosGerais.eaa0102despacho != null ? getAcessoAoBanco().buscarRegistroUnicoById("Abe01", eaa01.eaa0102DadosGerais.eaa0102despacho.abe01id) : null;
				if(despacho != null) {
					String ie = eaa01.eaa0102DadosGerais != null && eaa01.eaa0102DadosGerais.eaa0102contribIcms == 0 ? null : NFeUtils.formatarIE(despacho.abe01ie);
					if(ie != null && !ie.equals("ISENTO")) {
						Abe0101 endDesp = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abe0101", Criterions.eq("abe0101ent", despacho.abe01id), Criterions.eq("abe0101principal", 1));
						if(endDesp.abe0101municipio == null) validations.addToValidations(new ValidationMessage("Para entidade com IE é necessário informar o municipio do local de despacho no " + documento + "."));
						if(endDesp.abe0101municipio != null && endDesp.abe0101municipio.aag0201uf == null) validations.addToValidations(new ValidationMessage("Para entidade com IE é necessário informar o estado do local de despacho no " + documento + "."));
					}
				}

				/** Local de retirada */
				if(endSaida != null && empresa.aac10endereco.equalsIgnoreCase(endSaida.eaa0101endereco))endSaida = null;
				if(endSaida != null) {
					if(endSaida.eaa0101endereco == null) validations.addToValidations(new ValidationMessage("Necessário informar o endereço do local de retirada no " + documento + "."));
					if(endSaida.eaa0101bairro == null) validations.addToValidations(new ValidationMessage("Necessário informar o bairro do local de retirada no " + documento + "."));
					if(endSaida.eaa0101municipio == null) validations.addToValidations(new ValidationMessage("Necessário informar o município do local de retirada no " + documento + "."));

					Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endSaida.eaa0101municipio.aag0201uf.aag02id);
					if(aag02 != null && !aag02.aag02uf.equalsIgnoreCase("EX")) {
						if(endSaida.eaa0101municipio != null && endSaida.eaa0101municipio.aag0201ibge == null) validations.addToValidations(new ValidationMessage("Necessário informar código IBGE do município do local de retirada no " + documento + "."));
						if(endSaida.eaa0101municipio != null && endSaida.eaa0101municipio.aag0201nome == null) validations.addToValidations(new ValidationMessage("Necessário informar nome do município do local de retirada no " + documento + "."));
					}
				}

				/** entrega - Identificação do local de entrega (G01) */
				if(endEntrega != null && empresa.aac10endereco.equalsIgnoreCase(endEntrega.eaa0101endereco))endEntrega = null;
				if(endEntrega != null) {
					if(endEntrega.eaa0101endereco == null) validations.addToValidations(new ValidationMessage("Necessário informar o endereço do local de entrega no " + documento + "."));
					if(endEntrega.eaa0101bairro == null) validations.addToValidations(new ValidationMessage("Necessário informar o bairro do local de entrega no " + documento + "."));
					if(endEntrega.eaa0101municipio == null) validations.addToValidations(new ValidationMessage("Necessário informar o município do local de entrega no " + documento + "."));

					Aag02 aag02 = getAcessoAoBanco().buscarRegistroUnicoById("Aag02", endEntrega.eaa0101municipio.aag0201uf.aag02id);
					if(aag02 != null && !aag02.aag02uf.equalsIgnoreCase("EX")) {
						if(endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201ibge == null) validations.addToValidations(new ValidationMessage("Necessário informar o código IBGE do município do local de entrega no " + documento + "."));
						if(endEntrega != null && endEntrega.eaa0101municipio != null && endEntrega.eaa0101municipio.aag0201nome == null) validations.addToValidations(new ValidationMessage("Necessário informar o nome do município do local de entrega no " + documento + "."));
					}
				}

				/** Itens */
				List<Eaa0103> eaa0103s = buscarItensDoDocumento(eaa01.eaa01id);
				String classTrib = getAcessoAoBanco().obterString("SELECT aaj01codigo FROM Aac13 INNER JOIN Aaj01 ON aac13classTrib = aaj01id WHERE aac13empresa = :aac10id", Parametro.criar("aac10id", empresa.aac10id));
				if(eaa0103s != null && eaa0103s.size() > 0 && classTrib == null) validations.addToValidations(new ValidationMessage("Necessario informar a classificação tributaria no cadastro da empresa."));
				for(Eaa0103 eaa0103 : eaa0103s) {
					TableMap camposLivresItem = eaa0103.eaa0103json;

					Abm0101 configItem = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", eaa0103.eaa0103item.abm01id), Criterions.eq("abm0101empresa", empresa.aac10id));
					if(configItem == null) validations.addToValidations(new ValidationMessage("Não foi encontrado as configurações para o item " + eaa0103.eaa0103item.abm01codigo));

					if(eaa0103.eaa0103umComl == null) validations.addToValidations(new ValidationMessage("Não foi informado a unidade de medida comercial no item: " + eaa0103.eaa0103item.abm01codigo + " no " + documento));
					if(eaa0103.eaa0103umt == null) validations.addToValidations(new ValidationMessage("Não foi informado a unidade de medida tributária para o item: " + eaa0103.eaa0103item.abm01codigo + " no " + documento));
					if(eaa0103.eaa0103descr == null) validations.addToValidations(new ValidationMessage("Necessário informar a descrição do item " + eaa0103.eaa0103codigo + " no documento." + documento));
					if(eaa0103.eaa0103cfop == null) validations.addToValidations(new ValidationMessage("Necessário informar o CFOP do item " + eaa0103.eaa0103codigo + " no documento." + documento));

					// SIMPLES NACIONAL
					if(classTrib != null && classTrib.equals("001") && eaa0103.eaa0103csosn == null) validations.addToValidations(new ValidationMessage("Necessário informar o CSOSN do item " + eaa0103.eaa0103codigo + " no documento." + documento + "."));

					// OUTRAS CLASSIFICAÇÕES
					if(classTrib != null && !classTrib.equals("001")) {
						if(camposLivresItem == null) validations.addToValidations(new ValidationMessage("Não foram encontrados valores de campos livres."));

						boolean temISS = eaa0103.eaa0103item.abm01tipo < 2 ? false : (camposLivresItem.get(getCampo("322-U04","vISSQN")) != null && !camposLivresItem.get(getCampo("322-U04","vISSQN")).equals(0));
						if(!temISS){

							if(eaa0103.eaa0103cstIcms == null) {
								validations.addToValidations(new ValidationMessage("Necessário informar o CST referente ao ICMS no " + documento + "."));
							}else {
								String cstIcms = eaa0103.eaa0103cstIcms.aaj10codigo.substring(1);
								if(!cstIcms.equals("00") && !cstIcms.equals("10") && !cstIcms.equals("20") && !cstIcms.equals("30") &&
										!cstIcms.equals("40") && !cstIcms.equals("41") && !cstIcms.equals("50") && !cstIcms.equals("51") &&
										!cstIcms.equals("60") && !cstIcms.equals("70") && !cstIcms.equals("90")) {
									validations.addToValidations(new ValidationMessage("O CST referente ao ICMS no " + documento + " é inválido."));
								}
							}
						}
					}

					if(eaa0103.eaa0103cstPis == null) {
						validations.addToValidations(new ValidationMessage("Necessário informar o CST referente ao PIS no " + documento + "."));
					}else {
						String cstPis = eaa0103.eaa0103cstPis.aaj12codigo;
						if(cstPis != null && !cstPis.equals("01") && !cstPis.equals("02") && !cstPis.equals("03") && !cstPis.equals("04") && !cstPis.equals("05") &&
								!cstPis.equals("06") && !cstPis.equals("07") && !cstPis.equals("08") && !cstPis.equals("09") &&
								!cstPis.equals("49") && !cstPis.equals("50") && !cstPis.equals("51") && !cstPis.equals("52") &&
								!cstPis.equals("53") && !cstPis.equals("54") && !cstPis.equals("55") && !cstPis.equals("56") &&
								!cstPis.equals("60") && !cstPis.equals("61") && !cstPis.equals("62") && !cstPis.equals("63") &&
								!cstPis.equals("64") && !cstPis.equals("65") && !cstPis.equals("66") && !cstPis.equals("67") &&
								!cstPis.equals("70") && !cstPis.equals("71") && !cstPis.equals("72") && !cstPis.equals("73") &&
								!cstPis.equals("74") && !cstPis.equals("75") && !cstPis.equals("98") && !cstPis.equals("99")) {
							validations.addToValidations(new ValidationMessage("O CST referente ao PIS no " + documento + " é inválido."));
						}
					}

					if(eaa0103.eaa0103cstCofins == null) {
						validations.addToValidations(new ValidationMessage("Necessário informar o CST referente ao COFINS no " + documento + "."));
					}else {
						String cstCofins = eaa0103.eaa0103cstCofins.aaj13codigo;
						if(!cstCofins.equals("01") && !cstCofins.equals("02") && !cstCofins.equals("03") && !cstCofins.equals("04") && !cstCofins.equals("05") &&
								!cstCofins.equals("06") && !cstCofins.equals("07") && !cstCofins.equals("08") && !cstCofins.equals("09") &&
								!cstCofins.equals("49") && !cstCofins.equals("50") && !cstCofins.equals("51") && !cstCofins.equals("52") &&
								!cstCofins.equals("53") && !cstCofins.equals("54") && !cstCofins.equals("55") && !cstCofins.equals("56") &&
								!cstCofins.equals("60") && !cstCofins.equals("61") && !cstCofins.equals("62") && !cstCofins.equals("63") &&
								!cstCofins.equals("64") && !cstCofins.equals("65") && !cstCofins.equals("66") && !cstCofins.equals("67") &&
								!cstCofins.equals("70") && !cstCofins.equals("71") && !cstCofins.equals("72") && !cstCofins.equals("73") &&
								!cstCofins.equals("74") && !cstCofins.equals("75") && !cstCofins.equals("98") && !cstCofins.equals("99")) {
							validations.addToValidations(new ValidationMessage("O CST referente ao COFINS no " + documento + " é inválido."));
						}
					}

					if((camposLivresItem.get(getCampo("254-O10","vBC")) != null && camposLivresItem.get(getCampo("254-O10","vBC")) != 0) || (camposLivresItem.get(getCampo("256-O12","vUnid")) != null && camposLivresItem.get(getCampo("256-O12","vUnid")) != 0)) {
						if(eaa0103.eaa0103cstIpi == null) validations.addToValidations(new ValidationMessage("Necessário informar o CST referente ao IPI no " + documento + "."));
					}

					if(camposLivresItem.get(getCampo("322-U04","vISSQN")) != null && camposLivresItem.get(getCampo("322-U04","vISSQN")) != 0) {
						if(eaa0103.eaa0103codServ == null) validations.addToValidations(new ValidationMessage("Necessário informar o tipo de serviço do item para tag de ISS no " + documento + "."));
					}

					/** Importação */
					List<Eaa01034> eaa01034s = buscarDeclaracaoDeImportacaoPorItem(eaa0103.eaa0103id);
					for(Eaa01034 eaa01034 : eaa01034s) {
						if(eaa01034.eaa01034dtReg == null) validations.addToValidations(new ValidationMessage("Necessário informar a data de registro da importação no " + documento + "."));
						if(eaa01034.eaa01034ufLocal == null) validations.addToValidations(new ValidationMessage("Necessário informar o estado do local de desembaraço do registro da importação no " + documento + "."));
						if(eaa01034.eaa01034dtDesemb == null) validations.addToValidations(new ValidationMessage("Necessário informar a data do desembaraço do registro da importação no " + documento + "."));
						if(eaa01034.eaa01034codExp == null) validations.addToValidations(new ValidationMessage("Necessário informar código do exportador do registro da importação no " + documento + "."));

						/** Adições	*/
						List<Eaa010341> eaa010341s = buscarAdicoesPorDI(eaa01034.eaa01034id);
						for(Eaa010341 eaa010341 : eaa010341s) {
							if(eaa010341.eaa010341codFabr == null) validations.addToValidations(new ValidationMessage("Necessário informar código do fabricante estrangeiro do registro da importação " + eaa010341.eaa010341num + " no " + documento + "."));
						}
					}
				}

				/** Parcelamento de Cupom Fiscal  */
				if(modelo.equals("65")){
					List<Eaa0113> eaa0113s = buscarFinanceiroPorDocumento(eaa01.eaa01id);
					for(Eaa0113 eaa0113 : eaa0113s) {
						if(eaa0113.eaa0113tipo == null) {
							validations.addToValidations(new ValidationMessage("Necessário informar o tipo de documento referente a parcela para geração da forma de pagamento no " + documento + "."));
						}
					}
				}
			}
		}

		if (validations.hasMessages()) throw validations;
	}

	private Integer verificarFormaPgto(Eaa01 eaa01){
		List<Eaa0113> eaa0113s = buscarFinanceiroPorDocumento(eaa01.eaa01id);
		if(eaa0113s == null || eaa0113s.size() == 0)return null; //Nenhum
		if(eaa0113s.size() > 1) return 1; //A prazo

		for(Eaa0113 eaa0113 : eaa0113s){
			long dif = DateUtils.dateDiff(eaa01.eaa01central.abb01data, eaa0113.eaa0113dtVctoN, ChronoUnit.DAYS);
			if(dif <= 0) return 0; //A vista
			return 1; //A prazo
		}

		return null; //Nenhum
	}

	private void comporFilhosDocumento() {
		// Central
		Abb01 abb01 = getAcessoAoBanco().buscarRegistroUnicoById("Abb01", eaa01.eaa01central.abb01id);
		eaa01.setEaa01central(abb01);

		// Dados gerais
		eaa0102 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0102", Criterions.eq("eaa0102doc", eaa01.eaa01id));
		eaa01.setEaa0102s(Utils.list(eaa0102));

		Aaj03 aaj03 = getAcessoAoBanco().buscarRegistroUnicoById("Aaj03", eaa01.eaa01sitDoc.aaj03id);
		eaa01.setEaa01sitDoc(aaj03);

		// Endereços
		endPrincipal = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.eaa01id), Criterions.eq("eaa0101principal", 1));
		endEntrega = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.eaa01id), Criterions.eq("eaa0101entrega", 1));
		endSaida = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01.eaa01id), Criterions.eq("eaa0101saida", 1));
	}

	private Aac1002 buscarInscricaoEstadualPorEstado(Aac10 aac10) {
		Long uf = aac10.aac10municipio.aag0201uf.aag02id;

		return getSession().createCriteria(Aac1002.class)
				.addJoin(Joins.join("Aac10", "aac10id = aac1002empresa"))
				.addWhere(Criterions.eq("aac1002uf", uf))
				.addWhere(Criterions.eq("aac1002empresa", aac10.aac10id))
				.setMaxResults(1)
				.get(ColumnType.ENTITY);
	}

	private List<Long> buscarDocumentosReferenciados(Long eaa01id) {
		return getSession().createQuery("SELECT DISTINCT eaa01id" +
				" FROM Eaa01033" +
				" INNER JOIN Eaa0103 eaa0103Ref ON eaa01033itemDoc = eaa0103Ref.eaa0103id" +
				" INNER JOIN Eaa01 ON eaa0103Ref.eaa0103doc = eaa01id" +
				" INNER JOIN Abb01 ON abb01id = eaa01central" +
				" INNER JOIN Aah01 ON aah01id = abb01tipo" +
				" INNER JOIN Eaa0103 eaa0103item ON eaa01033item = eaa0103item.eaa0103id" +
				" WHERE eaa0103item.eaa0103doc = :eaa01id")
				.setParameter("eaa01id", eaa01id)
				.getList(ColumnType.LONG);
	}

	private List<Eaa0103> buscarItensDoDocumento(Long eaa01id) {
		return getSession().createCriteria(Eaa0103.class)
				.addJoin(Joins.fetch("eaa0103item"))
				.addJoin(Joins.fetch("eaa0103umu").left(true))
				.addJoin(Joins.fetch("eaa0103umComl").left(true))
				.addJoin(Joins.fetch("eaa0103ncm").left(true))
				.addJoin(Joins.fetch("eaa0103cfop").left(true))
				.addJoin(Joins.fetch("eaa0103cstIcms").left(true))
				.addJoin(Joins.fetch("eaa0103csosn").left(true))
				.addJoin(Joins.fetch("eaa0103cstIpi").left(true))
				.addJoin(Joins.fetch("eaa0103cstPis").left(true))
				.addJoin(Joins.fetch("eaa0103cstCofins").left(true))
				.addJoin(Joins.fetch("eaa0103codServ").left(true))
				.addJoin(Joins.fetch("eaa0103codBcCred").left(true))
				.addWhere(Criterions.eq("eaa0103doc", eaa01id))
				.getList(ColumnType.ENTITY);

	}

	private List<Abg0101> buscarNVEsPorNCM(Long abg01id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Abg0101 WHERE abg0101ncm = :abg01id",
				Parametro.criar("abg01id", abg01id)
		);
	}

	private List<Eaa01034> buscarDeclaracaoDeImportacaoPorItem(Long eaa0103id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Eaa01034 WHERE eaa01034item = :eaa0103id",
				Parametro.criar("eaa0103id", eaa0103id)
		);
	}

	private List<Eaa010341> buscarAdicoesPorDI(Long eaa01034id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Eaa010341 WHERE eaa010341di = :eaa01034id",
				Parametro.criar("eaa01034id", eaa01034id)
		);
	}

	private List<Eaa0104> buscarDeclaracoesDeExportacao(Long eaa01id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Eaa0104 WHERE eaa0104doc = :eaa01id",
				Parametro.criar("eaa01id", eaa01id)
		);
	}

	private List<Eaa01038> buscarRastreabilidadeDoItem(Long eaa0103id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Eaa01038 WHERE eaa01038item = :eaa0103id",
				Parametro.criar("eaa0103id", eaa0103id)
		);
	}

	private List<TableMap> buscarLancamentosDoItem(Long abb01id) {
		return getAcessoAoBanco().buscarListaDeTableMap(
				"SELECT bcc01lote, SUM(bcc01qt) as bcc01qt, bcc01fabric, bcc01validade FROM Bcc01 " +
						"WHERE bcc01central = :central GROUP BY bcc01lote, bcc01validade, bcc01fabric",
				Parametro.criar("central", abb01id)
		);
	}

	private List<Eaa0113> buscarFinanceiroPorDocumento(Long eaa01id) {
		return getAcessoAoBanco().buscarListaDeRegistros(
				"SELECT * FROM Eaa0113 WHERE eaa0113doc = :eaa01id AND eaa0113clasParc = 0 ORDER BY eaa0113dtVctoN",
				Parametro.criar("eaa01id", eaa01id)
		);
	}

	private List<TableMap> buscarFormasDePagamentoPorDocumento(Long eaa01id) {
		String sql = "SELECT abf40descr, abf40meioPgto, SUM(eaa0113valor) AS valor FROM Eaa01131 " +
				"INNER JOIN Eaa0113 ON eaa0113id = eaa01131fin " +
				"INNER JOIN Abf40 ON abf40id = eaa01131fp " +
				"INNER JOIN Eaa01 ON eaa01id = eaa0113doc " +
				"WHERE eaa01id = :eaa01id AND eaa0113clasParc = 0 " +
				"GROUP BY abf40meioPgto, abf40descr";

		return getAcessoAoBanco().buscarListaDeTableMap(sql, Parametro.criar("eaa01id", eaa01id));
	}

	private BigDecimal buscarTotalParcelado(Long eaa01id) {
		String sql = " SELECT SUM(eaa0113valor) FROM Eaa0113 " +
				" LEFT JOIN Eaa01131 ON eaa01131fin = eaa0113id " +
				" WHERE eaa0113clasParc = 0 " +
				" AND eaa01131fin IS NULL " +
				" AND eaa0113doc = :eaa01id";

		return getAcessoAoBanco().obterBigDecimal(sql, criarParametroSql("eaa01id", eaa01id));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjgifQ==