package multitec.baseDemo

import java.time.LocalDate

import sam.dicdados.FormulaTipo
import sam.model.entities.cb.Cbe10
import sam.server.samdev.formula.FormulaBase

class SCV_Orcamento extends FormulaBase {
	
	private Cbe10 cbe10;
	private String procInvoc;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_DOC_ORCAMENTO;
	}
	
	@Override
	public void executar() {
		cbe10 = get("cbe10");
		procInvoc = get("procInvoc");
		
		if(!cbe10.isNew()) return;
		
		//Adicionando 30 dias a partir da data de hoje para se obter a data de vencimento
		LocalDate dataVencimento = LocalDate.now().plusDays(30);
		cbe10.cbe10vencto = dataVencimento;
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTE1In0=