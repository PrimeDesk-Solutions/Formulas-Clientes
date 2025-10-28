package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.ab.Abh80;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9523 - FGTS Aviso Prévio Indenizado
 **/
public class Eve9523 extends FormulaBase {

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

		def aliq = abh80.abh80fgtsAliq; // TODO veficar RAT
		def fgts = 0;
		def rat = 0;
		
		if(aliq > 0){
			def bc = sfpUtils.buscarValor("9053"); //BC FGTS Aviso Prévio Indenizado
			rat = bc * aliq / 100;
		}

		fba01011.fba01011refUnid = round(aliq, 2);
		fba01011.fba01011valor = round(rat, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==