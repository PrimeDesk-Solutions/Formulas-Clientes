package multitec.formulas.spv.sat

import java.math.RoundingMode
import java.util.stream.Collectors

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import br.com.multitec.utils.xml.ElementXml
import br.com.multitec.utils.xml.XMLConverter
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac1002
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aaj01
import sam.model.entities.aa.Aaj04
import sam.model.entities.aa.Aaj05
import sam.model.entities.ab.Abd10
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0113
import sam.model.entities.ea.Eaa01131
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.NFeUtils

class SAT_CFe_Venda extends FormulaBase {
	
	private Eaa01 eaa01;
	private Abd10 abd10;
	
	private Aac10 empresa;
	
	private String versaoDadosEnt = "0.07";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPV_CFE_SAT;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		abd10 = get("abd10");
		
		empresa = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		
		selecionarAlinhamento("0012");
		
		TableMap jsonEaa01 = eaa01.eaa01json;
		
		/**
		 * CFe - Cupom Fiscal Eletrônico SAT
		 */
		ElementXml cfe = XMLConverter.createElement("CFe");
		
		cfe.addCaracterSubstituir('\n'.toCharacter(), "");
		cfe.addCaracterSubstituir('<'.toCharacter(), "&lt;");
		cfe.addCaracterSubstituir('>'.toCharacter(), "&gt;");
		cfe.addCaracterSubstituir('&'.toCharacter(), "&amp;");
		cfe.addCaracterSubstituir('"'.toCharacter(), "&quot;");
		cfe.addCaracterSubstituir('\''.toCharacter(), "&#39;");
		
		/**
		 * infCFe - Dados do CFe (A01)
		 */
		ElementXml infCfe = cfe.addNode("infCFe");
		infCfe.setAttribute("versaoDadosEnt", versaoDadosEnt);
		
		/**
		 * ide - Identificação do CFe (B01)
		 */
		ElementXml ide = infCfe.addNode("ide");
		ide.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10aCnpj), 14), true);
		ide.addNode("signAC", abd10.abd10signAC, true);
		ide.addNode("numeroCaixa", StringUtils.ajustString(abd10.abd10caixa_Zero, 3), true);
		
		/**
		 * emit - Identificação do Emitente do CFe (C01)
		 */
		ElementXml emit = infCfe.addNode("emit");
		
		if(empresa.aac13fiscal == null) {
			throw new ValidacaoException("Não foi informada a classificação tributária da empresa.");
		}
		
		Aac1002 ieEstado = buscarInscricaoEstadualPorEstado(empresa);
		emit.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(empresa.aac10ni), 14), true);
		emit.addNode("IE", NFeUtils.formatarIE(ieEstado.aac1002ie), true);
		emit.addNode("IM", empresa.aac10im == null ? null : NFeUtils.formatarIE(empresa.aac10im), false);
		emit.addNode("cRegTribISSQN", empresa.aac13fiscal.aac13regTrib, false);
		emit.addNode("indRatISSQN", "S", true);
		
		/**
		 * dest - Identificação do Destinatário do CFe (E01)
		 */
		ElementXml dest = infCfe.addNode("dest");
		Eaa0102 eaa0102 = eaa01.getEaa0102DadosGerais();
		if(eaa0102.eaa0102ni != null) {
			String ni = StringUtils.extractNumbers(eaa0102.eaa0102pvCPF);
			if(ni.length() == 14) {
				dest.addNode("CNPJ", StringUtils.ajustString(ni, 14, '0', true), false);
				dest.addNode("xNome", eaa0102.eaa0102nome, false, 60);
			}else if(ni.length() == 11) {
				dest.addNode("CPF", StringUtils.ajustString(ni, 11, '0', true), false);
				dest.addNode("xNome", eaa0102.eaa0102nome, false, 60);
			}
		}
		
		/**
		 * entrega - Grupo de identificação do Local de entrega	(G1)
		 */
		Eaa0101 eaa0101Entrega = eaa01.eaa0101s.stream().filter({eaa0101 -> eaa0101.eaa0101entrega_Zero == 1}).findFirst().orElse(null);
		Eaa0101	eaa0101Principal = eaa01.eaa0101s.stream().filter({eaa0101 -> eaa0101.eaa0101principal_Zero == 1}).findFirst().orElse(null);
		Eaa0101 eaa0101 = eaa0101Entrega != null ? eaa0101Entrega : eaa0101Principal;
		
		if(eaa0101 == null) {
			throw new ValidacaoException("Não foi obtido endereço de entrega ou principal da entidade.");
		}
		
		if(eaa0101.eaa0101municipio == null) {
			throw new ValidacaoException("Necessário informar o município na entidade.");
		}
		
		Aag0201 aag0201 = buscarMunicipioPeloId(eaa0101.eaa0101municipio.aag0201id);
		
		if(eaa0101 != null && eaa0101.eaa0101endereco != null) {
			ElementXml entrega = infCfe.addNode("entrega");
			entrega.addNode("xLgr", eaa0101.eaa0101endereco, true, 60);
			entrega.addNode("nro", eaa0101.eaa0101numero, true);
			entrega.addNode("xCpl", eaa0101.eaa0101complem, false, 60);
			entrega.addNode("xBairro", eaa0101.eaa0101bairro, true, 60);

			entrega.addNode("xMun", aag0201.aag0201nome, true, 60);
			entrega.addNode("UF", aag0201.aag0201uf.aag02uf, true);
		}
		
		/**
		 * det - Detalhamento de Produtos e Serviços do CF-e (H01)
		 */
		List<Eaa0103> eaa0103s = eaa01.eaa0103s.stream().sorted({o1, o2 -> o1.eaa0103seq_Zero.compareTo(o2.eaa0103seq_Zero)}).collect(Collectors.toList());
		int item = 1;
		for(Eaa0103 eaa0103 : eaa0103s) {
			ElementXml det = infCfe.addNode("det");
			det.setAttribute("nItem", item.toString());
			
			TableMap jsonEaa0103 = eaa0103.eaa0103json;
			
			Abm01 abm01 = eaa0103.eaa0103item;
			BigDecimal vAliqISS = jsonEaa0103.get(getCampo("U04","vAliq"));
			
			boolean temISS = abm01.abm01tipo_Zero != Abm01.TIPO_SERV ? false : vAliqISS > 0;
			
			/**
			 * prod - Produtos e Serviços do CF-e (I01)
			 */
			ElementXml prod = det.addNode("prod");
			prod.addNode("cProd", eaa0103.eaa0103codigo, true, 60);
			prod.addNode("cEAN", eaa0103.eaa0103gtin, false);
			prod.addNode("xProd", eaa0103.eaa0103descr, true, 120);
			
			String ncm = null;
			Abg01 abg01 = eaa0103.eaa0103ncm;
			if(abm01.abm01tipo_Zero == Abm01.TIPO_SERV && temISS) {
				ncm = "99";
			}else {
				ncm = abg01 == null ? null : abg01.abg01codigo;
			}
			prod.addNode("NCM", ncm, false);
			
			if(eaa0103.eaa0103cfop == null) {
				throw new ValidacaoException("Não foi encontrado o CFOP no item da venda.");
			}
			
			prod.addNode("CFOP", eaa0103.eaa0103cfop.aaj15codigo, true);
			
			if(eaa0103.eaa0103umComl == null) {
				throw new ValidacaoException("Não foi informada a unidade de medida comercial.");
			}
			
			prod.addNode("uCom", eaa0103.eaa0103umComl.aam06codigo, true, 6);
			prod.addNode("qCom", formatarDecimal(eaa0103.eaa0103qtComl, 4, false), true);
			prod.addNode("vUnCom", formatarDecimal(eaa0103.eaa0103unit, 2, false), true);
			prod.addNode("indRegra", "A", true); //Indicador da regra de cálculo utilizada para Valor Bruto dos Produtos e Serviços: A - Arredondamento T - Truncamento
			prod.addNode("vDesc", formatarDecimal(jsonEaa0103.get(getCampo("I12","vDesc")), 2, true), false);
			prod.addNode("vOutro", formatarDecimal(jsonEaa0103.get(getCampo("I13","vOutro")), 2, true), false);
			
			/**
			 * CEST / COD. ANP
			 */
			Abm0101 abm0101 = getAcessoAoBanco().buscarRegistroUnicoByCriterion("Abm0101", Criterions.eq("abm0101item", eaa0103.eaa0103item.abm01id), Criterions.eq("abm0101empresa", empresa.aac10id));
			
			if(eaa0103.eaa0103cest != null) {
				ElementXml obsFiscoDet = prod.addNode("obsFiscoDet");
				obsFiscoDet.setAttribute("xCampoDet", "Cod. CEST");
				obsFiscoDet.addNode("xTextoDet", eaa0103.eaa0103cest, true);
			}else {
				if(abm0101 != null && abm0101.abm0101fiscal != null && abm0101.abm0101fiscal.abm12codANP != null) {
					ElementXml obsFiscoDet = prod.addNode("obsFiscoDet");
					obsFiscoDet.setAttribute("xCampoDet", "Cód. Produto ANP");
					
					Aaj04 aaj04 = abm0101.abm0101fiscal.abm12codANP == null ? null : getAcessoAoBanco().buscarRegistroUnicoById("Aaj04", abm0101.abm0101fiscal.abm12codANP.aaj04id);
					obsFiscoDet.addNode("xTextoDet", aaj04.aaj04codigo, true);
				}
			}
			
			/**
			 * imposto - Tributos incidentes no Produto ou Serviço (M01)
			 */
			ElementXml imposto = det.addNode("imposto");
			
			imposto.addNode("vItem12741", formatarDecimal(jsonEaa0103.get(getCampo("M02","vItem12741")), 2, false), true);
			
			//Se houver ISS não gerar ICMS e vice-versa
			
			/** ISSQN - ISSQN (U01) */
			if(temISS) {
				ElementXml ISSQN = imposto.addNode("ISSQN");
				
				ISSQN.addNode("vDeducISSQN", formatarDecimal(jsonEaa0103.get(getCampo("U02","vDeducISSQN")), 2, false), true);
				ISSQN.addNode("vAliq", formatarDecimal(jsonEaa0103.get(getCampo("U04","vAliq")), 2, false), true);
				ISSQN.addNode("cMunFG", aag0201.aag0201ibge, false);
				
				Aaj05 aaj05 = eaa0103.eaa0103codServ;
				ISSQN.addNode("cListServ", aaj05 == null ? null : aaj05.aaj05codigo, false);
				ISSQN.addNode("cServTribMun", aaj05 == null ? null : aaj05.aaj05codigo, false);
				
				int cNatOp = eaa0103.eaa0103issExig;
				if(cNatOp == 2) {
					cNatOp = 7;
				}else if(cNatOp == 4) {
					cNatOp = 8;
				}else if(cNatOp == 5) {
					cNatOp = 4;
				}else if(cNatOp == 6) {
					cNatOp = 5;
				}else if(cNatOp == 7) {
					cNatOp = 6;
				}
				ISSQN.addNode("cNatOp", StringUtils.ajustString(cNatOp, 2), true);
				
				Integer indIncFisc = 2;
				if(abm0101 != null && abm0101.abm0101fiscal != null) {
					indIncFisc = abm0101.abm0101fiscal.abm12incFisc_Zero == 1 ? 1 : 2;
				}
				ISSQN.addNode("indIncFisc", indIncFisc, true);
				
			}else {
				
				Aaj01 aaj01 = empresa.aac13fiscal.aac13classTrib;
				if(aaj01.aaj01nfe == null) {
					throw new ValidacaoException("Código de regime tributário - NFe não informado no cadastro da empresa.");
				}
				
				/**
				 * ICMS - ICMS Normal e ST (N01)
				 */
				ElementXml ICMS = imposto.addNode("ICMS");
				
				if(aaj01.aaj01nfe == "1") { //Simples Nacional
					
					String csosn = eaa0103.eaa0103csosn == null ? null : eaa0103.eaa0103csosn.aaj14codigo;
					if(csosn != null) {
						String orig = csosn.substring(0, 1);
						String cso = csosn.substring(1);
						
						if(cso.equals("102") || cso.equals("300") || cso.equals("400") || cso.equals("500")) {
							/** ICMSSN102 */
							
							ElementXml ICMSSN102 = ICMS.addNode("ICMSSN102");
							ICMSSN102.addNode("Orig", orig, true);
							ICMSSN102.addNode("CSOSN", cso, true);
						
						}else if(cso.equals("900")) {
							/** ICMSSN900 */
							
							ElementXml ICMSSN900 = ICMS.addNode("ICMSSN900");
							ICMSSN900.addNode("Orig", orig, true);
							ICMSSN900.addNode("CSOSN", cso, true);
							ICMSSN900.addNode("pICMS", formatarDecimal(jsonEaa0103.get(getCampo("N08","pICMS")), 2, false), true);
						}
					}
					
				}else {
					
					String cstIcms = eaa0103.eaa0103cstIcms == null ? null : eaa0103.eaa0103cstIcms.aaj10codigo;
					if(cstIcms != null) {
						String orig = cstIcms.substring(0, 1);
						String cst = cstIcms.substring(1);
						
						if(cst.equals("00") || cst.equals("20") || cst.equals("90")){
							/** ICMS00 */
							
							ElementXml ICMS00 = ICMS.addNode("ICMS00");
							ICMS00.addNode("Orig", orig, true);
							ICMS00.addNode("CST", cst, true);
							ICMS00.addNode("pICMS", formatarDecimal(jsonEaa0103.get(getCampo("N08","pICMS")), 2, false), true);
							
						}else if(cst.equals("40") || cst.equals("41") || cst.equals("60")) {
							/** ICMS40 */
							
							ElementXml ICMS40 = ICMS.addNode("ICMS40");
							ICMS40.addNode("Orig", orig, true);
							ICMS40.addNode("CST", cst, true);
						}	
					}
				}
				
				/**
				 * PIS - PIS (Q01)
				 */
				ElementXml PIS = imposto.addNode("PIS");
				
				String cstPis = eaa0103.eaa0103cstPis == null ? null : eaa0103.eaa0103cstPis.aaj12codigo;
				if(cstPis != null) {
					
					if(cstPis.equals("01") || cstPis.equals("02") || cstPis.equals("05")) {
						/** PISAliq - PIS tributado pela alíquota, CST = 01 , 02 e 05 (Q02) */
						
						ElementXml PISAliq = PIS.addNode("PISAliq");
						PISAliq.addNode("CST", cstPis, true);
						PISAliq.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("Q08","vBC")), 2, false), true);
						PISAliq.addNode("pPIS", formatarDecimal(jsonEaa0103.get(getCampo("Q09","pPIS")), 4, false), true);
						
					}else if(cstPis.equals("03")) {
						/** PISQtde - PIS tributado por quantidade, CST = 03 (Q03) */
						
						ElementXml PISQtde = PIS.addNode("PISQtde");
						PISQtde.addNode("CST", cstPis, true);
						PISQtde.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("Q11","qBCProd")), 4, false), true);
						PISQtde.addNode("vAliqProd", formatarDecimal(jsonEaa0103.get(getCampo("Q12","vAliqProd")), 4, false), true);
						
					}else if(cstPis.equals("04") || cstPis.equals("06") || cstPis.equals("07") || cstPis.equals("08") || cstPis.equals("09")) {
						/** PISNT - PIS não-tributado, CST = 04, 06, 07, 08 e 09 (Q04) */
						
						ElementXml PISNT = PIS.addNode("PISNT");
						PISNT.addNode("CST", cstPis, true);
						
					}else if(cstPis.equals("49")) {
						/** PISSN - PIS Simles Nacional, CST = 49 (Q05) */
						
						ElementXml PISSN = PIS.addNode("PISSN");
						PISSN.addNode("CST", cstPis, true);
						
					}else if(cstPis.equals("99")) {
						/** PISOutr - PIS Outras operações, CST = 99 (Q06) */
						
						ElementXml PISOutr = PIS.addNode("PISOutr");
						PISOutr.addNode("CST", cstPis, true);
						
						BigDecimal q09_pPIS = jsonEaa0103.get(getCampo("Q09","pPIS"));
						
						if(q09_pPIS != null && q09_pPIS != 0) {
							PISOutr.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("Q08","vBC")), 2, false), true);
							PISOutr.addNode("pPIS", formatarDecimal(q09_pPIS, 4, false), true);
						}else{
							PISOutr.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("Q11","qBCProd")), 4, false), true);
							PISOutr.addNode("vAliqProd", formatarDecimal(jsonEaa0103.get(getCampo("Q12","vAliqProd")), 4, false), true);
						}
					}
				}
				
				/**
				 * PISST - PIS ST (R01)
				 */
				BigDecimal r03_pPIS = jsonEaa0103.get(getCampo("R03","pPIS"));
				BigDecimal r05_vAliqProd = jsonEaa0103.get(getCampo("R05","vAliqProd"));
				
				if((r03_pPIS != null && r03_pPIS != 0) || (r05_vAliqProd != null && r05_vAliqProd != 0)) {
					ElementXml PISST = imposto.addNode("PISST");
										
					if(r03_pPIS != null && r03_pPIS != 0) {
						PISST.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("R02","vBC")), 2, false), true);
						PISST.addNode("pPIS", formatarDecimal(r03_pPIS, 4, false), true);
					}else {
						PISST.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("R04","qBCProd")), 4, false), true);
						PISST.addNode("vAliqProd", formatarDecimal(jsonEaa0103.get(getCampo("R05","vAliqProd")), 4, false), true);
					}
				}
				
				/**
				 * COFINS - COFINS (S01)
				 */
				ElementXml COFINS = imposto.addNode("COFINS");
				
				String cstCofins = eaa0103.eaa0103cstCofins == null ? null : eaa0103.eaa0103cstCofins.aaj13codigo;
				if(cstCofins != null) {
					
					if(cstCofins.equals("01") || cstCofins.equals("02") || cstCofins.equals("05")) {
						/** COFINSAliq - COFINS tributado pela alíquota, CST = 01, 02 ou 05 (S02) */
						
						ElementXml COFINSAliq = COFINS.addNode("COFINSAliq");
						COFINSAliq.addNode("CST", cstCofins, true);
						COFINSAliq.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("S08","vBC")), 2, false), true);
						COFINSAliq.addNode("pCOFINS", formatarDecimal(jsonEaa0103.get(getCampo("S09","pCOFINS")), 4, false), true);
						
					}else if(cstCofins.equals("03")) {
						/** COFINSQtde - COFINS tributado por Qtde, CST = 03 (S03)*/
						
						ElementXml COFINSQtde = COFINS.addNode("COFINSQtde");
						COFINSQtde.addNode("CST", cstCofins, true);
						COFINSQtde.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("S11","qBCProd")), 4, false), true);
						COFINSQtde.addNode("vAliqProd", formatarDecimal(jsonEaa0103.get(getCampo("S12","vAliqProd")), 4, false), true);
						
					}else if(cstCofins.equals("04") || cstCofins.equals("06") || cstCofins.equals("07") || cstCofins.equals("08") || cstCofins.equals("09")) {
						/** COFINSNT - COFINS não tributado, CST = 04, 06, 07, 08 ou 09 (S04) */
						
						ElementXml COFINSNT = COFINS.addNode("COFINSNT");
						COFINSNT.addNode("CST", cstCofins, true);
						
					}else if(cstCofins.equals("49")) {
						/** COFINSSN - COFINS Simples Nacional, CST = 49 (S05) */
						
						ElementXml COFINSSN = COFINS.addNode("COFINSSN");
						COFINSSN.addNode("CST", cstCofins, true);
						
					}else if(cstCofins.equals("99")) {
						/** COFINSOutr - COFINS outras operações, CST = 99 (S06) */
						
						ElementXml COFINSOutr = COFINS.addNode("COFINSOutr");
						COFINSOutr.addNode("CST", cstCofins, true);
						
						BigDecimal s09_pCOFINS = jsonEaa0103.get(getCampo("S09","pCOFINS"));
						
						if(s09_pCOFINS != null && s09_pCOFINS != 0) {
							COFINSOutr.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("S08","vBC")), 2, false), true);
							COFINSOutr.addNode("pCOFINS", formatarDecimal(jsonEaa0103.get(getCampo("S09","pCOFINS")), 4, false), true);
						}else{
							COFINSOutr.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("S11","qBCProd")), 4, false), true);
							COFINSOutr.addNode("vAliqProd", formatarDecimal(jsonEaa0103.get(getCampo("S12","vAliqProd")), 4, false), true);
						}
					}
				}
				
				/** COFINSST - COFINS ST (T01) */
				BigDecimal t03_pCOFINS = jsonEaa0103.get(getCampo("T03","pCOFINS"));
				BigDecimal t05_vAliqProd = jsonEaa0103.get(getCampo("T05","vAliqProd"));
				
				if((t03_pCOFINS != null && t03_pCOFINS != 0) || (t05_vAliqProd != null && t05_vAliqProd != 0)) {
					ElementXml COFINSST = imposto.addNode("COFINSST");
										
					if(t03_pCOFINS != null && t03_pCOFINS != 0) {
						COFINSST.addNode("vBC", formatarDecimal(jsonEaa0103.get(getCampo("T02","vBC")), 2, false), true);
						COFINSST.addNode("pCOFINS", formatarDecimal(t03_pCOFINS, 4, false), true);
					}else {
						COFINSST.addNode("qBCProd", formatarDecimal(jsonEaa0103.get(getCampo("T04","qBCProd")), 4, false), true);
						COFINSST.addNode("vAliqProd", formatarDecimal(t05_vAliqProd, 4, false), true);
					}
				}
			}
			
			/**
			 * infAdProd - Informações Adicionais (V01)
			 */
			det.addNode("infAdProd", eaa0103.eaa0103infAdic, false, 500);
			
			item++;	
		}
		
		/**
		 * total - Valores totais do CF-e (W01)
		 */
		ElementXml total = infCfe.addNode("total");
		
		/** Desconto/Acréscimo sobre Subtotal */
		BigDecimal vDescSubtot = jsonEaa01.get(getCampo("W20","vDescSubtot"));
		BigDecimal vAcresSubtot = jsonEaa01.get(getCampo("W21","vAcresSubtot"));
		
		if((vDescSubtot != null && vDescSubtot != 0) || (vAcresSubtot != null && vAcresSubtot != 0)) {
			ElementXml DescAcrEntr = total.addNode("DescAcrEntr");
			
			if(vDescSubtot != null && vDescSubtot != 0) {
				DescAcrEntr.addNode("vDescSubtot", formatarDecimal(vDescSubtot, 2, false), true);
			}
			
			if(vAcresSubtot != null && vAcresSubtot != 0) {
				DescAcrEntr.addNode("vAcresSubtot", formatarDecimal(vAcresSubtot, 2, false), true);
			}
		}
		
		/** Valor aproximado dos tributos do CFe-SAT – Lei 12741/12. */
		total.addNode("vCFeLei12741", formatarDecimal(jsonEaa01.get(getCampo("W22","vCFeLei12741")), 2, false), true);
		
		/**
		 * pgto - Informações sobre Pagamento (WA)
		 */
		ElementXml pgto = infCfe.addNode("pgto");
		
		Map<String, BigDecimal> mapPgtos = new HashMap<>();
		
		if(eaa01.eaa0113s == null || eaa01.eaa0113s.size() == 0) {
			mapPgtos.put("99", BigDecimal.ZERO);
		}else {
			for(Eaa0113 eaa0113 : eaa01.eaa0113s) {
				if(eaa0113.eaa01131s != null && eaa0113.eaa01131s.size() > 0) {
					for(Eaa01131 eaa01131 : eaa0113.eaa01131s) {
						if(eaa01131.eaa01131valor < 0) continue;
						
						String meioPgto = eaa01131.eaa01131fp.abf40meioPgto;
						BigDecimal valorPgto = BigDecimal.ZERO;
						if(mapPgtos.containsKey(meioPgto)) {
							valorPgto = mapPgtos.get(meioPgto);
						}
						valorPgto = valorPgto + eaa01131.eaa01131valor;
						
						mapPgtos.put(meioPgto, valorPgto);
					}
				}else {
					String obs = eaa0113.eaa0113obs;
					if(obs == null) obs = "";
					
					String meioPgto = null;
					if(obs.toLowerCase().contains("cashback")) {
						meioPgto = "05";
					}else {
						meioPgto = "99";
					}
					
					BigDecimal valorPgto = BigDecimal.ZERO;
					if(mapPgtos.containsKey(meioPgto)) {
						valorPgto = mapPgtos.get(meioPgto);
					}
					valorPgto = valorPgto + eaa0113.eaa0113valor;
					
					mapPgtos.put(meioPgto, valorPgto);
				}
			}
		}
		
		Set<String> setPgtos = mapPgtos.keySet().stream().sorted().collect(Collectors.toList());
		for(String cMP : setPgtos) {
			BigDecimal vMP = mapPgtos.get(cMP);
			
			ElementXml MP = pgto.addNode("MP");
			MP.addNode("cMP", cMP, true);
			MP.addNode("vMP", formatarDecimal(vMP, 2, false), true);
			
			if(cMP == 3 || cMP == 4) {
				String cAdmC = getCampo("WA05","cAdmC");
				MP.addNode("cAdmC", cAdmC, false);
			}
		}
		
		/**
		 * infAdic - Informações Adicionais (Z01)
		 */
		if(eaa01.eaa01obsContrib != null && eaa01.eaa01obsContrib.length() > 0){
			ElementXml infAdic = infCfe.addNode("infAdic");
			infAdic.addNode("infCpl", eaa01.eaa01obsContrib.trim(), false, 5000);
		}
				
		String dados = NFeUtils.gerarXML(cfe);
		
		put("dados", dados);
	}
	
	private Aac1002 buscarInscricaoEstadualPorEstado(Aac10 aac10) {
		Long uf = aac10.aac10municipio.aag0201uf.aag02id;
		
		return getSession().createCriteria(Aac1002.class)
						   .addJoin(Joins.join("Aac10", "aac10id = aac1002empresa"))
						   .addWhere(Criterions.eq("aac1002uf", uf))
						   .addWhere(Criterions.eq("aac1002empresa", aac10.aac10id))
						   .setMaxResults(1)
						   .get();
	}
	
	private Aag0201 buscarMunicipioPeloId(Long aag0201id) {
		return getSession().createCriteria(Aag0201.class)
						   .addJoin(Joins.fetch("aag0201uf"))
						   .addWhere(Criterions.eq("aag0201id", aag0201id))
						   .get();
	}
	
	private static String formatarDecimal(BigDecimal value, int casasDecimais, boolean vlrZeroRetornaNull) {
		if(value == null || value.compareTo(BigDecimal.ZERO) == 0) {
			if(vlrZeroRetornaNull) {
				return null;
			}
			value = BigDecimal.ZERO;
		}

		BigDecimal bigDecimal = value.setScale(casasDecimais, RoundingMode.HALF_EVEN);
		return bigDecimal.toString();
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzUifQ==