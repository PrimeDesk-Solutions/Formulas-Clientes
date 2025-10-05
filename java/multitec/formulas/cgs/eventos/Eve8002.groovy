package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8002 - Aviso Prévio Indenizado
 **/
public class Eve8002 extends FormulaBase {
	
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
		
		def avisoPrevio = 0;
		def diasAP = fbd10.fbd10apDias;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			avisoPrevio = fba01011.fba01011base;
			
		}else{
			avisoPrevio = fbd10.fbd10ssRem;
			if(diasAP != 30){
				avisoPrevio = avisoPrevio / 30 * diasAP;
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(diasAP, 2);
		fba01011.fba01011valor = round(avisoPrevio, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==