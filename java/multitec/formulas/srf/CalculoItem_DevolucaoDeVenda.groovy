package multitec.formulas.srf

import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag01
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.aa.Aaj10
import sam.model.entities.aa.Aaj11
import sam.model.entities.aa.Aaj12
import sam.model.entities.aa.Aaj13
import sam.model.entities.aa.Aaj14
import sam.model.entities.aa.Aaj15
import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abb01
import sam.model.entities.ab.Abb10
import sam.model.entities.ab.Abd01
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abg01
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm1001
import sam.model.entities.ab.Abm12
import sam.model.entities.ab.Abm13
import sam.model.entities.ab.Abm1301
import sam.model.entities.ea.Eaa01
import sam.model.entities.ea.Eaa0101
import sam.model.entities.ea.Eaa0102
import sam.model.entities.ea.Eaa0103
import sam.server.samdev.formula.FormulaBase

class CalculoItem_DevolucaoDeVenda extends FormulaBase{
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
	private Abm12 abm12;
	private Abm13 abm13;
	private Abm1301 abm1301;
	
	private Eaa01 eaa01;
	private Eaa0101 eaa0101princ;
	private Eaa0102 eaa0102;
	private Eaa0103 eaa0103;
	
	private TableMap jsonEaa0103;
	private TableMap jsonAbm1001;
	private TableMap jsonAbe01;
	private TableMap jsonAbm0101;
	private TableMap jsonAag02Ent;
	private TableMap jsonAag02Empr;

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
		abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = "+ ufEnt.aag02id + " AND abm1001cv = "+ abm10.abm10id)) : null;
		
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
		jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
		jsonAag02Empr = ufEmpr != null && ufEmpr.aag02json != null ? ufEmpr.aag02json : new TableMap();
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
		
		//Ajuste de CFOP, venda(01) ou revenda(02))
		if (eaa0103.eaa0103cfop != null) {
			def cfop = aaj15_cfop.aaj15codigo.substring(1);

			//Tem IVA no Item?
			if (jsonAbm1001.getBigDecimal_Zero("iva_venda") > 0) {
				cfop = abm12.abm12tipo == 0 ? "411" : "410";
			}
			
			def primDigCfop = dentroEstado ? "1" : "2";
			
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
			
			// Converte Qt.Venda para Qtde SCE
			if (abm13 != null && abm13.abm13fcVU_Zero > 0) {
				eaa0103.eaa0103qtUso = eaa0103.eaa0103qtUso * abm13.abm13fcVU;
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
				//BC IPI = Total Item + Outras Despesas (frete, seguro e outras)
				jsonEaa0103.put("ipi_bc", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("vlr_frete_dest") + 
						                                         jsonEaa0103.getBigDecimal_Zero("vlr_seguro") +
																 jsonEaa0103.getBigDecimal_Zero("vlr_outras"));

				//Alíquota de IPI do cadastro de NCM
				if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
					jsonEaa0103.put("ipi_aliq", abg01.abg01txIpi);
				}

				//Ajusta CST para Simples Nacional
				if (abe01.abe01regTrib == null) {
					if (abm12 != null && (abm12.abm12tipo == 1 || abm12.abm12tipo == 2 || abm12.abm12tipo == 4)) { //01 - Matéria-Prima | 02 - Embalagem | 04 - Produto Acabado
						def cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "49"));
						if(cstIpi == null) throw new ValidacaoException("Não foi possível localizar CST de IPI 49.");
						
						eaa0103.eaa0103cstIpi = cstIpi;
						aaj11_cstIpi = cstIpi;
					}
				}

				//Ajusta CST de acordo com a classificação do ITEM e Gera valor da obs fiscal
				if (abm12 != null && (abm12.abm12tipo == 0 || abm12.abm12tipo == 7)) { //00 - Revenda e 07-Uso e Consumo

					if (jsonEaa0103.getBigDecimal_Zero("ipi_aliq") > 0) {
						def cstIpi = getSession().get(Aaj11.class, Criterions.eq("aaj11codigo", "49"));
						if(cstIpi == null) throw new ValidacaoException("Não foi possível localizar CST de IPI 49.");
						
						eaa0103.eaa0103cstIpi = cstIpi;
						aaj11_cstIpi = cstIpi;

						if (abe01.abe01regTrib == null && jsonAbe01.getString("regime_especial") == "S") {
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
			
			//================================
			//******       ICMS         ******
			//================================
			def cstA = abm12.abm12cstA == null ? "0" : abm12.abm12cstA;
			if (eaa0103.eaa0103cstIcms != null) {
				def cst = aaj10_cstIcms.aaj10codigo.length() > 2 ? aaj10_cstIcms.aaj10codigo.substring(1) : aaj10_cstIcms.aaj10codigo;
				
				//Venda c/ Substituição tributária (O PCD deve ser configurado com CST 00 e CFOP 5101)
				if (jsonAbm1001.getBigDecimal_Zero("iva_venda") > 0 && cst != "910" && eaa0102.eaa0102contribIcms != 0) {
					if (operacao == 1 || operacao == 2 || operacao == 7) {
						cst = cstA + "10";
					}
				}

				//Redução do ICMS c/ ST no Item - Estado (da Entidade Destino), exceto 03-Simples Nacional e com ST
				if (jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc") > 0 && jsonAbm1001.getBigDecimal_Zero("iva_venda") > 0 && cst != "910" && eaa0102.eaa0102contribIcms != 0) {
					if (operacao == 1 || operacao == 2) {
						cst = cstA + "70";
					}
				}

				//Redução do ICMS do Item - Estado (da Entidade Destino), exceto 03-Simples Nacional e com ST
				if (jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc") > 0 && jsonAbm1001.getBigDecimal_Zero("iva_venda") == 0 && cst != "910") {
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

				//% ICMS FIXO da UF da entidade destino especificado no item
				if (jsonAbm1001.getBigDecimal_Zero("icm_aliq") > 0) {
					jsonEaa0103.put("icm_aliq", jsonAbm1001.getBigDecimal_Zero("icm_aliq"));
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
					jsonEaa0103.put("tx_iva_st", jsonAbm1001.getBigDecimal_Zero("iva_venda"));

					if (jsonEaa0103.getBigDecimal_Zero("icm_aliq") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada alíquota de ICMS.")
					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") == 0) throw new ValidacaoException("Informado CST ICMS ST, porém não foi informado o IVA no item referente a esta operação.");
						
					cstValido = true;
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", 0.00);
					jsonEaa0103.put("icm_isento", 0.00);

					//Alíquota do ICMS ST = Alíquota para operações internas do cadastro de Estados da entidade destino
					jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));

					//% ICMS da UF da entidade destino especificada no Item para cálculo do ICMS ST
					if (jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq") > 0) {
						jsonEaa0103.put("st_aliq", jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq"));
					}

					//BC ICMS ST
					jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc"));

					//Adiciona IVA a Base de Cálculo do ICMS ST
					jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) /  100) + 1).round(2));

					//Cálcula redução da BC ICMS ST
					if (jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc") > 0) {
						jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
						jsonEaa0103.put("icm_reducst_bc", jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc"));
					}

					//Cálcula ICMS ST
					//ICMS ST = Base * Alíquota Interna Estado de Destino - Valor Icms Normal
					if (jsonEaa0103.getBigDecimal_Zero("st_aliq") > 0) {
						jsonEaa0103.put("st_icm", ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("st_aliq")) / 100).round(2));
					}
				}

				//CST x20 - Operação Tributada com REDUÇÃO da base de cálculo
				if (aaj10_cstIcms.aaj10codigo.substring(1) == "20") {
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc"));

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

						if (cfop == "102" || cfop == "202" ) {
							cfop = dentroEstado == 1 ? "5405" : "6403";

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
					
					jsonEaa0103.put("tx_iva_st", jsonAbm1001.getBigDecimal_Zero("iva_venda"));
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc"));
					
					def vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_reduc_bc")) / 100).round(2);
					jsonEaa0103.put("icm_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") - vlrReducao);
					jsonEaa0103.put("icm_icm", ((jsonEaa0103.getBigDecimal_Zero("icm_bc") * jsonEaa0103.getBigDecimal_Zero("icm_aliq")) / 100).round(2));
					jsonEaa0103.put("icm_outras", vlrReducao);
					jsonEaa0103.put("icm_isento", 0.00);

					jsonEaa0103.put("st_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));

					if (jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq") > 0) {
						jsonEaa0103.put("st_aliq", jsonAbm1001.getBigDecimal_Zero("fixicmsst_aliq"));
					}

					if (jsonEaa0103.getBigDecimal_Zero("tx_iva_st") > 0) {
						jsonEaa0103.put("st_bc", jsonEaa0103.getBigDecimal_Zero("icm_bc") + vlrReducao);
						//Adiciona IVA a Base de Cálculo do ICMS ST
						jsonEaa0103.put("st_bc", (((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonEaa0103.getBigDecimal_Zero("tx_iva_st")) / 100) + 1).round(2));

						//Cálcula redução da BC ICMS ST
						if (jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc") > 0) {
							jsonEaa0103.put("st_bc", (jsonEaa0103.getBigDecimal_Zero("st_bc") - ((jsonEaa0103.getBigDecimal_Zero("st_bc") * jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc")) / 100)).round(2));
							jsonEaa0103.put("icm_reducst_bc", jsonAbm1001.getBigDecimal_Zero("icm_reducst_bc"));
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
					jsonEaa0103.put("icm_reduc_bc", jsonAbm1001.getBigDecimal_Zero("icm_reduc_bc"));

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
						jsonEaa0103.put("icm_bc", 0.00);
						jsonEaa0103.put("icm_reduc_bc", 0.00);
						jsonEaa0103.put("icm_aliq", 0.00);
						jsonEaa0103.put("icm_icm", 0.00);
						jsonEaa0103.put("icm_isento", 0.00);
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
			eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;
		}
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==