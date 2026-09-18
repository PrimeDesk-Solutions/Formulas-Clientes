package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.ba.Bab01
import sam.server.samdev.formula.FormulaBase

class SPP_OP extends FormulaBase {
	
	private Bab01 bab01;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPP_ORDEM_DE_PRODUCAO;
	}
	
	@Override
	public void executar() {
		bab01 = get("bab01");
		
		
		
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODMifQ==