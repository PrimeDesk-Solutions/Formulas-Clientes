package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.fb.Fba0101;
import sam.model.entities.fb.Fba01011;
import sam.model.entities.fb.Fbd10;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 8501 - INSS Rescisão
 **/
public class Eve8501 extends FormulaBase {

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
		Fbd10 fbd10 = get("fbd10");
		
		def inss = 0;
		
		if (fba01011.fba01011base != null && fba01011.fba01011base > 0) {
			inss = fba01011.fba01011base;
			
		}else{ 
			if(abh80.abh80contribInss == 1) { //Contribuinte do INSS
				def inssOutras = sfpUtils.buscarValor("9660"); //INSS de Outras Empresas
						
				//BC do INSS das férias do mês
				def scInssFerias = sfpUtils.buscarValorNoCalculo("*", true, fbd10.fbd10dtRes, true, false, "9021", [3] as Integer[]); 	//SC INSS Empresa Férias
				def inssFerias = sfpUtils.buscarValorNoCalculo("*", true, fbd10.fbd10dtRes, true, false, "3501", [3] as Integer[]); 	//INSS Sobre Férias
				
				//BC do INSS do mês
				def scInssMes = sfpUtils.buscarValorNoCalculo("*", true, fbd10.fbd10dtRes, true, false, "9020", [0] as Integer[]);		//SC INSS Empresa
				def inssMes = sfpUtils.buscarValorNoCalculo("*", true, fbd10.fbd10dtRes, true, false, "2001", [0] as Integer[]);		//INSS
		
				//Total SC INSS Empresa 
				def scInss = sfpUtils.buscarValor("9020") + scInssFerias + scInssMes; 
				
				//Limita o SC ao teto do INSS
				def tetoINSS = sfpUtils.buscarTetoDoInss("001");
				if(scInss > tetoINSS) {
					scInss = tetoINSS;
				}
				
				//Aplica a tabela de INSS
				inss = sfpUtils.aplicarTabelaDeINSS("001", scInss);
						
				//Desconta do INSS os valores de INSS já descontados
				inss = inss - inssFerias - inssMes - inssOutras;
				if(inss < 0) inss = 0;	
			}
		}
		
		fba01011.fba01011base = 0;
		fba01011.fba01011valor = trunc(inss, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==