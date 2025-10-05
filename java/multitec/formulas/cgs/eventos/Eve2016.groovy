package multitec.formulas.cgs.eventos;

import java.math.RoundingMode;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2016 - Vale Refeição
 **/
public class Eve2016 extends FormulaBase {
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.EVENTOS;
	}
	
	@Override
	public void executar() {
		SFPUtils sfpUtils = get("sfpUtils");
		SFPVarDto sfpVarDto = sfpUtils.getSfpVarDto();
		List<Fba01011> fba01011s = sfpVarDto.getFba01011s();
		Fba01011 fba01011 = fba01011s.get(sfpVarDto.getIndex());

		def diasTrabalhados = 0;	
		
		def valeAlim = 0;
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			diasTrabalhados = fba01011.fba01011base;
		}else{
			diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc);
		}

		valeAlim = diasTrabalhados * 20;

		fba01011.fba01011refDias = round(diasTrabalhados, 2);
		fba01011.fba01011valor = round(valeAlim, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==