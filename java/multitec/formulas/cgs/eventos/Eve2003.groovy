package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2003 - Contribuição Sindical
 **/
public class Eve2003 extends FormulaBase {
	
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

		def contribSindical = 0;
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			contribSindical = fba01011.fba01011base;
			
		}else{
			if(abh80.abh80sindCs == 1){ //0-Não contribuinte 1-Contribuinte 2-Pago
				//Composição das datas
				def mesCalc = fba0101.fba0101dtCalc.getMonthValue();
				def anoCalc = fba0101.fba0101dtCalc.getYear();
				
				def mesAdmis = abh80.abh80dtAdmis.getMonthValue();
				def anoAdmis = abh80.abh80dtAdmis.getYear();
		
				//Calcula a contribuição sindical
				def salarioDia = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id) / 30;
		
				if(mesCalc == 3 && (anoCalc - anoAdmis) > 0) {
					contribSindical = salarioDia;
				}else if(mesCalc > 3 && mesCalc != mesAdmis) {
					contribSindical = salarioDia;
				}else if(mesCalc == 1 && mesAdmis == 12 && (anoCalc - anoAdmis) == 1) {
					contribSindical = salarioDia;
				}	
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(contribSindical, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==