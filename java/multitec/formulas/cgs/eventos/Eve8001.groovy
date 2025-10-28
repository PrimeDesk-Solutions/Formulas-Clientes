package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.aa.Aap18;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8001 - Saldo de Salários
 **/
public class Eve8001 extends FormulaBase {

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

		def diasLicMat = sfpUtils.buscarDiasAfastados("1961", fba0101.fba0101dtCalc);

		def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc) - diasLicMat;
		if (aap18.aap18codigo == "5" && (diasTrabalhados > 30 || (diasTrabalhados > 27 && fba0101.fba0101dtCalc.getMonthValue() == 2))) {
			diasTrabalhados = 30;
		}

		def saldoSalario = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			saldoSalario = fba01011.fba01011base;

		}else{
			def salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
			saldoSalario = salarioMensal / 30 * diasTrabalhados;
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(diasTrabalhados, 2);
		fba01011.fba01011valor = round(saldoSalario, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==