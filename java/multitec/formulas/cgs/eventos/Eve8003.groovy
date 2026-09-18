package multitec.formulas.cgs.eventos;

import java.time.LocalDate
import java.time.temporal.ChronoUnit

import br.com.multitec.utils.DateUtils
import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto
import sam.model.entities.fb.Fba01011
import sam.model.entities.fb.Fbd10
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils

/**
 * 8003 - Multa Estabilidade Art. 479/CLT
 **/

public class Eve8003 extends FormulaBase{
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
		Fbd10 fbd10 = get("fbd10");
		
		def periodo1 = 45; // Define a quantidade de dias do primeiro periodo
		def periodo2 = 90; // Define a quantidade de dias do segundo periodo
		
		def valorMulta = 0;
		def restoExp = 0;
		
		def tempoServico = DateUtils.dateDiff(sfpVarDto.abh80.abh80dtAdmis, fbd10.fbd10dtRes, ChronoUnit.DAYS);
		def salario = sfpVarDto.abh80.abh80salario;
		def salarioDia = salario / 30;
		
		if(fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			valorMulta = fba01011.fba01011base;
		}else {
			if(tempoServico == periodo1 || tempoServico == periodo2) {
				valorMulta = 0;
			}else if(tempoServico < periodo1) {
				restoExp = periodo1 - (tempoServico + 1);
				valorMulta = (salarioDia * restoExp) / 2;
			}else {
				restoExp = periodo2 - (tempoServico + 1);
				valorMulta = (salarioDia * restoExp) / 2;
			}
		}
		
		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(restoExp, 2);
		fba01011.fba01011valor = round(valorMulta, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==