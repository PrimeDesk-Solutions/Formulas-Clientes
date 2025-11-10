package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh21
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbc01
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 6002 - Maior Remuneração Férias - Média de horas extras 50%
 **/
public class Eve6002 extends FormulaBase {
	
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
		Abh80 abh80 = sfpVarDto.getAbh80();
		
		def fbc01pai = get("fbc01pai");
		def fbc01paf = get("fbc01paf");

		def mediaHR = 0;
		def horas = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			mediaHR = fba01011.fba01011base;
			
		}else{
			Abh21 eveOrigem = fba01011.fba01011eve.abh21origem;
			
			if(eveOrigem != null) {
				def salarioHora = sfpUtils.buscarValorDoSalarioHora(abh80.abh80id);

				horas = sfpUtils.buscarMediaDeRefHoras(eveOrigem.abh21codigo, fbc01pai, fbc01paf);

				horas = round(horas, 2);
				mediaHR = (horas * salarioHora) * eveOrigem.abh21fator;
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refHoras = horas;
		fba01011.fba01011valor = round(mediaHR, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==