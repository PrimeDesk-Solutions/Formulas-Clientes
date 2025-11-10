package multitec.formulas.cas.eSocial;

import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;

public class S_1005_lcto extends FormulaBase {

	@Override
	public void executar() {
		put("aaa15Autorizado", false);
	}

	@Override
	public FormulaTipo obterTipoFormula() {

		return FormulaTipo.ESOCIAL;
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==