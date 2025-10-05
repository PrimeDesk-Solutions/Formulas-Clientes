package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh04;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 1034 - Adicional por Tempo de Serviço
 **/
public class Eve1034 extends FormulaBase{

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
		
		def adicional = 0;
		if(abh80.getAbh80tmpServ() != null){
			Abh04 abh04 = getAcessoAoBanco().buscarRegistroUnicoById("Abh04", abh80.getAbh80tmpServ().getAbh04id());
			def salMensal = 0;
			def salMinimo = 0;

			if(abh04.getAbh04tipo() == 0){ 	//Adicional por índice
				if(abh04.getAbh04bc() == 0){	//Salário base
					salMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
					
				}else if(abh04.getAbh04bc() == 1){	//Salário mínimo
					salMinimo = getAcessoAoBanco().buscarParametro("SALARIOMINIMO", "FB");
				}
			}

			adicional = sfpUtils.buscarAdicionalPorTempoDeServico("9030", fba0101.fba0101dtCalc, salMensal, salMinimo);	
		}
		
		fba01011.fba01011valor = round(adicional, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==