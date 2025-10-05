package multitec.baseDemo;

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.SCEUtils

public class SCE_EstoqueInicial extends FormulaBase{
	private Bcc01 bcc01;
	private Abm0101 abm0101;
	
	@Override
	public void executar() {
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101)get("abm0101");
		
		//Cálculo do Preço Médio
		def pmu = 0;
		if(bcc01.bcc01custo > 0) {
			pmu = bcc01.bcc01custo / bcc01.bcc01qt;
			pmu = round(pmu, 6);
		}
				
		//Atualizar dados do lançamento
		bcc01.bcc01pmu = pmu;
		bcc01.bcc01custoVar = 0;
		bcc01.bcc01custoFixo = 0;
		
		//Atualizar o novo Preço Médio no cadastro do item
		abm0101.abm0101pmu = bcc01.bcc01pmu;
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==