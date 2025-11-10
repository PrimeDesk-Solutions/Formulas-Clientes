package multitec.baseDemo;

import br.com.multitec.utils.ValidacaoException;
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abe4001;
import sam.server.samdev.formula.FormulaBase;

public class CGS_TabelaPreco extends FormulaBase{

	private Abe4001 abe4001;
	
	@Override
	public void executar() {
		abe4001 = (Abe4001)get("abe4001");
		
		if(abe4001 == null) throw new ValidacaoException("Necessário informar o item da tabela de preço.");
		
		TableMap mapJson = abe4001.abe4001json == null ? new TableMap() : abe4001.abe4001json;
		if(mapJson == null || mapJson.size() == 0) return;
				
		def custo_cpra = mapJson.getBigDecimal("custo_cpra");
		if(custo_cpra == null)custo_cpra = 0;
		
		def imposto = mapJson.getBigDecimal("imposto");
		if(imposto == null)imposto = 0;
		
		def lucro = mapJson.getBigDecimal("lucro");
		if(lucro == null)lucro = 0;
		
		def preco = custo_cpra + imposto + lucro;
		
		abe4001.abe4001preco = preco;
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.TABELA_PRECO;
	}

}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzAifQ==