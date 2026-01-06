package Atilatte.formulas.itensdocumento;
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
import sam.model.entities.aa.Aaj07;
import sam.model.entities.aa.Aaj09;
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


public class PedidoSImpostosICMSIsento extends FormulaBase {

	private Aac10 aac10;
	private Aag01 aag01;
	private Aag02 ufEnt;
	private Aag02 ufEmpr;
	private Aag0201 municipioEnt;
	private Aag0201 municipioEmpr;
    private Aaj07 aaj07;
    private Aaj09 aaj09;
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
	private TableMap jsonAbm0101;
	private TableMap jsonAag02Ent;
	private TableMap jsonAag0201Ent;
	private TableMap jsonAag02Empr;
	private TableMap jsonAac10;
	private TableMap jsonAbe4001;
	private TableMap jsonAbe02;
    private TableMap jsonAaj07clasTrib;


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
		
		if (eaa0102.eaa0102ti == 1 && eaa0102.eaa0102contribIcms == 1) {
			throw new ValidacaoException("A entidade informada é pessoa física e está caracterizada como contribuinte de ICMS.");
		}
		
		//Central de Documento
		abb01 = eaa01.eaa01central;
		
		//PCD
		abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);
		//if (abd01 != null && abd01.abd01es == 0)  throw new ValidacaoException("Esta fórmula poderá ser utilizada somente em documentos de saída.");
		
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

        // Class. Trib CBS/IBS
        aaj07 = eaa0103.eaa0103clasTribCbsIbs != null ? getSession().get(Aaj07.class, eaa0103.eaa0103clasTribCbsIbs.aaj07id) : null;
        if(aaj07 == null) throw new ValidacaoException("Necessário informar a Classificação tribtária de CBS/IBS do item: " + abm01.abm01codigo + " - " + abm01.abm01na);

        aaj09 = eaa0103.eaa0103cstCbsIbs != null ? getSession().get(Aaj09.class, eaa0103.eaa0103cstCbsIbs.aaj09id) : null;
        if(aaj09 == null) interromper("Necessário informar o CST de CBS/IBS no item: " + abm01.abm01codigo + " - " + abm01.abm01na);

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
        jsonAaj07clasTrib = aaj07.aaj07json != null ? aaj07.aaj07json : new TableMap();

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

		//Ajusta CFOP (Dentro ou Fora do Estado)
		if (eaa0103.eaa0103cfop != null) {
				def cfop = aaj15_cfop.aaj15codigo;
				
				def primeiroDigito = cfop.substring(0,1);
				if(!dentroEstado){
					if(primeiroDigito == "5"){
						primeiroDigito == "6";
					}
					if(primeiroDigito == "1"){
						primeiroDigito == "2";
					}
					cfop = primeiroDigito + cfop.substring(1);
					aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
					eaa0103.eaa0103cfop = aaj15_cfop;
				}
			}
		
		
		

		//=====================================
		// ******     Valores do Item     ******
		//=====================================
		if (eaa0103.eaa0103qtComl > 0 ) {

			// Define a data de entrega dos itens
			defineDataEntregaItens();

			//Define Quantidade Comercial como Quantidade Convertida
			jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);

			// Define Unitario convertido
			jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);

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
				
				BigDecimal volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP);
				
				jsonEaa0103.put("volumes", volume);
			}

			// Peso Bruto
			if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));

			// Peso Líquido
			if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));

			//TotalItem = QuantidadeVenda * PreçoUnit
			eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;
			
			//TotalDocumento = TotalItem
			eaa0103.eaa0103totDoc = eaa0103.eaa0103total;

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
	        	jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total);

	        	//Carga Tributaria 
			  if(jsonAbe01.getBigDecimal_Zero("consufinal") == 0){
			  	jsonEaa0103.put("bc_carga_trib", 0);
			  	jsonEaa0103.put("carga_trib_", 0);
			  	jsonEaa0103.put("carga_trib", 0);
			  }else{
			  	jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);
			  }

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
			    	jsonEaa0103.put("frete_item", eaa0103.eaa0103qtComl * valorFrete);
			}else{
				jsonEaa0103.put("frete_item", 0);
			}

			//Calculo de base de calculo comissão com desconto condicional
				
			//BC Comissão
			def txdesc = 0;
			def vlrtxdesc = 0;   
			txdesc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
			vlrtxdesc =  (eaa0103.eaa0103totFinanc * txdesc) / 100;
			jsonEaa0103.put("bc_comissao", eaa0103.eaa0103total - vlrtxdesc);

            calcularCBSIBS();


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
		}
	}
    private void calcularCBSIBS() {
        // *********************************************
        // ************ REFORMA TRIBUTÁRIA *************
        // *********************************************

        //================================
        //******  BASE DE CALCULO   ******
        //================================

        //(vProd + vServ + vFrete + vSeg + vOutro + vII) -
        // (vDesc - vPIS - vCOFINS - vICMS - vICMSUFDest - vFCP - vFCPUFDest - vICMSMono - vISSQN)
        /*VBCIS*/
        jsonEaa0103.put("is_bc", (eaa0103.eaa0103total +
                jsonEaa0103.getBigDecimal_Zero("total_servico") +
                jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                jsonEaa0103.getBigDecimal_Zero("seguro") +
                jsonEaa0103.getBigDecimal_Zero("outras")) -
                (jsonEaa0103.getBigDecimal_Zero("desconto") -
                        jsonEaa0103.getBigDecimal_Zero("pis") -
                        jsonEaa0103.getBigDecimal_Zero("cofins") -
                        jsonEaa0103.getBigDecimal_Zero("icms") -
                        jsonEaa0103.getBigDecimal_Zero("ufdest_icms") -
                        jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_")))


        //vProd + vServ + vFrete + vSeg + vOutro
        //VBC (CBS e IBS) - Base de Caculo CBS/IBS
        jsonEaa0103.put("cbs_ibs_bc", eaa0103.eaa0103total +
                jsonEaa0103.getBigDecimal_Zero("total_servico") +
                jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                jsonEaa0103.getBigDecimal_Zero("seguro") +
                jsonEaa0103.getBigDecimal_Zero("outras"))

        //================================
        //******       VALORES      ******
        //================================

        //AJUSTE DA COMPETENCIA (UB112)
        if (jsonAaj07clasTrib.getBoolean("ajuste_comp")) {
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))
            jsonEaa0103.put("vlr_cbs", (jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * jsonEaa0103.getBigDecimal_Zero("cbs_aliq")) / 100)
        }
        //CREDITO PRESUMIDO DA OPERAÇÃO(UB120)
        if (jsonAaj07clasTrib.getBoolean("cred_presumido")) {
            jsonEaa0103.put("cred_presumido", jsonEaa0103.getBigDecimal_Zero("aliq_credpresum") * jsonEaa0103.getBigDecimal_Zero("bc_credpresum"))
        }

        //CRÉDITO PRESUMIDO IBS ZONA FRANCA DE MANAUS
        if (jsonAaj07clasTrib.getBoolean("cred_pres_ibs_zfm")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //CST CBS/IBS
        if (jsonAaj07clasTrib.getString("cst_cbsibs")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //DESCRIÇÃO CST CBS/IBS
        if (jsonAaj07clasTrib.getString("desc_cstcbsibs")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //DIFERIMENTO CBS/IBS
        if (jsonAaj07clasTrib.getBoolean("dif_cbsibs")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //ESTORNO DE CRÉDITO
        if (jsonAaj07clasTrib.getBoolean("estorno_cred")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //MONOFÁSICA
        if (jsonAaj07clasTrib.getBoolean("monofasica_cbsibs")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }

        //REDUÇÃO BASE DE CÁLCULO
        if (jsonAaj07clasTrib.getBoolean("red_bc")) {

        }
        //REDUÇÃO BASE DE CÁLCULO CST
        if (jsonAaj07clasTrib.getBoolean("red_bc_cst")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //REDUÇÃO DE ALÍQUOTA
        if (jsonAaj07clasTrib.getBoolean("red_bc_aliq")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //TRANSFERÊNCIA DE CRÉDITO
        if (jsonAaj07clasTrib.getBoolean("transf_cred")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }
        //TRIBUTAÇÃO REGULAR
        if (jsonAaj07clasTrib.getBoolean("tributacao")) {
            jsonEaa0103.put("", 0.0) //VERIFICAR COMO SERÁ FEITO
        }

        // CBS
        jsonEaa0103.put("cbs_aliq", jsonAag02Ent.getBigDecimal_Zero("cbs_aliq"))//Alíquota CBS
        jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") / 100))

        // Aliquotas IBS
        jsonEaa0103.put("ibs_uf_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_uf_aliq"));//Alíquota IBS Estadual
        jsonEaa0103.put("ibs_mun_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_mun_aliq"));

        // IBS Municipio
        jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") / 100));

        //IBS
        jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") / 100))//IBS Estadual
        jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))// total de IBS

        //CST 200 - Tributação c/ Redução
        if(aaj09.aaj09codigo == "200"){
            //PERCENTUAL REDUÇÃO CBS
            if (jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_cbs")) {
                jsonEaa0103.put("perc_red_cbs", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_cbs"))
            }
            //PERCENTUAL REDUÇÃO IBS UF
            if (jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_uf")) {
                jsonEaa0103.put("perc_red_ibs_uf", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_uf")); // Mudar nome do campo
            }

            //PERCENTUAL DE REDUÇÃO IBS MUNIC
            if(jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_munic")){
                jsonEaa0103.put("perc_red_ibs_munic", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_munic")) // Criar campo
            }

            // Aliquotas Efetivas
            jsonEaa0103.put("aliq_efet_ibs_uf", (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_uf")) / 100)); // Mudar nome campo
            jsonEaa0103.put("aliq_efet_ibs_mun", (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_mun")) / 100));
            jsonEaa0103.put("aliq_efet_cbs", (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_cbs")) / 100));

            // CBS
            jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_cbs") / 100))

            // IBS Município
            jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_munic") / 100));

            // IBS UF
            jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_uf") / 100))//IBS Estadual

            // Soma total do IBS UF/Municipio
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))// total de IBS

        }

        if(jsonAaj07clasTrib.getInteger("exige_tributacao") == 0){ // Zera impostos caso não exige tributação
            jsonEaa0103.put("cbs_aliq", new BigDecimal(0));
            jsonEaa0103.put("vlr_cbs", new BigDecimal(0));
            jsonEaa0103.put("ibs_uf_aliq", new BigDecimal(0));
            jsonEaa0103.put("ibs_mun_aliq", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibsmun", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibsuf", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibs", new BigDecimal(0));
        }
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
			feriados.add(feriado);
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

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==