package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2009 - Adiantamento 
 **/
public class Eve2009 extends FormulaBase {

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
		Fba0101 fba0101 = get("fba0101");
		Abh80 abh80 = sfpVarDto.getAbh80();

		
		def adiantamento = 0;
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			adiantamento = fba01011.fba01011base;
			
		}else{
			adiantamento = sfpUtils.buscarValorNoCalculo("*", true, fba0101.getFba0101dtCalc(), true, false, "1004", [1] as Integer[]); 		
		}
		
		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(adiantamento, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==