package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh21
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils;

/**
 * 4008 - Média Adicional Insalubridade
 **/
public class Eve4008 extends FormulaBase {
	
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

		def media = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			media = fba01011.fba01011base;
			
		}else{
			def dtInicial = fba0101.getDtsDataInicial();
			def dtFinal = fba0101.getDtsDataFinal();
			def dtAdmissao = abh80.abh80dtAdmis;
			def salarioHora = sfpUtils.buscarValorDoSalarioHora(abh80.abh80id);
			 
			Abh21 eveOrigem = fba01011.fba01011eve.abh21origem;
			
			if(eveOrigem != null) {
				if(dtAdmissao > dtInicial && dtAdmissao <= dtFinal) {
					media = sfpUtils.buscarMediaDeValores(eveOrigem.abh21codigo, dtAdmissao, dtFinal);
				}else {
					media = sfpUtils.buscarMediaDeValores(eveOrigem.abh21codigo, dtInicial, dtFinal);
				}

				media = media / 2;
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(media, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==