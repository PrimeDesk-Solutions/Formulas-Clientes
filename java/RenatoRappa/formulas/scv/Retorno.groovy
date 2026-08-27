package RenatoRappa.formulas.scv;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo

public class Retorno extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCV_SRF_ITEM_DO_DOCUMENTO; 
	}

	@Override 
	public void executar() {
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjIifQ==