package Atilatte.formulas.sce;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException

public class SCE_ComplementoPreco extends FormulaBase{


    SCEUtils sceUtils;
	
	private Bcc01 bcc01;
	private Abm0101 abm0101;
	
	@Override
	public void executar() {
		sceUtils = (SCEUtils) get("sceUtils");
		
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101)get("abm0101");

		// Campos Livres
		TableMap jsonBcc01 = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;
		TableMap jsonAbm0101 = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;

		// Quantidade
		bcc01.bcc01qt = new BigDecimal(0);

		// Custo Total
		bcc01.bcc01custo = round((jsonBcc01.getBigDecimal_Zero("unitario_estoque") - jsonBcc01.getBigDecimal_Zero("icms") - jsonBcc01.getBigDecimal_Zero("pis") - jsonBcc01.getBigDecimal_Zero("cofins")),2);

		// Custo Unitário
		jsonBcc01.put("unitario_estoque", bcc01.bcc01custo);

		// Preço Médio
		def saldoAtual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id);

		if(saldoAtual > 0){
			bcc01.bcc01pmu = ((abm0101.abm0101pmu_Zero * saldoAtual) + bcc01.bcc01custo) / saldoAtual ;
		}else{
			bcc01.bcc01pmu = abm0101.abm0101pmu_Zero
		}

		// Maior Preço
		jsonBcc01.put("maior_preco_unit", jsonAbm0101.getBigDecimal_Zero("maior_preco_unit"));

		// Menor Preço
		jsonBcc01.put("menor_preco_unit", jsonAbm0101.getBigDecimal_Zero("menor_preco_unit"));

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