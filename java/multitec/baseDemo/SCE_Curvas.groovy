package multitec.baseDemo;

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterion
import br.com.multiorm.criteria.criterion.Criterions
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm0101
import sam.server.samdev.formula.FormulaBase

public class SCE_Curvas extends FormulaBase{

	@Override 
	public FormulaTipo obterTipoFormula() { 
		return FormulaTipo.SCE_CURVA_ABC_XYZ; 
	}

	@Override 
	public void executar() {
		List<Long> abm01ids = get("abm01ids");
		def percAX = get("percAX");
		def percBY = get("percBY");
		def percCZ = get("percCZ");
		Criterion critAbm20 = get("filtroAbm20");
		Criterion critPeriodo = get("filtroPeriodo");
		boolean isAbc = get("isAbc");
		
		for(Long abm01id : abm01ids){
			Abm0101 abm0101 = buscarConfiguracoesItem(abm01id);
			if(abm0101 != null) {
				if (isAbc) {
					abm0101.abm0101abc = 'A';
				} else {
					abm0101.abm0101xyz = 'X';
				}
				session.persist(abm0101);
			}
		}
	}

	public Abm0101 buscarConfiguracoesItem(Long abm01id) {
		return getSession().createCriteria(Abm0101.class)
					    .addWhere(Criterions.eq("abm0101item", abm01id))
					    .addWhere(Criterions.isNotNull("abm0101comercial"))
					    .addWhere(Criterions.eq("abm0101empresa", obterEmpresaAtiva().aac10id))
				         .get(ColumnType.ENTITY);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMjQifQ==