package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.aa.Aap18;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1001 - Salário Mensal
 */
public class Eve1001 extends FormulaBase {
	
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
		Aap18 aap18 = abh80.getAbh80unidPagto();
		
		def diasAtestado = sfpUtils.buscarDiasAfastados("1313", fba0101.fba0101dtCalc);
		
		def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc) + diasAtestado;	
		if (diasTrabalhados > 30 || (diasTrabalhados > 27 && fba0101.fba0101dtCalc.getMonthValue() == 2)) {
			diasTrabalhados = 30;
		}
		
		// verifica se a referencia dias foi digitado
		if(fba01011.fba01011refDias != null && fba01011.fba01011refDias > 0) {
			diasTrabalhados = fba01011.fba01011refDias;
		}

		def salarioMensal = 0;
		
		//Salário mensal informado
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			salarioMensal = fba01011.fba01011base;
			
		}else{ //Salário mensal calculado
			if (aap18.aap18codigo == "5") {
				salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
				
				if (diasTrabalhados < 30) {
					salarioMensal = salarioMensal / 30 * diasTrabalhados
			   	}
			}else{
				diasTrabalhados = 0
				salarioMensal = 0							      
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = diasTrabalhados;
		fba01011.fba01011valor = round(salarioMensal, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==