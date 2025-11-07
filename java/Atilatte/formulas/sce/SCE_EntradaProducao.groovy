package Atilatte.formulas.sce;

import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException

public class SCE_EntradaProducao extends FormulaBase{


    SCEUtils sceUtils;
	
	private Bcc01 bcc01;
	private Abm0101 abm0101;
	
	@Override
	public void executar() {
		sceUtils = (SCEUtils) get("sceUtils");
		
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101)get("abm0101");
		TableMap mapJson = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;

		TableMap abm0101mapJson = abm0101 != null && abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		
		//Carga dos dados do lançamento
		def custo_aquisicao = mapJson.getBigDecimal_Zero("unitario_estoque");
		def custo_fixo = bcc01.bcc01custoFixo_Zero;
		def custo_var = bcc01.bcc01custoVar_Zero;
		def qtde = bcc01.bcc01qt_Zero;

		//Cálculo do Custo Base
		def custo_base = custo_aquisicao + custo_fixo + custo_var + (mapJson.getBigDecimal_Zero("frete_item")) + (mapJson.getBigDecimal_Zero("ipi")) - (mapJson.getBigDecimal_Zero("icms")) - (mapJson.getBigDecimal_Zero("pis")) - (mapJson.getBigDecimal_Zero("cofins"));
		if (bcc01.bcc01mov == 1) {
			custo_base = custo_base * -1
			qtde = qtde * -1
		}
		
		//Cálculo do Preço Médio
		def saldoAtual = sceUtils.saldoEstoque(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id);
		def pmAtual =  sceUtils.buscarPMRetroativo(bcc01.bcc01item.abm01id, bcc01.bcc01data, bcc01.bcc01id, abm0101.abm0101pmu_Zero);
		
		def pmu = pmAtual;

		def ultimo = custo_aquisicao;
		
		if((saldoAtual + qtde) > 0) {
			pmu = ((pmAtual * saldoAtual) + custo_base) / (saldoAtual + qtde);
			pmu = round(pmu, 6);
		}

		
		//Atualizar dados do lançamento
		bcc01.bcc01custo = custo_base.abs();
		bcc01.bcc01pmu = pmu;
		
		//Atualizar o novo Preço Médio no cadastro do item
		abm0101.abm0101pmu = pmu;
		abm0101mapJson.put("ultimo_preco", ultimo);
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==