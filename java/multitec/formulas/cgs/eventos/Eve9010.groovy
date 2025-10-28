package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 9010 - SC INSS empregado
 **/
public class Eve9010 extends FormulaBase {

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

		//Valores relacionados na tabela de eventos
		def totalEventos = sfpUtils.buscarValorEventosRelacionados();

		//Limitar o SC ao teto de contribuição
		def tetoINSS = sfpUtils.buscarTetoDoInss("001");
		if(totalEventos > tetoINSS) totalEventos = tetoINSS;
		if(totalEventos < 0) totalEventos = 0;

		fba01011.fba01011valor = round(totalEventos, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==