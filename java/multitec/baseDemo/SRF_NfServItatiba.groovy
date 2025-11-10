package multitec.baseDemo;

import sam.dicdados.FormulaTipo;
import sam.model.entities.ea.Eaa01;
import sam.server.samdev.formula.FormulaBase;

public class SRF_NfServItatiba extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.NFSE;
	}
	
	@Override
	public void executar() {
		List<Eaa01> eaa01s = get("eaa01s");
		
		put("dados", null);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzIifQ==