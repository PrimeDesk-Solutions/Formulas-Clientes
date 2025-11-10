package multitec.baseDemo;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.bc.Bcd0101;
import sam.server.samdev.formula.FormulaBase;

public class SCE_SI extends FormulaBase{

	private Bcd0101 bcd0101;
	
	@Override
	public void executar() {
		bcd0101 = (Bcd0101)get("bcd0101");
		
		if(bcd0101 == null) throw new ValidacaoException("Necessário informar o item da solicitação.");
		
	    //Preço Livre (json) = Quantidade Solicitada
	    TableMap mapJson = bcd0101.bcd0101json == null ? new TableMap() : bcd0101.bcd0101json;
		mapJson.put("preco", bcd0101.bcd0101qtS);
		bcd0101.bcd0101json = mapJson;
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_DOCUMENTOS;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNjAifQ==