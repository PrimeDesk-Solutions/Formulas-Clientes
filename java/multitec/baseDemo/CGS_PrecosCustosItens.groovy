package multitec.baseDemo

import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.server.samdev.formula.FormulaBase

class CGS_PrecosCustosItens extends FormulaBase {
	
	Long abm01id;
	BigDecimal valorComposicao;
	BigDecimal valorProcesso;
	TableMap json;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.CGS_PRECOS_E_CUSTOS_DE_ITENS;
	}
	
	@Override
	public void executar() {
		abm01id = get("abm01id");
		valorComposicao = get("valorComposicao");
		valorProcesso = get("valorProcesso");
		json = get("json");
		
		//Custo Operacional
		def custo_operacional = valorComposicao + valorProcesso;
		json.put("custo_operacional", custo_operacional);
		
		def custo_preco_venda = custo_operacional;
		
		//Custo Indireto
		def custo_indireto_perc = json.getBigDecimal_Zero("custo_indireto_perc");
		custo_preco_venda = custo_preco_venda / ((100 - custo_indireto_perc) / 100);
		custo_preco_venda = round(custo_preco_venda, 6);
		def custo_indireto_vlr = custo_preco_venda - custo_operacional;
		json.put("custo_indireto_vlr", custo_indireto_vlr);
		
		//Custo Total
		def custo_total = custo_preco_venda;
		json.put("custo_total", custo_total);
		
		//Margem
		def custo_lucro_perc = json.getBigDecimal_Zero("custo_lucro_perc");
		custo_preco_venda = custo_preco_venda / ((100 - custo_lucro_perc) / 100);
		custo_preco_venda = round(custo_preco_venda, 6);
		def custo_lucro_vlr = custo_preco_venda - custo_operacional - custo_indireto_vlr;
		json.put("custo_lucro_vlr", custo_lucro_vlr);
		
		//Impostos
		def custo_impostos_perc = json.getBigDecimal_Zero("custo_impostos_perc");
		custo_preco_venda = custo_preco_venda / ((100 - custo_impostos_perc) / 100);
		custo_preco_venda = round(custo_preco_venda, 6);
		def custo_impostos_vlr = custo_preco_venda - custo_operacional - custo_indireto_vlr - custo_lucro_vlr;
		json.put("custo_impostos_vlr", custo_impostos_vlr);
		
		json.put("custo_preco_venda", custo_preco_venda);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzQifQ==