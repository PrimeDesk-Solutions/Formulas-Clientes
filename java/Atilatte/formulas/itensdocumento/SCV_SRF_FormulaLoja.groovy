package Atilatte.formulas.itensdocumento

import br.com.multiorm.Query;
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
import sam.model.entities.ab.Abd02;
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
import sam.server.samdev.formula.FormulaBase

import java.time.LocalDate
import java.time.format.DateTimeFormatter;

public class SCV_SRF_FormulaLoja extends FormulaBase {

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
    private TableMap jsonAaj07clasTrib;


    @Override
    public void executar() {

        //Item do documento
        eaa0103 = get("eaa0103");
        if (eaa0103 == null) return;

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

        //PCD Fiscais
        abd02 = getSession().get(Abd02.class, abd01.abd01ceFiscais.abd02id);

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
        abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = " + ufEnt.aag02id + " AND abm1001cv = " + abm10.abm10id)) : null;

        //Valores do Item - Entidade
        abm1003 = abm10 != null && abm10.abm10id != null ? getSession().get(Abm1003.class, Criterions.where("abm1003ent = " + abe01.abe01id + " AND abm1003cv = " + abm10.abm10id)) : null;

        //Dados Fiscais do item
        abm12 = abm0101 != null && abm0101.abm0101fiscal != null ? getSession().get(Abm12.class, abm0101.abm0101fiscal.abm12id) : null;
        if (abm12 == null) throw new ValidacaoException("Não foi encontrada a configuração fiscal do item: " + abm01.abm01codigo);
        if (abm12.abm12tipo == null) throw new ValidacaoException("Necessário informar o tipo fiscal do item: " + abm01.abm01codigo);

        //Dados Comerciais do item
        abm13 = abm0101 != null && abm0101.abm0101comercial != null ? getSession().get(Abm13.class, abm0101.abm0101comercial.abm13id) : null;

        //Fatores de Conv. da Unid de Compra para Estoque
        abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));

        //Unidade de Medida
        aam06 = abm13 != null && abm13.abm13umv != null ? getSession().get(Aam06.class, abm13.abm13umv.aam06id) : null;

        //Operação Comercial
        abb10 = abb01 != null && abb01.abb01operCod != null ? getSession().get(Abb10.class, abb01.abb01operCod.abb10id) : null;

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

        // Verifica se tem itens repetidos na tabela de preço
        if (abe40 != null) {
            Query tmItensTabela = getSession().createQuery("select abe4001id from abe4001 where abe4001tab = " + abe40.abe40id + " AND abe4001item = " + abm01.abm01id);

            List<TableMap> countItens = tmItensTabela.getListTableMap();

            if (countItens != null && countItens.size() > 1) throw new ValidacaoException("O item " + abm01.abm01codigo + " foi inserido mais de uma vez na tabela de preço " + abe40.abe40codigo);
        }

        //Itens da Tabela de Preço
        abe4001 = abe40 != null ? getSession().get(Abe4001.class, Criterions.where("abe4001tab = " + abe40.abe40id + " AND abe4001item = " + abm01.abm01id)) : null;

        if (abe4001 == null && eaa01.eaa01tp != null) throw new ValidacaoException("Item " + abm01.abm01codigo + " Não Encontrado Na Tabela De Preço " + abe40.abe40codigo)

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
        jsonAaj07clasTrib = aaj07.aaj07json != null ? aaj07.aaj07json : new TableMap();



        calcularItem();

        eaa0103.eaa0103json = jsonEaa0103;
        put("eaa0103", eaa0103);
    }

    private void calcularItem() {


        //=====================================
        // ******     Valores do Item     ******
        //=====================================
        if (eaa0103.eaa0103qtComl > 0) {

            if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Não foi informado quantidade de produto por caixa no cadastro do item " + abm01.abm01codigo)

            //Determina se a operação é dentro ou fora do estado
            def dentroEstado = false;
            if (ufEmpr != null && ufEnt != null) {
                dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
            }

            //Troca CFOP (Dentro ou Fora do Estado)
            if (eaa0103.eaa0103cfop != null) {
                def cfop = aaj15_cfop.aaj15codigo.substring(1);

                def primeiroDigito = aaj15_cfop.aaj15codigo.substring(0, 1);
                if (!dentroEstado) {
                    if (primeiroDigito == "1") {
                        primeiroDigito = "2";
                    }
                    if (primeiroDigito == "5") {
                        primeiroDigito = "6";
                    }
                }
                cfop = primeiroDigito + cfop;
                aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));
                eaa0103.eaa0103cfop = aaj15_cfop;
            }

            // Definir Peso Bruto e Peso Liquido
            calcularPesoBrutoeLiquido();

            // Unitario Item
            definirPrecoUnitarioItem();

            //Define o Campo de Unitário para Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

            // Quantidade Original
            if (jsonEaa0103.getBigDecimal_Zero("qt_original") == 0) {
                jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
            }

            // Quantidade Caixa e frasco
            def caixa = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf")

            def frasco = eaa0103.eaa0103qtComl.toInteger() % jsonAbm0101.getBigDecimal_Zero("cvdnf").toInteger()
            def caixaArredondada = new BigDecimal(caixa).setScale(0, BigDecimal.ROUND_UP)
            jsonEaa0103.put("caixa", caixaArredondada);
            jsonEaa0103.put("frasco", frasco);

            // Volumes
            jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

            // Quantidade uso
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

            //Total Item 
            eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit).round(2)

            //Total Documento 
            eaa0103.eaa0103totDoc = eaa0103.eaa0103total;

            //Valor do Financeiro
            eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

            jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
            jsonEaa0103.put("total_conv", eaa0103.eaa0103total);
            jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
            jsonEaa0103.put("umv", aam06.aam06codigo);

            calcularCBSIBS();

            definirCSTICMS();


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

    private void definirPrecoUnitarioItem() {
        if (eaa01.eaa01tp != null) {
            if (jsonAbe4001 != null) {
                if (jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0) {
                    DateTimeFormatter formato2 = DateTimeFormatter.ofPattern("yyyyMMdd");
                    LocalDate dataPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_fin"), formato2);
                    LocalDate dataAtual = LocalDate.now();
                    def precoPromocao = jsonAbe4001.getBigDecimal_Zero("preco_promocao");
                    if (dataPromo > dataAtual) {
                        eaa0103.eaa0103unit = precoPromocao.round(4);
                    } else {
                        eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
                    }
                } else {
                    eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
                }
            } else {
                eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
            }
        }
    }

    private void calcularPesoBrutoeLiquido() {
        if (jsonEaa0103.getString("umv") == "KG") {
            def taraEmb = jsonAbm0101.getBigDecimal_Zero("tara_emb_") * jsonAbm0101.getBigDecimal_Zero("cvdnf");
            def taraCaixa = jsonAbm0101.getBigDecimal_Zero("tara_caixa");
            def taraTotal = taraEmb + taraCaixa;

            // Peso Bruto Para Itens em KG
            if (jsonEaa0103.getString("umv") == 'KG') {
                jsonEaa0103.put("peso_bruto", jsonEaa0103.getBigDecimal_Zero("qt_convertida") + taraTotal)
            }

            // Peso Líquido Para Itens em KG
            if (jsonEaa0103.getString("umv") == 'KG') {
                jsonEaa0103.put("peso_liquido", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
            }
        } else {
            // Peso Bruto
            if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));

            // Peso Líquido
            if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
        }
    }

    private definirCSTICMS() {

        // Busca primeiramente o CST de ICMS no cadastro do PCD, caso não econcontrado, busca no cadastro do item
        String cst = "";
        if (abd02.abd02cstIcmsB != null) {
            aaj10_cstIcms = getSession().get(Aaj10.class, abd02.abd02cstIcmsB.aaj10id);
            cst = aaj10_cstIcms.aaj10codigo;

        } else if (abm12.abm12cstIcms != null) {
            aaj10_cstIcms = getSession().get(Aaj10.class, abm12.abm12cstIcms.aaj10id);
            cst = aaj10_cstIcms.aaj10codigo;

        } else {
            throw new ValidacaoException("Necessário preencher o CST de ICMS no cadastro do item " + abm01.abm01codigo + " ou no cadastro do PCD " + abd01.abd01codigo);
        }
    }


    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==