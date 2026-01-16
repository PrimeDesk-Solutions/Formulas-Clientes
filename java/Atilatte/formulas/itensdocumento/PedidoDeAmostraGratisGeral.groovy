package Atilatte.formulas.itensdocumento;

import br.com.multiorm.Query
import java.lang.Math;
import sam.server.samdev.utils.Parametro;
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo;
import sam.model.entities.aa.Aae20;
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


public class PedidoDeAmostraGratisGeral extends FormulaBase {
    private Aae20 aae20;
    private Aac10 aac10;
    private Aag01 aag01;
    private Aag02 ufEnt;
    private Aag02 ufEmpr;
    private Aag0201 municipioEnt;
    private Aag0201 municipioEmpr;
    private Aaj07 aaj07;
    private Aaj09 aaj09;
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
        //if (abd01 != null && abd01.abd01es == 0)  throw new ValidacaoException("Esta fórmula poderá ser utilizada somente em documentos de saída.");

        //PCD Fiscais
        abd02 = getSession().get(Abd02.class, abd01.abd01ceFiscais.abd02id);

        if (abd02 == null) throw new ValidacaoException("Não foi encontrado PCD fiscal no parametro fiscal do PCD " + abd01.abd01codigo);

        //Dados da Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

        // Classe Entidades
        aae20 = abe01.abe01classe == null ? null : getSession().get(Aae20.class, abe01.abe01classe.aae20id);

        // Entidade (Cliente)
        abe02 = getSession().get(Abe02.class, Criterions.eq("abe02ent", abe01.abe01id));

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
        aaj15_cfop = abd02 != null && abd02.abd02cfop != null ? getSession().get(Aaj15.class, abd02.abd02cfop.aaj15id) : null;

        if (aaj15_cfop == null) throw new ValidacaoException("Não foi encontrado CFOP nas configurações fiscais do PCD " + abd01.abd01codigo);

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
        if (abe4001 == null && eaa01.eaa01tp != null) throw new ValidacaoException("Item Não Encontrado Na Tabela De Preço!")

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
        jsonAbe02 = abe02.abe02json != null ? abe02.abe02json : new TableMap();
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

        //Determina se a operação é dentro ou fora do estado
        def dentroEstado = false;
        if (ufEmpr != null && ufEnt != null) {
            dentroEstado = ufEmpr.aag02uf == ufEnt.aag02uf;
        }

        //Define se a entidade é ou não contribuinte de ICMS
        def contribICMS = 0;
        if (abe01.abe01cli == 1) {
            contribICMS = abe01.abe01contribIcms; // Cliente
        }
        if (abe01.abe01for == 1) {
            contribICMS = abe01.abe01contribIcms; // Fornecedor
        }

        // Verifica se o tipo de inscrição é CPF, se sim, define como não contribuinte de ICMS
        if (abe01.abe01ti == 1) {
            contribICMS = 0;
        }


        if (eaa0103.eaa0103qtComl > 0) {

            // Define a Mensagem dos itens
            jsonEaa0103.put("mensagem", jsonAbm0101.getString("mensagem"));

            // Define a Especie do Documento
            eaa0102.eaa0102especie = "Volumes";

            // Define a ordem de separação dos itens
            jsonEaa0103.put("ordem_separacao", jsonAbm0101.getBigDecimal_Zero("ordem_separacao"))

            // Define a categoria dos itens
            String grupo = buscarCategoriaItem(abm01.abm01codigo);

            //Define o preço unitário de acordo com a Tabela de Preço
            definirPrecoUnitarioItem();

            // Define a data de entrega dos itens
            defineDataEntregaItens();

            //Unidade Medida Venda
            jsonEaa0103.put("umv",aam06.aam06codigo);

            // Altera o Gtin do item caso entidade compra em caixa
            if(jsonAbe01.getBigDecimal_Zero("unidade_caixa") == 1 && aam06.aam06codigo == 'UN') eaa0103.eaa0103gtin = jsonAbm0101.getString("descricao_livre");

            //Define o Campo de Unitário para Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

            // Quantidade Original
            if(jsonEaa0103.getBigDecimal_Zero("qt_original") == 0){
                jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
            }

            // Verifica a Mensagem do IVA no cadastro do Ítem
            if(jsonAbm1001_UF_Item.getString("mensagem") != null){
                jsonEaa0103.put("mensagem", jsonAbm1001_UF_Item.getString("mensagem"));
            }

            // Quantidade SCE
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;


            //==================================================================================================================================================
            //============================================= CALCULO PARA OS ITENS COM CRITERIO DE SELEÇÃO "IOGURTE" =============================================
            //==================================================================================================================================================

            if (grupo.contains("IOGURTE") || grupo.contains("BAUNILHA")) {

                // Converte a quantidade do Item
                converterQuantidadeItem(grupo)

                // Peso Bruto/ Peso Liquido
                calcularPesoBrutoeLiquido();

                // Unitário Convertido
                converterUnitarioItem(grupo);

                // Quantidade de caixa e frasco
                definirQuantidadeCaixaFrasco();

                // Converte Qt.Documento para Volume
                jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

                // Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",jsonEaa0103.getBigDecimal_Zero("caixa") * jsonAbm0101.getBigDecimal_Zero("m3"));

                // Define CST ICMS Itens
                definirCstICMS();

                // Total do item
                eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit).round(2);

                // Total Do Item com Desconto
                jsonEaa0103.put("total_item_desc", eaa0103.eaa0103total - jsonEaa0103.getBigDecimal_Zero("desconto"));

                // Total Item Convertido
                converterTotalItem()

                calcularCBSIBS();

                // Calcula o IPI dos Itens
                calcularIPI()

                // Calcula ICMS dos Itens
                calcularICMS(dentroEstado, grupo);

                // Define ICMS ST
                calcularIcmsST();

                // CFOP
                definirCFOP();

                // Troca o primeiro digito do CFOP, caso fora do estado
                trocarPrimeiroDigitoCFOP(dentroEstado, aaj15_cfop.aaj15codigo);

                // Fundo de Combate a pobreza
                calcularFundoDeCombatePobreza();

                //Total do Documento sem ST
                //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
                eaa0103.eaa0103totDoc = (eaa0103.eaa0103total +
                        jsonEaa0103.getBigDecimal_Zero("icms_st") +
                        jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") +
                        jsonEaa0103.getBigDecimal_Zero("ipi") +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("outras") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") -
                        jsonEaa0103.getBigDecimal_Zero("desconto")).round(2);

                // Ajusta o valor do IPI Outras
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Valor do Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;


                // Calcula PIS Item
                calcularPis();

                // Calcula COFINS Item
                calcularCofins()

                // Calcula Difal dos Itens
                calcularDifal(dentroEstado)

                // Preenche os SPEDs dos Itens
                preencherSPED();

            }

            //==================================================================================================================================================
            //============================================= CALCULO PARA OS ITENS COM CRITERIO DE SELEÇÃO "QUEIJO" =============================================
            //==================================================================================================================================================

            if(grupo == "QUEIJO"){

                // Quantidade Convertida
                converterQuantidadeItem(grupo);

                // Definir Peso Bruto e Peso Liquido
                calcularPesoBrutoeLiquido();

                // Definir Peso Bruto e Peso Liquido
                calcularPesoBrutoeLiquido();

                //Define Valor do Unitário convertido, caso Caixa
                converterUnitarioItem(grupo)

                // Define Quantidade de Caixa do Item
                definirQuantidadeCaixaFrasco();

                // Define a quantidade de volumes dos itens
                jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

                //Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",jsonEaa0103.getBigDecimal_Zero("caixa") * jsonAbm0101.getBigDecimal_Zero("m3"));

                // Define CST ICMS Itens
                definirCstICMS();

                // Total do item
                eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit).round(2);

                calcularCBSIBS();

                // Total Do Item com Desconto
                jsonEaa0103.put("total_item_desc", eaa0103.eaa0103total - jsonEaa0103.getBigDecimal_Zero("desconto"));

                // Total Convertido
                converterTotalItem();

                // Calcula ICMS Itens
                calcularICMS(dentroEstado, grupo);

                // Calcula ICMS ST dos Itens
                calcularIcmsST();

                // Fundo de Combate a pobreza
                calcularFundoDeCombatePobreza();

                // CFOP
                definirCFOP();

                // Troca o primeiro digito do CFOP, caso fora do estado
                trocarPrimeiroDigitoCFOP(dentroEstado, aaj15_cfop.aaj15codigo);

                // ==================
                // TOTAL DO DOCUMENTO
                // ==================
                //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
                eaa0103.eaa0103totDoc = eaa0103.eaa0103total +
                        jsonEaa0103.getBigDecimal_Zero("ipi") +
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
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

                // Calcula Difal dos Itens
                calcularDifal(dentroEstado)

                // Preenche os SPEDs dos Itens
                preencherSPED();

            }

            if (grupo == "LEITE") {

                // Converte a quantidade dos itens
                converterQuantidadeItem(grupo);

                // Converte Unitário Item
                converterUnitarioItem(grupo);

                // Define Quantidade de Caixa e Frascos dos Itens
                definirQuantidadeCaixaFrasco();

                // Define a quantidade de volumes dos itens
                jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

                // Calculo Metro Cúbico por item
                jsonEaa0103.put("m3",jsonEaa0103.getBigDecimal_Zero("volumes") * jsonAbm0101.getBigDecimal_Zero("m3"));

                // Define CST ICMS Itens
                definirCstICMS();

                // Calcular Peso Bruto e Peso Liquido
                calcularPesoBrutoeLiquido();

                //Total Do Item
                eaa0103.eaa0103total = (eaa0103.eaa0103qtComl * eaa0103.eaa0103unit).round(2);

                calcularCBSIBS();

                // Total Do Item com Desconto
                jsonEaa0103.put("total_item_desc", eaa0103.eaa0103total - jsonEaa0103.getBigDecimal_Zero("desconto"));

                // CFOP
                definirCFOP();

                // Troca o primeiro digito do CFOP, caso fora do estado
                trocarPrimeiroDigitoCFOP(dentroEstado, aaj15_cfop.aaj15codigo)

                // Total Item Convertido
                converterTotalItem();

                // Calcula ICMS
                calcularICMS(dentroEstado, grupo)

                // Zera Calculo de ST dos itens
                jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
                jsonEaa0103.put("icms_st", new BigDecimal(0));
                jsonEaa0103.put("_icms_st", new BigDecimal(0));

                //TotalDocumento(10) = TotalItem(9)
                eaa0103.eaa0103totDoc = eaa0103.eaa0103total.round(2);

                // Aliquota dos tributos no cadastro de NCM
                calcularCargaTributaria();

                // Calcula Diferimento dos Itens
                calcularDiferimento();

                //IcmsOutras = TotalDocumento
                jsonEaa0103.put("icms_outras", eaa0103.eaa0103totDoc);

                //OutrasIPI = TotalDocumento
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Verifica se o PCD gera financeiro e cria valor no campo Total Financ.
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc

                // Fundo de Combate a pobreza
                calcularFundoDeCombatePobreza();

                // Calcula Difal dos Itens
                calcularDifal(dentroEstado)

                // Preenche os SPEDs dos Itens
                preencherSPED();

            }
        }
    }

    private String buscarCategoriaItem(codItem){

        Query descrCriterios = getSession().createQuery("select aba3001descr from aba3001 "+
                "inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
                "inner join abm01 on abm0102item = abm01id "+
                "where abm01codigo = '"+codItem+"'"+
                "and abm01tipo = 1 ");

        List<TableMap> listCriterios = descrCriterios.getListTableMap();

        if (listCriterios == null && listCriterios.size() == 0) throw new ValidacaoException("Não foi encontrado critério de seleção de grupo para o item " + codItem);

        String grupo = "";

        for(TableMap criterio : listCriterios){
            if(criterio.getString("aba3001descr").toUpperCase().contains("QUEIJO")){
                grupo = "QUEIJO"
            }
            if(criterio.getString("aba3001descr").toUpperCase().contains("LEITE")){
                grupo = "LEITE"
            }

            if(criterio.getString("aba3001descr").toUpperCase().contains("IOGURTE") || criterio.getString("aba3001descr").toUpperCase().contains("BAUNILHA")){
                grupo = "IOGURTE"
            }
        }

        return grupo;

    }

    private void definirPrecoUnitarioItem(){
        if(abb01.abb01operAutor != "SRF"){
            if(eaa01.eaa01tp != null){
                if(jsonAbe4001 != null){
                    if(jsonAbe4001.getString("data_promo_ini") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0){
                        DateTimeFormatter formato2 = DateTimeFormatter.ofPattern("yyyyMMdd");
                        LocalDate dataIniPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_ini"), formato2);
                        LocalDate dataFinPromo = LocalDate.parse(jsonAbe4001.getString("data_promo_fin"), formato2);
                        LocalDate dataAtual = LocalDate.now();
                        def precoPromocao = jsonAbe4001.getBigDecimal_Zero("preco_promocao");
                        if(dataAtual >= dataIniPromo && dataAtual <= dataFinPromo){
                            eaa0103.eaa0103unit = precoPromocao.round(4);
                        }else{
                            eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
                        }
                    }else{
                        eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
                    }
                }else{
                    eaa0103.eaa0103unit = abe4001.abe4001preco.round(4)
                }
            }
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
                jsonEaa0103.getBigDecimal_Zero("outras") -
                jsonEaa0103.getBigDecimal_Zero("desconto") -
                jsonEaa0103.getBigDecimal_Zero("pis") -
                jsonEaa0103.getBigDecimal_Zero("cofins") -
                jsonEaa0103.getBigDecimal_Zero("icms"));

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
        jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") / 100));
        jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("vlr_cbs").round(2));


        // Aliquotas IBS
        jsonEaa0103.put("ibs_uf_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_uf_aliq"));//Alíquota IBS Estadual
        jsonEaa0103.put("ibs_mun_aliq", jsonAag0201Ent.getBigDecimal_Zero("ibs_mun_aliq"));

        // IBS Municipio
        jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") / 100));
        jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun").round(2));

        //IBS UF
        jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") / 100))//IBS Estadual
        jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf").round(2));


        jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))// total de IBS
        jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibs").round(2));

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
            if(jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_mun")){
                jsonEaa0103.put("perc_red_ibs_mun", jsonAaj07clasTrib.getBigDecimal_Zero("perc_red_ibs_mun")) // Criar campo
            }

            // Aliquotas Efetivas
            jsonEaa0103.put("aliq_efet_ibs_uf", (jsonEaa0103.getBigDecimal_Zero("ibs_uf_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_uf")) / 100)); // Mudar nome campo
            jsonEaa0103.put("aliq_efet_ibs_mun", (jsonEaa0103.getBigDecimal_Zero("ibs_mun_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_ibs_mun")) / 100));
            jsonEaa0103.put("aliq_efet_cbs", (jsonEaa0103.getBigDecimal_Zero("cbs_aliq") * ( 100 -  jsonEaa0103.getBigDecimal_Zero("perc_red_cbs")) / 100));

            // CBS
            jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_cbs") / 100))
            jsonEaa0103.put("vlr_cbs", jsonEaa0103.getBigDecimal_Zero("vlr_cbs").round(2));

            // IBS Município
            jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_munic") / 100));
            jsonEaa0103.put("vlr_ibsmun", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun").round(2));

            // IBS UF
            jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("cbs_ibs_bc") * (jsonEaa0103.getBigDecimal_Zero("aliq_efet_ibs_uf") / 100))//IBS Estadual
            jsonEaa0103.put("vlr_ibsuf", jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf").round(2))

            // Soma total do IBS UF/Municipio
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibsmun") + jsonEaa0103.getBigDecimal_Zero("vlr_ibsuf"))// total de IBS
            jsonEaa0103.put("vlr_ibs", jsonEaa0103.getBigDecimal_Zero("vlr_ibs").round(2))

        }

        if(jsonAaj07clasTrib.getInteger("exige_tributacao") == 0){ // Zera impostos caso não exige tributação
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
    private void calcularPesoBrutoeLiquido(){
        if(jsonEaa0103.getString("umv") == "KG"){
            def taraEmb = jsonAbm0101.getBigDecimal_Zero("tara_emb_") * jsonAbm0101.getBigDecimal_Zero("cvdnf");
            def taraCaixa = jsonAbm0101.getBigDecimal_Zero("tara_caixa");
            def taraTotal = taraEmb + taraCaixa;

            // Peso Bruto Para Itens em KG
            if(jsonEaa0103.getString("umv") == 'KG'){
                jsonEaa0103.put("peso_bruto", jsonEaa0103.getBigDecimal_Zero("qt_convertida") + taraTotal )
            }

            // Peso Líquido Para Itens em KG
            if(jsonEaa0103.getString("umv") == 'KG'){
                jsonEaa0103.put("peso_liquido", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
            }
        }else{
            // Peso Bruto
            if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));

            // Peso Líquido
            if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
        }
    }

    private void converterQuantidadeItem(String grupo){
        //Define Quantidade Comercial como Quantidade Convertida
        jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
//
//        if(jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Quantidade de produto por caixa no cadastro do item "+abm01.abm01codigo+ " é inválido! Necessário um valor maior que Zero!");
//
//        if(jsonEaa0103.getString("umv") != 'KG' && grupo != "LEITE"){
//            //Troca a Unidade de medida de acordo com o parâmetro na entidade
//            if(jsonAbe01.getBigDecimal_Zero("unidade_caixa") == 1 ){
//                jsonEaa0103.put("umv", 'CX');
//                jsonEaa0103.put("umf", 'CX');
//                //eaa0103.eaa0103umu == 'CX';
//
//                jsonEaa0103.put("qt_convertida", (eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf")).round(2));
//            }
//        }
    }

    private void converterUnitarioItem(String grupo){
        // Define o unitário convertido como o unitário do item
        jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
//        if(jsonEaa0103.getString("umv") == 'CX' && grupo != "LEITE"){
//            def valrUnit = eaa0103.eaa0103unit;
//            def fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf");
//            jsonEaa0103.put("unitario_conv", (eaa0103.eaa0103unit * fatorUM))//.round(2));
//        }else{
//            jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
//        }
    }

    private void converterTotalItem(){
        //Total Item Convertido
        jsonEaa0103.put("total_conv", eaa0103.eaa0103total)
    }

    private void definirQuantidadeCaixaFrasco(){
        // Quantidade Caixa = Quantidade Convertida
        jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));

        // Quantidade de caixa e frasco (Unidade de Medida KG)
        if(jsonEaa0103.getString("umv") == 'KG'){
            def caixa = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("peso_caixa");
            def caixaArredondada = new BigDecimal(caixa).setScale(0,BigDecimal.ROUND_UP)
            jsonEaa0103.put("caixa", caixaArredondada);
            jsonEaa0103.put("frasco", new BigDecimal(0));
        }else{
            def caixa = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf")
            def frasco = eaa0103.eaa0103qtComl.toInteger() % jsonAbm0101.getBigDecimal_Zero("cvdnf").toInteger()
            def caixaArredondada = new BigDecimal(caixa).setScale(0,BigDecimal.ROUND_UP)
            jsonEaa0103.put("caixa", caixaArredondada);
            jsonEaa0103.put("frasco", frasco);
        }
    }

    private calcularIPI(){

        if (eaa0103.eaa0103cstIpi != null) {
            //BC de IPI = Total do Item + Frete + Seguro + Despesas Acessorias
            jsonEaa0103.put("bc_ipi", eaa0103.eaa0103total +
                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
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
                jsonEaa0103.put("bc_ipi",0.0000000);
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
    }

    private void calcularICMS(def dentroEstado, def grupo){
        if(jsonEaa0103.getBigDecimal_Zero("_icms") != -1){
            def vlrReducao = 0;
            // BC ICMS
            jsonEaa0103.put("bc_icms", 	eaa0103.eaa0103total +
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

            // ICMS
            jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));

        }else{
            def bcICMs = eaa0103.eaa0103total +
                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                    jsonEaa0103.getBigDecimal_Zero("outras_despesas");

            jsonEaa0103.put("icms", new BigDecimal(0));
            jsonEaa0103.put("_icms", new BigDecimal(0));
            jsonEaa0103.put("icms_outras", bcICMs);
            jsonEaa0103.put("bc_icms", new BigDecimal(0));
            jsonEaa0103.put("_red_bc_icms", new BigDecimal(0));
            jsonEaa0103.put("icms_isento", new BigDecimal(0));
        }

        // Zera os impostos do leite se dentro do estado
        if(dentroEstado && grupo == 'LEITE'){
            jsonEaa0103.put("bc_icms",new BigDecimal(0));
            jsonEaa0103.put("_red_bc_icms",new BigDecimal(0));
            jsonEaa0103.put("_icms",new BigDecimal(0));
            jsonEaa0103.put("icms", new BigDecimal(0));
            jsonEaa0103.put("icms_outras",new BigDecimal(0));
            jsonEaa0103.put("bc_icms", new BigDecimal(0) );
            jsonEaa0103.put("bc_icms_st", new BigDecimal(0) );
            jsonEaa0103.put("_icms_st", new BigDecimal(0));
            jsonEaa0103.put("icms_st", new BigDecimal(0) );
            jsonEaa0103.put("_red_bc_icms", new BigDecimal(0));
            jsonEaa0103.put("icms_isento", new BigDecimal(0));
        }
    }

    private void calcularIcmsST(){
//        def ivaST = 0;
//        if(jsonEaa0103.getBigDecimal_Zero("_icms_st") != -1){
//            if(jsonAbe01 != null){
//                if(jsonAbe01.getBigDecimal_Zero("calcula_st") == 1 ){
//
//                    //BC ICMS ST
//                    jsonEaa0103.put("bc_icms_st", (eaa0103.eaa0103total+
//                            jsonEaa0103.getBigDecimal_Zero("frete_dest") +
//                            jsonEaa0103.getBigDecimal_Zero("seguro") +
//                            jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
//                            jsonEaa0103.getBigDecimal_Zero("ipi")).round(2));
//
//                    // Aliquota de ICMS ST
//                    if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno") != null) jsonEaa0103.put("_icms_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno"));
//
//                    // Taxa IVA cadastro Item
//                    ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st");
//
//                    // Soma o IVA do item na base de cálculo de ICMS
//                    if(ivaST > 0){
//                        // BC ICMS ST com IVA
//                        jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") + (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * ( ivaST / 100 ))).round(2) );
//
//                        // ICMS ST com IVA
//                        jsonEaa0103.put("icms_st", ((jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (jsonEaa0103.getBigDecimal_Zero("_icms_st")/ 100))- jsonEaa0103.getBigDecimal_Zero("icms") ).round(2));
//                    }else{
//                        jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
//                        jsonEaa0103.put("icms_st", new BigDecimal(0));
//                        jsonEaa0103.put("_icms_st", new BigDecimal(0));
//                    }
//
//                    // Define Tx. Iva do Item no documento
//                    if(jsonEaa0103.getBigDecimal_Zero("bc_icms_st") > 0){
//                        jsonEaa0103.put("tx_iva_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st"));
//                    }else{
//                        jsonEaa0103.put("tx_iva_st", new BigDecimal(0));
//                    }
//
//                    // Zera ICMS ST caso entidade está caracterizada para não calcular
//                    if(jsonAbe01.getBigDecimal_Zero("isento_st") == 1){
//                        jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
//                        jsonEaa0103.put("icms_st", new BigDecimal(0));
//                        jsonEaa0103.put("_icms_st", new BigDecimal(0));
//                    }
//
//                }else{
//                    jsonEaa0103.put("bc_icms_st",new BigDecimal(0));
//                    jsonEaa0103.put("_icms_st",new BigDecimal(0));
//                    jsonEaa0103.put("icms_st",new BigDecimal(0));
//                    jsonEaa0103.put("tx_iva_st", new BigDecimal(0));
//                }
//            }
//        }else{
//            jsonEaa0103.put("bc_icms_st",new BigDecimal(0));
//            jsonEaa0103.put("_icms_st",new BigDecimal(0));
//            jsonEaa0103.put("icms_st",new BigDecimal(0));
//            jsonEaa0103.put("tx_iva_st", new BigDecimal(0));
//        }

        jsonEaa0103.put("bc_icms_st",new BigDecimal(0));
        jsonEaa0103.put("_icms_st",new BigDecimal(0));
        jsonEaa0103.put("icms_st",new BigDecimal(0));
        jsonEaa0103.put("tx_iva_st", new BigDecimal(0));

    }
    private void definirCFOP(){

        if(abd02.abd02cfop != null){
            aaj15_cfop = getSession().get(Aaj15.class, abd02.abd02cfop.aaj15id);
        }else if(jsonAbm1001_UF_Item.getString("cfop_saida") != null){
            def cfop = jsonAbm1001_UF_Item.getString("cfop_saida");

            aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));

            if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrado o CFOP com o código " + cfop );
        }else{
            throw new ValidacaoException("Não foi informado o CFOP no PCD ou nos parâmetros do item " + abm01.abm01codigo + " - " + abm01.abm01na + " ")
        }

        if(aae20 != null && aae20.aae20codigo == '001'){
            aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", '5911'));

            if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrado CFOP com código 5911 cadastrado no sistema.")
        }

        eaa0103.eaa0103cfop = aaj15_cfop;
    }

    private trocarPrimeiroDigitoCFOP(def dentroEstado, String cfop){
        def cfopItem = cfop;
        def primeiroDigito = cfopItem.substring(0,1);
        def restoCFOP = cfopItem.substring(1)

        // Troca o primeiro digito do CFOP de 1 para 2 ou de 5 para 6
        if(!dentroEstado) primeiroDigito = primeiroDigito == '1' ? '2' : '6';

        def cfopAlterado = primeiroDigito + restoCFOP;

        aaj15_cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfopAlterado));

        if(aaj15_cfop == null) throw new ValidacaoException("Não foi encontrado o CFOP com o código " + cfop);

        eaa0103.eaa0103cfop = aaj15_cfop;
    }

    private calcularPis(){
        if(jsonAbm0101.getBigDecimal_Zero("_pis") > 0){
            jsonEaa0103.put("bc_pis", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st") - jsonEaa0103.getBigDecimal_Zero("icms"));
            if(jsonEaa0103.getBigDecimal_Zero("_pis") != -1){
                jsonEaa0103.put("_pis", jsonAbm0101.getBigDecimal_Zero("_pis"));
                jsonEaa0103.put("pis", (jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("_pis")) / 100 );
                jsonEaa0103.put("pis", jsonEaa0103.getBigDecimal_Zero("pis").round(2));
                //Obtendo CST de Pis e Cofins
                eaa0103.eaa0103cstPis = getSession().get(Aaj12.class, Criterions.eq("aaj12codigo", "01"));
                eaa0103.eaa0103cstCofins = getSession().get(Aaj13.class, Criterions.eq("aaj13codigo", "01"));
            }else{
                jsonEaa0103.put("_pis", new BigDecimal(0));
                jsonEaa0103.put("bc_pis", new BigDecimal(0));
            }
        }
    }

    private calcularCofins(){
        if(jsonAbm0101.getBigDecimal_Zero("_cofins") > 0){
            jsonEaa0103.put("bc_cofins", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("cofins") - jsonEaa0103.getBigDecimal_Zero("icms_st") - jsonEaa0103.getBigDecimal_Zero("icms"));
            if(jsonEaa0103.getBigDecimal_Zero("_cofins") > -1){
                jsonEaa0103.put("_cofins", jsonAbm0101.getBigDecimal_Zero("_cofins"));
                jsonEaa0103.put("cofins", (jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("_cofins")) / 100 );
                jsonEaa0103.put("cofins", jsonEaa0103.getBigDecimal_Zero("cofins").round(2));
            }else{
                jsonEaa0103.put("cofins", 0.000000);
                jsonEaa0103.put("bc_cofins", 0.000000 );
            }
        }
    }
    private calcularFundoDeCombatePobreza(){
        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 ){
            jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
            jsonEaa0103.put("icms_fcp_", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
            jsonEaa0103.put("vlr_icms_fcp_", (jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp_") / 100).round(2));
        }
    }
    private calcularDifal(def dentroEstado){
        /*
            DIFAL = (Aliquota interna RJ−Alıiquota interestadual) × Base de Calculo
         */
        if((eaa0102.eaa0102consFinal == 1 && eaa0102.eaa0102contribIcms == 0) || (eaa0102.eaa0102consFinal == 0 && eaa0102.eaa0102contribIcms == 0 )){
            if(!dentroEstado){
                BigDecimal icmsInterestadual = jsonEaa0103.getBigDecimal_Zero("icms");

                BigDecimal icmsInterno = (eaa0103.eaa0103totDoc * (jsonAag02Ent.getBigDecimal_Zero("txicminterna") / 100)).round(2);

                BigDecimal difal = (icmsInterno - icmsInterestadual).round(2);

                jsonEaa0103.put("bc_difal", eaa0103.eaa0103total);
                jsonEaa0103.put("aliq_icms_inter", jsonAag02Ent.getBigDecimal_Zero("txicminterna"));
                jsonEaa0103.put("vlr_difal", difal + jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_"));
                jsonEaa0103.put("icms_inter_part", 100);
            }
        }else{
            jsonEaa0103.put("bc_difal", new BigDecimal(0));
            jsonEaa0103.put("aliq_icms_inter", new BigDecimal(0));
            jsonEaa0103.put("vlr_difal", new BigDecimal(0));
        }

    }

    private void calcularCargaTributaria(){
        // Busca campo customizado no cadastro de NCM para encontrar aliquota da carga tributária
        def sql = "select abg01camposcustom from abg01 "+
                "inner join abm0101 on abm0101ncm = abg01id "+
                "inner join abm01 on abm01id = abm0101item "+
                "where abm01id = :abm01id "+
                "AND abm01tipo = :eaa0103tipo "+
                "AND abm0101empresa = :aac10id";

        def aliqCargaTrib;

        if(abm0101.abm0101ncm == null) throw new ValidacaoException("Não foi informado NCM no cadastro do item "+abm01.abm01codigo + " para calculo da carga tributária.  ")

        TableMap abg01camposCustom = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id",abm01.abm01id), Parametro.criar("eaa0103tipo", eaa0103.eaa0103tipo), Parametro.criar("aac10id",aac10.aac10id)).getTableMap("abg01camposcustom");

        if(abg01camposCustom == null) throw new ValidacaoException("Valor da aliquota dos tributos no cadastro do NCM " + abm0101.abm0101ncm.abg01codigo + " não é válido para cálculo da carga tributária.")


        //Alíquota Carga Tributária
        aliqCargaTrib = abg01camposCustom.getBigDecimal_Zero("_carga_trib");
        jsonEaa0103.put("carga_trib_", aliqCargaTrib);

        //BC Carga Tributaria
        jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total);

        // Carga Tributaria
        if(jsonEaa0103.getBigDecimal_Zero("carga_trib_") > 0){
            jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);
            jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("VlrCargaTrib").round(2));
        }
    }

    private void calcularDiferimento(){
        if(jsonAbm1001_UF_Item.getBigDecimal_Zero("diferimento") > 0){

            jsonEaa0103.put("_diferimento", jsonAbm1001_UF_Item.getBigDecimal_Zero("diferimento"));

            // Troca ST para 051 para itens com diferimento
            if(jsonEaa0103.getBigDecimal_Zero("_diferimento") > 0 ) eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "051"));

        }else{
            jsonEaa0103.put("_diferimento", 0.000000);
        }
    }


    private definirCstICMS(){
        // CST ICMS B
        if (abd02.abd02cstIcmsB != null) {
            eaa0103.eaa0103cstIcms = aaj10_cstIcmsB;
        } else {
            eaa0103.eaa0103cstIcms = aaj10_cstIcms
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
            throw new ValidacaoException("Dia de faturamento "+ diaFaturamento.toUpperCase() +" cadastrado no municipio " + municipioEnt.aag0201nome + " não é válidado ")
        }

        LocalDate data;

        if(eaa0102.eaa0102despacho != null && eaa0102.eaa0102redespacho != null){ // Documento com Redespacho
            LocalDate dataAux = mesmoDia == 0 ? dataAtual.plusDays(1) : dataAtual;
            String diaSemama = buscarDiaSemana(dataAux.getDayOfWeek().toString());

            Integer count = 0;
            while(!diaFaturamento.toUpperCase().contains(diaSemama.toUpperCase()) ){
                dataAux = dataAux.plusDays(1)
                diaSemama = buscarDiaSemana(dataAux.getDayOfWeek().toString());

                count++

                if(count > 20) throw new ValidacaoException("Processo interrompido para não consumir aumentar processamento! Verificar formula de data de entrega programada.")
            }

            data = somarDiasUteis(dataAux, prazoEntrega + 1, feriados); // Soma o prazo de entrega mais um dia do redespacho

        }else{ // Documentos somente com despacho
            LocalDate dataAux = mesmoDia == 0 ? dataAtual.plusDays(1) : dataAtual;
            String diaSemama = buscarDiaSemana(dataAux.getDayOfWeek().toString());
            Integer count = 0;
            while(!diaFaturamento.toUpperCase().contains(diaSemama.toUpperCase()) ){
                dataAux = dataAux.plusDays(1)
                diaSemama = buscarDiaSemana(dataAux.getDayOfWeek().toString());

                count++

                if(count > 20) throw new ValidacaoException("Processo interrompido para não consumir aumentar processamento! Verificar formula de data de entrega programada.")
            }

            data = somarDiasUteis(dataAux, 1, feriados);

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
                // Se houver dia faturamento cadastrado, calcula a data de entrega com base nos dias da semana cadastrado nos municipios
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


    private void preencherSPED(){
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



    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==