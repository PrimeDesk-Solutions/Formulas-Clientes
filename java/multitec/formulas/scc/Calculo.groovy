package multitec.formulas.scc;

import sam.dicdados.FormulaTipo
import sam.model.entities.dc.Dcd01
import sam.server.samdev.formula.FormulaBase

public class Calculo extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCC_CALCULO; 
	}

	@Override 
	public void executar() {
		Dcd01 dcd01 = get("dcd01");
		def dcd01json = dcd01.dcd01json;

		def vales = dcd01json.getBigDecimal("vales") == null ? 0 : dcd01json.getBigDecimal("vales");
		def plr = dcd01json.getBigDecimal("plr") == null ? 0 : dcd01json.getBigDecimal("plr");
		def liquido = dcd01.dcd01totDoc + vales + plr;

		dcd01.dcd01liquido = liquido;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzcifQ==