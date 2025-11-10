package multitec.formulas.cgs.eventos;

import sam.dicdados.FormulaTipo;
import sam.dto.sfp.SFPVarDto
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba0101
import sam.model.entities.fb.Fba01011
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SFPUtils

/**
 * 4001 - Primeira parcela 13° salário
 **/
public class Eve4001 extends FormulaBase {
	
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

		def decTerceiro = 0;
		def mesesDireito = 0;

		def adiantamento = sfpUtils.buscarValorNoUltimoCalculo13Salario(null, true, false, fba0101.getFba0101dtCalc(), "9594"); //Total rend.  adiant. 13º nas férias
		if (adiantamento == 0) {
			mesesDireito = fba0101.getDtsMesesDireito() / 2;
		
			if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
				decTerceiro = fba01011.fba01011base;
			}else{
				decTerceiro = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id) / 12 * mesesDireito;
			}
		}
		
		fba01011.fba01011base = 0;
		fba01011.fba01011refUnid = round(mesesDireito, 2);
		fba01011.fba01011valor = round(decTerceiro, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==