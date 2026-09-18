package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFPVarDto
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba01011
import sam.model.entities.fb.Fbd10
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.SFPUtils
/**
 * Eve8165 - Formula para Calculo de Férias Sobre Aviso Indenizado
 * @author Samuel André
 *
 */
public class Eve8165 extends FormulaBase {
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
		
		def feriasProp = 0;
		def mesesAvIden = fbd10.getFbd10apDias() / 30;
		def salario = sfpUtils.buscarValor('8001');

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			feriasProp = fba01011.fba01011base;
			
		}else{
			feriasProp = fbd10.fbd10fpRem / 12 * mesesAvIden;
		}


		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(mesesAvIden, 2);
		fba01011.fba01011valor = round(feriasProp, 2);
		
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==