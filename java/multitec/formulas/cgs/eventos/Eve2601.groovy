package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2601 - IR PLR
 **/
public class Eve2601 extends FormulaBase {
	
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

		def ir = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			ir = fba01011.fba01011base;
			
		}else{
			//Aplica a tabela de IR
			def qtdDepIR = sfpUtils.buscarQtdeDependentesIR(sfpVarDto.getAbh80().getAbh80id());
			ir = sfpUtils.aplicarTabelaDeIR("001", sfpUtils.buscarValor("9035"), qtdDepIR);
		}
		
		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(ir, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==