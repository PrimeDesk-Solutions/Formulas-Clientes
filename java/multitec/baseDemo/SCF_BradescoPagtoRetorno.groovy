package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase
import sam.server.scf.service.SCFService

class SCF_BradescoPagtoRetorno extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_RETORNO_DE_PAGAMENTO;
	}

	@Override
	public void executar() {
		SCFService scfService = instanciarService(SCFService.class);
		
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDUifQ==