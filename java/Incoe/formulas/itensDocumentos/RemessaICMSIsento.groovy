package Incoe.formulas.itensDocumentos

import br.com.multiorm.ColumnType
import sam.model.entities.aa.Aac13;
import sam.model.entities.ab.Abd02
import sam.model.entities.ea.Eab01
import sam.model.entities.ea.Eab0101;
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

public class RemessaICMSIsento extends FormulaBase {

    private Aac10 aac10;
    private Aac13 aac13;
    private Aag01 aag01;
    private Aag02 aag02;
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
    private TableMap jsonAag02;
    private TableMap jsonAag02Ent;
    private TableMap jsonAag0201Ent;
    private TableMap jsonAag02Empr;
    private TableMap jsonAac10;
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

        //Central de Documento
        abb01 = eaa01.eaa01central;

        //PCD
        abd01 = getSession().get(Abd01.class, eaa01.eaa01pcd.abd01id);

        //PCD Fiscais
        if (abd01.abd01ceFiscais == null) throw new ValidacaoException("Não foi econtrado PCD fiscal no cadastro do PCD " + abd01.abd01codigo);

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
        if (abm13 == null) throw new ValidacaoException("Não foi encontrada as configurações comerciais do item: " + abm01.abm01codigo);

        //Fatores de Conv. da Unid de Compra para Estoque
        abm1301 = abm13 == null ? null : eaa0103.eaa0103umComl == null ? null : getSession().get(Abm1301.class, Criterions.where("abm1301cc = " + abm13.abm13id + " AND abm1301umc = " + eaa0103.eaa0103umComl.aam06id));
        if (abm1301 == null) throw new ValidacaoException("Não foi informado fator de conversão no cadastro do item " + abm01.abm01codigo);
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

        // Class. Trib CBS/IBS
        aaj07 = eaa0103.eaa0103clasTribCbsIbs != null ? getSession().get(Aaj07.class, eaa0103.eaa0103clasTribCbsIbs.aaj07id) : null;
        if (aaj07 == null) throw new ValidacaoException("É nescessário informar a Classificação tribtária de CBS/IBS do item: " + abm01.abm01codigo + " - " + abm01.abm01na);

        // CST IBS/CBS
        aaj09 = eaa0103.eaa0103cstCbsIbs != null ? getSession().get(Aaj09.class, eaa0103.eaa0103cstCbsIbs.aaj09id) : null;
        if (aaj09 == null) interromper("Necessário informar o CST de CBS/IBS no item: " + abm01.abm01codigo + " - " + abm01.abm01na);

        //CAMPOS LIVRES
        jsonAac10 = aac10.aac10json != null ? aac10.aac10json : new TableMap();
        jsonAag02 = aag02 != null ? aag02.aag02json : new TableMap();
        jsonAag02Ent = ufEnt != null && ufEnt.aag02json != null ? ufEnt.aag02json : new TableMap();
        jsonAag0201Ent = municipioEnt != null && municipioEnt.aag0201json != null ? municipioEnt.aag0201json : new TableMap();
        jsonAag02Empr = ufEmpr != null && ufEmpr.aag02json != null ? ufEmpr.aag02json : new TableMap();
        jsonAbe01 = abe01.abe01json != null ? abe01.abe01json : new TableMap();
        jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
        jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
        jsonAbm1003_Ent_Item = abm1003 != null && abm1003.abm1003json != null ? abm1003.abm1003json : new TableMap();
        jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
        jsonAaj07clasTrib = aaj07.aaj07json != null ? aaj07.aaj07json : new TableMap();

        calcularItem();

        eaa0103.eaa0103json = jsonEaa0103;
        put("eaa0103", eaa0103);
    }

    private void calcularItem() {

        if (eaa0103.eaa0103qtComl > 0) {

            // Determina se a operação é dentro ou fora do estado
            Boolean dentroEstado = true;

            if (ufEmpr != null && ufEnt != null) {
                dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
            }

            // Trocar CFOP (Dentro ou fora do estado)
            definirCFOP(dentroEstado);

            // ea011vlr1 não existe mais

            // QtdeSCE (antigo ea011vlr2 = QuantidadeVenda)
            if (abm13.abm13fcUV == null) throw new ValidacaoException("Não foi informado o Fator Uso para Venda no cadastro comercial do item " + abm01.abm01codigo);
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl * abm13.abm13fcUV;
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtUso.round(2);

            // Quantidade Tributável
            jsonEaa0103.put("qtd_tributavel", eaa0103.eaa0103qtComl);

            // Unitário Tributável
            jsonEaa0103.put("unit_tributavel", eaa0103.eaa0103unit);

            // Total do item = Quantidade * Preço Unitário (a fórmula antiga não subtrai desconto)
            eaa0103.eaa0103total = eaa0103.eaa0103qtComl * eaa0103.eaa0103unit;
            eaa0103.eaa0103total = eaa0103.eaa0103total.round(2);

            calcularPesos();

            // Obrigatório: antes do total do documento (a fórmula não calcula ICMS, IPI, PIS e COFINS)
            zerarImpostos();

            // Total do Documento = Total do Item
            eaa0103.eaa0103totDoc = eaa0103.eaa0103total;

            // ICMS Outras e IPI Outras: a fórmula antiga zera
            jsonEaa0103.put("icms_outras", new BigDecimal(0));
            jsonEaa0103.put("ipi_outras", new BigDecimal(0));

            // ICMS Isento e IPI Isento = Total do Documento
            jsonEaa0103.put("icms_isento", eaa0103.eaa0103totDoc);
            jsonEaa0103.put("ipi_isento", eaa0103.eaa0103totDoc);

            // Total Financeiro
            if(eaa01.eaa01cp != null){
                if(eaa0103.eaa0103retInd == 0){
                    eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;
                }else{
                    eaa0103.eaa0103totFinanc = BigDecimal.ZERO;
                }
            }

            trocarCSTIcms();

            calcularCBSIBS();

            preencherCamposEstoque();

            definirCodigoBeneficioFiscal();

            preencherSPEDS();

        }

    }

    private void definirCFOP(boolean dentroEstado){
        if(eaa0103.eaa0103cfop != null){
            String cfop = aaj15_cfop.aaj15codigo;
            String primeiroDigito = cfop.substring(0,1);

            if(!dentroEstado){
                if (primeiroDigito == "5"){
                    primeiroDigito = "6";
                }
            }

            cfop = primeiroDigito + cfop.substring(1);

            aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));

            if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrado CFOP com o código " +  cfop);
            eaa0103.eaa0103cfop = aaj15_cfop;
        }
    }

    // Regra do cliente: peso = Qt. Faturamento * peso do cadastro, só quando o peso do cadastro é maior que zero
    // (sempre recalcula, mesmo que o peso já esteja preenchido na linha do item)
    private void calcularPesos(){
        // Peso Bruto
        if (abm01.abm01pesoBruto_Zero > 0) {
            jsonEaa0103.put("peso_bruto", eaa0103.eaa0103qtComl * abm01.abm01pesoBruto_Zero);
            jsonEaa0103.put("peso_bruto", jsonEaa0103.getBigDecimal_Zero("peso_bruto").round(3));
        }

        // Peso Líquido
        if (abm01.abm01pesoLiq_Zero > 0) {
            jsonEaa0103.put("peso_liquido", eaa0103.eaa0103qtComl * abm01.abm01pesoLiq_Zero);
            jsonEaa0103.put("peso_liquido", jsonEaa0103.getBigDecimal_Zero("peso_liquido").round(3));
        }
    }

    // Obrigatório em toda fórmula: imposto sem alíquota (0 ou -1) fica com base e valor zerados.
    // A alíquota não é alterada (o -1 é mantido).
    private void zerarImpostos(){
        if (jsonEaa0103.getBigDecimal_Zero("aliq_icms") <= 0) {
            jsonEaa0103.put("bc_icms", new BigDecimal(0));
            jsonEaa0103.put("icms", new BigDecimal(0));
        }
        if (jsonEaa0103.getBigDecimal_Zero("aliq_ipi") <= 0) {
            jsonEaa0103.put("bc_ipi", new BigDecimal(0));
            jsonEaa0103.put("ipi", new BigDecimal(0));
        }
        if (jsonEaa0103.getBigDecimal_Zero("aliq_pis") <= 0) {
            jsonEaa0103.put("bc_pis", new BigDecimal(0));
            jsonEaa0103.put("pis", new BigDecimal(0));
        }
        if (jsonEaa0103.getBigDecimal_Zero("aliq_cofins") <= 0) {
            jsonEaa0103.put("bc_cofins", new BigDecimal(0));
            jsonEaa0103.put("cofins", new BigDecimal(0));
        }
    }

    // Regra do cliente: CST de ICMS sempre 041 (não tributada)
    private void trocarCSTIcms(){
        aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "041"));
        if (aaj10_cstIcms == null) throw new ValidacaoException("Não foi encontrado CST de ICMS com o código 041");
        eaa0103.eaa0103cstIcms = aaj10_cstIcms;
    }

    private void calcularCBSIBS() {
        // *********************************************
        // ************ REFORMA TRIBUTÁRIA *************
        // *********************************************

        //================================
        //******  BASE DE CALCULO   ******
        //================================

        // Desconto É subtraído aqui: nesta fórmula o eaa0103total NÃO é líquido do desconto
        //(vProd + vServ + vFrete + vSeg + vOutro + vII) -
        // (vDesc - vPIS - vCOFINS - vICMS - vICMSUFDest - vFCP - vFCPUFDest - vICMSMono - vISSQN)
        /*VBCIS*/
        jsonEaa0103.put("is_bc", eaa0103.eaa0103total +
                jsonEaa0103.getBigDecimal_Zero("total_servicos") +
                jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                jsonEaa0103.getBigDecimal_Zero("seguro") +
                jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                jsonEaa0103.getBigDecimal_Zero("desconto") -
                jsonEaa0103.getBigDecimal_Zero("pis") -
                jsonEaa0103.getBigDecimal_Zero("cofins") -
                jsonEaa0103.getBigDecimal_Zero("icms") -
                jsonEaa0103.getBigDecimal_Zero("ufdest_icms") -
                jsonEaa0103.getBigDecimal_Zero("fcp"))


        //vProd + vServ + vFrete + vSeg + vOutro - vDesc
        //VBC (CBS e IBS) - Base de Caculo CBS/IBS
        jsonEaa0103.put("cbs_ibs_bc", eaa0103.eaa0103total +
                jsonEaa0103.getBigDecimal_Zero("total_servicos") +
                jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                jsonEaa0103.getBigDecimal_Zero("seguro") +
                jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                jsonEaa0103.getBigDecimal_Zero("desconto") -
                jsonEaa0103.getBigDecimal_Zero("pis") -
                jsonEaa0103.getBigDecimal_Zero("cofins") -
                jsonEaa0103.getBigDecimal_Zero("icms"));

        jsonEaa0103.put("cbs_ibs_bc", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc").round(2))

        //================================
        //******       VALORES      ******
        //================================

        //CREDITO PRESUMIDO DA OPERAÇÃO(UB120)
        if (jsonAaj07clasTrib.getBoolean("cred_presumido")) {
            jsonEaa0103.put("cred_presumido", jsonEaa0103.getBigDecimal_Zero("aliq_credpresum") * jsonEaa0103.getBigDecimal_Zero("bc_credpresum"))
        }

        //CRÉDITO PRESUMIDO IBS ZONA FRANCA DE MANAUS
        if (jsonAaj07clasTrib.getBoolean("cred_pres_ibs_zfm")) {
            // TODO: regra ainda não definida
        }
        //CST CBS/IBS
        if (jsonAaj07clasTrib.getString("cst_cbsibs")) {
            // TODO: regra ainda não definida
        }
        //DESCRIÇÃO CST CBS/IBS
        if (jsonAaj07clasTrib.getString("desc_cstcbsibs")) {
            // TODO: regra ainda não definida
        }
        //DIFERIMENTO CBS/IBS
        if (jsonAaj07clasTrib.getBoolean("dif_cbsibs")) {
            // TODO: regra ainda não definida
        }
        //ESTORNO DE CRÉDITO
        if (jsonAaj07clasTrib.getBoolean("estorno_cred")) {
            // TODO: regra ainda não definida
        }
        //MONOFÁSICA
        if (jsonAaj07clasTrib.getBoolean("monofasica_cbsibs")) {
            // TODO: regra ainda não definida
        }

        //REDUÇÃO BASE DE CÁLCULO
        if (jsonAaj07clasTrib.getBoolean("red_bc")) {
            // TODO: regra ainda não definida
        }
        //REDUÇÃO BASE DE CÁLCULO CST
        if (jsonAaj07clasTrib.getBoolean("red_bc_cst")) {
            // TODO: regra ainda não definida
        }
        //REDUÇÃO DE ALÍQUOTA
        if (jsonAaj07clasTrib.getBoolean("red_bc_aliq")) {
            // TODO: regra ainda não definida
        }
        //TRANSFERÊNCIA DE CRÉDITO
        if (jsonAaj07clasTrib.getBoolean("transf_cred")) {
            // TODO: regra ainda não definida
        }
        //TRIBUTAÇÃO REGULAR
        if (jsonAaj07clasTrib.getBoolean("tributacao")) {
            // TODO: regra ainda não definida
        }

        // CBS
        jsonEaa0103.put("cbs_aliq", jsonAag02Ent.getBigDecimal_Zero("cbs_aliq"))//Alíquota CBS
        jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") / 100));
        jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("vlr_cbs").round(2));


        // Aliquotas IBS
        jsonEaa0103.put("ibs_uf_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_uf_aliq"));//Alíquota IBS Estadual
        jsonEaa0103.put("ibs_mun_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_mun_aliq"));

        // IBS Municipio
        jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") / 100));
        jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun").round(2));

        //IBS UF
        jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") / 100))
