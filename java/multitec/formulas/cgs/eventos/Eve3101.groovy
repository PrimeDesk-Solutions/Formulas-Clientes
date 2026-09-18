package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 3101 - Diferença de férias
 **/
public class Eve3101 extends FormulaBase {

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

		def diferencaFerias = 0;
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			diferencaFerias = fba01011.fba01011base;
			
		}else{ 
			def salarioAtual = abh80.abh80salario;
			def salarioAntigo = salarioAtual;
			def salarioFerias = sfpUtils.buscarValorNoCalculo(null, false, fba0101.fba0101dtCalc, true, false, "9998", [3] as Integer[]);

			//Comparar salário pago nas férias com o salário atual
			if(salarioFerias != salarioAtual) {
				salarioAntigo = sfpUtils.buscarSalarioDoMesAnterior(fba0101.fba0101dtCalc);
			}
	
			if(salarioAntigo > 0) {
				def indice = (salarioAtual / salarioAntigo) - 1;
	
				if(indice > 0) {
					def ferias = sfpUtils.buscarValorNoCalculo(null, false, fba0101.fba0101dtCalc, true, false, "3001", [3] as Integer[]);
					diferencaFerias = ferias * indice;
				}
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(diferencaFerias, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==