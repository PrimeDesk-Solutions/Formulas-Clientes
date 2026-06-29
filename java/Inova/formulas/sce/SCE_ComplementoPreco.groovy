/*
    Desenvolvido por: Leonardo Ledo
 */

package Inova.formulas.sce

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.server.samdev.utils.SCEUtils

public class SCE_CompraInsumo extends FormulaBase {
    SCEUtils sceUtils;
    private Bcc01 bcc01;
    private Abm0101 abm0101;

    // Campos Livres
    TableMap jsonBcc01 = new TableMap();
    TableMap jsonAbm0101 = new TableMap();

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.LCTO_SCE;
    }

    @Override
    public void executar() {
        sceUtils = (SCEUtils) get("sceUtils");

        bcc01 = (Bcc01) get("bcc01");
        abm0101 = (Abm0101) get("abm0101");

        // Campos Livres Lançamento
        jsonBcc01 = bcc01.bcc01json != null ? bcc01.bcc01json : new TableMap();
        jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

        bcc01.bcc01qt = new BigDecimal(0);

        // Custo Total
        bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("total_item_estoque") +
                jsonBcc01.getBigDecimal_Zero("frete_transp") +
                jsonBcc01.getBigDecimal_Zero("frete_inova");

        bcc01.bcc01custo = bcc01.bcc01custo.round(2);

        // Custo Unitario
        if (bcc01.bcc01qt > 0) {
            jsonBcc01.put("custo_unitario", bcc01.bcc01custo / bcc01.bcc01qt);
        } else {
            jsonBcc01.put("custo_unitario", bcc01.bcc01custo);
        }

        // Preco Médio Unitário
        def saldoAtual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id);

        if (saldoAtual > 0) {
            bcc01.bcc01pmu = ((jsonAbm0101.getBigDecimal_Zero("preco_medio_atual") * saldoAtual) + bcc01.bcc01custo) / saldoAtual;
        } else {
            bcc01.bcc01pmu = jsonAbm0101.getBigDecimal_Zero("preco_medio_atual")
        }

        // Maior preço
        jsonBcc01.put("preco_diverso_maior", jsonAbm0101.getBigDecimal_Zero("preco_diverso_maior"));

        //Menor Preço
        jsonBcc01.put("preco_diverso_menor", jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor"));

        // Ultimo Preço
        jsonAbm0101.put("preco_diverso_ultimo", jsonAbm0101.getBigDecimal_Zero("preco_diverso_ultimo"));


        // Custo Simples
        if (jsonBcc01.getBigDecimal_Zero("custo_unitario") > 0) {
            jsonBcc01.put("preco_livre", jsonBcc01.getBigDecimal_Zero("custo_unitario") + (jsonBcc01.getBigDecimal_Zero("ipi") / bcc01.bcc01qt))
        } else {
            jsonBcc01.put("preco_livre", jsonAbm0101.getBigDecimal_Zero("preco_livre"));
        }

        if (bcc01.bcc01qt > 0) {
            jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("preco_livre") - jsonBcc01.getBigDecimal_Zero("ipi") / bcc01.bcc01qt - jsonBcc01.getBigDecimal_Zero("icms") / bcc01.bcc01qt);
            jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("custo_presumido").round(2));
        } else {
            jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("preco_livre") - (jsonBcc01.getBigDecimal_Zero("ipi") / bcc01.bcc01qt) - (jsonBcc01.getBigDecimal_Zero("icms") / bcc01.bcc01qt));
            jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("custo_presumido").round(2));
        }

        /*
            VERIFICAR COM WILSON
            //Custo Simples
            //ListResultSet Acha nº de nota fiscal de compra
            ListResultSet dados = executarConsulta("select ea01.ea01num as docnum,ea011.ea011item as docitem,ea01.ea01vlr6 as docunit, "+
                                                           "docvenda.ea01num as docvendanum,itemdocvenda.ea011item as docvendaitem, "+
                                                           "itemdocvenda.ea011vlr0 as itemdocvendaqtd,itemdocvenda.ea011vlr6 as itemdocvendaunit,itemdocvenda.ea011vlr10 / itemdocvenda.ea011vlr2 as custounit  "+
                                                    "from ea01 "+
                                                    "inner join ea011 on ea011doc = ea01id "+
                                                    "left join ea01 as docvenda on ea011num = docvenda.ea01num "+
                                                    "left join ea011 as itemdocvenda on itemdocvenda.ea011doc = docvenda.ea01id  "+
                                                    "where ea01.ea01num = " + bc02.bc02num + " and  itemdocvenda.ea011item = " + bc02.ab50.ab50id);

            if(dados != null && dados.size() > 0){
               def qtd = dados.getDecimal(0,"itemdocvendaqtd")
               def unit = dados.getDecimal(0, "itemdocvendaunit");
               def custounit = dados.getDecimal(0, "custounit");
               bc02.bc02precolivre = bc02.bc02custounit + custounit;
            }
            if(bc02.bc02vlr2 > 0){
            //Custo Presumido
            bc02.bc02precolivre1 = bc02.bc02precolivre - (bc02.bc02vlr20 / bc02.bc02vlr2) - (bc02.bc02vlr27 / bc02.bc02vlr2)
            }
         */
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==