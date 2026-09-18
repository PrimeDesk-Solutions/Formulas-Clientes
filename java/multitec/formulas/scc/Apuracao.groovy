package multitec.formulas.scc

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.criteria.client.ClientCriterion
import sam.core.criteria.ClientCriteriaConvert
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abe01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase

class Apuracao extends FormulaBase {

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SCC_APURACAO;
	}

	@Override
	public void executar() {
		ClientCriterion whereTipo = get("whereTipo");
		ClientCriterion whereData = get("whereData");
		ClientCriterion whereRep = get("whereRep");
		ClientCriterion whereDtCred = get("whereDtCred");
		
		def critTipo = ClientCriteriaConvert.convertCriterion(whereTipo);
		def critData = ClientCriteriaConvert.convertCriterion(whereData);
		def critRep = ClientCriteriaConvert.convertCriterion(whereRep);
		def critdtCred = ClientCriteriaConvert.convertCriterion(whereDtCred);
		
		List<Long> abe01ids = getSession().createCriteria(Abe01.class)
				.addFields("abe01id")
				.addWhere(Criterions.eq("abe01rep", Abe01.SIM))
				.addWhere(critRep)
				.addWhere(getSamWhere().getCritPadrao(Abe01.class))
				.getList(ColumnType.LONG);

		List<Eaa01> eaa01s = getSession().createCriteria(Eaa01.class)
				.addFields("eaa01id, eaa01totDoc, eaa01rep0, rep0.abe01id AS idRep0, eaa01rep1, rep1.abe01id AS idRep1, eaa01rep2, rep2.abe01id AS idRep2, eaa01rep3, rep3.abe01id AS idRep3, eaa01rep4, rep4.abe01id AS idRep4")
				.addJoin(Joins.join("eaa01central").left(false).alias("abb01"))
				.addJoin(Joins.join("abb01.abb01tipo").left(false).alias("aah01"))
				.addJoin(Joins.join("eaa01rep0").left(true).alias("rep0"))
				.addJoin(Joins.join("eaa01rep1").left(true).alias("rep1"))
				.addJoin(Joins.join("eaa01rep2").left(true).alias("rep2"))
				.addJoin(Joins.join("eaa01rep3").left(true).alias("rep3"))
				.addJoin(Joins.join("eaa01rep4").left(true).alias("rep4"))
				.addWhere(critTipo).addWhere(critData)
				.addWhere(Criterions.or(Criterions.in("rep0.abe01id", abe01ids), Criterions.in("rep1.abe01id", abe01ids), Criterions.in("rep2.abe01id", abe01ids), Criterions.in("rep3.abe01id", abe01ids), Criterions.in("rep4.abe01id", abe01ids)))
				.addWhere(getSamWhere().getCritPadrao(Eaa01.class))
				.getList(ColumnType.ENTITY);
		
		Map<Long, BigDecimal> mapRep = new HashMap<>();
		for (eaa01 in eaa01s) {
			if (eaa01.eaa01rep0 != null && abe01ids.contains(eaa01.eaa01rep0.abe01id)) {
				if (mapRep.containsKey(eaa01.eaa01rep0.abe01id)) {
					def total = mapRep.get(eaa01.eaa01rep0.abe01id);
					total = total + eaa01.eaa01totDoc;
					mapRep.put(eaa01.eaa01rep0.abe01id, total)
				} else {
					mapRep.put(eaa01.eaa01rep0.abe01id, eaa01.eaa01totDoc)
				}
			}
			
			if (eaa01.eaa01rep1 != null && abe01ids.contains(eaa01.eaa01rep1.abe01id)) {
				if (mapRep.containsKey(eaa01.eaa01rep1.abe01id)) {
					def total = mapRep.get(eaa01.eaa01rep1.abe01id);
					total = total + eaa01.eaa01totDoc;
					mapRep.put(eaa01.eaa01rep1.abe01id, total)
				} else {
					mapRep.put(eaa01.eaa01rep1.abe01id, eaa01.eaa01totDoc)
				}
			}
			
			if (eaa01.eaa01rep2 != null && abe01ids.contains(eaa01.eaa01rep2.abe01id)) {
				if (mapRep.containsKey(eaa01.eaa01rep2.abe01id)) {
					def total = mapRep.get(eaa01.eaa01rep2.abe01id);
					total = total + eaa01.eaa01totDoc;
					mapRep.put(eaa01.eaa01rep2.abe01id, total)
				} else {
					mapRep.put(eaa01.eaa01rep2.abe01id, eaa01.eaa01totDoc)
				}
			}
			
			if (eaa01.eaa01rep3 != null && abe01ids.contains(eaa01.eaa01rep3.abe01id)) {
				if (mapRep.containsKey(eaa01.eaa01rep3.abe01id)) {
					def total = mapRep.get(eaa01.eaa01rep3.abe01id);
					total = total + eaa01.eaa01totDoc;
					mapRep.put(eaa01.eaa01rep3.abe01id, total)
				} else {
					mapRep.put(eaa01.eaa01rep3.abe01id, eaa01.eaa01totDoc)
				}
			}
			
			if (eaa01.eaa01rep4 != null && abe01ids.contains(eaa01.eaa01rep4.abe01id)) {
				if (mapRep.containsKey(eaa01.eaa01rep4.abe01id)) {
					def total = mapRep.get(eaa01.eaa01rep4.abe01id);
					total = total + eaa01.eaa01totDoc;
					mapRep.put(eaa01.eaa01rep4.abe01id, total)
				} else {
					mapRep.put(eaa01.eaa01rep4.abe01id, eaa01.eaa01totDoc)
				}
			}
		}
		
		put("mapRep", mapRep);
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMzYifQ==