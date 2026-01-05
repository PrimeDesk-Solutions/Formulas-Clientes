/*
    Desenvolvido por: Leonardo
 */

package Profer.formulas.sce

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abm0101
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.server.samdev.utils.SCEUtils

public class SceMovimentacaoGeral extends FormulaBase{
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

        bcc01 = (Bcc01)get("bcc01");
        abm0101 = (Abm0101)get("abm0101");

        // Campos Livres Lançamento
        jsonBcc01 = bcc01.bcc01json;
        jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();

        // Custo Unitario
        jsonBcc01.put("custo_unitario", jsonAbm0101.getBigDecimal_Zero("preco_diverso_atual"));

        // Custo Total
        if(bcc01.bcc01qt > 0){
            bcc01.bcc01custo = (bcc01.bcc01qt * jsonBcc01.getBigDecimal_Zero("unitario_estoque")).round(2);
        }else{
            bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("unitario_estoque").round(2)
        }

        // Preco Médio Unitário
        bcc01.bcc01pmu = jsonAbm0101.getBigDecimal_Zero("preco_medio_atual")

        // Maior preço
        if(jsonBcc01.getBigDecimal_Zero("unitario_estoque") > jsonAbm0101.getBigDecimal_Zero("preco_diverso_maior")){
            jsonBcc01.put("preco_diverso_maior", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        }else{
            jsonBcc01.put("preco_diverso_maior", jsonAbm0101.getBigDecimal_Zero("preco_diverso_maior"));
        }


        //Menor Preço
        if(jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor") == 0 || jsonBcc01.getBigDecimal_Zero("unitario_estoque") < jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor") ){
            jsonBcc01.put("preco_diverso_menor", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
        }else{
            jsonBcc01.put("preco_diverso_menor", jsonAbm0101.getBigDecimal_Zero("preco_diverso_menor"));
        }

        // Ultimo Preço
        jsonAbm0101.put("preco_diverso_ultimo", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));

        // Preço Livre
        jsonBcc01.put("preco_livre_item", new BigDecimal(0));

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==