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
 * 1002 - Salário Horista
 */
public class Eve1002 extends FormulaBase {
	
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

		def salarioHorista = 0;
		def refHoras = 0;
			
		//Qtde de horas informada
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			refHoras = fba01011.fba01011base;
		
		}else{
			if (aap18.aap18codigo == "1") {
				//Dias trabalhados
				def diasAtestado = sfpUtils.buscarDiasAfastados("1008", fba0101.fba0101dtCalc);
				def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc) + diasAtestado;

				//Horas a descontar
				def horasDescontar = sfpUtils.buscarRefHoras("1003")	               //DSR
				horasDescontar = horasDescontar + sfpUtils.buscarRefHoras("1008")  	//Atestado Médico
				horasDescontar = horasDescontar + sfpUtils.buscarRefHoras("1041")  	//Acidente do Trabalho
				horasDescontar = horasDescontar + sfpUtils.buscarRefHoras("1060")  	//Atestado ASO
			
				//Qtde de horas calculada
				refHoras = (diasTrabalhados * abh80.abh80hs) / 6
				refHoras = refHoras - horasDescontar
				refHoras = round(refHoras, 2)
			}
          }

          //Cálculo do Salário Horista
		salarioHorista = sfpUtils.buscarValorDoSalarioHora(abh80.abh80id);
		salarioHorista = salarioHorista * refHoras

          fba01011.fba01011base = 0
		fba01011.fba01011refHoras = refHoras;
		fba01011.fba01011valor = round(salarioHorista, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==