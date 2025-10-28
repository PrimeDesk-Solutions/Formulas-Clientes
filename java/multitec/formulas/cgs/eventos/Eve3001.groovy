package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbc0101;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 3001 - Férias normais
 **/
public class Eve3001 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.EVENTOS;
	}

	@Override
	public void executar() {
		SFPUtils sfpUtils = get("sfpUtils");
		SFPVarDto sfpVarDto = sfpUtils.getSfpVarDto();
		List<Fba01011> fba01011s = sfpVarDto.getFba01011s();
		Fba01011 fba01011 = fba01011s.get(sfpUtils.getSfpVarDto().getIndex());
		Fbc0101 fbc0101 = get("fbc0101");

		def valorFerias = 0;
		def refDias = 0;

		if(fba01011.fba01011refDias != null && fba01011.fba01011refDias > 0) {
			refDias = fba01011.fba01011refDias;
		}else {
			refDias = fbc0101.fbc0101diasFerias;
		}
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			valorFerias = fba01011.fba01011base;
			
		}else{ 
			if(fbc0101.fbc0101diasFerias == 30) {
				valorFerias = fbc0101.fbc0101mr;
			}else {
				valorFerias = fbc0101.fbc0101mr / 30 * fbc0101.fbc0101diasFerias;
			}
		}

		fba01011.fba01011refDias = round(refDias, 2);
		fba01011.fba01011valor = round(valorFerias, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==