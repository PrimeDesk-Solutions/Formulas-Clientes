package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo
import sam.dto.sfp.SFPVarDto
import sam.model.entities.aa.Aap18
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba01011
import sam.server.samdev.formula.FormulaBase
import sam.server.samdev.utils.SFPUtils

public class EveBaseDiasValor extends FormulaBase {
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
		Aap18 aap18 = abh80.getAbh80unidPagto();
		
		def valor = 0;
		def refDias = fba01011.fba01011base == null ? 0 : fba01011.fba01011base;

		 if (refDias > 0) {
			def salario = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
			def salarioDia = salario / 30;
			valor = (salarioDia * refDias) * fba01011.fba01011eve.abh21fator;
		}
		
		fba01011.fba01011refHoras = round(refDias, 2);
		fba01011.fba01011valor = round(valor, 2);
		
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==