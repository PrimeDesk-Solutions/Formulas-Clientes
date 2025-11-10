package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1998 - Arredondamento do mês
 **/
public class Eve1998 extends FormulaBase {
	
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
		def arredondamento = 0;
		
		if(salarioLiquido > 0) {
			def valor = salarioLiquido - salarioLiquido.intValue();
			
			if(valor > 0) {
				arredondamento = 1 - valor
				if (arredondamento > 0.99){
					arredondamento = 0.99
				}
			}
		}
		
		fba01011.fba01011valor = round(arredondamento, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==