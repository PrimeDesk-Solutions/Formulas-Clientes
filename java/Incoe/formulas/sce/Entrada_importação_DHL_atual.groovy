package Inova.formulas.sce

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.server.samdev.utils.SCEUtils

public class Entrada_importação_DHL_atual extends FormulaBase {
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

        bcc01.bcc01custo

        // Campos Livres Lançamento
        jsonBcc01 = bcc01.bcc01json != null ? bcc01.bcc01json : new TableMap();
        jsonAbm0101 = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

        // Total Item
        jsonBcc01.put("total_item", bcc01.bcc01qt * jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        jsonBcc01.put("total_item", jsonBcc01.getBigDecimal_Zero("total_item").round(2));

        // Custo Total
        bcc01.bcc01custo =  jsonBcc01.getBigDecimal_Zero("total_item") +
                            jsonBcc01.getBigDecimal_Zero("outras_despesas") +
                            jsonBcc01.getBigDecimal_Zero("insumo_faturas") -
                            jsonBcc01.getBigDecimal_Zero("icms");

        bcc01.bcc01custo = bcc01.bcc01custo.round(2);

        // Custo Unitário
        //Custo unitário
        if(bcc01.bcc01qt > 0){
            jsonBcc01.put("custo_unitario",  bcc01.bcc01custo / bcc01.bcc01qt);
            jsonBcc01.put("custo_unitario",  jsonBcc01.getBigDecimal_Zero("custo_unitario").round(2));
        }else{
            jsonBcc01.put("custo_unitario", bcc01.bcc01custo);
            jsonBcc01.put("custo_unitario", jsonBcc01.getBigDecimal_Zero("custo_unitario").round(2));
        }

        //Preço médio
        def saldoatual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id)
        if((saldoatual + bcc01.bcc01qt) > 0){
            bcc01.bcc01pmu = ((jsonAbm0101.getBigDecimal_Zero("preco_medio_atual") * saldoatual) + bcc01.bcc01custo) / (saldoatual + bcc01.bcc01qt)
        }else{
            bcc01.bcc01pmu = jsonAbm0101.getBigDecimal_Zero("preco_medio_atual")
            bcc01.bcc01pmu = bcc01.bcc01pmu.round(2)
        }

        //Maior preço
        if(jsonBcc01.getBigDecimal_Zero("unitario_estoque") > jsonAbm0101.getBigDecimal_Zero("preco_diverso_maior")) {
            jsonBcc01.put("preco_diverso_maior", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        }else{
            jsonBcc01.put("preco_diverso_maior", jsonAbm0101.getBigDecimal_Zero("preco_diverso_maior"));
            jsonBcc01.put("preco_diverso_maior", jsonBcc01.getBigDecimal_Zero("preco_diverso_maior").round(2));
        }

        //Menor preço
        if(jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor") == 0 || jsonBcc01.getBigDecimal_Zero("unitario_estoque") < jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor")) {
            jsonBcc01.put("preco_diverso_menor", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        }else{
            jsonBcc01.put("preco_diverso_menor", jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor"));
            jsonBcc01.put("preco_diverso_menor", jsonBcc01.getBigDecimal_Zero("preco_diverso_menor").round(2));
        }

        //Último preço
        jsonBcc01.put("preco_diverso_ultimo", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        jsonBcc01.put("preco_diverso_ultimo", jsonBcc01.getBigDecimal_Zero("preco_diverso_ultimo").round(2));
    }
}
