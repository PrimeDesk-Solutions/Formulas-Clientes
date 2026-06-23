package Atilatte.formulas.sce;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException

public class SCE_CompraInsumo extends FormulaBase{


	SCEUtils sceUtils;

	private Bcc01 bcc01;
	private Abm0101 abm0101;

	@Override
	public void executar() {
		sceUtils = (SCEUtils) get("sceUtils");

		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101)get("abm0101");
		TableMap mapJson = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;
		TableMap abm0101mapJson = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;

		//Carga dos dados do lançamento

		def icms = mapJson.getBigDecimal_Zero("icms");
		def ipi = mapJson.getBigDecimal_Zero("ipi");
		def pis = mapJson.getBigDecimal_Zero("pis");
		def cofins = mapJson.getBigDecimal_Zero("cofins");
		def custo_aquisicao = mapJson.getBigDecimal_Zero("unitario_estoque") - icms - ipi - pis - cofins;
		def custo_fixo = bcc01.bcc01custoFixo_Zero;
		def custo_var = bcc01.bcc01custoVar_Zero;
		def qtde = bcc01.bcc01qt_Zero;


    //Cálculo do Custo Base
		def custo_base = qtde * (custo_aquisicao + custo_fixo + custo_var + (mapJson.getBigDecimal_Zero("frete_item")));
		if (bcc01.bcc01mov == 1) {
			custo_base = custo_base * -1
			qtde = qtde * -1
		}

          //Cálculo do Preço Médio
		def saldoAtual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id);
		def pmAtual =  sceUtils.buscarPMRetroativo(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id, abm0101.abm0101pmu_Zero);
		
		def pmu = pmAtual ;

		def ultimo = custo_aquisicao;
		
		if((saldoAtual + qtde) > 0) {
			pmu = ((pmAtual * saldoAtual) + custo_base) / (saldoAtual + qtde);
			pmu = round(pmu, 6);
		}
		if(pmu == 0){
			pmu = mapJson.getBigDecimal_Zero("unitario_estoque")
			custo_base = qtde * pmu
		}
		
		//throw new ValidacaoException(pmu.toString())
		//Atualizar dados do lançamento
		bcc01.bcc01custo = custo_base.abs();
		bcc01.bcc01pmu = pmu;

		abm0101mapJson.put("precolivre",((bcc01.bcc01qt * mapJson.getBigDecimal_Zero("unitario_estoque")) - mapJson.getBigDecimal_Zero("icms") -  mapJson.getBigDecimal_Zero("pis") -  mapJson.getBigDecimal_Zero("cofins")) / bcc01.bcc01qt )
		//throw new ValidacaoException(bcc01.bcc01pmu.toString())
		//bcc01.bcc01precolivre = custo_base - (mapJson.getBigDecimal_Zero("ipi")) - (mapJson.getBigDecimal_Zero("icms")) - (mapJson.getBigDecimal_Zero("pis")) - (mapJson.getBigDecimal_Zero("cofins")) / qtde
	
		// Custo
		//def custo = qtde * pmu;
		
		// Define os custos na tabela de estoque
		//bcc01.bcc01custo = custo;
		//bcc01.bcc01pmu = pmu;

		//Atualizar o novo Preço Médio no cadastro do item
		//def livre = custo_base - (mapJson.getBigDecimal_Zero("ipi")) - (mapJson.getBigDecimal_Zero("icms")) - (mapJson.getBigDecimal_Zero("pis")) - (mapJson.getBigDecimal_Zero("cofins")) / qtde
		abm0101.abm0101pmu = pmu;
		//abm0101mapJson.put("precolivre", pmu);
		//abm0101mapJson.put("ultimo_preco", ultimo);
		//abm0101mapJson.put("ultimo_preco", ultimo);
		//abm0101mapJson.put("ultimo_preco", ultimo);

		/*
		--------- Formula Multitec ----------
		//Cálculo do Custo Base
		def custo_base = custo_aquisicao + custo_fixo + custo_var + (mapJson.getBigDecimal_Zero("frete_item")) + (mapJson.getBigDecimal_Zero("ipi"));
		if (bcc01.bcc01mov == 1) {
			custo_base = custo_base * -1
			qtde = qtde * -1
		}

		//Cálculo do Preço Médio
		def saldoAtual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id);
		def pmAtual =  sceUtils.buscarPMRetroativo(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id, abm0101.abm0101pmu_Zero);

		def pmu = pmAtual;

		if((saldoAtual + qtde) > 0) {
			pmu = ((pmAtual * saldoAtual) + custo_base) / (saldoAtual + qtde);
			pmu = round(pmu, 6);
		}
		//throw new ValidacaoException(pmu.toString())
		//Atualizar dados do lançamento
		bcc01.bcc01custo = custo_base.abs();
		bcc01.bcc01pmu = pmu;

		//Atualizar o novo Preço Médio no cadastro do item
		abm0101.abm0101pmu = pmu;
		abm0101mapJson.put("ultimo_preco", ultimo);

		 */
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==