package multitec.formulas.sce;

import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.formula.FormulaBase;

public class SCE_PleEntSaiPrecoMedio extends FormulaBase{
	private Bcc01 bcc01;
	private Abm0101 abm0101;
	
	@Override
	public void executar() {
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101) get("abm0101");
			
		//Carga inicial
		def qtde = bcc01.bcc01qt_Zero;
		def pmAtual = abm0101.abm0101pmu_Zero;
		
		//Cálculo do Custo
		def custoBase = qtde * pmAtual;
		custoBase = round(custoBase, 2);
		
		//Atualizar dados do lançamento
		bcc01.bcc01custo = custoBase;
		bcc01.bcc01custoVar = 0;
		bcc01.bcc01custoFixo = 0;
		bcc01.bcc01pmu = pmAtual;
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==