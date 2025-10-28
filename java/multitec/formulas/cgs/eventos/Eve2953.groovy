package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2953 - Acidente do Trabalho 
 **/
public class Eve2953 extends FormulaBase {
	private def diasAfastados
	
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
		Abh80 abh80 = sfpVarDto.abh80;

		diasAfastados = 0;
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			diasAfastados = fba01011.fba01011base;
		}else{
			diasAfastados = sfpUtils.buscarDiasAfastados(fba01011.getFba01011eve().getAbh21codigo(), fba0101.getFba0101dtCalc());
		}
		
		def salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
		def valor = salarioMensal / 30 * diasAfastados;
		
		fba01011.fba01011refDias = round(diasAfastados, 2);
		fba01011.fba01011valor = round(valor, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==