package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8030 - Adicional de Insalubridade
 */
public class Eve8030 extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}

	@Override
	public void executar() {
		SFPUtils sfpUtils = get("sfpUtils");
		SFPVarDto sfpVarDto = sfpUtils.getSfpVarDto();
		List<Fba01011> fba01011s = sfpVarDto.getFba01011s();
		Fba01011 fba01011 = fba01011s.get(sfpVarDto.getIndex());
		Fba0101 fba0101 = get("fba0101");
		Fbd10 fbd10 = get("fbd10");
		Abh80 abh80 = sfpVarDto.getAbh80();

		def valorInsalubridade = 0;

		def diasTrabalhados = sfpUtils.calcularDiasTrabalhados(fbd10.fbd10dtRes);	
		if (diasTrabalhados > 30 || (diasTrabalhados > 27 && fbd10.fbd10dtRes.getMonthValue() == 2)) {
			diasTrabalhados = 30;
		}

		//Insalubridade informada
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			valorInsalubridade = fba01011.fba01011base;

		}else{ //Insalubridade calculada
			def txInsalubridade = abh80?.getAbh80json()?.get("trab_taxa_insalub") ?: 0;

			if(txInsalubridade > 0) {
				def salarioMinimo = getAcessoAoBanco().buscarParametro("SALARIOMINIMO", "FB");
				if(salarioMinimo == null) salarioMinimo = 0;
				valorInsalubridade = salarioMinimo / 30 * diasTrabalhados;
				valorInsalubridade = valorInsalubridade * txInsalubridade / 100;
			}else{
				valorInsalubridade = 0;
				diasTrabalhados = 0;
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refDias = round(diasTrabalhados, 2);
		fba01011.fba01011valor = round(valorInsalubridade, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==