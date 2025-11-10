package multitec.baseDemo;

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import br.com.multiorm.ColumnType;
import br.com.multiorm.criteria.criterion.Criterions;
import br.com.multiorm.criteria.fields.Fields;
import br.com.multitec.utils.collections.TableMap;
import sam.dicdados.FormulaTipo;
import sam.model.entities.ab.Abm40
import sam.model.entities.bc.Bcb11;
import sam.model.entities.bc.Bcc01
import sam.model.entities.eb.Ebb02;
import sam.server.samdev.formula.FormulaBase;
import sam.server.samdev.utils.Parametro

public class SCE_InventarioFiscal extends FormulaBase{

	private Bcb11 bcb11;

	@Override
	public void executar() {
		bcb11 = (Bcb11)get("bcb11");

		LocalDate bcb10data = bcb11.bcb11inv.bcb10data;
		
		//Obter o preço unitário para compor Bcb11unit
		def unit = buscarUnitarioLancamento(bcb11.bcb11item.abm01id, bcb10data);
		bcb11.bcb11unit = unit;
		
		//Calculando o preço total para compor Bcb11total
		def total = bcb11.bcb11qtde * bcb11.bcb11unit;
		bcb11.bcb11total = round(total, 2);
		
		//Compondo os campos Bcb11json
		TableMap mapJson = bcb11.bcb11json == null ? new TableMap() : bcb11.bcb11json;
		
		mapJson.put("inv_ir", bcb11.bcb11total);
		mapJson.put("inv_bc_icms", 0);
		mapJson.put("inv_icms", 0);
		mapJson.put("inv_cst_icms", "");
		mapJson.put("inv_st_bc_icms", 0);
		mapJson.put("inv_st_icms", 0);
		mapJson.put("inv_st_op", 0);
		mapJson.put("inv_st_fcp", 0);

		//Compondo o saldo da conta na data do inventário
		def saldoGrupo = buscarSaldoDoGrupo(bcb11.bcb11grupo.abm40id);
		
		// Verifica se o campo livre 'inv_est_sdo_cta' ja possui o saldo do grupo
		if(saldoGrupo.equals(new BigDecimal(0))) {
			mapJson.put("inv_est_sdo_cta", buscarSaldoConta(bcb10data, bcb11.bcb11grupo));
		}else {
			mapJson.put("inv_est_sdo_cta", 0);
		}
	}

	public BigDecimal buscarUnitarioLancamento(Long bcb11item, LocalDate bcb10data) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		def data = bcb10data.format(formatter)
		
		def unit = getSession().createCriteria(Bcc01.class)
									  .addFields("bcc01pmu")
									  .addWhere(Criterions.where("bcc01data <= '"+ data + "'"))
									  .addWhere(Criterions.eq("bcc01item", bcb11item))
									  .addWhere(getSamWhere().getCritPadrao(Bcc01.class))
									  .setOrder("bcc01id DESC")
									  .setMaxResults(1)
									  .get(ColumnType.BIG_DECIMAL);
												
		return unit != null ? unit : BigDecimal.ZERO;
	}
	
	public BigDecimal buscarSaldoConta(LocalDate bcb10data, Abm40 abm40) {
		def mes = bcb10data.getMonthValue();
		def ano = bcb10data.getYear();
				
		if(abm40.abm40cta == null) return BigDecimal.ZERO;
		
		BigDecimal saldo = getSession().createQuery(" SELECT ebb02saldo FROM Ebb02",
										" WHERE ebb02cta = :abc10id ",
										" AND ", Fields.numMeses("ebb02mes", "ebb02ano"), " <= :numMeses",
										getSamWhere().getWherePadrao("AND", Ebb02.class),
										" ORDER BY ebb02ano DESC, ebb02mes DESC")
										.setParameters("numMeses", Criterions.valNumMeses(mes, ano), "abc10id", abm40.abm40cta.abc10id)
										.setMaxResult(1)
										.getUniqueResult(ColumnType.BIG_DECIMAL);

		return saldo != null ? saldo : BigDecimal.ZERO;
	}
	
	public BigDecimal buscarSaldoDoGrupo(Long idGrupo) {
		def sql = "SELECT COALESCE(SUM((bcb11json ->> 'inv_est_sdo_cta')::NUMERIC), 0) FROM Bcb11 WHERE bcb11grupo = :idGrupo";
		return getAcessoAoBanco().obterBigDecimal(sql, Parametro.criar("idGrupo", idGrupo));
	}

	@Override
	public FormulaTipo obterTipoFormula() {
		return FormulaTipo.INVENTARIO;
	}
}
//meta-sis-eyJ0aXBvIjoiZm9ybXVsYSIsImZvcm11bGF0aXBvIjoiNTQifQ==