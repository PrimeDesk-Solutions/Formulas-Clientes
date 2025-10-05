package multitec.formulas.srf;

import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aaj10;
import sam.model.entities.aa.Aaj11;
import sam.model.entities.aa.Aaj12;
import sam.model.entities.aa.Aaj13;
import sam.model.entities.aa.Aaj15;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abg01;
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ab.Abm10;
import sam.model.entities.ab.Abm1001;
import sam.model.entities.ab.Abm12;
import sam.model.entities.ab.Abm13;
import sam.model.entities.ab.Abm1301;
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101;
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase;

public class CalculoItem_Entrada_Importacao extends FormulaBase {

	private Aac10 aac10;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
	private Aaj10 aaj10_cstIcms;
	private Aaj11 aaj11_cstIpi;
	private Aaj12 aaj12_cstPis;
	private Aaj13 aaj13_cstCof;
	private Aaj15 aaj15_cfop;

	private Abb01 abb01;
	private Abd01 abd01;
	private Abe01 abe01;
	private Abg01 abg01;
	private Abm01 abm01;
	private Abm0101 abm0101;
	private Abm10 abm10;
	private Abm1001 abm1001;
	private Abm12 abm12;
	private Abm13 abm13;
	private Abm1301 abm1301;
	
	private Eaa01 eaa01;
	private Eaa0101 eaa0101princ;
	private Eaa0102 eaa0102;
	private Eaa0103 eaa0103;
	
	private TableMap jsonAag02Ent;
	private TableMap jsonAbe01;
	private TableMap jsonAbm0101;
	private TableMap jsonAbm1001_UF_Item;
	private TableMap jsonEaa0103;
 
	@Override
	public void executar() {

		//Item do documento
		eaa0103 = get("eaa0103");
		if(eaa0103 == null) return;
		
		//Documento
		eaa01 = eaa0103.eaa0103doc;
		
		//Central de Documento
		abb01 = eaa01.eaa01central;
		
		//PCD
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
		
		//Dados da Entidade/Cliente
		abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
		if (abe01 != null && abe01.abe01regTrib == null) throw new ValidacaoException("Entidade sem regime de tributação. Verificar o cadastro e refazer o documento.");
		
		//Endereço principal da entidade no documento
		for (Eaa0101 eaa0101 : eaa01.eaa0101s) {
			if (eaa0101.eaa0101principal == 1) {
				eaa0101princ = eaa0101;
			}
		}
		if (eaa0101princ == null) throw new ValidacaoException("Não foi encontrado o endereço principal da entidade no documento.");
		
		municipioEnt = eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;
		ufEnt = municipioEnt != null ? getSession().get(Aag02.class, municipioEnt.aag0201uf.aag02id) : null;
		
		//Empresa
		aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		municipioEmpr = aac10.aac10municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", aac10.aac10municipio.aag0201id)) : null;
		ufEmpr = municipioEmpr != null ? getSession().get(Aag02.class, municipioEmpr.aag0201uf.aag02id) : null;
		
		//Item
		abm01 = eaa0103.eaa0103item != null ? getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id) : null;

		//Configurações do item, por empresa 
		abm0101 = abm01 != null &&  abm01.abm01id != null && aac10.aac10id != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

		//Dados Fiscais do item
		abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null;
		if (abm12 == null) throw new ValidacaoException("Não foi encontrada a configuração fiscal do item: " + abm01.abm01codigo);

        //Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Fatores de Conv. da Unid de Compra para Estoque
		abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));
		
		//CFOP
		aaj15_cfop = eaa0103.eaa0103cfop != null ? getSession().get(Aaj15.class, eaa0103.eaa0103cfop.aaj15id) : null;
		
		//NCM
		abg01 = eaa0103.eaa0103ncm != null ? getSession().get(Abg01.class, eaa0103.eaa0103ncm.abg01id) : null;
		
		//Valor
		abm10 = abm0101 != null && abm0101.abm0101valores.abm10id != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		abm1001 = ufEnt != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = " + ufEnt.aag02id + " AND abm1001cv = " + abm10.abm10id)) : null;
		
		//CST ICMS
		aaj10_cstIcms = eaa0103.eaa0103cstIcms != null ? getSession().get(Aaj10.class, eaa0103.eaa0103cstIcms.aaj10id) : null;
		
		//CST IPI
		aaj11_cstIpi = eaa0103.eaa0103cstIpi != null ? getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id) : null;
		
		//CST PIS
		aaj12_cstPis = eaa0103.eaa0103cstPis != null ? getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id) : null;
		
		//CST COFINS
		aaj13_cstCof = eaa0103.eaa0103cstCofins != null ? getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id) : null;
		
		//CAMPOS LIVRES
		jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
		jsonAbe01 = abe01.abe01json != null && abe01.abe01json != null ? abe01.abe01json : new TableMap();
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
		jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();

		calcularItem();
		
		eaa0103.eaa0103json = jsonEaa0103;
		put("eaa0103", eaa0103);
	}

	private void calcularItem() {
		//=====================================
		//******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0 && eaa0103.eaa0103unit > 0) {

			// ******* Total do item (sem IPI) *********
			// Total Item = Qtd * vlr.unit
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

			//================================
			//******     Quantidades   *******
			//================================
			// Converte Qt.Documento para Qtde SCE
			eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

			if (abm1301 != null && abm1301.abm1301fcCU > 0) {
				eaa0103.eaa0103qtUso = eaa0103.eaa0103qtUso * abm1301.abm1301fcCU_Zero;
			}

			// Converte Qt.Documento para Volume
			if (abm13 != null && abm13.abm13fcVW > 0) {
				jsonEaa0103.put("vlr_vlme", eaa0103.eaa0103qtComl * abm13.abm13fcVW_Zero);
			}

			//================================
			//***** Peso Bruto e Líquido *****
			//================================
			if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("vlr_pl", (eaa0103.eaa0103qtComl * abm01.abm01pesoLiq_Zero).round(4));
			if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("vlr_pb", (eaa0103.eaa0103qtComl * abm01.abm01pesoBruto_Zero).round(4));

			//================================
			//******     Desconto     ******
			//================================
			if (jsonEaa0103.getBigDecimal("tx_desc_incond") != null) {
				jsonEaa0103.put("vlr_desc", ((eaa0103.eaa0103total * jsonEaa0103.getBigDecimal_Zero("tx_desc_incond")) / 100).round(2));
			}
			
			//================================
			//**  II - Imposto de importação  **
			//================================
			jsonEaa0103.put("ii_aliq", jsonAbm0101.getBigDecimal_Zero("ii_aliq"));
			jsonEaa0103.put("imp_bc", eaa0103.eaa0103total);

			if (jsonEaa0103.getBigDecimal_Zero("ii_aliq") > 0) {
				jsonEaa0103.put("ii_ii", ((jsonEaa0103.getBigDecimal_Zero("imp_bc") * jsonEaa0103.getBigDecimal_Zero("ii_aliq")) / 100).round(2));
			}

			// ===========================
			// **********  IPI  **********
			// ===========================
			jsonEaa0103.put("ipi_bc", eaa0103.eaa0103total);

			//Alíquota de IPI do cadastro de NCM
			if (abg01 != null && abg01.abg01txIpi != null) {
				jsonEaa0103.put("ipi_aliq", abg01.abg01txIpi);
			}

			// Valor do IPI
			if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") > 0) {
				jsonEaa0103.put("ipi_ipi", ((jsonEaa0103.getBigDecimal_Zero("ipi_bc") * jsonEaa0103.getBigDecimal_Zero("ipi_aliq")) / 100).round(2));
			}

			// Se Item tiver isenção de IPI  (Alíquota Zero no NCM) move valor da base para isento
			if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") == 0) {
				jsonEaa0103.put("ipi_ipi", 0.00);
				jsonEaa0103.put("ipi_isento", jsonEaa0103.getBigDecimal_Zero("ipi_bc")); // Move BC IPI p/ IPI Isento
				jsonEaa0103.put("ipi_bc", 0.00);
				jsonEaa0103.put("ipi_aliq", 0.00);
			}

			// ==================================
			// **********    PIS       **********
			// ==================================
			jsonEaa0103.put("pis_aliq", jsonAbm0101.getBigDecimal_Zero("pis_aliq"));

			if(jsonEaa0103.getBigDecimal_Zero("pis_aliq") > 0) {
				jsonEaa0103.put("pis_bc", eaa0103.eaa0103total);
				jsonEaa0103.put("pis_pis", ((jsonEaa0103.getBigDecimal_Zero("pis_bc") * jsonEaa0103.getBigDecimal_Zero("pis_aliq")) / 100).round(2));
			} else {
				jsonEaa0103.put("pis_bc", 0.00);
				jsonEaa0103.put("pis_pis", 0.00);
			}

			// ==================================
			// **********    COFINS    **********
			// ==================================
			jsonEaa0103.put("cofins_aliq", jsonAbm0101.getBigDecimal_Zero("cofins_aliq"));

			if(jsonEaa0103.getBigDecimal_Zero("cofins_aliq") > 0) {
				jsonEaa0103.put("cofins_bc", eaa0103.eaa0103total);
				jsonEaa0103.put("cofins_cofins", ((jsonEaa0103.getBigDecimal_Zero("cofins_bc") * jsonEaa0103.getBigDecimal_Zero("cofins_aliq")) / 100).round(2));
			} else {
				jsonEaa0103.put("cofins_bc", 0.00);
				jsonEaa0103.put("cofins_cofins", 0.00);
			}

			// ==================================
			// **********     ICMS     **********
			// ==================================

			//BC ICMS  = Vlr. Item + Frete + Seguro + Outras + IPI + PIS + COFINS + II
			jsonEaa0103.put("icm_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
				                                             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") + 
				                                             jsonEaa0103.getBigDecimal_Zero("vlr_outras") +
					                                         jsonEaa0103.getBigDecimal_Zero("ipi_ipi") +
                        				                     jsonEaa0103.getBigDecimal_Zero("pis_pis") + 
									                         jsonEaa0103.getBigDecimal_Zero("cofins_cofins") + 
									                         jsonEaa0103.getBigDecimal_Zero("ii_ii"));

			//Alíquota do ICMS
			if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada") > 0) {
				jsonEaa0103.put("icm_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada"));
			}

			// Calcular valor do ICMS  e  Valor ICMS Isento
			if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") > 0) {
				jsonEaa0103.put("icm_bc", (jsonEaa0103.getBigDecimal_Zero("icm_bc") / (1 - (jsonEaa0103.getBigDecimal_Zero("icm_aliq") / 100))).round(2));
				jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
			} else {
				jsonEaa0103.put("icm_icm", 0.00);
				jsonEaa0103.put("icm_isento", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
				jsonEaa0103.put("icm_bc", 0.00);
			}
			
			//Custo de aquisição
			def custoAquisicao = eaa0103.eaa0103total;
			jsonEaa0103.put("custo_aquisicao", (custoAquisicao + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
																 jsonEaa0103.getBigDecimal_Zero("vlr_desc") -
																 jsonEaa0103.getBigDecimal_Zero("icm_icm") -
																 jsonEaa0103.getBigDecimal_Zero("ipi_ipi") -
																 jsonEaa0103.getBigDecimal_Zero("pis_pis") -
																 jsonEaa0103.getBigDecimal_Zero("cofins_cofins")));
															 
			// Total do Documento
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi_ipi") + 
														   jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_outras") +
														   jsonEaa0103.getBigDecimal_Zero("pis_pis") +
														   jsonEaa0103.getBigDecimal_Zero("cofins_cofins") + 
														   jsonEaa0103.getBigDecimal_Zero("icm_icm") +
														   jsonEaa0103.getBigDecimal_Zero("ii_ii");
			
			eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
														   
			//Valor do Financeiro
			eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==