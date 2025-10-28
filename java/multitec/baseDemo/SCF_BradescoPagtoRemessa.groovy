package multitec.baseDemo

import br.com.multitec.utils.TextFile
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

class SCF_BradescoPagtoRemessa extends FormulaBase{

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_REMESSA_DE_PAGAMENTO;
	}

	@Override
	public void executar() {
		TextFile txt = new TextFile();

		txt.print("PAGAMENTO");
		txt.print("REMESSA");
		txt.newLine();
		
		put("txt", txt);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDQifQ==