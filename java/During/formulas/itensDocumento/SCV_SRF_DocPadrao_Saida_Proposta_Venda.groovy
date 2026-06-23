//removido round 2 da linha 438 ou 439 em 12/08/2024 Beth

package During.formulas.itensDocumento;

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
import sam.model.entities.aa.Aam01;
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
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro;

public class SCV_SRF_DocPadrao_Saida_Proposta_Venda extends FormulaBase {

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
    private Aam01 aam01;

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

	   // Classe dos itens
        aam01 = abm01.abm01classe != null ? getSession().get(Aam01.class, abm01.abm01classe.aam01id) : null;

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

        //Troca CFOP (Dentro ou Fora do Estado)
        if (eaa0103.eaa0103cfop != null) {
            def cfop = aaj15_cfop.aaj15codigo.substring(1);


            def primeiroDigito = aaj15_cfop.aaj15codigo.substring(0,1);

            if(!dentroEstado){
                if(primeiroDigito == "5"){
                    primeiroDigito = "6";
                }
                if(ufEnt.aag02uf == "EX"){
                    primeiroDigito = "7"
                }
            }
            cfop = primeiroDigito + cfop;
            aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
            eaa0103.eaa0103cfop = aaj15_cfop;

        }

        if(eaa0103.eaa0103qtComl > 0){
		
            // Aliquota de ICMS
            if(aam01 != null){
            	if(aam01.aam01codigo == "01"){
            		jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_importado"));
            		if(jsonEaa0103.getBigDecimal_Zero("aliq_icms") == 0) jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_fixa_icms") )
            	}else{
            		jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_fixa_icms"));
            	}
            }else if(abm12 != null){
            	
            	if(abm12.abm12cstA != 0 && abm12.abm12cstA != 3 && abm12.abm12cstA != 4 && abm12.abm12cstA != 5 && abm12.abm12cstA != 8){
            	   jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_importado"));
            	   if(jsonEaa0103.getBigDecimal_Zero("aliq_icms") == 0) jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_fixa_icms") )
            	}else{
            		jsonEaa0103.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_fixa_icms"));
            	}
            }else{
            	throw new ValidacaoException("Necessário informar classe ou parametro fiscal do item " + abm01.abm01codigo)
            }

            def icms =  (jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_pis") +
                                    jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_cofins") +
                                    jsonAbm0101.getBigDecimal_Zero("iss") +
                                    jsonAbm0101.getBigDecimal_Zero("ir") +
                                    jsonAbm0101.getBigDecimal_Zero("csll")).round(2);

            def icmsRed = new BigDecimal(0);

            if(jsonAbm1001_UF_Item != null){
                icmsRed = (jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms") * jsonEaa0103.getBigDecimal_Zero("aliq_icms") / 100).round(2);
            }

            icmsRed = (jsonEaa0103.getBigDecimal_Zero("aliq_icms") - icmsRed + icms).round(2);
            icmsRed = ((100 - icmsRed) / 100).round(2);

           
            // Quantidade Original
            if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                jsonEaa0103.put("qt_original",eaa0103.eaa0103qtComl);
            }

            // Quantidade SCE (Quantiade Uso)
            if(abm13.abm13fcUV == null) throw new ValidacaoException("Necessário informar o fator de conversão de venda para uso no item "+abm01.abm01codigo);

            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl //* abm13.abm13fcUV_Zero;

            // Peso Bruto
            if(jsonEaa0103.getBigDecimal_Zero("peso_bruto") == 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtComl * abm01.abm01pesoBruto).round(3));

            // Peso Líquido
            if(jsonEaa0103.getBigDecimal_Zero("peso_liquido") == 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtComl * abm01.abm01pesoLiq).round(3));

            // Desconto Incondional
            if(jsonEaa0103.getBigDecimal_Zero("tx_desc_incond") < 0){
                jsonEaa0103.put("desconto",new BigDecimal(0));
            }

            if(jsonEaa0103.getBigDecimal_Zero("tx_desc_incond") > 0 && jsonEaa0103.getBigDecimal_Zero("desconto") > 0){
                jsonEaa0103.put("desconto",(jsonEaa0103.getBigDecimal_Zero("qt_venda") * eaa0103.eaa01unit * jsonEaa0103.getBigDecimal_Zero("tx_desc_incond")) / 100 );
                jsonEaa0103.put("desconto", jsonEaa0103.getBigDecimal_Zero("desconto").round(2));
            }else if(jsonEaa0103.getBigDecimal_Zero("tx_desc_incond") > 0 && jsonEaa0103.getBigDecimal_Zero("desconto") == 0){
                jsonEaa0103.put("desconto",(jsonEaa0103.getBigDecimal_Zero("qt_venda") * eaa0103.eaa01unit * jsonEaa0103.getBigDecimal_Zero("tx_desc_incond")) / 100  );
                jsonEaa0103.put("desconto", jsonEaa0103.getBigDecimal_Zero("desconto").round(2));
            }else if(jsonEaa0103.getBigDecimal_Zero("tx_desc_incond") <= 0 && jsonEaa0103.getBigDecimal_Zero("desconto") > 0){
                jsonEaa0103.put("tx_desc_incond", (jsonEaa0103.getBigDecimal_Zero("desconto") / (jsonEaa0103.getBigDecimal_Zero("tx_desc_incond") * eaa0103.eaa0103unit)) * 100);
                jsonEaa0103.put("tx_desc_incond", jsonEaa0103.getBigDecimal_Zero("tx_desc_incond").round(6));
            }else{
                jsonEaa0103.put("desconto",new BigDecimal(0));
            }

            def pisCof = ((100-(jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_pis") + jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_cofins")))/100);

            def icmsTot = 1 - (jsonEaa0103.getBigDecimal_Zero("aliq_icms") / 100);


            // Chamada da Função para calulo de impostos de serviços
            calcularImpostosServicos()

            // Total do Item sem Imposto
            jsonEaa0103.put("tot_item_s_imp", (jsonEaa0103.getBigDecimal_Zero("unit_s_imp") * eaa0103.eaa0103qtComl).round(2));

           
            // Chamada para a função de calculo de IPI
            calcularIPI(pisCof,icmsTot);

            // Chamada da função para calculo de PIS
            calcularPIS(pisCof);

            // Chamada da função para calculo de COFINS
            calcularCOFINS();


            // Chamada para função para calculo de ICMS
            calcularICMS();

            // Chamada para a função para calculo de ICMS ST
            calcularICMSST();

            // Chamada da função para cálculo da carga tributária
            calculaCargaTributaria();

            // Total Liquido
            jsonEaa0103.put("total_liquido", eaa0103.eaa0103totDoc -
					                    jsonEaa0103.getBigDecimal_Zero("icms") -
					                    jsonEaa0103.getBigDecimal_Zero("ipi") -
					                    jsonEaa0103.getBigDecimal_Zero("pis") -
					                    jsonEaa0103.getBigDecimal_Zero("cofins") -
					                    jsonEaa0103.getBigDecimal_Zero("iss"));

            jsonEaa0103.put("total_liquido", jsonEaa0103.getBigDecimal_Zero("total_liquido").round(2));


            // IPI OUtras
            if(jsonEaa0103.getBigDecimal_Zero("ipi") == 0){
                jsonEaa0103.put("bc_ipi", new BigDecimal(0));
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);
                jsonEaa0103.put("ipi_isento", new BigDecimal(0));
            }

            // Unitário Frete
            if(jsonEaa0103.getBigDecimal_Zero("cif") > 0){
                eaa0103.eaa0103unit = (eaa0103.eaa0103unit + jsonEaa0103.getBigDecimal_Zero("cif").round(2))
            }

            // Quantidade Tributável
            jsonEaa0103.put("qtd_tribut", eaa0103.eaa0103qtComl)

            // Unitário Tributável
            jsonEaa0103.put("unit_tribut", eaa0103.eaa0103unit);


            // Chamada da Função para calulo dos SPED's
            preencherSPEDS();

        }

    }
    private void calcularIPI(BigDecimal pisCof, BigDecimal icmsTot ){

        // ============================================================================================
        // ***************************************** IPI **********************************************
        // ============================================================================================

        // Valor do IPI
        if(jsonEaa0103.getBigDecimal_Zero("aliq_ipi") != -1){
            if (eaa0103.eaa0103cstIpi != null) {
			
                //BC de IPI = Total do Item + Frete + Seguro + Despesas Acessorias
                jsonEaa0103.put("bc_ipi", ((jsonEaa0103.getBigDecimal_Zero("tot_item_s_imp") / pisCof) / icmsTot) + jsonEaa0103.getBigDecimal_Zero("seguro") + jsonEaa0103.getBigDecimal_Zero("frete_dest") + jsonEaa0103.getBigDecimal_Zero("outras_despesas"));

                jsonEaa0103.put("bc_ipi", jsonEaa0103.getBigDecimal_Zero("bc_ipi").round(2));

                //Alíquota de IPI do cadastro de NCM
                if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
                    jsonEaa0103.put("aliq_ipi", abg01.abg01txIpi);
                }
            }

            // Aplica aliquota para calcular IPI
            if(jsonEaa0103.getBigDecimal_Zero("aliq_ipi") > -1 ){
                jsonEaa0103.put("ipi",(jsonEaa0103.getBigDecimal_Zero("bc_ipi") * jsonEaa0103.getBigDecimal_Zero("aliq_ipi") / 100).round(2));
                jsonEaa0103.put("ipi",jsonEaa0103.getBigDecimal_Zero("ipi"));
            }


        }else{
            jsonEaa0103.put("bc_ipi", new BigDecimal(0));
            jsonEaa0103.put("ipi", new BigDecimal(0));
        }
    }

    private void calcularICMS(){

        // ==========================================================================================
        // *************************************** ICMS *********************************************
        // ==========================================================================================

        def cdBcIcms = new BigDecimal(0);
        def cdIcmsIsento = new BigDecimal(0);
        def cdIcms = new BigDecimal(0);
        def vlrReducao = new BigDecimal(0);
        def pisCof = ((100-(jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_pis") + jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_cofins")))/100);
        pisCof == null ? 1 : pisCof;
	   def icmsTot = 1 - (jsonEaa0103.getBigDecimal_Zero("aliq_icms") / 100);
	   icmsTot == null ? 1 : icmsTot;

        if(abm01.abm01tipo != 3){
            jsonEaa0103.put("bc_icms", (((jsonEaa0103.getBigDecimal_Zero("tot_item_s_imp") / pisCof) / icmsTot)));
        }

        // Tratar Redução da Base de Calculo
        if(abm01.abm01tipo != 3){
            if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0){
                jsonEaa0103.put("_red_bc_icms",jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms"));
            }
        }

        // Calculo da Redução
        if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") >= 0 ){
            vlrReducao = (jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") / 100).round(2);
            jsonEaa0103.put("bc_icms", (jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao).round(2));
        }


        // Calcular Valor do ICMS e ICMS Isento
        if(jsonEaa0103.getBigDecimal_Zero("aliq_icms") < 0){
            jsonEaa0103.put("icms_isento", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
            vlrReducao = new BigDecimal(0);
        }else{
            if(abm01.abm01tipo != 3){
                jsonEaa0103.put("icms", (jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("aliq_icms") / 100).round(2));
            }
        }

        // Zera base de Cálculo de ICMS se alíquota menor que Zero
        if(jsonEaa0103.getBigDecimal_Zero("aliq_icms") < 0){
            jsonEaa0103.put("icms_outras", eaa0103.eaa0103totDoc.round(2));
            jsonEaa0103.put("bc_icms", new BigDecimal(0));
            jsonEaa0103.put("icms", new BigDecimal(0));
        }

        // Isentas de ICMS
        if(jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0){
            jsonEaa0103.put("icms_isento", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("bc_icms"));
            jsonEaa0103.put("icms_isento", jsonEaa0103.getBigDecimal_Zero("icms_isento").round(2));
        }else{
            jsonEaa0103.put("icms_isento", new BigDecimal(0));
        }

        // Outras de ICMS
        if(jsonEaa0103.getBigDecimal_Zero("icms") == 0){
            jsonEaa0103.put("icms_outras", (eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("desconto")).round(2));
            jsonEaa0103.put("bc_icms", new BigDecimal(0));
        }else{
            jsonEaa0103.put("icms_outras", new BigDecimal(0));

        }
	}

    private void calcularICMSST(){

        // ==========================================================================================
        // *************************************** ICMS ST ******************************************
        // ==========================================================================================

        //Alíquota do ICMS ST
        jsonEaa0103.put("aliq_icms_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_fixa_icms_st"));

        def ivaST;

        if(jsonEaa0103.getBigDecimal_Zero("aliq_icms") == jsonEaa0103.getBigDecimal_Zero("aliq_icms_st")){
            jsonEaa0103.put("icms_st", new BigDecimal(0));
        }else{
            jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms") + jsonEaa0103.getBigDecimal_Zero("frete_dest") + jsonEaa0103.getBigDecimal_Zero("seguro") + jsonEaa0103.getBigDecimal_Zero("outras_despesas") +jsonEaa0103.getBigDecimal_Zero("ipi")).round(2))

            if(jsonEaa0103.getBigDecimal_Zero("aliq_icms_st") == -1){
                ivaST = 0;
            }else{
                if(jsonAbe01.getInteger("varejista_atacadista") == 0 || jsonAbe01.getBigDecimal_Zero("varejista_atacadista") == 1 || jsonAbe01.getBigDecimal_Zero("varejista_atacadista") == 2 ){
                    // % IVA ST
                    ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("_iva_st_atac");
                }
            }

            if(ivaST > 0){
                jsonEaa0103.put("aliq_icms_st", ivaST);

            }

            if(jsonEaa0103.getBigDecimal_Zero("aliq_icms_st") == 0){
                jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
                jsonEaa0103.put("icms_st", new BigDecimal(0));
            }

        }

        if(ivaST > 0){
            // Adicionar IVA ST á Base
            jsonEaa0103.put("bc_icms_st", jsonEaa0103.getBigDecimal_Zero("bc_icms"))
            jsonEaa0103.put("bc_icms_st", jsonEaa0103.getBigDecimal_Zero("bc_icms").round(2));

            def aliqIcmsSt = jsonEaa0103.getBigDecimal_Zero("aliq_icms") - jsonEaa0103.getBigDecimal_Zero("aliq_icms_st");

            if(aliqIcmsSt < 0 ){
                aliqIcmsSt = aliqIcmsSt * (-1);
            }

            jsonEaa0103.put("icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * aliqIcmsSt) / 100);
            jsonEaa0103.put("icms_st", jsonEaa0103.getBigDecimal_Zero("icms_st").round(2));
        }else{
            jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
            jsonEaa0103.put("_icms_st", new BigDecimal(0));
            jsonEaa0103.put("icms_st", new BigDecimal(0));
        }
    }

    private void calcularPIS( BigDecimal pisCof){
        // ==========================================================================================
        // *************************************** PIS **********************************************
        // ==========================================================================================


        jsonEaa0103.put("bc_pis", (jsonEaa0103.getBigDecimal_Zero("tot_item_s_imp") / pisCof).round(2));


        if(jsonEaa0103.getBigDecimal_Zero("aliq_pis") == 0){
            jsonEaa0103.put("aliq_pis", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_pis"));
        }

        if(jsonEaa0103.getBigDecimal_Zero("aliq_pis") < 0){
            jsonEaa0103.put("bc_pis", new BigDecimal(0));
            jsonEaa0103.put("pis", new BigDecimal(0));
        }else{
            jsonEaa0103.put("pis",(jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("aliq_pis")) / 100);
            jsonEaa0103.put("pis",jsonEaa0103.getBigDecimal_Zero("pis").round(2));
        }
    }
    private void calcularCOFINS(){

        // ==========================================================================================
        // *************************************** COFINS *******************************************
        // ==========================================================================================

        jsonEaa0103.put("bc_cofins", jsonEaa0103.getBigDecimal_Zero("bc_pis").round(2));

        if(jsonEaa0103.getBigDecimal_Zero("aliq_cofins") == 0){
            jsonEaa0103.put("aliq_cofins", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_cofins"));
        }

        if(jsonEaa0103.getBigDecimal_Zero("aliq_cofins") < 0){
            jsonEaa0103.put("bc_cofins", new BigDecimal(0));
            jsonEaa0103.put("cofins", new BigDecimal(0));
        }else{
            jsonEaa0103.put("cofins",(jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("aliq_cofins")) / 100);
            jsonEaa0103.put("cofins",jsonEaa0103.getBigDecimal_Zero("cofins").round(2));
        }
    }

    private void calculaCargaTributaria(){

           // Total Do Documento = Total Item C/ Imposto
              jsonEaa0103.put("tot_item_c_imp", + jsonEaa0103.getBigDecimal_Zero("tot_item_s_imp") + jsonEaa0103.getBigDecimal_Zero("pis") +
						                                       							jsonEaa0103.getBigDecimal_Zero("cofins") +
						                                       							jsonEaa0103.getBigDecimal_Zero("icms") +
						                                        							jsonEaa0103.getBigDecimal_Zero("ipi"));

            // Total Do Documento = Total Documento (SAM)
           	eaa0103.eaa0103totDoc = jsonEaa0103.getBigDecimal_Zero("tot_item_c_imp").round(2);

           // Novo Unitário (SAM)
               eaa0103.eaa0103unit = (jsonEaa0103.getBigDecimal_Zero("tot_item_c_imp") / eaa0103.eaa0103qtComl).round(2);

           // Total do Item (SAM)
            eaa0103.eaa0103total = (eaa0103.eaa0103unit * eaa0103.eaa0103qtComl).round(2);
                        
            // Total Financeiro
            eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;
           


        // ==========================================================================================
        // *************************************** CARGA TRIBUTÁRIA *********************************
        // ==========================================================================================

        def consFinal;
        def aliqTrib;
	   if(abm01.abm01tipo != 3){
	   	//Aliquota dos tributos no cadastro de NCM
	        def sql = "select abg01camposcustom from abg01 "+
	                "inner join abm0101 on abm0101ncm = abg01id "+
	                "inner join abm01 on abm01id = abm0101item "+
	                "where abm01id = :abm01id "+
	                "AND abm01tipo = :eaa0103tipo "+
	                "AND abm0101empresa = :aac10id";
	
	        if(abm0101.abm0101ncm == null) throw new ValidacaoException("Não foi informado NCM no cadastro do item "+abm01.abm01codigo + " para calculo da carga tributária.  ")
	
	        TableMap abg01CamposCustom = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id",abm01.abm01id), Parametro.criar("eaa0103tipo", eaa0103.eaa0103tipo), Parametro.criar("aac10id",aac10.aac10id)).getTableMap("abg01camposcustom");
	
	        if(abm01.abm01tipo != 3){
	            //consFinal = jsonAbe01.getInteger("consu_final");
	            if(abg01CamposCustom != null){
	                aliqTrib = abg01CamposCustom.getBigDecimal_Zero("aliq_trib");
	            }
	
	            if(aliqTrib == 0 ) throw new ValidacaoException("Informe a aliquota no NCM " +abg01.abg01codigo + " para cálculo da carga tributária");
	
	            if(jsonEaa0103.getBigDecimal_Zero("carga_trib_") == 0) {
	                jsonEaa0103.put("carga_trib_", aliqTrib)
	            }
	        }
	
	        // BC Carga Tributária
	        jsonEaa0103.put("bc_carga_trib_", eaa0103.eaa0103total.round(2));
	
	        // Valor da Carga Tributária
	        jsonEaa0103.put("vlrcargatrib", (jsonEaa0103.getBigDecimal_Zero("bc_carga_trib_") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100));
	        jsonEaa0103.put("vlrcargatrib", jsonEaa0103.getBigDecimal_Zero("vlrcargatrib").round(2));	
	   }
   
    }


    private void calcularImpostosServicos(){
        // BC ISS
        jsonEaa0103.put("bc_iss", jsonEaa0103.getBigDecimal_Zero("total_servico"));

        // Aliquota ISS
        jsonEaa0103.put("aliq_iss", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_iss"));

        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_iss") == 0){
            jsonEaa0103.put("aliq_iss", new BigDecimal(0));
        }

        // Calculo ISS
        if(jsonEaa0103.getBigDecimal_Zero("aliq_iss") == 0){
            jsonEaa0103.put("bc_iss", new BigDecimal(0));
            jsonEaa0103.put("iss", new BigDecimal(0));
        }else{
            jsonEaa0103.put("iss", (jsonEaa0103.getBigDecimal_Zero("bc_iss") * jsonEaa0103.getBigDecimal_Zero("aliq_iss") / 100).round(2));
        }

        // BC CSLL
        jsonEaa0103.put("bc_csll", jsonEaa0103.getBigDecimal_Zero("total_servico"));

        // Aliquota CSLL
        jsonEaa0103.put("aliq_csll", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_csll"));

        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_csll") == 0){
            jsonEaa0103.put("aliq_csll", new BigDecimal(0));
        }

        // Calculo CSLL
        if(jsonEaa0103.getBigDecimal_Zero("aliq_csll") == 0){
            jsonEaa0103.put("bc_csll", new BigDecimal(0));
            jsonEaa0103.put("csll", new BigDecimal(0));
        }else{
            jsonEaa0103.put("csll", (jsonEaa0103.getBigDecimal_Zero("bc_csll") * jsonEaa0103.getBigDecimal_Zero("aliq_csll") / 100).round(2));
        }

        // BC IR
        jsonEaa0103.put("bc_ir", jsonEaa0103.getBigDecimal_Zero("total_servico"));

        // Aliquota IR
        jsonEaa0103.put("aliq_ir", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_ir"));

        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_ir") == 0){
            jsonEaa0103.put("aliq_ir", new BigDecimal(0));
        }

        // Calculo IR
        if(jsonEaa0103.getBigDecimal_Zero("aliq_ir") == 0){
            jsonEaa0103.put("bc_ir", new BigDecimal(0));
            jsonEaa0103.put("ir", new BigDecimal(0));
        }else{
            jsonEaa0103.put("ir", round((jsonEaa0103.getBigDecimal_Zero("bc_ir") * jsonEaa0103.getBigDecimal_Zero("aliq_ir") / 100),2) );
        }
    }

    private void preencherSPEDS(){

        // ========================================================================================
        //                                 PIS/COFINS SPED
        // ========================================================================================

        // BC Cofins SPED = Cofins
        jsonEaa0103.put("bc_cofins_sped", jsonEaa0103.getBigDecimal_Zero("bc_cofins"));

        //Aliq Cofins SPED = Aliq Cofins
        jsonEaa0103.put("_cofins_sped", jsonEaa0103.getBigDecimal_Zero("aliq_cofins"));

        // Cofins SPED = Cofins
        jsonEaa0103.put("cofins_sped", jsonEaa0103.getBigDecimal_Zero("cofins"));

        // BC PIS SPED = BC PIS
        jsonEaa0103.put("bc_pis_sped", jsonEaa0103.getBigDecimal_Zero("bc_pis"));

        // Aliq PIS SPED = Aliq SPED
        jsonEaa0103.put("_pis_sped", jsonEaa0103.getBigDecimal_Zero("aliq_pis"));

        // PIS SPED = PIS
        jsonEaa0103.put("pis_sped", jsonEaa0103.getBigDecimal_Zero("pis"));

        // ========================================================================================
        // 								  ICMS SPED
        // ========================================================================================

        //BC ICMS SPED = BC ICMS
        jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));

        //Aliq ICMS SPED = Aliq ICMS
        jsonEaa0103.put("_icms_sped", jsonEaa0103.getBigDecimal_Zero("aliq_icms"));


        //Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
        jsonEaa0103.put("_red_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("_red_bc_icms"));

        //ICMS Outras SPED = ICMS Outras
        jsonEaa0103.put("icms_outras_sped", jsonEaa0103.getBigDecimal_Zero("icms_outras"));

        //ICMS Isento SPED = ICMS Isento
        jsonEaa0103.put("icms_isento_sped", jsonEaa0103.getBigDecimal_Zero("icms_isento"));

        //ICMS SPED = ICMS
        jsonEaa0103.put("icms_sped", jsonEaa0103.getBigDecimal_Zero("icms"));


        // ========================================================================================
        // 								  ICMS ST SPED
        // ========================================================================================

        //BC ICMS ST SPED = BC ICMS ST
        jsonEaa0103.put("bc_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));

        //Aliq ICMS ST SPED = Aliq ICMS ST
        jsonEaa0103.put("_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("_icms_st"));

        //ICMS ST SPED = ICMS ST
        jsonEaa0103.put("icms_st_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));


        // ========================================================================================
        // 								  IPI SPED
        // ========================================================================================

        //BC IPI SPED = BC IPI
        jsonEaa0103.put("bc_ipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));

        //Aliq IPI SPED = Aliq IPI
        jsonEaa0103.put("_ipi_sped", jsonEaa0103.getBigDecimal_Zero("aliq_ipi"));

        //IPI Outras SPED = IPI Outras
        jsonEaa0103.put("ipi_outras_sped", jsonEaa0103.getBigDecimal_Zero("ipi_outras"));

        //IPI Isento SPED = IPI Isento
        jsonEaa0103.put("ipi_isento_sped", jsonEaa0103.getBigDecimal_Zero("ipi_isento"));

        //IPI SPED = IPI
        jsonEaa0103.put("ipi_sped", jsonEaa0103.getBigDecimal_Zero("ipi"));
    }

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==