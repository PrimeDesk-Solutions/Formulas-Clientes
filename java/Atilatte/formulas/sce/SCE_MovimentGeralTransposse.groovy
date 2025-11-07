package Atilatte.formulas.sce;
import sam.model.entities.aa.Aam06;
import sam.model.entities.ab.Abm01;
import sam.model.entities.ab.Abm0101;
import sam.model.entities.bc.Bcc01;

import br.com.multitec.utils.collections.TableMap;
import br.com.multitec.utils.ValidacaoException
import sam.server.samdev.formula.FormulaBase
import sam.dicdados.FormulaTipo
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multiorm.ColumnType

public class SceMovimentGeralTransposse extends FormulaBase{
	private Aam06 aam06;
	private Abm01 abm01;
	private Abm0101 abm0101;
	
	private Bcc01 bcc01;
	
	private TableMap jsonBcc01;
	private TableMap jsonAbm0101;
	
	@Override 
	public void executar() {
		bcc01 = (Bcc01)get("bcc01");
		abm0101 = (Abm0101) get("abm0101");
		
		//Item
		abm01 = getSession().createCriteria(Abm01.class)
				.addJoin(Joins.join("abm0101", "abm0101item = abm01id"))
				.addWhere(Criterions.eq("abm0101id", abm0101.abm0101id))
				.get(ColumnType.ENTITY)
	

		//Unidade de Medida
		aam06 = getSession().get(Aam06.class,  abm01.abm01umu.aam06id);
		

		//Campos Livres
		jsonBcc01 = bcc01.bcc01json != null ? bcc01.bcc01json : new TableMap();
		jsonAbm0101 = abm0101.abm0101json != null ? abm0101.abm0101json : new TableMap();
		
		
		//Define os preços no lançamento de acordo com o cadastro do item
		bcc01.bcc01pmu = abm0101.abm0101pmu;
		jsonBcc01.put("maior_preco_unit", jsonAbm0101.getBigDecimal_Zero("maior_preco_unit"));
		jsonBcc01.put("menor_preco_unit", jsonAbm0101.getBigDecimal_Zero("menor_preco_unit"));
		jsonBcc01.put("ultimo_preco", jsonAbm0101.getBigDecimal_Zero("ultimo_preco"));

		//Preço Livre
		def unitario = jsonBcc01.getBigDecimal_Zero("unitario_estoque");

		if(unitario > 0){
			jsonBcc01.put("preco_livre", unitario);
		}

		//Preço Livre 
		jsonBcc01.put("preco_livre", jsonAbm0101.getBigDecimal_Zero("preco_livre"));

		//Custo Total
		bcc01.bcc01custo = bcc01.bcc01qt * jsonAbm0101.getBigDecimal_Zero("ultimo_preco");

		//Total Item 
		jsonBcc01.put("tot_itens", bcc01.bcc01qt * jsonAbm0101.getBigDecimal_Zero("preco_livre"));

		//jsonBcc01.put("unitario_estoque",jsonAbm0101.getBigDecimal_Zero("ultimo_preco"));

		//Volume Fat. Frasco 
		if(aam06.aam06codigo == 'FR'){
			if(jsonAbm0101.getBigDecimal_Zero("cvdnf") == 0) throw new ValidacaoException("Capacidade Volumétrica no Cadastro do Item Inválida.")
			jsonBcc01.put("volumes", bcc01.bcc01qt / jsonAbm0101.getBigDecimal_Zero("cvdnf"));
			 def Vol = jsonBcc01.getBigDecimal_Zero("volumes")
			 BigDecimal volume = new BigDecimal(Vol).setScale(0,BigDecimal.ROUND_UP);
			 jsonBcc01.put("volumes", volume);
		}
		
	}

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.LCTO_SCE; 
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTAifQ==