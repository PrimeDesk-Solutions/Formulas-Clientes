package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.fb.Fba0101
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1030 - DSR de Horas Extras 100%
 **/
public class Eve1030 extends FormulaBase {
	
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
		Fba0101 fba0101 = get("fba0101");

		def mes = fba0101.fba0101dtCalc.getMonthValue();

		Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-DIAS-UTEIS-REMUN", "jGet(aba2001json.mes) = '" + mes + "'");
		def diasUteis = aba2001?.getAba2001json()?.get("dias_uteis") ?: 0;
		def diasRemun = aba2001?.getAba2001json()?.get("dias_remun") ?: 0;

		//Divide horas extras por dias uteis e dias remunerados
		def horasExtras = 0;
		if(diasUteis > 0) {
			horasExtras = sfpUtils.buscarRefHoras("1024");
			horasExtras = horasExtras / diasUteis * diasRemun;
		}

		//Cálculo hora extra
		def salarioHora = sfpUtils.buscarValorDoSalarioHora(abh80.abh80id);
		def valorDSR = horasExtras * salarioHora * fba01011.getFba01011eve().getAbh21fator();
			
		fba01011.fba01011refHoras = round(horasExtras, 2);
		fba01011.fba01011valor = round(valorDSR, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==