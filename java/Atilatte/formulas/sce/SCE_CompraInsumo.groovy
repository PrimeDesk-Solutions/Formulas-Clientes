package Atilatte.formulas.sce;

import sam.model.entities.ab.Abm01;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Abm1301;
import sam.server.samdev.utils.Parametro
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException
import sam.model.entities.ab.Abm13;
import sam.model.entities.ab.Abm1301
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.ColumnType;


public class SCE_CompraInsumo extends FormulaBase{


	SCEUtils sceUtils;

	private Bcc01 bcc01;
	private Abm0101 abm0101;
	private Abm13 abm13;
	private Abm1301 abm1301;

	@Override
	public void executar() {
		sceUtils = (SCEUtils) get("sceUtils");

		bcc01 = (Bcc01)get("bcc01");
		
		abm0101 = (Abm0101)get("abm0101");
		
		// Item
		Abm01 abm01 = bcc01.bcc01item;
							
		// Campos Livres
		TableMap jsonBcc01 = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;
		
		TableMap jsonAbe0101 = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;
		
		def qtde = bcc01.bcc01qt;
		def unitEstoque = jsonBcc01.getBigDecimal_Zero("unitario_estoque");
		def saldoAtual = sceUtils.saldoEstoque(abm01.abm01id, bcc01.bcc01data, bcc01.bcc01id);
		def valorSaldo = saldoAtual * abm0101.abm0101pmu_Zero;
		def valorEstoque = unitEstoque * qtde;
		
		def pmu = (valorSaldo + valorEstoque) / (saldoAtual + qtde );

		def custo = qtde * pmu;

		 
		// Define os custos na tabela de estoque
		bcc01.bcc01custo = custo;
		bcc01.bcc01pmu = pmu;

		//Atualizar o novo Preço Médio no cadastro do item
		abm0101.abm0101pmu = pmu;
		jsonAbe0101.put("ultimo_preco", unitEstoque);
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==