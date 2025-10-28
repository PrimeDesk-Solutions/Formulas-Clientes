package multitec.baseDemo;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import sam.model.entities.ed.Edd40

public class SGT_LctoIR extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SGT_LCTO_IR; 
	}

	@Override 
	public void executar() {
		Edd40 edd40 = get('edd40')
		def edd40obs = edd40.edd40obs

		edd40obs = edd40obs != null ? edd40obs + '\nFórmula executada!' : 'Fórmula executada!'
		edd40.edd40obs = edd40obs
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTIwIn0=