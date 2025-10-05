package Atilatte.formulas.scq

import br.com.multiorm.criteria.criterion.Criterions
import com.lowagie.text.Table
import sam.model.entities.ab.Abm01
import sam.model.entities.ab.Abm0101
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

import br.com.multitec.utils.collections.TableMap
import sam.model.entities.bb.Bbb01

public class SCQ_FichaInspecao extends FormulaBase {

    private Bbb01 bbb01;
	private String procInvoc;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCQ_FI;
    }

    @Override
    public void executar() {
        bbb01 = get("bbb01");
		procInvoc = get("procInvoc");

        // Item da ficha
        Abm01 abm01 = getSession().get(Abm01.class, bbb01.bbb01item.abm01id);

        // Itens Valores
        Abm0101 abm0101 = getSession().get(Abm0101.class, Criterions.where("abm0101item = " + abm01.abm01id + " and abm0101empresa = " + obterEmpresaAtiva().getAac10id()))

        // Campos Livres
        TableMap jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
        TableMap jsonBbb01 = bbb01.bbb01json != null ? bbb01.bbb01json : new TableMap();

        // Quantidade amostra do item
        def qtAmostra = jsonAbm0101.getBigDecimal_Zero("qt_amostra");

        jsonBbb01.put("qtde_amostra", qtAmostra);

        // Preenche a quantidade de amostra da ficha de acordo com o cadastro do item
        bbb01.bbb01json = jsonBbb01;

        //Calculando quantidade de amostra limitando a 10%
        bbb01.bbb01qtAmostra = round(qtAmostra, 2);

    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzgifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzgifQ==