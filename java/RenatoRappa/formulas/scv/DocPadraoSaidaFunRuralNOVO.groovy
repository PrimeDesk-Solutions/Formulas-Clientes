package RenatoRappa.formulas.scv;

import sam.model.entities.ab.Abm1301;

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

public class DocPadraoSaidaFunRuralNOVO extends FormulaBase {

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
    private Abe02 abe02;
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
    aaj10_cstIcms = abm12.abm12cstIcms != null ? getSession().get(Aaj10.class, abm12.abm12cstIcms.aaj10id) : null;
  
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
    
        
        //=====================================
        // ******     Valores do Item     ******
        //=====================================
        if (eaa0103.eaa0103qtComl > 0) {

        	  // Define o CST de acordo com o cadastro do Item
        	  if(eaa0103.eaa0103cstIcms == null ) eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo",aaj10_cstIcms.aaj10codigo))

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

            //Determina se a operação é dentro ou fora do estado
            def dentroEstado = false;
            if (ufEmpr != null && ufEnt != null) {
                dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
            }

            
            //Troca CFOP (Dentro ou Fora do Estado)
            if (eaa0103.eaa0103cfop != null && jsonEaa0103.getBigDecimal_Zero("calculado") == 0 ) {
                def cfop = aaj15_cfop.aaj15codigo.substring(1);
                
                def primeiroDigito = aaj15_cfop.aaj15codigo.substring(0,1);
                if(!dentroEstado){
                    if(primeiroDigito == "5"){
                        primeiroDigito = "6";
                    }
                }
                cfop = primeiroDigito + cfop;
                aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
                eaa0103.eaa0103cfop = aaj15_cfop;
                jsonEaa0103.put("calculado", 1);
            }
            
            
            //Define o Campo de Unitário para Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

            jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
            
            // *** Processa QUANTIDADES
            // Conserva Qt.Original do documento (Qt.Faturamento original)
            if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
            }

            // Converte Qt.Faturamento para Qt.Venda
            if(abm13.abm13fcUV == null) throw new ValidacaoException("Fator de conversão para venda no cadastro do item "+abm01.abm01codigo+" é inválido.")
            jsonEaa0103.put("qt_documento", eaa0103.eaa0103qtComl * abm13.abm13fcUV)

            
            // Converte Qt.Documento para Qtde SCE
           if(abm13.abm13fcVU == null) throw new ValidacaoException("Fator de Conversão de Venda para uso no cadastro do item "+abm01.abm01codigo+ " é inválida")
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl * abm13.abm13fcVU;

            jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl)
            
            // Converte Qt.Documento para Volume
            jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("qt_documento") * abm13.abm13fcVW);

            // Peso Bruto
            if (abm01.abm01pesoLiq_Zero >= 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));
            
            // Peso Líquido
            if (abm01.abm01pesoBruto_Zero >= 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));

           
            // *** Processa Valores 
            //Desconto Incondicional
            if(jsonEaa0103.getBigDecimal_Zero("_desc_incond") < 0 ){
                jsonEaa0103.put("desconto",0);
            }

            if(jsonEaa0103.getBigDecimal_Zero("_desc_incond") > 0 && jsonEaa0103.getBigDecimal_Zero("desconto") > 0 ){
            	//throw new ValidacaoException("teste1")
                jsonEaa0103.put("desconto", jsonEaa0103.getBigDecimal_Zero("qt_documento") * eaa0103.eaa0103unit * jsonEaa0103.getBigDecimal_Zero("_desc_incond") / 100);
            }else if(jsonEaa0103.getBigDecimal_Zero("_desc_incond") > 0 && jsonEaa0103.getBigDecimal_Zero("desconto") == 0){
                 //throw new ValidacaoException("teste2")
                 jsonEaa0103.put("desconto", jsonEaa0103.getBigDecimal_Zero("qt_documento") * eaa0103.eaa0103unit * jsonEaa0103.getBigDecimal_Zero("_desc_incond") / 100);
            }else if(jsonEaa0103.getBigDecimal_Zero("_desc_incond") <= 0 && jsonEaa0103.getBigDecimal_Zero("desconto") > 0){
                //throw new ValidacaoException("teste3")
                jsonEaa0103.put("desc_unit", jsonEaa0103.getBigDecimal_Zero("desconto") /  (jsonEaa0103.getBigDecimal_Zero("qt_documento") * eaa0103.eaa0103unit) * 100);
            }else if(jsonEaa0103.getBigDecimal_Zero("_desc_incond") < 0){
                //throw new ValidacaoException("teste4")
                jsonEaa0103.put("desconto",0);
            }

            if(abe01.abe01ti == 1){
            	jsonEaa0103.put("desconto", 0);
            }

            // Total do item
            eaa0103.eaa0103total = (jsonEaa0103.getBigDecimal_Zero("qt_documento") * eaa0103.eaa0103unit);
            
           
            
            //================================
            //******       ICMS         ******
            //================================
            
            def vlrReducao = 0;
            def cdBCICMS = 0;
            def cdICMSIsento = 0;
            def cdICMS = 0;
                                    
            // Tratar redução da base de cálculo
            // % Reduc BC ICMS = % reduc BC ICMS do ítem
            if (jsonEaa0103.getBigDecimal_Zero("_reduc_bc_icms") == 0) {
                jsonEaa0103.put("_reduc_bc_icms", jsonAbm0101.getBigDecimal_Zero("_red_bc_icms")); //Aliquota fixa cadastro itens
            }

            if(jsonEaa0103.getBigDecimal_Zero("_reduc_bc_icms") == -1){
            	vlrReducao = 0;
            }

            //Se pessoas físicas ou não contribuintes - Não tem redução da base de cálculo
            if(abe01.abe01ti == 1 || contribICMS == 0){
            	jsonEaa0103.put("_reduc_bc_icms", -1);
            }
            
            
            // Calculo da Redução 
            if(jsonEaa0103.getBigDecimal_Zero("_reduc_bc_icms") > 0){
                vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_reduc_bc_icms")) / 100).round(2);
                jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao);
                jsonEaa0103.put("bc_icms",jsonEaa0103.getBigDecimal_Zero("bc_icms").round(2) );
            }
            
            // Calcular valor do ICMS e Valor ICMS Isento
            if(jsonEaa0103.getBigDecimal_Zero("_icms") < 0){ // Aliquota menor que zero = Isento
                jsonEaa0103.put("icms", 0);
                jsonEaa0103.put("icms_isento", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
                jsonEaa0103.put("bc_icms", 0 );
                jsonEaa0103.put("_reduc_bc_icms", 0);
                vlrReducao = 0; 
            }else{
                jsonEaa0103.put("icms", (jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms") / 100).round(2));
            }

            //Outras de ICMS
            if(jsonEaa0103.getBigDecimal_Zero("icms") == 0){
                jsonEaa0103.put("icms_outras", 0);
                jsonEaa0103.put("bc_icms",0);
                jsonEaa0103.put("icms_isento", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                                    jsonEaa0103.getBigDecimal_Zero("outras_despesas"));
            }


             //================================
            //******       PIS          ******
            //================================
            if (eaa0103.eaa0103cstPis != null) {
                if(jsonAbm0101.getBigDecimal_Zero("_pis") > 0){
                    //Base de Cálculo de PIS
                    jsonEaa0103.put("bc_pis", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") + jsonEaa0103.getBigDecimal_Zero("frete_dest") + jsonEaa0103.getBigDecimal_Zero("seguro"));
                    if(jsonEaa0103.getBigDecimal_Zero("_pis") == 0){
	                  jsonEaa0103.put("_pis", jsonAbm0101.getBigDecimal_Zero("_pis")); //Aliquota de PIS 
                    }

                    if(jsonEaa0103.getBigDecimal_Zero("_pis") <= 0){
                    	jsonEaa0103.put("bc_pis", 0);
                    	jsonEaa0103.put("pis", 0)
                    }else{
                    	jsonEaa0103.put("pis", jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("_pis")); 
                    }
                }               
            }
            
            //================================
            //******      COFINS        ******
            //================================
            if (eaa0103.eaa0103cstCofins != null) {
                if(jsonAbm0101.getBigDecimal_Zero("_cofins") > 0){
                    //Base de Cálculo de PIS
                    jsonEaa0103.put("bc_cofins", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") + jsonEaa0103.getBigDecimal_Zero("frete_dest") + jsonEaa0103.getBigDecimal_Zero("seguro"));
                    
                    if(jsonEaa0103.getBigDecimal_Zero("_cofins") == 0){
	                  jsonEaa0103.put("_cofins", jsonAbm0101.getBigDecimal_Zero("_cofins")); //Aliquota de COFINS
                    }
                    
                    if(jsonEaa0103.getBigDecimal_Zero("_cofins") <= 0){
                    	jsonEaa0103.put("bc_cofins", 0);
                    	jsonEaa0103.put("cofins", 0)
                    }else{
                    	jsonEaa0103.put("cofins", jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("_cofins")); 
                    }
                }
            }

            //BC de FunRural 
            jsonEaa0103.put("bc_fun_rural",eaa0103.eaa0103total);

            //Aliquota FunRural 
            if(jsonEaa0103.getBigDecimal_Zero("_fun_rural") == 0){
                jsonEaa0103.put("_fun_rural", 1.5);
            }

            //FunRural
            if(jsonEaa0103.getBigDecimal_Zero("_fun_rural") == 0 || jsonEaa0103.getBigDecimal_Zero("_fun_rural") == -1){
                jsonEaa0103.put("bc_fun_rural",0);
                jsonEaa0103.put("_fun_rural",0);
                jsonEaa0103.put("funrural",0);
            }else{
                 jsonEaa0103.put("funrural",(jsonEaa0103.getBigDecimal_Zero("bc_fun_rural") * jsonEaa0103.getBigDecimal_Zero("_fun_rural")) / 100);
            }
            

            //Total Doc sem ST
            //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
            eaa0103.eaa0103totDoc = eaa0103.eaa0103total +
                                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                                    jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                                    jsonEaa0103.getBigDecimal_Zero("desconto");
            
            //Valor do Financeiro
            eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  


            // Define o campo de unitário Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

           // Preenche quantidades convertidas (Utilizada nas formulas de saída da Atilatte)
		 jsonEaa0103.put("umv",aam06.aam06codigo);
		 jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
		 jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
		 jsonEaa0103.put("total_conv", eaa0103.eaa0103total);
            
           
            
            //*******Calculo para SPED ICMS*******
            
            //BC ICMS SPED = BC ICMS 
            jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
            
            //Aliq ICMS SPED = Aliq ICMS
            jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("_icms"));
            
            
            //Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
            jsonEaa0103.put("_reduc_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("_reduc_bc_icms"));
            
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
            
            
            
            
//          //=======================================
//          // ******** OUTRAS INFORMAÇÕES **********
//          //=======================================
//          if (aaj15_cfop != null) {
//          // Ajuste de classificação da receita de PIS/Cofins para saídas
//          if (aaj15_cfop.aaj15codigo.substring(1) == "901" || aaj15_cfop.aaj15codigo.substring(1) == "905" || aaj15_cfop.aaj15codigo.substring(1) == "910" || aaj15_cfop.aaj15codigo.substring(1) == "911" || 
//          aaj15_cfop.aaj15codigo.substring(1) == "913" || aaj15_cfop.aaj15codigo.substring(1) == "916" || aaj15_cfop.aaj15codigo.substring(1) == "925" || aaj15_cfop.aaj15codigo.substring(1) == "949") {
//          
//          eaa0103.eaa0103clasReceita = 0;
//          }
//          
//          // Para ITENS RETORNADOS, não considerar PESO nem VOLUME
//          if (aaj15_cfop.aaj15codigo.substring(1) == "902") {
//          jsonEaa0103.put("volumes", 0.00);
//          jsonEaa0103.put("peso_liquido", 0.0000);
//          jsonEaa0103.put("peso_bruto", 0.0000);
//          }
//          }
            
//          // ----------------------------------------------------------------
//          // GERAR VALOR APROXIMADO DE IMPOSTOS PARA VENDA A CONSUMIDOR FINAL
//          // ----------------------------------------------------------------
//          // Excetua-se industrialização
//          if (aaj15_cfop.aaj15codigo.substring(1) != "902") {
//          
//          if (eaa0102.eaa0102consFinal || eaa0102.eaa0102ti == 1) {
//          if (abg01!= null) { // Busca aliquota gravada no NCM
//          if (eaa0103.eaa0103cstIcms != null) {
//          // FEDERAL (IMPORTADOS)
//          // --------------------
//          def aliqImpAproxFed = 0;
//          if (cstA == 0 || cstA == 3 || cstA == 4 || cstA == 5) { //Item NACIONAL
//          aliqImpAproxFed = abg01.abg01vatFedNac;
//          } else {                                                // Item IMPORTADO
//          aliqImpAproxFed = abg01.abg01vatFedImp;
//          }
//          
//          if (aliqImpAproxFed != null && aliqImpAproxFed > 0 ) {
//          jsonEaa0103.put("impfed_vlr", ((eaa0103.eaa0103total * aliqImpAproxFed) / 100).round(2))
//          }
//          
//          // ESTADUAL
//          // --------
//          def aliqImpAproxEst = abg01.abg01vatEst;
//          if (aliqImpAproxEst != null && aliqImpAproxEst > 0 ) {
//          jsonEaa0103.put("impest_vlr", ((eaa0103.eaa0103total * aliqImpAproxEst) / 100).round(2));
//          }
//          
//          // FEDERAL + ESTADUAL
//          jsonEaa0103.put("impaprx_vlr", (jsonEaa0103.getBigDecimal_Zero("impfed_vlr") + jsonEaa0103.getBigDecimal_Zero("impest_vlr")).round(2));
//          }
//          }
//          }
//          }
//          
//          
//          //=================================================================
//          // Diferencial de Aliquota - Interestadual - a Partir de 01/01/2016
//          //=================================================================
//          def ano = eaa01.eaa01esData != null ? eaa01.eaa01esData.getYear() : MDate.date().getYear();
//          
//          // Para venda a Pessoa Física ou não Contribuinte a Partilha do diferencial de aliquota
//          if (eaa0102.eaa0102contribIcms == 0 && !dentroEstado && (aag01 != null && aag01.aag01codigo == "1058")) {
//          if (ano >= 2016){
//          
//          // Define % Partilha UF Destino
//          if (ano == 2016) jsonEaa0103.put("partilha_aliq", 40);
//          if (ano == 2017) jsonEaa0103.put("partilha_aliq", 60);
//          if (ano == 2018) jsonEaa0103.put("partilha_aliq", 80);
//          if (ano >= 2019) jsonEaa0103.put("partilha_aliq", 100);
//          
//          // Atribui aliquota Interna Destinatario
//          jsonEaa0103.put("internaufdest_aliq", jsonAag02Ent.getBigDecimal_Zero("aliq_icmsint"));
//          
//          // Atribuiu % FCP
//          if (abm1001 != null && jsonAbm1001_UF_Item.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
//          jsonEaa0103.put("fcpufdest_aliq", jsonAbm1001_UF_Item.getBigDecimal_Zero("fcpufdest_aliq"));
//          }
//          
//          //Base de Calculo do ICMS na UF Destino
//          jsonEaa0103.put("icmsufdest_bc", jsonEaa0103.getBigDecimal_Zero("bc_icms"));
//          
//          //Aliquota de Icms Interestadual
//          jsonEaa0103.put("interesuf_aliq", jsonEaa0103.getBigDecimal_Zero("_icms"));
//          
//          //Calcula o ICMS FCP
//          if (jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq") > 0) {
//          jsonEaa0103.put("icms_fcp", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("fcpufdest_aliq")) / 100).round(2));
//          }
//          
//          //Calcula o ICMS Interno UF Destino
//          if (jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq") > 0) {
//          jsonEaa0103.put("interufdest_icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("internaufdest_aliq")) / 100).round(2));
//          }
//          
//          //Calcula vlr de ICMS devido a UF Destino
//          jsonEaa0103.put("icms_ufdest", (jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - ((jsonEaa0103.getBigDecimal_Zero("icms") * jsonEaa0103.getBigDecimal_Zero("partilha_aliq")) / 100)).round(2));
//          
//          //Calcula vlr de ICMS devido a UF Origem
//          jsonEaa0103.put("uforig_icms", jsonEaa0103.getBigDecimal_Zero("interufdest_icms") - jsonEaa0103.getBigDecimal_Zero("icms") - jsonEaa0103.getBigDecimal_Zero("icms_ufdest"));
//          }
//          
//          }
        }
    }
    
    @Override
    public FormulaTipo obterTipoFormula() {
    return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==