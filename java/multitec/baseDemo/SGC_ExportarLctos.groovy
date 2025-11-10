package multitec.baseDemo

import br.com.multiorm.ColumnType
import br.com.multiorm.criteria.criterion.Criterions
import br.com.multiorm.criteria.join.Joins
import br.com.multitec.utils.DateUtils
import br.com.multitec.utils.TextFileEscrita
import sam.dicdados.FormulaTipo
import sam.model.entities.eb.Ebb05
import sam.model.entities.eb.Ebb0501
import sam.server.samdev.formula.FormulaBase

class SGC_ExportarLctos extends FormulaBase {
	
	private List<Long> ebb05ids;
	
	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.LCTO_SGC;
	}
	
	public void executar() {
		this.ebb05ids = get("ebb05ids");
		
		TextFileEscrita txt = new TextFileEscrita("|");
		
		for(Long ebb05id : this.ebb05ids) {
			Ebb05 ebb05 = buscarLctoContabilPorId(ebb05id);
			
			txt.print("0");
			txt.print(DateUtils.formatDate(ebb05.getEbb05data(), "ddMMyyyy"));   																												//002-009 - Data do lançamento
			txt.print(null);																																									//010-014 - Código do lançamento contábil padrão
			txt.print(ebb05.getEbb05deb().getAbc10codigo());				    																												//015-025 - Conta débito ou reduzido
			txt.print(ebb05.getEbb05concDeb());																																					//026-026 - Conta débito conciliada
			txt.print(ebb05.getEbb05cred().getAbc10codigo());  																																	//027-037 - Conta crédito ou reduzido
			txt.print(ebb05.getEbb05concCred());																																				//038-038 - Conta crédito conciliada
			txt.print(ebb05.getEbb05intDepto());																																				//039-039 - Integração com departamento
			txt.print(ebb05.getEbb05valor().multiply(new BigDecimal(100)).longValue());																											//040-053 - Valor do lançamento
			txt.print(ebb05.getEbb05aceite());																																					//054-054 - Aceite
			txt.print(ebb05.getEbb05historico().length() > 2000 ? ebb05.getEbb05historico().substring(0, 2000) : ebb05.getEbb05historico());													//054-2053 - Histórico
			txt.print(ebb05.getEbb05central() == null ? null : ebb05.getEbb05central().getAbb01tipo() == null ? null : ebb05.getEbb05central().getAbb01tipo().getAah01codigo());				//2054-2060 - Central: Código do tipo de documento
			txt.print(ebb05.getEbb05central() == null ? null : ebb05.getEbb05central().getAbb01ent() == null ? null : ebb05.getEbb05central().getAbb01ent().getAbe01codigo());					//2060-2069 - Central: Código da entidade
			txt.print(ebb05.getEbb05central() != null ? ebb05.getEbb05central().getAbb01num() : null);																							//2070-2078 - Central: Número do documento
			txt.print(ebb05.getEbb05central() != null && ebb05.getEbb05central().getAbb01data() != null ? DateUtils.formatDate(ebb05.getEbb05central().getAbb01data(), "ddMMyyyy") : null);		//2079-2088 - Central: Data
			txt.print(ebb05.getEbb05central() != null ? ebb05.getEbb05central().getAbb01serie() : null);																						//2089-2092 - Central: Série
			txt.print(ebb05.getEbb05central() != null ? ebb05.getEbb05central().getAbb01parcela() : null);																						//2093-2097 - Central: Parcela
			txt.print(ebb05.getEbb05central() != null ? ebb05.getEbb05central().getAbb01quita() : null);																						//2098-2100 - Central: Quita
			txt.print(ebb05.getEbb05json());
			
			txt.newLine();
			
			if(ebb05.getEbb0501s() != null && ebb05.getEbb0501s().size() >  0){
				for(Ebb0501 ebb0501 : ebb05.getEbb0501s()){
					txt.print("1");																																								//001-001 - Tipo de registro (1 = Departamentos)
					txt.print(ebb0501.getEbb0501depto().getAbb11codigo());                          																							//002-008 - Código do centro de custo
					txt.print(ebb0501.getEbb0501valor().multiply(new BigDecimal(100)).longValue()); 																							//009-022 - Valor
					txt.newLine();
				}
			}
		}
		
		put("dados", txt.getTexto().getBytes());
	}
	
	private Ebb05 buscarLctoContabilPorId(Long ebb05id) {
		return getSession().createCriteria(Ebb05.class).alias("ebb05")
						   .addJoin(Joins.fetch("ebb05.ebb05deb").alias("abc10deb"))
						   .addJoin(Joins.fetch("ebb05.ebb05cred").alias("abc10cred"))
						   .addJoin(Joins.fetch("ebb05.ebb05central").alias("abb01").left(true))
						   .addJoin(Joins.fetch("abb01.abb01tipo").alias("aah01").left(true))
						   .addJoin(Joins.fetch("abb01.abb01ent").alias("abe01").left(true))
						   .addJoin(Joins.fetch("ebb0501s").alias("ebb0501").left(true))
						   .addJoin(Joins.fetch("ebb0501.ebb0501depto").alias("abb11").left(true))
						   .addWhere(Criterions.eq("ebb05id", ebb05id))
						   .get(ColumnType.ENTITY);
	}
	
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTUifQ==