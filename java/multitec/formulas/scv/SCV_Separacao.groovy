package multitec.formulas.scv

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import sam.model.entities.ea.Eaa0103
import sam.model.entities.ea.Eaa0106;
import sam.model.entities.ea.Eaa01061;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class SCV_Separacao extends FormulaBase{

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCV_SEPARACAO_CONFERENCIA_DE_ITENS;
    }

    @Override
    public void executar() {
        Eaa0106 eaa0106 = get("eaa0106");

        List<Eaa01061> eaa01061s = eaa0106.getEaa01061s();

        for(Eaa01061 eaa01061 : eaa01061s){
            Eaa0103 eaa0103 = getSession().createCriteria(Eaa0103.class)
                .addFields("eaa0103qtUso, eaa0103codigo, eaa0103descr")
                .addWhere(Criterions.eq("eaa0103id", eaa01061.getEaa01061item().getEaa0103id()))
                .get(ColumnType.ENTITY);

            if(eaa01061.getEaa01061qtde().compareTo(eaa0103.getEaa0103qtUso_Zero()) > 0){
                interromper("O item " + eaa0103.getEaa0103codigo() + " - " + eaa0103.getEaa0103descr() + " foi separado uma quantidade maior." )
            }
        }
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTE3In0=