package Atilatte.formulas.itensdocumento;

import br.com.multiorm.Query
import java.lang.Math;
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
import sam.model.entities.ab.Abd02;
import sam.model.entities.ab.Abb10;
import sam.model.entities.ab.Abd01;
import sam.model.entities.ab.Abe01;
import sam.model.entities.ab.Abe02;
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

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

public class FormulaGeral extends FormulaBase {

	private Aac10 aac10;
	private Aag01 aag01;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
	private Aaj10 aaj10_cstIcms;
	private Aaj10 aaj10_cstIcmsB;
	private Aaj11 aaj11_cstIpi;
	private Aaj12 aaj12_cstPis;
	private Aaj13 aaj13_cstCof;
	private Aaj14 aaj14_cstCsosn;
	private Aaj15 aaj15_cfop;
	private Aam06 aam06;
	
	private Abb01 abb01;
	private Abb10 abb10;
	private Abd01 abd01;
	private Abd02 abd02;
	private Abe01 abe01;
	private Abe02 abe02;
	private Abe40 abe40;
	private Abe4001 abe4001;
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
	private TableMap jsonAbe4001;

	@Override
	public void executar() {

		//Item do documento
		eaa0103 = get("eaa0103");
		if(eaa0103 == null) return;
		
		//Documento
		eaa01 = eaa0103.eaa0103doc;
		
		for(Eaa0102 dadosGerais : eaa01.eaa0102s) {
			eaa0102 = dadosGerais;
		}
		
		
		
		//Central de Documento
		abb01 = eaa01.eaa01central;
		
		//PCD
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
		//if (abd01 != null && abd01.abd01es == 0)  throw new ValidacaoException("Esta fórmula poderá ser utilizada somente em documentos de saída.");
		
		//PCD Fiscais 
		abd02 = getSession().get(Abd02.class, abd01.abd01ceFiscais.abd02id);
		
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

		//Tabela Preço
		abe40 = eaa01.eaa01tp != null ? getSession().get(Abe40.class, eaa01.eaa01tp.abe40id) : null;
		
		//Itens da Tabela de Preço
		abe4001 = abe40 != null ? getSession().get(Abe4001.class, Criterions.where("abe4001tab = " + abe40.abe40id + " AND abe4001item = " + abm01.abm01id)) : null;
		if(abe4001 == null && eaa01.eaa01tp != null) throw new ValidacaoException("Item Não Encontrado Na Tabela De Preço!")


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
		jsonAbe4001 = abe4001 != null ? abe4001.abe4001json : new TableMap(); 
		jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();

		
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
			
			//Define se a entidade é ou não contribuinte de ICMS
			def contribICMS = 0;
			if(abe01.abe01cli == 1){
				contribICMS = abe01.abe01contribIcms; // Cliente
			}
			if(abe01.abe01for == 1){
				contribICMS = abe01.abe01contribIcms; // Fornecedor
			}
			
			// Verifica se o tipo de inscrição é CPF, se sim, define como não contribuinte de ICMS
			if(abe01.abe01ti == 1){
				contribICMS = 0;
			}
			
		
			if(eaa0103.eaa0103qtComl > 0){

				// Define a data de entrega dos itens
				defineDataEntregaItens();
				
				jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);	
	
				// Define a Especie do Documento
				eaa0102.eaa0102especie = "Volumes";
				
				
				
				def codItem = abm01.abm01codigo;
			Query descrCriterios = getSession().createQuery("select aba3001descr from aba3001 "+
															"inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
															"inner join abm01 on abm0102item = abm01id "+
															"where abm01codigo = '"+codItem+"'"+
															"and abm01tipo = 1 ");

			List<TableMap> listCriterios = descrCriterios.getListTableMap();
			String grupo = ""; 
			for(TableMap criterio : listCriterios){
				if(criterio.getString("aba3001descr").toUpperCase().contains("QUEIJO")){
					grupo = "Queijo"
				}
				if(criterio.getString("aba3001descr").toUpperCase().contains("LEITE")){
					grupo = "Leite"
				}

				if(criterio.getString("aba3001descr").toUpperCase().contains("IOGURTE") || criterio.getString("aba3001descr").toUpperCase().contains("BAUNILHA")){
					grupo = "Iogurte"
				}
			}
				
				
				//Define o preço unitário de acordo com a Tabela de Preço
				//if(jsonEaa0103.getBigDecimal_Zero("calculado") == 0){
				if(eaa0103.eaa0103unit == 0){
					if(eaa01.eaa01tp != null){
						if(jsonAbe4001 != null){
							if(jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0){
								
								DateTimeFormatter formato2 = DateTimeFormatter.ofPattern("yyyyMMdd"); 
								LocalDate dataPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_fin"), formato2);
								LocalDate dataAtual = LocalDate.now();
								def precoPromocao = jsonAbe4001.getBigDecimal_Zero("preco_promocao");
								if(dataPromo > dataAtual){
									eaa0103.eaa0103unit = precoPromocao;
								}else{
									eaa0103.eaa0103unit = abe4001.abe4001preco
								}
							}else{
								eaa0103.eaa0103unit = abe4001.abe4001preco
							}	
						}else{
							eaa0103.eaa0103unit = abe4001.abe4001preco
						}
					}
				}
					//jsonEaa0103.put("calculado", 1);
				//}


				
				// Define o campo ordem de separação de acordo com o cadastro do item
				jsonEaa0103.put("ordem_separacao", jsonAbm0101.getBigDecimal_Zero("ordem_separacao"));

				// Define CFOP 
				if(eaa0103.eaa0103cfop == null) eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "5910"));
				
				if(grupo == "Iogurte"){
	
					//Define Quantidade Comercial como Quantidade Convertida
					jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
					
					//Unidade Medida Venda 
					jsonEaa0103.put("umv",aam06.aam06codigo);
					
					//Troca a Unidade de medida de acordo com o parâmetro na entidade
					if(jsonAbe01.getBigDecimal_Zero("unidade_caixa") == 1 && jsonEaa0103.getString("umv") == 'FR'){
						jsonEaa0103.put("umv", 'CX');
						jsonEaa0103.put("umf", 'CX');
						eaa0103.eaa0103umu == 'CX';
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf") > 0){
							jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}else{
							throw new ValidacaoException("Quantidade CVDNF Inválida!");
						}
					}
					
					
					if(jsonEaa0103.getBigDecimal_Zero("calculado") == 0){
						if(eaa0103.eaa0103unit == 0){
							// Calculando o preço unitario de acordo com a tabela de preço
							if(eaa01.eaa01tp != null){
								def tab = eaa01.eaa01tp.abe40id;
								def item = abm01.abm01id; 
								def precoUnit = getAcessoAoBanco().obterBigDecimal("select abe4001preco from abe4001 where abe4001tab ='"+ tab +"' and abe4001item ="+ item);
								//throw new ValidacaoException(tab)
								eaa0103.eaa0103unit = precoUnit;	
							}	
						}	
						jsonEaa0103.put("calculado", 1);
					}

					
					//Define o Campo de Unitário para Estoque
					jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);
					
					
					//Verifica a Mensagem do IVA no cadastro do Ítem
					if(jsonAbm1001_UF_Item.getString("mensagem") != null){
						jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
					}
					
					// *** Processa QUANTIDADES
					// Conserva Qt.Original do documento (Qt.Faturamento original)
					if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
						jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
					}
					
					
					
					// Converte Qt.Documento para Qtde SCE
					eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl//jsonEaa0103.getBigDecimal_Zero("qt_convertida") * jsonAbm0101.getBigDecimal_Zero("cvdnf"); 
					
					
					// Peso Bruto
					if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));
					
					// Peso Líquido
					if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
					
					
					// *** Processa VALORES
					//Verifica se a Unidade de Medida é caixa então calcula o valor da caixa
					if(jsonEaa0103.getBigDecimal_Zero("tabunit") == 0){
						jsonEaa0103.put("tabunit", eaa0103.eaa0103unit ); 
					}
					
					if(jsonEaa0103.getString("umv") == 'CX'){
						def valrUnit = eaa0103.eaa0103unit;
						def fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf");
						jsonEaa0103.put("unitario_conv", jsonEaa0103.getBigDecimal_Zero("tabunit") * fatorUM);
					}
					
					
					// Quantidade de caixa e frasco
					if(jsonAbm0101.getBigDecimal_Zero("volume_caixa") != 0){
						if(jsonEaa0103.getString("umv") == 'CX'){
							jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
							}else{
							def int fatorUM = jsonAbm0101.getBigDecimal_Zero("volume_caixa");
							def int qtd = jsonEaa0103.getBigDecimal_Zero("qt_convertida");
							def int caixa;
							def int frasco;
							
							caixa = qtd / fatorUM;
							frasco = qtd % fatorUM;
							
							jsonEaa0103.put("caixa", caixa );
							jsonEaa0103.put("frasco", frasco );
						}
					}
					
					
					
					
					// Converte Qt.Documento para Volume
					jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
					
					//Desconto unitário
					if (jsonEaa0103.getString("umv") == 'CX'){
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf")){
							jsonEaa0103.put("desconto",jsonEaa0103.getBigDecimal_Zero("desc_unit") * jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}
					}else{
						jsonEaa0103.put("desconto", (( jsonEaa0103.getBigDecimal_Zero("desc_unit") * eaa0103.eaa0103qtComl)).round(2));
					}
					
					
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
						jsonEaa0103.put("frete_item", 0.000000);
					}
					// Total do item
					eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

					//Total Item Convertido
					jsonEaa0103.put("total_conv", (jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")).round(2))
				
					
					//================================
					//******        IPI         ******
					//================================
					if (eaa0103.eaa0103cstIpi != null) {
						//BC de IPI = Total do Item + Frete + Seguro + Despesas Acessorias
						jsonEaa0103.put("bc_ipi", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
						jsonEaa0103.getBigDecimal_Zero("seguro") +
						jsonEaa0103.getBigDecimal_Zero("outras_despesas"));
						
						//Alíquota de IPI do cadastro de NCM
						if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
							jsonEaa0103.put("_ipi", abg01.abg01txIpi);
						}
						
						def cstValido = false;
						//CST 50 - Saída Tributada
						if (aaj11_cstIpi.aaj11codigo == "50") {
							if (jsonEaa0103.getBigDecimal_Zero("_ipi") == 0) throw new ValidacaoException("CST indica saída tributada, porém não foi informada a alíquota de IPI.");
							
							cstValido = true;
							//Valor do IPI = BC IPI X Alíquota de IPI
							jsonEaa0103.put("ipi", ((jsonEaa0103.getBigDecimal_Zero("bc_ipi") * jsonEaa0103.getBigDecimal_Zero("_ipi")) / 100).round(2));
						}
						
						//CST 51 - Saída tributavel com alíquota zero
						//CST 53 - Saída não tributada
						//CST 54 - Saída Imune
						//CST 55 - Saída com Suspensão
						//CST 99 - Outras Saídas
						if (aaj11_cstIpi.aaj11codigo == "51" || aaj11_cstIpi.aaj11codigo == "53" ||	aaj11_cstIpi.aaj11codigo == "54" || aaj11_cstIpi.aaj11codigo == "55" || aaj11_cstIpi.aaj11codigo == "99") {
							cstValido = true;
							jsonEaa0103.put("ipi_outras", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));
							jsonEaa0103.put("bc_ipi",0.000000);
							jsonEaa0103.put("_ipi",0.000000);
							jsonEaa0103.put("ipi_isento",0.000000);
						}
						
						//CST 52 - Saída Isenta
						if (aaj11_cstIpi.aaj11codigo == "52") {
							cstValido = 1;
							jsonEaa0103.put("ipi_isento",eaa0103.eaa0103totDoc);
							jsonEaa0103.put("bc_ipi",0.000000);
							jsonEaa0103.put("_ipi",0.000000);
							jsonEaa0103.put("ipi_outras",0.000000);
						}
						
						if (!cstValido) throw new ValidacaoException("CST de IPI inválido.");
					}
					
					//================================
					//******       ICMS         ******
					//================================
					
					def vlrReducao = 0;
					//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
					jsonEaa0103.put("bc_icms", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
					jsonEaa0103.getBigDecimal_Zero("seguro") +
					jsonEaa0103.getBigDecimal_Zero("outras_despesas"));
					// Tratar redução da base de cáulculo
					// % Reduc BC ICMS = % reduc BC ICMS do ítem
					if(abe01.abe01contribIcms == 1 ){
						if (jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms") != 0) {
							jsonEaa0103.put("_red_bc_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms"));
						}
					}
					
					// Calculo da Redução 
					if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){
						vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_red_bc_icms")) / 100).round(2);
						jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao);
					}
					
					// Obter a Aliquota de ICMS 
					if(jsonEaa0103.getBigDecimal_Zero("_icms") == 0){
						if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms") != 0){
							//Alíquota padrão de ICMS para operações internas (ENTIDADE)
							jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
						}
					}
					
					// Calcular valor do ICMS e Valor ICMS Isento
					if(jsonEaa0103.getBigDecimal_Zero("_icms") < 0){ // Aliquota menor que zero = Isento
						jsonEaa0103.put("icms", 0.000000);
						jsonEaa0103.put("icms_outras", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
						jsonEaa0103.put("bc_icms", 0.000000 );
						jsonEaa0103.put("_red_bc_icms", 0.000000);
						jsonEaa0103.put("icms_isento", 0.000000);
						vlrReducao = 0;
					}else{
						jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));
					}
					if(jsonAbe01 != null){
						if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 || jsonAbe01.getBigDecimal_Zero("calcula_st") == 2){
							//BC ICMS ST
							jsonEaa0103.put("bc_icms_st", (eaa0103.eaa0103total + 
							jsonEaa0103.getBigDecimal_Zero("frete_dest") + 
							jsonEaa0103.getBigDecimal_Zero("seguro") + 
							jsonEaa0103.getBigDecimal_Zero("outras_despesas") + jsonEaa0103.getBigDecimal_Zero("ipi")).round(2));
							
							def ivaST = 0;
							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno") != null){
								//Alíquota do ICMS ST = Alíquota para operações internas do cadastro de Estados da entidade destino
								jsonEaa0103.put("_icms_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno"));
								if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 || jsonAbe01.getBigDecimal_Zero("calcula_st") == 2){
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
								jsonEaa0103.put("bc_icms_st", 0.000000);
								jsonEaa0103.put("icms_st", 0.000000);
								jsonEaa0103.put("_icms_st", 0.000000);
							}

							// Calculo de redução da base de ICMS solicitado pela Deyse 31/03/2025
						def vlrReducaoST = new BigDecimal(0);
						def aliqReducaoST = new BigDecimal(0);

						// Calcula Redução da Base de ICMS ST
						if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms_st") > 0){
							aliqReducaoST = jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms_st");

							vlrReducaoST = jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms_st") / 100);

							// Aplica a aliquota de ICMS interestadual para calculo do ICMS ao invés da aliquota normal de ICMS
							def icmsAux = jsonEaa0103.getBigDecimal_Zero("bc_icms") * (jsonAbm1001_UF_Item.getBigDecimal_Zero("_aliq_benef_st")  / 100);

							// Preenche a Aliquota de Beneficio na linha do item
							jsonEaa0103.put("_aliq_benef_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_aliq_benef_st"));
							
							// BC ICMS ST Reduzida
							jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") - vlrReducaoST).round(2));

							// ICMS ST Com Reducao
							jsonEaa0103.put("icms_st", ((jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (jsonEaa0103.getBigDecimal_Zero("_icms_st")/ 100))- icmsAux ).round(2));

						}

						// Preenche a linha do item com a aliq. de redução de ICMS ST
						jsonEaa0103.put("_red_bc_icms_st", aliqReducaoST);
						
						// Preenche a linha do item com a redução de ICMS ST
						jsonEaa0103.put("vlr_reduc_icms_st", vlrReducaoST.round(2));
							
							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 ){
								jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
								jsonEaa0103.put("icms_fcp_", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
								jsonEaa0103.put("vlr_icms_fcp_", (jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp_") / 100).round(2));
							}
						}
						
						// Define Tx. Iva do Item
						if(jsonEaa0103.getBigDecimal_Zero("bc_icms_st") > 0){
							jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st"))
						}else{
							jsonEaa0103.put("tx_iva_st", 0.000000)
						}
					}
					//Total do documento com ST
					if(jsonEaa0103.getBigDecimal_Zero("icms_st") != 0 ){
						jsonEaa0103.put("total_doc_st", 	eaa0103.eaa0103total + 
													jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") + 
													jsonEaa0103.getBigDecimal_Zero("ipi") +
													jsonEaa0103.getBigDecimal_Zero("frete_dest") +
													jsonEaa0103.getBigDecimal_Zero("seguro") +
													jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
													jsonEaa0103.getBigDecimal_Zero("icms_st_sped") -
													jsonEaa0103.getBigDecimal_Zero("desconto"));										  						  										  
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
					
					
					//if (aaj10_cstIcms == null) throw new ValidacaoException("Não foi possível localizar CST de ICMS " + cst + ".");
					
					//==========================================================================//
					//         Zona Franca / Área de Livre Comércio e Amazônia Ocidental        //
					//==========================================================================//
					def alc = jsonAag0201Ent.getInteger("munic_alc");
					def zfm = jsonAag0201Ent.getInteger("munic_zfm");
					if(alc == null ) alc = 0;
					if(zfm == null ) zfm = 0;
					
					//ALC: 2 -Zona Franca de Manaus ou 1 - Área de Livre Comércio
					if (alc == 1 || zfm == 1) {
					
						//ICMS
						jsonEaa0103.put("icms", 0.000000);
						jsonEaa0103.put("bc_icms", 0.000000); 
						jsonEaa0103.put("_icms", 0.000000); 
						
						//Obtendo CFOP
						eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6910"));
						
						//Obtendo CST de ICMS 
						eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "040"));
						
						//ICMS como Desconto
						jsonEaa0103.put("desc_icms_z_franca", (eaa0103.eaa0103totDoc * jsonAag02Ent.getBigDecimal_Zero("txicmsaida")) / 100 );
						
						//Total Documento
						eaa0103.eaa0103totDoc = 	eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
						jsonEaa0103.getBigDecimal_Zero("seguro") +
						jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
						jsonEaa0103.getBigDecimal_Zero("desc_icms_z_franca");
						//ICMS Isentas
						jsonEaa0103.put("icms_isento", eaa0103.eaa0103totDoc);	
					
					}
					
					if(ufEnt.aag02uf == 'AM'){
						if(jsonEaa0103.getBigDecimal_Zero("desc_icms_z_f_") > 0){
							//Total do Documento sem ST
							eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") +
							jsonEaa0103.getBigDecimal_Zero("frete_dest") +
							jsonEaa0103.getBigDecimal_Zero("seguro") +
							jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
							jsonEaa0103.getBigDecimal_Zero("icms_st") -
							jsonEaa0103.getBigDecimal_Zero("desc_icms_z_f_");
						}
					}else{
						
						//Total do Documento
						eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") +
						jsonEaa0103.getBigDecimal_Zero("frete_dest") +
						jsonEaa0103.getBigDecimal_Zero("seguro") +
						jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
						jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") +
						jsonEaa0103.getBigDecimal_Zero("icms_st") -
						jsonEaa0103.getBigDecimal_Zero("desconto");
					}
					
					eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
					
					// Ajusta o valor do IPI Outras
					jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);
					
					//Valor do Financeiro
					eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  
					
					//Calculo de base de calculo comissão com desconto condicional
					
					//BC Comissão
					def txdesc = 0;
					def vlrtxdesc = 0;   
					txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
					vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
					jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc);  
					
					//================================
					//******       PIS          ******
					//================================
					if(jsonAbm0101.getBigDecimal_Zero("_pis") > 0){
						jsonEaa0103.put("bc_pis", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st"));
						if(jsonEaa0103.getBigDecimal_Zero("_pis") > -1){
							jsonEaa0103.put("_pis", jsonAbm0101.getBigDecimal_Zero("_pis"));
							jsonEaa0103.put("pis", (jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("_pis")) / 100 )
						}
					}
					// Zera PIS
					if(jsonEaa0103.getBigDecimal_Zero("_pis") < 0){
						jsonEaa0103.put("pis", 0.000000);
						jsonEaa0103.put("bc_pis", 0.000000);  
					}
					
					//================================
					//******      COFINS        ******
					//================================
					if(jsonAbm0101.getBigDecimal_Zero("_cofins")){
						jsonEaa0103.put("bc_cofins", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st"));
						if(jsonEaa0103.getBigDecimal_Zero("_cofins") > -1){
							jsonEaa0103.put("_cofins", jsonAbm0101.getBigDecimal_Zero("_cofins"));
							jsonEaa0103.put("cofins", (jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("_cofins")) / 100 );
						}
					}
					// Zera Cofins
					if(jsonEaa0103.getBigDecimal_Zero("_cofins") < 0){
						jsonEaa0103.put("cofins", 0.000000);
						jsonEaa0103.put("bc_cofins", 0.000000);  
					}

					//*******Calculo para SPED ICMS*******

					//BC ICMS SPED = BC ICMS 
					jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
		
					//Aliq ICMS SPED = Aliq ICMS
					jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("_icms"));
		
		
					//Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
					jsonEaa0103.put("_red_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("_red_bc_icms"));
		
					//ICMS Outras SPED = ICMS Outras
					jsonEaa0103.put("icms_outras_sped", jsonEaa0103.getBigDecimal_Zero("icms_outras"));
		
					//ICMS Isento SPED = ICMS Isento
					jsonEaa0103.put("icms_isento_sped", jsonEaa0103.getBigDecimal_Zero("icms_isento"));
		
		
					//ICMS SPED = ICMS
					jsonEaa0103.put("icms_sped", jsonEaa0103.getBigDecimal_Zero("icms"));
		
		
					//*******Calculo para SPED ICMS ST*******
		
					//BC ICMS ST SPED = BC ICMS ST
					jsonEaa0103.put("bc_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
		
					//Aliq ICMS ST SPED = Aliq ICMS ST
					jsonEaa0103.put("_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("_icms_st"));
		
					//ICMS ST SPED = ICMS ST
					jsonEaa0103.put("icms_st_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));
		
		
		
					//*******Calculo para SPED IPI*******
		
					//BC IPI SPED = BC IPI
					jsonEaa0103.put("bc_ipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));
		
					//Aliq IPI SPED = Aliq IPI
					jsonEaa0103.put("_ipi_sped", jsonEaa0103.getBigDecimal_Zero("_ipi"));
		
					//IPI Outras SPED = IPI Outras
					jsonEaa0103.put("ipi_outras_sped", jsonEaa0103.getBigDecimal_Zero("ipi_outras"));
		
					//IPI Isento SPED = IPI Isento
					jsonEaa0103.put("ipi_isento_sped", jsonEaa0103.getBigDecimal_Zero("ipi_isento"));
		
					//IPI SPED = IPI
					jsonEaa0103.put("ipi_sped", jsonEaa0103.getBigDecimal_Zero("ipi"));
					
	
	
				}
				if(grupo == "Queijo"){
						
	
					//Define Quantidade Comercial como Quantidade Convertida
					jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
					
					//Unidade Medida Venda 
					jsonEaa0103.put("umv",aam06.aam06codigo);
					
					//Troca a Unidade de medida de acordo com o parâmetro na entidade
					if(jsonAbe01.getBigDecimal_Zero("unidade_caixa") == 1 && jsonEaa0103.getString("umv") == 'UN'){
						
						jsonEaa0103.put("umv", 'CX');
						jsonEaa0103.put("umf", 'CX');
						eaa0103.eaa0103umu == 'CX';
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf") > 0){
							jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}else{
							throw new ValidacaoException("Quantidade CVDNF Inválida!");
						}
					}
				
					if(jsonEaa0103.getBigDecimal_Zero("calculado") == 0){
					
						if(eaa0103.eaa0103unit == 0){
							// Calculando o preço unitario de acordo com a tabela de preço
							if(eaa01.eaa01tp != null){
								def tab = eaa01.eaa01tp.abe40id;
								def item = abm01.abm01id; 
								def precoUnit = getAcessoAoBanco().obterBigDecimal("select abe4001preco from abe4001 where abe4001tab ='"+ tab +"' and abe4001item ="+ item);
								//throw new ValidacaoException(tab)
								eaa0103.eaa0103unit = precoUnit;	
							}	
						}	
						jsonEaa0103.put("calculado", 1);
					}
					
					//Define o Campo de Unitário para Estoque
					jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);
					
					// *** Processa QUANTIDADES
					// Conserva Qt.Original do documento (Qt.Faturamento original)
					if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
						jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
					}
					
					//Quantidade SCE
//					if(jsonEaa0103.getString("umv") == 'CX'){
//						if(jsonAbm0101.getBigDecimal_Zero("cvdnf") != null){
//							eaa0103.eaa0103qtUso = jsonEaa0103.getBigDecimal_Zero("qt_convertida") * jsonAbm0101.getBigDecimal_Zero("cvdnf");
//						}
//					}
//					if(jsonEaa0103.getString("umv") == 'KG'){
//						eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;//jsonEaa0103.getBigDecimal_Zero("qt_convertida")
//					}
//					if(jsonEaa0103.getString("umv") != 'CX' && jsonEaa0103.getString("umv") != 'KG'){
//						eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;//jsonEaa0103.getBigDecimal_Zero("qt_convertida");
//					}

					eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;
					
					
					
					if(jsonEaa0103.getString("umv") == 'CX'){
					// Peso Bruto
					jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtComl * abm01.abm01pesoBruto).round(3));
	
					// Peso Líquido
						jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtComl * abm01.abm01pesoLiq).round(3));
					}else{
						// Peso Bruto
						jsonEaa0103.put("peso_bruto", (jsonEaa0103.getBigDecimal_Zero("qt_convertida") * abm01.abm01pesoBruto).round(3));
		
						// Peso Líquido
						jsonEaa0103.put("peso_liquido", (jsonEaa0103.getBigDecimal_Zero("qt_convertida") * abm01.abm01pesoLiq).round(3));
					}
					
	
					def taraTotal = (jsonAbm0101.getBigDecimal_Zero("cvdnf")  * jsonAbm0101.getBigDecimal_Zero("tara_emb_")) + jsonAbm0101.getBigDecimal_Zero("tara_caixa") 
					
					// Peso Bruto Para Itens em KG
					if(jsonEaa0103.getString("umv") == 'KG'){
						jsonEaa0103.put("peso_bruto", jsonEaa0103.getBigDecimal_Zero("qt_convertida") + taraTotal )
					}
					// Peso Líquido Para Itens em KG
					if(jsonEaa0103.getString("umv") == 'KG'){
						//def taraTotal = jsonAbm0101.getBigDecimal_Zero("cvdnf") +  jsonAbm0101.getBigDecimal_Zero("tara_emb") + jsonAbm0101.getBigDecimal_Zero("tara_caixa");
						jsonEaa0103.put("peso_liquido", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
					}
					
					// *** Processa VALORES
					//Verifica se a Unidade de Medida é caixa então calcula o valor da caixa
					if(jsonEaa0103.getBigDecimal_Zero("tabunit") == 0){
						jsonEaa0103.put("tabunit", eaa0103.eaa0103unit ); 
					}
					
					// Unitario Convertido
					if(jsonEaa0103.getString("umv") == 'CX'){
						def valrUnit = eaa0103.eaa0103unit;
						def fatorUM = jsonAbm0101.getBigDecimal_Zero("volume_caixa");
						jsonEaa0103.put("unitario_conv", jsonEaa0103.getBigDecimal_Zero("tabunit") * fatorUM);
					}else{
						jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
					}
					// Quantidade de Caixas
					if(abm01codigo == '2001001' || abm01.abm01codigo == '2001002' || abm01.abm01codigo == '2001008' || abm01.abm01codigo == '2001009'){
						if(jsonEaa0103.getString("umv") == 'UN'){
							if(jsonEaa0103.getBigDecimal_Zero("cvdnf") != 0){
								jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
								def caixa = jsonEaa0103.getBigDecimal_Zero("caixa");
								def numCaixaRound = new BigDecimal(caixa).setScale(0,BigDecimal.ROUND_UP);
								jsonEaa0103.put("caixa", numCaixaRound);	
							}
						}
					}
					
					
					//Quantidade de Caixa e Frasco (Unidade Medida CX)
					if(jsonEaa0103.getString("umv") == 'CX'){
						jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida") )
					}
					
					// Quantidade de caixa e frasco (Unidade de Medida KG)
					if(jsonEaa0103.getString("umv") == 'KG'){
						jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("peso_caixa"));
						def vol = jsonEaa0103.getBigDecimal_Zero("caixa");
						BigDecimal volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
						jsonEaa0103.put("caixa", volume);
					}
					
					
					
					//Desconto unitário
					if (jsonEaa0103.getString("umv") == 'CX'){
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf")){
							jsonEaa0103.put("desconto",jsonEaa0103.getBigDecimal_Zero("desc_unit") * jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}
					}else{
						jsonEaa0103.put("desconto", (( jsonEaa0103.getBigDecimal_Zero("desc_unit") * eaa0103.eaa0103qtComl)).round(2));
					}
					
					
					// Total do item
					eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

					//Total Item Convertido
					jsonEaa0103.put("total_conv", (jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")).round(2))
				

									
					//================================
					//******       ICMS         ******
					//================================
					
					def vlrReducao = 0;
					//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
					jsonEaa0103.put("bc_icms", eaa0103.eaa0103total +
										  jsonEaa0103.getBigDecimal_Zero("frete_dest") +
									       jsonEaa0103.getBigDecimal_Zero("seguro") +
										  jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
									       jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
					// Tratar redução da base de cáulculo
					// % Reduc BC ICMS = % reduc BC ICMS do ítem
					if(abe01.abe01contribIcms == 1 ){
						if (jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms") != 0) {
							jsonEaa0103.put("_red_bc_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms"));
						}
					}
					
					// Calculo da Redução 
					if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){
						vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_red_bc_icms")) / 100).round(2);
						jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao);
					}
					
					// Obter a Aliquota de ICMS 
					if(jsonEaa0103.getBigDecimal_Zero("_icms") == 0){
						if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms") != 0){
							//Alíquota padrão de ICMS para operações internas (ENTIDADE)
							jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
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
						if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 || jsonAbe01.getBigDecimal_Zero("calcula_st") == 2){
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
								
								if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 || jsonAbe01.getBigDecimal_Zero("calcula_st") == 2){
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
								jsonEaa0103.put("bc_icms_st", 0.000000);
								jsonEaa0103.put("icms_st", 0.000000);
								jsonEaa0103.put("_icms_st", 0.000000);
							
							}
							
							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 ){

								jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
								
								jsonEaa0103.put("icms_fcp_", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
								jsonEaa0103.put("vlr_icms_fcp_", (jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp_") / 100).round(2));
							}

							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st") == 0){
								jsonEaa0103.put("bc_icms_st", 0.000000);
							}
						}

						// Define Tx. Iva do Item
						if(jsonEaa0103.getBigDecimal_Zero("bc_icms_st") > 0){
							jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st"))
						}else{
							jsonEaa0103.put("tx_iva_st", 0.000000)
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
					eaa0103.eaa0103totDoc =  eaa0103.eaa0103total + 
										jsonEaa0103.getBigDecimal_Zero("ipi") +
										jsonEaa0103.getBigDecimal_Zero("frete_dest") +
										jsonEaa0103.getBigDecimal_Zero("seguro") +
										jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
										jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") +
										jsonEaa0103.getBigDecimal_Zero("icms_st") -
										jsonEaa0103.getBigDecimal_Zero("desconto");
										
					eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);
					
					//==========================================================================//
					//         Zona Franca / Área de Livre Comércio e Amazônia Ocidental        //
					//==========================================================================//
					def alc = jsonAag0201Ent.getInteger("munic_alc");
					def zfm = jsonAag0201Ent.getInteger("munic_zfm");
					if(alc == null ) alc = 0;
					if(zfm == null ) zfm = 0;
					
					//ALC: 2 -Zona Franca de Manaus ou 1 - Área de Livre Comércio
					if (alc == 1 || zfm == 1) {
					
						//ICMS
						jsonEaa0103.put("icms", 0);
						jsonEaa0103.put("bc_icms", 0); 
						jsonEaa0103.put("_icms", 0); 
						
						//Obtendo CFOP
						eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6109"));
						
						//Obtendo CST de ICMS 
						eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "040"));
						
						//ICMS como Desconto
						jsonEaa0103.put("desc_icms_z_franca", (eaa0103.eaa0103totDoc * jsonAag02Ent.getBigDecimal_Zero("txicmsaida")) / 100 );
						
						//Total Documento
						eaa0103.eaa0103totDoc = 	eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
						jsonEaa0103.getBigDecimal_Zero("seguro") +
						jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
						jsonEaa0103.getBigDecimal_Zero("desc_icms_z_franca");
						//ICMS Isentas
						jsonEaa0103.put("icms_isento", eaa0103.eaa0103totDoc);	
					}
					
					// Ajusta o valor do IPI Outras
					jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);
					
					//Valor do Financeiro
					eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  
					
					//Calculo de base de calculo comissão com desconto condicional
					
					//BC Comissão
					def txdesc = 0;
					def vlrtxdesc = 0;   
					txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
					vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
					jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc); 
					
					//Volume por Caixa 
					if(jsonEaa0103.getString("umv") == 'CX'){
						jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));
					}
					
					//Volume
					if(jsonEaa0103.getString("umv") == 'UN'){
						jsonEaa0103.put("volumes",  jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						def vol = jsonEaa0103.getBigDecimal_Zero("volumes");
						BigDecimal volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
						jsonEaa0103.put("volumes", volume);
					}
					// Volume Fat. Kilo
					if(jsonAbm0101.getBigDecimal_Zero("peso_caixa") != null){
						if(jsonEaa0103.getString("umv") == 'KG'){
							jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("peso_caixa"));
							def vol = jsonEaa0103.getBigDecimal_Zero("volumes");
							BigDecimal volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
							jsonEaa0103.put("volumes", volume);
						}
					}
					// Volume Fat. Frasco
					if(jsonEaa0103.getString("umv") != 'UN' && jsonEaa0103.getString("umv") != 'CX' && jsonEaa0103.getString("umv") != 'KG'){
						jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
					}

					//*******Calculo para SPED ICMS*******

					//BC ICMS SPED = BC ICMS 
					jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
		
					//Aliq ICMS SPED = Aliq ICMS
					jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("_icms"));
		
		
					//Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
					jsonEaa0103.put("_red_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("_red_bc_icms"));
		
					//ICMS Outras SPED = ICMS Outras
					jsonEaa0103.put("icms_outras_sped", jsonEaa0103.getBigDecimal_Zero("icms_outras"));
		
					//ICMS Isento SPED = ICMS Isento
					jsonEaa0103.put("icms_isento_sped", jsonEaa0103.getBigDecimal_Zero("icms_isento"));
		
		
					//ICMS SPED = ICMS
					jsonEaa0103.put("icms_sped", jsonEaa0103.getBigDecimal_Zero("icms"));
		
		
					//*******Calculo para SPED ICMS ST*******
		
					//BC ICMS ST SPED = BC ICMS ST
					jsonEaa0103.put("bc_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
		
					//Aliq ICMS ST SPED = Aliq ICMS ST
					jsonEaa0103.put("_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("_icms_st"));
		
					//ICMS ST SPED = ICMS ST
					jsonEaa0103.put("icms_st_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));
		
		
		
					//*******Calculo para SPED IPI*******
		
					//BC IPI SPED = BC IPI
					jsonEaa0103.put("bc_ipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));
		
					//Aliq IPI SPED = Aliq IPI
					jsonEaa0103.put("_ipi_sped", jsonEaa0103.getBigDecimal_Zero("_ipi"));
		
					//IPI Outras SPED = IPI Outras
					jsonEaa0103.put("ipi_outras_sped", jsonEaa0103.getBigDecimal_Zero("ipi_outras"));
		
					//IPI Isento SPED = IPI Isento
					jsonEaa0103.put("ipi_isento_sped", jsonEaa0103.getBigDecimal_Zero("ipi_isento"));
		
					//IPI SPED = IPI
					jsonEaa0103.put("ipi_sped", jsonEaa0103.getBigDecimal_Zero("ipi"));
					
				}
	
				if(grupo == "Leite"){
					
					//Define Quantidade Comercial como Quantidade Convertida
					jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
					
					//Unidade Medida Venda 
					jsonEaa0103.put("umv",aam06.aam06codigo);
					
					//Troca a Unidade de medida de acordo com o parâmetro na entidade
					if(jsonAbe01.getBigDecimal_Zero("unidade_caixa") == 1 && jsonEaa0103.getString("umv") == 'FR'){
						jsonEaa0103.put("umv", 'CX');
						jsonEaa0103.put("umf", 'CX');
						eaa0103.eaa0103umu == 'CX';
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf") > 0){
							jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}else{
							throw new ValidacaoException("Capacidade volumétrica no cadastro do item Inválida! Necessário um valor maior que Zero!");
						}	
					}
					
					
					//Define o preço unitário de acordo com a Tabela de Preço
					//if(jsonEaa0103.getBigDecimal_Zero("calculado") == 0){
					if(eaa0103.eaa0103unit == 0){
						if(eaa01.eaa01tp != null){
							if(jsonAbe4001 != null){
								if(jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0){
									
									DateTimeFormatter formato2 = DateTimeFormatter.ofPattern("yyyyMMdd"); 
									LocalDate dataPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_fin"), formato2);
									LocalDate dataAtual = LocalDate.now();
									def precoPromocao = jsonAbe4001.getBigDecimal_Zero("preco_promocao");
									if(dataPromo > dataAtual){
										eaa0103.eaa0103unit = precoPromocao;
									}else{
										eaa0103.eaa0103unit = abe4001.abe4001preco
									}
								}else{
									eaa0103.eaa0103unit = abe4001.abe4001preco
								}	
							}else{
								eaa0103.eaa0103unit = abe4001.abe4001preco
							}
						}
					}
						//jsonEaa0103.put("calculado", 1);
					//}
					
					//Define o Campo de Unitário para Estoque
					jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);
					
					// *** Processa VALORES
					//Verifica se a Unidade de Medida é caixa então calcula o valor da caixa
					if(jsonEaa0103.getBigDecimal_Zero("tabunit") == 0){
						jsonEaa0103.put("tabunit", eaa0103.eaa0103unit ); 
					}
					
					if(jsonEaa0103.getString("umv") == 'CX'){
						def valrUnit = eaa0103.eaa0103unit;
						def fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf");
						eaa0103.eaa0103unit = (jsonEaa0103.getBigDecimal_Zero("tabunit") * fatorUM);
					}
					
					//Verifica a Mensagem do IVA no cadastro do Ítem
					if(jsonAbm1001_UF_Item.getString("mensagem") != null){
						jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
					}
					
					//Qtd de Caixa e Frasco
					if(jsonAbm0101.getBigDecimal_Zero("cvdnf") != 0){
						if(jsonEaa0103.getString("umv") == 'CX'){
							jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
						}else{
							def int fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf");
							def int qtd = jsonEaa0103.getBigDecimal_Zero("qt_convertida");
							def int caixa;
							def int frasco;
							
							caixa =  qtd / fatorUM;
							frasco = qtd % fatorUM;
							
							jsonEaa0103.put("caixa", caixa );
							jsonEaa0103.put("frasco", frasco );
						}
					}
					if(abm01.abm01codigo == "0101002"){
						jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("volume_caixa"));
						jsonEaa0103.put("frasco", 0);
					}
					
					
					//Desconto unitário
					if (jsonEaa0103.getString("umv") == 'CX'){
						if(jsonAbm0101.getBigDecimal_Zero("cvdnf")){
							jsonEaa0103.put("desconto",jsonEaa0103.getBigDecimal_Zero("desc_unit") * jsonAbm0101.getBigDecimal_Zero("cvdnf"));
						}
					}else{
						jsonEaa0103.put("desconto", (( jsonEaa0103.getBigDecimal_Zero("desc_unit") * eaa0103.eaa0103qtComl)).round(2));
					}
					
					//QtdeSCE(2) = QuantidadeVenda(1)
					// Converte Qt.Documento para Qtde SCE
					if(jsonEaa0103.getString("umv") == 'CX'){
						eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl * jsonAbm0101.getBigDecimal_Zero("cvdnf"); 	
					}else{
						eaa0103.eaa0103qtUso = jsonEaa0103.getBigDecimal_Zero("qt_convertida"); 
					}
					
					//Volumes
					jsonEaa0103.put("volumes",jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
					
					//Volume Leite Garrafa
					if(abm01.abm01codigo == "0101002"){
						jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("volume_caixa"));
						
						def vol = jsonEaa0103.getBigDecimal_Zero("volumes");
						
						BigDecimal volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
						
						jsonEaa0103.put("volumes", volume);
					}
					
					// Peso Bruto
					if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));
					
					// Peso Líquido
					if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
					
					//TotalItem = QuantidadeVenda * PreçoUnit
					eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;

					//Total Item Convertido
					jsonEaa0103.put("total_conv", (jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")).round(2))
				
					//TotalDocumento = TotalItem
					eaa0103.eaa0103totDoc = eaa0103.eaa0103total;

					///if(!dentroEstado){
						def vlrReducao = 0;
						//BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
						jsonEaa0103.put("bc_icms", eaa0103.eaa0103total + 
						jsonEaa0103.getBigDecimal_Zero("frete_dest") +
						jsonEaa0103.getBigDecimal_Zero("seguro") +
						jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
						jsonEaa0103.getBigDecimal_Zero("vlr_desc"));
	
	//					aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
	//					eaa0103.eaa0103cstIcms = aaj10_cstIcms;
	
						// Tratar redução da base de cálculo
						// % Reduc BC ICMS (25) = % Reduc BC ICMS do ítem
						if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0){
							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms") != 0){
								jsonEaa0103.put("_red_bc_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms"));
							}
						}
	
						// Obter a Aliquota de ICMS 
						if(jsonEaa0103.getBigDecimal_Zero("_icms") == 0){
							if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms") != 0){
								//Alíquota padrão de ICMS para operações internas (ENTIDADE)
								jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
							}
						}
						// Calcular valor do ICMS e Valor ICMS Isento
						if(jsonEaa0103.getBigDecimal_Zero("_icms") < 0){ // Aliquota menor que zero = Isento
							jsonEaa0103.put("icms", 0.000000);
							jsonEaa0103.put("icms_outras", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
							jsonEaa0103.put("bc_icms", 0.000000 );
							jsonEaa0103.put("_red_bc_icms", 0.000000);
							jsonEaa0103.put("icms_isento", 0.000000);
							vlrReducao = 0;
						}else{
							jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));
						}
					//}else{
					//	jsonEaa0103.put("bc_icms",0.000000);
					//	jsonEaa0103.put("_red_bc_icms",0.000000);
					//	jsonEaa0103.put("_icms",0.000000);
					//	jsonEaa0103.put("icms", 0.000000);
					//	jsonEaa0103.put("icms_outras",0.000000);
					//	jsonEaa0103.put("bc_icms", 0.000000 );
					//	jsonEaa0103.put("bc_icms_st", 0.000000 );
					//	jsonEaa0103.put("_icms_st", 0.000000);
					//	jsonEaa0103.put("icms_st", 0.000000 );
					//	jsonEaa0103.put("_red_bc_icms", 0.000000);
					//	jsonEaa0103.put("icms_isento", 0.0000000);
						
					//}
					
					//Aliquota dos tributos no cadastro de NCM
					def sql = "select abg01camposcustom from abg01 "+
							"inner join abm0101 on abm0101ncm = abg01id "+
							"inner join abm01 on abm01id = abm0101item "+
							"where abm01id = :abm01id "+
							"AND abm01tipo = :eaa0103tipo "+
							"AND abm0101empresa = :aac10id";
					def aliq;
					TableMap abg01camposcustom = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id",abm01.abm01id), Parametro.criar("eaa0103tipo", eaa0103.eaa0103tipo), Parametro.criar("aac10id",aac10.aac10id)).getTableMap("abg01camposcustom");
					
					if(abg01camposcustom != null){
						aliq = abg01camposcustom.getBigDecimal_Zero("_carga_trib");
					}
					
					
					//Alíquota Carga Tributária
					def consfinal = jsonAbe01.getBigDecimal_Zero("consufinal");
					def aliqtrib = aliq;
					if(aliqtrib == 0 && consfinal == 1){
						throw new ValidacaoException("Informe a alíquota no NCM, para cálculo da Carga Tributária.");
					}else{
						if(jsonEaa0103.getBigDecimal_Zero("carga_trib_") == 0){
							jsonEaa0103.put("carga_trib_", aliqtrib);
						}
					
					}
					
					//BC Carga Tributaria
					jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total );
					
					//Carga Tributaria 
					if(jsonAbe01.getBigDecimal_Zero("consufinal") == 0){
						jsonEaa0103.put("bc_carga_trib", 0.000000);
						jsonEaa0103.put("carga_trib_", 0.000000);
						jsonEaa0103.put("carga_trib", 0.000000);
					}else{
						jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);
					}

					// Calcula Difal dos Itens
					calcularDifal();
					
					//IcmsOutras(28) = TotalDocumento(10)
					jsonEaa0103.put("icms_outras", eaa0103.eaa0103totDoc);
					
					//OutrasIPI(22) = TotalDocumento(10)
					jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);
					
					//Verifica se o ítem soma no total do Ítem da NFe
					jsonEaa0103.put("soma_nfe", 1);
					
					//Verifica se o PCD gera financeiro e cria valor no campo Total Financ.
					eaa0103.eaa0103totFinanc =  eaa0103.eaa0103totDoc
					
					//Calculo de Frete
					if(eaa0102.eaa0102despacho != null){
						String codDesp = eaa0102.eaa0102despacho.abe01id;
						def itemId = abm01.abm01id;
						
						Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("001","aba2001ent = '" + codDesp + "' and aba2001item = '" + itemId + "'");
						
						def valorFrete = aba2001?.getAba2001json()?.get("vlr_frete_transp") ?: 0;
						jsonEaa0103.put("frete_item", eaa0103.eaa0103qtComl * valorFrete)
					}else{
						jsonEaa0103.put("frete_item", 0.000000);
					}
					
					//Diferimento
					
					if (eaa0103.eaa0103cstIcms != null) {
						def cst = aaj10_cstIcms.aaj10codigo;
						
						//CST x51 - Mercadoria com diferimento
						if ( aaj10_cstIcms.aaj10codigo.substring(1) == "51") {
							jsonEaa0103.put("_diferimento", jsonAbm0101.getBigDecimal_Zero("_diferimento"));
						}else{
							jsonEaa0103.put("_diferimento", 0.000000);
						}
					}
					
					//Calculo de base de calculo comissão com desconto condicional
					
					//BC Comissão
					def txdesc = 0;
					def vlrtxdesc = 0;   
					txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
					vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
					jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc); 

				

					//*******Calculo para SPED ICMS*******

					//BC ICMS SPED = BC ICMS 
					jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
		
					//Aliq ICMS SPED = Aliq ICMS
					jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("_icms"));
		
		
					//Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
					jsonEaa0103.put("_red_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("_red_bc_icms"));
		
					//ICMS Outras SPED = ICMS Outras
					jsonEaa0103.put("icms_outras_sped", jsonEaa0103.getBigDecimal_Zero("icms_outras"));
		
					//ICMS Isento SPED = ICMS Isento
					jsonEaa0103.put("icms_isento_sped", jsonEaa0103.getBigDecimal_Zero("icms_isento"));
		
		
					//ICMS SPED = ICMS
					jsonEaa0103.put("icms_sped", jsonEaa0103.getBigDecimal_Zero("icms"));
		
		
					//*******Calculo para SPED ICMS ST*******
		
					//BC ICMS ST SPED = BC ICMS ST
					jsonEaa0103.put("bc_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
		
					//Aliq ICMS ST SPED = Aliq ICMS ST
					jsonEaa0103.put("_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("_icms_st"));
		
					//ICMS ST SPED = ICMS ST
					jsonEaa0103.put("icms_st_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));
		
		
		
					//*******Calculo para SPED IPI*******
		
					//BC IPI SPED = BC IPI
					jsonEaa0103.put("bc_ipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));
		
					//Aliq IPI SPED = Aliq IPI
					jsonEaa0103.put("_ipi_sped", jsonEaa0103.getBigDecimal_Zero("_ipi"));
		
					//IPI Outras SPED = IPI Outras
					jsonEaa0103.put("ipi_outras_sped", jsonEaa0103.getBigDecimal_Zero("ipi_outras"));
		
					//IPI Isento SPED = IPI Isento
					jsonEaa0103.put("ipi_isento_sped", jsonEaa0103.getBigDecimal_Zero("ipi_isento"));
		
					//IPI SPED = IPI
					jsonEaa0103.put("ipi_sped", jsonEaa0103.getBigDecimal_Zero("ipi"));
				}

				// Define o CFOP dos Itens
				// Calcula CFOP dos itens
			if (eaa0103.eaa0103cfop != null) {
							
				def cfop = "5910"
				eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop))
				
		
				def primeiroDigito = cfop.substring(0,1);
		
				if(!dentroEstado){
					
					if(primeiroDigito == "5"){
						primeiroDigito = "6";
					}
		
					cfop = primeiroDigito + cfop.substring(1);
					aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
					eaa0103.eaa0103cfop = aaj15_cfop;
		
				}	
			}
			
		}
		// Calcula Difal dos itens
				calcularDifal();	
	}

	

	private	LocalDate calcularDataEntrega(){

		Integer prazoEntrega = jsonAag0201Ent.getInteger("prazo");
		String diaFaturamento = jsonAag0201Ent.getString("dia_faturamento");
		LocalDate dataAtual = LocalDate.now()
		String diaSemanaSistema = buscarDiaSemana(dataAtual.getDayOfWeek().toString());
		Integer mesmoDia = jsonAag02Ent.getInteger("mesmo_dia");
	
		diaFaturamento = formatarString(diaFaturamento.toUpperCase())
	
		if(prazoEntrega == null) throw new ValidacaoException("Necessário informar o prazo de entrega no município " + municipioEnt.aag0201nome);
	
		if(mesmoDia == null) throw new ValidacaoException("Necessário preencher o campo 'Captação Mesmo Dia Faturamento' no estado " + ufEnt.aag02uf);
	
		List<LocalDate> feriados = buscarFeriados()
	
		if(!diaFaturamento.toUpperCase().contains("DOMINGO") && !diaFaturamento.toUpperCase().contains("SEGUNDA") && !diaFaturamento.toUpperCase().contains("TERCA") && !diaFaturamento.toUpperCase().contains("QUARTA") &&
					!diaFaturamento.toUpperCase().contains("QUINTA") && !diaFaturamento.toUpperCase().contains("SEXTA") && !diaFaturamento.toUpperCase().contains("SABADO")){
			throw new ValidacaoException("Dia de faturamento "+ diaFaturamento.toUpperCase() +"cadastrado no municipio " + municipioEnt.aag0201nome + " não é válidado ")
		}
	
		LocalDate data;
	
		if(diaFaturamento.toUpperCase().contains(diaSemanaSistema.toUpperCase())){
			if(mesmoDia == 0) dataAtual = dataAtual.plusDays(7);
			data = somarDiasUteis(dataAtual, prazoEntrega, feriados)
		}else{
			LocalDate dataAux = dataAtual;
			String diaSemama = diaSemanaSistema;
	
			Integer count = 0;
			while(!diaFaturamento.toUpperCase().contains(diaSemama.toUpperCase()) ){
				dataAux = dataAux.plusDays(1)
				diaSemama = buscarDiaSemana(dataAux.getDayOfWeek().toString());
	
				count++
	
				if(count > 20) throw new ValidacaoException("Processo interrompido para não consumir aumentar processamento! Verificar formula de data de entrega programada.")
			}
	
			data = somarDiasUteis(dataAux, prazoEntrega, feriados);
		}
	
		return data;
	}
	
	private String buscarDiaSemana(String diaSemana){
		switch (diaSemana){
			case "SUNDAY":
				return "Domingo";
				break;
			case "MONDAY":
				return "Segunda";
				break;
			case "TUESDAY":
				return "Terca";
				break;
			case "WEDNESDAY":
				return "Quarta";
				break;
			case "THURSDAY":
				return "Quinta";
				break;
			case "FRIDAY":
				return "Sexta";
				break;
			default:
				return "Sabado";
				break;
		}
	}
	
	private LocalDate somarDiasUteis(LocalDate dataAtual, Integer prazoEntrega, List<LocalDate> feriados){
		LocalDate data = dataAtual;
		Integer diasSomados = 0;
		while(diasSomados < prazoEntrega){
			data = data.plusDays(1);
			if(data.getDayOfWeek() != DayOfWeek.SATURDAY && data.getDayOfWeek() != DayOfWeek.SUNDAY
					&& !feriados.contains(data)){
				diasSomados++
			}
		}
		return data;
	}
	
	private String formatarString(String semana){
	
		if(semana.contains("Ã")){
			semana = semana.replace("Ã","A");
		}
		if(semana.contains("Á")){
			semana = semana.replace("Á","A");
		}
	
		if(semana.contains("À")){
			semana = semana.replace("À","A");
		}
	
		if(semana.contains("Â")){
			semana = semana.replace("Â","A");
		}
		if(semana.contains("É")){
			semana = semana.replace("É","E");
		}
		if(semana.contains("Ê")){
			semana = semana.replace("Ê","E");
		}
		if(semana.contains("Í")){
			semana = semana.replace("Í","I");
		}
		if(semana.contains("Õ")){
			semana = semana.replace("Õ","O");
		}
		if(semana.contains("Ô")){
			semana = semana.replace("Ô","O");
		}
		if(semana.contains("Ó")){
			semana = semana.replace("Ó","O");
		}
		if(semana.contains("Ú")){
			semana = semana.replace("Ú","U");
		}
		if(semana.contains("Ü")){
			semana = semana.replace("Ü","U");
		}
		if(semana.contains("Ç")){
			semana = semana.replace("Ç","C");
		}
	
		return semana;
	
	}
	
	private List<LocalDate> buscarFeriados(){
		List<LocalDate> feriados = new ArrayList<>();
	
		String sqlFeriados = "select cast(aba2001json ->> 'data' as date) as data from aba20 " +
							"inner join aba2001 on aba2001rd = aba20id " +
							"where aba20codigo = '026'"
	
		List<TableMap> tmFeriado = getAcessoAoBanco().buscarListaDeTableMap(sqlFeriados)
	
		if(tmFeriado.size() == 0) throw new ValidacaoException("Não foi encontrado repositório de dados para cálculo da data de entrega ou não foi informado lista de feriados no repositório de dados.")
	
		for(feriado in tmFeriado){
			feriados.add(feriado.getDate("data"));
		}
	
		return feriados;
	}

	private void defineDataEntregaItens(){
		LocalDate dataEntrega;
	
		if(jsonAag0201Ent.getInteger("prazo") == null) throw new ValidacaoException("Necessário informar o prazo de entrega no município " + municipioEnt.aag0201nome)
	
		if(abb01.abb01operAutor != "SRF" && abb01.abb01operAutor != "SRF1002"){ 
			if(eaa0103.eaa0103dtEntrega == null){
				if(jsonAag0201Ent.getString("dia_faturamento") != null){
					dataEntrega = calcularDataEntrega();
	
					eaa0103.eaa0103dtEntrega = dataEntrega;
				}else{
	
					List<LocalDate> feriados = buscarFeriados()
	
					dataEntrega = somarDiasUteis(LocalDate.now(), jsonAag0201Ent.getInteger("prazo"), feriados)
	
					eaa0103.eaa0103dtEntrega = dataEntrega;
				}
			}
	
		}else{     // Faturamento
			List<LocalDate> feriados = buscarFeriados()
	
			dataEntrega = somarDiasUteis(abb01.abb01data, jsonAag0201Ent.getInteger("prazo"), feriados)
	
			eaa0103.eaa0103dtEntrega = dataEntrega;
		}
	
	}
	private calcularDifal(){
		//======================================
		//************* DIFAL *****************
		//======================================
		
		if(!dentroEstado){
				if(ufEnt.aag02uf == 'RJ'){
					if(jsonEaa0103.getBigDecimal_Zero("bc_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0){
						// Aliquota fundo combate a pobreza (Rio de Janeiro)
						jsonEaa0103.put("icms_fcp_",2);
						
						//Calculo Fundo de Combate a Pobreza 
						jsonEaa0103.put("vlr_fcp_difal_", jsonEaa0103.getBigDecimal_Zero("venda_liquida") * jsonEaa0103.getBigDecimal_Zero("icms_st_rep_ded") / 100);
					}else{
						// Aliquota fundo combate a pobreza (Rio de Janeiro)
						jsonEaa0103.put("icms_fcp_",0.000000);
						
						//Calculo Fundo de Combate a Pobreza 
						jsonEaa0103.put("vlr_fcp_difal_",0.000000);	
					}		
				}
				
				if(ufEnt.aag02uf == 'AL'){
					// Aliquota fundo combate a pobreza (Alagoas)
					jsonEaa0103.put("icms_fcp_", 1);
					
					//Calculo Fundo de Combate a Pobreza 
					jsonEaa0103.put("vlr_fcp_difal_", jsonEaa0103.getBigDecimal_Zero("bc_icms_dest") * jsonEaa0103.getBigDecimal_Zero("icms_st_rep_ded") / 100);
				}
			
				if(abe01.abe01contribIcms == 0){
					//Diferencial de Alíquota
					def difDestino = 0 
					def difOrigem = 0
					
					// Bc de ICMS Destino
					jsonEaa0103.put("bc_icms_dest", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
					
					// Aliquota de ICMS Destino
					jsonEaa0103.put("_icms_dest", jsonAag02Ent.getBigDecimal_Zero("txicminterna"));
					
					// Teste ICMS Part
					jsonEaa0103.put("icms_inter_part",100);
					
					// Diferencial de Aliquota 
					def difAliq = jsonEaa0103.getBigDecimal_Zero("_icms_dest") - jsonEaa0103.getBigDecimal_Zero("_icms");
					
					if(difAliq < 0 ){
						difAliq = (difAliq * -1);
					}
					
					difDestino = jsonEaa0103.getBigDecimal_Zero("bc_icms_dest");
					jsonEaa0103.put("_dif_dest", 100);
					
					// Valor Diferencial Origem
					jsonEaa0103.put("vlr_icms_origem", 0.000000 );
					
					// Valor diferencial aliquota destino 
					jsonEaa0103.put("vlr_icms_dest", jsonEaa0103.getBigDecimal_Zero("bc_icms_dest") * (difAliq / 100));
				
				}
			}
		}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==