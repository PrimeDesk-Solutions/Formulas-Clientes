package multitec.baseDemo

import sam.dicdados.FormulaTipo
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase
import br.com.multiorm.ColumnType
import sam.model.entities.cb.Cbb01
import br.com.multiorm.criteria.criterion.Criterions

class SCV_Pre_Gravacao_OC extends FormulaBase {
	
	private Eaa01 eaa01;
	private Integer gravar = 1; //0-Não 1-Sim
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCV_SRF_PRE_GRAVACAO;
	}
	
	@Override
	public void executar() {
		eaa01 = get("eaa01");
		
		if(eaa01.eaa01id != null) { //Verificando somente quando alteração
			if(eaa01.eaa01clasDoc == 0 && eaa01.eaa01esMov == 0) { //Doc SCV de Entrada
				List<Long> abb01idsOrigem = buscarIdsDesdobramentosOrigemDaCentral(eaa01.eaa01central.abb01id);
				if(abb01idsOrigem != null && abb01idsOrigem.size() > 0) {
					Integer countOC = buscarCountOC(abb01idsOrigem);
					if(countOC != null && countOC > 0) {
						interromper("Documento SCV originado por Ordem de Compra não pode ser alterado.");
					}
				}
			}
		}
		
		put("gravar", gravar);
	}
	
	private List<Long> buscarIdsDesdobramentosOrigemDaCentral(Long abb01id) {
		return getSession().createQuery(" SELECT abb0102central",
										" FROM Abb0102",
										" WHERE abb0102doc = :abb01id")
						   .setParameter("abb01id", abb01id)
						   .getList(ColumnType.LONG);
	}

	private Integer buscarCountOC(List<Long> abb01idsOrigem) {
		return getSession().createCriteria(Cbb01.class)
						   .addFields("COUNT(*)")
						   .addWhere(Criterions.in("cbb01central", abb01idsOrigem))
						   .addWhere(getSamWhere().getCritPadrao(Cbb01.class))
						   .get(ColumnType.INTEGER);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTcifQ==