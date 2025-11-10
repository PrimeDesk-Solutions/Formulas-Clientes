package multitec.baseDemo

import java.math.BigDecimal

import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

class SPC_RateioCustoVariavel extends FormulaBase {
	
	private Long idProduto;
	private BigDecimal fatorProduto;
	private BigDecimal qtdeFabricadaProduto;
	private BigDecimal custoVariavelProduto;
	private BigDecimal custoFixoTotal;
	private BigDecimal qtdeFabricadaTotal;
	private BigDecimal custoVariavelTotal;
	
	private BigDecimal custoFixoProduto;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPC_CUSTO;
	}

	@Override
	public void executar() {
		idProduto = get("idProduto");
		fatorProduto = get("fatorProduto");
		qtdeFabricadaProduto = get("qtdeFabricadaProduto");
		custoVariavelProduto = get("custoVariavelProduto");
		custoFixoTotal = get("custoFixoTotal");
		qtdeFabricadaTotal = get("qtdeFabricadaTotal");
		custoVariavelTotal = get("custoVariavelTotal");
		custoFixoProduto = 0.0;
		
		if(custoFixoTotal > 0 && custoVariavelTotal > 0 && custoVariavelProduto > 0) {
			def percentualProduto = (custoVariavelProduto * 100) / custoVariavelTotal;
			custoFixoProduto = (custoFixoTotal * percentualProduto) / 100;
			custoFixoProduto = round(custoFixoProduto, 2);
		}
		
		put("custoFixoProduto", custoFixoProduto);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTAifQ==