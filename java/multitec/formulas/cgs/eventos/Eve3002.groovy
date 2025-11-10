package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbc0101;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 3002 - Abono Pecuniário
 **/
public class Eve3002 extends FormulaBase {

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
		Fbc0101 fbc0101 = get("fbc0101");

		def valorAbono = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			valorAbono = fba01011.fba01011base;
			
		}else{ 
			if(fbc0101.fbc0101diasAbono == 30) {
				valorAbono = fbc0101.fbc0101mr;
			}else {
				valorAbono = fbc0101.fbc0101mr / 30 * fbc0101.fbc0101diasAbono;
			}
		}

		fba01011.fba01011refDias = round(fbc0101.fbc0101diasAbono, 2);
		fba01011.fba01011valor = round(valorAbono, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==