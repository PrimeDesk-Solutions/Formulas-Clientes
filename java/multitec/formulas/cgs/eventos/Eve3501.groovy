package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 3501 - INSS sobre férias
 **/
public class Eve3501 extends FormulaBase {
	
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
		
		def inss = 0;

		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			inss = fba01011.fba01011base;
			
		}else{ 
			if(abh80.abh80contribInss == 1) { //Contribuinte do INSS
				def inssOutras = sfpUtils.buscarValor("9660"); 	//INSS de Outras Empresas
				def scInssFerias = sfpUtils.buscarValor("9021");	//SC INSS Empresa Férias

				//Limita o SC ao teto do INSS
				def tetoINSS = sfpUtils.buscarTetoDoInss("001");
				if(scInssFerias > tetoINSS) {
					scInssFerias = tetoINSS;
				}

				//Aplica a tabela de INSS
				inss = sfpUtils.aplicarTabelaDeINSS("001", scInssFerias);

				//Desconta do INSS os valores de INSS de outras empresas
				inss = inss - inssOutras;
				if(inss < 0) inss = 0;	
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011valor = trunc(inss, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==