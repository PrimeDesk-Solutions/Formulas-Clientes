package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9999 - Salário Contratual
 **/
public class Eve9999 extends FormulaBase {
	
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

		def bc = sfpUtils.buscarValor("9030");	//RB Imposto Renda
		bc = bc + sfpUtils.buscarValor("9003");	//BC FGTS 13º SALÁRIO
		
		def salario = 0;
		if(bc > 0) {
			salario = sfpVarDto.getAbh80().getAbh80salario();
		}
		
		fba01011.setFba01011valor(round(salario, 2));
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==