package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.db.Dbb1001
import sam.server.samdev.formula.FormulaBase

class SCN_ItemDaReserva extends FormulaBase {
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCN_RESERVAS;
	}

	@Override
	public void executar() {
		Dbb1001 dbb1001 = get('dbb1001')
		dbb1001.dbb1001total = dbb1001.dbb1001unit * dbb1001.dbb1001qt
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjMifQ==