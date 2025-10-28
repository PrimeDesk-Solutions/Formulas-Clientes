package multitec.formulas.srf;

import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm1002
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase;

public class CalculoServico_Entrada extends FormulaBase{

	private Aac10 aac10;
	private Aag0201 municipioEnt;
	
	private Abm01 abm01;
	private Abm0101 abm0101;
	private Abm10 abm10;
	private Abm1002 abm1002;
	
	private Eaa01 eaa01;
	private Eaa0101 eaa0101princ;
	private Eaa0103 eaa0103;

	private TableMap jsonAbm0101;
	private TableMap jsonAbm1002;
	private TableMap jsonEaa0103;
	
	@Override
	public void executar() {
		//Item do documento
		eaa0103 = get("eaa0103");
		if(eaa0103 == null) return;

		//Documento
		eaa01 = eaa0103.eaa0103doc;

		//Endereço principal da entidade
		for (Eaa0101 eaa0101 : eaa01.eaa0101s) {
			if (eaa0101.eaa0101principal == 1) {
				eaa0101princ = eaa0101;
			}
		}
		if (eaa0101princ == null) throw new ValidacaoException("Não foi encontrado o endereço principal da entidade no documento.");

		municipioEnt = eaa0101princ != null && eaa0101princ.eaa0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", eaa0101princ.eaa0101municipio.aag0201id)) : null;

		//Empresa
		aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);
		
		//Item
		abm01 = eaa0103.eaa0103item.abm01id != null ? getSession().get(Abm01.class, eaa0103.eaa0103item.abm01id) : null;

		//Configurações do item, por empresa 
		abm0101 = abm01 != null &&  abm01.abm01id != null && aac10.aac10id != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;
		if (abm0101 == null) throw new ValidacaoException("Verifique a configuração por empresa do item.");

		//Valores do Item
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		abm1002 = municipioEnt != null && municipioEnt.aag0201id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1002.class, Criterions.where("abm1002municipio = " + municipioEnt.aag0201id + " AND abm1002cv = " + abm10.abm10id)) : null;
		
		//CAMPOS LIVRES
		jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
		jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAbm1002 = abm1002 != null && abm1002.abm1002json != null ? abm1002.abm1002json : new TableMap();

		calcularItem();

		eaa0103.eaa0103json = jsonEaa0103;
		put("eaa0103", eaa0103);
	}

	private void calcularItem() {
		if (eaa0103.eaa0103tipo != 3) throw new ValidacaoException("Item não está caracterizado como Serviço.");

		if (eaa0103.eaa0103qtComl > 0 && eaa0103.eaa0103unit > 0) {

			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;
			//Valor do serviço
			def vlrTotalServ = eaa0103.eaa0103total;
			
			eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

			//Alíquotas
			def aliq_IR = jsonAbm0101.getBigDecimal_Zero("ir_aliq");
			def aliq_INSS = jsonAbm0101.getBigDecimal_Zero("inss_aliq");
			def aliq_PisCofCsll = jsonAbm0101.getBigDecimal_Zero("pis_cof_csll_aliq")
			jsonEaa0103.put("ir_aliq", aliq_IR);
			jsonEaa0103.put("inss_aliq", aliq_INSS);
			jsonEaa0103.put("pis_cof_csll_aliq", aliq_PisCofCsll);
			jsonEaa0103.put("pis_aliq", jsonAbm0101.getBigDecimal_Zero("pis_aliq"));
			jsonEaa0103.put("cofins_aliq", jsonAbm0101.getBigDecimal_Zero("cofins_aliq"));

			def aliq_ISS = jsonAbm1002.getBigDecimal_Zero("iss_aliq");
			jsonEaa0103.put("iss_aliq", aliq_ISS);
			
			//Desconto incondicional
			if (jsonEaa0103.getBigDecimal("tx_desc_incond") != null) {
				jsonEaa0103.put("vlr_desc", ((eaa0103.eaa0103total * jsonEaa0103.getBigDecimal_Zero("tx_desc_incond")) / 100).round(2));
			}

			//Total do serviço + Outras - Desconto
			vlrTotalServ = vlrTotalServ + jsonEaa0103.getBigDecimal_Zero("vlr_outras") - jsonEaa0103.getBigDecimal_Zero("vlr_desc");

			//====================
			//      IMPOSTOS
			//====================
			//Imposto de Renda
			if (aliq_IR > 0 ) {
				jsonEaa0103.put("ir_bc", vlrTotalServ);
				jsonEaa0103.put("ir_ir", ((jsonEaa0103.getBigDecimal_Zero("ir_bc") * aliq_IR) / 100).round(2));

				def vlrIR = jsonEaa0103.getBigDecimal_Zero("ir_ir");

				//Só retém valor de IR Maior que R$ 10,00
				if (vlrIR <= 10) {
					jsonEaa0103.put("ir_bc", 0.00);
					jsonEaa0103.put("ir_ir", 0.00);
				}
			}

			//Cálculo INSS
			if (aliq_INSS > 0) {
				jsonEaa0103.put("inss_bc", vlrTotalServ);
				jsonEaa0103.put("inss_vlr", ((jsonEaa0103.getBigDecimal_Zero("inss_bc") * aliq_INSS) / 100).round(2));
			}

			//Cálculo ISS
			if (aliq_ISS > 0) {
				jsonEaa0103.put("iss_bc", vlrTotalServ);
				jsonEaa0103.put("iss_iss", ((jsonEaa0103.getBigDecimal_Zero("iss_bc") * aliq_ISS) / 100).round(2));
			}

			//Cálculo PIS/COFINS/CSLL
			if (aliq_PisCofCsll > 0) {
				jsonEaa0103.put("pis_cof_csll_bc", vlrTotalServ);
				jsonEaa0103.put("pis_cofins_csll", ((jsonEaa0103.getBigDecimal_Zero("pis_cof_csll_bc") * aliq_PisCofCsll) / 100).round(2));
			}

			//PIS
			if (jsonEaa0103.getBigDecimal_Zero("pis_aliq") > 0) {
				jsonEaa0103.put("pis_bc", vlrTotalServ);
				jsonEaa0103.put("pis_pis", ((jsonEaa0103.getBigDecimal_Zero("pis_bc") * jsonEaa0103.getBigDecimal_Zero("pis_aliq")) / 100).round(2));
			}

			//COFINS
			if (jsonEaa0103.getBigDecimal_Zero("cofins_aliq") > 0) {
				jsonEaa0103.put("cofins_bc", vlrTotalServ);
				jsonEaa0103.put("cofins_cofins", ((jsonEaa0103.getBigDecimal_Zero("cofins_bc") * jsonEaa0103.getBigDecimal_Zero("cofins_aliq")) / 100).round(2));
			}

			//====================
			// Totais do documento
			//====================
			jsonEaa0103.put("vlr_total_serv", vlrTotalServ);

			//Total Serviços (Liquido)
			jsonEaa0103.put("vlr_total_serv_liq", vlrTotalServ - 
				                                  jsonEaa0103.getBigDecimal_Zero("ir_ir") - 
												  jsonEaa0103.getBigDecimal_Zero("inss_vlr") - 
												  jsonEaa0103.getBigDecimal_Zero("iss_iss") - 
												  jsonEaa0103.getBigDecimal_Zero("pis_cofins_csll"));

            eaa0103.eaa0103totDoc = jsonEaa0103.getBigDecimal_Zero("vlr_total_serv_liq");
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