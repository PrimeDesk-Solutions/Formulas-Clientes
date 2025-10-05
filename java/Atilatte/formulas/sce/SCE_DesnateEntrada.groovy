package Atilatte.formulas.sce;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException


public class SCE_DesnateEntrada extends FormulaBase{
	
		private Bcc01 bcc01;
	     private Abm0101 abm0101;
	
	
         
	@Override
	public void executar() {
		 bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101) get("abm0101");
		
		TableMap jsonBcc01 = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;
		TableMap jsonAbm0101 = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;

		// Custo Unitário
		jsonBcc01.put("unitario_estoque", jsonAbm0101.getBigDecimal_Zero("custo"));

		// Custo Total
		if(bcc01.bcc01qt > 0){
			bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("unitario_estoque") * bcc01.bcc01qt + jsonBcc01.getBigDecimal_Zero("tx_dif_aliq_") + jsonBcc01.getBigDecimal_Zero("vl_dif_aliq_");
			bcc01.bcc01custo = round(bcc01.bcc01custo, 2);
		}else{
			bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("unitario_estoque") + jsonBcc01.getBigDecimal_Zero("tx_dif_aliq_") + jsonBcc01.getBigDecimal_Zero("vl_dif_aliq_");
			bcc01.bcc01custo = round(bcc01.bcc01custo, 2);
		}

		// Preço Médio
		bcc01.bcc01pmu = abm0101.abm0101pmu_Zero;

		// Maior Preço
		if(jsonBcc01.getBigDecimal_Zero("unitario_estoque") > jsonAbm0101.getBigDecimal_Zero("maior_preco_unit")){
			jsonBcc01.put("maior_preco_unit",jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
		}else{
			jsonBcc01.put("maior_preco_unit", jsonAbm0101.getBigDecimal_Zero("maior_preco_unit"));
		}

		// Menor Preço Unitário
		if(jsonBcc01.getBigDecimal_Zero("unitario_estoque") == 0 || jsonBcc01.getBigDecimal_Zero("unitario_estoque") < jsonAbm0101.getBigDecimal_Zero("menor_preco_unit")){
			jsonBcc01.put("menor_preco_unit",jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
		}else{
			jsonBcc01.put("menor_preco_unit", jsonAbm0101.getBigDecimal_Zero("menor_preco_unit"));
		}

		//Ultimo Preco
		jsonBcc01.put("ultimo_preco", jsonAbm0101.getBigDecimal_Zero("ultimo_preco"));

	}

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.LCTO_SCE; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==