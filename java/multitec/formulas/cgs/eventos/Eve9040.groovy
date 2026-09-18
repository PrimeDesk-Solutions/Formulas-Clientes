package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9040 - Dependentes de IR
 **/
public class Eve9040 extends FormulaBase {
	
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

		def rb = sfpUtils.buscarValor("9030"); //RB Imposto de Renda
		def qtdDepIR = 0;
		def ir = 0;
		
		if(rb > 0) {
			qtdDepIR = sfpUtils.buscarQtdeDependentesIR(abh80.abh80id);
			ir = qtdDepIR * sfpUtils.buscarValorPorDependenteIR("001");
		}
		
		fba01011.fba01011refDias = round(qtdDepIR, 2);
		fba01011.fba01011valor = round(ir, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==