//IBS Estadual
        jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf").round(2));


        jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))
// total de IBS
        jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibs").round(2));

        //AJUSTE DA COMPETENCIA (UB112)
        if (jsonAaj07clasTrib.getBoolean("ajuste_comp")) {
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))
            jsonEaa0103.put("vlr_cbs", ((jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * jsonEaa0103.getBigDecimal_Zero("cbs_aliq")) / 100).round(2))
        }

        //CST 200 - Tributação c/ Redução
        if (aaj09.aaj09codigo == "200") {
            //PERCENTUAL REDUÇÃO CBS
            if (jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_cbs")) {
                jsonEaa0103.put("perc_red_cbs", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_cbs"))
            }
            //PERCENTUAL REDUÇÃO IBS UF
            if (jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_uf")) {
                jsonEaa0103.put("perc_red_ibs_uf", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_uf"));
                // Mudar nome do campo
            }

            //PERCENTUAL DE REDUÇÃO IBS MUNIC
            if (jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_mun")) {
                jsonEaa0103.put("perc_red_ibs_mun", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_mun"))
                // Criar campo
            }

            // Aliquotas Efetivas
            jsonEaa0103.put("aliq_efet_ibs_uf", (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") * (100 - jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_uf")) / 100));
            // Mudar nome campo
            jsonEaa0103.put("aliq_efet_ibs_mun", (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") * (100 - jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_mun")) / 100));
            jsonEaa0103.put("aliq_efet_cbs", (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") * (100 - jsonEaa0103.getBigDecimal_Zero("perc_red_cbs")) / 100));

            // CBS
            jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_cbs") / 100))
            jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("vlr_cbs").round(2));

            // IBS Município
            jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_mun") / 100));
            jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun").round(2));

            // IBS UF
            jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_uf") / 100))
//IBS Estadual
            jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf").round(2))

            // Soma total do IBS UF/Municipio
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))
// total de IBS
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibs").round(2))

        }

        if (jsonAaj07clasTrib.getInteger("exige_tributacao") == 0) { // Zera impostos caso não exige tributação
            jsonEaa0103.put("cbs_aliq", new BigDecimal(0));
            jsonEaa0103.put("vlr_cbs", new BigDecimal(0));
            jsonEaa0103.put("ibs_uf_aliq", new BigDecimal(0));
            jsonEaa0103.put("ibs_mun_aliq", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibsmun", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibsuf", new BigDecimal(0));
            jsonEaa0103.put("vlr_ibs", new BigDecimal(0));
            jsonEaa0103.put("cbs_ibs_bc", new BigDecimal(0));
        }
    }

    private void definirCodigoBeneficioFiscal() {

        // =============================================================================
        // ************************** CÓDIGO BENEFICIO FISCAL **************************
        // =============================================================================

        if(eaa0103.eaa0103cstIcms == null || eaa0103.eaa0103cfop == null) return;

        def cst = eaa0103.eaa0103cstIcms.aaj10codigo;
        def cfop = eaa0103.eaa0103cfop.aaj15codigo;
        def cBenef = null

        if(cst != "00" && cst != "000"){
            if((cst == "41" || cst == "041") && (cfop == "5915" || cfop == "6915")){
                cBenef = "SP070090";
            }else if((cst == "41" || cst == "041") && (cfop == "5916" || cfop == "6916")){
                cBenef = "SP070100";
            }else if((cst == "41" || cst == "041") && (cfop == "7101" || cfop == "7102")){
                cBenef = "SP070050";
            }else if((cst == "50" || cst == "050") && (cfop == "5901" || cfop == "5924" || cfop == "6924")){
                cBenef = "SP054020";
            }else if((cst == "20" || cst == "020") && (cfop == "5101" || cfop == "6101" || cfop == "6102")){
                cBenef = "SP020120";
            }
        }

        eaa0103.eaa0103codBenef = cBenef;
    }

    private void preencherCamposEstoque(){
        jsonEaa0103.put("total_item_estoque", eaa0103.eaa0103total);
        jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);
    }

    private void preencherSPEDS() {

        // ========================================================================================
        //                                 PIS/COFINS SPED
        // ========================================================================================

        // BC Cofins SPED = Cofins
        jsonEaa0103.put("bc_cofins_sped", jsonEaa0103.getBigDecimal_Zero("bc_cofins"));

        //Aliq Cofins SPED = Aliq Cofins
        jsonEaa0103.put("aliq_cofins_sped", jsonEaa0103.getBigDecimal_Zero("aliq_cofins"));

        // Cofins SPED = Cofins
        jsonEaa0103.put("cofins_sped", jsonEaa0103.getBigDecimal_Zero("cofins"));

        // BC PIS SPED = BC PIS
        jsonEaa0103.put("bc_pis_sped", jsonEaa0103.getBigDecimal_Zero("bc_pis"));

        // Aliq PIS SPED = Aliq SPED
        jsonEaa0103.put("aliq_pis_sped", jsonEaa0103.getBigDecimal_Zero("aliq_pis"));

        // PIS SPED = PIS
        jsonEaa0103.put("pis_sped", jsonEaa0103.getBigDecimal_Zero("pis"));

        // ========================================================================================
        // 								  ICMS SPED
        // ========================================================================================

        //BC ICMS SPED = BC ICMS
        jsonEaa0103.put("bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("bc_icms"));

        //Aliq ICMS SPED = Aliq ICMS
        jsonEaa0103.put("aliq_icms_sped", jsonEaa0103.getBigDecimal_Zero("aliq_icms"));


        //Aliq Reduc BC ICMS SPED = Aliq Reduc BC ICMS
        jsonEaa0103.put("aliq_red_bc_icms_sped", jsonEaa0103.getBigDecimal_Zero("aliq_red_bc_icms"));

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
        jsonEaa0103.put("aliq_icms_st_sped", jsonEaa0103.getBigDecimal_Zero("aliq_icms_st"));

        //ICMS ST SPED = ICMS ST
        jsonEaa0103.put("icms_st_sped", jsonEaa0103.getBigDecimal_Zero("icms_st"));


        // ========================================================================================
        // 								  IPI SPED
        // ========================================================================================

        //BC IPI SPED = BC IPI
        jsonEaa0103.put("bc_ipi_sped", jsonEaa0103.getBigDecimal_Zero("bc_ipi"));

        //Aliq IPI SPED = Aliq IPI
        jsonEaa0103.put("aliq_ipi_sped", jsonEaa0103.getBigDecimal_Zero("aliq_ipi"));

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