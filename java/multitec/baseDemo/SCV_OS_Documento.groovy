package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.cb.Cbd01
import sam.model.entities.cb.Cbd0101
import sam.server.samdev.formula.FormulaBase

class SCV_OS_Documento extends FormulaBase {
	
	private Cbd01 cbd01;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_OS_DOC;
	}
	
	@Override
	public void executar() {
		cbd01 = (Cbd01)get("cbd01");
		
		TableMap mapJson = cbd01.cbd01json == null ? new TableMap() : cbd01.cbd01json;
		def custo = mapJson.getBigDecimal_Zero("valoradm") + mapJson.getBigDecimal_Zero("valor1") + mapJson.getBigDecimal_Zero("valor2") + mapJson.getBigDecimal_Zero("valor3");
		
		mapJson.put("custo", custo);
		cbd01.cbd01json = mapJson;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTMifQ==