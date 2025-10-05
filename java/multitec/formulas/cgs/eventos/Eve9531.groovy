package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;

import java.util.List;

import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9531 - Terceiros - 13º Salario
 **/
public class Eve9531 extends FormulaBase {

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
		
		def bc = sfpUtils.buscarValor("9029");  //BC para encargos 13 salario
		def terce13Sal = 0;
		def aliquota = 0;
		
		if(bc > 0){
			aliquota = sfpUtils.buscarAliquotaDeTerceirosTotal();
			terce13Sal = bc * aliquota /100;
		}

		fba01011.fba01011refUnid = round(aliquota, 2);
		fba01011.fba01011valor = round(terce13Sal, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==