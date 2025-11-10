package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8162 - Férias Proporcionais
 **/
public class Eve8162 extends FormulaBase {
	
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
		Fbd10 fbd10 = get("fbd10");
		
		def feriasProp = 0;
		def diasDireito = fbd10.fbd10fpDiasDir;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			feriasProp = fba01011.fba01011base;
			
		}else{
			feriasProp = fbd10.fbd10fpRem / 30 * diasDireito;
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(diasDireito, 2);
		fba01011.fba01011valor = round(feriasProp, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==