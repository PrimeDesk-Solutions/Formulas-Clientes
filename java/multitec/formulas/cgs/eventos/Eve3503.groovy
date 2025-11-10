package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 3503 - Pensão Judicial sobre Férias
 **/
public class Eve3503 extends FormulaBase {
	
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

		def pensaoAlimenticia = 0;
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			pensaoAlimenticia = fba01011.fba01011base;
			
		}else{
			def txPensao = abh80?.getAbh80json()?.get("trab_taxa_pensao") ?: 0;
			if(txPensao > 0){
				pensaoAlimenticia = sfpUtils.buscarValor("9095") * txPensao / 100;
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = round(pensaoAlimenticia, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==