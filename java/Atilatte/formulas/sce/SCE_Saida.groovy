package Atilatte.formulas.sce

import sam.model.entities.aa.Aam06
import sam.model.entities.ab.Abm01;
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multitec.utils.collections.TableMap;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;
import sam.server.samdev.utils.SCEUtils;
import br.com.multitec.utils.ValidacaoException

public class SCE_Saida extends FormulaBase {


	



	

	@Override
	public void executar() {
		Abm01 abm01;
		SCEUtils sceUtils;

		Bcc01 bcc01;
		Abm0101 abm0101;
		//private Abm01 abm01;
		Aam06 aam06;

		//TableMap
		TableMap jsonBcc01 = new TableMap();
		TableMap jsonAbm0101 = new TableMap();
		
		sceUtils = (SCEUtils) get("sceUtils");

		bcc01 = (Bcc01) get("bcc01");

		//Itens Valores
		abm0101 = (Abm0101) get("abm0101");

		//Item
		abm01 = bcc01.bcc01item != null ? getSession().get(Abm01.class,bcc01.bcc01item.abm01id) : null;
		
		//Unidade de medida
		aam06 = abm01.abm01umu != null ? getSession().get(Aam06.class,abm01.abm01umu.aam06id) : null;

		if(aam06 == null ) throw new ValidacaoException("O item " + abm01.abm01codigo + " encontra-se sem unidade de medida no cadastro.")
		//Campos Livres
		jsonBcc01 = bcc01.bcc01json == null ? new TableMap() : bcc01.bcc01json;
		jsonAbm0101 = abm0101.abm0101json == null ? new TableMap() : abm0101.abm0101json;

		//Custo Unitario
		if (jsonBcc01.getBigDecimal_Zero("unitario_estoque") == 0) {
			jsonBcc01.put("custo_unit", abm0101.abm0101pmu);
		} else {
			jsonBcc01.put("custo_unit", jsonBcc01.getBigDecimal_Zero("unitario_estoque"));
		}

		//Custo Total
		if (bcc01.bcc01qt > 0) {
			bcc01.bcc01custo = abm0101.abm0101pmu_Zero * bcc01.bcc01qt;
		} else {
			bcc01.bcc01custo = jsonBcc01.getBigDecimal_Zero("custo_unit");
		}

		//Preço Médio
		if (abm0101.abm0101pmu <= 0) {
			bcc01.bcc01pmu = jsonBcc01.getBigDecimal_Zero("custo_unit")
		} else {
			bcc01.bcc01pmu = abm0101.abm0101pmu;
		}

		//Maior Preço
		if (jsonBcc01.getBigDecimal_Zero("custo_unit") > jsonAbm0101.getBigDecimal_Zero("maior_preco_unit")) {
			jsonBcc01.put("maior_preco_unit", jsonBcc01.getBigDecimal_Zero("custo_unit"));
		} else {
			jsonBcc01.put("maior_preco_unit", jsonAbm0101.getBigDecimal_Zero("maior_preco_unit"));
		}

		//Menor Preço
		if (jsonBcc01.getBigDecimal_Zero("custo_unit") == 0 || jsonBcc01.getBigDecimal_Zero("custo_unit") < jsonAbm0101.getBigDecimal_Zero("menor_preco_unit")) {
			jsonBcc01.put("menor_preco_unit", jsonBcc01.getBigDecimal_Zero("custo_unit"))
		} else {
			jsonBcc01.put("menor_preco_unit", jsonAbm0101.getBigDecimal_Zero("menor_preco_unit"))
		}

		//Ultimo Preco
		jsonBcc01.put("ultimo_preco", jsonAbm0101.getBigDecimal_Zero("ultimo_preco"));

		//Volume Fat. Frasco
		if(aam06.aam06codigo == 'FR' ){
			if(jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Não foi preenchido o campo 'Quantidade de produto por caixa' no cadastro do item " + abm01.abm01codigo);
			
			jsonBcc01.put("volumes",bcc01.bcc01qt / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
			def Vol = jsonBcc01.getBigDecimal_Zero("volumes");

			BigDecimal volume = new BigDecimal(Vol).setScale(0,BigDecimal.ROUND_UP)
			jsonBcc01.put("volumes",volume)
		}


	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SCE;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==