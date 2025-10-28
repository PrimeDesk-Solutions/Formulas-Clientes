package multitec.baseDemo

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap
import sam.model.entities.ab.Abb01041

public class CGS_AtividadeWorkflow extends FormulaBase {
	
	private Abb01041 abb01041;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CGS_WORKFLOW;
	}
	
	@Override
	public void executar() {
		abb01041 = (Abb01041)get("abb01041");
		
		TableMap json = abb01041.abb01041json;
		
		if(json == null) json = new TableMap();
		
		json.put("texto", "Atividade");
		
		abb01041.abb01041json = json;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjEifQ==