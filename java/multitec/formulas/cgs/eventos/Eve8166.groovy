package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto
import sam.model.entities.fb.Fba01011
import sam.model.entities.fb.Fbd10
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils
/**
 * Eve8166 - Formula para calculo de 1/3 de férias sobre aviso indenizado
 * @author Samuel André
 *
 */
public class Eve8166 extends FormulaBase {
	@Override
	public FormulaTipo obterTipoFormula() {
		// TODO Auto-generated method stub
		return FormulaTipo.EVENTOS;
	}

	@Override
	public void executar() {
		SFPUtils sfpUtils = get("sfpUtils");
		SFPVarDto sfpVarDto = sfpUtils.getSfpVarDto();
		List<Fba01011> fba01011s = sfpVarDto.getFba01011s();
		Fba01011 fba01011 = fba01011s.get(sfpVarDto.getIndex());
		Fbd10 fbd10 = get("fbd10");
		
		def tercoFerias = 0;
		def valFerias = sfpUtils.buscarValor('8165');

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			tercoFerias = fba01011.fba01011base;
			
		}else{
			tercoFerias = valFerias / 3;
		}

		fba01011.fba01011valor = round(tercoFerias, 2);
		
	}


}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==