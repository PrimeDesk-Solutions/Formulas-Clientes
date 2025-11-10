package multitec.formulas.cas.eSocial

import java.time.LocalDate

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.StringUtils
import sam.dicdados.FormulaTipo
import sam.model.entities.aa.Aaa15
import sam.model.entities.aa.Aap50
import sam.model.entities.fb.Fba0101
import sam.model.entities.fb.Fba31
import sam.server.samdev.formula.FormulaBase

class S_1200_lcto extends FormulaBase {

	@Override
	public void executar() {
		Aaa15 aaa15 = get("aaa15");
		def mesAnoRef = get("perApur")
		def fba0101s = get("fba0101s");
		def abh80id = get("abh80id")
		String cnpj = StringUtils.extractNumbers(obterEmpresaAtiva().getAac10ni());
		cnpj = StringUtils.ajustString(cnpj, 8);
		
		Boolean temFba31 = false
		for(fba0101 in fba0101s) {
			List<Fba31> fba31 = buscarFba31011s(fba0101,mesAnoRef)
			if(fba31 != null && fba31.size() > 0){
				temFba31 = true
			}
		}
		
		Boolean temEvento = verificaSeTemEventoS2299ES2399AprovadoParaOTrabalhador(abh80id,cnpj,DateUtils.getStartAndEndMonth(mesAnoRef))
		
		Boolean autorizado = true
		if(temEvento && !temFba31) autorizado = false  

		put("aaa15Autorizado", !temEvento);
	}

	private List<Fba31> buscarFba31011s(Fba0101 fba0101, LocalDate mesAnoRef) {
		return getSession().createCriteria(Fba31.class)
				.alias("fba31")
				.addJoin(Joins.fetch("fba31.fba31instrum").left(false).alias("fba30"))
				.addJoin(Joins.fetch("fba3101s").left(false).alias("fba3101s"))
				.addJoin(Joins.fetch("fba3101s.fba3101lotacao").left(false).alias("abh02"))
				.addJoin(Joins.fetch("fba3101s.fba31011s").left(false).alias("fba31011s"))
				.addJoin(Joins.fetch("fba31011s.fba31011eve").left(false).alias("fba01011"))
				.addJoin(Joins.fetch("fba01011.fba01011eve").left(false).alias("abh21"))
				.addJoin(Joins.fetch("fba01011.fba01011vlr").left(false).alias("fba0101"))
				.addWhere(Criterions.eq("fba01011.fba01011vlr", fba0101.fba0101id))
				.addWhere(Criterions.eq("fba31mes", mesAnoRef.getMonthValue()))
				.addWhere(Criterions.eq("fba31ano", mesAnoRef.getYear()))
				.addWhere(Criterions.eq("fba0101.fba0101trab", fba0101.fba0101trab.abh80id))
				.getList(ColumnType.ENTITY);
	}

	private boolean verificaSeTemEventoS2299ES2399AprovadoParaOTrabalhador(Long abh80id, String cnpj, LocalDate[] periodo) {
		List<Long> aap50ids = session.createCriteria(Aap50.class)
				.addFields("aap50id")
				.addWhere(Criterions.in("aap50evento", ["S-2299", "S-2399"]))
				.getList(ColumnType.LONG);

		long count = session.createCriteria(Aaa15.class)
				.addFields("COUNT(aaa15id)")
				.addWhere(Criterions.in("aaa15evento", aap50ids))
				.addWhere(Criterions.between("aaa15data", periodo[0], periodo[1]))
				.addWhere(Criterions.eq("aaa15cnpj", cnpj))
				.addWhere(Criterions.eq("aaa15tabela", "Abh80").ignoreCase(true))
				.addWhere(Criterions.eq("aaa15registro", abh80id))
				.addWhere(Criterions.eq("aaa15status", Aaa15.STATUS_APROVADO))
				.get(ColumnType.LONG);

		return count > 0;
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.ESOCIAL;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiMTAifQ==