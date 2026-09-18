package multitec.formulas.scv;


import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag02;
import sam.model.entities.aa.Aag0201;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe0101;
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ab.Abm10;
import sam.model.entities.ab.Abm1001;
import sam.model.entities.ab.Abm13;
import sam.model.entities.cb.Cbb11;
import sam.model.entities.cb.Cbb1101;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro

public class Cotacao extends FormulaBase {

	private Aac10 aac10;
	private Aag02 ufRep;
	private Aag0201 municipioRep;
	
	private Abe01 abe01;
	private Abe0101 abe0101princ;
	private Abg01 abg01;
	private Abm01 abm01;
	private Abm0101 abm0101;
	private Abm10 abm10;
	private Abm1001 abm1001;
	private Abm13 abm13;
	
	private Cbb11 cbb11;
	private Cbb1101 cbb1101;
	
	private TableMap jsonCbb1101;
	private TableMap jsonAbm1001;
	private TableMap jsonAbm0101;
	private TableMap jsonAag02Rep;

	@Override
	public void executar() {

		//Item
		cbb1101 = get("cbb1101");
		if(cbb1101 == null) return;
		
		cbb11 = cbb1101.cbb1101cot;
		
		//Dados da Entidade
		if(cbb11.cbb11ent == null) return;
		abe01 = getSession().get(Abe01.class, cbb11.cbb11ent.abe01id);
		
		//Endereço principal da entidade no documento
		abe0101princ = getAcessoAoBanco().buscarRegistroUnico("SELECT * FROM Abe0101 WHERE abe0101principal = 1 AND abe0101ent = :abe01id ", Parametro.criar("abe01id", abe01.abe01id));
		if (abe0101princ == null) throw new ValidacaoException("Não foi encontrado o endereço principal do representante.");
		
		municipioRep = abe0101princ.abe0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", abe0101princ.abe0101municipio.aag0201id)) : null;
		ufRep = municipioRep != null ? getSession().get(Aag02.class, municipioRep.aag0201uf.aag02id) : null;
		
		//Empresa
		aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);

		//Item
		abm01 = cbb1101.cbb1101item != null ? getSession().get(Abm01.class, cbb1101.cbb1101item.abm01id) : null;

		//Configurações do item, por empresa
		abm0101 = abm01 != null &&  abm01.abm01id != null && aac10.aac10id != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

		//Valores do Item
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		
		//Estados
		abm1001 = ufRep != null && ufRep.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = "+ ufRep.aag02id + " AND abm1001cv = "+abm10.abm10id)) : null;
		
		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;
		
		//NCM
		abg01 = abm0101.abm0101ncm != null ? getSession().get(Abg01.class, abm0101.abm0101ncm.abg01id) : null;

		//CAMPOS LIVRES
		jsonAag02Rep = ufRep != null && ufRep.aag02json != null ? ufRep.aag02json : new TableMap();
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAbm1001 = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
		jsonCbb1101 = cbb1101.cbb1101json != null ? cbb1101.cbb1101json : new TableMap();
		
		calcularItem();
		
		cbb1101.cbb1101json = jsonCbb1101;
		put("cbb1101", cbb1101);
	}

	private void calcularItem() {
		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (cbb1101.cbb1101qtC > 0 && cbb1101.cbb1101unit > 0) {

			cbb1101.cbb1101total = cbb1101.cbb1101qtC * cbb1101.cbb1101unit;
			
			//================================
			// ***** Peso Bruto e Líquido *****
			//================================
			jsonCbb1101.put("vlr_pl", cbb1101.cbb1101qtC * abm01.abm01pesoLiq_Zero);
			jsonCbb1101.put("vlr_pb", cbb1101.cbb1101qtC * abm01.abm01pesoBruto_Zero);

			//================================
			// ******     Desconto     ******
			//================================
			if (jsonCbb1101.getBigDecimal("tx_desc_incond") != null) {
				jsonCbb1101.put("vlr_desc", cbb1101.cbb1101total * (jsonCbb1101.getBigDecimal_Zero("tx_desc_incond") / 100));
			}
			
			//================================
			//******        IPI         ******
			//================================
			//BC de IPI = Total do Item + Frete + Seguro + Despesas Acessorias
			jsonCbb1101.put("ipi_bc", cbb1101.cbb1101total + jsonCbb1101.getBigDecimal_Zero("vlr_frete_dest") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_seguro") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_outras"));

            jsonCbb1101.put("ipi_aliq", 0);
													   
			if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
				jsonCbb1101.put("ipi_aliq", abg01.abg01txIpi);
			}
			
			jsonCbb1101.put("ipi_ipi", jsonCbb1101.getBigDecimal_Zero("ipi_bc") * (jsonCbb1101.getBigDecimal_Zero("ipi_aliq") / 100));

			//================================
			//******       ICMS         ******
			//================================
			//Alíquota padrão de ICMS para operações internas (ENTIDADE)
			jsonCbb1101.put("icm_aliq", jsonAag02Rep.getBigDecimal_Zero("aliq_icmsaida"));

			//% ICMS FIXO da UF da entidade destino especificado no item
			if (jsonAbm1001.getBigDecimal_Zero("icm_aliq") > 0) {
				jsonCbb1101.put("icm_aliq", jsonAbm1001.getBigDecimal("icm_aliq"));
			}

			//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
			jsonCbb1101.put("icm_bc", cbb1101.cbb1101total + jsonCbb1101.getBigDecimal_Zero("vlr_frete_dest") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_seguro") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_outras") -
														     jsonCbb1101.getBigDecimal_Zero("vlr_desc"));

			jsonCbb1101.put("icm_icm", jsonCbb1101.getBigDecimal_Zero("icm_bc") * (jsonCbb1101.getBigDecimal_Zero("icm_aliq") / 100));
			jsonCbb1101.put("icm_outras", 0);
			jsonCbb1101.put("icm_isento", 0);

			//REDUÇÃO da base de cálculo
			if(jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc") > 0) {
				jsonCbb1101.put("icm_reduc_bc", jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc"));
				
				def vlrReducao = jsonCbb1101.getBigDecimal_Zero("icm_bc") * jsonCbb1101.getBigDecimal_Zero("icm_reduc_bc") / 100;
				jsonCbb1101.put("icm_bc", jsonCbb1101.getBigDecimal_Zero("icm_bc") - vlrReducao);
				jsonCbb1101.put("icm_icm", jsonCbb1101.getBigDecimal_Zero("icm_bc") * jsonCbb1101.getBigDecimal_Zero("icm_aliq") / 100);
				jsonCbb1101.put("icm_outras", vlrReducao);
				jsonCbb1101.put("icm_isento", 0);
			}

			//================================
			//******       PIS          ******
			//================================
			jsonCbb1101.put("pis_bc", cbb1101.cbb1101total + jsonCbb1101.getBigDecimal_Zero("vlr_frete_dest") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_seguro") +
														     jsonCbb1101.getBigDecimal_Zero("vlr_outras") -
														     jsonCbb1101.getBigDecimal_Zero("vlr_desc"));
														   
			jsonCbb1101.put("pis_aliq", jsonAbm0101.getBigDecimal_Zero("pis_aliq"));
			jsonCbb1101.put("pis_pis", jsonCbb1101.getBigDecimal_Zero("pis_bc") * jsonCbb1101.getBigDecimal_Zero("pis_aliq") / 100);

			//================================
			//******      COFINS        ******
			//================================
			jsonCbb1101.put("cofins_bc", cbb1101.cbb1101total + jsonCbb1101.getBigDecimal_Zero("vlr_frete_dest") +
															    jsonCbb1101.getBigDecimal_Zero("vlr_seguro") +
															    jsonCbb1101.getBigDecimal_Zero("vlr_outras") -
															    jsonCbb1101.getBigDecimal_Zero("vlr_desc"));

			jsonCbb1101.put("cofins_aliq", jsonAbm0101.getBigDecimal_Zero("cofins_aliq"));
			jsonCbb1101.put("cofins_cofins", jsonCbb1101.getBigDecimal_Zero("cofins_bc") * jsonCbb1101.getBigDecimal_Zero("cofins_aliq") / 100);

			// ==================
			// TOTAL
			// ==================
			//Total = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
			cbb1101.cbb1101total = cbb1101.cbb1101total + jsonCbb1101.getBigDecimal_Zero("ipi_ipi") +
														  jsonCbb1101.getBigDecimal_Zero("vlr_frete_dest") +
														  jsonCbb1101.getBigDecimal_Zero("vlr_seguro") +
														  jsonCbb1101.getBigDecimal_Zero("vlr_outras") +
														  jsonCbb1101.getBigDecimal_Zero("st_icm") -
														  jsonCbb1101.getBigDecimal_Zero("vlr_desc");
			
			cbb1101.cbb1101total = round(cbb1101.cbb1101total, 2);
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_COTACOES;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzcifQ==