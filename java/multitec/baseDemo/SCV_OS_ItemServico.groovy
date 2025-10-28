package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.cb.Cbd01011
import sam.server.samdev.formula.FormulaBase

class SCV_OS_ItemServico extends FormulaBase {
	
	private Cbd01011 cbd01011;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_OS_ITENS;
	}
	
	@Override
	public void executar() {
		cbd01011 = (Cbd01011)get("cbd01011");
		
		cbd01011.cbd01011total = cbd01011.cbd01011unit_Zero * cbd01011.cbd01011qtPrev_Zero;
		cbd01011.cbd01011total = round(cbd01011.cbd01011total, 2);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTUifQ==