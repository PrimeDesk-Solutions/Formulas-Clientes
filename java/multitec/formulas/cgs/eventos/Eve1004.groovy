package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1004 - Adiantamento de Salário
 */
public class Eve1004 extends FormulaBase {
	
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

		def valorAdiantamento = 0;
		
		//Adiantamento informado
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			valorAdiantamento = fba01011.fba01011base;

		}else{
			def diasMaternidade = sfpUtils.buscarDiasAfastados("1961", fba0101.fba0101dtPgto);
			def salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
		
			//Adiantamento de 40% para trabalhadora com licença maternidade
			if(diasMaternidade > 0 && fba0101.fba0101dtCalc() >= abh80.abh80dtAdmis) {
				valorAdiantamento = salarioMensal * 0.4;

			//Adiantamento de 40% proporcional aos dias trabalhados
			}else {	
				def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc);
	   			if (diasTrabalhados > 30 || (diasTrabalhados > 27 && fba0101.fba0101dtCalc.getMonthValue() == 2)) {
					diasTrabalhados = 30;
				}
						
				valorAdiantamento = (salarioMensal / 30 * diasTrabalhados) * 0.4
			}
		}
		
		fba01011.fba01011base = 0
		fba01011.fba01011valor = round(valorAdiantamento, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==