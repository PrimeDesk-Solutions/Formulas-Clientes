package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1955 - Auxílio doença
 **/
public class Eve1955 extends FormulaBase {
	
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

		
		def salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
		def refDias;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			salarioMensal = salarioMensal / 30 * fba01011.fba01011base;
			refDias = fba01011.fba01011base;
			
		}else {
			def diasAfastados = sfpUtils.buscarDiasAfastados('1955', fba0101.fba0101dtCalc);
		
			salarioMensal = salarioMensal / 30 * diasAfastados;
			refDias = diasAfastados;
		}

		fba01011.fba01011base = 0
		fba01011.fba01011refDias = round(refDias, 2);
		fba01011.fba01011valor = round(salarioMensal, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==