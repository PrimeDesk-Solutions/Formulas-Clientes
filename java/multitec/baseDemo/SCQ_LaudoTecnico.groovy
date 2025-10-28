package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.bb.Bbc01
import sam.server.samdev.formula.FormulaBase

public class SCQ_LaudoTecnico extends FormulaBase {
	
	private Bbc01 bbc01;
    private String procInvoc;

    @Override
    public FormulaTipo obterTipoFormula() {
        return FormulaTipo.SCQ_LT;
    }

    @Override
    public void executar() {
        bbc01 = get("bbc01");
		procInvoc = get("procInvoc");
		
		bbc01.bbc01garant = "Garantia gerada pela fórmula.";
		bbc01.bbc01oco = "Ocorrência gerada pela fórmula."
		bbc01.bbc01implic = "Implicação gerada pela fórmula.";
        bbc01.bbc01obs = "Observação gerada pela fórmula.";
    }
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTMwIn0=