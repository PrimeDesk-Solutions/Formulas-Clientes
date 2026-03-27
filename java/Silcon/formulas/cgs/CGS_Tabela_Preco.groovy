package Silcon.formulas.cgs;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abe4001;
import sam.server.samdev.formula.FormulaBase;

public class CGS_Tabela_Preco extends FormulaBase{

    private Abe4001 abe4001;
    private TableMap mapJsonFormula;

    @Override
    public void executar() {
        abe4001 = (Abe4001)get("abe4001");
        mapJsonFormula = (TableMap)get("mapJsonFormula");

        if(abe4001 == null) throw new ValidacaoException("Necessário informar o item da tabela de preço.");

        abe4001.abe4001preco = round(abe4001.abe4001preco,2);
    }

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.TABELA_PRECO;
    }

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzAifQ==