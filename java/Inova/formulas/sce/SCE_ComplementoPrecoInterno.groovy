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

public class SCE_ComplementoPrecoInterno extends FormulaBase {
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
        bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("unitario_estoque") +
                jsonBcc01.getBigDecimal_Zero("frete_transp") +
                jsonBcc01.getBigDecimal_Zero("frete_inova") +
                jsonBcc01.getBigDecimal_Zero("ipi");

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
        jsonBcc01.put("preco_livre", jsonAbm0101.getBigDecimal_Zero("preco_livre"))

        jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("preco_livre") - (jsonBcc01.getBigDecimal_Zero("ipi") / bcc01.bcc01qt) - (jsonBcc01.getBigDecimal_Zero("icms") / bcc01.bcc01qt));
        jsonBcc01.put("custo_presumido", jsonBcc01.getBigDecimal_Zero("custo_presumido").round(2));
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==