package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1999 - Complemento de salário
 **/
public class Eve1999 extends FormulaBase {
	
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

		def salarioLiquido = sfpUtils.somarRendimentos() - sfpUtils.somarDescontos() - fba01011.getFba01011valor();
		def complemento = 0;

		if(salarioLiquido < 0) {
			complemento = salarioLiquido.negate();
		}

		if(complemento > 0){
			def salarioFamilia = sfpUtils.buscarValor("1015");
			if(salarioFamilia > 0) {
				complemento = complemento + salarioFamilia;
			}	
		}
				
		fba01011.fba01011valor = round(complemento, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==