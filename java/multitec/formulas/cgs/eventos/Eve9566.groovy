package multitec.formulas.cgs.eventos;

import java.util.List;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;
/**
 * 9566 - Contr INSS Empresa - Férias
 * @author Samuel
 *
 */
public class Eve9566 extends FormulaBase{
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
		
		def taxa = 20;
		def inss = 0;

		if(taxa > 0){
			inss = sfpUtils.buscarValor("9028"); //BC para encargos ferias
			inss = inss * taxa / 100;
		}

		fba01011.fba01011refUnid = round(taxa, 2);
		fba01011.fba01011valor = trunc(inss, 2);
		
	}


}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==