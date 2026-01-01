package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2002 - Imposto de Renda
 **/
public class Eve2002 extends FormulaBase {

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
		Abh80 abh80 = sfpUtils.getSfpVarDto().getAbh80();

		def ir = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			ir = fba01011.fba01011base;

		}else{
			//Aplica a tabela de IR
			def qtdDepIR = sfpUtils.buscarQtdeDependentesIR(abh80.getAbh80id());

			ir = sfpUtils.aplicarTabelaDeIR("001", sfpUtils.buscarValor("9030"), qtdDepIR); //RB IR Salários

			//Diminui o imposto já recolhido no mês
			def irAnt = sfpUtils.buscarValorNoCalculo(null, true, fba0101.fba0101dtPgto, true, true, "2002", [0, 1, 9] as Integer[]);

			ir = ir - irAnt;
			if(ir < 0) ir = 0;
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(ir, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==