package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8202 - 13º Salário Sobre Aviso Prévio Indenizado
 **/
public class Eve8202 extends FormulaBase {
	
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

		def dias = fbd10.getFbd10apDias();
		def meses = 0;
		def decTercProporc = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			decTercProporc = fba01011.fba01011base;
			
		}else if(dias >= 15 && dias < 45 ){
			decTercProporc = fbd10.fbd10dtRem / 12;
			meses = 1;
		}else if(dias >= 45 && dias < 75) {
			decTercProporc = (fbd10.fbd10dtRem / 12) * 2;
			meses = 2;
		}else if(dias >= 75) {
			decTercProporc = (fbd10.fbd10dtRem / 12) * 3;
			meses = 3;
		}

		fba01011.fba01011refUnid = meses;
		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(decTercProporc, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==