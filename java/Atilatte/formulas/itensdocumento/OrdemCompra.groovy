package Atilatte.formulas.itensdocumento;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.cb.Cbb0101;
import sam.server.samdev.formula.FormulaBase;
import sam.dicdados.FormulaTipo;

public class SCV_OC extends FormulaBase{

	private Cbb0101 cbb0101;
	
	@Override
	public void executar() {
		cbb0101 = (Cbb0101)get("cbb0101");
		
		if(cbb0101 == null) throw new ValidacaoException("Necessário informar o item da ordem de compra.");
		
	    //Preço Livre (json) = Quantidade Solicitada
	    TableMap mapJson = cbb0101.cbb0101json == null ? new TableMap() : cbb0101.cbb0101json;
		mapJson.put("preco", cbb0101.cbb0101qt);
		cbb0101.cbb0101json = mapJson;
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_DOCUMENTOS;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODEifQ==