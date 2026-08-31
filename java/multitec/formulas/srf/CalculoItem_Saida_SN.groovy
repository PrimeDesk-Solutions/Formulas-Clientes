package multitec.formulas.srf;

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aac10;
import sam.model.entities.aa.Aag01
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201;
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj14;
import sam.model.entities.aa.Aaj15;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abb10;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm1001
import sam.model.entities.ab.Abm12;
import sam.model.entities.ab.Abm13;
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase;


public class CalculoItem_Saida_SN extends FormulaBase {

	private Aac10 aac10;
	private Aag01 aag01;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
	private Aaj11 aaj11_cstIpi;
	private Aaj12 aaj12_cstPis;
	private Aaj13 aaj13_cstCof;
	private Aaj14 aaj14_cstCsosn;
	private Aaj15 aaj15_cfop;
	private Aam06 aam06;
	
	private Abb01 abb01;
	private Abb10 abb10;
	private Abd01 abd01;
	private Abe01 abe01;
	private Abg01 abg01;
	private Abm01 abm01;
	private Abm0101 abm0101;
	private Abm10 abm10;
	private Abm1001 abm1001;
	private Abm12 abm12;
	private Abm13 abm13;
	
	private Eaa01 eaa01;
	private Eaa0101 eaa0101princ;
	private Eaa0102 eaa0102;
	private Eaa0103 eaa0103;
	
	private TableMap jsonEaa0103;
	private TableMap jsonAbm1001;
	private TableMap jsonAbe01;
	private TableMap jsonAbm0101;
	private TableMap jsonAag02Ent;
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
		if (abd01 != null && abd01.abd01es == 0)  throw new ValidacaoException("Esta fórmula poderá ser utilizada somente em documentos de saída.");
		
		//Dados da Entidade
		abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);
		
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
		abm0101 = abm01 != null &&  abm01.abm01id != null && aac10.aac10id != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

		//Valores do Item
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		
		//Estados
		abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = "+ ufEnt.aag02id+" AND abm1001cv = "+abm10.abm10id)) : null;
		
		//Dados Fiscais do item
		abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null;
		if (abm12 == null) throw new ValidacaoException("Não foi encontrada a configuração fiscal do item: " + abm01.abm01codigo);
		if (abm12.abm12tipo == null) throw new ValidacaoException("Necessário informar o tipo fiscal do item: " + abm01.abm01codigo);
		
		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Unidade de Medida
		aam06 = abm13 != null &&  abm13.abm13umv != null ? getSession().get(Aam06.class, abm13.abm13umv.aam06id) : null;

		//Operação Comercial
		abb10 = abb01 != null &&  abb01.abb01operCod != null ? getSession().get(Abb10.class, abb01.abb01operCod.abb10id) : null;

		//NCM
		abg01 = eaa0103.eaa0103ncm != null ? getSession().get(Abg01.class, eaa0103.eaa0103ncm.abg01id) : null;

		//CFOP
		aaj15_cfop = eaa0103.eaa0103cfop != null ? getSession().get(Aaj15.class, eaa0103.eaa0103cfop.aaj15id) : null;
		
		//CSOSN (ICMS)
		aaj14_cstCsosn = eaa0103.eaa0103csosn != null ? getSession().get(Aaj14.class, eaa0103.eaa0103csosn.aaj14id) : null;

		//CST IPI
		aaj11_cstIpi = eaa0103.eaa0103cstIpi != null ? getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id) : null;

		//CST PIS
		aaj12_cstPis = eaa0103.eaa0103cstPis != null ? getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id) : null;

		//CST COFINS
		aaj13_cstCof = eaa0103.eaa0103cstCofins != null ? getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id) : null;

		//CAMPOS LIVRES
		jsonAac10 = aac10.aac10json != null ? aac10.aac10json : new TableMap();
		jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
		jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAbm1001 = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
		jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
		
		calcularItem();
		
		eaa0103.eaa0103json = jsonEaa0103;
		put("eaa0103", eaa0103);
	}

	private void calcularItem() {

		//Operação comercial
		def operacao = abb10.abb10tipoCod == null ? -1 : abb10.abb10tipoCod;
		
		//Determina se a operação é dentro ou fora do estado
		def dentroEstado = false;
		if (ufEmpr != null && ufEnt != null) {
			dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
		}

		//Ajuste de CFOP e CSOSN
		//Excetua-se industrialização que deverá buscar os mesmos dados do PCD
		if (eaa0103.eaa0103cfop != null) {
			def cfop = aaj15_cfop.aaj15codigo.substring(1);
			def csosn;
			
			if (cfop != "124" && cfop != "902") {
				if (operacao == 1 || operacao == 2) {
					if (eaa0102.eaa0102ti == 0) { //CNPJ
						cfop = abm12.abm12tipo == 0 ? "102" : "101";
					}

					if(dentroEstado) {
						if (abm12.abm12tipo == 0) cfop = "102";
						if (abm12.abm12tipo == 4) cfop = "101";
					}else {
						if (abm12.abm12tipo == 0) cfop = eaa0102.eaa0102contribIcms == 1 ? "102" : "108";
						if (abm12.abm12tipo == 4) cfop = eaa0102.eaa0102contribIcms == 1 ? "101" : "107";
					}
					
					//Tem IVA no Item ?
					if (jsonAbm1001.getBigDecimal_Zero("iva_venda") > 0) {
						cfop = "401";
						csosn = "201";
					}
				}

				//Remessa em consignação
				if (operacao == 5 && cfop == "917") {
					if (eaa0102.eaa0102ti == 1) { //CPF
						csosn = "102"
					}else {
						csosn = "101"

						//Tem IVA no Item ?
						if (jsonAbm1001.getBigDecimal_Zero("iva_venda") > 0) {
							csosn = "201"
						}
					}
				}

				//Entidade com Regime Especial
				if (jsonAbe01.getString('regime_especial') == "S") {
					csosn = "102"
					cfop = "101"
				}

				def primDigCfop = dentroEstado ? "5" : "6";
				
				cfop = primDigCfop + cfop;

				aaj15_cfop =  getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
				if(aaj15_cfop == null) throw new ValidacaoException("Não foi possível encontrar o CFOP " + cfop);

				eaa0103.eaa0103cfop = aaj15_cfop;
				
				if (csosn != null) {
					csosn = abm12.abm12cstA + csosn;
					
					aaj14_cstCsosn = getSession().get(Aaj14.class, Criterions.eq("aaj14codigo", csosn));
					eaa0103.eaa0103csosn = aaj14_cstCsosn;
				}
			}
		}

		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0 && eaa0103.eaa0103unit > 0) {
			
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

			//================================
			// ******     Quantidades    ******
			//================================
			//Converte Qt.Documento para Qtde SCE
			eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

			if (abm13 != null && abm13.abm13fcVU_Zero > 0) {
				eaa0103.eaa0103qtUso = eaa0103.eaa0103qtUso * abm13.abm13fcVU_Zero;
			}

			// Converte Qt.Documento para Volume
			if (abm13 != null && abm13.abm13fcVW > 0) {
				jsonEaa0103.put("vlr_vlme", (eaa0103.eaa0103qtComl * abm13.abm13fcVW_Zero).round(0));
			}

			//================================
			// ***** Peso Bruto e Líquido *****
			//================================
			if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("vlr_pl", (eaa0103.eaa0103qtComl * abm01.abm01pesoLiq_Zero).round(4));
			if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("vlr_pb", (eaa0103.eaa0103qtComl * abm01.abm01pesoBruto_Zero).round(4));

			//================================
			// ******     Desconto     ******
			//================================
			if (jsonEaa0103.getBigDecimal("tx_desc_incond") != null) {
				jsonEaa0103.put("vlr_desc", ((eaa0103.eaa0103total * jsonEaa0103.getBigDecimal_Zero("tx_desc_incond")) / 100).round(2));
			}

			//================================
			// ******        IPI         ******
			//================================
			if (eaa0103.eaa0103cstIpi != null) {
				//BC de IPI = Total do Item + Frete + Seguro + Despesas Acessorias
				jsonEaa0103.put("ipi_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras"));

				//Alíquota de IPI do cadastro de NCM
				if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
					jsonEaa0103.put("ipi_aliq", abg01.abg01txIpi);
				}

				def cstValido = false;
				//CST 50 - Saída Tributada
				if (aaj11_cstIpi.aaj11codigo == "50") {
					if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada a alíquota de IPI.");
					
					cstValido = true;
					//Valor do IPI = BC IPI X Alíquota de IPI
					jsonEaa0103.put("ipi_ipi", ((jsonEaa0103.getBigDecimal_Zero("ipi_bc") * jsonEaa0103.getBigDecimal_Zero("ipi_aliq")) / 100).round(2));
				}

				//CST 51 - Saída tributável com alíquota zero
				if (aaj11_cstIpi.aaj11codigo == "51") {
					cstValido = true;
					jsonEaa0103.put("ipi_outras", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc", 0.00);
					jsonEaa0103.put("ipi_aliq", 0.00);
					jsonEaa0103.put("ipi_isento", 0.00);
				}

				//CST 52 - Saída Isenta
				//CST 53 - Saída não tributada
				//CST 54 - Saída Imune
				//CST 55 - Saída com Suspensão
				//CST 99 - Outras Saídas
				if (aaj11_cstIpi.aaj11codigo == "52" || aaj11_cstIpi.aaj11codigo == "53" || aaj11_cstIpi.aaj11codigo == "54" || aaj11_cstIpi.aaj11codigo == "55" || aaj11_cstIpi.aaj11codigo == "99") {
					cstValido = true;
					jsonEaa0103.put("ipi_isento", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc", 0.00);
					jsonEaa0103.put("ipi_aliq", 0.00);
					jsonEaa0103.put("ipi_outras", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de IPI inválido.");
			}

			// ============================
			// **********  CSOSN **********
			// ============================
			if (eaa0103.eaa0103csosn != null) {
				def csosn = aaj14_cstCsosn.aaj14codigo.length() > 3 ? aaj14_cstCsosn.aaj14codigo.substring(1) : aaj14_cstCsosn.aaj14codigo;

				//101 - Tributada pelo Simples Nacional com permissão de credito
				//102 - Tributada pelo Simples Nacional sem permissão de crédito
				//103 - Isenção do ICMS no Simples Nacional para faixa de receita bruta
				//201 - Trib Pelo Simples Naiconal com permissão de crédito e com cobrança do ICMS por substituição tributária
				//202 - Tributada pelo Simples Nacional sem permissão de crédito e com cobrança do ICMS por substituição tributária
				//203 - Isenção do ICMS no Simples Nacional para faixa de receita bruta e com cobrança do ICMS por substituição tributária
				//300 - Imune
				//400 - Não tributada pelo Simples Nacional
				//500 - ICMS cobrado anteriormente por substituição tributária (substituido) ou por antecipação
				//900 - Outros

				//Zera valor que se acumula durante o processo
				jsonEaa0103.put("icm_isento", 0.00);

				//BC ICMS = Vlr Item + Frete + Seguro + Outras Desp. - Desconto incondicional
				jsonEaa0103.put("icm_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
															     jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				def csosnValido = false;
				//Cálculo do valor do ICMS para os CSOSN:
				if (csosn == "101" || csosn == "201" || csosn == "900"){
					csosnValido = true;

					// % ICMS do SIMPLES NACIONAL- Cad. Empresa
					if (jsonAac10.getBigDecimal_Zero("simpnacional_aliq") > 0){
						jsonEaa0103.put("icm_aliq", jsonAac10.getBigDecimal_Zero("simpnacional_aliq"));
					}

					//Alíquota de redução do ICMS Fixo para o Estado
					if (jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc") > 0) {
						jsonEaa0103.put("icm_reduc_bc", jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc"));
					}

					//Se Pessoa física ou não contribuinte:
					//1-Adiciona valor do IPI a Base de Cálculo do ICMS
					//2-Pessoas físicas ou não contribuintes - não têm redução de base de cálculo de ICMS
					if (eaa0102.eaa0102ti == 1 || eaa0102.eaa0102contribIcms == 0 ) {
						jsonEaa0103.put("icm_bc", (jsonEaa0103.getBigDecimal_Zero("icm_bc") + jsonEaa0103.getBigDecimal_Zero("ipi_ipi")).round(2));
						jsonEaa0103.put("icm_reduc_bc", 0.00);
					}

					//Calculo da redução (O valor reduzido entra em Isento)
					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") > 0) {
						jsonEaa0103.put("icm_isento", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2));
						jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - jsonEaa0103.getBigDecimal_Zero("icm_isento"));
					}

					//Valor do ICMS
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") > 0) {
						jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					}
				}

				//ICMS ST para os CSOSN
				if (csosn == "201" || csosn == "202" || csosn == "203" || csosn == "900") {
					if (jsonAbm1001.getBigDecimal_Zero("iva_venda") == 0) throw new ValidacaoException("CST indica operação com ST, mas não foi informado o percentual de IVA no item.");

					csosnValido = true;

					//BC para ICMS dedutível do ICMS ST: Valor Total Item + Frete + Seguro + Outras Desp. - Desconto incondicional
					jsonEaa0103.put("ded_st_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																	    jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																	    jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
																	    jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

					// Aliquota de redução do ICMS Fixo para o Estado
					if (jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//Se Pessoa física ou não contribuinte:
					//1-Adiciona valor do IPI a Base de Cálculo do ICMS
					//2-Pessoas físicas ou não contribuintes - não têm redução de base de cálculo de ICMS
					if (eaa0102.eaa0102ti == 1) {
						jsonEaa0103.put("ded_st_bc", jsonEaa0103.getBigDecimal_Zero("ded_st_bc") + jsonEaa0103.getBigDecimal_Zero("ipi_ipi"));
						jsonEaa0103.put("icm_reducst_bc", 0.00);
					}

					//Cálculo da redução
					if (jsonEaa0103.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("vlr_icm_reducst_bc", ((jsonEaa0103.getBigDecimal_Zero("ded_st_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reducst_bc")) / 100).round(2));
						jsonEaa0103.put("ded_st_bc", jsonEaa0103.getBigDecimal_Zero("ded_st_bc") - jsonEaa0103.getBigDecimal_Zero("vlr_icm_reducst_bc"));
					}

					//Alíquota ICMS
					if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint") > 0) {
						jsonEaa0103.put("ded_st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));
					}

					//Dedução ICMS ST
					if (jsonEaa0103.getBigDecimal_Zero("ded_st_aliq") > 0) {
						jsonEaa0103.put("ded_st_icm", ((jsonEaa0103.getBigDecimal_Zero("ded_st_bc") * jsonEaa0103.getBigDecimal_Zero("ded_st_aliq")) / 100).round(2));
					}

					//IVA ST
					jsonEaa0103.put("tx_iva_st", jsonAbm1001.getBigDecimal_Zero("iva_venda"));

					//BC ICMS ST = Vlr Total Item  + Frete  + Seguro + Outras Desp. - Desconto incondicional
					jsonEaa0103.put("st_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																    jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																    jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
																    jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
															  
					//Adiciona IVA à Base de Cálculo do ICMS ST
					jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) / 100) + 1).round(2));

					//Redução da BC ICMS ST
					if (jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//ICMS ST
					//Alíquota interna no Estado Destino
					if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada") > 0) {
						jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada"));
					}

					if (jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq") > 0) {
						jsonEaa0103.put("st_aliq",jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq"));
					}

					//ICMS ST = BC * Alíquota Interna Estado Destino - Vlr ICMS Normal
					if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
						jsonEaa0103.put("st_icm", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100) - jsonEaa0103.getBigDecimal_Zero("ded_st_icm")).round(2));
					}
				}

				if (csosn == "102" || csosn == "103" || csosn == "202" || csosn == "203" || csosn == "300" || csosn == "400") {
					csosnValido = true;

					jsonEaa0103.put("icm_isento", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_reduc_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
				}

				if (csosn == "500") csosnValido = true;

				if (!csosnValido) throw new ValidacaoException("CSOSN apurado para o item está inválido.");
			}

			//================================
			// ******       PIS          ******
			//================================
			if (eaa0103.eaa0103cstPis != null) {
				//Base de Cálculo de PIS
				jsonEaa0103.put("pis_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
															     jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				//Alíquota do PIS
				jsonEaa0103.put("pis_aliq", jsonAbm0101.getBigDecimal_Zero("pis_aliq"));

				def cstValido = false;
				//CST 01 - Operação Tributável com Alíquota Básica
				//CST 02 - Operação Tributável com Alíquota Diferenciada
				if (aaj12_cstPis.aaj12codigo == "01" || aaj12_cstPis.aaj12codigo == "02") {
					if (jsonEaa0103.getBigDecimal_Zero("pis_aliq") == 0) throw new ValidacaoException("CST de PIS indica operação tributada, porém não foi informada a alíquota de PIS.");

					cstValido = true;
					jsonEaa0103.put("pis_pis", ((jsonEaa0103.getBigDecimal_Zero("pis_bc") * jsonEaa0103.getBigDecimal_Zero("pis_aliq")) / 100).round(2));
				}

				//CST 03 - Operação Tributável com Alíquota por Unidade de Medida de Produto
				if (aaj12_cstPis.aaj12codigo == "03") {
					jsonEaa0103.put("pis_bc", 0.00);
					jsonEaa0103.put("pis_aliq", 0.00);
					jsonEaa0103.put("pis_pis", 0.00);
					throw new ValidacaoException("Fórmula não contempla CST de PIS 03.");
				}

				//CST 04 - Operação Tributável Monofásica – Revenda a Alíquota Zero
				//CST 05 - Operação Tributável por Substituição Tributária
				//CST 06 - Operação Tributável a Alíquota Zero
				//CST 07 - Operação Isenta da Contribuição
				//CST 08 - Operação sem Incidência da Contribuição
				//CST 09 - Operação com Suspensão da Contribuição
				//CST 99 - Outras Operações de Saída
				if (aaj12_cstPis.aaj12codigo == "04" ||  aaj12_cstPis.aaj12codigo == "05" || aaj12_cstPis.aaj12codigo == "06" || aaj12_cstPis.aaj12codigo == "07" ||
					aaj12_cstPis.aaj12codigo == "08" ||  aaj12_cstPis.aaj12codigo == "09" || aaj12_cstPis.aaj12codigo == "49") {
					cstValido = true;
					jsonEaa0103.put("pis_bc", 0.00);
					jsonEaa0103.put("pis_aliq", 0.00);
					jsonEaa0103.put("pis_pis", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de PIS apurado para o item está inválido.");
			}

			//================================
			// ******      COFINS        ******
			//================================
			if (eaa0103.eaa0103cstCofins != null) {
				//Base de Cálculo Cofins
				jsonEaa0103.put("cofins_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																    jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																    jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
																    jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				//Alíquota do Cofins
				jsonEaa0103.put("cofins_aliq", jsonAbm0101.getBigDecimal_Zero("cofins_aliq"));

				def cstValido = false;
				//CST 01 - Operação Tributável com Alíquota Básica
				//CST 02 - Operação Tributável com Alíquota Diferenciada
				if (aaj13_cstCof.aaj13codigo == "01" || aaj13_cstCof.aaj13codigo == "02") {
					if (jsonEaa0103.getBigDecimal_Zero("cofins_aliq") == 0) throw new ValidacaoException("CST de COFINS indica operação tributada, porém não foi informada a alíquota de COFINS.");

					cstValido = true;
					jsonEaa0103.put("cofins_cofins", ((jsonEaa0103.getBigDecimal_Zero("cofins_bc") * jsonEaa0103.getBigDecimal_Zero("cofins_aliq")) / 100).round(2));
				}

				//CST 03 - Operação Tributável com Alíquota por Unidade de Medida de Produto
				if (aaj13_cstCof.aaj13codigo == "03") {
					cstValido = true;
					jsonEaa0103.put("cofins_bc", 0.00);
					jsonEaa0103.put("cofins_aliq", 0.00);
					jsonEaa0103.put("cofins_cofins", 0.00);
					throw new ValidacaoException("Fórmula não contempla CST de COFINS 03.");
				}

				//CST 04 - Operação Tributável Monofásica - Revenda a Alíquota Zero
				//CST 05 - Operação Tributável por Substituição Tributária
				//CST 06 - Operação Tributável a Alíquota Zero
				//CST 07 - Operação Isenta da Contribuição
				//CST 08 - Operação sem Incidência da Contribuição
				//CST 09 - Operação com Suspensão da Contribuição
				//CST 99 - Outras Operações de Saída
				if (aaj13_cstCof.aaj13codigo == "04" || aaj13_cstCof.aaj13codigo == "05" || aaj13_cstCof.aaj13codigo == "06" ||
					aaj13_cstCof.aaj13codigo == "07" || aaj13_cstCof.aaj13codigo == "08" || aaj13_cstCof.aaj13codigo == "09" || aaj13_cstCof.aaj13codigo == "49") {
					cstValido = true;
					jsonEaa0103.put("cofins_bc", 0.00);
					jsonEaa0103.put("cofins_aliq", 0.00);
					jsonEaa0103.put("cofins_cofins", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de COFINS apurado para o item está inválido.");
			}

			//=======================================
			// ********       ZFM/ALC      **********
			//=======================================
			//Cálculo do desconto referente ao valor do icms quando venda para zona franca
			jsonEaa0103.put("icmszf_bc", 0.00);
			jsonEaa0103.put("tx_icms_zf", 0.00);
			jsonEaa0103.put("vlr_descicmszf", 0.00);

			if (operacao == 1 || operacao == 3) {
				if (eaa0102.eaa0102suframa != null) {
					jsonEaa0103.put("tx_icms_zf", 0.00);
					jsonEaa0103.put("icmszf_bc", 0.00);

					// ICMS ZFM/ALC
					def descIcmsZfm = ((jsonEaa0103.getBigDecimal_Zero("icmszf_bc") * jsonEaa0103.getBigDecimal_Zero("tx_icms_zf")) / 100).round(2);
					jsonEaa0103.put("vlr_descicmszf", descIcmsZfm);

					// Ajusta CFOP
					def cfop = abm12.abm12tipo == 0? "110" : "109"; //Merc.Revenda

					def origemCstA = abm12.abm12cstA != null ? abm12.abm12cstA : 0;
					cfop = origemCstA + cfop;

					aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
					if (aaj15_cfop == null) throw new ValidacaoException("Não foi possível localizar CFOP: " + cfop + ".");
						
					eaa0103.eaa0103cfop = aaj15_cfop;
				}
			}
			
			//=======================================
			// ******** TOTAL DO DOCUMENTO **********
			//=======================================
			// Total Doc. = Item Total + IPI + Frete + Seguro + Outras Despesas +  Desc.Z.F. - ICMS ST - Desconto incond
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi_ipi") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_outras") +
														   jsonEaa0103.getBigDecimal_Zero("st_icm") -
														   jsonEaa0103.getBigDecimal_Zero("vlr_descicmszf") -
														   jsonEaa0103.getBigDecimal_Zero("vlr_desc");
			
			eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
			
			//Valor do Financeiro
			eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

			//=======================================
			// ******** OUTRAS INFORMAÇÕES **********
			//=======================================

			// Para ITENS RETORNADOS, não considerar PESO nem VOLUME
			if (aaj15_cfop.aaj15codigo.substring(1) == "902" || aaj15_cfop.aaj15codigo.substring(1) == "124" || aaj15_cfop.aaj15codigo.substring(1) == "903") {
				jsonEaa0103.put("vlr_vlme", 0.00);
				jsonEaa0103.put("vlr_pl", 0.0000);
				jsonEaa0103.put("vlr_pb", 0.0000);
			}

			// ----------------------------------------------------------------
			// GERAR VALOR APROXIMADO DE IMPOSTOS PARA VENDA A CONSUMIDOR FINAL
			// ----------------------------------------------------------------
			// Cálculo valor quando PCD for Documento de Saída
			if (operacao == 1) {
				//Excetua-se industrialização
				if (aaj15_cfop.aaj15codigo.substring(1) != "902") {

					if (abg01 != null) { //Item tem NCM
						if (eaa0102.eaa0102consFinal == 1) {
							def aliqImpAprox = 0;
							def aliqImpAproxFed = abg01.abg01vatFedNac;
							def aliqImpAproxEst = abg01.abg01vatEst;

							if (aliqImpAproxFed != null && aliqImpAproxFed > 0) {
								jsonEaa0103.put("impfed_vlr", (((eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi_ipi")) * aliqImpAproxFed) / 100).round(2));
							}

							if (aliqImpAproxEst != null && aliqImpAproxEst > 0) {
								jsonEaa0103.put("impest_vlr", (((eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi_ipi")) * aliqImpAproxEst) / 100).round(2));
							}

							// Valor total dos impostos aproximados
							jsonEaa0103.put("impaprx_vlr", jsonEaa0103.getBigDecimal_Zero("impfed_vlr") + jsonEaa0103.getBigDecimal_Zero("impest_vlr"));
						}
					}
				}
			}


			// F.C.I.
			// ======
			//BASE DE CALCULO DA FCI
			if (operacao == 1 || operacao == 3) {
				jsonEaa0103.put("fci_bc", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi_ipi") - jsonEaa0103.getBigDecimal_Zero("icm_icm"));
			}

			//=======================================
			// ******** OUTRAS INFORMAÇÕES **********
			//=======================================

			// Ajuste de classificação da receita de PIS/Cofins para saídas
			if (aaj15_cfop.aaj15codigo.substring(1) == "901" || aaj15_cfop.aaj15codigo.substring(1) == "905" || aaj15_cfop.aaj15codigo.substring(1) == "910" || aaj15_cfop.aaj15codigo.substring(1) == "911" ||
				aaj15_cfop.aaj15codigo.substring(1) == "913" || aaj15_cfop.aaj15codigo.substring(1) == "916" || aaj15_cfop.aaj15codigo.substring(1) == "925" || aaj15_cfop.aaj15codigo.substring(1) == "949") {
				eaa0103.eaa0103clasReceita = 0;
			}


			//===============================================================================
			// CALCULO DO DIFERENCIAL DE ALIQUOTA SAIDA PARA NÃO CONTRIBUINTE FORA DO ESTADO
			//===============================================================================

			// icmsufdest_bc = BC ICMS UF Dest
			// fcpufdest_aliq = % FCP UF Dest
			// internaufdest_aliq = Aliquota Interna UF Destino
			// icms_ufdest = ICMS UF Dest
			// interesuf_aliq = Aliq Interestaduais UF
			// partilha_aliq = % Partilha ICMS
			// icms_fcp = Vlr Icms FCP
			// icmsufdest_bc = Valor ICMS UFDes
			// uforig_icms = Valor ICMS UFOri
			// interufdest_icms = ICMS Interestadual

			def ano = MDate.date().getYear();

			// Para venda a Pessoa Física ou não Contribuinte a Partilha do diferencial de aliquota
			if (eaa01.eaa01esData != null) {
				ano = eaa01.eaa01esData.getYear();
			}

			// Para venda a Pessoa Física ou não Contribuinte a Partilha do diferencial de aliquota
			if (eaa0102.eaa0102contribIcms == 0 && (aag01 != null && aag01.aag01codigo == "1058") && !dentroEstado) {
				if (operacao == 1) {
					if (aaj14_cstCsosn.aaj14codigo == "102" && ano >= 2016) {

						// Define % Partilha UF Destino
						if (ano == 2016) jsonEaa0103.put("partilha_aliq", 40);
						if (ano == 2017) jsonEaa0103.put("partilha_aliq", 60);
						if (ano == 2018) jsonEaa0103.put("partilha_aliq", 80);
						if (ano >= 2019) jsonEaa0103.put("partilha_aliq", 100);

						// Atribuiu % FCP
						if (jsonAbm1001.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
							jsonEaa0103.put("fcpufdest_aliq", jsonAbm1001.getBigDecimal_Zero("fcpufdest_aliq"));
						}

						//Aliquota de Icms Interestadual
						if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada") > 0) {
							jsonEaa0103.put("interesuf_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada"));
						}

						// Atribui aliquota Interna Destinatario
						if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint") > 0) {
							jsonEaa0103.put("internaufdest_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint") - jsonAag02Ent.getBigDecimal_Zero("fcpufdest_aliq"));
						}

						//Base de Calculo do ICMS na UF Destino
						jsonEaa0103.put("icmsufdest_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest")
																			  + jsonEaa0103.getBigDecimal_Zero("vlr_seguro")
																			  + jsonEaa0103.getBigDecimal_Zero("vlr_outras")
																			  - jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

						//Calcula ICMS Interestadual
						if (jsonEaa0103.getBigDecimal_Zero("interesuf_aliq") > 0) {
							jsonEaa0103.put("interufdest_icms", ((jsonEaa0103.getBigDecimal_Zero("icmsufdest_bc") * jsonEaa0103.getBigDecimal_Zero("interesuf_aliq")) / 100).round(2));
						}

						//Calcula o ICMS FCP
						if (jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
							jsonEaa0103.put("icms_fcp", ((jsonEaa0103.getBigDecimal_Zero("icmsufdest_bc") * jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq")) / 100).round(2));
						}

						//Calcula o ICMS Interno UF Destino
						if (jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq") > 0) {
							jsonEaa0103.put("icms_ufdest", ((jsonEaa0103.getBigDecimal_Zero("icmsufdest_bc") * jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq")) / 100).round(2));
						}

						//Calcula vlr de ICMS devido a UF Destino
						jsonEaa0103.put("icmsufdest_bc", (((jsonEaa0103.getBigDecimal_Zero("icms_ufdest") - jsonEaa0103.getBigDecimal_Zero("interufdest_icms")) * jsonEaa0103.getBigDecimal_Zero("partilha_aliq")) / 100).round(2));

						// Calcula vlr de ICMS devido a UF Origem
						jsonEaa0103.put("uforig_icms", jsonEaa0103.getBigDecimal_Zero("icms_ufdest") - jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - jsonEaa0103.getBigDecimal_Zero("icmsufdest_bc"));
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