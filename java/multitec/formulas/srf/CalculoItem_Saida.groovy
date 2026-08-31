package multitec.formulas.srf;


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
import sam.model.entities.aa.Aaj15;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abb10;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.ab.Abm10;
import sam.model.entities.ab.Abm1001;
import sam.model.entities.ab.Abm1003
import sam.model.entities.ab.Abm12;
import sam.model.entities.ab.Abm13;
import sam.model.entities.ab.Abm1301
import sam.model.entities.ea.Eaa01;
import sam.model.entities.ea.Eaa0101;
import sam.model.entities.ea.Eaa0102;
import sam.model.entities.ea.Eaa0103;
import sam.server.samdev.formula.FormulaBase;

public class CalculoItem_Saida extends FormulaBase {

	private Aac10 aac10;
	private Aag01 aag01;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
	private Aaj10 aaj10_cstIcms;
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
	private Abm1003 abm1003;
	private Abm12 abm12;
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
		abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

		//Valores do Item 
		abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;
		
		//Valores do Item - Estados
		abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = "+ ufEnt.aag02id + " AND abm1001cv = "+abm10.abm10id)) : null;
		
		//Valores do Item - Entidade
		abm1003 = abm10 != null && abm10.abm10id != null ? getSession().get(Abm1003.class, Criterions.where("abm1003ent = "+ abe01.abe01id + " AND abm1003cv = "+abm10.abm10id)) : null;
		
		//Dados Fiscais do item
		abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null;
		if (abm12 == null) throw new ValidacaoException("Não foi encontrada a configuração fiscal do item: " + abm01.abm01codigo);
		if (abm12.abm12tipo == null) throw new ValidacaoException("Necessário informar o tipo fiscal do item: " + abm01.abm01codigo);
		
		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Fatores de Conv. da Unid de Compra para Estoque
		abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));
		
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

		//CST ICMS
		aaj10_cstIcms = eaa0103.eaa0103cstIcms != null ? getSession().get(Aaj10.class, eaa0103.eaa0103cstIcms.aaj10id) : null;
		
		//CST IPI
		aaj11_cstIpi = eaa0103.eaa0103cstIpi != null ? getSession().get(Aaj11.class, eaa0103.eaa0103cstIpi.aaj11id) : null;

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
		jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
		jsonAbm1003_Ent_Item = abm1003 != null && abm1003.abm1003json != null ? abm1003.abm1003json : new TableMap();
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
		
		//Ajuste de CFOP, venda(01) ou revenda(02))
		if (eaa0103.eaa0103cfop != null) {
			def cfop = aaj15_cfop.aaj15codigo.substring(1);

			if (cfop != "122" && cfop != "124" && cfop != "151" && cfop != "901" && cfop != "902" && cfop != "903" && cfop != "910" && 
				cfop != "915" && cfop != "916" && cfop != "920" && cfop != "924" && cfop != "949") {
				
				if (operacao == 1 || operacao == 2) {
					if (eaa0102.eaa0102contribIcms == 0) {
						if (abm12.abm12tipo == 0) cfop = dentroEstado ? "102" : "108";
						if (abm12.abm12tipo == 4) cfop = dentroEstado ? "101" : "107";
					}
				}

				//Pessoa Juridica
				//Tipo de Inscrição (0-CNPJ)
				if (eaa0102.eaa0102ti == 0 && cfop != "109") {
					cfop = abm12.abm12tipo == 0 ? "102" : "101";
					
					//Tem IVA no Item?
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda") > 0) {
						cfop = "401";
					}
				}
			}
			
			def primDigCfop = dentroEstado ? "5" : "6";
			
			cfop = primDigCfop + cfop;

			aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
			if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrar o CFOP " + cfop);

			eaa0103.eaa0103cfop = aaj15_cfop;
		}
		

		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0 && eaa0103.eaa0103unit > 0) {
			
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

			//================================
			// ******     Quantidades    ******
			//================================
			eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;
			
			// Converte Qt.Venda para Qtde SCE em documentos de saída
			if (abm13 != null && abm13.abm13fcUV_Zero > 0) {
				eaa0103.eaa0103qtUso = eaa0103.eaa0103qtUso * abm13.abm13fcUV_Zero;
			}

			// Converte Qt.Documento para Volume
			if (abm13 != null && abm13.abm13fcVW_Zero > 0) {
				jsonEaa0103.put("vlr_vlme", eaa0103.eaa0103qtComl * abm13.abm13fcVW_Zero);
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
			//******        IPI         ******
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

				//CST 51 - Saída tributavel com alíquota zero
				//CST 53 - Saída não tributada
				//CST 54 - Saída Imune
				//CST 55 - Saída com Suspensão
				//CST 99 - Outras Saídas
				if (aaj11_cstIpi.aaj11codigo == "51" || aaj11_cstIpi.aaj11codigo == "53" ||	aaj11_cstIpi.aaj11codigo == "54" || aaj11_cstIpi.aaj11codigo == "55" || aaj11_cstIpi.aaj11codigo == "99") {
					cstValido = true;
					jsonEaa0103.put("ipi_outras", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc",0);
					jsonEaa0103.put("ipi_aliq",0);
					jsonEaa0103.put("ipi_isento",0);
				}

				//CST 52 - Saída Isenta
				if (aaj11_cstIpi.aaj11codigo == "52") {
					cstValido = 1;
					jsonEaa0103.put("ipi_isento",jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc",0);
					jsonEaa0103.put("ipi_aliq",0);
					jsonEaa0103.put("ipi_outras",0);
				}

				if (!cstValido) throw new ValidacaoException("CST de IPI inválido.");
			}

			//================================
			//******       ICMS         ******
			//================================
			def cstA = abm12.abm12cstA == null ? "0" : abm12.abm12cstA;
			if (eaa0103.eaa0103cstIcms != null) {
				def cst = aaj10_cstIcms.aaj10codigo.length() > 2 ? aaj10_cstIcms.aaj10codigo.substring(1) : aaj10_cstIcms.aaj10codigo;
				
				//Venda c/ Substituição tributária (O PCD deve ser configurado com CST 00 e CFOP 5101)
				if (jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda") > 0 && cst != "910" && eaa0102.eaa0102contribIcms != 0) {
					if (operacao == 1 || operacao == 2 || operacao == 7) {
						cst = cstA + "10";
					}
				}

				//Redução do ICMS c/ ST no Item - Estado (da Entidade Destino), exceto 03-Simples Nacional e com ST
				if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc") > 0 && jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda") > 0 && cst != "910" && eaa0102.eaa0102contribIcms != 0) {
					if (operacao == 1 || operacao == 2) {
						cst = cstA + "70";
					}
				}

				//Redução do ICMS do Item - Estado (da Entidade Destino), exceto 03-Simples Nacional e com ST
				if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc") > 0 && jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda") == 0 && cst != "910") {
					if (operacao == 1 || operacao == 2) {
						cst = cstA + "20";
					}
				}

				//Entidades com Regime Especial
				if ((operacao == 1 || operacao == 2) && jsonAbe01.getString('regime_especial') == "S") {
					cst = cstA + "00";
				}

				if(cst.length() <= 2) cst = cstA + "" + cst;
				
				aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", cst));
				if (aaj10_cstIcms == null) throw new ValidacaoException("Não foi possível localizar CST de ICMS " + cst + ".");

				eaa0103.eaa0103cstIcms = aaj10_cstIcms;

				//Alíquota padrão de ICMS para operações internas (ENTIDADE)
				jsonEaa0103.put("icm_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada"));

				//% ICMS FIXO 
				//Primeiro: considerar da entidade destino especificado no item
				//Segundo: da entidade destino especificado no item
				if (jsonAbm1003_Ent_Item.getBigDecimal_Zero("icm_aliq") > 0) {
					jsonEaa0103.put("icm_aliq", jsonAbm1003_Ent_Item.getBigDecimal_Zero("icm_aliq"));
					
				}else if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_aliq") > 0) {
					jsonEaa0103.put("icm_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_aliq"));
				}

				//% ICMS venda p/ Consumidor final dentro do estado
				if (eaa0102.eaa0102consFinal == 1 && dentroEstado) {
					jsonEaa0103.put("icm_aliq", jsonAag02Empr.getBigDecimal_Zero("aliq_icmsint"));
				}

				//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
				jsonEaa0103.put("icm_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
					                                             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
															     jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

				//Se Pessoa Física ou Não Contribuinte, adiciona valor do IPI a Base de Cálculo de ICMS
				if (eaa0102.eaa0102contribIcms == 0) {
					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") + jsonEaa0103.getBigDecimal_Zero("ipi_ipi"));
				}

				def cstValido = false;
				//CST x00 - Mercadoria Tributada Integralmente
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "00") {
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada alíquota de ICMS.");

					cstValido = true;
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
				}

				//CST x10 - Mercadoria tributada e com ICMS de ST
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "10") {
					jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda"));

					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada alíquota de ICMS.")
					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") == 0) throw new ValidacaoException("Informado CST ICMS ST, porém não foi informado o IVA no item referente a esta operação.");
						
					cstValido = true;
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);

					//Alíquota do ICMS ST = Alíquota para operações internas do cadastro de Estados da entidade destino
					jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));

					//% ICMS da UF da entidade destino especificada no Item para cálculo do ICMS ST
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("fixicmsst_aliq") > 0) {
						jsonEaa0103.put("st_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("fixicmsst_aliq"));
					}

					//BC ICMS ST
					jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc"));

					//Adiciona IVA a Base de Cálculo do ICMS ST
					jsonEaa0103.put("st_bc", ((jsonEaa0103.getBigDecimal_Zero("st_bc") * (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") /  100)) + 1).round(2));

					//Cálcula redução da BC ICMS ST
					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//Cálcula ICMS ST
					//ICMS ST = Base * Alíquota Interna Estado de Destino - Valor Icms Normal
					if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
						jsonEaa0103.put("st_icm", ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100).round(2));
					}
				}

				//CST x20 - Operação Tributada com REDUÇÃO da base de cálculo
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "20") {
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc"));

					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica entrada tributada, porém não foi informada alíquota de ICMS.")
					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") == 0) throw new ValidacaoException("CST de ICMS indica operação com redução, porém não foi informado o % de redução.");

					cstValido = true;
					def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);
					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", vlrReducao);
					jsonEaa0103.put("icm_isento", 0.00);
				}

				//CST x30 - Mercadoria isenta ou não tributada com ICMS por ST
				//CST x40 - Mercadoria isenta
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "30" || aaj10_cstIcms.aaj10codigo.substring(1) == "40") {
					cstValido = true;
					jsonEaa0103.put("icm_isento", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_reduc_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_outras", 0.00);
				}

				//CST x41 - Mercadoria nacional não tributada
				//CST x50 - Mercadoria com suspensão
				//CST x51 - Mercadoria com diferimento
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "41" || aaj10_cstIcms.aaj10codigo.substring(1) == "50" || aaj10_cstIcms.aaj10codigo.substring(1) == "51") {
					cstValido = true;
					jsonEaa0103.put("icm_outras", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_reduc_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);
				}

				//CST x60 - Mercadoria com ICMS cobrado anteriormente por ST
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "60") {
					cstValido = true;
					jsonEaa0103.put("icm_outras", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_reduc_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);

					if (aaj15_cfop != null) {
						def cfop = aaj15_cfop.aaj15codigo.substring(1);

						if (cfop == "102" || cfop == "202") {
							cfop = dentroEstado ? "5405" : "6403";

							aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
							if(aaj15_cfop == null) throw new ValidacaoException("Não foi possível encontrar o CFOP " + cfop);

							eaa0103.eaa0103cfop = aaj15_cfop;
						}
					}
				}

				//CST x70 - Mercadoria com redução de base de cálculo com ICMS por ST
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "70") {
					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada alíquota de ICMS.");
					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") == 0) throw new ValidacaoException("CST de ICMS indica operação com redução, porém não foi informado o % de redução.");
					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") == 0) throw new ValidacaoException("Informado CST ICMS ST, porém não foi informado o IVA no item referente a esta operação.");

					cstValido = true;
					
					jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("iva_venda"));
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc"));
					
					def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);
					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", vlrReducao);
					jsonEaa0103.put("icm_isento", 0.00);

					jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));

					if (jsonAbm1001_UF_Item.getBigDecimal_Zero("fixicmsst_aliq") > 0) {
						jsonEaa0103.put("st_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("fixicmsst_aliq"));
					}

					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") > 0) {
						jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") + vlrReducao);
						//Adiciona IVA a Base de Cálculo do ICMS ST
						jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) / 100) + 1).round(2));

						//Cálcula redução da BC ICMS ST
						if (jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc") > 0) {
							jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
							jsonEaa0103.put("icm_reducst_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reducst_bc"));
						}

						//Cálcula ICMS ST
						//ICMS ST = Base * Alíquota Interna Estado de Destino - Valor Icms Normal
						if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
							jsonEaa0103.put("st_icm", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100) - jsonEaa0103.getBigDecimal_Zero("icm_icm")).round(2));
						}
					} else {
						jsonEaa0103.put("st_bc", 0.00);
						jsonEaa0103.put("st_aliq", 0.00);
						jsonEaa0103.put("st_icm", 0.00);
					}
				}

				//CST x90 - Mercadoria outras
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "90") {
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001_UF_Item.getBigDecimal_Zero("icm_reduc_bc"));

					cstValido = true;

					if (jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc") > 0) {
						//Cálculo da Redução
						def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);
						jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
						jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
						jsonEaa0103.put("icm_outras", vlrReducao);
						jsonEaa0103.put("icm_isento", 0.00);
					} else {
						jsonEaa0103.put("icm_outras", jsonEaa0103.getBigDecimal_Zero("icm_bc"));
						jsonEaa0103.put("icm_bc",0);
						jsonEaa0103.put("icm_reduc_bc",0);
						jsonEaa0103.put("icm_aliq",0);
						jsonEaa0103.put("icm_icm",0);
						jsonEaa0103.put("icm_isento",0);
					}
				}
				
				if (!cstValido) throw new ValidacaoException("CST de ICMS apurado para o item está inválido.");
			}

			//================================
			//******       PIS          ******
			//================================
			if (eaa0103.eaa0103cstPis != null) {
				//Base de Cálculo de PIS
				jsonEaa0103.put("pis_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
					                                             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
															     jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
															     jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
														   
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
					cstValido = true;
					
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
				//CST 49 - Outras Operações de Saída
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
			//******      COFINS        ******
			//================================
			if (eaa0103.eaa0103cstCofins != null) {
				//Base de Cálculo Cofins
				jsonEaa0103.put("cofins_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
						                                            jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																    jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
						                                            jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

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
				//CST 49 - Outras Operações de Saída
				if (aaj13_cstCof.aaj13codigo == "04" || aaj13_cstCof.aaj13codigo == "05" || aaj13_cstCof.aaj13codigo == "06" || aaj13_cstCof.aaj13codigo == "07" || 
					aaj13_cstCof.aaj13codigo == "08" || aaj13_cstCof.aaj13codigo == "09" || aaj13_cstCof.aaj13codigo == "49") {

					cstValido = true;
					jsonEaa0103.put("cofins_bc", 0.00);
					jsonEaa0103.put("cofins_aliq", 0.00);
					jsonEaa0103.put("cofins_cofins", 0.00);
				}

				if (!cstValido) throw new ValidacaoException("CST de COFINS apurado para o item está inválido.");
			}

			//==========================================================================//
			//         Zona Franca / Área de Livre Comércio e Amazônia Ocidental        //
			//==========================================================================//
			jsonEaa0103.put("icmszf_bc", 0.00);
			jsonEaa0103.put("tx_icms_zf", 0.00);
			jsonEaa0103.put("vlr_descicmszf", 0.00);

			def alc = jsonAag0201Ent.getInteger("livre_comercio");
			if(alc == null) alc = 0;
			
			//ALC: 3 - Amazônia Ocidental
			if (alc == 3) {
				aaj11_cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "55"));
				if (aaj11_cstIpi == null)throw new ValidacaoException("Operação com Amazônia Ocidental, não foi possível localizar CST de IPI: 55."); 
				
				eaa0103.eaa0103cstIpi = aaj11_cstIpi;

				jsonEaa0103.put("ipi_bc", 0.00);
				jsonEaa0103.put("ipi_aliq", 0.00);
				jsonEaa0103.put("ipi_ipi", 0.00);
				jsonEaa0103.put("ipi_outras", 0.00);
				
				//IPI ISENTO = Valor Total do Item + Frete + Seguro + Outras Despesas
				jsonEaa0103.put("ipi_isento", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
					                                                 jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																     jsonEaa0103.getBigDecimal_Zero("vlr_outras"));
			}

			//ALC: 2 -Zona Franca de Manaus ou 1 - Área de Livre Comércio
			if (alc == 2 || alc == 1) {
				//Item de Origem Nacional
				if (cstA == 0 || cstA == 3 || cstA == 4 || cstA == 5) {
					if (jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada") > 0) {
						jsonEaa0103.put("tx_icms_zf", jsonAag02Ent.getBigDecimal_Zero("aliq_icmentrada"));
					}

					//BC ICMS Z. FRANCA = Valor Total Item + Frete + Seguro + Outras Desp - Desc Incondicional
					jsonEaa0103.put("icmszf_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
						                                                jsonEaa0103.getBigDecimal_Zero("vlr_seguro") + 
																	    jsonEaa0103.getBigDecimal_Zero("vlr_outras") -
							                                            jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
									
					//Desc.ICMS Z.F.
					jsonEaa0103.put("vlr_descicmszf", ((jsonEaa0103.getBigDecimal_Zero("icmszf_bc") * jsonEaa0103.getBigDecimal_Zero("tx_icms_zf")) / 100).round(2));

					//Desconto
					BigDecimal desconto = jsonEaa0103.getBigDecimal_Zero("vlr_desc");
					if(desconto > 0) {
						if(jsonEaa0103.getBigDecimal_Zero("vlr_descicmszf") == desconto) desconto = 0;
					}
					jsonEaa0103.put("vlr_desc", (desconto + jsonEaa0103.getBigDecimal_Zero("vlr_descicmszf")).round(2));

					// Zera valores originais após o cálculo da Zona Franca
					jsonEaa0103.put("icm_bc", 0.00);
					jsonEaa0103.put("icm_reduc_bc", 0.00);
					jsonEaa0103.put("icm_aliq", 0.00);
					jsonEaa0103.put("icm_icm", 0.00);
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", jsonEaa0103.getBigDecimal_Zero("icmszf_bc"));

					//CFOP
					// 00-Mercadoria para revenda
					if (abm12.abm12tipo == 0) {
						aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6110"));
						if(aaj15_cfop == null) throw new ValidacaoException("Não foi possível encontrar o CFOP: 6110");
					}
					// 04-Produto acabado
					if (abm12.abm12tipo == 4) {
						aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6109"));
						if(aaj15_cfop == null) throw new ValidacaoException("Não foi possível encontrar o CFOP: 6109");
					}

					eaa0103.eaa0103cfop = aaj15_cfop;

					//CST de ICMS
					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "040"));
					if (aaj10_cstIcms == null) throw new ValidacaoException("Não foi possível encontrar o CST de ICMS: 040");
					
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;

					//======================/
					//SUSPENSÃO DO IPI **/
					//======================/
					// BC de IPI = Total Item  + Frete + Seguro + Outras Desp
					jsonEaa0103.put("ipi_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
						                                             jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																     jsonEaa0103.getBigDecimal_Zero("vlr_outras"));

					aaj11_cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "55"));
					if (aaj11_cstIpi == null) throw new ValidacaoException("Não foi possível encontrar o CST de IPI: 55.");
					
					eaa0103.eaa0103cstIpi = aaj11_cstIpi;

					//IPI ISENTO
					jsonEaa0103.put("ipi_isento", jsonEaa0103.getBigDecimal_Zero("ipi_bc"));
					jsonEaa0103.put("ipi_bc", 0.00);
					jsonEaa0103.put("ipi_aliq", 0.00);
					jsonEaa0103.put("ipi_ipi", 0.00);
					jsonEaa0103.put("ipi_outras", 0.00);
				}

				//Regime de Tributação diferente de Lucro Real
				if (abe01.abe01regTrib != 2) {
					// =======================================
					// ** ALIQUOTA ZERO PARA PIS E COFINS **//
					// =======================================
					aaj12_cstPis = getSession().get(Aaj12.class, Criterions.eq("aaj12codigo", "06"));
					if (aaj12_cstPis == null) throw new ValidacaoException("Não foi possível encontrar CST de PIS: 06")
					
					eaa0103.eaa0103cstPis = aaj12_cstPis;

					jsonEaa0103.put("pis_bc", 0.00);
					jsonEaa0103.put("pis_aliq", 0.00);
					jsonEaa0103.put("pis_pis", 0.00);

					
					aaj13_cstCof = getSession().get(Aaj13.class, Criterions.eq("aaj13codigo", "06"));
					if (aaj13_cstCof == null) throw new ValidacaoException("Não foi possível encontrar CST de COFINS: 06");
					
					eaa0103.eaa0103cstCofins = aaj13_cstCof;

					jsonEaa0103.put("cofins_bc", 0.00);
					jsonEaa0103.put("cofins_aliq", 0.00);
					jsonEaa0103.put("cofins_cofins", 0.00);
				}
			}

			// ==================
			// TOTAL DO DOCUMENTO
			// ==================
			//Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi_ipi") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
														   jsonEaa0103.getBigDecimal_Zero("vlr_outras") +
														   jsonEaa0103.getBigDecimal_Zero("st_icm") -
														   jsonEaa0103.getBigDecimal_Zero("vlr_desc");
			
            eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);

            //Valor do Financeiro
		    eaa0103.eaa0103totFinanc = eaa0103.eaa0103retInd == 0 ? eaa0103.eaa0103totDoc : 0.00;

			//=======================================
			// ******** OUTRAS INFORMAÇÕES **********
			//=======================================
			if (aaj15_cfop != null) {
				// Ajuste de classificação da receita de PIS/Cofins para saídas
				if (aaj15_cfop.aaj15codigo.substring(1) == "901" || aaj15_cfop.aaj15codigo.substring(1) == "905" || aaj15_cfop.aaj15codigo.substring(1) == "910" || aaj15_cfop.aaj15codigo.substring(1) == "911" || 
					aaj15_cfop.aaj15codigo.substring(1) == "913" || aaj15_cfop.aaj15codigo.substring(1) == "916" || aaj15_cfop.aaj15codigo.substring(1) == "925" || aaj15_cfop.aaj15codigo.substring(1) == "949") {

					eaa0103.eaa0103clasReceita = 0;
				}

				// Para ITENS RETORNADOS, não considerar PESO nem VOLUME
				if (aaj15_cfop.aaj15codigo.substring(1) == "902") {
					jsonEaa0103.put("vlr_vlme", 0.00);
					jsonEaa0103.put("vlr_pl", 0.0000);
					jsonEaa0103.put("vlr_pb", 0.0000);
				}
			}

			// ----------------------------------------------------------------
			// GERAR VALOR APROXIMADO DE IMPOSTOS PARA VENDA A CONSUMIDOR FINAL
			// ----------------------------------------------------------------
			if (operacao == 1) {
				// Excetua-se industrialização
				if (aaj15_cfop.aaj15codigo.substring(1) != "902") {

					if (eaa0102.eaa0102consFinal || eaa0102.eaa0102ti == 1) {
						if (abg01!= null) { // Busca aliquota gravada no NCM
							if (eaa0103.eaa0103cstIcms != null) {
								// FEDERAL (IMPORTADOS)
								// --------------------
								def aliqImpAproxFed = 0;
								if (cstA == 0 || cstA == 3 || cstA == 4 || cstA == 5) { //Item NACIONAL
									aliqImpAproxFed = abg01.abg01vatFedNac;
								} else {												// Item IMPORTADO
									aliqImpAproxFed = abg01.abg01vatFedImp;
								}

								if (aliqImpAproxFed != null && aliqImpAproxFed > 0 ) {
									jsonEaa0103.put("impfed_vlr", ((eaa0103.eaa0103total * aliqImpAproxFed) / 100).round(2))
								}

								// ESTADUAL
								// --------
								def aliqImpAproxEst = abg01.abg01vatEst;
								if (aliqImpAproxEst != null && aliqImpAproxEst > 0 ) {
									jsonEaa0103.put("impest_vlr", ((eaa0103.eaa0103total * aliqImpAproxEst) / 100).round(2));
								}

								// FEDERAL + ESTADUAL
								jsonEaa0103.put("impaprx_vlr", (jsonEaa0103.getBigDecimal_Zero("impfed_vlr") + jsonEaa0103.getBigDecimal_Zero("impest_vlr")).round(2));
							}
						}
					}
				}
			}

			//=================================================================
			// Diferencial de Aliquota - Interestadual - a Partir de 01/01/2016
			//=================================================================
			def ano = eaa01.eaa01esData != null ? eaa01.eaa01esData.getYear() : MDate.date().getYear();

			// Para venda a Pessoa Física ou não Contribuinte a Partilha do diferencial de aliquota
			if (eaa0102.eaa0102contribIcms == 0 && !dentroEstado && (aag01 != null && aag01.aag01codigo == "1058")) {
				if (operacao == 1) {
					if (ano >= 2016){

						// Define % Partilha UF Destino
						if (ano == 2016) jsonEaa0103.put("partilha_aliq", 40);
						if (ano == 2017) jsonEaa0103.put("partilha_aliq", 60);
						if (ano == 2018) jsonEaa0103.put("partilha_aliq", 80);
						if (ano >= 2019) jsonEaa0103.put("partilha_aliq", 100);

						// Atribui aliquota Interna Destinatario
						jsonEaa0103.put("internaufdest_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));

						// Atribuiu % FCP
						if (abm1001 != null && jsonAbm1001_UF_Item.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
							jsonEaa0103.put("fcpufdest_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("fcpufdest_aliq"));
						}

						//Base de Calculo do ICMS na UF Destino
						jsonEaa0103.put("icmsufdest_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc"));

						//Aliquota de Icms Interestadual
						jsonEaa0103.put("interesuf_aliq", jsonEaa0103.getBigDecimal_Zero("icm_aliq"));

						//Calcula o ICMS FCP
						if (jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
							jsonEaa0103.put("icms_fcp", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq")) / 100).round(2));
						}

						//Calcula o ICMS Interno UF Destino
						if (jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq") > 0) {
							jsonEaa0103.put("interufdest_icms", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq")) / 100).round(2));
						}

						//Calcula vlr de ICMS devido a UF Destino
						jsonEaa0103.put("icms_ufdest", (jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - ((jsonEaa0103.getBigDecimal_Zero("icm_icm") * jsonEaa0103.getBigDecimal_Zero("partilha_aliq")) / 100)).round(2));

						//Calcula vlr de ICMS devido a UF Origem
						jsonEaa0103.put("uforig_icms", jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - jsonEaa0103.getBigDecimal_Zero("icm_icm") - jsonEaa0103.getBigDecimal_Zero("icms_ufdest"));
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