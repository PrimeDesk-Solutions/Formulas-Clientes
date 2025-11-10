package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8504 - INSS sobre 13° Salário da Rescisão
 **/
public class Eve8504 extends FormulaBase {
	
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
		Abh80 abh80 = sfpVarDto.getAbh80();

		def inss = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			inss = fba01011.fba01011base;
			
		}else{
			// Aplica a tabela do INSS			
			inss = sfpUtils.aplicarTabelaDeINSS("001", sfpUtils.buscarValor("9024"));
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = trunc(inss, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==