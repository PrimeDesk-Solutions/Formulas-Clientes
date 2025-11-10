package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;

import java.util.List;

import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9516 - Valor RAT Empresa - 13º Salario
 **/
public class Eve9538 extends FormulaBase {

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

		def taxa = sfpUtils.buscarTaxaRAT() * sfpUtils.buscarIndiceFAP();
		def rat = 0;
		
		if(taxa > 0){
			def bc = sfpUtils.buscarValor("9029"); //BC para encargos 13 salario
			rat = bc * taxa / 100;
		}

		fba01011.fba01011refUnid = round(taxa, 2);
		fba01011.fba01011valor = round(rat, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==