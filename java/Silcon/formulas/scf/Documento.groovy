package Silcon.formulas.scf;

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
public class Documento extends FormulaBase{

	private Daa01 daa01;
	
	
	@Override 
	public void executar() {
		daa01 = get("daa01");
		TableMap mapJson = daa01.daa01json == null ? new TableMap() : daa01.daa01json;
		
		DayOfWeek diaDaSemanaDoVcto = daa01.daa01dtVctoN.getDayOfWeek();
//		if(diaDaSemanaDoVcto.equals(DayOfWeek.SATURDAY) ) {
//			daa01.daa01dtVctoN = daa01.daa01dtVctoN.plusDays(2);
//			daa01.daa01dtVctoR = daa01.daa01dtVctoR.plusDays(2);
//		} else if (diaDaSemanaDoVcto.equals(DayOfWeek.SUNDAY)) {
//			daa01.daa01dtVctoN = daa01.daa01dtVctoN.plusDays(1);
//			daa01.daa01dtVctoR = daa01.daa01dtVctoR.plusDays(2);
//		}

		mapJson.put("juros", (daa01.daa01valor * 0.17 / 100).round(2));
	}
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCF_DOCUMENTOS; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDAifQ==