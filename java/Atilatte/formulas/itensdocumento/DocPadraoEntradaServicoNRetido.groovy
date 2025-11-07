package Atilatte.formulas.itensdocumento;

import sam.model.entities.ab.Abe02;

import sam.server.samdev.utils.Parametro;
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag01;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aaj10;
import sam.model.entities.aa.Aaj11;
import sam.model.entities.aa.Aaj12;
import sam.model.entities.aa.Aaj13;
import sam.model.entities.aa.Aaj14;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abb10;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe02;
import sam.model.entities.ab.Abe40;
import sam.model.entities.ab.Abe4001;
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ab.Abm10;
import sam.model.entities.ab.Abm1001;
import sam.model.entities.ab.Abm1003
import sam.model.entities.ab.Abm13;
import sam.model.entities.ab.Abm1301
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101;
import sam.model.entities.ea.Eaa0102;
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase;

public class DocPadraoEntradaServicoNRetido extends FormulaBase {

	private Aac10 aac10;
	private Aag01 aag01;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
	private Aaj12 aaj12_cstPis;
	private Aaj13 aaj13_cstCof;
	private Aam06 aam06;
	
	private Abb01 abb01;
	private Abb10 abb10;
	private Abd01 abd01;
	private Abe01 abe01;
	private Abe02 abe02;
	private Abm01 abm01;
	private Abm0101 abm0101;
	private Abm10 abm10;
	private Abm1001 abm1001;
	private Abm1003 abm1003;
	private Abm13 abm13;
	private Abm1301 abm1301;
	
	private Eaa01 eaa01;
	private Eaa0101 eaa0101princ;
	private Eaa0102 eaa0102;
	private Eaa0103 eaa0103;
	
	private TableMap jsonEaa0103;
	private TableMap jsonAbm1001_UF_Item;
	private TableMap jsonAbm1003_Ent_Item;
	private TableMap jsonAbe01;
	private TableMap jsonAbe02;
	private TableMap jsonAbm0101;
	private TableMap jsonAag02Ent;
	private TableMap jsonAag0201Ent;
	private TableMap jsonAag02Empr;
	private TableMap jsonAac10;

	@Override
	public void executar() {

		//Item do documento
		eaa0103 = get("eaa0103");
		if(eaa0103 == null) return;
		
		//Documento
		eaa01 = eaa0103.eaa0103doc;
		
		for (Eaa0102 dadosGerais : eaa01.eaa0102s) {
			eaa0102 = dadosGerais;
		}
		
		if (eaa0102.eaa0102ti == 1 && eaa0102.eaa0102contribIcms == 1) {
			throw new ValidacaoException("A entidade informada é pessoa física e está caracterizada como contribuinte de ICMS.");
		}
		
		//Central de Documento
		abb01 = eaa01.eaa01central;
		
		//PCD
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
			
		//Dados da Entidade
		abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

		// Entidade (Cliente)
		abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent",abe01.abe01id));
		
		//Endereço principal da entidade no documento
		for (Eaa0101 eaa0101 : eaa01.eaa0101s) {
			if (eaa0101.eaa0101principal == 1) {
				eaa0101princ = eaa0101;
			}
		}
		if (eaa0101princ == null) throw new ValidacaoException("Não foi encontrado o endereço principal da entidade no documento.");
		
		municipioEnt = eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;
		ufEnt = municipioEnt != null ? getSession().get(Aag02.class, municipioEnt.aag0201uf.aag02id) : null;
		aag01 = eaa0101princ.eaa0101pais != null ? getSession().get(Aag01.class, Criterions.eq("aag01id", eaa0101princ.eaa0101pais.aag01id)) : null;
		
		//Empresa
		aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		municipioEmpr = aac10.aac10municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", aac10.aac10municipio.aag0201id)) : null;
		ufEmpr = municipioEmpr != null ? getSession().get(Aag02.class, municipioEmpr.aag0201uf.aag02id) : null;
		
		//Item
		abm01 = eaa0103.eaa0103item != null ? getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id) : null;

		//Configurações do item, por empresa
		abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

		//Valores do Item 
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		
		//Valores do Item - Estados
		abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = "+ ufEnt.aag02id + " AND abm1001cv = "+abm10.abm10id)) : null;
		
		//Valores do Item - Entidade
		abm1003 = abm10 != null && abm10.abm10id != null ? getSession().get(Abm1003.class, Criterions.where("abm1003ent = "+ abe01.abe01id + " AND abm1003cv = "+abm10.abm10id)) : null;
		
		
		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Fatores de Conv. da Unid de Compra para Estoque
		abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));
		
		//Unidade de Medida
		aam06 = abm13 != null &&  abm13.abm13umv != null ? getSession().get(Aam06.class, abm13.abm13umv.aam06id) : null;

		//CST PIS
		aaj12_cstPis = eaa0103.eaa0103cstPis != null ? getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id) : null;

		//CST COFINS
		aaj13_cstCof = eaa0103.eaa0103cstCofins != null ? getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id) : null;

		
		//CAMPOS LIVRES
		jsonAac10 = aac10.aac10json != null ? aac10.aac10json : new TableMap();
		jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
		jsonAag0201Ent = municipioEnt != null && municipioEnt.aag0201json != null ? municipioEnt.aag0201json : new TableMap();
		jsonAag02Empr = ufEmpr != null && ufEmpr.aag02json != null ? ufEmpr.aag02json : new TableMap();
		jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();
		jsonAbe02 = abe02 != null && abe02.abe02json != null ? abe02.abe02json : new TableMap();
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
		jsonAbm1003_Ent_Item = abm1003 != null && abm1003.abm1003json != null ? abm1003.abm1003json : new TableMap();
		jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
		
		
		calcularItem();
		
		eaa0103.eaa0103json = jsonEaa0103;
		put("eaa0103", eaa0103);
	}

	private void calcularItem() {
		
		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0 ) {
			
			//Verifica a Mensagem do IVA no cadastro do Ítem
			if(jsonAbm1001_UF_Item.getString("mensagem") != null){
				jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
			}

			
			//Define o Campo de Unitário para Estoque
			jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

			// *** Processa QUANTIDADES
			// Conserva Qt.Original do documento (Qt.Faturamento original)
			if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
				jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
			}
		

			// Total do item
			eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit) //- jsonEaa0103.getBigDecimal_Zero("outras_despesas");

	          //Total Serviço Item
	          jsonEaa0103.put("servicos_item", eaa0103.eaa0103total);
	          
			// ==================
			// TOTAL DO DOCUMENTO
			// ==================
			//Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("desconto") ;
				
		     eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
	
		  
	       //Valor do Financeiro
		  eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  
			   
		  
	 		
	 		
		//BC Comissão
		 def txdesc = 0;
		 def vlrtxdesc = 0;
			 
		      
	 	 txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
		 vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
		 jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc); 
	 	
		 
	
			     //================================
				//******       IR          ******
				//================================
				if (eaa0103.eaa0103cstPis != null) {
					if(jsonAbm0101.getBigDecimal_Zero("_ir") > 0){
						//Base de Cálculo de PIS
						jsonEaa0103.put("bc_ir", eaa0103.eaa0103totDoc);
							if(jsonEaa0103.getBigDecimal_Zero("_ir") != -1){
								jsonEaa0103.put("_ir", jsonAbm0101.getBigDecimal_Zero("_ir"));
								jsonEaa0103.put("ir", (jsonEaa0103.getBigDecimal_Zero("bc_ir") * jsonEaa0103.getBigDecimal_Zero("_ir"))/100);
							}else{
								jsonEaa0103.put("_ir", 0);
								jsonEaa0103.put("ir", 0);
								jsonEaa0103.put("bc_ir", 0);
							}
						
					}				
				}
	
			     //================================
				//******       ISS          ******
				//================================
				//if (eaa0103.eaa0103cstPis != null) {
					if(jsonAbm0101.getBigDecimal_Zero("_iss") > 0){
						//Base de Cálculo de PIS
						jsonEaa0103.put("bc_iss", eaa0103.eaa0103totDoc);
							if(jsonEaa0103.getBigDecimal_Zero("_iss") != -1){
								jsonEaa0103.put("_iss", jsonAbm0101.getBigDecimal_Zero("_iss"));
								jsonEaa0103.put("iss", (jsonEaa0103.getBigDecimal_Zero("bc_iss") * jsonEaa0103.getBigDecimal_Zero("_iss"))/100);
							}else{
								jsonEaa0103.put("_iss", 0);
								jsonEaa0103.put("iss", 0);
								jsonEaa0103.put("bc_iss", 0);
							}
						
					}				
				//}
				
			     //================================
				//******       InSS          ******
				//================================
				//if (eaa0103.eaa0103cstPis != null) {
					if(jsonAbm0101.getBigDecimal_Zero("_inss") > 0){
						//Base de Cálculo de PIS
						jsonEaa0103.put("bc_inss", eaa0103.eaa0103totDoc);
							if(jsonEaa0103.getBigDecimal_Zero("_inss") != -1){
								jsonEaa0103.put("_inss", jsonAbm0101.getBigDecimal_Zero("_inss"));
								jsonEaa0103.put("inss", (jsonEaa0103.getBigDecimal_Zero("bc_inss") * jsonEaa0103.getBigDecimal_Zero("_inss"))/100);
							}else{
								jsonEaa0103.put("_inss", 0);
								jsonEaa0103.put("inss", 0);
								jsonEaa0103.put("bc_inss", 0);
							}
						
					}
				
			     //================================
				//******       Pis/Cof/Csll          ******
				//================================
				//if (eaa0103.eaa0103cstPis != null) {
					if(jsonAbm0101.getBigDecimal_Zero("_pis_c_csll") > 0){
						//Base de Cálculo de PIS
						jsonEaa0103.put("bc_pis_cof_csll", eaa0103.eaa0103totDoc);
							if(jsonEaa0103.getBigDecimal_Zero("_pis_c_csll") != -1){
								jsonEaa0103.put("_pis_c_csll", jsonAbm0101.getBigDecimal_Zero("_pis_c_csll"));
								jsonEaa0103.put("pis_cofins_csll", (jsonEaa0103.getBigDecimal_Zero("bc_pis_cof_csll") * jsonEaa0103.getBigDecimal_Zero("_pis_c_csll"))/100);
							}else{
								jsonEaa0103.put("_pis_c_csll", 0);
								jsonEaa0103.put("pis_cofins_csll", 0);
								jsonEaa0103.put("bc_pis_cof_csll", 0);
							}
						
					}
	
					// Converte Qt.Documento para Volume
					jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl);
					
				 	//================================
					//******       PIS          ******
					//================================
					if (eaa0103.eaa0103cstPis != null) {
						if(jsonAbm0101.getBigDecimal_Zero("_pis") > 0){
							//Base de Cálculo de PIS
							jsonEaa0103.put("bc_pis", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st"));
								if(jsonEaa0103.getBigDecimal_Zero("_pis") != -1){
									jsonEaa0103.put("_pis", jsonAbm0101.getBigDecimal_Zero("_pis"));
									jsonEaa0103.put("pis", (jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("_pis"))/100);
								}else{
									jsonEaa0103.put("_pis", 0);
									jsonEaa0103.put("pis", 0);
									jsonEaa0103.put("bc_pis", 0);
								}
							
						}				
					}
		
					//================================
					//******      COFINS        ******
					//================================
					if (eaa0103.eaa0103cstCofins != null) {
						if(jsonAbm0101.getBigDecimal_Zero("_cofins") > 0){
							//Base de Cálculo de PIS
							jsonEaa0103.put("bc_cofins", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st"));
								if(jsonEaa0103.getBigDecimal_Zero("_cofins") != -1){
									jsonEaa0103.put("_cofins", jsonAbm0101.getBigDecimal_Zero("_cofins"));
									jsonEaa0103.put("cofins", (jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("_cofins"))/100);
								}else{
									jsonEaa0103.put("_cofins", 0);
									jsonEaa0103.put("cofins", 0);
									jsonEaa0103.put("bc_cofins", 0);
								}
							
						}
		
						
					}
				
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==