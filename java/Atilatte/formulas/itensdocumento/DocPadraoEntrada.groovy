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
import sam.model.entities.aa.Aaj15;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.ab.Abb01;
import sam.model.entities.ab.Abb10;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe40;
import sam.model.entities.ab.Abe4001;
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

public class DocPadraoEntrada extends FormulaBase {

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
	private Aam06 aam06umc;
	
	private Abb01 abb01;
	private Abb10 abb10;
	private Abd01 abd01;
	private Abe01 abe01;
	private Abe02 abe02;
	private Abe40 abe40;
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
		
		//Dados Fiscais do item
		abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null;
		if (abm12 == null) throw new ValidacaoException("Não foi encontrada a configuração fiscal do item: " + abm01.abm01codigo);
		if (abm12.abm12tipo == null) throw new ValidacaoException("Necessário informar o tipo fiscal do item: " + abm01.abm01codigo);
		
		//Dados Comerciais do item
		abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

		//Fatores de Conv. da Unid de Compra para Estoque
		abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));
		if (abm1301 == null) throw new ValidacaoException("Não foi informado fator de conversão de compra para o item." + abm01.abm01codigo);
		
		//Unidade de Medida
		aam06 = abm13 != null &&  abm13.abm13umv != null ? getSession().get(Aam06.class, abm13.abm13umv.aam06id) : null;

		//Unidade de Medida de Compra
		aam06umc = getSession().get(Aam06.class, eaa0103.eaa0103umComl.aam06id);


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

		// Tabela de Preço
		abe40 = eaa01.eaa01tp != null ? getSession().get(Abe40.class, eaa01.eaa01tp.abe40id) : null;

		
		


		
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
		
		//Determina se a operação é dentro ou fora do estado
		def dentroEstado = false;
		if (ufEmpr != null && ufEnt != null) {
			dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
		}

		//Troca CFOP (Dentro ou Fora do Estado)
		if (eaa0103.eaa0103cfop != null) {
				def cfop = aaj15_cfop.aaj15codigo.substring(1);
				
				def primeiroDigito = aaj15_cfop.aaj15codigo.substring(0,1);
				if(!dentroEstado){
					if(primeiroDigito == "1"){
						primeiroDigito = "2";
					}
					
				}
				cfop = primeiroDigito + cfop;
				aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
				eaa0103.eaa0103cfop = aaj15_cfop;
		}
		
		
		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0) {

			// Quantidade Convertida
			jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
			
			// Considera o valor unitário do último pedido de compra caso não informado a tabela de preço 
			if(abe40 == null){
				if(eaa0103.eaa0103unit == 0 ){
					
					String sql = "select max(abb01data),eaa0103unit as unit from eaa01 "+
							"inner join eaa0103 on eaa01id = eaa0103doc "+
							"inner join abb01 on abb01id = eaa01central "+ 
							"where eaa0103item = :item "+
							"and eaa01clasdoc = 0 "+
							"group by eaa0103unit ";
					
					TableMap ultimoRegistro = getAcessoAoBanco().buscarUnicoTableMap(sql,Parametro.criar("item",abm01.abm01id));
					if(ultimoRegistro != null) eaa0103.eaa0103unit = ultimoRegistro.getBigDecimal("unit");
				}	
			}

			// Unitario Convertido 
			jsonEaa0103.put("unitario_conv",eaa0103.eaa0103unit)

			//Verifica a Mensagem do IVA no cadastro do Ítem
			if(jsonAbm1001_UF_Item.getString("mensagem") != null){
				jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
			}

			// UMV 
			jsonEaa0103.put("umv", aam06.aam06codigo);

			// UMC
			jsonEaa0103.put("umc", aam06umc.aam06codigo);
			
			//Define o Campo de Unitário para Estoque
			if(abm1301.abm1301fcCU == BigDecimal.ZERO) throw new ValidacaoException("Valor inválido informado para fator de conversão do item " + abm01.abm01codigo)
			
			jsonEaa0103.put("unitario_estoque", (eaa0103.eaa0103unit / abm1301.abm1301fcCU).round(2));

			// *** Processa QUANTIDADES
			// Conserva Qt.Original do documento (Qt.Faturamento original)
			if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
				jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
			}

			// Converte Qt.Documento para Qtde SCE
			eaa0103.eaa0103qtUso = round((eaa0103.eaa0103qtComl * abm1301.abm1301fcCU),2);
			
			// Peso Bruto
			if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));

			// Peso Líquido
			if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
				
			
			// Converte Qt.Documento para Volume
			jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl);
		
			//=========================================
			//********* Calculo Item Frete ************
			//=========================================
		
			if(eaa0102.eaa0102despacho != null){
				String codDesp = eaa0102.eaa0102despacho.abe01id;
				def itemId = abm01.abm01id;
			
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("001","aba2001ent = '" + codDesp + "' and aba2001item = '" + itemId + "'");
				
				def valorFrete = aba2001?.getAba2001json()?.get("vlr_frete_transp") ?: 0;
			    	jsonEaa0103.put("frete_item", eaa0103.eaa0103qtComl * valorFrete)
			}else{
				jsonEaa0103.put("frete_item", 0);
			}

			// Total do item
			eaa0103.eaa0103total = round((eaa0103.eaa0103qtComl * eaa0103.eaa0103unit),2);

			//Total Item Convertido
			jsonEaa0103.put("total_conv", (jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")).round(2))
			
			
			//================================
			//******        IPI         ******
			//================================
			
			//Base de cálculo IPI
			jsonEaa0103.put("bc_ipi",eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") + jsonEaa0103.getBigDecimal_Zero("seguro") + jsonEaa0103.getBigDecimal_Zero("outras_despesas"))

			//Alíquota de IPI do cadastro de NCM
			
			if(jsonEaa0103.getBigDecimal_Zero("_ipi") == 0){
				if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
					jsonEaa0103.put("_ipi", abg01.abg01txIpi);
				}
			}

			//Aplica a aliquota Para Calcular IPI
			jsonEaa0103.put("ipi", (jsonEaa0103.getBigDecimal_Zero("bc_ipi") * jsonEaa0103.getBigDecimal_Zero("_ipi") / 100))
			jsonEaa0103.put("ipi",jsonEaa0103.getBigDecimal_Zero("ipi").round(2));

			//Valor de IPI outras 
			if(jsonEaa0103.getBigDecimal_Zero("_ipi") == -1){
				jsonEaa0103.put("bc_ipi", 0);
				jsonEaa0103.put("_ipi", 0);
				jsonEaa0103.put("ipi", 0);

			}
		

			//================================
			//******       ICMS         ******
			//================================

			def vlrReducao = 0;
			//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
				jsonEaa0103.put("bc_icms", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
					                                             jsonEaa0103.getBigDecimal_Zero("seguro") +
														jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
													     jsonEaa0103.getBigDecimal_Zero("desconto"));
		     // Tratar redução da base de cáulculo
		     // % Reduc BC ICMS = % reduc BC ICMS do ítem
		     if(jsonAbm1001 != null){
		     	if(abe01.abe01contribIcms == 1 ){
			     	if (jsonAbm1001.getBigDecimal_Zero("_red_bc_icms") != 0) {
						jsonEaa0103.put("_red_bc_icms", jsonAbm1001.getBigDecimal_Zero("_red_bc_icms"));
					}
		     	}
		     }
		     
		     
		     // Calculo da Redução 
		     if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){
		     	vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_red_bc_icms")) / 100).round(2);
		     	jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao);
		     }
		      
//		     // Obter a Aliquota de ICMS 
//		     if(jsonEaa0103.getBigDecimal_Zero("_icms") == 0){
//		     	if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms") != 0){
//		     		//Alíquota padrão de ICMS para operações internas (ENTIDADE)
//					jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
//		     	}
//		     }
		     // Obter a Aliquota de ICMS 
		     if(jsonEaa0103.getBigDecimal_Zero("_icms") == 0){
		     	if(jsonAag02Ent.getBigDecimal_Zero("txicmsaida") != 0){
		     		//Alíquota padrão de ICMS para operações internas (ENTIDADE)
					jsonEaa0103.put("_icms", jsonAag02Ent.getBigDecimal_Zero("txicmsaida"));
		     	}
		     }

		     // Calcular valor do ICMS e Valor ICMS Isento
		     if(jsonEaa0103.getBigDecimal_Zero("_icms") < 0){ // Aliquota menor que zero = Isento
		     	jsonEaa0103.put("icms", 0);
		     	jsonEaa0103.put("icms_outras", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
		     	jsonEaa0103.put("bc_icms", 0 );
		     	jsonEaa0103.put("_red_bc_icms", 0);
		     	jsonEaa0103.put("icms_isento", 0);
		     	vlrReducao = 0;
		     	
		     }else{
		     	jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));
		     }
			if(jsonAbe01 != null){
				 if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1){
		     	//BC ICMS ST
				jsonEaa0103.put("bc_icms_st", (eaa0103.eaa0103total - 
				jsonEaa0103.getBigDecimal_Zero("vlr_desc") + 
				jsonEaa0103.getBigDecimal_Zero("frete_dest") + 
				jsonEaa0103.getBigDecimal_Zero("seguro") + 
				jsonEaa0103.getBigDecimal_Zero("outras_despesas") + jsonEaa0103.getBigDecimal_Zero("ipi")).round(2));

				def ivaST = 0;
				if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno") != null){
					//Alíquota do ICMS ST = Alíquota para operações internas do cadastro de Estados da entidade destino
					jsonEaa0103.put("_icms_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno"));
					if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 || jsonAbe01.getBigDecimal_Zero("calcula_st") == 2 ){
						// % IVA_ST para Varejista
						ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st");
					}
				}

				if(ivaST > 0){
					//Adiciona IVA a Base de Cálculo do ICMS ST
					jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") + (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * ( ivaST / 100 ))).round(2) );
					
					//Cálcula ICMS ST
					//ICMS ST = Base * Alíquota Interna Estado de Destino - Valor Icms Normal
					jsonEaa0103.put("icms_st", ((jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (jsonEaa0103.getBigDecimal_Zero("_icms_st")/ 100))- jsonEaa0103.getBigDecimal_Zero("icms") ).round(2));
				}else{
					jsonEaa0103.put("bc_icms_st", 0);
					jsonEaa0103.put("icms_st", 0);
					jsonEaa0103.put("_icms_st", 0);
					
				}

				if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 ){
					jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
					jsonEaa0103.put("icms_fcp", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
					jsonEaa0103.put("vlr_icms_fcp_", jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp") / 100);
				}
				
				
					
		     	}
			}
		    

			// Troca CST 
			if(dentroEstado){
				if(jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0 ){
					
					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "070"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
		
				}else if(jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && (jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0 || jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == -1)){

					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "010"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
					
				}else if(jsonEaa0103.getBigDecimal_Zero("icms_st") == 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){

					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "020"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
					
				}else{
					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
				}
			}else{
				if(jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0 ){
					
					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "070"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
		
				}else if(jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && (jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0 || jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == -1)){

					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "010"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
					
				}else if(jsonEaa0103.getBigDecimal_Zero("icms_st") == 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){

					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "020"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
					
				}else{
					//CST x00 - Mercadoria Tributada Integralmente
					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
				}
			}
			
		
	          
		// ==================
		// TOTAL DO DOCUMENTO
		// ==================
		//Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
		eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") +
										jsonEaa0103.getBigDecimal_Zero("frete_dest") +
										jsonEaa0103.getBigDecimal_Zero("seguro") +
										jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
										jsonEaa0103.getBigDecimal_Zero("icms_st") +
										jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp")-
										jsonEaa0103.getBigDecimal_Zero("desconto");
										
				
	     eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
	     
	     // Ajusta o valor do IPI Outras
		jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

	
       //Valor do Financeiro
	  eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  
		   
	  
	
		
	 
		 //BC Comissão
		 def txdesc = 0;
		 def vlrtxdesc = 0;
			 
		      
	 	 txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
		 vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
		 jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc);     
		
		 

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

			//*******Calculo para SPED ICMS*******
			
			//BC ICMS SPED = BC ICMS 
			jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
			
			//Aliq ICMS SPED = Aliq ICMS
			jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("_icms"));
			
			
			//Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
			jsonEaa0103.put("_reduc_bcicms_sped", jsonEaa0103.getBigDecimal_Zero("_red_bc_icms"));
			
			//ICMS Outras SPED = ICMS Outras
			jsonEaa0103.put("icms_outras_sped", jsonEaa0103.getBigDecimal_Zero("icms_outras"));
			
			//ICMS Isento SPED = ICMS Isento
			jsonEaa0103.put("icmsisento_sped", jsonEaa0103.getBigDecimal_Zero("icms_isento"));
			
			
			//ICMS SPED = ICMS
			jsonEaa0103.put("icms_sped", jsonEaa0103.getBigDecimal_Zero("icms"));


			//*******Calculo para SPED ICMS ST*******
			
			//BC ICMS ST SPED = BC ICMS ST
			jsonEaa0103.put("bc_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
			
			//Aliq ICMS ST SPED = Aliq ICMS ST
			jsonEaa0103.put("_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("_icms_st"));
			
			//ICMS ST SPED = ICMS ST
			jsonEaa0103.put("icmsst_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));



			//*******Calculo para SPED IPI*******
			
			//BC IPI SPED = BC IPI
			jsonEaa0103.put("bcipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));
			
			//Aliq IPI SPED = Aliq IPI
			jsonEaa0103.put("_ipi_sped", jsonEaa0103.getBigDecimal_Zero("_ipi"));
			
			//IPI Outras SPED = IPI Outras
			jsonEaa0103.put("ipi_outras_sped", jsonEaa0103.getBigDecimal_Zero("ipi_outras"));
			
			//IPI Isento SPED = IPI Isento
			jsonEaa0103.put("ipi_isento_sped", jsonEaa0103.getBigDecimal_Zero("ipi_isento"));
			
			//IPI SPED = IPI
			jsonEaa0103.put("ipi_sped", jsonEaa0103.getBigDecimal_Zero("ipi"));


		 

		 
		 
		 

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
					jsonEaa0103.put("volumes", 0.00);
					jsonEaa0103.put("peso_liquido", 0.0000);
					jsonEaa0103.put("peso_bruto", 0.0000);
				}
			}

			// ----------------------------------------------------------------
			// GERAR VALOR APROXIMADO DE IMPOSTOS PARA VENDA A CONSUMIDOR FINAL
			// ----------------------------------------------------------------
				// Excetua-se industrialização
			if(aaj15_cfop != null){
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
						jsonEaa0103.put("icmsufdest_bc", jsonEaa0103.getBigDecimal_Zero("bc_icms"));

						//Aliquota de Icms Interestadual
						jsonEaa0103.put("interesuf_aliq", jsonEaa0103.getBigDecimal_Zero("_icms"));

						//Calcula o ICMS FCP
						if (jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
							jsonEaa0103.put("icms_fcp", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq")) / 100).round(2));
						}

						//Calcula o ICMS Interno UF Destino
						if (jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq") > 0) {
							jsonEaa0103.put("interufdest_icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq")) / 100).round(2));
						}

						//Calcula vlr de ICMS devido a UF Destino
						jsonEaa0103.put("icms_ufdest", (jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - ((jsonEaa0103.getBigDecimal_Zero("icms") * jsonEaa0103.getBigDecimal_Zero("partilha_aliq")) / 100)).round(2));

						//Calcula vlr de ICMS devido a UF Origem
						jsonEaa0103.put("uforig_icms", jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - jsonEaa0103.getBigDecimal_Zero("icms") - jsonEaa0103.getBigDecimal_Zero("icms_ufdest"));
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