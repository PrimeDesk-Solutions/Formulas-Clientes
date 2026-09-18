package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8161 - Férias Vencidas
 **/
public class Eve8161 extends FormulaBase {
	
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
		
		def feriasVencidas = 0;
		def diasDireito = fbd10.fbd10fvDiasDir;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			feriasVencidas = fba01011.fba01011base;
			
		}else{
			feriasVencidas = fbd10.fbd10fvRem / 30 * diasDireito;
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(diasDireito, 2);
		fba01011.fba01011valor = round(feriasVencidas, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==