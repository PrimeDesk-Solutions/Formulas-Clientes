package multitec.baseDemo;

import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.formula.FormulaBase;

class SCF_BradescoCobrancaRetorno extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCF_RETORNO_DE_COBRANCA;
	}
	
	@Override
	public void executar() {
		List<TableMap> tmList = new ArrayList();
		put("tmList", tmList);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNDMifQ==