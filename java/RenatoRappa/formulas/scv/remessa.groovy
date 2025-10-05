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

public class remessa extends FormulaBase {

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

            //eaa0103.eaa0103unit = round(eaa0103.eaa0103unit,2)
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

            // Volumes
            jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl)
            
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

		  // Quantidade SCE
            eaa0103.eaa0103qtUso = jsonEaa0103.getBigDecimal_Zero("qt_documento");

            //Total Item 
            eaa0103.eaa0103total = round(jsonEaa0103.getBigDecimal_Zero("qt_documento") * eaa0103.eaa0103unit,2);

            //Total Documento
            eaa0103.eaa0103totDoc = round(eaa0103.eaa0103total - jsonEaa0103.getBigDecimal_Zero("desconto"),2);

            //ICMS isentas
            jsonEaa0103.put("icms_isento",eaa0103.eaa0103totDoc);

            //Ipi Isento 
            jsonEaa0103.put("ipi_isento",eaa0103.eaa0103totDoc);

            //Carga Tributária
            jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total.round(2));

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
            def aliqtrib = aliq;
            if(aliqtrib == 0 ){
                throw new ValidacaoException("Informe a alíquota no NCM para cálculo da Carga Tributária.");
            }else{
                jsonEaa0103.put("carga_trib_", aliqtrib);
            }

            //Valor da Carga tributária
            jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);

            //Financeiro 
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
            
 
        }
    }
    
    @Override
    public FormulaTipo obterTipoFormula() {
    return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==