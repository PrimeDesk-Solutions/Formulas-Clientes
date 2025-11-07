package Atilatte.formulas.spp;

import sam.dicdados.FormulaTipo
import sam.model.entities.ba.Bab01
import sam.server.samdev.formula.FormulaBase
import br.com.multitec.utils.ValidacaoException
import br.com.multitec.utils.collections.TableMap;
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.ColumnType
import br.com.multiorm.Query




class SPP_ordemProducao extends FormulaBase {
	
	private Bab01 bab01;

	TableMap jsonBab01;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SPP_ORDEM_DE_PRODUCAO;
	}
	
	@Override
	public void executar() {
		bab01 = get("bab01");
		
		//Campos Livres
		jsonBab01 = bab01.bab01json != null ? bab01.bab01json : new TableMap();

		//Busca o ultimo lote registrado 
		TableMap tmLote = getSession().createQuery("SELECT MAX(bab01lote::numeric)+1 as proximoLote from bab01").setMaxResult(1).getUniqueTableMap();

		Long proximoLote = tmLote.getLong("proximoLote");

		bab01.bab01lote = proximoLote;
		
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiODMifQ==