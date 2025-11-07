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


public class DevolucaoDeVendaGeral extends FormulaBase {

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

        //Dados da Entidade
        abe01 = getSession().get(Abe01.class, abb01.abb01ent.abe01id);

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
        if (abe4001 == null && eaa01.eaa01tp != null) throw new ValidacaoException("Item Não Encontrado Na Tabela De Preço!")


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

        // CFOP
        if (eaa0103.eaa0103cfop == null) eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "1410"));

        if (eaa0103.eaa0103qtComl > 0) {

            // Define o campo ordem de separação de acordo com o cadastro do item
            jsonEaa0103.put("ordem_separacao", jsonAbm0101.getBigDecimal_Zero("ordem_separacao"))

            // Define a Especie do Documento
            eaa0102.eaa0102especie = "Volumes"

            // Ordem de Separação
            jsonEaa0103.put("ordem_separacao", jsonAbm0101.getBigDecimal_Zero("ordem_separacao"));

            // Define a categoria dos itens
            String grupo = buscarCategoriaItem(abm01.abm01codigo);

            // Quantidade Original
            if (jsonEaa0103.getBigDecimal_Zero("qt_original") == 0) {
                jsonEaa0103.put("qt_original", eaa0103.eaa0103qtComl);
            }

            //Define o Campo de Unitário para Estoque
            jsonEaa0103.put("unitario_estoque", eaa0103.eaa0103unit);

            // Quantidade de Uso
            eaa0103.eaa0103qtUso = eaa0103.eaa0103qtComl;

            //Define o preço unitário de acordo com a Tabela de Preço
            definirPrecoUnitarioItem();

            //==================================================================================================================================================
            //============================================= CALCULO PARA OS ITENS COM CRITERIO DE SELEÇÃO "QUEIJO" =============================================
            //==================================================================================================================================================

            if (grupo == "QUEIJO") {

                // Converte a quantidade dos Itens
                converterQuantidadeItem(grupo)

                // Converte unitário Item
                converterUnitarioItem(grupo)

                // Calcula o Peso Bruto dos itens
                calcularPesoBrutoeLiquido();

                // Define quantidade de caixa e frasco
                definirQuantidadeCaixaFrasco()

                // Volumes
                jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

                // Define Total Convertido dos itens
                converterTotalItem()

                // Total do item
                eaa0103.eaa0103total = ((eaa0103.eaa0103qtComl * eaa0103.eaa0103unit) - jsonEaa0103.getBigDecimal_Zero("desconto")).round(2);

                // Calcula ICMS
                calcularICMS();

                // Calcula ICMS ST
                calcularICMSST()

                // Calcula FCP
                calcularFCP()

                // Define o CST de ICMS dos itens
                definirCstICMS(dentroEstado)

                // Define o CFOP dos itens
                definirCFOP()

                // ==================
                // TOTAL DO DOCUMENTO
                // ==================
                //Total Doc = Total Item + IPI + Frete + Seguro + Outras Despesas + ICMS ST - Desconto incond
                eaa0103.eaa0103totDoc = eaa0103.eaa0103total  +
                                        jsonEaa0103.getBigDecimal_Zero("ipi") +
                                        jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp") +
                                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
                                        jsonEaa0103.getBigDecimal_Zero("icms_st") -
                                        jsonEaa0103.getBigDecimal_Zero("desconto");

                eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 4);


                // Calculo Zona Franca Manaus
                calculaZonaFrancaManaus()

                // Ajusta o valor do IPI Outras
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Valor do Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

                // Base de Calculo de Comissão
                calcularComissao(abm01.abm01codigo, grupo);

                // Preenche os SPEDS dos itens
                preencherSPEDS()

            }
            
            //==================================================================================================================================================
            //============================================= CALCULO PARA OS ITENS COM CRITERIO DE SELEÇÃO "LEITE" =============================================
            //==================================================================================================================================================

            if (grupo == "LEITE") {

                // Converte a quantidade dos Itens
                converterQuantidadeItem(grupo)

                // Calcula o Peso Bruto dos itens
                calcularPesoBrutoeLiquido();

                // Converte unitário Item
                converterUnitarioItem(grupo)

                // Quantidade de Caixa e Frasco
                definirQuantidadeCaixaFrasco()

                // Volumes
                jsonEaa0103.put("volumes", eaa0103.eaa0103qtComl);

                // Total do item
                eaa0103.eaa0103total = ((eaa0103.eaa0103qtComl * eaa0103.eaa0103unit) - jsonEaa0103.getBigDecimal_Zero("desconto")).round(2);

                //Total Item Convertido
                converterTotalItem();

                // Calcula IPI
                calcularIPI();

                //Zera ICMS
                jsonEaa0103.put("bc_icms", new BigDecimal(0));
                jsonEaa0103.put("icms", new BigDecimal(0));

                // PIS
                calcularPIS()

                // COFINS
                calcularCOFINS()

                // ==================
                // TOTAL DO DOCUMENTO
                // ==================
                eaa0103.eaa0103totDoc = (eaa0103.eaa0103total +
                                        jsonEaa0103.getBigDecimal_Zero("ipi") +
                                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
                                        jsonEaa0103.getBigDecimal_Zero("icms_st")).round(2);

                // Calculo para Zona Franca Manaus
                calculaZonaFrancaManaus()

                // Diferimento
                calcularDiferimento()

                // CFOP
                definirCFOP()

                //Total Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

                // Base de Calculo de Comissão
                calcularComissao(abm01.abm01codigo, grupo)

                // Preenche SPEDS
                preencherSPEDS()

            }

            //==================================================================================================================================================
            //============================================= CALCULO PARA OS ITENS COM CRITERIO DE SELEÇÃO "IOGURTE" =============================================
            //==================================================================================================================================================

            if (grupo == "IOGURTE" || grupo == "BAUNILHA") {

                // Converte a quantidade dos Itens
                converterQuantidadeItem(grupo)

                // Calcula o Peso Bruto dos itens
                calcularPesoBrutoeLiquido();

                // Converte unitário Item
                converterUnitarioItem(grupo);

                // Caixa e Frasco
                definirQuantidadeCaixaFrasco()

                // Volumes
                jsonEaa0103.put("volumes", jsonEaa0103.getBigDecimal_Zero("caixa"));

                // Total do item
                eaa0103.eaa0103total = ((eaa0103.eaa0103qtComl * eaa0103.eaa0103unit) - jsonEaa0103.getBigDecimal_Zero("desconto")).round(2);

                //Total Item Convertido
                converterTotalItem()

                // ICMS
                calcularICMS();

                // ICMS ST
                calcularICMSST();

                // Troca CST
                definirCstICMS(dentroEstado);

                // PIS
                calcularPIS();

                // COFINS
                calcularCOFINS();

                // CFOP
                definirCFOP()

                //Total Documento
                eaa0103.eaa0103totDoc = eaa0103.eaa0103total +
                        jsonEaa0103.getBigDecimal_Zero("icms_st") +
                        jsonEaa0103.getBigDecimal_Zero("ipi") +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
                        jsonEaa0103.getBigDecimal_Zero("vlr_icms_fcp_") -
                        jsonEaa0103.getBigDecimal_Zero("desconto");

                eaa0103.eaa0103totDoc = round(eaa0103.eaa0103totDoc, 2)

                // Carga Tributária
                calcularCargaTributaria()

                // Zona Franca de Manaus
                calculaZonaFrancaManaus();


                // Ajusta o valor do IPI Outras
                jsonEaa0103.put("ipi_outras", eaa0103.eaa0103totDoc);

                //Financeiro
                eaa0103.eaa0103totFinanc = eaa0103.eaa0103totDoc;

                // Base de Calculo de Comissão
                calcularComissao(abm01.abm01codigo, grupo);

                // Preenche SPEDS
                preencherSPEDS()

            }
        }

    }

    private String buscarCategoriaItem(codItem) {

        Query descrCriterios = getSession().createQuery("select aba3001descr from aba3001 " +
                "inner join abm0102 on abm0102criterio = aba3001id and aba3001criterio = 542858 " +
                "inner join abm01 on abm0102item = abm01id " +
                "where abm01codigo = '" + codItem + "'" +
                "and abm01tipo = 1 ");

        List<TableMap> listCriterios = descrCriterios.getListTableMap();

        if (listCriterios == null && listCriterios.size() == 0) throw new ValidacaoException("Não foi encontrado critério de seleção de grupo para o item " + codItem);

        String grupo = "";

        for (TableMap criterio : listCriterios) {
            if (criterio.getString("aba3001descr").toUpperCase().contains("QUEIJO")) {
                grupo = "QUEIJO"
            }
            if (criterio.getString("aba3001descr").toUpperCase().contains("LEITE")) {
                grupo = "LEITE"
            }

            if (criterio.getString("aba3001descr").toUpperCase().contains("IOGURTE") || criterio.getString("aba3001descr").toUpperCase().contains("BAUNILHA")) {
                grupo = "IOGURTE"
            }
        }

        return grupo;

    }

    private void definirPrecoUnitarioItem() {
        if (eaa01.eaa01tp != null) {
            if (jsonAbe4001 != null) {
                if (jsonAbe4001.getString("data_promo_ini") != null && jsonAbe4001.getString("data_promo_fin") != null && jsonAbe4001.getBigDecimal_Zero("preco_promocao") > 0) {
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

    private void converterQuantidadeItem(String grupo) {
        if (eaa0103.eaa0103umComl.aam06codigo == 'CX' && grupo != "LEITE") {
            if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Quantidade de itens por caixa no cadastro do item " + abm01.abm01codigo + " é inválida. Necessário um valor diferente de zero.")
            jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
        } else {
            jsonEaa0103.put("qt_convertida", eaa0103.eaa0103qtComl);
        }
    }

    private void converterUnitarioItem(String grupo) {
        if (eaa0103.eaa0103umComl.aam06codigo == 'CX' && grupo != "LEITE") {
            def valrUnit = eaa0103.eaa0103unit;
            def fatorUM = jsonAbm0101.getBigDecimal_Zero("cvdnf");
            jsonEaa0103.put("unitario_conv", (eaa0103.eaa0103unit * fatorUM))//.round(2));
        } else {
            jsonEaa0103.put("unitario_conv", eaa0103.eaa0103unit);
        }
    }

    private void converterTotalItem() {
        //Total Item Convertido
        jsonEaa0103.put("total_conv", (jsonEaa0103.getBigDecimal_Zero("unitario_conv") * jsonEaa0103.getBigDecimal_Zero("qt_convertida")).round(2))
    }

    private void definirQuantidadeCaixaFrasco() {
        // Quantidade Caixa = Quantidade Convertida
        jsonEaa0103.put("caixa", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));

        // Quantidade de caixa e frasco (Unidade de Medida KG)
        if (eaa0103.eaa0103umComl.aam06codigo == 'KG') {
            if (jsonAbm0101.getBigDecimal_Zero("peso_caixa") == 0) throw new ValidacaoException("O valor do campo Peso da Caixa no cadastro do item " + abm01.abm01codigo + " - " + abm01.abm01na + " é inválido.")
            def caixa = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("peso_caixa");
            def caixaArredondada = new BigDecimal(caixa).setScale(0, BigDecimal.ROUND_UP)
            jsonEaa0103.put("caixa", caixaArredondada);
            jsonEaa0103.put("frasco", new BigDecimal(0));
        } else {
            if (jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("O valor do campo Itens Por Caixa no cadastro do item " + abm01.abm01codigo + " - " + abm01.abm01na + " é inválido.")
            def caixa = eaa0103.eaa0103qtComl / jsonAbm0101.getBigDecimal_Zero("cvdnf")
            def frasco = eaa0103.eaa0103qtComl.toInteger() % jsonAbm0101.getBigDecimal_Zero("cvdnf").toInteger()
            def caixaArredondada = new BigDecimal(caixa).setScale(0, BigDecimal.ROUND_UP)
            jsonEaa0103.put("caixa", caixaArredondada);
            jsonEaa0103.put("frasco", frasco);
        }
    }

    private void calcularPesoBrutoeLiquido() {
        if (eaa0103.eaa0103umComl.aam06codigo == "KG") {
            def taraEmb = jsonAbm0101.getBigDecimal_Zero("tara_emb_") * jsonAbm0101.getBigDecimal_Zero("cvdnf");
            def taraCaixa = jsonAbm0101.getBigDecimal_Zero("tara_caixa");
            def taraTotal = taraEmb + taraCaixa;

            // Peso Bruto Para Itens em KG
            if (eaa0103.eaa0103umComl.aam06codigo == 'KG') {
                jsonEaa0103.put("peso_bruto", jsonEaa0103.getBigDecimal_Zero("qt_convertida") + taraTotal)
            }

            // Peso Líquido Para Itens em KG
            if (eaa0103.eaa0103umComl.aam06codigo == 'KG') {
                jsonEaa0103.put("peso_liquido", jsonEaa0103.getBigDecimal_Zero("qt_convertida"));
            }
        } else {
            // Peso Bruto
            if (abm01.abm01pesoLiq_Zero > 0) jsonEaa0103.put("peso_bruto", (eaa0103.eaa0103qtUso * abm01.abm01pesoBruto).round(4));

            // Peso Líquido
            if (abm01.abm01pesoBruto_Zero > 0) jsonEaa0103.put("peso_liquido", (eaa0103.eaa0103qtUso * abm01.abm01pesoLiq).round(4));
        }
    }

    private calcularICMS() {
        def vlrReducao = 0;
        if (jsonEaa0103.getBigDecimal_Zero("_icms") != -1) {

            //BC ICMS = Valor do Item + Frete + Seguro + Outras Desp. - Desconto Incondicional
            jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("total_conv") +
                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                    jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                    jsonEaa0103.getBigDecimal_Zero("desconto"));

            // Aliquota Redução
            if (abe01.abe01contribIcms == 1) {
                if (jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms") != 0) {
                    jsonEaa0103.put("_red_bc_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_red_bc_icms"));
                }
            }

            // Calculo da Redução
            if (jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0) {
                vlrReducao = ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_red_bc_icms")) / 100).round(2);
                jsonEaa0103.put("bc_icms", jsonEaa0103.getBigDecimal_Zero("bc_icms") - vlrReducao);
            }

            // Obter a Aliquota de ICMS
            if (jsonEaa0103.getBigDecimal_Zero("_icms") == 0) {
                if (jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms") != 0) {
                    jsonEaa0103.put("_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fixa_icms"));
                }
            }

            // ICMS
            jsonEaa0103.put("icms", ((jsonEaa0103.getBigDecimal_Zero("bc_icms") * jsonEaa0103.getBigDecimal_Zero("_icms")) / 100).round(2));


        } else {
            jsonEaa0103.put("icms", 0);
            jsonEaa0103.put("icms_outras", jsonEaa0103.getBigDecimal_Zero("bc_icms") + vlrReducao);
            jsonEaa0103.put("bc_icms", 0);
            jsonEaa0103.put("_red_bc_icms", 0);
            jsonEaa0103.put("icms_isento", 0);
            vlrReducao = 0;
        }

    }

    private void calcularICMSST() {
        if (jsonEaa0103.getBigDecimal_Zero("_icms_st") != -1 && jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno") > 0) {
            if (jsonAbe01.getBigDecimal_Zero("calcula_st") == 1) {
                //BC ICMS ST
                jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("total_conv") +
                        jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                        jsonEaa0103.getBigDecimal_Zero("seguro") +
                        jsonEaa0103.getBigDecimal_Zero("outras_despesas") +
                        jsonEaa0103.getBigDecimal_Zero("ipi") -
                        jsonEaa0103.getBigDecimal_Zero("desconto")).round(2));

                //Alíquota do ICMS ST
                jsonEaa0103.put("_icms_st", jsonAbm1001_UF_Item.getBigDecimal_Zero("_icms_interno"));

                def ivaST = 0;
                // Taxa IVA cadastro Item
                ivaST = jsonAbm1001_UF_Item.getBigDecimal_Zero("tx_iva_st");

                if (ivaST > 0 && jsonEaa0103.getBigDecimal_Zero("_icms_st") != -1) {
                    //Adiciona IVA a Base de Cálculo do ICMS ST
                    jsonEaa0103.put("bc_icms_st", (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") + (jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (ivaST / 100))).round(2));

                    //Cálcula ICMS ST
                    //ICMS ST = Base * Alíquota Interna Estado de Destino - Valor Icms Normal
                    jsonEaa0103.put("icms_st", ((jsonEaa0103.getBigDecimal_Zero("bc_icms_st") * (jsonEaa0103.getBigDecimal_Zero("_icms_st") / 100)) - jsonEaa0103.getBigDecimal_Zero("icms")).round(2));
                }
            }
        } else {
            jsonEaa0103.put("bc_icms_st", new BigDecimal(0));
            jsonEaa0103.put("_icms_st", new BigDecimal(0));
            jsonEaa0103.put("icms_st", new BigDecimal(0));
        }
    }

    private calcularFCP() {
        if (jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp") > 0 && jsonEaa0103.getBigDecimal_Zero("icms_st") > 0) {
            jsonEaa0103.put("bc_fcp", jsonEaa0103.getBigDecimal_Zero("bc_icms_st"));
            jsonEaa0103.put("icms_fcp", jsonAbm1001_UF_Item.getBigDecimal_Zero("_fcp"));
            jsonEaa0103.put("vlr_icms_fcp_", (jsonEaa0103.getBigDecimal_Zero("bc_fcp") * jsonEaa0103.getBigDecimal_Zero("icms_fcp") / 100).round(2));
        } else {
            jsonEaa0103.put("bc_fcp", 0.000000);
            jsonEaa0103.put("icms_fcp", 0.000000);
            jsonEaa0103.put("vlr_icms_fcp_", 0.000000)
        }
    }

    private definirCstICMS(def dentroEstado) {
        // Troca CST
        if (dentroEstado) {
            if (jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "070"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else if (jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && (jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0 || jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == -1)) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "010"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else if (jsonEaa0103.getBigDecimal_Zero("icms_st") == 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "020"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else {
                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;
            }
        } else {
            if (jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "070"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else if (jsonEaa0103.getBigDecimal_Zero("icms_st") > 0 && (jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == 0 || jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") == -1)) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "010"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else if (jsonEaa0103.getBigDecimal_Zero("icms_st") == 0 && jsonEaa0103.getBigDecimal_Zero("_red_bc_icms") > 0) {

                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "020"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;

            } else {
                //CST x00 - Mercadoria Tributada Integralmente
                aaj10_cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "000"));
                eaa0103.eaa0103cstIcms = aaj10_cstIcms;
            }
        }
    }

    private void definirCFOP() {
        // CFOP
        def cfop = jsonAbm1001_UF_Item.getString("cfop_entrada");

        if (cfop == null) throw new ValidacaoException("Não foi informado CFOP no parâmetro itens-valores. Item:  " + abm01.abm01codigo);


        eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", cfop));

    }

    private void calculaZonaFrancaManaus() {
        // Calculo para Zona Franca Manaus
        def alc = jsonAag0201Ent.getInteger("munic_alc");
        def zfm = jsonAag0201Ent.getInteger("munic_zfm");
        if (alc == null) alc = 0;
        if (zfm == null) zfm = 0;

        //ALC: 2 -Zona Franca de Manaus ou 1 - Área de Livre Comércio
        if (alc == 1 || zfm == 1) {


            if (abe01.abe01suframa != null) {
                //ICMS como Desconto
                jsonEaa0103.put("desc_icms_z_franca", (eaa0103.eaa0103totDoc * jsonAag02Ent.getBigDecimal_Zero("txicmsaida")) / 100);

                //Obtendo CFOP
                eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6109"));

                //Obtendo CST de ICMS
                eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "041"));

                //ICMS
                jsonEaa0103.put("icms", new BigDecimal(0));
                jsonEaa0103.put("bc_icms", new BigDecimal(0));
                jsonEaa0103.put("_icms", new BigDecimal(0));

            } else {
                //Obtendo CFOP
                eaa0103.eaa0103cfop = getSession().get(Aaj15.class, Criterions.eq("aaj15codigo", "6101"));

                //Obtendo CST de ICMS
                eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "041"));
            }

            //Total Documento
            eaa0103.eaa0103totDoc = (jsonEaa0103.getBigDecimal("total_conv") +
                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                    jsonEaa0103.getBigDecimal_Zero("ipi") +
                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                    jsonEaa0103.getBigDecimal_Zero("outras_despesas") -
                    jsonEaa0103.getBigDecimal_Zero("desc_icms_z_franca")).round(4);

            //ICMS Isentas
            jsonEaa0103.put("icms_isento", eaa0103.eaa0103totDoc);
        }

    }

    private void calcularComissao(String codItem, String grupo) {
        // Base de Calculo de Comissão
        def aplicaReducao = jsonAbe01.getInteger("aplica_reducao");
        def _reducao = jsonAbe01.getBigDecimal_Zero("_reduc_bc_comissao");
        def txDescontoFinanc = jsonAbe02.getBigDecimal_Zero("tx_fixa");
        def bcComissao;

        if(grupo == "LEITE"){
            bcComissao = eaa0103.eaa0103total - ((eaa0103.eaa0103totFinanc * txDescontoFinanc) / 100);
            bcComissao = bcComissao * (_reducao / 100);
        }else{
            if (aplicaReducao == 1) {
                bcComissao = eaa0103.eaa0103total - ((eaa0103.eaa0103totFinanc * txDescontoFinanc) / 100);
                bcComissao = bcComissao * (_reducao / 100);
            } else {
                bcComissao = eaa0103.eaa0103total - ((eaa0103.eaa0103totFinanc * txDescontoFinanc) / 100);
            }
        }


        if (jsonAbe01.getBigDecimal_Zero("_reduzido_entidade") > 0) {
            def _reduzido = jsonAbe01.getBigDecimal_Zero("_reduzido_entidade");
            bcComissao = bcComissao * (jsonAbe01.getBigDecimal_Zero("_reduzido_entidade") / 100);
        }

        if (codItem == "0101001") bcComissao = new BigDecimal(0)

        jsonEaa0103.put("bc_comissao", bcComissao.round(2));
    }

    private void calcularIPI() {
        if (jsonEaa0103.getBigDecimal_Zero("_ipi") != -1 && abg01.abg01txIpi_Zero > 0) {
            //Base de Cálculo do IPI
            jsonEaa0103.put("bc_ipi", eaa0103.eaa0103total +
                    jsonEaa0103.getBigDecimal_Zero("frete_dest") +
                    jsonEaa0103.getBigDecimal_Zero("seguro") +
                    jsonEaa0103.getBigDecimal_Zero("outras_despesas"));

            //Alíquota de IPI do cadastro de NCM
            if (abg01 != null && abg01.abg01txIpi_Zero > 0) {
                jsonEaa0103.put("_ipi", abg01.abg01txIpi);
            }

            //IPI
            jsonEaa0103.put("ipi", (jsonEaa0103.getBigDecimal_Zero("bc_ipi") * jsonEaa0103.getBigDecimal_Zero("_ipi")) / 100);

        } else {
            jsonEaa0103.put("bc_ipi", 0.000000);
            jsonEaa0103.put("_ipi", 0.0000000);
            jsonEaa0103.put("ipi", 0.0000000);
        }

        if (jsonEaa0103.getBigDecimal_Zero("ipi") == 0) jsonEaa0103.put("ipi_isento", eaa0103.eaa0103totDoc);

    }

    private void calcularPIS() {
        if (jsonAbm0101.getBigDecimal_Zero("_pis") > 0) {
            if (jsonEaa0103.getBigDecimal_Zero("pis") == 0) {
                jsonEaa0103.put("bc_pis", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("ipi") - jsonEaa0103.getBigDecimal_Zero("icms_st") - jsonEaa0103.getBigDecimal_Zero("icms"));
                if (jsonEaa0103.getBigDecimal_Zero("_pis") != -1) {
                    jsonEaa0103.put("_pis", jsonAbm0101.getBigDecimal_Zero("_pis"));
                    jsonEaa0103.put("pis", (jsonEaa0103.getBigDecimal_Zero("bc_pis") * jsonEaa0103.getBigDecimal_Zero("_pis")) / 100);
                    jsonEaa0103.put("pis", jsonEaa0103.getBigDecimal_Zero("pis").round(2));
                    //Obtendo CST de Pis e Cofins
                    eaa0103.eaa0103cstPis = getSession().get(Aaj12.class, Criterions.eq("aaj12codigo", "01"));
                    eaa0103.eaa0103cstCofins = getSession().get(Aaj13.class, Criterions.eq("aaj13codigo", "01"));
                } else {
                    jsonEaa0103.put("_pis", new BigDecimal(0));
                    jsonEaa0103.put("bc_pis", new BigDecimal(0));
                }
            }
        }
    }

    private void calcularCOFINS() {
        if (jsonAbm0101.getBigDecimal_Zero("_cofins") > 0) {
            if (jsonEaa0103.getBigDecimal_Zero("cofins") == 0) {
                jsonEaa0103.put("bc_cofins", eaa0103.eaa0103totDoc - jsonEaa0103.getBigDecimal_Zero("cofins") - jsonEaa0103.getBigDecimal_Zero("icms_st") - jsonEaa0103.getBigDecimal_Zero("icms"));
                if (jsonEaa0103.getBigDecimal_Zero("_cofins") > -1) {
                    jsonEaa0103.put("_cofins", jsonAbm0101.getBigDecimal_Zero("_cofins"));
                    jsonEaa0103.put("cofins", (jsonEaa0103.getBigDecimal_Zero("bc_cofins") * jsonEaa0103.getBigDecimal_Zero("_cofins")) / 100);
                    jsonEaa0103.put("cofins", jsonEaa0103.getBigDecimal_Zero("cofins").round(2));
                } else {
                    jsonEaa0103.put("cofins", 0.000000);
                    jsonEaa0103.put("bc_cofins", 0.000000);
                }
            }

        }
    }

    private void calcularDiferimento() {
        if (jsonAbm1001_UF_Item.getBigDecimal_Zero("diferimento") > 0) {
            jsonEaa0103.put("_diferimento", jsonAbm1001_UF_Item.getBigDecimal_Zero("diferimento"));
            // Troca ST para 051 para itens com diferimento
            if (jsonEaa0103.getBigDecimal_Zero("_diferimento") > 0) eaa0103.eaa0103cstIcms = getSession().get(Aaj10.class, Criterions.eq("aaj10codigo", "051"));
        } else {
            jsonEaa0103.put("_diferimento", 0.000000);
        }
    }

    private void calcularCargaTributaria() {
        // Busca campo customizado no cadastro de NCM para encontrar aliquota da carga tributária
        def sql = "select abg01camposcustom from abg01 " +
                "inner join abm0101 on abm0101ncm = abg01id " +
                "inner join abm01 on abm01id = abm0101item " +
                "where abm01id = :abm01id " +
                "AND abm01tipo = :eaa0103tipo " +
                "AND abm0101empresa = :aac10id";

        def aliqCargaTrib;

        if (abm0101.abm0101ncm == null) throw new ValidacaoException("Não foi informado NCM no cadastro do item " + abm01.abm01codigo + " para calculo da carga tributária.  ")

        TableMap abg01camposCustom = getAcessoAoBanco().buscarUnicoTableMap(sql, Parametro.criar("abm01id", abm01.abm01id), Parametro.criar("eaa0103tipo", eaa0103.eaa0103tipo), Parametro.criar("aac10id", aac10.aac10id)).getTableMap("abg01camposcustom");

        if (abg01camposCustom == null) throw new ValidacaoException("Valor da aliquota dos tributos no cadastro do NCM " + abm0101.abm0101ncm.abg01codigo + " não é válido para cálculo da carga tributária.")


        //Alíquota Carga Tributária
        aliqCargaTrib = abg01camposCustom.getBigDecimal_Zero("_carga_trib");
        jsonEaa0103.put("carga_trib_", aliqCargaTrib);

        //BC Carga Tributaria
        jsonEaa0103.put("bc_carga_trib", eaa0103.eaa0103total);

        // Carga Tributaria
        if (jsonEaa0103.getBigDecimal_Zero("carga_trib_") > 0) {
            jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("bc_carga_trib") * jsonEaa0103.getBigDecimal_Zero("carga_trib_") / 100);
            jsonEaa0103.put("VlrCargaTrib", jsonEaa0103.getBigDecimal_Zero("VlrCargaTrib").round(2));
        }
    }

    private void preencherSPEDS() {

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


    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==