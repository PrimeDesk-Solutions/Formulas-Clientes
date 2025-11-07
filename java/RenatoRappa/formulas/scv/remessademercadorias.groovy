package RenatoRappa.formulas.scv;

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
import sam.model.entities.ab.Abd02;
import sam.model.entities.ab.Abe02;
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
    
    private Abb01 abb01;
    private Abb10 abb10;
    private Abd01 abd01;
    private Abd02 abd02;
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

     //PCD
     abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);

    //PCD Fiscais
    if(abd01.abd01ceFiscais == null) throw new ValidacaoException("Não foi econtrado PCD fiscal no cadastro do PCD " + abd01.abd01codigo);

    abd02 = getSession().get(Abd02.class, abd01.abd01ceFiscais.abd02id);
    
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
    
        
        //=====================================
        // ******     Valores do Item     ******
        //=====================================
        if (eaa0103.eaa0103qtComl > 0) {

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
                    if(primeiroDigito == "5"){
                         primeiroDigito = "6";
                    }
                }
                cfop = primeiroDigito + cfop;
                aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
                eaa0103.eaa0103cfop = aaj15_cfop;
            }
            
            
            //Define o Campo de Unitário para Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);
            
            // *** Processa QUANTIDADES
            // Conserva Qt.Original do documento (Qt.Faturamento original)
            if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
            }

            // Converte Qt.Faturamento para Qt.Venda
            if(abm13.abm13fcUV == null) throw new ValidacaoException("Fator de conversão para venda no cadastro do item "+abm01.abm01codigo+" é inválido.")
            jsonEaa0103.put("qt_documento", eaa0103.eaa0103qtComl * abm13.abm13fcUV);

            //Total Item 
            eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit) - jsonEaa0103.getBigDecimal_Zero("desconto");

            //Total Documento 
            eaa0103.eaa0103totDoc = eaa0103.eaa0103total;

            // Total Financeiro
            eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

            // Quantidade SCE
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

             // Busca CST ICMS
            String cst = buscarCstICMS();
            
            eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", cst));

            // Calculo ICMS
            calcularICMS()

			

            //===============================
            //              IPI 
            //================================

            //Base de Calculo de IPI
            jsonEaa0103.put("bc_ipi", eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("seguro") + jsonEaa0103.getBigDecimal_Zero("outras_despesas"))
            
            //Alíquota de IPI do cadastro de NCM  
            if(jsonEaa0103.getBigDecimal_Zero("_ipi") == 0){
                if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
                    jsonEaa0103.put("_ipi", abg01.abg01txIpi);
                }
            }

            // Aplica Aliquota para calcular IPI
            jsonEaa0103.put("ipi",((jsonEaa0103.getBigDecimal_Zero("bc_ipi") * jsonEaa0103.getBigDecimal_Zero("_ipi")) / 100).round(2));

            //Valor de IPI outras 
            if(jsonEaa0103.getBigDecimal_Zero("_ipi") == -1){
                jsonEaa0103.put("bc_ipi", 0);
                jsonEaa0103.put("_ipi", 0);
                jsonEaa0103.put("ipi", 0);
            }

            //Valor do Financeiro
            eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;  

            
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
            
 
        }
    }
       private void calcularICMS(){

    		def vlrReducao = 0;
    		
		//BC ICMS
		jsonEaa0103.put("bc_icms", eaa0103.eaa0103total +
		jsonEaa0103.getBigDecimal_Zero("frete_dest") +
		jsonEaa0103.getBigDecimal_Zero("seguro") +
		jsonEaa0103.getBigDecimal_Zero("outras_despesas"));
		
		// Tratar redução da base de cálculo
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
				jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
			}
		}
		
		// Calcular valor do ICMS e Valor ICMS Isento
		if(jsonEaa0103.getBigDecimal_Zero("_icms") > 0){ // Aliquota menor que zero = Isento
			jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));
		}else{
			jsonEaa0103.put("icms", 0.000000);
			jsonEaa0103.put("icms_outras", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
			jsonEaa0103.put("bc_icms", 0.000000 );
			jsonEaa0103.put("_red_bc_icms", 0.000000);
			jsonEaa0103.put("icms_isento", 0.000000);
			vlrReducao = 0;
		}

    }

    private String buscarCstICMS(){
        // Busca primeiramente o CST de ICMS no cadastro do PCD, caso não econcontrado, busca no cadastro do item
        String cst = "";

        if(abd02.abd02cstIcmsB != null){
            aaj10_cstIcms = getSession().get(Aaj10.class,  abd02.abd02cstIcmsB.aaj10id);
            cst = aaj10_cstIcms.aaj10codigo
        }else if(abm12.abm12cstIcms != null){
            aaj10_cstIcms = getSession().get(Aaj10.class,  abm12.abm12cstIcms.aaj10id);
            cst = aaj10_cstIcms.aaj10codigo;
        }else{
            throw new ValidacaoException("Necessário preencher o CST de ICMS cadastro do item " + abm01.abm01codigo + " ou no cadastro do PCD " + abd01.abd01codigo)
        }

        return cst;
    }
    
    @Override
    public FormulaTipo obterTipoFormula() {
    return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==