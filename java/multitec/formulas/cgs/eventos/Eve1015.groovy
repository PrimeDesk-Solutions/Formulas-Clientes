package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;
import sam.model.entities.ab.Abh80;

/**
 * 1015 - Salário Família
 */
public class Eve1015 extends FormulaBase {

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

		def diasLicMaternidade = sfpUtils.buscarDiasAfastados("1961", fba0101.fba0101dtCalc);
		def diasFerias = sfpUtils.calcularDiasDeFerias(fba0101.fba0101dtCalc);

		def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fba0101.fba0101dtCalc);
		diasTrabalhados = diasTrabalhados + diasLicMaternidade + diasFerias;
		if (diasTrabalhados > 30 || (diasTrabalhados > 27 && fba0101.fba0101dtCalc.getMonthValue() == 2)) {
			diasTrabalhados = 30;
		}

		def bcSalFamFerias = sfpUtils.buscarValorNoCalculo(null, true, fba0101.fba0101dtCalc, true, false, "9061", [3] as Integer[]); //BC Salário Família Férias
		def bcSalFam = sfpUtils.buscarValor("9060"); //BC Salário Família
		def totalBc = bcSalFamFerias + bcSalFam;
		def qtdDepSF = sfpUtils.buscarQtdeDependentesSF(abh80.abh80id);
		
		def salarioFamilia = sfpUtils.buscarSalarioFamilia("001", totalBc, qtdDepSF);
		salarioFamilia = salarioFamilia / 30 * diasTrabalhados;

		fba01011.fba01011refUnid = qtdDepSF;
		fba01011.fba01011valor = round(salarioFamilia, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==