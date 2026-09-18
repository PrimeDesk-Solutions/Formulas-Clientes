package Atilatte.formulas.sce;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException

public class SceMovimentacaoGeral extends FormulaBase{
		private Bcc01 bcc01;
	     private Abm0101 abm0101;
	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.LCTO_SCE; 
	}

	@Override
	public void executar() {
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101) get("abm0101");
		TableMap abm0101mapJson = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;	
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
		abm0101mapJson.put("ultimo_preco", ultimo);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==