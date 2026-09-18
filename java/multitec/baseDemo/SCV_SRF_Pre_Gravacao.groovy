package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase

class SCV_SRF_Pre_Gravacao extends FormulaBase {
	
	private Eaa01 eaa01;
	private Integer gravar = 1; //0-Não 1-Sim
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		
		//Efetuar validações, consistências, etc...
		
		put("gravar", gravar);
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==