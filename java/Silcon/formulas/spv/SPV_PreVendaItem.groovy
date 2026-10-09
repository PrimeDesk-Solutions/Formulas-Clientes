package Silcon.formulas.spv

import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap
import sam.core.variaveis.MDate
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aab10
import sam.model.entities.aa.Aac10
import sam.model.entities.aa.Aag02
import sam.model.entities.aa.Aag0201
import sam.model.entities.ab.Abe01
import sam.model.entities.ab.Abe0101
import sam.model.entities.ab.Abe30
import sam.model.entities.ab.Abe40
import sam.model.entities.ab.Abe4001
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.model.entities.ab.Abm10
import sam.model.entities.ab.Abm1001
import sam.model.entities.cc.Ccb01
import sam.model.entities.cc.Ccb0101
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SPV_PreVendaItem extends FormulaBase {

    private Ccb0101 ccb0101;
    private String procInvoc;
    private Integer campoDigitado; // 0: Nenhum, 1: Quantidade, 2: Unitário, 3: % Desconto, 4: Valor Desconto


    private Aac10 aac10;
    private Ccb01 ccb01;
    private Aag02 ufEnt;
    private Aag0201 municipioEnt;
    private Abe01 abe01;
    private Abe0101 abe0101Principal;
    private Abe40 abe40;
    private Abe30 abe30;
    private Abm01 abm01;
    private Abm0101 abm0101;
    private Abm10 abm10;
    private Abm1001 abm1001;


    private TableMap jsonAbm1001_UF_Item;
    private TableMap jsonCcb0101;

    @Override
    public void executar() {

        ccb0101 = get("ccb0101");
        procInvoc = get("procInvoc");
        campoDigitado = get("campoDigitado");

        ccb01 = ccb0101.ccb0101pv;
        abe01 = ccb01.ccb01ent;
        abe40 = ccb01.ccb01tp;
        abe30 = ccb01.ccb01cp;
        abm01 = ccb0101.ccb0101item;

        // Endereço Entidade (Principal)
        abe0101Principal = abe01 != null ? getSession().get(Abe0101.class, Criterions.where("abe0101ent = " + abe01.abe01id + " AND abe0101principal = 1")) : null;

        // Municipio Entidade
        municipioEnt = abe0101Principal != null && abe0101Principal.abe0101municipio != null ? getSession().get(Aag0201.class, Criterions.eq("aag0201id", abe0101Principal.abe0101municipio.aag0201id)) : null;

        // UF Entidade
        ufEnt = municipioEnt != null ? getSession().get(Aag02.class, municipioEnt.aag0201uf.aag02id) : null;

        // Empresa
        aac10 = getSession().get(Aac10.class, obterEmpresaAtiva().aac10id);

        // Itens Configurações
        abm0101 = abm01 != null ? getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " AND abm0101empresa = " + aac10.aac10id)) : null;

        //Valores do Item
        abm10 = abm0101 != null && abm0101.abm0101valores != null ? getSession().get(Abm10.class, abm0101.abm0101valores.abm10id) : null;

        //Valores do Item - Estados
        abm1001 = ufEnt != null && ufEnt.aag02id != null && abm10 != null && abm10.abm10id != null ? getSession().get(Abm1001.class, Criterions.where("abm1001uf = " + ufEnt.aag02id + " AND abm1001cv = " + abm10.abm10id)) : null;

        // Campos Livres
        jsonAbm1001_UF_Item = abm1001 != null && abm1001.abm1001json != null ? abm1001.abm1001json : new TableMap();
        jsonCcb0101 = ccb0101.ccb0101json != null ? ccb0101.ccb0101json : new TableMap();

        preencherCamposLivres();

        //Calcula somente o % Desconto e TotalDoc caso tenha sido digitado o Valor Desconto
        if(campoDigitado == 4) {
            def percDesc = 0;
            if(ccb0101.ccb0101total_Zero > 0 && ccb0101.ccb0101desc_Zero > 0) {
                if(ccb0101.ccb0101desc_Zero > ccb0101.ccb0101total_Zero) {
                    throw new ValidacaoException("Valor do desconto não pode ser maior que o total do item.");
                }
            }

            if(ccb0101.ccb0101total_Zero == 0) throw new ValidacaoException("O total do item " + ccb0101.ccb0101seq + " não pode ser zero.");

            percDesc = (ccb0101.ccb0101desc_Zero * 100) / ccb0101.ccb0101total_Zero;

            ccb0101.ccb0101totDoc = ccb0101.ccb0101total_Zero - ccb0101.ccb0101desc_Zero;

            ccb0101.ccb0101percDesc = percDesc;

            ccb0101.ccb0101percDesc = round(ccb0101.ccb0101percDesc_Zero, 2);

            return;
        }else if(campoDigitado == 3){
            //Calculando o valor do desconto com base no % Desconto
            def valorDesc = 0;
            if(ccb0101.ccb0101total_Zero > 0 && ccb0101.ccb0101percDesc_Zero > 0) {
                if(ccb0101.ccb0101percDesc_Zero > 100) {
                    throw new ValidacaoException("% do desconto não pode ser maior que 100,00.");
                }

                valorDesc = (ccb0101.ccb0101total_Zero * ccb0101.ccb0101percDesc_Zero) / 100;
            }
            ccb0101.ccb0101desc = valorDesc;
            ccb0101.ccb0101desc = round(ccb0101.ccb0101desc_Zero, 2);
        }

        if(campoDigitado == 0){ // Inserindo item pela primeira vez ou alterando tabela
            setarObterPrecoUnitario();

            if(procInvoc == "CAS0240") return;

            ccb0101.ccb0101percDesc = new BigDecimal(0) // Zera percentual de desconto

            //Calculando total do item
            ccb0101.ccb0101total = ccb0101.ccb0101qtComl_Zero * ccb0101.ccb0101unit_Zero;
            ccb0101.ccb0101total = round(ccb0101.ccb0101total_Zero, 2);

            //Calculando o TotalDoc
            ccb0101.ccb0101totDoc = ccb0101.ccb0101total_Zero - ccb0101.ccb0101desc_Zero;
        }else{
            //Calculando total do item
            ccb0101.ccb0101total = ccb0101.ccb0101qtComl_Zero * ccb0101.ccb0101unit_Zero;
            ccb0101.ccb0101total = round(ccb0101.ccb0101total_Zero, 2);

            //Calculando o TotalDoc
            ccb0101.ccb0101totDoc = ccb0101.ccb0101total_Zero - ccb0101.ccb0101desc_Zero;
        }

    }
    private void verificarLimMaxDesconto(){
        if(abe40 == null) return;

        // Usuário Ativo
        Aab10 aab10 = obterUsuarioLogado();

        // Nome Usuário Ativo
        String nomeUser = aab10.aab10user;

        // Taxa Maxima Desconto Venda
        BigDecimal txMaxDescVenda = getAcessoAoBanco().obterBigDecimal("SELECT aab1001conteudo FROM aab1001 WHERE aab1001user = :idUser AND aab1001param = 'TXMAXDESCVDA'", Parametro.criar("idUser", aab10.aab10id));

        // Taxa Máxima Desconto Item
        BigDecimal txMaxDescItem = getAcessoAoBanco().obterBigDecimal("SELECT aab1001conteudo FROM aab1001 WHERE aab1001user = :idUser AND aab1001param = 'TXMAXDESCITEM'", Parametro.criar("idUser", aab10.aab10id));

        // Usuários Permitidos
        String usuarios = "NATALIA;DARCI;MARCELO;RICARDO;SILVANA;THAIS;FRANCICO;RONY;FERNANDO;FAISSAL";

        if((ccb0101.ccb0101percDesc > txMaxDescVenda || ccb0101.ccb0101percDesc > txMaxDescItem) && !usuarios.contains(nomeUser)) interromper("O % de desconto aplicado para o item" + ccb0101.ccb0101item.abm01codigo + " - " + ccb0101.ccb0101item.abm01na + " é maior que o permitido para o usuário.")
    }

    private void setarObterPrecoUnitario() {
        if(abe40 == null) return;

        //if(ccb0101.ccb0101unit != 0) return;

        //Verificando se a tabela de preço está vencida
        def sql = " SELECT abe40dtVcto" +
                " FROM Abe40" +
                " WHERE abe40id = :abe40id";

        def abe40dtVcto = getAcessoAoBanco().obterDate(sql, Parametro.criar("abe40id", abe40.abe40id));

        if(abe40dtVcto != null) {
            if(MDate.date() > abe40dtVcto) {
                throw new ValidacaoException("Tabela de preços vencida.");
            }
        }

        //Buscando preço na tabela de preço por item, tabela, condição de pagamento, qtde comercial, taxa de desconto
        sql = " SELECT abe4001preco" +
                " FROM Abe4001 " +
                " INNER JOIN Abe40 ON abe4001tab = abe40id" +
                " WHERE abe4001item = :abm01id" +
                " AND abe4001tab = :abe40id"

        List<Parametro> parametros = new ArrayList<>();
        parametros.add(Parametro.criar("abm01id", abm01.abm01id));
        parametros.add(Parametro.criar("abe40id", abe40.abe40id));

        TableMap tm = getAcessoAoBanco().buscarUnicoTableMap(sql, parametros.toArray(new Parametro[parametros.size()]));

        def unit = 0;
        if(tm != null) {
            unit = tm.getBigDecimal("abe4001preco");
        }
        if(unit == null) unit = 0;
        ccb0101.ccb0101unit = unit;
    }

    private void preencherCamposLivres(){
        jsonCcb0101.put("aliq_icms", jsonAbm1001_UF_Item.getBigDecimal_Zero("aliq_icms"));

        ccb0101.ccb0101json = jsonCcb0101;
    }

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SPV_PREVENDA;
    }

}