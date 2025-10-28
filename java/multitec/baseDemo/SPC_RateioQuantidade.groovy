package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

class SPC_RateioQuantidade extends FormulaBase {
	
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
		
		if(custoFixoTotal > 0 && qtdeFabricadaTotal > 0 && qtdeFabricadaProduto > 0) {
			def percentualProduto = (qtdeFabricadaProduto * 100) / qtdeFabricadaTotal;
			custoFixoProduto = (custoFixoTotal * percentualProduto) / 100;
			custoFixoProduto = round(custoFixoProduto, 2);
		}
		
		put("custoFixoProduto", custoFixoProduto);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTAifQ==