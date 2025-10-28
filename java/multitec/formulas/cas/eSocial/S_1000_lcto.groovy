package multitec.formulas.cas.eSocial;

import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;

public class S_1000_lcto extends FormulaBase{

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}
	
	@Override
	public void executar() {
		put("aaa15Autorizado",  true);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==