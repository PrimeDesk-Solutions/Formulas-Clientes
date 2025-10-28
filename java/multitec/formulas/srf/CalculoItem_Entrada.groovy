package multitec.formulas.srf;

import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aac13
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj15
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm1001
import sam.model.entities.ab.Abm1003
import sam.model.entities.ab.Abm12
import sam.model.entities.ab.Abm13
import sam.model.entities.ab.Abm1301
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

public class CalculoItem_Entrada extends FormulaBase{

	private Aac10 aac10;
	private Aac13 aac13;
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
	private Abm1003 abm1003;
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
	private TableMap jsonAbm1003_Ent_Item;
	private TableMap jsonEaa0103;
	

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
		
		//Central de Documento
		abb01 = eaa01.eaa01central;
		
		//PCD
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
		if (abd01 != null && abd01.abd01es == 1)  throw new ValidacaoException("Esta fórmula poderá ser utilizada somente em documentos de entrada.");
		
		//Dados da Entidade
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
		aac10 = getAcessoAoBanco().obterEmpresa(obterEmpresaAtiva().aac10id);
		municipioEmpr = aac10.aac10municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", aac10.aac10municipio.aag0201id)) : null;
		ufEmpr = municipioEmpr != null ? getSession().get(Aag02.class, municipioEmpr.aag0201uf.aag02id) : null;
		aac13 = aac10.aac13fiscal;
				
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
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		abm1001 = ufEnt != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = " + ufEnt.aag02id + " AND abm1001cv = " + abm10.abm10id)) : null;
		abm1003 = abm10 != null && abm10.abm10id != null ? getSession().get(Abm1003.class, Criterions.where("abm1003ent = "+ abe01.abe01id + " AND abm1003cv = "+abm10.abm10id)) : null;
		
		//CST ICMS
		aaj10_cstIcms =  eaa0103.eaa0103cstIcms != null ? getSession().get(Aaj10.class, eaa0103.eaa0103cstIcms.aaj10id) : null;
		
		//CST IPI
		aaj11_cstIpi = eaa0103.eaa0103cstIpi != null ? getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id) : null;
		
		//CST PIS
		aaj12_cstPis = eaa0103.eaa0103cstPis != null ? getSession().get(Aaj12.class, eaa0103.eaa0103cstPis.aaj12id) : null;
		
		//CST COFINS
		aaj13_cstCof = eaa0103.eaa0103cstCofins != null ? getSession().get(Aaj13.class, eaa0103.eaa0103cstCofins.aaj13id) : null;
		
		//CAMPOS LIVRES
		jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
		jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
		jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
		jsonAbm1003_Ent_Item = abm1003 != null && abm1003.abm1003json != null ? abm1003.abm1003json : new TableMap();
		
		calcularItem();

		eaa0103.eaa0103json = jsonEaa0103; 
		put("eaa0103", eaa0103);
	}

	private void calcularItem() {

		// Determina se a operação é dentro ou fora do estado
		def dentroEstado = false;
		if (ufEmpr != null && ufEnt != null) {
			dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
		}
		
		//Ajuste de CFOP
		if (eaa0103.eaa0103cfop != null) {
			def cfop = aaj15_cfop.aaj15codigo.substring(1);

			if (abm12.abm12tipo != null) {
				//00 - Mercadoria para Revenda
				if (abm12.abm12tipo == 0 && cfop != "124" && cfop != "902" && cfop != "901" && cfop != "903" && cfop != "909") {
					cfop = "102"

					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra") > 0) {
						cfop = "403"
					}
				}

				//01 - Matéria-Prima ou 02 - Embalagem
				if ((abm12.abm12tipo == 1 || abm12.abm12tipo == 2) && cfop != "124" && cfop != "901" && cfop != "902" && cfop != "903" && cfop != "116") {
					cfop = "101"

					if (jsonAbm1001_UF_Item != null && jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra") > 0) {
						cfop = "401"
					}
				}

				if (abm12.abm12tipo == 7) { //07 - Material de Uso e Consumo
					cfop = "556"

					if (eaa0103.eaa0103cstIcms != null && aaj10_cstIcms != null && aaj10_cstIcms.aaj10codigo.substring(1) == "60" ) {
						cfop = "407"
					}
				}
			}

			def primDigCfop = dentroEstado ? "1" : "2";
			
			cfop = primDigCfop + cfop;

			aaj15_cfop =  getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
			if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrar o CFOP " + cfop);
			
			eaa0103.eaa0103cfop = aaj15_cfop;
		}

		//=====================================
		//******     Valores do Item     ******
		//=====================================
		
		if (eaa0103.eaa0103qtComl > 0 && eaa0103.eaa0103unit > 0) {
			
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

			//================================
			//******     Quantidades    ******
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

			// ===========================
			// **********  IPI  **********
			// ===========================
			if (eaa0103.eaa0103cstIpi != null) {
				//BC IPI = Total Item + Outras Despesas (frete, seguro e outras)
				jsonEaa0103.put("ipi_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
						                                         jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras"));

				//Alíquota de IPI do cadastro de NCM
				if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
					jsonEaa0103.put("ipi_aliq", abg01.abg01txIpi);
				}

				//Ajusta CST para Simples Nacional
				if (abe01.abe01regTrib == 0) { 
					if (abm12.abm12tipo == 1 || abm12.abm12tipo == 2 || abm12.abm12tipo == 4) { //01 - Matéria-Prima | 02 - Embalagem | 04 - Produto Acabado
						def cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "49"));
						if(cstIpi == null) throw new ValidacaoException("Não foi possível localizar CST de IPI 49.");
						
						eaa0103.eaa0103cstIpi = cstIpi;
						aaj11_cstIpi = cstIpi;
					}
				}

				//Ajusta CST de acordo com a classificação do ITEM e Gera valor da obs fiscal
				if ((abm12.abm12tipo == 0 || abm12.abm12tipo == 7) || (aac13 != null && aac13.aac13classTrib != null && aac13.aac13classTrib.aaj01codigo == "2")) { //00 - Revenda, 07-Uso e Consumo e 2-Simples Nacional, excesso rec bruta

					//Só deve gerar "ipi_obs", se alíquota for maior que zero, tipo do item : 00-Revenda/07- Uso e Consumo OU 2-Simples Nacional
					//Mas se a entidade (fornecedor) não for simples nacional em sua tributação.
					if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") > 0) {
						def cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "49"));
						if(cstIpi == null) throw new ValidacaoException("Não foi possível localizar CST de IPI 49.");
						
						eaa0103.eaa0103cstIpi = cstIpi;
						aaj11_cstIpi = cstIpi;

						if(abe01.abe01regTrib != 0) { //Só gera a obs IPI se fornecedor não é simples nacional
							//OBS fiscal do IPI = BC IPI  x Aliquota IPI
							jsonEaa0103.put("ipi_obs", ((jsonEaa0103.getBigDecimal_Zero("ipi_bc") * jsonEaa0103.getBigDecimal_Zero("ipi_aliq")) / 100).round(2));
						}
					}
				}

				def cstValido = false;
				//CST 00 - Entrada com Recuperação de Crédito
				if (aaj11_cstIpi.aaj11codigo == "00") {
					if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") == 0) throw new ValidacaoException("CST indica entrada com recuperação de crédito, porém não foi informada a alíquota de IPI.");

                    cstValido = true;
					  
					//Valor do IPI = BC IPI * Alíquota de IPI
					jsonEaa0103.put("ipi_ipi", ((jsonEaa0103.getBigDecimal_Zero("ipi_bc") * jsonEaa0103.getBigDecimal_Zero("ipi_aliq")) / 100).round(2));
					jsonEaa0103.put("ipi_outras", 0.00);
					jsonEaa0103.put("ipi_isento", 0.00);
				}
				
				// ** CST 02 - Entrada isenta
				// ** CST 04 - Entrada imune
				// ** CST 05 - Entrada com suspensão
				if (aaj11_cstIpi.aaj11codigo == "02" || aaj11_cstIpi.aaj11codigo == "04" || aaj11_cstIpi.aaj11codigo == "05") {
					cstValido = true;
					jsonEaa0103.put("ipi_isento", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc", 0.00);
					jsonEaa0103.put("ipi_aliq", 0.00);
					jsonEaa0103.put("ipi_outras", 0.00);
				}

				// ** CST 03 - Entrada não-tributada
				// ** CST 49 - Outras entradas
				if (aaj11_cstIpi.aaj11codigo == "03" || aaj11_cstIpi.aaj11codigo == "49") {
					cstValido = true;
					jsonEaa0103.put("ipi_outras", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc", 0.00);
					jsonEaa0103.put("ipi_aliq", 0.00);
					jsonEaa0103.put("ipi_isento", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de IPI inválido");
			}

			// ===========================
			// **********  ICMS **********
			// ===========================
			if (eaa0103.eaa0103cstIcms != null) {

				//Definição da alíquota do ICMS 
				//para Entidade com regime DIFERENTE DE: 0-Simples Nacional
				if (abe01.abe01regTrib != 0) {

					// % ICMS da Entidade (Remetente)
					if (jsonAag02Ent.getBigDecimal("aliq_icmsaida") != null) {
						jsonEaa0103.put("icm_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsaida"));

					} 

					//% ICMS FIXO 
					//Primeiro: considerar da entidade destino especificado no item
					//Segundo: da entidade destino especificado no item
					if (jsonAbm1003_Ent_Item.getBigDecimal_Zero("icm_aliq") > 0) {
						jsonEaa0103.put("icm_aliq", jsonAbm1003_Ent_Item.getBigDecimal_Zero("icm_aliq"));
						
					}else if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_aliq") > 0) {
						jsonEaa0103.put("icm_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_aliq"));
					}
				}

				//para Simples Nacional
				if (abe01.abe01regTrib == 0) {
					//00 - Mercadoria para Revenda | 01 - Matéria-Prima | 02 - Embalagem | 04 - Produto Acabado
					if (abm12.abm12tipo == 0 || abm12.abm12tipo == 1 || abm12.abm12tipo == 2 || abm12.abm12tipo == 4) {
						// % ICMS da Entidade no cadastro de Itens
						if (jsonAbe01.getBigDecimal_Zero("simpnacional_aliq") > 0) {
							jsonEaa0103.put("icm_aliq", jsonAbe01.getBigDecimal_Zero("simpnacional_aliq"));
							
						} else {
							def codigo = aaj10_cstIcms.aaj10codigo.substring(0, 1) + "90";

							def cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", codigo));
							if (cstIcms == null) throw new ValidacaoException("Não foi possível localizar CST de ICMS " + codigo + ".");

							eaa0103.eaa0103cstIcms = cstIcms;
							aaj10_cstIcms = cstIcms;
						}
					}
				}

				//BASE DE CALCULO do ICMS
				//BC ICMS = Vlr Item + Frete + Seguro + Outras Desp. - Desconto incondicional
				jsonEaa0103.put("icm_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
						                                         jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
															     jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
					
				//Ajuste do CST para itens com ST
				def codigoCst;
				if (aaj10_cstIcms.aaj10codigo.length() > 2) { //CST do Item
					codigoCst = aaj10_cstIcms.aaj10codigo.substring(1);
				} else {
					codigoCst = aaj10_cstIcms.aaj10codigo;
				}

				if (codigoCst != "10" && codigoCst != "30" && codigoCst != "40" && codigoCst != "41" && codigoCst != "50" && codigoCst != "51" && codigoCst != "90") {
					// Item com IVA de compra
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra") > 0) {
						jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra"));
						codigoCst = "60";
					}
				}

				//REDUÇÃO da BC ICMS
				//Aliquota de redução do ICMS no Item - Estado (da Entidade Remetente)
				if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc") > 0 && abe01.abe01regTrib != 0 && eaa0102.eaa0102contribIcms) {
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc"));
					
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra") > 0) {
						codigoCst = "70";
					}else {
						codigoCst = "20";
					}
				}
				
				def origemCstA = abm12.abm12cstA != null ? abm12.abm12cstA : 0;
				codigoCst = origemCstA + codigoCst; 
				
				aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", codigoCst));
				if (aaj10_cstIcms == null) throw new ValidacaoException("Não foi possível localizar CST de ICMS " + codigoCst + ".");

				eaa0103.eaa0103cstIcms = aaj10_cstIcms;
				
				//CÁLCULO DO ICMS DE ACORDO COM O CST
				//CST x00 - Mercadoria tributada integralmente
				def cstValido = false;
				def cstParteB = aaj10_cstIcms.aaj10codigo.substring(1);
				
				if (cstParteB == "00") {
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica entrada tributada, porém não foi informada alíquota de ICMS.");

					cstValido = true;

					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
				}

				//CST x10 - Mercadoria tributada e com ICMS por ST
				if (cstParteB == "10" ) {
					jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_compra"));
					
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica entrada tributada, porém não foi informada alíquota de ICMS.")
					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") == 0) throw new ValidacaoException("Informado CST ICMS ST, porém não foi informado o IVA no item referente a esta operação.");

					cstValido = true;
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
					jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc"));

					//Adiciona IVA à Base de Cálculo do ICMS ST
					jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) / 100) + 1).round(2));

					//Calcula redução da BC ICMS ST
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//Calcula ICMS ST
					// Alíquota ICMS_ST = Aliquota para operações internas do cadastro de Estados da entidade destino
					jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));
					
					//ICMS ST = Base * Alíquota Interna Estado Destino - Vlr ICMS Normal
					if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
						jsonEaa0103.put("st_icm", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100) - jsonEaa0103.getBigDecimal_Zero("icm_icm")).round(2));
					}
				}

				//CST x20 - Operação tributada com REDUÇÃO da base de cálculo
				if (cstParteB == "20") {
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica entrada tributada, porém não foi informada alíquota de ICMS.")
					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") == 0) throw new ValidacaoException("CST de ICMS indica operação com redução, porém não foi informado o % de redução.");

					cstValido = true;
					def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);

					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_isento", vlrReducao);
					jsonEaa0103.put("icm_outras", 0.00);
				}

				//CST x30 - Mercadoria isenta ou não tributada com ICMS por ST
				//CST x40 - Mercadoria isenta
				if (cstParteB == "30" || cstParteB == "40") {
					cstValido = true;
					
					jsonEaa0103.put("icm_isento", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_outras", 0.00);
				}

				//CST x41 - Mercadoria nacional não tributada
				//CST x50 - Mercadoria com suspensão
				//CST x51 - Mercadoria com diferimento
				//CST x90 - Mercadoria outras
				if (cstParteB == "41" || cstParteB == "50" || cstParteB == "51" || cstParteB == "90") {
					cstValido = true;
					
					jsonEaa0103.put("icm_outras", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
				}

				//CST x60 - Mercadoria com ICMS cobrado anteriormente por ST
				if (cstParteB == "60" ) {
					cstValido = true;

					jsonEaa0103.put("icm_outras", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
					//Zera valores de ICMS ST
					jsonEaa0103.put("st_bc", 0.00);
					jsonEaa0103.put("st_aliq", 0.00);
					jsonEaa0103.put("st_icm", 0.00);
				}

				//CST x70 - Mercadoria com redução de base de cálculo e com ICMS por ST
				if (cstParteB == "70" ) {
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica entrada tributada, porém não foi informada alíquota de ICMS.");
					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") == 0) throw new ValidacaoException("CST de ICMS indica operação com redução, porém não foi informado o % de redução.");
					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") == 0) throw new ValidacaoException("Informado CST ICMS ST, porém não foi informado o IVA no item referente a esta operação.");

					cstValido = true;

					def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);
					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", vlrReducao);
					jsonEaa0103.put("icm_isento", 0.00);
					jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc"));

					//Adiciona IVA à Base de Cálculo do ICMS ST
					jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) / 100) + 1).round(2));

					//Calcula redução da BC ICMS ST
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//Calcula ICMS ST
					//ICMS ST = Base * Alíquota Interna Estado Destino - Vlr ICMS Normal
					if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
						jsonEaa0103.put("st_icm", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100) - jsonEaa0103.getBigDecimal_Zero("icm_icm")).round(2));
					}
				}

				if (!cstValido) throw new ValidacaoException("CST de ICMS apurado para o item está inválido.");
			}

			// ==========================
			// **********  PIS **********
			//===========================
			if (eaa0103.eaa0103cstPis != null) {
				def cstValido = false;

				//BASE DE CÁLCULO DE PIS
				jsonEaa0103.put("pis_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
					                                             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
						                                         jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				//Alíquota do PIS
				jsonEaa0103.put("pis_aliq", jsonAbm0101.getBigDecimal_Zero("pis_aliq"));

				//CST 50 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Tributada no Mercado Interno
				//CST 51 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Não-Tributada no Mercado Interno
				//CST 52 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita de Exportação
				//CST 53 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno
				//CST 54 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas no Mercado Interno e de Exportação
				//CST 55 - Operação com Direito a Crédito - Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação
				//CST 56 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno e de Exportação
				//CST 60 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Tributada no Mercado Interno
				//CST 61 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Não-Tributada no Mercado Interno
				//CST 62 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita de Exportação
				//CST 64 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas no Mercado Interno e de Exportação
				//CST 65 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Não-Tributadas no Mercado Interno e de Exportação
				//CST 66 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno e de Exportação
				//CST 67 - Crédito Presumido - Outras Operações
				if (aaj12_cstPis.aaj12codigo == "50" || aaj12_cstPis.aaj12codigo == "51" || aaj12_cstPis.aaj12codigo == "52" || aaj12_cstPis.aaj12codigo == "53" || aaj12_cstPis.aaj12codigo == "54" || 
					aaj12_cstPis.aaj12codigo == "55" || aaj12_cstPis.aaj12codigo == "56" || aaj12_cstPis.aaj12codigo == "60" || aaj12_cstPis.aaj12codigo == "61" || aaj12_cstPis.aaj12codigo == "62" || 
					aaj12_cstPis.aaj12codigo == "64" || aaj12_cstPis.aaj12codigo == "65" || aaj12_cstPis.aaj12codigo == "66" || aaj12_cstPis.aaj12codigo == "67") {

					if (jsonEaa0103.getBigDecimal_Zero("pis_aliq") == 0) throw new ValidacaoException("CST de PIS indica operação tributada, porém não foi informada a alíquota de PIS.");

					cstValido = true;
					jsonEaa0103.put("pis_pis", ((jsonEaa0103.getBigDecimal_Zero("pis_bc") * jsonEaa0103.getBigDecimal_Zero("pis_aliq")) / 100).round(2));
				}

				//CST 70 - Operação de Aquisição sem Direito a Crédito
				//CST 71 - Operação de Aquisição com Isenção
				//CST 72 - Operação de Aquisição com Suspensão
				//CST 73 - Operação de Aquisição a Alíquota Zero
				//CST 74 - Operação de Aquisição sem Incidência da Contribuição
				//CST 75 - Operação de Aquisição por Substituição Tributária
				//CST 98 - Outras Operações de Entrada
				if (aaj12_cstPis.aaj12codigo == "70" || aaj12_cstPis.aaj12codigo == "71" || aaj12_cstPis.aaj12codigo == "72" || aaj12_cstPis.aaj12codigo == "73" || 
					aaj12_cstPis.aaj12codigo == "74" || aaj12_cstPis.aaj12codigo == "75" || aaj12_cstPis.aaj12codigo == "98") {

					cstValido = true;
					jsonEaa0103.put("pis_bc", 0.00);
					jsonEaa0103.put("pis_aliq", 0.00);
					jsonEaa0103.put("pis_pis", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de PIS apurado para o item está inválido.");
			}

			// =============================
			// **********  COFINS **********
			//==============================
			if (eaa0103.eaa0103cstCofins != null) {
				def cstValido = false;

				//BASE DE CÁLCULO DE PIS
				jsonEaa0103.put("cofins_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
					                                                jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																	jsonEaa0103.getBigDecimal_Zero("vlr_outras") - 
				                                                    jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				//Alíquota do COFINS
				jsonEaa0103.put("cofins_aliq", jsonAbm0101.getBigDecimal_Zero("cofins_aliq"));

				//CST 50 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Tributada no Mercado Interno
				//CST 51 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita Não-Tributada no Mercado Interno
				//CST 52 - Operação com Direito a Crédito - Vinculada Exclusivamente a Receita de Exportação
				//CST 53 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno
				//CST 54 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas no Mercado Interno e de Exportação
				//CST 55 - Operação com Direito a Crédito - Vinculada a Receitas Não Tributadas no Mercado Interno e de Exportação
				//CST 56 - Operação com Direito a Crédito - Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno e de
				//CST 60 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Tributada no Mercado Interno
				//CST 61 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita Não-Tributada no Mercado Interno
				//CST 62 - Crédito Presumido - Operação de Aquisição Vinculada Exclusivamente a Receita de Exportação
				//CST 64 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas no Mercado Interno e de Exportação
				//CST 65 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Não-Tributadas no Mercado Interno e de Exportação
				//CST 66 - Crédito Presumido - Operação de Aquisição Vinculada a Receitas Tributadas e Não-Tributadas no Mercado Interno e de Exportação
				//CST 67 - Crédito Presumido - Outras Operações
				if (aaj13_cstCof.aaj13codigo == "50" || aaj13_cstCof.aaj13codigo == "51" || aaj13_cstCof.aaj13codigo == "52" || aaj13_cstCof.aaj13codigo == "53" || aaj13_cstCof.aaj13codigo == "54" || 
					aaj13_cstCof.aaj13codigo == "55" || aaj13_cstCof.aaj13codigo == "56" || aaj13_cstCof.aaj13codigo == "60" || aaj13_cstCof.aaj13codigo == "61" || aaj13_cstCof.aaj13codigo == "62" || 
					aaj13_cstCof.aaj13codigo == "64" || aaj13_cstCof.aaj13codigo == "65" || aaj13_cstCof.aaj13codigo == "66" || aaj13_cstCof.aaj13codigo == "67") {

					if (jsonEaa0103.getBigDecimal_Zero("cofins_aliq") == 0) throw new ValidacaoException("CST de COFINS indica operação tributada, porém não foi informada a alíquota de COFINS.");

					cstValido = true;
					
					jsonEaa0103.put("cofins_cofins", ((jsonEaa0103.getBigDecimal_Zero("cofins_bc") * jsonEaa0103.getBigDecimal_Zero("cofins_aliq")) / 100).round(2));
				}

				//CST 70 - Operação de Aquisição sem Direito a Crédito
				//CST 71 - Operação de Aquisição com Isenção
				//CST 72 - Operação de Aquisição com Suspensão
				//CST 73 - Operação de Aquisição a Alíquota Zero
				//CST 74 - Operação de Aquisição sem Incidência da Contribuição
				//CST 75 - Operação de Aquisição por Substituição Tributária
				//CST 98 - Outras Operações de Entrada
				if (aaj13_cstCof.aaj13codigo == "70" || aaj13_cstCof.aaj13codigo == "71" || aaj13_cstCof.aaj13codigo == "72" || 
					aaj13_cstCof.aaj13codigo == "73" || aaj13_cstCof.aaj13codigo == "74" || aaj13_cstCof.aaj13codigo == "75" || aaj13_cstCof.aaj13codigo == "98") {

					cstValido = true;
					jsonEaa0103.put("cofins_bc", 0.00);
					jsonEaa0103.put("cofins_aliq", 0.00);
					jsonEaa0103.put("cofins_cofins", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de COFINS apurado para o item está inválido.");
			}

			//=======================================
			// ******** Custo de Aquisição **********
			//=======================================
			def custoAquisicao = eaa0103.eaa0103total;
			jsonEaa0103.put("custo_aquisicao", (custoAquisicao + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
																 jsonEaa0103.getBigDecimal_Zero("vlr_desc") -
																 jsonEaa0103.getBigDecimal_Zero("icm_icm") -
																 jsonEaa0103.getBigDecimal_Zero("ipi_ipi") -
																 jsonEaa0103.getBigDecimal_Zero("pis_pis") -
																 jsonEaa0103.getBigDecimal_Zero("cofins_cofins") + 
																 jsonEaa0103.getBigDecimal_Zero("ipi_obs")));
					   
			//=======================================
			// ******** TOTAL DO DOCUMENTO **********
			//=======================================

			//Total Doc = Total do Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST + IPI em obs fisc - Desc.Z.F. - Desconto incond
			def ipi = jsonEaa0103.getBigDecimal_Zero("ipi_ipi");
			if (aaj15_cfop != null && aaj15_cfop.aaj15codigo == "1116") ipi = 0; //Não Soma o IPI no Total de Documento quando for CFOP = 1116
						
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total + ipi + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
													             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
													             jsonEaa0103.getBigDecimal_Zero("vlr_outras") +
														         jsonEaa0103.getBigDecimal_Zero("st_icm") +
														         jsonEaa0103.getBigDecimal_Zero("ipi_obs") - 
														         jsonEaa0103.getBigDecimal_Zero("vlr_desc") -
														         jsonEaa0103.getBigDecimal_Zero("vlr_descicmszf");
														  
            eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
														   
			//Valor do Financeiro
			eaa0103.eaa0103totFinanc = eaa0103.eaa0103retInd == 0 ? eaa0103.eaa0103totDoc : 0.00;
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==