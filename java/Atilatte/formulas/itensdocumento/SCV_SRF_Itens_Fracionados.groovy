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


public class SCV_SRF_Itens_Fracionado extends FormulaBase {

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
    private TableMap jsonAbm0101;
    private TableMap jsonAag02Ent;
    private TableMap jsonAag0201Ent;
    private TableMap jsonAag02Empr;
    private TableMap jsonAac10;
    private TableMap jsonAbe4001;
    private TableMap jsonAbe02;

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

        // CST ICMS B
        aaj10_cstIcmsB = abd02.abd02cstIcmsB != null ? getSession().get(Aaj10.class, abd02.abd02cstIcmsB.aaj10id) : null

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
        jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();
        jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
        jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
        jsonAbm1003_Ent_Item = abm1003 != null && abm1003.abm1003json != null ? abm1003.abm1003json : new TableMap();
        jsonEaa0103 = eaa0103.eaa0103json != null ? eaa0103.eaa0103json : new TableMap();
        jsonAbe4001 = abe4001 != null ? abe4001.abe4001json : new TableMap();

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

            // Define a ordem de separação dos itens
            jsonEaa0103.put("ordem_separacao", jsonAbm0101.getBigDecimal_Zero("ordem_separacao"))

            //Veriicando se a entidade é varejista ou atacadista
            def varAtac = "select abe01camposcustom from abe01 "+
                    "where abe01id = :abe01id ";

            TableMap abe01camposCustom = getAcessoAoBanco().buscarUnicoTableMap(varAtac, Parametro.criar("abe01id",abe01.abe01id)).getTableMap("abe01camposcustom");

            def codItem = abm01.abm01codigo;

            Query descrCriterios = getSession().createQuery( "select aba3001descr from aba3001 "+
                    "inner join abm0102 on abm0102criterio = aba3001id " +
                    "inner join abm01 on abm0102item = abm01id "+
                    "where abm01codigo = '"+codItem+"'"+
                    "and abm01tipo = 1");

            List<TableMap> listCriterios = descrCriterios.getListTableMap();

            String grupo = "";

            for(TableMap criterio : listCriterios){
                if(criterio.getString("aba3001descr").contains("Queijo")){
                    grupo = "Queijo"
                }
                if(criterio.getString("aba3001descr").contains("Leite")){
                    grupo = "Leite";
                }
                if(criterio.getString("aba3001descr").contains("Iogurte")){
                    grupo = "Iogurte";
                }
            }

            //Define o preço unitário de acordo com a Tabela de Preço
            if(eaa01.eaa01tp != null){
                if(jsonAbe4001 != null){
                    if(jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0){
                        DateTimeFormatter formato2 = DateTimeFormatter.ofPattern("yyyyMMdd");
                        LocalDate dataPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_fin"), formato2);
                        LocalDate dataAtual = LocalDate.now();
                        def precoPromocao = jsonAbe4001.getBigDecimal_Zero("preco_promocao");
                        if(dataPromo > dataAtual){
                            eaa0103.eaa0103unit = round(precoPromocao,4);
                        }else{
                            eaa0103.eaa0103unit = round(abe4001.abe4001preco,4)
                        }
                    }else{
                        eaa0103.eaa0103unit = round(abe4001.abe4001preco,4)
                    }
                }else{
                    eaa0103.eaa0103unit = round(abe4001.abe4001preco,4)
                }
            }

            if(grupo == "Iogurte"){

                // Unidade de Medida de Venda
                jsonEaa0103.put("umv",aam06.aam06codigo);

                // Define a Especie do Documento
                eaa0102.eaa0102especie = "FRASCOS";

                //Verifica a Mensagem do IVA no cadastro do Ítem
                if(jsonAbm1001_UF_Item.getString("mensagem") != null){
                    jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
                }

                // Quantidade Original
                if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                    jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
                }

                // Qauntidade SCE
                eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

                // Peso Bruto
                if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", round((eaa0103.eaa0103qtUso * abm01.abm01pesoBruto),4));

                // Peso Líquido
                if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", round((eaa0103.eaa0103qtUso * abm01.abm01pesoLiq),4));

                //Define o Campo de Unitário para Estoque
                jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

                // Quantidade de caixa e frasco
                int fatorUM = jsonAbm0101.getBigDecimal_Zero("volume_caixa").intValue();
                int qtd = eaa0103.eaa0103qtComl.intValue();
                int caixa = qtd / fatorUM;
                int frasco = qtd % fatorUM;

                jsonEaa0103.put("caixa", caixa );
                jsonEaa0103.put("frasco", frasco );

                // Volumes
                jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl);

                //Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",round((jsonEaa0103.getBigDecimal_Zero("volumes") * jsonAbm0101.getBigDecimal_Zero("m3")),2));

                /*
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

                 */

                // Total do item
                eaa0103.eaa0103total = round(eaa0103.eaa0103qtComl * eaa0103.eaa0103unit,2);

                //Define Quantidade Comercial como Quantidade Convertida
                jsonEaa0103.put("qt_convertida", round(eaa0103.eaa0103qtComl,2));

                // Unitario Convertido
                jsonEaa0103.put("unitario_conv", round(eaa0103.eaa0103unit,4));

                //Total Item Convertido
                jsonEaa0103.put("total_conv", round(jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida"),2));

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
                        jsonEaa0103.put("ipi_outras",0.0000000);
                    }

                    if (!cstValido) throw new ValidacaoException("CST de IPI inválido.");
                }

                //================================
                //******       ICMS         ******
                //================================
                def vlrReducao = 0;
                //BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
                jsonEaa0103.put("bc_icms", eaa0103.eaa0103total +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                        jsonEaa0103.getBigDecimal_Zero("desconto"));

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


                if(abe01camposCustom != null){
                    if(abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 1 || abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 2){
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
                            if(abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 1 || abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 2 ){
                                // % IVA_ST para Varejista
                                ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("_iva_st_atac");
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

                        //if(jsonAbe01.getBigDecimal_Zero("consufinal") == 0){
                        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 ){
                            jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
                            jsonEaa0103.put("icms_fcp", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
                            jsonEaa0103.put("vlr_icms_fcp_", jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp") / 100);
                        }else{
                            jsonEaa0103.put("bc_fcp",0.000000);
                            jsonEaa0103.put("icms_fcp",0.000000);
                            jsonEaa0103.put("vlr_icms_fcp_",0.000000);
                        }
                        //}


                    }else{
                        jsonEaa0103.put("bc_icms_st",0.000000);
                        jsonEaa0103.put("_icms_st",0.000000);
                        jsonEaa0103.put("icms_st",0.000000);
                        jsonEaa0103.put("_red_bc_icms_st",0.000000);
                    }

                    // Define Tx. Iva do Item
                    if(jsonEaa0103.getBigDecimal_Zero("bc_icms_st") > 0){
                        jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_iva_st_atac"))
                    }else{
                        jsonEaa0103.put("tx_iva_st", 0.000000)
                    }

                }else{
                    jsonEaa0103.put("bc_icms_st",0.000000);
                    jsonEaa0103.put("_icms_st",0.000000);
                    jsonEaa0103.put("icms_st",0.000000);


                }

                if(jsonAbe01.getBigDecimal_Zero("isento_st") == 1){
                    jsonEaa0103.put("bc_icms_st", 0.000000);
                    jsonEaa0103.put("icms_st", 0.000000);
                    jsonEaa0103.put("_icms_st", 0.000000);
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
                        aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
                        eaa0103.eaa0103cstIcms = aaj10_cstIcms;
                    }
                }

                // CFOP 
                def cfop = jsonAbm1001_UF_Item.getString("cfop_saida");

                if(cfop == null ) throw new ValidacaoException("Não foi informado CFOP no parâmetro itens-valores. Item:  " + abm01.abm01codigo);


                eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop))
                
                //Total do Documento
                //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
                eaa0103.eaa0103totDoc = eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                        jsonEaa0103.getBigDecimal_Zero("icms_st") +
                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                        jsonEaa0103.getBigDecimal_Zero("desconto");

                eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);

                // Ajusta o valor do IPI Outras
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Valor do Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc

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
                    jsonEaa0103.put("pis", 0);
                    jsonEaa0103.put("bc_pis", 0);
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
                    jsonEaa0103.put("cofins", 0);
                    jsonEaa0103.put("bc_cofins", 0);
                }

                //======================================
                //************* DIFAL *****************
                //======================================

                if(!dentroEstado){
                    if(abe01.abe01contribIcms == 0){
                        //Diferencial de Alíquota
                        def difDestino = 0
                        def difOrigem = 0

                        // Bc de ICMS Destino
                        jsonEaa0103.put("bc_icms_dest", jsonEaa0103.getBigDecimal_Zero("bc_icms"))

                        // Aliquota de ICMS Destino
                        jsonEaa0103.put("_icms_dest", jsonAag02Ent.getBigDecimal_Zero("txicminterna"));

                        // Diferencial de Aliquota
                        def difAliq = jsonEaa0103.getBigDecimal_Zero("_icms_dest") - jsonEaa0103.getBigDecimal_Zero("_icms");

                        if(difAliq < 0 ){
                            difAliq = (difAliq * -1);
                        }

                        difDestino = jsonEaa0103.getBigDecimal_Zero("bc_icms_dest") * (difAliq / 100);
                        jsonEaa0103.put("_dif_dest", new BigDecimal(100));

                        // Valor diferencial aliquota destino
                        jsonEaa0103.put("vlr_icms_dest", difDestino);

                        if(ufEnt.aag02uf == 'RJ'){
                            // Aliquota fundo combate a pobreza (Rio de Janeiro)
                            jsonEaa0103.put("icms_fcp_", new BigDecimal(2));
                        }
                        if(ufEnt.aag02uf == 'AL'){
                            // Aliquota fundo combate a pobreza (Alagoas)
                            jsonEaa0103.put("icms_fcp_", new BigDecimal(1));
                        }

                        // Calculo Fundo combate a Pobreza
                        jsonEaa0103.put("vlr_fcp_difal_", jsonEaa0103.getBigDecimal_Zero("bc_icms_dest") * jsonEaa0103.getBigDecimal_Zero("icms_fcp_") / 100);
                    }
                }

            }

            if(grupo == "Queijo"){

                //Unidade Medida Venda
                jsonEaa0103.put("umv",aam06.aam06codigo);

                // Define a Especie do Documento
                eaa0102.eaa0102especie = "CAIXAS";

                //Verifica a Mensagem do IVA no cadastro do Ítem
                if(jsonAbm1001_UF_Item.getString("mensagem") != null){
                    jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
                }

                // Quantidade Original
                if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                    jsonEaa0103.put("qt_original", round(eaa0103.eaa0103qtComl,4));
                }

                // Quantidade SCE
                eaa0103.eaa0103qtUso = round(eaa0103.eaa0103qtComl,4);

                // Peso Bruto
                if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", round((eaa0103.eaa0103qtUso * abm01.abm01pesoBruto),4));

                // Peso Líquido
                if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", round((eaa0103.eaa0103qtUso * abm01.abm01pesoLiq),4));

                def taraTotal = (jsonAbm0101.getBigDecimal_Zero("cvdnf")  * jsonAbm0101.getBigDecimal_Zero("tara_emb_")) + jsonAbm0101.getBigDecimal_Zero("tara_caixa")

                // Peso Bruto Para Itens em KG
                if(jsonEaa0103.getString("umv") == 'KG'){
                    jsonEaa0103.put("peso_bruto", eaa0103.eaa0103qtComl + taraTotal );
                    jsonEaa0103.put("peso_bruto",round(jsonEaa0103.getBigDecimal_Zero("peso_bruto"),4))

                }
                // Peso Líquido Para Itens em KG
                if(jsonEaa0103.getString("umv") == 'KG'){
                    jsonEaa0103.put("peso_liquido", eaa0103.eaa0103qtComl);
                    jsonEaa0103.put("peso_liquido",round(jsonEaa0103.getBigDecimal_Zero("peso_bruto"),4))
                }

                //Define o Campo de Unitário para Estoque
                jsonEaa0103.put("unitario_estoque", round(eaa0103.eaa0103unit,4));

                // Quantidade de caixa e frasco
                if(jsonEaa0103.getString("umv") == 'KG'){
                    def vol = jsonEaa0103.getBigDecimal_Zero("qt_convertida") / jsonAbm0101.getBigDecimal_Zero("peso_caixa")
                    def volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
                    jsonEaa0103.put("caixa", volume);
                }else{
                    def vol = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("volume_caixa")
                    def volume = new BigDecimal(vol).setScale(0,BigDecimal.ROUND_UP)
                    jsonEaa0103.put("caixa", volume);
                }

                // Volumes
                jsonEaa0103.put("volumes", new BigDecimal(jsonEaa0103.getInteger("caixa")));

                //Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",round(jsonEaa0103.getBigDecimal_Zero("volumes") * jsonAbm0101.getBigDecimal_Zero("m3"),2));

                // Total do item
                eaa0103.eaa0103total = round((eaa0103.eaa0103qtComl * eaa0103.eaa0103unit),2);

                // Quantidade de convertida
                jsonEaa0103.put("qt_convertida", round(eaa0103.eaa0103qtComl,4));


                // Unitario Convertido
                jsonEaa0103.put("unitario_conv", round(eaa0103.eaa0103unit,4));

                //Total Item Convertido
                jsonEaa0103.put("total_conv", round((jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")),2));

                // CST ICMS B
                if(abd02.abd02cstIcmsB != null){
                    eaa0103.eaa0103cstIcms = aaj10_cstIcmsB;
                }else{
                    eaa0103.eaa0103cstIcms = aaj10_cstIcms
                }

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

                // Tratar redução da base de cálculo
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
                if(abe01camposCustom != null){
                    if(abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 1 || abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 2){
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
                            if(abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 1 || abe01camposCustom.getBigDecimal_Zero("varejista_atac") == 2 ){
                                // % IVA_ST para Varejista
                                ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("_iva_st_atac");
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

                        // Define Tx. Iva do Item
                        if(jsonEaa0103.getBigDecimal_Zero("bc_icms_st") > 0){
                            jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_iva_st_atac"))
                        }else{
                            jsonEaa0103.put("tx_iva_st", 0.000000)
                        }

//						if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 ){
//							jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
//							jsonEaa0103.put("icms_fcp", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
//							jsonEaa0103.put("vlr_icms_fcp_", jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp") / 100);
//						}
                    }

                    // ICMS FCP
                    if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 ){
                        jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
                        jsonEaa0103.put("icms_fcp", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
                        jsonEaa0103.put("vlr_icms_fcp_", jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp") / 100);
                    }

                    // Zera ICMS ST caso entidade está caracterizada para não calcular
                    if(jsonAbe01.getBigDecimal_Zero("isento_st") == 1){
                        jsonEaa0103.put("bc_icms_st", 0.000000);
                        jsonEaa0103.put("icms_st", 0.000000);
                        jsonEaa0103.put("_icms_st", 0.000000);
                    }
                }
                
                // Troca CST 
                def consfinal = jsonAbe01.getBigDecimal_Zero("consufinal");
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
                        aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
                        eaa0103.eaa0103cstIcms = aaj10_cstIcms;
                    }
                }

                // CFOP 
                def cfop = jsonAbm1001_UF_Item.getString("cfop_saida");

                if(cfop == null ) throw new ValidacaoException("Não foi informado CFOP no parâmetro itens-valores. Item:  " + abm01.abm01codigo);
                
                eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop))

                // ==================
                // TOTAL DO DOCUMENTO
                // ==================
                //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
                eaa0103.eaa0103totDoc =  eaa0103.eaa0103total + jsonEaa0103.getBigDecimal_Zero("ipi") +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                        jsonEaa0103.getBigDecimal_Zero("icms_st") +
                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                        jsonEaa0103.getBigDecimal_Zero("desconto");

                eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2);

                //IPI Isentas
                jsonEaa0103.put("ipi_isentas", eaa0103.eaa0103totDoc);

                //ICMS Isentas
                jsonEaa0103.put("icms_isentas", eaa0103.eaa0103totDoc);

                //Valor do Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc ;

            }

            if(grupo == "Leite"){

                //Unidade Medida Venda
                jsonEaa0103.put("umv",aam06.aam06codigo);

                // Define a Especie do Documento
                eaa0102.eaa0102especie = "FRASCOS";

                //Verifica a Mensagem do IVA no cadastro do Ítem
                if(jsonAbm1001_UF_Item.getString("mensagem") != null){
                    jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
                }

                // Quantidade Original
                if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                    jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
                }

                // Quantidade SCE
                eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

                // Peso Bruto
                if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", round((eaa0103.eaa0103qtUso * abm01.abm01pesoBruto),4));

                // Peso Líquido
                if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", round((eaa0103.eaa0103qtUso * abm01.abm01pesoLiq),4));

                //Define o Campo de Unitário para Estoque
                jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

                //Quantidade de Caixa e Frasco
                if(abm01.abm01codigo == "0101002"){
                    int fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf").intValue();
                    int qtd = jsonEaa0103.getBigDecimal_Zero("qt_convertida").intValue();
                    int caixa = qtd / fatorUM;
                    int frasco = qtd % fatorUM;

                    jsonEaa0103.put("caixa", caixa );
                    jsonEaa0103.put("frasco", frasco );

                }else{
                    jsonEaa0103.put("caixa", new BigDecimal(0) );
                    jsonEaa0103.put("frasco", eaa0103.eaa0103qtComl);
                }

                // Volumes
                jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl);

                //Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",round((jsonEaa0103.getBigDecimal_Zero("volumes") * jsonAbm0101.getBigDecimal_Zero("m3")),2));

                /*
                //Calculo de Frete
                if(eaa0102.eaa0102despacho != null){
                    String codDesp = eaa0102.eaa0102despacho.abe01id;
                    def itemId = abm01.abm01id;

                    Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("001","aba2001ent = '" + codDesp + "' and aba2001item = '" + itemId + "'");

                    def valorFrete = aba2001?.getAba2001json()?.get("vlr_frete_transp") ?: 0;
                    jsonEaa0103.put("frete_item",eaa0103.eaa0103qtUso * valorFrete)
                }else{
                    jsonEaa0103.put("frete_item", 0.000000);
                }

                 */

                // Total do item
                eaa0103.eaa0103total = round(eaa0103.eaa0103qtComl * eaa0103.eaa0103unit,2);

                //Define Quantidade Comercial como Quantidade Convertida
                jsonEaa0103.put("qt_convertida", round(eaa0103.eaa0103qtComl,2));

                // Unitário Convertido
                jsonEaa0103.put("unitario_conv", round(eaa0103.eaa0103unit,4));

                //Total Item Convertido
                jsonEaa0103.put("total_conv", round(jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida"),2));


                if(!dentroEstado){
                    def vlrReducao = 0;
                    //BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
                    jsonEaa0103.put("bc_icms", eaa0103.eaa0103total +
                            jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                            jsonEaa0103.getBigDecimal_Zero("seguro") +
                            jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                            jsonEaa0103.getBigDecimal_Zero("vlr_desc"));

                    aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
                    eaa0103.eaa0103cstIcms = aaj10_cstIcms;

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
                }else{
                    jsonEaa0103.put("bc_icms",0.000000);
                    jsonEaa0103.put("_red_bc_icms",0.000000);
                    jsonEaa0103.put("_icms",0.000000);
                    jsonEaa0103.put("icms", 0.000000);
                    jsonEaa0103.put("icms_outras",0.000000);
                    jsonEaa0103.put("bc_icms", 0.000000);
                    jsonEaa0103.put("bc_icms_st", 0.000000);
                    jsonEaa0103.put("_icms_st", 0.000000);
                    jsonEaa0103.put("icms_st", 0.000000 );
                    jsonEaa0103.put("_red_bc_icms", 0.000000);
                    jsonEaa0103.put("icms_isento", 0.000000);

                }

                //Total Documento
                eaa0103.eaa0103totDoc = round(eaa0103.eaa0103total,2);

                //Aliquota dos tributos no cadastro de NCM
                def sql =   "select abg01camposcustom from abg01 "+
                        "inner join abm0101 on abm0101ncm = abg01id "+
                        "inner join abm01 on abm01id = abm0101item "+
                        "where abm01id = :abm01id "+
                        "AND abm01tipo = :eaa0103tipo "+
                        "AND abm0101empresa = :aac10id";
                def aliq;

                if(abm0101.abm0101ncm == null) throw new ValidacaoException("Não foi informado NCM no cadastro do item "+abm01.abm01codigo + " para calculo da carga tributária.  ")

                TableMap abg01camposcustom = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id",abm01.abm01id), Parametro.criar("eaa0103tipo", eaa0103.eaa0103tipo), Parametro.criar("aac10id",aac10.aac10id)).getTableMap("abg01camposcustom");

                if(abg01camposcustom != null){
                    aliq = abg01camposcustom.getBigDecimal_Zero("_carga_trib");
                }

                //Alíquota Carga Tributária
                def consfinal = jsonAbe01.getBigDecimal_Zero("consufinal");
                def aliqtrib = aliq;
                if(aliqtrib == 0 && consfinal == 1){
                    throw new ValidacaoException("Informe a alíquota no NCM para cálculo da Carga Tributária.");
                }else{
                    jsonEaa0103.put("carga_trib_", aliqtrib);
                }

                //BC Carga Tributaria
                jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total );

                //Carga Tributaria
                if(jsonAbe01.getBigDecimal_Zero("consufinal") == 0){
                    jsonEaa0103.put("bc_carga_trib", 0);
                    jsonEaa0103.put("carga_trib_", 0);
                    jsonEaa0103.put("carga_trib", 0);
                }else{
                    jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);
                }

                //IcmsOutras = TotalDocumento
                jsonEaa0103.put("icms_outras", eaa0103.eaa0103totDoc);

                //OutrasIPI = TotalDocumento
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Verifica se o ítem soma no total do Ítem da NFe
                jsonEaa0103.put("soma_nfe", 1);

                //Verifica se o PCD gera financeiro e cria valor no campo Total Financ.
                eaa0103.eaa0103totFinanc =  eaa0103.eaa0103totDoc


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