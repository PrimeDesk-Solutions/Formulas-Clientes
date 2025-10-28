package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8201 - 13º Salário Proporcional
 **/
public class Eve8201 extends FormulaBase {
	
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

		def decTercProporc = 0;
		def mesesDireito = fbd10.fbd10dtMesesDir; // Meses de direito incluindo Aviso Indenizado
		def mesesAvIden = sfpUtils.buscarRefUnid('8202'); // Somente meses de Aviso indenizado
		mesesDireito = mesesDireito - mesesAvIden; // Meses de direito descontado os meses de aviso indenizado

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			decTercProporc = fba01011.fba01011base;
		}else{
			decTercProporc = fbd10.fbd10dtRem / 12 * mesesDireito; // Calculo o valor do 13º Proporcional
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refUnid = round(mesesDireito, 2);
		fba01011.fba01011valor = round(decTercProporc, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==