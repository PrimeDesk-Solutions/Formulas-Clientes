package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.ba.Baa0101
import sam.server.samdev.formula.FormulaBase

class SPP_PP extends FormulaBase {
	
	private Baa0101 baa0101;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPP_PLANO_DE_PRODUCAO;
	}
	
	@Override
	public void executar() {
		baa0101 = get("baa0101");
		
		def necessidadeLiquida = baa0101.baa0101nb - baa0101.baa0101se - baa0101.baa0101rp - baa0101.baa0101oc - baa0101.baa0101op - baa0101.baa0101pp + baa0101.baa0101em;
		
		if(necessidadeLiquida < 0) necessidadeLiquida = 0;
		
		baa0101.baa0101nl = necessidadeLiquida;
		
		baa0101.baa0101ap = necessidadeLiquida;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODIifQ==