package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9509 - Terceiros - Aliquota total
 **/
public class Eve9509 extends FormulaBase {

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
		
		def bc = sfpUtils.buscarValor("9026");  //BC para encargos
		def salarioEducacao = 0;
		def aliquota = 0;
		
		if(bc > 0){
			aliquota = sfpUtils.buscarAliquotaDeTerceirosTotal();
			salarioEducacao = bc * aliquota /100;
		}

		fba01011.fba01011refUnid = round(aliquota, 2);
		fba01011.fba01011valor = round(salarioEducacao, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==