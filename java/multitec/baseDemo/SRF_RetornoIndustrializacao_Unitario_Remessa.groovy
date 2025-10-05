package multitec.baseDemo

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm01
import sam.model.entities.bc.Bcc01
import sam.model.entities.ea.Eaa01
import sam.server.samdev.formula.FormulaBase

class SRF_RetornoIndustrializacao_Unitario_Remessa extends FormulaBase {
	
	private Long abb01id;
	private Long abm01id;
	private String lote;
	private String serie;
	
	private BigDecimal unitarioFiscal;
	private BigDecimal unitarioAdm;
	
	@Override
	public void executar() {
		abb01id = get("abb01id");
		abm01id = get("abm01id");
		lote = get("lote");
		serie = get("serie");
		
		unitarioFiscal = obterPrecoUnitarioRemessa(abb01id, abm01id, lote, serie);
		if(unitarioFiscal == 0.0) {
			Long abb01idLctoEntrada = obterCentralLctoEntrada(abb01id, abm01id, lote, serie);
			if(abb01idLctoEntrada != null) {
				unitarioFiscal = obterPrecoUnitarioRemessa(abb01idLctoEntrada, abm01id, lote, serie);
			}
		}
		
		unitarioAdm = obterPrecoMedioItem(abm01id);
		
		put("unitarioFiscal", unitarioFiscal);
		put("unitarioAdm", unitarioAdm);
	}
	
	private BigDecimal obterPrecoUnitarioRemessa(Long abb01id, Long abm01id, String lote, String serie) {
		Query query = getSession().createQuery(" SELECT eaa0103unit",
											   " FROM Eaa0103",
											   " INNER JOIN Eaa01 ON eaa0103doc = eaa01id",
											   " LEFT JOIN Eaa01038 ON eaa01038item = eaa0103id",
											   " AND ", (lote != null ? "eaa01038lote = :lote" : "eaa01038lote IS NULL"),
											   " AND ", (serie != null ? "eaa01038serie = :serie" : "eaa01038serie IS NULL"),
											   " WHERE eaa01central = :abb01id",
											   " AND eaa0103item = :abm01id",
											   getSamWhere().getWhereGc("AND", Eaa01.class));
		query.setParameter("abb01id", abb01id);
		query.setParameter("abm01id", abm01id);
		if(lote != null) query.setParameter("lote", lote);
		if(serie != null) query.setParameter("serie", serie);
		query.setMaxResult(1);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private BigDecimal obterPrecoMedioItem(Long abm01id) {
		Query query = getSession().createQuery(" SELECT abm0101pmu",
											   " FROM Abm0101",
											   " INNER JOIN Abm01 ON abm0101item = abm01id ",
											   " WHERE abm0101item = :abm01id",
											   " AND abm0101empresa = :aac10id",
												getSamWhere().getWhereGc("AND", Abm01.class));
		query.setParameter("abm01id", abm01id);
		query.setParameter("aac10id", obterEmpresaAtiva().aac10id);
		query.setMaxResult(1);
		BigDecimal result = query.getUniqueResult(ColumnType.BIG_DECIMAL);
		return result != null ? result : BigDecimal.ZERO;
	}
	
	private Long obterCentralLctoEntrada(Long abb01id, Long abm01id, String lote, String serie) {
		Query query = getSession().createQuery(" SELECT bcc01central ",
											   " FROM Bcc01 ",
											   " WHERE bcc01mov = 0 ",
											   " AND bcc01centralEst = :abb01id ",
											   " AND bcc01item = :abm01id ",
											   " AND ", (lote != null ? "bcc01lote = :lote" : "bcc01lote IS NULL"),
											   " AND ", (serie != null ? "bcc01serie = :serie" : "bcc01serie IS NULL"),
											   getSamWhere().getWhereGc("AND", Bcc01.class),
											   " ORDER BY bcc01data, bcc01id");
		query.setParameter("abb01id", abb01id);
		query.setParameter("abm01id", abm01id);
		if(lote != null) query.setParameter("lote", lote);
		if(serie != null) query.setParameter("serie", serie);
		query.setMaxResult(1);
		return query.getUniqueResult(ColumnType.LONG);
	}
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SRF_RETORNO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTEifQ==