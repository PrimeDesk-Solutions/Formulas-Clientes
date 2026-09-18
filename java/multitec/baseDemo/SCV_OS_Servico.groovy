package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.cb.Cbd0101
import sam.server.samdev.formula.FormulaBase

class SCV_OS_Servico extends FormulaBase {
	
	private Cbd0101 cbd0101;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_OS_SERV;
	}
	
	@Override
	public void executar() {
		cbd0101 = (Cbd0101)get("cbd0101");
		
		TableMap mapJson = cbd0101.cbd0101json == null ? new TableMap() : cbd0101.cbd0101json;
		def valoradm = mapJson.getBigDecimal_Zero("valor") + mapJson.getBigDecimal_Zero("valor1") + mapJson.getBigDecimal_Zero("valor2");
		
		mapJson.put("valoradm", valoradm);
		cbd0101.cbd0101json = mapJson;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTQifQ==