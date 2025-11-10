package multitec.formulas.cgs.eventos;

import sam.dto.sfp.SFPVarDto;
import sam.model.entities.ab.Abh80;
import sam.model.entities.ab.Aba2001;
import sam.model.entities.fb.Fba01011;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;
import sam.server.samdev.utils.SFPUtils;

/**
 * 2006 - Vale transporte
 */
public class Eve2006 extends FormulaBase {

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


		def valeTransp = 0;
		def qtVale = 0;

		if(abh80.abh80vtDesc > 0){
			if(fba01011.fba01011base != null && fba01011.fba01011base > 0){
				qtVale = fba01011.fba01011base;		
			}else{
				qtVale = abh80.getAbh80vt0() + abh80.getAbh80vt1() + abh80.getAbh80vt2() + abh80.getAbh80vt3() + abh80.getAbh80vt4();
			}

			if(abh80.getAbh80vt0() > 0){
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-VLR-VALE-TRANSP", "aba2001lcto = 0");	
				def valor = aba2001?.getAba2001json()?.get("sfp_vt_valor") ?: 0;
				valeTransp = valeTransp + (valor * abh80.getAbh80vt0());
			}

			if(abh80.getAbh80vt1() > 0){
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-VLR-VALE-TRANSP", "aba2001lcto = 1");	
				def valor = aba2001?.getAba2001json()?.get("sfp_vt_valor") ?: 0;
				valeTransp = valeTransp + (valor * abh80.getAbh80vt1());
			}
			
			if(abh80.getAbh80vt2() > 0){
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-VLR-VALE-TRANSP", "aba2001lcto = 2");	
				def valor = aba2001?.getAba2001json()?.get("sfp_vt_valor") ?: 0;
				valeTransp = valeTransp + (valor * abh80.getAbh80vt2());
			}
			
			if(abh80.getAbh80vt3() > 0){
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-VLR-VALE-TRANSP", "aba2001lcto = 3");	
				def valor = aba2001?.getAba2001json()?.get("sfp_vt_valor") ?: 0;
				valeTransp = valeTransp + (valor * abh80.getAbh80vt3());
			}
			
			if(abh80.getAbh80vt4() > 0){
				Aba2001 aba2001 = getAcessoAoBanco().buscarRepositorioJson("SFP-VLR-VALE-TRANSP", "aba2001lcto = 4");	
				def valor = aba2001?.getAba2001json()?.get("sfp_vt_valor") ?: 0;
				valeTransp = valeTransp + (valor * abh80.getAbh80vt4());
			}

			if(valeTransp > 0 && abh80.abh80vtDesc == 2){ //2-Proporcional ao salário
				def salarioMensal = sfpUtils.buscarValorDoSalarioMes(abh80.abh80id);
			
				salarioMensal = salarioMensal * 6 / 100; // Cálculo de 6% do salário

				if(valeTransp > salarioMensal){
					valeTransp = salarioMensal;
				}
			}
		}

		fba01011.fba01011base = 0;
		fba01011.fba01011refUnid = qtVale;
		fba01011.fba01011valor = round(valeTransp, 2);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTEifQ==