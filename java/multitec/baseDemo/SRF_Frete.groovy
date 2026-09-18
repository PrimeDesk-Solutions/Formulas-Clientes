package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase

class SRF_Frete extends FormulaBase {
	
	private Eaa01 eaa01;
	private String procInvoc;
			
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		procInvoc = get("procInvoc");
		
		def frete_dest = eaa01.eaa01totDoc * 0.1;
		frete_dest = round(frete_dest, 2);
		
		TableMap mapJson = eaa01.eaa01json == null ? new TableMap() : eaa01.eaa01json;
		mapJson.put("frete_dest", frete_dest);
		eaa01.eaa01json = mapJson;
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_FRETE;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjEifQ==