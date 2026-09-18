package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm01
import sam.model.entities.bg.Bgb01
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.Parametro

class SGP_CustoItens extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SGP;
	}

	@Override
	public void executar() {
		Bgb01 bgb01 = get("bgb01");
		Abm01 abm01 = get("abm01");
		def quantidade = get("quantidade");
		
		def pmu = getAcessoAoBanco().obterBigDecimal("SELECT abm0101pmu FROM abm0101 WHERE abm0101item = :abm01id", Parametro.criar("abm01id", abm01.abm01id));
		
		def custo = pmu != null ? pmu * quantidade : 0;
		put("custo", custo);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNzEifQ==