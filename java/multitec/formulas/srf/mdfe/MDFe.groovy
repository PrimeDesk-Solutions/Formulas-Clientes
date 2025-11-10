package multitec.formulas.srf.mdfe;

import java.time.temporal.ChronoUnit

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import br.com.multitec.utils.Utils
import br.com.multitec.utils.xml.ElementXml
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aah20
import sam.model.entities.aa.Aah21
import sam.model.entities.ab.Abb01
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa10;
import sam.model.entities.ea.Eaa1001
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.MDFeUtils;

public class MDFe extends FormulaBase {
	private Eaa10 eaa10;
	private Integer tipo;
	private Aac10 aac10;
	
	private String versaoXMLLayoutMDFe = "3.00";
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_MDFE;
	}

	@Override
	public void executar() {
		eaa10 = get("eaa10");
		tipo = get("tipo");
		
		def formaEmissao = 1; 
		
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		
		def codigoIbge = buscarCodigoIbge(aac10);
		
		def abb01 = getSession().get(Abb01.class, eaa10.eaa10central.abb01id);
		
		def chaveAcesso = MDFeUtils.gerarChaveDeAcesso(codigoIbge, abb01.abb01data, abb01.abb01serie, abb01.abb01num, eaa10.eaa10id, aac10.aac10ni, "58", formaEmissao);

		/*** GERAÇÃO DO XML ***/
		def dtProdNFe = getAcessoAoBanco().buscarParametro("MDFeDataProducao", "EA");
		def isProducao = dtProdNFe == null ? false : DateUtils.dateDiff(dtProdNFe, abb01.abb01data, ChronoUnit.DAYS) >= 0;

		/*** MDFe - Manifesto Eletrônico de Documentos Fiscais ***/
		def mdfe = MDFeUtils.criarElementXmlNFe("http://www.portalfiscal.inf.br/mdfe");

		def infMDFe = mdfe.addNode("infMDFe");
		infMDFe.setAttribute("versao", versaoXMLLayoutMDFe);
		infMDFe.setAttribute("Id", "MDFe" + chaveAcesso);																													


		/*** ide - Identificação do MDFe ***/
		def ide = infMDFe.addNode("ide");
		ide.addNode("cUF", codigoIbge, true);
		ide.addNode("tpAmb", isProducao ? 1 : 2, true);
		ide.addNode("tpEmit", 2, true);
		ide.addNode("mod", "58", true);
		ide.addNode("serie", abb01.abb01serie, true);
		ide.addNode("nMDF", abb01.abb01num, true);
		ide.addNode("cMDF", StringUtils.ajustString(eaa10.eaa10id, 8), true);
		ide.addNode("cDV", chaveAcesso.substring(43), true);
		ide.addNode("modal", 1, true);
		ide.addNode("dhEmi", MDFeUtils.dataFormatoUTC(abb01.abb01data, abb01.abb01operHora), false);
		ide.addNode("tpEmis", formaEmissao, true);
		ide.addNode("procEmi", 0, true);
		ide.addNode("verProc", "SAM4" + Utils.getVersao(), true);

		def ufCar = getSession().get(Aag02.class, eaa10.eaa10ufCar.aag02id);
		def ufDes = getSession().get(Aag02.class, eaa10.eaa10ufDesc.aag02id);
		
		ide.addNode("UFIni", ufCar == null ? null : ufCar.aag02uf, true);
		ide.addNode("UFFim", ufDes == null ? null : ufDes.aag02uf, true);

		/*** infMunCarrega - Municípios de Carregamento ***/
		def aag0201 = getSession().get(Aag0201.class, aac10.aac10municipio.aag0201id);
		def infMunCarrega = ide.addNode("infMunCarrega");
		infMunCarrega.addNode("cMunCarrega", aag0201.aag0201ibge, true);
		infMunCarrega.addNode("xMunCarrega", aag0201.aag0201nome, true);
		
		/*** infPercurso - Percurso do MDFe ***/
		def eaa1001s = buscarPercurso();
		if(eaa1001s != null) {
			for(Eaa1001 eaa1001 : eaa1001s) {
		    	def infPercurso = ide.addNode("infPercurso");
		    	infPercurso.addNode("UFPer", eaa1001.eaa1001uf.aag02uf, true);	
		    }
		}

		/*** emit - Identificação do Emitente do MDFe ***/
		def emit = infMDFe.addNode("emit");
		emit.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(aac10.aac10ni), 14), true);
		emit.addNode("IE", MDFeUtils.formatarIE(obterIEEmpresa()), true);
		emit.addNode("xNome", aac10.aac10rs, true, 60);
		emit.addNode("xFant", aac10.aac10fantasia, false, 60);

		
		/**	enderEmit - Endereço */
		def enderEmit = emit.addNode("enderEmit");
		enderEmit.addNode("xLgr", aac10.aac10endereco, true, 60);
		enderEmit.addNode("nro", aac10.aac10numero, true);
		enderEmit.addNode("xCpl", aac10.aac10complem, false, 60);
		enderEmit.addNode("xBairro", aac10.aac10bairro, true, 60);
		enderEmit.addNode("cMun", aac10.aac10municipio.aag0201ibge, true);
		enderEmit.addNode("xMun", aac10.aac10municipio.aag0201nome, true, 60);
		enderEmit.addNode("CEP", aac10.aac10cep, true);
		
		def uf = getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", aac10.aac10municipio.aag0201uf.aag02id));
		enderEmit.addNode("UF", uf, true);
		enderEmit.addNode("fone", aac10.aac10fone == null ? null : aac10.aac10dddFone == null ? aac10.aac10fone : aac10.aac10dddFone + aac10.aac10fone, false);
		enderEmit.addNode("email", aac10.aac10email, true);


		/*** infModal - Informações do modal ***/
		def infModal = infMDFe.addNode("infModal");
		infModal.setAttribute("versaoModal", "3.00");

		/*** INÍCIO MODAL RODOVIÀRIO ***/
		/*** rodo - Informações do modal Rodoviário ***/
		Aah20 aah20 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah20 WHERE aah20id = :aah20id", criarParametroSql("aah20id", eaa10.eaa10veiculo.aah20id));
		def rodo = infModal.addNode("rodo");
		def infANTT = rodo.addNode("infANTT");
		infANTT.addNode("RNTRC", aah20.aah20rntrc, false);
		
		if(eaa10.eaa10ciot != null) {
			def infCIOT = infANTT.addNode("infCIOT");
			infCIOT.addNode("CIOT", eaa10.eaa10ciot, false);
			
			def ni = StringUtils.extractNumbers(eaa10.eaa10ciotNI);
			infCIOT.addNode(ni.length() > 11 ? "CNPJ" : "CPF", ni, false);	
		}

		/*** veicTracao - Dados do Veículo com a Tração ***/
		def veicTracao = rodo.addNode("veicTracao");
		veicTracao.addNode("cInt", aah20.aah20codigo, false);
		veicTracao.addNode("placa", aah20.aah20placa, true);
		veicTracao.addNode("RENAVAM", aah20.aah20renavam, false);
		veicTracao.addNode("tara", aah20.aah20tara, true);
		veicTracao.addNode("capKG", aah20.aah20capKg == 0 ? null : aah20.aah20capKg, false);
		veicTracao.addNode("capM3", aah20.aah20capM3 == 0 ? null : aah20.aah20capM3, false);

		/*** prop - Proprietários do Veículo (terceiro) ***/
		if(aah20.aah20terceiro == 1) {
			def prop = veicTracao.addNode("prop");
			if(aah20.aah20tercNI.length() > 11) {
				prop.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(aah20.aah20tercNI), 14), true);	
			}else {
				prop.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(aah20.aah20tercNI), 11), true);
			}

			prop.addNode("RNTRC", aah20.aah20rntrc, true);
			prop.addNode("xNome", aah20.aah20tercNome, true);
			prop.addNode("IE", MDFeUtils.formatarIE(aah20.aah20tercIE), true);
			
			def ufTerc = getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", aah20.aah20tercUF.aag02id));
			prop.addNode("UF", ufTerc == null ? null : ufTerc, true);
			prop.addNode("tpProp", aah20.aah20tercTipo, true);
		}

		/*** condutor - Condutor ***/
		Aah21 aah21 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah21 WHERE aah21id = :aah21id", criarParametroSql("aah21id", eaa10.eaa10motorista.aah21id))
		def condutor = veicTracao.addNode("condutor");
		condutor.addNode("xNome", aah21.aah21nome, true, 60);
		condutor.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(aah21.aah21cpf), 11), true);

		veicTracao.addNode("tpRod", StringUtils.ajustString(aah20.aah20rodado, 2), true);
		veicTracao.addNode("tpCar", StringUtils.ajustString(aah20.aah20carroceria, 2), true);
		
		def ufVeic = aah20.aah20licUF == null ? null : getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", aah20.aah20licUF.aag02id));
		veicTracao.addNode("UF", ufVeic, true);

		/*** veicReboque - Dados dos Reboques ***/
		if(eaa10.eaa10reboque1 != null) {
			Aah20 reboque1 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah20 WHERE aah20id = :aah20id", criarParametroSql("aah20id", eaa10.eaa10reboque1.aah20id));
			if(reboque1 != null) {
				def veicReboque1 = rodo.addNode("veicReboque");
				veicReboque1.addNode("cInt", reboque1.aah20codigo, false);
				veicReboque1.addNode("placa", reboque1.aah20placa, true);
				veicReboque1.addNode("tara", reboque1.aah20tara, true);
				veicReboque1.addNode("capKG", reboque1.aah20capKg, true);
				veicReboque1.addNode("capM3", reboque1.aah20capM3, true);
				
				/*** prop - Proprietários do Veículo (terceiro) ***/
				if(reboque1.aah20terceiro == 1) {
					def prop = veicTracao.addNode("prop");
					if(reboque1.aah20tercNI > 11) {
						prop.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(reboque1.aah20tercNI), 14), true);	
					}else {
						prop.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(reboque1.aah20tercNI), 11), true);
					}
					
					prop.addNode("RNTRC", reboque1.aah20rntrc, true);
					prop.addNode("xNome", reboque1.aah20tercNome, true);
					prop.addNode("IE", MDFeUtils.formatarIE(reboque1.aah20tercIE), true);
					
					def ufRebTerc = getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", reboque1.aah20tercUF.aag02id));
					prop.addNode("UF", ufRebTerc, true);
					prop.addNode("tpProp", StringUtils.ajustString(reboque1.aah20tercTipo, 2), true);
				}
				
				veicReboque1.addNode("tpCar", StringUtils.ajustString(reboque1.aah20carroceria, 2), true);
				
				def ufReb = reboque1.aah20licUF == null ? null : getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", reboque1.aah20licUF.aag02id));
				veicReboque1.addNode("UF", ufReb, true);
			}
		}
		
		if(eaa10.eaa10reboque2 != null) {
			Aah20 reboque2 = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Aah20 WHERE aah20id = :aah20id", criarParametroSql("aah20id", eaa10.eaa10reboque2.aah20id));
			if(reboque2 != null) {
				def veicReboque2 = rodo.addNode("veicReboque");
				veicReboque2.addNode("cInt", reboque2.aah20codigo, false);
				veicReboque2.addNode("placa", reboque2.aah20placa, true);
				veicReboque2.addNode("tara", reboque2.aah20tara, true);
				veicReboque2.addNode("capKG", reboque2.aah20capKg, true);
				veicReboque2.addNode("capM3", reboque2.aah20capM3, true);
				
				/*** prop - Proprietários do Veículo (terceiro) ***/
				if(reboque2.aah20terceiro == 1) {
					def prop = veicTracao.addNode("prop");
					if(reboque2.aah20tercNI > 11) {
						prop.addNode("CNPJ", StringUtils.ajustString(StringUtils.extractNumbers(reboque2.aah20tercNI), 14), true);
					}else {
						prop.addNode("CPF", StringUtils.ajustString(StringUtils.extractNumbers(reboque2.aah20tercNI), 11), true);
					}
					
					prop.addNode("RNTRC", reboque2.aah20rntrc, true);
					prop.addNode("xNome", reboque2.aah20tercNome, true);
					prop.addNode("IE", MDFeUtils.formatarIE(reboque2.aah20tercIE), true);
					
					def ufRebTerc = getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", reboque2.aah20tercUF.aag02id));
					prop.addNode("UF", ufRebTerc, true);
					prop.addNode("tpProp", StringUtils.ajustString(reboque2.aah20tercTipo, 2), true);
				}
				
				veicReboque2.addNode("tpCar", StringUtils.ajustString(reboque2.aah20carroceria, 2), true);
				
				def ufReb = reboque2.aah20licUF == null ? null : getAcessoAoBanco().obterString("SELECT aag02uf FROM Aag02 WHERE aag02id = :aag02id", criarParametroSql("aag02id", reboque2.aah20licUF.aag02id));
				veicReboque2.addNode("UF", ufReb, true);
			}
		}
		
		/*** FIM DO MODAL RODOVIÁRIO ***/

		/*** infDoc - Informações dos Documentos vinculados ao MDFe ***/
		def infDoc = infMDFe.addNode("infDoc");

		/*** infMunDescarga - Informações dos municípios de descarregamento ***/
		def setMunEntrega = new HashSet<Long>();
		ElementXml infMunDescarga = null;

		List<Long> eaa01ids = buscarDocumentosSRFDoMDFe(eaa10.eaa10central.abb01id);
		for(def eaa01id : eaa01ids) {
			def eaa01 = getSession().get(Eaa01.class, eaa01id);
			Eaa0101 eaa0101 = buscarEnderecoEntregaDocumento(eaa01id);
			
			if(eaa0101.eaa0101municipio != null) {
				if(!setMunEntrega.contains(eaa0101.eaa0101municipio.aag0201id)) {
					infMunDescarga = infDoc.addNode("infMunDescarga");
					infMunDescarga.addNode("cMunDescarga", eaa0101.eaa0101municipio.aag0201ibge, true);
					infMunDescarga.addNode("xMunDescarga", eaa0101.eaa0101municipio.aag0201nome, true);

					setMunEntrega.add(eaa0101.eaa0101municipio.aag0201id);
				}

				/** infNFe - NFe */													
				def infNFe = infMunDescarga.addNode("infNFe");
				infNFe.addNode("chNFe", eaa01.eaa01nfeChave, true);

				/** infUnidTransp - Informações das Unidades de Transporte */
				if(aah20 != null) {
					def infUnidTransp = infNFe.addNode("infUnidTransp");
					infUnidTransp.addNode("tpUnidTransp", 1, true);
					infUnidTransp.addNode("idUnidTransp", aah20.aah20placa, true);
				}
				
				def reboque = null;
				if (eaa10.eaa10reboque1 != null) {
					 reboque = getSession().get(Aah20.class, eaa10.eaa10reboque1.aah20id);
					if(reboque != null) {
						def infUnidTransp = infNFe.addNode("infUnidTransp");
						infUnidTransp.addNode("tpUnidTransp", 2, true);
						infUnidTransp.addNode("idUnidTransp", reboque.aah20placa, true); 
					}
				}

				if (eaa10.eaa10reboque2 != null) {
					reboque = getSession().get(Aah20.class, eaa10.eaa10reboque2.aah20id);
					if(reboque != null) {
						def infUnidTransp = infNFe.addNode("infUnidTransp");
						infUnidTransp.addNode("tpUnidTransp", 2, true);
						infUnidTransp.addNode("idUnidTransp", reboque.aah20placa, true);
					}
				}
			}
		}

		/*** tot - Totalizadores da carga transportada e seus documentos fiscais ***/
		def tot = infMDFe.addNode("tot");
		tot.addNode("qNFe", eaa01ids.size(), false);
		tot.addNode("vCarga", MDFeUtils.formatarDecimal(eaa10.eaa10valor, 2, true), true);
		tot.addNode("cUnid", StringUtils.ajustString(eaa10.eaa10unidPB == 0 ? 1 : 2, 2), true);
		
		tot.addNode("qCarga", MDFeUtils.formatarDecimal(eaa10.eaa10pesoBruto, 4, false), true);

		/*** infAdic - Informações Adicionais ***/
		def infAdic = infMDFe.addNode("infAdic");
		infAdic.addNode("infAdFisco", eaa10.eaa10obsFisco, false, 2000);
		infAdic.addNode("infCpl", eaa10.eaa10obsContrib, false, 5000);
		
		def qrCode = new StringBuilder();
		qrCode.append("https://dfe-portal.svrs.rs.gov.br/mdfe/QRCode?");
		qrCode.append("chMDFe=");
		qrCode.append(chaveAcesso);
		qrCode.append("&tpAmb=");
		qrCode.append(formaEmissao);
		
		/*** QRCode ***/
		def infMDFeSupl = mdfe.addNode("infMDFeSupl");
		infMDFeSupl.addNode("qrCodMDFe", qrCode, true);
		
		/** Gera o XML */
		def dados = MDFeUtils.gerarXML(mdfe);
		
		put("chaveMDFe", chaveAcesso);
		put("qrCode", qrCode);
		put("dados", dados);
	}
	
	def obterIEEmpresa() {
		def aag02id = getAcessoAoBanco().obterLong(
			" SELECT aag02id FROM Aag0201 INNER JOIN aag02 ON aag02id = aag0201uf WHERE aag0201id = :aag0201id ",
			criarParametroSql("aag0201id", aac10.aac10municipio.aag0201id)
		);
		
		return getAcessoAoBanco().buscarIEEmpresaPorEstado(aac10.aac10id, aag02id);
	}
	
	def buscarCodigoIbge(Aac10 aac10) {
		def sql = " SELECT aag02ibge FROM Aac10 " +
				  " INNER JOIN Aag0201 ON aag0201id = aac10municipio " +
				  " INNER JOIN Aag02 ON aag02id = aag0201uf " +
				  " WHERE aac10id = :municipio " + obterWherePadrao("Aac10");
				  
		return getAcessoAoBanco().obterString(sql, criarParametroSql("municipio", aac10.aac10id));
	}
	
	def buscarPercurso() {
		def sql = " SELECT * FROM Eaa1001 AS eaa1001 " +
				  " INNER JOIN FETCH eaa1001.eaa1001uf AS aag0201 " +
				  " WHERE eaa1001mdfe = :eaa10id ";
				  
		return getAcessoAoBanco().buscarListaDeRegistros(sql, criarParametroSql("eaa10id", eaa10.eaa10id));
	}
	
	def buscarDocumentosSRFDoMDFe(Long abb01id) {
		return getSession().createQuery(" SELECT DISTINCT eaa01id FROM Eaa01 " +
										" INNER JOIN Abb01 ON abb01id = eaa01central " +
										" INNER JOIN Aah01 ON aah01id = abb01tipo " +
										" INNER JOIN Abe01 ON abe01id = abb01ent " +
										" INNER JOIN Eaa0101 ON eaa0101doc = eaa01id " +
										" INNER JOIN Aag0201 ON aag0201id = eaa0101municipio " +
										" INNER JOIN Aag02 ON aag02id = aag0201uf " +
										" INNER JOIN Abb0102 ON eaa01central = abb0102central " +
										" WHERE abb0102doc = :abb01id AND eaa0101entrega = 1 " +
										 obterWherePadrao("Eaa01"))
										.setParameter("abb01id", abb01id)
										.getList(ColumnType.LONG);
	}
	
	def buscarEnderecoEntregaDocumento(Long eaa01id) {
		return getAcessoAoBanco().buscarRegistroUnicoByCriterion("Eaa0101", Criterions.eq("eaa0101doc", eaa01id), Criterions.eq("eaa0101entrega", 1));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzQifQ==