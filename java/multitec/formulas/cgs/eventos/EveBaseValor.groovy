package multitec.formulas.cgs.eventos;

import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * Fórmula genérica que recebe um valor na coluna "Base" por digitação ou oriundo das ocorrências e transfere para a coluna "Valor"
 * do evento, não considerando no cálculo as colunas de referências de horas, dias e unidades.
 */
public class EveBaseValor extends FormulaBase {
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.EVENTOS;
	}
	
	@Override
	public void executar() {
		SFPUtils sfpUtils = (SFPUtils) get("sfpUtils");
		List<Fba01011> fba01011s = sfpUtils.getSfpVarDto().getFba01011s();
		Fba01011 fba01011 = fba01011s.get(sfpUtils.getSfpVarDto().getIndex());
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			fba01011.fba01011valor = fba01011.fba01011base;	
		}else{
			fba01011.fba01011valor = 0;
		}
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==