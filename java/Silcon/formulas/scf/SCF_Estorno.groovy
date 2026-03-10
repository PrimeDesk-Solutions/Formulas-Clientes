package multitec.formulas.scf;

import java.time.DayOfWeek;

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.da.Daa01;
import sam.server.samdev.formula.FormulaBase;

/**
 *
 * Fórmula para manipular um Documento Financeiro
 *
 */
public class SCF_Estorno extends FormulaBase{

    private Daa01 daa01;
    @Override
    public void executar() {
        daa01 = get("daa01");
        TableMap mapJson = daa01.daa01json == null ? new TableMap() : daa01.daa01json;

        mapJson.put("jurosq", null);
        mapJson.put("multaq", null);
        mapJson.put("encargosq", null);
        mapJson.put("descontoq", null);
    }
    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCF_DOCUMENTOS;
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDAifQ==