package multitec.baseDemo

import br.com.multiorm.ColumnType
import br.com.multiorm.Query
import br.com.multitec.utils.collections.TableMap
import sam.dicdados.FormulaTipo
import sam.model.entities.ab.Abm01
import sam.server.samdev.formula.FormulaBase

class SLM_QtdeMaxMinSeparacao extends FormulaBase {
	
	private BigDecimal qtdeBase;
	private Long abm01id;
	private Long abe01id;
	
	private BigDecimal qtdeMax;
	private BigDecimal qtdeMin;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.SLM_QTDE_MAX_MIN_SEPARACAO;
	}
	
	@Override
	public void executar() {
		qtdeBase = get("qtdeBase");
		abm01id = get("abm01id");
		abe01id = get("abe01id");
		
		TableMap json = obterJsonItem(abm01id);
		def percentualPermitido = json != null ? json.getBigDecimal_Zero("unidperc") : 0.0;
		
		qtdeMin = qtdeBase;
		qtdeMax = qtdeBase;
		if(percentualPermitido == 0) {
			qtdeMin = 0.000001;
			qtdeMax = 999999999999.999999;
		}else if(percentualPermitido > 0 && percentualPermitido != 100) {
			BigDecimal percentualUtilizado = percentualPermitido;
			if(percentualPermitido > 100) {
				percentualUtilizado = percentualUtilizado - 100;
			}
	
			def qtVariavel = qtdeBase * percentualUtilizado;
			qtVariavel = qtVariavel / 100;
			qtVariavel = round(qtVariavel, 6);
			
			qtdeMin = qtdeMin - qtVariavel;
	
			if(percentualPermitido < 100) {
				qtdeMax = qtdeMax + qtVariavel;
			}
		}
		
		put("qtdeMax", qtdeMax);
		put("qtdeMin", qtdeMin);
	}
	
	private TableMap obterJsonItem(Long abm01id) {
		Query query = getSession().createQuery(" SELECT abm0101json",
											   " FROM Abm0101",
											   " INNER JOIN Abm01 ON abm0101item = abm01id ",
											   " WHERE abm0101item = :abm01id",
											   " AND abm0101empresa = :aac10id",
												getSamWhere().getWhereGc("AND", Abm01.class));
		query.setParameter("abm01id", abm01id);
		query.setParameter("aac10id", obterEmpresaAtiva().aac10id);
		query.setMaxResult(1);
		return query.getUniqueResult(ColumnType.JSON);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiOTYifQ==