package multitec.baseDemo

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
		
        TableMap tmJsonBbb01 = bbb01.bbb01json == null ? new TableMap() : bbb01.bbb01json;

        //Calculando quantidade de amostra limitando a 10%
        bbb01.bbb01qtAmostra = (bbb01.bbb01qtTotal * 10) / 100;
        bbb01.bbb01qtAmostra = round(bbb01.bbb01qtAmostra, 2);

        //Iniciar a FI com a quantidade Sem Restrição a 100% da Quantidade Total
        def qtde = tmJsonBbb01.get("scq_fi_qt_sem_restr");
        if(qtde == 0){
            qtde = bbb01.bbb01qtTotal;
        }
        tmJsonBbb01.put("scq_fi_qt_sem_restr", qtde);
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzgifQ==